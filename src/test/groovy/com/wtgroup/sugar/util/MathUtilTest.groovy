package com.wtgroup.sugar.util


import spock.lang.Specification
import spock.lang.Unroll

class MathUtilTest extends Specification {

    @Unroll
    def "toIntExact value=#value, defVal=#defVal, expect=#expect"() {
        expect:
        MathUtil.toIntExact(value, defVal) == expect
        where:
        value          | defVal | expect
        Long.MAX_VALUE | 99     | 99
        Long.MIN_VALUE | 99     | 99
        88             | 99     | 88
    }

}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme