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

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.NullTypes;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for all methods part of the the {@link RealmCollection} interface.
 * This class only tests collections that are managed by Realm. See {@link UnManagedRealmCollectionTests} for
 * all tests targeting unmanaged collections.
 *
 * Methods tested in this class:
 *
 * # RealmCollection
 *
 * + RealmQuery<E> where();
 * + Number min(String fieldName);
 * + Number max(String fieldName);
 * + Number sum(String fieldName);
 * + double average(String fieldName);
 * + Date maxDate(String fieldName);
 * + Date minDate(String fieldName);
 * + void deleteAllFromRealm();
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
 */
@RunWith(Parameterized.class)
public class ManagedRealmCollectionTests extends CollectionTests {

    private static final int TEST_SIZE = 10;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private final ManagedCollection collectionClass;
    private Realm realm;

    // Collections used for testing
    private RealmCollection<AllJavaTypes> collection;

    @Parameterized.Parameters(name = "{0}")
    public static List<ManagedCollection> data() {
        return Arrays.asList(ManagedCollection.values());
    }

    public ManagedRealmCollectionTests(ManagedCollection collectionType) {
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

    private OrderedRealmCollection<AllJavaTypes> createCollection(ManagedCollection collectionClass) {
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
                orderedCollection = realm.where(AllJavaTypes.class)
                        .sort(AllJavaTypes.FIELD_LONG, Sort.ASCENDING)
                        .findAll();
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

    private OrderedRealmCollection<NullTypes> createAllNullRowsForNumericTesting(Realm realm, ManagedCollection collectionClass) {
        TestHelper.populateAllNullRowsForNumericTesting(realm);
        OrderedRealmCollection<NullTypes> orderedCollection;
        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case MANAGED_REALMLIST:
                RealmResults<NullTypes> results = realm.where(NullTypes.class).findAll();
                RealmList<NullTypes> list = results.get(0).getFieldListNull();
                realm.beginTransaction();
                for (int i = 0; i < results.size(); i++) {
                    list.add(results.get(i));
                }
                realm.commitTransaction();
                orderedCollection = list;
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

    private OrderedRealmCollection<NullTypes> createPartialNullRowsForNumericTesting(Realm realm, ManagedCollection collectionClass) {
        populatePartialNullRowsForNumericTesting(realm);
        OrderedRealmCollection<NullTypes> orderedCollection;
        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case MANAGED_REALMLIST:
                RealmResults<NullTypes> results = realm.where(NullTypes.class).findAll();
                RealmList<NullTypes> list = results.get(0).getFieldListNull();
                realm.beginTransaction();
                int size = results.size();
                for (int i = 0; i < size; i++) {
                    list.add(results.get(i));
                }
                realm.commitTransaction();
                orderedCollection = list;
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

    // PRE-CONDITION: populateRealm() was called as part of setUp()
    private OrderedRealmCollection<NonLatinFieldNames> createNonLatinCollection(Realm realm, ManagedCollection collectionClass) {
        OrderedRealmCollection<NonLatinFieldNames> orderedCollection;
        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case MANAGED_REALMLIST:
                realm.beginTransaction();
                RealmResults<NonLatinFieldNames> results = realm.where(NonLatinFieldNames.class).findAll();
                RealmList<NonLatinFieldNames> list = results.get(0).getChildren();
                for (int i = 0; i < results.size(); i++) {
                    list.add(results.get(i));
                }
                realm.commitTransaction();
                orderedCollection = list;
                break;

            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
            case REALMRESULTS:
                orderedCollection = realm.where(NonLatinFieldNames.class).findAll();
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
    public void where() {
        if (isSnapshot(collectionClass)) {
            thrown.expect(UnsupportedOperationException.class);
        }
        RealmResults<AllJavaTypes> results = collection.where().findAll();
        assertEquals(TEST_SIZE, results.size());
    }

    @Test
    public void where_contains() {
        RealmQuery<AllJavaTypes> query = realm.where(AllJavaTypes.class).findAll().where();
        AllJavaTypes item = query.findFirst();
        assertTrue("Item should exist in results.", query.findAll().contains(item));
    }

    @Test
    public void where_contains_null() {
        RealmQuery<AllJavaTypes> query = realm.where(AllJavaTypes.class).findAll().where();
        assertFalse("Should not contain a null item.", query.findAll().contains(null));
    }

    @Test
    public void where_shouldNotContainRemovedItem() {
        RealmQuery<AllJavaTypes> query = realm.where(AllJavaTypes.class).findAll().where();
        AllJavaTypes item = realm.where(AllJavaTypes.class).findFirst();
        realm.beginTransaction();
        item.deleteFromRealm();
        realm.commitTransaction();
        assertFalse("Should not contain a removed item.", query.findAll().contains(item));
    }

    /**
     * Tests to see if a particular item that does exist in the same Realm does not
     * exist in the result set of another query.
     */
    @Test
    public void where_lessThanGreaterThan() {
        RealmResults<AllJavaTypes> items = realm.where(AllJavaTypes.class).lessThan(AllJavaTypes.FIELD_LONG, 1000).findAll();
        AllJavaTypes anotherType = realm.where(AllJavaTypes.class).greaterThan(AllJavaTypes.FIELD_LONG, 1000).findFirst();
        assertFalse("Should not be able to find item in another result list.", items.contains(anotherType));
    }

    @Test
    public void where_equalTo_manyConditions() {
        RealmQuery<AllJavaTypes> query = realm.where(AllJavaTypes.class);
        query.equalTo(AllJavaTypes.FIELD_LONG, 0);
        for (int i = 1; i < TEST_SIZE; i++) {
            query.or().equalTo(AllJavaTypes.FIELD_LONG, i);
        }
        RealmResults<AllJavaTypes> allTypesRealmResults = query.findAll();
        assertEquals(TEST_SIZE, allTypesRealmResults.size());
    }

    @Test
    public void where_findAll_size() {
        RealmResults<AllJavaTypes> results = realm.where(AllJavaTypes.class).findAll();
        assertEquals(TEST_SIZE, results.size());

        // Querying a RealmResults should find objects that fulfill the condition.
        RealmResults<AllJavaTypes> onedigits = results.where().lessThan(AllJavaTypes.FIELD_LONG, 10).findAll();
        assertEquals(Math.min(10, TEST_SIZE), onedigits.size());

        // If no objects fulfill conditions, the result has zero objects.
        RealmResults<AllJavaTypes> none = results.where().greaterThan(AllJavaTypes.FIELD_LONG, TEST_SIZE).findAll();
        assertEquals(0, none.size());

        // Querying a result with zero objects must give zero objects.
        RealmResults<AllJavaTypes> stillNone = none.where().greaterThan(AllJavaTypes.FIELD_LONG, TEST_SIZE).findAll();
        assertEquals(0, stillNone.size());
    }

    @Test
    public void where_sort() {
        RealmResults<AllJavaTypes> results = realm.where(AllJavaTypes.class).sort(AllJavaTypes.FIELD_LONG, Sort.ASCENDING).findAll();
        assertEquals(TEST_SIZE, results.size());
        //noinspection ConstantConditions
        assertEquals(0, results.first().getFieldLong());
        //noinspection ConstantConditions
        assertEquals(TEST_SIZE - 1, results.last().getFieldLong());

        RealmResults<AllJavaTypes> reverseList = realm.where(AllJavaTypes.class).sort(AllJavaTypes.FIELD_LONG, Sort.DESCENDING).findAll();
        assertEquals(TEST_SIZE, reverseList.size());
        //noinspection ConstantConditions
        assertEquals(0, reverseList.last().getFieldLong());
        //noinspection ConstantConditions
        assertEquals(TEST_SIZE - 1, reverseList.first().getFieldLong());

        try {
            realm.where(AllJavaTypes.class).sort("invalid", Sort.DESCENDING).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void where_queryDateField() {
        RealmQuery<AllJavaTypes> query = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_DATE, new Date(YEAR_MILLIS * 20));
        RealmResults<AllJavaTypes> all = query.findAll();
        assertEquals(1, query.count());
        assertEquals(1, all.size());

        // before 1901
        query = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_DATE, new Date(YEAR_MILLIS * -100));
        all = query.findAll();
        assertEquals(1, query.count());
        assertEquals(1, all.size());

        // after 2038
        query = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_DATE, new Date(YEAR_MILLIS * 80));
        all = query.findAll();
        assertEquals(1, query.count());
        assertEquals(1, all.size());
    }

    @Test
    public void min() {
        Number minimum = collection.min(AllJavaTypes.FIELD_LONG);
        assertEquals(0, minimum.intValue());
    }

    // Tests min on empty columns.
    @Test
    public void min_emptyNonNullFields() {
        OrderedRealmCollection<NullTypes> results = createEmptyCollection(realm, collectionClass);
        assertNull(results.min(NullTypes.FIELD_INTEGER_NOT_NULL));
        assertNull(results.min(NullTypes.FIELD_FLOAT_NOT_NULL));
        assertNull(results.min(NullTypes.FIELD_DOUBLE_NOT_NULL));
        assertNull(results.minDate(NullTypes.FIELD_DATE_NOT_NULL));
    }

    // Tests min on nullable rows with all null values.
    @Test
    public void min_emptyNullFields() {
        OrderedRealmCollection<NullTypes> results = createAllNullRowsForNumericTesting(realm, collectionClass);
        assertNull(results.max(NullTypes.FIELD_INTEGER_NULL));
        assertNull(results.max(NullTypes.FIELD_FLOAT_NULL));
        assertNull(results.max(NullTypes.FIELD_DOUBLE_NULL));
        assertNull(results.maxDate(NullTypes.FIELD_DATE_NULL));
    }

    // Tests min on nullable rows with partial null values.
    @Test
    public void min_partialNullRows() {
        OrderedRealmCollection<NullTypes> results = createPartialNullRowsForNumericTesting(realm, collectionClass);
        assertEquals(0, results.min(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(0f, results.min(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(0d, results.min(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    @Test
    public void max() {
        Number maximum = collection.max(AllJavaTypes.FIELD_LONG);
        assertEquals(TEST_SIZE - 1, maximum.intValue());
    }

    // Tests max on empty columns.
    @Test
    public void max_emptyNonNullFields() {
        OrderedRealmCollection<NullTypes> results = createEmptyCollection(realm, collectionClass);
        assertNull(results.max(NullTypes.FIELD_INTEGER_NOT_NULL));
        assertNull(results.max(NullTypes.FIELD_FLOAT_NOT_NULL));
        assertNull(results.max(NullTypes.FIELD_DOUBLE_NOT_NULL));
        assertNull(results.maxDate(NullTypes.FIELD_DATE_NOT_NULL));
    }

    // Tests max on nullable rows with all null values.
    @Test
    public void max_emptyNullFields() {
        OrderedRealmCollection<NullTypes> results = createAllNullRowsForNumericTesting(realm, collectionClass);
        assertNull(results.max(NullTypes.FIELD_INTEGER_NULL));
        assertNull(results.max(NullTypes.FIELD_FLOAT_NULL));
        assertNull(results.max(NullTypes.FIELD_DOUBLE_NULL));
        assertNull(results.maxDate(NullTypes.FIELD_DATE_NULL));
    }

    // Tests max on nullable rows with partial null values.
    @Test
    public void max_partialNullRows() {
        OrderedRealmCollection<NullTypes> results = createPartialNullRowsForNumericTesting(realm, collectionClass);
        assertEquals(1, results.max(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(2f, results.max(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(3d, results.max(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    @Test
    public void sum() {
        Number sum = collection.sum(AllJavaTypes.FIELD_LONG);
        // Sum of numbers 0 to M-1: (M-1)*M/2
        assertEquals((TEST_SIZE - 1) * TEST_SIZE / 2, sum.intValue());
    }

    // Tests sum on nullable rows with all null values.
    @Test
    public void sum_nullRows() {
        OrderedRealmCollection<NullTypes> resultList = createAllNullRowsForNumericTesting(realm, collectionClass);
        assertEquals(0, resultList.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(0f, resultList.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(0d, resultList.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    // Tests sum on nullable rows with partial null values.
    @Test
    public void sum_partialNullRows() {
        OrderedRealmCollection<NullTypes> resultList = createPartialNullRowsForNumericTesting(realm, collectionClass);

        assertEquals(1, resultList.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(2f, resultList.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(3d, resultList.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    @Test
    public void sum_nonLatinColumnNames() {
        OrderedRealmCollection<NonLatinFieldNames> resultList = createNonLatinCollection(realm, collectionClass);

        Number sum = resultList.sum(NonLatinFieldNames.FIELD_LONG_KOREAN_CHAR);
        // Sum of numbers 0 to M-1: (M-1)*M/2
        assertEquals((TEST_SIZE - 1) * TEST_SIZE / 2, sum.intValue());

        sum = resultList.sum(NonLatinFieldNames.FIELD_LONG_GREEK_CHAR);
        // Sum of numbers 0 to M-1: (M-1)*M/2
        assertEquals((TEST_SIZE - 1) * TEST_SIZE / 2, sum.intValue());
    }

    @Test
    public void avg() {
        double N = (double) TEST_SIZE;

        // Sum of numbers 1 to M: M*(M+1)/2
        // See setUp() for values of fields.
        // N = TEST_DATA_SIZE

        // Type: double; a = Math.PI
        // a, a+1, ..., a+i, ..., a+N-1
        // sum = Math.PI*N + N*(N-1)/2
        // average = sum/N = Math.PI+(N-1)/2
        double average = Math.PI + (N - 1.0) * 0.5;
        assertEquals(average, collection.average(AllJavaTypes.FIELD_DOUBLE), 0.0001);

        // Type: long
        // 0, 1, ..., N-1
        // sum = N*(N-1)/2
        // average = sum/N = (N-1)/2
        assertEquals(0.5 * (N - 1), collection.average(AllJavaTypes.FIELD_LONG), 0.0001);

        // Type: float; b = 1.234567
        // b, b+1, ..., b+i, ..., b+N-1
        // sum = b*N + N*(N-1)/2
        // average = sum/N = b + (N-1)/2
        assertEquals(1.234567 + 0.5 * (N - 1.0), collection.average(AllJavaTypes.FIELD_FLOAT), 0.0001);
    }

    // Tests average on empty columns.
    @Test
    public void avg_emptyNonNullFields() {
        OrderedRealmCollection<NullTypes> resultList = createEmptyCollection(realm, collectionClass);
        assertEquals(0d, resultList.average(NullTypes.FIELD_INTEGER_NOT_NULL), 0d);
        assertEquals(0d, resultList.average(NullTypes.FIELD_FLOAT_NOT_NULL), 0d);
        assertEquals(0d, resultList.average(NullTypes.FIELD_DOUBLE_NOT_NULL), 0d);
    }

    // Tests average on nullable rows with all null values.
    @Test
    public void avg_emptyNullFields() {
        OrderedRealmCollection<NullTypes> resultList = createEmptyCollection(realm, collectionClass);
        assertEquals(0d, resultList.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(0d, resultList.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(0d, resultList.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    // Tests average on nullable rows with partial null values.
    @Test
    public void avg_partialNullRows() {
        OrderedRealmCollection<NullTypes> resultList = createPartialNullRowsForNumericTesting(realm, collectionClass);
        assertEquals(0.5d, resultList.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(1.0d, resultList.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(1.5d, resultList.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    @Test
    public void maxDate() {
        assertEquals(TEST_SIZE, collection.size());
        assertEquals(new Date(YEAR_MILLIS * 20 * (TEST_SIZE / 2 - 1)), collection.maxDate(AllJavaTypes.FIELD_DATE));
    }

    @Test
    public void minDate() {
        assertEquals(TEST_SIZE, collection.size());
        assertEquals(new Date(-YEAR_MILLIS * 20 * TEST_SIZE / 2), collection.minDate(AllJavaTypes.FIELD_DATE));
    }

    // Deletes the last row in the collection then tests the aggregates methods.
    // Since deletion will turn the corresponding object into invalid for collection snapshot, this tests if the
    // aggregates methods ignore the invalid rows and return the correct result.
    @Test
    public void aggregates_deleteLastRow() {
        assertTrue(TEST_SIZE > 3);
        assertEquals(TEST_SIZE, collection.size());
        realm.beginTransaction();
        realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_LONG, TEST_SIZE - 1).findFirst().deleteFromRealm();
        realm.commitTransaction();

        int sizeAfterRemove = TEST_SIZE - 1;

        assertEquals(0, collection.min(AllJavaTypes.FIELD_LONG).intValue());
        assertEquals(sizeAfterRemove - 1, collection.max(AllJavaTypes.FIELD_LONG).intValue());
        // Sum of numbers 0 to M-1: (M-1)*M/2
        assertEquals((sizeAfterRemove - 1) * sizeAfterRemove / 2, collection.sum(AllJavaTypes.FIELD_LONG).intValue());
        double average = Math.PI + (sizeAfterRemove - 1.0) * 0.5;
        assertEquals(average, collection.average(AllJavaTypes.FIELD_DOUBLE), 0.0001);
        assertEquals(new Date(YEAR_MILLIS * 20 * (sizeAfterRemove / 2 - 1)), collection.maxDate(AllJavaTypes.FIELD_DATE));
        assertEquals(new Date(-YEAR_MILLIS * 20 * TEST_SIZE / 2), collection.minDate(AllJavaTypes.FIELD_DATE));
    }

    @Test
    public void realmMethods_invalidFieldNames() {
        String[] fieldNames = new String[] {
                null, "", "foo", AllJavaTypes.FIELD_STRING + ".foo", TestHelper.getRandomString(65)
        };

        for (RealmCollectionMethod realmMethod : RealmCollectionMethod.values()) {
            for (String fieldName : fieldNames) {
                try {
                    switch (realmMethod) {
                        case MIN: collection.min(fieldName); break;
                        case MAX: collection.max(fieldName); break;
                        case SUM: collection.sum(fieldName); break;
                        case AVERAGE: collection.average(fieldName); break;
                        case MIN_DATE: collection.minDate(fieldName); break;
                        case MAX_DATE: collection.maxDate(fieldName); break;

                        // These methods doesn't take any arguments.
                        // Just skip them.
                        case WHERE:
                        case DELETE_ALL_FROM_REALM:
                        case IS_VALID:
                        case IS_MANAGED:
                            continue;

                        default:
                            fail("Unknown method: " + realmMethod);

                    }
                    fail(realmMethod + " did not throw an exception for input: " + fieldName);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    @Test
    public void realmMethods_invalidFieldType() {
        String fieldName = AllJavaTypes.FIELD_STRING;
        for (RealmCollectionMethod realmMethod : RealmCollectionMethod.values()) {
            try {
                switch (realmMethod) {
                    case MIN: collection.min(fieldName); break;
                    case MAX: collection.max(fieldName); break;
                    case SUM: collection.sum(fieldName); break;
                    case AVERAGE: collection.average(fieldName); break;
                    case MIN_DATE: collection.minDate(fieldName); break;
                    case MAX_DATE: collection.maxDate(fieldName); break;

                    // These methods doesn't take any arguments.
                    // Just skip them.
                    case WHERE:
                    case DELETE_ALL_FROM_REALM:
                    case IS_VALID:
                    case IS_MANAGED:
                        continue;

                    default:
                        fail("Unknown method: " + realmMethod);

                }
                fail(realmMethod + " did not throw an exception for input: " + fieldName);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void deleteAllFromRealm() {
        // If we have a self-referencing collection, removing all objects will crash
        // any following method. To avoid that scenario we make sure to use a collection
        // without cycles.
        int size = TEST_SIZE;
        if (collectionClass == ManagedCollection.MANAGED_REALMLIST) {
            RealmList list = (RealmList) collection;
            realm.beginTransaction();
            list.remove(0); // Breaks the cycle.
            realm.commitTransaction();
            size = TEST_SIZE - 1;
        }

        assertEquals(size, collection.size());
        realm.beginTransaction();
        assertTrue(collection.deleteAllFromRealm());
        realm.commitTransaction();
        if (isSnapshot(collectionClass)) {
            assertEquals(TEST_SIZE, collection.size());
        } else {
            assertEquals(0, collection.size());
        }
        if (isRealmList(collectionClass)) {
            // The parent object was not deleted
            assertEquals(1, realm.where(AllJavaTypes.class).count());
        } else {
            assertEquals(0, realm.where(AllJavaTypes.class).count());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void deleteAllFromRealm_outsideTransaction() {
        collection.deleteAllFromRealm();
    }

    @Test
    public void deleteAllFromRealm_emptyList() {
        OrderedRealmCollection<NullTypes> collection = createEmptyCollection(realm, collectionClass);

        realm.beginTransaction();
        assertFalse(collection.deleteAllFromRealm());
        realm.commitTransaction();
        assertEquals(0, collection.size());
    }

    @Test
    public void deleteAllFromRealm_invalidList() {
        realm.close();
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(CoreMatchers.containsString(
                "This Realm instance has already been closed, making it unusable."));
        collection.deleteAllFromRealm();
    }

    @Test
    public void isLoaded() {
        // RealmCollections are currently always loaded. Only exception is RealmResults.
        // See RealmResultsTests for extended tests on this.
        assertTrue(collection.isLoaded());
    }

    @Test
    public void load() {
        // RealmCollections are currently always loaded, so this just returns true. Only exception is RealmResults.
        // See RealmResultsTests for extended tests on this.
        assertTrue(collection.load());
    }

    @Test
    public void isValid() {
        assertTrue(collection.isValid());
    }

    @Test
    public void isValid_realmClosed() {
        realm.close();
        assertFalse(collection.isValid());
    }

    @Test
    public void isManaged() {
        assertTrue(collection.isManaged());
    }

    @Test
    public void contains_deletedRealmObject() {
        AllJavaTypes obj = collection.iterator().next();
        realm.beginTransaction();
        obj.deleteFromRealm();
        realm.commitTransaction();

        assertFalse(collection.contains(obj));
    }

    @Test
    public void equals_sameRealmObjectsDifferentCollection() {
        assertTrue(collection.equals(createCollection(collectionClass)));
    }

    // Tests all methods that mutate data throw correctly if not inside an transaction.
    // Due to implementation details both UnsupportedOperation and IllegalState is accepted at this level.
    @Test
    public void mutableMethodsOutsideTransactions() {
        for (CollectionMutatorMethod method : CollectionMutatorMethod.values()) {

            // Defines expected exception.
            Class<? extends Throwable> expected = IllegalStateException.class;
            if (collectionClass == ManagedCollection.REALMRESULTS || isSnapshot(collectionClass)) {
                switch (method) {
                    case ADD_OBJECT:
                    case ADD_ALL_OBJECTS:
                    case CLEAR:
                    case REMOVE_OBJECT:
                    case REMOVE_ALL:
                    case RETAIN_ALL:
                        expected = UnsupportedOperationException.class;
                        break;
                    default:
                        // use default exception
                }
            }

            try {
                switch (method) {
                    case DELETE_ALL: collection.deleteAllFromRealm(); break;
                    case ADD_OBJECT: collection.add(new AllJavaTypes()); break;
                    case ADD_ALL_OBJECTS: collection.addAll(Collections.singletonList(new AllJavaTypes())); break;
                    case CLEAR: collection.clear(); break;
                    case REMOVE_OBJECT: collection.remove(new AllJavaTypes()); break;
                    case REMOVE_ALL: collection.removeAll(Collections.singletonList(new AllJavaTypes())); break;
                    case RETAIN_ALL: collection.retainAll(Collections.singletonList(new AllJavaTypes())); break;
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
        realm.beginTransaction();
        AllJavaTypes allJavaTypes = realm.createObject(AllJavaTypes.class, 42);
        realm.commitTransaction();
        for (RealmCollectionMethod method : RealmCollectionMethod.values()) {
            assertTrue(method + " failed", runMethodOnWrongThread(method));
        }
        for (CollectionMethod method : CollectionMethod.values()) {
            assertTrue(method + " failed", runMethodOnWrongThread(method, allJavaTypes));
        }
    }

    private boolean runMethodOnWrongThread(final RealmCollectionMethod method)
            throws ExecutionException, InterruptedException {
        realm.beginTransaction();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    switch (method) {
                        case WHERE: collection.where(); break;
                        case MIN: collection.min(AllJavaTypes.FIELD_LONG); break;
                        case MAX: collection.min(AllJavaTypes.FIELD_LONG); break;
                        case SUM: collection.sum(AllJavaTypes.FIELD_LONG); break;
                        case AVERAGE: collection.average(AllJavaTypes.FIELD_LONG); break;
                        case MIN_DATE: collection.minDate(AllJavaTypes.FIELD_DATE); break;
                        case MAX_DATE: collection.maxDate(AllJavaTypes.FIELD_DATE); break;
                        case DELETE_ALL_FROM_REALM: collection.deleteAllFromRealm(); break;
                        case IS_VALID: collection.isValid(); break;
                        case IS_MANAGED: collection.isManaged(); return true;
                    }
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                } catch (UnsupportedOperationException ignored) {
                    return (method == RealmCollectionMethod.WHERE && isSnapshot(collectionClass));
                }
            }
        });
        Boolean result = future.get();
        realm.cancelTransaction();
        return result;
    }

    private boolean runMethodOnWrongThread(final CollectionMethod method, final AllJavaTypes tempObject)
            throws ExecutionException, InterruptedException {
        realm.beginTransaction();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                // Defines expected exception.
                Class<? extends Throwable> expected = IllegalStateException.class;
                if (collectionClass == ManagedCollection.REALMRESULTS || isSnapshot(collectionClass)) {
                    switch (method) {
                        case ADD_OBJECT:
                        case ADD_ALL_OBJECTS:
                        case CLEAR:
                        case REMOVE_OBJECT:
                        case REMOVE_ALL:
                        case RETAIN_ALL:
                            expected = UnsupportedOperationException.class;
                            break;
                        default:
                            // use default exception
                    }
                }

                try {
                    switch (method) {
                        case ADD_OBJECT: collection.add(new AllJavaTypes()); break;
                        case ADD_ALL_OBJECTS: collection.addAll(Collections.singletonList(new AllJavaTypes())); break;
                        case CLEAR: collection.clear(); break;
                        case CONTAINS: collection.contains(tempObject); break;
                        case CONTAINS_ALL: collection.containsAll(Collections.singletonList(tempObject)); break;
                        case EQUALS:
                            //noinspection ResultOfMethodCallIgnored
                            collection.equals(createCollection(collectionClass)); break;
                        case HASHCODE:
                            //noinspection ResultOfMethodCallIgnored
                            collection.hashCode();
                            break;
                        case IS_EMPTY: collection.isEmpty(); break;
                        case ITERATOR: return true; // Creating an iterator should be safe. Accessing it will fail, but tested elsewhere.
                        case REMOVE_OBJECT: collection.remove(new AllJavaTypes()); break;
                        case REMOVE_ALL: collection.removeAll(Collections.singletonList(new AllJavaTypes())); break;
                        case RETAIN_ALL: collection.retainAll(Collections.singletonList(new AllJavaTypes())); break;
                        case SIZE: collection.size(); break;
                        case TO_ARRAY: collection.toArray(); break;
                        case TO_ARRAY_INPUT: collection.toArray(new Object[collection.size()]); break;
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

