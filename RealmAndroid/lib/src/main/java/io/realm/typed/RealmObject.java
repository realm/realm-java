package io.realm.typed;

import io.realm.Row;

public abstract class RealmObject {

    private Row row;
    long realmAddedAtRowIndex = -1;

    protected Row realmGetRow() {
        return row;
    }

    protected void realmSetRow(Row row) {
        this.row = row;
    }

    public  String[] getTableRowNames() {return null;}

    public  int[] getTableRowTypes() {return null;}

}
