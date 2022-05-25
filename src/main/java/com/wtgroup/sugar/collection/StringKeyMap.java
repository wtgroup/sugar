package com.wtgroup.sugar.collection;

import cn.hutool.core.convert.Convert;

import java.util.HashMap;
import java.util.Map;

/**
 * String key Map
 * <p>
 * {@link HashMap&lt;String,?&gt;} 别名.
 * 提供一些便利方法.
 * <p>
 *
 * @author L&J
 * @date 2021/9/11 3:53 上午
 */
public interface StringKeyMap<V> extends Map<String, V> {

    default <T> T getValue(String key, Class<T> type, T defaultValue) {
        Object ori = this.get(key);
        if (ori == null) {
            return defaultValue;
        }

        if (type.isAssignableFrom(ori.getClass())) {
            return (T) ori;
        }

        return Convert.convert(type, key, defaultValue);
    }

    default <T> T getValueQuietly(String key, Class<T> type, T defaultValue) {
        Object ori = this.get(key);
        return Convert.convertQuietly(type, ori, defaultValue);
    }

    default String getString(String key) {
        Object ori = this.get(key);
        return String.valueOf(ori);
    }

    default boolean getBoolean(String key) {
        String ori = this.getString(key);
        return Boolean.parseBoolean(ori);
    }

    default Long getLong(String key) {
        return this.getValue(key, Long.class, null);
    }

    default Integer getInteger(String key) {
        return this.getValue(key, Integer.class, null);
    }

    /**
     * 批量添加 key-value 对
     * <p>
     * ! key 会统一为 String !
     * <p>
     * kvs.length 奇数个数时, 最后的 value 会用 null 填充
     *
     * @param kvs k, v, k, v, ...
     */
    StringKeyMap<V> puts(Object... kvs);

}
