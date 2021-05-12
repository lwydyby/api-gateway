package com.loopswork.loops.util;


import com.loopswork.loops.entity.*;
import com.loopswork.loops.exception.LoopsException;
import com.loopswork.loops.manager.RouterManager;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 路由工具类 RouterUtil
 *
 * @author Fan Zhang
 */

public class RouterUtil {
  protected static final Logger log = LoggerFactory.getLogger(RouterUtil.class);
  private static Pattern uriPrefixPattern = Pattern.compile("^[a-zA-Z0-9.\\-_~/%]*$");

  /**
   * 构造Router列表
   *
   * @param routes 路由列表
   * @return Router object list.
   */
  public static List<Router> getRouterList(List<Route> routes) {
    log.debug("Building routers list using routes and servers.");
    List<Router> routers = new ArrayList<>();
    if (routes == null) {
      log.warn("No routes , stop building routers list.");
      return routers;
    }
    for (Route route : routes) {
      if (route == null || !route.isEnable()) {
        log.debug(route + " is disabled, skip it in building routers.");
        continue;
      }
      Server server = route.getServer();
      if (server == null) {
        log.warn("Server " + route.getServerId() + " not found.");
        continue;
      }
      if (!server.getEnable()) {
        log.debug(server + " is disabled, skip it in building routers.");
        continue;
      }

      Router router = new Router();
      Protocol protocol = server.getProtocols();
      router.setProtocol(protocol);
      Set<String> hosts = route.getHosts() == null ? new HashSet<>() : route.getHosts();
      String[] headers = hosts.toArray(new String[]{});
      router.setHeaders(headers);
      router.setRoute(route);
      router.setServer(server);
      routers.add(router);
    }

    Comparator<Router> comparator = (o1, o2) -> {
      Route r1 = o1.getRoute();
      Route r2 = o2.getRoute();
      if (r1.getPriority() == r2.getPriority()) {
        if (r1.getCreatedAt().before(r2.getCreatedAt())) {
          return -1;
        } else {
          return 1;
        }
      } else {
        if (r1.getPriority() > r2.getPriority()) {
          return -1;
        } else {
          return 1;
        }
      }
    };
    routers.sort(comparator);
    log.trace("Done build routers list.");
    log.debug("Now routers are " + routers);
    return routers;
  }

  private static Server getServerById(String serverId, List<Server> servers) {
    Server server = null;
    for (Server s : servers) {
      if (s.getId().equals(serverId)) {
        server = s;
        break;
      }
    }
    return server;
  }

  public static void organizeRouter(Router router) {
    Route route = router.getRoute();
    Server server = router.getServer();
    String[] headers = router.getHeaders();
    Set<String> paths = route.getPaths();
    Set<HttpMethod> methods = route.getMethods();
    Protocol protocol = server.getProtocols();
    byte matchRule = 0;
    int matchWeight = 0;
    router.setStripUri(route.isStripPath());
    if (headers == null && paths == null && methods == null && protocol == null) {
      String msg = "Cannot organize router of route " + route.getId() + " and server " + server.getId();
      log.warn(msg);
      throw new LoopsException(msg);
    }
    if (headers != null) {
      if (headers.length > 0) {
        matchRule |= RouterManager.MATCH_RULES.HOST.rule;
        matchWeight += 1;
      }
      ArrayList<Host> hosts = new ArrayList<>();
      for (String host : headers) {
        Host h = new Host();
        h.setValue(host);
        if (host.contains("*")) {
          h.setWildcard(true);
          String regex = host.replace(".", "\\.")
            .replace("*", ".+")
            .concat("$");
          h.setRegex(regex);
        } else {
          h.setWildcard(false);
        }
        hosts.add(h);
      }
      router.setHosts(hosts.toArray(new Host[]{}));
    } else {
      router.setHosts(new Host[]{});
    }
    if (paths != null) {
      if (paths.size() > 0) {
        matchRule |= RouterManager.MATCH_RULES.URI.rule;
        matchWeight += 1;
      }
      ArrayList<Uri> uris = new ArrayList<>();
      for (String uri : paths) {
        Uri u = new Uri();
        if (uriPrefixPattern.matcher(uri).matches()) {
          u.setPrefix(true);
          u.setValue(uri);
        } else {
          u.setRegex(true);
          u.setValue(uri);
          u.setRegexUri(uri);
          //fixme 下面的两个字段，目前看到应该是没有用处的，酌情拿掉
          u.setStripRegex(uri.concat("(?<uri_postfix>.*)"));
          u.setHasCaptures(hasCaptures(uri));
        }
        uris.add(u);
      }
      router.setUris(uris.toArray(new Uri[]{}));
    } else {
      router.setUris(new Uri[]{});
    }
    if (methods != null) {
      if (methods.size() > 0) {
        matchRule |= RouterManager.MATCH_RULES.METHOD.rule;
        matchWeight += 1;
      }
      Set<String> methodSet = new HashSet<>();
      for (HttpMethod method : methods) {
        methodSet.add(method.name());
      }
      router.setMethods(methodSet);
    } else {
      router.setMethods(new HashSet<String>() {
      });
    }
    router.setMatchRule(matchRule);
    router.setMatchWeight(matchWeight);
  }

