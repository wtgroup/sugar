package com.wtgroup.sugar.stopwatch;

import lombok.SneakyThrows;
import org.junit.Assert;

import java.util.concurrent.CountDownLatch;

public class SpeedStatorTest {

    @SneakyThrows
    // @Test
    public void demo() {
        // SpeedStator speedStator = new SpeedStator("ss-demo", (momentInfo, ex) -> {
        //     System.out.println(momentInfo + "\n" + ex);
        // });
        SpeedStator speedStator = new SpeedStator("ss-demo");

        // speedStator.start();


        int N = 50, M = 100;
        CountDownLatch countDownLatch = new CountDownLatch(N * M);

        for (int i = 0; i < N; i++) {
            new Thread(() -> {
                for (int j = 0; j < M; j++) {
                    speedStator.log("{}-----{}", 7979797, "红红火火恍恍惚惚或或或或或或或或或或或或或或或或或");
                    System.out.println(speedStator.getLatestMoment());
                    // try {
                    //     TimeUnit.MILLISECONDS.sleep(200);
                    // } catch (InterruptedException e) {
                    //     e.printStackTrace();
                    // }
                    countDownLatch.countDown();
                }
            }).start();
        }

        countDownLatch.await();
        speedStator.stop();
        Assert.assertEquals(N * M, speedStator.getHandledCount());

        /**
         * 100 * 1000
         * [ss-demo] STOP: 共处理 100000 条, 耗时 PT0.198S, TPS 50_5050.505/秒
         * 200 * 10000
         * [ss-demo] STOP: 共处理 2000000 条, 耗时 PT1.155S, TPS 173_1601.732/秒
         * 1000 * 10000
         * [ss-demo] STOP: 共处理 10000000 条, 耗时 PT4.998S, TPS 200_0800.320/秒
         * 10000 * 10000
         * [ss-demo] STOP: 共处理 100000000 条, 耗时 PT25.523S, TPS 391_8034.714/秒
         *
         * // 去 synchronized 后
         * 100 * 1000
         * [ss-demo] STOP: 共处理 100000 条, 耗时 PT0.093S, TPS 107_5268.817/秒
         * 100 * 10000
         * [ss-demo] STOP: 共处理 1000000 条, 耗时 PT0.257S, TPS 389_1050.584/秒
         * 200 * 10000
         * [ss-demo] STOP: 共处理 2000000 条, 耗时 PT0.425S, TPS 470_5882.353/秒
         * 1000 * 10000
         * [ss-demo] STOP: 共处理 10000000 条, 耗时 PT2.068S, TPS 483_5589.942/秒
         * 10000 * 10000
         * [ss-demo] STOP: 共处理 10000_0000 条, 耗时 PT17.803S, TPS 561_7030.837/秒
         *
         * 总结: tryLock 方式, 提升 2.5 倍
         */
    }


}