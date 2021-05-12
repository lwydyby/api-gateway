package com.loopswork.loops.manager;

import com.loopswork.loops.entity.*;
import com.loopswork.loops.util.RouterUtil;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: RouterMatcher
 * @Author: Fan Zhang
 * @Date: 2019-03-29 09:26
 */
@Data
public class RouterMatcher {
  private static final Logger log = LoggerFactory.getLogger(RouterMatcher.class);
  private Map<Byte, Category> categories = new ConcurrentHashMap<>();
  private List<CategoriesWeight> categoriesWeights = new ArrayList<>();
  private Map<Byte, Integer> categoriesLookup = new ConcurrentHashMap<>();
  private Map<Integer,Router> tcpMaps=new ConcurrentHashMap<>();

  private PlainIndex plainIndex = new PlainIndex();
  private List<Host> wildcardHosts = new ArrayList<>();
  private List<Uri> prefixUris = new ArrayList<>();
  private List<Uri> regexUris = new ArrayList<>();


  void init(List<Router> routers) {
    long start = System.currentTimeMillis();
    log.trace("Initiate RouterMatcher...");
    for (Router router : routers) {
      Route route = router.getRoute();

      if (route.isBlocked()) {
        //在匹配路由的初始化中，只对blocked为false的route进行构建
        log.debug("Skipping blocked route {}", route);
        continue;
      }
      if(router.getProtocol()!=null&&router.getProtocol().equals(Protocol.TCP)){
        //如果是tcp连接
        tcpMaps.put(route.getPort(),router);
        continue;
      }
      RouterUtil.organizeRouter(router);
      RouterUtil.categoryRouter(router, this.categories);
      RouterUtil.indexRouter(router, this.plainIndex, this.wildcardHosts, this.prefixUris, this.regexUris);
    }
    RouterUtil.buildCategoriesWeightsAndLookup(this.categories,
      this.categoriesWeights, this.categoriesLookup);
    prefixUris.sort(Comparator.comparingInt(o -> o.getValue().length()));
    long end = System.currentTimeMillis();
    log.trace("Done initiate RouterMatcher, it costs [{}] milliseconds", end - start);
  }
}
