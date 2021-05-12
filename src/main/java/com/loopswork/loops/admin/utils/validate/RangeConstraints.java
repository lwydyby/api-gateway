package com.loopswork.loops.admin.utils.validate;


import com.github.houbb.valid.api.api.constraint.IConstraintContext;
import com.github.houbb.valid.core.api.constraint.AbstractStrictConstraint;

/**
 * @author liwei
 * @description 范围判断
 * @date 2019-12-09 17:25
 */
public class RangeConstraints extends AbstractStrictConstraint {
  private Range range;

  public RangeConstraints(Range range) {
    this.range = range;
  }

  @Override
  protected boolean pass(IConstraintContext iConstraintContext, Object o) {
    int num = (int) o;
    return num >= range.min() && num <= range.max();
  }


}
