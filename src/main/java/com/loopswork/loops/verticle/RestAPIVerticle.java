package com.loopswork.loops.verticle;

import com.google.inject.Inject;
import com.loopswork.loops.admin.collector.ICollector;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.HashSet;
import java.util.Set;

import static io.vertx.core.http.HttpMethod.POST;
import static io.vertx.core.http.HttpMethod.PUT;

/**
 * @author liwei
 * @description REST工具类
 * @date 2019-11-25 15:25
 */
public abstract class RestAPIVerticle extends AbstractVerticle {

  @Inject
  private ICollector entityDAO;

  /**
   * 路由配置更新
   */
  private void updateGenerator(RoutingContext context) {
    HttpMethod method = context.request().method();
    if (method == PUT || method == POST | method.equals(HttpMethod.DELETE)) {
      entityDAO.updateGeneration(ar -> {
        //主动触发更新
        vertx.eventBus().send(UpdateVerticle.COLLECTOR_UPDATE_ADDRESS, "");
      });
    }
  }

  protected void enableCorsSupport(Router router) {
    Set<String> allowHeaders = new HashSet<>();
    allowHeaders.add("x-requested-with");
    allowHeaders.add("Access-Control-Allow-Origin");
    allowHeaders.add("origin");
    allowHeaders.add("Content-Type");
    allowHeaders.add("accept");
    allowHeaders.add("loops-token");
    Set<HttpMethod> allowMethods = new HashSet<>();
    allowMethods.add(HttpMethod.GET);
    allowMethods.add(PUT);
    allowMethods.add(HttpMethod.OPTIONS);
    allowMethods.add(POST);
    allowMethods.add(HttpMethod.DELETE);
    allowMethods.add(HttpMethod.PATCH);
    router.route().handler(CorsHandler.create("*")
      .allowedHeaders(allowHeaders)
      .allowedMethods(allowMethods));
  }

  protected <T> Handler<AsyncResult<T>> returnHandler(RoutingContext context) {
    return ar -> {
      if (ar.succeeded()) {
        updateGenerator(context);
        T res = ar.result();
        context.response()
          .putHeader("content-type", "application/json")
          .end(res == null ? "{}" : Json.encode(res));
      } else {
        internalError(context, ar.cause());
        ar.cause().printStackTrace();
      }
    };
  }

  protected void internalError(RoutingContext context, Throwable ex) {
    context.fail(ex);
  }

}
