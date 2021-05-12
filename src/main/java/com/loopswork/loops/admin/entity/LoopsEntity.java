package com.loopswork.loops.admin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.loopswork.loops.entity.DateDeSerializer;
import com.loopswork.loops.entity.DateSerializer;
import lombok.Data;

import java.util.Date;

/**
 * @author codi
 * @description Loops实体类
 * @date 2020/2/21 11:13 上午
 */
@Data
public class LoopsEntity {
  @JsonProperty("_id")
  private String id;
  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(converter = DateDeSerializer.class)
  private Date createdAt;
  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(converter = DateDeSerializer.class)
  private Date updatedAt;
  private String name;
}
