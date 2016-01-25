/*
 * Copyright 2014 Realm Inc.
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

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.Cat;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.NullTypes;
import io.realm.entities.Owner;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmResultsTests {

    private final static int TEST_DATA_SIZE = 2516;
    private final static int TEST_DATA_FIRST_HALF = 2 * (TEST_DATA_SIZE / 4) - 1;
    private final static int TEST_DATA_LAST_HALF = 2 * (TEST_DATA_SIZE / 4) + 1;
    private final static long YEAR_MILLIS = TimeUnit.DAYS.toMillis(365);

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private  Realm realm;

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
        populateTestRealm();
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    private void populateTestRealm(int objects) {
        realm.beginTransaction();
        realm.allObjects(AllTypes.class).clear();
        realm.allObjects(NonLatinFieldNames.class).clear();

        for (int i = 0; i < objects; ++i) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 2) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date(YEAR_MILLIS * (i - objects / 2)));
            allTypes.setColumnDouble(3.1415 + i);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            Dog d = realm.createObject(Dog.class);
            d.setName("Foo " + i);
            allTypes.setColumnRealmObject(d);
            allTypes.getColumnRealmList().add(d);
            NonLatinFieldNames nonLatinFieldNames = realm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
        }
        realm.commitTransaction();
    }

    private void populateTestRealm() {
        populateTestRealm(TEST_DATA_SIZE);
    }

    private void populatePartialNullRowsForNumericTesting() {
        NullTypes nullTypes1 = new NullTypes();
        nullTypes1.setId(1);
        nullTypes1.setFieldIntegerNull(1);
        nullTypes1.setFieldFloatNull(2F);
        nullTypes1.setFieldDoubleNull(3D);
        nullTypes1.setFieldBooleanNull(true);
        nullTypes1.setFieldStringNull("4");
        nullTypes1.setFieldDateNull(new Date(12345));

        NullTypes nullTypes2 = new NullTypes();
        nullTypes2.setId(2);

        NullTypes nullTypes3 = new NullTypes();
        nullTypes3.setId(3);
        nullTypes3.setFieldIntegerNull(0);
        nullTypes3.setFieldFloatNull(0F);
        nullTypes3.setFieldDoubleNull(0D);
        nullTypes3.setFieldBooleanNull(false);
        nullTypes3.setFieldStringNull("0");
        nullTypes3.setFieldDateNull(new Date(0));

        realm.beginTransaction();
        realm.copyToRealm(nullTypes1);
        realm.copyToRealm(nullTypes2);
        realm.copyToRealm(nullTypes3);
        realm.commitTransaction();
    }

    private enum Method {
        METHOD_MIN,
        METHOD_MAX,
        METHOD_SUM,
        METHOD_AVG,
        METHOD_SORT,
        METHOD_WHERE,
        METHOD_REMOVE,
        METHOD_REMOVE_LAST,
        METHOD_CLEAR
    }

    @Test
    public void methodsThrowOnWrongThread() throws ExecutionException, InterruptedException {
        for (Method method : Method.values()) {
            assertTrue(runMethodOnWrongThread(method));
        }
    }

    private boolean runMethodOnWrongThread(final Method method) throws ExecutionException, InterruptedException {
        final RealmResults<AllTypes> allTypeses = realm.where(AllTypes.class).findAll();
        realm.beginTransaction();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    switch (method) {
                        case METHOD_MIN:
                            allTypeses.min(AllTypes.FIELD_FLOAT);
                            break;
                        case METHOD_MAX:
                            allTypeses.max(AllTypes.FIELD_FLOAT);
                            break;
                        case METHOD_SUM:
                            allTypeses.sum(AllTypes.FIELD_FLOAT);
                            break;
                        case METHOD_AVG:
                            allTypeses.average(AllTypes.FIELD_FLOAT);
                            break;
                        case METHOD_SORT:
                            allTypeses.sort(AllTypes.FIELD_FLOAT);
                            break;
                        case METHOD_WHERE:
                            allTypeses.where();
                            break;
                        case METHOD_REMOVE:
                            allTypeses.remove(0);
                            break;
                        case METHOD_REMOVE_LAST:
                            allTypeses.removeLast();
                            break;
                        case METHOD_CLEAR:
                            allTypeses.clear();
                            break;
                    }
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                }
            }
        });
        Boolean result = future.get();
        realm.cancelTransaction();
        return result;
    }

    @Test
    public void clear() {
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE, results.size());

        realm.beginTransaction();
        results.clear();
        realm.commitTransaction();

        assertEquals(0, results.size());
    }

    @Test
    public void removeLast_emptyList() {
        RealmResults<AllTypes> resultsList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "Not there").findAll();
        assertEquals(0, resultsList.size());
        realm.beginTransaction();
        resultsList.removeLast();
        assertEquals(0, resultsList.size());
    }

    @Test
    public void get() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.get(0);
        assertTrue(allTypes.getColumnString().startsWith("test data"));
    }

    @Test
    public void first() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.first();
        assertTrue(allTypes.getColumnString().startsWith("test data 0"));
    }

    // first() and last() will throw an exception when no element exist
    @Test
    public void firstAndLast_throwsIfEmpty() {
        realm.beginTransaction();
        realm.clear(AllTypes.class);
        realm.commitTransaction();

        RealmResults<AllTypes> allTypes = realm.allObjects(AllTypes.class);
        assertEquals(0, allTypes.size());
        try {
            allTypes.first();
            fail();
        } catch (ArrayIndexOutOfBoundsException ignored) {}

        try {
            allTypes.last();
            fail();
        } catch (ArrayIndexOutOfBoundsException ignored) {}
    }

    @Test
    public void last() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.last();
        assertEquals((TEST_DATA_SIZE - 1), allTypes.getColumnLong());
    }

    @Test
    public void min() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();

        Number minimum = resultList.min(AllTypes.FIELD_LONG);
        assertEquals(0, minimum.intValue());
    }

    // Test min on empty columns
    @Test
    public void min_emptyNonNullFields() {
        RealmResults<NullTypes> results = realm.where(NullTypes.class).findAll();
        assertNull(results.min(NullTypes.FIELD_INTEGER_NOT_NULL));
        assertNull(results.min(NullTypes.FIELD_FLOAT_NOT_NULL));
        assertNull(results.min(NullTypes.FIELD_DOUBLE_NOT_NULL));
        assertNull(results.minDate(NullTypes.FIELD_DATE_NOT_NULL));
    }

    // Test min on nullable rows with all null values
    @Test
    public void min_emptyNullFields() {
        TestHelper.populateAllNullRowsForNumericTesting(realm);

        RealmResults<NullTypes> results = realm.where(NullTypes.class).findAll();
        assertNull(results.max(NullTypes.FIELD_INTEGER_NULL));
        assertNull(results.max(NullTypes.FIELD_FLOAT_NULL));
        assertNull(results.max(NullTypes.FIELD_DOUBLE_NULL));
        assertNull(results.maxDate(NullTypes.FIELD_DATE_NULL));
    }

    // Test min on nullable rows with partial null values
    @Test
    public void min_partialNullRows() {
        populatePartialNullRowsForNumericTesting();

        RealmResults<NullTypes> results = realm.where(NullTypes.class).findAll();
        assertEquals(0, results.min(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(0f, results.min(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(0d, results.min(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    @Test
    public void max() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();

        Number maximum = resultList.max(AllTypes.FIELD_LONG);
        assertEquals(TEST_DATA_SIZE - 1, maximum.intValue());
    }

    // Test max on empty columns
    @Test
    public void max_emptyNonNullFields() {
        RealmResults<NullTypes> results = realm.where(NullTypes.class).findAll();
        assertNull(results.max(NullTypes.FIELD_INTEGER_NOT_NULL));
        assertNull(results.max(NullTypes.FIELD_FLOAT_NOT_NULL));
        assertNull(results.max(NullTypes.FIELD_DOUBLE_NOT_NULL));
        assertNull(results.maxDate(NullTypes.FIELD_DATE_NOT_NULL));
    }

    // Test max on nullable rows with all null values
    @Test
    public void max_emptyNullFields() {
        TestHelper.populateAllNullRowsForNumericTesting(realm);

        RealmResults<NullTypes> results = realm.where(NullTypes.class).findAll();
        assertNull(results.max(NullTypes.FIELD_INTEGER_NULL));
        assertNull(results.max(NullTypes.FIELD_FLOAT_NULL));
        assertNull(results.max(NullTypes.FIELD_DOUBLE_NULL));
        assertNull(results.maxDate(NullTypes.FIELD_DATE_NULL));
    }

    // Test max on nullable rows with partial null values
    @Test
    public void max_partialNullRows() {
        populatePartialNullRowsForNumericTesting();

        RealmResults<NullTypes> results = realm.where(NullTypes.class).findAll();
        assertEquals(1, results.max(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(2f, results.max(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(3d, results.max(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    @Test
    public void sum() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();

        Number sum = resultList.sum(AllTypes.FIELD_LONG);
        // Sum of numbers 0 to M-1: (M-1)*M/2
        assertEquals((TEST_DATA_SIZE - 1) * TEST_DATA_SIZE / 2, sum.intValue());
    }

    // Test sum on nullable rows with all null values
    @Test
    public void sum_nullRows() {
        TestHelper.populateAllNullRowsForNumericTesting(realm);

        RealmResults<NullTypes> resultList = realm.where(NullTypes.class).findAll();
        assertEquals(0, resultList.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(0f, resultList.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(0d, resultList.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    // Test sum on nullable rows with partial null values
    @Test
    public void sum_partialNullRows() {
        populatePartialNullRowsForNumericTesting();
        RealmResults<NullTypes> resultList = realm.where(NullTypes.class).findAll();

        assertEquals(1, resultList.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(2f, resultList.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(3d, resultList.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    @Test
    public void sum_nonLatinColumnNames() {
        RealmResults<NonLatinFieldNames> resultList = realm.where(NonLatinFieldNames.class).findAll();

        Number sum = resultList.sum(NonLatinFieldNames.FIELD_LONG_KOREAN_CHAR);
        // Sum of numbers 0 to M-1: (M-1)*M/2
        assertEquals((TEST_DATA_SIZE - 1) * TEST_DATA_SIZE / 2, sum.intValue());

        sum = resultList.sum(NonLatinFieldNames.FIELD_LONG_GREEK_CHAR);
        // Sum of numbers 0 to M-1: (M-1)*M/2
        assertEquals((TEST_DATA_SIZE - 1) * TEST_DATA_SIZE / 2, sum.intValue());
    }

    @Test
    public void avg() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        double N = (double) TEST_DATA_SIZE;

        // Sum of numbers 1 to M: M*(M+1)/2
        // See setUp() for values of fields
        // N = TEST_DATA_SIZE

        // Type: double; a = 3.1415
        // a, a+1, ..., a+i, ..., a+N-1
        // sum = 3.1415*N + N*(N-1)/2
        // average = sum/N = 3.1415+(N-1)/2
        double average = 3.1415 + (N - 1.0) * 0.5;
        assertEquals(average, resultList.average(AllTypes.FIELD_DOUBLE), 0.0001);

        // Type: long
        // 0, 1, ..., N-1
        // sum = N*(N-1)/2
        // average = sum/N = (N-1)/2
        assertEquals(0.5 * (N - 1), resultList.average(AllTypes.FIELD_LONG), 0.0001);

        // Type: float; b = 1.234567
        // b, b+1, ..., b+i, ..., b+N-1
        // sum = b*N + N*(N-1)/2
        // average = sum/N = b + (N-1)/2
        assertEquals(1.234567 + 0.5 * (N - 1.0), resultList.average(AllTypes.FIELD_FLOAT), 0.0001);
    }

    // Test average on empty columns
    @Test
    public void avg_emptyNonNullFields() {
        RealmResults<NullTypes> resultList = realm.where(NullTypes.class).findAll();

        assertEquals(0d, resultList.average(NullTypes.FIELD_INTEGER_NOT_NULL), 0d);
        assertEquals(0d, resultList.average(NullTypes.FIELD_FLOAT_NOT_NULL), 0d);
        assertEquals(0d, resultList.average(NullTypes.FIELD_DOUBLE_NOT_NULL), 0d);
    }

    // Test average on nullable rows with all null values
    @Test
    public void avg_emptyNullFields() {
        TestHelper.populateAllNullRowsForNumericTesting(realm);

        RealmResults<NullTypes> resultList = realm.where(NullTypes.class).findAll();
        assertEquals(0d, resultList.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(0d, resultList.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(0d, resultList.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    // Test average on nullable rows with partial null values
    @Test
    public void avg_partialNullRows() {
        populatePartialNullRowsForNumericTesting();
        RealmResults<NullTypes> resultList = realm.where(NullTypes.class).findAll();

        assertEquals(0.5, resultList.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(1.0, resultList.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(1.5, resultList.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    @Test
    public void remove() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        realm.beginTransaction();
        resultList.remove(0);
        realm.commitTransaction();

        assertEquals(TEST_DATA_SIZE - 1, resultList.size());

        AllTypes allTypes = resultList.get(0);
        assertEquals(1, allTypes.getColumnLong());
    }

    @Test
    public void removeLast() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        realm.beginTransaction();
        resultList.removeLast();
        realm.commitTransaction();

        assertEquals("ResultList.removeLast did not remove record", TEST_DATA_SIZE - 1, resultList.size());

        AllTypes allTypes = resultList.get(resultList.size() - 1);
        assertEquals("ResultList.removeLast unexpected last record", TEST_DATA_SIZE - 2, allTypes.getColumnLong());

        RealmResults<AllTypes> resultListCheck = realm.where(AllTypes.class).findAll();
        assertEquals("ResultList.removeLast not committed", TEST_DATA_SIZE - 1, resultListCheck.size());
    }

    @Test
    public void sort_long() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = realm.allObjects(AllTypes.class);
        sortedList.sort(AllTypes.FIELD_LONG, Sort.DESCENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("First excepted to be last", resultList.first().getColumnLong(), sortedList.last().getColumnLong());

        sortedList.sort(AllTypes.FIELD_LONG, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("First excepted to be first", resultList.first().getColumnLong(), sortedList.first().getColumnLong());
        assertEquals("Last excepted to be last", resultList.last().getColumnLong(), sortedList.last().getColumnLong());

        sortedList.sort(AllTypes.FIELD_LONG, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, sortedList.size());
    }

    @Test
    public void sort_date() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(AllTypes.FIELD_DATE, Sort.DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(resultList.first().getColumnDate(), sortedList.last().getColumnDate());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(AllTypes.FIELD_DATE, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(resultList.first().getColumnDate(), reverseList.first().getColumnDate());
        assertEquals(resultList.last().getColumnDate(), reverseList.last().getColumnDate());

        RealmResults<AllTypes> reserveSortedList = reverseList.where().findAll();
        reserveSortedList.sort(AllTypes.FIELD_DATE, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
    }

    @Test
    public void sort_boolean() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(AllTypes.FIELD_BOOLEAN, Sort.DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(false, sortedList.last().isColumnBoolean());
        assertEquals(true, sortedList.first().isColumnBoolean());
        assertEquals(true, sortedList.get(TEST_DATA_FIRST_HALF).isColumnBoolean());
        assertEquals(false, sortedList.get(TEST_DATA_LAST_HALF).isColumnBoolean());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(AllTypes.FIELD_BOOLEAN, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(true, reverseList.last().isColumnBoolean());
        assertEquals(false, reverseList.first().isColumnBoolean());
        assertEquals(false, reverseList.get(TEST_DATA_FIRST_HALF).isColumnBoolean());
        assertEquals(true, reverseList.get(TEST_DATA_LAST_HALF).isColumnBoolean());

        RealmResults<AllTypes> reserveSortedList = reverseList.where().findAll();
        reserveSortedList.sort(AllTypes.FIELD_BOOLEAN, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
        assertEquals(reserveSortedList.first(), sortedList.first());
    }

    @Test
    public void sort_string() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(AllTypes.FIELD_STRING, Sort.DESCENDING);

        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(resultList.first().getColumnString(), sortedList.last().getColumnString());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(AllTypes.FIELD_STRING, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(resultList.first().getColumnString(), reverseList.first().getColumnString());

        int numberOfDigits = 1 + ((int) Math.log10(TEST_DATA_SIZE));
        int largestNumber = 1;
        for (int i = 1; i < numberOfDigits; i++)
            largestNumber *= 10;  // 10*10* ... *10
        largestNumber = largestNumber - 1;
        assertEquals(resultList.get(largestNumber).getColumnString(), reverseList.last().getColumnString());
        RealmResults<AllTypes> reverseSortedList = reverseList.where().findAll();
        reverseList.sort(AllTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseSortedList.size());
    }

    @Test
    public void sort_double() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(AllTypes.FIELD_DOUBLE, Sort.DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(resultList.first().getColumnDouble(), sortedList.last().getColumnDouble(), 0D);

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(AllTypes.FIELD_DOUBLE, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(resultList.first().getColumnDouble(), reverseList.first().getColumnDouble(), 0D);
        assertEquals(resultList.last().getColumnDouble(), reverseList.last().getColumnDouble(), 0D);

        RealmResults<AllTypes> reverseSortedList = reverseList.where().findAll();
        reverseSortedList.sort(AllTypes.FIELD_DOUBLE, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseSortedList.size());
    }

    @Test
    public void sort_float() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(AllTypes.FIELD_FLOAT, Sort.DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(resultList.first().getColumnFloat(), sortedList.last().getColumnFloat(), 0D);

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(AllTypes.FIELD_FLOAT, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(resultList.first().getColumnFloat(), reverseList.first().getColumnFloat(), 0D);
        assertEquals(resultList.last().getColumnFloat(), reverseList.last().getColumnFloat(), 0D);

        RealmResults<AllTypes> reverseSortedList = reverseList.where().findAll();
        reverseSortedList.sort(AllTypes.FIELD_FLOAT, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseSortedList.size());
    }

    private void doTestSortOnColumnWithPartialNullValues(String fieldName) {
        RealmResults<NullTypes> resultList = realm.where(NullTypes.class).findAll();
        // Ascending
        RealmResults<NullTypes> sortedList = realm.allObjects(NullTypes.class);
        sortedList.sort(fieldName, Sort.ASCENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        // Null should always be the first one in the ascending sorted list
        assertEquals(2, sortedList.first().getId());
        assertEquals(1, sortedList.last().getId());

        // Descending
        sortedList = realm.allObjects(NullTypes.class);
        sortedList.sort(fieldName, Sort.DESCENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(1, sortedList.first().getId());
        // Null should always be the last one in the descending sorted list
        assertEquals(2, sortedList.last().getId());
    }

    // Test sort on nullable fields with null values partially
    @Test
    public void sort_rowsWithPartialNullValues() {
        populatePartialNullRowsForNumericTesting();

        // 1 String
        doTestSortOnColumnWithPartialNullValues(NullTypes.FIELD_STRING_NULL);

        // 3 Boolean
        doTestSortOnColumnWithPartialNullValues(NullTypes.FIELD_BOOLEAN_NULL);

        // 6 Integer
        doTestSortOnColumnWithPartialNullValues(NullTypes.FIELD_INTEGER_NULL);

        // 7 Float
        doTestSortOnColumnWithPartialNullValues(NullTypes.FIELD_FLOAT_NULL);

        // 8 Double
        doTestSortOnColumnWithPartialNullValues(NullTypes.FIELD_DOUBLE_NULL);

        // 10 Date
        doTestSortOnColumnWithPartialNullValues(NullTypes.FIELD_DATE_NULL);
    }

    @Test
    public void sort_nonExistingColumn() {
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        thrown.expect(IllegalArgumentException.class);
        resultList.sort("Non-existing");
    }

    @Test
    public void sort_danishCharacters() {
        realm.beginTransaction();
        realm.clear(AllTypes.class);
        AllTypes at1 = realm.createObject(AllTypes.class);
        at1.setColumnString("Æble");
        AllTypes at2 = realm.createObject(AllTypes.class);
        at2.setColumnString("Øl");
        AllTypes at3 = realm.createObject(AllTypes.class);
        at3.setColumnString("Århus");
        realm.commitTransaction();

        RealmResults<AllTypes> result = realm.allObjects(AllTypes.class);
        RealmResults<AllTypes> sortedResult = result.where().findAll();
        sortedResult.sort(AllTypes.FIELD_STRING);

        assertEquals(3, sortedResult.size());
        assertEquals("Æble", sortedResult.first().getColumnString());
        assertEquals("Æble", sortedResult.get(0).getColumnString());
        assertEquals("Øl", sortedResult.get(1).getColumnString());
        assertEquals("Århus", sortedResult.get(2).getColumnString());

        RealmResults<AllTypes> reverseResult = result.where().findAll();
        reverseResult.sort(AllTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(3, reverseResult.size());
        assertEquals("Æble", reverseResult.last().getColumnString());
        assertEquals("Århus", reverseResult.get(0).getColumnString());
        assertEquals("Øl", reverseResult.get(1).getColumnString());
        assertEquals("Æble", reverseResult.get(2).getColumnString());
    }

    @Test
    public void sort_russianCharacters() {
        realm.beginTransaction();
        realm.clear(AllTypes.class);
        AllTypes at1 = realm.createObject(AllTypes.class);
        at1.setColumnString("Санкт-Петербург");
        AllTypes at2 = realm.createObject(AllTypes.class);
        at2.setColumnString("Москва");
        AllTypes at3 = realm.createObject(AllTypes.class);
        at3.setColumnString("Новороссийск");
        realm.commitTransaction();

        RealmResults<AllTypes> result = realm.allObjects(AllTypes.class);
        RealmResults<AllTypes> sortedResult = result.where().findAll();
        sortedResult.sort(AllTypes.FIELD_STRING);

        assertEquals(3, sortedResult.size());
        assertEquals("Москва", sortedResult.first().getColumnString());
        assertEquals("Москва", sortedResult.get(0).getColumnString());
        assertEquals("Новороссийск", sortedResult.get(1).getColumnString());
        assertEquals("Санкт-Петербург", sortedResult.get(2).getColumnString());

        RealmResults<AllTypes> reverseResult = result.where().findAll();
        reverseResult.sort(AllTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(3, reverseResult.size());
        assertEquals("Москва", reverseResult.last().getColumnString());
        assertEquals("Санкт-Петербург", reverseResult.get(0).getColumnString());
        assertEquals("Новороссийск", reverseResult.get(1).getColumnString());
        assertEquals("Москва", reverseResult.get(2).getColumnString());
    }

    @Test
    public void sort_greekCharacters() {
        realm.beginTransaction();
        realm.clear(AllTypes.class);
        AllTypes at1 = realm.createObject(AllTypes.class);
        at1.setColumnString("αύριο");
        AllTypes at2 = realm.createObject(AllTypes.class);
        at2.setColumnString("ημέρες");
        AllTypes at3 = realm.createObject(AllTypes.class);
        at3.setColumnString("δοκιμές");
        realm.commitTransaction();

        RealmResults<AllTypes> result = realm.allObjects(AllTypes.class);
        RealmResults<AllTypes> sortedResult = result.where().findAll();
        sortedResult.sort(AllTypes.FIELD_STRING);

        assertEquals(3, sortedResult.size());
        assertEquals("αύριο", sortedResult.first().getColumnString());
        assertEquals("αύριο", sortedResult.get(0).getColumnString());
        assertEquals("δοκιμές", sortedResult.get(1).getColumnString());
        assertEquals("ημέρες", sortedResult.get(2).getColumnString());

        RealmResults<AllTypes> reverseResult = result.where().findAll();
        reverseResult.sort(AllTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(3, reverseResult.size());
        assertEquals("αύριο", reverseResult.last().getColumnString());
        assertEquals("ημέρες", reverseResult.get(0).getColumnString());
        assertEquals("δοκιμές", reverseResult.get(1).getColumnString());
        assertEquals("αύριο", reverseResult.get(2).getColumnString());
    }

    //No sorting order defined. There are Korean, Arabic and Chinese characters.
    @Test
    public void sort_manyDifferentCharacters() {
        realm.beginTransaction();
        realm.clear(AllTypes.class);
        AllTypes at1 = realm.createObject(AllTypes.class);
        at1.setColumnString("단위");
        AllTypes at2 = realm.createObject(AllTypes.class);
        at2.setColumnString("테스트");
        AllTypes at3 = realm.createObject(AllTypes.class);
        at3.setColumnString("وحدة");
        AllTypes at4 = realm.createObject(AllTypes.class);
        at4.setColumnString("اختبار");
        AllTypes at5 = realm.createObject(AllTypes.class);
        at5.setColumnString("单位");
        AllTypes at6 = realm.createObject(AllTypes.class);
        at6.setColumnString("试验");
        AllTypes at7 = realm.createObject(AllTypes.class);
        at7.setColumnString("單位");
        AllTypes at8 = realm.createObject(AllTypes.class);
        at8.setColumnString("測試");
        realm.commitTransaction();

        RealmResults<AllTypes> result = realm.allObjects(AllTypes.class);
        RealmResults<AllTypes> sortedResult = result.where().findAll();
        sortedResult.sort(AllTypes.FIELD_STRING);

        assertEquals(8, sortedResult.size());

        @SuppressWarnings("UnnecessaryLocalVariable")
        RealmResults<AllTypes> reverseResult = result;
        reverseResult.sort(AllTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(8, reverseResult.size());
    }

    @Test
    public void sort_twoLanguages() {
        realm.beginTransaction();
        realm.clear(AllTypes.class);
        AllTypes allTypes1 = realm.createObject(AllTypes.class);
        allTypes1.setColumnString("test");
        AllTypes allTypes2 = realm.createObject(AllTypes.class);
        allTypes2.setColumnString("αύριο");
        AllTypes allTypes3 = realm.createObject(AllTypes.class);
        allTypes3.setColumnString("work");
        realm.commitTransaction();

        try {
            RealmResults<AllTypes> result = realm.allObjects(AllTypes.class);
            result.sort(AllTypes.FIELD_STRING);
        } catch (IllegalArgumentException e) {
            fail("Failed to sort with two kinds of alphabets");
        }
    }

    @Test
    public void sort_usingChildObject() {
        realm.beginTransaction();
        Owner owner = realm.createObject(Owner.class);
        owner.setName("owner");
        Cat cat = realm.createObject(Cat.class);
        cat.setName("cat");
        owner.setCat(cat);
        realm.commitTransaction();

        RealmQuery<Owner> query = realm.where(Owner.class);
        RealmResults<Owner> owners = query.findAll();

        try {
            owners.sort("cat.name");
            fail("Sorting by child object properties should result in a IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void sort_nullArguments() {
        RealmResults<AllTypes> result = realm.allObjects(AllTypes.class);
        try {
            result.sort(null);
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
        realm.beginTransaction();
        realm.clear(AllTypes.class);
        realm.commitTransaction();
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        assertEquals(0, results.size());
        results.sort(AllTypes.FIELD_STRING);
        assertEquals(0, results.size());
    }

    @Test
    public void sort_singleField() {
        RealmResults<AllTypes> sortedList = realm.allObjects(AllTypes.class);
        sortedList.sort(new String[]{AllTypes.FIELD_LONG}, new Sort[]{Sort.DESCENDING});
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(TEST_DATA_SIZE - 1, sortedList.first().getColumnLong());
        assertEquals(0, sortedList.last().getColumnLong());
    }

    @Test
    public void count() {
        assertEquals(TEST_DATA_SIZE, realm.where(AllTypes.class).count());
    }

    @Test
    public void findFirst() {
        AllTypes result = realm.where(AllTypes.class).findFirst();
        assertEquals(0, result.getColumnLong());
        assertEquals("test data 0", result.getColumnString());

        AllTypes none = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "smurf").findFirst();
        assertNull(none);
    }

    @Test
    public void where_equalTo_manyConditions() {
        RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        query.equalTo(AllTypes.FIELD_LONG, 0);
        for (int i = 1; i < TEST_DATA_SIZE; i++) {
            query.or().equalTo(AllTypes.FIELD_LONG, i);
        }
        RealmResults<AllTypes> allTypesRealmResults = query.findAll();
        assertEquals(TEST_DATA_SIZE, allTypesRealmResults.size());
    }

    @Test
    public void where() {
        RealmQuery<AllTypes> query = realm.where(AllTypes.class).findAll().where();
        assertNotNull(query);
    }

    @Test
    public void where_contains() {
        RealmQuery<AllTypes> query = realm.where(AllTypes.class).findAll().where();
        AllTypes item = query.findFirst();
        assertTrue("Item should exist in results.", query.findAll().contains(item));
    }

    @Test
    public void where_contains_null() {
        RealmQuery<AllTypes> query = realm.where(AllTypes.class).findAll().where();
        assertFalse("Should not contain a null item.", query.findAll().contains(null));
    }

    @Test
    public void where_shouldNotContainRemovedItem() {
        RealmQuery<AllTypes> query = realm.where(AllTypes.class).findAll().where();
        AllTypes item = realm.where(AllTypes.class).findFirst();
        realm.beginTransaction();
        item.removeFromRealm();
        realm.commitTransaction();
        assertFalse("Should not contain a removed item.", query.findAll().contains(item));
    }

    /**
     * Test to see if a particular item that does exist in the same Realm does not
     * exist in the result set of another query.
     */
    @Test
    public void where_lessThanGreaterThan() {
        RealmResults<AllTypes> items = realm.where(AllTypes.class).lessThan(AllTypes.FIELD_LONG, 1000).findAll();
        AllTypes anotherType = realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_LONG, 1000).findFirst();
        assertFalse("Should not be able to find item in another result list.", items.contains(anotherType));
    }

    // Tests that `contains()` correctly doesn't find RealmObjects that belongs to another Realm file.
    @Test
    public void contains_realmObjectFromOtherRealm() {
        RealmConfiguration realmConfig = configFactory.createConfiguration("contains_test.realm");
        Realm realmTwo = Realm.getInstance(realmConfig);
        try {

            realmTwo.beginTransaction();
            realmTwo.allObjects(AllTypes.class).clear();
            realmTwo.allObjects(NonLatinFieldNames.class).clear();

            for (int i = 0; i < TEST_DATA_SIZE; ++i) {
                AllTypes allTypes = realmTwo.createObject(AllTypes.class);
                allTypes.setColumnBoolean((i % 2) == 0);
                allTypes.setColumnBinary(new byte[]{1, 2, 3});
                allTypes.setColumnDate(new Date(YEAR_MILLIS * (i - TEST_DATA_SIZE / 2)));
                allTypes.setColumnDouble(3.1415 + i);
                allTypes.setColumnFloat(1.234567f + i);
                allTypes.setColumnString("test data " + i);
                allTypes.setColumnLong(i);
                Dog d = realmTwo.createObject(Dog.class);
                d.setName("Foo " + i);
                allTypes.setColumnRealmObject(d);
                allTypes.getColumnRealmList().add(d);
                NonLatinFieldNames nonLatinFieldNames = realmTwo.createObject(NonLatinFieldNames.class);
                nonLatinFieldNames.set델타(i);
                nonLatinFieldNames.setΔέλτα(i);
            }
            realmTwo.commitTransaction();

            final AllTypes item = realmTwo.where(AllTypes.class).findFirst();

            assertFalse("Should not be able to find one object in another Realm via RealmResults#contains",
                    realm.where(AllTypes.class).findAll().contains(item));

        } finally {
            if (realmTwo != null && !realmTwo.isClosed()) {
                realmTwo.close();
            }
        }
    }

    @Test
    public void where_findAll_size() {
        RealmResults<AllTypes> allTypes = realm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE, allTypes.size());

        // querying a RealmResults should find objects that fulfill the condition
        RealmResults<AllTypes> onedigits = allTypes.where().lessThan(AllTypes.FIELD_LONG, 10).findAll();
        assertEquals(Math.min(10, TEST_DATA_SIZE), onedigits.size());

        // if no objects fulfill conditions, the result has zero objects
        RealmResults<AllTypes> none = allTypes.where().greaterThan(AllTypes.FIELD_LONG, TEST_DATA_SIZE).findAll();
        assertEquals(0, none.size());

        // querying a result with zero objects must give zero objects
        RealmResults<AllTypes> stillNone = none.where().greaterThan(AllTypes.FIELD_LONG, TEST_DATA_SIZE).findAll();
        assertEquals(0, stillNone.size());
    }

    @Test
    public void where_findAllSorted() {
        RealmResults<AllTypes> allTypes = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_LONG, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, allTypes.size());
        assertEquals(0, allTypes.first().getColumnLong());
        assertEquals(TEST_DATA_SIZE - 1, allTypes.last().getColumnLong());

        RealmResults<AllTypes> reverseList = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_LONG, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(0, reverseList.last().getColumnLong());
        assertEquals(TEST_DATA_SIZE - 1, reverseList.first().getColumnLong());

        try {
            realm.where(AllTypes.class).findAllSorted("invalid",
                    Sort.DESCENDING);
            fail();
        } catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void where_queryDateField() {
        RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_DATE, new Date(YEAR_MILLIS * 5));
        RealmResults<AllTypes> all = query.findAll();
        assertEquals(1, query.count());
        assertEquals(1, all.size());

        // before 1901
        query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_DATE, new Date(YEAR_MILLIS * -100));
        all = query.findAll();
        assertEquals(1, query.count());
        assertEquals(1, all.size());

        // after 2038
        query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_DATE, new Date(YEAR_MILLIS * 100));
        all = query.findAll();
        assertEquals(1, query.count());
        assertEquals(1, all.size());
    }

    @Test
    public void indexOf() {
        try {
            RealmResults<AllTypes> all = realm.allObjects(AllTypes.class);
            all.indexOf(all.first());
            fail();
        } catch (NoSuchMethodError ignored) {}
    }

    @Test
    public void subList() {
        RealmResults<AllTypes> list = realm.allObjects(AllTypes.class);
        list.sort("columnLong");
        List<AllTypes> sublist = list.subList(Math.max(list.size() - 20, 0), list.size());
        assertEquals(TEST_DATA_SIZE - 1, sublist.get(sublist.size() - 1).getColumnLong());
    }

    // Setting a not-nullable field to null is an error
    // TODO Move this to RealmObjectTests?
    @Test
    public void setter_nullValueInRequiredField() {
        TestHelper.populateTestRealmForNullTests(realm);
        RealmResults<NullTypes> list = realm.allObjects(NullTypes.class);

        // 1 String
        try {
            realm.beginTransaction();
            list.first().setFieldStringNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            realm.cancelTransaction();
        }

        // 2 Bytes
        try {
            realm.beginTransaction();
            list.first().setFieldBytesNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            realm.cancelTransaction();
        }

        // 3 Boolean
        try {
            realm.beginTransaction();
            list.first().setFieldBooleanNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            realm.cancelTransaction();
        }

        // 4 Byte
        try {
            realm.beginTransaction();
            list.first().setFieldBytesNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            realm.cancelTransaction();
        }

        // 5 Short 6 Integer 7 Long are skipped for this case, same with Bytes

        // 8 Float
        try {
            realm.beginTransaction();
            list.first().setFieldFloatNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            realm.cancelTransaction();
        }

        // 9 Double
        try {
            realm.beginTransaction();
            list.first().setFieldDoubleNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            realm.cancelTransaction();
        }

        // 10 Date
        try {
            realm.beginTransaction();
            list.first().setFieldDateNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            realm.cancelTransaction();
        }
    }

    // Setting a nullable field to null is not an error
    // TODO Move this to RealmObjectsTest?
    @Test
    public void setter_nullValueInNullableField() {
        TestHelper.populateTestRealmForNullTests(realm);
        RealmResults<NullTypes> list = realm.allObjects(NullTypes.class);

        // 1 String
        realm.beginTransaction();
        list.first().setFieldStringNull(null);
        realm.commitTransaction();
        assertNull(realm.allObjects(NullTypes.class).first().getFieldStringNull());

        // 2 Bytes
        realm.beginTransaction();
        list.first().setFieldBytesNull(null);
        realm.commitTransaction();
        assertNull(realm.allObjects(NullTypes.class).first().getFieldBytesNull());

        // 3 Boolean
        realm.beginTransaction();
        list.first().setFieldBooleanNull(null);
        realm.commitTransaction();
        assertNull(realm.allObjects(NullTypes.class).first().getFieldBooleanNull());

        // 4 Byte
        // 5 Short 6 Integer 7 Long are skipped
        realm.beginTransaction();
        list.first().setFieldByteNull(null);
        realm.commitTransaction();
        assertNull(realm.allObjects(NullTypes.class).first().getFieldByteNull());

        // 8 Float
        realm.beginTransaction();
        list.first().setFieldFloatNull(null);
        realm.commitTransaction();
        assertNull(realm.allObjects(NullTypes.class).first().getFieldFloatNull());

        // 9 Double
        realm.beginTransaction();
        list.first().setFieldDoubleNull(null);
        realm.commitTransaction();
        assertNull(realm.allObjects(NullTypes.class).first().getFieldDoubleNull());

        // 10 Date
        realm.beginTransaction();
        list.first().setFieldDateNull(null);
        realm.commitTransaction();
        assertNull(realm.allObjects(NullTypes.class).first().getFieldDateNull());
    }

    @Test
    public void unsupportedMethods() {
        RealmResults<AllTypes> result = realm.where(AllTypes.class).findAll();

        try { //noinspection deprecation
            result.add(null);     fail();
        } catch (UnsupportedOperationException ignored) {
        }
        try {
            result.set(0, null);  fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }


    // Test that all methods that require a transaction (ie. any function that mutates Realm data)
    @Test
    public void mutableMethodsOutsideTransactions() {
        RealmResults<AllTypes> result = realm.where(AllTypes.class).findAll();

        try {
            result.clear();
            fail();
        } catch (IllegalStateException ignored) {
        }
        try {
            result.remove(0);
            fail();
        } catch (IllegalStateException ignored) {
        }
        try {
            result.removeLast();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    // TODO: More extended tests of querying all types must be done.

    @Test
    public void isValid() {
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();

        assertTrue(results.isValid());
        populateTestRealm(1);
        // still valid if result changed
        assertTrue(results.isValid());

        realm.close();
        assertFalse(results.isValid());
    }

    // Triggered an ARM bug
    @Test
    public void verifyArmComparisons() {
        realm.beginTransaction();
        realm.clear(AllTypes.class);
        long id = -1;
        for (int i = 0; i < 10; i++) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnLong(id--);
        }
        realm.commitTransaction();

        assertEquals(10, realm.where(AllTypes.class).between(AllTypes.FIELD_LONG, -10, -1).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).greaterThan(AllTypes.FIELD_LONG, -11).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).greaterThanOrEqualTo(AllTypes.FIELD_LONG, -10).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).lessThan(AllTypes.FIELD_LONG, 128).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).lessThan(AllTypes.FIELD_LONG, 127).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).lessThanOrEqualTo(AllTypes.FIELD_LONG, -1).findAll().size());
        assertEquals(10, realm.where(AllTypes.class).lessThan(AllTypes.FIELD_LONG, 0).findAll().size());
    }

    // RealmResults.distinct(): requires indexing, and type = boolean, integer, date, string
    private void populateForDistinct(Realm realm, long numberOfBlocks, long numberOfObjects, boolean withNull) {
        realm.beginTransaction();
        for (int i = 0; i < numberOfObjects * numberOfBlocks; i++) {
            for (int j = 0; j < numberOfBlocks; j++) {
                AnnotationIndexTypes obj = realm.createObject(AnnotationIndexTypes.class);
                obj.setIndexBoolean(j % 2 == 0);
                obj.setIndexLong(j);
                obj.setIndexDate(withNull ? null : new Date(1000 * (long) j));
                obj.setIndexString(withNull ? null : "Test " + j);
                obj.setNotIndexBoolean(j % 2 == 0);
                obj.setNotIndexLong(j);
                obj.setNotIndexDate(withNull ? null : new Date(1000 * (long) j));
                obj.setNotIndexString(withNull ? null : "Test " + j);
            }
        }
        realm.commitTransaction();
    }

    private void populateForDistinctInvalidTypesLinked(Realm realm) {
        realm.beginTransaction();
        AllJavaTypes notEmpty = new AllJavaTypes();
        notEmpty.setFieldBinary(new byte[]{1, 2, 3});
        realm.copyToRealm(notEmpty);
        realm.commitTransaction();
    }

    @Test
    public void distinct() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class).findAll().distinct("indexBoolean");
        assertEquals(2, distinctBool.size());
        for (String fieldName : new String[]{"Long", "Date", "String"}) {
            RealmResults<AnnotationIndexTypes> distinct = realm.where(AnnotationIndexTypes.class).findAll().distinct("index" + fieldName);
            assertEquals("index" + fieldName, numberOfBlocks, distinct.size());
        }
    }

    @Test
    public void distinct_withNull() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        for (String fieldName : new String[]{"Date", "String"}) {
            RealmResults<AnnotationIndexTypes> distinct = realm.where(AnnotationIndexTypes.class).findAll().distinct("index" + fieldName);
            assertEquals("index" + fieldName, 1, distinct.size());
        }
    }

    @Test
    public void distinct_noneIndexedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        for (String fieldName : new String[]{"Boolean", "Long", "Date", "String"}) {
            try {
                realm.where(AnnotationIndexTypes.class).findAll().distinct("notIndex" + fieldName);
                fail("notIndex" + fieldName);
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }

    @Test
    public void distinct_noneExistingField() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        try {
            realm.where(AnnotationIndexTypes.class).findAll().distinct("doesNotExist");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testDistinctInvalidTypes() {
        populateTestRealm();

        for (String field : new String[]{"columnRealmObject", "columnRealmList", "columnDouble", "columnFloat"}) {
            try {
                realm.where(AllTypes.class).findAll().distinct(field);
                fail(field);
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }

    @Test
    public void distinct_indexedLinkedFields(){
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        for (String fieldName : new String[]{"Boolean", "Long", "Date", "String"}) {
            try {
                realm.where(AnnotationIndexTypes.class).findAll().distinct(AnnotationIndexTypes.FIELD_OBJECT + ".index" + fieldName);
                fail("Unsupported Index" + fieldName + " linked field");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_notIndexedLinkedFields(){
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, true);

        for (String fieldName : new String[]{"Boolean", "Long", "Date", "String"}) {
            try {
                realm.where(AnnotationIndexTypes.class).findAll().distinct(AnnotationIndexTypes.FIELD_OBJECT + ".notIndex" + fieldName);
                fail("Unsupported notIndex" + fieldName + " linked field");
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void distinct_invalidTypesLinkedFields() {
        populateForDistinctInvalidTypesLinked(realm);

        try {
            realm.where(AllJavaTypes.class).findAll().distinct(AllJavaTypes.FIELD_OBJECT + ".columnBinary");
            fail("Unsupported columnBinary linked field");
        } catch (IllegalArgumentException ignored) {
        }
    }

    private RealmResults<Dog> populateRealmResultsOnDeletedLinkView() {
        realm.beginTransaction();
        Owner owner = realm.createObject(Owner.class);
        for (int i = 0; i < 10; i++) {
            Dog dog = new Dog();
            dog.setName("name_" + i);
            dog.setOwner(owner);
            owner.getDogs().add(dog);
        }
        realm.commitTransaction();


        RealmResults<Dog> dogs = owner.getDogs().where().equalTo(Dog.FIELD_NAME, "name_0").findAll();
        //dogs = dogs.where().findFirst().getOwner().getDogs().where().equalTo(Dog.FIELD_NAME, "name_0").findAll();

        realm.beginTransaction();
        owner.removeFromRealm();
        realm.commitTransaction();
        return dogs;
    }

    @Test
    public void isValid_resultsBuiltOnDeletedLinkView() {
        assertEquals(false, populateRealmResultsOnDeletedLinkView().isValid());
    }

    @Test
    public void size_resultsBuiltOnDeletedLinkView() {
        assertEquals(0, populateRealmResultsOnDeletedLinkView().size());
    }

    @Test
    public void first_resultsBuiltOnDeletedLinkView() {
        try {
            populateRealmResultsOnDeletedLinkView().first();
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void last_resultsBuiltOnDeletedLinkView() {
        try {
            populateRealmResultsOnDeletedLinkView().last();
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void sum_resultsBuiltOnDeletedLinkView() {
        RealmResults<Dog> dogs = populateRealmResultsOnDeletedLinkView();
        assertEquals(0, dogs.sum(Dog.FIELD_AGE).intValue());
        assertEquals(0f, dogs.sum(Dog.FIELD_HEIGHT).floatValue(), 0f);
        assertEquals(0d, dogs.sum(Dog.FIELD_WEIGHT).doubleValue(), 0d);
    }

    @Test
    public void average_resultsBuiltOnDeletedLinkView() {
        RealmResults<Dog> dogs = populateRealmResultsOnDeletedLinkView();
        assertEquals(0d, dogs.average(Dog.FIELD_AGE), 0d);
        assertEquals(0d, dogs.average(Dog.FIELD_HEIGHT), 0d);
        assertEquals(0d, dogs.average(Dog.FIELD_WEIGHT), 0d);
    }

    @Test
    public void clear_resultsBuiltOnDeletedLinkView() {
        RealmResults<Dog> dogs = populateRealmResultsOnDeletedLinkView();
        realm.beginTransaction();
        dogs.clear();
        assertEquals(0, dogs.size());
        realm.commitTransaction();
    }

    @Test
    public void max_resultsBuiltOnDeletedLinkView() {
        RealmResults<Dog> dogs = populateRealmResultsOnDeletedLinkView();
        assertNull(dogs.max(Dog.FIELD_AGE));
        assertNull(dogs.max(Dog.FIELD_HEIGHT));
        assertNull(dogs.max(Dog.FIELD_WEIGHT));
    }

    @Test
    public void max_dateResultsBuiltOnDeletedLinkView() {
        assertEquals(null, populateRealmResultsOnDeletedLinkView().maxDate(Dog.FIELD_BIRTHDAY));
    }

    @Test
    public void min_resultsBuiltOnDeletedLinkView() {
        RealmResults<Dog> dogs = populateRealmResultsOnDeletedLinkView();
        assertNull(dogs.min(Dog.FIELD_AGE));
        assertNull(dogs.min(Dog.FIELD_HEIGHT));
        assertNull(dogs.min(Dog.FIELD_WEIGHT));
    }

    @Test
    public void minDateResultsBuiltOnDeletedLinkView() {
        assertEquals(null, populateRealmResultsOnDeletedLinkView().minDate(Dog.FIELD_BIRTHDAY));
    }

    @Test
    public void whereResultsBuiltOnDeletedLinkView() {
        try {
            populateRealmResultsOnDeletedLinkView().where();
            fail();
        } catch (IllegalStateException e) {
            assertEquals("The RealmList which this RealmResults is created on has been deleted.", e.getMessage());
        }
    }
}
