package com.loopswork.loops.exception;

/**
 * @ClassName: MohoException
 * @Author: Fan Zhang
 * @Date: 2019-03-21 16:31
 */
public class LoopsException extends RuntimeException {

  public LoopsException(String message) {
    super(message);
  }

  public LoopsException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
