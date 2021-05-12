package com.loopswork.loops.plugin.impl;

import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.entity.ContextKeys;
import com.loopswork.loops.entity.Plugin;
import com.loopswork.loops.http.entity.BodyBuffer;
import com.loopswork.loops.http.entity.HttpResponse;
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
 * @date 2019/3/29 14:42
 */
public class ResponseTransformerPlugin extends BasePluginHandler {
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void handle(RoutingContext context, Plugin plugin) {
    Map<String, Object> config = plugin.getConfig();
    HttpResponse httpResponse = context.get(ContextKeys.HTTP_RESPONSE);
    String[] operationTypes = new String[]{"remove", "rename", "replace", "add", "append"};
    Map<String, String> headers = httpResponse.getHeaders();
    Stream.of(operationTypes).forEach(operation -> {
      Object operationHeaders = config.get(operation + "_headers");
      if (operationHeaders instanceof List) {
        JsonObjectUtil.transformerMap(headers, operation, (List<String>) operationHeaders);
      }
    });
    httpResponse.setHeaders(headers);
    boolean bodyChange = Stream.of(operationTypes).
      anyMatch(operation -> config.containsKey(operation + "_json"));
    if (bodyChange &&
      httpResponse.getHeaders().get("Content-Type").contains("application/json")) {
      JsonObject responseBody = new JsonObject(httpResponse.getBody().toString());
      Stream.of(operationTypes).forEach(operation -> {
        Object operationBody = config.get(operation + "_json");
        if (operationBody instanceof List) {
          JsonObjectUtil.transformerJson(responseBody, operation, (List<String>) operationBody);
        }
      });
      httpResponse.setBody(new BodyBuffer(responseBody.toString()));
    }
    context.next();
  }

  @Override
  public int priority() {
    return PluginPriority.RESPONSE_TRANSFORMER;
  }

  @Override
  public PluginType getPluginType() {
    return PluginType.POST;
  }

  public String getName() {
    return PluginName.RESPONSE_TRANSFORMER;
  }
}
