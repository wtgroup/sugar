package com.wtgroup.sugar.annotation;

import java.lang.annotation.*;

/**
 * 组合注解
 *
 * 有点类似 {@code org.springframework.core.annotation.AliasFor} .
 * 但使用场景刚好相反. 其间差别, 需要耐心品味. 需要理解 Spring 的 AliasFor 机制.
 *
 * 假设 `@A` 是别人的注解, 无法改动, 且不能作为元注解. 但你需要在 `@A` 基础上增加一些属性, 增强功能.
 * 但很多属性想沿用 `A` 的. 比如 `B.a` 等价 `A.a` , 在 `B.a` 有效时, 用 `B.a`, 否则看`A.a`是否可用.
 * 就好像 `B` 组装|继承了 `A.a`. 此外, `B` 还可以有其他自己特有的属性, 如 `B.b`.
 * <pre>
 *          know
 *   A a <------+ API User
 *     ^            +
 * '@AliasFor'      |
 *     |            | maybe unknown
 *     +            |
 *   B a  <---------+
 *
 * </pre>
 *
 * <pre>
 *    A a <-----------+
 *      ^             |
 * '@Composite'  maybe|unknown
 *      |             |
 *      +    know     +
 *    B a <------+ API User
 * </pre>
 *
 * @author dafei
 * @date 2021年1月26日22:41:31
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Composite {

    /**
     * 组合的注解属性名.
     * 默认和当前属性方法名同名.
     */
    String value() default "";

    /**
     * 表示当前注解的该属性组合来的属性所在的注解类型.
     */
    Class<? extends Annotation> annotation();
}
