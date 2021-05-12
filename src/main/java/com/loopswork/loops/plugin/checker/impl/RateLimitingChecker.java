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
public class RateLimitingChecker implements IPluginChecker {
  private Map<String, Class<?>> config = new HashMap<>();

  @Inject
  public void init() {
    config.put("second", Integer.class);
    config.put("minute", Integer.class);
    config.put("hour", Integer.class);
    config.put("day", Integer.class);
    config.put("month", Integer.class);
    config.put("year", Integer.class);
    config.put("redis_host", String.class);
    config.put("redis_password", String.class);
    config.put("redis_port", Integer.class);
    config.put("redis_timeout", Integer.class);
    config.put("redis_database", Integer.class);
  }

  @Override
  public void check(Map<String, Object> configs) {
    checkType(config, configs);
    if (!configs.containsKey("second") && !configs.containsKey("minute") && !configs.containsKey("year") && !configs.containsKey(
      "hour") && !configs.containsKey("day") && !configs.containsKey("month")) {
      throw new SimpleException(3005, "六个限制项最少设置一项");
    }
    if (!configs.containsKey("redis_host")) {
      throw new SimpleException(3005, "redis_host没有填写");
    }
  }

  @Override
  public String getName() {
    return PluginName.RATE_LIMITING;
  }

  @Override
  public String getPluginConfig() throws JsonProcessingException {
    return getPluginConfig(config);
  }
}
