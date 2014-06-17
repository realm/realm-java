package io.realm.typed.entities;

import java.util.ArrayList;
import java.util.List;

import io.realm.typed.RealmObject;

public class Dog extends RealmObject {

    private List<User> owners = new ArrayList<User>();
    private String name;

    public List<User> getOwners() {
        return owners;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
