package com.loopswork.loops.entity;

import lombok.Data;

/**
 * @ClassName: CategoriesWeight
 * @Author: Fan Zhang
 * @Date: 2019-03-18 16:34
 */
@Data
public class CategoriesWeight {
  //categories.matchRule
  private byte categoryBit;
  //categories.category.matchWeight;
  private int categoryWeight;
}
