

# sugar(_糖_)

> 提供一些好用的工具方法. 就像一些好吃的糖果.



## maven引用

```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<!--增加依赖-->
<dependency>
    <groupId>com.github.wtgroup</groupId>
    <artifactId>sugar</artifactId>
    <version>1.1.4</version>
</dependency>
```


## 父子表解析器 `TreeTableHandler`
将DB父子表格式的表格数据解析成java的树形结构.
代码参见: `com.wtgroup.sugar.db.TreeTableHandlerTest`
```
// DB数据格式样例:
id pid ...
1  null   
2  1      
3  1      
4  2      
5  2      
6  3  

// 解析
// 模拟数据
List<Map> data = mock();

// to tree
// p1: DB查询到原始数据集
// p2: 指定 id 获取方式
// p3: pid 获取方式
// p4: 原始数据行向java树节点的转换方式
TreeTableHandler<Map, Map, Integer> treeTableHandler = new TreeTableHandler<>(data,
        row -> (Integer) row.get("id"),
        row -> (Integer) row.get("pid"),
        new TreeTableHandler.ResultMapper<Map, Map>() {
            @Override
            public Map mapProperties( Map row , int lvl, Optional<Map> parent) {
                HashMap res = new HashMap();
                res.put("rid", row.get("id"));
                res.put("rpid", row.get("pid"));
                res.put("rgdp", row.get("gdp"));
                res.put("rpopulation", row.get("population"));
                res.put("lvl", lvl);
                return res;
            }

            // 当子集数据就绪时调用
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

// 转换成树
List<Map> results = treeTableHandler.toTree();
// System.out.println(treeTableHandler.getTreeMeta());
System.out.println(JSON.toJSONString(results, true));
    
```

## 注解"组合"

有点类似 `org.springframework.core.annotation.AliasFor` . 但使用场景刚好相反. 其间差别, 需要耐心品味. 需要理解 Spring 的 `AliasFor` 机制. 

假设 `@A` 是别人的注解, 无法改动, 且不能作为元注解. 但你需要在 `@A` 基础上增加一些属性, 增强功能. 但很多属性想沿用 `A` 的. 比如 `B.a` 等价 `A.a` , 
在 `B.a` 有效时, 用 `B.a`, 否则看`A.a`是否可用. 就好像 `B` 组装|继承了 `A.a`. 此外, `B` 还可以有其他自己特有的属性, 如 `B.b`.
```
        know
 A a <------+ API User
   ^            +
'@AliasFor'     |
   |            | maybe unknown
   +            |
 B a  <---------+


  A a <-----------+
    ^             |
'@Composite'  maybe|unknown
    |             |
    +    know     +
  B a <------+ API User
```

**演示**

```java
// A 注解

String a() default "A.a";
```

```java
// B 注解

// a 属性组合 A.a , 自己的 a 无效时, 就会去找 A.a, 如果有效, 则用 A.a ,
// 此处, '无效' 已就是只有默认值, 使用者没有显示设置值. 
// @Composite(value = "a", annotation = A.class) // 或简写为: (默认采用当前属性方法名)
@Composite(annotation = A.class)
String a() default "B.a";

String b();
```

```java
// 被注解bean, AnnotatedElement
public class AnnotatedBean {

    // 预期: B.a == "AAAAAAAAAAAAA.aaaaa"
    @B(b = "B.b")
    @A(a = "AAAAAAAAAAAAA.aaaaa")
    private String case1;

    // 预期: B.a == "BBBBBBBBBBBBB.bbbbb"
    @B(a = "BBBBBBBBBBBBB.bbbbb", b="B.b")
    @A(a = "AAAAAAAAAAAAA.aaaaa")
    private Integer case2;

}
```

`CompositeAnnotationUtil.getCompositeAnnotation`
```java
// 基本使用

Field case1 = AnnotatedBean.class.getDeclaredField("case1");
B composite = CompositeAnnotationUtil.getCompositeAnnotation(B.class, case1);
Assert.assertEquals("case1", "AAAAAAAAAAAAA.aaaaa", composite.a());
Assert.assertEquals("B.b", composite.b());
```

```java
// 查找的目标注解可以不存在
// 需要组合的第三方注解属性基本满足需求时, 您就可以偷个懒, 不需要写自己的注解了

Field case3 = AnnotatedBean.class.getDeclaredField("case3");
// 获取没有的注解的 "组合" 实例
C composite = CompositeAnnotationUtil.getNotNullCompositeAnnotation(C.class, case3);
System.out.println(composite);
```

详情参考: `com.wtgroup.sugar.annotation.CompositeAnnotationUtilTest`.



## `FieldNameUtil` 方法名->字段名 (`com.wtgroup.sugar.reflect.FieldNameUtil`)

