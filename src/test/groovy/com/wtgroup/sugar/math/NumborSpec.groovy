package com.wtgroup.sugar.math


import spock.lang.Specification
import spock.lang.Unroll

/**
 * null 值运算参见 com.wtgroup.sugar.math.NumborTest , groovy null 无法定位重载方法
 */
class NumborSpec extends Specification {

    static Number nil = null

    def setup() {
    }

    def "demo"() {
        expect:
        println 3.0 - 2.0
        println 3.0 - 2.0 == 1.0

        Numbor numbor = new Numbor(1);
        println "new Numbor(1) => " + numbor
        Numbor numbor2 = Numbor.Rule.LOOSE.apply(1);
        println "Numbor.Rule.LOOSE.apply(1) => " + numbor2
        println "Numbor.Rule.LOOSE.apply(null) => " + Numbor.Rule.LOOSE.apply(null)
        println "Numbor.Rule.STRICT.apply(null) => " + Numbor.Rule.STRICT.apply(null)
        Numbor.Rule nullAsZero = Numbor.Rule.builder().nullAsZero(true).build();
        System.out.println(nullAsZero.isInfinityAsZero());
        Numbor n2 = nullAsZero.apply(1);
        System.out.println(n2);
        n2.equals(1)
    }


    def "宽松模式 add"() {
        def res = Numbor.Rule.LOOSE.apply(n1).add(n2)
        println res
        expect:
        res.equals(r)
        new Numbor(n1, Numbor.Rule.LOOSE).add(n2).get().intValue() == expectResult

        where:
        n1   | n2  | r              || expectResult
        9    | 2   | new Numbor(11) || 11
        null | 0   | new Numbor(0)  || 0
        3    | 0   | new Numbor(3)  || 3
        3.0  | 1.0 | new Numbor(4)  || 4
    }

    def "宽松模式 sub"() {
        def res = Numbor.Rule.LOOSE.apply(n1).sub(n2)
        println res
        expect:
        res.equals(r)
        new Numbor(n1, Numbor.Rule.LOOSE).sub(n2).get().intValue() == expectResult

        where:
        n1   | n2  | r             || expectResult
        9    | 2   | new Numbor(7) || 7
        null | 0   | new Numbor(0) || 0
        3    | 0   | new Numbor(3) || 3
        3.0  | 1.0 | new Numbor(2) || 2
    }

    @Unroll
    def "宽松模式 div n1=#n1, n2=#n2"() {
        def res = Numbor.Rule.LOOSE.apply(n1).div(n2)
        println res
        expect:
        res.equals(r)
        new Numbor(n1, Numbor.Rule.LOOSE).div(n2).get().intValue() == expectResult

        where:
        n1   | n2  | r                 || expectResult
        9    | 2   | new Numbor(9 / 2) || 4
        null | 0   | new Numbor(0)     || 0
        3    | 0   | new Numbor(0)     || 0
        3.0  | 1.0 | new Numbor(3)     || 3
    }

    @Unroll
    def "严格模式 div n1=#n1, n2=#n2"() {
        def res = Numbor.Rule.STRICT.apply(n1).div(n2)
        println res
        expect:
        res.equals(r)
        new Numbor(n1, Numbor.Rule.STRICT).div(n2).orElse(0).intValue() == expectResult

        where:
        n1   | n2 | r                                    || expectResult
        9    | 2  | new Numbor(9 / 2)                    || 4
        null | 0  | Numbor.EMPTY                         || 0
        3    | 0  | new Numbor(Double.POSITIVE_INFINITY) || Integer.MAX_VALUE
        -3   | 0  | new Numbor(Double.NEGATIVE_INFINITY) || Integer.MIN_VALUE
        0    | 0  | new Numbor(Double.NaN)               || 0 // NaN.intValue => 0
    }


}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme