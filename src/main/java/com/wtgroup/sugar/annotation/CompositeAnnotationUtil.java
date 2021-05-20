package com.wtgroup.sugar.annotation;


import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Proxy;

/**
 * @author dafei
 * @version 0.1
 * @date 2021/1/26 23:18
 */
public class CompositeAnnotationUtil {

    /**
     * 查找组合注解, 内含"组合"语义.
     *
     * @param annotationType   目标注解类型
     * @param annotatedElement 注解的元素, 属性, 方法等. {@link AnnotatedElement}
     * @param <A>              注解泛型
     * @return 不存在时, 返回 null
     */
    public static <A extends Annotation> A getCompositeAnnotation(Class<A> annotationType, AnnotatedElement annotatedElement) {
        return composite(annotationType, annotatedElement, false);
    }

    /**
     * 获取组合注解, 目标注解可以省略.
     * <p>
     * 被查找注解没有, 会反射实例化实例, 这样所以属性值就是默认值.
     *
     * Note: 目标注解没有默认值的属性将输出 null , 故, 请注意 null 值处理.
     *
     * @param annotationType   目标注解类型
     * @param annotatedElement 注解的元素, 属性, 方法等. {@link AnnotatedElement}
     * @param <A>              注解泛型
     * @return NotNull
     */
    public static <A extends Annotation> A getNotNullCompositeAnnotation(Class<A> annotationType, AnnotatedElement annotatedElement) {
        return composite(annotationType, annotatedElement, true);
    }

    // /**
    //  * 实现注解的 "组合" 语义
    //  *
    //  * 灵感来源: {@code org.springframework.core.annotation.AnnotationUtils#synthesizeAnnotation}
    //  */
    // @SuppressWarnings("unchecked")
    // public static <A extends Annotation> A composite(A annotation, AnnotatedElement annotatedElement) {
    //     if (annotation == null) {
    //         return null;
    //     }
    //     if (annotation instanceof CompositedAnnotation) {
    //         return annotation;
    //     }
    //
    //     return (A) composite(annotation.annotationType(), annotatedElement, false);
    // }

    /**
     * 实现注解的 "组合" 语义
     * <p>
     * 灵感来源: {@code org.springframework.core.annotation.AnnotationUtils#synthesizeAnnotation}
     *
     * @param annotationType
     * @param annotatedElement
     * @param allowAbsent      annotatedElement 上没有 annotationType 类型注解时, 是否继续. true: 允许省略目标注解, 返回值 NotNull . false: 不存在时, 返回 null .
     * @param <A>
     * @return 目标注解的代理实例
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A composite(Class<A> annotationType, AnnotatedElement annotatedElement, boolean allowAbsent) {
        A annotation = annotatedElement.getAnnotation(annotationType);
        // 目标注解必须存在时, 为 null 时, 返回
        if (!allowAbsent && annotation == null) {
            return null;
        }

        if (annotation instanceof CompositedAnnotation) {
            return annotation;
        }
        CompositeAnnotationAttributeExtractor attributeExtractor;
        if (annotation == null) {
            attributeExtractor = new CompositeAnnotationAttributeExtractor(annotationType, annotatedElement);
        } else {
            attributeExtractor = new CompositeAnnotationAttributeExtractor(annotation, annotatedElement);
        }
        CompositeAnnotationInvocationHandler handler = new CompositeAnnotationInvocationHandler(attributeExtractor);

        Class<?>[] exposedInterfaces = new Class<?>[]{annotationType, CompositedAnnotation.class};
        return (A) Proxy.newProxyInstance(annotationType.getClassLoader(), exposedInterfaces, handler);
    }


    public static <A extends Annotation> A getUpcastCompositeAnnotation(Class<A> compositedAnnotationType, Class<? extends Annotation> compositerAnnotationType, AnnotatedElement annotatedElement) {
        return compositeUpcast(compositedAnnotationType, compositerAnnotationType, annotatedElement, false);
    }

    public static <A extends Annotation> A getNotNullUpcastCompositeAnnotation(Class<A> compositedAnnotationType, Class<? extends Annotation> compositerAnnotationType, AnnotatedElement annotatedElement) {
        return compositeUpcast(compositedAnnotationType, compositerAnnotationType, annotatedElement, true);
    }

    /**
     * @param compositedAnnotationType "被组合者"注解类型
     * @param compositerAnnotationType "组合者"注解类型
     * @param annotatedElement 目标注解类型
     * @param allowAbsent annotatedElement 上是否可以缺失 "被组合者" 注解. "组合者"注解缺失时, 直接返回检索到的"被组合者"注解(null就返回null).
     * @param <A>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A compositeUpcast(Class<A> compositedAnnotationType, Class<? extends Annotation> compositerAnnotationType, AnnotatedElement annotatedElement, boolean allowAbsent) {
        A annotation = annotatedElement.getAnnotation(compositedAnnotationType);
        // 目标注解必须存在时, 为 null 时, 返回
        if (!allowAbsent && annotation == null) {
            return null;
        }

        Annotation compositerAnnotation = annotatedElement.getAnnotation(compositerAnnotationType);
        // 组合者需要存在, 否则, 此套逻辑没有意义. 直接返回 "被组合者" 自身就可以了
        if (compositerAnnotation == null) {
            return annotation;
        }

        if (annotation instanceof CompositedAnnotation) {
            return annotation;
        }
        CompositeAnnotationAttributeExtractor attributeExtractor;
        if (annotation == null) {
            attributeExtractor = new UpcastCompositeAnnotationAttributeExtractor(compositerAnnotation, compositedAnnotationType, annotatedElement);
        } else {
            attributeExtractor = new UpcastCompositeAnnotationAttributeExtractor(compositerAnnotation, annotation, annotatedElement);
        }
        CompositeAnnotationInvocationHandler handler = new CompositeAnnotationInvocationHandler(attributeExtractor);

        Class<?>[] exposedInterfaces = new Class<?>[]{compositedAnnotationType, CompositedAnnotation.class};
        return (A) Proxy.newProxyInstance(compositedAnnotationType.getClassLoader(), exposedInterfaces, handler);
    }
}
