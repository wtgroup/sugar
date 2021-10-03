package com.wtgroup.sugar.stopwatch;

import lombok.SneakyThrows;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class SpeedStatorTest {

    @SneakyThrows
    @Test
    public void demo() {
        SpeedStator speedStator = new SpeedStator("ss-demo", (momentInfo -> {
            System.out.println(momentInfo);
        }));

        speedStator.start();

        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    speedStator.log();
                    try {
                        TimeUnit.MILLISECONDS.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


        Thread.currentThread().join();
    }


}