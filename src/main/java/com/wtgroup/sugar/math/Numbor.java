package com.wtgroup.sugar.math;

import cn.hutool.core.util.NumberUtil;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Numbor
 * <p>
 * Just like {@link Number} , but far more amusing!
 * <p>
 * <pre>
 * 3.0 - 2.0 等于?
 * 3.0 - 2.0 == 1.0 ?
 * </pre>
 * <pre>
 * (double) 0.3 - 0.2; // 0.09999999999999998
 * (double) 0.3 - 0.2 == 1.0; // false
 * // o~o, NumberUtil 可以帮你
 * NumberUtil.sub(0.3, 0.2); // 0.1
 * NumberUtil.sub(0.3, 0.2) == 0.1; // true
 * // Numbor 也可以
 * new Numbor(0.3).sub(0.2); // 0.1
 * new Numbor(0.3).sub(0.2).equals(0.1); // true
 *
 * // 但但, NumberUtil 在多个变量, 同时又不知道是否 null, 是否为 0 时, 在计算前需要很多判断校验, 绕费心思...
 * // NumberUtil.div(1, null); // NullPointerException
 * NumberUtil.mul(1, null); // 应该等于多少? 1 or 0 ?
 * // NumberUtil.div(1, 0); // ArithmeticException
 *
 * // 看我的!
 * new Numbor(1).mul((Number) null).add(12345).div(0).orElse(0); // 管它什么null, 无脑算就是了  => 0
 * </pre>
 * <p>
 * 一些不合法的运算处理思路:
 * 1. 分子非 0, 分母 0 => ±Infinity
 * 2. 分子 0, 分母 0 => NaN
 * 3. 分子非Infinity, 分母 Infinity => 0
 * +Infinity * 1 --> 1/0 * 1 => 1/0 == +Infinity
 * <p>
 * 特殊值计算二维表
 * <p>
 * 参考 Double 值运算规则.
 * [0, +∞, -∞, NaN], 两两组合运算.
 * <pre>
 * // +
 * 0.000     Infinity  -Infinity NaN
 * Infinity  Infinity  NaN       NaN
 * -Infinity NaN       -Infinity NaN
 * NaN       NaN       NaN       NaN
 * // -
 * 0.000     -Infinity Infinity  NaN
 * Infinity  NaN       Infinity  NaN
 * -Infinity -Infinity NaN       NaN
 * NaN       NaN       NaN       NaN
 * // ×
 * 0.000     NaN       NaN       NaN
 * NaN       Infinity  -Infinity NaN
 * NaN       -Infinity Infinity  NaN
 * NaN       NaN       NaN       NaN
 * // ÷
 * NaN       0.000     -0.000    NaN
 * Infinity  NaN       NaN       NaN
 * -Infinity NaN       NaN       NaN
 * NaN       NaN       NaN       NaN
 * </pre>
 * <p>
 * 注: 对性能极致要求时, 不建议使用, 请使用简单的基本运算.
 *
 * @author L&J
 * @date 2021-10-21
 */
public class Numbor implements Serializable {
    /**
     * 全局唯一的, 代表 null Number
     * 在判null时, 关于null的规则不适用
     * Common instance for {@code empty()}.
     */
    public static final Numbor EMPTY = new Empty();

    /**
     * 内部值
     * <p>
     * 你永远不知道也不必知道它实际是什么,
     * 而且, 永远不要试图直接获取它, 任何时候取值要用 get(),
     * 并提前使用 isNull 判断
     */
    private final Number value;

    /**
     * 透传应用到后续入参 Number,
     * 如果传入 Numbor 以我的为主.
     */
    private final Rule rule;

    /**
     * zero
     */
    private Numbor() {
        this(0, Rule.STRICT);
    }

    /**
     * 默认 {@link Rule.STRICT} 模式
     *
     * @param num 数值
     */
    public Numbor(Number num) {
        this(num, Rule.STRICT);
    }

    public Numbor(Number num, Rule rule) {
        this.value = num;
        this.rule = rule;
    }

    /**
     * {@code isNull()} 不通过时时抛异常 {@link NoSuchElementException}
     *
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
        // 这里 value 一定 != null
        assert value != null;
        if (Double.isInfinite(value.doubleValue()) && rule.infinityAsZero) {
            return 0;
        }
        if (Double.isNaN(value.doubleValue()) && rule.nanAsZero) {
            return 0;
        }

        return value;
    }

    /**
     * 若不想看到 NPE, 建议使用此方法, 指定一个降级的默认值.
     *
     * @param other
     * @return
     */
    public Number orElse(Number other) {
        return !isNull() ? get() : other;
    }

    /**
     * Return the value if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
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
     *                              null
     */
    public void ifPresent(Consumer<? super Number> consumer) {
        if (notNull())
            consumer.accept(this.get());
    }

    private Numbor tryCalculate(Numbor other, BiFunction<Number, Number, Number> calculator, BiFunction<Double, Double, Number> onException) {
        if (this.isNull() || other == null || other.isNull()) {
            return EMPTY;
        }
        // 先正常计算, 一旦遇到 Exception 说明, 有特殊值, 导致正常价计算失败, 此时, 取 double 值计算, 按照 double 边界值规则计算出结果
        Number res = null;
        try {
            res = calculator.apply(this.get(), other.get());
        } catch (NumberFormatException | ArithmeticException e) {
            res = onException.apply(this.get().doubleValue(), other.get().doubleValue());
        }

        return new Numbor(res, this.rule);
    }

