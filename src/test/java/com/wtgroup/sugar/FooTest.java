package com.wtgroup.sugar;

import cn.hutool.core.util.NumberUtil;
import com.wtgroup.sugar.math.Numbor;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * @author dafei
 * @version 0.1
 * @date 2019/12/2 15:34
 */
public class FooTest {

    @Test
    public void foo5() {
        RuleFlag ruleFlag = new RuleFlag(RuleFlag.IGNORE_NULL | RuleFlag.NULL0 | RuleFlag.INFINITY0);
        System.out.println(ruleFlag);
        // ruleFlag.add(RuleFlag.IGNORE_NAN | RuleFlag.NULL0 | RuleFlag.INFINITY0);
        // System.out.println(ruleFlag);
        System.out.println(ruleFlag.has(RuleFlag.NULL0));
    }


    @Test
    public void foo4() {
        System.out.println((double) 0.3 - 0.2); // 0.09999999999999998
        System.out.println((double) 0.3 - 0.2 == 1.0); // false
        // o~o, NumberUtil 可以帮你
        System.out.println(NumberUtil.sub(0.3, 0.2)); // 0.1
        System.out.println(NumberUtil.sub(0.3, 0.2) == 0.1); // true
        // Numbor 也可以
        System.out.println(new Numbor(0.3).sub(0.2)); // 0.1
        System.out.println(new Numbor(0.3).sub(0.2).equals(0.1)); // true

        // 但但, NumberUtil 在多个变量, 同时又不知道是否 null, 是否为 0 时, 在计算前需要很多判断校验, 绕费心思...
        // System.out.println(NumberUtil.div(1, null)); // NullPointerException
        System.out.println(NumberUtil.mul(1, null)); // 应该等于多少? 1 or 0 ?
        // System.out.println(NumberUtil.div(1, 0)); // ArithmeticException

        // 看我的!
        System.out.println(new Numbor(1).mul((Number) null).add(12345).div(0).orElse(0)); // 管它什么null, 无脑算就是了  => 0


        // System.out.println((double) 0.3 - (double) 0.2);
        // System.out.println(NumberUtil.sub((double) 0.3, (double) 0.2));
        // System.out.println(NumberUtil.add(Double.NaN));
        // System.out.println(BigDecimal.valueOf(Double.NaN));
        // System.out.println(BigDecimal.valueOf(Double.POSITIVE_INFINITY));
        // System.out.println(BigDecimal.valueOf(Double.NEGATIVE_INFINITY));
        // System.out.println(BigDecimal.valueOf(Double.NaN));

        // System.out.println(new Numbor(null, Numbor.Rule.LOOSE).add((Number) null).get().intValue());
    }


    @Test
    public void foo3() {
        // System.out.println(BigDecimal.valueOf(0).equals(BigDecimal.valueOf(-0)));
        // System.out.println(BigDecimal.valueOf((double) 0).equals(BigDecimal.valueOf(0.000)));
        System.out.println(BigDecimal.valueOf(0.000).equals(BigDecimal.valueOf(0)));
        System.out.println(BigDecimal.valueOf(-0.000).equals(BigDecimal.valueOf(0)));
        // System.out.println(BigDecimal.ZERO.equals(BigDecimal.valueOf(0)));
        // System.out.println(BigDecimal.valueOf(-0.00000).equals(BigDecimal.valueOf(0.000)));

        // System.out.println(NumberUtil.equals(0, 0.00));
        System.out.println(NumberUtil.equals(BigDecimal.ZERO, BigDecimal.valueOf(0.000)));
        System.out.println(BigDecimal.ZERO.compareTo(BigDecimal.valueOf(0.000)));
        System.out.println(BigDecimal.ZERO.compareTo(BigDecimal.valueOf(-0.000)));
        System.out.println(BigDecimal.ZERO.compareTo(BigDecimal.valueOf(0)));
        // System.out.println(NumberUtil.equals(BigDecimal.valueOf(0), BigDecimal.valueOf(0.000)));
        // System.out.println(NumberUtil.equals(BigDecimal.valueOf(0), BigDecimal.valueOf(-0.000)));

        System.out.println(NumberUtil.add(1, 2));
        System.out.println((double) 1 / 0 * 1 / 0);
    }


    @Test
    public void foo2() {
        // java.lang.NumberFormatException: Infinite or NaN
        // System.out.println(((Double) Double.NaN).doubleValue() == 0.0);
        // System.out.println(new BigDecimal(Double.POSITIVE_INFINITY));
        System.out.println(0 * Double.POSITIVE_INFINITY);
        System.out.println(0 * Double.NEGATIVE_INFINITY);
        System.out.println(0 * Double.NaN);
        System.out.println(Double.NaN * Double.NaN);
        // 1/0 * 9
        System.out.println(Double.POSITIVE_INFINITY * 9);
        System.out.println(Double.NEGATIVE_INFINITY * 9);
        System.out.println(Double.NEGATIVE_INFINITY * Double.POSITIVE_INFINITY);
        System.out.println(Double.NEGATIVE_INFINITY / Double.POSITIVE_INFINITY);
        System.out.println(12352656 / Double.POSITIVE_INFINITY);
        System.out.println(12352656 / -Double.POSITIVE_INFINITY);
    }


    @Test
    public void foo() {



    }


}
