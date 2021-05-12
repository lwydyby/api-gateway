package com.loopswork.loops.entity;

import lombok.Data;

/**
 * @ClassName: Host
 * @Author: Fan Zhang
 * @Date: 2019-03-18 16:34
 */
@Data
public class Host {
  private boolean wildcard;
  private String value;
  private String regex;
}
