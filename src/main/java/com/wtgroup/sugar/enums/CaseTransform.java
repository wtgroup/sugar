package com.wtgroup.sugar.enums;

import com.google.common.base.CaseFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@link CaseFormat} 枚举类组合简化
 *
 * 注: 部分常用的, 需要的再添加.
 * @author L&J
 * @date 2021-12-15 16:45:46
 */
@Getter
@AllArgsConstructor
public enum CaseTransform {
    LC2LU(CaseFormat.LOWER_CAMEL, CaseFormat.LOWER_UNDERSCORE),
    LC2LH(CaseFormat.LOWER_CAMEL, CaseFormat.LOWER_HYPHEN),
    LC2UC(CaseFormat.LOWER_CAMEL, CaseFormat.UPPER_CAMEL),
    LC2UU(CaseFormat.LOWER_CAMEL, CaseFormat.UPPER_UNDERSCORE),
    LU2LC(CaseFormat.LOWER_UNDERSCORE, CaseFormat.LOWER_CAMEL),
    LU2LH(CaseFormat.LOWER_UNDERSCORE, CaseFormat.LOWER_HYPHEN),
    LU2UC(CaseFormat.LOWER_UNDERSCORE, CaseFormat.UPPER_CAMEL),
    UC2LC(CaseFormat.UPPER_CAMEL, CaseFormat.LOWER_CAMEL),
    UC2LU(CaseFormat.UPPER_CAMEL, CaseFormat.LOWER_UNDERSCORE),
    UC2LH(CaseFormat.UPPER_CAMEL, CaseFormat.LOWER_HYPHEN),
    UC2UU(CaseFormat.UPPER_CAMEL, CaseFormat.UPPER_UNDERSCORE),
    ;
    private final CaseFormat from;
    private final CaseFormat to;

}