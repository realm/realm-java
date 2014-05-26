package io.realm.example.entities;

import io.realm.typed.RealmObject;

public class RoyalPerson extends RealmObject {

    private String title;
    private int age;
    private boolean hasHorse;

    public RoyalPerson(String title, int age, boolean hasHorse) {
        this.title = title;
        this.age = age;
        this.hasHorse = hasHorse;
    }

    public RoyalPerson() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isHasHorse() {
        return hasHorse;
    }

    public void setHasHorse(boolean hasHorse) {
        this.hasHorse = hasHorse;
    }

}
