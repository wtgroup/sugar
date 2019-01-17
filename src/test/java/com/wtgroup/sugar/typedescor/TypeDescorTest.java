package com.wtgroup.sugar.typedescor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.*;


public class TypeDescorTest {

    @Test
    public void fun3() throws ClassNotFoundException {

        //region 生成测试json串
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        HashMap<String, Object> m = new HashMap<>();
        m.put("user", new User("libai", 89));
        m.put("A", "aaaa");
        LinkedHashMap<String, Object> m2 = new LinkedHashMap<>();
        m2.put("U", 676767);
        m2.put("V", "vvv");
        maps.add(m);
        maps.add(m2);

        String s = JSON.toJSONString(maps);
        //endregion
        //Object jo = JSON.parse(s);

        TypeDescor td = new TypeDescor();
        // 开始debug, 可以打印类型表达式分解后的结果
        td.setDebug(true);
        // 解析类型表达式
        Type[] types = td.resolveTypeDesc("List<map<string,string>>");

        // 测试 json 转换
        Object jo = JSON.parseObject(s, types[0]);


        System.out.println();
        System.out.println("解析得到对象: ");
        System.out.println(jo);
    }

    @Data
    class User{
        private String name;
        private int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }


    @Test
    public void fun() throws ClassNotFoundException {
        String typeDesc = "List<Integer>>>";

        TypeDescor td = new TypeDescor();
        td.setDebug(true);

        Type[] types = td.resolveTypeDesc(typeDesc);
        System.out.println(types);

        ArrayList obj = new ArrayList();
        obj.add("797");
        String s = JSON.toJSONString(obj);

        List<Integer> o = JSON.parseObject(s, types[0]);

        System.out.println("json -> object: "+o);


        //System.out.println(JSON.toJSONString(typeDescs,true));
    }

    @Test
    public void fun2(){
        TypeReference<List<? super Number>> ref = new TypeReference<List<? super Number>>() {
        };
        ArrayList<Number> list = new ArrayList<>();
        list.add(89);
        list.add(78L);
        list.add(5.8);
        String s = JSON.toJSONString(list);
        List<? super Number> numbers = JSON.parseObject(s, ref);
        System.out.println(numbers);
    }

    @Test
    public void 测试类型表达式解析() {
        String type = "A<a1 < a11,a12>, a2<a21,a22>,a3>, B<b>,C";
        TypeDescor td = new TypeDescor();
        td.setDebug(true);
        try {
            td.resolveTypeDesc(type);
        } catch (ClassNotFoundException e) {
            // 仅测试类型表达式解析结果, 忽略类型转换
        }

    }

    // String 类型 原文要带有"
    //String jo = JSON.parseObject("\"xxxx\"", String.class);

}