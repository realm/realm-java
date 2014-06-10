package io.realm.examples.entities;

import io.realm.typed.RealmList;
import io.realm.typed.RealmObject;

public class Person extends RealmObject {

    private String name;
    private byte[] picture;
    private RealmList<Dog> dogs;

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

    public RealmList<Dog> getDogs() {
        return dogs;
    }

    public void setDogs(RealmList<Dog> dogs) {
        this.dogs = dogs;
    }
}
