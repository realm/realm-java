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


import io.realm.base.BaseRealmResults;
import io.realm.internal.TableOrView;

/**
 * This class holds all the matches of a {@link io.realm.RealmQuery} for a given Realm. The objects
 * are not copied from the Realm to the RealmResults list, but are just referenced from the
 * RealmResult instead. This saves memory and increases speed.
 * <p>
 * RealmResults are live views, which means that if it is on an {@link android.os.Looper} thread,
 * it will automatically update its query results after a transaction has been committed. If on a
 * non-looper thread, {@link Realm#refresh()} must be called to update the results.
 * <p>
 * Updates to RealmObjects from a RealmResults list must be done from within a transaction and the
 * modified objects are persisted to the Realm file during the commit of the transaction.
 * <p>
 * A RealmResults object cannot be passed between different threads.
 * <p>
 * Notice that a RealmResults is never null not even in the case where it contains no objects. You
 * should always use the size() method to check if a RealmResults is empty or not.
 *
 * @param <E> The class of objects in this list
 * @see RealmQuery#findAll()
 * @see Realm#allObjects(Class)
 * @see io.realm.Realm#beginTransaction()
 */
public class RealmResults<E extends RealmObject> extends BaseRealmResults<E, RealmQuery<E>> {

    private Class<E> classSpec;
    private Realm realm;

    public static final boolean SORT_ORDER_ASCENDING = true;
    public static final boolean SORT_ORDER_DESCENDING = false;

    RealmResults(Realm realm, Class<E> classSpec) {
        super(realm.getTable(classSpec));
        this.realm = realm;
        this.classSpec = classSpec;
    }

    RealmResults(Realm realm, TableOrView table, Class<E> classSpec) {
        super(table);
        this.realm = realm;
        this.classSpec = classSpec;
    }

    Realm getRealm() {
        return realm;
    }

    @Override
    protected void checkIsRealmValid() {
        realm.checkIfValid();
    }

    @Override
    protected TableOrView getTable() {
        if (table == null) {
            return realm.getTable(classSpec);
        } else {
            return table;
        }
    }

    @Override
    protected RealmQuery<E> getQuery() {
        return new RealmQuery<E>(realm, classSpec);
    }

    @Override
    protected E getObject(long rowIndex) {
        return realm.get(classSpec, rowIndex);
    }
}
