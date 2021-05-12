package com.loopswork.loops.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.*;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.manager.UpstreamManager;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author codi
 * @description 目标信息处理工具
 * @date 2020-04-17 16:51
 */
@Singleton
public class TargetUtil {
  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Inject
  private UpstreamManager upstreamManager;

  public TargetInfo getTargetInfo(HttpRequest request, MatchResult matchResult) throws RouterException {
    log.trace("Starting to get target info.");
    Server server = matchResult.getMatchRouter().getServer();
    Router router = matchResult.getMatchRouter().getRouter();
    boolean strip = router.isStripUri();
    String serverUri = server.getPath();
    String requestUri = request.getUri();
    String serverHost = server.getHost();
    //获取匹配的uri
    Uri matchUri = matchResult.getMatchRouter().getMatches().getUri();
    String host;
    int port;
    TargetType type;
    String upstreamId;
    String targetId;
    //判断上游服务是否存在Upstream
    Upstream upstream = upstreamManager.getUpstream(serverHost);
    if (upstream != null) {
      //存在upstream 使用负载均衡逻辑
      Target target = upstreamManager.balance(serverHost, request);
      host = target.getHost();
      port = target.getPort();
      type = TargetType.TARGET;
      targetId = target.getId();
      upstreamId = target.getUpstreamId();
    } else {
      //不存在upstream 使用server中的配置
      //处理Host 将转发请求header中的Host设置为目标server的host:port
      host = server.getHost();
      port = server.getPort();
      type = TargetType.SERVER;
      targetId = null;
      upstreamId = null;
    }
    //获取最终请求的uri
    String remoteUri = handlePath(serverUri, requestUri, matchUri, strip);
    //组织请求目标信息
    TargetInfo targetInfo = new TargetInfo();
    targetInfo.setTargetType(type);
    targetInfo.setHost(host);
    targetInfo.setPort(port);
    targetInfo.setRemoteUri(remoteUri);
    targetInfo.setTargetId(targetId);
    targetInfo.setUpstreamId(upstreamId);
    log.trace("Done getting target info.");
    return targetInfo;
  }

  /**
   * @param serverUri  上游服务的uri
   * @param requestUri 请求携带的uri
   * @param uri        匹配的uri对象
   * @param strip      是否除去path
   * @return 最终请求上游的uri
   */
  private String handlePath(String serverUri, String requestUri, Uri uri, boolean strip) {
    serverUri = serverUri == null ? "" : serverUri;
    requestUri = requestUri == null ? "" : requestUri;
    log.trace("In handlePath, serverUri is {}, requestUri is {}", serverUri, requestUri);
    if (strip && uri != null) {
      String path = uri.getValue();
      if (uri.isPrefix()) {
        requestUri = requestUri.replace(path, "");
      } else if (uri.isRegex()) {
        Pattern pattern = Pattern.compile(path);
        Matcher matcher = pattern.matcher(requestUri);
        if (matcher.find()) {
          //将与正则匹配的字符串去掉
          String matched = matcher.group();
          requestUri = requestUri.replace(matched, "");
        }
      }
      log.trace("After stripping the path, requestUri is {}", requestUri);
    }
    String finalUri = serverUri + requestUri;
    //如果serverUri末尾有`/`，同时请求的uri开头有`/`，则把前面的去掉
    //e.g: serverUri = '/servers/'，requestUri = '/v2.1'，则finalUri应为'/servers/v2.1'
    if (serverUri.endsWith("/") && requestUri.startsWith("/")) {
      finalUri = serverUri.substring(0, serverUri.length() - 1) + requestUri;
    }
    log.trace("Final uri is {}", finalUri);
    return finalUri;
  }
}
