package io.realm.buildtransformer.testclasses;

import io.realm.internal.annotations.ObjectServer;

public class NestedTestClass {
    public String name;

    @ObjectServer
    public static class StaticInnerClass {
        public String foo;
    }


    @ObjectServer
    public class InnerClass {
        public String foo;
    }

    @ObjectServer
    public enum Enum {
        FOO
    }

    @ObjectServer
    public interface Interface {
        void foo();
    }
}
