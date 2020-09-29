package com.wtgroup.sugar.reflect;

import com.wtgroup.sugar.bean.User;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class FieldUtilTest {

    @Test
    public void foo() {
        User user = new User();
        user.setAge(88887);
        System.out.println(user);
        Field[] fields = FieldUtil.getFields(User.class, false, false);
        for (Field field : fields) {
            if (field.getName().equals("uname")) {
                FieldUtil.writeField(user, field, "zhangsan999");
            }
            if (field.getName().equals("age")) {
                FieldUtil.writeField(user, "age", null);
            }
        }

        System.out.println(user);
    }


}
