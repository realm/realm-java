package io.realm.objectserver.model;

import io.realm.RealmObject;

/**
 * Created by Nabil on 25/09/2017.
 */

public class PartialSyncObjectB extends RealmObject {
    private int number;
    private String firstString;
    private String secondString;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getFirstString() {
        return firstString;
    }

    public void setFirstString(String firstString) {
        this.firstString = firstString;
    }

    public String getSecondString() {
        return secondString;
    }

    public void setSecondString(String secondString) {
        this.secondString = secondString;
    }
}
