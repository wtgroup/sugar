package com.wtgroup.sugar.unit;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import org.junit.Test;

import java.time.Duration;

public class ByterTest {

    public static void main(String[] args) {
        System.out.println(Byter.ofB(1099511676527776L));
        System.out.println(Byter.ofGB(2).toKB());
        System.out.println(Byter.ofEB(5).toPB());
        System.out.println(Byter.ofKB((long) Byter.ofGB(2).toKB()));
        System.out.println(Byter.ofPB((long) Byter.ofEB(5).toPB()));
        System.out.println(Byter.ofB(3263737L));
    }

    @Test
    public void foo() {
        System.out.println(Duration.ofMillis(1500));
        System.out.println(Duration.ofSeconds(1520).toString());
        System.out.println(Duration.ofSeconds(1520));
        System.out.println(DateUtil.formatBetween(1098L, BetweenFormatter.Level.DAY));
        System.out.println(DateUtil.formatBetween(1098L, BetweenFormatter.Level.SECOND));
        System.out.println(DateUtil.formatBetween(1098L, BetweenFormatter.Level.MILLISECOND));
    }


}
