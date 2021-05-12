package com.loopswork.loops.admin.handler;

/**
 * @author codi
 * @date 2020/3/22 9:27 下午
 * @description 字段检查类型
 */
public enum FieldCheckType {
  /**
   * 检查关联对象需要存在
   */
  EXISTS,
  /**
   * 检查关联对象需要不存在
   */
  NOT_EXISTS
}
