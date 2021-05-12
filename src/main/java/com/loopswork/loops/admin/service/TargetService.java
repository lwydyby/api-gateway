package com.loopswork.loops.admin.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.admin.handler.RequestBodyHandler;
import com.loopswork.loops.entity.HttpMethod;
import com.loopswork.loops.entity.Route;
import com.loopswork.loops.entity.Target;
import com.loopswork.loops.entity.Upstream;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.loopswork.loops.entity.SimpleCode.TARGET_HOST_CANT_EMPTY;

/**
 * @author codi
 * @date 2020/3/24 3:22 下午
 * @description 目标服务
 */
@Singleton
public class TargetService extends LoopsEntityService {

  @Inject
  private Managers managers;

  public void list(String nameOrId, Handler<AsyncResult<Collection<Target>>> resultHandler) {
    Upstream upstream = managers.getEntity(Upstream.class, nameOrId);
    if (upstream != null) {
      List<Target> list = managers.getEntityList(Target.class);
      List<Target> result = list.stream().filter(entity -> upstream.getId().equals(entity.getUpstreamId()) || upstream.getName().equals(entity.getUpstreamId())).collect(Collectors.toList());
      resultHandler.handle(Future.succeededFuture(result));
    } else {
      resultHandler.handle(Future.succeededFuture(Collections.emptyList()));
    }
  }

  @Override
  public <T extends LoopsEntity, R> void add(RoutingContext context, Class<R> requestClazz, Class<T> entityClazz, Handler<AsyncResult<Void>> handler) {
    if (targetRequestBodyCheck(context)){
      super.add(context, requestClazz, entityClazz, handler);
    } else {
      handler.handle(Future.failedFuture(new SimpleException(TARGET_HOST_CANT_EMPTY)));
    }
  }

  @Override
  public <T extends LoopsEntity, R> void update(RoutingContext context, String nameOrId, Class<R> requestClazz, Class<T> entityClazz, Handler<AsyncResult<Void>> handler) {
    if (targetRequestBodyCheck(context)){
      super.update(context, nameOrId, requestClazz, entityClazz, handler);    } else {
      handler.handle(Future.failedFuture(new SimpleException(TARGET_HOST_CANT_EMPTY)));
    }
  }

  public boolean targetRequestBodyCheck(RoutingContext context){
    Target targetEntity = context.get(RequestBodyHandler.REQUEST_BODY_KEY);
    String host = targetEntity.getHost();
    return !host.trim().equals("");
  }

}
