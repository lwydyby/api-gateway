package com.loopswork.loops.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.ContextKeys;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.http.entity.HttpResponse;
import com.loopswork.loops.http.wraper.IHttpResponseWrapper;
import com.loopswork.loops.plugin.impl.LogPlugin;
import com.loopswork.loops.util.ConsoleLogger;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @author codi
 * @description 返回处理器
 * @date 2020/1/17 2:23 下午
 */
@Singleton
public class ReturnHandler implements Handler<RoutingContext> {
  @Inject
  private IHttpResponseWrapper responseAdaptor;
  @Inject
  private ConsoleLogger consoleLogger;
  @Inject
  private LogPlugin logPlugin;

  @Override
  public void handle(RoutingContext context) {
    HttpResponse response = context.get(ContextKeys.HTTP_RESPONSE);
    HttpServerResponse httpServerResponse = context.response();
    //记录日志
    consoleLogger.log(context, RouterCode.SUCCESS.getCode());
    httpServerResponse.bodyEndHandler(var1 -> logPlugin.handle(context));
    //处理数据
    responseAdaptor.response(response, httpServerResponse);
    //将需要返回的body转化为buffer
    if (response.getBody() != null) {
      Buffer body = Buffer.buffer(response.getBody().getByteBuf());
      httpServerResponse.end(body);
    } else {
      httpServerResponse.end();
    }
  }
}
