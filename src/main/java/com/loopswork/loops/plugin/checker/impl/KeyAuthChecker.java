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
 * @className KeyAuthCheck
 * @date 2019/7/22 0022 下午 13:35
 */
@Singleton
public class KeyAuthChecker implements IPluginChecker {

  private Map<String, Class<?>> config = new HashMap<>();

  @Inject
  public void init() {
    config.put(KeyAuthConfigEnum.KEY_NAMES.configName, List.class);
    config.put(KeyAuthConfigEnum.KEY_IN_BODY.configName, Boolean.class);
    config.put(KeyAuthConfigEnum.HIDE_CREDENTIALS.configName, Boolean.class);
    config.put(KeyAuthConfigEnum.ANONYMOUS.configName, String.class);
    config.put(KeyAuthConfigEnum.RUN_ON_PREFLIGHT.configName, Boolean.class);
  }

  @Override
  public void check(Map<String, Object> configs) {
    checkType(config, configs);
    List<?> keyNameList = (List<?>) configs.get(KeyAuthConfigEnum.KEY_NAMES.configName);
    if (!configs.containsKey(KeyAuthConfigEnum.KEY_NAMES.configName) || keyNameList.size() == 0) {
      List<String> defaultKeyName = new ArrayList<>();
      //默认名称
      String defaultName = "apikey";
      defaultKeyName.add(defaultName);
      configs.put(KeyAuthConfigEnum.KEY_NAMES.configName, defaultKeyName);
    }
    if (!configs.containsKey(KeyAuthConfigEnum.KEY_IN_BODY.configName)) {
      configs.put(KeyAuthConfigEnum.KEY_IN_BODY.configName, false);
    }
    if (!configs.containsKey(KeyAuthConfigEnum.HIDE_CREDENTIALS.configName)) {
      configs.put(KeyAuthConfigEnum.HIDE_CREDENTIALS.configName, false);
    }
    if (!configs.containsKey(KeyAuthConfigEnum.RUN_ON_PREFLIGHT.configName)) {
      configs.put(KeyAuthConfigEnum.RUN_ON_PREFLIGHT.configName, true);
    }
  }

  @Override
  public String getName() {
    return PluginName.KEY_AUTH;
  }

  @Override
  public String getPluginConfig() throws JsonProcessingException {
    return getPluginConfig(config);
  }

  enum KeyAuthConfigEnum {
    /**
     * key的名称
     */
    KEY_NAMES("keyNames"),
    /**
     * key是否在请求体中
     */
    KEY_IN_BODY("keyInBody"),
    /**
     * 是否隐藏授权信息
     */
    HIDE_CREDENTIALS("hideCredentials"),
    /**
     * 匿名用户uuid
     */
    ANONYMOUS("anonymous"),
    /**
     * 是否拦截OPTION请求
     */
    RUN_ON_PREFLIGHT("runOnPreflight");

    private String configName;

    KeyAuthConfigEnum(String configName) {
      this.configName = configName;
    }
  }

}