  private static boolean hasCaptures(String uri) {
    //fixme 需要理解什么是capture group及其应用场景，根据实际情况来判断是否有必要做该处理。
    return false;
  }

  public static void categoryRouter(Router router, Map<Byte, Category> categories) {
    //fill in categories
    Category category = categories.get(router.getMatchRule());
    if (category == null) {
      category = new Category();
      category.setMatchWeight(router.getMatchWeight());
      categories.put(router.getMatchRule(), category);
    }

    ArrayList<Router> allRouters = category.getAllRouters();
    if (allRouters == null) {
      allRouters = new ArrayList<>();
      category.setAllRouters(allRouters);
    }
    allRouters.add(router);

    Host[] hosts = router.getHosts();
    Map<String, ArrayList<Router>> routersByHosts = category.getRoutersByHosts();
    if (routersByHosts == null) {
      routersByHosts = new HashMap<>();
      category.setRoutersByHosts(routersByHosts);
    }
    for (Host h : hosts) {
      String host = h.getValue();
      ArrayList<Router> routersList = routersByHosts.computeIfAbsent(host, k -> new ArrayList<>());
      routersList.add(router);
    }

    Uri[] uris = router.getUris();
    Map<String, ArrayList<Router>> routersByUris = category.getRoutersByUris();
    if (routersByUris == null) {
      routersByUris = new HashMap<>();
      category.setRoutersByUris(routersByUris);
    }
    for (Uri u : uris) {
      String uri = u.getValue();
      ArrayList<Router> routersList = routersByUris.computeIfAbsent(uri, k -> new ArrayList<>());
      routersList.add(router);
    }

    Set<String> methods = router.getMethods();
    Map<String, ArrayList<Router>> routersByMethods = category.getRoutersByMethods();
    if (routersByMethods == null) {
      routersByMethods = new HashMap<>();
      category.setRoutersByMethods(routersByMethods);
    }
    for (String m : methods) {
      ArrayList<Router> routersList = routersByMethods.computeIfAbsent(m, k -> new ArrayList<>());
      routersList.add(router);
    }
  }

  public static void indexRouter(Router router, PlainIndex plainIndex, List<Host> wildcardHosts,
                                 List<Uri> prefixUris, List<Uri> regexUris) {
    //填充plainIndex, wildcardHosts, prefixUris和regexUris
    Host[] hosts = router.getHosts();
    Set<String> hostSet = plainIndex.getHosts();
    if (hostSet == null) {
      hostSet = new HashSet<>();
      plainIndex.setHosts(hostSet);
    }
    for (Host h : hosts) {
      if (h.isWildcard()) {
        wildcardHosts.add(h);
      } else {
        hostSet.add(h.getValue());
      }
    }

    Uri[] uris = router.getUris();
    Set<String> uriSet = plainIndex.getUris();
    if (uriSet == null) {
      uriSet = new HashSet<>();
      plainIndex.setUris(uriSet);
    }
    for (Uri u : uris) {
      if (u.isRegex()) {
        if (u.getRegexUri().contains("*")) {
          u.setRegexUri(u.getRegexUri().replace("*", ".*?"));
        }
        regexUris.add(u);
      } else if (u.isPrefix()) {
        prefixUris.add(u);
        uriSet.add(u.getValue());
      }
    }

    Set<String> methodSet = plainIndex.getMethods();
    if (methodSet == null) {
      methodSet = new HashSet<>();
      plainIndex.setMethods(methodSet);
    }
    methodSet.addAll(router.getMethods());
  }

  public static void buildCategoriesWeightsAndLookup(Map<Byte, Category> categories,
                                                     List<CategoriesWeight> categoriesWeights,
                                                     Map<Byte, Integer> categoriesLookup) {
    //构造categoriesWeight
    for (Map.Entry<Byte, Category> entry : categories.entrySet()) {
      CategoriesWeight cw = new CategoriesWeight();
      cw.setCategoryBit(entry.getKey());
      cw.setCategoryWeight(entry.getValue().getMatchWeight());
      categoriesWeights.add(cw);
    }
    Comparator<CategoriesWeight> comparator = (o1, o2) -> {
      if (o1.getCategoryWeight() != o2.getCategoryWeight()) {
        return o1.getCategoryWeight() > o2.getCategoryWeight() ? -1 : 1;
      } else {
        return Byte.compare(o2.getCategoryBit(), o1.getCategoryBit());
      }
    };
    categoriesWeights.sort(comparator);

    for (int i = 0; i < categoriesWeights.size(); i++) {
      categoriesLookup.put(categoriesWeights.get(i).getCategoryBit(), i);
    }
  }
}
