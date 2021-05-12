package com.loopswork.loops.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.*;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.manager.balancer.IBalancer;
import com.loopswork.loops.manager.balancer.impl.EmptyBalancer;
import com.loopswork.loops.manager.balancer.impl.ErrorBalancer;
import com.loopswork.loops.manager.balancer.impl.LvsBalancer;
import com.loopswork.loops.manager.balancer.impl.WeightedRoundRobinBalancer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author codi
 * @description 上游服务
 * @date 2019-08-06 14:47
 */
@Singleton
public class UpstreamManager implements IDataManager {
  private static final Logger log = LoggerFactory.getLogger(UpstreamManager.class);
  @Inject
  private Managers managers;
  private Map<String, Upstream> upstreamMap = new HashMap<>();
  private Map<String, Map<String, Target>> targetMap = new HashMap<>();
  private Map<String, IBalancer> balancerMap = new HashMap<>();

  @Override
  public void init() {
    load();
  }

  @Override
  public void update() {
    load();
  }

  public Upstream getUpstream(String name) {
    return upstreamMap.get(name);
  }

  public Map<String, Upstream> getUpstreamMap() {
    return upstreamMap;
  }

  public Map<String, Target> getUpstreamTargetMap(String upstreamId) {
    return targetMap.get(upstreamId);
  }

  public Map<String, Map<String, Target>> getTargetMap() {
    return targetMap;
  }

  public Upstream getUpstreamById(String id) {
    Optional<Upstream> exist = upstreamMap.values().stream().filter(upstream -> id.equals(upstream.getId())).findFirst();
    return exist.orElse(null);
  }

  public Target getTarget(String upstreamId, String targetId) {
    Map<String, Target> targets = targetMap.get(upstreamId);
    if (targets != null) {
      return targets.get(targetId);
    } else {
      return null;
    }
  }

  public Target balance(String name, HttpRequest request) throws RouterException {
    IBalancer balancer = balancerMap.get(name);
    return balancer.balance(request);
  }

  public void addPassiveResponseStatus(String upstreamId, String targetId, int statusCode) {
    AtomicBoolean fail = new AtomicBoolean(false);
    Upstream upstream = getUpstreamById(upstreamId);
    Optional.of(upstream)
      .map(Upstream::getPassiveHealthCheck)
      .map(HealthCheck::getUnhealthyCondition)
      .map(HealthCondition::getHttpStatuses)
      .ifPresent(list -> {
        if (list.stream().anyMatch(status -> status.equals(statusCode))) {
          fail.set(true);
        }
      });
    if (fail.get()) {
      //命中失败statusCode 记录失败
      addTargetStateCount(upstreamId, targetId, HealthType.PASSIVE, HealthStatus.UNHEALTHY);
    }
  }

  /**
   * 更新目标健康状态计数
   *
   * @param upstreamId 上游服务id
   * @param targetId   目标id
   * @param type       检查类型: 主动 被动
   * @param status     检查结果: 健康 不健康
   */
  public void addTargetStateCount(String upstreamId, String targetId, HealthType type, HealthStatus status) {
    Upstream upstream = getUpstreamById(upstreamId);
    Target target = getTarget(upstreamId, targetId);
    if (upstream != null && target != null) {
      boolean stateChanged = addTargetStateCount(upstream, target, type, status);
      if (stateChanged) {
        //监测到状态变化 重建负载均衡器
        reBuildBalancer(upstream);
      }
    }
  }

