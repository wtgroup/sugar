package com.wtgroup.sugar.unit;

public class ByterTest {

    public static void main(String[] args) {
        System.out.println(Byter.ofB(1099511676527776L));
        System.out.println(Byter.ofGB(2).toKB());
        System.out.println(Byter.ofEB(5).toPB());
    }

}
