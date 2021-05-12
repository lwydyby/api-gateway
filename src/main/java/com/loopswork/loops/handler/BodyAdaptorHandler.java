package com.loopswork.loops.handler;

import com.google.inject.Singleton;
import com.loopswork.loops.entity.ContextKeys;
import com.loopswork.loops.http.adaptor.VertHttpRequestAdaptor;
import com.loopswork.loops.http.entity.HttpRequest;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * @author codi
 * @description body处理器
 * @date 2020-03-31 15:22
 */
@Singleton
public class BodyAdaptorHandler implements Handler<RoutingContext> {

  @Override
  public void handle(RoutingContext context) {
    HttpRequest request = context.get(ContextKeys.HTTP_REQUEST);
    request.setParams(VertHttpRequestAdaptor.adaptParams(context));
    request.setBody(VertHttpRequestAdaptor.adaptBody(context.getBody()));
    context.next();
  }

}
