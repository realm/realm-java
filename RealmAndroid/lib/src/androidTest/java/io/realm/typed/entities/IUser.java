package io.realm.typed.entities;

import io.realm.typed.RealmObject;

public interface IUser {

    public int getId();

    public void setId(int id);

    public String getName();

    public void setName(String name);

    public String getEmail();

    public void setEmail(String email);

}
