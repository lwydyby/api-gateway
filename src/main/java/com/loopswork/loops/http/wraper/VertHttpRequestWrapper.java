package com.loopswork.loops.http.wraper;

import com.loopswork.loops.entity.TargetInfo;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;

/**
 * Created by Codi on 2019-04-09.
 */

public class VertHttpRequestWrapper {

  public static void request(com.loopswork.loops.http.entity.HttpRequest request, TargetInfo targetInfo, HttpRequest<Buffer> wrapped) {
    wrapped.host(targetInfo.getHost());
    wrapped.uri(targetInfo.getRemoteUri());
    wrapped.port(targetInfo.getPort());
    wrapped.method(transMethod(request.getMethod()));
    request.getHeaders().forEach(wrapped::putHeader);
    request.getHeaders().remove("Content-Length");
  }

  public static HttpMethod transMethod(com.loopswork.loops.entity.HttpMethod method) {
    switch (method) {
      case GET:
        return HttpMethod.GET;
      case OPTIONS:
        return HttpMethod.OPTIONS;
      case DELETE:
        return HttpMethod.DELETE;
      case TRACE:
        return HttpMethod.TRACE;
      case PATCH:
        return HttpMethod.PATCH;
      case POST:
        return HttpMethod.POST;
      case HEAD:
        return HttpMethod.HEAD;
      case PUT:
        return HttpMethod.PUT;
      default:
        return HttpMethod.OTHER;
    }
  }
}
