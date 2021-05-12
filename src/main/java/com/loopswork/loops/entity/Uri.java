package com.loopswork.loops.entity;

import lombok.Data;

/**
 * @ClassName: Uri
 * @Author: Fan Zhang
 * @Date: 2019-03-18 16:34
 */
@Data
public class Uri {
  private boolean prefix;
  private boolean regex;
  private String value;
  private String regexUri;
  private String stripRegex;
  private boolean hasCaptures;
}
