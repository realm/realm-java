package io.realm.example.entities;


import io.realm.typed.RealmObject;

public class User extends RealmObject {

    private int id;
    private String name;
    private String email;

    private User friend;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    @Override
    public String toString() {
        return this.getName();
    }

}
