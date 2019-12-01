package com.wtgroup.sugar.operation;

import cn.hutool.core.util.NumberUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 数字运算算子工具类
 *
 * 解决计算时 0值, null 值 的烦人处理. 开发者使用此类, 将不必care任何0值和null值.
 * 参考 Java8 的 Optional 的思想设计.
 *
 * 0/0
 * null/null
 * 0/null
 * null/0
 *
 * 出于性能考虑, 不建议大数据量计算时运用此工具类计算.
 * @author dafei
 * @version 0.1
 * @date 2019/11/25 12:42
 */
public class Operator {

    /**全局唯一的, 代表 null Number
     * 在判null时, 关于null的规则不适用
     * Common instance for {@code empty()}.
     */
    private static final Operator EMPTY = new Operator();

    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final Number value;
    /**
     * 起始的算子配置好后, 由此算子开始的运算式中所有算子公用该 Rule
     */
    private final Rule rule;

    // private boolean nullAsZero = true;
    // private boolean nanAsZero = true;
    // private boolean infinityAsZero = true;

    /**
     * Constructs an empty instance.
     *
     * @implNote Generally only one empty instance, {@link Operator#EMPTY},
     * should exist per VM.
     */
    private Operator() {
        this.value = null;
        this.rule = Rule.DEFAULT;
    }

    private Operator(Number value) {
        this(value, Rule.DEFAULT);
    }
    /**
     * @throws NullPointerException if value is null
     */
    private Operator(Number value, Rule rule) {
        this.value = Objects.requireNonNull(value);
        this.rule = rule == null ? Rule.DEFAULT : rule;
    }

    /**
     * @return an empty {@code Operator}
     */
    public static Operator empty() {
        @SuppressWarnings("unchecked")
        Operator t = (Operator) EMPTY;
        return t;
    }

    /**
     * 此构造时, 要求 value 非 null
     * Returns an {@code Operator} with the specified present non-null value.
     *
     * @param value the value to be present, which must be non-null
     * @return an {@code Operator} with the value present
     * @throws NullPointerException if value is null
     */
    public static Operator of(Number value) {
        return new Operator(value);
    }

    /**此构造时, 要求 value 非 null
     * @param value
     * @param rule
     * @return
     */
    public static Operator of(Number value, Rule rule) {
        return new Operator(value, rule);
    }

    /**
     * Returns an {@code Operator} describing the specified value, if non-null,
     * otherwise returns an empty {@code Operator}.
     *
     * @param value the possibly-null value to describe
     * @return an {@code Operator} with a present value if the specified value
     * is non-null, otherwise an empty {@code Operator}
     */
    public static Operator ofNullable(Number value) {
        return ofNullable(value, Rule.DEFAULT);
    }

    public static Operator ofNullable(Number value, Rule rule) {
        if (value == null) {
            if (rule.nullAsZero) {
                value = 0;
            }
        }

        return value == null ? empty() : of(value, rule);
    }


    /**若想及时发现 NPE, 建议使用此方法获取 Number.
     * 重要的是, 此方法得到逻辑一致的值(不会改变原始值, 如原始值是 null, 但 Rule.nullAsZero=true, 那么, 我会返回 0 )
     * @return
     */
    public Number get() {
        if (isNull()) {
            throw new NoSuchElementException("No value present");
            // throw new NullPointerException("算子为空");
        }
        // 经过 isNull 的校验, value==null, 说明刚好就是 value == null && rule.nullAsZero 的情况
        if (value == null && rule.nullAsZero) {
            return 0;
        }
        return value;
    }

    /**若不想看到 NPE, 建议使用此方法, 制定一个降级的默认值.
     * @param other
     * @return
     */
    public Number orElse(Number other) {
        return !isNull() ? get() : other;
    }


    // /**如非必要不建议此方法获取原始值, 因为设置的Rule将会失效.
    //  * 破坏整个逻辑链条.
    //  * @return
    //  */
    // public Number getRaw() {
    //     return value;
    // }

    // /**
    //  * Return {@code true} if there is a value present, otherwise {@code false}.
    //  *
    //  * @return {@code true} if there is a value present, otherwise {@code false}
    //  */
    // public boolean isPresent() {
    //     return value != null;
    // }
    //
    // /**
    //  * If a value is present, invoke the specified consumer with the value,
    //  * otherwise do nothing.
    //  *
    //  * @param consumer block to be executed if a value is present
    //  * @throws NullPointerException if value is present and {@code consumer} is
    //  * null
    //  */
    // public void ifPresent(Consumer<? super T> consumer) {
    //     if (value != null)
    //         consumer.accept(value);
    // }


    public Operator div(Number divisor) {
        Operator other = Operator.ofNullable(divisor, rule);
        if (isNull() || other.isNull()) {
            return empty();
        }

        if (other.isZero()) {
            // 非0 / 0
            if (!isZero()) {
                if (rule.infinityAsZero) {
                    return Operator.of(0, rule);
                }else{
                    Number n = get().doubleValue() > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                    return Operator.of(n, rule);
                }
            }

            // 0 / 0 NaN
            if (isZero()) {
                if (rule.nanAsZero) {
                    return Operator.of(0, rule);
                }else{
                    return Operator.ofNullable(Double.NaN, rule);
                }
            }
        }

        // Number res = get().doubleValue()/other.get().doubleValue();
        Number res = NumberUtil.div(get(), other.get());

        return Operator.ofNullable(res, rule);
    }

    public Operator mul(Number other) {
        Operator otherOpt = Operator.ofNullable(other, rule);
        if (isNull() || otherOpt.isNull()) {
            return empty();
        }

        BigDecimal mul = NumberUtil.mul(get(), otherOpt.get());

        return Operator.ofNullable(mul, rule);

    }

    public Operator add(Number other) {
        Operator otherOpt = Operator.ofNullable(other, rule);
        if (isNull() || otherOpt.isNull()) {
            return empty();
        }

        BigDecimal add = NumberUtil.add(get(), otherOpt.get());

        return Operator.ofNullable(add, rule);
    }

    public Operator sub(Number other) {
        Operator otherOpt = Operator.ofNullable(other, rule);
        if (isNull() || otherOpt.isNull()) {
            return empty();
        }

        BigDecimal sub = NumberUtil.sub(get(), otherOpt.get());

        return Operator.ofNullable(sub, rule);
    }

    /**常规 四舍五入 的快捷方式
     * 等价于: {@link RoundingMode#HALF_UP}
     * @param scale
     * @return
     */
    public Operator round(int scale) {
        return this.round(scale, null);
    }

    /**
     * 保留固定位数小数<br>
     * 例如保留四位小数：123.456789 =》 123.4567
     *
     * @param scale 保留小数位数，如果传入小于0，则默认0
     * @param roundingMode 保留小数的模式 {@link RoundingMode}，如果传入null则默认四舍五入
     * @return 新值
     */
    public Operator round(int scale, RoundingMode roundingMode) {
        if (this.isNull()) {
            return empty();
        }
        BigDecimal round = NumberUtil.round(String.valueOf(this.get()), scale, roundingMode);
        return Operator.of(round);
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

    public boolean isNotNull() {
        return !isNull();
    }

    @Data
    @Accessors(chain = true)
    public static class Rule {
        public static final Rule DEFAULT = new Rule(true,true,true);
        public static final Rule NOT_NULL_AS_ZERO = new Rule(false,true,true);
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


    @Override
    public String toString() {
        return isNull() ? null : get().toString();
    }
}
