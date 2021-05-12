package com.loopswork.loops.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.entity.LoopsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * key-auth认证类
 *
 * @author lixiaoxiao
 * @date 2019/7/19 0019 上午 10:18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyAuthCredentials extends LoopsEntity {
  private String consumerId;
  private Consumer consumer;
  private String key;
  private boolean enable = true;
}
