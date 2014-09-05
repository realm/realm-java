package io.realm;

import io.realm.internal.IOException;
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

    /*
    /**
     * Get all objects of this type from the default Realm.
     *
     * @return       A RealmList of objects of the same class
     * @see          io.realm.RealmList
     */
    public static RealmList<?> all() {
        Realm realm;

        realm = NULL;
        try {
            realm = new Realm();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return realm.allObjects();
    }

    /**
     * Get all objects of this type from the specified Realm.
     *
     * @param realm    The Realm instance to query.
     * @return         An RealmList of all objects of this type in the specified Realm.
     * @see io.realm.RealmList
     */
    public static RealmList all(Realm realm) {
        Class clazz;

        clazz = this.getClass();
        return realm.allObjects(clazz);
    }

    /**
     * Get a query for this type from the default Realm.
     *
     * @return        An RealmQuery object.
     * @see io.realm.RealmQuery
     */
    public static RealmQuery where() {
        Class clazz;
        Realm realm;

        realm = new Realm();
        clazz = this.getClass();
        return realm.where(clazz);
    }

    /**
     * Get a query for this type from the specified Realm.
     *
     * @param realm     The Realm instance to query.
     * @return          An RealmQuery object.
     * @see io.realm.RealmObject
     */
    public static RealmQuery where(Realm realm) {
        Class clazz;

        clazz = this.getClass();
        return realm.where(clazz);
    }

    */

}
