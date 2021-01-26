package com.wtgroup.sugar.annotation;

import org.springframework.core.annotation.AnnotationConfigurationException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dafei
 * @version 0.1
 * @date 2021/1/26 23:35
 */
public class CompositeAnnotationInvocationHandler implements InvocationHandler {

    private CompositeAnnotationAttributeExtractor attributeExtractor;

    private final Map<String, Object> valueCache = new ConcurrentHashMap<>(8);

    public CompositeAnnotationInvocationHandler(CompositeAnnotationAttributeExtractor attributeExtractor) {
        this.attributeExtractor = attributeExtractor;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (ReflectionUtils.isEqualsMethod(method)) {
            return method.invoke(proxy, args);
        }
        if (ReflectionUtils.isHashCodeMethod(method)) {
            return method.invoke(proxy, args);
        }
        if (ReflectionUtils.isToStringMethod(method)) {
            return method.invoke(proxy, args);
        }
        // if (AnnotationUtils.isAnnotationTypeMethod(method)) {
        //     return method.invoke(proxy, args);
        // }
        // if (!AnnotationUtils.isAttributeMethod(method)) {
        //     throw new AnnotationConfigurationException(String.format(
        //             "Method [%s] is unsupported for synthesized annotation type [%s]", method, annotationType()));
        // }

        // 取注解属性方法的值
        return getAttributeValue(method);
    }


    private Object getAttributeValue(Method attributeMethod) {
        Object value = this.valueCache.get(attributeMethod.getName());
        if (value == null) {
            value = this.attributeExtractor.getAttributeValue(attributeMethod);
            if (value != null) {
                this.valueCache.put(attributeMethod.getName(), value);
            }
        }

        return value;
    }
}
