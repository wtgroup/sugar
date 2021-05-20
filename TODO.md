

## 组合注解

-[x] 返回第三方的注解, 但属性是被我覆写后的 ?
类似子类实例, 向上转型, 由父类接收. 父类引用指向子类对象. 向上转型(upcasting).
但又很不同. 实际还是"父类"实例, 但部分属性在"子类"设了值得情况下, 会被覆写.
```
// B composite A , A 注解是我们无法修改的
B composite = CompositeAnnotationUtil.getCompositeAnnotation(B.class, case1);
// 有时候想返回 A 注解, 但部分属性可以被 B 覆写
```

-[ ] 组合注解多级支持?
目前只能支持 B 组合 A 的, 那么 A 还有组合其他的呢, 暂不支持往上递归检索.