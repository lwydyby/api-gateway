package com.loopswork.loops.manager.balancer;


import com.loopswork.loops.entity.Target;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.http.entity.HttpRequest;

import java.util.Map;

/**
 * @author codi
 * @title: IBalancer
 * @projectName admin
 * @description: 负载均衡器接口
 * @date 2019-08-02 16:18
 */
public interface IBalancer {

  void init(Map<String, Target> targets, int slot);

  Target balance(HttpRequest request) throws RouterException;

  Map<String, Target> targets();

  int targetCount();
}
