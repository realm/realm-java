package io.realm.examples.realmintroexample.model;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Person extends RealmObject {

    private String name;
    private int age;
    private Dog dog;
    private RealmList<Cat> cats;

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

    public Dog getDog() {
        return dog;
    }

    public void setDog(Dog dog) {
        this.dog = dog;
    }

    public RealmList<Cat> getCats() {
        return cats;
    }

    public void setCats(RealmList<Cat> cats) {
        this.cats = cats;
    }
}