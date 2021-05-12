package com.loopswork.loops.plugin;

import com.loopswork.loops.LoopsApplication;
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

import static io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * keyAuth Acl插件测试
 *
 * @author  lixiaoxiao
 * @date  2020/3/31
 */
@ExtendWith(VertxExtension.class)
public class KeyAuthAclTest {
  private static LoopsApplication loops;

  private static int port1 = 20001;

  @BeforeAll
  static void beforeAll() {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
  }

  @BeforeEach
  void before(Vertx vertx, VertxTestContext testContext) {
    Future.<Long>future(h -> KeyAuthAclTest.initLoops(vertx, h))
      .compose(time -> Future.<Void>future(h -> KeyAuthAclTest.initTestWebServer(vertx, h)))
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
    entityMap.put(EntityType.server, Collections.singletonList(server1));
    //初始化测试Routes
    Route route1 = new Route();
    route1.setEnable(true);
    route1.setId("route1");
    route1.setServerId(server1.getId());
    route1.setPaths(Collections.singleton("/server1"));
    entityMap.put(EntityType.route, Collections.singletonList(route1));
    //在server1上创建key-auth插件、acl插件
    Plugin keyAuthPlugin = new Plugin();
    keyAuthPlugin.setId("plugin1");
    keyAuthPlugin.setServerId(server1.getId());
    keyAuthPlugin.setRouteId("");
    keyAuthPlugin.setConsumerId("");
    keyAuthPlugin.setEnable(true);
    Map<String, Object> map = new HashMap<>();
    List<String> keyNames = new ArrayList<>();
    keyNames.add("apikey");
    map.put("keyNames", keyNames);
    map.put("keyInBody", false);
    map.put("hideCredentials", false);
    keyAuthPlugin.setConfig(map);
    keyAuthPlugin.setName("key-auth");
    Plugin aclPlugin = new Plugin();
    aclPlugin.setEnable(true);
    aclPlugin.setId("plugin2");
    aclPlugin.setName("acl");
    aclPlugin.setServerId("server1");
    aclPlugin.setRouteId("");
    aclPlugin.setConsumerId("");
    map = new HashMap<>();
    map.put("whitelist", Collections.singletonList("group1"));
    map.put("hideGroupsHeader", false);
    aclPlugin.setConfig(map);
    entityMap.put(EntityType.plugin, Arrays.asList(keyAuthPlugin, aclPlugin));
    //创建租户
    Consumer consumer1 = new Consumer();
    consumer1.setId("consumer1");
    consumer1.setName("test1");
    consumer1.setEnable(true);
    Consumer consumer2 = new Consumer();
    consumer2.setId("consumer2");
    consumer2.setName("test2");
    consumer2.setEnable(true);
    Consumer consumer3 = new Consumer();
    consumer3.setId("consumer3");
    consumer3.setName("test3");
    consumer3.setEnable(true);
    entityMap.put(EntityType.consumer, Arrays.asList(consumer1, consumer2, consumer3));
    //在consumer1 consumer2上创建key-auth
    KeyAuthCredentials keyAuthCredential1 = new KeyAuthCredentials();
    keyAuthCredential1.setId("keyAuth1");
    keyAuthCredential1.setConsumerId(consumer1.getId());
    keyAuthCredential1.setKey("1");
    keyAuthCredential1.setEnable(true);
    KeyAuthCredentials keyAuthCredential2 = new KeyAuthCredentials();
    keyAuthCredential2.setId("keyAuth2");
    keyAuthCredential2.setConsumerId(consumer2.getId());
    keyAuthCredential2.setKey("2");
    keyAuthCredential2.setEnable(true);
    KeyAuthCredentials keyAuthCredential3 = new KeyAuthCredentials();
    keyAuthCredential3.setId("keyAuth3");
    keyAuthCredential3.setConsumerId(consumer3.getId());
    keyAuthCredential3.setKey("3");
    keyAuthCredential3.setEnable(true);
    entityMap.put(EntityType.key_auth, Arrays.asList(keyAuthCredential1, keyAuthCredential2, keyAuthCredential3));
    //在consumer1 consumer3用户上创建acl
    ACL acl1 = new ACL();
    acl1.setId("acl1");
    acl1.setConsumerId(consumer1.getId());
    acl1.setGroups(Collections.singletonList("group1"));
    acl1.setEnable(true);
    ACL acl2 = new ACL();
    acl2.setId("acl2");
    acl2.setConsumerId(consumer3.getId());
    acl2.setGroups(Collections.singletonList("group2"));
    acl2.setEnable(true);
    entityMap.put(EntityType.acl, Arrays.asList(acl1, acl2));
    collector.setEntityMap(entityMap);
  }

  /**
   * 初始化测试用web服务器
   */
  private static void initTestWebServer(Vertx vertx, Handler<AsyncResult<Void>> handler) {
    HttpServer httpServer = vertx.createHttpServer().requestHandler(request -> {
      HttpServerResponse response = request.response();
      response.putHeader("content-type", "text/plain");
      response.end("server response success");
    });
    Future<HttpServer> future1 = Future.future(h -> httpServer.listen(port1, h));
    CompositeFuture.all(Collections.singletonList(future1))
      .onSuccess(success -> handler.handle(Future.succeededFuture()))
      .onFailure(failure -> handler.handle(Future.failedFuture(failure)));
  }

  @Test
  void noKeyAuthTest(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    webClient.get(loops.getConfig().getRouterPort(), "localhost", "/server1")
      .send(ar -> testContext.verify(() -> {
          if (ar.failed()) {
            testContext.failNow(ar.cause());
          } else {
            //未携带key-auth认证返回403拒绝访问
            assertThat(403).isEqualTo(ar.result().statusCode());
            testContext.completeNow();
          }
        })
      );
  }

  @Test
  public void withKeyAuthNoAclTest(Vertx vertx, VertxTestContext testContext){
    //1、使用consumer1用户的key-auth且具备acl,可正常访问
    WebClient webClient = WebClient.create(vertx);
    webClient.get(loops.getConfig().getRouterPort(), "localhost", "/server1?apikey=1")
      .send(ar -> testContext.verify(() -> {
          if (ar.failed()) {
            testContext.failNow(ar.cause());
          } else {
            assertThat(200).isEqualTo(ar.result().statusCode());
            testContext.completeNow();
          }
        })
      );
    //2、使用consumer2用户的key-auth但是不具备acl无法正常访问
    webClient.get(loops.getConfig().getRouterPort(), "localhost", "/server1?apikey=2")
      .send(ar -> testContext.verify(() -> {
          if (ar.failed()) {
            testContext.failNow(ar.cause());
          } else {
            assertThat(403).isEqualTo(ar.result().statusCode());
            testContext.completeNow();
          }
        })
      );
  }

  @Test
  public void witKeyAuthAclWrongGroupTest(Vertx vertx, VertxTestContext testContext){
    WebClient webClient = WebClient.create(vertx);
    webClient.get(loops.getConfig().getRouterPort(), "localhost", "/server1?apikey=3")
      .send(ar -> testContext.verify(() -> {
          if (ar.failed()) {
            testContext.failNow(ar.cause());
          } else {
            //使用consumer3用户的key-auth、acl，但是group错误无法正常访问
            assertThat(403).isEqualTo(ar.result().statusCode());
            testContext.completeNow();
          }
        })
      );
  }

}
