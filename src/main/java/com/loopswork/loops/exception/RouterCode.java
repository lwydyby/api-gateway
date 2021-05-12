package com.loopswork.loops.exception;

/**
 * 路由服务相关Code
 *
 * @author Codi
 */
public enum RouterCode {
  /**
   * 请求成功
   */
  SUCCESS(0, 200, "Success"),
  /**
   * 内部错误
   */
  INTERNAL_ERROR(500, 500, "Internal error, please check logs"),
  /**
   * 无匹配路由
   */
  NO_ROUTE_MATCHED(10001, 404, "No Route matched"),
  /**
   * 上游请求错误
   */
  REQUEST_ERROR(10002, 502, "Request upstream error"),
  /**
   * 网关状态异常错误
   */
  ROUTER_ERROR(10003, 400, "Router state error"),
  /**
   * 路由服务未就绪
   */
  ROUTER_NOT_READY(10004, 400, "Router not ready"),
  /**
   * 路由被拦截
   */
  ROUTE_BLOCKED(10005, 403, "Route is blocked"),
  /**
   * 服务已关闭
   */
  STOP_ERROR(10006, 403, "Service Has stopped"),
  /**
   * 负载均衡上游服务没有目标
   */
  UPSTREAM_NO_TARGET(11001, 503, "Upstream have no target"),
  /**
   * 负载均衡上游服务没有目标
   */
  UPSTREAM_ALL_TARGETS_ERROR(11001, 503, "Upstream have no healthy target"),
  /**
   * IP地址不允许访问
   */
  IP_NOT_ALLOWED(20001, 403, "Your IP address is not allowed"),
  /**
   * 触发API限流控制
   */
  RATE_LIMIT_EXCEEDED(20002, 409, "API rate limit exceeded"),
  /**
   * key-auth认证失败
   */
  KEY_AUTH_FAILED(30001, 403, "Key-Auth authentication failed"),
  /**
   * group不允许访问
   */
  GROUP_NOT_ALLOWED(30002, 403, "Consumer's group is not allowed"),

  /**
   * JWT认证失败
   */
  JWT_FAILED(30003, 401, "JWT authentication failed");

  private int code;
  private int status;
  private String message;

  RouterCode(int code, int status, String message) {
    this.code = code;
    this.status = status;
    this.message = message;
  }

  @Override
  public String toString() {
    return message;
  }

  public int getCode() {
    return code;
  }

  public int getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
