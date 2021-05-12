package com.loopswork.loops.admin.entity;

import com.loopswork.loops.entity.*;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author codi
 * @date 2020/3/16 12:10 上午
 * @description 实体类型
 */
public enum EntityType {
  server(Server.class),
  route(Route.class),
  plugin(Plugin.class),
  consumer(Consumer.class),
  upstream(Upstream.class),
  target(Target.class),
  acl(ACL.class),
  key_auth(KeyAuthCredentials.class);

  private Class<? extends LoopsEntity> entityClass;

  EntityType(Class<? extends LoopsEntity> entityClass) {
    this.entityClass = entityClass;
  }

  public static <T extends LoopsEntity> EntityType get(Class<T> clazz) {
    Optional<EntityType> result = Arrays.stream(values()).filter(v -> v.getEntityClass().equals(clazz)).findFirst();
    return result.orElse(null);
  }

  public Class<? extends LoopsEntity> getEntityClass() {
    return entityClass;
  }
}
