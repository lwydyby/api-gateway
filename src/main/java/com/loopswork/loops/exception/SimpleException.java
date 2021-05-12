package com.loopswork.loops.exception;


import com.loopswork.loops.entity.SimpleCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Codi on 2019-03-21.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleException extends RuntimeException {
  private int code;
  private String message;
  private Object data;

  public SimpleException(SimpleCode simpleCode) {
    code = simpleCode.getCode();
    message = simpleCode.getMessage();
  }

  public SimpleException(int code) {
    this.code = code;
    this.message = "";
  }

  public SimpleException(int code, String message) {
    this.code = code;
    this.message = message;
  }

}
