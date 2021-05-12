package com.loopswork.loops.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.loopswork.loops.admin.collector.ICollector;
import com.loopswork.loops.admin.collector.impl.MockCollector;
import com.loopswork.loops.admin.collector.impl.MongoCollector;
import com.loopswork.loops.admin.collector.impl.YamlCollector;
import com.loopswork.loops.config.LoopsConfig;

/**
 * @author codi
 * @title: CollectorModule
 * @projectName loops
 * @description: 配置加载器
 * @date 2020/1/21 3:05 下午
 */
public class CollectorModule extends AbstractModule {
  @Override
  protected void configure() {
    binder().bind(ICollector.class).toProvider(new Provider<ICollector>() {
      @Inject
      LoopsConfig loopsConfig;
      @Inject
      MongoCollector mongoCollector;
      @Inject
      YamlCollector yamlCollector;
      @Inject
      MockCollector mockCollector;

      @Override
      public ICollector get() {
        switch (loopsConfig.getCollector()) {
          case mock:
            return mockCollector;
          case yaml:
            return yamlCollector;
          case mongo:
          default:
            return mongoCollector;
        }
      }
    });
    super.configure();
  }
}
