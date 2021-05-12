package com.loopswork.loops.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.config.LoopsConfig;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author codi
 * @description WebClient管理器
 * @date 2020/1/13 5:04 下午
 */
@Singleton
public class ClientManager {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private Map<String, WebClient> webClientMap = new HashMap<>();
  private Map<String, HttpClient> httpClientMap = new HashMap<>();
  private Map<String, NetClient> netClientMap = new HashMap<>();
  @Inject
  private Vertx vertx;
  @Inject
  private LoopsConfig loopsConfig;

  public HttpClient getHttpClient(String key) {
    if (!httpClientMap.containsKey(key)) {
      httpClientMap.put(key, vertx.createHttpClient(new HttpClientOptions()
        .setProtocolVersion(getHttpClientVersion()).setMaxChunkSize(4096)));
      log.debug("create http client [{}]", key);
    }
    return httpClientMap.get(key);
  }


  public HttpClient getCurrentThreadHttpClient() {
    return getHttpClient(Thread.currentThread().getName());
  }


  public WebClient getWebClient(String key) {
    if (!webClientMap.containsKey(key)) {
      webClientMap.put(key, createWebClient());
      log.debug("create web client [{}]", key);
    }
    return webClientMap.get(key);
  }

  public WebClient getCurrentThreadWebClient() {
    return getWebClient(Thread.currentThread().getName());
  }

  public NetClient getNetClient(String key){
    if(!netClientMap.containsKey(key)){
      netClientMap.put(key, vertx.createNetClient(new NetClientOptions()));
      log.debug("create http client [{}]", key);
    }
    return netClientMap.get(key);
  }

  public NetClient getCurrentThreadNetClient(){
    return getNetClient(Thread.currentThread().getName());
  }
  private WebClient createWebClient() {
    WebClientOptions webClientOptions = new WebClientOptions();
    webClientOptions.setMaxPoolSize(loopsConfig.getClientPollSize());
    webClientOptions.setMaxInitialLineLength(loopsConfig.getMaxInitialLineLength());
    webClientOptions.setConnectTimeout(loopsConfig.getConnectTimeout());
    webClientOptions.setIdleTimeout(loopsConfig.getReadTimeout());
    webClientOptions.setIdleTimeoutUnit(TimeUnit.MILLISECONDS);
    webClientOptions.setKeepAlive(loopsConfig.isKeepAlive());
    return WebClient.create(vertx, webClientOptions);
  }

  private HttpVersion getHttpClientVersion(){
    String version=loopsConfig.getHttpVersion();
    switch (version){
      case "1.0":
        return HttpVersion.HTTP_1_0;
      case "2.0":
        return HttpVersion.HTTP_2;
      default:
        return HttpVersion.HTTP_1_1;
    }
  }

}
