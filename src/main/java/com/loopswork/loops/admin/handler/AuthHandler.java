package com.loopswork.loops.admin.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.service.UserService;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.User;
import com.loopswork.loops.exception.SimpleException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import static com.loopswork.loops.entity.SimpleCode.USER_AUTH_FAILED;

/**
 * @author liwei
 * @description 鉴权处理器
 * @date 2019-12-04 16:14
 */
@Singleton
public class AuthHandler implements Handler<RoutingContext> {
  @Inject
  UserService userService;
  @Inject
  LoopsConfig config;

  @Override
  public void handle(RoutingContext context) {
    if (!config.isAdminAuth() || context.request().path().equals("/") || context.request().path().contains("login")) {
      //不使用权限检查
      context.next();
    } else {
      //权限检查
      String token = context.request().getHeader("loops-token");
      if (userService.verifyToken(token)) {
        //验证通过
        context.next();
      } else {
        context.fail(new SimpleException(USER_AUTH_FAILED));
      }
    }
  }

  public void login(RoutingContext context, Handler<AsyncResult<User>> returnHandler) {
    String username = context.request().getFormAttribute("username");
    String password = context.request().getFormAttribute("password");
    userService.login(username, password, handler -> {
      if (handler.succeeded()) {
        User user = handler.result();
        returnHandler.handle(Future.succeededFuture(user));
      } else {
        returnHandler.handle(Future.failedFuture(handler.cause()));
      }
    });
  }

}
