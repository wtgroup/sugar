package com.wtgroup.sugar.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface C {
    @Composite(annotation = A.class)
    String a() default "B.a";
    @Composite(annotation = B.class)
    String b();

    int c();
}
