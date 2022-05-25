package com.wtgroup.sugar.collection;

import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *
 * @author L&J
 * @version 0.1
 * @since 2022/4/29 11:13 下午
 */
public class NameValuePair implements Map<String, String> {
    private final LinkedHashMap<String, String> pairs = new LinkedHashMap<>();

    public NameValuePair() {
    }

    public void puts(Object... kvs) {
        if (kvs != null) {
            int len = kvs.length;
            for (int i = 0; i < len; i++) {
                String key = String.valueOf(kvs[i]);
                ++i;
                Object value = i < len ? kvs[i] : null;
                this.put(key, value == null ? null : value.toString());
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        pairs.forEach((k, v) -> {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(k);
            if (v != null) {
                sb.append("=").append(v);
            }
        });
        return sb.toString();
    }

    public static NameValuePair from(String nameValuePair) {
        NameValuePair inst = new NameValuePair();
        if (StrUtil.isBlank(nameValuePair)) {
            return inst;
        }
        nameValuePair = nameValuePair.trim();
        Arrays.stream(nameValuePair.split("&")).forEach(it -> {
            int ix = it.indexOf("=");
            String key = ix > 0 ? it.substring(0, ix) : it;
            String value = ix > 0 && it.length() > ix + 1 ? it.substring(ix + 1) : null;
            inst.put(key, value);
        });

        return inst;
    }


    @Override
    public int size() {
        return this.pairs.size();
    }

    @Override
    public boolean isEmpty() {
        return this.pairs.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.pairs.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.pairs.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return this.pairs.get(key);
    }

    @Override
    public String put(String key, String value) {
        return this.pairs.put(key, value);
    }

    @Override
    public String remove(Object key) {
        return this.pairs.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> map) {
        this.pairs.putAll(map);
    }

    @Override
    public void clear() {
        this.pairs.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.pairs.keySet();
    }

    @Override
    public Collection<String> values() {
        return this.pairs.values();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return this.pairs.entrySet();
    }
}
