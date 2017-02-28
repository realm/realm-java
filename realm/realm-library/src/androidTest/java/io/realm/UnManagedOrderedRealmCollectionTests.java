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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import io.realm.entities.AllJavaTypes;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for all methods part of the the {@link OrderedRealmCollection} interface, that have a different behavior
 * than managed RealmCollection classes.
 *
 * Methods tested in this class:
 *
 * # OrderedRealmCollection
 *
 * - E first()
 * - E last()
 * - void sort(String field)
 * - void sort(String field, Sort sortOrder)
 * - void sort(String field1, Sort sortOrder1, String field2, Sort sortOrder2)
 * - void sort(String[] fields, Sort[] sortOrders)
 * - void deleteFromRealm(int location)
 * - void deleteFirstFromRealm()
 * - void deleteLastFromRealm();
 *
 * # List
 *
 *  - void add(int location, E object);
 *  - boolean addAll(int location, Collection<? extends E> collection);
 *  - E get(int location);
 *  - int indexOf(Object object);
 *  - int lastIndexOf(Object object);
 *  - ListIterator<E> listIterator();
 *  - ListIterator<E> listIterator(int location);
 *  - E remove(int location);
 *  - E set(int location, E object);
 *  - List<E> subList(int start, int end);
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
 * + boolean isLoaded();
 * + boolean load();
 * + boolean isValid();
 * + BaseRealm getRealm();
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
 **
 * See {@link ManagedOrderedRealmCollectionTests} for similar tests for the managed behavior.
 */
@RunWith(Parameterized.class)
public class UnManagedOrderedRealmCollectionTests extends CollectionTests {

    private static final int TEST_SIZE = 10;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private final UnManagedCollection collectionClass;
    private Realm realm;
    private OrderedRealmCollection<AllJavaTypes> collection;

    @Parameterized.Parameters(name = "{0}")
    public static List<UnManagedCollection> data() {
        return Arrays.asList(UnManagedCollection.values());
    }

    public UnManagedOrderedRealmCollectionTests(UnManagedCollection collectionType) {
        this.collectionClass = collectionType;
    }

    @Before
    public void setup() {
        realm = Realm.getInstance(configFactory.createConfiguration());
        collection = createCollection(collectionClass);
    }

    private OrderedRealmCollection<AllJavaTypes> createCollection(UnManagedCollection collectionClass) {
        switch (collectionClass) {
            case UNMANAGED_REALMLIST:
                return populateInMemoryList(TEST_SIZE);

            default:
                throw new AssertionError("Unsupported class: " + collectionClass);
        }
    }

    @After
    public void tearDown() {
        realm.close();
    }

    @Test
    public void unsupportedMethods_unManagedCollections() {
        // RealmCollection methods.
        for (OrderedRealmCollectionMethod method : OrderedRealmCollectionMethod.values()) {
            try {
                switch (method) {
                    case DELETE_INDEX: collection.deleteFromRealm(0); break;
                    case DELETE_FIRST: collection.deleteFirstFromRealm(); break;
                    case DELETE_LAST: collection.deleteLastFromRealm(); break;
                    case SORT: collection.sort(AllJavaTypes.FIELD_STRING); break;
                    case SORT_FIELD: collection.sort(AllJavaTypes.FIELD_STRING, Sort.ASCENDING); break;
                    case SORT_2FIELDS: collection.sort(AllJavaTypes.FIELD_STRING, Sort.ASCENDING, AllJavaTypes.FIELD_LONG, Sort.DESCENDING); break;
                    case SORT_MULTI: collection.sort(new String[] { AllJavaTypes.FIELD_STRING, AllJavaTypes.FIELD_LONG }, new Sort[] { Sort.ASCENDING, Sort.DESCENDING }); break;
                    case CREATE_SNAPSHOT: collection.createSnapshot();
                }
                fail(method + " should have thrown an exception.");
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }

    @Test
    public void isLoaded() {
        assertTrue(collection.isLoaded());
    }

    @Test
    public void load() {
        assertTrue(collection.load());
    }

    @Test
    public void isValid() {
        assertTrue(collection.isValid());
    }

}
