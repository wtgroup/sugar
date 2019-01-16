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
        // String 类型 原文要带有"
        //String jo = JSON.parseObject("\"xxxx\"", String.class);

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
        //Object jo = JSON.parse(s);

        TypeDescor td = new TypeDescor();
        td.setDebug(true);
        Type[] types = td.resolveTypeDesc("List<map<string,string>>");

        Object jo = JSON.parseObject(s, types[0]);


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

}