package com.loopswork.loops.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.ACL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class ACLManager implements IDataManager {
  @Inject
  private Managers managers;
  private Map<String, List<String>> consumerIdGroupMap = new HashMap<>(1024);

  @Override
  public void init() {
    load();
  }

  @Override
  public void update() {
    load();
  }

  private void load() {
    getConsumerAclCache();
  }

  public Map<String, List<String>> getConsumerIdGroupMap() {
    return consumerIdGroupMap;
  }

  /**
   * 获取acl相关内容并缓存
   */
  private void getConsumerAclCache() {
    List<ACL> acls = managers.getEntityList(ACL.class);
    if (acls != null) {
      this.consumerIdGroupMap.clear();
      this.consumerIdGroupMap = acls.stream().collect(Collectors.toMap(ACL::getConsumerId, ACL::getGroups));
    }
  }

}
