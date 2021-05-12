package com.loopswork.loops.admin.collector.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.admin.collector.ICollector;
import com.loopswork.loops.admin.entity.EntityType;
import com.loopswork.loops.admin.entity.LoopsEntity;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.util.UUIDUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.loopswork.loops.entity.SimpleCode.ENTITY_NOT_EXISTS;
import static com.loopswork.loops.entity.SimpleCode.ENTITY_TYPE_NOT_EXISTS;

/**
 * @author codi
 * @date 2020/3/15 8:28 下午
 * @description Mongo配置加载器
 */
@Singleton
public class MongoCollector implements ICollector {
  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Inject
  @Nullable
  private MongoClient mongoClient;

  private Map<EntityType, List<LoopsEntity>> entityMap;
  private int generation;

  @Override
  @SuppressWarnings("rawtypes")
  public void load(Handler<AsyncResult<Boolean>> handler) {
    entityMap = new HashMap<>();
    List<Future> futures = new ArrayList<>();
    for (EntityType type : EntityType.values()) {
      futures.add(Future.<List<JsonObject>>future(h -> mongoClient.find(type.toString(), new JsonObject(), h)));
    }
    CompositeFuture compositeFuture = CompositeFuture.all(futures);
    compositeFuture.onSuccess(result -> {
      for (int i = 0; i < EntityType.values().length; i++) {
        //序列化数据 转换数据类型
        EntityType type = EntityType.values()[i];
        List<JsonObject> list = result.resultAt(i);
        List<LoopsEntity> entities = list.stream().map(json -> json.mapTo(type.getEntityClass())).collect(Collectors.toList());
        entityMap.put(type, entities);
      }
      handler.handle(Future.succeededFuture());
    }).onFailure(failure -> handler.handle(Future.failedFuture(failure)));
  }

  @Override
  public Map<EntityType, List<LoopsEntity>> getEntity() {
    return entityMap;
  }

  @Override
  public void addEntity(LoopsEntity entity, Handler<AsyncResult<Void>> handler) {
    EntityType type = EntityType.get(entity.getClass());
    if (type == null) {
      handler.handle(Future.failedFuture(new SimpleException(ENTITY_TYPE_NOT_EXISTS)));
      return;
    }
    entity.setId(UUIDUtil.getUUID());
    Date date = new Date();
    entity.setCreatedAt(date);
    entity.setUpdatedAt(date);
    saveEntity(entity, handler);
  }

  @Override
  public void updateEntity(LoopsEntity entity, String nameOrId, Handler<AsyncResult<Void>> handler) {
    EntityType type = EntityType.get(entity.getClass());
    if (type == null) {
      handler.handle(Future.failedFuture(new SimpleException(ENTITY_TYPE_NOT_EXISTS)));
    } else {
      LoopsEntity exist = getEntity(type, nameOrId);
      if (exist == null) {
        handler.handle(Future.failedFuture(new SimpleException(ENTITY_NOT_EXISTS)));
      } else {
        entity.setUpdatedAt(new Date());
        entity.setId(exist.getId());
        saveEntity(entity, handler);
      }
    }
  }

  private void saveEntity(LoopsEntity entity, Handler<AsyncResult<Void>> handler) {
    mongoClient.save(EntityType.get(entity.getClass()).name(), JsonObject.mapFrom(entity), h -> {
      if (h.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(h.cause()));
      }
    });
  }

  @Override
  public void removeEntity(EntityType type, String nameOrId, Handler<AsyncResult<Void>> handler) {
    JsonObject query = new JsonObject();
    query.put("$or", new JsonArray().add(new JsonObject().put("_id", nameOrId)).add(new JsonObject().put("name", nameOrId)));
    mongoClient.removeDocument(type.name(), query, h -> {
      if (h.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(h.cause()));
      }
    });
  }

  @Override
  public void updateGeneration(Handler<AsyncResult<Void>> handler) {
    mongoClient.findOneAndUpdate("generation", new JsonObject(), new JsonObject().put("$inc", new JsonObject().put("generation", 1)), ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void initGeneration(Handler<AsyncResult<Void>> handler) {
    getGeneration(ar -> {
      if (ar.result() == null) {
        log.debug("generation not exists init to 0");
        //初始化
        mongoClient.save("generation", new JsonObject().put("generation", 0), r -> {
          if (r.succeeded()) {
            handler.handle(Future.succeededFuture());
          } else {
            handler.handle(Future.failedFuture(r.cause()));
          }
        });
      } else {
        generation = ar.result();
        log.debug("generation now is {}", generation);
        handler.handle(Future.succeededFuture());
      }
    });
  }

  @Override
  public void checkGeneration(Handler<AsyncResult<Boolean>> handler) {
    getGeneration(ar -> {
      Integer generationNow = ar.result();
      if (generationNow != null && generation < generationNow) {
        generation = generationNow;
        handler.handle(Future.succeededFuture(true));
      } else {
        handler.handle(Future.succeededFuture(false));
      }
    });
  }

  private void getGeneration(Handler<AsyncResult<Integer>> handler) {
    mongoClient.findOne("generation", new JsonObject(), null, ar -> {
      if (ar.succeeded()) {
        Integer generation = ar.result() == null ? null : ar.result().getInteger("generation");
        handler.handle(Future.succeededFuture(generation));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  private LoopsEntity getEntity(EntityType type, String nameOrId) {
    List<LoopsEntity> list = entityMap.get(type);
    if (list != null) {
      Optional<LoopsEntity> result = list.stream().filter(entity -> nameOrId.equals(entity.getId()) || nameOrId.equals(entity.getName())).findFirst();
      return result.orElse(null);
    } else {
      return null;
    }
  }
}
