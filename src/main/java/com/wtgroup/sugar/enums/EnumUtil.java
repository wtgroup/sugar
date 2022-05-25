package com.wtgroup.sugar.enums;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * 枚举工具
 * <p>
 * 参考 hutool EnumUtil
 *
 * @author L&J
 * @date 2021/9/30 10:52 下午
 */
public class EnumUtil extends cn.hutool.core.util.EnumUtil {

    /**
     * 枚举项字段名+值-->枚举
     *
     * 注: 只会返回第一个匹配的. 自己确保唯一语义.
     * @param enumClass 枚举类
     * @param fieldName 搜索的字段名
     * @param value 值
     * @param <E> 枚举类
     * @return
     */
    public static <E extends Enum<E>> Optional<E> fromField(Class<E> enumClass, String fieldName, Object value) {
        if (value == null || StrUtil.isBlank(fieldName) || enumClass == null) {
            return Optional.empty();
        }
        if (value instanceof CharSequence) {
            value = value.toString().trim();
        }

        final Field field = ReflectUtil.getField(enumClass, fieldName);
        if (field == null) {
            return Optional.empty();
        }

        final Enum<?>[] enums = enumClass.getEnumConstants();

            for (Enum<?> enumObj : enums) {
                if (ObjectUtil.equal(value, ReflectUtil.getFieldValue(enumObj, field))) {
                    return Optional.of((E) enumObj);
                }
            }

        return Optional.empty();
    }

}
