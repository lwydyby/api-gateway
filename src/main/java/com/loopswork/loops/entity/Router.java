package com.loopswork.loops.entity;


import lombok.Data;

import java.util.Set;

/**
 * @author Fan Zhang
 * @date 2019-03-18 16:34
 */
@Data
public class Router {
  private Protocol protocol;
  private Route route;
  private Server server;
  private boolean stripUri;
  private byte matchRule;
  private int matchWeight;
  private String[] headers;
  private Host[] hosts;
  private Uri[] uris;
  private Set<String> methods;
}
