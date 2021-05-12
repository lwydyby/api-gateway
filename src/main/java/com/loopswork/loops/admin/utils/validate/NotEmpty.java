package com.loopswork.loops.admin.utils.validate;

import com.github.houbb.valid.api.annotation.constraint.Constraint;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(NotEmptyConstraint.class)
public @interface NotEmpty {
  /**
   * 提示消息
   *
   * @return 错误提示
   */
  String message() default "";
}
