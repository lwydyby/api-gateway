package com.loopswork.loops.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.entity.LoopsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author codi
 * @title: Target
 * @projectName admin
 * @description: 目标服务定义
 * @date 2019-07-26 16:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Target extends LoopsEntity {
  private String upstreamId;
  private String host;
  private int port;
  private int weight;
  private HealthStatus health;
  private HealthState activeState;
  private HealthState passiveState;
}
