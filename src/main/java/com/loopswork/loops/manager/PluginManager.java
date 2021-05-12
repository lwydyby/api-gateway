package com.loopswork.loops.manager;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: PluginService
 * @Author: Fan Zhang
 * @Date: 2019-04-04 09:52
 */
@Singleton
public class PluginManager implements IDataManager {
  @Inject
  private Managers managers;
  private Map<String, Plugin> configuredPlugins = new HashMap<>(1024);

  /**
   * 根据提供的信息生成cacheKey，格式：plugins:$plugin_name:$(routeId):$(serverId):$(serverId):
   *
   * @param pluginName 插件的名称，e.g. iprestriction，ratelimiting
   */
  public static String generateCacheKey(String pluginName, String routeId, String serverId, String consumerId) {
    return "plugins:" + pluginName + ":" + (routeId == null ? ":" : routeId + ":") +
      (serverId == null ? ":" : serverId + ":") +
      (consumerId == null ? ":" : consumerId + ":");
  }

  @Override
  public void init() {
    load();
  }

  @Override
  public void update() {
    load();
  }

  private void load() {
    configuredPlugins.clear();
    List<Plugin> plugins = managers.getEntityList(Plugin.class);
    plugins.stream().filter(Plugin::isEnable).forEach(plugin -> {
      String cacheKey = generateCacheKey(plugin.getName(), plugin.getRouteId(), plugin.getServerId(), plugin.getConsumerId());
      configuredPlugins.put(cacheKey, plugin);
    });
  }

  /**
   * 获取请求命中的插件
   *
   * @param pluginName 插件名称
   * @param routeId    路由id
   * @param serverId   服务id
   * @param consumerId 租户id
   * @return 插件
   */
  public Plugin getPlugin(String pluginName, String routeId, String serverId, String consumerId) {
    //依次遍历所有命中可能
    for (String rid : Arrays.asList(routeId, null)) {
      for (String sid : Arrays.asList(serverId, null)) {
        for (String cid : Arrays.asList(consumerId, null)) {
          Plugin plugin = matchPlugin(pluginName, rid, sid, cid);
          if (plugin != null) {
            return plugin;
          }
        }
      }
    }
    return null;
  }

  private Plugin matchPlugin(String pluginName, String routeId, String serverId, String consumerId) {
    String key = generateCacheKey(pluginName, routeId, serverId, consumerId);
    return configuredPlugins.get(key);
  }


}
