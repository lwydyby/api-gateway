package com.loopswork.loops.plugin;

/**
 * @author Armstrong Liu
 * @description 插件优先级 数值越大越先执行
 * @date 2019/4/16 11:29
 */
public class PluginPriority {

  public final static int JWT = 1005;

  public final static int KEY_AUTH = 1003;

  public final static int IP_RESTRICTION = 990;

  public final static int ACL = 950;

  public final static int RATE_LIMITING = 901;

  public final static int REQUEST_TRANSFORMER = 801;

  public final static int RESPONSE_TRANSFORMER = 800;

  public final static int LOG = 6;
}
