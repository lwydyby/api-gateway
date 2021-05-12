package com.loopswork.loops.plugin.impl;


import com.google.inject.Inject;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.ContextKeys;
import com.loopswork.loops.entity.Plugin;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.plugin.BasePluginHandler;
import com.loopswork.loops.plugin.PluginPriority;
import com.loopswork.loops.plugin.PluginType;
import com.loopswork.loops.util.JsonObjectUtil;
import com.loopswork.loops.util.StringUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.SecretOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lixiaoxiao
 * @date 2020/10/16 9:30
 */
public class JWTPlugin extends BasePluginHandler {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject
  private LoopsConfig loopsConfig;

  @Override
  public void handle(RoutingContext context, Plugin plugin) {
    //1、获取插件config
    Map<String, Object> config = plugin.getConfig();
    //2、获取http请求
    HttpRequest httpRequest = context.get(ContextKeys.HTTP_REQUEST);
    //不处理keystone请求
    if (httpRequest.getHost().contains("keystone")) {
      context.next();
      return;
    }
    String corsHeader = httpRequest.getHeaders().get("Access-Control-Request-Headers");
    if (corsHeader != null) {
      if (corsHeader.toLowerCase().contains("x-auth-token")) {
        context.next();
        return;
      }
    }
    //3、获取并解析token
    String token = getToken(httpRequest, config);
    if (token != null) {
      if (token.contains("Bearer ") || token.contains("bearer ")) {
        token = token.replace("Bearer ", "");
        token = token.replace("bearer ", "");
      }
      if (!StringUtils.isEmpty(token)) {
        log.info("X-Auth-Token:" + token);
        JWTAuth jwtAuth = JWTAuth.create(context.vertx(), new JWTAuthOptions()
          .addSecret(new SecretOptions().setSecret(loopsConfig.getSecret())));
        String finalToken = token;
        jwtAuth.authenticate(new JsonObject().put("jwt", token), res -> {
          if (res.succeeded()) {
            context.put(ContextKeys.JWT_TOKEN, res.result());
            Map<String, String> headers = httpRequest.getHeaders();
            List<String> addHeaders = new ArrayList<>();
            addHeaders.add("X-Auth-Token:" + finalToken);
            JsonObjectUtil.transformerMap(headers, "add", addHeaders);
            httpRequest.setHeaders(headers);
            context.next();
          } else {
            log.error(res.cause());
            setJwtFailedHeader(context);
            context.fail(RouterException.e(RouterCode.JWT_FAILED));
          }
        });
      }
    } else {
      log.error("token is empty: " + httpRequest.getHeaders());
      setJwtFailedHeader(context);
      context.fail(RouterException.e(RouterCode.JWT_FAILED));
    }
  }

  @Override
  public int priority() {
    return PluginPriority.JWT;
  }

  @Override
  public PluginType getPluginType() {
    return PluginType.PRE;
  }

  @Override
  public String getName() {
    return PluginName.JWT;
  }

  /**
   * 获取token值
   *
   * @param request 请求
   * @param config  插件配置信息
   * @return String
   */
  private String getToken(HttpRequest request, Map<String, Object> config) {
    String token = null;
    List<String> uriTokenNames = (List<String>) config.get("uriParamNames");
    List<String> cookieTokenNames = (List<String>) config.get("cookieNames");
    List<String> headerTokenNames = (List<String>) config.get("headerNames");
    //1、从querystring中获取
    if (uriTokenNames != null) {
      for (String name : uriTokenNames) {
        token = request.getParams().get(name);
        if (token != null) {
          return token;
        }
      }
    }
    //2、从cookie中获取
    if (cookieTokenNames != null) {
      token = request.getHeaders().get("Cookie");
      if (token == null) {
        token = request.getHeaders().get("cookie");
      }
      if (token != null) {
        for (String name : cookieTokenNames) {
          token = stringToMap(token).get(name);
          if (token != null) {
            return token;
          }
        }
      }
    }
    //3、从header中获取
    if (headerTokenNames != null) {
      for (String name : headerTokenNames) {
        token = request.getHeaders().get(name);
        if (token != null) {
          return token;
        }
      }
    }
    return token;
  }

  public Map<String, String> stringToMap(String value) {
    return getStringMap(value);
  }

  static Map<String, String> getStringMap(String value) {
    String[] array = value.split(";");
    Map<String, String> map = new HashMap<>();
    for (String s : array) {
      String[] ss = s.split("=");
      if (ss.length == 2)
        map.put(ss[0].trim(), ss[1].trim());
    }
    return map;
  }

  private void setJwtFailedHeader(RoutingContext context) {
    context.response().putHeader("Access-Control-Expose-Headers", "X-Auth-Token,X-Subject-Token");
    context.response().putHeader("Access-Control-Allow-Origin", "*");
    context.response().putHeader("Access-Control-Allow-Methods", "*");
    context.response().putHeader("Access-Control-Allow-Headers", "*");
  }

}
