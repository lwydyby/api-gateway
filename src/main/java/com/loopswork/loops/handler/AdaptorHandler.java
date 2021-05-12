package com.loopswork.loops.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.ContextKeys;
import com.loopswork.loops.entity.MatchResult;
import com.loopswork.loops.entity.Router;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * @author codi
 * @title: AdapterHandler
 * @projectName loops
 * @description: TODO
 * @date 2020/2/18 3:41 下午
 */
@Singleton
public class AdaptorHandler implements Handler<RoutingContext> {
  private BodyHandler bodyHandler;
  @Inject
  private DirectHandler directHandler;

  @Inject
  public void init() {
    bodyHandler = BodyHandler.create();
  }

  @Override
  public void handle(RoutingContext context) {
    MatchResult matchResult = context.get(ContextKeys.MATCH_RESULT);
    Router router = matchResult.getMatchRouter().getRouter();
    if (router.getRoute().isDirect()) {
      //直接转发
      directHandler.handle(context);
    } else {
      bodyHandler.handle(context);
    }
  }

}
