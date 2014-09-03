package io.realm.tests.examples.entities;

import io.realm.typed.RealmArrayList;
import io.realm.typed.RealmList;
import io.realm.typed.RealmObject;
import io.realm.base.RealmClass;
import io.realm.typed.RealmTableOrViewList;

@RealmClass
public class Person extends RealmObject {

    private String name;
    public RealmList<Dog> dogs = new RealmArrayList<Dog>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<Dog> getDogs() {
        return dogs;
    }

    public void setDogs(RealmTableOrViewList<Dog> dogs) {
        this.dogs = dogs;
    }
}
