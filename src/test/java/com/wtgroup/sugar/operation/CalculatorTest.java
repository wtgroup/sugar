package com.wtgroup.sugar.operation;

import org.junit.Assert;
import org.junit.Test;

public class CalculatorTest {

    @Test
    public void foo1() {
        Calculator c = Calculator.LOOSE;
        // 3 * 5 + (4 + 6) / 8
        Calculator.Num res = c.exe(3).mul(5).add(c.exe(4).add(6).div(8));
        System.out.println(res.get());

        Integer a = null;
        // 根据默认规则, null-->0, 结果会是 0
        System.out.println(c.exe(null).sub(a).get());
    }

    // @Test
    // public void foo2() {
    //     Calculator c = Calculator.noNullAsZero();
    //     // 或
    //     // Calculator c = Calculator.create(Calculator.Rule.NOT_NULL_AS_ZERO);
    //
    //     Number res1 = c.of(null).sub(9).orElse(9999); // 预期 9999
    //     System.out.println(res1);
    //
    //     Number res2 = c.of(null).sub(9).get(); // 根据规则, 结果 null, 使用 get() 会抛异常
    //     System.out.println(res2);
    //
    // }

    @Test
    public void foo3() {
        Calculator clc = Calculator.of(Calculator.Rule.STRICT);
        clc.exe(0).div((Number)0).sub(1).ifPresent(e->{
            System.out.println(e);
        });
    }

    @Test
    public void notNullAsZero() {
        Calculator.Num res = Calculator.NO_NULL_AS_ZERO.exe(null).add(5);
        Assert.assertTrue(res.isNull());
    }

    @Test
    public void testCreate() {
    }

    @Test
    public void of() {
    }

    @Test
    public void empty() {
    }
}
