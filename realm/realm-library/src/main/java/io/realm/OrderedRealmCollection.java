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

import javax.annotation.Nullable;


/**
 * An {@code OrderedRealmCollection} is a collection which maintains an ordering for its elements. Every
 * element in the {@code OrderedRealmCollection} has an index. Each element can thus be accessed by its
 * index, with the first index being zero. Normally, {@code OrderedRealmCollection}s allow duplicate
 * elements, as compared to Sets, where elements have to be unique.
 * <p>
 * <p>
 * <p>
 * There are three types of {@link OrderedRealmCollection}. {@link RealmResults} and {@link RealmList} are live
 * collections. They are up-to-date all the time and they will never contain an invalid {@link RealmObject}.
 * {@link OrderedRealmCollectionSnapshot} is different. An {@link OrderedRealmCollectionSnapshot} can be created from
 * another {@link OrderedRealmCollection}. Its size and elements order stay the same as the original collection's when
 * it was created. {@link OrderedRealmCollectionSnapshot} may contain invalid {@link RealmObject}s if the objects get
 * deleted.
 * <p>
 * <p>
 * <p>
 * <div class="loops"></div>
 * Using iterators to iterate on {@link OrderedRealmCollection} will always work. You can delete or modify the elements
 * without impacting the iterator. See below example:
 * <p>
 * <pre>
 * {@code
 * RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
 * int s = dogs.size(); // 10
 * realm.beginTransaction();
 * for (Dog dog : dogs) {
 *     dog.deleteFromRealm();
 *     s = dogs.size(); // This will be decreased by 1 every time after a dog is removed.
 * }
 * realm.commitTransaction();
 * s = dogs.size(); // 0
 * }
 * </pre>
 * <p>
 * An iterator created from a live collection will create a stable view when the iterator is created, allowing you to
 * delete and modify elements while iterating without impacting the iterator. However, the {@code RealmResults} backing
 * the iterator will still be live updated meaning that size and order of elements can change when iterating.
 * {@link RealmList} has the same behaviour as {@link RealmResults} since they are both live collections.
 * <p>
 * <p>
 * <p>
 * A simple for-loop is different. See below example:
 * <p>
 * <pre>
 * {@code
 * RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
 * realm.beginTransaction();
 * for (int i = 0; i < dogs.size(); i++) {
 *     dogs.get(i).deleteFromRealm();
 * }
 * realm.commitTransaction();
 * s = dogs.size(); // 5
 * }
 * </pre>
 * <p>
 * The above example only deletes half of elements in the {@link RealmResults}. This is because of {@code dogs.size()}
 * decreased by 1 for every loop. The deletion happens in the loop will immediately impact the size of
 * {@code RealmResults}. To solve this problem, you can create a {@link OrderedRealmCollectionSnapshot} from the
 * {@link RealmResults} or {@link RealmList} and do simple for-loop on that instead:
 * <p>
 * <pre>
 * {@code
 * RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
 * OrderedRealmCollectionSnapshot snapshot = dogs.createSnapshot();
 * // dogs.size() == 10 && snapshot.size() == 10
 * realm.beginTransaction();
 * for (int i = 0; i < snapshot.size(); i++) {
 *     snapshot.get(0).deleteFromRealm();
 *     // snapshot.get(0).isValid() == false
 * }
 * realm.commitTransaction();
 * // dogs.size() == 0 && snapshot.size() == 10
 * }
 * </pre>
 * <p>
 * As you can see, after deletion, the size and elements order of snapshot stay the same as before. But the element at
 * the position becomes invalid.
 */
public interface OrderedRealmCollection<E> extends List<E>, RealmCollection<E> {

    /**
     * Gets the first object from the collection.
     *
     * @return the first object.
     * @throws IndexOutOfBoundsException if the collection is empty.
     */
    @Nullable
    E first();

    /**
     * Gets the first object from the collection. If the collection is empty, the provided default will be used instead.
     *
     * @return the first object or the provided default.
     */
    @Nullable
    E first(@Nullable E defaultValue);

    /**
     * Gets the last object from the collection.
     *
     * @return the last object.
     * @throws IndexOutOfBoundsException if the collection is empty.
     */
    @Nullable
    E last();

