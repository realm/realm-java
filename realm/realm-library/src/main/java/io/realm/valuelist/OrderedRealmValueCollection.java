/*
 * Copyright 2017 Realm Inc.
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

package io.realm.valuelist;

import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.OrderedRealmCollectionSnapshot;
import io.realm.Sort;


public interface OrderedRealmValueCollection<E> extends List<E>, RealmValueCollection<E> {


    boolean isNull(int index);

    /**
     * Sorts a collection based on the provided field in ascending order.
     *
     * @return a new sorted {@link RealmValueListResults} will be created and returned. The original collection stays unchanged.
     * @throws java.lang.IllegalArgumentException if field name does not exist or it has an invalid type.
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     * an unmanaged collection.
     */
    RealmValueListResults<E> sort();


    /**
     * Sorts a collection based on the provided field and sort order.
     *
     * @param sortOrder the direction to sort by.
     * @return a new sorted {@link RealmValueListResults} will be created and returned. The original collection stays unchanged.
     * @throws java.lang.IllegalArgumentException if field name does not exist or has an invalid type.
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     * an unmanaged collection.
     */
    RealmValueListResults<E> sort(Sort sortOrder);

    /**
     * Creates a snapshot from this {@link OrderedRealmCollection}.
     *
     * @return the snapshot of this collection.
     * @throws java.lang.IllegalStateException if the Realm is closed or the method is called from the wrong thread.
     * @throws UnsupportedOperationException if the collection is unmanaged.
     * @see OrderedRealmCollectionSnapshot
     */
    RealmValueListSnapshot<E> createSnapshot();
}
