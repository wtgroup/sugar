package com.wtgroup.sugar.reflect;

import com.google.common.base.CaseFormat;
import com.wtgroup.sugar.function.SFunction;

import java.beans.Introspector;

/**
 * @author L&J
 * @date 2021/10/3 15:50
 */
public class FieldNameUtil {

    private static final String PREFIX_IS = "is";
    private static final String PREFIX_GET = "get";
    private static final String PREFIX_SET = "set";

    public static <T> String get(SFunction<T, ?> getter) {
        String implMethodName = LambdaUtil.getSerializedLambda(getter).getImplMethodName();
        return fromAccessor(implMethodName);
    }

    public static <T> String get(SFunction<T, ?> accessor, CaseFormat toFormat) {
        return get(accessor, toFormat, CaseFormat.LOWER_CAMEL);
    }

    public static <T> String get(SFunction<T, ?> accessor, CaseFormat srcFormat, CaseFormat toFormat) {
        return srcFormat.to(toFormat, get(accessor));
    }


    /**
     * 参考: {@code org.apache.ibatis.reflection.property.PropertyNamer#methodToProperty}
     *
     * @param accessor
     */
    public static String fromAccessor(String accessor) {
        /*
        一般JavaBean属性以小写字母开头，驼峰命名格式，相应的 getter/setter 方法是 get/set 接上首字母大写的属性名。例如：属性名为userName，其对应的getter/setter 方法是 getUserName/setUserName。
        但是，还有一些特殊情况：
        1、如果属性名的第二个字母大写，那么该属性名直接用作 getter/setter 方法中 get/set 的后部分，就是说大小写不变。例如属性名为uName，方法是getuName/setuName。
        2、如果属性名的前两个字母是大写（一般的专有名词和缩略词都会大写），也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为URL，方法是getURL/setURL。
        3、如果属性名的首字母大写，也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为Name，方法是getName/setName，这种是最糟糕的情况，会找不到属性出错，因为默认的属性名是name。
        4、如果属性名以"is"开头，则getter方法会省掉get，set方法会去掉is。例如属性名为isOK，方法是isOK/setOK。
        需要注意的是有些开发工具自动生成的getter/setter方法，并没有考虑到上面所说的特例情况，会导致bug的产生。
        我们在定义JavaBean的属性名时，应该尽量避免属性名的头两个字母中任意一个为大写以及属性名以"is"开头。
        */
        if (accessor.startsWith(PREFIX_IS)) {
            accessor = accessor.substring(2);
        } else {
            if (!accessor.startsWith(PREFIX_GET) && !accessor.startsWith(PREFIX_SET)) {
                throw new RuntimeException("Error parsing property accessor '" + accessor + "'.  Didn't start with 'is', 'get' or 'set'.");
            }

            accessor = accessor.substring(3);
        }

        // if (accessor.length() == 1 || accessor.length() > 1 && !Character.isUpperCase(accessor.charAt(1))) {
        //     accessor = accessor.substring(0, 1).toLowerCase(Locale.ENGLISH) + accessor.substring(1);
        // }
        // 符合 Java getter setter 规范
        accessor = Introspector.decapitalize(accessor);
        return accessor;
    }
}
