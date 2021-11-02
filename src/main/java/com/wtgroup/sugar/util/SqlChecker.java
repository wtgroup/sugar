package com.wtgroup.sugar.util;

import cn.hutool.core.util.StrUtil;

import java.util.regex.Pattern;

public class SqlChecker {
    private static final Pattern VALID_IDENTIFIER_PATTERN_IN_MYSQL = Pattern.compile("[a-zA-Z0-9$_.]{1,256}");

    public SqlChecker() {
    }

    public static boolean isValidSqlIdentifier(String sqlIdentifier) {
        return !StrUtil.isEmpty(sqlIdentifier) && VALID_IDENTIFIER_PATTERN_IN_MYSQL.matcher(sqlIdentifier).matches();
    }
}