package io.realm.typed;

import io.realm.Row;

public abstract class RealmObject {

    protected Row row;
    long realmAddedAtRowIndex = -1;
}
