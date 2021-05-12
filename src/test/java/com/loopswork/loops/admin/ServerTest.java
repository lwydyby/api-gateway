package com.loopswork.loops.admin;


import com.loopswork.loops.LoopsApplication;
import com.loopswork.loops.admin.entity.dto.ServerRequest;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.util.ConfigUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME;


@ExtendWith(VertxExtension.class)
public class ServerTest {

  private LoopsApplication loops;

  private MongoClient mongoClient;

  @BeforeAll
  static void beforeAll() {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
  }

  @BeforeEach
  void before(Vertx vertx, VertxTestContext testContext) {
    Future.<Long>future(h -> initLoops(vertx, h))
      .onSuccess(h -> testContext.completeNow())
      .onFailure(testContext::failNow);
  }

  @AfterEach
  void after(Vertx vertx, VertxTestContext testContext) {
    vertx.close(handler -> testContext.completeNow());
  }

  private void initLoops(Vertx vertx, Handler<AsyncResult<Long>> handler) {
    loops = new LoopsApplication();
    Future.<LoopsConfig>future(h -> ConfigUtil.loadConfig(vertx, h))
      .compose(config -> Future.<Long>future(h -> {
        JsonObject mongoConfig = new JsonObject();
        mongoConfig.put("connection_string", config.getConnectionString());
        mongoClient = MongoClient.create(vertx, mongoConfig);
        mongoClient.removeDocuments("server", new JsonObject(),
          ar -> loops.run(vertx, config, h));
      })).onFailure(failure -> handler.handle(Future.failedFuture(failure)))
      .onSuccess(success -> handler.handle(Future.succeededFuture()));
  }

  /**
   * 添加服务
   */
  @Test
  public void testAddServer(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    ServerRequest serverRequest = new ServerRequest();
    serverRequest.setName("testServer1");
    serverRequest.setRetries(1);
    serverRequest.setHost("host1");
    webClient.post(loops.getConfig().getAdminPort(), "localhost", "/servers")
      .putHeader("content-type", "application/json")
      .sendJson(serverRequest, ar -> testContext.verify(() -> {
          if (ar.failed()) {
            testContext.failNow(ar.cause());
          } else {
            testContext.completeNow();
          }
        })
      );
  }

  /**
   * 查询服务
   */
  @Test
  public void getServiceList(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    webClient.get(loops.getConfig().getAdminPort(), "localhost", "/servers")
      .send(ar -> testContext.verify(() -> {
        if (ar.failed()) {
          testContext.failNow(ar.cause());
        } else {
          testContext.completeNow();
        }
      }));
  }
}
