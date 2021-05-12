package com.loopswork.loops.http.wraper;


import com.loopswork.loops.http.entity.HttpResponse;

/**
 * Created by Codi on 2019-04-09.
 */
public interface IHttpResponseWrapper<T> {

  void response(HttpResponse response, T wrapped);

}
