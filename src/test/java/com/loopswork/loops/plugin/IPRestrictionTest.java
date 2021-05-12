package com.loopswork.loops.plugin;

import com.loopswork.loops.LoopsApplication;
import com.loopswork.loops.admin.collector.CollectorType;
import com.loopswork.loops.admin.collector.impl.MockCollector;
import com.loopswork.loops.admin.entity.EntityType;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.*;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.web.client.WebClient;
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
 * IPRestrict测试
 *
 * @author  lixiaoxiao
 * @date  2020/3/31
 */
@ExtendWith(VertxExtension.class)
public class IPRestrictionTest {
  private static LoopsApplication loops;

  private static int port1 = 20001;
  private static int port2 = 20002;

  @BeforeAll
  static void beforeAll() {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
  }

  @BeforeEach
  void before(Vertx vertx, VertxTestContext testContext) {
    Future.<Long>future(h -> IPRestrictionTest.initLoops(vertx, h))
      .compose(time -> Future.<Void>future(h -> IPRestrictionTest.initTestWebServer(vertx, h)))
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
   * 初始化Servers Routes Plugins Consumers
   *
   * @param collector mockCollector
   */
  private static void initTestData(MockCollector collector) {
    Map<EntityType, List<LoopsEntity>> entityMap = new HashMap<>();
    //初始化测试Server
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
    entityMap.put(EntityType.server, Arrays.asList(server1, server2));
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
    entityMap.put(EntityType.route, Arrays.asList(route1, route2));
    //在server1 server2上创建ipRestrict插件
    Plugin ipRestrict1 = new Plugin();
    ipRestrict1.setId("plugin1");
    ipRestrict1.setServerId(server1.getId());
    ipRestrict1.setRouteId("");
    ipRestrict1.setConsumerId("");
    ipRestrict1.setEnable(true);
    Map<String, Object> map = new HashMap<>();
    map.put("blacklist", Collections.singletonList("127.0.0.1"));
    map.put("whitelist", Collections.singletonList(""));
    ipRestrict1.setConfig(map);
    ipRestrict1.setName(PluginName.IP_RESTRICTION);
    Plugin ipRestrict2 = new Plugin();
    ipRestrict2.setId("plugin2");
    ipRestrict2.setServerId(server2.getId());
    ipRestrict2.setRouteId("");
    ipRestrict2.setConsumerId("");
    ipRestrict2.setEnable(true);
    map = new HashMap<>();
    map.put("blacklist", Collections.singletonList(""));
    map.put("whitelist", Collections.singletonList("127.0.0.1"));
    map.put("switch", "white");
    ipRestrict2.setConfig(map);
    ipRestrict2.setName(PluginName.IP_RESTRICTION);
    entityMap.put(EntityType.plugin, Arrays.asList(ipRestrict1, ipRestrict2));
    collector.setEntityMap(entityMap);
  }

  /**
   * 初始化测试用web服务器
   */
  private static void initTestWebServer(Vertx vertx, Handler<AsyncResult<Void>> handler) {
    HttpServer httpServer1 = vertx.createHttpServer().requestHandler(request -> {
      HttpServerResponse response = request.response();
      response.putHeader("content-type", "text/plain");
      response.end("server1 response success");
    });
    HttpServer httpServer2 = vertx.createHttpServer().requestHandler(request -> {
      HttpServerResponse response = request.response();
      response.putHeader("content-type", "text/plain");
      response.end("server2 response success");
    });
    Future<HttpServer> future1 = Future.future(h -> httpServer1.listen(port1, h));
    Future<HttpServer> future2 = Future.future(h -> httpServer2.listen(port2, h));
    CompositeFuture.all(future1, future2)
      .onSuccess(success -> handler.handle(Future.succeededFuture()))
      .onFailure(failure -> handler.handle(Future.failedFuture(failure)));
  }

  @Test
  void blackTest(Vertx vertx, VertxTestContext testContext){
    WebClient webClient = WebClient.create(vertx);
    webClient.get(loops.getConfig().getRouterPort(), "localhost", "/server1")
      .send(ar -> testContext.verify(() -> {
          if (ar.failed()) {
            testContext.failNow(ar.cause());
          } else {
            //被加入了黑名单返回403拒绝访问
            assertThat(403).isEqualTo(ar.result().statusCode());
            testContext.completeNow();
          }
        })
      );
  }

  @Test
  void whiteTest(Vertx vertx, VertxTestContext testContext){
    WebClient webClient = WebClient.create(vertx);
    webClient.get(loops.getConfig().getRouterPort(), "localhost", "/server2")
      .send(ar -> testContext.verify(() -> {
          if (ar.failed()) {
            testContext.failNow(ar.cause());
          } else {
            //被加入了白名单返回200
            assertThat(200).isEqualTo(ar.result().statusCode());
            testContext.completeNow();
          }
        })
      );
  }

}
