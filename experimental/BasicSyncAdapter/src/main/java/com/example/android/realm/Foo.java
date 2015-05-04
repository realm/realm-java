package com.example.android.realm;

import io.realm.RealmObject;

public class Foo extends RealmObject {

    private String bar;

    public String getBar() {
        return bar;
    }

    public void setBar(String bar) {
        this.bar = bar;
    }
}
