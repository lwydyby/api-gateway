package com.loopswork.loops.admin.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Sets;
import com.loopswork.loops.admin.utils.validate.HasNotEmpty;
import com.loopswork.loops.admin.utils.validate.NotEmpty;
import com.loopswork.loops.entity.HttpMethod;
import com.loopswork.loops.entity.Protocol;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * @author codi
 * @date 2020/3/4 1:18 下午
 * @description 路由
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteRequest {
  @NotEmpty(message = "Server不能为空")
  private String serverId;
  @NotEmpty(message = "名称不能为空")
  @Size(max = 100, message = "名称长度不能超过100")
  private String name;
  @HasNotEmpty(value = {"paths", "hosts", "methods"}, message = "不能paths hosts methods同时为空")
  private Set<String> paths;
  @HasNotEmpty(value = {"paths", "hosts", "methods"}, message = "不能paths hosts methods同时为空")
  private Set<String> hosts;
  private Set<Protocol> protocols = Sets.newHashSet(Protocol.HTTP, Protocol.HTTPS);
  @HasNotEmpty(value = {"paths", "hosts", "methods"}, message = "不能paths hosts methods同时为空")
  private Set<HttpMethod> methods;
  @Min(0)
  @Max(65535)
  private int priority;
  private boolean stripPath = true;
  private boolean enable = true;
  private boolean blocked = false;
  private boolean direct = false;
  private Integer port;
}
