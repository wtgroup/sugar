package com.wtgroup.sugar.reflect;

import cn.hutool.core.lang.Assert;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ==> v1.0 2020年9月29日
 * 引进工程
 * @author dafei
 * @version 0.1
 * @date 2020/4/23 16:52
 */
public class FieldUtil {


    /**获取所有子类, 包括所有超类.
     * 超类的字段依次追加在后面.
     * @param clazz
     * @param includeSuper 是否包含超类的字段. 追加到后面.
     * @param includeStatic 是否排除静态字段
     * @return
     */
    public static Field[] getFields(Class<?> clazz, boolean includeSuper, boolean includeStatic) {

        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (!includeStatic && Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                fieldList.add(field);
            }
            clazz = includeSuper ? clazz.getSuperclass() : null;
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }

    public static Field[] getFields(Object object, boolean includeSuper, boolean includeStatic) {
        Assert.notNull(object);
        return getFields(object.getClass(), includeSuper, includeStatic);
    }

    /**
     * field name --> field
     * 自身和超类的字段名重复时, 优先使用自身的.
     */
    public static Map<String, Field> getFieldMap(Class<?> clz, boolean includeSuper, boolean includeStatic) {

        Field[] allFields = getFields(clz, includeSuper, includeStatic);
        Map<String, Field> map = new LinkedHashMap<>();
        // 自己的替换超类的 (父类的在尾部追加的)
        for (Field f : allFields) {
            if (!map.containsKey(f.getName())) {
                map.put(f.getName(), f);
            }
        }

        return map;
    }


    /**
     * @param field
     * @param obj
     * @return
     * @author dafei
     * @date 2020-4-10
     */
    public static Object readField(Object obj, Field field) {
        Object v;
        try {
            field.setAccessible(true);
            v = field.get(obj);
            field.setAccessible(false);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return v;
    }


    public static Object readField(Object obj, String fieldName) {
        if (StringUtils.isBlank(fieldName)) {
            return null;
        }

        try {
            Object o = FieldUtils.readField(obj, fieldName, true);
            return o;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeField(Object obj, String fieldName, Object val) {
        try {
            FieldUtils.writeDeclaredField(obj, fieldName, val, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeField(Object obj, Field field, Object val) {
        try {
            FieldUtils.writeField(field, obj, val, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
