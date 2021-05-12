package com.loopswork.loops.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.collector.ICollector;
import com.loopswork.loops.admin.entity.EntityType;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.entity.*;
import com.loopswork.loops.exception.LoopsException;
import com.loopswork.loops.util.StringUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.*;

/**
 * @author codi
 * @title: ServiceManager
 * @projectName loops
 * @description: 配置管理器
 * @date 2020/1/21 2:57 下午
 */
@Singleton
public class Managers {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject
  private ICollector collector;
  @Inject
  private Set<IDataManager> dataManagers;
  private RouterState state = RouterState.PREPARING;
  private Map<EntityType, List<LoopsEntity>> entityMap;

  public RouterState getState() {
    return state;
  }

  public void init(Handler<AsyncResult<Long>> initHandler) {
    log.info("Managers start to init");
    collector.initGeneration(ar -> initInterval(initHandler));
  }

  public void update(Handler<AsyncResult<Long>> initHandler) {
    log.info("Managers start to update");
    initInterval(initHandler);
  }

  /**
   * 加载数据完成初始化
   */
  private synchronized void initInterval(Handler<AsyncResult<Long>> initHandler) {
    changeState(RouterState.PREPARING);
    long startTime = System.currentTimeMillis();
    loadData(handler -> {
      if (handler.succeeded()) {
        //整理数据
        initData();
        //初始化数据管理器
        initDataManager();
        //切换状态为可用
        changeState(RouterState.ACTIVE);
        //记录时间
        long time = System.currentTimeMillis() - startTime;
        initHandler.handle(Future.succeededFuture(time));
      } else {
        //切换状态为异常
        changeState(RouterState.ERROR);
        initHandler.handle(Future.failedFuture(handler.cause()));
      }
    });
  }

  private void loadData(Handler<AsyncResult<Void>> handler) {
    //调用加载器
    log.info("Collector is [{}]", collector.getClass().getSimpleName());
    Future.<Boolean>future(h -> {
      collector.load(h);
    }).onSuccess(result -> {
      //加载成功 存储数据
      this.entityMap = new HashMap<>(collector.getEntity());
      log.info("Managers data load success");
      StringBuilder countLog = new StringBuilder();
      for (EntityType type : EntityType.values()) {
        String name = type.name();
        List<LoopsEntity> list = this.entityMap.computeIfAbsent(type, k -> new ArrayList<>());
        countLog.append(name).append(" ");
        countLog.append(list.size()).append(" ");
      }
      log.info(countLog.toString());
      handler.handle(Future.succeededFuture());
    }).onFailure(result -> {
      log.error("Managers data load error ", result);
      handler.handle(Future.failedFuture(result));
    });
  }

  /**
   * 整理数据
   */
  private void initData() {
    //TODO 填充关联对象 此处使用硬编码处理 可优化为通用逻辑
    this.entityMap.get(EntityType.route).forEach(v -> {
      Server server = getEntity(Server.class, ((Route) v).getServerId());
      ((Route) v).setServer(server);
      if (server != null) ((Route) v).setServerId(server.getId());
    });
    this.entityMap.get(EntityType.plugin).forEach(v -> {
      Server server = getEntity(Server.class, ((Plugin) v).getServerId());
      Route route = getEntity(Route.class, ((Plugin) v).getRouteId());
      Consumer consumer = getEntity(Consumer.class, ((Plugin) v).getConsumerId());
      ((Plugin) v).setServer(server);
      if (server != null) ((Plugin) v).setServerId(server.getId());
      ((Plugin) v).setRoute(route);
      if (route != null) ((Plugin) v).setRouteId(route.getId());
      ((Plugin) v).setConsumer(consumer);
      if (consumer != null) ((Plugin) v).setConsumerId(consumer.getId());
    });
    this.entityMap.get(EntityType.acl).forEach(v -> {
      Consumer consumer = getEntity(Consumer.class, ((ACL) v).getConsumerId());
      ((ACL) v).setConsumer(consumer);
      if (consumer != null) ((ACL) v).setConsumerId(consumer.getId());
    });
    this.entityMap.get(EntityType.key_auth).forEach(v -> {
      Consumer consumer = getEntity(Consumer.class, ((KeyAuthCredentials) v).getConsumerId());
      ((KeyAuthCredentials) v).setConsumer(consumer);
      if (consumer != null) ((KeyAuthCredentials) v).setConsumerId(consumer.getId());
    });
    this.entityMap.get(EntityType.target).forEach(v ->
      ((Target) v).setUpstreamId(getEntity(Upstream.class, ((Target) v).getUpstreamId()).getId()));
  }

  /**
   * 初始化数据管理器
   */
  private void initDataManager() {
    dataManagers.forEach(IDataManager::init);
    log.info("Data managers init success");
  }

  private void changeState(RouterState state) {
    if (state == RouterState.ERROR) {
      log.error("Router state change [{}] --> [{}]", this.state, state);
    } else {
      log.info("Router state change [{}] --> [{}]", this.state, state);
    }
    this.state = state;
  }

  public <T extends LoopsEntity> T getEntity(Class<T> clazz, String nameOrId) {
    EntityType type = EntityType.get(clazz);
    List<LoopsEntity> list = entityMap.get(type);
    if (StringUtils.isEmpty(nameOrId)) {
      return null;
    }
    if (list != null) {
      Optional<LoopsEntity> result = list.stream().filter(entity -> nameOrId.equals(entity.getId()) || nameOrId.equals(entity.getName())).findFirst();
      return clazz.cast(result.orElseThrow(() ->
        new LoopsException(String.format("%s name or id Can't find association target, nameOrId: %s", clazz.getSimpleName(), nameOrId))));
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends LoopsEntity> List<T> getEntityList(Class<T> clazz) {
    EntityType type = EntityType.get(clazz);
    return (List<T>) entityMap.get(type);
  }

  public void addEntity(LoopsEntity entity, Handler<AsyncResult<Void>> handler) {
    collector.addEntity(entity, handler);
  }

  public void updateEntity(LoopsEntity entity, String nameOrId, Handler<AsyncResult<Void>> handler) {
    collector.updateEntity(entity, nameOrId, handler);
  }

  public <T extends LoopsEntity> void removeEntity(Class<T> clazz, String nameOrId, Handler<AsyncResult<Void>> handler) {
    EntityType type = EntityType.get(clazz);
    collector.removeEntity(type, nameOrId, handler);
  }

}
