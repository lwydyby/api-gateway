package com.loopswork.loops.plugin;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface IPluginHandler extends Handler<RoutingContext> {

  /**
   * 获取插件名称
   */
  String getName();

  /**
   * 定义plugin的优先级
   */
  int priority();

  /**
   * 获取plugin的类型
   *
   * @return 可选值：null，pre和post。默认为null
   */
  PluginType getPluginType();

}
