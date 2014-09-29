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

import java.io.IOException;
import java.util.Date;

import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.internal.Table;


public class RealmTest extends AndroidTestCase {


    protected final static int TEST_DATA_SIZE = 159;

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


    private final static int BACKGROUND_COMMIT_TEST_DATA_SET_SIZE = 5;

    private void getNotifications(int totalExpected) {
        int lastCount = 0;
        int retries = 8;

        while (testCount < totalExpected && retries-- > 0) {
            try {
                Thread.sleep(125);
            } catch (Exception ex) {
                fail("Unexpected exception " + ex.getMessage());
            }
            if (lastCount != testCount) {
                lastCount = testCount;
                retries = 8;
            }
        }
    }


    public void testRealmThreadCachingSpeed() {
        long tic1 = System.currentTimeMillis();
        Realm realm1 = Realm.getInstance(this.getContext());
        long toc1 = System.currentTimeMillis();
        long t1 = toc1 - tic1;

        long tic2 = System.currentTimeMillis();
        Realm realm2 = Realm.getInstance(this.getContext());
        long toc2 = System.currentTimeMillis();
        long t2 = toc2 - tic2;

        // At least 5 times faster?
        assertTrue(t2 < (t1 / 5));
    }

    // Test io.realm.Realm API

    // Realm Constructors
    public void testShouldCreateRealm() {
        Realm realm = Realm.getInstance(getContext());
        assertNotNull("Realm.getInstance unexpectedly returns null", realm);
        assertTrue("Realm.getInstance does not contain expected table", realm.contains(AllTypes.class));
    }

    public void testShouldNotFailCreateRealmWithNullContext() {
        Context c = null;

        Realm realm = Realm.getInstance(c);
        assertNull("Realm has been created with null Context", realm);
    }

    // Table getTable(Class<?> clazz)
    public void testShouldGetTable() {
        Table table = testRealm.getTable(AllTypes.class);
        assertNotNull("getTable is returning a null Table object", table);
        assertEquals("Unexpected query result after getTable", TEST_DATA_SIZE, table.count(table.getColumnIndex("columndouble"), 3.1415));
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
        Dog allTypes = testRealm.createObject(Dog.class);
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
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).equalTo("columnlong", 33).findAll();
        assertEquals("ResultList.where not returning expected result", 1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo("columnlong", 3333).findAll();
        assertEquals("ResultList.where not returning expected result", 0, resultList.size());


    }

