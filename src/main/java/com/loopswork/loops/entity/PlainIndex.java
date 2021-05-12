package com.loopswork.loops.entity;


import lombok.Data;

import java.util.Set;

/**
 * @ClassName: PlainIndex
 * @Author: Fan Zhang
 * @Date: 2019-03-18 16:34
 */
@Data
public class PlainIndex {
  private Set<String> hosts;
  private Set<String> uris;
  private Set<String> methods;
}
