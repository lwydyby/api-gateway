package com.loopswork.loops.exception;

/**
 * @ClassName: RouterException
 * @Author: Codi
 * @Date: 2019-04-16 16:31
 */
public class RouterException extends Exception {
  private int code;
  private int status;
  private String message;

  private RouterException(RouterCode simpleCode) {
    this(simpleCode.getCode(), simpleCode.getMessage(), simpleCode.getStatus());
  }

  private RouterException(RouterCode simpleCode, Throwable throwable) {
    super(throwable);
    this.code = simpleCode.getCode();
    this.message = simpleCode.getMessage();
    this.status = simpleCode.getStatus();
  }

  private RouterException(int code, String message, int status) {
    this.code = code;
    this.message = message;
    this.status = status;
  }

  public static RouterException e(RouterCode code) {
    return new RouterException(code);
  }

  public static RouterException e(RouterCode code, Throwable throwable) {
    return new RouterException(code, throwable);
  }

  public static RouterException e(int code, String message, int status) {
    return new RouterException(code, message, status);
  }

  public int getCode() {
    return code;
  }


  public String getMessage() {
    return message;
  }

  public int getStatus() {
    return status;
  }

}
