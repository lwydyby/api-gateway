package com.loopswork.loops.route;

import cn.hutool.core.lang.Assert;
import com.loopswork.loops.LoopsApplication;
import com.loopswork.loops.admin.collector.CollectorType;
import com.loopswork.loops.admin.collector.impl.MockCollector;
import com.loopswork.loops.admin.entity.EntityType;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.HttpMethod;
import com.loopswork.loops.entity.Protocol;
import com.loopswork.loops.entity.Route;
import com.loopswork.loops.entity.Server;
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

/**
 * 路由匹配测试
 *
 * @author lixiaoxiao
 * @date 2020/3/3 9:00
 */
@ExtendWith(VertxExtension.class)
public class RouteMatchTest {
  private static LoopsApplication loops;
  /**
   * vertx创建模拟服务端口1
   */
  private static int port1 = 20001;
  /**
   * vertx创建模拟服务端口2
   */
  private static int port2 = 20002;

  @BeforeAll
  static void beforeAll() {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
  }

  @BeforeEach
  void before(Vertx vertx, VertxTestContext testContext) {
    Future.<Long>future(h -> RouteMatchTest.initLoops(vertx, h))
      .compose(time -> Future.<Void>future(h -> RouteMatchTest.initTestWebServer(vertx, h)))
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
    entityMap.put(EntityType.server, Arrays.asList(server1, server2));
    //初始化测试Routes
    //route1测试path匹配
    Route route1 = new Route();
    route1.setEnable(true);
    route1.setId("route1");
    route1.setServerId(server1.getId());
    route1.setPaths(Collections.singleton("/api"));
    route1.setPriority(100);
    //route2测试host匹配
    Route route2 = new Route();
    route2.setEnable(true);
    route2.setId("route2");
    route2.setServerId(server1.getId());
    route2.setHosts(Collections.singleton("localhost"));
    route2.setPriority(99);
    //route3测试method匹配
    Route route3 = new Route();
    route3.setEnable(true);
    route3.setId("route3");
    route3.setServerId(server2.getId());
    route3.setMethods(Collections.singleton(HttpMethod.POST));
    route3.setPriority(98);
    //route4测试method,path匹配
    Route route4 = new Route();
    route4.setEnable(true);
    route4.setId("route4");
    route4.setServerId(server2.getId());
    route4.setMethods(Collections.singleton(HttpMethod.POST));
    route4.setPaths(Collections.singleton("/method/match"));
    route4.setPriority(97);
    //route5测试路径通配符*匹配
    Route route5 = new Route();
    route5.setEnable(true);
    route5.setId("route5");
    route5.setServerId(server2.getId());
    route5.setPaths(Collections.singleton("/wildcard/*/match"));
    route5.setPriority(96);
    //route6测试host通配符*匹配
    Route route6 = new Route();
    route6.setEnable(true);
    route6.setId("route6");
    route6.setServerId(server2.getId());
    route6.setHosts(Collections.singleton("*.ctyun.*"));
    route6.setPriority(95);
    //route7测试host&path匹配
    Route route7 = new Route();
    route7.setEnable(true);
    route7.setId("route7");
    route7.setServerId(server2.getId());
    route7.setHosts(Collections.singleton("www.moho.cn"));
    route7.setPaths(Collections.singleton("/hostPath"));
    route7.setPriority(94);
    //route8测试host&method匹配
    Route route8 = new Route();
    route8.setEnable(true);
    route8.setId("route8");
    route8.setServerId(server2.getId());
    route8.setHosts(Collections.singleton("www.request.cn"));
    route8.setMethods(Collections.singleton(HttpMethod.DELETE));
    route8.setPriority(93);
    //route9测试method&path匹配
    Route route9 = new Route();
    route9.setEnable(true);
    route9.setId("route9");
    route9.setServerId(server2.getId());
    route9.setMethods(Collections.singleton(HttpMethod.PATCH));
    route9.setPaths(Collections.singleton("/methodPath"));
    route9.setPriority(92);
    //route10测试path&host&method匹配
    Route route10 = new Route();
    route10.setEnable(true);
    route10.setId("route10");
    route10.setServerId(server2.getId());
    route10.setMethods(Collections.singleton(HttpMethod.PUT));
    route10.setPaths(Collections.singleton("/pathHostMethod"));
    route10.setHosts(Collections.singleton("cty.cn"));
    route10.setPriority(91);
    //route11测试path&host&method匹配同优先级
    Route route11 = new Route();
    route11.setEnable(true);
    route11.setId("route11");
    route11.setServerId(server1.getId());
    route11.setMethods(Collections.singleton(HttpMethod.PUT));
    route11.setPaths(Collections.singleton("/pathHostMethod"));
    route11.setHosts(Collections.singleton("cty.cn"));
    route11.setPriority(900);
    entityMap.put(EntityType.route, Arrays.asList(route1, route2, route3, route4, route5, route6, route7, route8, route9, route10, route11));
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

  /**
   * 路径匹配测试
   */
  @Test
  public void testPathMatch(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/api")
      .send(ar -> testContext.verify(() -> {
        String result = ar.result().bodyAsString();
        //请求成功返回临时创建的测试服务1返回的信息server1 response success
        System.out.println(result);
        Assert.isTrue(result.equals("server1 response success"));
        testContext.completeNow();
      }));
  }

  @Test
  public void testPathNotMatched(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/test")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server1 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
  }

  /**
   * host匹配测试
   */
  @Test
  public void testHostMatch(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/").putHeader("Host", "localhost")
      .send(ar -> testContext.verify(() -> {
        String result = ar.result().bodyAsString();
        //请求成功返回临时创建的测试服务1返回的信息server1 response success
        System.out.println(result);
        Assert.isTrue(result.equals("server1 response success"));
        testContext.completeNow();
      }));
  }

  @Test
  public void testHostNotMatched(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server1 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
  }

  /**
   * method匹配测试
   */
  @Test
  public void testMethodMatch(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.post(loops.getConfig().getRouterPort(), "localhost", "/")
      .send(ar -> testContext.verify(() -> {
        String result = ar.result().bodyAsString();
        //请求成功返回临时创建的测试服务2返回的信息server2 response success
        System.out.println(result);
        Assert.isTrue(result.equals("server2 response success"));
        testContext.completeNow();
      }));
    client.post(loops.getConfig().getRouterPort(), "localhost", "/method/match")
      .send(ar -> testContext.verify(() -> {
        String result = ar.result().bodyAsString();
        //请求成功返回临时创建的测试服务2返回的信息server2 response success
        System.out.println(result);
        Assert.isTrue(result.equals("server2 response success"));
        testContext.completeNow();
      }));
  }

  @Test
  public void testMethodNotMatched(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/method/match")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server2 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
  }

  /**
   * path通配符*匹配
   */
  @Test
  public void testPathWildCardMatch(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/wildcard/test/match")
      .send(ar -> testContext.verify(() -> {
        String result = ar.result().bodyAsString();
        //请求成功返回临时创建的测试服务2返回的信息server2 response success
        System.out.println(result);
        Assert.isTrue(result.equals("server2 response success"));
        testContext.completeNow();
      }));
  }

  @Test
  public void testPathWildCardNotMatched(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/wildcard/match")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server2 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
  }

  /**
   * host通配符*匹配
   */
  @Test
  public void testHostWildCardMatch(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/host/wildcard/match").putHeader("Host", "www.ctyun.cn")
      .send(ar -> testContext.verify(() -> {
        String result = ar.result().bodyAsString();
        //请求成功返回临时创建的测试服务2返回的信息server2 response success
        System.out.println(result);
        Assert.isTrue(result.equals("server2 response success"));
        testContext.completeNow();
      }));
  }

  @Test
  public void testHostWildCardNotMatched(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/host/wildcard/match").putHeader("Host", "www.ctyos.cn")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server2 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
  }

  /**
   * 测试host path组合
   */
  @Test
  public void testHostPathMatch(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/hostPath").putHeader("Host", "www.moho.cn")
      .send(ar -> testContext.verify(() -> {
        String result = ar.result().bodyAsString();
        //请求成功返回临时创建的测试服务2返回的信息server2 response success
        System.out.println(result);
        Assert.isTrue(result.equals("server2 response success"));
        testContext.completeNow();
      }));
  }

  @Test
  public void testHostPathNotMatched(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/hostPath")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server2 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
    client.get(loops.getConfig().getRouterPort(), "localhost", "/hostpath").putHeader("Host", "www.moho.cn")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server2 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
  }

  /**
   * 测试host method组合
   */
  @Test
  public void testHostMethodMatch(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.delete(loops.getConfig().getRouterPort(), "localhost", "/").putHeader("Host", "www.request.cn")
      .send(ar -> testContext.verify(() -> {
        String result = ar.result().bodyAsString();
        //请求成功返回临时创建的测试服务2返回的信息server2 response success
        System.out.println(result);
        Assert.isTrue(result.equals("server2 response success"));
        testContext.completeNow();
      }));
  }

  @Test
  public void testHostMethodNotMatched(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/").putHeader("Host", "www.request.cn")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server2 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
    client.delete(loops.getConfig().getRouterPort(), "localhost", "/").putHeader("Host", "www.response.cn")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server2 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
  }

  /**
   * 测试method path组合
   */
  @Test
  public void testMethodPathMatch(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.patch(loops.getConfig().getRouterPort(), "localhost", "/methodPath")
      .send(ar -> testContext.verify(() -> {
        String result = ar.result().bodyAsString();
        //请求成功返回临时创建的测试服务2返回的信息server2 response success
        System.out.println(result);
        Assert.isTrue(result.equals("server2 response success"));
        testContext.completeNow();
      }));
  }

  @Test
  public void testMethodPathNotMatched(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(loops.getConfig().getRouterPort(), "localhost", "/methodPath")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server2 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
    client.patch(loops.getConfig().getRouterPort(), "localhost", "/")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server2 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
  }

  /**
   * 测试host method path组合
   */
  @Test
  public void testPathHostMethodMatch(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.put(loops.getConfig().getRouterPort(), "localhost", "/pathHostMethod").putHeader("Host", "cty.cn")
      .send(ar -> testContext.verify(() -> {
        String result = ar.result().bodyAsString();
        //请求成功返回临时创建的测试服务1返回的信息server1 response success
        System.out.println(result);
        Assert.isTrue(result.equals("server1 response success"));
        testContext.completeNow();
      }));
  }

  @Test
  public void testPathHostMethodNotMatched(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.put(loops.getConfig().getRouterPort(), "localhost", "/pathHostMethod")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server1 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
    client.get(loops.getConfig().getRouterPort(), "localhost", "/pathHostMethod").putHeader("Host", "cty.cn")
      .send(ar -> testContext.verify(() -> {
        //未匹配到路由
        String result = ar.result().bodyAsString();
        System.out.println(result);
        Assert.isFalse(result.equals("server1 response success"));
        Assert.isTrue(result.contains("10001"));
        testContext.completeNow();
      }));
  }

}
