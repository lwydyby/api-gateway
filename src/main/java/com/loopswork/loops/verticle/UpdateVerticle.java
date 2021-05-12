package com.loopswork.loops.verticle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.collector.ICollector;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author codi
 * @date 2020/3/10 1:50 下午
 * @description 更新配置
 */
@Singleton
public class UpdateVerticle extends AbstractVerticle {
  public static final String COLLECTOR_UPDATE_ADDRESS = "collector_update";
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  @Inject
  private Managers managers;
  @Inject
  private ICollector entityDAO;

  @Override
  public void start() {
    //由更新主动触发
    vertx.eventBus().consumer(COLLECTOR_UPDATE_ADDRESS, handler -> update());
    //每5秒检查一次是否需要更新
    vertx.setPeriodic(5000, handler -> update());
  }

  public void update() {
    entityDAO.checkGeneration(ar -> {
      if (ar.result()) {
        //需要更新
        managers.update(result -> {
          if (result.succeeded()) {
            log.info("Managers update success in {} ms", result.result());
          } else {
            log.warn("Managers update error ", result.cause());
          }
        });
      }
    });
  }

}
