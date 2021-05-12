package com.loopswork.loops.util;

import cn.hutool.core.bean.BeanUtil;
import com.loopswork.loops.config.LoopsConfig;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Map;

/**
 * @author codi
 * @date 2020/3/6 1:24 下午
 * @description 配置工具
 */
public class ConfigUtil {

  /**
   * 使用vertx读取环境变量和yaml文件中的配置信息
   */
  public static void loadConfig(Vertx vertx, Handler<AsyncResult<LoopsConfig>> handler) {
    //读取环境变量
    ConfigStoreOptions propertyWitHierarchical = new ConfigStoreOptions()
      .setType("env");
    //读取配置文件
    ConfigStoreOptions yamlStore = new ConfigStoreOptions()
      .setType("file")
      .setFormat("yaml")
      .setConfig(new JsonObject().put("path", "application.yml"));
    ConfigRetrieverOptions options = new ConfigRetrieverOptions()
      .addStore(yamlStore)
      .addStore(propertyWitHierarchical);
    //环境变量配置将覆盖配置文件配置
    ConfigRetriever configRetriever = ConfigRetriever.create(vertx, options);
    configRetriever.getConfig(ar -> {
      if (ar.succeeded()) {
        JsonObject config = ar.result();
        Map<String, Object> map = config.getMap();
        LoopsConfig result = BeanUtil.mapToBean(map, LoopsConfig.class, true);
        handler.handle(Future.succeededFuture(result));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

}
