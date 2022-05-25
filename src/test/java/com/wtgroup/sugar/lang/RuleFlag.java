package com.wtgroup.sugar.lang;

/**
 * @author L_J
 * @date 2021/10/23 3:56
 */
public class RuleFlag extends AbstractFlag {

    public static final int IGNORE_NULL = 1;
    public static final int IGNORE_INFINITY = 1 << 1;
    public static final int IGNORE_NAN = 1 << 2;
    public static final int NULL0 = 1 << 3;
    public static final int INFINITY0 = 1 << 4;
    public static final int NAN0 = 1 << 5;

    public RuleFlag(int flags) {
        super(flags);
        if (has(IGNORE_NULL) && has(NULL0)) {
            remove(NULL0);
        }
        if (has(IGNORE_INFINITY) && has(INFINITY0)) {
            remove(INFINITY0);
        }
        if (has(IGNORE_NAN) && has(NAN0)) {
            remove(NAN0);
        }
    }
}
