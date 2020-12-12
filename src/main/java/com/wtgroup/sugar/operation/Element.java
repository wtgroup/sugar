package com.wtgroup.sugar.operation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 *
 * 方便从集合或数组中取元素, 不用担心索引越界带来异常
 * @author dafei
 * @version 0.1
 * @date 2020/12/12 22:37
 */
public class Element<T> {

    private final Collection<T> elements;

    public static <T> Element<T> of(Collection<T> list) {
        assert list != null;
        Element<T> element = new Element<T>(list);
        return element;
    }

    public static <T> Element<T> of(T[] array) {

        return of(Arrays.asList(array));
    }

    private Element(Collection<T> elements) {
        this.elements = elements;
    }

    /**
     * 取元素
     * <p>
     * 无需担心索引越界带来异常
     *
     * @param ix
     * @return
     */
    public Optional<T> getOptional(int ix) {
        return Optional.ofNullable(this.get(ix));
    }

    public T get(int ix) {
        if (ix < 0 || ix >= elements.size()) {
            return null;
        }

        int i = 0;
        for (T element : elements) {
            if (i == ix) {
                return element;
            }
            i++;
        }

        return null;
    }
}
