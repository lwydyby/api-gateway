package com.loopswork.loops.http.adaptor;

import com.loopswork.loops.http.entity.BodyBuffer;
import com.loopswork.loops.http.entity.HttpResponse;
import io.vertx.core.buffer.Buffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Codi on 2019-04-09.
 */

public class VertHttpResponseAdaptor {

  public static HttpResponse response(io.vertx.ext.web.client.HttpResponse<Buffer> bufferHttpResponse) {
    HttpResponse response = new HttpResponse();
    if (bufferHttpResponse.body() != null) {
      response.setBody(new BodyBuffer(bufferHttpResponse.body().getByteBuf()));
    }
    Map<String, String> headers = new HashMap<>();
    bufferHttpResponse.headers().forEach(node -> {
      if (!"Content-Length".equals(node.getKey())) {
        headers.put(node.getKey(), node.getValue());
      }
    });
    response.setHeaders(headers);
    response.setStatusCode(bufferHttpResponse.statusCode());
    response.setStatusMessage(bufferHttpResponse.statusMessage());
    return response;
  }
}
