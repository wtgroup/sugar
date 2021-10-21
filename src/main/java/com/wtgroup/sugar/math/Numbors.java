package com.wtgroup.sugar.math;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Numbor 工厂类
 *
 * == 2021-10-22 ==
 * 优化 API 语义
 *
 * == 2021-10-20 ==
 * 优化 API
 *
 * == 2020-12-28 ==
 * - NOT_NULL_AS_ZERO 改名为 NO_NULL_AS_ZERO , not->no, 避免歧义, no 表示取反. not null 容易误解为 '非空'
 * - 新增 `com.wtgroup.sugar.operation.Calculator#strict()`
 * @author dafei
 * @version 0.1
 * @date 2020/4/29 10:29
 */
@ToString
public class Numbors {

    /**
     * 计算用到规则配置
     */
    private final Rule rule;

    private Numbors() {
        rule = Rule.STRICT;
    }

    private Numbors(Rule rule) {
        this.rule = rule;
    }

    /**
     * 严格规则
     *
     * <pre>
     * Rule(false, false, false)
     * </pre>
     */
    public static Numbors STRICT = of(Rule.STRICT);
    /**
     * 宽松模式: null as 0, NaN as 0, infinity as 0
     */
    public static Numbors LOOSE = of(Rule.LOOSE);
    /**
     * null 还是 null
     */
    public static Numbors NO_NULL_AS_ZERO = of(Rule.NO_NULL_AS_ZERO);

    public static Numbors of(Rule rule) {
        return new Numbors(rule);
    }

    /**开始一个表达式计算
     * @param num
     * @return
     */
    public Numbor get(Number num) {
        return new Numbor(num, this.rule);
    }

    @Data
    @Accessors(chain = true)
    public static class Rule {
        /**
         * 宽松模式: null as 0, NaN as 0, infinity as 0
         */
        public static final Rule LOOSE = new Rule(true,true,true);
        /**
         * null 还是 null
         */
        public static final Rule NO_NULL_AS_ZERO = new Rule(false,true,true);
        /**严格模式*/
        public static final Rule STRICT = new Rule(false, false, false);
        final boolean nullAsZero;
        final boolean nanAsZero;
        final boolean infinityAsZero;

        private Rule() {
            this.nullAsZero = false;
            this.nanAsZero = false;
            this.infinityAsZero = false;
        }

        public Rule(boolean nullAsZero, boolean nanAsZero, boolean infinityAsZero) {
            this.nullAsZero = nullAsZero;
            this.nanAsZero = nanAsZero;
            this.infinityAsZero = infinityAsZero;
        }
    }
}
