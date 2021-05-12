package com.loopswork.loops.admin.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.github.houbb.valid.api.api.constraint.IConstraintResult;
import com.github.houbb.valid.api.api.result.IResult;
import com.github.houbb.valid.core.api.result.ResultHandlers;
import com.github.houbb.valid.core.bs.ValidBs;
import com.github.houbb.valid.jsr.api.validator.JsrValidator;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.entity.SimpleCode;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author codi
 * @date 2020/3/22 10:10 上午
 * @description 请求体处理器
 */
public class RequestBodyHandler implements Handler<RoutingContext> {
  public static final String REQUEST_BODY_KEY = "REQUEST_BODY";
  private Managers managers;
  private Class<?> requestClazz;
  private Class<? extends LoopsEntity> entityClazz;

  public RequestBodyHandler(Managers managers, Class<?> requestClazz, Class<? extends LoopsEntity> entityClazz) {
    this.managers = managers;
    this.requestClazz = requestClazz;
    this.entityClazz = entityClazz;
  }

  public static RequestBodyHandler create(Managers managers, Class<?> requestClazz, Class<? extends LoopsEntity> entityClazz) {
    return new RequestBodyHandler(managers, requestClazz, entityClazz);
  }

  @Override
  public void handle(RoutingContext context) {
    String nameOrId = context.pathParam("id");
    JsonObject jsonObject = context.getBodyAsJson();
    if (jsonObject != null) {
      try {
        //序列化对象
        Object dto = jsonObject.mapTo(requestClazz);
        //数据检查
        IResult validate = validate(dto);
        LoopsEntity entity = new LoopsEntity();
        if (!validate.pass()) {
          SimpleException exception = new SimpleException(SimpleCode.REQUEST_VALIDATE_ERROR);
          exception.setMessage(validate.notPassList().get(0).message());
          List<String> messages = validate.notPassList().stream().map(IConstraintResult::message).collect(Collectors.toList());
          exception.setData(messages);
          context.fail(exception);
        } else {
          if (nameOrId != null) {
            //更新
            entity = managers.getEntity(entityClazz, nameOrId.trim());
            if (entity == null) {
              //对象不存在
              context.fail(new SimpleException(SimpleCode.ENTITY_NOT_EXISTS));
              return;
            }
          }
          LoopsEntity requestEntity = entityClazz.newInstance();
          BeanUtil.copyProperties(dto, requestEntity, CopyOptions.create().setIgnoreNullValue(true));
          if (entity.getId() != null){
            requestEntity.setId(entity.getId().trim());
          }
          if (requestEntity.getName() != null){
            requestEntity.setName(requestEntity.getName().trim());
          }
          if (entity.getCreatedAt() != null){
            requestEntity.setCreatedAt(entity.getCreatedAt());
          }
          context.put(REQUEST_BODY_KEY, requestEntity);
          context.next();
        }
      } catch (Exception e) {
        context.fail(new SimpleException(400, e.getMessage()));
      }
    } else {
      context.fail(new SimpleException(SimpleCode.REQUEST_BODY_EMPTY));
    }
  }

  private <R> IResult validate(R requestClazz) {
    return ValidBs.on(requestClazz).valid(JsrValidator.getInstance()).result(ResultHandlers.simple());
  }

}
