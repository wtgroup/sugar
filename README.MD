

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
