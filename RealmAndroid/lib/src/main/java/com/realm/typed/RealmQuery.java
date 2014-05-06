package com.realm.typed;

public class RealmQuery<T> {


    public RealmQuery<T> whereEquals(String columnName, String value) {
        return this;
    }


    public T find() {
        T result = null;
        return result;
    }

}
