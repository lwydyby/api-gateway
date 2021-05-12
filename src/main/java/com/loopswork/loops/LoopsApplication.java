package com.loopswork.loops;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.manager.Managers;
import com.loopswork.loops.module.*;
import com.loopswork.loops.util.ConfigUtil;
import com.loopswork.loops.verticle.AdminVerticle;
import com.loopswork.loops.verticle.RouterVerticle;
import com.loopswork.loops.verticle.TcpVerticle;
import com.loopswork.loops.verticle.UpdateVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;

import java.io.BufferedReader;

import static io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author codi
 * @title: LoopsApplication
 * @projectName loops
 * @description: loops
 * @date 2019/12/31 3:06 下午
 */
public class LoopsApplication {
  private final Logger log = LoggerFactory.getLogger(LoopsApplication.class);
  private Vertx vertx;
  private Injector injector;
  private LoopsConfig config;

  public static void main(String[] args) {
    //该配置保证vertx使用logback记录日志
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
    Vertx vertx = Vertx.vertx();
    //读取系统配置
    Future.<LoopsConfig>future(h -> ConfigUtil.loadConfig(vertx, h)).compose(config -> {
      //启动
      return Future.<Long>future(h -> new LoopsApplication().run(vertx, config, h));
    }).onFailure(failure -> {
      System.exit(1);
    });
  }

  /**
   * 启动服务
   *
   * @param config 启动配置
   */
  public void run(Vertx vertx, LoopsConfig config, Handler<AsyncResult<Long>> startHandler) {
    long startTime = System.currentTimeMillis();
    //显示banner
    printBanner();
    //创建Vertx
    this.vertx = vertx;
    //读取配置
    this.config = config;
    log.info(config.toString());
    log.info("config load success");
    //创建注入器
    createInjector(config);
    //配置优雅关闭
    shutdown(config.getShutdownTime());
    //初始化Managers
    Future.future(this::initManagers).compose(s -> {
      log.info("Manager init in {}ms", s);
      //部署管理服务
      return Future.future(this::deployAdminVerticle);
    }).compose(s -> {
      //部署路由服务
      return Future.future(this::deployRouterVerticle);
    }).compose(s -> {
      //部署定时服务
      return Future.future(this::deploySchedulerVerticle);
    }).compose(s->{
      //部署tcp服务
      return Future.future(this::deployTcpVerticle);
    }).onSuccess(success -> {
      long time = System.currentTimeMillis() - startTime;
      log.info("Loops start success in {}ms have fun ^_^", time);
      startHandler.handle(Future.succeededFuture(time));
    }).onFailure(failure -> {
      log.error("Loops start error", failure);
      startHandler.handle(Future.failedFuture(failure));
    });
  }

  /**
   * 关闭服务
   */
  public void close(Handler<AsyncResult<Void>> completeHandler) {
    log.info("Loops start to close");
    vertx.close(completeHandler);
  }

  private void printBanner() {
    //加载banner
    BufferedReader reader = ResourceUtil.getReader("banner.txt", UTF_8);
    String banner = IoUtil.read(reader);
    System.out.println(banner);
  }

  private void createInjector(LoopsConfig config) {
    injector = Guice.createInjector(new VertxModule(vertx, config), new LoopsModule(), new PluginModule(), new CollectorModule(), new DataManagerModule());
    log.info("Loops injector created");
  }

  private void initManagers(Handler<AsyncResult<Long>> completeHandler) {
    Managers managers = injector.getInstance(Managers.class);
    managers.init(completeHandler);
  }

  private void deployAdminVerticle(Handler<AsyncResult<String>> completeHandler) {
    AdminVerticle adminVerticle = injector.getInstance(AdminVerticle.class);
    vertx.deployVerticle(adminVerticle, completeHandler);
  }

  private void deployRouterVerticle(Handler<AsyncResult<String>> completeHandler) {
    RouterVerticle routerVerticle = injector.getInstance(RouterVerticle.class);
    vertx.deployVerticle(routerVerticle, completeHandler);
  }

  private void deploySchedulerVerticle(Handler<AsyncResult<String>> completeHandler) {
    UpdateVerticle updateVerticle = injector.getInstance(UpdateVerticle.class);
    vertx.deployVerticle(updateVerticle, completeHandler);
  }

  private void deployTcpVerticle(Handler<AsyncResult<String>> completeHandler){
    TcpVerticle tcpVerticle=injector.getInstance(TcpVerticle.class);
    vertx.deployVerticle(tcpVerticle,completeHandler);
  }

  private void shutdown(int time) {
    //TODO 优雅关闭
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Loops start shutdown, please wait {} ms.", time);
      vertx.deploymentIDs().forEach(vertx::undeploy);
      try {
        Thread.sleep(time);
        log.info("Loops shutdown success");
      } catch (InterruptedException e) {
        log.error("Loops stop interrupted", e);
      }
    }));
  }


  public LoopsConfig getConfig() {
    return config;
  }

  public Injector getInjector() {
    return injector;
  }
}
