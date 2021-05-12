package com.loopswork.loops.plugin.impl;


import com.google.inject.Inject;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.entity.ContextKeys;
import com.loopswork.loops.entity.HttpMethod;
import com.loopswork.loops.entity.Plugin;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.manager.ConsumerManager;
import com.loopswork.loops.manager.KeyAuthManager;
import com.loopswork.loops.plugin.BasePluginHandler;
import com.loopswork.loops.plugin.PluginPriority;
import com.loopswork.loops.plugin.PluginType;
import com.loopswork.loops.util.StringUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.loopswork.loops.plugin.impl.JWTPlugin.getStringMap;

/**
 * @author liwei
 * @description KeyAuth鉴权插件
 * @date 2019-12-16 19:33
 */
public class KeyAuthPlugin extends BasePluginHandler {
  private Logger log = LoggerFactory.getLogger(this.getClass());
  @Inject
  private KeyAuthManager keyAuthManager;
  @Inject
  private ConsumerManager consumerManager;

  public Map<String, String> stringToMap(String value) {
    return getStringMap(value);
  }

  @Override
  public void handle(RoutingContext context, Plugin plugin) {
    //1、获取插件config
    Map<String, Object> config = plugin.getConfig();
    //2、获取http请求
    HttpRequest httpRequest = context.get(ContextKeys.HTTP_REQUEST);
    //runOnPreflight的属性为false则所有OPTIONS请求都放过
    String key = null;
    String consumerId = null;
    Boolean enable = Boolean.FALSE;
    if (httpRequest.getMethod().equals(HttpMethod.OPTIONS) && !(Boolean) config.get("runOnPreflight")) {
      context.next();
    } else {
      //3、解析配置，获取key值
      key = getKey(httpRequest, config);
      if (!StringUtils.isEmpty(key)) {
        log.debug("Request key:{}", key);
        //4、匹配key-auth
        consumerId = keyAuthManager.getKeyConsumerIdMap().get(key);
        if (!StringUtils.isEmpty(consumerId)) {
          log.debug("Request consumer:{}", consumerId);
          //5、检查租户是否可用
          enable = consumerManager.getEnableMap().get(consumerId);
          log.debug("consumerId:{}'s status is enable:{}", consumerId, enable);
          if (enable != null && enable) {
            context.put(ContextKeys.KEY_AUTH, key);
            context.put(ContextKeys.CONSUMER_ID, consumerId);
            context.next();
          }
        }
      }
    }
    //6、key-auth鉴权失败，查看是否匿名
    if (key == null || consumerId == null || (enable != null && !enable) || enable == null) {
      //匿名租户id
      String anonymous = (String) config.get("anonymous");
      //判断匿名是否通过
      if (!StringUtils.isEmpty(anonymous)) {
        Boolean anonymousEnable = consumerManager.getEnableMap().get(anonymous);
        log.debug("anonymous:{} is enable:{}", anonymous, anonymousEnable);
        if (anonymousEnable != null && anonymousEnable) {
          context.put(ContextKeys.CONSUMER_ID, anonymous);
          context.next();
        } else {
          context.fail(RouterException.e(RouterCode.KEY_AUTH_FAILED));
        }
      } else {
        context.fail(RouterException.e(RouterCode.KEY_AUTH_FAILED));
      }
    }
  }

  @Override
  public int priority() {
    return PluginPriority.KEY_AUTH;
  }

  @Override
  public PluginType getPluginType() {
    return PluginType.PRE;
  }

  @Override
  public String getName() {
    return PluginName.KEY_AUTH;
  }

  /**
   * 获取key值
   *
   * @param request 请求
   * @param config  插件配置信息
   * @return String
   */
  private String getKey(HttpRequest request, Map<String, Object> config) {
    String keyValue;
    List<String> keyNames = (List<String>) config.get("keyNames");
    Boolean hide = (Boolean) config.get("hideCredentials");
    if (keyNames.size() == 0) {
      keyNames.add("apikey");
    }
    //1、从cookie中获取
    keyValue = request.getHeaders().get("Cookie");
    if (keyValue == null) {
      keyValue = request.getHeaders().get("cookie");
    }
    if (!StringUtils.isEmpty(keyValue)) {
      if (hide) {
        request.getHeaders().keySet().remove("Cookie");
        request.getHeaders().keySet().remove("cookie");
      }
      keyValue = stringToMap(keyValue).get("apikey");
      log.debug("Get keyValue:{} from cookie.", keyValue);
      if (keyValue != null) {
        return keyValue;
      }
    }
    for (String keyName : keyNames) {
      //2、从header中获取
      keyValue = request.getHeaders().get(keyName);
      if (!StringUtils.isEmpty(keyValue)) {
        if (hide) {
          request.getHeaders().keySet().remove(keyName);
        }
        log.debug("Get keyValue:{} from HEADER:{}", keyValue, keyName);
        return keyValue;
      }
      //3、从querystring中获取
      keyValue = request.getParams().get(keyName);
      if (!StringUtils.isEmpty(keyValue)) {
        if (hide) {
          request.setUri(request.getUri().substring(0, request.getUri().indexOf(keyName) - 1));
        }
        log.debug("Get keyValue:{} from query and keyName is {}", keyValue, keyName);
        return keyValue;
      }
      //4、从requestBody中获取
      if ((Boolean) config.get("keyInBody")) {
        Map result = new JsonObject(request.getBody().toString()).getMap();
        keyValue = result.get(keyName).toString();
        if (!StringUtils.isEmpty(keyValue)) {
          log.debug("Get keyValue:{} from requestBody and keyName is {}", keyValue, keyName);
          return keyValue;
        }
      }
    }
    //5、从Referer中获取
    String referer = request.getHeaders().get("Referer");
    log.debug("Get keyValue from referer:{}", referer);
    if (StringUtils.isEmpty(keyValue)) {
      keyValue = getKeyFromReferer(referer, keyNames);
      if (!StringUtils.isEmpty(keyValue)) {
        return keyValue;
      }
    }
    return keyValue;
  }

  /**
   * 从referer去获取key
   *
   * @param referer
   * @param keyNames
   * @return
   */
  private String getKeyFromReferer(String referer, List<String> keyNames) {
    String keyValue = null;
    URI refererUri;
    try {
      if (referer != null) {
        refererUri = new URI(referer);
        String query = refererUri.getQuery();
        log.debug("Get from Referer and query is {}", query);
        if (!StringUtils.isEmpty(query)) {
          String[] params = query.split("&");
          Map<String, String> resMap = new HashMap<String, String>();
          for (String paramString : params) {
            String[] param = paramString.split("=");
            if (param.length >= 2) {
              String key = param[0];
              String value = param[1];
              for (int j = 2; j < param.length; j++) {
                value += "=" + param[j];
              }
              resMap.put(key, value);
            }
          }
          for (String keyName : keyNames) {
            keyValue = resMap.get(keyName);
            if (!StringUtils.isEmpty(query)) {
              log.debug("Get keyValue:{} from Referer and keyName is {}", keyValue, keyName);
              return keyValue;
            }
          }
        }
      }
    } catch (URISyntaxException e) {
      log.error("WrapperResponseGlobalFilter  解析URI error.", e);
    }
    return keyValue;
  }
}
