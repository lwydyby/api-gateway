package com.loopswork.loops.admin.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.User;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.util.StringUtils;
import com.loopswork.loops.util.UUIDUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import static com.loopswork.loops.entity.SimpleCode.USER_NOT_EXISTS;
import static com.loopswork.loops.entity.SimpleCode.USER_PASSWORD_WRONG;


/**
 * @author liwei
 * @date 2019-12-04 15:11
 * @description 用户服务
 */
@Singleton
public class UserService {
  @Inject
  private LoopsConfig config;
  private String token;

  public void login(String username, String password, Handler<AsyncResult<User>> handler) {
    if (config.getUsername().equals(username)) {
      if (config.getPassword().equals(password)) {
        if (StringUtils.isEmpty(token)) {
          token = UUIDUtil.getUUID();
        }
        User user = new User();
        user.setUsername(username);
        user.setSecretKey(token);
        handler.handle(Future.succeededFuture(user));
      } else {
        handler.handle(Future.failedFuture(new SimpleException(USER_PASSWORD_WRONG)));
      }
    } else {
      handler.handle(Future.failedFuture(new SimpleException(USER_NOT_EXISTS)));
    }
  }

  public boolean verifyToken(String token) {
    return !StringUtils.isEmpty(token) && !StringUtils.isEmpty(this.token) && token.equals(this.token);
  }

}
