package com.loopswork.loops.verticle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.handler.*;
import com.loopswork.loops.plugin.IPluginHandler;
import com.loopswork.loops.plugin.PluginType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author codi
 * @description 处理请求转发的核心Verticle
 * @date 2020/1/16 4:58 下午
 */
@Singleton
public class RouterVerticle extends AbstractVerticle {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  @Inject
  private LoopsConfig loopsConfig;
  @Inject
  private Set<IPluginHandler> plugins;
  @Inject
  private StateHandler stateHandler;
  @Inject
  private RequestHandler requestHandler;
  @Inject
  private RouterHandler routerHandler;
  @Inject
  private AdaptorHandler adaptorHandler;
  @Inject
  private BodyAdaptorHandler bodyAdaptorHandler;
  @Inject
  private HttpClientHandler httpClientHandler;
  @Inject
  private ReturnHandler returnHandler;
  @Inject
  private GlobalErrorHandler globalErrorHandler;

  @Override
  public void start(Promise<Void> startFuture) {
    //初始化处理器
    Router router = Router.router(vertx);
    initHandlers(router);
    //增加配置初始长度，规避可能存在的uri过长被服务器拒绝的问题
    HttpServerOptions options = new HttpServerOptions();
    options.setMaxInitialLineLength(loopsConfig.getMaxInitialLineLength());
    //创建Http服务器
    vertx.createHttpServer(options).requestHandler(router).listen(loopsConfig.getRouterPort(), http -> {
      if (http.succeeded()) {
        log.info("Loops router HTTP server started on port:" + loopsConfig.getRouterPort());
        startFuture.handle(Future.succeededFuture());
      } else {
        log.error("Loops router HTTP server start error!", http.cause());
        startFuture.handle(Future.failedFuture(http.cause()));
      }
    });
  }

  private void initHandlers(Router router) {
    //状态处理器 状态异常直接拒绝请求
    router.route().handler(stateHandler);
    //请求处理器 整理请求体
    router.route().handler(requestHandler);
    //路由处理器 匹配路由
    router.route().handler(routerHandler);
    //请求体处理器
    router.route().handler(adaptorHandler);
    router.route().handler(bodyAdaptorHandler);
    //前置插件
    initPrePlugins(router);
    //转发处理器 转发请求
    router.route().handler(httpClientHandler);
    //后置插件
    initPostPlugins(router);
    //返回处理器
    router.route().handler(returnHandler);
    //全局错误处理器
    router.route().failureHandler(globalErrorHandler);
  }

  private void initPrePlugins(Router router) {
    //前置插件
    List<IPluginHandler> prePlugins = plugins.stream()
      .filter(plugin -> plugin.getPluginType() == PluginType.PRE)
      .sorted(Comparator.comparingInt(IPluginHandler::priority).reversed())
      .collect(Collectors.toList());
    for (IPluginHandler plugin : prePlugins) {
      router.route().handler(plugin);
      log.info("PrePlugin init success [{}]", plugin.getName());
    }
  }

  private void initPostPlugins(Router router) {
    //后置插件
    List<IPluginHandler> postPlugins = plugins.stream()
      .filter(plugin -> plugin.getPluginType() == PluginType.POST)
      .sorted(Comparator.comparingInt(IPluginHandler::priority).reversed())
      .collect(Collectors.toList());
    for (IPluginHandler plugin : postPlugins) {
      router.route().handler(plugin);
      log.info("PostPlugin init success [{}]", plugin.getName());
    }
  }

}
