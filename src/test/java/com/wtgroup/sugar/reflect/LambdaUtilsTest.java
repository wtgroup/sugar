package com.wtgroup.sugar.reflect;

import com.google.common.base.CaseFormat;
import org.junit.Test;

import com.wtgroup.sugar.bean.User;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

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

    @Test
    public void foo3() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Fn<User, Integer> fn = User::getAge;
        Class<? extends Fn> clazz = fn.getClass();
        Method method = clazz.getDeclaredMethod("writeReplace");
        method.setAccessible(Boolean.TRUE);
        SerializedLambda lambda = (SerializedLambda) method.invoke(fn);
        method.setAccessible(Boolean.FALSE);
        System.out.println(lambda.getImplMethodName());

        /**
         * Q: Function 不行, 自己定义的 Fn 就可以
         * A: 关键是 Serializable 接口, 才有 writeReplace 方法.
         *
         * 如果一个序列化类中含有Object writeReplace()方法，那么实际序列化的对象将是作为 writeReplace 方法返回值的对象，
         * 而且序列化过程的依据是该返回对象的序列化实现。
         * 就是说, A.writeReplace return B, 那么序列化 A 时, 实际序列化的将是 B . 和 A 无关.
         * 正式 "替换写" 的语义.
         * 这样, Fn 作为 Lambda , (1)存在 writeReplace 方法, (2)该方法返回 SerializedLambda .
         * 故, 拿到该方法, 进而可以去到字段名(Lambda元数据之一).
         */

    }




}
