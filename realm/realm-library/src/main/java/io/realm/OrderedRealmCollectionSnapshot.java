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

package io.realm;

import java.util.Locale;

import io.realm.internal.OsResults;
import io.realm.internal.UncheckedRow;


/**
 * An {@link OrderedRealmCollectionSnapshot} is a special type of {@link OrderedRealmCollection}. It can be created by
 * calling {@link OrderedRealmCollection#createSnapshot()}. Unlike {@link RealmResults} and {@link RealmList}, its
 * size and order of elements will never be changed after creation.
 * <p>
 * {@link OrderedRealmCollectionSnapshot} is useful when making changes which may impact the size or order of the
 * collection in simple loops. For example:
 * <pre>
 * {@code
 * final RealmResults<Dog>  dogs = realm.where(Dog.class).findAll();
 * final OrderedRealmCollectionSnapshot<Dog> snapshot = dogs.createSnapshot();
 * final int dogsCount = snapshot.size(); // dogs.size() == snapshot.size() == 10
 * realm.executeTransaction(new Realm.Transaction() {
 *     /@Override
 *     public void execute(Realm realm) {
 *         for (int i = 0; i < dogsCount; i++) {
 *         // This won't work since RealmResults is always up-to-date, its size gets decreased by 1 after every loop. An
 *         // IndexOutOfBoundsException will be thrown after 5 loops.
 *         // dogs.deleteFromRealm(i);
 *         snapshot.deleteFromRealm(i); // Deletion on OrderedRealmCollectionSnapshot won't change the size of it.
 *         }
 *     }
 * });
 * }
 * </pre>
 */
public class OrderedRealmCollectionSnapshot<E> extends OrderedRealmCollectionImpl<E> {

    private int size = -1;

    OrderedRealmCollectionSnapshot(BaseRealm realm, OsResults osResults, Class<E> clazz) {
        super(realm, osResults.createSnapshot(), clazz);
    }

    OrderedRealmCollectionSnapshot(BaseRealm realm, OsResults osResults, String className) {
        super(realm, osResults.createSnapshot(), className);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        // Optimization for simple loops. The size of snapshot will never be changed.
        if (size == -1) {
            size = super.size();
        }
        return size;
    }

    /**
     * Not supported by {@link OrderedRealmCollectionSnapshot}. Use 'sort()' on the original
     * {@link OrderedRealmCollection} instead.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public RealmResults<E> sort(String fieldName) {
        throw getUnsupportedException("sort");
    }

    /**
     * Not supported by {@link OrderedRealmCollectionSnapshot}. Use 'sort()' on the original
     * {@link OrderedRealmCollection} instead.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public RealmResults<E> sort(String fieldName, Sort sortOrder) {
        throw getUnsupportedException("sort");
    }

    /**
     * Not supported by {@link OrderedRealmCollectionSnapshot}. Use 'sort()' on the original
     * {@link OrderedRealmCollection} instead.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public RealmResults<E> sort(String fieldName1, Sort sortOrder1, String fieldName2, Sort sortOrder2) {
        throw getUnsupportedException("sort");
    }

    /**
     * Not supported by {@link OrderedRealmCollectionSnapshot}. Use 'sort()' on the original
     * {@link OrderedRealmCollection} instead.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public RealmResults<E> sort(String[] fieldNames, Sort[] sortOrders) {
        throw getUnsupportedException("sort");
    }

    /**
     * Not supported by {@link OrderedRealmCollectionSnapshot}. Use 'where()' on the original
     * {@link OrderedRealmCollection} instead.
     *
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public RealmQuery<E> where() {
        throw getUnsupportedException("where");
    }

    private UnsupportedOperationException getUnsupportedException(String methodName) {
        return new UnsupportedOperationException(
                String.format(Locale.US, "'%s()' is not supported by OrderedRealmCollectionSnapshot. " +
                        "Call '%s()' on the original 'RealmCollection' instead.", methodName, methodName));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoaded() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean load() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderedRealmCollectionSnapshot<E> createSnapshot() {
        realm.checkIfValid();
        return this;
    }

    /**
     * Deletes the object at the given index from the Realm. The object at the given index will become invalid. Just
     * returns if the object is invalid already.
     *
     * @param location the array index identifying the object to be removed.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}.
     * @throws java.lang.IllegalStateException if the Realm is closed or the method is called from the wrong thread.
     */
    @Override
    public void deleteFromRealm(int location) {
        realm.checkIfValidAndInTransaction();
        UncheckedRow row = osResults.getUncheckedRow(location);
        if (row.isAttached()) {
            osResults.delete(location);
        }
    }

    /**
     * Deletes the first object from the Realm. The first object will become invalid.
     *
     * @return {@code true} if an object was deleted, {@code false} otherwise.
     * @throws java.lang.IllegalStateException if the Realm is closed or the method is called on the wrong thread.
     */
    @Override
    public boolean deleteFirstFromRealm() {
        realm.checkIfValidAndInTransaction();
        UncheckedRow row = osResults.firstUncheckedRow();
        return row != null && row.isAttached() && osResults.deleteFirst();
    }

    /**
     * Deletes the last object from the Realm. The last object will become invalid.
     *
     * @return {@code true} if an object was deleted, {@code false} otherwise.
     * @throws java.lang.IllegalStateException if the Realm is closed or the method is called from the wrong thread.
     */
    @Override
    public boolean deleteLastFromRealm() {
        realm.checkIfValidAndInTransaction();
        UncheckedRow row = osResults.lastUncheckedRow();
        return row != null && row.isAttached() && osResults.deleteLast();
    }

    /**
     * This deletes all objects in the collection from the underlying Realm. All objects in the collection snapshot
     * will become invalid.
     *
     * @return {@code true} if objects was deleted, {@code false} otherwise.
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    @Override
    public boolean deleteAllFromRealm() {
        return super.deleteAllFromRealm();
    }
}
