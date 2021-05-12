package com.loopswork.loops.plugin.checker.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.plugin.checker.IPluginChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lixiaoxiao
 * @date 2020/10/16 9:32
 */
@Singleton
public class JWTChecker implements IPluginChecker {

  private final Map<String, Class<?>> config = new HashMap<>();

  @Inject
  public void init() {
    config.put(JWTConfigEnum.URI_PARAM_NAMES.configName, List.class);
    config.put(JWTConfigEnum.COOKIE_NAMES.configName, List.class);
    config.put(JWTConfigEnum.HEADER_NAMES.configName, List.class);
  }

  @Override
  public void check(Map<String, Object> configs) {
    checkType(config, configs);
    List<String> tokenNames = (List<String>) configs.get(JWTConfigEnum.URI_PARAM_NAMES.configName);
    //uri请求参数中token默认名称
    if (tokenNames == null) {
      tokenNames = new ArrayList<>();
      String defaultUriTokenName = "jwt";
      tokenNames.add(defaultUriTokenName);
    }
    configs.put(JWTConfigEnum.URI_PARAM_NAMES.configName, tokenNames);
    tokenNames = (List<String>) configs.get(JWTConfigEnum.COOKIE_NAMES.configName);
    if (tokenNames != null) {
      configs.put(JWTConfigEnum.COOKIE_NAMES.configName, tokenNames);
    }
    tokenNames = (List<String>) configs.get(JWTConfigEnum.HEADER_NAMES.configName);
    if (tokenNames == null) {
      tokenNames = new ArrayList<>();
      String defaultHeaderName = "Authorization";
      tokenNames.add(defaultHeaderName);
    }
    configs.put(JWTConfigEnum.HEADER_NAMES.configName, tokenNames);
  }

  @Override
  public String getName() {
    return PluginName.JWT;
  }

  @Override
  public String getPluginConfig() throws JsonProcessingException {
    return getPluginConfig(config);
  }

  public enum JWTConfigEnum {
    /**
     * token在请求参数中的名称
     */
    URI_PARAM_NAMES("uriParamNames"),
    /**
     * token在cookie中的名称
     */
    COOKIE_NAMES("cookieNames"),
    /**
     * token在header中的名称
     */
    HEADER_NAMES("headerNames");

    private final String configName;

    JWTConfigEnum(String configName) {
      this.configName = configName;
    }
  }

}
