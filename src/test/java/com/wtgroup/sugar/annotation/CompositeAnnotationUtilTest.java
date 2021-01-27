package com.wtgroup.sugar.annotation;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

public class CompositeAnnotationUtilTest {

    @Test
    public void foo() {
        Field[] fields = AnnotatedBean.class.getDeclaredFields();
        for (Field field : fields) {
                B composite = CompositeAnnotationUtil.composite(B.class, field, false);
                // case1
                if (field.getName().equals("case1")) {
                    Assert.assertEquals("case1", "AAAAAAAAAAAAA.aaaaa", composite.a());
                    Assert.assertEquals("B.b", composite.b());
                }
                // case2
                else if (field.getName().equals("case2")) {
                    Assert.assertEquals("case2", "BBBBBBBBBBBBB.bbbbb", composite.a());
                    Assert.assertEquals("B.b", composite.b());
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


    @Test
    public void foo3() throws NoSuchFieldException {
        Field[] fields = AnnotatedBean.class.getDeclaredFields();
        for (Field field : fields) {
            B composite = CompositeAnnotationUtil.getNotNullCompositeAnnotation(B.class, field);
            // case1
            if (field.getName().equals("case1")) {
                Assert.assertEquals("case1", "AAAAAAAAAAAAA.aaaaa", composite.a());
                Assert.assertEquals("B.b", composite.b());
            }
            // case2
            else if (field.getName().equals("case2")) {
                Assert.assertEquals("case2", "BBBBBBBBBBBBB.bbbbb", composite.a());
                Assert.assertEquals("B.b", composite.b());
            }
        }
    }

    @Test
    public void foo4() throws NoSuchFieldException {
        Field case3 = AnnotatedBean.class.getDeclaredField("case3");
        // 获取没有的注解的 "组合" 实例
        C composite = CompositeAnnotationUtil.getNotNullCompositeAnnotation(C.class, case3);
        System.out.println(composite);
    }


    @Test
    public void fun() throws NoSuchFieldException {
        Field case3 = AnnotatedBean.class.getDeclaredField("case3");
        A a = case3.getAnnotation(A.class);
        String a1 = a.a();
        System.out.println(a1);
        // sun.reflect.annotation.AnnotationInvocationHandler;
    }


}
