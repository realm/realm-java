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

import java.io.IOException;
import java.util.Date;

import io.realm.entities.AllTypes;

public class RealmResultsTest extends AndroidTestCase {


    protected final static int TEST_DATA_SIZE = 516;

    protected Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());

        testRealm.beginTransaction();

        testRealm.allObjects(AllTypes.class).clear();

        for (int i = 0; i < TEST_DATA_SIZE; ++i) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date());
            allTypes.setColumnDouble(3.1415);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
        }
        testRealm.commitTransaction();
    }


    // test io.realm.ResultList Api

    // void clear(Class<?> classSpec)
    public void testClearEmptiesTable() throws IOException {
        testRealm.beginTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals("ResultList.where test setup did not produce required test data", TEST_DATA_SIZE, resultList.size());

        resultList.clear();
        assertEquals("ResultList.clear did not remove records", 0, resultList.size());

        testRealm.commitTransaction();
    }

    public void testResultListGet() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.get(0);
        assertNotNull("ResultList.get has returned null", allTypes);
        assertTrue("ResultList.get returned invalid data", allTypes.getColumnString().startsWith("test data"));
    }


    // void clear(Class<?> classSpec)
    public void testIsResultListSizeOk() throws IOException {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertNotNull("ResultList.where has returned null", resultList);
        assertEquals("ResultList.where unexpected number of objects returned", TEST_DATA_SIZE, resultList.size());
    }


    public void testResultListFirstIsFirst() throws IOException {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.first();
        assertNotNull("ResultList.first has returned null", allTypes);
        assertTrue("ResultList.first returned invalid data", allTypes.getColumnString().startsWith("test data 0"));
    }

    public void testResultListLastIsLast() throws IOException {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.last();
        assertNotNull("ResultList.last has returned null", allTypes);
        assertEquals("ResultList.last returned invalid data", (TEST_DATA_SIZE - 1), allTypes.getColumnLong());
    }

    public void testMinValueIsMinValue() throws IOException {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Number minimum = resultList.min("columnLong");
        assertEquals("ResultList.min returned wrong value", 0, minimum.intValue());
    }

    public void testMaxValueIsMaxValue() throws IOException {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Number maximum = resultList.max("columnLong");
        assertEquals("ResultList.max returned wrong value", TEST_DATA_SIZE -1, maximum.intValue());
    }

    public void testSumGivesCorrectValue() throws IOException {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Number sum = resultList.sum("columnLong");

        int checkSum = 0;
        for (int i = 0; i < TEST_DATA_SIZE; ++i) {
            checkSum += i;
        }
        assertEquals("ResultList.sum returned wrong sum", checkSum, sum.intValue());
    }

    public void testAvgGivesCorrectValue() throws IOException {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Double avg = Math.round(resultList.average("columnDouble")*10000.0)/10000.0;

        assertEquals("ResultList.sum returned wrong sum", 3.1415 ,avg);
    }


    // void clear(Class<?> classSpec)
    public void testRemoveIsResultListSizeOk() throws IOException {
        testRealm.beginTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        resultList.remove(0);

        testRealm.commitTransaction();

        boolean checkListSize = resultList.size() == TEST_DATA_SIZE - 1;
        assertTrue("ResultList.remove did not remove record", checkListSize);

        AllTypes allTypes = resultList.get(0);
        assertTrue("ResultList.remove unexpected first record", allTypes.getColumnLong() == 1);
    }

    public void testIsResultRemoveLastListSizeOk() throws IOException {
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

    public void testSort() throws IOException {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmResults<AllTypes> sortedList = resultList.sort("columnLong", RealmResults.SORT_ORDER_DECENDING);
        assertEquals("Should have same size", resultList.size(), sortedList.size());
        assertEquals("First excepted to be last", resultList.first().getColumnString(), sortedList.last().getColumnString());
    }

    public void testCount() throws IOException {
        assertEquals(TEST_DATA_SIZE, testRealm.where(AllTypes.class).count());
    }
}
