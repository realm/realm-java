package com.realm.typed;

import java.util.Iterator;

public class RealmIterator<E> implements Iterator<E> {

    private int rowIndex = 0;
    private Realm<E> realm;

    RealmIterator(Realm<E> realm) {
        this.realm = realm;
    }

    @Override
    public boolean hasNext() {
        return (this.realm.size()-1 > this.rowIndex);
    }

    @Override
    public E next() {
        this.rowIndex++;
        return this.realm.get(rowIndex);
    }

    @Override
    public void remove() {

    }

}
