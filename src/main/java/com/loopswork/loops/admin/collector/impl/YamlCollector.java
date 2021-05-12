package com.loopswork.loops.admin.collector.impl;

import cn.hutool.core.bean.BeanUtil;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.collector.ICollector;
import com.loopswork.loops.admin.entity.EntityType;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.*;
import com.loopswork.loops.exception.CollectorException;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.util.StringUtils;
import com.loopswork.loops.util.UUIDUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author codi
 * @date 2020/3/17 11:37 上午
 * @description YAML配置加载器
 */
@Singleton
public class YamlCollector implements ICollector {
  @Inject
  private Vertx vertx;
  @Inject
  private LoopsConfig loopsConfig;
  private Map<EntityType, List<LoopsEntity>> entityMap;

  @Override
  public void load(Handler<AsyncResult<Boolean>> handler) {
    FileSystem fs = vertx.fileSystem();
    if (StringUtils.isEmpty(loopsConfig.getYamlPath())) {
      handler.handle(Future.failedFuture(new CollectorException("Yaml path is empty please check yaml_path in your config")));
    }
    fs.readFile(loopsConfig.getYamlPath(), h -> {
      if (h.succeeded()) {
        try {
          String string = h.result().toString();
          Yaml yaml = new Yaml();
          YamlSettings result = yaml.loadAs(string, YamlSettings.class);
          initData(result);
          handler.handle(Future.succeededFuture());
        } catch (Throwable t) {
          handler.handle(Future.failedFuture(t));
        }
      } else {
        handler.handle(Future.failedFuture(h.cause()));
      }
    });
  }

  /**
   * 加载配置文件的数据并初始化
   */
  private void initData(YamlSettings settings) {
    entityMap = new HashMap<>();
    entityMap.put(EntityType.server, settings.getServers() == null ? new ArrayList<>() : init(settings.getServers(), Server.class));
    entityMap.put(EntityType.route, settings.getRoutes() == null ? new ArrayList<>() : init(settings.getRoutes(), Route.class));
    entityMap.put(EntityType.consumer, settings.getConsumers() == null ? new ArrayList<>() : init(settings.getConsumers(), Consumer.class));
    entityMap.put(EntityType.plugin, settings.getPlugins() == null ? new ArrayList<>() : init(settings.getPlugins(), Plugin.class));
    entityMap.put(EntityType.upstream, settings.getUpstreams() == null ? new ArrayList<>() : init(settings.getUpstreams(), Upstream.class));
    entityMap.put(EntityType.target, settings.getTargets() == null ? new ArrayList<>() : init(settings.getTargets(), Target.class));
    entityMap.put(EntityType.acl, settings.getAcls() == null ? new ArrayList<>() : init(settings.getAcls(), ACL.class));
    entityMap.put(EntityType.key_auth, settings.getKeyAuthCredentials() == null ? new ArrayList<>() : init(settings.getKeyAuthCredentials(), KeyAuthCredentials.class));
  }

  /**
   * 为所有数据初始化ID 创建时间
   */
  private <T extends LoopsEntity> List<LoopsEntity> init(List<?> data, Class<T> c) {
    return data.stream().map(i -> {
      T entity = BeanUtil.toBean(i, c);
      entity.setId(UUIDUtil.getUUID());
      entity.setCreatedAt(new Date());
      entity.setUpdatedAt(new Date());
      return entity;
    }).collect(Collectors.toList());
  }

  @Override
  public void initGeneration(Handler<AsyncResult<Void>> handler) {
    handler.handle(Future.succeededFuture());
  }

  @Override
  public void checkGeneration(Handler<AsyncResult<Boolean>> handler) {
    handler.handle(Future.succeededFuture(false));
  }

  @Override
  public void updateGeneration(Handler<AsyncResult<Void>> handler) {
    handler.handle(Future.succeededFuture());
  }

  @Override
  public Map<EntityType, List<LoopsEntity>> getEntity() {
    return entityMap;
  }

  @Override
  public void addEntity(LoopsEntity entity, Handler<AsyncResult<Void>> handler) {
    handler.handle(Future.failedFuture(new SimpleException(SimpleCode.UPDATE_NOT_ALLOW)));
  }

  @Override
  public void updateEntity(LoopsEntity entity, String nameOrId, Handler<AsyncResult<Void>> handler) {
    handler.handle(Future.failedFuture(new SimpleException(SimpleCode.UPDATE_NOT_ALLOW)));
  }

  @Override
  public void removeEntity(EntityType type, String nameOrId, Handler<AsyncResult<Void>> handler) {
    handler.handle(Future.failedFuture(new SimpleException(SimpleCode.UPDATE_NOT_ALLOW)));
  }

}
