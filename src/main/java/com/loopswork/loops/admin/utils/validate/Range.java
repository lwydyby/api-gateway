package com.loopswork.loops.admin.utils.validate;

import com.github.houbb.valid.api.annotation.constraint.Constraint;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(RangeConstraint.class)
public @interface Range {

  /**
   * 提示消息
   *
   * @return 错误提示
   */
  String message() default "";

  int min() default 0;

  int max() default Integer.MAX_VALUE;
}
