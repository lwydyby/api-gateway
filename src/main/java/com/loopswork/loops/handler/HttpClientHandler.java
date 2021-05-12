package com.loopswork.loops.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.*;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.http.adaptor.VertHttpResponseAdaptor;
import com.loopswork.loops.http.entity.BodyBuffer;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.http.entity.HttpResponse;
import com.loopswork.loops.http.wraper.VertHttpRequestWrapper;
import com.loopswork.loops.manager.ClientManager;
import com.loopswork.loops.manager.UpstreamManager;
import com.loopswork.loops.util.TargetUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;

/**
 * @author codi
 * @description HTTP转发处理器
 * @date 2020/1/20 10:01 上午
 */
@Singleton
public class HttpClientHandler implements Handler<RoutingContext> {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  @Inject
  private UpstreamManager upstreamManager;
  @Inject
  private ClientManager clientManager;
  @Inject
  private LoopsConfig loopsConfig;
  @Inject
  private TargetUtil targetUtil;

  @Override
  public void handle(RoutingContext context) {
    //提取httpRequest
    HttpRequest request = context.get(ContextKeys.HTTP_REQUEST);
    log.trace("Starting to handle request {}", request);
    MatchResult matchResult = context.get(ContextKeys.MATCH_RESULT);
    try {
      send(request, matchResult, context, 0);
    } catch (RouterException e) {
      log.error("fail router", e);
      context.fail(e);
    }
  }

  private void send(HttpRequest request, MatchResult matchResult, RoutingContext context, int tries) throws RouterException {
    //整理发送目标信息
    TargetInfo targetInfo = targetUtil.getTargetInfo(request, matchResult);
    context.put(ContextKeys.TARGET_INFO, targetInfo);
    log.trace("target info is {}", targetInfo);
    SocketAddress socketAddress = SocketAddress.inetSocketAddress(targetInfo.getPort(), targetInfo.getHost());
    //整理成需要发送的request
    io.vertx.ext.web.client.HttpRequest<Buffer> vertRequest = webClient().request(HttpMethod.GET, socketAddress, "");
    VertHttpRequestWrapper.request(request, targetInfo, vertRequest);
    //转发请求
    if (checkFormData(request)) {
      vertRequest.sendForm(MultiMap.caseInsensitiveMultiMap().addAll(request.getParams()), ar ->
        asSend(ar, request, matchResult, context, tries, targetInfo));
    } else {
      vertRequest.sendBuffer(wrapBody(request.getBody()), ar ->
        asSend(ar, request, matchResult, context, tries, targetInfo));
    }
  }

  private boolean checkFormData(HttpRequest request) {
    return (request.getHeaders().get("Content-Type") != null && request.getHeaders().get("Content-Type").contains("form-data")) ||
      (request.getHeaders().get("content-type") != null && request.getHeaders().get("content-type").contains("form-data"));
  }

  private void asSend(AsyncResult<io.vertx.ext.web.client.HttpResponse<Buffer>> ar, HttpRequest request, MatchResult matchResult, RoutingContext context, int tries, TargetInfo targetInfo) {
    if (ar.succeeded()) {
      //请求成功
      //整理成httpResponse
      log.trace("Request succeeds in http client handler");
      HttpResponse response = VertHttpResponseAdaptor.response(ar.result());
      //负载均衡请求时 记录返回码
      if (targetInfo.getTargetType() == TargetType.TARGET) {
        upstreamManager.addPassiveResponseStatus(targetInfo.getUpstreamId(), targetInfo.getTargetId(),
          response.getStatusCode());
      }
      context.put(ContextKeys.HTTP_RESPONSE, response);
      context.next();
    } else {
      //请求失败
      log.trace("Request error in http client handler", ar.cause());
      if (targetInfo.getTargetType() == TargetType.TARGET && tries < loopsConfig.getBalancerRetry()) {
        log.trace("Target error, try other targets.");
        //负载均衡上游请求失败 目标被动失败计数
        upstreamManager.addTargetStateCount(targetInfo.getUpstreamId(), targetInfo.getTargetId(), HealthType.PASSIVE,
          HealthStatus.UNHEALTHY);
        //少于重试次数时 切换目标重试
        try {
          send(request, matchResult, context, tries + 1);
        } catch (RouterException e) {
          context.fail(e);
        }
      } else {
        log.debug("All targets error!");
        context.fail(RouterException.e(RouterCode.REQUEST_ERROR, ar.cause()));
      }
    }
  }

  private WebClient webClient() {
    return clientManager.getCurrentThreadWebClient();
  }

  private Buffer wrapBody(BodyBuffer body) {
    return Buffer.buffer(body.getByteBuf());
  }

}
