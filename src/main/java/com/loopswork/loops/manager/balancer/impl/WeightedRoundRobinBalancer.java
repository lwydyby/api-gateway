package com.loopswork.loops.manager.balancer.impl;

import com.loopswork.loops.entity.Target;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.manager.balancer.IBalancer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author codi
 * @title: WeightedRoundRobin
 * @projectName admin
 * @description: 权重轮询均衡器
 * @date 2019-08-02 16:27
 */

public class WeightedRoundRobinBalancer implements IBalancer {

  /**
   * 哈希槽的幂 哈希槽的个数一定为2的n次方个 因此只需指定幂
   */
  private int slot;
  /**
   * 哈希槽的个数
   */
  private int slotCount;
  private String[] hashslot;
  private int round = 0;
  private Random random = new Random();
  private Map<String, Target> targets;

  /**
   * 初始化负载均衡器
   *
   * @param targets 负载均衡目标
   * @param slot    哈希槽的幂
   */
  @Override
  public void init(Map<String, Target> targets, int slot) {
    slot = Math.min(slot, 16);
    slot = Math.max(slot, 4);
    //计算哈希槽个数
    this.slot = slot;
    slotCount = 1 << slot;
    //使用Target填充hash槽
    this.targets = targets;
    hashslot = new String[slotCount];
    //计算各Target槽个数
    Map<String, Integer> targetCount = new HashMap<>();
    int totalWeight = 0;
    for (Target target : targets.values()) {
      totalWeight += target.getWeight();
    }
    //计算权重和槽数量关系
    double radio = (double) slotCount / totalWeight;
    for (Target target : targets.values()) {
      //确定Target的槽个数
      int count = (int) Math.ceil(radio * target.getWeight());
      if (count != 0) {
        //权重为0的Target不计入
        targetCount.put(target.getId(), count);
      }
    }
    for (int i = 0; i < hashslot.length; i++) {
      int index = random.nextInt(targetCount.size());
      String targetId = targetCount.keySet().toArray(new String[0])[index];
      hashslot[i] = targetId;
      targetCount.put(targetId, targetCount.get(targetId) - 1);
      if (targetCount.get(targetId) == 0) {
        targetCount.remove(targetId);
      }
    }
  }

  @Override
  public Target balance(HttpRequest request) {
    round = (round + 1) & (slotCount - 1);
    String targetId = hashslot[round];
    return targets.get(targetId);
  }

  @Override
  public Map<String, Target> targets() {
    return targets;
  }

  @Override
  public int targetCount() {
    return targets == null ? 0 : targets.size();
  }
}
