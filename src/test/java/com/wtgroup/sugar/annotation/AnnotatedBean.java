package com.wtgroup.sugar.annotation;

/**
 * @author dafei
 * @version 0.1
 * @date 2021/1/27 0:35
 */
public class AnnotatedBean {

    // 预期: B.a == "AAAAAAAAAAAAA.aaaaa"
    @B(b = "B.b")
    @A(a = "AAAAAAAAAAAAA.aaaaa")
    private String case1;

    // 预期: B.a == "BBBBBBBBBBBBB.bbbbb"
    @B(a = "BBBBBBBBBBBBB.bbbbb", b="B.b")
    @A(a = "AAAAAAAAAAAAA.aaaaa")
    private Integer case2;

}
