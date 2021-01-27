package com.wtgroup.sugar.annotation;


import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 复制修改自: {@link org.springframework.core.annotation.AnnotationUtils}
 * @author 60906
 */
public class AnnotationUtil {

    private static final Map<Class<? extends Annotation>, List<Method>> attributeMethodsCache =
            new ConcurrentReferenceHashMap<>(256, ConcurrentReferenceHashMap.ReferenceType.WEAK); // Soft 和 Weak 可选

    public static List<Method> getAttributeMethods(Class<? extends Annotation> annotationType) {
        List<Method> methods = attributeMethodsCache.get(annotationType);
        if (methods != null) {
            return methods;
        }

        methods = new ArrayList<>();
        for (Method method : annotationType.getDeclaredMethods()) {
            if (isAttributeMethod(method)) {
                ReflectionUtils.makeAccessible(method);
                methods.add(method);
            }
        }

        attributeMethodsCache.put(annotationType, methods);
        return methods;
    }

    /**
     * Determine if the supplied {@code method} is an annotation attribute method.
     * @param method the method to check
     * @return {@code true} if the method is an attribute method
     * @since 4.2
     */
    public static boolean isAttributeMethod(@Nullable Method method) {
        return (method != null && method.getParameterCount() == 0 && method.getReturnType() != void.class);
    }

    /**
     * Determine if the supplied method is an "annotationType" method.
     * @return {@code true} if the method is an "annotationType" method
     * @since 4.2
     * @see Annotation#annotationType()
     */
    public static boolean isAnnotationTypeMethod(@Nullable Method method) {
        return (method != null && method.getName().equals("annotationType") && method.getParameterCount() == 0);
    }
}
