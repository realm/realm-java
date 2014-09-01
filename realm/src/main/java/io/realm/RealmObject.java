package io.realm;

import java.util.NoSuchElementException;

import io.realm.internal.Row;

public abstract class RealmObject {

    private Row row;
    long realmAddedAtRowIndex = -1;

    protected Row realmGetRow() {
        return row;
    }

    protected void realmSetRow(Row row) {
        this.row = row;
    }

    /**
     *
     * This method is not implemented yet.
     *
     * @return
     */
    public <E extends RealmObject> RealmList<E> allObjects() throws NoSuchMethodException {
        throw new NoSuchMethodException("allObjects is not implemented");
    }


    /**
     *
     * This method is not implemented yet.
     * Suggested rename: allObjects
     *
     * @param realm
     * @return
     */
    public <E extends RealmObject> RealmList<E> allObjectsInRealm(Realm realm) throws NoSuchMethodException {
        throw new NoSuchMethodException("allObjectsInRealm is not implemented");
    }

    /**
     *
     * This method is not implemented yet.
     * Suggested rename: create
     *
     * @param element
     */
    public <E extends RealmObject> void createInDefaultRealmWithObject(E element) throws NoSuchMethodException {
        throw new NoSuchMethodException("allObjectsInRealm is not implemented");
    }

    /**
     *
     * This method is not implemented yet.
     * Suggested rename: create
     *
     * @param realm
     * @param element
     */
    public <E extends RealmObject> void createInRealmWithObject(Realm realm, E element) throws NoSuchMethodException {
        throw new NoSuchMethodException("allObjectsInRealmWithObject is not implemented");
    }

    /**
     * This method is not implemented yet.
     * Suggested reanme: where
     *
     * @param realm
     * @return
     */
    public <E extends RealmObject> RealmList<E> objectsInRealmWhere(Realm realm) throws NoSuchMethodException {
        throw new NoSuchMethodException("objectsInRealmWhere is not implemented");
    }

    /**
     * This method is not implemented yet.
     * Suggested reanme: where
     *
     * @return
     */
    public <E extends RealmObject> RealmList<E> objectsDefaultRealmWhere() throws NoSuchMethodException {
        throw new NoSuchMethodException("objectsInDefaultRealmWhere is not implemented");
    }


    /**
     * This method is not implemented yet.
     *
     * @return
     */

    public String JSONString() throws NoSuchMethodException {
        throw new NoSuchMethodException("JSONString is not implemented");
    }
}
