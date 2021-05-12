package com.loopswork.loops.admin.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.loopswork.loops.entity.Plugin;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.plugin.checker.IPluginChecker;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * @author lw
 * @description 检查插件config是否合法的工具类
 */
@Singleton
public class PluginCheck {

  @Inject
  private Set<IPluginChecker> pluginChecks;
  private Map<String, IPluginChecker> checkMap;

  @Inject
  public void init() {
    checkMap = new HashMap<>();
    pluginChecks.forEach(check -> checkMap.put(check.getName(), check));
  }

  public void checkConfigAndName(Plugin plugin) {
    if (!checkMap.containsKey(plugin.getName().trim())) {
      throw new SimpleException(3002, "插件名格式错误");
    }
    if (plugin.getConfig() == null) {
      throw new SimpleException(3005, "插件config不能为空");
    }
    IPluginChecker check = checkMap.get(plugin.getName().trim());
    check.check(plugin.getConfig());
  }

  public List<String> getPluginName() {
    return new ArrayList<>(checkMap.keySet());
  }

  public String getPluginConfig(String name) throws JsonProcessingException {
    IPluginChecker check = checkMap.get(name);
    return check.getPluginConfig();
  }


}
