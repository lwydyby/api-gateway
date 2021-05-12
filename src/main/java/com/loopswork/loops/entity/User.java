package com.loopswork.loops.entity;

import lombok.Data;

/**
 * @author liwei
 * @description 管理用户
 * @date 2019-12-04 14:55
 */
@Data
public class User {
  private String username;
  private String password;
  private String secretKey;
}
