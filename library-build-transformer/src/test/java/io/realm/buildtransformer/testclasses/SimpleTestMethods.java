package io.realm.buildtransformer.testclasses;

import io.realm.internal.annotations.ObjectServer;

public class SimpleTestMethods {

    @ObjectServer
    public String foo() {
        return "foo";
    }

    @ObjectServer
    public String foo1(String input) {
        return "foo1";
    }

    public String bar() {
        return "bar";
    }

}
