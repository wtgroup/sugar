package com.wtgroup.sugar.math;

import cn.hutool.core.util.NumberUtil;
import com.wtgroup.sugar.lang.AbstractFlag;
import lombok.Builder;
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
 * 3.0 - 2.0 == 0.1 ?
 * </pre>
 * <pre>
 * (double) 0.3 - 0.2; // 0.09999999999999998
 * (double) 0.3 - 0.2 == 0.1; // false
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
 * new Numbor(1).mul((Number) null).add(12345).div(0).orElse(0); // 管它什么null, 无脑算就是了 => 0
 * </pre>
 * <p>
 * 一些不合法的运算处理思路:
 * <li>1. 分子非 0, 分母 0 => ±Infinity
 * <li>2. 分子 0, 分母 0 => NaN
 * <li>3. 分子非Infinity, 分母 Infinity => 0
 * +Infinity * 1 --> 1/0 * 1 => 1/0 == +Infinity
 * <p>
 * <br>
 * <h4>规则配置</h4>
 * <p>
 * 分为'ignore'和'as'两类, 前者表示忽略异常值, 不参与运算, 后者表示按照规则将异常值转为正常值参与运算.
 * 优先级更高, 同时设置会覆盖后者.
 * <p>
 * 默认, 都是false, 即按照jdk正常运算规则计算.
 *
 * <li>IGNORE_NULL: 忽略 null 值. 如 <code>null * 8 = 8</code>
 * <li>IGNORE_INFINITY: 忽略无穷大. 如 <code>1/0 + 8 = 8</code>
 * <li>IGNORE_NAN: 忽略NaN. 如 <code>0/0 + 8 = 8</code>
 * <li>NULL_AS_0: null 当作 0 参与运算. 如 <code>null * 8 = 0</code>
 * <li>INFINITY_AS_0: 无穷大当作 0 参与运算. 如 <code>1/0 + 8 = 8</code>
 * <li>NAN_AS_0: NaN 当作 0 参与运算. 如 <code>0/0 + 8 = 8</code>
 *
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
public class Numbor extends Number implements Comparable<Numbor>, Serializable {
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
     * 并提前使用 isEmpty 判断
     */
    private final Number value;

    /**
     * 透传应用到后续入参 Number,
     * 如果传入 Numbor 以我的为主.
     */
    private final Rule rule;

    public static Rule rule(int ruleFlags) {
        return new Rule(ruleFlags);
    }

    /**
     * zero
     */
    private Numbor() {
        this(0, Rule.STRICT);
    }

    @Override
    public int intValue() {
        return isEmpty() ? 0 : get().intValue();
    }

    @Override
    public long longValue() {
        return isEmpty() ? 0 : get().intValue();
    }

    @Override
    public float floatValue() {
        return isEmpty() ? 0F : get().floatValue();
    }

    @Override
    public double doubleValue() {
        return isEmpty() ? 0D : get().doubleValue();
    }

    // /**
    //  * 输出 BigDecimal 快捷方式
    //  * <p>
    //  * 无效数字输出 null !!
    //  *
    //  * @return BigDecimal
    //  */
    // public BigDecimal bigDecimal() {
    //     if (!isValid()) {
    //         return null;
    //     }
    //     Number val = this.get();
    //     return NumberUtil.toBigDecimal(val);
    // }

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

    public Numbor(Number num, int ruleFlags) {
        this(num, new Rule(ruleFlags));
    }

