package com.loopswork.loops.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author codi
 * @title: HealthCheck
 * @projectName admin
 * @description: 健康检查配置
 * @date 2019-07-26 17:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HealthCheck {
  private Boolean enable;
  private HealthType type;
  /**
   * 主动检查时为检查间隔 被动检查时为重新标记为可用的时间
   */
  private Integer interval = 0;
  private Integer timeout = 0;
  private String httpPath;
  private Integer concurrency = 10;
  private HealthCondition healthyCondition;
  private HealthCondition unhealthyCondition;
  private Long lastCheckTime = 0L;

}
