package com.wtgroup.sugar.json;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Iterator;

/**
 * 日志过滤工具类
 * <p>
 * -- v2.0.0 2020年10月23日 dafei --
 * - 专注于 ValueFilter
 * - 数组|集合元素数量达到阈值(1000)后才触发截取, 减小开销.
 * - 数组|集合类, 元素个数截取 + 元素内容截取.
 * - 开销缩小
 * <p>
 * 数组类的元素一般很少直接放很长的字符串; 如果是对象, 后面会递归扫描到对象的key,value, 会被处理.
 * 数组类截取, 会复制一遍, 虽然不再多做一遍序列化, 但还是会有损耗.
 * Note: 数组类里元素如果是简单类型, 不会被扫描处理了, 但Map形式, 会被递归扫描到.
 *
 * @author wangshuaiqiang
 * @className LogFilterUtil
 * @date 2019/11/21 19:50
 */
@Slf4j
public class JsonFilterUtil {
    /**
     * base64图片开头
     */
    private static final String BASE64_IMG_PREFIX = "data:image/";
    // private static final String[] FILTER_FIELD_ARR = {"byteStr"};

    private static final String JSON_OBJ_START_KEY = "{";
    private static final String JSON_OBJ_END_KEY = "}";
    private static final String JSON_ARR_START_KEY = "[";
    private static final String JSON_ARR_END_KEY = "]";

    private JsonFilterUtil() {}

    public static JsonValueFilter valueFilter(LogFilterConfig config) {
        return new JsonValueFilter(config);
    }


    /**
     * 定制的 ValueFilter
     * 日志, 过滤太长的字段值进行截取,FastJson 过滤器
     * <p>
     * 标记 1 表示该key会被过滤器遍历到.
     * <pre>
     * {
     * 	"a":"aaaaaaaaaaaaaaaaaaaaa", // 1
     * 	"b":53463458, // 1
     * 	"c":[ // 1
     * 		"1111111111",
     * 		"222222222222"
     * 	],
     * 	"d":[ // 1
     *      {
     * 		    "d.d":"d.d.d.d" // 1
     *      },
     * 		"665",
     * 		8
     * 	],
     * 	"subMap":{ // 1
     * 		"a.a":"a.a.a.a" // 1
     *    }
     * }
     * </pre>
     * <p>
     * 1. 值是字符串时, 限定长度截取.
     * 2. 值是集合或数组时, (1)元素个数限制, 且(2)递归1,2, 即, 字符串时, 长度截取, 集合或数组时...
     */
    @NoArgsConstructor
    public static class JsonValueFilter implements ValueFilter {
        public static final JsonValueFilter DEFAULT = new JsonValueFilter(LogFilterConfig.DEFAULT);

        @Setter
        private LogFilterConfig config;

        public JsonValueFilter(LogFilterConfig config) {
            Assert.notNull(config);
            this.config = config;
        }

        @Override
        public Object process(Object o, String name, Object value) {
            if (StrUtil.isBlankIfStr(value)) {
                return value;
            }

            try {
                if (value instanceof String) {
                    return truncateString(name, (String) value);
                } else if (isJsonArray(value)) {
                    return truncateJsonArray(name, value);
                }
            } catch (Exception e) {
                log.error("日志截取超长字段异常 name: " + name, e);
            }


            return value;
        }


        /**
         * 截取是String类型的数据
         *
         * @param name
         * @param value
         * @return
         * @author wangshuaiqiang
         * @date 2019/11/23  18:36
         */
        private String truncateString(String name, String value) {
            if (value == null || value.length() <= config.stringMaxLen) {
                return value;
            }

            String type = "String";

            // base64 图片
            if (value.startsWith(BASE64_IMG_PREFIX)) {
                log.info("日志截取base64字段: {}, type: {}, length: {}", name, type,
                        value.length());
                return substring(value, config.b64ImageMaxLen);
            }
            // json 串
            else if (likeJson(value)) {
                if (value.length() > config.jsonStringMaxLen) {
                    log.info("日志截取超长JSON字符串字段: {}, type: {}, length: {}", name, type,
                            value.length());
                    return substring(value, config.jsonStringMaxLen);
                }
            }
            // 普通字符串
            else {
                log.info("日志截取超长字段: {}, type: {}, length: {}", name, type,
                        value.length());
                return substring(value, config.stringMaxLen);
            }

            return value;
        }


        private static boolean isJsonArray(Object value) {
            return value instanceof Collection || ArrayUtil.isArray(value);
        }

