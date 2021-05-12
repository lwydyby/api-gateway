package com.loopswork.loops.plugin;

import com.loopswork.loops.LoopsApplication;
import com.loopswork.loops.admin.collector.CollectorType;
import com.loopswork.loops.admin.collector.impl.MockCollector;
import com.loopswork.loops.admin.entity.EntityType;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.Plugin;
import com.loopswork.loops.entity.Protocol;
import com.loopswork.loops.entity.Route;
import com.loopswork.loops.entity.Server;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
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
 * Transformer插件测试
 *
 * @author  lixiaoxiao
 * @date  2020/3/31
 */
@ExtendWith(VertxExtension.class)
public class TransformerTest {

  private static LoopsApplication loops;

  private static int port1 = 20001;
  private static int port2 = 20002;

  @BeforeAll
  static void beforeAll() {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
  }

  @BeforeEach
  void before(Vertx vertx, VertxTestContext testContext) {
    Future.<Long>future(h -> TransformerTest.initLoops(vertx, h))
      .compose(time -> Future.<Void>future(h -> TransformerTest.initTestWebServer(vertx, h)))
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
    //在server1上创建requestTransformer插件
    Plugin requestTransformerPlugin = new Plugin();
    requestTransformerPlugin.setId("plugin1");
    requestTransformerPlugin.setServerId(server1.getId());
    requestTransformerPlugin.setRouteId("");
    requestTransformerPlugin.setConsumerId("");
    requestTransformerPlugin.setEnable(true);
    Map<String, Object> map = new HashMap<>();
    map.put("add_headers", Collections.singletonList("hello:world"));
    map.put("remove_headers", Collections.singletonList(""));
    map.put("remove_querystring", Collections.singletonList(""));
    map.put("remove_body", Collections.singletonList(""));
    map.put("rename_querystring", Collections.singletonList(""));
    map.put("rename_body", Collections.singletonList(""));
    map.put("replace_headers", Collections.singletonList(""));
    map.put("replace_querystring", Collections.singletonList(""));
    map.put("replace_body", Collections.singletonList(""));
    map.put("add_querystring", Collections.singletonList(""));
    map.put("append_headers", Collections.singletonList(""));
    map.put("append_querystring", Collections.singletonList(""));
    map.put("append_body", Collections.singletonList(""));
    map.put("add_body", Collections.singletonList(""));
    map.put("rename_headers", Collections.singletonList(""));
    requestTransformerPlugin.setConfig(map);
    requestTransformerPlugin.setName(PluginName.REQUEST_TRANSFORMER);
    Plugin responseTransformerPlugin = new Plugin();
    responseTransformerPlugin.setId("plugin2");
    responseTransformerPlugin.setServerId(server2.getId());
    responseTransformerPlugin.setRouteId("");
    responseTransformerPlugin.setConsumerId("");
    responseTransformerPlugin.setEnable(true);
    map = new HashMap<>();
    map.put("add_json", Collections.singletonList(""));
    map.put("remove_headers", Collections.singletonList(""));
    map.put("remove_json", Collections.singletonList(""));
    map.put("replace_headers", Collections.singletonList(""));
    map.put("replace_json", Collections.singletonList(""));
    map.put("add_headers", Collections.singletonList("hello:world"));
    map.put("append_headers", Collections.singletonList(""));
    map.put("append_json", Collections.singletonList(""));
    responseTransformerPlugin.setConfig(map);
    responseTransformerPlugin.setName(PluginName.RESPONSE_TRANSFORMER);
    entityMap.put(EntityType.plugin, Arrays.asList(requestTransformerPlugin, responseTransformerPlugin));
    collector.setEntityMap(entityMap);
  }

  /**
   * 初始化测试用web服务器
   */
  private static void initTestWebServer(Vertx vertx, Handler<AsyncResult<Void>> handler) {
    //测试requestTransformer
    HttpServer httpServer1 = vertx.createHttpServer().requestHandler(request -> {
      //插件在请求头中添加了hello,转发后可以在上游服务获取其内容
      assertThat(request.getHeader("hello")).isEqualToIgnoringCase("world");
      HttpServerResponse response = request.response();
      response.putHeader("content-type", "text/plain");
      response.end(request.getHeader("hello"));
    });
    //测试responseTransformer
    HttpServer httpServer2 = vertx.createHttpServer().requestHandler(request -> {
//      request.bodyHandler(body -> request.response().end(body.toJsonObject().toString()))
      JsonObject jsonObject = new JsonObject().put("server2", "responseTest");
      HttpServerResponse response = request.response();
      response.putHeader("Content-Type", "application/json");
      response.end(jsonObject.encode());
    });
    Future<HttpServer> future1 = Future.future(h -> httpServer1.listen(port1, h));
    Future<HttpServer> future2 = Future.future(h -> httpServer2.listen(port2, h));
    CompositeFuture.all(future1, future2)
      .onSuccess(success -> handler.handle(Future.succeededFuture()))
      .onFailure(failure -> handler.handle(Future.failedFuture(failure)));
  }

  @Test
  void requestTransformerTest(Vertx vertx, VertxTestContext testContext){
    WebClient webClient = WebClient.create(vertx);
    webClient.get(loops.getConfig().getRouterPort(), "localhost", "/server1")
      .putHeader("Content-Type", "application/x-www-form-urlencoded")
      .send(ar -> testContext.verify(() -> {
          if (ar.failed()) {
            testContext.failNow(ar.cause());
          } else {
            //返回添加的header内容
            assertThat(ar.result().bodyAsString()).isEqualToIgnoringCase("world");
            testContext.completeNow();
          }
        })
      );
  }

  @Test
  void responseTransformerTest(Vertx vertx, VertxTestContext testContext){
    WebClient webClient = WebClient.create(vertx);
    webClient.get(loops.getConfig().getRouterPort(), "localhost", "/server2")
      .send(ar -> testContext.verify(() -> {
          if (ar.failed()) {
            testContext.failNow(ar.cause());
          } else {
            //返回添加的header内容
            assertThat(ar.result().getHeader("hello")).isEqualToIgnoringCase("world");
            testContext.completeNow();
          }
        })
      );
  }

}
