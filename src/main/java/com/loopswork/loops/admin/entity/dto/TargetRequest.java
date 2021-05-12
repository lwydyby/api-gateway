package com.loopswork.loops.admin.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.utils.validate.NotEmpty;
import com.loopswork.loops.admin.utils.validate.Range;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author codi
 * @date 2020/3/4 1:18 下午
 * @description 目标
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TargetRequest {
  @NotEmpty(message = "名称不能为空")
  @Size(max = 100, message = "名称长度不能超过100")
  private String name;
  @NotEmpty(message = "UpstreamId不能为空")
  private String upstreamId;
  @NotEmpty(message = "HOST 不能为空")
  private String host;
  @Range(max = 65535, message = "端口范围必须在0~65535之间")
  private int port = 80;
  @Range(max = 1000, message = "比重范围必须在0~1000之间")
  private int weight = 100;
}
