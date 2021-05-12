package com.loopswork.loops.manager.balancer.impl;

import com.loopswork.loops.entity.Target;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.manager.balancer.IBalancer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author codi
 * @title: RandomBalancer
 * @projectName admin
 * @description: 随机负载均衡器
 * @date 2019-08-02 19:09
 */

public class RandomBalancer implements IBalancer {

  private Random random = new Random();
  private Map<String, Target> targets = new HashMap<>();

  @Override
  public void init(Map<String, Target> targets, int slot) {
    this.targets = targets;
  }

  @Override
  public Target balance(HttpRequest request) {
    int index = random.nextInt(targets.size());
    return targets.values().toArray(new Target[0])[index];
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
