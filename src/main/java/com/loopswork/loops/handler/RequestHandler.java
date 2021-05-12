package com.loopswork.loops.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.ContextKeys;
import com.loopswork.loops.http.adaptor.IHttpRequestAdaptor;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.util.StringUtils;
import com.loopswork.loops.util.UUIDUtil;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * @author codi
 * @description 请求处理器
 * @date 2020/1/20 9:57 上午
 */
@Singleton
public class RequestHandler implements Handler<RoutingContext> {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  @SuppressWarnings("rawtypes")
  @Inject
  private IHttpRequestAdaptor httpRequestAdaptor;

  @SuppressWarnings("unchecked")
  @Override
  public void handle(RoutingContext context) {
    log.trace("Request handler begin");
    //设置唯一标示
    context.put(ContextKeys.ID, UUIDUtil.getUUID());
    context.put(ContextKeys.TIME_START, System.currentTimeMillis());
    //处理request数据
    HttpRequest request = httpRequestAdaptor.request(context);
    //设置Host
    String host = request.getHeaders().get("Host");
    if (!StringUtils.isEmpty(host)) {
      //header中存在host 则使用header中的host作为匹配依据
      request.setHost(host);
    }
    //放入上下文
    context.put(ContextKeys.HTTP_REQUEST, request);
    log.trace("request >> " + request.toString());
    context.next();
  }
}
