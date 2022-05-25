package com.wtgroup.sugar.collection;

import java.util.HashMap;

/**
 * {@link HashMap&lt;String,Object&gt;} 别名
 * <p>
 *
 * @author L&J
 * @date 2021/9/11 3:53 上午
 */
public class SoMap extends HashMap<String, Object> implements StringKeyMap<Object> {

    public static SoMap of(Object... kvs) {
        SoMap soMap = new SoMap();
        return soMap.puts(kvs);
    }

    /**
     * 批量添加 key-value 对
     * <p>
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