    // Note that this test is relying on the values set while initializing the test dataset
    public void testQueriesFailWithWrongDataTypes() throws IOException {
        RealmResults<AllTypes> resultList = null;

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnstring", 3333).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnstring", true).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnstring", 3.1415f).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnstring", 3.1415d).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnfloat", 12).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnfloat", true).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnfloat", "string").findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnfloat", 3.1415d).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnlong", true).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnlong", "string").findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnlong", 3.1415f).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnlong", 3.1415d).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnboolean", "test").findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnboolean", 7).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnboolean", 3.1415f).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnboolean", 3.1415d).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnbinary", "test").findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnbinary", 7).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnbinary", 3.1415f).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnbinary", 3.1415d).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testQueriesFailWithInvalidDataTypes() throws IOException {
        RealmResults<AllTypes> resultList = null;

        resultList = testRealm.where(AllTypes.class).equalTo("invalidcolumnname", 33).findAll();
        assertNotNull("ResultList.where.equalTo returns null when column name is non-existing", resultList);
        assertEquals("ResultList.where.equalTo returns wrong result", 0, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo("invalidcolumnname", "test").findAll();
        assertNotNull("ResultList.where.equalTo returns null when column name is non-existing", resultList);
        assertEquals("ResultList.where.equalTo returns wrong result", 0, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo("invalidcolumnname", true).findAll();
        assertNotNull("ResultList.where.equalTo returns null when column name is non-existing", resultList);
        assertEquals("ResultList.where.equalTo returns wrong result", 0, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo("invalidcolumnname", 3.1415d).findAll();
        assertNotNull("ResultList.where.equalTo returns null when column name is non-existing", resultList);
        assertEquals("ResultList.where.equalTo returns wrong result", 0, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo("invalidcolumnname", 3.1415f).findAll();
        assertNotNull("ResultList.where.equalTo returns null when column name is non-existing", resultList);
        assertEquals("ResultList.where.equalTo returns wrong result", 0, resultList.size());
    }

    public void testQueriesFailWithNullQueryValue() throws IOException {
        RealmResults<AllTypes> resultList = null;

        String nullString = null;
        Float nullFloat = null;
        Long nullLong = null;
        Boolean nullBoolean = null;

        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnstring", nullString).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnlong", nullLong).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnboolean", nullBoolean).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
        try {
            resultList = testRealm.where(AllTypes.class).equalTo("columnfloat", nullFloat).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException e) {
        }
    }

    // <E extends RealmObject> RealmTableOrViewList<E> allObjects(Class<E> clazz)
    public void testShouldReturnTableOrViewList() {
        RealmResults<AllTypes> resultList = testRealm.allObjects(AllTypes.class);
        assertEquals("Realm.get is returning wrong result set", TEST_DATA_SIZE, resultList.size());
    }

    // addChangeListener(RealmChangeListener listener)
    static int testCount = 0;

    public void testChangeNotify() {
        fail("Change listeners are not presently working");

        testRealm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                testCount++;
            }
        });

        testRealm.beginTransaction();
        for (int i = 0; i < BACKGROUND_COMMIT_TEST_DATA_SET_SIZE; i++) {

            Dog dog = testRealm.createObject(Dog.class);
            dog.setName("King" + Integer.toString(i));
        }

        testRealm.commitTransaction();

        getNotifications(1); // This really should be BACKGROUND_COMMIT_TEST_DATA_SET_SIZE
        // But there is a bug that prevents us from getting the right
        // number of notifications back. So for now we are only checking
        // that we do get at least one notification.

        testRealm.removeAllChangeListeners();

        assertTrue("Have not received the expected number of events in ChangeListener", 0 < testCount);

    }


    // void removeChangeListener(RealmChangeListener listener)
    public void testChangeNotifyRemove() {
        fail("Change listeners are not presently working");

        testCount = 0;

        RealmChangeListener realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                testCount++;
            }
        };

        testRealm.addChangeListener(realmChangeListener);

        testRealm.beginTransaction();
        for (int i = 0; i < BACKGROUND_COMMIT_TEST_DATA_SET_SIZE; i++) {

            Dog dog = testRealm.createObject(Dog.class);
            dog.setName("King" + Integer.toString(i));
        }

        testRealm.commitTransaction();

        getNotifications(1);  // This really should be BACKGROUND_COMMIT_TEST_DATA_SET_SIZE
        // But there is a bug that prevents us from getting the right
        // number of notifications back. So for now we are only checking
        // that we do get at least one notification.

        testRealm.removeAllChangeListeners();

        assertTrue("Have not received the expected number of events in ChangeListener", 0 < testCount);

        testCount = 0;

        testRealm.beginTransaction();
        for (int i = 0; i < BACKGROUND_COMMIT_TEST_DATA_SET_SIZE; i++) {

            Dog dog = testRealm.createObject(Dog.class);
            dog.setName("Fido" + Integer.toString(i));
        }

        testRealm.commitTransaction();

        getNotifications(1);
        assertEquals("Should not receive change notifications after removeChangeListener", 0, testCount);
    }

    // void removeChangeListener(RealmChangeListener listener)
    public void testFailChangeNotifyNoListener() {
        fail("Change listeners are not presently working");

        // Invalid input parameter to removeChangeListener:
        testRealm.removeChangeListener(null);

        RealmChangeListener realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                testCount++;
            }
        };

        // Invalid input parameter, realmChangeListener has never been added:
        testRealm.removeChangeListener(realmChangeListener);


        // Check that change listeners are still functional:
        testRealm.addChangeListener(realmChangeListener);

        testCount = 0;

        testRealm.beginTransaction();
        for (int i = 0; i < BACKGROUND_COMMIT_TEST_DATA_SET_SIZE; i++) {

            Dog dog = testRealm.createObject(Dog.class);
            dog.setName("Fido" + Integer.toString(i));
        }

        testRealm.commitTransaction();

        getNotifications(1);  // This really should be BACKGROUND_COMMIT_TEST_DATA_SET_SIZE
        // But there is a bug that prevents us from getting the right
        // number of notifications back. So for now we are only checking
        // that we do get at least one notification.

        testRealm.removeAllChangeListeners();
        assertTrue("Should receive change notifications after adding addChangeListener", 0 < testCount);
    }

    // void removeAllChangeListeners()
    public void testRemoveAllChangeListeners() {
        fail("Change listeners are not presently working");

        RealmChangeListener realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                testCount++;
            }
        };

        testRealm.addChangeListener(realmChangeListener);

        testRealm.removeAllChangeListeners();


        testCount = 0;

        testRealm.beginTransaction();
        for (int i = 0; i < BACKGROUND_COMMIT_TEST_DATA_SET_SIZE; i++) {

            Dog dog = testRealm.createObject(Dog.class);
            dog.setName("Fido" + Integer.toString(i));
        }

        testRealm.commitTransaction();

        getNotifications(1);

        assertEquals("Should not receive change notifications after removeAllChangeListeners", 0, testCount);

    }

    // void removeAllChangeListeners()
    public void testFailRemoveAllChangeListeners() {

        fail("Change listeners are not presently working");

        // Calling removeAllChangeListeners w/o any ChangeListeners installed:
        testRealm.removeAllChangeListeners();

        // Verify that change notification is still working:

        RealmChangeListener realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                testCount++;
            }
        };

        testRealm.addChangeListener(realmChangeListener);

        testCount = 0;

        testRealm.beginTransaction();
        for (int i = 0; i < BACKGROUND_COMMIT_TEST_DATA_SET_SIZE; i++) {

            Dog dog = testRealm.createObject(Dog.class);
            dog.setName("Fido" + Integer.toString(i));
        }

        testRealm.commitTransaction();

        getNotifications(1);  // This really should be BACKGROUND_COMMIT_TEST_DATA_SET_SIZE
        // But there is a bug that prevents us from getting the right
        // number of notifications back. So for now we are only checking
        // that we do get at least one notification.

        testRealm.removeAllChangeListeners();

        assertTrue("Should not receive change notifications after removeAllChangeListeners", 0 < testCount);
    }


    public void testChangeUpdateFromOtherThread() {

        fail("testChangeUpdateFromOtherThread has been disabled because it currently causes a native crash in the next test");

        testCount = 0;

        RealmChangeListener realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                testCount++;
            }
        };

        testRealm.addChangeListener(realmChangeListener);

        Thread addFromThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm localRealm = Realm.getInstance(getContext());
                localRealm.beginTransaction();
                for (int i = 0; i < BACKGROUND_COMMIT_TEST_DATA_SET_SIZE; i++) {

                    Dog dog = localRealm.createObject(Dog.class);
                    dog.setName("Fido" + Integer.toString(i));
                }
                localRealm.commitTransaction();

                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {

                }
            }
        });

        addFromThread.start();

        try {
            addFromThread.join();

            getNotifications(BACKGROUND_COMMIT_TEST_DATA_SET_SIZE);

            testRealm.removeAllChangeListeners();

            assertTrue("Should receive change notifications when modifying table in another thread ", BACKGROUND_COMMIT_TEST_DATA_SET_SIZE <= testCount);
        } catch (Exception ex) {
            fail("Unexpected exception " + ex.getMessage());
        }
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

        resultList = testRealm.where(AllTypes.class).equalTo("columnstring", "a unique string").findAll();
        assertEquals("Change has not been committed correctly", 1, resultList.size());
        resultList = testRealm.where(AllTypes.class).equalTo("columnfloat", 3.1415f).findAll();
        assertEquals("Change has not been committed", 1, resultList.size());
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

    // void clear(Class<?> classSpec)
    public void testClassClear() {

        // Currently clear will not work outside a transaction:

        //testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        //testRealm.commitTransaction();

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

        //testRealm.beginTransaction();
        testRealm.clear(Dog.class);
        //testRealm.commitTransaction();

        RealmResults<AllTypes> resultListTypes = testRealm.where(AllTypes.class).findAll();
        RealmResults<Dog> resultListDogs = testRealm.where(Dog.class).findAll();

        assertEquals("Realm.clear does not clear table", 0, resultListDogs.size());
        assertEquals("Realm.clear cleared wrong table", TEST_DATA_SIZE, resultListTypes.size());


        //testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        //testRealm.commitTransaction();

        resultListTypes = testRealm.where(AllTypes.class).findAll();
        assertEquals("Realm.clear does not remove table", 0, resultListTypes.size());
    }

    // int getVersion()
    public void testGetVersion() throws IOException {

        long version = testRealm.getVersion();

        assertTrue("Realm.version returns invalid version number", version > 0);
    }

    // void setVersion(int version)setVersion(int version)
    public void testSetVersion() {
        long version = 42;

        testRealm.setVersion(version);

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
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).between("columnlong", 0, 9).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 10, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith("columnstring", "test data ").findAll();
        assertEquals("Not the expected number records " + resultList.size(), TEST_DATA_SIZE, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith("columnstring", "test data 1").between("columnlong", 2, 20).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 10, resultList.size());

        resultList = testRealm.where(AllTypes.class).between("columnlong", 2, 20).beginsWith("columnstring", "test data 1").findAll();
        assertEquals("Not the expected number records " + resultList.size(), 10, resultList.size());
    }


    public void testRealmQueryGreaterThan() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).greaterThan("columnfloat", 10.234567f).findAll();
        assertEquals("Not the expected number records " + resultList.size(), TEST_DATA_SIZE - 10, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith("columnstring", "test data 1").greaterThan("columnfloat", 50.234567f).findAll();
        assertEquals("Not the expected number records " + resultList.size(), TEST_DATA_SIZE - 100, resultList.size());

        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).greaterThan("columnfloat", 11.234567f);
        resultList = query.between("columnlong", 1, 20).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 10, resultList.size());
    }


    public void testRealmQueryGreaterThanOrEqualTo() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).greaterThanOrEqualTo("columnfloat", 10.234567f).findAll();
        assertEquals("Not the expected number records " + resultList.size(), TEST_DATA_SIZE - 9, resultList.size());

        resultList = testRealm.where(AllTypes.class).beginsWith("columnstring", "test data 1").greaterThanOrEqualTo("columnfloat", 50.234567f).findAll();
        assertEquals("Not the expected number records " + resultList.size(), TEST_DATA_SIZE - 100, resultList.size());

        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).greaterThanOrEqualTo("columnfloat", 11.234567f);
        query =  query.between("columnlong", 1, 20);

        resultList = query.beginsWith("columnstring", "test data 15").findAll();
        assertEquals("Not the expected number records " + resultList.size(), 1, resultList.size());
    }

    public void testRealmQueryOr() {
        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).equalTo("columnfloat", 31.234567f);
        RealmResults<AllTypes> resultList = query.or().between("columnlong", 1, 20).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 21, resultList.size());

        resultList = query.or().equalTo("columnstring", "test data 15").findAll();
        assertEquals("Not the expected number records " + resultList.size(), 21, resultList.size());

        resultList = query.or().equalTo("columnstring", "test data 117").findAll();
        assertEquals("Not the expected number records " + resultList.size(), 22, resultList.size());
    }

    public void testRealmQueryImplicitAnd() {
        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).equalTo("columnfloat", 31.234567f);
        RealmResults<AllTypes> resultList = query.between("columnlong", 1, 10).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 0, resultList.size());

        query = testRealm.where(AllTypes.class).equalTo("columnfloat", 81.234567f);
        resultList = query.between("columnlong", 1, 100).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 1, resultList.size());
    }

    public void testRealmQueryLessThan() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).lessThan("columnfloat", 31.234567f).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 30, resultList.size());
        RealmQuery<AllTypes> query = testRealm.where(AllTypes.class).lessThan("columnfloat", 31.234567f);
        resultList = query.between("columnlong", 1, 10).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 10, resultList.size());
    }

    public void testRealmQueryLessThanOrEqual() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).lessThanOrEqualTo("columnfloat", 31.234567f).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 31, resultList.size());
        resultList = testRealm.where(AllTypes.class).lessThanOrEqualTo("columnfloat", 31.234567f).between("columnlong", 11, 20).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 10, resultList.size());
    }

    public void testRealmQueryEqualTo() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).equalTo("columnfloat", 31.234567f).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 1, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThan("columnfloat", 11.0f).equalTo("columnlong", 10).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 1, resultList.size());
        resultList = testRealm.where(AllTypes.class).greaterThan("columnfloat", 11.0f).equalTo("columnlong", 1).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 0, resultList.size());
    }

    public void testRealmQueryNotEqualTo() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).notEqualTo("columnlong", 31).findAll();
        assertEquals("Not the expected number records " + resultList.size(), TEST_DATA_SIZE - 1, resultList.size());
        resultList = testRealm.where(AllTypes.class).notEqualTo("columnfloat", 11.234567f).equalTo("columnlong", 10).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 0, resultList.size());
        resultList = testRealm.where(AllTypes.class).notEqualTo("columnfloat", 11.234567f).equalTo("columnlong", 1).findAll();
        assertEquals("Not the expected number records " + resultList.size(), 1, resultList.size());
    }
}
