package com.loopswork.loops.plugin.checker.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.admin.utils.IpUtils;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.plugin.checker.IPluginChecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lw
 */
@Singleton
public class IpRestrictionChecker implements IPluginChecker {
  private Map<String, Class<?>> config = new HashMap<>();

  @Inject
  public void init() {
    config.put("whitelist", List.class);
    config.put("blacklist", List.class);
    config.put("switch", String.class);
  }

  @Override
  public void check(Map<String, Object> configs) {
    checkType(config, configs);
    if (!configs.containsKey("switch")) {
      configs.put("switch", "black");
    }
    String switchValue = (String) configs.get("switch");
    if (!"black".equals(switchValue) && !"white".equals(switchValue)) {
      throw new SimpleException(3005, "switch 可选值应为black/white");
    }
    if (!configs.containsKey("whitelist") && !configs.containsKey("blacklist")) {
      throw new SimpleException(3005, "whitelist,blacklist不能同时为空");
    }
    if ("black".equals(switchValue)) {
      if (!configs.containsKey("blacklist")) {
        throw new SimpleException(3005, "blacklist不能为空");
      }
      List<String> blackList = (List<String>) configs.get("blacklist");
      if (!IpUtils.checkIpList(blackList)) {
        throw new SimpleException(3005, "blacklist中数据格式错误");
      }

    }
    if ("white".equals(switchValue)) {
      if (!configs.containsKey("whitelist")) {
        throw new SimpleException(3005, "whitelist不能为空");
      }
      List<String> whiteList = (List<String>) configs.get("whitelist");
      if (!IpUtils.checkIpList(whiteList)) {
        throw new SimpleException(3005, "whitelist中数据格式错误");
      }
    }
  }

  @Override
  public String getName() {
    return PluginName.IP_RESTRICTION;
  }

  @Override
  public String getPluginConfig() throws JsonProcessingException {
    return getPluginConfig(config);
  }


}
