package com.loopswork.loops.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.loopswork.loops.plugin.IPluginHandler;
import com.loopswork.loops.plugin.checker.IPluginChecker;
import com.loopswork.loops.plugin.checker.impl.*;
import com.loopswork.loops.plugin.impl.*;

/**
 * @author codi
 * @description 插件加载器
 * @date 2020/1/19 10:09 上午
 */
public class PluginModule extends AbstractModule {
  @Override
  protected void configure() {
    //加载插件
    Multibinder<IPluginHandler> pluginBinder = Multibinder.newSetBinder(binder(), IPluginHandler.class);
    pluginBinder.addBinding().to(ACLPlugin.class);
    pluginBinder.addBinding().to(IPRestrictionPlugin.class);
    pluginBinder.addBinding().to(KeyAuthPlugin.class);
    pluginBinder.addBinding().to(RateLimitPlugin.class);
    pluginBinder.addBinding().to(RequestTransformerPlugin.class);
    pluginBinder.addBinding().to(ResponseTransformerPlugin.class);
    pluginBinder.addBinding().to(JWTPlugin.class);
    //加载插件检查器
    Multibinder<IPluginChecker> checkBinder = Multibinder.newSetBinder(binder(), IPluginChecker.class);
    checkBinder.addBinding().to(AclChecker.class);
    checkBinder.addBinding().to(IpRestrictionChecker.class);
    checkBinder.addBinding().to(KeyAuthChecker.class);
    checkBinder.addBinding().to(LogChecker.class);
    checkBinder.addBinding().to(RateLimitingChecker.class);
    checkBinder.addBinding().to(RequestTransformerChecker.class);
    checkBinder.addBinding().to(ResponseTransformerChecker.class);
    checkBinder.addBinding().to(JWTChecker.class);
    super.configure();
  }
}
