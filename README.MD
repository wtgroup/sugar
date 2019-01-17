

# sugar(糖)

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
	    <groupId>com.github.Nisus-Liu</groupId>
	    <artifactId>sugar</artifactId>
	    <version>{version}</version>
	</dependency>
```







## TypeDescor

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
