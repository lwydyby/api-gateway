package com.loopswork.loops.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.Consumer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liwei
 * @description 租户管理器
 * @date 2019-12-16 19:57
 */
@Singleton
public class ConsumerManager implements IDataManager {
  @Inject
  private Managers managers;
  private Map<String, Consumer> consumerMap;
  private Map<String, Boolean> enableMap;

  @Override
  public void init() {
    load();
  }

  @Override
  public void update() {
    load();
  }

  private void load() {
    List<Consumer> list = managers.getEntityList(Consumer.class);
    consumerMap = list.stream().collect(Collectors.toMap(Consumer::getId, s -> s));
    enableMap = list.stream().filter(Consumer::isEnable).collect(Collectors.toMap(Consumer::getId, Consumer::isEnable));
  }

  public Map<String, Consumer> getConsumerMap() {
    return consumerMap;
  }

  public Map<String, Boolean> getEnableMap() {
    return enableMap;
  }
}
