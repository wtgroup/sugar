package com.wtgroup.sugar.util


import spock.lang.Specification
import spock.lang.Unroll


class ThrowUtilTest extends Specification {


    def setup() {

    }

    @Unroll
    def "_ where exceptionType=#exceptionType and msg=#msg and args=#args then expect: #expectedResult"() {
        expect:
        try {
            ThrowUtil.$(exceptionType, msg, args)
        } catch (Exception exception) {
            exception.message == expectedResult
        }

        where:
        exceptionType | msg   | args   || expectedResult
        IllegalArgumentException.class          | null | null || null
        RuntimeException.class          | "{}-{}-{}" | [1, "a", "b"].toArray() || "1-a-b"
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme