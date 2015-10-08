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

import android.test.AndroidTestCase;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.realm.entities.AllTypes;
import io.realm.entities.Cat;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.NullTypes;
import io.realm.entities.Owner;

public class RealmResultsTest extends AndroidTestCase {
    protected final static int TEST_DATA_SIZE = 2516;
    protected final static int TEST_DATA_FIRST_HALF = 2 * (TEST_DATA_SIZE / 4) - 1;
    protected final static int TEST_DATA_LAST_HALF = 2 * (TEST_DATA_SIZE / 4) + 1;


    protected Realm testRealm;

    private final static String FIELD_STRING = "columnString";
    private final static String FIELD_LONG = "columnLong";
    private final static String FIELD_FLOAT = "columnFloat";
    private final static String FIELD_DOUBLE = "columnDouble";
    private final static String FIELD_BOOLEAN = "columnBoolean";
    private final static String FIELD_DATE = "columnDate";
    private final static String FIELD_KOREAN_CHAR = "델타";
    private final static String FIELD_GREEK_CHAR = "Δέλτα";

    private final static long YEAR_MILLIS = TimeUnit.DAYS.toMillis(365);

    @Override
    protected void setUp() throws InterruptedException {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext()).build();
        Realm.deleteRealm(realmConfig);
        testRealm = Realm.getInstance(realmConfig);
        populateTestRealm();
    }

    private void populateTestRealm(int objects) {
        testRealm.beginTransaction();
        testRealm.allObjects(AllTypes.class).clear();
        testRealm.allObjects(NonLatinFieldNames.class).clear();

        for (int i = 0; i < objects; ++i) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 2) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date(YEAR_MILLIS * (i - objects / 2)));
            allTypes.setColumnDouble(3.1415 + i);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            Dog d = testRealm.createObject(Dog.class);
            d.setName("Foo " + i);
            allTypes.setColumnRealmObject(d);
            allTypes.getColumnRealmList().add(d);
            NonLatinFieldNames nonLatinFieldNames = testRealm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
        }
        testRealm.commitTransaction();
    }

    private void populateTestRealm() {
        populateTestRealm(TEST_DATA_SIZE);
    }


    private void populatePartialNullRowsForNumericTesting () {
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

        testRealm.beginTransaction();
        testRealm.copyToRealm(nullTypes1);
        testRealm.copyToRealm(nullTypes2);
        testRealm.copyToRealm(nullTypes3);
        testRealm.commitTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        testRealm.close();
    }


    public void testMethodsThrowOnWrongThread() throws ExecutionException, InterruptedException {
        for (Method method : Method.values()) {
            assertTrue(methodWrongThread(method));
        }
    }

    private enum Method {
        METHOD_MIN,
        METHOD_MAX,
        METHOD_SUM,
        METHOD_AVG,
        METHOD_SORT,
        METHOD_WHERE
    }

    public boolean methodWrongThread(final Method method) throws ExecutionException, InterruptedException {
        final RealmResults<AllTypes> allTypeses = testRealm.where(AllTypes.class).findAll();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    switch (method) {
                        case METHOD_MIN:
                            allTypeses.min(FIELD_FLOAT);
                            break;
                        case METHOD_MAX:
                            allTypeses.max(FIELD_FLOAT);
                            break;
                        case METHOD_SUM:
                            allTypeses.sum(FIELD_FLOAT);
                            break;
                        case METHOD_AVG:
                            allTypeses.average(FIELD_FLOAT);
                            break;
                        case METHOD_SORT:
                            allTypeses.sort(FIELD_FLOAT);
                            break;
                        case METHOD_WHERE:
                            allTypeses.where();
                    }
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                }
            }
        });
        return future.get();
    }

    // test io.realm.ResultList Api

    // void clear(Class<?> classSpec)
    public void testClearEmptiesTable() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE, resultList.size());

        testRealm.beginTransaction();
        resultList.clear();
        testRealm.commitTransaction();

        assertEquals(0, resultList.size());
    }

    /*public void testRemoveLastShouldFail() {
        RealmResults<AllTypes> resultsList = realm.where(AllTypes.class).equalTo(FIELD_STRING, "Not there").findAll();
        try {
            realm.beginTransaction();
            resultsList.removeLast();
            fail("Should give exception");
        } catch (IllegalArgumentException e) {

        } finally {
            realm.commitTransaction();
        }
    }*/

    public void testResultListGet() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.get(0);
        assertTrue(allTypes.getColumnString().startsWith("test data"));
    }

    public void testResultListFirstIsFirst() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.first();
        assertTrue(allTypes.getColumnString().startsWith("test data 0"));
    }

    // first() and last() will throw an exception when no element exist
    public void testResultListFirstLastThrowIfEmpty() {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        testRealm.commitTransaction();

        RealmResults<AllTypes> allTypes = testRealm.allObjects(AllTypes.class);
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

    public void testResultListLastIsLast() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.last();
        assertEquals((TEST_DATA_SIZE - 1), allTypes.getColumnLong());
    }

    public void testMinValueIsMinValue() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Number minimum = resultList.min(FIELD_LONG);
        assertEquals(0, minimum.intValue());
    }

    // Test min on empty columns
    public void testMinValueForEmptyColumns() {
        RealmResults<NullTypes> results = testRealm.where(NullTypes.class).findAll();
        assertNull(results.min(NullTypes.FIELD_INTEGER_NOT_NULL));
        assertNull(results.min(NullTypes.FIELD_FLOAT_NOT_NULL));
        assertNull(results.min(NullTypes.FIELD_DOUBLE_NOT_NULL));
        assertNull(results.minDate(NullTypes.FIELD_DATE_NOT_NULL));
    }

    // Test min on nullable rows with all null values
    public void testMinValueForAllNullRows() {
        TestHelper.populateAllNullRowsForNumericTesting(testRealm);

        RealmResults<NullTypes> results = testRealm.where(NullTypes.class).findAll();
        assertNull(results.max(NullTypes.FIELD_INTEGER_NULL));
        assertNull(results.max(NullTypes.FIELD_FLOAT_NULL));
        assertNull(results.max(NullTypes.FIELD_DOUBLE_NULL));
        assertNull(results.maxDate(NullTypes.FIELD_DATE_NULL));
    }

    // Test min on nullable rows with partial null values
    public void testMinValueForPartialNullRows() {
        populatePartialNullRowsForNumericTesting();

        RealmResults<NullTypes> results = testRealm.where(NullTypes.class).findAll();
        assertEquals(0, results.min(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(0f, results.min(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(0d, results.min(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    public void testMaxValueIsMaxValue() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Number maximum = resultList.max(FIELD_LONG);
        assertEquals(TEST_DATA_SIZE - 1, maximum.intValue());
    }

    // Test max on empty columns
    public void testMaxValueForEmptyColumns() {
        RealmResults<NullTypes> results = testRealm.where(NullTypes.class).findAll();
        assertNull(results.max(NullTypes.FIELD_INTEGER_NOT_NULL));
        assertNull(results.max(NullTypes.FIELD_FLOAT_NOT_NULL));
        assertNull(results.max(NullTypes.FIELD_DOUBLE_NOT_NULL));
        assertNull(results.maxDate(NullTypes.FIELD_DATE_NOT_NULL));
    }

    // Test max on nullable rows with all null values
    public void testMaxValueForAllNullRows() {
        TestHelper.populateAllNullRowsForNumericTesting(testRealm);

        RealmResults<NullTypes> results = testRealm.where(NullTypes.class).findAll();
        assertNull(results.max(NullTypes.FIELD_INTEGER_NULL));
        assertNull(results.max(NullTypes.FIELD_FLOAT_NULL));
        assertNull(results.max(NullTypes.FIELD_DOUBLE_NULL));
        assertNull(results.maxDate(NullTypes.FIELD_DATE_NULL));
    }

    // Test max on nullable rows with partial null values
    public void testMaxValueForPartialNullRows() {
        populatePartialNullRowsForNumericTesting();

        RealmResults<NullTypes> results = testRealm.where(NullTypes.class).findAll();
        assertEquals(1, results.max(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(2f, results.max(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(3d, results.max(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    public void testSumGivesCorrectValue() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Number sum = resultList.sum(FIELD_LONG);
        // Sum of numbers 0 to M-1: (M-1)*M/2
        assertEquals((TEST_DATA_SIZE - 1) * TEST_DATA_SIZE / 2, sum.intValue());
    }

    // Test sum on nullable rows with all null values
    public void testSumGivesCorrectValueForAllNullRows() {
        TestHelper.populateAllNullRowsForNumericTesting(testRealm);

        RealmResults<NullTypes> resultList = testRealm.where(NullTypes.class).findAll();
        assertEquals(0, resultList.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(0f, resultList.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(0d, resultList.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    // Test sum on nullable rows with partial null values
    public void testSumGivesCorrectValueForPartialNullRows() {
        populatePartialNullRowsForNumericTesting();
        RealmResults<NullTypes> resultList = testRealm.where(NullTypes.class).findAll();

        assertEquals(1, resultList.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(2f, resultList.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(3d, resultList.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    public void testSumGivesCorrectValueWithNonLatinColumnNames() {
        RealmResults<NonLatinFieldNames> resultList = testRealm.where(NonLatinFieldNames.class).findAll();

        Number sum = resultList.sum(FIELD_KOREAN_CHAR);
        // Sum of numbers 0 to M-1: (M-1)*M/2
        assertEquals((TEST_DATA_SIZE - 1) * TEST_DATA_SIZE / 2, sum.intValue());

        sum = resultList.sum(FIELD_GREEK_CHAR);
        // Sum of numbers 0 to M-1: (M-1)*M/2
        assertEquals((TEST_DATA_SIZE - 1) * TEST_DATA_SIZE / 2, sum.intValue());
    }

    public void testAvgGivesCorrectValue() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        double N = (double) TEST_DATA_SIZE;

        // Sum of numbers 1 to M: M*(M+1)/2
        // See setUp() for values of fields
        // N = TEST_DATA_SIZE

        // Type: double; a = 3.1415
        // a, a+1, ..., a+i, ..., a+N-1
        // sum = 3.1415*N + N*(N-1)/2
        // average = sum/N = 3.1415+(N-1)/2
        double average = 3.1415 + (N - 1.0) * 0.5;
        assertEquals(average, resultList.average(FIELD_DOUBLE), 0.0001);

        // Type: long
        // 0, 1, ..., N-1
        // sum = N*(N-1)/2
        // average = sum/N = (N-1)/2
        assertEquals(0.5 * (N - 1), resultList.average(FIELD_LONG), 0.0001);

        // Type: float; b = 1.234567
        // b, b+1, ..., b+i, ..., b+N-1
        // sum = b*N + N*(N-1)/2
        // average = sum/N = b + (N-1)/2
        assertEquals(1.234567 + 0.5 * (N - 1.0), resultList.average(FIELD_FLOAT), 0.0001);
    }

    // Test average on empty columns
    public void testAvgGivesCorrectValueForEmptyColumns() {
        RealmResults<NullTypes> resultList = testRealm.where(NullTypes.class).findAll();

        assertEquals(0d, resultList.average(NullTypes.FIELD_INTEGER_NOT_NULL), 0d);
        assertEquals(0d, resultList.average(NullTypes.FIELD_FLOAT_NOT_NULL), 0d);
        assertEquals(0d, resultList.average(NullTypes.FIELD_DOUBLE_NOT_NULL), 0d);
    }

    // Test average on nullable rows with all null values
    public void testAvgGivesCorrectValueForAllNullRows() {
        TestHelper.populateAllNullRowsForNumericTesting(testRealm);

        RealmResults<NullTypes> resultList = testRealm.where(NullTypes.class).findAll();
        assertEquals(0d, resultList.average(NullTypes.FIELD_INTEGER_NULL), 0d);
        assertEquals(0d, resultList.average(NullTypes.FIELD_FLOAT_NULL), 0d);
        assertEquals(0d, resultList.average(NullTypes.FIELD_DOUBLE_NULL), 0d);
    }

    // Test sum on nullable rows with partial null values
    public void testAvgGivesCorrectValueForPartialNullRows() {
        populatePartialNullRowsForNumericTesting();
        RealmResults<NullTypes> resultList = testRealm.where(NullTypes.class).findAll();

        assertEquals(1, resultList.sum(NullTypes.FIELD_INTEGER_NULL).intValue());
        assertEquals(2f, resultList.sum(NullTypes.FIELD_FLOAT_NULL).floatValue(), 0f);
        assertEquals(3d, resultList.sum(NullTypes.FIELD_DOUBLE_NULL).doubleValue(), 0d);
    }

    public void testRemove() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        testRealm.beginTransaction();
        resultList.remove(0);
        testRealm.commitTransaction();

        assertEquals(TEST_DATA_SIZE - 1, resultList.size());

        AllTypes allTypes = resultList.get(0);
        assertEquals(1, allTypes.getColumnLong());
    }

    public void testRemoveLast() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        testRealm.beginTransaction();
        resultList.removeLast();
        testRealm.commitTransaction();

        assertEquals("ResultList.removeLast did not remove record", TEST_DATA_SIZE - 1, resultList.size());

        AllTypes allTypes = resultList.get(resultList.size() - 1);
        assertEquals("ResultList.removeLast unexpected last record", TEST_DATA_SIZE - 2, allTypes.getColumnLong());

        RealmResults<AllTypes> resultListCheck = testRealm.where(AllTypes.class).findAll();
        assertEquals("ResultList.removeLast not committed", TEST_DATA_SIZE - 1, resultListCheck.size());
    }

    public void testRemoveLastEmptyList() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        testRealm.beginTransaction();
        resultList.clear();
        assertEquals(0, resultList.size());
        resultList.removeLast();
        testRealm.commitTransaction();

        assertEquals(0, resultList.size());
    }

    public void testSortByLong() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = testRealm.allObjects(AllTypes.class);
        sortedList.sort(FIELD_LONG, Sort.DESCENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("First excepted to be last", resultList.first().getColumnLong(), sortedList.last().getColumnLong());

        RealmResults<AllTypes> reverseList = sortedList;
        reverseList.sort(FIELD_LONG, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals("First excepted to be first", resultList.first().getColumnLong(), reverseList.first().getColumnLong());
        assertEquals("Last excepted to be last", resultList.last().getColumnLong(), reverseList.last().getColumnLong());

        RealmResults<AllTypes> reserveSortedList = reverseList;
        reverseList.sort(FIELD_LONG, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
    }

    public void testSortByDate() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(FIELD_DATE, Sort.DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(resultList.first().getColumnDate(), sortedList.last().getColumnDate());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(FIELD_DATE, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(resultList.first().getColumnDate(), reverseList.first().getColumnDate());
        assertEquals(resultList.last().getColumnDate(), reverseList.last().getColumnDate());

        RealmResults<AllTypes> reserveSortedList = reverseList.where().findAll();
        reserveSortedList.sort(FIELD_DATE, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
    }

    public void testSortByBoolean() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(FIELD_BOOLEAN, Sort.DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(false, sortedList.last().isColumnBoolean());
        assertEquals(true, sortedList.first().isColumnBoolean());
        assertEquals(true, sortedList.get(TEST_DATA_FIRST_HALF).isColumnBoolean());
        assertEquals(false, sortedList.get(TEST_DATA_LAST_HALF).isColumnBoolean());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(FIELD_BOOLEAN, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(true, reverseList.last().isColumnBoolean());
        assertEquals(false, reverseList.first().isColumnBoolean());
        assertEquals(false, reverseList.get(TEST_DATA_FIRST_HALF).isColumnBoolean());
        assertEquals(true, reverseList.get(TEST_DATA_LAST_HALF).isColumnBoolean());

        RealmResults<AllTypes> reserveSortedList = reverseList.where().findAll();
        reserveSortedList.sort(FIELD_BOOLEAN, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
        assertEquals(reserveSortedList.first(), sortedList.first());
    }

    public void testSortByString() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(FIELD_STRING, Sort.DESCENDING);

        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(resultList.first().getColumnString(), sortedList.last().getColumnString());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(FIELD_STRING, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(resultList.first().getColumnString(), reverseList.first().getColumnString());

        int numberOfDigits = 1 + ((int) Math.log10(TEST_DATA_SIZE));
        int largestNumber = 1;
        for (int i = 1; i < numberOfDigits; i++)
            largestNumber *= 10;  // 10*10* ... *10
        largestNumber = largestNumber - 1;
        assertEquals(resultList.get(largestNumber).getColumnString(), reverseList.last().getColumnString());
        RealmResults<AllTypes> reverseSortedList = reverseList.where().findAll();
        reverseList.sort(FIELD_STRING, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseSortedList.size());
    }

    public void testSortByDouble() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(FIELD_DOUBLE, Sort.DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(resultList.first().getColumnDouble(), sortedList.last().getColumnDouble());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(FIELD_DOUBLE, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(resultList.first().getColumnDouble(), reverseList.first().getColumnDouble());
        assertEquals(resultList.last().getColumnDouble(), reverseList.last().getColumnDouble());

        RealmResults<AllTypes> reverseSortedList = reverseList.where().findAll();
        reverseSortedList.sort(FIELD_DOUBLE, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseSortedList.size());
    }

    public void testSortByFloat() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(FIELD_FLOAT, Sort.DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(resultList.first().getColumnFloat(), sortedList.last().getColumnFloat());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(FIELD_FLOAT, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(resultList.first().getColumnFloat(), reverseList.first().getColumnFloat());
        assertEquals(resultList.last().getColumnFloat(), reverseList.last().getColumnFloat());

        RealmResults<AllTypes> reverseSortedList = reverseList.where().findAll();
        reverseSortedList.sort(FIELD_FLOAT, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseSortedList.size());
    }

    private void doTestSortOnColumnWithPartialNullValues(String fieldName) {
        RealmResults<NullTypes> resultList = testRealm.where(NullTypes.class).findAll();
        // Ascending
        RealmResults<NullTypes> sortedList = testRealm.allObjects(NullTypes.class);
        sortedList.sort(fieldName, Sort.ASCENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        // Null should always be the first one in the ascending sorted list
        assertEquals(2, sortedList.first().getId());
        assertEquals(1, sortedList.last().getId());

        // Descending
        sortedList = testRealm.allObjects(NullTypes.class);
        sortedList.sort(fieldName, Sort.DESCENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(1, sortedList.first().getId());
        // Null should always be the last one in the descending sorted list
        assertEquals(2, sortedList.last().getId());
    }

    // Test sort on nullable fields with null values partially
    public void testSortOnColumnWithPartialNullValues() {
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

    public void testSortOnNonExistingColumn() {
        try {
            RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
            resultList.sort("Non-existing");
            fail("Column should not exist");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testSortWithDanishCharacters() {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes at1 = testRealm.createObject(AllTypes.class);
        at1.setColumnString("Æble");
        AllTypes at2 = testRealm.createObject(AllTypes.class);
        at2.setColumnString("Øl");
        AllTypes at3 = testRealm.createObject(AllTypes.class);
        at3.setColumnString("Århus");
        testRealm.commitTransaction();

        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);
        RealmResults<AllTypes> sortedResult = result.where().findAll();
        sortedResult.sort(FIELD_STRING);

        assertEquals(3, sortedResult.size());
        assertEquals("Æble", sortedResult.first().getColumnString());
        assertEquals("Æble", sortedResult.get(0).getColumnString());
        assertEquals("Øl", sortedResult.get(1).getColumnString());
        assertEquals("Århus", sortedResult.get(2).getColumnString());

        RealmResults<AllTypes> reverseResult = result.where().findAll();
        reverseResult.sort(FIELD_STRING, Sort.DESCENDING);
        assertEquals(3, reverseResult.size());
        assertEquals("Æble", reverseResult.last().getColumnString());
        assertEquals("Århus", reverseResult.get(0).getColumnString());
        assertEquals("Øl", reverseResult.get(1).getColumnString());
        assertEquals("Æble", reverseResult.get(2).getColumnString());
    }

    public void testSortWithRussianCharacters() {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes at1 = testRealm.createObject(AllTypes.class);
        at1.setColumnString("Санкт-Петербург");
        AllTypes at2 = testRealm.createObject(AllTypes.class);
        at2.setColumnString("Москва");
        AllTypes at3 = testRealm.createObject(AllTypes.class);
        at3.setColumnString("Новороссийск");
        testRealm.commitTransaction();

        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);
        RealmResults<AllTypes> sortedResult = result.where().findAll();
        sortedResult.sort(FIELD_STRING);

        assertEquals(3, sortedResult.size());
        assertEquals("Москва", sortedResult.first().getColumnString());
        assertEquals("Москва", sortedResult.get(0).getColumnString());
        assertEquals("Новороссийск", sortedResult.get(1).getColumnString());
        assertEquals("Санкт-Петербург", sortedResult.get(2).getColumnString());

        RealmResults<AllTypes> reverseResult = result.where().findAll();
        reverseResult.sort(FIELD_STRING, Sort.DESCENDING);
        assertEquals(3, reverseResult.size());
        assertEquals("Москва", reverseResult.last().getColumnString());
        assertEquals("Санкт-Петербург", reverseResult.get(0).getColumnString());
        assertEquals("Новороссийск", reverseResult.get(1).getColumnString());
        assertEquals("Москва", reverseResult.get(2).getColumnString());
    }

    public void testSortWithGreekCharacters() {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes at1 = testRealm.createObject(AllTypes.class);
        at1.setColumnString("αύριο");
        AllTypes at2 = testRealm.createObject(AllTypes.class);
        at2.setColumnString("ημέρες");
        AllTypes at3 = testRealm.createObject(AllTypes.class);
        at3.setColumnString("δοκιμές");
        testRealm.commitTransaction();

        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);
        RealmResults<AllTypes> sortedResult = result.where().findAll();
        sortedResult.sort(FIELD_STRING);

        assertEquals(3, sortedResult.size());
        assertEquals("αύριο", sortedResult.first().getColumnString());
        assertEquals("αύριο", sortedResult.get(0).getColumnString());
        assertEquals("δοκιμές", sortedResult.get(1).getColumnString());
        assertEquals("ημέρες", sortedResult.get(2).getColumnString());

        RealmResults<AllTypes> reverseResult = result.where().findAll();
        reverseResult.sort(FIELD_STRING, Sort.DESCENDING);
        assertEquals(3, reverseResult.size());
        assertEquals("αύριο", reverseResult.last().getColumnString());
        assertEquals("ημέρες", reverseResult.get(0).getColumnString());
        assertEquals("δοκιμές", reverseResult.get(1).getColumnString());
        assertEquals("αύριο", reverseResult.get(2).getColumnString());
    }

    //No sorting order defined. There are Korean, Arabic and Chinese characters.
    public void testSortWithManyDifferentCharacters() {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes at1 = testRealm.createObject(AllTypes.class);
        at1.setColumnString("단위");
        AllTypes at2 = testRealm.createObject(AllTypes.class);
        at2.setColumnString("테스트");
        AllTypes at3 = testRealm.createObject(AllTypes.class);
        at3.setColumnString("وحدة");
        AllTypes at4 = testRealm.createObject(AllTypes.class);
        at4.setColumnString("اختبار");
        AllTypes at5 = testRealm.createObject(AllTypes.class);
        at5.setColumnString("单位");
        AllTypes at6 = testRealm.createObject(AllTypes.class);
        at6.setColumnString("试验");
        AllTypes at7 = testRealm.createObject(AllTypes.class);
        at7.setColumnString("單位");
        AllTypes at8 = testRealm.createObject(AllTypes.class);
        at8.setColumnString("測試");
        testRealm.commitTransaction();

        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);
        RealmResults<AllTypes> sortedResult = result.where().findAll();
        sortedResult.sort(FIELD_STRING);

        assertEquals(8, sortedResult.size());

        @SuppressWarnings("UnnecessaryLocalVariable")
        RealmResults<AllTypes> reverseResult = result;
        reverseResult.sort(FIELD_STRING, Sort.DESCENDING);
        assertEquals(8, reverseResult.size());
    }

    public void testSortWithTwoLanguages() {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes allTypes1 = testRealm.createObject(AllTypes.class);
        allTypes1.setColumnString("test");
        AllTypes allTypes2 = testRealm.createObject(AllTypes.class);
        allTypes2.setColumnString("αύριο");
        AllTypes allTypes3 = testRealm.createObject(AllTypes.class);
        allTypes3.setColumnString("work");
        testRealm.commitTransaction();

        try {
            RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);
            result.sort(FIELD_STRING);
        } catch (IllegalArgumentException e) {
            fail("Failed to sort with two kinds of alphabets");
        }
    }

    public void testSortByChildObject() {
        testRealm.beginTransaction();
        Owner owner = testRealm.createObject(Owner.class);
        owner.setName("owner");
        Cat cat = testRealm.createObject(Cat.class);
        cat.setName("cat");
        owner.setCat(cat);
        testRealm.commitTransaction();

        RealmQuery<Owner> query = testRealm.where(Owner.class);
        RealmResults<Owner> owners = query.findAll();

        try {
            owners.sort("cat.name");
            fail("Sorting by child object properties should result in a IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
    }

    public void testSortWithNullThrows() {
        RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);
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

    public void testWithEmptyRealmObjects() {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        testRealm.commitTransaction();
        try {
            testRealm.where(AllTypes.class).findAll().sort(FIELD_STRING);
        } catch (IllegalArgumentException e) {
            fail("Failed to sort an empty RealmResults");
        }
    }

    public void testSortSingleField() {
        RealmResults<AllTypes> sortedList = testRealm.allObjects(AllTypes.class);
        sortedList.sort(new String[]{FIELD_LONG}, new Sort[]{Sort.DESCENDING});
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(TEST_DATA_SIZE - 1, sortedList.first().getColumnLong());
        assertEquals(0, sortedList.last().getColumnLong());
    }

    public void testCount() {
        assertEquals(TEST_DATA_SIZE, testRealm.where(AllTypes.class).count());
    }

    public void testFindFirst() {
        AllTypes result = testRealm.where(AllTypes.class).findFirst();
        assertEquals(0, result.getColumnLong());
        assertEquals("test data 0", result.getColumnString());

        AllTypes none = testRealm.where(AllTypes.class).equalTo(FIELD_STRING, "smurf").findFirst();
        assertNull(none);
    }

    public void testManyConditions() {
        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class);
        query.equalTo(FIELD_LONG, 0);
        for (int i = 1; i < TEST_DATA_SIZE; i++) {
            query.or().equalTo(FIELD_LONG, i);
        }
        RealmResults<AllTypes> allTypesRealmResults = query.findAll();
        assertEquals(TEST_DATA_SIZE, allTypesRealmResults.size());
    }

    public void testWhere() {
        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).findAll().where();
        assertNotNull(query);
    }

    public void testQueryResult() {
        RealmResults<AllTypes> allTypes = testRealm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE, allTypes.size());

        // querying a RealmResults should find objects that fulfill the condition
        RealmResults<AllTypes> onedigits = allTypes.where().lessThan(FIELD_LONG, 10).findAll();
        assertEquals(Math.min(10, TEST_DATA_SIZE), onedigits.size());

        // if no objects fulfill conditions, the result has zero objects
        RealmResults<AllTypes> none = allTypes.where().greaterThan(FIELD_LONG, TEST_DATA_SIZE).findAll();
        assertEquals(0, none.size());

        // querying a result with zero objects must give zero objects
        RealmResults<AllTypes> stillNone = none.where().greaterThan(FIELD_LONG, TEST_DATA_SIZE).findAll();
        assertEquals(0, stillNone.size());
    }

    public void testFindAllSorted() {
        RealmResults<AllTypes> allTypes = testRealm.where(AllTypes.class).findAllSorted(FIELD_LONG, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, allTypes.size());
        assertEquals(0, allTypes.first().getColumnLong());
        assertEquals(TEST_DATA_SIZE - 1, allTypes.last().getColumnLong());

        RealmResults<AllTypes> reverseList = testRealm.where(AllTypes.class).findAllSorted(FIELD_LONG, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(0, reverseList.last().getColumnLong());
        assertEquals(TEST_DATA_SIZE - 1, reverseList.first().getColumnLong());

        try {
            testRealm.where(AllTypes.class).findAllSorted("invalid",
                    Sort.DESCENDING);
            fail();
        } catch (IllegalArgumentException ignored) {}
    }

    public void testQueryDateField() {
        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).equalTo(FIELD_DATE, new Date(YEAR_MILLIS * 5));
        RealmResults<AllTypes> all = query.findAll();
        assertEquals(1, query.count());
        assertEquals(1, all.size());

        // before 1901
        query = testRealm.where(AllTypes.class).equalTo(FIELD_DATE, new Date(YEAR_MILLIS * -100));
        all = query.findAll();
        assertEquals(1, query.count());
        assertEquals(1, all.size());

        // after 2038
        query = testRealm.where(AllTypes.class).equalTo(FIELD_DATE, new Date(YEAR_MILLIS * 100));
        all = query.findAll();
        assertEquals(1, query.count());
        assertEquals(1, all.size());
    }

    public void testIndexOf() {
        try {
            RealmResults<AllTypes> all = testRealm.allObjects(AllTypes.class);
            all.indexOf(all.first());
            fail();
        } catch (NoSuchMethodError ignored) {}
    }

    public void testSubList() {
        RealmResults<AllTypes> list = testRealm.allObjects(AllTypes.class);
        list.sort("columnLong");
        List<AllTypes> sublist = list.subList(Math.max(list.size() - 20, 0), list.size());
        assertEquals(TEST_DATA_SIZE - 1, sublist.get(sublist.size() - 1).getColumnLong());
    }

    // Setting a not-nullable field to null is an error
    public void testNullFieldNotNullableField() {
        TestHelper.populateTestRealmForNullTests(testRealm);
        RealmResults<NullTypes> list = testRealm.allObjects(NullTypes.class);

        // 1 String
        try {
            testRealm.beginTransaction();
            list.first().setFieldStringNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            testRealm.cancelTransaction();
        }

        // 2 Bytes
        try {
            testRealm.beginTransaction();
            list.first().setFieldBytesNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            testRealm.cancelTransaction();
        }

        // 3 Boolean
        try {
            testRealm.beginTransaction();
            list.first().setFieldBooleanNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            testRealm.cancelTransaction();
        }

        // 4 Byte
        try {
            testRealm.beginTransaction();
            list.first().setFieldBytesNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            testRealm.cancelTransaction();
        }

        // 5 Short 6 Integer 7 Long are skipped for this case, same with Bytes

        // 8 Float
        try {
            testRealm.beginTransaction();
            list.first().setFieldFloatNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            testRealm.cancelTransaction();
        }

        // 9 Double
        try {
            testRealm.beginTransaction();
            list.first().setFieldDoubleNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            testRealm.cancelTransaction();
        }

        // 10 Date
        try {
            testRealm.beginTransaction();
            list.first().setFieldDateNotNull(null);
            fail();
        }
        catch (IllegalArgumentException ignored) {
        }
        finally {
            testRealm.cancelTransaction();
        }
    }

    // Setting a nullable field to null is not an error
    public void testSetNullField() {
        TestHelper.populateTestRealmForNullTests(testRealm);
        RealmResults<NullTypes> list = testRealm.allObjects(NullTypes.class);

        // 1 String
        testRealm.beginTransaction();
        list.first().setFieldStringNull(null);
        testRealm.commitTransaction();
        assertNull(testRealm.allObjects(NullTypes.class).first().getFieldStringNull());

        // 2 Bytes
        testRealm.beginTransaction();
        list.first().setFieldBytesNull(null);
        testRealm.commitTransaction();
        assertNull(testRealm.allObjects(NullTypes.class).first().getFieldBytesNull());

        // 3 Boolean
        testRealm.beginTransaction();
        list.first().setFieldBooleanNull(null);
        testRealm.commitTransaction();
        assertNull(testRealm.allObjects(NullTypes.class).first().getFieldBooleanNull());

        // 4 Byte
        // 5 Short 6 Integer 7 Long are skipped
        testRealm.beginTransaction();
        list.first().setFieldByteNull(null);
        testRealm.commitTransaction();
        assertNull(testRealm.allObjects(NullTypes.class).first().getFieldByteNull());

        // 8 Float
        testRealm.beginTransaction();
        list.first().setFieldFloatNull(null);
        testRealm.commitTransaction();
        assertNull(testRealm.allObjects(NullTypes.class).first().getFieldFloatNull());

        // 9 Double
        testRealm.beginTransaction();
        list.first().setFieldDoubleNull(null);
        testRealm.commitTransaction();
        assertNull(testRealm.allObjects(NullTypes.class).first().getFieldDoubleNull());

        // 10 Date
        testRealm.beginTransaction();
        list.first().setFieldDateNull(null);
        testRealm.commitTransaction();
        assertNull(testRealm.allObjects(NullTypes.class).first().getFieldDateNull());
    }

    public void testUnsupportedMethods() {
        RealmResults<AllTypes> result = testRealm.where(AllTypes.class).findAll();

        try { //noinspection deprecation
            result.add(null);     fail();
        } catch (UnsupportedOperationException ignored) {
        }
        try {
            result.set(0, null);  fail();
        } catch (UnsupportedOperationException ignored) {
        }
    }


    // Test that all methods that require a write transaction (ie. any function that mutates Realm data)
    public void testMutableMethodsOutsideWriteTransactions() {
        RealmResults<AllTypes> result = testRealm.where(AllTypes.class).findAll();

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
}
