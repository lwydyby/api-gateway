package com.loopswork.loops.admin.utils;


import com.loopswork.loops.entity.SimpleCode;
import com.loopswork.loops.exception.SimpleException;
import com.loopswork.loops.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lw
 */
public class IpUtils {

  private static Pattern p = Pattern.compile("(?<=//|)([\\w-]+\\.)+\\w+(<=:\\d*)?");

  /**
   * 获取url中的host
   *
   * @param url
   * @return
   */
  public static String getHost(String url) {
    if (url == null || url.trim().equals("")) {
      return "";
    }
    String host = "";
    Matcher matcher = p.matcher(url);
    if (matcher.find()) {
      host = matcher.group();
    }
    if (StringUtils.isEmpty(host)) {
      throw new SimpleException(SimpleCode.SERVER_HOST_ERROR);
    }
    return host;
  }


  public static boolean isIpV4(String ip) {
    String[] groups = ip.split("\\.");
    try {
      return Arrays.stream(groups).filter(s -> s.length() >= 1).map(Integer::parseInt).filter(i -> (i >= 0 && i <= 255)).count() == 4;
    } catch (Exception e) {
      return false;
    }
  }


  public static boolean isInclusiveHost(String ip) {
    return true;
  }

  public static boolean checkIp(String ip) {
    return isIpV4(ip) || isInclusiveHost(ip);
  }

  public static boolean checkIpList(List<String> ips) {
    for (String ip : ips) {
      if (!IpUtils.checkIp(ip)) {
        return false;
      }
    }
    return true;
  }

}
