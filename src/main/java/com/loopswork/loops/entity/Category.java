package com.loopswork.loops.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.Map;

/**
 * @ClassName: Category
 * @Author: Fan Zhang
 * @Date: 2019-03-18 16:34
 */
@Data
public class Category {
  private int matchWeight;
  private Map<String, ArrayList<Router>> routersByHosts;
  private Map<String, ArrayList<Router>> routersByUris;
  private Map<String, ArrayList<Router>> routersByMethods;
  private ArrayList<Router> allRouters;
}
