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
        Numbor numbor = new Numbor(1);
        println "new Numbor(1) => " + numbor
        Numbor numbor2 = Numbor.Rule.loose().apply(1);
        println "Numbor.Rule.loose().apply(1) => " + numbor2
        println "Numbor.Rule.loose().apply(null) => " + Numbor.Rule.loose().apply(null)
        println "Numbor.Rule.STRICT.apply(null) => " + Numbor.Rule.strict().apply(null)
        Numbor.Rule nullAsZero = Numbor.rule(Numbor.Rule.NULL_AS_0);
        System.out.println(nullAsZero.isInfinityAs0());
        Numbor n2 = nullAsZero.apply(1);
        System.out.println(n2);
        n2.equals(1)
    }


    def "宽松模式 add"() {
        def res = Numbor.Rule.loose().apply(n1).add(n2)
        println res
        expect:
        res.equals(r)
        new Numbor(n1, Numbor.Rule.loose()).add(n2).get().intValue() == expectResult

        where:
        n1   | n2  | r              || expectResult
        9    | 2   | new Numbor(11) || 11
        null | 0   | new Numbor(0)  || 0
        3    | 0   | new Numbor(3)  || 3
        3.0  | 1.0 | new Numbor(4)  || 4
    }

    def "宽松模式 sub"() {
        def res = Numbor.Rule.loose().apply(n1).sub(n2)
        println res
        expect:
        res.equals(r)
        new Numbor(n1, Numbor.Rule.loose()).sub(n2).get().intValue() == expectResult

        where:
        n1   | n2  | r             || expectResult
        9    | 2   | new Numbor(7) || 7
        null | 0   | new Numbor(0) || 0
        3    | 0   | new Numbor(3) || 3
        3.0  | 1.0 | new Numbor(2) || 2
    }

    @Unroll
    def "宽松模式 div n1=#n1, n2=#n2"() {
        def res = Numbor.Rule.loose().apply(n1).div(n2)
        println res
        expect:
        res.equals(r)
        new Numbor(n1, Numbor.Rule.loose()).div(n2).get().intValue() == expectResult

        where:
        n1   | n2  | r                 || expectResult
        9    | 2   | new Numbor(9 / 2) || 4
        null | 0   | new Numbor(0)     || 0
        3    | 0   | new Numbor(0)     || 0
        3.0  | 1.0 | new Numbor(3)     || 3
    }

    @Unroll
    def "严格模式 div n1=#n1, n2=#n2"() {
        def res = Numbor.Rule.strict().apply(n1).div(n2)
        println res
        expect:
        res.equals(r)
        new Numbor(n1, Numbor.Rule.strict()).div(n2).orElse(0).intValue() == expectResult

        where:
        n1   | n2 | r                                    || expectResult
        9    | 2  | new Numbor(9 / 2)                    || 4
        null | 0  | Numbor.EMPTY                         || 0
        3    | 0  | new Numbor(Double.POSITIVE_INFINITY) || Integer.MAX_VALUE
        -3   | 0  | new Numbor(Double.NEGATIVE_INFINITY) || Integer.MIN_VALUE
        0    | 0  | new Numbor(Double.NaN)               || 0 // NaN.intValue => 0
    }

    @Unroll
    def "规则 ignore: n1=#n1, n2=#n2, n3=#n3"() {
        def res = Numbor.Rule.strict().apply(n1).div(n2)
        println res
        expect:
        Numbor.rule(Numbor.Rule.IGNORE_NULL).apply(n1).mul(n2).equals(r1)
        Numbor.rule(Numbor.Rule.IGNORE_INFINITY | Numbor.Rule.IGNORE_NAN).apply(n1).mul(n2).equals(r2)

        where:
        n1                       | n2 || r1                       | r2
        null                     | 2  || 2                        | Numbor.EMPTY
        null                     | 0  || 0                        | Numbor.EMPTY
        Double.NaN               | 10 || Double.NaN               | 10
        Double.POSITIVE_INFINITY | 10 || Double.POSITIVE_INFINITY | 10
    }

    @Unroll
    def "取结果 n1=#n1 n2=#n2"() {
        expect:
        Numbor.rule(Numbor.Rule.NAN_AS_0 | Numbor.Rule.INFINITY_AS_0).apply(n1).div(n2).isValid()
        !new Numbor(n1).div(n2).isValid()
        println "invalid orElse: " + new Numbor(n1).div(n2).orElse(9999)

        where:
        n1 | n2 | n3
        0  | 0  | 0
        1  | 0  | 0

    }

    @Unroll
    def "compareTo n1=#n1 n2=#n2"() {
        expect:
        new Numbor(n1).compareTo(new Numbor(n2)) == r

        where:
        n1                       | n2                       | r
        0                        | 0                        | 0
        1                        | 0                        | 1
        null                     | null                     | 0
        null                     | 1                        | 1
        Double.POSITIVE_INFINITY | 1                        | 1
        Double.POSITIVE_INFINITY | Double.NEGATIVE_INFINITY | 1
        Double.NaN               | Double.NEGATIVE_INFINITY | 1
    }


}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme