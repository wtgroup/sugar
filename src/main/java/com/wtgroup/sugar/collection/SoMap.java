package com.wtgroup.sugar.collection;

import java.util.HashMap;

/**
 * {@link HashMap(String, Object)} 快捷方式
 */
public class SoMap extends HashMap<String, Object> {

    // public static final SoMap EMPTY_MAP = new SoMap();

    public String getString(String key) {
        Object ori = this.get(key);
        return String.valueOf(ori);
    }

    public boolean getBoolean(String key) {
        String ori = this.getString(key);
        return Boolean.parseBoolean(ori);
    }

    /**批量添加 key-value 对.
     * ! key 会统一为 String !
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
