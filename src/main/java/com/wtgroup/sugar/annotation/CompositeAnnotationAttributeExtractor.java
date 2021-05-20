package com.wtgroup.sugar.annotation;



import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * @author dafei
 * @version 0.1
 * @date 2021/1/26 23:57
 */
public class CompositeAnnotationAttributeExtractor {

    @Getter
    protected Annotation annotation;
    @Getter
    protected final AnnotatedElement annotatedElement;
    protected final Class<? extends Annotation> annotationType;

    public CompositeAnnotationAttributeExtractor(Annotation annotation, AnnotatedElement annotatedElement) {
        this.annotation = annotation;
        this.annotatedElement = annotatedElement;
        this.annotationType = annotation.annotationType();
    }

    public CompositeAnnotationAttributeExtractor(Class<? extends Annotation> annotationType, AnnotatedElement annotatedElement) {
        this.annotation = null; // allowAbsent
        this.annotatedElement = annotatedElement;
        this.annotationType = annotationType;
    }

    /**获取属性方法值
     *
     * annotatedElement + annotation @Composite 属性方法,
     * B.a 不可用 --> @Composite --> A.a
     * @param attributeMethod
     * @return
     */
    protected Object getAttributeValue(Method attributeMethod) {
        // 自身的本属性方法值
        Object attributeValue = null;
        Object defaultValue = attributeMethod.getDefaultValue();
        // 允许 annotation 为null, null 时, 所有属性取默认值.
        if (this.annotation != null) {
            attributeValue = getRawAttributeValue(attributeMethod, this.annotation);
        } else {
            attributeValue = defaultValue;
        }

        if (attributeValue == null || attributeValue.equals(defaultValue)) {
            // 进一步, 看组合的注解的属性(@Composite指向的注解的属性)
            Composite composite = attributeMethod.getAnnotation(Composite.class);
            if (composite != null) {
                Annotation compositeAnnotation = this.annotatedElement.getAnnotation(composite.annotation());
                if (compositeAnnotation != null) {
                    String compositeAttributeMethodName = composite.value();
                    if(compositeAttributeMethodName.isEmpty()) {
                        compositeAttributeMethodName = attributeMethod.getName();
                    }
                    Method compositeAttributeMethod = ReflectionUtils.findMethod(composite.annotation(), compositeAttributeMethodName);
                    Object compositeAttributeValue = compositeAttributeMethod != null ? getRawAttributeValue(compositeAttributeMethod, compositeAnnotation) : null;
                    if (compositeAttributeValue != null && !compositeAttributeValue.equals(compositeAttributeMethod.getDefaultValue())) {
                        attributeValue = compositeAttributeValue;
                    }
                }
            }

        }

        return attributeValue;
    }


    // protected Object getRawAttributeValue(Method attributeMethod) {
    //     return getRawAttributeValue(attributeMethod, getSource());
    // }

    // protected Object getRawAttributeValue(String attributeName) {
    //     Method attributeMethod = ReflectionUtils.findMethod(getAnnotationType(), attributeName);
    //     return (attributeMethod != null ? getRawAttributeValue(attributeMethod) : null);
    // }


    protected Class<? extends Annotation> getAnnotationType() {
        return this.annotationType;
    }

    @Override
    public String toString() {
        return "CompositeAnnotationAttributeExtractor{" +
                "annotation=" + annotation +
                ", annotatedElement=" + annotatedElement +
                '}';
    }

    protected static Object getRawAttributeValue(Method attributeMethod, Object source) {
        ReflectionUtils.makeAccessible(attributeMethod);
        return ReflectionUtils.invokeMethod(attributeMethod, source);
    }
}
