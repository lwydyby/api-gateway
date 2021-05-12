package com.loopswork.loops.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.loopswork.loops.manager.*;

/**
 * @author codi
 * @description 数据加载器
 * @date 2020/1/19 10:09 上午
 */
public class DataManagerModule extends AbstractModule {
  @Override
  protected void configure() {
    //加载插件
    Multibinder<IDataManager> pluginBinder = Multibinder.newSetBinder(binder(), IDataManager.class);
    pluginBinder.addBinding().to(ACLManager.class);
    pluginBinder.addBinding().to(ConsumerManager.class);
    pluginBinder.addBinding().to(KeyAuthManager.class);
    pluginBinder.addBinding().to(PluginManager.class);
    pluginBinder.addBinding().to(RouterManager.class);
    pluginBinder.addBinding().to(UpstreamManager.class);
    super.configure();
  }
}
