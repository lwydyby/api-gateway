package com.loopswork.loops.entity;


import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author codi
 * @title: HealthState
 * @projectName admin
 * @description: 健康检查计数
 * @date 2019-09-04 15:17
 */
@Data
public class HealthState {
  /**
   * 成功次数
   */
  private AtomicInteger successes = new AtomicInteger();
  /**
   * 失败次数
   */
  private AtomicInteger failures = new AtomicInteger();
}
