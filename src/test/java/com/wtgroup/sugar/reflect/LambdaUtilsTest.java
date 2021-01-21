package com.wtgroup.sugar.reflect;

import com.google.common.base.CaseFormat;
import org.junit.Test;

import com.wtgroup.sugar.bean.User;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LambdaUtilsTest {

    @Test
    public void foo1() {
        System.out.println(LambdaUtil.fieldName(User::getI_am_xia_Huaxian));
    }


    @Test
    public void fnToFieldName() {
        String s = LambdaUtil.fieldName(User::getUname);
        System.out.println(s);                                      // => uname
        String s1 = LambdaUtil.fieldName(User::getAge);
        System.out.println(s1);                                     // => age
        System.out.println(LambdaUtil.fieldName(User::isMale));    // => male

        LambdaUtil.Origin lamb = LambdaUtil.lowerCamel(User::getHelloFieldNameUtils);
        System.out.println(lamb.getFieldName());                    // 原样 helloFieldNameUtils
        System.out.println(lamb.to(CaseFormat.UPPER_UNDERSCORE));   // => HELLO_FIELD_NAME_UTILS

    }


    @Test
    public void foo2() {
        System.out.println(LambdaUtil.lowerCamel(User::getHelloFieldNameUtils).to(CaseFormat.LOWER_UNDERSCORE)); // => hello_field_name_utils
        System.out.println(LambdaUtil.lowerCamel(User::getHelloFieldNameUtils).toString()); // => helloFieldNameUtils
        System.out.println(LambdaUtil.lowerCamelToLowerUnderscore(User::getHelloFieldNameUtils)); // => hello_field_name_utils
        System.out.println(LambdaUtil.of(User::getI_am_xia_Huaxian, CaseFormat.LOWER_UNDERSCORE).to(CaseFormat.UPPER_CAMEL)); // => IAmXiaHuaxian
        System.out.println(LambdaUtil.of(User::getI_am_xia_Huaxian, CaseFormat.LOWER_UNDERSCORE).to(CaseFormat.LOWER_CAMEL)); // => iAmXiaHuaxian
        System.out.println(LambdaUtil.lowerCamel(User::getI_am_xia_Huaxian).toString()); // => i_am_xia_Huaxian
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
