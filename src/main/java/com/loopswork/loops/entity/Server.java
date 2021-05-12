package com.loopswork.loops.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.entity.LoopsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Codi on 2019-03-12.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Server extends LoopsEntity {
  private Integer retries;
  private Protocol protocols;
  private String host;
  private Integer port;
  private String path;
  private Boolean enable;
}
