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

import io.realm.RealmQuery;
import io.realm.RealmResults;

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
public class DynamicRealmQuery extends RealmQuery<DynamicRealmObject> {

    /**
     * Creates a DynamicRealmQuery instance.
     *
     * @param realm  The DynamicRealm realm to query within.
     * @param className  The class to query.
     */
    public DynamicRealmQuery(DynamicRealm realm, String className) {
        super(realm, className);
    }
}
