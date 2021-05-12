package com.loopswork.loops.manager.balancer.impl;


import com.loopswork.loops.entity.Target;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.manager.balancer.IBalancer;

import java.util.Map;

/**
 * @author codi
 * @title: ErrorBalancer
 * @projectName admin
 * @description: 上游服务无目标时 构造此均衡器 负载时抛出异常
 * @date 2019-09-04 16:59
 */
public class EmptyBalancer implements IBalancer {
  @Override
  public void init(Map<String, Target> targets, int slot) {

  }

  @Override
  public Target balance(HttpRequest request) throws RouterException {
    //没有可用目标
    throw RouterException.e(RouterCode.UPSTREAM_NO_TARGET);
  }

  @Override
  public Map<String, Target> targets() {
    return null;
  }

  @Override
  public int targetCount() {
    return 0;
  }
}
