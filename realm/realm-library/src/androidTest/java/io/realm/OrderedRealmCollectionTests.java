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

import android.util.Pair;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.realm.entities.AllJavaTypes;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Test class for all methods specific to OrderedRealmCollections no matter if they are managed or unmanaged.
 *
 * Methods tested in this class:
 *
 * # RealmOrderedCollection
 *
 * + E first();
 * + E last();
 * - void sort(String field);
 * - void sort(String field, Sort sortOrder);
 * - void sort(String field1, Sort sortOrder1, String field2, Sort sortOrder2);
 * - void sort(String[] fields, Sort[] sortOrders);
 * - void deleteFromRealm(int location);
 * - void deleteFirstFromRealm();
 * - void deleteLastFromRealm();
 * - OrderedRealmCollectionSnapshot createSnapshot();
 *
 * # List
 *
 *  - void add(int location, E object);
 *  - boolean addAll(int location, Collection<? extends E> collection);
 *  + E get(int location);
 *  + int indexOf(Object object);
 *  + int lastIndexOf(Object object);
 *  - ListIterator<E> listIterator();
 *  - ListIterator<E> listIterator(int location);
 *  - E remove(int location);
 *  - E set(int location, E object);
 *  + List<E> subList(int start, int end);
 *
 * # RealmCollection
 *
 * - RealmQuery<E> where();
 * - Number min(String fieldName);
 * - Number max(String fieldName);
 * - Number sum(String fieldName);
 * - double average(String fieldName);
 * - Date maxDate(String fieldName);
 * - Date minDate(String fieldName);
 * - void deleteAllFromRealm();
 * - boolean isLoaded();
 * - boolean load();
 * - boolean isValid();
 * - BaseRealm getRealm();
 *
 * # Collection
 *
 * - public boolean add(E object);
 * - public boolean addAll(Collection<? extends E> collection);
 * - public void deleteAll();
 * - public boolean contains(Object object);
 * - public boolean containsAll(Collection<?> collection);
 * - public boolean equals(Object object);
 * - public int hashCode();
 * - public boolean isEmpty();
 * - public Iterator<E> iterator();
 * - public boolean remove(Object object);
 * - public boolean removeAll(Collection<?> collection);
 * - public boolean retainAll(Collection<?> collection);
 * - public int size();
 * - public Object[] toArray();
 * - public <T> T[] toArray(T[] array);
 *
 * @see RealmCollectionTests
 * @see ManagedRealmCollectionTests
 * @see UnManagedRealmCollectionTests
 */
@RunWith(Parameterized.class)
public class OrderedRealmCollectionTests extends CollectionTests {

    private static final int TEST_SIZE = 10;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final CollectionClass collectionClass;
    private Realm realm;
    private OrderedRealmCollection<AllJavaTypes> collection;

    @Parameterized.Parameters(name = "{0}")
    public static List<CollectionClass> data() {
        return Arrays.asList(CollectionClass.values());
    }

    public OrderedRealmCollectionTests(CollectionClass collectionType) {
        this.collectionClass = collectionType;
    }

    @Before
    public void setup() {
        realm = Realm.getInstance(configFactory.createConfiguration());
        collection = createCollection(realm, collectionClass);
    }

    @After
    public void tearDown() {
        realm.close();
    }

