package com.wtgroup.sugar.operation;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void foo() {
        List<Integer> list1 = Arrays.asList(1, 2, 3, 4, 5, 6);
        Element<Integer> element = Element.of(list1);
        System.out.println(element.get(0));
        System.out.println(element.get(100));
        System.out.println(element.get(-11));

        String[] arr = {"a", "b", "c"};
        Element<String> element1 = Element.of(arr);
        System.out.println(element1.get(2));
        System.out.println(element1.get(100));
        System.out.println(element1.get(-11));
    }


}
