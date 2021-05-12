package com.loopswork.loops.http.adaptor;


import com.google.inject.Singleton;
import com.loopswork.loops.entity.HttpMethod;
import com.loopswork.loops.http.entity.BodyBuffer;
import com.loopswork.loops.http.entity.HttpRequest;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Codi on 2019-04-09.
 */
@Singleton
public class VertHttpRequestAdaptor implements IHttpRequestAdaptor<RoutingContext> {
  public static BodyBuffer adaptBody(Buffer buffer) {
    return new BodyBuffer(buffer.getByteBuf());
  }

  public static Map<String, String> adaptParams(RoutingContext context){
    HttpServerRequest httpServerRequest = context.request();
    Map<String, String> params = new HashMap<>();
    httpServerRequest.params().forEach(node -> params.put(node.getKey(), node.getValue()));
    return params;
  }

  @Override
  public HttpRequest request(RoutingContext context) {
    HttpServerRequest httpServerRequest = context.request();
    httpServerRequest.uri();
    HttpRequest request = new HttpRequest();
    Map<String, String> headers = new HashMap<>();
    httpServerRequest.headers().forEach(node -> headers.put(node.getKey(), node.getValue()));
    request.setUri(httpServerRequest.uri());
    request.setClientIP(httpServerRequest.remoteAddress().host());
    request.setClientPort(httpServerRequest.remoteAddress().port());
    request.setHeaders(headers);
    request.setMethod(methodTransform(httpServerRequest.method()));
    request.setHost(httpServerRequest.host());
    return request;
  }

  private HttpMethod methodTransform(io.vertx.core.http.HttpMethod method) {
    switch (method) {
      case GET:
        return HttpMethod.GET;
      case PUT:
        return HttpMethod.PUT;
      case HEAD:
        return HttpMethod.HEAD;
      case POST:
        return HttpMethod.POST;
      case PATCH:
        return HttpMethod.PATCH;
      case TRACE:
        return HttpMethod.TRACE;
      case DELETE:
        return HttpMethod.DELETE;
      case OPTIONS:
        return HttpMethod.OPTIONS;
      case CONNECT:
      case OTHER:
      default:
        return null;
    }
  }

}
