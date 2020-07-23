package com.wtgroup.sugar.reflect;

import com.google.common.base.CaseFormat;
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
        System.out.println(LambdaUtils.of(User::getHelloFieldNameUtils).to(CaseFormat.LOWER_UNDERSCORE));
        System.out.println(LambdaUtils.of(User::getHelloFieldNameUtils).toString());
        System.out.println(LambdaUtils.of(User::getI_am_xia_Huaxian).to(CaseFormat.UPPER_CAMEL));
        System.out.println(LambdaUtils.of(User::getI_am_xia_Huaxian).to(CaseFormat.UPPER_UNDERSCORE));
        System.out.println(LambdaUtils.of(User::getI_am_xia_Huaxian).toString());
        // hello_field_name_utils
        // helloFieldNameUtils
        // I_am_xia_Huaxian
        // I_AM_XIA__HUAXIAN
        // i_am_xia_Huaxian
    }



}
