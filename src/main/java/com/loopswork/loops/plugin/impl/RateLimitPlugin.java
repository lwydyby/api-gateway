package com.loopswork.loops.plugin.impl;

import com.google.inject.Singleton;
import com.loopswork.loops.admin.entity.enums.PluginName;
import com.loopswork.loops.entity.ContextKeys;
import com.loopswork.loops.entity.Plugin;
import com.loopswork.loops.exception.RouterCode;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.plugin.BasePluginHandler;
import com.loopswork.loops.plugin.PluginPriority;
import com.loopswork.loops.plugin.PluginType;
import com.loopswork.loops.util.StringUtils;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisClientType;
import io.vertx.redis.client.RedisOptions;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author codi
 * @description 请求限流插件
 * @date 2020/1/19 9:47 上午
 */
@Singleton
public class RateLimitPlugin extends BasePluginHandler {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private Map<String, Future<Redis>> redisClientMap = new HashMap<>();

  /**
   * 日期转化为字符串
   *
   * @param pattern 描述日期和时间格式的模式
   * @param date    待转化的日期
   */
  private static String dateFormat(String pattern, Date date) {
    date = Optional.ofNullable(date).orElse(new Date());
    SimpleDateFormat dFormat = new SimpleDateFormat(pattern);
    return dFormat.format(date);
  }

  @Override
  public String getName() {
    return PluginName.RATE_LIMITING;
  }

  @Override
  public int priority() {
    return PluginPriority.RATE_LIMITING;
  }

  @Override
  public PluginType getPluginType() {
    return PluginType.PRE;
  }

  @Override
  public void handle(RoutingContext context, Plugin plugin) {
    Map<String, Object> config = plugin.getConfig();
    Date currentDate = new Date();
    Future<Redis> future = this.createClient(context, plugin);
    future.setHandler(ar -> {
      if (future.succeeded()) {
        // 只有redis client创建成功之后才将其放入缓存
        String clientKey = this.getClientKey(plugin.getId(), plugin.getUpdatedAt());
        redisClientMap.put(clientKey, future);
        // 针对第二种情况，需要删除旧的plugin的信息。
        List<String> olders = new ArrayList<>();
        redisClientMap.forEach((key, value) -> {
          // 筛选出原先旧的plugin的信息
          if (!key.equals(clientKey) && key.startsWith(plugin.getId())) {
            olders.add(key);
          }
        });
        olders.forEach(value -> {
          // 从map中删除plugin对应的信息
          Future<Redis> result = redisClientMap.remove(value);
          if (result != null) {
            Redis redisClient = result.result();
            log.debug("[request: {}] Deleting the redis client from the cache, and the client's cache key is {}.",
              context.get(ContextKeys.ID).toString(), value);
            // TODO 是否可以执行关闭的操作。无法确定的原因：可能有请求正在使用该client进行数据库的操作。
            redisClient.close();
          }
        });
        Redis redisClient = future.result();
        RedisAPI redisAPI = RedisAPI.api(redisClient);
        this.checkLimit(redisAPI, config, currentDate, "second")
          .compose(r -> this.checkLimit(redisAPI, config, currentDate, "minute"))
          .compose(r -> this.checkLimit(redisAPI, config, currentDate, "hour"))
          .compose(r -> this.checkLimit(redisAPI, config, currentDate, "day"))
          .compose(r -> this.checkLimit(redisAPI, config, currentDate, "month"))
          .compose(r -> this.checkLimit(redisAPI, config, currentDate, "year"))
          .setHandler(r -> {
            if (r.failed()) {
              context.fail(r.cause());
            } else {
              context.next();
            }
          });
      } else {
        context.fail(ar.cause());
      }
    });
  }

