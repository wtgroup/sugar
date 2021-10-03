package com.wtgroup.sugar.reflect;

import com.google.common.base.CaseFormat;
import com.wtgroup.sugar.bean.User;
import org.junit.Assert;
import org.junit.Test;

public class FieldNameUtilTest {

    @Test
    public void testGet() throws Exception {
        String result = FieldNameUtil.get(User::getHelloFieldNameUtils);
        Assert.assertEquals("helloFieldNameUtils", result);
        String r2 = FieldNameUtil.get(User::getI_am_xia_Huaxian);
        Assert.assertEquals("i_am_xia_Huaxian", r2);
    }

    @Test
    public void testGet2() throws Exception {
        String result = FieldNameUtil.get(User::getHelloFieldNameUtils, CaseFormat.LOWER_HYPHEN);
        Assert.assertEquals("hello-field-name-utils", result);
    }

    @Test
    public void testGet3() throws Exception {
        // String result = FieldNameUtil.get(null, CaseFormat.LOWER_HYPHEN, CaseFormat.LOWER_HYPHEN);
        // Assert.assertEquals("replaceMeWithExpectedResult", result);
        String result1 = FieldNameUtil.get(User::getI_am_xia_Huaxian, CaseFormat.LOWER_UNDERSCORE, CaseFormat.LOWER_HYPHEN);
        Assert.assertEquals("i-am-xia-Huaxian", result1);
    }

    @Test
    public void testFromAccessor() throws Exception {
        String result = FieldNameUtil.fromAccessor("getName");
        Assert.assertEquals("name", result);
        String result1 = FieldNameUtil.fromAccessor("setIsBool");
        Assert.assertEquals("isBool", result1);
        String result2 = FieldNameUtil.fromAccessor("getURL");
        Assert.assertEquals("URL", result2);
        String result3 = FieldNameUtil.fromAccessor("setuName");
        Assert.assertEquals("uName", result3);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme