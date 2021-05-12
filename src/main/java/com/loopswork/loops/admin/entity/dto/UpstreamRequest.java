package com.loopswork.loops.admin.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.inject.internal.cglib.proxy.$Callback;
import com.loopswork.loops.admin.utils.validate.NotEmpty;
import com.loopswork.loops.admin.utils.validate.Range;
import com.loopswork.loops.entity.HashType;
import com.loopswork.loops.entity.HealthCheck;
import com.loopswork.loops.entity.HealthType;
import lombok.Data;

import javax.validation.constraints.Size;

import static com.loopswork.loops.entity.HealthCondition.*;

/**
 * @author codi
 * @date 2020/8/13 1:18 下午
 * @description 上游
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpstreamRequest {
  public static HealthCheck DEFAULT_ACTIVE_HEALTH_CHECK = new HealthCheck(false, HealthType.ACTIVE, 0, 0, null, 0, DEFAULT_ACTIVITY_HEALTH_CONDITION,
    DEFAULT_ACTIVITY_UN_HEALTH_CONDITION, 0L);
  public static HealthCheck DEFAULT_PASSIVE_HEALTH_CHECK = new HealthCheck(false, HealthType.PASSIVE, 0, 0, null, 0, DEFAULT_PASSIVE_HEALTH_CONDITION,
    DEFAULT_PASSIVE_UN_HEALTH_CONDITION, 0L);
  @NotEmpty(message = "名称不能为空")
  @Size(max = 100, message = "名称长度不能超过100")
  private String name;
  private HashType hashOn = HashType.NONE;
  private HashType hashFallback = HashType.NONE;
  private String hashOnHeader;
  private String hashFallbackHeader;
  @Range(min = 4, max = 16, message = "slots范围必须在4~16之间")
  private int slots = 6;
  private HealthCheck activeHealthCheck = DEFAULT_ACTIVE_HEALTH_CHECK;
  private HealthCheck passiveHealthCheck = DEFAULT_PASSIVE_HEALTH_CHECK;
  private boolean enable = true;
  private boolean lvs = false;
}
