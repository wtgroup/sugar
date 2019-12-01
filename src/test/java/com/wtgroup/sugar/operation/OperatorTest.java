package com.wtgroup.sugar.operation;

import org.junit.Test;

import java.math.RoundingMode;

import static org.junit.Assert.*;

public class OperatorTest {

    @Test
    public void round() {
        // (5*6 + 7) / 8
        System.out.println((5*6 + 7) / 8.0);
        Operator a = Operator.ofNullable(5).mul(6).add(7).div(8).round(1, null);

        System.out.println(a);

    }


    @Test
    public void foo1() {

        // // (5*6 + 7) / 8
        // System.out.println((5*6 + 7) / 8);
        // Operator a = Operator.ofNullable(5).mul(6).add(7); //.div(8);
        // System.out.println(a);
        // System.out.println(a.get());
        // Operator.Rule rule = new Operator.Rule().setInfinityAsZero(false);
        // Operator b = Operator.ofNullable(5, rule).mul(6).add(7).div(null);
        // System.out.println(b);
        // System.out.println(b.orElse(90));

        System.out.println(0+8-4.555667);
        System.out.println(Operator.ofNullable(null).add(8).sub(4.555667));

    }


}