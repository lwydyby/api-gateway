package com.loopswork.loops.admin.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.houbb.valid.core.annotation.constraint.HasNotNull;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author codi
 * @date 2020/3/4 1:18 下午
 * @description 租户
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsumerRequest {
  @HasNotNull(value = {"name", "customId"}, message = "不能name customId 同时为空")
  @Size(max = 100, message = "名称长度不能超过100")
  private String name;
  @HasNotNull(value = {"name", "customId"}, message = "不能name customId 同时为空")
  private String customId;
  private boolean enable = true;
}
