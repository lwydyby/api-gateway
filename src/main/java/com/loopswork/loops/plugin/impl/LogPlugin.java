package com.loopswork.loops.plugin.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.*;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.http.entity.HttpResponse;
import com.loopswork.loops.manager.PluginManager;
import com.loopswork.loops.util.StringUtils;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author codi
 * @description 日志插件
 * @date 2020/1/19 9:58 上午
 */
@Singleton
public class LogPlugin implements Handler<RoutingContext> {

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private WebClient webClient;

  @Inject
  private PluginManager pluginManager;

  @Inject
  private LoopsConfig loopsConfig;

  @Inject
  private Vertx vertx;

  @Override
  public void handle(RoutingContext context) {
    //记录请求执行结束时间
    long endAt = System.currentTimeMillis();
    context.put(ContextKeys.TIME_END, endAt);
    long startAt = context.get(ContextKeys.TIME_START);
    //计算执行时间差
    long duration = endAt - startAt;
    context.put(ContextKeys.TIME_DURATION, duration);
    //获取插件
    Plugin plugin = this.getPlugin(context);
    if (plugin == null) {
      return;
    }
    Map<String, Object> config = plugin.getConfig();
    //处理Body
    com.loopswork.loops.http.entity.HttpRequest httpRequest = context.get(ContextKeys.HTTP_REQUEST);
    HttpResponse httpResponse = context.get(ContextKeys.HTTP_RESPONSE);
    //处理body
    if (httpRequest != null) {
      if (httpRequest.getBody() != null) {
        httpRequest.setBodyString(httpRequest.getBody().toString());
        httpRequest.setBodyLength(httpRequest.getBody().length());
      }
      httpRequest.setBody(null);
    }
    if (httpResponse != null) {
      if (httpResponse.getBody() != null) {
        //如果response的bodyBuffer的length不超过配置项bodyBufferMaxLength，则可以打印到日志中
        if (httpResponse.getBody().length() <= loopsConfig.getBodyBufferMaxLength()) {
          httpResponse.setBodyString(httpResponse.getBody().toString());
        }
        httpResponse.setBodyLength(httpResponse.getBody().length());
      }
      httpResponse.setBody(null);
    }
    //转换成Json
    JsonObject requestJson = JsonObject.mapFrom(context.data());
    log.trace(requestJson.toString());
    //构建ES请求
    HttpRequest<Buffer> request = createRequest(config, requestJson);
    //发送ES请求
    sendESRequest(request, requestJson);
  }

  private void sendESRequest(HttpRequest<Buffer> request, JsonObject requestJson) {
    String id = requestJson.getString(ContextKeys.ID);
    request.sendJsonObject(requestJson, ar -> {
      if (ar.failed()) {
        log.error("[request: {}] Sending log information to log server is failed. The reason is {} ", id, ar.cause().getMessage());
      } else {
        io.vertx.ext.web.client.HttpResponse<Buffer> result = ar.result();
        // 对于4**(客户端错误)和5**(服务端错误),打印出返回的错误信息。
        if (result.statusCode() >= 400) {
          log.error("[request: {}] Sending log information to log server is failed. The reason is {}.", id,
            result.bodyAsString());
        } else {
          log.debug("[request: {}] Sending log information to log server is successful.", id);
        }
      }
    });
  }

  private Plugin getPlugin(RoutingContext context) {
    // 从上下文中获取该请求匹配到的路由的信息
    MatchResult matchResult = context.get(ContextKeys.MATCH_RESULT);
    if (matchResult == null || matchResult.getMatchState().equals(MatchState.NO_MATCH)) {
      return null;
    } else {
        Route route = matchResult.getMatchRouter().getRouter().getRoute();
        return pluginManager.getPlugin(this.getName(), route.getId(), route.getServerId(), context.get(ContextKeys.CONSUMER_ID));
    }
  }

  private HttpRequest<Buffer> createRequest(Map<String, Object> config, JsonObject requestJson) {
    String id = requestJson.getString(ContextKeys.ID);
    RequestOptions options = new RequestOptions();
    String host = Optional.ofNullable(config.get("elasticsearch_host"))
      .map(String::valueOf)
      .orElse("127.0.0.1");
    int port = Optional.ofNullable(config.get("elasticsearch_port"))
      .map(String::valueOf)
      .map(Float::valueOf)
      .map(Float::intValue)
      .orElse(9200);
    String username = Optional.ofNullable(config.get("elasticsearch_username")).map(String::valueOf).orElse("");
    String password = Optional.ofNullable(config.get("elasticsearch_password")).map(String::valueOf).orElse("");
    String index = Optional.ofNullable(config.get("elasticsearch_index")).map(String::valueOf).orElse("");
    String type = Optional.ofNullable(config.get("elasticsearch_type")).map(String::valueOf).orElse("");
    options.setHost(host);
    options.setPort(port);
    options.setURI(index + "/" + type + "/" + id);
    HttpRequest<Buffer> request = getWebClient(config).request(HttpMethod.PUT, options);
    if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
      //增加HttpBasic鉴权
      request = request.basicAuthentication(username, password);
    }
    log.trace("log request info host: " + options.getHost() + " port: " + options.getPort() + " uri:" + options.getURI());
    return request;
  }

  private WebClient getWebClient(Map<String, Object> config) {
    if (Objects.isNull(this.webClient)) {
      int timeout = Optional.ofNullable(config.get("timeout"))
        .map(String::valueOf)
        .map(Integer::valueOf)
        .orElse(60);
      int queueSize = Optional.ofNullable(config.get("queue_size"))
        .map(String::valueOf)
        .map(Integer::valueOf)
        .orElse(100);
      this.webClient = WebClient.create(vertx,new WebClientOptions()
        .setIdleTimeout(timeout).setMaxWaitQueueSize(queueSize));
    }
    return this.webClient;
  }

  private String getName() {
    return PluginName.LOG;
  }


}
