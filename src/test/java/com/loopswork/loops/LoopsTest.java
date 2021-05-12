package com.loopswork.loops;

import com.loopswork.loops.admin.collector.CollectorType;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.RouterState;
import com.loopswork.loops.exception.RouterCode;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author codi
 * @date 2020/2/28 8:46 下午
 * @description 服务启动测试
 */
@ExtendWith(VertxExtension.class)
public class LoopsTest {
  private static LoopsApplication loops;

  @BeforeAll
  static void beforeAll() {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
  }

  @BeforeEach
  void before(Vertx vertx, VertxTestContext testContext) {
    initLoops(vertx, handler -> {
      if (handler.succeeded()) {
        testContext.completeNow();
      } else {
        testContext.failNow(handler.cause());
      }
    });
  }

  @AfterEach
  void after(Vertx vertx, VertxTestContext testContext) {
    vertx.close(h -> {
      testContext.completeNow();
    });
  }

  private static void initLoops(Vertx vertx, Handler<AsyncResult<Long>> handler) {
    loops = new LoopsApplication();
    LoopsConfig loopsConfig = new LoopsConfig();
    loopsConfig.setCollector(CollectorType.mock);
    loops.run(vertx, loopsConfig, handler);
  }

  @Test
  void startLoopsServer(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getAdminPort(), "localhost", "/")
      .send(ar -> testContext.verify(() -> {
          if (ar.failed()) {
            testContext.failNow(ar.cause());
          } else {
            assertThat(RouterState.ACTIVE).isEqualTo(RouterState.valueOf(ar.result().bodyAsJsonObject().getString("status")));
            assertThat(200).isEqualTo(ar.result().statusCode());
            testContext.completeNow();
          }
        })
      );
  }

  @Test
  void TestRouter(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/")
      .send(ar -> testContext.verify(() -> {
          if (ar.failed()) {
            testContext.failNow(ar.cause());
          } else {
            assertThat(RouterCode.NO_ROUTE_MATCHED.getCode()).isEqualTo(ar.result().bodyAsJsonObject().getInteger("code"));
            assertThat(404).isEqualTo(ar.result().statusCode());
            testContext.completeNow();
          }
        })
      );
  }

}
