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

import java.io.IOException;

import io.realm.entities.AllTypes;

public class ResultListTest extends RealmSetupTests {

    // test io.realm.ResultList Api

    //void clear(Class<?> classSpec)
    public void testClearEmptiesTable() throws IOException {

        testRealm.beginWrite();

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals("ResultList.clear test setup did not produce required test data", TEST_DATA_SIZE, resultList.size());

        resultList.clear();
        assertEquals("ResultList.clear did not remove records", 0, resultList.size());

        testRealm.commit();
    }

    public void testResultListGet() {

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.get(0);
        assertNotNull("ResultList.get has returned null", allTypes);
        assertTrue("ResultList.get returned invalid data", allTypes.getColumnString().startsWith("test data"));
    }


    //void clear(Class<?> classSpec)
    public void testIsResultListSizeOk() throws IOException {
        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertNotNull("ResultList.where has returned null", resultList);
        assertEquals("ResultList.where unexpected number of objects returned", TEST_DATA_SIZE, resultList.size());
    }


    public void testResultListFirstIsFirst() throws IOException {

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.first();
        assertNotNull("ResultList.first has returned null", allTypes);
        assertTrue("ResultList.first returned invalid data", allTypes.getColumnString().startsWith("test data 0"));
    }

    public void testResultListLastIsLast() throws IOException {

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        AllTypes allTypes = resultList.last();
        assertNotNull("ResultList.last has returned null", allTypes);
        assertEquals("ResultList.last returned invalid data", (TEST_DATA_SIZE - 1), allTypes.getColumnLong());
    }

    public void testMinValueIsMinValue() throws IOException {

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Number minimum = resultList.min("columnlong");
        assertEquals("ResultList.min returned wrong value", 0, minimum.intValue());
    }

    public void testMaxValueIsMaxValue() throws IOException {

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Number maximum = resultList.max("columnlong");
        assertEquals("ResultList.max returned wrong value", TEST_DATA_SIZE -1, maximum.intValue());
    }

    public void testSumGivesCorrectValue() throws IOException {

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Number sum = resultList.sum("columnlong");

        int checkSum = 0;
        for (int i = 0; i < TEST_DATA_SIZE; ++i) {
            checkSum += i;
        }
        assertEquals("ResultList.sum returned wrong sum", checkSum, sum.intValue());
    }

    public void testAvgGivesCorrectValue() throws IOException {

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        Double avg = resultList.average("columndouble");

        Double checkAvg = 0.0;
        for (int i = 0; i < TEST_DATA_SIZE; ++i) {
            checkAvg += 3.1415 + i;
        }
        checkAvg /= TEST_DATA_SIZE;

        assertEquals("ResultList.sum returned wrong sum", checkAvg ,avg);
    }


    //void clear(Class<?> classSpec)
    public void testRemoveIsResultListSizeOk() throws IOException {

        testRealm.beginWrite();

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        resultList.remove(0);


        testRealm.commit();

        boolean checkListSize = resultList.size() == TEST_DATA_SIZE - 1;
        assertTrue("ResultList.remove did not remove record", checkListSize);

        AllTypes allTypes = resultList.get(0);
        assertTrue("ResultList.remove unexpected first record", allTypes.getColumnLong() == 1);
    }

    public void testIsResultRemoveLastListSizeOk() throws IOException {

        testRealm.beginWrite();

        ResultList<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        resultList.removeLast();

        testRealm.commit();

        assertEquals("ResultList.removeLast did not remove record", TEST_DATA_SIZE - 1, resultList.size());

        AllTypes allTypes = resultList.get(resultList.size() - 1);
        assertEquals("ResultList.removeLast unexpected last record", TEST_DATA_SIZE - 2, allTypes.getColumnLong());
    }

}