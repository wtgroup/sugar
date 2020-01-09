package com.wtgroup.sugar.reflect;

import org.junit.Test;

import com.wtgroup.sugar.bean.User;

public class LambdaUtilsTest {

    @Test
    public void foo1() {
        System.out.println(LambdaUtils.fieldName(User::getI_am_xia_Huaxian));
    }


    @Test
    public void fnToFieldName() {
        String s = LambdaUtils.fieldName(User::getUname);
        System.out.println(s);
        String s1 = LambdaUtils.fieldName(User::getAge);
        System.out.println(s1);
        System.out.println(LambdaUtils.fieldName(User::getTags));
        System.out.println(LambdaUtils.fieldName(User::isMale));
    }


    @Test
    public void foo2() {


    }



}