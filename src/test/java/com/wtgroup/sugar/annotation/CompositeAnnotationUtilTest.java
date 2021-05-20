package com.wtgroup.sugar.annotation;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

public class CompositeAnnotationUtilTest {
    /*
    A
    <-composite- B
    <-composite- C
     */

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
        // B composite A , 假设 A 注解是我们无法修改的, 搞一个 B 来增强功能, 组合复用 A 已有属性设计
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


    @Test
    public void foo5() throws NoSuchFieldException {
        Field case1 = AnnotatedBean.class.getDeclaredField("case1");
        A composite1 = CompositeAnnotationUtil.compositeUpcast(A.class, B.class, case1, false);
        System.out.println(composite1); // A(a=AAAAAAAAAAAAA.aaaaa)

        Field case2 = AnnotatedBean.class.getDeclaredField("case2");
        A composite2 = CompositeAnnotationUtil.compositeUpcast(A.class, B.class, case2, false);
        System.out.println(composite2); // A(a=BBBBBBBBBBBBB.bbbbb), 被 B.a() "覆写"了

        Field case4 = AnnotatedBean.class.getDeclaredField("case4");
        A a = CompositeAnnotationUtil.compositeUpcast(A.class, C.class, case4, false);
        System.out.println(a); // 预期 null
        A a1 = CompositeAnnotationUtil.compositeUpcast(A.class, C.class, case4, true);
        System.out.println(a1); // A(a=A.a) , A.a() 的默认值
        B b = CompositeAnnotationUtil.compositeUpcast(B.class, C.class, case4, true);
        System.out.println(b); // B(a=B.a, b=C.b) , B.b 被 C.b() "覆写" 了
    }

    @Test
    public void foo6() throws NoSuchFieldException {
        Field case4 = AnnotatedBean.class.getDeclaredField("case4");
        A a = CompositeAnnotationUtil.getUpcastCompositeAnnotation(A.class, C.class, case4);
        System.out.println(a);
        A a1 = CompositeAnnotationUtil.getNotNullUpcastCompositeAnnotation(A.class, C.class, case4);
        System.out.println(a1);

        B b = CompositeAnnotationUtil.getNotNullUpcastCompositeAnnotation(B.class, C.class, case4);
        System.out.println(b);
    }


}
