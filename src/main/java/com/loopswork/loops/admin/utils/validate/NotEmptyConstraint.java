package com.loopswork.loops.admin.utils.validate;

import com.github.houbb.valid.api.api.constraint.IConstraint;
import com.github.houbb.valid.core.api.constraint.annotation.AbstractAnnotationConstraint;

/**
 * @author liwei
 * @description 非空检查
 * @date 2019-12-09 17:22
 */
public class NotEmptyConstraint extends AbstractAnnotationConstraint<NotEmpty> {

  @Override
  protected IConstraint buildConstraint(NotEmpty notEmpty) {
    return new GatewayConstraints();
  }
}
