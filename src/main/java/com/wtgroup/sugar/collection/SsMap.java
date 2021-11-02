package com.wtgroup.sugar.collection;

import java.util.HashMap;

/**
 * {@link HashMap&lt;String,String&gt;} 别名
 * <p>
 *
 * @author L&J
 * @date 2021/9/11 3:53 上午
 */
public class SsMap extends HashMap<String,String> implements StringKeyMap<String> {

    public static SsMap of(String ... kvs) {
        SsMap soMap = new SsMap();
        return soMap.puts((Object[]) kvs);
    }

    /**
     * 批量添加 key-value 对
     * <p>
     * ! key 会统一为 String !
     * ! value 会统一为 String !
     *
     * @param kvs
     * @return
     */
    public SsMap puts(Object... kvs) {
        int len = kvs.length;
        for (int i = 0; i < len; i++) {
            String key = String.valueOf(kvs[i]);
            ++i;
            Object value = i < len ? kvs[i] : null;
            this.put(key, value == null ? null : value.toString());
        }

        return this;
    }

}
