package io.realm.examples.realmintroexample.model;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

@RealmClass
public class Cat extends RealmObject {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
