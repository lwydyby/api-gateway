package com.loopswork.loops.entity;

/**
 * Created by Codi on 2019-05-10.
 */
public class ContextKeys {
  /**
   * 请求唯一表示
   */
  public static final String ID = "id";
  /**
   * http请求
   */
  public static final String HTTP_REQUEST = "httpRequest";
  /**
   * http响应
   */
  public static final String HTTP_RESPONSE = "httpResponse";
  /**
   * 匹配结果
   */
  public static final String MATCH_RESULT = "matchResult";
  /**
   * 目标信息
   */
  public static final String TARGET_INFO = "targetInfo";
  /**
   * 请求开始时间
   */
  public static final String TIME_START = "timeStart";
  /**
   * 请求结束时间
   */
  public static final String TIME_END = "timeEnd";
  /**
   * 请求持续时间(ms)
   */
  public static final String TIME_DURATION = "timeDuration";
  /**
   * 各处理器耗时
   */
  public static final String HANDLER_TIME = "handlerTime";
  /**
   * key-auth标识
   */
  public static final String KEY_AUTH = "keyAuth";
  /**
   * 租户id
   */
  public static final String CONSUMER_ID = "consumerId";
  /**
   * jwt payload信息
   */
  public static final String JWT_TOKEN = "jwtToken";
}
