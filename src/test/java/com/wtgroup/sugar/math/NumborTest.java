package com.wtgroup.sugar.math;

import org.junit.Test;

import java.util.Arrays;

public class NumborTest {

    @Test
    public void loose() {
        System.out.println(Numbor.Rule.loose().apply(null).add((Number) null));
        System.out.println(Numbor.Rule.loose().apply(null).div((Number) null));
        System.out.println(Numbor.Rule.loose().apply(null).mul((Number) null));
        System.out.println(Numbor.Rule.loose().apply(1).div((Number) 0));
        System.out.println(Numbor.Rule.loose().apply(0).div((Number) 1));
        System.out.println(Numbor.Rule.loose().apply(0).div((Number) 1).isZero());
        System.out.println(Numbor.Rule.loose().apply(0).div((Number) 1).equals(0));
        System.out.println(Numbor.Rule.loose().apply(Double.NaN).mul((Number) 1));
        System.out.println(Numbor.Rule.loose().apply(Double.POSITIVE_INFINITY).mul((Number) 1));
    }
    @Test
    public void strict() {
        System.out.println(Numbor.Rule.strict().apply(null).add((Number) null));
        System.out.println(Numbor.Rule.strict().apply(null).div((Number) null));
        System.out.println(Numbor.Rule.strict().apply(null).mul((Number) null));
        System.out.println(Numbor.Rule.strict().apply(1).div((Number) 0));
        System.out.println(Numbor.Rule.strict().apply(0).div((Number) 1));
        System.out.println(Numbor.Rule.strict().apply(0).div((Number) 1).isZero());
        System.out.println(Numbor.Rule.strict().apply(0).div((Number) 1).equals(0));
        System.out.println(Numbor.Rule.strict().apply(Double.NaN).mul((Number) 1));
        System.out.println(Numbor.Rule.strict().apply(Double.POSITIVE_INFINITY).mul((Number) 1));
        System.out.println(Numbor.Rule.strict().apply(Double.NEGATIVE_INFINITY).mul((Number) 1));
        System.out.println(Numbor.Rule.strict().apply(Double.NEGATIVE_INFINITY).mul(Double.POSITIVE_INFINITY));
        System.out.println(Numbor.Rule.strict().apply(Double.NEGATIVE_INFINITY).mul(Double.NEGATIVE_INFINITY));
        System.out.println(Numbor.Rule.strict().apply(Double.NEGATIVE_INFINITY).div(Double.POSITIVE_INFINITY));
        System.out.println(Numbor.Rule.strict().apply(Double.NEGATIVE_INFINITY).div(Double.NEGATIVE_INFINITY));
        System.out.println(Numbor.Rule.strict().apply(0).div(0));
    }

    @Test
    public void 特殊计算组合() {
        Number[] nums = {0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN};
        for (Number a : nums) {
            for (Number b : nums) {
                System.out.printf("%-10.3f", (a.doubleValue() / b.doubleValue()));
            }
            System.out.println();
        }
    }


    @Test
    public void of() {
        // 默认
        Numbor numbor = new Numbor(1);
        // 宽松
        Numbor.Rule.loose().apply(1);
        // new 时指定规则
        Number number = new Numbor(1, Numbor.Rule.NULL_AS_0 | Numbor.Rule.INFINITY_AS_0).get();
        // 预备规则, 复用
        Numbor.Rule rule = Numbor.rule(Numbor.Rule.NULL_AS_0 | Numbor.Rule.INFINITY_AS_0);
        // 复用
        rule.apply(1).add(2).get();
        rule.apply(-99).div(9).mul(8).orElse(0);
        // 复用
        Numbor num = new Numbor(1, rule);
        Arrays.asList(
                numbor,
                number,
                rule,
                num,
                Numbor.Rule.loose()
        ).forEach(System.out::println);
    }

    @Test
    public void get() {
    }

    @Test
    public void empty() {
    }

    @Test
    public void testToString() {
    }
}