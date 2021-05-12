package com.loopswork.loops.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.RouterState;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * @author codi
 * @description 状态处理器
 * @date 2020/2/14 10:56 上午
 */
@Singleton
public class StateHandler implements Handler<RoutingContext> {
  @Inject
  private Managers managers;

  @Override
  public void handle(RoutingContext context) {
    if (managers.getState() == RouterState.ACTIVE) {
      context.next();
    } else if (managers.getState() == RouterState.PREPARING) {
      context.fail(RouterException.e(RouterCode.ROUTER_NOT_READY));
    } else {
      context.fail(RouterException.e(RouterCode.ROUTER_ERROR));
    }
  }

}
