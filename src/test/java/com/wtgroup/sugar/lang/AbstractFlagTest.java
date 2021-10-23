package com.wtgroup.sugar.lang;

import org.junit.Test;

public class AbstractFlagTest {

    @Test
    public void testToString() {
        RuleFlag ruleFlag = new RuleFlag(RuleFlag.IGNORE_NULL | RuleFlag.NULL0 | RuleFlag.INFINITY0);
        System.out.println(ruleFlag);
        // ruleFlag.add(RuleFlag.IGNORE_NAN | RuleFlag.NULL0 | RuleFlag.INFINITY0);
        // System.out.println(ruleFlag);
        System.out.println(ruleFlag.has(RuleFlag.IGNORE_NULL));
        System.out.println(ruleFlag.has(RuleFlag.NULL0));
    }
}