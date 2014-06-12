package io.realm.typed.entities;

import java.util.ArrayList;
import java.util.List;

import io.realm.typed.RealmObject;

public class Dog extends RealmObject {

    private List<User> owners = new ArrayList<User>();

    public List<User> getOwners() {
        return owners;
    }

}
