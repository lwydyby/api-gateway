package com.loopswork.loops.entity;

import com.loopswork.loops.admin.entity.dto.*;
import lombok.Data;

import java.util.List;

/**
 * @author codi
 * @description YAML配置实体类
 * @date 2020/2/18 7:35 下午
 */
@Data
public class YamlSettings {
  private String version;
  private List<ServerRequest> servers;
  private List<RouteRequest> routes;
  private List<PluginRequest> plugins;
  private List<ConsumerRequest> consumers;
  private List<UpstreamRequest> upstreams;
  private List<TargetRequest> targets;
  private List<ACLRequest> acls;
  private List<KeyAuthCredentialsRequest> keyAuthCredentials;
}
