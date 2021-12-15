package com.wtgroup.sugar.util;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.CaseFormat;
import com.wtgroup.sugar.collection.SsMap;
import com.wtgroup.sugar.enums.CaseTransform;
import com.wtgroup.sugar.function.SFunction;
import com.wtgroup.sugar.reflect.FieldNameUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * @author L&J
 * @date 2021/10/18 2:25 下午
 */
public class OrderBy {
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";


    /**
     * list of (column, direction)
     */
    private final List<Tuple> orderByList = new ArrayList<>();
    private final Map<String, String> columnAliasMapping = new HashMap<>();
    /**
     * 决定字段风格转换规则, 默认 Low camel -> Low underscore.
     * 注: 在最终生成 SQL 片段时转换类名.
     */
    private CaseTransform caseTransform;

    public static OrderBy of() {
        return of(CaseTransform.LC2LU);
    }

    public static OrderBy of(CaseTransform caseTransform, String... columnAlias) {
        OrderBy orderBy = new OrderBy();
        if (columnAlias != null) {
            orderBy.configColumnAlias(columnAlias);
        }
        if (caseTransform != null) {
            orderBy.configCaseTransform(caseTransform);
        }
        return orderBy;
    }

    private OrderBy() {
        // private
    }

    /**
     * 配置列别名
     *
     * @param columnAlias 原名, 别名, 原名, 别名, ...
     */
    public OrderBy configColumnAlias(String... columnAlias) {
        return this.configColumnAlias(SsMap.of(columnAlias));
    }

    /**
     * 配置列别名
     *
     * @param columnAlias 原名, 别名, 原名, 别名, ...
     */
    public OrderBy configColumnAlias(Map<String, String> columnAlias) {
        this.columnAliasMapping.putAll(columnAlias);
        return this;
    }

    /**
     * 配置字段风格转换规则
     * @param caseTransform 风格转换规则
     */
    public OrderBy configCaseTransform(CaseTransform caseTransform) {
        this.caseTransform = caseTransform;
        return this;
    }

    public <T> OrderBy orderBy(SFunction<T, ?> column, boolean isAsc) {
        return orderBy(column, isAsc ? ASC : DESC);
    }

    /**
     * 字段 lambda 表达式, 默认 驼峰->下划线 风格
     *
     * @param column    字段 getter
     * @param direction 方向
     * @param <T>       实体类
     */
    public <T> OrderBy orderBy(SFunction<T, ?> column, String direction) {
        return orderBy(FieldNameUtil.get(column, CaseFormat.LOWER_CAMEL, CaseFormat.LOWER_UNDERSCORE), direction);
    }

    public OrderBy orderBy(String column, boolean isAsc) {
        return this.orderBy(column, isAsc ? ASC : DESC);
    }

    public OrderBy orderBy(String column, String direction) {
        // 无列名, 略过
        if (StrUtil.isBlank(column)) {
            return this;
        }
        // 无排序方向, 默认 ASC
        if (StrUtil.isBlank(direction)) {
            direction = ASC;
        }

        // 使用来动态排序的列名是安全的，可以放心拼接到SQL中
        if (SqlChecker.isValidSqlIdentifier(column) && SqlChecker.isValidSqlIdentifier(direction)) {
            orderByList.add(new Tuple(column, direction));
        }
        return this;
    }

    private String caseFormatting(String origin) {
        return this.caseTransform.getFrom().to(this.caseTransform.getTo(), origin);
    }

    /**
     * 终端操作: 获取 sql 片段
     * 不含 order by 关键字
     */
    public String orderSegment() {
        if (orderByList.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Tuple tuple : orderByList) {
            String column = tuple.get(0);
            String direction = tuple.get(1);
            // 列别名
            column = this.columnAliasMapping.getOrDefault(column, column);
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(caseFormatting(column)).append(" ").append(direction);
        }

        return sb.toString();
    }

    /**
     * 终端操作: 获取 sql 片段
     * 含 order by 关键字
     */
    public String fullOrderSegment() {
        String bys = this.orderSegment();
        return StrUtil.isEmpty(bys) ? "" : "ORDER BY " + this.orderSegment();
    }

    /**
     * 清除已有 order by s
     * <p>
     * 可在复用 OrderBy 实例前使用
     */
    public OrderBy clearOrderBys() {
        this.orderByList.clear();
        return this;
    }

}
