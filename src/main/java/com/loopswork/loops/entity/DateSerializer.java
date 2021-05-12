package com.loopswork.loops.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

/**
 * @author liwei
 * @description 日期时间戳转换器
 * @date 2019-12-11 15:35
 */
public class DateSerializer extends JsonSerializer<Date> {
  @Override
  public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeObject(date.getTime());
  }
}
