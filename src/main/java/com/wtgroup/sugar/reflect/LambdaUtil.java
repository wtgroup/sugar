package com.wtgroup.sugar.reflect;

import com.wtgroup.sugar.function.SFunction;

import java.lang.invoke.SerializedLambda;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LambdaUtils
 * <p>
 * 遵循 Java 的字段, getter, setter 命名规范.
 * 错误示例: private boolean isMale, 应为 private boolean male.
 * ! 特殊格式的字段名少用, 如若需要, 请确保符合预期 !
 * </p>
 * <p>
 * -- 2021年10月3日 --
 * 只管 lambda 相关事宜, 将字段名逻辑分离出去.
 * <p>
 * -- 2021年1月12日 --
 * Origin 类的属性改为 final .
 * <p>
 * -- v1.0 2020年7月23日 --
 * 提高易用性, 增加可随时转换变量风格的方法.
 * <p>
 * -- 0.1 --
 *
 * @author dafei
 * @version 1.0
 * @date 2019/11/29 15:44
 */
public class LambdaUtil {
    /**
     * SerializedLambda 反序列化缓存
     */
    private static final Map<Class<?>, WeakReference<SerializedLambda>> FUNC_CACHE = new ConcurrentHashMap<>();
    private static final String PREFIX_IS = "is";
    private static final String PREFIX_GET = "get";
    private static final String PREFIX_SET = "set";
    private static final String WRITE_REPLACE = "writeReplace";


    /**
     * Get SerializedLambda
     * <p>
     * Q: Function 不行, 自己定义的 Fn 就可以<br>
     * A: 关键是 Serializable 接口, 才有 writeReplace 方法.
     * <p>
     * 如果一个序列化类中含有Object writeReplace()方法，那么实际序列化的对象将是作为 writeReplace 方法返回值的对象，
     * 而且序列化过程的依据是该返回对象的序列化实现。
     * 就是说, A.writeReplace return B, 那么序列化 A 时, 实际序列化的将是 B . 和 A 无关.
     * 正式 "替换写" 的语义.
     * 这样, Fn 作为 Lambda , (1)存在 writeReplace 方法, (2)该方法返回 SerializedLambda .
     * 故, 拿到该方法, 进而可以去到字段名(Lambda元数据之一).
     *
     * @param fn Lambda
     */
    public static <T> SerializedLambda getSerializedLambda(SFunction<T, ?> fn) {
        // User::getName 和 User::getAge , class 不同
        Class<? extends SFunction> clazz = fn.getClass();
        if (!clazz.isSynthetic()) {
            throw new IllegalArgumentException("SFunction class must be synthetic");
        }
        return Optional.ofNullable(FUNC_CACHE.get(clazz))
                .map(WeakReference::get)
                .orElseGet(() -> {
                    SerializedLambda lambda = null;
                    try {
                        Method method = clazz.getDeclaredMethod(WRITE_REPLACE);
                        method.setAccessible(Boolean.TRUE);
                        lambda = (SerializedLambda) method.invoke(fn);
                        method.setAccessible(Boolean.FALSE);
                    } catch (Exception e) {
                        throw new RuntimeException("method `writeReplace` call fail, get SerializedLambda of `" + clazz.getName() + "` fail");
                    }
                    FUNC_CACHE.put(clazz, new WeakReference<>(lambda));
                    return lambda;
                });
    }


}
