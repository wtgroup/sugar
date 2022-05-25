package com.wtgroup.sugar.db;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * <pre>
 *     v1.3
 *      selector 改用内置的 Function
 *     v1.2
 *      简化使用, 使用 Collection 输入输出
 *     v1.1
 *       - 分段规则改为 CommonUtil中的自动伸缩的分组规则.
 * </pre>
 * @author L&J
 */
@Slf4j
public class SqlSegmentInUtil {


    /**
     * 分段执行IN查询
     * <p>考虑IN查询元素过多的情况, 可能不走索引</p>
     *
     * <p>暂只支持List和Set类型, 父级和子级集合类型一致</p>
     *
     * @param inList in 条件 list
     * @param subInListSize 单批 in list 最大 size
     * @param selector 数据查询逻辑
     * @param <E>      IN(..)中的元素类型
     * @param <R>      查询结果返回的List元素类型
     * @return 合并后的总结果集
     */
    public static <R, E> Collection<R> select(Collection<E> inList, int subInListSize, Function<Collection<E>, Collection<R>> selector) {
        long t0 = System.currentTimeMillis();
        Collection<E> segInList = new ArrayList<>();

        int size = inList.size();

        Collection<R> compositeResults;
        if (subInListSize >= size) {
            //一次查就可以了
            //compositeResults = mobileTagMapper.selectMobileTags(mobiles);
            // compositeResults = selector.select(inList);
            compositeResults = selector.apply(inList);

            log.debug("size({}) <= maxSeg({}), 仅执行单次查询", size, subInListSize);
        } else {
            // 伸缩分组
            Map<Integer, Integer> segInfo = groupByLimitSize(size, subInListSize);
            //log.debug("size({}) > maxSeg({}), 将执行 {} 次分段查询", size, maxSeg, size / maxSeg + (size % maxSeg > 0 ? 1 : 0));
            log.debug("size({}) > maxSeg({}), 将执行 {} 次分段查询(伸缩分组)", size, subInListSize, segInfo.size());
            compositeResults = new ArrayList<R>(size);

            int offset = 0;
            int k = 0;
            int segSize;
            int j = 0;
            Iterator<E> iterator = inList.iterator();
            while (iterator.hasNext()) {
                segInList.add(iterator.next());
                j++;
                segSize = segInfo.get(k);
                if (j >= segSize || (offset + 1) >= size) {
                    k++;
                    //到达段分点, 执行查询
                    long st = System.currentTimeMillis();
                    // compositeResults.addAll(selector.select(segInList));
                    compositeResults.addAll(selector.apply(segInList));
                    long et = System.currentTimeMillis();
                    log.trace("第 {} 次分段IN查询完成, size: {}, segSize: {}, 耗时: {}ms", k, size, segSize, et - st);
                    //完事了要清空临时list
                    segInList.clear();
                    j = 0;
                }

                offset++;
            }
        }
        long t1 = System.currentTimeMillis();
        log.info("Segment IN select cost: {}ms, total size: {}", t1 - t0, size);
        return compositeResults;
    }

    // /**
    //  * @param <R> 返回元素类型
    //  * @param <E> 入参集合类型, 即IN list的类型
    //  */
    // @FunctionalInterface
    // public interface Selector<R, E> {
    //     Collection<R> select(Collection<E> segInList);
    // }

    // public static void main(String[] args) {
    //     HashSet<Integer> set = new HashSet<Integer>();
    //     set.add(3);
    //     set.add(1);
    //     set.add(4);
    //     set.add(6);
    //     set.add(7);
    //     set.add(9);
    //     set.add(10);
    //     // SqlSegmentInUtil.select(set, 2, segInList -> {
    //     //     System.out.println(segInList);
    //     //     return Collections.emptyList();
    //     // });
    //
    //     SqlSegmentInUtil.select(set, 3, sublist -> {
    //         System.out.println(sublist);
    //         return Arrays.asList("aa", "bb", "cc");
    //     });
    //
    // }


    /**
     * 指定总长度和<b>子集长度</b>分组
     * <p>组数和组大小都会浮动, 以便均匀分摊总数</p>
     * <p>分组后的信息在于Map中, 第一组索引为 0 .</p>
     * <p>不能整分的, 剩余部分会自适应伸缩组数或组大小, 但会优先从后往前分摊剩余的</p>
     *
     * @param total
     * @param subSize 会在指定的基础上伸缩
     * @return
     */
    //balance group
    private static Map<Integer, Integer> balanceGroupBySize(int total, int subSize) {
        if (total <= 0 || subSize <= 0) {
            throw new IllegalArgumentException("`total` OR `subSize` <= 0 , expected positive integer.");
        }
        if (total < subSize) {
            log.warn("`subSize` must < `total`, has been reset `subSize`=`total`");
            subSize = total;
        }

        int[] groupParams = recurveGroupParams(total, subSize);
        subSize = groupParams[0];
        int groupNum = groupParams[1];
        int res = groupParams[2];

        Map<Integer, Integer> groups = new HashMap<>(groupNum + 1);
        for (int i = 0; i < groupNum; i++) {
            groups.put(i, subSize);
        }
        if (res < (subSize >> 1)) {
            // 小于一半, 由已有组分摊
            // 上文已经保证了 res <= groupNum
            // 从后往前, 每组分摊1个, 直到分摊完毕
            for (int i = 1; i <= res; i++) {
                groups.put(groupNum - i, groups.get(groupNum - i) + 1);
            }

        } else {
            // 新增 1 组, ∵ 余数一定小于除数, res < subSize, 新增的一组一定不会过大
            groups.put(groupNum, res);
        }


        return groups;
    }

    /**
     * 扩充组数, 直到组数 >= 余数, 以便余数分摊到每个组里
     *
     * @param total
     * @param subSize
     * @return
     */
    private static int[] recurveGroupParams(int total, int subSize) {
        int groupNum = total / subSize;
        int res = total - (groupNum * subSize);
        while (res > groupNum) {
            //组数不够
            groupNum += 1;
            subSize = total / groupNum;
            res = total % groupNum;
        }
        return new int[]{subSize, groupNum, res};
    }


    /**
     * 每组不超过指定的size
     *
     * @param total
     * @param subSize
     * @return
     */
    private static Map<Integer, Integer> groupByLimitSize(int total, int subSize) {
        if (total <= 0 || subSize <= 0) {
            throw new IllegalArgumentException("`total` OR `subSize` <= 0 , expected positive integer.");
        }
        if (total < subSize) {
            log.warn("`subSize` must < `total`, has been reset `subSize`=`total`");
            subSize = total;
        }

        int[] groupParams = recurveGroupParams(total, subSize);
        subSize = groupParams[0];
        int groupNum = groupParams[1];
        int res = groupParams[2];

        Map<Integer, Integer> groups = new HashMap<>(groupNum + 1);
        for (int i = 0; i < groupNum; i++) {
            groups.put(i, subSize);
        }

        // 新增 1 组, ∵ 余数一定小于除数, res < subSize, 新增的一组一定不会过大
        if (res>0) {
            groups.put(groupNum, res);
        }


        return groups;
    }
}
