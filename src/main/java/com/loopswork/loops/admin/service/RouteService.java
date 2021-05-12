package com.loopswork.loops.admin.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.collector.ICollector;
import com.loopswork.loops.admin.entity.EntityType;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.admin.handler.RequestBodyHandler;
import com.loopswork.loops.entity.HttpMethod;
import com.loopswork.loops.entity.Route;
import com.loopswork.loops.exception.SimpleException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.loopswork.loops.entity.SimpleCode.ROUTE_INFO_ERROR;
import static com.loopswork.loops.entity.SimpleCode.SERVER_CANT_DELETE;

/**
 * @author  lixiaoxiao
 * @date  2020/5/8 15:45
 */
@Singleton
public class RouteService extends LoopsEntityService{

  @Override
  public <T extends LoopsEntity, R> void add(RoutingContext context, Class<R> requestClazz, Class<T> entityClazz, Handler<AsyncResult<Void>> handler) {
    if (routeRequestBodyCheck(context)){
      super.add(context, requestClazz, entityClazz, handler);
    } else {
      handler.handle(Future.failedFuture(new SimpleException(ROUTE_INFO_ERROR)));
    }
  }

  @Override
  public <T extends LoopsEntity, R> void update(RoutingContext context, String nameOrId, Class<R> requestClazz, Class<T> entityClazz, Handler<AsyncResult<Void>> handler) {
    if (routeRequestBodyCheck(context)){
      super.update(context, nameOrId, requestClazz, entityClazz, handler);
    } else {
      handler.handle(Future.failedFuture(new SimpleException(ROUTE_INFO_ERROR)));
    }
  }

  public boolean routeRequestBodyCheck(RoutingContext context){
    Route routeEntity = context.get(RequestBodyHandler.REQUEST_BODY_KEY);
    Set<String> paths = routeEntity.getPaths().stream().filter(path -> !path.trim().equals("")).collect(Collectors.toSet());
    Set<HttpMethod> methods = routeEntity.getMethods();
    Set<String> hosts = routeEntity.getHosts().stream().filter(host -> !host.trim().equals("")).collect(Collectors.toSet());
    if (paths.size() == 0 && hosts.size() == 0) {
      return methods.size() != 0;
    }
    return true;
  }
}
