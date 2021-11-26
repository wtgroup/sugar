package com.wtgroup.sugar.util;

/**
 * <p>
 *
 * @author L&J
 * @date 2021/11/26 3:01 下午
 */
public class MathUtil extends cn.hutool.core.math.MathUtil {

    /**
     * Math.toIntExact 增强, 溢出时, 返回 defaultValue
     */
    public static int toIntExact(long value, int defaultValue) {
        try {
            return Math.toIntExact(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
