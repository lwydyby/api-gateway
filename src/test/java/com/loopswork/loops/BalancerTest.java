package com.loopswork.loops;

import com.loopswork.loops.admin.collector.CollectorType;
import com.loopswork.loops.admin.collector.impl.MockCollector;
import com.loopswork.loops.admin.entity.EntityType;
import com.loopswork.loops.admin.entity.LoopsEntity;
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
import java.util.concurrent.atomic.AtomicInteger;

import static com.loopswork.loops.admin.entity.dto.UpstreamRequest.DEFAULT_PASSIVE_HEALTH_CHECK;
import static io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author codi
 * @date 2020/3/2 10:03 上午
 * @description 负载均衡测试
 */
@ExtendWith(VertxExtension.class)
public class BalancerTest {
  private static LoopsApplication loops;
  private static int port1 = 20001;
  private static int port2 = 20002;

  @BeforeAll
  static void beforeAll() {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
  }

  @BeforeEach
  void before(Vertx vertx, VertxTestContext testContext) {
    Future.<Long>future(h -> BalancerTest.initLoops(vertx, h))
      .compose(time -> Future.<Void>future(h -> BalancerTest.initTestWebServer(vertx, h)))
      .onSuccess(h -> testContext.completeNow())
      .onFailure(testContext::failNow);
  }

  @AfterEach
  void after(Vertx vertx, VertxTestContext testContext) {
    vertx.close(handler -> testContext.completeNow());
  }

  private static void initLoops(Vertx vertx, Handler<AsyncResult<Long>> handler) {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
    loops = new LoopsApplication();
    LoopsConfig config = new LoopsConfig();
    config.setCollector(CollectorType.mock);
    Future.<Long>future(h -> loops.run(vertx, config, h)).compose(time -> {
      MockCollector collector = loops.getInjector().getInstance(MockCollector.class);
      initTestRouters(collector);
      Managers managers = loops.getInjector().getInstance(Managers.class);
      return Future.future(managers::update);
    }).onSuccess(success -> {
      handler.handle(Future.succeededFuture());
    }).onFailure(failure -> {
      handler.handle(Future.failedFuture(failure));
    });
  }

  private static void initTestRouters(MockCollector collector) {
    Map<EntityType, List<LoopsEntity>> entityMap = new HashMap<>();
    //初始化用于测试的路由配置
    Server server = new Server();
    server.setId("server");
    server.setEnable(true);
    server.setHost("balancer");
    entityMap.put(EntityType.server, Collections.singletonList(server));
    Route route = new Route();
    route.setEnable(true);
    route.setId("route");
    route.setServerId(server.getId());
    route.setMethods(Collections.singleton(HttpMethod.GET));
    entityMap.put(EntityType.route, Collections.singletonList(route));
    Upstream upstream = new Upstream();
    upstream.setId("upstream");
    upstream.setEnable(true);
    upstream.setName("balancer");
    upstream.setSlots(4);
    upstream.setPassiveHealthCheck(DEFAULT_PASSIVE_HEALTH_CHECK);
    entityMap.put(EntityType.upstream, Collections.singletonList(upstream));
    Target target1 = new Target();
    target1.setId("target1");
    target1.setHost("localhost");
    target1.setPort(port1);
    target1.setWeight(100);
    target1.setUpstreamId(upstream.getId());
    Target target2 = new Target();
    target2.setId("target2");
    target2.setHost("localhost");
    target2.setPort(port2);
    target2.setWeight(100);
    target2.setUpstreamId(upstream.getId());
    entityMap.put(EntityType.target, Arrays.asList(target1, target2));
    collector.setEntityMap(entityMap);
  }

  /**
   * 初始化测试用web服务器
   */
  private static void initTestWebServer(Vertx vertx, Handler<AsyncResult<Void>> handler) {
    HttpServer httpServer1 = vertx.createHttpServer().requestHandler(request -> {
      HttpServerResponse response = request.response();
      response.putHeader("content-type", "text/plain");
      response.end("1");
    });
    HttpServer httpServer2 = vertx.createHttpServer().requestHandler(request -> {
      HttpServerResponse response = request.response();
      response.putHeader("content-type", "text/plain");
      response.end("2");
    });
    Future<HttpServer> future1 = Future.<HttpServer>future(h -> httpServer1.listen(port1, h));
    Future<HttpServer> future2 = Future.<HttpServer>future(h -> httpServer2.listen(port2, h));
    CompositeFuture.all(future1, future2)
      .onSuccess(success -> {
        handler.handle(Future.succeededFuture());
      })
      .onFailure(failure -> {
        handler.handle(Future.failedFuture(failure));
      });
  }

  @Test
  void TestRouter(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    AtomicInteger count = new AtomicInteger();
    AtomicInteger server1 = new AtomicInteger();
    AtomicInteger server2 = new AtomicInteger();
    for (int i = 0; i < 33; i++) {
      client.get(loops.getConfig().getRouterPort(), "localhost", "/").send(ar -> testContext.verify(() -> {
        if (count.getAndAdd(1) < 32) {
          if (ar.result().bodyAsString().equals("1")) {
            server1.addAndGet(1);
          } else if (ar.result().bodyAsString().equals("2")) {
            server2.addAndGet(1);
          }
        } else {
          assertThat(server1.get()).isEqualTo(16);
          assertThat(server2.get()).isEqualTo(16);
          testContext.completeNow();
        }
      }));
    }

  }

}
