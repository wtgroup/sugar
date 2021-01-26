package com.wtgroup.sugar.annotation;

import cn.hutool.core.comparator.CompareUtil;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class CompositeAnnotationUtilTest {

    @Test
    public void foo() {
        Field[] fields = AnnotatedBean.class.getDeclaredFields();
        for (Field field : fields) {
            B b = field.getAnnotation(B.class);
            if (b!=null) {
                B composite = CompositeAnnotationUtil.composite(b, field);
                // case1
                if (field.getName().equals("case1")) {
                    Assert.assertEquals("case1", "AAAAAAAAAAAAA.aaaaa", composite.a());
                    Assert.assertEquals("B.b", composite.b());
                } else if (field.getName().equals("case2")) {
                    Assert.assertEquals("case2", "BBBBBBBBBBBBB.bbbbb", composite.a());
                    Assert.assertEquals("B.b", composite.b());
                }
            }
        }
    }

    @Test
    public void foo2() throws NoSuchFieldException {
        Field case1 = AnnotatedBean.class.getDeclaredField("case1");
        B composite = CompositeAnnotationUtil.getCompositeAnnotation(B.class, case1);
        Assert.assertEquals("case1", "AAAAAAAAAAAAA.aaaaa", composite.a());
        Assert.assertEquals("B.b", composite.b());
    }



}
