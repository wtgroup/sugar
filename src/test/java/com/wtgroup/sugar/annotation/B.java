package com.wtgroup.sugar.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface B {

    // @Composite(value = "a", annotation = A.class) // 或简写为:
    @Composite(annotation = A.class)
    String a() default "B.a";

    String b();
}
