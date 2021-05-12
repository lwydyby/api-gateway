package com.loopswork.loops.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.entity.LoopsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * @author Codi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Route extends LoopsEntity {
  private String serverId;
  private Set<String> paths;
  private Set<String> hosts;
  private Set<Protocol> protocols;
  private Set<HttpMethod> methods;
  private int priority;
  private boolean stripPath;
  private boolean enable;
  private boolean direct;
  private boolean blocked;
  private Server server;
  private Integer port;
}
