package com.wtgroup.sugar.math


import spock.lang.Specification

class NumborSpec extends Specification {

    def setup() {
    }

    def "宽松模式"() {
        expect:
        Numbors.LOOSE.get(n1).div(n2).mul(n3).orElse(null) == expectResult

        where:
        n1   | n2 | n3 || expectResult
        9    | 3  | 3  || 9
        null | 0  | 0  || 0
        3    | 0  | 0  || 0

    }

    def "严格模式"() {
        expect:
        Numbors.STRICT.get(n1).div(n2).mul(n3).orElse(null) == expectResult

        where:
        n1   | n2 | n3 || expectResult
        9    | 3  | 3  || 9
        null | 0  | 0  || null
        3    | 0  | 0  || null()

    }


}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme