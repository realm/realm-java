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
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.entities.NullTypes;
import io.realm.entities.Owner;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for all methods specific to OrderedRealmCollections that are not implementation specific.
 *
 * Methods tested in this class:
 *
 * # OrderedRealmCollection
 *
 * - E first()
 * - E last()
 * + void sort(String field)
 * + void sort(String field, Sort sortOrder)
 * + void sort(String field1, Sort sortOrder1, String field2, Sort sortOrder2)
 * + void sort(String[] fields, Sort[] sortOrders)
 * + void deleteFromRealm(int location)
 * + void deleteFirstFromRealm()
 * + void deleteLastFromRealm();
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
public class ManagedOrderedRealmCollectionTests extends CollectionTests {

    private static final int TEST_SIZE = 10;
    private final static int TEST_DATA_FIRST_HALF = (int) ((TEST_SIZE / 2.0D) - 1);
    private final static int TEST_DATA_LAST_HALF = (int) ((TEST_SIZE / 2.0D) + 1);

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final ManagedCollection collectionClass;
    private Realm realm;
    private OrderedRealmCollection<AllJavaTypes> collection;

    @Parameterized.Parameters(name = "{0}")
    public static List<ManagedCollection> data() {
        return Arrays.asList(ManagedCollection.values());
    }

    public ManagedOrderedRealmCollectionTests(ManagedCollection collectionType) {
        this.collectionClass = collectionType;
    }

    @Before
    public void setup() {
        realm = Realm.getInstance(configFactory.createConfiguration());
        populateRealm(realm, TEST_SIZE);
        collection = createCollection(collectionClass);
    }

    @After
    public void tearDown() {
        realm.close();
    }

    OrderedRealmCollection<AllJavaTypes> createCollection(ManagedCollection collectionClass) {
        OrderedRealmCollection<AllJavaTypes> orderedCollection;
        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case MANAGED_REALMLIST:
                orderedCollection = realm.where(AllJavaTypes.class)
                        .equalTo(AllJavaTypes.FIELD_LONG, 0)
                        .findFirst()
                        .getFieldList();
                break;

            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
            case REALMRESULTS:
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

    private OrderedRealmCollection<NullTypes> createEmptyCollection(Realm realm, ManagedCollection collectionClass) {
        OrderedRealmCollection<NullTypes> orderedCollection;
        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case MANAGED_REALMLIST:
                realm.beginTransaction();
                NullTypes obj = realm.createObject(NullTypes.class, 0);
                realm.commitTransaction();
                orderedCollection = obj.getFieldListNull();
                break;

            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
            case REALMRESULTS:
                orderedCollection = realm.where(NullTypes.class).findAll();
                break;
            default:
                throw new AssertionError("Unknown collection: " + collectionClass);
        }

        if (isSnapshot(collectionClass)) {
            orderedCollection = orderedCollection.createSnapshot();
        }
        return orderedCollection;
    }

