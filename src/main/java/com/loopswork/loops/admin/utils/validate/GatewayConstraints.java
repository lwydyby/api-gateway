package com.loopswork.loops.admin.utils.validate;


import com.github.houbb.valid.api.api.constraint.IConstraintContext;
import com.github.houbb.valid.core.api.constraint.AbstractStrictConstraint;
import com.loopswork.loops.util.StringUtils;

/**
 * @author liwei
 * @description 网关检查
 * @date 2019-12-09 17:25
 */
public class GatewayConstraints extends AbstractStrictConstraint {

  @Override
  protected boolean pass(IConstraintContext iConstraintContext, Object o) {
    if (o instanceof String) {
      return !StringUtils.isEmpty((String) o);
    }
    return o != null;
  }

}
