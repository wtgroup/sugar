package com.wtgroup.sugar.math;

import org.junit.Test;

public class NumborTest {

    @Test
    public void loose() {
        System.out.println(Numbor.Rule.LOOSE.apply(null).add((Number) null));
        System.out.println(Numbor.Rule.LOOSE.apply(null).div((Number) null));
        System.out.println(Numbor.Rule.LOOSE.apply(null).mul((Number) null));
        System.out.println(Numbor.Rule.LOOSE.apply(1).div((Number) 0));
        System.out.println(Numbor.Rule.LOOSE.apply(0).div((Number) 1));
        System.out.println(Numbor.Rule.LOOSE.apply(0).div((Number) 1).isZero());
        System.out.println(Numbor.Rule.LOOSE.apply(0).div((Number) 1).equals(0));
        System.out.println(Numbor.Rule.LOOSE.apply(Double.NaN).mul((Number) 1));
        System.out.println(Numbor.Rule.LOOSE.apply(Double.POSITIVE_INFINITY).mul((Number) 1));
    }
    @Test
    public void strict() {
        System.out.println(Numbor.Rule.STRICT.apply(null).add((Number) null));
        System.out.println(Numbor.Rule.STRICT.apply(null).div((Number) null));
        System.out.println(Numbor.Rule.STRICT.apply(null).mul((Number) null));
        System.out.println(Numbor.Rule.STRICT.apply(1).div((Number) 0));
        System.out.println(Numbor.Rule.STRICT.apply(0).div((Number) 1));
        System.out.println(Numbor.Rule.STRICT.apply(0).div((Number) 1).isZero());
        System.out.println(Numbor.Rule.STRICT.apply(0).div((Number) 1).equals(0));
        System.out.println(Numbor.Rule.STRICT.apply(Double.NaN).mul((Number) 1));
        System.out.println(Numbor.Rule.STRICT.apply(Double.POSITIVE_INFINITY).mul((Number) 1));
        System.out.println(Numbor.Rule.STRICT.apply(Double.NEGATIVE_INFINITY).mul((Number) 1));
        System.out.println(Numbor.Rule.STRICT.apply(Double.NEGATIVE_INFINITY).mul(Double.POSITIVE_INFINITY));
        System.out.println(Numbor.Rule.STRICT.apply(Double.NEGATIVE_INFINITY).mul(Double.NEGATIVE_INFINITY));
        System.out.println(Numbor.Rule.STRICT.apply(Double.NEGATIVE_INFINITY).div(Double.POSITIVE_INFINITY));
        System.out.println(Numbor.Rule.STRICT.apply(Double.NEGATIVE_INFINITY).div(Double.NEGATIVE_INFINITY));
        System.out.println(Numbor.Rule.STRICT.apply(0).div(0));
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
        // Numbor numbor = Numbors.LOOSE.get(null);
        // System.out.println(numbor.add(5));
        // Numbors of = Numbors.of(new Numbors.Rule(true, false, false));
        // System.out.println(of.get(0).div(0));
        // System.out.println(of.get(1).div(0));
        // System.out.println(Numbors.STRICT);
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