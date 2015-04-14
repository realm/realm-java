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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.CyclicType;
import io.realm.entities.CyclicTypePrimaryKey;
import io.realm.entities.Cat;
import io.realm.entities.Dog;
import io.realm.entities.DogPrimaryKey;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.Owner;
import io.realm.entities.OwnerPrimaryKey;
import io.realm.entities.PrimaryKeyAsLong;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.entities.PrimaryKeyMix;
import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmIOException;
import io.realm.internal.Table;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;

public class RealmTest extends AndroidTestCase {

    protected final static int TEST_DATA_SIZE = 10;

    protected Realm testRealm;

    protected List<String> columnData = new ArrayList<String>();

    private final static String FIELD_STRING = "columnString";
    private final static String FIELD_LONG = "columnLong";
    private final static String FIELD_FLOAT = "columnFloat";
    private final static String FIELD_DOUBLE = "columnDouble";
    private final static String FIELD_BOOLEAN = "columnBoolean";
    private final static String FIELD_DATE = "columnDate";

    protected void setColumnData() {
        columnData.add(0, FIELD_BOOLEAN);
        columnData.add(1, FIELD_DATE);
        columnData.add(2, FIELD_DOUBLE);
        columnData.add(3, FIELD_FLOAT);
        columnData.add(4, FIELD_STRING);
        columnData.add(5, FIELD_LONG);
    }

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        if (testRealm != null) {
            testRealm.close();
        }
    }

    private void populateTestRealm(int objects) {
        testRealm.beginTransaction();
        testRealm.allObjects(AllTypes.class).clear();
        testRealm.allObjects(NonLatinFieldNames.class).clear();
        for (int i = 0; i < objects; ++i) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date());
            allTypes.setColumnDouble(3.1415);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            NonLatinFieldNames nonLatinFieldNames = testRealm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
            nonLatinFieldNames.set베타(1.234567f + i);
            nonLatinFieldNames.setΒήτα(1.234567f + i);
        }
        testRealm.commitTransaction();
    }

    private void populateTestRealm() {
        populateTestRealm(TEST_DATA_SIZE);
    }


    public void testGetInstanceNullFolderThrows() {
        try {
            Realm.getInstance((File) null);
            fail("Parsing null as folder should throw an error");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetInstanceNullNameThrows() {
        try {
            Realm.getInstance(getContext(), (String) null);
            fail("Parsing null as realm name should throw an error");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetInstanceCreateSubFoldersThrows() {
        File folder = new File(getContext().getFilesDir().getAbsolutePath() + "/subfolder1/subfolder2/");
        try {
            Realm.getInstance(getContext(), (String) null);
            fail("Assuming that subfolders are created automatically should fail");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetInstanceFolderNoWritePermissionThrows() {
        File folder = new File("/");
        try {
            Realm realm = Realm.getInstance(folder);
            fail("Pointing to a folder with no write permission should throw an error");
        } catch (RealmIOException expected) {
        }
    }

    public void testGetInstanceFileNoWritePermissionThrows() throws IOException {
        String REALM_FILE = "readonly.realm";
        File folder = getContext().getFilesDir();
        File realmFile = new File(folder, REALM_FILE);
        if (realmFile.exists()) {
            realmFile.delete(); // Reset old test data
        }

        assertTrue(realmFile.createNewFile());
        assertTrue(realmFile.setWritable(false));

        try {
            Realm.getInstance(folder, REALM_FILE);
            fail("Trying to open a read-only file should fail");
        } catch (RealmIOException expected) {
        }
    }

    // TODO Disabled due to the build phone keep crashing on this. It might be related to https://github.com/realm/realm-java/issues/1008
    public void DISABLEtestGetInstanceClearsCacheWhenFailed() {
        String REALM_NAME = "invalid_cache.realm";
        Realm.deleteRealmFile(getContext(), REALM_NAME);
        Random random = new Random();
        byte[] key = new byte[64];
        random.nextBytes(key);
        Realm realm = Realm.getInstance(getContext(), REALM_NAME, key); // Create starting Realm with key1
        realm.close();
        random.nextBytes(key);
        try {
            Realm.getInstance(getContext(), REALM_NAME, key); // Try to open with key 2
        } catch (IllegalArgumentException expected) {
            // Delete Realm so key 2 works. This should work as a Realm shouldn't be cached
            // if initialization failed.
            assertTrue(Realm.deleteRealmFile(getContext(), REALM_NAME));
            Realm.getInstance(getContext(), REALM_NAME, key);
            realm.close();
        }
    }

    public void testRealmCache() {
        Realm newRealm = Realm.getInstance(getContext());
        assertEquals(testRealm, newRealm);
        newRealm.close();
    }

    public void testInternalRealmChangedHandlersRemoved() {
        final String REALM_NAME = "test-internalhandlers";
        Realm.deleteRealmFile(getContext(), REALM_NAME);
        Realm.handlers.clear(); // Make sure that handlers from other unit tests doesn't interfere.

        // Open and close first instance of a Realm
        Realm realm = null;
        try {
            realm = Realm.getInstance(getContext(), REALM_NAME);
            assertEquals(1, Realm.handlers.size());
            realm.close();

            // All Realms closed. No handlers should be alive.
            assertEquals(0, Realm.handlers.size());

            // Open instance the 2nd time. Old handler should now be gone
            realm = Realm.getInstance(getContext(), REALM_NAME);
            assertEquals(1, Realm.handlers.size());
            realm.close();

        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public void testShouldCreateRealm() {
        assertNotNull("Realm.getInstance unexpectedly returns null", testRealm);
        assertTrue("Realm.getInstance does not contain expected table", testRealm.contains(AllTypes.class));
    }

    public void testShouldNotFailCreateRealmWithNullContext() {
        Realm realm = null;
        try {
            realm = Realm.getInstance((Context) null); // throws when c.getDirectory() is called;
            // has nothing to do with Realm
            fail("Should throw an exception");
        } catch (NullPointerException ignore) {
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    // Table getTable(Class<?> clazz)
    public void testShouldGetTable() {
        Table table = testRealm.getTable(AllTypes.class);
        assertNotNull(table);
    }

    // <E> void remove(Class<E> clazz, long objectIndex)
    public void testShouldRemoveRow() {
        populateTestRealm();
        testRealm.beginTransaction();
        testRealm.remove(AllTypes.class, 0);
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE - 1, resultList.size());
    }

    // <E extends RealmObject> E get(Class<E> clazz, long rowIndex)
    public void testShouldGetObject() {
        populateTestRealm();
        AllTypes allTypes = testRealm.get(AllTypes.class, 0);
        assertNotNull(allTypes);
        assertEquals("test data 0", allTypes.getColumnString());
    }

    // boolean contains(Class<?> clazz)
    public void testShouldContainTable() {
        testRealm.beginTransaction();
        testRealm.createObject(Dog.class);
        testRealm.commitTransaction();
        assertTrue("contains returns false for newly created table", testRealm.contains(Dog.class));
        assertFalse("contains returns true for non-existing table", testRealm.contains(RealmTest.class));
    }

    // <E extends RealmObject> RealmQuery<E> where(Class<E> clazz)
    public void testShouldReturnResultSet() {
        populateTestRealm();
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE, resultList.size());
    }

    // Note that this test is relying on the values set while initializing the test dataset
    public void testQueriesResults() throws IOException {
        populateTestRealm(159);
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).equalTo(FIELD_LONG, 33).findAll();
        assertEquals(1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo(FIELD_LONG, 3333).findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo(FIELD_STRING, "test data 0").findAll();
        assertEquals(1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo(FIELD_STRING, "test data 0", RealmQuery.CASE_INSENSITIVE).findAll();
        assertEquals(1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo(FIELD_STRING, "Test data 0", RealmQuery.CASE_SENSITIVE).findAll();
        assertEquals(0, resultList.size());
    }

    public void testQueriesWithDataTypes() throws IOException {
        populateTestRealm();
        setColumnData();

        for (int i = 0; i < columnData.size(); i++) {
            try {
                testRealm.where(AllTypes.class).equalTo(columnData.get(i), true).findAll();
                if (i != 0) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }

            try {
                testRealm.where(AllTypes.class).equalTo(columnData.get(i), new Date()).findAll();
                if (i != 1) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }

            try {
                testRealm.where(AllTypes.class).equalTo(columnData.get(i), 13.37d).findAll();
                if (i != 2) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }

            try {
                testRealm.where(AllTypes.class).equalTo(columnData.get(i), 13.3711f).findAll();
                if (i != 3) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }

            try {
                testRealm.where(AllTypes.class).equalTo(columnData.get(i), "test").findAll();
                if (i != 4) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }

            try {
                testRealm.where(AllTypes.class).equalTo(columnData.get(i), 1337).findAll();
                if (i != 5) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void testQueriesFailWithInvalidDataTypes() throws IOException {
        try {
            testRealm.where(AllTypes.class).equalTo("invalidcolumnname", 33).findAll();
            fail("Invalid field name");
        } catch (Exception ignored) {
        }

        try {
            testRealm.where(AllTypes.class).equalTo("invalidcolumnname", "test").findAll();
            fail("Invalid field name");
        } catch (Exception ignored) {
        }

        try {
            testRealm.where(AllTypes.class).equalTo("invalidcolumnname", true).findAll();
            fail("Invalid field name");
        } catch (Exception ignored) {
        }

        try {
            testRealm.where(AllTypes.class).equalTo("invalidcolumnname", 3.1415d).findAll();
            fail("Invalid field name");
        } catch (Exception ignored) {
        }

        try {
            testRealm.where(AllTypes.class).equalTo("invalidcolumnname", 3.1415f).findAll();
            fail("Invalid field name");
        } catch (Exception ignored) {
        }
    }

    public void testQueriesFailWithNullQueryValue() throws IOException {
        try {
            testRealm.where(AllTypes.class).equalTo(FIELD_STRING, (String) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }
    }

    // <E extends RealmObject> RealmTableOrViewList<E> allObjects(Class<E> clazz)
    public void testShouldReturnTableOrViewList() {
        populateTestRealm();
        RealmResults<AllTypes> resultList = testRealm.allObjects(AllTypes.class);
        assertEquals("Realm.get is returning wrong result set", TEST_DATA_SIZE, resultList.size());
    }

    public void testAllObjectsSorted() {
        populateTestRealm();
        RealmResults<AllTypes> sortedList = testRealm.allObjectsSorted(AllTypes.class, FIELD_STRING, RealmResults.SORT_ORDER_ASCENDING);
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("test data 0", sortedList.first().getColumnString());

        RealmResults<AllTypes> reverseList = testRealm.allObjectsSorted(AllTypes.class, FIELD_STRING, RealmResults.SORT_ORDER_DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals("test data 0", reverseList.last().getColumnString());

        try {
            RealmResults<AllTypes> none = testRealm.allObjectsSorted(AllTypes.class, "invalid", RealmResults.SORT_ORDER_ASCENDING);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testSortTwoFields() {
        io.realm.internal.test.TestHelper.populateForMultiSort(testRealm);

        RealmResults<AllTypes> results1 = testRealm.allObjectsSorted(AllTypes.class,
                new String[]{FIELD_STRING, FIELD_LONG},
                new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});

        assertEquals(3, results1.size());

        assertEquals("Adam", results1.get(0).getColumnString());
        assertEquals(4, results1.get(0).getColumnLong());

        assertEquals("Adam", results1.get(1).getColumnString());
        assertEquals(5, results1.get(1).getColumnLong());

        assertEquals("Brian", results1.get(2).getColumnString());
        assertEquals(4, results1.get(2).getColumnLong());

        RealmResults<AllTypes> results2 = testRealm.allObjectsSorted(AllTypes.class,
                new String[]{FIELD_LONG, FIELD_STRING},
                new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});

        assertEquals(3, results2.size());

        assertEquals("Adam", results2.get(0).getColumnString());
        assertEquals(4, results2.get(0).getColumnLong());

        assertEquals("Brian", results2.get(1).getColumnString());
        assertEquals(4, results2.get(1).getColumnLong());

        assertEquals("Adam", results2.get(2).getColumnString());
        assertEquals(5, results2.get(2).getColumnLong());
    }

    public void testSortMultiFailures() {
        RealmResults<AllTypes> allTypes = testRealm.allObjects(AllTypes.class);

        // zero fields specified
        try {
            testRealm.allObjectsSorted(AllTypes.class, new String[]{}, new boolean[]{});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // number of fields and sorting orders don't match
        try {
            testRealm.allObjectsSorted(AllTypes.class,
                    new String[]{FIELD_STRING},
                    new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // null is not allowed
        try {
            testRealm.allObjectsSorted(AllTypes.class, null, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            testRealm.allObjectsSorted(AllTypes.class, new String[]{FIELD_STRING}, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // non-existing field name
        try {
            testRealm.allObjectsSorted(AllTypes.class,
                    new String[]{FIELD_STRING, "dont-exist"},
                    new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testSortSingleField() {
        populateTestRealm();
        RealmResults<AllTypes> sortedList = testRealm.allObjectsSorted(AllTypes.class,
                new String[]{FIELD_LONG},
                new boolean[]{RealmResults.SORT_ORDER_DESCENDING});
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(TEST_DATA_SIZE - 1, sortedList.first().getColumnLong());
        assertEquals(0, sortedList.last().getColumnLong());
    }

    // void beginTransaction()
    public void testBeginTransaction() throws IOException {
        populateTestRealm();

        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnFloat(3.1415f);
        allTypes.setColumnString("a unique string");
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE + 1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo(FIELD_STRING, "a unique string").findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(AllTypes.class).equalTo(FIELD_FLOAT, 3.1415f).findAll();
        assertEquals(1, resultList.size());
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
    private boolean transactionMethodWrongThread(final TransactionMethod method) throws InterruptedException,
            ExecutionException {
        if (method != TransactionMethod.METHOD_BEGIN) {
            testRealm.beginTransaction();
            testRealm.createObject(Dog.class); // FIXME: Empty transactions cannot be cancelled
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    switch (method) {
                        case METHOD_BEGIN:
                            testRealm.beginTransaction();
                            break;
                        case METHOD_COMMIT:
                            testRealm.commitTransaction();
                            break;
                        case METHOD_CANCEL:
                            testRealm.cancelTransaction();
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
            testRealm.cancelTransaction();
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
        populateTestRealm();

        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnBoolean(true);
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE + 1, resultList.size());
    }


    public void testCancelTransaction() {
        populateTestRealm();

        testRealm.beginTransaction();
        testRealm.createObject(AllTypes.class);
        testRealm.cancelTransaction();
        assertEquals(TEST_DATA_SIZE, testRealm.allObjects(AllTypes.class).size());

        try {
            testRealm.cancelTransaction();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }


    public void testExecuteTransactionNull() {
        testRealm.executeTransaction(null); // Nothing happens
        assertFalse(testRealm.hasChanged());
    }

    public void testExecuteTransactionCommit() {
        assertEquals(0, testRealm.allObjects(Owner.class).size());
        testRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Owner owner = realm.createObject(Owner.class);
                owner.setName("Owner");
            }
        });
        assertEquals(1, testRealm.allObjects(Owner.class).size());
    }

    public void testExecuteTransactionCancel() {
        assertEquals(0, testRealm.allObjects(Owner.class).size());
        try {
            testRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Owner owner = realm.createObject(Owner.class);
                    owner.setName("Owner");
                    throw new RuntimeException("Boom");
                }
            });
        } catch (RealmException ignore) {
        }
        assertEquals(0, testRealm.allObjects(Owner.class).size());
    }


    // void clear(Class<?> classSpec)
    public void testClear() {
        // ** clear non existing table should succeed

        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        testRealm.commitTransaction();

        // ** clear existing class, but leave other classes classes

        // Add two classes
        populateTestRealm();
        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("Castro");
        testRealm.commitTransaction();
        // Clear
        testRealm.beginTransaction();
        testRealm.clear(Dog.class);
        testRealm.commitTransaction();
        // Check one class is cleared but other class is still there
        RealmResults<AllTypes> resultListTypes = testRealm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE, resultListTypes.size());
        RealmResults<Dog> resultListDogs = testRealm.where(Dog.class).findAll();
        assertEquals(0, resultListDogs.size());

        // ** clear() must throw outside a transaction
        try {
            testRealm.clear(AllTypes.class);
            fail("Expected exception");
        } catch (IllegalStateException ignored) {
        }
    }

    // int getVersion() AND void setVersion(int version)
    public void testGetVersionAndSetVersion() throws IOException {
        // ** Initial version must be 0
        populateTestRealm();
        long version = testRealm.getVersion();
        assertEquals(0, version);

        // ** Version should be updateable
        version = 42;
        testRealm.beginTransaction();
        testRealm.setVersion(version);
        testRealm.commitTransaction();
        assertEquals(version, testRealm.getVersion());
    }

    public void testShouldFailOutsideTransaction() {
        // These calls should fail outside a Transaction:
        try {
            testRealm.createObject(AllTypes.class);
            fail("Realm.createObject should fail outside write transaction");
        } catch (IllegalStateException ignored) {
        }
        try {
            testRealm.remove(AllTypes.class, 0);
            fail("Realm.remove should fail outside write transaction");
        } catch (IllegalStateException ignored) {
        }
    }

    public void createAndTestFilename(String language, String fileName) {
        Realm.deleteRealmFile(getContext(), fileName);
        Realm realm1 = Realm.getInstance(getContext(), fileName);
        realm1.beginTransaction();
        Dog dog1 = realm1.createObject(Dog.class);
        dog1.setName("Rex");
        realm1.commitTransaction();
        realm1.close();

        File file = new File(getContext().getFilesDir(), fileName);
        assertTrue(language, file.exists());

        Realm realm2 = Realm.getInstance(getContext(), fileName);
        Dog dog2 = realm2.allObjects(Dog.class).first();
        assertEquals(language, "Rex", dog2.getName());
        realm2.close();
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

    // This test is slow. Move it to another testsuite that runs once a day on Jenkins.
    public void rarely_run_testUTF8() {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        testRealm.commitTransaction();

        String file = "assets/unicode_codepoints.csv";
        Scanner scanner = new Scanner(getClass().getClassLoader().getResourceAsStream(file));
        int i = 0;
        String currentUnicode = null;
        try {
            testRealm.beginTransaction();
            while (scanner.hasNextLine()) {
                currentUnicode = scanner.nextLine();
                char[] chars = Character.toChars(Integer.parseInt(currentUnicode, 16));
                String codePoint = new String(chars);
                AllTypes o = testRealm.createObject(AllTypes.class);
                o.setColumnLong(i);
                o.setColumnString(codePoint);

                AllTypes realmType = testRealm.where(AllTypes.class).equalTo("columnLong", i).findFirst();
                if (i > 1) {
                    assertEquals("Codepoint: " + i + " / " + currentUnicode, codePoint,
                            realmType.getColumnString()); // codepoint 0 is NULL, ignore for now.
                }
                i++;
            }
            testRealm.commitTransaction();
        } catch (Exception e) {
            fail("Failure, Codepoint: " + i + " / " + currentUnicode + " " + e.getMessage());
        }
    }

    private List<String> getCharacterArray() {
        List<String> chars_array = new ArrayList<String>();
        String file = "assets/unicode_codepoints.csv";
        Scanner scanner = new Scanner(getClass().getClassLoader().getResourceAsStream(file));
        int i = 0;
        String currentUnicode = null;
        try {
            while (scanner.hasNextLine()) {
                currentUnicode = scanner.nextLine();
                char[] chars = Character.toChars(Integer.parseInt(currentUnicode, 16));
                String codePoint = new String(chars);
                chars_array.add(codePoint);
                i++;
            }
        } catch (Exception e) {
            fail("Failure, Codepoint: " + i + " / " + currentUnicode + " " + e.getMessage());
        }
        return chars_array;
    }

    // This test is disabled.
    // The test writes and reads random Strings.
    public void disabledTestUnicodeString() {
        List<String> chars_array = getCharacterArray();
        // Change seed value for new random values.
        long seed = 20;
        Random random = new Random(seed);

        int random_value = 0;

        String test_char = "";
        String test_char_old = "";
        String get_data = "";

        for (int i = 0; i < 1000; i++) {
            random_value = random.nextInt(25);

            for (int j = 0; j < random_value; j++) {
                test_char = test_char_old + chars_array.get(random.nextInt(27261));
                test_char_old = test_char;
            }
            testRealm.beginTransaction();
            StringOnly stringOnly = testRealm.createObject(StringOnly.class);
            stringOnly.setChars(test_char);
            testRealm.commitTransaction();

            get_data = testRealm.allObjects(StringOnly.class).get(0).getChars();

            testRealm.beginTransaction();
            testRealm.clear(StringOnly.class);
            testRealm.commitTransaction();
        }
    }

    public void testReferenceCounting() {
        // At this point reference count should be one because of the setUp method
        try {
            testRealm.where(AllTypes.class).count();
        } catch (IllegalStateException e) {
            fail();
        }

        // Make sure the reference counter is per realm file
        Realm.deleteRealmFile(getContext(), "anotherRealm.realm");
        Realm otherRealm = Realm.getInstance(getContext(), "anotherRealm.realm");

        // Raise the reference
        Realm realm = null;
        try {
            realm = Realm.getInstance(getContext());
        } finally {
            if (realm != null) realm.close();
        }

        try {
            // This should not fail because the reference is now 1
            if (realm != null) {
                realm.where(AllTypes.class).count();
            }
        } catch (IllegalStateException e) {
            fail();
        }

        testRealm.close();
        try {
            testRealm.where(AllTypes.class).count();
            fail();
        } catch (IllegalStateException ignored) {
        }

        try {
            otherRealm.where(AllTypes.class).count();
        } catch (IllegalStateException e) {
            fail();
        } finally {
            otherRealm.close();
        }

        try {
            otherRealm.where(AllTypes.class).count();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testReferenceCountingDoubleClose() {
        testRealm.close();
        testRealm.close(); // Count down once too many. Counter is now potentially negative
        testRealm = Realm.getInstance(getContext());
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        RealmResults<AllTypes> queryResult = testRealm.allObjects(AllTypes.class);
        assertEquals(allTypes, queryResult.get(0));
        testRealm.commitTransaction();
        testRealm.close(); // This might not close the Realm if the reference count is wrong
        try {
            allTypes.getColumnString();
            fail("Realm should be closed");
        } catch (IllegalStateException expected) {
        }
    }

    public void testWriteCopyTo() throws IOException {
        Realm.deleteRealmFile(getContext(), "file1.realm");
        Realm.deleteRealmFile(getContext(), "file2.realm");

        Realm realm1 = null;
        try {
            realm1 = Realm.getInstance(getContext(), "file1.realm");
            realm1.beginTransaction();
            AllTypes allTypes = realm1.createObject(AllTypes.class);
            allTypes.setColumnString("Hello World");
            realm1.commitTransaction();

            realm1.writeCopyTo(new File(getContext().getFilesDir(), "file2.realm"));
        } finally {
            if (realm1 != null) {
                realm1.close();
            }
        }

        // Copy is compacted i.e. smaller than original
        File file1 = new File(getContext().getFilesDir(), "file1.realm");
        File file2 = new File(getContext().getFilesDir(), "file2.realm");
        assertTrue(file1.length() >= file2.length());

        Realm realm2 = null;
        try {
            // Contents is copied too
            realm2 = Realm.getInstance(getContext(), "file2.realm");
            RealmResults<AllTypes> results = realm2.allObjects(AllTypes.class);
            assertEquals(1, results.size());
            assertEquals("Hello World", results.first().getColumnString());
        } finally {
            if (realm2 != null) {
                realm2.close();
            }
        }
    }

    public void testCompactRealmFile() throws IOException {
        final String copyRealm = "copy.realm";
        fileCopy(
                new File(getContext().getFilesDir(), Realm.DEFAULT_REALM_NAME),
                new File(getContext().getFilesDir(), copyRealm));
        long before = new File(getContext().getFilesDir(), copyRealm).length();
        assertTrue(Realm.compactRealmFile(getContext()));
        long after = new File(getContext().getFilesDir(), copyRealm).length();
        assertTrue(before >= after);
    }

    public void testCopyToRealmNullObjectThrows() {
        testRealm.beginTransaction();
        try {
            testRealm.copyToRealm((AllTypes) null);
            fail("Copying null objects into Realm should not be allowed");
        } catch (IllegalArgumentException ignore) {
        } finally {
            testRealm.cancelTransaction();
        }
    }

    public void testCopyManagedObjectIsNoop() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnString("Test");
        testRealm.commitTransaction();

        testRealm.commitTransaction();
        AllTypes copiedAllTypes = testRealm.copyToRealm(allTypes);
        testRealm.commitTransaction();

        assertTrue(allTypes == copiedAllTypes);
    }

    public void testCopManagedObjectToOtherRealm() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnString("Test");
        testRealm.commitTransaction();

        Realm.deleteRealmFile(getContext(), "other-realm");
        Realm otherRealm = Realm.getInstance(getContext(), "other-realm");
        otherRealm.beginTransaction();
        AllTypes copiedAllTypes = otherRealm.copyToRealm(allTypes);
        otherRealm.commitTransaction();

        assertNotSame(allTypes, copiedAllTypes); // Same object in different Realms is not the same
        assertEquals(allTypes.getColumnString(), copiedAllTypes.getColumnString()); // But data is still the same
        otherRealm.close();
    }

    public void testCopyToRealmObject() {
        Date date = new Date();
        date.setTime(1000); // Remove ms. precission as Realm doesn't support it yet.
        Dog dog = new Dog();
        dog.setName("Fido");
        RealmList<Dog> list = new RealmList<Dog>();
        list.add(dog);

        AllTypes allTypes = new AllTypes();
        allTypes.setColumnString("String");
        allTypes.setColumnLong(1l);
        allTypes.setColumnFloat(1f);
        allTypes.setColumnDouble(1d);
        allTypes.setColumnBoolean(true);
        allTypes.setColumnDate(date);
        allTypes.setColumnBinary(new byte[]{1, 2, 3});
        allTypes.setColumnRealmObject(dog);
        allTypes.setColumnRealmList(list);

        testRealm.beginTransaction();
        AllTypes realmTypes = testRealm.copyToRealm(allTypes);
        testRealm.commitTransaction();

        assertNotSame(allTypes, realmTypes); // Objects should not be considered equal
        assertEquals(allTypes.getColumnString(), realmTypes.getColumnString()); // But they contain the same data
        assertEquals(allTypes.getColumnLong(), realmTypes.getColumnLong());
        assertEquals(allTypes.getColumnFloat(), realmTypes.getColumnFloat());
        assertEquals(allTypes.getColumnDouble(), realmTypes.getColumnDouble());
        assertEquals(allTypes.isColumnBoolean(), realmTypes.isColumnBoolean());
        assertEquals(allTypes.getColumnDate(), realmTypes.getColumnDate());
        assertArrayEquals(allTypes.getColumnBinary(), realmTypes.getColumnBinary());
        assertEquals(allTypes.getColumnRealmObject().getName(), dog.getName());
        assertEquals(list.size(), realmTypes.getColumnRealmList().size());
        assertEquals(list.get(0).getName(), realmTypes.getColumnRealmList().get(0).getName());
    }

    public void testCopyToRealmCyclic() {
        CyclicType oneCyclicType = new CyclicType();
        oneCyclicType.setName("One");
        CyclicType anotherCyclicType = new CyclicType();
        anotherCyclicType.setName("Two");
        oneCyclicType.setObject(anotherCyclicType);
        anotherCyclicType.setObject(oneCyclicType);

        testRealm.beginTransaction();
        CyclicType realmObject = testRealm.copyToRealm(oneCyclicType);
        testRealm.commitTransaction();

        assertEquals("One", realmObject.getName());
        assertEquals("Two", realmObject.getObject().getName());
        assertEquals(2, testRealm.allObjects(CyclicType.class).size());
    }

    public void testCopyToRealmCyclicList() {
        CyclicType oneCyclicType = new CyclicType();
        oneCyclicType.setName("One");
        CyclicType anotherCyclicType = new CyclicType();
        anotherCyclicType.setName("Two");
        oneCyclicType.setObjects(new RealmList(anotherCyclicType));
        anotherCyclicType.setObjects(new RealmList(oneCyclicType));

        testRealm.beginTransaction();
        CyclicType realmObject = testRealm.copyToRealm(oneCyclicType);
        testRealm.commitTransaction();

        assertEquals("One", realmObject.getName());
        assertEquals(2, testRealm.allObjects(CyclicType.class).size());
    }

    // Check that if a field has a null value it gets converted to the default value for that type
    public void testCopyToRealmDefaultValues() {
        testRealm.beginTransaction();
        AllTypes realmTypes = testRealm.copyToRealm(new AllTypes());
        testRealm.commitTransaction();

        assertEquals("", realmTypes.getColumnString());
        assertEquals(new Date(0), realmTypes.getColumnDate());
        assertArrayEquals(new byte[0], realmTypes.getColumnBinary());
    }

    // Check that using copyToRealm will set the primary key directly instead of first setting
    // it to the default value (which can fail)
    public void testCopyToRealmWithPrimaryKeySetValueDirectly() {
        testRealm.beginTransaction();
        testRealm.createObject(OwnerPrimaryKey.class);
        testRealm.copyToRealm(new OwnerPrimaryKey(1, "Foo"));
        testRealm.commitTransaction();
        assertEquals(2, testRealm.where(OwnerPrimaryKey.class).count());
    }

    public void testCopyToRealmWithPrimaryAsNullThrows() {
        testRealm.beginTransaction();
        try {
            testRealm.copyToRealm(new PrimaryKeyAsString());
            fail();
        } catch (RealmException expected) {
        } finally {
            testRealm.cancelTransaction();
        }
    }

    public void testCopyToRealmDontCopyNestedRealmObjets() {
        testRealm.beginTransaction();
        CyclicTypePrimaryKey childObj = testRealm.createObject(CyclicTypePrimaryKey.class);
        childObj.setName("Child");
        childObj.setId(1);

        CyclicTypePrimaryKey parentObj = new CyclicTypePrimaryKey(2);
        parentObj.setObject(childObj);
        testRealm.copyToRealm(parentObj);
        testRealm.commitTransaction();

        assertEquals(2, testRealm.where(CyclicTypePrimaryKey.class).count());
    }

    public void testCopyToRealmList() {
        Dog dog1 = new Dog();
        dog1.setName("Dog 1");
        Dog dog2 = new Dog();
        dog2.setName("Dog 2");
        RealmList<Dog> list = new RealmList<Dog>();
        list.addAll(Arrays.asList(dog1, dog2));

        testRealm.beginTransaction();
        List<Dog> copiedList = new ArrayList<Dog>(testRealm.copyToRealm(list));
        testRealm.commitTransaction();

        assertEquals(2, copiedList.size());
        assertEquals(dog1.getName(), copiedList.get(0).getName());
        assertEquals(dog2.getName(), copiedList.get(1).getName());
    }

    public void testCopyToRealmOrUpdateNullThrows() {
        try {
            testRealm.copyToRealmOrUpdate((AllTypes) null);
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testCopyOrUpdateNoPrimaryKeyThrows() {
        try {
            testRealm.copyToRealmOrUpdate(new AllTypes());
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    public void testCopyOrUpdateAddObject() {
        testRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                PrimaryKeyAsLong obj = new PrimaryKeyAsLong();
                obj.setId(1);
                obj.setName("Foo");
                realm.copyToRealm(obj);

                PrimaryKeyAsLong obj2 = new PrimaryKeyAsLong();
                obj2.setId(2);
                obj2.setName("Bar");
                realm.copyToRealmOrUpdate(obj2);
            }
        });

        assertEquals(2, testRealm.allObjects(PrimaryKeyAsLong.class).size());
    }

    public void testCopyOrUpdateObject() {
        testRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
                obj.setColumnString("Foo");
                obj.setColumnLong(1);
                obj.setColumnFloat(1.23F);
                obj.setColumnDouble(1.234D);
                obj.setColumnBoolean(false);
                obj.setColumnBinary(new byte[]{1, 2, 3});
                obj.setColumnDate(new Date(1000));
                obj.setColumnRealmObject(new DogPrimaryKey(1, "Dog1"));
                obj.setColumnRealmList(new RealmList<DogPrimaryKey>(new DogPrimaryKey(2, "Dog2")));
                realm.copyToRealm(obj);

                AllTypesPrimaryKey obj2 = new AllTypesPrimaryKey();
                obj2.setColumnString("Bar");
                obj2.setColumnLong(1);
                obj2.setColumnFloat(2.23F);
                obj2.setColumnDouble(2.234D);
                obj2.setColumnBoolean(true);
                obj2.setColumnBinary(new byte[] {2, 3, 4});
                obj2.setColumnDate(new Date(2000));
                obj2.setColumnRealmObject(new DogPrimaryKey(3, "Dog3"));
                obj2.setColumnRealmList(new RealmList<DogPrimaryKey>(new DogPrimaryKey(4, "Dog4")));
                realm.copyToRealmOrUpdate(obj2);
            }
        });

        assertEquals(1, testRealm.allObjects(AllTypesPrimaryKey.class).size());
        AllTypesPrimaryKey obj = testRealm.allObjects(AllTypesPrimaryKey.class).first();

        // Check that the the only element has all its properties updated
        assertEquals("Bar", obj.getColumnString());
        assertEquals(1, obj.getColumnLong());
        assertEquals(2.23F, obj.getColumnFloat());
        assertEquals(2.234D, obj.getColumnDouble());
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{2, 3, 4}, obj.getColumnBinary());
        assertEquals(new Date(2000), obj.getColumnDate());
        assertEquals("Dog3", obj.getColumnRealmObject().getName());
        assertEquals(1, obj.getColumnRealmList().size());
        assertEquals("Dog4", obj.getColumnRealmList().get(0).getName());
    }

    public void testUpdateCyclicObject() {
        CyclicTypePrimaryKey oneCyclicType = new CyclicTypePrimaryKey(1);
        oneCyclicType.setName("One");
        CyclicTypePrimaryKey anotherCyclicType = new CyclicTypePrimaryKey(2);
        anotherCyclicType.setName("Two");
        oneCyclicType.setObject(anotherCyclicType);
        anotherCyclicType.setObject(oneCyclicType);

        testRealm.beginTransaction();
        testRealm.copyToRealm(oneCyclicType);
        testRealm.commitTransaction();

        oneCyclicType.setName("Three");
        anotherCyclicType.setName("Four");
        testRealm.beginTransaction();
        testRealm.copyToRealmOrUpdate(oneCyclicType);
        testRealm.commitTransaction();

        assertEquals(2, testRealm.allObjects(CyclicTypePrimaryKey.class).size());
        assertEquals("Three", testRealm.where(CyclicTypePrimaryKey.class).equalTo("id", 1).findFirst().getName());
    }


    // Checks that a standalone object with only default values can override data
    public void testCopyOrUpdateWithStandaloneDefaultObject() {
        testRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
                obj.setColumnString("Foo");
                obj.setColumnLong(1);
                obj.setColumnFloat(1.23F);
                obj.setColumnDouble(1.234D);
                obj.setColumnBoolean(false);
                obj.setColumnBinary(new byte[]{1, 2, 3});
                obj.setColumnDate(new Date(1000));
                obj.setColumnRealmObject(new DogPrimaryKey(1, "Dog1"));
                obj.setColumnRealmList(new RealmList<DogPrimaryKey>(new DogPrimaryKey(2, "Dog2")));
                realm.copyToRealm(obj);

                AllTypesPrimaryKey obj2 = new AllTypesPrimaryKey();
                obj2.setColumnLong(1);
                realm.copyToRealmOrUpdate(obj2);
            }
        });

        assertEquals(1, testRealm.allObjects(AllTypesPrimaryKey.class).size());

        AllTypesPrimaryKey obj = testRealm.allObjects(AllTypesPrimaryKey.class).first();
        assertEquals("", obj.getColumnString());
        assertEquals(1, obj.getColumnLong());
        assertEquals(0.0F, obj.getColumnFloat());
        assertEquals(0.0D, obj.getColumnDouble());
        assertEquals(false, obj.isColumnBoolean());
        assertArrayEquals(new byte[0], obj.getColumnBinary());
        assertEquals(new Date(0), obj.getColumnDate());
        assertNull(obj.getColumnRealmObject());
        assertEquals(0, obj.getColumnRealmList().size());
    }


    // Tests that if references to objects are removed, the objects are still in the Realm
    public void testCopyOrUpdateReferencesNotDeleted() {
        testRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
                obj.setColumnLong(1);
                obj.setColumnRealmObject(new DogPrimaryKey(1, "Dog1"));
                obj.setColumnRealmList(new RealmList<DogPrimaryKey>(new DogPrimaryKey(2, "Dog2")));
                realm.copyToRealm(obj);

                AllTypesPrimaryKey obj2 = new AllTypesPrimaryKey();
                obj2.setColumnLong(1);
                obj2.setColumnRealmObject(new DogPrimaryKey(3, "Dog3"));
                obj2.setColumnRealmList(new RealmList<DogPrimaryKey>(new DogPrimaryKey(4, "Dog4")));
                realm.copyToRealmOrUpdate(obj2);
            }
        });

        assertEquals(1, testRealm.allObjects(AllTypesPrimaryKey.class).size());
        assertEquals(4, testRealm.allObjects(DogPrimaryKey.class).size());
    }

    public void testCopyOrUpdatePrimaryKeyMix() {
        // Crate Object graph where tier 2 consists of 1 object with primary key and one doesn't.
        // Tier 3 both have objects with primary keys.
        //
        //        PK
        //     /      \
        //    PK      nonPK
        //    |        |
        //    PK       PK
        DogPrimaryKey dog = new DogPrimaryKey(1, "Dog");
        OwnerPrimaryKey owner = new OwnerPrimaryKey(1, "Owner");
        owner.setDog(dog);

        Cat cat = new Cat();
        cat.setScaredOfDog(dog);

        PrimaryKeyMix mixObject = new PrimaryKeyMix(1);
        mixObject.setDogOwner(owner);
        mixObject.setCat(cat);

        testRealm.beginTransaction();
        PrimaryKeyMix realmObject = testRealm.copyToRealmOrUpdate(mixObject);
        testRealm.commitTransaction();

        assertEquals("Dog", realmObject.getCat().getScaredOfDog().getName());
        assertEquals("Dog", realmObject.getDogOwner().getDog().getName());
    }

   public void testCopyOrUpdateIterable() {
        testRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                PrimaryKeyAsLong obj = new PrimaryKeyAsLong();
                obj.setId(1);
                obj.setName("Foo");
                realm.copyToRealm(obj);

                PrimaryKeyAsLong obj2 = new PrimaryKeyAsLong();
                obj2.setId(1);
                obj2.setName("Bar");

                PrimaryKeyAsLong obj3 = new PrimaryKeyAsLong();
                obj3.setId(1);
                obj3.setName("Baz");

                realm.copyToRealmOrUpdate(Arrays.asList(obj2, obj3));
            }
        });

        assertEquals(1, testRealm.allObjects(PrimaryKeyAsLong.class).size());
        assertEquals("Baz", testRealm.allObjects(PrimaryKeyAsLong.class).first().getName());
    }

    public void testCopyOrUpdateIterableChildObjects() {
        DogPrimaryKey dog = new DogPrimaryKey(1, "Snoop");

        AllTypesPrimaryKey allTypes1 = new AllTypesPrimaryKey();
        allTypes1.setColumnLong(1);
        allTypes1.setColumnRealmObject(dog);

        AllTypesPrimaryKey allTypes2 = new AllTypesPrimaryKey();
        allTypes1.setColumnLong(2);
        allTypes2.setColumnRealmObject(dog);

        testRealm.beginTransaction();
        testRealm.copyToRealmOrUpdate(Arrays.asList(allTypes1, allTypes2));
        testRealm.commitTransaction();

        assertEquals(2, testRealm.allObjects(AllTypesPrimaryKey.class).size());
        assertEquals(1, testRealm.allObjects(DogPrimaryKey.class).size());
    }

    private void fileCopy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    public void testOpeningOfEncryptedRealmWithDifferentKeyInstances() {
        byte[] key1 = new byte[64];
        byte[] key2 = new byte[64];
        new Random(42).nextBytes(key1);
        new Random(42).nextBytes(key2);

        // Make sure the key is the same, but in two different instances
        assertArrayEquals(key1, key2);
        assertTrue(key1 != key2);

        final String ENCRYPTED_REALM = "differentKeys.realm";
        Realm.deleteRealmFile(getContext(), ENCRYPTED_REALM);
        Realm realm1 = null;
        Realm realm2 = null;
        try {
            realm1 = Realm.getInstance(getContext(), ENCRYPTED_REALM, key1);
            try {
                realm2 = Realm.getInstance(getContext(), ENCRYPTED_REALM, key2);
            } catch (Exception e) {
                fail();
            } finally {
                if (realm2 != null) {
                    realm2.close();
                }
            }
        } finally {
            if (realm1 != null) {
                realm1.close();
            }
        }
    }

    public void testWriteEncryptedCopy() throws Exception {
        populateTestRealm();
        long before = testRealm.where(AllTypes.class).count();
        assertEquals(TEST_DATA_SIZE, before);

        final String ENCRYPTED_REALM_FILE_NAME = "encryptedTestRealm.realm";
        final String RE_ENCRYPTED_REALM_FILE_NAME = "reEncryptedTestRealm.realm";
        final String DECRYPTED_REALM_FILE_NAME = "decryptedTestRealm.realm";

        // Delete files if present
        for (String fileName : Arrays.asList(ENCRYPTED_REALM_FILE_NAME, RE_ENCRYPTED_REALM_FILE_NAME, DECRYPTED_REALM_FILE_NAME)) {
            File fileToDelete = new File(getContext().getFilesDir(), fileName);
            if (fileToDelete.exists() && !fileToDelete.delete()) {
                fail();
            }
        }

        File destination = new File(getContext().getFilesDir(), ENCRYPTED_REALM_FILE_NAME);
        byte[] key = new byte[64];
        new Random(42).nextBytes(key);
        try {
            // Unencrypted to encrypted
            testRealm.writeEncryptedCopyTo(destination, key);
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }

        Realm encryptedRealm = null;
        try {
            encryptedRealm = Realm.getInstance(getContext(), ENCRYPTED_REALM_FILE_NAME, key);
            assertEquals(TEST_DATA_SIZE, encryptedRealm.where(AllTypes.class).count());

            destination = new File(getContext().getFilesDir(), RE_ENCRYPTED_REALM_FILE_NAME);
            new Random(1234321).nextBytes(key);
            try {
                // Encrypted to encrypted
                encryptedRealm.writeEncryptedCopyTo(destination, key);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
            Realm reEncryptedRealm = null;
            try {
                reEncryptedRealm = Realm.getInstance(getContext(), RE_ENCRYPTED_REALM_FILE_NAME, key);
                assertEquals(TEST_DATA_SIZE, reEncryptedRealm.where(AllTypes.class).count());
            } finally {
                if (reEncryptedRealm != null) {
                    reEncryptedRealm.close();
                    boolean isDeleted = new File(reEncryptedRealm.getPath()).delete();
                    if (!isDeleted) {
                        fail();
                    }
                }
            }

            destination = new File(getContext().getFilesDir(), DECRYPTED_REALM_FILE_NAME);
            try {
                // Encrypted to decrypted
                encryptedRealm.writeEncryptedCopyTo(destination, null);
            } catch (Exception e) {
                fail();
            }
            Realm decryptedRealm = null;
            try {
                decryptedRealm = Realm.getInstance(getContext(), DECRYPTED_REALM_FILE_NAME);
                assertEquals(TEST_DATA_SIZE, decryptedRealm.where(AllTypes.class).count());
            } finally {
                if (decryptedRealm != null) {
                    decryptedRealm.close();
                    boolean isDeleted = new File(decryptedRealm.getPath()).delete();
                    if (!isDeleted) {
                        fail();
                    }
                }
            }
        } finally {
            if (encryptedRealm != null) {
                encryptedRealm.close();
                boolean isDeleted = new File(encryptedRealm.getPath()).delete();
                if (!isDeleted) {
                    fail();
                }
            }
        }
    }

    public void testOpenRealmFileDeletionShouldThrow() {
        final String OTHER_REALM_NAME = "yetAnotherRealm.realm";

        // This instance is already cached because of the setUp() method so this deletion should throw
        try {
            Realm.deleteRealmFile(getContext());
            fail();
        } catch (IllegalStateException ignored) {
        }

        // Create a new Realm file
        Realm yetAnotherRealm = Realm.getInstance(getContext(), OTHER_REALM_NAME);

        // Deleting it should fail
        try {
            Realm.deleteRealmFile(getContext(), OTHER_REALM_NAME);
            fail();
        } catch (IllegalStateException ignored) {
        }

        // But now that we close it deletion should work
        yetAnotherRealm.close();
        try {
            Realm.deleteRealmFile(getContext(), OTHER_REALM_NAME);
        } catch (Exception e) {
            fail();
        }
    }

    public void testWrongKeyShouldThrow() {
        final String WRONG_KEY_REALM = "wrong-key-realm.realm";
        Realm.deleteRealmFile(getContext(), WRONG_KEY_REALM);

        // Wrong key size
        try {
            Realm.getInstance(getContext(), WRONG_KEY_REALM, new byte[63]);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        Realm.getInstance(getContext(), WRONG_KEY_REALM);

        try {
            Realm.getInstance(getContext(), WRONG_KEY_REALM, new byte[64]);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    public void testUpdateObjectWithLinks() throws Exception {
        testRealm.beginTransaction();

        // Create an owner with two dogs
        OwnerPrimaryKey owner = testRealm.createObject(OwnerPrimaryKey.class);
        owner.setId(1);
        owner.setName("Jack");
        Dog rex = testRealm.createObject(Dog.class);
        rex.setName("Rex");
        Dog fido = testRealm.createObject(Dog.class);
        fido.setName("Fido");
        owner.getDogs().add(rex);
        owner.getDogs().add(fido);
        assertEquals(2, owner.getDogs().size());

        // Changing the name of the owner should not affect the number of dogs
        owner.setName("Peter");
        assertEquals(2, owner.getDogs().size());

        // Updating the user should not affect it either. This is actually a no-op since owner is a Realm backed object
        OwnerPrimaryKey owner2 = testRealm.copyToRealmOrUpdate(owner);
        assertEquals(2, owner.getDogs().size());
        assertEquals(2, owner2.getDogs().size());

        testRealm.commitTransaction();
    }

    public void testDeleteNonRealmFile() throws IOException {
        File tmpFile = new File(getContext().getFilesDir(), "tmp");
        tmpFile.delete();
        assertTrue(tmpFile.createNewFile());
        assertTrue(Realm.deleteRealmFile(tmpFile));
    }
}
