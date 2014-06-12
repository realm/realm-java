package io.realm.examples.entities;

import io.realm.typed.RealmObject;
import io.realm.typed.RealmTableOrViewList;

public class Person extends RealmObject {

    private String name;
    private byte[] picture;
    private RealmTableOrViewList<Dog> dogs;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public RealmTableOrViewList<Dog> getDogs() {
        return dogs;
    }

    public void setDogs(RealmTableOrViewList<Dog> dogs) {
        this.dogs = dogs;
    }
}
