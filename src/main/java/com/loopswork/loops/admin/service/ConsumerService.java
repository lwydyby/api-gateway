package com.loopswork.loops.admin.service;

import com.google.inject.Singleton;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.admin.handler.RequestBodyHandler;
import com.loopswork.loops.entity.Consumer;
import com.loopswork.loops.exception.SimpleException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import static com.loopswork.loops.entity.SimpleCode.CONSUMER_NAME_OR_ID_EMPTY;

/**
 * @author  lixiaoxiao
 * @date  2020/5/8 15:45
 */
@Singleton
public class ConsumerService extends LoopsEntityService{

  @Override
  public <T extends LoopsEntity, R> void add(RoutingContext context, Class<R> requestClazz, Class<T> entityClazz, Handler<AsyncResult<Void>> handler) {
    if (consumerRequestBodyCheck(context)){
      super.add(context, requestClazz, entityClazz, handler);
    } else {
      handler.handle(Future.failedFuture(new SimpleException(CONSUMER_NAME_OR_ID_EMPTY)));
    }
  }

  @Override
  public <T extends LoopsEntity, R> void update(RoutingContext context, String nameOrId, Class<R> requestClazz, Class<T> entityClazz, Handler<AsyncResult<Void>> handler) {
    if (consumerRequestBodyCheck(context)){
      super.update(context, nameOrId, requestClazz, entityClazz, handler);
    } else {
      handler.handle(Future.failedFuture(new SimpleException(CONSUMER_NAME_OR_ID_EMPTY)));
    }
  }

  public boolean consumerRequestBodyCheck(RoutingContext context){
    Consumer consumerEntity = context.get(RequestBodyHandler.REQUEST_BODY_KEY);
    String name = consumerEntity.getName();
    String customId = consumerEntity.getCustomId();
    return !name.trim().equals("") || !customId.trim().equals("");
  }

}