    /**
     * Gets the last object from the collection. If the collection is empty, the provided default will be used instead.
     *
     * @return the last object or the provided default.
     */
    @Nullable
    E last(@Nullable E defaultValue);

    /**
     * Sorts a collection based on the provided field in ascending order.
     *
     * @param fieldName the field name to sort by. Only fields of type boolean, short, int, long, float, double, Date,
     * and String are supported.
     * @return a new sorted {@link RealmResults} will be created and returned. The original collection stays unchanged.
     * @throws java.lang.IllegalArgumentException if field name does not exist or it has an invalid type.
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     * an unmanaged collection.
     */
    RealmResults<E> sort(String fieldName);

    /**
     * Sorts a collection based on the provided field and sort order.
     *
     * @param fieldName the field name to sort by. Only fields of type boolean, short, int, long, float, double, Date,
     * and String are supported.
     * @param sortOrder the direction to sort by.
     * @return a new sorted {@link RealmResults} will be created and returned. The original collection stays unchanged.
     * @throws java.lang.IllegalArgumentException if field name does not exist or has an invalid type.
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     * an unmanaged collection.
     */
    RealmResults<E> sort(String fieldName, Sort sortOrder);

    /**
     * Sorts a collection based on the provided fields and sort orders.
     *
     * @param fieldName1 first field name. Only fields of type boolean, short, int, long, float,
     * double, Date, and String are supported.
     * @param sortOrder1 sort order for first field.
     * @param fieldName2 second field name. Only fields of type boolean, short, int, long, float,
     * double, Date, and String are supported.
     * @param sortOrder2 sort order for second field.
     * @return a new sorted {@link RealmResults} will be created and returned. The original collection stays unchanged.
     * @throws java.lang.IllegalArgumentException if a field name does not exist or has an invalid type.
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     * an unmanaged collection.
     */
    RealmResults<E> sort(String fieldName1, Sort sortOrder1, String fieldName2, Sort sortOrder2);

    /**
     * Sorts a collection based on the provided fields and sort orders.
     *
     * @param fieldNames an array of field names to sort by. Only fields of type boolean, short, int, long, float,
     * double, Date, and String are supported.
     * @param sortOrders the directions to sort by.
     * @return a new sorted {@link RealmResults} will be created and returned. The original collection stays unchanged.
     * @throws java.lang.IllegalArgumentException if a field name does not exist or has an invalid type.
     * @throws java.lang.IllegalStateException if the Realm is closed, called on the wrong thread or the collection is
     * an unmanaged collection.
     */
    RealmResults<E> sort(String[] fieldNames, Sort[] sortOrders);

    /**
     * Deletes the object at the given index from the Realm. This also removes it from the collection.
     *
     * @param location the array index identifying the object to be removed.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}.
     * @throws java.lang.IllegalStateException if the Realm is closed or the method is called from the wrong thread.
     * @throws UnsupportedOperationException if the collection is unmanaged.
     */
    void deleteFromRealm(int location);

    /**
     * Deletes the first object from the Realm. This also removes it from this collection.
     *
     * @return {@code true} if an object was deleted, {@code false} otherwise.
     * @throws java.lang.IllegalStateException if the Realm is closed or the method is called on the wrong thread.
     * @throws UnsupportedOperationException if the collection is unmanaged.
     */
    boolean deleteFirstFromRealm();

    /**
     * Deletes the last object from the Realm. This also removes it from this collection.
     *
     * @return {@code true} if an object was deleted, {@code false} otherwise.
     * @throws java.lang.IllegalStateException if the Realm is closed or the method is called from the wrong thread.
     * @throws UnsupportedOperationException if the collection is unmanaged.
     */
    boolean deleteLastFromRealm();

    /**
     * Creates a snapshot from this {@link OrderedRealmCollection}.
     *
     * @return the snapshot of this collection.
     * @throws java.lang.IllegalStateException if the Realm is closed or the method is called from the wrong thread.
     * @throws UnsupportedOperationException if the collection is unmanaged.
     * @see OrderedRealmCollectionSnapshot
     */
    OrderedRealmCollectionSnapshot<E> createSnapshot();
}
