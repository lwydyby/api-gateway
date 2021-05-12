package com.loopswork.loops.admin.utils.validate;

import com.github.houbb.valid.api.api.constraint.IConstraint;
import com.github.houbb.valid.core.api.constraint.annotation.AbstractAnnotationConstraint;

/**
 * @author liwei
 * @description 不能同时为空判断
 * @date 2019-12-16 18:48
 */
public class AtHasNotEmptyConstraint extends AbstractAnnotationConstraint<HasNotEmpty> {
  public AtHasNotEmptyConstraint() {
  }

  protected IConstraint buildConstraint(HasNotEmpty annotation) {
    return new HasNotEmptyConstraint(annotation.value());
  }
}
