package com.loopswork.loops.admin.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.utils.validate.NotEmpty;
import lombok.Data;

/**
 * @author codi
 * @date 2020/3/4 1:18 下午
 * @description 鉴权
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyAuthCredentialsRequest {
  @NotEmpty(message = "ConsumerId 不能为空")
  private String consumerId;
  @NotEmpty(message = "Key不能为空")
  private String key;
}
