package com.loopswork.loops.plugin.impl;

import com.google.inject.Singleton;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.entity.Plugin;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.plugin.BasePluginHandler;
import com.loopswork.loops.plugin.PluginPriority;
import com.loopswork.loops.plugin.PluginType;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.net.util.SubnetUtils;

import java.util.List;
import java.util.Map;

/**
 * @author codi
 * @description IP黑白检查名单插件
 * @date 2020/2/24 2:57 下午
 */
@Singleton
public class IPRestrictionPlugin extends BasePluginHandler {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void handle(RoutingContext context, Plugin plugin) {
    Map<String, Object> config = plugin.getConfig();
    HttpRequest httpRequest = context.get("httpRequest");

    String switchKey = config.get("switch") == null ? "black" : config.get("switch").toString();
    Object ips = config.get(switchKey.concat("list"));
    // switchKey="black"时，如果黑名单列表为空，不予执行；
    // switchKey="white"时，如果白名单列表为空，不予执行。
    Boolean black = switchKey.equals("black");
    if (ips instanceof List) {
      List<String> restrictionList = (List) ips;
      Boolean block = this.isRestrict(httpRequest.getClientIP(), restrictionList, black);
      if (block) {
        context.fail(RouterException.e(RouterCode.IP_NOT_ALLOWED));
      }
    }
    context.next();
  }

  /**
   * 判断IP是否在名单中
   *
   * @param clientIP        客户端IP
   * @param restrictionList 名单列表
   * @param black           true or False
   * @return 如果black=ture，客户端ip在名单列表中，返回true，反之返回False；
   * 如果switch=false，客户端ip不在名单列表中，返回true，反之返回False。
   */
  private Boolean isRestrict(String clientIP, List<String> restrictionList, Boolean black) {
    boolean block = restrictionList.stream().anyMatch(ip -> clientIP.equals(ip));
    if (!block) {
      block = restrictionList.stream().
        filter(ip -> ip.contains("/")).anyMatch(cidr -> {
        try {
          SubnetUtils subnetUtils = new SubnetUtils(cidr);
          return subnetUtils.getInfo().isInRange(clientIP);
        } catch (IllegalArgumentException e) {
          return false;
        }
      });
    }
    return black == block;
  }

  @Override
  public int priority() {
    return PluginPriority.IP_RESTRICTION;
  }

  @Override
  public PluginType getPluginType() {
    return PluginType.PRE;
  }

  @Override
  public String getName() {
    return PluginName.IP_RESTRICTION;
  }
}
