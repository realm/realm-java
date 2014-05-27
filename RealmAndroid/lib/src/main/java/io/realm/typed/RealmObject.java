package io.realm.typed;

public abstract class RealmObject {


    private long rowIndex = -1;

    protected long realmGetRowIndex() {
        return rowIndex;
    }

    protected void realmSetRowIndex(long rowIndex) {
        this.rowIndex = rowIndex;
    }

}
