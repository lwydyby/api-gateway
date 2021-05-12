package com.loopswork.loops.http.entity;


import com.loopswork.loops.entity.HttpMethod;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

/**
 * Created by Codi on 2019-04-10.
 */
@Data
@ToString
public class HttpRequest {

  private String host;
  private String uri;
  private Map<String, String> params;
  private Map<String, String> headers;
  private HttpMethod method;
  private String clientIP;
  private int clientPort;
  private BodyBuffer body;
  private String bodyString;
  private int bodyLength;

}
