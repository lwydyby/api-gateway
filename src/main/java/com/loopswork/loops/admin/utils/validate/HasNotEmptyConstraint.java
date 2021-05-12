package com.loopswork.loops.admin.utils.validate;

import com.github.houbb.heaven.util.lang.ObjectUtil;
import com.github.houbb.valid.api.api.constraint.IConstraintContext;
import com.github.houbb.valid.core.api.constraint.AbstractStrictConstraint;

import java.util.Set;

/**
 * @author liwei
 * @description 不能同时为空判断
 * @date 2019-12-16 18:47
 */
public class HasNotEmptyConstraint extends AbstractStrictConstraint {

  private final String[] otherFieldNames;

  HasNotEmptyConstraint(String[] otherFieldNames) {
    this.otherFieldNames = otherFieldNames;
  }

  protected boolean pass(IConstraintContext context, Object value) {
    if (ObjectUtil.isNotNull(value)) {
      return true;
    } else {
      String[] otherFields = this.otherFieldNames;
      int length = otherFields.length;
      for (int index = 0; index < length; ++index) {
        String fieldName = otherFields[index];
        Object fieldValue = context.getFieldValue(fieldName);
        if (isNotEmpty(fieldValue)) {
          return true;
        }
      }

      return false;
    }
  }

  private boolean isNotEmpty(Object o) {
    if (o instanceof Set) {
      Set set = (Set) o;
      return set.size() != 0;
    }
    return o != null;
  }
}
