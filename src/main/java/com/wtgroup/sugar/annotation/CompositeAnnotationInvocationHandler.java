package com.wtgroup.sugar.annotation;


import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dafei
 * @version 0.1
 * @date 2021/1/26 23:35
 */
public class CompositeAnnotationInvocationHandler implements InvocationHandler {

    private final CompositeAnnotationAttributeExtractor attributeExtractor;

    private final Map<String, Object> valueCache = new ConcurrentHashMap<>(8);

    public CompositeAnnotationInvocationHandler(CompositeAnnotationAttributeExtractor attributeExtractor) {
        this.attributeExtractor = attributeExtractor;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Proxy 对象本身无法调用hashCode、toString、equals方法
        if (ReflectionUtils.isEqualsMethod(method)) {
            return annotationEquals(args[0]);
        }
        // if (ReflectionUtils.isHashCodeMethod(method)) {
        //     return annotationHashCode();
        // }
        if (ReflectionUtils.isToStringMethod(method)) {
            // return "Proxy of " + this.attributeExtractor.getAnnotationType() + " on " + this.attributeExtractor.getAnnotatedElement();
            return annotationToString();
        }
        if (AnnotationUtil.isAnnotationTypeMethod(method)) {
            return annotationType();
        }
        if (!AnnotationUtil.isAttributeMethod(method)) {
            throw new UnsupportedOperationException(String.format(
                    "Method [%s] is unsupported for composited annotation type [%s]", method, annotationType()));
        }

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

    private Class<? extends Annotation> annotationType() {
        Class<? extends Annotation> annotationType = this.attributeExtractor.getAnnotationType();
        return annotationType;
    }

    /**
     * See {@link Annotation#equals(Object)} for a definition of the required algorithm.
     * @param other the other object to compare against
     */
    private boolean annotationEquals(Object other) {
        if (this == other) {
            return true;
        }
        Class<? extends Annotation> annotationType = this.attributeExtractor.getAnnotationType();
        if (!annotationType.isInstance(other)) {
            return false;
        }

        for (Method attributeMethod : AnnotationUtil.getAttributeMethods(annotationType)) {
            Object thisValue = getAttributeValue(attributeMethod);
            Object otherValue = ReflectionUtils.invokeMethod(attributeMethod, other);
            if (!ObjectUtils.nullSafeEquals(thisValue, otherValue)) {
                return false;
            }
        }

        return true;
    }

    /**
     * See {@link Annotation#toString()} for guidelines on the recommended format.
     */
    private String annotationToString() {
        StringBuilder sb = new StringBuilder("@").append(annotationType().getName()).append("(");

        Iterator<Method> iterator = AnnotationUtil.getAttributeMethods(annotationType()).iterator();
        while (iterator.hasNext()) {
            Method attributeMethod = iterator.next();
            sb.append(attributeMethod.getName());
            sb.append('=');
            sb.append(attributeValueToString(getAttributeValue(attributeMethod)));
            sb.append(iterator.hasNext() ? ", " : "");
        }

        return sb.append(")").toString();
    }

    private String attributeValueToString(Object value) {
        if (value instanceof Object[]) {
            return "[" + StringUtils.arrayToDelimitedString((Object[]) value, ", ") + "]";
        }
        return String.valueOf(value);
    }
}
