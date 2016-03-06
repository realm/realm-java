/*
 * Copyright 2016 Realm Inc.
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

import java.util.List;

/**
 * A {@code OrderedRealmCollection} is a collection which maintains an ordering for its elements. Every
 * element in the {@code OrderedRealmCollection} has an index. Each element can thus be accessed by its
 * index, with the first index being zero. Normally, {@code OrderedRealmCollection}s allow duplicate
 * elements, as compared to Sets, where elements have to be unique.
 */
public interface OrderedRealmCollection<E extends RealmObject> extends List<E>, RealmCollection<E> {

    /**
     * Gets the first object from the list or {@code null} if the collection is empty.
     *
     * @return the first object or {@code null} if the collection is empty.
     */
    E first();

    /**
     * Gets the last object from the list or {@code null} if the collection is empty.
     *
     * @return the last object or {@code null} if the collection is empty.
     */
    E last();

    /**
     * Sorts (ascending) a collection..
     *
     * @param fieldName the field name to sort by. Only fields of type boolean, short, int, long, float, double, Date,
     *                  and String are supported.
     * @throws java.lang.IllegalArgumentException if field name does not exist or it has an invalid type.
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     *                                         an un-managed collection.
     * @return A sorted view of the collection.
     */
    RealmResults<E> sort(String fieldName);

    /**
     * Sorts existing collection in-place.
     *
     * @param fieldName the field name to sort by. Only fields of type boolean, short, int, long, float, double, Date,
     *                  and String are supported.
     * @param sortOrder the direction to sort by.
     * @throws java.lang.IllegalArgumentException if field name does not exist or has an invalid type.
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     *                                         an un-managed collection.
     * @return A sorted copy of the collection.
     */
    RealmResults<E> sort(String fieldName, Sort sortOrder);

    /**
     * Sorts existing {@link io.realm.RealmResults} in-place using two fields.
     *
     * @param fieldName1 first field name.
     * @param sortOrder1 sort order for first field.
     * @param fieldName2 second field name.
     * @param sortOrder2 sort order for second field.
     * @throws java.lang.IllegalArgumentException if a field name does not exist or has an invalid type.
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     *                                         an un-managed collection.
     * @return a sorted view of the collection.
     */
    RealmResults<E> sort(String fieldName1, Sort sortOrder1, String fieldName2, Sort sortOrder2);

    /**
     * Sorts existing {@link io.realm.RealmResults} in-place.
     *
     * @param fieldNames an array of field names to sort by. Only fields of type boolean, short, int, long, float,
     *                   double, Date, and String are supported.
     * @param sortOrders the directions to sort by.
     * @throws java.lang.IllegalArgumentException if a field name does not exist or has an invalid type.
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     *                                         an un-managed collection.
     * @return a sorted view of the collection.
     */
    RealmResults<E> sort(String[] fieldNames, Sort[] sortOrders);

    /**
     * Deletes the object at the given index from the Realm. This also removes it from the collection.
     *
     * @param location the array index identifying the object to be removed.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}.
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     *                                         an un-managed collection.
     */
    void deleteFromRealm(int location);

    /**
     * Deletes the first object from the Realm. This also removes it from this collection.
     *
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     *                                         an un-managed collection.
     * @return {@code true} if an object was deleted, {@code false} otherwise.
     */
    boolean deleteFirstFromRealm();

    /**
     * Deletes the last object from the Realm. This also removes it from this collection.
     *
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     *                                         an un-managed collection.
     * @return {@code true} if an object was deleted, {@code false} otherwise.
     */
    boolean deleteLastFromRealm();
}
