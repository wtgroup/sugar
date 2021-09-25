package com.wtgroup.sugar.collection;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class CollDiffTest extends TestCase {

    public void testOf() {

        HashSet<String> set = new HashSet<String>() {{
            add("6");
            add("7");
        }};
        // CollDiff<List<String>, List<String>, String> diff = CollDiff.of(Arrays.asList("1", "2", "3"), Arrays.asList("3", "4"), t -> t);
        CollDiff<List<String>, Collection<String>, String> diff = CollDiff.of(Arrays.asList("1", "2", "3"), set);

        System.out.println(diff.getCollection1());
        System.out.println(diff.getCollection2());
        System.out.println(diff.getIntersection());
        System.out.println(diff.getOnlyCollection1());
        System.out.println(diff.getOnlyCollection2());

    }
}