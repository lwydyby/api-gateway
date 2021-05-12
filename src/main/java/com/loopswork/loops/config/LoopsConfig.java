package com.loopswork.loops.config;

import com.google.inject.Singleton;
import com.loopswork.loops.admin.collector.CollectorType;
import lombok.Data;
import lombok.ToString;

/**
 * @author codi
 * @description 启动配置信息
 * @date 2020/1/7 3:06 下午
 */
@Data
@Singleton
@ToString
public class LoopsConfig {
  /**
   * 上游服务连接超时时间 单位为毫秒
   */
  private int connectTimeout = 5000;
  /**
   * 上游服务加载超时时间 单位为毫秒
   */
  private int readTimeout = 60000;
  /**
   * 重试间隔，单位为毫秒ms，默认为5000ms
   */
  private int retryInterval = 5000;
  /**
   * 网关服务的端口号
   */
  private int routerPort = 9000;
  /**
   * 管理服务端口号
   */
  private int adminPort = 9100;
  /**
   * 初始化RouterVerticle的个数
   */
  private int count = 1;
  /**
   * httpClient连接池容量
   */
  private int clientPollSize = 4096;
  /**
   * Response的bodyBuffer可以打印的默认bodyBuffer的最大长度
   * 超过该长度的response.getBody()不打印到日志中
   */
  private int bodyBufferMaxLength = 1000;
  /**
   * Default max length of the initial line (e.g. "GET / HTTP/1.0") in bytes.
   */
  private int maxInitialLineLength = 65535;
  /**
   * 负载均衡器重试次数
   */
  private int balancerRetry = 5;
  /**
   * 配置加载器 可选择 mongo yaml mock
   */
  private CollectorType collector = CollectorType.yaml;
  /**
   * yaml配置文件路径
   * 只当使用yaml配置加载器时有效
   */
  private String yamlPath;
  /**
   * mongo数据库连接信息
   * 只当使用mongo配置加载器时有效
   */
  private String connectionString;
  /**
   * 是否打开请求流水日志
   */
  private boolean traceLog = false;
  /**
   * 优雅关闭延迟
   */
  private int shutdownTime = 5000;
  /**
   * 管理端口权限检查
   */
  private boolean adminAuth = false;
  /**
   * 管理员账号
   */
  private String username = "admin";
  /**
   * 管理员密码
   */
  private String password = "loopswork";

  private String httpVersion = "1.1";
  /**
   * webclient配饰keepAlive
   */
  private boolean keepAlive = true;
  /**
   * jwt私钥
   */
  private String secret;
}
