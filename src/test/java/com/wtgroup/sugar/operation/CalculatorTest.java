package com.wtgroup.sugar.operation;

import org.junit.Test;

import static org.junit.Assert.*;

public class CalculatorTest {

    @Test
    public void foo1() {
        Calculator c = Calculator.create();
        // 3 * 5 + (4 + 6) / 8
        Calculator.Num res = c.of(3).mul(5).add(c.of(4).add(6).div(8));
        System.out.println(res.get());

        Integer a = null;
        // 根据默认规则, null-->0, 结果会是 0
        System.out.println(c.of(null).sub(a).get());
    }

    @Test
    public void foo2() {
        Calculator c = Calculator.notNullAsZero();
        // 或
        // Calculator c = Calculator.create(Calculator.Rule.NOT_NULL_AS_ZERO);

        Number res1 = c.of(null).sub(9).orElse(9999); // 预期 9999
        System.out.println(res1);

        Number res2 = c.of(null).sub(9).get(); // 根据规则, 结果 null, 使用 get() 会抛异常
        System.out.println(res2);

    }



    @Test
    public void create() {
        Calculator c = Calculator.create();
        System.out.println(c);
    }

    @Test
    public void notNullAsZero() {
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