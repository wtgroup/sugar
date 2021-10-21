package com.wtgroup.sugar.math;

import org.junit.Test;

public class NumborsTest {

    @Test
    public void of() {
        Numbor numbor = Numbors.LOOSE.get(null);
        System.out.println(numbor.add(5));
        Numbors of = Numbors.of(new Numbors.Rule(true, false, false));
        System.out.println(of.get(0).div(0));
        System.out.println(of.get(1).div(0));
        System.out.println(Numbors.STRICT);
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