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
public class RequestTransformerChecker implements IPluginChecker {
  private Map<String, Class<?>> config = new HashMap<>();

  @Inject
  public void init() {
    config.put("remove_headers", List.class);
    config.put("remove_querystring", List.class);
    config.put("remove_body", List.class);
    config.put("rename_querystring", List.class);
    config.put("rename_body", List.class);
    config.put("replace_headers", List.class);
    config.put("replace_querystring", List.class);
    config.put("replace_body", List.class);
    config.put("add_headers", List.class);
    config.put("add_querystring", List.class);
    config.put("append_headers", List.class);
    config.put("append_querystring", List.class);
    config.put("append_body", List.class);
    config.put("add_body", List.class);
    config.put("rename_headers", List.class);
  }

  @Override
  public void check(Map<String, Object> configs) {
    checkType(config, configs);
    checkNeedConfig(configs, config);
  }

  @Override
  public String getName() {
    return PluginName.REQUEST_TRANSFORMER;
  }

  @Override
  public String getPluginConfig() throws JsonProcessingException {
    return getPluginConfig(config);
  }
}
