package com.wtgroup.sugar.math;

import cn.hutool.core.util.NumberUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Numbor
 *
 * Just like {@link Number} , but far more amusing!
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
     */
    private final Number value;

    /**
     * 透传至每个算子 {@link Numbor}
     */
    private final Numbors.Rule rule;


    public static Numbor ofNullable(Number number, Numbors.Rule rule) {
        return new Numbor(number, rule);
    }

    /**
     * 仅包内可见
     */
    Numbor() {
        this.value = null;
        this.rule = Numbors.Rule.STRICT;
    }

    Numbor(Number num, Numbors.Rule rule) {
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
        if ((value.doubleValue() == Double.POSITIVE_INFINITY || value.doubleValue() == Double.NEGATIVE_INFINITY) && rule.infinityAsZero) {
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
            consumer.accept(value);
    }


    public Numbor div(Number other) {
        Numbor n = Numbor.ofNullable(other, rule);
        return div(n);
    }

    public Numbor div(Numbor other) {
        if (isNull() || other.isNull()) {
            return EMPTY;
        }

        if (other.isZero()) {
            // 非0 / 0
            if (!isZero()) {
                if (rule.infinityAsZero) {
                    return Numbor.ofNullable(0, rule);
                } else {
                    Number n = get().doubleValue() > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                    return Numbor.ofNullable(n, rule);
                }
            }
            // 0 / 0 NaN
            else {
                if (rule.nanAsZero) {
                    return Numbor.ofNullable(0, rule);
                } else {
                    // return Num.ofNullable(Double.NaN, rule);
                    // 'NaN' 无法运算  改为返回 empty
                    return EMPTY;
                }
            }
        }

        // Number res = get().doubleValue()/other.get().doubleValue();
        Number res = NumberUtil.div(get(), other.get());

        return Numbor.ofNullable(res, rule);
    }

    public Numbor mul(Number other) {
        Numbor n = Numbor.ofNullable(other, rule);
        return mul(n);
    }

    public Numbor mul(Numbor other) {
        if (isNull() || other.isNull()) {
            return EMPTY;
        }

        BigDecimal mul = NumberUtil.mul(get(), other.get());

        return Numbor.ofNullable(mul, rule);

    }

    public Numbor add(Number other) {
        Numbor n = Numbor.ofNullable(other, rule);
        return add(n);
    }

    public Numbor add(Numbor other) {
        if (isNull() || other.isNull()) {
            return EMPTY;
        }

        BigDecimal add = NumberUtil.add(get(), other.get());

        return Numbor.ofNullable(add, rule);
    }

    public Numbor sub(Number other) {
        Numbor n = Numbor.ofNullable(other, rule);
        return sub(n);
    }

    public Numbor sub(Numbor other) {
        if (isNull() || other.isNull()) {
            return EMPTY;
        }

        BigDecimal sub = NumberUtil.sub(get(), other.get());

        return Numbor.ofNullable(sub, rule);
    }

    /**
     * 常规 四舍五入 的快捷方式
     * 等价于: {@link RoundingMode#HALF_UP}
     *
     * @param scale
     * @return
     */
    public Numbor round(int scale) {
        return this.round(scale, null);
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
        BigDecimal round = NumberUtil.round(String.valueOf(this.get()), scale, roundingMode);
        return Numbor.ofNullable(round, rule);
    }


    public boolean isZero() {
        return !isNull() && get().doubleValue() == 0.0;
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


    private static class Empty extends Numbor {

        public Empty() {
            super(null, Numbors.Rule.STRICT);
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
}
