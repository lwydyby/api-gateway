package com.loopswork.loops.admin.collector;

import com.loopswork.loops.admin.entity.EntityType;
import com.loopswork.loops.admin.entity.LoopsEntity;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.Map;

/**
 * @author codi
 * @date 2020/3/4 1:18 下午
 * @description 加载器接口
 */
public interface ICollector {

  /**
   * 加载数据
   */
  void load(Handler<AsyncResult<Boolean>> handler);

  /**
   * 获取对象
   */
  Map<EntityType, List<LoopsEntity>> getEntity();

  /**
   * 插入对象
   */
  void addEntity(LoopsEntity entity, Handler<AsyncResult<Void>> handler);

  /**
   * 更新对象
   */
  void updateEntity(LoopsEntity entity, String nameOrId, Handler<AsyncResult<Void>> handler);

  /**
   * 删除对象
   */
  void removeEntity(EntityType type, String nameOrId, Handler<AsyncResult<Void>> handler);

  /**
   * 初始化更新代编号信息
   */
  void initGeneration(Handler<AsyncResult<Void>> handler);

  /**
   * 检查当前是否需要更新配置
   */
  void checkGeneration(Handler<AsyncResult<Boolean>> handler);

  /**
   * 更新最新编号
   */
  void updateGeneration(Handler<AsyncResult<Void>> handler);

}
