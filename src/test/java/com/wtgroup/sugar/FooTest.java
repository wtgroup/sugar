package com.wtgroup.sugar;

import org.junit.Test;

/**
 * @author dafei
 * @version 0.1
 * @date 2019/12/2 15:34
 */
public class FooTest {

    @Test
    public void foo2() {
        System.out.println(Integer.parseInt(""));
        System.out.println(Integer.parseInt("null"));
    }


    @Test
    public void foo() {

        String s = "為們jflsj내가 너야tgwsoj我图我听";
        System.out.println(s.length());
        for (char c : s.toCharArray()) {
            if (c>255) {
                System.out.println("双字节: "+ c);
            }else{
                System.out.println("单字节: "+ c);
            }
        }

    }


}
