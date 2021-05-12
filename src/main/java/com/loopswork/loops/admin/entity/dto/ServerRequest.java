package com.loopswork.loops.admin.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.loopswork.loops.admin.utils.validate.NotEmpty;
import com.loopswork.loops.entity.Protocol;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * @author codi
 * @date 2020/3/4 1:18 下午
 * @description 服务
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerRequest {
  @NotEmpty(message = "名称不能为空")
  @Size(max = 100, message = "名称长度不能超过100")
  private String name;
  @Max(value = 32767, message = "重试次数必须小于32767")
  @Min(value = 0, message = "重试次数必须大于等于0")
  private int retries;
  private Protocol protocols = Protocol.HTTP;
  @NotEmpty(message = "host 不能为空")
  private String host;
  @Max(value = 65535, message = "端口不能大于65535")
  @Min(value = 0, message = "端口不能小于0")
  private int port = 80;
  private String path;
  private int connectTimeout = 60000;
  private int writeTimeout = 60000;
  private int readTimeout = 60000;
  private boolean enable = true;
}
