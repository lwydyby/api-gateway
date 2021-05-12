package com.loopswork.loops.plugin;

import com.google.inject.Inject;
import com.loopswork.loops.entity.ContextKeys;
import com.loopswork.loops.entity.MatchResult;
import com.loopswork.loops.entity.Plugin;
import com.loopswork.loops.entity.Route;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.manager.PluginManager;
import io.vertx.ext.web.RoutingContext;

import java.util.Date;

/**
 * @author Armstrong Liu
 * @date 2019/4/3 10:10
 */
public abstract class BasePluginHandler implements IPluginHandler {
  @Inject
  private PluginManager pluginManager;

  /**
   * 具体的业务逻辑
   *
   * @param context 上下文
   * @param plugin  插件
   */
  public abstract void handle(RoutingContext context, Plugin plugin);

  @Override
  public void handle(RoutingContext context) {
    Plugin plugin = this.getPlugin(context);
    if (plugin == null) {
      context.next();
    } else {
      this.handle(context, plugin);
    }
  }

  private Plugin getPlugin(RoutingContext context) {
    // 从上下文中获取该请求匹配到的路由的信息
    MatchResult matchResult = context.get(ContextKeys.MATCH_RESULT);
    if (matchResult == null) {
      context.fail(RouterException.e(RouterCode.NO_ROUTE_MATCHED));
      return null;
    } else {
      Route route = matchResult.getMatchRouter().getRouter().getRoute();
      return pluginManager.getPlugin(this.getName(), route.getId(), route.getServerId(), context.get(ContextKeys.CONSUMER_ID));
    }
  }

  /**
   * 部分插件中需要创建client（redis client或者 web client），为了提高性能，同一个插件只创建一次client（不更新的前提下）。
   * 该插件返回插件ID和插件更新日期组成的字符串，根据这个值在内存中遍历，如果有针对的client，则使用即可，如若没有找到，
   * 需要创建新的client，然后插入到内存中。
   *
   * @param pluginId 插件ID
   * @param date     插件更新日期
   */
  public String getClientKey(String pluginId, Date date) {
    return String.format("%s:%s", pluginId, date.getTime());
  }

}
