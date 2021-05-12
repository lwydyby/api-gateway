package com.loopswork.loops.admin.handler;

import com.google.inject.Inject;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author codi
 * @description 状态控制器
 * @date 2020/2/23 12:46 上午
 */
public class StatusHandler {

  @Inject
  private Managers managers;

  public void getStatus(Handler<AsyncResult<JsonObject>> handler) {
    JsonObject status = new JsonObject();
    status.put("status", managers.getState());
    handler.handle(Future.succeededFuture(status));
  }

}
