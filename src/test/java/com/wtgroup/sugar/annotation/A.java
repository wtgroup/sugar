package com.wtgroup.sugar.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface A {

    String a() default "A.a";
}
