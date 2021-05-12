package com.loopswork.loops.manager;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loopswork.loops.entity.*;
import com.loopswork.loops.http.entity.HttpRequest;
import com.loopswork.loops.util.RouterUtil;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 路由核心服务实现，RouterServiceImpl
 *
 * @author Fan Zhang
 */
@Singleton
public class RouterManager implements IDataManager {
  private static final Set<String> CACHE_EXCLUDE_METHODS = new HashSet<>(Collections.singletonList("DELETE"));
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * RouterMatcher 负责存放整理好的数据。另外，RouterService是初始化、路由匹配和路由更新的入口
   */
  private RouterMatcher routerMatcher;
  /**
   * RouterBlocker 负责"拦截路由"的命中
   */
  private RouterMatcher routerBlocker;
  private Map<String, MatchRouter> matchRoutersCache = new HashMap<>();
  /**
   * 默认要仅注入RestCollectorService
   */
  @Inject
  private Managers managers;

  @Override
  public void init() {
    load();
  }

  @Override
  public void update() {
    //调用这个方法的是一个订阅消息队列的subscriber
    load();
  }

  private synchronized void load() {
    List<Route> routes = new ArrayList<>(managers.getEntityList(Route.class));
    List<Router> routers = RouterUtil.getRouterList(routes);
    RouterMatcher routerMatcher = new RouterMatcher();
    routerMatcher.init(routers);
    this.routerMatcher = routerMatcher;
    RouterMatcher routerBlocker = new RouterMatcher();
    routerBlocker.init(routers);
    this.routerBlocker = routerBlocker;
    //rebuild之后要清空matchRoutersCache
    this.matchRoutersCache = new ConcurrentHashMap<>();
  }
  /**
   * 获取tcp router端口
   */
  public Set<Integer> getTcpPorts(){
    Map<Integer,Router> tcpMaps=routerMatcher.getTcpMaps();
    return tcpMaps.keySet();
  }
  /**
   * 匹配tcp router
   * @param port
   * @return
   */
  public MatchResult matchTcpRequest(Integer port){
    MatchRouter matchRouter;
    MatchResult matchResult = new MatchResult();
    Map<Integer,Router> tcpMaps=routerMatcher.getTcpMaps();
    Router router=tcpMaps.get(port);
    if(router==null){
      matchResult.setMatchState(MatchState.NO_MATCH);
    }else {
      matchResult.setMatchState(MatchState.MATCHED);
      matchRouter=new MatchRouter();
      matchRouter.setRouter(router);
      matchRouter.setServer(router.getServer());
      matchResult.setMatchRouter(matchRouter);
    }
    return matchResult;
  }

  public MatchResult matchHttpRequest(HttpRequest httpRequest, boolean isBlocked) {
    log.trace("Ready to match http request " + httpRequest);
    MatchRouter matchRouter;
    MatchResult matchResult = new MatchResult();
    try {
      HttpMethod method = httpRequest.getMethod();
      String host = httpRequest.getHeaders().get("Host") == null ? "" : httpRequest.getHeaders().get("Host");
      String uri = httpRequest.getUri();
      matchRouter = doHttpRequestMatch(method, host, uri, isBlocked);
      matchResult.setMatchRouter(matchRouter);
      if (matchRouter != null) {
        matchResult.setMatchState(MatchState.MATCHED);
      } else {
        matchResult.setMatchState(MatchState.NO_MATCH);
      }
    } catch (Exception e) {
      log.error("Fail to match http request " + httpRequest, e);
      matchResult.setMatchState(MatchState.ERROR);
    }
    return matchResult;
  }

  /**
   * 用于测试路由匹配
   *
   * @param httpMethod Http方法
   * @param host       目标host
   * @param uri        目标uri
   * @return MatchRouter
   */
  public MatchRouter matchHttpRequest(HttpMethod httpMethod, String host, String uri) {
    if (httpMethod == null) {
      httpMethod = HttpMethod.GET;
    }
    host = host == null ? "" : host;
    uri = uri == null ? "" : uri;
    return doHttpRequestMatch(httpMethod, host, uri, false);
  }

