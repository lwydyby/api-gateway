package com.loopswork.loops.admin.utils.validate;

import com.github.houbb.valid.api.annotation.constraint.Constraint;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(AtHasNotEmptyConstraint.class)
public @interface HasNotEmpty {
  String[] value() default {};

  String message() default "";

  Class[] group() default {};
}
