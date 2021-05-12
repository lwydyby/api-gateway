package com.loopswork.loops.route;

import com.loopswork.loops.LoopsApplication;
import com.loopswork.loops.admin.collector.CollectorType;
import com.loopswork.loops.admin.collector.impl.MockCollector;
import com.loopswork.loops.admin.entity.EntityType;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.Protocol;
import com.loopswork.loops.entity.Route;
import com.loopswork.loops.entity.Server;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试可以接收的数据类型
 *
 * @author lixiaoxiao
 * @date 2020/3/30
 */
@ExtendWith(VertxExtension.class)
public class ContentTypeAcceptTest {
  private static LoopsApplication loops;
  /**
   * vertx创建模拟服务端口1
   */
  private static int port1 = 20001;
  /**
   * vertx创建模拟服务端口2
   */
  private static int port2 = 20002;
  private static int port3 = 20003;

  @BeforeAll
  static void beforeAll() {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
  }

  @BeforeEach
  void before(Vertx vertx, VertxTestContext testContext) {
    Future.<Long>future(h -> ContentTypeAcceptTest.initLoops(vertx, h))
      .compose(time -> Future.<Void>future(h -> ContentTypeAcceptTest.initTestWebServer(vertx, h)))
      .onSuccess(h -> testContext.completeNow())
      .onFailure(testContext::failNow);
  }

  @AfterEach
  void after(Vertx vertx, VertxTestContext testContext) {
    vertx.close(handler -> testContext.completeNow());
  }

  private static void initLoops(Vertx vertx, Handler<AsyncResult<Long>> handler) {
    loops = new LoopsApplication();
    LoopsConfig config = new LoopsConfig();
    config.setCollector(CollectorType.mock);
    Future.<Long>future(h -> loops.run(vertx, config, h)).compose(time -> {
      MockCollector collector = loops.getInjector().getInstance(MockCollector.class);
      initTestData(collector);
      Managers managers = loops.getInjector().getInstance(Managers.class);
      return Future.future(managers::update);
    }).onSuccess(success -> handler.handle(Future.succeededFuture())).
      onFailure(failure -> handler.handle(Future.failedFuture(failure)));
  }

  /**
   * 初始化Servers Routes
   *
   * @param collector mockCollector
   */
  private static void initTestData(MockCollector collector) {
    Map<EntityType, List<LoopsEntity>> entityMap = new HashMap<>();
    //初始化测试Servers
    Server server1 = new Server();
    server1.setId("server1");
    server1.setEnable(true);
    server1.setHost("localhost");
    server1.setPort(port1);
    server1.setProtocols(Protocol.HTTP);
    Server server2 = new Server();
    server2.setId("server2");
    server2.setEnable(true);
    server2.setHost("localhost");
    server2.setPort(port2);
    server2.setProtocols(Protocol.HTTP);
    Server server3 = new Server();
    server3.setId("server3");
    server3.setEnable(true);
    server3.setHost("localhost");
    server3.setPort(port3);
    server3.setProtocols(Protocol.HTTP);
    entityMap.put(EntityType.server, Arrays.asList(server1, server2, server3));
    //初始化测试Routes
    Route route1 = new Route();
    route1.setEnable(true);
    route1.setId("route1");
    route1.setServerId(server1.getId());
    route1.setPaths(Collections.singleton("/server1"));
    route1.setPriority(100);
    Route route2 = new Route();
    route2.setEnable(true);
    route2.setId("route2");
    route2.setServerId(server2.getId());
    route2.setPaths(Collections.singleton("/server2"));
    route2.setPriority(99);
    Route route3 = new Route();
    route3.setEnable(true);
    route3.setId("route3");
    route3.setServerId(server3.getId());
    route3.setPaths(Collections.singleton("/server3"));
    route3.setPriority(98);
    entityMap.put(EntityType.route, Arrays.asList(route1, route2, route3));
    collector.setEntityMap(entityMap);
  }

  /**
   * 初始化测试用web服务器
   */
  private static void initTestWebServer(Vertx vertx, Handler<AsyncResult<Void>> handler) {
    //测试form-data转发的server
    HttpServer httpServer1 = vertx.createHttpServer().requestHandler(request -> {
      String contentType = request.getHeader("content-type");
      //form-data被转发后content-type会变成urlencoded
      assertThat(contentType).isEqualTo("application/x-www-form-urlencoded");
      request.bodyHandler(body -> request.response().end(body.toString()));
    });
    //测试x-www-form-urlencoded转发
    HttpServer httpServer2 = vertx.createHttpServer().requestHandler(request ->
      request.bodyHandler(body -> request.response().end(body.toString())));
    //测试json转发
    HttpServer httpServer3 = vertx.createHttpServer().requestHandler(request ->
      request.bodyHandler(body -> request.response().end(body.toJsonObject().toString())));

    Future<HttpServer> future1 = Future.future(h -> httpServer1.listen(port1, h));
    Future<HttpServer> future2 = Future.future(h -> httpServer2.listen(port2, h));
    Future<HttpServer> future3 = Future.future(h -> httpServer3.listen(port3, h));
    CompositeFuture.all(future1, future2, future3)
      .onSuccess(success -> handler.handle(Future.succeededFuture()))
      .onFailure(failure -> handler.handle(Future.failedFuture(failure)));
  }

  @Test
  void fromDataTest(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    webClient.post(loops.getConfig().getRouterPort(), "localhost", "/server1/")
      .putHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
      .sendMultipartForm(MultipartForm.create().attribute("hello", "world"), ar -> testContext.verify(() -> {
        if (ar.failed()) {
          testContext.failNow(ar.cause());
        } else {
          assertThat(ar.result().bodyAsString()).isEqualTo("hello=world");
          testContext.completeNow();
        }
      }));
  }

  @Test
  void formUrlencodedTest(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    webClient.post(loops.getConfig().getRouterPort(), "localhost", "/server2/")
      .putHeader("content-type", "application/x-www-form-urlencoded")
      .sendForm(MultiMap.caseInsensitiveMultiMap().add("hello", "world"), ar -> testContext.verify(() -> {
        if (ar.failed()) {
          testContext.failNow(ar.cause());
        } else {
          assertThat(ar.result().bodyAsString()).isEqualTo("hello=world");
          testContext.completeNow();
        }
      }));
  }

  @Test
  void jsonTest(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    webClient.post(loops.getConfig().getRouterPort(), "localhost", "/server3/")
      .putHeader("content-type", "application/json")
      .sendJsonObject(new JsonObject().put("hello", "world"), ar -> testContext.verify(() -> {
        if (ar.failed()) {
          testContext.failNow(ar.cause());
        } else {
          assertThat(ar.result().bodyAsString()).isEqualTo("{\"hello\":\"world\"}");
          testContext.completeNow();
        }
      }));
  }
}
