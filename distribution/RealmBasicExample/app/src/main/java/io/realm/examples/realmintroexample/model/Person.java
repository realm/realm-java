package io.realm.examples.realmintroexample.model;

import java.lang.String;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

@RealmClass
public class Person extends RealmObject {

    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}