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

import android.content.Context;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.internal.Table;


public class RealmTest extends AndroidTestCase {

    protected final static int TEST_DATA_SIZE = 159;

    protected Realm testRealm;

    protected List<String> columnData = new ArrayList<String>();

    private boolean CASE_INSENSITIVE = false;

    protected void setColumnData() {
        columnData.add(0, "columnBoolean");
        columnData.add(1, "columnDate");
        columnData.add(2, "columnDouble");
        columnData.add(3, "columnFloat");
        columnData.add(4, "columnString");
        columnData.add(5, "columnLong");
    }

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());

        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
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


    private final static int BACKGROUND_COMMIT_TEST_DATA_SET_SIZE = 5;


    public void testRealmCache() {
        assertEquals(testRealm, Realm.getInstance(getContext()));
    }

    public void testShouldCreateRealm() {
        Realm realm = Realm.getInstance(getContext());
        assertNotNull("Realm.getInstance unexpectedly returns null", realm);
        assertTrue("Realm.getInstance does not contain expected table", realm.contains(AllTypes.class));
    }

    public void testShouldNotFailCreateRealmWithNullContext() {
        Context c = null;  // throws when c.getDirectory() is called; has nothing to do with Realm

        try {
            Realm realm = Realm.getInstance(c);
            fail("Should throw an exception");
        } catch (NullPointerException e) {
        }

    }

    // Table getTable(Class<?> clazz)
    public void testShouldGetTable() {
        Table table = testRealm.getTable(AllTypes.class);
        assertNotNull("getTable is returning a null Table object", table);
        assertEquals("Unexpected query result after getTable", TEST_DATA_SIZE, table.count(table.getColumnIndex("columnDouble"), 3.1415));
    }

    // <E> void remove(Class<E> clazz, long objectIndex)
    public void testShouldRemoveRow() {

        testRealm.beginTransaction();
        testRealm.remove(AllTypes.class, 0);
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals("Realm.delete has not deleted record correctly", TEST_DATA_SIZE - 1, resultList.size());

    }

    // <E extends RealmObject> E get(Class<E> clazz, long rowIndex)
    public void testShouldGetObject() {

        AllTypes allTypes = testRealm.get(AllTypes.class, 0);
        assertNotNull("get has returned null object", allTypes);
        assertEquals("get has returned wrong object", "test data 0", allTypes.getColumnString());
    }

    // boolean contains(Class<?> clazz)
    public void testShouldContainTable() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        testRealm.commitTransaction();
        assertTrue("contains returns false for newly created table", testRealm.contains(Dog.class));
    }

    // boolean contains(Class<?> clazz)
    public void testShouldNotContainTable() {

        assertFalse("contains returns true for non-existing table", testRealm.contains(RealmTest.class));
    }

    // <E extends RealmObject> RealmQuery<E> where(Class<E> clazz)
    public void testShouldReturnResultSet() {

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();

        assertEquals("Realm.get is returning wrong number of objects", TEST_DATA_SIZE, resultList.size());
    }

    // Note that this test is relying on the values set while initializing the test dataset
    public void testQueriesResults() throws IOException {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).equalTo("columnLong", 33).findAll();
        assertEquals("ResultList.where not returning expected result", 1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo("columnLong", 3333).findAll();
        assertEquals("ResultList.where not returning expected result", 0, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo("columnString", "test data 0").findAll();
        assertEquals(1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo("columnString", "test data 0", RealmQuery.CASE_INSENSITIVE).findAll();
        assertEquals(1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo("columnString", "Test data 0", RealmQuery.CASE_SENSITIVE).findAll();
        assertEquals(0, resultList.size());
    }

    public void testQueriesWithDataTypes() throws IOException {
        RealmResults<AllTypes> resultList = null;
        setColumnData();

        for (int i = 0; i < columnData.size(); i++) {
            try {
                resultList = testRealm.where(AllTypes.class).equalTo(columnData.get(i), true).findAll();
                if (i != 0) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException e) {
            }

            try {
                resultList = testRealm.where(AllTypes.class).equalTo(columnData.get(i), new Date()).findAll();
                if (i != 1) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException e) {
            }

            try {
                resultList = testRealm.where(AllTypes.class).equalTo(columnData.get(i), 13.37d).findAll();
                if (i != 2) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException e) {
            }

            try {
                resultList = testRealm.where(AllTypes.class).equalTo(columnData.get(i), 13.3711f).findAll();
                if (i != 3) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException e) {
            }

            try {
                resultList = testRealm.where(AllTypes.class).equalTo(columnData.get(i), "test").findAll();
                if (i != 4) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException e) {
            }

            try {
                resultList = testRealm.where(AllTypes.class).equalTo(columnData.get(i), 1337).findAll();
                if (i != 5) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public void testQueriesFailWithInvalidDataTypes() throws IOException {
        RealmResults<AllTypes> resultList = null;

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("invalidcolumnname", 33).findAll();
            fail("Invalid field name");
        } catch (Exception e) {
        }

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("invalidcolumnname", "test").findAll();
            fail("Invalid field name");
        } catch (Exception e) {
        }

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("invalidcolumnname", true).findAll();
            fail("Invalid field name");
        } catch (Exception e) {
        }

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("invalidcolumnname", 3.1415d).findAll();
            fail("Invalid field name");
        } catch (Exception e) {
        }

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("invalidcolumnname", 3.1415f).findAll();
            fail("Invalid field name");
        } catch (Exception e) {
        }
    }

    public void testQueriesFailWithNullQueryValue() throws IOException {
        RealmResults<AllTypes> resultList = null;

        String nullString = null;
        Float nullFloat = null;
        Long nullLong = null;
        Boolean nullBoolean = null;

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnString", nullString).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnLong", nullLong).findAll();
            fail("Realm.where should fail with illegal argument");

        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e) {
        }

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnBoolean", nullBoolean).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e) {
        }

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnFloat", nullFloat).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e) {
        }
    }

    // <E extends RealmObject> RealmTableOrViewList<E> allObjects(Class<E> clazz)
    public void testShouldReturnTableOrViewList() {
        RealmResults<AllTypes> resultList = testRealm.allObjects(AllTypes.class);
        assertEquals("Realm.get is returning wrong result set", TEST_DATA_SIZE, resultList.size());
    }

    // void beginTransaction()
    public void testBeginTransaction() throws IOException {

        testRealm.beginTransaction();

        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnFloat(3.1415f);
        allTypes.setColumnString("a unique string");
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals("Change has not been committed", TEST_DATA_SIZE + 1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo("columnString", "a unique string").findAll();
        assertEquals("Change has not been committed correctly", 1, resultList.size());
        resultList = testRealm.where(AllTypes.class).equalTo("columnFloat", 3.1415f).findAll();
        assertEquals("Change has not been committed", 1, resultList.size());
    }

    public void testNestedTransaction() {
        testRealm.beginTransaction();
        try {
            testRealm.beginTransaction();
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Nested transactions are not allowed. Use commitTransaction() after each beginTransaction().", e.getMessage());
        }
        testRealm.commitTransaction();
    }

    private enum TransactionMethod {
        METHOD_BEGIN,
        METHOD_COMMIT,
        METHOD_CANCEL
    }

    // Starting a transaction on the wrong thread will fail
    public boolean transactionMethodWrongThread(final TransactionMethod method) throws InterruptedException, ExecutionException {
        final Realm realm = Realm.getInstance(getContext());

        if (method != TransactionMethod.METHOD_BEGIN) {
            realm.beginTransaction();
            Dog dog = realm.createObject(Dog.class); // FIXME: Empty transactions cannot be cancelled
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    switch (method) {
                        case METHOD_BEGIN:
                            realm.beginTransaction();
                            break;
                        case METHOD_COMMIT:
                            realm.commitTransaction();
                            break;
                        case METHOD_CANCEL:
                            realm.cancelTransaction();
                            break;
                    }
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                }
            }
        });

        boolean result = future.get();
        if (result && method != TransactionMethod.METHOD_BEGIN) {
            realm.cancelTransaction();
        }
        return result;
    }

    public void testTransactionWrongThread() throws ExecutionException, InterruptedException {
        for (TransactionMethod method : TransactionMethod.values()) {
            assertTrue(method.toString(), transactionMethodWrongThread(method));
        }
    }

    // void commitTransaction()
    public void testCommitTransaction() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnBoolean(true);
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals("Change has not been committed", TEST_DATA_SIZE + 1, resultList.size());
    }


    public void testCancelTransaction() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        testRealm.cancelTransaction();
        assertEquals(TEST_DATA_SIZE, testRealm.allObjects(AllTypes.class).size());

        try {
            testRealm.cancelTransaction();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    // void clear(Class<?> classSpec)
    public void testClassClear() {

        // Currently clear will not work outside a transaction:

        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals("Realm.clear does not empty table", 0, resultList.size());
    }

    // void clear(Class<?> classSpec)
    public void testClassClearWithTwoTables() {
        testRealm.beginTransaction();

        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("Castro");

        testRealm.commitTransaction();

        // NOTE:
        // Currently clear will not work outside a transaction
        // if you want this test not to fail, add begin- and commitTransaction

        testRealm.beginTransaction();
        testRealm.clear(Dog.class);
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultListTypes = testRealm.where(AllTypes.class).findAll();
        RealmResults<Dog> resultListDogs = testRealm.where(Dog.class).findAll();

        assertEquals("Realm.clear does not clear table", 0, resultListDogs.size());
        assertEquals("Realm.clear cleared wrong table", TEST_DATA_SIZE, resultListTypes.size());


        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        testRealm.commitTransaction();

        resultListTypes = testRealm.where(AllTypes.class).findAll();
        assertEquals("Realm.clear does not remove table", 0, resultListTypes.size());
    }

    // int getVersion()
    public void testGetVersion() throws IOException {

        long version = testRealm.getVersion();

        assertTrue("Realm.version returns invalid version number", version >= 0);
    }

    // void setVersion(int version)setVersion(int version)
    public void testSetVersion() {
        long version = 42;

        testRealm.beginTransaction();
        testRealm.setVersion(version);
        testRealm.commitTransaction();

        assertEquals("Realm.version has not been set by setVersion", version, testRealm.getVersion());
    }

    public void testShouldFailOutsideTransaction() {

        // These API calls should fail outside a Transaction:
        try {
            AllTypes aT = testRealm.createObject(AllTypes.class);
            fail("Realm.createObject should fail outside write transaction");
        } catch (IllegalStateException e) {
        }
        try {
            testRealm.remove(AllTypes.class, 0);
            fail("Realm.remove should fail outside write transaction");
        } catch (IllegalStateException e) {
        }
    }

    public void testRealmQueryBetween() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).between("columnLong", 0, 9).findAll();
        assertEquals(10, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith("columnString", "test data ").findAll();
        assertEquals(TEST_DATA_SIZE, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith("columnString", "test data 1").between("columnLong", 2, 20).findAll();
        assertEquals(10, resultList.size());

        resultList = testRealm.where(AllTypes.class).between("columnLong", 2, 20).beginsWith("columnString", "test data 1").findAll();
        assertEquals(10, resultList.size());
    }


    public void testRealmQueryGreaterThan() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).greaterThan("columnFloat", 10.234567f).findAll();
        assertEquals(TEST_DATA_SIZE - 10, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith("columnString", "test data 1").greaterThan("columnFloat", 50.234567f).findAll();
        assertEquals(TEST_DATA_SIZE - 100, resultList.size());

        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).greaterThan("columnFloat", 11.234567f);
        resultList = query.between("columnLong", 1, 20).findAll();
        assertEquals(10, resultList.size());
    }


    public void testRealmQueryGreaterThanOrEqualTo() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).greaterThanOrEqualTo("columnFloat", 10.234567f).findAll();
        assertEquals(TEST_DATA_SIZE - 9, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith("columnString", "test data 1").greaterThanOrEqualTo("columnFloat", 50.234567f).findAll();
        assertEquals(TEST_DATA_SIZE - 100, resultList.size());

        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).greaterThanOrEqualTo("columnFloat", 11.234567f);
        query = query.between("columnLong", 1, 20);

        resultList = query.beginsWith("columnString", "test data 15").findAll();
        assertEquals(1, resultList.size());
    }

    public void testRealmQueryOr() {
        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).equalTo("columnFloat", 31.234567f);
        RealmResults<AllTypes> resultList = query.or().between("columnLong", 1, 20).findAll();
        assertEquals(21, resultList.size());

        resultList = query.or().equalTo("columnString", "test data 15").findAll();
        assertEquals(21, resultList.size());

        resultList = query.or().equalTo("columnString", "test data 117").findAll();
        assertEquals(22, resultList.size());
    }

    public void testRealmQueryImplicitAnd() {
        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).equalTo("columnFloat", 31.234567f);
        RealmResults<AllTypes> resultList = query.between("columnLong", 1, 10).findAll();
        assertEquals(0, resultList.size());

        query = testRealm.where(AllTypes.class).equalTo("columnFloat", 81.234567f);
        resultList = query.between("columnLong", 1, 100).findAll();
        assertEquals(1, resultList.size());
    }

    public void testRealmQueryLessThan() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).lessThan("columnFloat", 31.234567f).findAll();
        assertEquals(30, resultList.size());
        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).lessThan("columnFloat", 31.234567f);
        resultList = query.between("columnLong", 1, 10).findAll();
        assertEquals(10, resultList.size());
    }

    public void testRealmQueryLessThanOrEqual() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).lessThanOrEqualTo("columnFloat", 31.234567f).findAll();
        assertEquals(31, resultList.size());
        resultList = testRealm.where(AllTypes.class).lessThanOrEqualTo("columnFloat", 31.234567f).between("columnLong", 11, 20).findAll();
        assertEquals(10, resultList.size());
    }

    public void testRealmQueryEqualTo() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).equalTo("columnFloat", 31.234567f).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThan("columnFloat", 11.0f).equalTo("columnLong", 10).findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThan("columnFloat", 11.0f).equalTo("columnLong", 1).findAll();
        assertEquals(0, resultList.size());
    }

    public void testRealmQueryNotEqualTo() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).notEqualTo("columnLong", 31).findAll();
        assertEquals(TEST_DATA_SIZE - 1, resultList.size());
        resultList = testRealm.where(AllTypes.class).notEqualTo("columnFloat", 11.234567f).equalTo("columnLong", 10).findAll();
        assertEquals(0, resultList.size());
        resultList = testRealm.where(AllTypes.class).notEqualTo("columnFloat", 11.234567f).equalTo("columnLong", 1).findAll();
        assertEquals(1, resultList.size());
    }

    public void testRealmQueryContainsAndCaseSensitive() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .beginGroup().contains("columnString", "DaTa 0", CASE_INSENSITIVE)
                .or().contains("columnString", "20").endGroup().findAll();
        assertEquals(3, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "DATA").findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "TEST",CASE_INSENSITIVE).findAll();
        assertEquals(TEST_DATA_SIZE, resultList.size());
    }

    public void testRealmQueryContainsAndCaseSensitiveWithNonLatinCharacters() {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes at1 = testRealm.createObject(AllTypes.class);
        at1.setColumnString("Αλφα");
        AllTypes at2 = testRealm.createObject(AllTypes.class);
        at2.setColumnString("βήτα");
        AllTypes at3 = testRealm.createObject(AllTypes.class);
        at3.setColumnString("δέλτα");
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class)
                .beginGroup().contains("columnString", "Α", CASE_INSENSITIVE)
                .or().contains("columnString", "δ").endGroup().findAll();
        // Without case sensitive there is 3, Α = α
        // assertEquals(3,resultList.size());
        assertEquals(2, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "α").findAll();
        assertEquals(3, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "Δ").findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(AllTypes.class).contains("columnString", "Δ", CASE_INSENSITIVE).findAll();
        // Without case sensitive there is 1, Δ = δ
        // assertEquals(1,resultList.size());
        assertEquals(0, resultList.size());
    }

    public void testQueryWithNonExistingField() {
        try {
            RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).equalTo("NotAField", 13).findAll();
            fail("Should throw exception");
        } catch (IllegalArgumentException e) {

        }
    }

    public void createAndTestFilename(String language, String fileName) {
        Realm.deleteRealmFile(getContext(), fileName);
        Realm realm1 = Realm.getInstance(getContext(), fileName);
        realm1.beginTransaction();
        Dog dog1 = realm1.createObject(Dog.class);
        dog1.setName("Rex");
        realm1.commitTransaction();

        File file = new File(getContext().getFilesDir() + "/" + fileName);
        assertTrue(language, file.exists());

        Realm realm2 = Realm.getInstance(getContext(), fileName);
        Dog dog2 = realm2.allObjects(Dog.class).first();
        assertEquals(language, "Rex", dog2.getName());
    }

    public void testCreateFile() {
        createAndTestFilename("American", "Washington");
        createAndTestFilename("Danish", "København");
        createAndTestFilename("Russian", "Москва");
        createAndTestFilename("Greek", "Αθήνα");
        createAndTestFilename("Chinese", "北京市");
        createAndTestFilename("Korean", "서울시");
        createAndTestFilename("Arabic", "الرياض");
        createAndTestFilename("India", "नई दिल्ली");
        createAndTestFilename("Japanese", "東京都");
    }
}
