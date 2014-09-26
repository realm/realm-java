package io.realm.examples.performance.realm;

import io.realm.RealmObject;

public class RealmEmployee extends RealmObject {

    private int id;
    private String name;
    private int age;
    private int hired;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public int getHired() {
        return hired;
    }

    public void setHired(int hired) {
        this.hired = hired;
    }
}
