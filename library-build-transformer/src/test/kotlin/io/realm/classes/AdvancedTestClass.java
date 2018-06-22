package io.realm.classes;

import io.realm.internal.annotations.ObjectServer;

public class AdvancedTestClass {

    public String name;

    @ObjectServer
    public class MyInnerClass {

        public String foo;
    }

    @ObjectServer
    public static class MyStaticInnerClass {
        public String foo;
    }

    @ObjectServer
    public enum MyEnum {
        FOO
    }

    @ObjectServer
    public interface MyInterface {
        void foo();
    }
}
