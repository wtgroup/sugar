package com.wtgroup.sugar.bean;

import lombok.Data;

import java.util.List;

@Data
public class User {
    private String uname;
    private Integer age;
    // 错误示例: private boolean isMale
    private boolean male;
    private List<String> tags;

    private String i_am_xia_Huaxian;

    private String helloFieldNameUtils;
}
