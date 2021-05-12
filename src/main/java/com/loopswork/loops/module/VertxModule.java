package com.loopswork.loops.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.collector.CollectorType;
import com.loopswork.loops.config.LoopsConfig;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * @author codi
 * @description VertxModule
 * @date 2019/12/31 3:54 下午
 */
public class VertxModule extends AbstractModule {

  private final Vertx vertx;
  private final LoopsConfig config;

  public VertxModule(Vertx vertx, LoopsConfig config) {
    this.vertx = vertx;
    this.config = config;
  }

  @Override
  protected void configure() {
    bind(Vertx.class).toInstance(vertx);
    bind(LoopsConfig.class).toInstance(config);
    super.configure();
  }

  @Provides
  @Singleton
  public MongoClient providesMongoClient() {
    if (config.getCollector() == CollectorType.mongo) {
      JsonObject mongoConfig = new JsonObject();
      mongoConfig.put("connection_string", config.getConnectionString());
      return MongoClient.create(vertx, mongoConfig);
    } else {
      return null;
    }
  }

}
