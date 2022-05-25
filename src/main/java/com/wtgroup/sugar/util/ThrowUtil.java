package com.wtgroup.sugar.util;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.Arrays;

/**异常抛出工具
 * @author L&J
 * @date 2021-10-3 15:14:07
 */
@Slf4j
public class ThrowUtil {
    private static final RuntimeException STUB_EXCEPTION = new RuntimeException("ThrowUtil#_ default Exception instance");

    public static <T extends RuntimeException> T $(Class<T> exceptionType, @Nullable String msg, Object ...args) {
        if (exceptionType == null) {
            throw STUB_EXCEPTION;
        }
        T inst = null;
        String message = null;
        Throwable nestEx = null;
        if (args.length == 0) {
            message = msg;
        } else if (args[args.length - 1] instanceof Throwable) {
            nestEx = (Throwable) args[args.length - 1];
            args = Arrays.copyOf(args, args.length - 1);
        }
        if(message == null && msg != null) message = StrUtil.format(msg, args);

        try {
            Constructor<T> constructor;
            if (message == null && nestEx == null) {
                // 无参构造
                constructor = ReflectUtil.getConstructor(exceptionType);
                inst = constructor != null ? constructor.newInstance() : null;
            } else if (message == null) {
                constructor = ReflectUtil.getConstructor(exceptionType, Throwable.class);
                inst = constructor != null ? constructor.newInstance(nestEx) : null;
            } else if (nestEx == null) {
                constructor = ReflectUtil.getConstructor(exceptionType, String.class);
                inst = constructor != null ? constructor.newInstance(message) : null;
            } else {
                constructor = ReflectUtil.getConstructor(exceptionType, String.class, Throwable.class);
                inst = constructor != null ? constructor.newInstance(message, nestEx) : null;
            }
        } catch (Exception e) {
            log.error("hrowUtil#_ 异常: exceptionType: {}, msg: {}, args: {}", exceptionType, msg, Arrays.toString(args), e);
        }

        throw inst != null ? inst : STUB_EXCEPTION;
    }


}
