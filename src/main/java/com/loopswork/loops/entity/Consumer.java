package com.loopswork.loops.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.entity.LoopsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Codi on 2019-03-13.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Consumer extends LoopsEntity {
  private String customId;
  private boolean enable;
}
