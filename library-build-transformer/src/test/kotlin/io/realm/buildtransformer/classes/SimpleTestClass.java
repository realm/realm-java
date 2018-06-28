package io.realm.buildtransformer.classes;

import io.realm.internal.annotations.ObjectServer;

@ObjectServer
public class SimpleTestClass {
    public String name;

    public static class Foo {
        public String bar;
    }
}
