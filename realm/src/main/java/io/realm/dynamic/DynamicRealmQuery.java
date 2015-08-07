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
 *
 */

package io.realm.dynamic;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import io.realm.RealmResults;
import io.realm.base.BaseRealmQuery;
import io.realm.internal.LinkView;
import io.realm.internal.Table;
import io.realm.internal.TableView;

/**
 * A DynamicRealmQuery encapsulates a query on a {@link io.realm.dynamic.DynamicRealm} or a
 * {@link io.realm.RealmResults} using the Builder pattern. The query is executed using either
 * {@link #findAll()} or {@link #findFirst()}
 * <p>
 * The input to many of the query functions take a field name as String. Note that this is not
 * type safe. If a model class is refactored care has to be taken to not break any queries.
 * <p>
 * A {@link io.realm.dynamic.DynamicRealm} is unordered, which means that there is no guarantee that
 * querying a Realm will return the objects in the order they where inserted. Use
 * {@link #findAllSorted(String)} and similar methods if a specific order is required.
 * <p>
 * A DynamicRealmQuery cannot be passed between different threads.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Builder_pattern">Builder pattern</a>
 * @see DynamicRealm#where(String)
 * @see RealmResults#where()
 */
public class DynamicRealmQuery extends BaseRealmQuery<DynamicRealmObject, DynamicRealmQuery> {

    private final DynamicRealm realm;
    private final String className;

    /**
     * Creates a RealmQuery instance.
     *
     * @param realm  The realm to query within.
     * @param className  The class to query.
     * @throws java.lang.RuntimeException Any other error.
     */
    public DynamicRealmQuery(DynamicRealm realm, String className) {
        this.realm = realm;
        this.className = className;
        this.table = realm.getTable(className);
        this.query = table.where();
        this.columns = new ColumnMap(table);
    }

    /**
     * Create a RealmQuery instance from a @{link io.realm.RealmResults}.
     *
     * @param queryResults   The @{link io.realm.RealmResults} to query
     * @param className       The class to query
     * @throws java.lang.RuntimeException Any other error
     */
    public DynamicRealmQuery(DynamicRealmResults queryResults, String className) {
        this.realm = queryResults.getRealm();
        this.className = className;
        this.table = realm.getTable(className);
        this.query = queryResults.getTable().where();
        this.columns = new ColumnMap(table);
    }

    DynamicRealmQuery(DynamicRealm realm, LinkView view, String className) {
        this.realm = realm;
        this.className = className;
        this.query = view.where();
        this.view = view;
        this.table = realm.getTable(className);
        this.columns = new ColumnMap(table);
    }

    @Override
    protected DynamicRealmQuery getSelf() {
        return this;
    }

    @Override
    protected RealmResults<DynamicRealmObject> getResults(TableView queryResult) {
        return null;
    }

    @Override
    protected DynamicRealmObject getObject(long rowIndex) {
        return realm.get(className, rowIndex);
    }

    // TODO Replace with Schema when it is available
    private static class ColumnMap implements Map<String, Long> {

        private final Table table;

        public ColumnMap(Table table) {
            this.table = table;
        }

        @Override
        public Long get(Object key) {
            return table.getColumnIndex((String) key);
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Entry<String, Long>> entrySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> keySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long put(String key, Long value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ? extends Long> map) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Long> values() {
            throw new UnsupportedOperationException();
        }
    }
}
