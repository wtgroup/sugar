package com.wtgroup.sugar.db;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TreeTableHandlerTest {
    @Test
    public void toTree() {
        // 模拟数据
        List<Map> data = new ArrayList<>();
        for ( int i = 0; i < 10; i++ ) {
            data.add(buildRow(i));
        }
        HashMap<String, Object> r10 = buildRow(10);
        r10.put("pid", 3);
        HashMap<String, Object> r11 = buildRow(11);
        r11.put("pid", 3);
        data.add(r10);
        data.add(r11);

        System.out.println(data);

        // to tree
        TreeTableHandler<Map, Map, Integer> treeTableHandler = new TreeTableHandler<>(data,
                row -> (Integer) row.get("id"),
                row -> (Integer) row.get("pid"),
                new TreeTableHandler.ResultMapper<Map, Map>() {
                    @Override
                    public Map mapProperties( Map row , int lvl) {
                        HashMap res = new HashMap();
                        res.put("rid", row.get("id"));
                        res.put("rpid", row.get("pid"));
                        res.put("rgdp", row.get("gdp"));
                        res.put("rpopulation", row.get("population"));
                        res.put("lvl", lvl);
                        return res;
                    }

                    @Override
                    public void onChildren( Map parent, List<Map> children ) {
                        parent.put("children", children);
                        parent.put("count", children.size());
                        int s = 0;
                        for ( Map child : children ) {
                            s += (int)child.get("rgdp");
                        }
                        parent.put("sum", s);
                    }
                }
        );

        List<Map> results = treeTableHandler.toTree();
        System.out.println(treeTableHandler.getTreeMeta());
        System.out.println(JSON.toJSONString(results, true));


    }

    private HashMap<String, Object> buildRow( int i) {
        HashMap<String, Object> row1 = new HashMap<>();
        row1.put("id", i);
        row1.put("pid", i-1);
        row1.put("gdp", 5+i);
        row1.put("population", 4+i);
        return row1;
    }
}