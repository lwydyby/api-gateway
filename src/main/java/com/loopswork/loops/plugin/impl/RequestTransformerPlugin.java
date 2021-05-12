package com.loopswork.loops.plugin.impl;


import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.entity.ContextKeys;
import com.loopswork.loops.entity.Plugin;
import com.loopswork.loops.http.entity.BodyBuffer;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.plugin.BasePluginHandler;
import com.loopswork.loops.plugin.PluginPriority;
import com.loopswork.loops.plugin.PluginType;
import com.loopswork.loops.util.JsonObjectUtil;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Armstrong Liu
 * @date 2019/3/29 14:34
 */
public class RequestTransformerPlugin extends BasePluginHandler {
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void handle(RoutingContext context, Plugin plugin) {
    Map<String, Object> config = plugin.getConfig();
    HttpRequest httpRequest = context.get(ContextKeys.HTTP_REQUEST);
    String[] operationTypes = new String[]{"remove", "rename", "replace", "add", "append"};
    Map<String, String> headers = httpRequest.getHeaders();
    Map<String, String> params = httpRequest.getParams();
    Stream.of(operationTypes).forEach(operation -> {
      Object operationHeaders = config.get(operation + "_headers");
      if (operationHeaders instanceof List) {
        JsonObjectUtil.transformerMap(headers, operation, (List<String>) operationHeaders);
      }
      Object operationQueryparam = config.get(operation + "_queryparam");
      if (operationQueryparam instanceof List) {
        JsonObjectUtil.transformerMap(headers, operation, (List<String>) operationQueryparam);
      }
    });
    // 更新headers和params
    httpRequest.setHeaders(headers);
    httpRequest.setParams(params);
    boolean bodyChange = Stream.of(operationTypes).
      anyMatch(operation -> config.containsKey(operation + "_body"));
    if (bodyChange &&
      httpRequest.getHeaders().get("Content-Type").contains("application/json")) {
      // 只处理context-type为application/json的request body
      JsonObject requestBody = new JsonObject(httpRequest.getBody().toString());
      Stream.of(operationTypes).forEach(operation -> {
        Object operationBody = config.get(operation + "_body");
        if (operationBody instanceof List) {
          JsonObjectUtil.transformerMap(headers, operation, (List<String>) operationBody);
        }
      });
      httpRequest.setBody(new BodyBuffer(requestBody.toString()));
    }
    context.next();
  }

  @Override
  public int priority() {
    return PluginPriority.REQUEST_TRANSFORMER;
  }

  @Override
  public PluginType getPluginType() {
    return PluginType.PRE;
  }

  @Override
  public String getName() {
    return PluginName.REQUEST_TRANSFORMER;
  }

}
