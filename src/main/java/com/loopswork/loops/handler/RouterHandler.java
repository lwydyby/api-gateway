package com.loopswork.loops.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.ContextKeys;
import com.loopswork.loops.entity.MatchResult;
import com.loopswork.loops.entity.MatchState;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.manager.RouterManager;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * @author codi
 * @description 路由处理器
 * @date 2020/1/20 10:00 上午
 */
@Singleton
public class RouterHandler implements Handler<RoutingContext> {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  @Inject
  private RouterManager routerManager;

  @Override
  public void handle(RoutingContext context) {
    HttpRequest request = context.get(ContextKeys.HTTP_REQUEST);
    //路由匹配
    MatchResult matchResult = routerManager.matchHttpRequest(request, false);
    context.put(ContextKeys.MATCH_RESULT, matchResult);
    if (matchResult.getMatchState() == MatchState.MATCHED) {
      //匹配成功 保存匹配结果
      log.trace("Route matched serverId:" + matchResult.getMatchRouter().getServer().getId());
      log.trace("Route matched routerId:" + matchResult.getMatchRouter().getRouter().getRoute().getId());
      context.next();
    } else if (matchResult.getMatchState() == MatchState.ERROR) {
      context.fail(RouterException.e(RouterCode.ROUTER_ERROR));
    } else {
      //匹配失败
      log.trace("No Route matched");
      context.fail(RouterException.e(RouterCode.NO_ROUTE_MATCHED));
    }
  }
}
