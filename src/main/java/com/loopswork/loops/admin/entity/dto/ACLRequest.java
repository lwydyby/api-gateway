package com.loopswork.loops.admin.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.utils.validate.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * @author lixiaoxiao
 * @date 2019/7/26 9:57
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ACLRequest {
  @NotEmpty(message = "ConsumerId 不能为空")
  private String consumerId;
  private List<String> groups;
  private boolean enable = true;
}
