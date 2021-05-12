package com.loopswork.loops.entity;

/**
 * Created by Codi on 2019-03-21.
 */
public enum SimpleCode {
  SUCCESS(0, "成功"),
  NOT_FOUND(404, "无效地址"),
  NOT_AUTH(401, "用户验证失败"),
  INTERNAL_ERROR(500, "服务器内部错误"),
  INTERNAL_JSON_ERROR(501, "JSON处理失败"),
  REQUEST_PARAM_ERROR(2, "传入参数错误"),
  REQUEST_METHOD_ERROR(3, "请求方法错误"),
  REQUEST_VALIDATE_ERROR(7, "传入参数校验失败"),
  REQUEST_CONTENT_TYPE_ERROR(5, "需要 application/json 请求"),
  AUTH_LESS(10, "缺少授权信息"),
  AUTH_TIME_OUT(11, "授权信息失效"),
  AUTH_ERROR(12, "授权信息验证失败"),
  AUTH_NEED_ADMIN_AUTH(13, "缺少超级权限"),
  AUTH_NEED_MASTER_AUTH(14, "缺少管理权限"),
  AUTH_REPLAY_ATTACK(15, "拒绝重放攻击"),
  UPDATE_NOT_ALLOW(20, "当前配置路由信息禁止更新"),
  //1xx 通用逻辑
  REQUEST_JSON_ERROR(101, "JSON格式错误"),
  REQUEST_BODY_EMPTY(106, "传入Body不能为空"),
  ENTITY_TYPE_NOT_EXISTS(120, "实体类不存在"),
  ENTITY_NOT_EXISTS(121, "对象不存在 无法更新"),
  ENTITY_RELATION_ALREADY_EXISTS(131, "关联对象已存在"),
  ENTITY_RELATION_NOT_EXISTS(132, "关联对象不存在"),
  ENTITY_RELATION_FIELD_EMPTY(133, "关联字段不能为空"),
  //1xxx 服务相关
  SERVER_NAME_EXISTS(1001, "服务名已存在"),
  SERVER_NAME_ERROR(1002, "服务名格式错误"),
  SERVER_NAME_OR_ID_EMPTY(1003, "服务查询条件不能为空"),
  SERVER_NOT_EXISTS(1004, "服务不存在"),
  SERVER_CANT_DELETE(1005, "服务存在关联路由不能删除"),
  SERVER_HOST_ERROR(1006, "服务host格式不正确"),
  //2xxx 路由相关
  ROUTE_NAME_EXISTS(2001, "路由名已存在"),
  ROUTE_NAME_OR_ID_EMPTY(2002, "路由查询条件不能为空"),
  ROUTE_NOT_EXISTS(2003, "路由不存在"),
  ROUTE_INFO_ERROR(2004, "methods hosts paths 不能同时为空"),
  //3xxx 插件相关
  PLUGIN_NAME_EXISTS(3001, "插件名已存在"),
  PLUGIN_NAME_ERROR(3002, "插件名格式错误"),
  PLUGIN_NAME_OR_ID_EMPTY(3003, "插件名和插件id不能同时为空"),
  PLUGIN_NOT_EXISTS(3004, "插件不存在"),
  PLUGIN_ERROR_TYPE(3005, "插件配置不合法"),
  //4xxx 租户相关
  CONSUMER_NAME_EXISTS(4001, "租户名已存在"),
  CONSUMER_NAME_ERROR(4002, "租户名格式错误"),
  CONSUMER_NAME_OR_ID_EMPTY(4003, "租户名和租户id不能同时为空"),
  CONSUMER_NOT_EXISTS(4004, "租户不存在"),
  CONSUMER_KEY_EXISTS(4005, "租户key已存在"),
  //5xxx 上游服务相关
  UPSTREAM_NAME_EXISTS(5001, "上游服务名称已存在"),
  UPSTREAM_NAME_ERROR(5002, "上游服务名称格式错误"),
  UPSTREAM_NAME_OR_ID_EMPTY(5003, "上游服务名和id不能同时为空"),
  UPSTREAM_NOT_EXISTS(5004, "上游服务不存在"),
  UPSTREAM_CANT_DELETE(5005, "上游服务存在关联目标不能删除"),
  TARGET_NOT_EXISTS(5101, "目标不存在"),
  TARGET_HOST_CANT_EMPTY(5102, "目标host不能为空"),
  //6xxx 用户登录相关
  USER_NOT_EXISTS(6001, "用户不存在"),
  USER_PASSWORD_WRONG(6002, "密码错误"),
  USER_AUTH_FAILED(6000, "用户鉴权失败");

  private int code;
  private String message;

  SimpleCode(int code, String message) {
    this.code = code;
    this.message = message;
  }

  @Override
  public String toString() {
    return message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