        /**
         * 截取 集合|数组
         *
         * @param name
         * @param value
         * @return
         */
        private Object truncateJsonArray(String name, Object value) {
            if (value instanceof Collection) {
                Collection value1 = (Collection) value;
                // 没有达到阈值时, 不截取, 减小开销
                if (value1.size() <= config.truncateArrayWhenSizeGreaterThen) {
                    return value1;
                }

                JSONArray objects = new JSONArray(value1.size());
                int i = 0;
                Iterator iterator = value1.iterator();
                while (iterator.hasNext() && i++ < config.arrayMaxSize) {
                    Object next = iterator.next();
                    if (next instanceof String) {
                        objects.add(truncateString(name, (String) next));
                    } else if (isJsonArray(next)) {
                        objects.add(truncateJsonArray(name, next));
                    } else {
                        objects.add(next);
                    }
                }
                if (i >= config.arrayMaxSize) {
                    log.info("日志截取超长 Collection: {}, type: {}, length: {}", name, "Collection",
                            value1.size());
                }

                return objects;
            } else if (ArrayUtil.isArray(value)) {
                int len = ArrayUtil.length(value);
                if (len <= config.truncateArrayWhenSizeGreaterThen) {
                    return value;
                }

                int k = Math.min(len, config.arrayMaxSize);
                JSONArray objects = new JSONArray(k);
                for (int i = 0; i < k; i++) {
                    Object next = ArrayUtil.get(value, i);
                    if (next instanceof String) {
                        objects.add(truncateString(name, (String) next));
                    } else if (isJsonArray(next)) {
                        objects.add(truncateJsonArray(name, next));
                    } else {
                        objects.add(next);
                    }
                }

                if (len > config.arrayMaxSize) {
                    log.info("日志截取超长 Array: {}, type: {}, length: {}", name, "Collection",
                            len);
                }

                return objects;
            } else {
                return value;
            }
        }

        /**
         * 判断是否可能是个json串, 大致判断下即可, 节省成本.
         *
         * @param src
         * @return
         */
        private static boolean likeJson(@NotNull String src) {
            final boolean isJson = JSONValidator.from(src).validate();
            return isJson;
            // return (src.startsWith(JSON_OBJ_START_KEY) && src.endsWith(JSON_OBJ_END_KEY))
            //         || (src.startsWith(JSON_ARR_START_KEY) && src.endsWith(JSON_ARR_END_KEY));
        }

        private String substring(String str, int maxLen) {
            String substr = str.substring(0, maxLen);
            if (StrUtil.isBlank(config.omitTipTpl)) {
                return substr;
            }
            final String omitTip = StrUtil.format(config.omitTipTpl, str.length());
            return substr + omitTip;
        }

    }


    public static String toJSONString(Object obj, LogFilterConfig config, SerializerFeature... features) {
        JsonValueFilter jsonValueFilter = JsonValueFilter.DEFAULT;
        if (config != null) { // 配置不同, 需要新建 LogValueFilter
            jsonValueFilter = new JsonValueFilter(config);
        }
        return JSON.toJSONString(obj, jsonValueFilter, features);
    }

    /**
     * 截取过长字段值
     *
     * @param obj
     * @return
     * @author wangshuaiqiang
     * @date 2019/11/23  17:01
     */
    public static String toJSONString(Object obj, SerializerFeature... features) {
        return toJSONString(obj, null, features);
    }

    @Data
    @Accessors(fluent = true, chain = true)
    public static class LogFilterConfig {
        /**
         * 简单类型最大截取长度
         */
        int stringMaxLen = 1024;
        /**
         * json 字符串最大截取长度.
         * '{...}' 或 '[...]'
         */
        int jsonStringMaxLen = 1024;
        /**
         * b64 最大截取長度
         */
        int b64ImageMaxLen = 1024;
        /**
         * 数组|list 最大截取元素个数
         */
        int arrayMaxSize = 10000;
        /**
         * 截取后字符串後綴提示, 佔位符會被替換成原始字符串的長度.
         * 不参与最大长度计算.
         * 空, 则不会追加提示.
         */
        String omitTipTpl = "<...{}>";

        /**
         * 当数组|集合元素个数超过此值时, 触发数组类截取.
         * 截取时, 会复制一原数组, 有点开销. 一般数组元素个数不会太多, 且元素自身长度不会太长.
         * 如果想要都截取, 可设为 0 .
         */
        int truncateArrayWhenSizeGreaterThen = 0;


        public static LogFilterConfig DEFAULT = new LogFilterConfig();
    }


    /*
    @Test
    public void xxx() {
        // 准备测试数据
        HashMap<String, Object> map = new HashMap<>();
        map.put("a", "aaaaaaaaaaaaaaaaaaaaa");
        map.put("b", 53463458);
        map.put("c", Arrays.asList("1111111111", "222222222222"));
        HashMap<String, Object> subMap = new HashMap<>();
        subMap.put("a.a", "a.a.a.a");
        map.put("subMap", subMap);
        HashMap<String, Object> subMap2 = new HashMap<>();
        subMap2.put("d.d", "d.d.d.d");
        map.put("d", new Object[]{subMap2, "665", 8});

        // 默认配置
        System.out.println(LogFilterUtil.getSubJsonString(map, SerializerFeature.PrettyFormat));
        // 自定义配置
        LogFilterUtil.LogFilterConfig config = new LogFilterUtil.LogFilterConfig();
        config.SUB_PRIMITIVE_MAX_LEN = 2;
        config.IS_SUB_JSON_ARRAY = true;
        System.out.println(LogFilterUtil.getSubJsonString(map, config, SerializerFeature.PrettyFormat));
        // =>
        // {
        // 	"a":"aa <...21>",
        // 	"b":53463458,
        // 	"c":[
        // 		"11 <...10>",
        // 		"22 <...12>"
        // 	],
        // 	"d":[
        // 		{
        // 			"d.d":"d. <...7>"
        // 		},
        // 		"66 <...3>",
        // 		8
        // 	],
        // 	"subMap":{
        // 		"a.a":"a. <...7>"
        // 	}
        // }


    }
    * */

}
