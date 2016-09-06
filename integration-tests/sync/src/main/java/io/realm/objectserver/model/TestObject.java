package io.realm.objectserver.model;

import io.realm.RealmObject;

public class TestObject extends RealmObject {
    private int intProp;
    private String stringProp;

    public int getIntProp() {
        return intProp;
    }

    public void setIntProp(int intProp) {
        this.intProp = intProp;
    }

    public String getStringProp() {
        return stringProp;
    }

    public void setStringProp(String stringProp) {
        this.stringProp = stringProp;
    }
}
