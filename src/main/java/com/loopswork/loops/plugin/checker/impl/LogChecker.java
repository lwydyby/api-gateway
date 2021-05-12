package com.loopswork.loops.plugin.checker.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.plugin.checker.IPluginChecker;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class LogChecker implements IPluginChecker {
  private Map<String, Class<?>> config = new HashMap<>();

  @Inject
  public void init() {
    config.put("elasticsearch_host", String.class);
    config.put("elasticsearch_port", Integer.class);
    config.put("elasticsearch_type", String.class);
    config.put("elasticsearch_index", String.class);
    config.put("elasticsearch_username", String.class);
    config.put("elasticsearch_password", String.class);
    config.put("timeout", Integer.class);
  }

  @Override
  public void check(Map<String, Object> configs) {
    if (configs.size() == 0) {
      throw new SimpleException(3005, "config不能为空");
    }
    checkType(config, configs);
    if (!configs.containsKey("elasticsearch_host")) {
      throw new SimpleException(3005, "elasticsearch_host不能为空");
    }
  }

  @Override
  public String getName() {
    return PluginName.LOG;
  }

  @Override
  public String getPluginConfig() throws JsonProcessingException {
    return getPluginConfig(config);
  }
}
