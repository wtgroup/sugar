package com.wtgroup.sugar.annotation;


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
     * 实现注解的 "组合" 语义
     *
     * 灵感来源: {@code org.springframework.core.annotation.AnnotationUtils#synthesizeAnnotation}
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A composite(A annotation, AnnotatedElement annotatedElement) {
        if (annotation instanceof CompositedAnnotation) {
            return annotation;
        }

        CompositeAnnotationAttributeExtractor attributeExtractor = new CompositeAnnotationAttributeExtractor(annotation, annotatedElement);
        CompositeAnnotationInvocationHandler handler = new CompositeAnnotationInvocationHandler(attributeExtractor);

        Class<?>[] exposedInterfaces = new Class<?>[] {annotation.annotationType(), CompositedAnnotation.class};
        return (A) Proxy.newProxyInstance(annotation.getClass().getClassLoader(), exposedInterfaces, handler);
    }

    /**查找组合注解, 内含"组合"语义.
     * @param annotationType 目标注解类型
     * @param annotatedElement 注解的元素, 属性, 方法等. {@link AnnotatedElement}
     * @param <A> 注解泛型
     * @return 不存在时, 返回 null
     */
    public static <A extends Annotation> A getCompositeAnnotation(Class<A> annotationType, AnnotatedElement annotatedElement) {
        A annotation = annotatedElement.getAnnotation(annotationType);
        if (annotation == null) {
            return null;
        }

        return composite(annotation, annotatedElement);
    }
}
