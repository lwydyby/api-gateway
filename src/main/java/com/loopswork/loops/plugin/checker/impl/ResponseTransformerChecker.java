package com.loopswork.loops.plugin.checker.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.plugin.checker.IPluginChecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ResponseTransformerChecker implements IPluginChecker {
  private Map<String, Class<?>> config = new HashMap<>();

  @Inject
  public void init() {
    config.put("remove_headers", List.class);
    config.put("remove_json", List.class);
    config.put("replace_headers", List.class);
    config.put("replace_json", List.class);
    config.put("add_headers", List.class);
    config.put("add_json", List.class);
    config.put("append_headers", List.class);
    config.put("append_json", List.class);
  }

  @Override
  public void check(Map<String, Object> configs) {
    checkType(config, configs);
    checkNeedConfig(configs, config);
  }

  @Override
  public String getName() {
    return PluginName.RESPONSE_TRANSFORMER;
  }

  @Override
  public String getPluginConfig() throws JsonProcessingException {
    return getPluginConfig(config);
  }
}
