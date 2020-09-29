package com.wtgroup.sugar.typedescor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     <b>语法:</b>
 *     <li>Java类型表达式, 支持嵌套.</li>
 *     <li>支持部分类型简写. 如: 'List&lt;String&gt;', 大小写不敏感. 可定制别名</li>
 *     <li>暂不支持通配泛型表达式, 如不支持 <code>'?','? extends Number','? super Number'</code></li>
 *     示例:
 *     <pre>
 *         A&lt;a1,a2&lt;a21,a22&gt;,a3&gt;, B&lt;b1,b2&gt;, C, D&lt;d1&gt;
 *     </pre>
 * </p>
 * <p>
 *     当出现异常时, 不防, 开启debug, 打印出解析类型表达式后的结果.
 *     可以清晰的看出表达式是否有语法错误.
 * </p>
 *
 *
 * @author Nisus Liu
 * @version 0.0.1
 * @email liuhejun108@163.com
 * @date 2019/1/16 19:19
 */
public class TypeDescor {
    protected static final Logger log = LoggerFactory.getLogger(TypeDescor.class);

    public static final char LT = '<';
    public static final char GT = '>';
    public static final String COMMA = ",";
    public volatile static Map<String, String> alias = new HashMap<>();
    // 别名配置
    static {
        // 常用类型设置别名映射, 键名统一小写
        // 部分, 待补充 ...
        alias.put("string", "java.lang.String");
        alias.put("integer", "java.lang.Integer");
        alias.put("long", "java.lang.Long");
        alias.put("short", "java.lang.Short");
        alias.put("float", "java.lang.Float");
        alias.put("double", "java.lang.Double");
        alias.put("boolean", "java.lang.Boolean");
        alias.put("character", "java.lang.Character");
        alias.put("byte", "java.lang.Byte");

        alias.put("list", "java.util.List");
        alias.put("map", "java.util.Map");
        alias.put("object", "java.lang.Object");
        alias.put("set", "java.util.Set");

    }

    private boolean debug = false;

    /**
     *
     * @param type
     * @return Type[] 一般顶层只有一个元素, 所以, 解析出结果后, 取第一元素即可.
     * @throws ClassNotFoundException
     */
    public Type[] resolveTypeDesc(String type) throws ClassNotFoundException {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        // 元素之间的空格去掉, 但 ? extend xx 这种空格要保留
        type = type.replaceAll("\\s*,\\s*", ",").trim();
        List<TypeDesc> typeDescs = resolveTypeDesc0(type);

        // 处理别名 / 类型校验
        boolean b = validate(typeDescs);

        if (log.isTraceEnabled() || debug) {
            System.out.println(type +" ==> ");
            System.out.println(JSON.toJSONString(typeDescs, true));
        }

        // 转化为 ParameterizedType
        Type[] types = toParameterizedType(typeDescs);

        return types;
    }

    private Type[] toParameterizedType(List<TypeDesc> typeDescs) throws ClassNotFoundException {
        Type[] ps = new Type[typeDescs.size()];
        for (int i = 0; i < typeDescs.size(); i++) {
            TypeDesc td = typeDescs.get(i);
            Class<?> rawType = Class.forName(td.rawType);
            Type[] argTypes = null;
            if (td.typeArgs != null && td.typeArgs.size() > 0) {
                argTypes = toParameterizedType(td.typeArgs);
            }

            // 用 fastjson 的impl
            if (argTypes != null) {
                ps[i] = new ParameterizedTypeImpl(argTypes, null, rawType);
            }else{
                // 无参时, 就是自身
                ps[i] = rawType;
            }

        }

        return ps;
    }

    /**
     * <ul>
     *     <li> 必须要有 rawType
     *     <li> 拒绝 WildcardType 类型, 因为构造起来貌似麻烦.
     *     <li> 剔除首尾空格</li>
     * </ul>
     *
     * @param typeDescs
     * @return
     */
    private boolean validate(List<TypeDesc> typeDescs) {

        // 别名 -> 全名
        for (TypeDesc td : typeDescs) {
            // 必须要有 rawType
            if (StringUtils.isBlank(td.rawType)) {
                throw new IllegalStateException("'rawType' mustn't be empty");
            }

            rejectWildcardType(td.rawType);

            // trim
            td.rawType = td.rawType.trim();

            if (td.rawType!=null && alias.containsKey(td.rawType.toLowerCase())) {
                td.rawType = alias.get(td.rawType.toLowerCase());
            }

            if (td.typeArgs != null && td.typeArgs.size() > 0) {
                // 递归校验参数
                validate(td.typeArgs);
            }
        }

        return true;
    }

    private void rejectWildcardType(String type) {
        if (type.contains("?")) {
            throw new IllegalStateException("Unsupported type: " + type);
        }
    }


