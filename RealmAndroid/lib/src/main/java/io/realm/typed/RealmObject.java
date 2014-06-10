package io.realm.typed;

public abstract class RealmObject {

    private boolean isInStore = false;

    protected boolean realmIsInStore() {
        return isInStore;
    }

    protected void realmSetInStore(boolean isInStore) {
        this.isInStore = isInStore;
    }

    /**
     * This is overriden in the proxy, so this implementation will never get called, internal use only.
     * @return The index of this object in the object store
     */
    long realmGetRowIndex() {
        return -1;
    }

}
