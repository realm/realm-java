package io.realm;

import io.realm.internal.Row;

public abstract class RealmObject {

    protected Row row;
    long realmAddedAtRowIndex = -1;

    protected Row realmGetRow() {
        return row;
    }

    protected void realmSetRow(Row row) {
        this.row = row;
    }
}
