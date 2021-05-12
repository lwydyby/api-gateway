package com.loopswork.loops.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.MatchResult;
import com.loopswork.loops.entity.MatchState;
import com.loopswork.loops.entity.RouterState;
import com.loopswork.loops.entity.TargetInfo;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.http.entity.HttpResponse;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import static com.loopswork.loops.entity.ContextKeys.*;

/**
 * @author codi
 * @description 命令行日志打印
 * @date 2019-09-26 16:57
 */
@Singleton
public class ConsoleLogger {
  private final Logger log = LoggerFactory.getLogger(ConsoleLogger.class);
  @Inject
  private LoopsConfig loopsConfig;
  @Inject
  private Managers managers;

  public void log(RoutingContext context, int code) {
    if (loopsConfig.isTraceLog() && managers.getState() == RouterState.ACTIVE) {
      //设置打开且状态正常时记录路由日志
      MatchResult matchResult = context.get(MATCH_RESULT);
      MatchState matchState = matchResult.getMatchState();
      String serverName = "";
      String routerName = "";
      String clientIP = "";
      String clientPort = "0";
      String targetHost = "";
      String targetPort = "";
      String requestBodyLength = "0";
      String responseBodyLength = "0";
      String statusCode = "0";
      switch (matchState) {
        case ERROR:
        case NO_MATCH:
          break;
        case MATCHED:
          serverName = String.valueOf(matchResult.getMatchRouter().getServer().getName());
          routerName = String.valueOf(matchResult.getMatchRouter().getRouter().getRoute().getName());
          break;
      }

      HttpRequest request = context.get(HTTP_REQUEST);
      if (request != null) {
        if (request.getBody() != null) {
          requestBodyLength = String.valueOf(request.getBody().length());
        }
        clientIP = request.getClientIP();
        clientPort = String.valueOf(request.getClientPort());
      }
      HttpResponse response = context.get(HTTP_RESPONSE);
      if (response != null) {
        if (response.getBody() != null) {
          responseBodyLength = String.valueOf(response.getBody().length());
        }
        statusCode = String.valueOf(response.getStatusCode());
      }
      int delay = context.get(TIME_DURATION) == null ? 0 : context.get(TIME_DURATION);
      TargetInfo targetInfo = context.get(TARGET_INFO);
      if (targetInfo != null) {
        targetHost = targetInfo.getHost();
        targetPort = String.valueOf(targetInfo.getPort());
      }
      //日志格式 <命中情况> [客户端ip:port] [目标ip:port] 服务名 路由名 请求长度 响应长度 返回码 延时
      log.info("<{}> <{}> [{}:{}] [{}:{}] {} {} {} {} {} {}", matchState, code, clientIP, clientPort, targetHost, targetPort, serverName,
        routerName, requestBodyLength, responseBodyLength, statusCode, delay);
    }
  }

  public void log(MatchResult matchResult, TargetInfo targetInfo, String clientIp) {
    if (loopsConfig.isTraceLog() && managers.getState() == RouterState.ACTIVE) {
      MatchState matchState = matchResult.getMatchState();
      String serverName = "";
      String routerName = "";
      String targetHost = "";
      String targetPort = "";
      switch (matchState) {
        case ERROR:
        case NO_MATCH:
          break;
        case MATCHED:
          serverName = String.valueOf(matchResult.getMatchRouter().getServer().getName());
          routerName = String.valueOf(matchResult.getMatchRouter().getRouter().getRoute().getName());
          targetHost = String.valueOf(matchResult.getMatchRouter().getServer().getHost());
          targetPort = String.valueOf(matchResult.getMatchRouter().getServer().getPort());
          break;
      }
      if (targetInfo != null) {
        targetHost = targetInfo.getHost();
        targetPort = String.valueOf(targetInfo.getPort());
      }
      //日志格式 <命中情况> [客户端ip] [目标ip:port] 服务名 路由名
      log.info("<{}>  [{}] [{}:{}] {} {} ", matchState, clientIp, targetHost, targetPort, serverName,
        routerName);
    }

  }

}
