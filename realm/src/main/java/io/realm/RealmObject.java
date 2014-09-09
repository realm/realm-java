/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm;

import io.realm.internal.Row;

public abstract class RealmObject {

    protected Row row;
    long realmAddedAtRowIndex = -1;
    protected Realm realm = null;

    /**
     * Get the Realm which the object belongs to.
     *
     * @return    The Realm.
     */
    protected Realm getRealm() {
        return realm;
    }

    protected void setRealm(Realm realm) {
        this.realm = realm;
    }

    protected Row realmGetRow() {
        return row;
    }

    protected void realmSetRow(Row row) {
        this.row = row;
    }

    // Find objects

    /**
     * Get all objects of this type from the default Realm.
     *
     * @return       A RealmList of objects of the same class
     * @see          io.realm.RealmList
     */
    public static RealmList<?> all() {
        throw new NoSuchMethodError();
    }

    /**
     * Get all objects of this type from the specified Realm.
     *
     * @param realm    The Realm instance to query.
     * @return         An RealmList of all objects of this type in the specified Realm.
     * @see io.realm.RealmList
     */
    public static RealmList<?> all(Realm realm) {
        throw new NoSuchMethodError();
    }

    /**
     * Get a query for this type from the default Realm.
     *
     * @return        An RealmQuery object.
     * @see io.realm.RealmQuery
     */
    public static RealmQuery where() {
        throw new NoSuchMethodError();
    }

    /**
     * Get a query for this type from the specified Realm.
     *
     * @param realm     The Realm instance to query.
     * @return          An RealmQuery object.
     * @see io.realm.RealmObject
     */
    public static RealmQuery where(Realm realm) {
        throw new NoSuchMethodError();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj     The object to compare with.
     * @return        true if the objects are equal, false otherwise.
     */
    public boolean equals(Object obj) {
        throw new NoSuchMethodError();
    }
}