    public Numbor add(Number other) {
        Numbor n = new Numbor(other, rule);
        return add(n);
    }

    public Numbor add(Numbor other) {
        return tryCalculate(
                other,
                NumberUtil::add,
                (selfDbl, otherDbl) -> selfDbl + otherDbl
        );
    }

    public Numbor sub(Number other) {
        Numbor n = new Numbor(other, rule);
        return sub(n);
    }

    public Numbor sub(Numbor other) {
        return tryCalculate(
                other,
                NumberUtil::sub,
                (selfDbl, otherDbl) -> selfDbl - otherDbl
        );
    }

    public Numbor mul(Number other) {
        Numbor n = new Numbor(other, rule);
        return mul(n);
    }

    public Numbor mul(Numbor other) {
        return tryCalculate(
                other,
                NumberUtil::mul,
                (selfDbl, otherDbl) -> selfDbl * otherDbl
        );
    }

    public Numbor div(Number other) {
        Numbor n = new Numbor(other, rule);
        return div(n);
    }

    public Numbor div(Numbor other) {
        return tryCalculate(
                other,
                NumberUtil::div,
                (selfDbl, otherDbl) -> selfDbl / otherDbl
        );
    }

    /**
     * 常规 四舍五入 的快捷方式
     * 等价于: {@link RoundingMode#HALF_UP}
     *
     * @param scale
     * @return
     */
    public Numbor round(int scale) {
        return this.round(scale, RoundingMode.HALF_UP);
    }

    /**
     * 保留固定位数小数<br>
     * 例如保留 4 位小数：123.456789 ==> 123.4567
     *
     * @param scale        保留小数位数，如果传入小于0，则默认0
     * @param roundingMode 保留小数的模式 {@link RoundingMode}，如果传入null则默认四舍五入
     * @return 新值
     */
    public Numbor round(int scale, RoundingMode roundingMode) {
        if (this.isNull()) {
            return EMPTY;
        }

        return tryGet(
                () -> {
                    BigDecimal round = NumberUtil.round(String.valueOf(this.get()), scale, roundingMode);
                    return new Numbor(round, rule);
                },
                // 特殊值, 无法 round, 原样返回
                () -> this
        );
    }

    public boolean isZero() {
        if (notNull() && !isNaN() && !isInfinity()) {
            return tryGet(
                    () -> BigDecimal.ZERO.compareTo(BigDecimal.valueOf(this.get().doubleValue())) == 0,
                    () -> this.get().doubleValue() == 0);
        } else {
            return false;
        }
    }

    public boolean isNaN() {
        return notNull() && Double.isNaN(this.get().doubleValue());
    }

    public boolean isPositiveInfinity() {
        return notNull() && this.get().doubleValue() == Double.POSITIVE_INFINITY;
    }

    public boolean isNegativeInfinity() {
        return notNull() && this.get().doubleValue() == Double.NEGATIVE_INFINITY;
    }

    public boolean isInfinity() {
        return notNull() && Double.isInfinite(this.get().doubleValue());
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
    public boolean equals(Object other) {
        if (other == this) return true;
        Numbor y;
        if (other instanceof Numbor) {
            y = (Numbor) other;
        } else if (other instanceof Number) {
            y = new Numbor((Number) other, this.rule);
        } else {
            return false;
        }

        if (this.isNull() && y.isNull()) {
            return true;
        }
        if (this.isNull() || y.isNull()) {
            return false;
        }
        // both not null
        return tryGet(
                () -> BigDecimal.valueOf(this.get().doubleValue()).compareTo(BigDecimal.valueOf(y.get().doubleValue())) == 0,
                // 注: 默认 NaN != NaN, 这里改为 true ???
                () -> {
                    double a = this.get().doubleValue();
                    double b = y.get().doubleValue();
                    if (Double.isNaN(a) && Double.isNaN(b)) {
                        return true;
                    }
                    return a == b;
                }
        );
    }

    @Override
    public String toString() {
        if (this.isNull()) {
            return "null";
        }
        Number value = this.get();
        if (value.doubleValue() == 0) {
            return "0"; // BigDecimal 是 0E-10, 但又不影响运算, 所以不放到 get() 里.
        }
        return String.valueOf(value);
    }

    private static <T> T tryGet(Supplier<T> doing, Supplier<T> fallback) {
        try {
            return doing.get();
        } catch (Exception e) {
            return fallback.get();
        }
    }


    private static class Empty extends Numbor {

        public Empty() {
            super(null, Rule.STRICT);
        }

        @Override
        public boolean isNull() {
            return true;
        }

        @Override
        public String toString() {
            return "null";
        }
    }

    /**
     * 规则项:
     * 1. nullAsZero: 默认 false
     * 2. nanAsZero: 默认 false
     * 3. infinityAsZero: 默认 false
     */
    @Data
    @Builder
    @Accessors(chain = true)
    public static class Rule implements Serializable {
        /**
         * 宽松模式: null as 0, NaN as 0, infinity as 0
         */
        public static final Rule LOOSE = new Rule(true, true, true);
        /**
         * 严格模式
         * 默认
         */
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

        /**
         * 运用此规则生成 Numbor 实例
         *
         * @return Numbor
         */
        public Numbor apply(Number num) {
            return new Numbor(num, this);
        }

    }


}
