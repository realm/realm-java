package io.realm.classes;

import io.realm.internal.annotations.ObjectServer;

@ObjectServer
public class SimpleTestClass {
    public String name;

    @ObjectServer
    public static class Foo {
        public String bar;
    }
}
