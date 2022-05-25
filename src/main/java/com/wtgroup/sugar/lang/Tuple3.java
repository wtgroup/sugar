package com.wtgroup.sugar.lang;

import cn.hutool.core.lang.Tuple;

/**
 * <p>
 *
 * @author L&J
 * @date 2021/10/31 2:30 下午
 */
public class Tuple3<T1, T2, T3> extends Tuple {

    public Tuple3(T1 first, T2 second, T3 third) {
        super(first, second, third);
    }

    public T1 getFirst() {
        return this.get(0);
    }

    public T2 getSecond() {
        return this.get(1);
    }

    public T3 getThird() {
        return this.get(2);
    }

}
