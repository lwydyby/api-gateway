package com.loopswork.loops.entity;

import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import lombok.Data;

/**
 * Created by Codi on 2019-04-16.
 */
@Data
public class SimpleResponse {
  private int code;
  private String message;

  public SimpleResponse(RouterCode routerCode) {
    this(routerCode.getCode(), routerCode.getMessage());
  }

  public SimpleResponse(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public static SimpleResponse success() {
    return new SimpleResponse(RouterCode.SUCCESS);
  }

  public static SimpleResponse error(RouterCode simpleCode) {
    return new SimpleResponse(simpleCode);
  }

  public static SimpleResponse fromException(RouterException e) {
    return new SimpleResponse(e.getCode(), e.getMessage());
  }

}
