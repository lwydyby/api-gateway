package com.loopswork.loops.admin.handler;

import cn.hutool.core.util.ObjectUtil;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.admin.utils.ReflectUtils;
import com.loopswork.loops.entity.SimpleCode;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.manager.Managers;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author codi
 * @date 2020/3/21 4:09 下午
 * @description 字段检查处理器
 */
public class FieldCheckHandler implements Handler<RoutingContext> {
  public static final String NAME_OR_ID = "NAME_OR_ID";
  private Managers managers;
  /**
   * 传入的实体类中待检查字段
   */
  private String field;
  /**
   * 被检查的实体类中被检查字段
   */
  private String column;
  /**
   * 目标实体类型
   */
  private Class<? extends LoopsEntity> clazz;
  /**
   * 检查类型
   */
  private FieldCheckType type;

  public FieldCheckHandler(Managers managers, Class<? extends LoopsEntity> clazz, String field, String column, FieldCheckType type) {
    this.clazz = clazz;
    this.field = field;
    this.column = column;
    this.type = type;
    this.managers = managers;
  }

  public static FieldCheckHandler create(Managers managers, Class<? extends LoopsEntity> clazz, String field, FieldCheckType type) {
    return new FieldCheckHandler(managers, clazz, field, field, type);
  }

  public static FieldCheckHandler create(Managers managers, Class<? extends LoopsEntity> clazz, String field, String column, FieldCheckType type) {
    return new FieldCheckHandler(managers, clazz, field, column, type);
  }

  @Override
  public void handle(RoutingContext context) {
    //取出请求对象
    LoopsEntity entity = context.get(RequestBodyHandler.REQUEST_BODY_KEY);
    //取出要检查的字段
    Object fieldValue = ReflectUtils.getFieldValue(entity, field);
    if (ObjectUtil.isEmpty(fieldValue)) {
      //字段为空 不检查
      context.next();
    } else {
      List<? extends LoopsEntity> list = managers.getEntityList(clazz);
      Optional<? extends LoopsEntity> exist = list.stream().filter(e -> {
        if (NAME_OR_ID.equals(column)) {
          //名称或id匹配
          Object name = ReflectUtils.getFieldValue(e, "name");
          Object id = ReflectUtils.getFieldValue(e, "id");
          ReflectUtils.setFieldValue(entity, field, id);
          return fieldValue.equals(name) || fieldValue.equals(id);
        } else {
          //字段匹配
          Object value = ReflectUtils.getFieldValue(e, column);
          return fieldValue.equals(value);
        }
      }).findFirst();
      if (exist.isPresent()) {
        //查到对象
        if (type == FieldCheckType.EXISTS) {
          context.next();
        } else {
          //对象已存在 冲突
          String newId = entity.getId();
          String oldId = exist.get().getId();
          if (oldId.equals(newId)) {
            //更新对象时不检查
            context.next();
          } else {
            SimpleException e = new SimpleException(SimpleCode.ENTITY_RELATION_ALREADY_EXISTS);
            e.setMessage(field + " 已存在");
            context.fail(e);
          }
        }
      } else {
        //未查到对象
        if (type == FieldCheckType.NOT_EXISTS) {
          context.next();
        } else {
          SimpleException e = new SimpleException(SimpleCode.ENTITY_RELATION_NOT_EXISTS);
          e.setMessage(field + " 不存在");
          context.fail(e);
        }
      }
    }
  }

  public Predicate<? extends LoopsEntity> getFilter(String fieldValue, String column) {
    return e -> {
      Object value = ReflectUtils.getFieldValue(e, column);
      return fieldValue.equals(value);
    };
  }

}
