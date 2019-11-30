package com.wtgroup.sugar.reflect;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  存遵循 Java 的字段, getter, setter 命名规范.
 *  错误示例: private boolean isMale, 应为 private boolean male
 * </p>
 *
 * @author dafei
 * @version 0.1
 * @date 2019/11/29 15:44
 */
public class LambdaUtils {
    /**
     * SerializedLambda 反序列化缓存
     */
    private static final Map<Class<?>, WeakReference<SerializedLambda>> FUNC_CACHE = new ConcurrentHashMap<>();
    public static final String PREFIX_IS = "is";
    public static final String PREFIX_GET = "get";
    public static final String PREFIX_SET = "set";
    public static final String WRITE_REPLACE = "writeReplace";


    public static <T> String fieldName(Fn<T, ?> fn) {
        // User::getName 和 User::getAge , class 不同
        Class<? extends Fn> clazz = fn.getClass();
        if (!clazz.isSynthetic()) {
            throw new IllegalArgumentException("该方法仅能传入 lambda 表达式产生的合成类");
        }

        SerializedLambda lambdaCache = getSerializedLambda(fn, clazz);

        String getter = lambdaCache.getImplMethodName();
        return methodToProperty(getter);
    }

    private static SerializedLambda getSerializedLambda(Fn<?, ?> fn, Class<? extends Fn> clazz) {
        return Optional.ofNullable(FUNC_CACHE.get(clazz))
                .map(WeakReference::get)
                .orElseGet(() -> {
                    SerializedLambda lambda = null;
                    try {
                        Method method = clazz.getDeclaredMethod(WRITE_REPLACE);
                        method.setAccessible(Boolean.TRUE);
                        lambda = (SerializedLambda) method.invoke(fn);
                    } catch (Exception e) {
                        throw new RuntimeException("method `writeReplace` call fail, get SerializedLambda of " + clazz.getName() + "fail");
                    }
                    FUNC_CACHE.put(clazz, new WeakReference<>(lambda));
                    return lambda;
                });
    }

    /**
     * 参考: org.apache.ibatis.reflection.property.PropertyNamer#methodToProperty
     * @param name
     * @return
     */
    public static String methodToProperty(String name) {
        if (name.startsWith(PREFIX_IS)) {
            name = name.substring(2);
        } else {
            if (!name.startsWith(PREFIX_GET) && !name.startsWith(PREFIX_SET)) {
                throw new RuntimeException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
            }

            name = name.substring(3);
        }

        // if (name.length() == 1 || name.length() > 1 && !Character.isUpperCase(name.charAt(1))) {
        //     name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        // }
        // 符合 Java getter setter 规范
        name = Introspector.decapitalize(name);
        return name;
    }


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
}
