package com.loopswork.loops.admin.collector.impl;

import com.google.inject.Singleton;
import com.loopswork.loops.admin.collector.ICollector;
import com.loopswork.loops.admin.entity.EntityType;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.entity.SimpleCode;
import com.loopswork.loops.exception.SimpleException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author codi
 * @date 2020/3/17 11:57 下午
 * @description Mock配置加载器
 */
@Singleton
public class MockCollector implements ICollector {
  private Map<EntityType, List<LoopsEntity>> entityMap = new HashMap<>();

  public void setEntityMap(Map<EntityType, List<LoopsEntity>> entityMap) {
    this.entityMap = entityMap;
  }

  @Override
  public void load(Handler<AsyncResult<Boolean>> handler) {
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
}
