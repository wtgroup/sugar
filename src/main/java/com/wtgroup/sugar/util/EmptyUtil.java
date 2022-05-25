package com.wtgroup.sugar.util;


import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 判空工具
 * <p>
 * - 无需知道目标值类型, 内置多种类型的判空. 数值, 字符串, 数组, List, Map, 日期
 * - 随意切换不同"空模式"实现你想要的判空逻辑. 注意, null 永远是空.
 *
 * @author L&J
 * @date 2021/10/1 9:33 下午
 */
public class EmptyUtil {

    // 默认模式直接调用, 不对外暴露
    private static final EmptyMode defaultMode = new StrictEmptyMode();

    /*注: 新增模式, 以 static final 以此列出. 字段名: {xxx}_MODE */

    public static final EmptyMode STRICT_MODE = new StrictEmptyMode();

    /**
     * jdk 代码层面的空
     * 1. 空白符
     * 2. 空集合
     * 3. 空 Map
     */
    public static final EmptyMode JDK_MODE = new JdkEmptyMode();


    /**
     * 目标对象是否为空
     *
     * @param subject         目标对象
     * @param otherEmptyCases 用户定义的其他为空的 case, 优先级更高.
     * @return
     */
    public static boolean isEmpty(Object subject, Object... otherEmptyCases) {
        return defaultMode.isEmpty(subject, otherEmptyCases);
    }

    public static boolean isNotEmpty(Object subject, Object... otherEmptyCases) {
        return !isEmpty(subject, otherEmptyCases);
    }

    public static <T> T defaultIfEmpty(final T subject, final T defaultValue, Object... emptyCases) {
        return defaultMode.defaultIfEmpty(subject, defaultValue, emptyCases);
    }


    /**
     * 可通过扩展"空模式"应对更复杂的场景
     */
    public interface EmptyMode {

        /**
         * @param subject         目标对象
         * @param otherEmptyCases 其他空的 case
         * @return
         */
        boolean isEmpty(Object subject, Object... otherEmptyCases);

        default boolean isNotEmpty(Object subject, Object... otherEmptyCases) {
            return !isEmpty(subject, otherEmptyCases);
        }

        /**
         * @param subject    目标对象
         * @param emptyCases 指定空 case, 命中其一则空; 都不命中, 非空. 注意, null 永远是空.
         * @return
         */
        default boolean isEmptyByCases(Object subject, Object... emptyCases) {
            if (subject == null) {
                return true;
            }
            boolean flg = false;

            if (emptyCases != null) {
                for (Object emptyCase : emptyCases) {
                    if (subject.equals(emptyCase)) {
                        flg = true;
                        break;
                    }
                }
            }

            return flg;
        }

        default <T> T defaultIfEmpty(final T subject, final T defaultValue, Object... emptyCases) {
            return this.isEmpty(subject, emptyCases) ? defaultValue : subject;
        }
    }


    /**
     * 严格模式
     * <p>
     * - 数字: 0
     * - 字符传: 空白符 | 'null' | 'NULL'
     * - 日期: 0 毫秒
     * - 集合: 无元素
     * - Map: 无entry
     */
    private static class StrictEmptyMode implements EmptyMode {

        @Override
        public boolean isEmpty(Object subject, Object... otherEmptyCases) {
            boolean flg = this.isEmptyByCases(subject, otherEmptyCases);

            if (flg) return flg; // 已经判断是 空, 下面不用在判断了, 用户指定的优先级更高

            if (subject instanceof Number) {
                flg = NumberUtil.equals(new BigDecimal(subject.toString()), BigDecimal.ZERO);
            } else if (subject instanceof CharSequence) {
                String strSubject = String.valueOf(subject);
                flg = StrUtil.isBlank((CharSequence) subject) || "null".equalsIgnoreCase(strSubject); // || "0".equals(strSubject);
            } else if (subject instanceof Date) {
                flg = ((Date) subject).getTime() == 0;
            } else if (subject instanceof Collection) {
                flg = ((Collection<?>) subject).isEmpty();
            } else if (subject instanceof Map) {
                flg = ((Map<?, ?>) subject).isEmpty();
            } else if (ArrayUtil.isArray(subject)) {
                flg = ArrayUtil.isEmpty((Object[]) subject);
            }

            return flg;
        }
    }

    /**
     * jdk 代码层面的空
     * 1. 空白符
     * 2. 空集合
     * 3. 空 Map
     */
    private static class JdkEmptyMode implements EmptyMode {

        @Override
        public boolean isEmpty(Object subject, Object... otherEmptyCases) {
            boolean flg = this.isEmptyByCases(subject, otherEmptyCases);

            if (flg) return flg; // 已经判断是 空, 下面不用在判断了, 用户指定的优先级更高

            if (subject instanceof CharSequence) {
                flg = StrUtil.isBlank((CharSequence) subject);
            } else if (subject instanceof Collection) {
                flg = ((Collection<?>) subject).isEmpty();
            } else if (subject instanceof Map) {
                flg = ((Map<?, ?>) subject).isEmpty();
            } else if (ArrayUtil.isArray(subject)) {
                flg = ArrayUtil.isEmpty((Object[])subject);
            }

            return flg;
        }
    }

}
