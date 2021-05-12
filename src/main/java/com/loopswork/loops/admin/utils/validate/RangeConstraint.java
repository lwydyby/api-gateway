package com.loopswork.loops.admin.utils.validate;

import com.github.houbb.valid.api.api.constraint.IConstraint;
import com.github.houbb.valid.core.api.constraint.annotation.AbstractAnnotationConstraint;

/**
 * @author liwei
 * @description 范围检查
 * @date 2019-12-09 17:22
 */
public class RangeConstraint extends AbstractAnnotationConstraint<Range> {

  @Override
  protected IConstraint buildConstraint(Range range) {
    return new RangeConstraints(range);
  }
}
