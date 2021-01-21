package com.wtgroup.sugar.reflect;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 支持序列化的 Function
 *
 * @author miemie
 * @since 2018-05-12
 */
public interface Fn<T, R> extends Function<T, R>, Serializable {
}
