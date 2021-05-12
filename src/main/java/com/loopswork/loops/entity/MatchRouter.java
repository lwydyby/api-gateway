package com.loopswork.loops.entity;


import lombok.Data;

/**
 * @ClassName: MatchRouter
 * @Author: Fan Zhang
 * @Date: 2019-03-18 16:34
 */
@Data
public class MatchRouter {
  private Router router;
  private Server server;
  private Matches matches;
}
