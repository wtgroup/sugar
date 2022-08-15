package com.wtgroup.sugar.json;

import com.alibaba.fastjson.JSON;
import com.wtgroup.sugar.util.Sout;
import lombok.Data;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LogFilterUtilTest {


    @Test
    public void foo() {
        final LogFilterUtil.LogFilterConfig config = new LogFilterUtil.LogFilterConfig();
        config.omitTipTpl("#OMIT#")
                .stringMaxLen(2)
                .jsonStringMaxLen(2)
                .arrayMaxSize(1);
        final LogFilterUtil.LogValueFilter logValueFilter = LogFilterUtil.valueFilter(config);

        final Demo bean = new Demo();
        bean.a = "帅哥;非空时;挨个lj23l56j2l6j2lj6";
        bean.b = 123;
        bean.c = "{\"y\": 8}";
        bean.d = "[ ]";
        final HashMap<String, Object> m = new HashMap<>();
        m.put("e1", Arrays.asList(1, 2, 3, 4));
        bean.e = m;


        final String s = JSON.toJSONString(bean, logValueFilter);

        Sout.println(s);

        final String s1 = LogFilterUtil.toJSONString(bean);
        Sout.println(s1);
    }

    @Data
    public class Demo {
        private String a;
        private int b;
        private String c;
        private String d;
        private Map<String, Object> e;
    }

}