package com.loopswork.loops.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.handler.RequestBodyHandler;
import com.loopswork.loops.admin.utils.PluginCheck;
import com.loopswork.loops.entity.Plugin;
import com.loopswork.loops.exception.SimpleException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

/**
 * @author liwei
 * @description 插件服务
 * @date 2019-11-27 15:39
 */
@Singleton
public class PluginService {
  @Inject
  private PluginCheck pluginCheck;

  public void checkPlugin(RoutingContext context) {
    Plugin plugin = context.get(RequestBodyHandler.REQUEST_BODY_KEY);
    try {
      pluginCheck.checkConfigAndName(plugin);
    } catch (SimpleException e) {
      context.fail(e.getCode(), e);
      return;
    }
    context.next();
  }

  public void getPluginName(RoutingContext context) {
    List<String> list = pluginCheck.getPluginName();
    context.response().putHeader("content-type", "application/json")
      .end(new JsonObject().put("data", list).toBuffer());
  }

  public void getPluginConfig(String name, RoutingContext context) {
    try {
      context.response()
        .putHeader("content-type", "application/json")
        .end(pluginCheck.getPluginConfig(name));
    } catch (JsonProcessingException e) {
      context.fail(e);
    }
  }


}
