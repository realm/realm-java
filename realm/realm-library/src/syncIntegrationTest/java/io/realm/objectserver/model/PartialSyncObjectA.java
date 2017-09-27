package io.realm.objectserver.model;

import io.realm.RealmObject;

/**
 * Created by Nabil on 25/09/2017.
 */

public class PartialSyncObjectA extends RealmObject {
    private int number;
    private String string;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
