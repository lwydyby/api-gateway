package com.loopswork.loops.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.entity.LoopsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author codi
 * @title: Upstream
 * @projectName admin
 * @description: 上游服务定义
 * @date 2019-07-24 14:49
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Upstream extends LoopsEntity {
  private HashType hashOn;
  private HashType hashFallback;
  private String hashOnHeader;
  private String hashFallbackHeader;
  private Integer slots;
  private HealthCheck activeHealthCheck;
  private HealthCheck passiveHealthCheck;
  private boolean enable;
  //是否开启主从热备
  private boolean lvs;
}
