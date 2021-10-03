package com.wtgroup.sugar.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 支持序列化的 Function
 *
 * @author miemie
 * @since 2018-05-12
 */
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}
