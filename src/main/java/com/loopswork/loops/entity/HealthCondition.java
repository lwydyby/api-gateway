package com.loopswork.loops.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * @author codi
 * @title: HealthCondition
 * @projectName admin
 * @description: 健康检查条件
 * @date 2019-08-05 16:41
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class HealthCondition {
  public static final HealthCondition DEFAULT_ACTIVITY_HEALTH_CONDITION = new HealthCondition(0, 0,
    Arrays.asList(200, 302));
  public static final HealthCondition DEFAULT_ACTIVITY_UN_HEALTH_CONDITION = new HealthCondition(0, 0,
    Arrays.asList(429, 404, 500, 501, 502, 503, 504, 505));
  public static final HealthCondition DEFAULT_PASSIVE_HEALTH_CONDITION = new HealthCondition(0, 0,
    Arrays.asList(200, 201, 202, 203, 204, 205, 206, 207, 208, 226, 300, 301, 302, 303, 304, 305, 306, 307, 308));
  public static final HealthCondition DEFAULT_PASSIVE_UN_HEALTH_CONDITION = new HealthCondition(0, 0,
    Arrays.asList(429, 500, 503));
  private Integer successes;
  private Integer failures;
  private List<Integer> httpStatuses;
}
