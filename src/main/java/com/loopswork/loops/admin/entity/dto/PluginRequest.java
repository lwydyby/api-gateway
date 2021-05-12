package com.loopswork.loops.admin.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.utils.validate.NotEmpty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Map;

/**
 * @author codi
 * @date 2020/3/4 1:18 下午
 * @description 插件
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginRequest {
  private String id;
  @NotEmpty(message = "插件名称不能为空")
  @Size(max = 100, message = "名称长度不能超过100")
  private String name;
  private Map<String, Object> config;
  private boolean enable = true;
  private String consumerId;
  private String routeId;
  private String serverId;
}
