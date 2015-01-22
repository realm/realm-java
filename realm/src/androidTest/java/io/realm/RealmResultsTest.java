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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.realm.entities.AllTypes;
import io.realm.entities.Cat;
import io.realm.entities.NonLatinFieldNames;
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

    @Override
    protected void setUp() throws InterruptedException {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
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
            allTypes.setColumnDate(new Date((long) 1000*i));
            allTypes.setColumnDouble(3.1415 + i);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            NonLatinFieldNames nonLatinFieldNames = testRealm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
        }
        testRealm.commitTransaction();
    }

    private void populateTestRealm() {
        populateTestRealm(TEST_DATA_SIZE);
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

    public void testMaxValueIsMaxValue() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Number maximum = resultList.max(FIELD_LONG);
        assertEquals(TEST_DATA_SIZE - 1, maximum.intValue());
    }

    public void testSumGivesCorrectValue() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Number sum = resultList.sum(FIELD_LONG);
        // Sum of numbers 0 to M-1: (M-1)*M/2
        assertEquals((TEST_DATA_SIZE - 1) * TEST_DATA_SIZE / 2, sum.intValue());
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
    // TODO: Should we reenable this test?
    public void DISABLEDtestRemove() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        testRealm.beginTransaction();
        resultList.remove(0);
        testRealm.commitTransaction();

        assertEquals(TEST_DATA_SIZE - 1, resultList.size());

        AllTypes allTypes = resultList.get(0);
        assertEquals(1, allTypes.getColumnLong());
    }

    // TODO: Should we reenable this test?
    public void DISABLEDtestRemoveLast() {
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

    public void testSortByLong() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = testRealm.allObjects(AllTypes.class);
        sortedList.sort(FIELD_LONG, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("First excepted to be last", resultList.first().getColumnLong(), sortedList.last().getColumnLong());

        RealmResults<AllTypes> reverseList = sortedList;
        reverseList.sort(FIELD_LONG, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals("First excepted to be first", resultList.first().getColumnLong(), reverseList.first().getColumnLong());
        assertEquals("Last excepted to be last", resultList.last().getColumnLong(), reverseList.last().getColumnLong());

        RealmResults<AllTypes> reserveSortedList = reverseList;
        reverseList.sort(FIELD_LONG, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
    }

    public void testSortByDate() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(FIELD_DATE, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(resultList.first().getColumnDate(), sortedList.last().getColumnDate());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(FIELD_DATE, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(resultList.first().getColumnDate(), reverseList.first().getColumnDate());
        assertEquals(resultList.last().getColumnDate(), reverseList.last().getColumnDate());

        RealmResults<AllTypes> reserveSortedList = reverseList.where().findAll();
        reserveSortedList.sort(FIELD_DATE, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
    }

    public void testSortByBoolean() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(FIELD_BOOLEAN, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(false, sortedList.last().isColumnBoolean());
        assertEquals(true, sortedList.first().isColumnBoolean());
        assertEquals(true, sortedList.get(TEST_DATA_FIRST_HALF).isColumnBoolean());
        assertEquals(false, sortedList.get(TEST_DATA_LAST_HALF).isColumnBoolean());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(FIELD_BOOLEAN, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(true, reverseList.last().isColumnBoolean());
        assertEquals(false, reverseList.first().isColumnBoolean());
        assertEquals(false, reverseList.get(TEST_DATA_FIRST_HALF).isColumnBoolean());
        assertEquals(true, reverseList.get(TEST_DATA_LAST_HALF).isColumnBoolean());

        RealmResults<AllTypes> reserveSortedList = reverseList.where().findAll();
        reserveSortedList.sort(FIELD_BOOLEAN, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
        assertEquals(reserveSortedList.first(), sortedList.first());
    }

    public void testSortByString() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(FIELD_STRING, RealmResults.SORT_ORDER_DESCENDING);

        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(resultList.first().getColumnString(), sortedList.last().getColumnString());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(FIELD_STRING, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(resultList.first().getColumnString(), reverseList.first().getColumnString());

        int numberOfDigits = 1 + ((int) Math.log10(TEST_DATA_SIZE));
        int largestNumber = 1;
        for (int i = 1; i < numberOfDigits; i++)
            largestNumber *= 10;  // 10*10* ... *10
        largestNumber = largestNumber - 1;
        assertEquals(resultList.get(largestNumber).getColumnString(), reverseList.last().getColumnString());
        RealmResults<AllTypes> reverseSortedList = reverseList.where().findAll();
        reverseList.sort(FIELD_STRING, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseSortedList.size());
    }

    public void testSortByDouble() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(FIELD_DOUBLE, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(resultList.first().getColumnDouble(), sortedList.last().getColumnDouble());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(FIELD_DOUBLE, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(resultList.first().getColumnDouble(), reverseList.first().getColumnDouble());
        assertEquals(resultList.last().getColumnDouble(), reverseList.last().getColumnDouble());

        RealmResults<AllTypes> reverseSortedList = reverseList.where().findAll();
        reverseSortedList.sort(FIELD_DOUBLE, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseSortedList.size());
    }

    public void testSortByFloat() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.where().findAll();
        sortedList.sort(FIELD_FLOAT, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals(resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(resultList.first().getColumnFloat(), sortedList.last().getColumnFloat());

        RealmResults<AllTypes> reverseList = sortedList.where().findAll();
        reverseList.sort(FIELD_FLOAT, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(resultList.first().getColumnFloat(), reverseList.first().getColumnFloat());
        assertEquals(resultList.last().getColumnFloat(), reverseList.last().getColumnFloat());

        RealmResults<AllTypes> reverseSortedList = reverseList.where().findAll();
        reverseSortedList.sort(FIELD_FLOAT, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseSortedList.size());
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
        reverseResult.sort(FIELD_STRING, RealmResults.SORT_ORDER_DESCENDING);
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
        reverseResult.sort(FIELD_STRING, RealmResults.SORT_ORDER_DESCENDING);
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
        reverseResult.sort(FIELD_STRING, RealmResults.SORT_ORDER_DESCENDING);
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

        RealmResults<AllTypes> reverseResult = result;
        reverseResult.sort(FIELD_STRING, RealmResults.SORT_ORDER_DESCENDING);
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
            result.sort((String)null);
            fail("Sorting with a null field name should throw an IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            result.sort((String[])null, (boolean[])null);
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
        sortedList.sort(new String[]{FIELD_LONG}, new boolean[]{RealmResults.SORT_ORDER_DESCENDING});
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
        RealmResults<AllTypes> allTypes = testRealm.where(AllTypes.class).findAll(FIELD_LONG, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, allTypes.size());
        assertEquals(0, allTypes.first().getColumnLong());
        assertEquals(TEST_DATA_SIZE - 1, allTypes.last().getColumnLong());

        RealmResults<AllTypes> reverseList = testRealm.where(AllTypes.class).findAll(FIELD_LONG, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals(0, reverseList.last().getColumnLong());
        assertEquals(TEST_DATA_SIZE - 1, reverseList.first().getColumnLong());

        try {
            RealmResults<AllTypes> none = testRealm.where(AllTypes.class).findAll("invalid", RealmResults.SORT_ORDER_DESCENDING);
            fail();
        } catch (IllegalArgumentException ignored) {}
    }

    public void testQueryDateField() {
        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).equalTo(FIELD_DATE, new Date(5000));
        RealmResults<AllTypes> all = query.findAll();
        assertEquals(1, query.count());
        assertEquals(1, all.size());
    }

    public void testIndexOf() {
        try {
            RealmResults<AllTypes> all = testRealm.allObjects(AllTypes.class);
            int index = all.indexOf(all.first());
            fail();
        } catch (NoSuchMethodError e) {}
    }
    // TODO: More extended tests of querying all types must be done.
}