  /**
   * 用于测试路由拦截
   *
   * @param httpMethod Http方法
   * @param host       目标host
   * @param uri        目标uri
   * @return MatchRouter
   */
  public MatchRouter blockHttpRequest(HttpMethod httpMethod, String host, String uri) {
    if (httpMethod == null) {
      httpMethod = HttpMethod.GET;
    }
    host = host == null ? "" : host;
    uri = uri == null ? "" : uri;
    return doHttpRequestMatch(httpMethod, host, uri, true);
  }

  private MatchRouter doHttpRequestMatch(HttpMethod httpMethod, String host, String uri, boolean isBlocked) {
    log.trace("Starting to do http request match for '" + httpMethod.name().toUpperCase() +
      " " + uri + "' , origin host is '" + host + "'");
    //执行请求路由匹配并返回一个MatchRouter对象
    //构造cacheKey，优先查询cache对象中是否有缓存的matchRouter，使用isBlocked区分是否是拦截路由，对DELETE请求不做缓存
    String cacheKey = "";
    if (!CACHE_EXCLUDE_METHODS.contains(httpMethod.name())) {
      cacheKey = httpMethod.name() + ":" + host + ":" + uri + ":" + (isBlocked ? "blocked" : "unblocked");
    }
    MatchRouter matchRouter = matchRoutersCache.get(cacheKey);
    if (matchRouter != null) {
      log.trace("Matched from matched routers cache, matched router is " + matchRouter);
      return matchRouter;
    }
    //如果没有命中缓存，则执行匹配处理，注，hits用于wildcard和method上
    Map<String, String> hits = new HashMap<>();
    //去掉端口
    if (host.contains(":")) {
      host = host.substring(0, host.indexOf(":"));
    }

    byte reqMatchRule = searchAndHit(hits, httpMethod, host, uri, isBlocked);

    if (reqMatchRule != (byte) 0) {
      Map<Byte, Integer> categoriesLookup;
      List<CategoriesWeight> categoriesWeights;
      Map<Byte, Category> categories;
      if (isBlocked) {
        categoriesLookup = routerBlocker.getCategoriesLookup();
        categoriesWeights = routerBlocker.getCategoriesWeights();
        categories = routerBlocker.getCategories();
      } else {
        categoriesLookup = routerMatcher.getCategoriesLookup();
        categoriesWeights = routerMatcher.getCategoriesWeights();
        categories = routerMatcher.getCategories();
      }
      //如果在categoriesLookup中没有找到reqMatchRule的category，不要灰心，
      // 我们从index=0开始再过一遍。因为可能存在reqMatchRule包含的范围会更大。
      int categoryIndex = categoriesLookup.get(reqMatchRule) == null ?
        0 : categoriesLookup.get(reqMatchRule);
      //从最可能的match到较低可能的match迭代进行处理
      while (categoryIndex < categoriesWeights.size()) {
        CategoriesWeight cw = categoriesWeights.get(categoryIndex);
        byte targetMatchRule = cw.getCategoryBit();
        Category category = categories.get(targetMatchRule);

        if (category != null) {
          List<Router> allRouters = category.getAllRouters();
          List<Router> routerCandidates = selectRouterCandidates(category, host, uri, httpMethod);
          log.trace("Try to match from router candidates " + routerCandidates);
          matchRouter = buildMatchRouter(routerCandidates, hits, reqMatchRule, httpMethod, host, uri);
          //如果在routerCandidates没有找到匹配的路由，则尝试在allRouters中查找
          if (matchRouter == null) {
            matchRouter = buildMatchRouter(allRouters, hits, reqMatchRule, httpMethod, host, uri);
          }
          //如果匹配成功，则缓存起来
          if (matchRouter != null) {
            if (!cacheKey.isEmpty()) {
              matchRoutersCache.put(cacheKey, matchRouter);
            }
            log.trace("Succeed in matching http request '" + httpMethod.name().toUpperCase() +
              " " + uri + "' , origin host is '" + host + "'. Match router is " + matchRouter);
            return matchRouter;
          }
        }
        //尝试权重较低的看是否有可能匹配
        categoryIndex += 1;
        log.trace("Try lower one to match http request '" + httpMethod.name().toUpperCase() +
          " " + uri + "' , origin host is '" + host + "'.");
      }
    }
    //没有匹配，返回null
    log.trace("No match for http request '" + httpMethod.name().toUpperCase() +
      " " + uri + "' , origin host is " + host + "'");
    return null;
  }

