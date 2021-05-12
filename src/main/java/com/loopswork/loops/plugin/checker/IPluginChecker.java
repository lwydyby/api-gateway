package com.loopswork.loops.plugin.checker;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopswork.loops.exception.SimpleException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public interface IPluginChecker {

  void check(Map<String, Object> configs);

  String getName();

  void init();

  default void checkType(Map<String, Class<?>> config, Map<String, Object> configs) {
    configs.forEach((k, v) -> {
      if (!config.containsKey(k)) {
        throw new SimpleException(3005, "config中不应包含" + k);
      }
      Class<?> type = config.get(k);
      if (!type.isInstance(v)) {
        throw new SimpleException(3005, "config中" + k + "的字段不合法");
      }
    });
  }

  default void checkNeedConfig(Map<String, Object> config, Map<String, Class<?>> needConfig) {
    AtomicBoolean flag = new AtomicBoolean(false);
    needConfig.forEach((k, v) -> {
      if (config.containsKey(k)) {
        flag.set(true);
      }
    });
    if (!flag.get()) {
      throw new SimpleException(3005, "缺少必要的字段");
    }
  }

  default String getPluginConfig(Map<String, Class<?>> config) throws JsonProcessingException {
    Map<String, Class<?>> needConfig = config;
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> jsonMap = new HashMap<>();
    needConfig.forEach((k, v) -> {
      if (v.isAssignableFrom(Integer.class)) {
        jsonMap.put(k, 0);
      } else if (v.isAssignableFrom(List.class)) {
        jsonMap.put(k, new ArrayList<>());
      } else {
        jsonMap.put(k, "");
      }
    });
    return mapper.writeValueAsString(jsonMap);

  }

  String getPluginConfig() throws JsonProcessingException;
}