  /**
   * 初始化redis client
   */
  private Future<Redis> createClient(RoutingContext context, Plugin plugin) {
    Map<String, Object> config = plugin.getConfig();
    // TODO 如果需要缓存redis client，避免每个请求都去创建redis的链接，redis.conf需要配置：
    // timeout 0
    // TODO 如果网络发生中断之后恢复正常，该client亦不可用。目前无此处理。报错为：connection was broken。
    String clientKey = this.getClientKey(plugin.getId(), plugin.getUpdatedAt());
    if (redisClientMap.containsKey(clientKey)) {
      return redisClientMap.get(clientKey);
    } else {
      // 如果map中不存在，有两种可能：
      // 1. 该plugin是第一次使用；
      // 2. 该plugin进行了更新。
      // 如果是第二种情况，需要对map中旧有的plugin的信息进行删除。
      Future<Redis> future = Future.future(Promise::complete);
      String redisHost = Optional.ofNullable(config.get("redis_host"))
        .map(String::valueOf)
        .orElse("127.0.0.1");
      int redisPort = Optional.ofNullable(config.get("redis_port"))
        .map(String::valueOf)
        .map(Float::valueOf)
        .map(Float::intValue)
        .orElse(6379);
      Integer redisDatabase = Optional.ofNullable(config.get("redis_database"))
        .map(String::valueOf)
        .map(Float::valueOf)
        .map(Float::intValue)
        .orElse(null);
      String redisPassword = Optional.ofNullable(config.get("redis_password"))
        .map(String::valueOf)
        .orElse(null);
      int redisTimeout = Optional.ofNullable(config.get("redis_timeout"))
        .map(String::valueOf)
        .map(Float::valueOf)
        .map(Float::intValue)
        .orElse(2000);
      SocketAddress endpoint = SocketAddress.inetSocketAddress(redisPort, redisHost);
      RedisOptions redisOptions = new RedisOptions()
        .setEndpoint(endpoint)
        .setType(RedisClientType.STANDALONE)
        .setNetClientOptions(new NetClientOptions().setConnectTimeout(redisTimeout));
      if (Objects.nonNull(redisDatabase)) {
        redisOptions.setSelect(redisDatabase);
      }
      if (!StringUtils.isEmpty(redisPassword)) {
        redisOptions.setPassword(redisPassword);
      }
      Redis.createClient(context.vertx(), redisOptions)
        .connect(onConnect -> {
          if (onConnect.succeeded()) {
            future.handle(Future.succeededFuture(onConnect.result()));
          } else {
            future.handle(Future.failedFuture(onConnect.cause()));
          }
        });
      return future;
    }
  }

  private Future<Void> checkLimit(RedisAPI redisAPI, Map<String, Object> config, Date currentDate, String type) {
    Future<Void> future = Future.future(Promise::complete);
    Long limit = Optional.ofNullable(config.get(type))
      .map(String::valueOf)
      .map(Float::valueOf)
      .map(Float::longValue)
      .orElse(null);
    String key = this.getKey(currentDate, type);
    if (limit != null) {
      Future<Long> future1 = this.increase(redisAPI, key, limit);
      future1.setHandler(r -> {
        if (r.succeeded()) {
          Long num = future1.result();
          if (num == 1) {
            this.expire(redisAPI, currentDate, type)
              .setHandler(h -> {
                if (h.succeeded()) {
                  future.handle(Future.succeededFuture());
                } else {
                  future.handle(Future.failedFuture(h.cause()));
                }
              });
          } else {
            future.handle(Future.succeededFuture());
          }
        } else {
          future.handle(Future.failedFuture(r.cause()));
        }
      });
    } else {
      future.handle(Future.succeededFuture());
    }
    return future;
  }

  /**
   * 执行redis的increase操作
   */
  private Future<Long> increase(RedisAPI redisAPI, String key, long limit) {
    Future<Long> future = Future.future(Promise::complete);
    redisAPI.incr(key, ar -> {
      if (ar.succeeded()) {
        Long result = ar.result().toLong();
        if (result > limit) {
          future.handle(Future.failedFuture(RouterException.e(RouterCode.RATE_LIMIT_EXCEEDED)));
        } else {
          future.handle(Future.succeededFuture(result));
        }
      } else {
        future.handle(Future.failedFuture(ar.cause()));
        return;
      }
    });
    return future;
  }

  /**
   * 设置key的超时时间
   */
  private Future<Long> expire(RedisAPI redisAPI, Date currentDate, String type) {
    Future<Long> future = Future.future(Promise::complete);
    String key = this.getKey(currentDate, type);
    redisAPI.expire(key, this.getExpireTime(type), ar -> {
      if (ar.succeeded()) {
        future.handle(Future.succeededFuture(0L));
      } else {
        future.handle(Future.failedFuture(ar.cause()));
        return;
      }
    });
    return future;
  }

  private String getKey(Date currentDate, String type) {
    String key = null;
    switch (type) {
      case "second":
        key = dateFormat("yyyyMMddHHmmss", currentDate);
        break;
      case "minute":
        key = dateFormat("yyyyMMddHHmm", currentDate);
        break;
      case "hour":
        key = dateFormat("yyyyMMddHH", currentDate);
        break;
      case "day":
        key = dateFormat("yyyyMMdd", currentDate);
        break;
      case "month":
        key = dateFormat("yyyyMM", currentDate);
        break;
      case "year":
        key = dateFormat("yyyy", currentDate);
        break;
      default:
        break;
    }
    return key;
  }

  private String getExpireTime(String type) {
    int key = 1;
    switch (type) {
      case "year":
        key = key * 12 * 30 * 24 * 60 * 60;
        break;
      case "month":
        key = key * 30 * 24 * 60 * 60;
        break;
      case "day":
        key = key * 24 * 60 * 60;
        break;
      case "hour":
        key = key * 60 * 60;
        break;
      case "minute":
        key = key * 60;
        break;
      default:
        break;
    }
    return key + "";
  }
}
