package com.loopswork.loops.entity;

/**
 * @ClassName: MatchState
 * @Author: Fan Zhang
 * @Date: 2019-04-15 15:30
 */
public enum MatchState {
  MATCHED("Succeed in matching the request. "),
  NO_MATCH("No match for the request."),
  ERROR("Fail to match the request");

  public final String message;

  MatchState(String message) {
    this.message = message;
  }
}
