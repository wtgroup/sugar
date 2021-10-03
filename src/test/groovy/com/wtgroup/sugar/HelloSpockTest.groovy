package com.wtgroup.sugar

import spock.lang.Specification

class HelloSpockTest extends Specification {

    def "hello spock"() {
        expect:
        (a == b) == res
        where:
        a | b || res
        1 | 2 || false
        2 | 2 || true
    }

}
