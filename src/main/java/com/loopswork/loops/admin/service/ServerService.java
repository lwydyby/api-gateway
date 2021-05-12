package com.loopswork.loops.admin.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.collector.ICollector;
import com.loopswork.loops.admin.entity.EntityType;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.entity.Route;
import com.loopswork.loops.entity.Server;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.loopswork.loops.entity.SimpleCode.SERVER_CANT_DELETE;

/**
 * @author  lixiaoxiao
 * @date  2020/5/8 15:45
 */
@Singleton
public class ServerService extends LoopsEntityService{

  @Inject
  private ICollector collector;

  @Override
  public <T extends LoopsEntity> void remove(String nameOrId, Class<T> clazz, Handler<AsyncResult<Void>> handler) {
    List<LoopsEntity> routeList = getRoutesByServer(nameOrId);
    if (routeList != null && routeList.size() > 0) {
      handler.handle(Future.failedFuture(new SimpleException(SERVER_CANT_DELETE)));
    } else {
      super.remove(nameOrId, clazz, handler);
    }
  }

  private List<LoopsEntity> getRoutesByServer(String serverIdOrName) {
    Map<EntityType, List<LoopsEntity>> entityMap =  collector.getEntity();
    //1、判断server是否存在
    List<LoopsEntity> serverList = entityMap.get(EntityType.server).stream()
      .filter(server -> server.getId().equals(serverIdOrName)
        || server.getName().equals(serverIdOrName)).collect(Collectors.toList());
    if (serverList.size() == 0) {
      return null;
    }
    //2、筛选server对应的route
    List<LoopsEntity> routeList = entityMap.get(EntityType.route);
    return routeList.stream()
      .filter(route -> {
        Route r = (Route) route;
        return r.getServer().getId().equals(serverIdOrName) || r.getServer().getName().equals(serverIdOrName);
      }).collect(Collectors.toList());
  }
}
