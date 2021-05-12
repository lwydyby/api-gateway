package com.loopswork.loops.manager.balancer.impl;

import com.loopswork.loops.entity.Target;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.manager.balancer.IBalancer;

import java.util.HashMap;
import java.util.Map;

public class LvsBalancer implements IBalancer {

  private Map<String, Target> targets = new HashMap<>();

  @Override
  public void init(Map<String, Target> targets, int slot) {
    this.targets = targets;
  }
  //只获取健康的第一个,以保证所有的请求到访问到同一个健康服务
  @Override
  public Target balance(HttpRequest request) throws RouterException {
    return targets.values().toArray(new Target[0])[0];
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
