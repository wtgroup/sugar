package com.wtgroup.sugar.stopwatch

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.TimeUnit


class StopWatchTest extends Specification {

    def setup() {

    }

    @Unroll
    def "demo"() {
        given:
        StopWatch stopWatch = new StopWatch("demo-stop-watch")
        stopWatch.start("起床")
        TimeUnit.SECONDS.sleep(3)
        stopWatch.stop()
        stopWatch.start("洗脸")
        TimeUnit.SECONDS.sleep(5)
        stopWatch.stop()
        stopWatch.start("吃早点")
        TimeUnit.SECONDS.sleep(8)
        stopWatch.stop()
        println stopWatch.shortSummary()
        println stopWatch.prettyString()
        println stopWatch.toString()

        expect:
        true
    }

    @Unroll
    def "is Running"() {
        given:
        StopWatch sw = new StopWatch("test is running")
        sw.start()
        expect:
        sw.isRunning()
    }


}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme