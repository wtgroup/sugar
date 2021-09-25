package com.wtgroup.sugar.annotation;

import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Up cast 组合注解属性抽取器
 *
 * 抽取属性值时, "被组合者"当作主, "组合者"当作从.
 *
 * 组合者必须存在, 被组合者, 可以缺失. 组合者不存在, 被组合者存在, 没有必要用此工具框架获取, 应该直接获取.
 * @author 60906
 * @date 2021/5/20 15:15
 */
public class UpcastCompositeAnnotationAttributeExtractor extends CompositeAnnotationAttributeExtractor {


    // /**composited*/
    // @Getter
    // private Annotation annotation;
    // @Getter
    // final AnnotatedElement annotatedElement;
    // /**composited 的 annotationType*/
    // final Class<? extends Annotation> annotationType;
    /**
     * 组合者注解
     */
    private final Annotation compositer;
    /**
     * 被组合者方法 -> 组合者方法. 传入的是被组合者方法, 如果没有设置值, 就看有无被组合者方法的值.
     */
    private final Map<String, Method> compositerMethods = new HashMap<>();


    public UpcastCompositeAnnotationAttributeExtractor(Annotation compositer, Annotation composited, AnnotatedElement annotatedElement) {
        super(composited, annotatedElement);
        this.compositer = compositer;

        // 组合者所有方法, 检索 @Composite 指向当前 被组合者 的
        fillCompositerMethods(compositer);
    }

    public UpcastCompositeAnnotationAttributeExtractor(Annotation compositer, Class<? extends Annotation> compositedAnnotationType, AnnotatedElement annotatedElement) {
        super(compositedAnnotationType, annotatedElement);
        this.compositer = compositer;

        // 组合者所有方法, 检索 @Composite 指向当前 被组合者 的
        fillCompositerMethods(compositer);
    }

    /**
     * 组合者所有方法, 检索 @Composite 指向当前 被组合者 的
     * @param compositer 组合者注解
     */
    private void fillCompositerMethods(Annotation compositer) {
        Method[] cdms = ReflectionUtils.getAllDeclaredMethods(compositer.annotationType());
        for (Method cdm : cdms) {
            Composite compAnn = cdm.getAnnotation(Composite.class);
            if (compAnn != null && compAnn.annotation() == this.annotationType) {
                String compositeAttributeMethodName = compAnn.value();
                if(compositeAttributeMethodName.isEmpty()) {
                    compositeAttributeMethodName = cdm.getName();
                }
                this.compositerMethods.put(compositeAttributeMethodName, cdm);
            }
        }
    }

    /**
     * 传入的是 被组合者 的属性方法,
     * 不管有没有设置值, 都需要看有没有被 组合者 "覆写".
     * @param attributeMethod
     * @return
     */
    @Override
    protected Object getAttributeValue(Method attributeMethod) {
        Object attributeValue = null;
        // 组合者的值优先使用
        Method compositerMethod = compositerMethods.get(attributeMethod.getName());
        if (compositerMethod != null) {
            Object compositeAttributeValue = getRawAttributeValue(compositerMethod, this.compositer);
            if (compositeAttributeValue != null && !compositeAttributeValue.equals(compositerMethod.getDefaultValue())) {
                attributeValue = compositeAttributeValue;
            }
        }

        // 没有被"覆写", 才考虑 被组合者 自己的
        if (attributeValue == null) {
            Object defaultValue = attributeMethod.getDefaultValue();
            // 允许 annotation 为null, null 时, 所有属性取默认值.
            if (this.annotation != null) {
                attributeValue = getRawAttributeValue(attributeMethod, this.annotation);
            } else {
                attributeValue = defaultValue;
            }
        }

        return attributeValue;
    }

}
