package com.wtgroup.sugar.operation;

import cn.hutool.core.util.NumberUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 计算器工具类
 *
 * {@link Operator} 的重构版
 * @author dafei
 * @version 0.1
 * @date 2020/4/29 10:29
 */
public class Calculator {

    /**
     * 计算用到规则配置
     */
    private final Rule rule;

    private Calculator(Rule rule) {
        this.rule = rule;
    }

    public static Calculator create() {
        return create(Rule.DEFAULT);
    }

    public static Calculator notNullAsZero() {
        return create(Rule.NOT_NULL_AS_ZERO);
    }

    public static Calculator create(Rule rule) {
        Calculator c = new Calculator(rule);
        return c;
    }

    /**开始一个表达式计算
     * @param num
     * @return
     */
    public Num of(Number num) {
        return new Num(num, this.rule);
    }

    /**
     * @return an empty {@code Num}
     */
    public static Num empty() {
        @SuppressWarnings("unchecked")
        Num t = Num.EMPTY;
        return t;
    }

    @Override
    public String toString() {
        return "Calculator{" +
                "rule=" + rule +
                '}';
    }

    /**
     * 算子
     */
    public static class Num {
        /**全局唯一的, 代表 null Number
         * 在判null时, 关于null的规则不适用
         * Common instance for {@code empty()}.
         */
        public static final Num EMPTY = new Num(null, null);
        /**内部值*/
        private final Number value;
        /**
         * 透传至每个算子 {@link Num}
         */
        private final Rule rule;


        public static Num ofNullable(Number number, Rule rule) {
            return new Num(number, rule);
        }
        public Num(Number num, Rule rule) {
            this.value = num;
            this.rule = rule;
        }

        /**{@code isNull()} 不通过时时抛异常 {@link NoSuchElementException}
         * @return
         */
        public Number get() {
            if (isNull()) {
                throw new NoSuchElementException("No value present");
            }
            // 经过 isNull 的校验, value==null, 说明刚好就是 value == null && rule.nullAsZero 的情况
            if (value == null && rule.nullAsZero) {
                return 0;
            }
            return value;
        }

        /**若不想看到 NPE, 建议使用此方法, 指定一个降级的默认值.
         * @param other
         * @return
         */
        public Number orElse(Number other) {
            return !isNull() ? get() : other;
        }

        /**
         * Return the value if present, otherwise invoke {@code other} and return
         * the result of that invocation.
         * @param other
         * @return
         */
        public Number orElseGet(Supplier<? extends Number> other) {
            return notNull() ? get() : other.get();
        }

        /**
         * If a value is present, invoke the specified consumer with the value,
         * otherwise do nothing.
         *
         * @param consumer block to be executed if a value is present
         * @throws NullPointerException if value is present and {@code consumer} is
         * null
         */
        public void ifPresent(Consumer<? super Number> consumer) {
            if (notNull())
                consumer.accept(value);
        }


        public Num div(Number other) {
            Num n = Num.ofNullable(other, rule);
            return div(n);
        }

        public Num div(Num other) {
            if (isNull() || other.isNull()) {
                return empty();
            }

            if (other.isZero()) {
                // 非0 / 0
                if (!isZero()) {
                    if (rule.infinityAsZero) {
                        return Num.ofNullable(0, rule);
                    }else{
                        Number n = get().doubleValue() > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                        return Num.ofNullable(n, rule);
                    }
                }

                // 0 / 0 NaN
                if (isZero()) {
                    if (rule.nanAsZero) {
                        return Num.ofNullable(0, rule);
                    }else{
                        return Num.ofNullable(Double.NaN, rule);
                    }
                }
            }

            // Number res = get().doubleValue()/other.get().doubleValue();
            Number res = NumberUtil.div(get(), other.get());

            return Num.ofNullable(res, rule);
        }

        public Num mul(Number other) {
            Num n = Num.ofNullable(other, rule);
            return mul(n);
        }

        public Num mul(Num other) {
            if (isNull() || other.isNull()) {
                return empty();
            }

            BigDecimal mul = NumberUtil.mul(get(), other.get());

            return Num.ofNullable(mul, rule);

        }

        public Num add(Number other) {
            Num n = Num.ofNullable(other, rule);
            return add(n);
        }

        public Num add(Num other) {
            if (isNull() || other.isNull()) {
                return empty();
            }

            BigDecimal add = NumberUtil.add(get(), other.get());

            return Num.ofNullable(add, rule);
        }

        public Num sub(Number other) {
            Num n = Num.ofNullable(other, rule);
            return sub(n);
        }

        public Num sub(Num other) {
            if (isNull() || other.isNull()) {
                return empty();
            }

            BigDecimal sub = NumberUtil.sub(get(), other.get());

            return Num.ofNullable(sub, rule);
        }

        /**常规 四舍五入 的快捷方式
         * 等价于: {@link RoundingMode#HALF_UP}
         * @param scale
         * @return
         */
        public Num round(int scale) {
            return this.round(scale, null);
        }

        /**
         * 保留固定位数小数<br>
         * 例如保留 4 位小数：123.456789 ==> 123.4567
         *
         * @param scale 保留小数位数，如果传入小于0，则默认0
         * @param roundingMode 保留小数的模式 {@link RoundingMode}，如果传入null则默认四舍五入
         * @return 新值
         */
        public Num round(int scale, RoundingMode roundingMode) {
            if (this.isNull()) {
                return empty();
            }
            BigDecimal round = NumberUtil.round(String.valueOf(this.get()), scale, roundingMode);
            return Num.ofNullable(round, rule);
        }


        public boolean isZero() {
            return get().doubleValue() == 0;
        }

        public boolean isNull() {
            // empty 就是 empty 关于 null 的宽容规则不适用
            if (this == EMPTY) {
                return true;
            }
            if (value == null) {
                if (rule.nullAsZero) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }

        public boolean notNull() {
            return !isNull();
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Rule {
        public static final Rule DEFAULT = new Rule(true,true,true);
        public static final Rule NOT_NULL_AS_ZERO = new Rule(false,true,true);
        /**严格模式*/
        public static final Rule STRICT_MODE = new Rule(false, false, false);
        private boolean nullAsZero;
        private boolean nanAsZero;
        private boolean infinityAsZero;

        public Rule() {
        }

        public Rule(boolean nullAsZero, boolean nanAsZero, boolean infinityAsZero) {
            this.nullAsZero = nullAsZero;
            this.nanAsZero = nanAsZero;
            this.infinityAsZero = infinityAsZero;
        }
    }
}