    private OrderedRealmCollection<AllJavaTypes> createCollection(Realm realm, CollectionClass collectionClass) {
        OrderedRealmCollection<AllJavaTypes> orderedCollection;
        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case MANAGED_REALMLIST:
                populateRealm(realm, TEST_SIZE);
                orderedCollection = realm.where(AllJavaTypes.class)
                        .equalTo(AllJavaTypes.FIELD_LONG, 0)
                        .findFirst()
                        .getFieldList();
                break;

            case UNMANAGED_REALMLIST:
                return populateInMemoryList(TEST_SIZE);

            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
            case REALMRESULTS:
                populateRealm(realm, TEST_SIZE);
                orderedCollection = realm.where(AllJavaTypes.class).findAll();
                break;

            default:
                throw new AssertionError("Unsupported class: " + collectionClass);
        }
        if (isSnapshot(collectionClass)) {
            orderedCollection = orderedCollection.createSnapshot();
        }
        return orderedCollection;
    }

    private OrderedRealmCollection<AllJavaTypes> createEmptyCollection(Realm realm, CollectionClass collectionClass) {
        OrderedRealmCollection<AllJavaTypes> orderedCollection;
        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case MANAGED_REALMLIST:
                orderedCollection = realm.where(AllJavaTypes.class)
                        .equalTo(AllJavaTypes.FIELD_LONG, 1)
                        .findFirst()
                        .getFieldList();
                break;

            case UNMANAGED_REALMLIST:
                return new RealmList<AllJavaTypes>();

            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
            case REALMRESULTS:
                orderedCollection = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_LONG, -1).findAll();
                break;

            default:
                throw new AssertionError("Unsupported class: " + collectionClass);
        }
        if (isSnapshot(collectionClass)) {
            orderedCollection = orderedCollection.createSnapshot();
        }
        return orderedCollection;
    }

    private Pair<AllJavaTypes, OrderedRealmCollection<AllJavaTypes>> createCollectionWithMultipleCopies(Realm realm, CollectionClass collectionClass) {

        OrderedRealmCollection<AllJavaTypes>  orderedCollection;
        AllJavaTypes obj;
        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case MANAGED_REALMLIST:
                obj = realm.where(AllJavaTypes.class)
                        .equalTo(AllJavaTypes.FIELD_LONG, 1)
                        .findFirst();
                RealmList<AllJavaTypes> list = obj.getFieldList();
                realm.beginTransaction();
                list.add(obj);
                realm.commitTransaction();
                orderedCollection = list;
                break;

            case UNMANAGED_REALMLIST:
                obj = new AllJavaTypes(1);
                return new Pair<AllJavaTypes, OrderedRealmCollection<AllJavaTypes>>(obj, new RealmList<AllJavaTypes>(obj, obj));

            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
            case REALMRESULTS:
                RealmResults<AllJavaTypes> result = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_LONG, 1).findAll();
                obj = result.first();
                orderedCollection = result;
                break;

            default:
                throw new AssertionError("Unsupported class: " + collectionClass);
        }

        if (isSnapshot(collectionClass)) {
            orderedCollection = orderedCollection.createSnapshot();
        }

        return new Pair<AllJavaTypes, OrderedRealmCollection<AllJavaTypes>>(obj, orderedCollection);
    }

    @Test
    public void first() {
        assertEquals(collection.get(0), collection.first());
    }

    @Test
    public void first_emptyCollection() {
        collection = createEmptyCollection(realm, collectionClass);
        try {
            collection.first();
            fail();
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void first_withDefault() {
        AllJavaTypes defaultObject = new AllJavaTypes();
        collection = createEmptyCollection(realm, collectionClass);
        assertEquals(defaultObject, collection.first(defaultObject));
        assertEquals(null, collection.first(null)); // Null is an acceptable default
    }

    @Test
    public void last() {
        assertEquals(collection.get(TEST_SIZE - 1), collection.last());
    }

    @Test
    public void last_emptyCollection() {
        collection = createEmptyCollection(realm, collectionClass);
        try {
            collection.last();
            fail();
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void last_withDefault() {
        AllJavaTypes defaultObject = new AllJavaTypes();
        collection = createEmptyCollection(realm, collectionClass);
        assertEquals(defaultObject, collection.last(defaultObject));
        assertEquals(null, collection.last(null)); // Null is an acceptable default
    }

    @Test
    public void get_validIndex() {
        AllJavaTypes first = collection.get(0);
        assertEquals(0, first.getFieldInt());

        AllJavaTypes last = collection.get(TEST_SIZE - 1);
        assertEquals(TEST_SIZE - 1, last.getFieldInt());
    }

    @Test
    public void get_indexOutOfBounds() {
        List<Integer> indexes = Arrays.asList(-1, TEST_SIZE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        for (Integer index : indexes) {
            try {
                collection.get(index);
                fail(index + " did not throw the expected Exception.");
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
    }

    @Test
    public void indexOf() {
        AllJavaTypes obj = collection.get(1);
        assertEquals(1, collection.indexOf(obj));
    }

    @Test
    public void indexOf_null() {
        assertEquals(-1, collection.indexOf(null));
    }

    @Test
    public void indexOf_objectNotInRealm() {
        assertEquals(-1, collection.indexOf(new AllJavaTypes()));
    }

    @Test
    public void lastIndexOf() {
        Pair<AllJavaTypes, OrderedRealmCollection<AllJavaTypes>> data = createCollectionWithMultipleCopies(realm, collectionClass);
        AllJavaTypes obj = data.first;
        collection = data.second;
        int lastIndex = collection.lastIndexOf(obj);
        assertEquals(collection.size() - 1, lastIndex);
    }

    @Test
    public void lastIndexOf_null() {
        assertEquals(-1, collection.lastIndexOf(null));
    }

    @Test
    public void lastIndexOf_objectNotInRealm() {
        assertEquals(-1, collection.lastIndexOf(new AllJavaTypes()));
    }

    @Test
    public void subList() {
        List<AllJavaTypes> list = collection.subList(0, 5);
        assertEquals(5, list.size());
        assertEquals(list.get(0), collection.get(0));
        assertEquals(list.get(4), collection.get(4));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void subList_invalidStartIndex() {
        collection.subList(-1, TEST_SIZE);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void subList_invalidEnd() {
        collection.subList(0, TEST_SIZE + 1);
    }

    // Checks that all relevant methods throw a correct IndexOutOfBounds
    @Test
    public void methods_indexOutOfBounds() {
        collection = createEmptyCollection(realm, collectionClass);

        for (ListMethod method : ListMethod.values()) {
            realm.beginTransaction();
            try {
                switch (method) {
                    case ADD_INDEX: collection.add(1, new AllJavaTypes()); break;
                    case ADD_ALL_INDEX: collection.addAll(1, Collections.singleton(new AllJavaTypes())); break;
                    case GET_INDEX: collection.get(1); break;
                    case LIST_ITERATOR_INDEX: collection.listIterator(1); break;
                    case REMOVE_INDEX: collection.remove(1); break;
                    case SET: collection.set(1, new AllJavaTypes()); break;
                    case SUBLIST: collection.subList(1, 2); break;

                    // Cannot fail with IndexOutOfBounds
                    case FIRST:
                    case LAST:
                    case INDEX_OF:
                    case LAST_INDEX_OF:
                    case LIST_ITERATOR:
                        continue;
                }
                fail(method + " did not throw an exception");
            } catch (IndexOutOfBoundsException ignored) {
            } catch (UnsupportedOperationException ignored) {
            } finally {
                realm.cancelTransaction();
            }
        }

        for (OrderedRealmCollectionMethod method : OrderedRealmCollectionMethod.values()) {
            realm.beginTransaction();
            try {
                switch (method) {
                    case DELETE_INDEX: collection.deleteFromRealm(1); break;

                    // Cannot fail with IndexOutOfBounds
                    case DELETE_FIRST:
                    case DELETE_LAST:
                    case SORT:
                    case SORT_FIELD:
                    case SORT_2FIELDS:
                    case SORT_MULTI:
                    case CREATE_SNAPSHOT:
                        continue;
                }
                fail(method + " did not throw an exception");
            } catch (IndexOutOfBoundsException ignored) {
            } catch (UnsupportedOperationException ignored) {
            } finally {
                realm.cancelTransaction();
            }
        }
    }

    @Test
    public void createSnapshot() {
        if (collectionClass == CollectionClass.UNMANAGED_REALMLIST) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollectionSnapshot snapshot = collection.createSnapshot();
        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
                // Creating snapshot from a snapshot will just return the same object.
                assertSame(collection, snapshot);
                break;
            case MANAGED_REALMLIST:
            case REALMRESULTS:
                int sizeBeforeChange = collection.size();
                realm.beginTransaction();
                collection.deleteLastFromRealm();
                realm.commitTransaction();
                assertEquals(sizeBeforeChange - 1, collection.size());
                assertEquals(sizeBeforeChange, snapshot.size());
                break;
            default:
                break;
        }
    }
}
