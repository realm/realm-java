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
import io.realm.annotations.RealmClass;

@RealmClass
public abstract class RealmObject {

    protected Row row;
    protected Realm realm = null;

    protected Realm getRealm() {
        return realm;
    }

    protected void setRealm(Realm realm) {
        this.realm = realm;
    }

    public Row realmGetRow() {
        return row;
    }

    public void realmSetRow(Row row) {
        this.row = row;
    }

    // Creating objects

    /**
     * Create a RealmObject in the default Realm with a set of given values.
     *
     * @param arguments   Values for the fields, encoded as plain Java objects. The order
     *                     of the arguments must match the order of the fields in the class
     *                     declaration.
     */
    abstract public void create(Object... arguments);


    /**
     * Create a RealmObject in the specified Realm with a set of given values.
     *
     * @param realm        The Realm instance to add object to.
     * @param arguments    Values for the fields, encoded as plain Java objects. The order
     *                     of the arguments must match the order of the fields in the class
     *                     declaration.
     */
    abstract public void create(Realm realm, Object... arguments);

    // Find objects

    /**
     * Get all objects of this type from the default Realm.
     *
     * @return       A RealmList of objects of the same class
     * @see          io.realm.RealmList
     */
    abstract public RealmList<?> all();

    /**
     * Get all objects of this type from the specified Realm.
     *
     * @param realm    The Realm instance to query.
     * @return         A RealmList of all objects of this type in the specified Realm.
     * @see io.realm.RealmList
     */
     abstract public RealmList<?> all(Realm realm);


    /**
     * Get a query for this type from the default Realm.
     *
     * @return        A RealmQuery object.
     * @see io.realm.RealmQuery
     */
    abstract public RealmQuery where();

    /**
     * Get a query for this type from the specified Realm.
     *
     * @param realm     The Realm instance to query.
     * @return          A RealmQuery object.
     * @see io.realm.RealmObject
     */
    abstract public RealmQuery where(Realm realm);

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj     The object to compare with.
     * @return        true if the objects are equal, false otherwise.
     */
    abstract public boolean equals(Object obj);
}
