package com.loopswork.loops.util;

import java.util.UUID;

/**
 * Created by Codi on 2019-02-19.
 */
public class UUIDUtil {

  public static String getUUID() {
    return UUID.randomUUID().toString().replace("-", "");
  }

}
