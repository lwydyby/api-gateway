package com.loopswork.loops.plugin.impl;

import com.google.inject.Inject;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.entity.ContextKeys;
import com.loopswork.loops.entity.Plugin;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.manager.ACLManager;
import com.loopswork.loops.plugin.BasePluginHandler;
import com.loopswork.loops.plugin.PluginPriority;
import com.loopswork.loops.plugin.PluginType;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;

/**
 * @author liwei
 * @description ACL插件
 * @date 2019-12-17 09:04
 */
public class ACLPlugin extends BasePluginHandler {
  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject
  private ACLManager aclManager;

  @Override
  public void handle(RoutingContext context, Plugin plugin) {
    //1、获取插件配置信息
    Map<String, Object> config = plugin.getConfig();
    //2、检查访问租户是否允许通过 true:允许 false:不允许
    boolean allow = allowConsume(context, config);
    if (allow) {
      context.next();
    } else {
      context.fail(RouterException.e(RouterCode.GROUP_NOT_ALLOWED));
    }
  }

  @SuppressWarnings("unchecked")
  private boolean allowConsume(RoutingContext context, Map<String, Object> config) {
    //获取白名单
    List<String> whiteList = (List<String>) config.get("whitelist");
    //获取黑名单
    List<String> blackList = (List<String>) config.get("blacklist");
    //黑、白名单必须有一个有值
    if (whiteList == null && blackList == null) {
      return false;
    }
    //黑、白名单不能同时存在
    if (whiteList != null && blackList != null) {
      return false;
    }
    List<String> consumerGroups = getGroup(context);
    if (consumerGroups == null) {
      return false;
    }
    //黑名单模式
    if (whiteList == null) {
      return consumerGroups.stream().noneMatch(blackList::contains);
    }
    //白名单模式
    return consumerGroups.stream().anyMatch(whiteList::contains);
  }

  private List<String> getGroup(RoutingContext context) {
    String consumerId = context.get(ContextKeys.CONSUMER_ID);
    if (consumerId != null) {
      return aclManager.getConsumerIdGroupMap().get(consumerId);
    }
    return null;
  }

  @Override
  public int priority() {
    return PluginPriority.ACL;
  }

  @Override
  public PluginType getPluginType() {
    return PluginType.PRE;
  }

  @Override
  public String getName() {
    return PluginName.ACL;
  }
}
