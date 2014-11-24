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
import io.realm.entities.NonLatinFieldNames;

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
    private final static String FIELD_BYTE = "columnBinary";
    private final static String FIELD_DOG = "columnRealmObject";

    @Override
    protected void setUp() throws InterruptedException {
        boolean result = Realm.deleteRealmFile(getContext());
        assertTrue(result);

        testRealm = Realm.getInstance(getContext());

        testRealm.beginTransaction();
        testRealm.allObjects(AllTypes.class).clear();
        testRealm.allObjects(NonLatinFieldNames.class).clear();

        for (int i = 0; i < TEST_DATA_SIZE; ++i) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 2) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date((long) 1000 * i));
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

    private enum Method {
        METHOD_MIN,
        METHOD_MAX,
        METHOD_SUM,
        METHOD_AVG,
        METHOD_SORT,
        METHOD_WHERE
    }

    ;

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
        testRealm.beginTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals("ResultList.where test setup did not produce required test data", TEST_DATA_SIZE, resultList.size());

        resultList.clear();
        assertEquals("ResultList.clear did not remove records", 0, resultList.size());

        testRealm.commitTransaction();
    }

    /*public void testRemoveLastShouldFail() {
        RealmResults<AllTypes> resultsList = testRealm.where(AllTypes.class).equalTo(FIELD_STRING, "Not there").findAll();
        try {
            testRealm.beginTransaction();
            resultsList.removeLast();
            fail("Should give exception");
        } catch (IllegalArgumentException e) {

        } finally {
            testRealm.commitTransaction();
        }
    }*/

    public void testResultListGet() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.get(0);
        assertNotNull(allTypes);
        assertTrue(allTypes.getColumnString().startsWith("test data"));
    }


    // void clear(Class<?> classSpec)
    public void testIsResultListSizeOk() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertNotNull(resultList);
        assertEquals(TEST_DATA_SIZE, resultList.size());
    }


    public void testResultListFirstIsFirst() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.first();
        assertNotNull(allTypes);
        assertTrue(allTypes.getColumnString().startsWith("test data 0"));
    }

    public void testResultListLastIsLast() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.last();
        assertNotNull(allTypes);
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

    // void clear(Class<?> classSpec)
    public void testRemoveIsResultListSizeOk() {
        testRealm.beginTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        resultList.remove(0);

        testRealm.commitTransaction();

        boolean checkListSize = resultList.size() == TEST_DATA_SIZE - 1;
        assertTrue(checkListSize);

        AllTypes allTypes = resultList.get(0);
        assertTrue(allTypes.getColumnLong() == 1);
    }

    public void testIsResultRemoveLastListSizeOk() {
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
        RealmResults<AllTypes> sortedList = resultList.sort(FIELD_LONG, RealmResults.SORT_ORDER_DECENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("First excepted to be last", resultList.first().getColumnLong(), sortedList.last().getColumnLong());

        RealmResults<AllTypes> reverseList = sortedList.sort(FIELD_LONG, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals("First excepted to be first", resultList.first().getColumnLong(), reverseList.first().getColumnLong());
        assertEquals("Last excepted to be last", resultList.last().getColumnLong(), reverseList.last().getColumnLong());

        RealmResults<AllTypes> reserveSortedList = reverseList.sort(FIELD_LONG, RealmResults.SORT_ORDER_DECENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
    }

    public void testSortByDate() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.sort(FIELD_DATE, RealmResults.SORT_ORDER_DECENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("First excepted to be last", resultList.first().getColumnDate(), sortedList.last().getColumnDate());

        RealmResults<AllTypes> reverseList = sortedList.sort(FIELD_DATE, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals("First excepted to be first", resultList.first().getColumnDate(), reverseList.first().getColumnDate());
        assertEquals("Last excepted to be last", resultList.last().getColumnDate(), reverseList.last().getColumnDate());

        RealmResults<AllTypes> reserveSortedList = reverseList.sort(FIELD_DATE, RealmResults.SORT_ORDER_DECENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
    }

    public void testSortByBoolean() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.sort(FIELD_BOOLEAN, RealmResults.SORT_ORDER_DECENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("Last expected to be false", false, sortedList.last().isColumnBoolean());
        assertEquals("First expected to be true", true, sortedList.first().isColumnBoolean());
        assertEquals("Expected to be true", true, sortedList.get(TEST_DATA_FIRST_HALF).isColumnBoolean());
        assertEquals("Expected to be false", false, sortedList.get(TEST_DATA_LAST_HALF).isColumnBoolean());

        RealmResults<AllTypes> reverseList = sortedList.sort(FIELD_BOOLEAN, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals("Last expected to be true", true, reverseList.last().isColumnBoolean());
        assertEquals("First expected to be false", false, reverseList.first().isColumnBoolean());
        assertEquals("Expected to be false", false, reverseList.get(TEST_DATA_FIRST_HALF).isColumnBoolean());
        assertEquals("Expected to be true", true, reverseList.get(TEST_DATA_LAST_HALF).isColumnBoolean());

        RealmResults<AllTypes> reserveSortedList = reverseList.sort(FIELD_BOOLEAN, RealmResults.SORT_ORDER_DECENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
        assertEquals(reserveSortedList.first(), sortedList.first());
    }

    public void testSortByString() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.sort(FIELD_STRING, RealmResults.SORT_ORDER_DECENDING);

        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("First excepted to be last", resultList.first().getColumnString(), sortedList.last().getColumnString());

        RealmResults<AllTypes> reverseList = sortedList.sort(FIELD_STRING, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals("First excepted to be first", resultList.first().getColumnString(), reverseList.first().getColumnString());

        int numberOfDigits = 1 + ((int) Math.log10(TEST_DATA_SIZE));
        int largestNumber = 1;
        for (int i = 1; i < numberOfDigits; i++)
            largestNumber *= 10;  // 10*10* ... *10
        largestNumber = largestNumber - 1;
        assertEquals("Last excepted to be last", resultList.get(largestNumber).getColumnString(), reverseList.last().getColumnString());
        RealmResults<AllTypes> reserveSortedList = reverseList.sort(FIELD_STRING, RealmResults.SORT_ORDER_DECENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
    }

    public void testSortByDouble() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.sort(FIELD_DOUBLE, RealmResults.SORT_ORDER_DECENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("First excepted to be last", resultList.first().getColumnDouble(), sortedList.last().getColumnDouble());

        RealmResults<AllTypes> reverseList = sortedList.sort(FIELD_DOUBLE, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals("First excepted to be first", resultList.first().getColumnDouble(), reverseList.first().getColumnDouble());
        assertEquals("Last excepted to be last", resultList.last().getColumnDouble(), reverseList.last().getColumnDouble());

        RealmResults<AllTypes> reserveSortedList = reverseList.sort(FIELD_DOUBLE, RealmResults.SORT_ORDER_DECENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
    }

    public void testSortByFloat() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.sort(FIELD_FLOAT, RealmResults.SORT_ORDER_DECENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("First excepted to be last", resultList.first().getColumnFloat(), sortedList.last().getColumnFloat());

        RealmResults<AllTypes> reverseList = sortedList.sort(FIELD_FLOAT, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals("First excepted to be first", resultList.first().getColumnFloat(), reverseList.first().getColumnFloat());
        assertEquals("Last excepted to be last", resultList.last().getColumnFloat(), reverseList.last().getColumnFloat());

        RealmResults<AllTypes> reserveSortedList = reverseList.sort(FIELD_FLOAT, RealmResults.SORT_ORDER_DECENDING);
        assertEquals(TEST_DATA_SIZE, reserveSortedList.size());
    }

    public void testSortOnNonExistingColumn() {
        try {
            RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
            RealmResults<AllTypes> sortedList = resultList.sort("Non-existing");
            fail("Column should not exist");
        } catch (ArrayIndexOutOfBoundsException e) {
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
        RealmResults<AllTypes> sortedResult = result.sort(FIELD_STRING);

        assertEquals(3, sortedResult.size());
        assertEquals("Æble", sortedResult.first().getColumnString());
        assertEquals("Æble", sortedResult.get(0).getColumnString());
        assertEquals("Øl", sortedResult.get(1).getColumnString());
        assertEquals("Århus", sortedResult.get(2).getColumnString());

        RealmResults<AllTypes> reverseResult = result.sort(FIELD_STRING, RealmResults.SORT_ORDER_DECENDING);
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
        RealmResults<AllTypes> sortedResult = result.sort(FIELD_STRING);

        assertEquals(3, sortedResult.size());
        assertEquals("Москва", sortedResult.first().getColumnString());
        assertEquals("Москва", sortedResult.get(0).getColumnString());
        assertEquals("Новороссийск", sortedResult.get(1).getColumnString());
        assertEquals("Санкт-Петербург", sortedResult.get(2).getColumnString());

        RealmResults<AllTypes> reverseResult = result.sort(FIELD_STRING, RealmResults.SORT_ORDER_DECENDING);
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
        RealmResults<AllTypes> sortedResult = result.sort(FIELD_STRING);

        assertEquals(3, sortedResult.size());
        assertEquals("αύριο", sortedResult.first().getColumnString());
        assertEquals("αύριο", sortedResult.get(0).getColumnString());
        assertEquals("δοκιμές", sortedResult.get(1).getColumnString());
        assertEquals("ημέρες", sortedResult.get(2).getColumnString());

        RealmResults<AllTypes> reverseResult = result.sort(FIELD_STRING, RealmResults.SORT_ORDER_DECENDING);
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
        RealmResults<AllTypes> sortedResult = result.sort(FIELD_STRING);

        assertEquals(8, sortedResult.size());

        RealmResults<AllTypes> reverseResult = result.sort(FIELD_STRING, RealmResults.SORT_ORDER_DECENDING);
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
        testRealm.commitTransaction();
        allTypes3.setColumnString("work");
        try {
            RealmResults<AllTypes> result = testRealm.allObjects(AllTypes.class);
            RealmResults<AllTypes> sortedResult = result.sort(FIELD_STRING);
        } catch (IllegalArgumentException e) {
            fail("Failed to sort with two kinds of alphabets");
        }
    }

    public void testWithEmptyRealmObjects() {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        testRealm.commitTransaction();
        try {
            RealmResults<AllTypes> sortResult = testRealm.where(AllTypes.class).findAll().sort(FIELD_STRING);
        } catch (IllegalArgumentException e) {
            fail("Failed to sort an empty RealmResults");
        }
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
}
