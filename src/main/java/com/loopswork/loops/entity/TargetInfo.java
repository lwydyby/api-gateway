package com.loopswork.loops.entity;

import lombok.Data;

/**
 * Created by Codi on 2019-05-07.
 */
@Data
public class TargetInfo {
  private String host;
  private int port;
  private String remoteUri;
  private TargetType targetType;
  private String upstreamId;
  private String targetId;
}
