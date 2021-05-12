package com.loopswork.loops.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.entity.LoopsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author lixiaoxiao
 * @date 2019/7/25 13:32
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ACL extends LoopsEntity {
  private String consumerId;
  private List<String> groups;
  private boolean enable = true;
  private Consumer consumer;
}