  /**
   * 更新目标健康状态计数
   *
   * @param upstream 上游服务
   * @param target   目标
   * @param type     检查类型: 主动 被动
   * @param status   检查结果: 健康 不健康
   * @return 目标状态是否变化
   */
  public boolean addTargetStateCount(Upstream upstream, Target target, HealthType type, HealthStatus status) {
    if (type == HealthType.ACTIVE) {
      if (upstream.getActiveHealthCheck().getEnable()) {
        if (status == HealthStatus.HEALTHY) {
          //主动检查健康
          int count = target.getActiveState().getSuccesses().incrementAndGet();
          if (count >= upstream.getActiveHealthCheck().getHealthyCondition().getSuccesses()) {
            changeTargetState(upstream, target, type, status);
            return true;
          }
        }
        if (status == HealthStatus.UNHEALTHY) {
          //主动检查不健康
          int count = target.getActiveState().getFailures().incrementAndGet();
          if (count >= upstream.getActiveHealthCheck().getUnhealthyCondition().getFailures()) {
            changeTargetState(upstream, target, type, status);
            return true;
          }
        }
      }
    }
    if (type == HealthType.PASSIVE) {
      if (upstream.getPassiveHealthCheck().getEnable()||upstream.isLvs()) {
        if (status == HealthStatus.HEALTHY) {
          //被动检查健康
          int count = target.getPassiveState().getSuccesses().incrementAndGet();
          if (count >= upstream.getPassiveHealthCheck().getHealthyCondition().getSuccesses()||upstream.isLvs()) {
            changeTargetState(upstream, target, type, status);
            return true;
          }
        }
        if (status == HealthStatus.UNHEALTHY) {
          //被动检查不健康
          int count = target.getPassiveState().getFailures().incrementAndGet();
          if (count >= upstream.getPassiveHealthCheck().getUnhealthyCondition().getFailures()||upstream.isLvs()) {
            changeTargetState(upstream, target, type, status);
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * 切换目标健康状态
   *
   * @param upstream 上游服务
   * @param target   目标
   * @param type     检查类型: 主动 被动
   * @param status   检查结果: 健康 不健康
   */
  public void changeTargetState(Upstream upstream, Target target, HealthType type, HealthStatus status) {
    if (type == HealthType.ACTIVE) {
      if (status == HealthStatus.HEALTHY) {
        //主动 健康
        target.getActiveState().getFailures().set(0);
        target.getActiveState().getSuccesses().set(0);
        target.setHealth(HealthStatus.HEALTHY);
      }
      if (status == HealthStatus.UNHEALTHY) {
        //主动 不健康
        target.getActiveState().getFailures().set(0);
        target.getActiveState().getSuccesses().set(0);
        target.setHealth(HealthStatus.UNHEALTHY);
      }
    }
    if (type == HealthType.PASSIVE) {
      if (status == HealthStatus.HEALTHY) {
        //被动 健康
        target.getPassiveState().getFailures().set(0);
        target.getPassiveState().getSuccesses().set(0);
        target.setHealth(HealthStatus.HEALTHY);
      }
      if (status == HealthStatus.UNHEALTHY) {
        //被动 不健康
        target.getPassiveState().getFailures().set(0);
        target.getPassiveState().getSuccesses().set(0);
        target.setHealth(HealthStatus.UNHEALTHY);
      }
    }
    log.info("Upstream [{}] target [{}:{}] {} status change to [{}]", upstream.getName(), target.getHost(), target.getPort(), type,
      status);
  }

  /**
   * 重建负载均衡器
   *
   * @param upstream 上游服务
   */
  public void reBuildBalancer(Upstream upstream) {
    IBalancer balancer = createBalancer(upstream);
    balancerMap.put(upstream.getName(), balancer);
    log.debug("Rebuild balancer success. upstream name: [{}] balancer: [{}] targetCounts: [{}]", upstream.getName(),
      balancer.getClass().getSimpleName(), balancer.targetCount());
  }

  private void load() {
    List<Upstream> upstreams = new ArrayList<>(managers.getEntityList(Upstream.class));
    upstreams = upstreams.stream().filter(Upstream::isEnable).collect(Collectors.toList());
    loadUpstreams(upstreams);
    List<Target> targets = new ArrayList<>(managers.getEntityList(Target.class));
    loadTargets(targets);
    initBalancer();
  }

  private IBalancer createBalancer(Upstream upstream) {
    Map<String, Target> targets = targetMap.get(upstream.getId());
    IBalancer balancer = createBalancer(upstream, targets);
    log.debug("Balancer init success. upstream name: [{}] balancer: [{}] targetCounts: [{}]", upstream.getName(),
      balancer.getClass().getSimpleName(), balancer.targetCount());
    return balancer;
  }

  private IBalancer createBalancer(Upstream upstream, Map<String, Target> targets) {
    if (targets == null || targets.size() == 0) {
      //无目标 返回空均衡器
      return new EmptyBalancer();
    }
    //筛选健康节点
    Map<String, Target> healthTargets = targets.entrySet().stream()
      .filter(entry -> entry.getValue().getHealth() == HealthStatus.HEALTHY)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    if (healthTargets.size() == 0) {
      //无健康目标 创建异常均衡器
      return new ErrorBalancer();
    }
    //生成负载均衡器
    IBalancer balancer = null;
    if (upstream.isLvs()){
      balancer=new LvsBalancer();
      balancer.init(healthTargets,0);
      return balancer;
    }
    if (upstream.getHashOn() == null) {
      upstream.setHashOn(HashType.NONE);
    }
    switch (upstream.getHashOn()) {
      case NONE:
        balancer = new WeightedRoundRobinBalancer();
        balancer.init(healthTargets, upstream.getSlots());
        break;
      case HEADER:
        //TODO 请求头哈希散列
        break;
    }
    return balancer;
  }

  private void loadUpstreams(List<Upstream> upstreams) {
    Map<String, Upstream> nameMap = new HashMap<>();
    for (Upstream upstream : upstreams) {
      nameMap.put(upstream.getName(), upstream);
    }
    this.upstreamMap = nameMap;
  }

  private synchronized void loadTargets(List<Target> targets) {
    Map<String, Map<String, Target>> targetMap = new HashMap<>();
    //整理目标
    for (Target target : targets) {
      //初始化信息
      target.setHealth(HealthStatus.HEALTHY);
      target.setPassiveState(new HealthState());
      target.setActiveState(new HealthState());
      //加入map
      String upstreamId = target.getUpstreamId();
      targetMap.computeIfAbsent(upstreamId, k -> new HashMap<>());
      targetMap.get(upstreamId).put(target.getId(), target);
    }
    this.targetMap = targetMap;
  }

  private synchronized void initBalancer() {
    log.debug("Init upstream balancer.");
    Map<String, IBalancer> balancerMap = new HashMap<>();
    //循环上游
    for (Upstream upstream : upstreamMap.values()) {
      //为每一个上游服务整理目标并创建负载均衡器
      IBalancer balancer = createBalancer(upstream);
      balancerMap.put(upstream.getName(), balancer);
    }
    this.balancerMap = balancerMap;
    log.debug("Init upstream balancer success.");
  }

}
