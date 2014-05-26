package io.realm.typed;

import io.realm.TableOrView;

public abstract class RealmObject {

    protected long rowIndex = -1;
    private RealmList realmList;

    protected void setup(RealmList realmList, long rowIndex) {
        this.realmList = realmList;
        this.rowIndex = rowIndex;
    }


    protected TableOrView realmGetTable() {
        return realmList.getTable();
    }

}
