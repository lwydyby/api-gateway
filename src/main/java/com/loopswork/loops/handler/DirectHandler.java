package com.loopswork.loops.handler;

import com.google.inject.Inject;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.*;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.http.wraper.VertHttpRequestWrapper;
import com.loopswork.loops.manager.ClientManager;
import com.loopswork.loops.manager.UpstreamManager;
import com.loopswork.loops.util.ConsoleLogger;
import com.loopswork.loops.util.TargetUtil;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

import static com.loopswork.loops.entity.HealthStatus.UNHEALTHY;

/**
 * @author liwei
 * @description 直接传输处理器（不处理前置后置插件）
 * @date 2019-12-10 15:08
 */
public class DirectHandler implements Handler<RoutingContext> {
  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Inject
  private UpstreamManager upstreamManager;
  @Inject
  private LoopsConfig loopsConfig;
  @Inject
  private ClientManager clientManager;
  @Inject
  private ConsoleLogger consoleLogger;
  @Inject
  private TargetUtil targetUtil;


  @Override
  public void handle(RoutingContext context) {
    HttpRequest request = context.get(ContextKeys.HTTP_REQUEST);
    log.trace("Starting to handle request {}", request);
    MatchResult matchResult = context.get(ContextKeys.MATCH_RESULT);
    try {
      upload(request, matchResult, context, 0);
    } catch (RouterException e) {
      context.fail(e);
    }
  }

  private void upload(HttpRequest request, MatchResult matchResult, RoutingContext context, int tries) throws RouterException {
    HttpServerRequest req = context.request();
    TargetInfo targetInfo = targetUtil.getTargetInfo(request, matchResult);
    HttpClient client =  clientManager.getCurrentThreadHttpClient();
    HttpClientRequest c_req = client.request(VertHttpRequestWrapper.transMethod(request.getMethod()), SocketAddress.inetSocketAddress(targetInfo.getPort(), targetInfo.getHost())
      , targetInfo.getPort(), targetInfo.getHost(), targetInfo.getRemoteUri(), res -> {
        consoleLogger.log(matchResult, targetInfo, req.uri());
        context.response().setChunked(true);
        context.response().setStatusCode(res.statusCode());
        context.response().headers().setAll(res.headers());
        res.pipeTo(req.response(),ar->{
          if (ar.succeeded()){
            log.trace("direct proxy complete");
//            req.response().end();
          }else {
            context.fail(ar.cause());
          }
        });
        res.exceptionHandler(getThrowableHandler(request, matchResult, context, tries, targetInfo));
      }).exceptionHandler(getThrowableHandler(request, matchResult, context, tries, targetInfo));
    c_req.headers().setAll(context.request().headers());
    c_req.setChunked(true);
    req.pipeTo(c_req,ar->{
      if(ar.succeeded()){
        log.trace("direct proxy complete");
      }else {
        context.fail(ar.cause());
      }
    });
  }

  private Handler<Throwable> getThrowableHandler(HttpRequest request, MatchResult matchResult, RoutingContext context, int tries, TargetInfo targetInfo) {
    return ex -> {
      if (targetInfo.getTargetType() == TargetType.TARGET && tries < loopsConfig.getBalancerRetry()) {
        log.trace("Target error, try other targets.");
        //负载均衡上游请求失败 目标被动失败计数
        upstreamManager.addTargetStateCount(targetInfo.getUpstreamId(), targetInfo.getTargetId(), HealthType.PASSIVE, UNHEALTHY);
        //少于重试次数时 切换目标重试  上传暂时无法重试
//      upload(request, matchResult, context, tries + 1);
        context.fail(ex);

      } else {
        log.debug("All targets error!");
        context.fail(RouterException.e(RouterCode.REQUEST_ERROR, ex));
      }
    };
  }


}
