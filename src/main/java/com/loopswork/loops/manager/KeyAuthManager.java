package com.loopswork.loops.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.KeyAuthCredentials;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lixiaoxiao
 * @className KeyAuthService
 * @date 2019/7/22 0022 上午 10:34
 */
@Singleton
public class KeyAuthManager implements IDataManager {
  @Inject
  private Managers managers;
  private Map<String, String> keyConsumerIdMap = new HashMap<>(1024);

  @Override
  public void init() {
    load();
  }

  @Override
  public void update() {
    load();
  }

  public Map<String, String> getKeyConsumerIdMap() {
    return keyConsumerIdMap;
  }

  private void load() {
    List<KeyAuthCredentials> list = managers.getEntityList(KeyAuthCredentials.class);
    keyConsumerIdMap.clear();
    list.forEach(keyAuth -> keyConsumerIdMap.put(keyAuth.getKey(), keyAuth.getConsumerId()));
  }
}
