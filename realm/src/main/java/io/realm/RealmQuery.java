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


import io.realm.base.BaseRealmQuery;
import io.realm.internal.LinkView;
import io.realm.internal.TableView;

/**
 * A RealmQuery encapsulates a query on a {@link io.realm.Realm} or a {@link io.realm.RealmResults}
 * using the Builder pattern. The query is executed using either {@link #findAll()} or
 * {@link #findFirst()}
 * <p>
 * The input to many of the query functions take a field name as String. Note that this is not
 * type safe. If a model class is refactored care has to be taken to not break any queries.
 * <p>
 * A {@link io.realm.Realm} is unordered, which means that there is no guarantee that querying a
 * Realm will return the objects in the order they where inserted. Use
 * {@link #findAllSorted(String)} and similar methods if a specific order is required.
 * <p>
 * A RealmQuery cannot be passed between different threads.
 *
 * @param <E> The class of the objects to be queried.
 * @see <a href="http://en.wikipedia.org/wiki/Builder_pattern">Builder pattern</a>
 * @see Realm#where(Class)
 * @see RealmResults#where()
 */
public class RealmQuery<E extends RealmObject> extends BaseRealmQuery<E, RealmQuery<E>> {

    public static final boolean CASE_SENSITIVE = true;
    public static final boolean CASE_INSENSITIVE = false;

    private final Class<E> clazz;
    private Realm realm;

    /**
     * Creates a RealmQuery instance.
     *
     * @param realm  The realm to query within.
     * @param clazz  The class to query.
     * @throws java.lang.RuntimeException Any other error.
     */
    public RealmQuery(Realm realm, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.table = realm.getTable(clazz);
        this.query = table.where();
        this.columns = realm.columnIndices.getClassFields(clazz);
    }

    /**
     * Create a RealmQuery instance from a @{link io.realm.RealmResults}.
     *
     * @param realmList   The @{link io.realm.RealmResults} to query
     * @param clazz       The class to query
     * @throws java.lang.RuntimeException Any other error
     */
    public RealmQuery(RealmResults realmList, Class<E> clazz) {
        this.realm = realmList.getRealm();
        this.clazz = clazz;
        this.table = realm.getTable(clazz);
        this.query = realmList.getTable().where();
        this.columns = realm.columnIndices.getClassFields(clazz);
    }

    RealmQuery(Realm realm, LinkView view, Class<E> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.query = view.where();
        this.view = view;
        this.table = realm.getTable(clazz);
        this.columns = realm.columnIndices.getClassFields(clazz);
    }

    @Override
    protected RealmQuery getSelf() {
        return this;
    }

    @Override
    protected RealmResults<E> getResults(TableView queryResult) {
        return new RealmResults<E>(realm, queryResult, clazz);
    }

    @Override
    protected E getObject(long rowIndex) {
        return realm.get(clazz ,rowIndex);
    }
}

