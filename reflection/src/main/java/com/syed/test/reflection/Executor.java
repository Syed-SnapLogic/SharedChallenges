package com.syed.test.reflection;

import java.lang.reflect.Method;

public class Executor {
    public static void main(String str[]) throws Exception {
        Class klass = Class.forName("com.syed.test.reflection.MyType");
        Method[] ms = klass.getDeclaredMethods();
        for (Method m : ms) {
            System.out.println(m.getName());
        }
    }
}
