package com.loopswork.loops.verticle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.config.LoopsConfig;
import com.loopswork.loops.entity.*;
import com.loopswork.loops.exception.RouterException;
import com.loopswork.loops.manager.ClientManager;
import com.loopswork.loops.manager.RouterManager;
import com.loopswork.loops.manager.UpstreamManager;
import com.loopswork.loops.util.ConsoleLogger;
import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

import java.util.Set;

/**
 * @author liwei
 * @description 处理TCP请求转发的核心Verticle
 * @date 2020/4/1 4:40 下午
 */
@Singleton
public class TcpVerticle extends AbstractVerticle {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  @Inject
  private LoopsConfig loopsConfig;
  @Inject
  private UpstreamManager upstreamManager;
  @Inject
  private RouterManager routerManager;
  @Inject
  private ConsoleLogger consoleLogger;
  @Inject
  private ClientManager clientManager;

  @SuppressWarnings("DuplicatedCode")
  @Override
  public void start(Promise<Void> startFuture) {
    Set<Integer> ports = routerManager.getTcpPorts();
    int num = ports.size();
    if (num == 0) {
      startFuture.handle(Future.succeededFuture());
      return;
    }
    Future<Void> future = Future.future(promise -> {
      log.info("start tcp verticle");
      promise.handle(Future.succeededFuture());
    });
    int index = 1;
    for (Integer port : ports) {
      if (index != num) {
        future.compose(s -> Future.<Void>future(promise -> {
          vertx.createNetServer().connectHandler(handle(port)).listen(port, listenHandler(promise,port));
        }));
      } else {
        future.onSuccess(v -> {
          vertx.createNetServer().connectHandler(handle(port)).listen(port, listenHandler(startFuture,port));
        }).onFailure(err->{
          startFuture.handle(Future.failedFuture(err));
        });
      }
      index++;
    }
  }
  private Handler<AsyncResult<NetServer>> listenHandler(Handler<AsyncResult<Void>> promise,int port){
    return server -> {
      if (server.succeeded()) {
        log.info("Loops router TCP server started on port:" + port);
        promise.handle(Future.succeededFuture());
      } else {
        log.error("Loops router TCP server start error!", server.cause());
        promise.handle(Future.failedFuture(server.cause()));
      }
    };
  }


  private Handler<NetSocket> handle(int port) {
    return socket -> {
      MatchResult matchResult = routerManager.matchTcpRequest(port);
      if (matchResult.getMatchState() == MatchState.MATCHED) {
        //匹配成功 保存匹配结果
        log.trace("Route matched serverId:" + matchResult.getMatchRouter().getServer().getId());
        log.trace("Route matched routerId:" + matchResult.getMatchRouter().getRouter().getRoute().getId());
      } else if (matchResult.getMatchState() == MatchState.ERROR) {
        socket.close();
        return;
      } else {
        //匹配失败
        log.trace("No Route matched");
        socket.close();
        return;
      }
      try {
        proxy(matchResult, socket, 0);
      } catch (RouterException e) {
        log.error("fail tcp router", e);
        socket.close();
      }
    };
  }

  private void proxy(MatchResult matchResult, NetSocket socket, int tries) throws RouterException {
    TargetInfo targetInfo = getTargetInfo(matchResult);
    int targetPort = targetInfo.getPort();
    String targetHost = targetInfo.getHost();
    log.trace("Connecting to proxy host:{} port:{}", targetHost, targetPort);
    NetClient client = clientManager.getCurrentThreadNetClient();
    client.connect(targetPort, targetHost, ar -> {
      if (ar.succeeded()) {
        log.trace("Connected to proxy,host:{}", targetHost);
        consoleLogger.log(matchResult, targetInfo, socket.localAddress().host());
        NetSocket serverSocket = ar.result();
        serverSocket.pipeTo(socket, asyncResult -> {
          if (asyncResult.succeeded()) {
            log.trace("Connected to proxy ,host:{} target is end", targetHost);
          } else {
            log.error("proxy connection error", asyncResult.cause());
          }
        });
        socket.pipeTo(serverSocket, asyncResult -> {
          if (asyncResult.succeeded()) {
            log.trace("client closed");
          } else {
            log.error("proxy connection error", asyncResult.cause());
          }
        });

      } else {
        log.error("Fail proxy connection");
        if (targetInfo.getTargetType() == TargetType.TARGET && tries < loopsConfig.getBalancerRetry()) {
          log.trace("Target error, try other targets.");
          //负载均衡上游请求失败 目标被动失败计数
          upstreamManager.addTargetStateCount(targetInfo.getUpstreamId(), targetInfo.getTargetId(), HealthType.PASSIVE,
            HealthStatus.UNHEALTHY);
          addCheckTask(targetInfo);
          //少于重试次数时 切换目标重试
          try {
            proxy(matchResult, socket, tries + 1);
          } catch (RouterException e) {
            socket.close();
          }
        } else {
          log.debug("All targets error!");
          socket.close();
        }
      }
    });
  }

  private void addCheckTask(TargetInfo targetInfo){
    vertx.setPeriodic(5000,id->{
      NetClient client = clientManager.getCurrentThreadNetClient();
      client.connect(targetInfo.getPort(),targetInfo.getHost(),ar->{
        if(ar.succeeded()){
          log.info("target has recover,host:{},port:{}",targetInfo.getHost(),targetInfo.getPort());
          upstreamManager.addTargetStateCount(targetInfo.getUpstreamId(), targetInfo.getTargetId(), HealthType.PASSIVE,
            HealthStatus.HEALTHY);
          ar.result().close();
          vertx.cancelTimer(id);
        }
      });

    });
  }
  /**
   * 获取tcp的后端服务目标地址，目前和http的逻辑基本保持一致
   *
   * @param matchResult 比对结果
   * @return 返回目标信息
   * @throws RouterException 路由异常
   */
  private TargetInfo getTargetInfo(MatchResult matchResult) throws RouterException {
    log.trace("Starting to get tcp target info.");
    Server server = matchResult.getMatchRouter().getServer();
    String serverHost = server.getHost();
    String host;
    int port;
    TargetType type;
    String upstreamId;
    String targetId;
    //判断上游服务是否存在Upstream
    Upstream upstream = upstreamManager.getUpstream(serverHost);
    if (upstream != null) {
      //存在upstream 使用负载均衡逻辑
      Target target = upstreamManager.balance(serverHost, null);
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
    //组织请求目标信息
    TargetInfo targetInfo = new TargetInfo();
    targetInfo.setTargetType(type);
    targetInfo.setHost(host);
    targetInfo.setPort(port);
    targetInfo.setTargetId(targetId);
    targetInfo.setUpstreamId(upstreamId);
    log.trace("Done getting tcp target info.");
    return targetInfo;
  }
}
