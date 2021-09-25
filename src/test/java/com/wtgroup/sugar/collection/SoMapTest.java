package com.wtgroup.sugar.collection;

import junit.framework.TestCase;
import org.junit.Test;

public class SoMapTest extends TestCase {

    @Test
    public void testPuts() {
        SoMap soMap = new SoMap();
        soMap.puts("a", "b", 4, true, 6);
        System.out.println(soMap);
    }


}