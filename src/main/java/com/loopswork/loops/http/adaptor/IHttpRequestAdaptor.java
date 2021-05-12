package com.loopswork.loops.http.adaptor;


import com.loopswork.loops.http.entity.HttpRequest;

/**
 * 将原始http请求转化为HttpRequest
 * Created by Codi on 2019-04-09.
 */
public interface IHttpRequestAdaptor<T> {

  HttpRequest request(T t);
}
