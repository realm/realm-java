/*
 * Copyright 2015 Realm Inc.
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

package io.realm.internal.android;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.internal.TableView;

/**
 * Adapter for a {@link RealmQuery} that adds new methods to perform operations across
 * different Realms
 * @param <E> type of the object which is to be queried for
 */
class RealmQueryAdapter<E extends RealmObject> extends RealmQuery<E> {
    public RealmQueryAdapter(Realm realm, Class<E> clazz) {
        super(realm, clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmQueryAdapter<E> between(String fieldName, int from, int to) {
        super.between(fieldName, from, to);
        return this;
    }

    /**
     * Perform the query on the Realm referenced by #backgroundRealmPtr then handover
     * the result to the Realm referenced by #callerRealmPtr
     * @param callerRealmPtr pointer to the caller Realm originally starting this query
     * @param backgroundRealmPtr pointer to the background Realm used to perform query in a worker thread
     * @return {@link TableView} ready to be used from the caller Realm
     */
    public TableView findAll(long callerRealmPtr, long backgroundRealmPtr) {
        return getTableQuery().findAllWithHandover(callerRealmPtr, backgroundRealmPtr);
    }
}
