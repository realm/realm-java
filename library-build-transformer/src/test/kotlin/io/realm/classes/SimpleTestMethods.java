package io.realm.classes;

import io.realm.internal.annotations.ObjectServer;

public class SimpleTestMethods {

    @ObjectServer
    public String foo() {
        return "foo";
    }

    public String bar() {
        return "bar";
    }

}