    /**
     * {@code isEmpty()} 不通过时时抛异常 {@link NoSuchElementException}
     *
     * @return
     */
    public Number get() {
        if (isEmpty()) {
            throw new NoSuchElementException("No value present");
        }
        // 经过 isEmpty 的校验, value==null, 说明刚好就是 value == null && rule.nullAs0 的情况
        if (value == null && rule.isNullAs0()) {
            return 0;
        }
        // 这里 value 一定 != null
        assert value != null;
        if (Double.isInfinite(value.doubleValue()) && rule.isInfinityAs0()) {
            return 0;
        }
        if (Double.isNaN(value.doubleValue()) && rule.isNanAs0()) {
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
        return isValid() ? get() : other;
    }

    /**
     * Return the value if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
     * @param other
     * @return
     */
    public Number orElseGet(Supplier<? extends Number> other) {
        return isValid() ? get() : other.get();
    }

    /**
     * If a value is present, invoke the specified consumer with the value,
     * otherwise do nothing.
     *
     * @param consumer block to be executed if a value is present
     * @throws NullPointerException if value is present and {@code consumer} is
     *                              null
     */
    public void ifValid(Consumer<? super Number> consumer) {
        if (isValid())
            consumer.accept(this.get());
    }

    private Numbor tryCalculate(Numbor other, BiFunction<Number, Number, Number> calculator, BiFunction<Double, Double, Number> onException) {
        // other 对象 null, 忽略
        if (other == null) {
            return this;
        }

        // 异常值忽略处理, 如果设置响应策略, 且刚好有对应异常值, 则忽略, 不运算
        if (rule.isIgnoreNull() && (isEmpty() || other.isEmpty())) {
            return isEmpty() ? (other.isEmpty() ? EMPTY : other) : this;
        }
        if (rule.isIgnoreInfinity() && (isInfinity() || other.isInfinity())) {
            return isInfinity() ? (other.isInfinity() ? EMPTY : other) : this;
        }
        if (rule.isIgnoreNan() && (isNaN() || other.isNaN())) {
            return isNaN() ? (other.isNaN() ? EMPTY : other) : this;
        }

        if (this.isEmpty() || other.isEmpty()) {
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
        if (this.isEmpty()) {
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
        if (isNotEmpty() && !isNaN() && !isInfinity()) {
            return tryGet(
                    () -> BigDecimal.ZERO.compareTo(BigDecimal.valueOf(this.get().doubleValue())) == 0,
                    () -> this.get().doubleValue() == 0);
        } else {
            return false;
        }
    }

    public boolean isNaN() {
        return isNotEmpty() && Double.isNaN(this.get().doubleValue());
    }

    public boolean isPositiveInfinity() {
        return isNotEmpty() && this.get().doubleValue() == Double.POSITIVE_INFINITY;
    }

    public boolean isNegativeInfinity() {
        return isNotEmpty() && this.get().doubleValue() == Double.NEGATIVE_INFINITY;
    }

    public boolean isInfinity() {
        return isNotEmpty() && Double.isInfinite(this.get().doubleValue());
    }

    public boolean isEmpty() {
        // empty 就是 empty 关于 null 的宽容规则不适用
        if (this == EMPTY) {
            return true;
        }
        if (value == null) {
            if (rule.isNullAs0()) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    /**
     * 是否有效: 不为 null, 且不是 NaN, Infinity
     *
     * 取最终计算结果, 会先经过次方法校验.
     */
    public boolean isValid() {
        return isNotEmpty()
                && !Double.isInfinite(this.get().doubleValue())
                && !Double.isNaN(this.get().doubleValue());
    }

    /**
     * @param other {@link Number} | {@link Numbor}
     * @return Boolean
     */
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

        if (this.isEmpty() && y.isEmpty()) {
            return true;
        }
        if (this.isEmpty() || y.isEmpty()) {
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
        if (this.isEmpty()) {
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

    /**
     * 比较大小
     * <p>
     * 大小顺序
     * <pre>
     * Double.NEGATIVE_INFINITY < Double.POSITIVE_INFINITY < Double.NaN < EMPTY
     * </pre>
     */
    @Override
    public int compareTo(Numbor other) {
        if (other == null) {
            return -1;
        }
        if (isNotEmpty() && other.isNotEmpty()) {
            return tryGet(
                    () -> Double.compare(this.get().doubleValue(), other.get().doubleValue()),
                    () -> 0
            );
        } else if (isNotEmpty()) {
            return -1;
        } else if (other.isNotEmpty()) {
            return 1;
        } else {
            return 0;
        }
    }


    private static class Empty extends Numbor {

        public Empty() {
            super(null, Rule.STRICT);
        }

        @Override
        public boolean isEmpty() {
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
    @Builder
    @Accessors(chain = true)
    public static class Rule extends AbstractFlag implements Serializable {

        private static final Rule STRICT = new Rule();
        private static final Rule LOOSE = new Rule(Rule.NULL_AS_0 | Rule.INFINITY_AS_0 | Rule.NAN_AS_0);
        private static final Rule IGNORE = new Rule(Rule.IGNORE_NULL | Rule.IGNORE_INFINITY | Rule.IGNORE_NAN);

        // -- 忽略特殊值, 优先级高于下面 AS 规则 --
        public static final int IGNORE_NULL     = 1;
        public static final int IGNORE_INFINITY = 1 << 1;
        public static final int IGNORE_NAN      = 1 << 2;
        // -- AS --
        public static final int NULL_AS_0       = 1 << 3;
        public static final int INFINITY_AS_0   = 1 << 4;
        public static final int NAN_AS_0        = 1 << 5;

        /**
         * 严格模式
         * <p>
         * 默认
         */
        public static Rule strict() {
            return STRICT;
        }

        /**
         * 宽松模式
         * <p>
         * null as 0, NaN as 0, Infinity as 0
         */
        public static Rule loose() {
            return LOOSE;
        }

        /**
         * 忽略所有异常值
         */
        public static Rule ignore() {
            return IGNORE;
        }

        /**
         * 默认规则, jdk 自身运算规则, 无自定义规则.
         */
        public Rule() {
            this(0);
        }
        public Rule(int flags) {
            super(flags);
            if (has(IGNORE_NULL) && has(NULL_AS_0)) {
                remove(NULL_AS_0);
            }
            if (has(IGNORE_INFINITY) && has(INFINITY_AS_0)) {
                remove(INFINITY_AS_0);
            }
            if (has(IGNORE_NAN) && has(NAN_AS_0)) {
                remove(NAN_AS_0);
            }
        }

        /**
         * 运用此规则生成 Numbor 实例
         *
         * @return Numbor
         */
        public Numbor apply(Number num) {
            return new Numbor(num, this);
        }

        public boolean isIgnoreNull() {
            return has(IGNORE_NULL);
        }

        public boolean isIgnoreInfinity() {
            return has(IGNORE_INFINITY);
        }

        public boolean isIgnoreNan() {
            return has(IGNORE_NAN);
        }

        public boolean isNullAs0() {
            return has(NULL_AS_0);
        }

        public boolean isInfinityAs0() {
            return has(INFINITY_AS_0);
        }

        public boolean isNanAs0() {
            return has(NAN_AS_0);
        }
    }


}