  private MatchRouter buildMatchRouter(List<Router> routers, Map<String, String> hits, byte reqMatchRule,
                                       HttpMethod httpMethod, String host, String uri) {
    if (routers == null) {
      return null;
    }

    MatchRouter matchRouter = null;
    boolean hostMatched = false;
    boolean uriMatched = false;
    boolean methodMatched = false;
    for (Router r : routers) {
      Matches matches = new Matches();
      //NOTE: 在searchAndHit时，会尽量匹配reqMatchRule，然而在实际与路由进行匹配时，
      //还是要以router.getMatchRule()为准进行匹配。
      byte targetMatchRule = r.getMatchRule();
      Host[] hosts = r.getHosts();
      for (Host h : hosts) {
        //先取hits中的host，用于匹配wildcard host，如果为null，则使用传入的host，即normal host
        String hostHit = hits.get("host") == null ? host : hits.get("host");
        if (hostHit.equals(h.getValue())) {
          hostMatched = true;
          matches.setHost(h);
          break;
        }
      }

      Uri[] uris = r.getUris();
      for (Uri u : uris) {
        //不管是prefix uri还是regex uri，都是用传入的uri字符串进行match，不使用hits
        if (u.isPrefix()) {
          //NOTE: 不应该用equals，比如 route的patch是"/flavors"，
          // 如果请求是"/flavors1"或者"/flavors/123"也是可以匹配上的
          if (uri.contains(u.getValue()) && uri.startsWith(u.getValue())) {
            uriMatched = true;
            matches.setUri(u);
            break;
          }
        } else if (u.isRegex()) {
          if (uri.matches(u.getRegexUri())) {
            uriMatched = true;
            matches.setUri(u);
            break;
          }
        } else {
          //既不是prefix又不是regex，则直接使用value进行判断
          if (uri.equals(u.getValue())) {
            uriMatched = true;
            matches.setUri(u);
            break;
          }
        }
      }

      String methodHit = hits.get("method") == null ? httpMethod.name() : hits.get("method");
      if (r.getMethods().contains(methodHit)) {
        methodMatched = true;
      }

      if (isMatched(hostMatched, uriMatched, methodMatched, targetMatchRule)) {
        matchRouter = new MatchRouter();
        matchRouter.setRouter(r);
        matchRouter.setServer(r.getServer());
        matchRouter.setMatches(matches);
        break;
      }
    }
    return matchRouter;
  }

  private boolean isMatched(boolean hostMatched, boolean uriMatched, boolean methodMatched, byte reqMatchRule) {
    //牺牲优雅，换来可读性
    if (reqMatchRule == MATCH_RULES.HOST.rule) {
      return hostMatched;
    } else if (reqMatchRule == MATCH_RULES.URI.rule) {
      return uriMatched;
    } else if (reqMatchRule == MATCH_RULES.METHOD.rule) {
      return methodMatched;
    } else if (reqMatchRule == (MATCH_RULES.HOST.rule | MATCH_RULES.URI.rule)) {
      return hostMatched && uriMatched;
    } else if (reqMatchRule == (MATCH_RULES.HOST.rule | MATCH_RULES.METHOD.rule)) {
      return hostMatched && methodMatched;
    } else if (reqMatchRule == (MATCH_RULES.URI.rule | MATCH_RULES.METHOD.rule)) {
      return uriMatched && methodMatched;
    } else if (reqMatchRule == (MATCH_RULES.HOST.rule | MATCH_RULES.URI.rule | MATCH_RULES.METHOD.rule)) {
      return hostMatched && uriMatched && methodMatched;
    } else {
      return false;
    }
  }

