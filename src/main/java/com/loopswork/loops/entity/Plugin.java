package com.loopswork.loops.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.entity.LoopsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * Created by Codi on 2019-03-13.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Plugin extends LoopsEntity {
  private String routeId;
  private String serverId;
  private String consumerId;
  private Server server;
  private Route route;
  private Consumer consumer;
  //  @JsonSerialize(keyUsing = DotSerializer.class)
//  @JsonDeserialize(keyUsing = DotDeSerializer.class)
  private Map<String, Object> config;
  private boolean enable;
}
