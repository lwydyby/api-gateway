package com.loopswork.loops.entity;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

import java.util.Date;

/**
 * @author liwei
 * @description 日期时间戳转换器
 * @date 2019-12-11 15:43
 */
public class DateDeSerializer implements Converter<Long, Date> {
  @Override
  public Date convert(Long s) {
    return new Date(s);
  }

  @Override
  public JavaType getInputType(TypeFactory typeFactory) {
    return typeFactory.constructType(Long.class);
  }

  @Override
  public JavaType getOutputType(TypeFactory typeFactory) {
    return typeFactory.constructType(Date.class);
  }
}