    @Test
    public void sort_twoFields() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> sortedList = collection.sort(AllJavaTypes.FIELD_BOOLEAN, Sort.ASCENDING, AllJavaTypes.FIELD_LONG, Sort.DESCENDING);
        AllJavaTypes obj = sortedList.first();
        assertFalse(obj.isFieldBoolean());
        assertEquals(TEST_SIZE - 1, obj.getFieldLong());
    }

    @Test
    public void sort_boolean() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> sortedList = collection.sort(AllJavaTypes.FIELD_BOOLEAN, Sort.DESCENDING);
        assertEquals(TEST_SIZE, sortedList.size());
        assertEquals(false, sortedList.last().isFieldBoolean());
        assertEquals(true, sortedList.first().isFieldBoolean());
        assertEquals(true, sortedList.get(TEST_DATA_FIRST_HALF).isFieldBoolean());
        assertEquals(false, sortedList.get(TEST_DATA_LAST_HALF).isFieldBoolean());

        RealmResults<AllJavaTypes> reverseList = sortedList.sort(AllJavaTypes.FIELD_BOOLEAN, Sort.ASCENDING);
        assertEquals(TEST_SIZE, reverseList.size());
        assertEquals(true, reverseList.last().isFieldBoolean());
        assertEquals(false, reverseList.first().isFieldBoolean());
        assertEquals(false, reverseList.get(TEST_DATA_FIRST_HALF).isFieldBoolean());
        assertEquals(true, reverseList.get(TEST_DATA_LAST_HALF).isFieldBoolean());

        RealmResults<AllJavaTypes> reserveSortedList = reverseList.sort(AllJavaTypes.FIELD_BOOLEAN, Sort.DESCENDING);
        assertEquals(TEST_SIZE, reserveSortedList.size());
        assertEquals(reserveSortedList.first(), sortedList.first());
    }

    @Test
    public void sort_string() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> resultList = collection;
        OrderedRealmCollection<AllJavaTypes> sortedList = createCollection(collectionClass);
        sortedList = sortedList.sort(AllJavaTypes.FIELD_STRING, Sort.DESCENDING);

        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_SIZE, sortedList.size());
        assertEquals(resultList.first().getFieldString(), sortedList.last().getFieldString());

        RealmResults<AllJavaTypes> reverseList = sortedList.sort(AllJavaTypes.FIELD_STRING, Sort.ASCENDING);
        assertEquals(TEST_SIZE, reverseList.size());
        assertEquals(resultList.first().getFieldString(), reverseList.first().getFieldString());

        int numberOfDigits = 1 + ((int) Math.log10(TEST_SIZE));
        int largestNumber = 1;
        largestNumber = (int) (largestNumber * Math.pow(10, numberOfDigits - 1));
        largestNumber = largestNumber - 1;
        assertEquals(resultList.get(largestNumber).getFieldString(), reverseList.last().getFieldString());
        RealmResults<AllJavaTypes> reverseSortedList = reverseList.sort(AllJavaTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(TEST_SIZE, reverseSortedList.size());
    }

    @Test
    public void sort_double() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> resultList = collection;
        OrderedRealmCollection<AllJavaTypes> sortedList = createCollection(collectionClass);
        sortedList = sortedList.sort(AllJavaTypes.FIELD_DOUBLE, Sort.DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_SIZE, sortedList.size());
        assertEquals(resultList.first().getFieldDouble(), sortedList.last().getFieldDouble(), 0D);

        RealmResults<AllJavaTypes> reverseList = sortedList.sort(AllJavaTypes.FIELD_DOUBLE, Sort.ASCENDING);
        assertEquals(TEST_SIZE, reverseList.size());
        assertEquals(resultList.first().getFieldDouble(), reverseList.first().getFieldDouble(), 0D);
        assertEquals(resultList.last().getFieldDouble(), reverseList.last().getFieldDouble(), 0D);

        RealmResults<AllJavaTypes> reverseSortedList = reverseList.sort(AllJavaTypes.FIELD_DOUBLE, Sort.DESCENDING);
        assertEquals(TEST_SIZE, reverseSortedList.size());
    }

    @Test
    public void sort_float() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> resultList = collection;
        OrderedRealmCollection<AllJavaTypes> sortedList = createCollection(collectionClass);
        sortedList = sortedList.sort(AllJavaTypes.FIELD_FLOAT, Sort.DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_SIZE, sortedList.size());
        assertEquals(resultList.first().getFieldFloat(), sortedList.last().getFieldFloat(), 0D);

        RealmResults<AllJavaTypes> reverseList = sortedList.sort(AllJavaTypes.FIELD_FLOAT, Sort.ASCENDING);
        assertEquals(TEST_SIZE, reverseList.size());
        assertEquals(resultList.first().getFieldFloat(), reverseList.first().getFieldFloat(), 0D);
        assertEquals(resultList.last().getFieldFloat(), reverseList.last().getFieldFloat(), 0D);

        RealmResults<AllJavaTypes> reverseSortedList = reverseList.sort(AllJavaTypes.FIELD_FLOAT, Sort.DESCENDING);
        assertEquals(TEST_SIZE, reverseSortedList.size());
    }

    private void doTestSortOnColumnWithPartialNullValues(String fieldName,
                                                         OrderedRealmCollection<NullTypes> original,
                                                         OrderedRealmCollection<NullTypes> copy) {

        RealmResults<NullTypes> sortedList = copy.sort(fieldName, Sort.ASCENDING);
        assertEquals("Should have same size", original.size(), sortedList.size());
        // Null should always be the first one in the ascending sorted list.
        assertEquals(2, sortedList.first().getId());
        assertEquals(1, sortedList.last().getId());

        // Descending
        sortedList = sortedList.sort(fieldName, Sort.DESCENDING);
        assertEquals("Should have same size", original.size(), sortedList.size());
        assertEquals(1, sortedList.first().getId());
        // Null should always be the last one in the descending sorted list.
        assertEquals(2, sortedList.last().getId());
    }

    // Tests sort on nullable fields with null values partially.
    @Test
    public void sort_rowsWithPartialNullValues() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        populatePartialNullRowsForNumericTesting(realm);
        OrderedRealmCollection<NullTypes> original;
        OrderedRealmCollection<NullTypes> copy;
        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case MANAGED_REALMLIST:
                realm.beginTransaction();
                RealmResults<NullTypes> objects = realm.where(NullTypes.class).findAll();
                NullTypes parent = realm.createObject(NullTypes.class, 0);
                for (int i = 0; i < objects.size(); i++) {
                    NullTypes object = objects.get(i);
                    if (object.getId() != 0) {
                        parent.getFieldListNull().add(object);
                    }
                }
                realm.commitTransaction();
                original = parent.getFieldListNull().where().findAll();
                copy = parent.getFieldListNull();
                break;

            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
            case REALMRESULTS:
                original = realm.where(NullTypes.class).findAll();
                copy = realm.where(NullTypes.class).findAll();
                break;

            default:
                throw new AssertionError("Unknown collection class: " + collectionClass);
        }

        if (isSnapshot(collectionClass)) {
            copy = copy.createSnapshot();
        }

        // 1 String
        doTestSortOnColumnWithPartialNullValues(NullTypes.FIELD_STRING_NULL, original, copy);

        // 3 Boolean
        doTestSortOnColumnWithPartialNullValues(NullTypes.FIELD_BOOLEAN_NULL, original, copy);

        // 6 Integer
        doTestSortOnColumnWithPartialNullValues(NullTypes.FIELD_INTEGER_NULL, original, copy);

        // 7 Float
        doTestSortOnColumnWithPartialNullValues(NullTypes.FIELD_FLOAT_NULL, original, copy);

        // 8 Double
        doTestSortOnColumnWithPartialNullValues(NullTypes.FIELD_DOUBLE_NULL, original, copy);

        // 10 Date
        doTestSortOnColumnWithPartialNullValues(NullTypes.FIELD_DATE_NULL, original, copy);
    }

    @Test
    public void sort_nonExistingColumn() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        } else {
            thrown.expect(IllegalArgumentException.class);
        }
        collection.sort("Non-existing");
    }

    @Test
    public void sort_danishCharacters() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> collection = createStringCollection(realm, collectionClass,
                "Æble",
                "Øl",
                "Århus"
        );

        collection = collection.sort(AllJavaTypes.FIELD_STRING);

        assertEquals(3, collection.size());
        assertEquals("Æble", collection.get(0).getFieldString());
        assertEquals("Øl", collection.get(1).getFieldString());
        assertEquals("Århus", collection.get(2).getFieldString());

        collection = collection.sort(AllJavaTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(3, collection.size());
        assertEquals("Århus", collection.get(0).getFieldString());
        assertEquals("Øl", collection.get(1).getFieldString());
        assertEquals("Æble", collection.get(2).getFieldString());
    }

    @Test
    public void sort_russianCharacters() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> collection = createStringCollection(realm, collectionClass,
                "Санкт-Петербург",
                "Москва",
                "Новороссийск"
        );

        collection = collection.sort(AllJavaTypes.FIELD_STRING);

        assertEquals(3, collection.size());
        assertEquals("Москва", collection.get(0).getFieldString());
        assertEquals("Новороссийск", collection.get(1).getFieldString());
        assertEquals("Санкт-Петербург", collection.get(2).getFieldString());

        collection = collection.sort(AllJavaTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(3, collection.size());
        assertEquals("Санкт-Петербург", collection.get(0).getFieldString());
        assertEquals("Новороссийск", collection.get(1).getFieldString());
        assertEquals("Москва", collection.get(2).getFieldString());
    }

    @Test
    public void sort_greekCharacters() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> collection = createStringCollection(realm, collectionClass,
                "αύριο",
                "ημέρες",
                "δοκιμές"
        );

        collection = collection.sort(AllJavaTypes.FIELD_STRING);

        assertEquals(3, collection.size());
        assertEquals("αύριο", collection.get(0).getFieldString());
        assertEquals("δοκιμές", collection.get(1).getFieldString());
        assertEquals("ημέρες", collection.get(2).getFieldString());

        collection = collection.sort(AllJavaTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(3, collection.size());
        assertEquals("ημέρες", collection.get(0).getFieldString());
        assertEquals("δοκιμές", collection.get(1).getFieldString());
        assertEquals("αύριο", collection.get(2).getFieldString());
    }

    // No sorting order defined. There are Korean, Arabic and Chinese characters.
    @Test
    public void sort_manyDifferentCharacters() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> collection = createStringCollection(realm, collectionClass,
                "단위",
                "테스트",
                "وحدة",
                "اختبار",
                "单位",
                "试验",
                "單位",
                "測試"
        );

        collection.sort(AllJavaTypes.FIELD_STRING);
        assertEquals(8, collection.size());

        collection.sort(AllJavaTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(8, collection.size());
    }

    @Test
    public void sort_twoLanguages() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> collection = createStringCollection(realm, collectionClass,
                "test",
                "αύριο",
                "work"
        );

        try {
            collection.sort(AllJavaTypes.FIELD_STRING);
        } catch (IllegalArgumentException e) {
            fail("Failed to sort with two kinds of alphabets");
        }
    }

    @Test
    public void sort_usingChildObject() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> resultList = collection;
        OrderedRealmCollection<AllJavaTypes> sortedList = createCollection(collectionClass);
        sortedList = sortedList.sort(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LONG, Sort.DESCENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(TEST_SIZE, sortedList.size());
        assertEquals("First excepted to be last", resultList.first().getFieldLong(), sortedList.last().getFieldLong());

        sortedList = sortedList.sort(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LONG, Sort.ASCENDING);
        assertEquals(TEST_SIZE, sortedList.size());
        assertEquals("First excepted to be first", resultList.first().getFieldLong(), sortedList.first().getFieldLong());
        assertEquals("Last excepted to be last", resultList.last().getFieldLong(), sortedList.last().getFieldLong());

        sortedList = sortedList.sort(AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_LONG, Sort.DESCENDING);
        assertEquals(TEST_SIZE, sortedList.size());
    }

    @Test
    public void sort_nullArguments() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> result = collection;
        try {
            result.sort((String) null);
            fail("Sorting with a null field name should throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            result.sort((String) null, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void sort_emptyResults() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<NullTypes> collection = createEmptyCollection(realm, collectionClass);
        assertEquals(0, collection.size());
        collection.sort(NullTypes.FIELD_STRING_NULL);
        assertEquals(0, collection.size());
    }

    @Test
    public void sort_singleField() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        RealmResults<AllJavaTypes> sortedList = collection.sort(new String[]{AllJavaTypes.FIELD_LONG}, new Sort[]{Sort.DESCENDING});
        assertEquals(TEST_SIZE, sortedList.size());
        assertEquals(TEST_SIZE - 1, sortedList.first().getFieldLong());
        assertEquals(0, sortedList.last().getFieldLong());
    }

    @Test
    public void sort_date() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> resultList = collection;
        OrderedRealmCollection<AllJavaTypes> sortedList = createCollection(collectionClass);
        sortedList = sortedList.sort(AllJavaTypes.FIELD_DATE, Sort.DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_SIZE, sortedList.size());
        assertEquals(resultList.first().getFieldDate(), sortedList.last().getFieldDate());

        sortedList = sortedList.sort(AllJavaTypes.FIELD_DATE, Sort.ASCENDING);
        assertEquals(TEST_SIZE, sortedList.size());
        assertEquals(resultList.first().getFieldDate(), sortedList.first().getFieldDate());
        assertEquals(resultList.last().getFieldDate(), sortedList.last().getFieldDate());

        sortedList = sortedList.sort(AllJavaTypes.FIELD_DATE, Sort.DESCENDING);
        assertEquals(TEST_SIZE, sortedList.size());
    }

    @Test
    public void sort_long() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        OrderedRealmCollection<AllJavaTypes> resultList = collection;
        OrderedRealmCollection<AllJavaTypes> sortedList = createCollection(collectionClass);
        sortedList = sortedList.sort(AllJavaTypes.FIELD_LONG, Sort.DESCENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(TEST_SIZE, sortedList.size());
        assertEquals("First excepted to be last", resultList.first().getFieldLong(), sortedList.last().getFieldLong());

        sortedList = sortedList.sort(AllJavaTypes.FIELD_LONG, Sort.ASCENDING);
        assertEquals(TEST_SIZE, sortedList.size());
        assertEquals("First excepted to be first", resultList.first().getFieldLong(), sortedList.first().getFieldLong());
        assertEquals("Last excepted to be last", resultList.last().getFieldLong(), sortedList.last().getFieldLong());

        sortedList = sortedList.sort(AllJavaTypes.FIELD_LONG, Sort.DESCENDING);
        assertEquals(TEST_SIZE, sortedList.size());
    }

    @Test
    public void deleteFromRealm() {
        OrderedRealmCollection<Dog> collection = createNonCyclicCollection(realm, collectionClass);
        assertEquals(1, collection.get(1).getAge());

        int[] indexToDelete = {TEST_SIZE/2, TEST_SIZE - 2, 0};
        int currentSize = TEST_SIZE;

        for (int i = 0; i < indexToDelete.length; i++) {
            int index = indexToDelete[i];
            realm.beginTransaction();
            Dog dog = collection.get(index);
            collection.deleteFromRealm(index);
            realm.commitTransaction();
            if (isSnapshot(collectionClass)) {
                assertEquals(TEST_SIZE, collection.size());
                assertFalse(collection.get(index).isValid());
            } else {
                assertEquals(currentSize- 1, collection.size());
            }
            assertFalse(dog.isValid());
            assertEquals(currentSize- 1, realm.where(Dog.class).count());
            currentSize -= 1;
        }
    }

    @Test
    public void deleteFromRealm_invalidIndex() {
        Integer[] indexes = new Integer[] { Integer.MIN_VALUE, -1, TEST_SIZE, Integer.MAX_VALUE };
        for (Integer index : indexes) {
            try {
                realm.beginTransaction();
                collection.deleteFromRealm(index);
                fail("Index should have thrown exception: " + index);
            } catch (ArrayIndexOutOfBoundsException ignored) {
            } finally {
                realm.cancelTransaction();
            }
        }
    }

    @Test
    public void deleteFirstFromRealm() {
        OrderedRealmCollection<Dog> collection = createNonCyclicCollection(realm, collectionClass);
        assertEquals(0, collection.get(0).getAge());

        realm.beginTransaction();
        Dog dog = collection.first();
        assertTrue(collection.deleteFirstFromRealm());
        realm.commitTransaction();
        if (isSnapshot(collectionClass)) {
            assertEquals(TEST_SIZE, collection.size());
            assertFalse(collection.first().isValid());
        } else {
            assertEquals(TEST_SIZE - 1, collection.size());
            assertEquals(1, collection.get(0).getAge());
        }
        assertFalse(dog.isValid());
        assertEquals(TEST_SIZE - 1, realm.where(Dog.class).count());
    }

    private OrderedRealmCollection<Dog> createNonCyclicCollection(Realm realm, ManagedCollection collectionClass) {
        realm.beginTransaction();
        realm.deleteAll();
        OrderedRealmCollection<Dog> orderedCollection;
        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
            case MANAGED_REALMLIST:
                Owner owner = realm.createObject(Owner.class);
                RealmList<Dog> dogs = owner.getDogs();
                for (int i = 0; i < TEST_SIZE; i++) {
                    Dog dog = realm.createObject(Dog.class);
                    dog.setName("Dog " + i);
                    dog.setAge(i);
                    dogs.add(dog);
                }
                realm.commitTransaction();
                orderedCollection = dogs;
                break;

            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case REALMRESULTS:
                for (int i = 0; i < TEST_SIZE; i++) {
                    Dog dog = realm.createObject(Dog.class);
                    dog.setAge(i);
                    dog.setName("Dog " + i);
                }
                realm.commitTransaction();
                orderedCollection = realm.where(Dog.class).sort(Dog.FIELD_AGE).findAll();
                break;

            default:
                throw new AssertionError("Unknown collection class: " + collectionClass);
        }
        if (isSnapshot(collectionClass)) {
            orderedCollection = orderedCollection.createSnapshot();
        }
        return orderedCollection;
    }

    @Test
    public void deleteFirstFromRealm_emptyCollection() {
        OrderedRealmCollection<NullTypes> collection = createEmptyCollection(realm, collectionClass);
        realm.beginTransaction();
        assertFalse(collection.deleteFirstFromRealm());
        realm.commitTransaction();
        assertEquals(0, collection.size());
    }

    @Test
    public void deleteLastFromRealm() {
        assertEquals(TEST_SIZE - 1, collection.last().getFieldLong());
        realm.beginTransaction();
        AllJavaTypes allJavaTypes = collection.last();
        assertTrue(collection.deleteLastFromRealm());
        realm.commitTransaction();
        if (isSnapshot(collectionClass)) {
            assertEquals(TEST_SIZE, collection.size());
            assertFalse(collection.last().isValid());
        } else {
            assertEquals(TEST_SIZE - 1, collection.size());
            assertEquals(TEST_SIZE - 2, collection.last().getFieldLong());
        }
        assertFalse(allJavaTypes.isValid());
        assertEquals(TEST_SIZE - 1, realm.where(AllJavaTypes.class).count());
    }

    @Test
    public void deleteLastFromRealm_emptyCollection() {
        OrderedRealmCollection<NullTypes> collection = createEmptyCollection(realm, collectionClass);
        realm.beginTransaction();
        assertFalse(collection.deleteLastFromRealm());
        realm.commitTransaction();
        assertEquals(0, collection.size());
    }

    // Tests all methods that mutate data throw correctly if not inside an transaction.
    // Due to implementation details both UnsupportedOperation and IllegalState is accepted at this level.
    @Test
    public void mutableMethodsOutsideTransactions() {

        for (OrderedCollectionMutatorMethod method : OrderedCollectionMutatorMethod.values()) {

            // Define expected exception
            Class<? extends Throwable> expected = IllegalStateException.class;
            if (collectionClass == ManagedCollection.REALMRESULTS || isSnapshot(collectionClass)) {
                switch (method) {
                    case ADD_INDEX:
                    case ADD_ALL_INDEX:
                    case SET:
                    case REMOVE_INDEX:
                        expected = UnsupportedOperationException.class;
                        break;
                    default:
                        // Uses default exception.
                }
            }

            try {
                switch (method) {
                    case DELETE_INDEX: collection.deleteFromRealm(0); break;
                    case DELETE_FIRST: collection.deleteFirstFromRealm(); break;
                    case DELETE_LAST: collection.deleteLastFromRealm(); break;
                    case ADD_INDEX: collection.add(0, new AllJavaTypes()); break;
                    case ADD_ALL_INDEX: collection.addAll(0, Collections.singletonList(new AllJavaTypes())); break;
                    case SET: collection.set(0, new AllJavaTypes()); break;
                    case REMOVE_INDEX: collection.remove(0); break;
                }
                fail("Unknown method or it failed to throw: " + method);
            } catch (IllegalStateException e) {
                assertEquals(expected, e.getClass());
            } catch (UnsupportedOperationException e) {
                assertEquals(expected, e.getClass());
            }
        }
    }

    @Test
    public void methodsThrowOnWrongThread() throws ExecutionException, InterruptedException {
        for (OrderedRealmCollectionMethod method : OrderedRealmCollectionMethod.values()) {
            assertTrue(method + " failed", runMethodOnWrongThread(method));
        }

        for (ListMethod method : ListMethod.values()) {
            assertTrue(method + " failed", runMethodOnWrongThread(method));
        }
    }

    private boolean runMethodOnWrongThread(final OrderedRealmCollectionMethod method) throws ExecutionException, InterruptedException {
        realm.beginTransaction();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // Defines expected exception.
                Class<? extends Throwable> expected = IllegalStateException.class;
                if (isSnapshot(collectionClass)) {
                    switch (method) {
                        case SORT:
                        case SORT_FIELD:
                        case SORT_2FIELDS:
                        case SORT_MULTI:
                            expected = UnsupportedOperationException.class;
                            break;
                        default:
                            break;
                    }
                }

                try {
                    switch (method) {
                        case DELETE_INDEX: collection.deleteFromRealm(0); break;
                        case DELETE_FIRST: collection.deleteFirstFromRealm(); break;
                        case DELETE_LAST: collection.deleteLastFromRealm(); break;
                        case SORT: collection.sort(AllJavaTypes.FIELD_STRING); break;
                        case SORT_FIELD: collection.sort(AllJavaTypes.FIELD_STRING, Sort.ASCENDING); break;
                        case SORT_2FIELDS: collection.sort(AllJavaTypes.FIELD_STRING, Sort.ASCENDING, AllJavaTypes.FIELD_LONG, Sort.DESCENDING); break;
                        case SORT_MULTI: collection.sort(new String[] { AllJavaTypes.FIELD_STRING }, new Sort[] { Sort.ASCENDING }); break;
                        case CREATE_SNAPSHOT: collection.createSnapshot(); break;
                    }
                    return false;
                } catch (Throwable t) {
                    return t.getClass().equals(expected);
                }
            }
        });
        Boolean result = future.get();
        realm.cancelTransaction();
        return result;
    }

    private boolean runMethodOnWrongThread(final ListMethod method) throws ExecutionException, InterruptedException {
        realm.beginTransaction();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // Defines expected exception.
                Class<? extends Throwable> expected = IllegalStateException.class;
                if (collectionClass == ManagedCollection.REALMRESULTS || isSnapshot(collectionClass)) {
                    switch (method) {
                        case ADD_INDEX:
                        case ADD_ALL_INDEX:
                        case SET:
                        case REMOVE_INDEX:
                            expected = UnsupportedOperationException.class;
                            break;
                        default:
                            // Uses default exception.
                    }
                }

                try {
                    switch (method) {
                        case FIRST: collection.first(); break;
                        case LAST: collection.last(); break;
                        case ADD_INDEX: collection.add(0, new AllJavaTypes()); break;
                        case ADD_ALL_INDEX: collection.addAll(0, Collections.singletonList(new AllJavaTypes())); break;
                        case GET_INDEX: collection.get(0); break;
                        case INDEX_OF: collection.indexOf(new AllJavaTypes()); break;
                        case LAST_INDEX_OF: collection.lastIndexOf(new AllJavaTypes()); break;
                        case LIST_ITERATOR: collection.listIterator(); break;
                        case LIST_ITERATOR_INDEX: collection.listIterator(0); break;
                        case REMOVE_INDEX: collection.remove(0); break;
                        case SET: collection.set(0, new AllJavaTypes()); break;
                        case SUBLIST: collection.subList(0, 1); break;
                    }
                    return false;
                } catch (Throwable t) {
                    if (!t.getClass().equals(expected)) {
                        return false;
                    }
                }
                return true;
            }
        });
        Boolean result = future.get();
        realm.cancelTransaction();
        return result;
    }

}
