package com.loopswork.loops.util;

/**
 * @author liwei
 * @description 字符串工具类
 * @date 2019-11-19 14:19
 */
public class StringUtils {

  public static boolean isEmpty(String value) {
    return value == null || "".equals(value);
  }
}
