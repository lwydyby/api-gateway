package com.loopswork.loops.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.SimpleResponse;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.plugin.impl.LogPlugin;
import com.loopswork.loops.util.ConsoleLogger;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * @author codi
 * @description 全局异常处理器
 * @date 2020/1/20 10:02 上午
 */
@Singleton
public class GlobalErrorHandler implements Handler<RoutingContext> {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  @Inject
  private ConsoleLogger consoleLogger;
  @Inject
  private LogPlugin logPlugin;

  @Override
  public void handle(RoutingContext context) {
    context.response().bodyEndHandler(var1 -> logPlugin.handle(context));
    if (context.failure() instanceof RouterException) {
      RouterException routerException = (RouterException) context.failure();
      //记录日志
      consoleLogger.log(context, routerException.getCode());
      //处理逻辑跳出异常
      SimpleResponse simpleResponse = SimpleResponse.fromException(routerException);
      context.response()
        .putHeader("Content-Type", "application/json;charset=UTF-8")
        .setStatusCode(routerException.getStatus()).end(Json.encodePrettily(simpleResponse));
    } else {
      //未处理异常
      log.error("error", context.failure());
      //记录日志
      consoleLogger.log(context, RouterCode.INTERNAL_ERROR.getCode());
      context.response()
        .putHeader("Content-Type", "application/json;charset=UTF-8")
        .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(Json.encodePrettily(new SimpleResponse(RouterCode.INTERNAL_ERROR)));
    }
  }
}
