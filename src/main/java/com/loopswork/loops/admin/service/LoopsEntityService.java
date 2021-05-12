package com.loopswork.loops.admin.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.admin.handler.RequestBodyHandler;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.Collection;
import java.util.List;

/**
 * @author codi
 * @date 2020/3/18 12:26 上午
 * @description 实体类处理服务
 */
@Singleton
public class LoopsEntityService {

  @Inject
  private Managers managers;

  public <T extends LoopsEntity, R> void add(RoutingContext context, Class<R> requestClazz, Class<T> entityClazz, Handler<AsyncResult<Void>> resultHandler) {
    save(context, null, resultHandler);
  }

  public <T extends LoopsEntity> void list(Class<T> clazz, Handler<AsyncResult<Collection<T>>> resultHandler) {
    List<T> entityList = managers.getEntityList(clazz);
    resultHandler.handle(Future.succeededFuture(entityList));
  }

  public <T extends LoopsEntity> void get(String nameOrId, Class<T> clazz, Handler<AsyncResult<LoopsEntity>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(managers.getEntity(clazz, nameOrId)));
  }

  public <T extends LoopsEntity, R> void update(RoutingContext context, String nameOrId, Class<R> requestClazz, Class<T> entityClazz, Handler<AsyncResult<Void>> resultHandler) {
    save(context, nameOrId, resultHandler);
  }

  public <T extends LoopsEntity> void remove(String nameOrId, Class<T> clazz, Handler<AsyncResult<Void>> resultHandler) {
    managers.removeEntity(clazz, nameOrId, resultHandler);
  }

  private void save(RoutingContext context, String nameOrId, Handler<AsyncResult<Void>> resultHandler) {
    LoopsEntity entity = context.get(RequestBodyHandler.REQUEST_BODY_KEY);
    if (nameOrId == null) {
      managers.addEntity(entity, resultHandler);
    } else {
      managers.updateEntity(entity, nameOrId, resultHandler);
    }
  }

}
