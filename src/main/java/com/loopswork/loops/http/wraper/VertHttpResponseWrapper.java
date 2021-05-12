package com.loopswork.loops.http.wraper;

import com.google.inject.Singleton;
import com.loopswork.loops.http.entity.HttpResponse;
import io.vertx.core.http.HttpServerResponse;

/**
 * Created by Codi on 2019-04-09.
 */
@Singleton
public class VertHttpResponseWrapper implements IHttpResponseWrapper<HttpServerResponse> {

  @Override
  public void response(HttpResponse response, HttpServerResponse wrapped) {
    wrapped.setStatusCode(response.getStatusCode());
    wrapped.setStatusMessage(response.getStatusMessage());
    if (response.getHeaders() != null) {
      response.getHeaders().forEach(wrapped::putHeader);
    }
  }
}
