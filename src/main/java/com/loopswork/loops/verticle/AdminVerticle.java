package com.loopswork.loops.verticle;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.entity.dto.*;
import com.loopswork.loops.admin.handler.*;
import com.loopswork.loops.admin.service.*;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.*;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * @author liwei
 * @description 处理配置增删改查
 * @date 2019-11-25 16:46
 */
@Singleton
public class AdminVerticle extends RestAPIVerticle {
  private Logger log;
  @Inject
  private PluginService pluginService;
  @Inject
  private TargetService targetService;
  @Inject
  private LoopsEntityService entityService;
  @Inject
  private ServerService serverService;
  @Inject
  private RouteService routeService;
  @Inject
  private ConsumerService consumerService;
  @Inject
  private StatusHandler statusHandler;
  @Inject
  private LoopsConfig config;
  @Inject
  private AuthHandler authHandler;
  @Inject
  private Managers managers;

  @Override
  public void start(Promise<Void> startFuture) {
    log = LoggerFactory.getLogger(this.getClass());
    int port = config.getAdminPort();
    Router router = Router.router(vertx);
    enableCorsSupport(router);
    router.route().handler(BodyHandler.create());
    //鉴权
    router.route().handler(authHandler);
    //服务相关接口
    setServerController(router);
    //路由相关接口
    setRouteController(router);
    //租户相关接口
    setConsumerController(router);
    //插件相关接口
    setPluginController(router);
    //上游相关接口
    setUpstreamController(router);
    //目标相关接口
    setTargetController(router);
    //ACL相关接口
    setAclsController(router);
    //keyAuth相关接口
    setKeyAuthController(router);
    //user接口
    setUserController(router);
    //状态相关接口
    setStatusController(router);
    //处理404
    setNotFoundController(router);
    //处理异常
    router.route().failureHandler(context -> {
      Throwable ex = context.failure();
      if (ex instanceof SimpleException) {
        context.response().setStatusCode(400)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("code", ((SimpleException) ex).getCode()).put("message", ex.getMessage()).put("data", ((SimpleException) ex).getData()).toBuffer());
      } else {
        context.response().setStatusCode(500)
          .putHeader("content-type", "application/json")
          .end(new JsonObject().put("code", "500").put("message", ex.getMessage()).toBuffer());
      }
      log.error("fail to route:", ex);
    });
    vertx.createHttpServer().requestHandler(router).listen(port, as -> {
      if (as.succeeded()) {
        log.info("Loops admin HTTP server start on port:{}", port);
        startFuture.handle(Future.succeededFuture());
      } else {
        log.error("Loops admin HTTP server start fail:", as.cause());
        startFuture.handle(Future.failedFuture(as.cause()));
      }
    });
  }

  private void setServerController(Router router) {
    router.post("/servers")
      .handler(RequestBodyHandler.create(managers, ServerRequest.class, Server.class))
      .handler(FieldCheckHandler.create(managers, Server.class, "name", FieldCheckType.NOT_EXISTS))
      .handler(context -> serverService.add(context, ServerRequest.class, Server.class, returnHandler(context)));
    router.put("/servers/:id")
      .handler(RequestBodyHandler.create(managers, ServerRequest.class, Server.class))
      .handler(FieldCheckHandler.create(managers, Server.class, "name", FieldCheckType.NOT_EXISTS))
      .handler(context -> serverService.update(context, context.pathParam("id"), ServerRequest.class, Server.class, returnHandler(context)));
    router.delete("/servers/:id").handler(context -> serverService.remove(context.pathParam("id"), Server.class, returnHandler(context)));
    router.get("/servers").handler(context -> serverService.list(Server.class, returnHandler(context)));
    router.get("/servers/:id").handler(context -> serverService.get(context.pathParam("id"), Server.class, returnHandler(context)));
  }

  private void setRouteController(Router router) {
    router.post("/routes")
      .handler(RequestBodyHandler.create(managers, RouteRequest.class, Route.class))
      .handler(FieldCheckHandler.create(managers, Route.class, "name", FieldCheckType.NOT_EXISTS))
      .handler(FieldCheckHandler.create(managers, Server.class, "serverId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(context -> routeService.add(context, RouteRequest.class, Route.class, returnHandler(context)));
    router.put("/routes/:id")
      .handler(RequestBodyHandler.create(managers, RouteRequest.class, Route.class))
      .handler(FieldCheckHandler.create(managers, Route.class, "name", FieldCheckType.NOT_EXISTS))
      .handler(FieldCheckHandler.create(managers, Server.class, "serverId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(context -> routeService.update(context, context.pathParam("id"), RouteRequest.class, Route.class, returnHandler(context)));
    router.delete("/routes/:id").handler(context -> routeService.remove(context.pathParam("id"), Route.class, returnHandler(context)));
    router.get("/routes").handler(context -> routeService.list(Route.class, returnHandler(context)));
    router.get("/routes/:id").handler(context -> routeService.get(context.pathParam("id"), Route.class, returnHandler(context)));
  }

  private void setConsumerController(Router router) {
    router.post("/consumers")
      .handler(RequestBodyHandler.create(managers, ConsumerRequest.class, Consumer.class))
      .handler(FieldCheckHandler.create(managers, Consumer.class, "name", FieldCheckType.NOT_EXISTS))
      .handler(FieldCheckHandler.create(managers, Consumer.class, "customId", FieldCheckType.NOT_EXISTS))
      .handler(context -> consumerService.add(context, ConsumerRequest.class, Consumer.class, returnHandler(context)));
    router.put("/consumers/:id")
      .handler(RequestBodyHandler.create(managers, ConsumerRequest.class, Consumer.class))
      .handler(FieldCheckHandler.create(managers, Consumer.class, "name", FieldCheckType.NOT_EXISTS))
      .handler(FieldCheckHandler.create(managers, Consumer.class, "customId", FieldCheckType.NOT_EXISTS))
      .handler(context -> consumerService.update(context, context.pathParam("id"), ConsumerRequest.class, Consumer.class, returnHandler(context)));
    router.delete("/consumers/:id").handler(context -> consumerService.remove(context.pathParam("id"), Consumer.class, returnHandler(context)));
    router.get("/consumers").handler(context -> consumerService.list(Consumer.class, returnHandler(context)));
    router.get("/consumers/:id").handler(context -> consumerService.get(context.pathParam("id"), Consumer.class, returnHandler(context)));
  }

  private void setPluginController(Router router) {
    router.get("/plugins/names")
      .handler(pluginService::getPluginName);
    router.get("/plugins/config/:name")
      .handler(context -> pluginService.getPluginConfig(context.pathParam("name"), context));
    router.post("/plugins")
      .handler(RequestBodyHandler.create(managers, PluginRequest.class, Plugin.class))
      .handler(pluginService::checkPlugin)
      .handler(FieldCheckHandler.create(managers, Server.class, "serverId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(FieldCheckHandler.create(managers, Route.class, "routeId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(FieldCheckHandler.create(managers, Consumer.class, "consumerId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(context -> entityService.add(context, PluginRequest.class, Plugin.class, returnHandler(context)));
    router.put("/plugins/:id")
      .handler(RequestBodyHandler.create(managers, PluginRequest.class, Plugin.class))
      .handler(pluginService::checkPlugin)
      .handler(FieldCheckHandler.create(managers, Server.class, "serverId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(FieldCheckHandler.create(managers, Route.class, "routeId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(FieldCheckHandler.create(managers, Consumer.class, "consumerId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(context -> entityService.update(context, context.pathParam("id"), PluginRequest.class, Plugin.class, returnHandler(context)));
    router.delete("/plugins/:id").handler(context -> entityService.remove(context.pathParam("id"), Plugin.class, returnHandler(context)));
    router.get("/plugins").handler(context -> entityService.list(Plugin.class, returnHandler(context)));
    router.get("/plugins/:id").handler(context -> entityService.get(context.pathParam("id"), Plugin.class, returnHandler(context)));
  }

  private void setUpstreamController(Router router) {
    router.post("/upstreams")
      .handler(RequestBodyHandler.create(managers, UpstreamRequest.class, Upstream.class))
      .handler(FieldCheckHandler.create(managers, Upstream.class, "name", FieldCheckType.NOT_EXISTS))
      .handler(context -> entityService.add(context, UpstreamRequest.class, Upstream.class, returnHandler(context)));
    router.put("/upstreams/:id")
      .handler(RequestBodyHandler.create(managers, UpstreamRequest.class, Upstream.class))
      .handler(FieldCheckHandler.create(managers, Upstream.class, "name", FieldCheckType.NOT_EXISTS))
      .handler(context -> entityService.update(context, context.pathParam("id"), UpstreamRequest.class, Upstream.class, returnHandler(context)));
    router.delete("/upstreams/:id").handler(context -> entityService.remove(context.pathParam("id"), Upstream.class, returnHandler(context)));
    router.get("/upstreams").handler(context -> entityService.list(Upstream.class, returnHandler(context)));
    router.get("/upstreams/:id").handler(context -> entityService.get(context.pathParam("id"), Upstream.class, returnHandler(context)));
  }

  private void setTargetController(Router router) {
    router.post("/targets")
      .handler(RequestBodyHandler.create(managers, TargetRequest.class, Target.class))
      .handler(FieldCheckHandler.create(managers, Target.class, "name", FieldCheckType.NOT_EXISTS))
      .handler(FieldCheckHandler.create(managers, Upstream.class, "upstreamId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(context -> targetService.add(context, TargetRequest.class, Target.class, returnHandler(context)));
    router.put("/targets/:id")
      .handler(RequestBodyHandler.create(managers, TargetRequest.class, Target.class))
      .handler(FieldCheckHandler.create(managers, Target.class, "name", FieldCheckType.NOT_EXISTS))
      .handler(FieldCheckHandler.create(managers, Upstream.class, "upstreamId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(context -> targetService.update(context, context.pathParam("id"), TargetRequest.class, Target.class, returnHandler(context)));
    router.delete("/targets/:id").handler(context -> targetService.remove(context.pathParam("id"), Target.class, returnHandler(context)));
    router.get("/targets").handler(context -> targetService.list(Target.class, returnHandler(context)));
    router.get("/targets/:id").handler(context -> targetService.get(context.pathParam("id"), Target.class, returnHandler(context)));
    router.get("/upstreams/:upstreamId/targets").handler(context -> targetService.list(context.pathParam("upstreamId"), returnHandler(context)));
  }

  private void setKeyAuthController(Router router) {
    router.post("/key-auth")
      .handler(RequestBodyHandler.create(managers, KeyAuthCredentialsRequest.class, KeyAuthCredentials.class))
      .handler(FieldCheckHandler.create(managers, Consumer.class, "consumerId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(context -> entityService.add(context, KeyAuthCredentialsRequest.class, KeyAuthCredentials.class, returnHandler(context)));
    router.put("/key-auth/:id")
      .handler(RequestBodyHandler.create(managers, KeyAuthCredentialsRequest.class, KeyAuthCredentials.class))
      .handler(FieldCheckHandler.create(managers, Consumer.class, "consumerId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(context -> entityService.update(context, context.pathParam("id"), KeyAuthCredentialsRequest.class, KeyAuthCredentials.class, returnHandler(context)));
    router.delete("/key-auth/:id").handler(context -> entityService.remove(context.pathParam("id"), KeyAuthCredentials.class, returnHandler(context)));
    router.get("/key-auth").handler(context -> entityService.list(KeyAuthCredentials.class, returnHandler(context)));
    router.get("/key-auth/:id").handler(context -> entityService.get(context.pathParam("id"), KeyAuthCredentials.class, returnHandler(context)));
  }

  private void setAclsController(Router router) {
    router.post("/acls")
      .handler(RequestBodyHandler.create(managers, ACLRequest.class, ACL.class))
      .handler(FieldCheckHandler.create(managers, Consumer.class, "consumerId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(context -> entityService.add(context, ACLRequest.class, ACL.class, returnHandler(context)));
    router.put("/acls/:id")
      .handler(RequestBodyHandler.create(managers, ACLRequest.class, ACL.class))
      .handler(FieldCheckHandler.create(managers, Consumer.class, "consumerId", FieldCheckHandler.NAME_OR_ID, FieldCheckType.EXISTS))
      .handler(context -> entityService.update(context, context.pathParam("id"), ACLRequest.class, ACL.class, returnHandler(context)));
    router.delete("/acls/:id").handler(context -> entityService.remove(context.pathParam("id"), ACL.class, returnHandler(context)));
    router.get("/acls").handler(context -> entityService.list(ACL.class, returnHandler(context)));
    router.get("/acls/:id").handler(context -> entityService.get(context.pathParam("id"), ACL.class, returnHandler(context)));
  }

  private void setUserController(Router router) {
    router.post("/login").handler(context -> authHandler.login(context, returnHandler(context)));
  }

  /**
   * 状态检查控制器
   */
  private void setStatusController(Router router) {
    router.get("/").handler(context -> statusHandler.getStatus(returnHandler(context)));
  }

  private void setNotFoundController(Router router) {
    router.route().handler(routingContext -> routingContext.response().setStatusCode(404)
      .putHeader("content-type", "application/json")
      .end(new JsonObject().put("error", "not found").toBuffer()));
  }

}
