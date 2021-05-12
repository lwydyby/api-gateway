package com.loopswork.loops.http.entity;

import lombok.Data;

import java.util.Map;

/**
 * Created by Codi on 2019-04-10.
 */
@Data
public class HttpResponse {

  private Map<String, String> headers;
  private int statusCode;
  private String statusMessage;
  private BodyBuffer body;
  private String bodyString;
  private int bodyLength;

}