将类的`getter`方法`Lambda`解析成对应的属性名.

你有没有这样的感受? 比如在查表中的某些字段时, 不得不硬编码这些字段名. 当表有所变动时, 不得不用肉眼检查相关代码, 同步调整.
有了这个工具, 便可以利用编辑器帮你检查. 如果只是简单修改字段名, 仅仅利用编辑器的重构, 代码就同步修改好了. 

```java
// 基本使用
String result = FieldNameUtil.fromAccessor("getName");
Assert.assertEquals("name", result);
String result1 = FieldNameUtil.fromAccessor("setIsBool");
Assert.assertEquals("isBool", result1);
String result2 = FieldNameUtil.fromAccessor("getURL");
Assert.assertEquals("URL", result2);
String result3 = FieldNameUtil.fromAccessor("setuName");
Assert.assertEquals("uName", result3);

// 还可以转换风格
String result = FieldNameUtil.get(User::getHelloFieldNameUtils, CaseFormat.LOWER_HYPHEN);
Assert.assertEquals("hello-field-name-utils", result);
```


## 数字类 `com.wtgroup.sugar.math.Numbor`

像 Number 类, 但比它更有趣!

在不知道计算因子是否为null时, 多个数字为了处理 Null Exception 很是麻烦.
使用本工具类, 则不用关系是否为null的问题.

demo: `com.wtgroup.sugar.math.NumborSpec`, `com.wtgroup.sugar.math.NumborTest`.

```
3.0 - 2.0 等于?
3.0 - 2.0 == 1.0 ?

(double) 0.3 - 0.2; // 0.09999999999999998
(double) 0.3 - 0.2 == 1.0; // false
// o~o, NumberUtil 可以帮你
NumberUtil.sub(0.3, 0.2); // 0.1
NumberUtil.sub(0.3, 0.2) == 0.1; // true
// Numbor 也可以
new Numbor(0.3).sub(0.2); // 0.1
new Numbor(0.3).sub(0.2).equals(0.1); // true

// 但但, NumberUtil 在多个变量, 同时又不知道是否 null, 是否为 0 时, 在计算前需要很多判断校验, 绕费心思...
// NumberUtil.div(1, null); // NullPointerException
NumberUtil.mul(1, null); // 应该等于多少? 1 or 0 ?
// NumberUtil.div(1, 0); // ArithmeticException

// 看我的!
new Numbor(1).mul((Number) null).add(12345).div(0).orElse(0); // 管它什么null, 无脑算就是了  => 0
```

## `TypeDescor`

将类型表达式转换成`Type`类型.

本工具关键逻辑是将类型表达式(字符串)解析成属性结构.

如: `"A<a1<a11,a12>, a2<a21,a22>,a3>, B<b>,C"`这种嵌套的泛型表达式结构, 将其分解.

分解结果:

```
[
	{
		"rawType":"A",
		"typeArgs":[
			{
				"rawType":"a1",
				"typeArgs":[
					{
						"rawType":"a11"
					},
					{
						"rawType":"a12"
					}
				]
			},
			{
				"rawType":"a2",
				"typeArgs":[
					{
						"rawType":"a21"
					},
					{
						"rawType":"a22"
					}
				]
			},
			{
				"rawType":"a3"
			}
		]
	},
	{
		"rawType":"B",
		"typeArgs":[
			{
				"rawType":"b"
			}
		]
	},
	{
		"rawType":"C"
	}
]
```



**类型表达式解析思路:**

 "A<a1<a11,a12>, a2<a21,a22>,a3>, B<b>,C"

1. 拆分出一级元素.
   目标: 找当前左括号匹配的右括号.
   先定好搜索的范围, 范围的终点是第一个右括号. 自左向右搜索左括号, 遇到一个时, 终点往后挪一次.
   这是因为: 每个左括号都需要匹配的右括号, 所以当搜索到了左括号时, 当前终点位置对应的右括号就需要与此
   左括号匹配, 应当放弃, 需要在尝试用后面的右括号.

   换句话说, 要找目标<匹配的>, 首先我假设第一个>就是要找的. 但是当我在目标<和这个>
   之间遇到了其他的<, 这说明当前这个>至少是遇到的这个<的匹配. 所以, 上文的假设被推翻, 于是,
   再次假设第二个>是目标<的匹配的. ...

2. 一级元素递归调用方法, 进一步拆分.





参见示例代码: [com.wtgroup.sugar.typedescor.TypeDescorTest](./src/test/java/com/wtgroup/sugar/typedescor/TypeDescorTest.java)



## TODO

- [ ] `LambdaUtils` case format 与实际不对应时, 转换有问题.
    需要: 提供检测类型的方法.

