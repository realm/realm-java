package io.realm.entities;

import io.realm.RealmObject;

public class IOSChild extends RealmObject {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
