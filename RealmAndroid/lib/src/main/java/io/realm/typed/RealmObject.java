package io.realm.typed;

public abstract class RealmObject {

    long realmRowIndex = -1;
    private boolean isInStore = false;

    protected boolean realmIsInStore() {
        return isInStore;
    }

    protected void realmSetInStore(boolean isInStore) {
        this.isInStore = isInStore;
    }

}
