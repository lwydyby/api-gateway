package com.loopswork.loops.plugin.checker.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.plugin.checker.IPluginChecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lixiaoxiao
 * @date 2019/7/29 14:12
 */
@Singleton
public class AclChecker implements IPluginChecker {
  private Map<String, Class<?>> config = new HashMap<>();

  @Inject
  public void init() {
    config.put("whitelist", List.class);
    config.put("blacklist", List.class);
    config.put("hideGroupsHeader", Boolean.class);
  }

  @Override
  public void check(Map<String, Object> configs) {
    checkType(config, configs);
    if (!configs.containsKey("whitelist") && !configs.containsKey("blacklist")) {
      throw new SimpleException(3005, "whitelist,blacklist不能同时为空");
    }
    if (configs.containsKey("whitelist") && configs.containsKey("blacklist")) {
      throw new SimpleException(3006, "whitelist,blacklist不能同时存在");
    }
    if (!configs.containsKey("hideGroupsHeader")) {
      configs.put("hideGroupsHeader", false);
    }
  }

  @Override
  public String getName() {
    return PluginName.ACL;
  }

  @Override
  public String getPluginConfig() throws JsonProcessingException {
    return getPluginConfig(config);
  }


}