    /**
     * <pre>
     *     "A&lt;a1&lt;a11,a12&gt;, a2&lt;a21,a22&gt;,a3&gt;, B&lt;b&gt;,C"
     *
     *     1. 拆分出一级元素.
     *       1.1 目标: 找当前左括号匹配的右括号.
     *         先定好搜索的范围, 范围的终点是第一个右括号. 自左向右搜索左括号, 遇到一个时, 终点往后挪一次.
     *         这是因为: 每个左括号都需要匹配的右括号, 所以当搜索到了左括号时, 当前终点位置对应的右括号就需要与此
     *         左括号匹配, 应当放弃, 需要在尝试用后面的右括号.
     *
     *         换句话说, 要找目标&lt;匹配的&gt;, 首先我假设第一个&gt;就是要找的. 但是当我在目标&lt;和这个&gt;
     *         之间遇到了其他的&lt;, 这说明当前这个&gt;至少是遇到的这个&lt;的匹配. 所以, 上文的假设被推翻, 于是,
     *         再次假设第二个&gt;是目标&lt;的匹配的. ...
     *
     *     2. 一级元素递归调用方法, 进一步拆分.
     * </pre>
     * @param type
     * @return
     */
    private List<TypeDesc> resolveTypeDesc0(String type) {
        List<TypeDesc> typeDescs = new ArrayList<TypeDesc>();


        int cellSt = 0;

        while (!type.isEmpty()) {
            int argSt = type.indexOf(LT) + 1;

            if (argSt > 0) {
                // 找出对应的 闭合 >
                int argEd = -1;
                int ltNum = 0;  // 中间的 '<' 数量
                // 假设的闭合标签
                int hypoMatchGt = type.indexOf(GT, argSt);
                if (hypoMatchGt <= 0) {
                    // 有'<', 但没有'>'
                    throw new IllegalStateException("Syntax error: '" + type + "' '<' at " + (argSt - 1) + " hasn't matched '>'");
                }
                // 第一个'>'前有多少个'<', 那么第几个'>'就是闭合
                for (int i = argSt; i < hypoMatchGt; i++) {
                    // nextLt = type.indexOf('<', i) + 1;
                    if (type.charAt(i) == LT) {
                        // 一旦区间内搜索到了新的开始标签 => 假设的闭合标签失效 --> 往后挪一个闭合标签作为新的假设闭合标签
                        hypoMatchGt = type.indexOf(GT, hypoMatchGt + 1);
                        if (hypoMatchGt < 0) {
                            // 1. 旧的闭合标签无效, 新的假设标签遇到字符串结束 => 语法格式错误
                            // 2. 未到末尾, 但没有找到'>' => 语法错误
                            throw new IllegalStateException("Syntax error: '" + type + "' '<' at " + (argSt - 1) + " hasn't matched '>'");
                        }
                        ltNum++;
                    }
                }//end for: 起始-假设闭合位置, 所有的'<'搜索完毕 => 假设闭合位置就是想要的真实闭合位置

                // 假设闭合 --> 真实闭合
                argEd = hypoMatchGt;

//                if (ltNum <= 0) {
//                    // 后面没有了 '<' => 第一个'>'就是闭合
//                    argEd = hypoMatchGt;
//                }else {
//                    // 往后略过 ltNum 个 '>'后, 遇到才是闭合>
//                    // i.e. hypoMatchGt+1 往后遇到的第 ltNum 个>
//                    int gtNum = 0;
//                    for (int i = hypoMatchGt + 1; i < type.length(); i++) {
//                        if (type.charAt(i) == GT) {
//                            gtNum++;
//                        }
//                        if (gtNum >= ltNum) {
//                            argEd = i;
//                            break;
//                        }
//                    }
//
//                    // ltNum > 0, 但argEnd还是<=0 => 后面没有找到匹配的>
//                    if (argEd <= 0) {
//                        throw new IllegalStateException("Syntax error: '" + type + "' '<' at " + (argSt - 1) + " hasn't matched '>'");
//                    }
//                }

                // 截取出参数
                String rawType = type.substring(cellSt, argSt - 1);
                String argType = type.substring(argSt, argEd);

                TypeDesc td = new TypeDesc();
                td.setRawType(rawType);

                List<TypeDesc> at = resolveTypeDesc0(argType);

                td.setTypeArgs(at);

                typeDescs.add(td);

                // 剔除已处理的
                int k = argEd + 2;  // >,Integer
                if ((k < type.length())) {
                    type = type.substring(k);
                } else {
                    type = "";
                }

            } else if (type.contains(COMMA)) {
                // 多个无参类型, 如 Integer,String,...
                String[] types = type.split(COMMA);
                for (String t : types) {
                    // 每个元素都是只有 rawType
                    typeDescs.addAll(resolveTypeDesc0(t));
                }

                break;
            } else {
                typeDescs.add(new TypeDesc(type, null));
                break;
            }
        }// end while

        return typeDescs;
    }


    /**
     * 在内置别名映射基础上增加定制别名映射
     * @param customAliasMap
     */
    public void customAlias(Map<String,String> customAliasMap) {
        // 在内置别名映射基础上增加定制别名映射
        alias.putAll(customAliasMap);
        if (debug) {
            log.info("alias after custom: ");
            System.out.println(alias);
        }
    }


    public boolean getDebug() {
        return debug;
    }

    public TypeDescor setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    @Data
    @ToString
    public class TypeDesc {

        private String rawType;
        private List<TypeDesc> typeArgs;

        public TypeDesc() {
        }

        public TypeDesc(String rawType, List<TypeDesc> typeArgs) {
            this.rawType = rawType;
            this.typeArgs = typeArgs;
        }
    }


//    public class ParameterizedTypeImpl implements ParameterizedType {
//
//        private final Type[] actualTypeArguments;
//        private final Type ownerType;
//        private final Type rawType;
//
//        public ParameterizedTypeImpl(Type[] actualTypeArguments, Type ownerType, Type rawType) {
//            this.actualTypeArguments = actualTypeArguments;
//            this.ownerType = ownerType;
//            this.rawType = rawType;
//        }
//
//        @Override
//        public Type[] getActualTypeArguments() {
//            return actualTypeArguments;
//        }
//
//        @Override
//        public Type getOwnerType() {
//            return ownerType;
//        }
//
//        @Override
//        public Type getRawType() {
//            return rawType;
//        }
//
//    }



}