  private List<Router> selectRouterCandidates(Category category, String host,
                                              String uri, HttpMethod httpMethod) {
    //候选的List<Router>应该是size最小的，如果当routersByX为null，设置其size为Integer.MAX_VALUE，来满足剪裁逻辑。
    List<Router> routersByHost = category.getRoutersByHosts().get(host);
    List<Router> routersByUri = category.getRoutersByUris().get(uri);
    List<Router> routersByMethod = category.getRoutersByMethods().get(httpMethod.name());
    int routersByHostSize = routersByHost == null ? Integer.MAX_VALUE : routersByHost.size();
    int routersByUriSize = routersByUri == null ? Integer.MAX_VALUE : routersByUri.size();
    int routersByMethodSize = routersByMethod == null ? Integer.MAX_VALUE : routersByMethod.size();
    if (routersByHostSize <= routersByUriSize && routersByHostSize <= routersByMethodSize) {
      return routersByHost;
    } else if (routersByUriSize <= routersByHostSize && routersByUriSize <= routersByMethodSize) {
      return routersByUri;
    } else {
      return routersByMethod;
    }
  }

  private byte searchAndHit(Map<String, String> hits,
                            HttpMethod httpMethod, String host, String uri, boolean isBlocked) {
    byte reqMatchRule = 0;
    PlainIndex plainIndex;
    List<Host> wildcardHosts;
    List<Uri> prefixUris;
    List<Uri> regexUris;
    if (isBlocked) {
      plainIndex = routerBlocker.getPlainIndex();
      wildcardHosts = routerBlocker.getWildcardHosts();
      prefixUris = routerBlocker.getPrefixUris();
      regexUris = routerBlocker.getRegexUris();
    } else {
      plainIndex = routerMatcher.getPlainIndex();
      wildcardHosts = routerMatcher.getWildcardHosts();
      prefixUris = routerMatcher.getPrefixUris();
      regexUris = routerMatcher.getRegexUris();
    }
    //host match
    if (plainIndex != null && plainIndex.getHosts() != null && plainIndex.getHosts().contains(host)) {
      reqMatchRule |= MATCH_RULES.HOST.rule;
    } else {
      for (Host h : wildcardHosts) {
        String regex = h.getRegex();
        if (host.matches(regex)) {
          reqMatchRule |= MATCH_RULES.HOST.rule;
          hits.put("host", h.getValue());
          break;
        }
      }
    }

    //uri match
    if (plainIndex != null && plainIndex.getUris() != null && plainIndex.getUris().contains(uri)) {
      reqMatchRule |= MATCH_RULES.URI.rule;
    } else {
      //先查询prefixUris，再查询regexUris
      for (Uri u : prefixUris) {
        if (uri.contains(u.getValue()) && uri.startsWith(u.getValue())) {
          reqMatchRule |= MATCH_RULES.URI.rule;
          hits.put("uri", u.getValue());
          break;
        }
      }
      for (Uri u : regexUris) {
        String regex = u.getRegexUri();
        if (uri.matches(regex)) {
          reqMatchRule |= MATCH_RULES.URI.rule;
          hits.put("uri", u.getValue());
          break;
        }
      }
    }

    //method match
    if (plainIndex != null) {
      Set<String> methods = plainIndex.getMethods() == null ? new HashSet<>() : plainIndex.getMethods();
      if (methods.contains(httpMethod.name())) {
        reqMatchRule |= MATCH_RULES.METHOD.rule;
        hits.put("method", httpMethod.name());
      }
    }
    log.trace("Request match rule is " + reqMatchRule);
    return reqMatchRule;
  }

  /**
   * 匹配规则
   */
  public enum MATCH_RULES {
    /**
     * 匹配Host
     */
    HOST((byte) 1),
    /**
     * 匹配URI
     */
    URI((byte) 2),
    /**
     * 匹配HTTP方法
     */
    METHOD((byte) 4);

    public final byte rule;

    MATCH_RULES(byte rule) {
      this.rule = rule;
    }

  }

}
