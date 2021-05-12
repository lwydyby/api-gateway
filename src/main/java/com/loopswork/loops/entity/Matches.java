package com.loopswork.loops.entity;


import lombok.Data;

/**
 * @ClassName: Matches
 * @Author: Fan Zhang
 * @Date: 2019-03-18 16:34
 */
@Data
public class Matches {
  private boolean uriCaptures;
  private Uri uri;
  private Host host;
  private HttpMethod method;
}
