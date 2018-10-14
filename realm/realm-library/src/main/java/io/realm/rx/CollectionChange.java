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

package io.realm.rx;

import javax.annotation.Nullable;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Container wrapping the result of a {@link io.realm.OrderedRealmCollectionChangeListener} being triggered.
 * <p>
 * This is used by {@link RealmResults#asChangesetObservable()}} and {@link RealmList#asChangesetObservable()} as
 * RxJava is only capable of emitting one item, not multiple.
 */
public class CollectionChange<E extends OrderedRealmCollection> {

    private final E collection;
    private final OrderedCollectionChangeSet changeset;

    /**
     * Constructor for a CollectionChange.
     *
     * @param collection the collection that changed.
     * @param changeset the changeset describing the change.
     */
    public CollectionChange(E collection, @Nullable OrderedCollectionChangeSet changeset) {
        this.collection = collection;
        this.changeset = changeset;
    }

    /**
     * Returns the collection that was updated.
     *
     * @return collection that was updated.
     */
    public E getCollection() {
        return collection;
    }

    /**
     * Returns the changeset describing the update.
     * <p>
     * This will be {@code null} the first time the stream emits the collection as well as when a asynchronous query
     * is loaded for the first time.
     * <p>
     * <pre>
     * {@code
     * // Example
     * realm.where(Person.class).findAllAsync().asChangesetObservable()
     *   .subscribe(new Consumer<CollectionChange>() {
     *    \@Override
     *     public void accept(CollectionChange item) throws Exception {
     *       item.getChangeset(); // Will return null the first two times
     *   }
     * });
     * }
     * </pre>
     *
     * @return the changeset describing how the collection was updated.
     */
    @Nullable
    public OrderedCollectionChangeSet getChangeset() {
        return changeset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectionChange<?> that = (CollectionChange<?>) o;

        if (!collection.equals(that.collection)) return false;
        return changeset != null ? changeset.equals(that.changeset) : that.changeset == null;
    }

    @Override
    public int hashCode() {
        int result = collection.hashCode();
        result = 31 * result + (changeset != null ? changeset.hashCode() : 0);
        return result;
    }
}