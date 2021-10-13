package com.wtgroup.sugar.collection;

import cn.hutool.core.convert.Convert;

import java.util.HashMap;

/**
 * {@link HashMap&lt;String,Object&gt;} 别名
 * <p>
 *
 * @author L&J
 * @date 2021/9/11 3:53 上午
 */
public class SoMap extends HashMap<String, Object> {

    public static SoMap create(Object... kvs) {
        SoMap soMap = new SoMap();
        return soMap.puts(kvs);
    }

    public <T> T getValue(String key, Class<T> type, T defaultValue) {
        Object ori = this.get(key);
        return Convert.convert(type, key, defaultValue);
    }

    public <T> T getValueQuietly(String key, Class<T> type, T defaultValue) {
        Object ori = this.get(key);
        return Convert.convertQuietly(type, ori, defaultValue);
    }

    public String getString(String key) {
        Object ori = this.get(key);
        return String.valueOf(ori);
    }

    public boolean getBoolean(String key) {
        String ori = this.getString(key);
        return Boolean.parseBoolean(ori);
    }

    public Long getLong(String key) {
        return this.getValue(key, Long.class, null);
    }

    public Integer getInteger(String key) {
        return this.getValue(key, Integer.class, null);
    }

    /**
     * 批量添加 key-value 对.
     * ! key 会统一为 String !
     *
     * @param kvs
     * @return
     */
    public SoMap puts(Object... kvs) {
        int len = kvs.length;
        for (int i = 0; i < len; i++) {
            String key = String.valueOf(kvs[i]);
            ++i;
            Object value = i < len ? kvs[i] : null;
            this.put(key, value);
        }

        return this;
    }

}
