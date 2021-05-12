package com.loopswork.loops.entity;

import lombok.Data;
import lombok.ToString;

/**
 * @ClassName: MatchResult
 * @Author: Fan Zhang
 * @Date: 2019-04-15 15:28
 */
@Data
@ToString
public class MatchResult {
  private MatchRouter matchRouter;
  private MatchState matchState;

  public MatchResult(MatchRouter matchRouter, MatchState matchState) {
    this.matchRouter = matchRouter;
    this.matchState = matchState;
  }

  public MatchResult() {

  }
}
