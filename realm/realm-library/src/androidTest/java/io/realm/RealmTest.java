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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.AssertionFailedError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.Cat;
import io.realm.entities.CyclicType;
import io.realm.entities.CyclicTypePrimaryKey;
import io.realm.entities.Dog;
import io.realm.entities.DogPrimaryKey;
import io.realm.entities.NoPrimaryKeyNullTypes;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.NullTypes;
import io.realm.entities.Owner;
import io.realm.entities.OwnerPrimaryKey;
import io.realm.entities.PrimaryKeyAsLong;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.entities.PrimaryKeyMix;
import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmIOException;
import io.realm.internal.Table;
import io.realm.internal.log.RealmLog;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmTest {
    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    private Context context;

    protected final static int TEST_DATA_SIZE = 10;
    protected Realm testRealm;

    protected List<String> columnData = new ArrayList<String>();

    private RealmConfiguration testConfig;

    protected void setColumnData() {
        columnData.add(0, AllTypes.FIELD_BOOLEAN);
        columnData.add(1, AllTypes.FIELD_DATE);
        columnData.add(2, AllTypes.FIELD_DOUBLE);
        columnData.add(3, AllTypes.FIELD_FLOAT);
        columnData.add(4, AllTypes.FIELD_STRING);
        columnData.add(5, AllTypes.FIELD_LONG);
    }

    @Before
    public void setUp() throws Exception {
        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        context = InstrumentationRegistry.getInstrumentation().getContext();

        testConfig = TestHelper.createConfiguration(context);
        Realm.deleteRealm(testConfig);
        testRealm = Realm.getInstance(testConfig);
    }

    @After
    public void tearDown() throws Exception {
        if (testRealm != null) {
            testRealm.close();
        }
    }

    private void populateTestRealm(Realm realm, int objects) {
        realm.beginTransaction();
        realm.allObjects(AllTypes.class).clear();
        realm.allObjects(NonLatinFieldNames.class).clear();
        for (int i = 0; i < objects; ++i) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date());
            allTypes.setColumnDouble(3.1415);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            NonLatinFieldNames nonLatinFieldNames = realm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
            nonLatinFieldNames.set베타(1.234567f + i);
            nonLatinFieldNames.setΒήτα(1.234567f + i);
        }
        realm.commitTransaction();
    }

    private void populateTestRealm() {
        populateTestRealm(testRealm, TEST_DATA_SIZE);
    }

    @Test
    public void testGetInstanceNullFolderThrows() {
        try {
            Realm.getInstance(new RealmConfiguration.Builder((File) null).build());
            fail("Parsing null as folder should throw an error");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGetInstanceFolderNoWritePermissionThrows() {
        File folder = new File("/");
        try {
            Realm.getInstance(new RealmConfiguration.Builder(folder).build());
            fail("Pointing to a folder with no write permission should throw an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGetInstanceFileNoWritePermissionThrows() throws IOException {
        String REALM_FILE = "readonly.realm";
        File folder = context.getFilesDir();
        File realmFile = new File(folder, REALM_FILE);
        if (realmFile.exists()) {
            realmFile.delete(); // Reset old test data
        }

        assertTrue(realmFile.createNewFile());
        assertTrue(realmFile.setWritable(false));

        try {
            Realm.getInstance(new RealmConfiguration.Builder(folder).name(REALM_FILE).build());
            fail("Trying to open a read-only file should fail");
        } catch (RealmIOException expected) {
        }
    }

    @Test
    public void testCheckValid() {
        // checkIfValid() must not throw any Exception against valid Realm instance.
        testRealm.checkIfValid();

        testRealm.close();
        try {
            testRealm.checkIfValid();
            fail("closed Realm instance must throw IllegalStateException.");
        } catch (IllegalStateException ignored) {
        }
        testRealm = null;
    }

    @Test
    @UiThreadTest
    public void testInternalRealmChangedHandlersRemoved() {
        testRealm.close(); // Clear handler created by testRealm in setUp()
        assertEquals(0, Realm.getHandlers().size());
        final String REALM_NAME = "test-internalhandlers";
        RealmConfiguration realmConfig = TestHelper.createConfiguration(context, REALM_NAME);
        Realm.deleteRealm(realmConfig);

        // Open and close first instance of a Realm
        Realm realm = null;
        try {
            realm = Realm.getInstance(realmConfig);
            assertFalse(testRealm == realm);
            assertEquals(1, Realm.getHandlers().size());
            realm.close();

            // All Realms closed. No handlers should be alive.
            assertEquals(0, Realm.getHandlers().size());

            // Open instance the 2nd time. Old handler should now be gone
            realm = Realm.getInstance(realmConfig);
            assertEquals(1, Realm.getHandlers().size());
            realm.close();

        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Test
    public void testShouldCreateRealm() {
        assertNotNull("Realm.getInstance unexpectedly returns null", testRealm);
        assertTrue("Realm.getInstance does not contain expected table", testRealm.contains(AllTypes.class));
    }

    @Test
    public void testShouldNotFailCreateRealmWithNullContext() {
        Realm realm = null;
        try {
            realm = Realm.getInstance((Context) null); // throws when c.getDirectory() is called;
            // has nothing to do with Realm
            fail("Should throw an exception");
        } catch (IllegalArgumentException ignored) {
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    // Table getTable(Class<?> clazz)
    @Test
    public void testShouldGetTable() {
        Table table = testRealm.getTable(AllTypes.class);
        assertNotNull(table);
    }

    // <E> void remove(Class<E> clazz, long objectIndex)
    @Test
    public void testShouldRemoveRow() {
        populateTestRealm();
        testRealm.beginTransaction();
        testRealm.remove(AllTypes.class, 0);
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE - 1, resultList.size());
    }

    // <E extends RealmObject> E get(Class<E> clazz, long rowIndex)
    @Test
    public void testShouldGetObject() {
        populateTestRealm();
        AllTypes allTypes = testRealm.get(AllTypes.class, 0);
        assertNotNull(allTypes);
        assertEquals("test data 0", allTypes.getColumnString());
    }

    // boolean contains(Class<?> clazz)
    @Test
    public void testShouldContainTable() {
        testRealm.beginTransaction();
        testRealm.createObject(Dog.class);
        testRealm.commitTransaction();
        assertTrue("contains returns false for newly created table", testRealm.contains(Dog.class));
        assertFalse("contains returns true for non-existing table", testRealm.contains(null));
    }

    // <E extends RealmObject> RealmQuery<E> where(Class<E> clazz)
    @Test
    public void testShouldReturnResultSet() {
        populateTestRealm();
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE, resultList.size());
    }

    // Note that this test is relying on the values set while initializing the test dataset
    @Test
    public void testQueriesResults() throws IOException {
        populateTestRealm(testRealm, 159);
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 33).findAll();
        assertEquals(1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 3333).findAll();
        assertEquals(0, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "test data 0").findAll();
        assertEquals(1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "test data 0", Case.INSENSITIVE).findAll();
        assertEquals(1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "Test data 0", Case.SENSITIVE).findAll();
        assertEquals(0, resultList.size());
    }

    @Test
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

    @Test
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

    @Test
    public void testQueriesFailWithNullQueryValue() throws IOException {
        // String
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NOT_NULL, (String) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Boolean
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NOT_NULL, (String) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Byte
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NOT_NULL, (Byte) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Short
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NOT_NULL, (Short) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Integer
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NOT_NULL, (Integer) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Long
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NOT_NULL, (Long) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Float
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NOT_NULL, (Float) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Double
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NOT_NULL, (Double) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Date
        try {
            testRealm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NOT_NULL, (Date) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }
    }

    // <E extends RealmObject> RealmTableOrViewList<E> allObjects(Class<E> clazz)
    @Test
    public void testShouldReturnTableOrViewList() {
        populateTestRealm();
        RealmResults<AllTypes> resultList = testRealm.allObjects(AllTypes.class);
        assertEquals("Realm.get is returning wrong result set", TEST_DATA_SIZE, resultList.size());
    }

    @Test
    public void testAllObjectsSorted() {
        populateTestRealm();
        RealmResults<AllTypes> sortedList = testRealm.allObjectsSorted(AllTypes.class, AllTypes.FIELD_STRING, Sort.ASCENDING);
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals("test data 0", sortedList.first().getColumnString());

        RealmResults<AllTypes> reverseList = testRealm.allObjectsSorted(AllTypes.class, AllTypes.FIELD_STRING, Sort.DESCENDING);
        assertEquals(TEST_DATA_SIZE, reverseList.size());
        assertEquals("test data 0", reverseList.last().getColumnString());

        try {
            RealmResults<AllTypes> none = testRealm.allObjectsSorted(AllTypes.class, "invalid", Sort.ASCENDING);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testSortTwoFields() {
        TestHelper.populateForMultiSort(testRealm);

        RealmResults<AllTypes> results1 = testRealm.allObjectsSorted(AllTypes.class,
                new String[]{AllTypes.FIELD_STRING, AllTypes.FIELD_LONG},
                new Sort[]{Sort.ASCENDING, Sort.ASCENDING});

        assertEquals(3, results1.size());

        assertEquals("Adam", results1.get(0).getColumnString());
        assertEquals(4, results1.get(0).getColumnLong());

        assertEquals("Adam", results1.get(1).getColumnString());
        assertEquals(5, results1.get(1).getColumnLong());

        assertEquals("Brian", results1.get(2).getColumnString());
        assertEquals(4, results1.get(2).getColumnLong());

        RealmResults<AllTypes> results2 = testRealm.allObjectsSorted(AllTypes.class,
                new String[]{AllTypes.FIELD_LONG, AllTypes.FIELD_STRING},
                new Sort[]{Sort.ASCENDING, Sort.ASCENDING});

        assertEquals(3, results2.size());

        assertEquals("Adam", results2.get(0).getColumnString());
        assertEquals(4, results2.get(0).getColumnLong());

        assertEquals("Brian", results2.get(1).getColumnString());
        assertEquals(4, results2.get(1).getColumnLong());

        assertEquals("Adam", results2.get(2).getColumnString());
        assertEquals(5, results2.get(2).getColumnLong());
    }

    @Test
    public void testSortMultiFailures() {
        RealmResults<AllTypes> allTypes = testRealm.allObjects(AllTypes.class);

        // zero fields specified
        try {
            testRealm.allObjectsSorted(AllTypes.class, new String[]{}, new Sort[]{});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // number of fields and sorting orders don't match
        try {
            testRealm.allObjectsSorted(AllTypes.class,
                    new String[]{AllTypes.FIELD_STRING},
                    new Sort[]{Sort.ASCENDING, Sort.ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // null is not allowed
        try {
            testRealm.allObjectsSorted(AllTypes.class, null, (Sort[]) null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            testRealm.allObjectsSorted(AllTypes.class, new String[]{AllTypes.FIELD_STRING}, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // non-existing field name
        try {
            testRealm.allObjectsSorted(AllTypes.class,
                    new String[]{AllTypes.FIELD_STRING, "dont-exist"},
                    new Sort[]{Sort.ASCENDING, Sort.ASCENDING});
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testSortSingleField() {
        populateTestRealm();
        RealmResults<AllTypes> sortedList = testRealm.allObjectsSorted(AllTypes.class,
                new String[]{AllTypes.FIELD_LONG},
                new Sort[]{Sort.DESCENDING});
        assertEquals(TEST_DATA_SIZE, sortedList.size());
        assertEquals(TEST_DATA_SIZE - 1, sortedList.first().getColumnLong());
        assertEquals(0, sortedList.last().getColumnLong());
    }

    // void beginTransaction()
    @Test
    public void testBeginTransaction() throws IOException {
        populateTestRealm();

        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnFloat(3.1415f);
        allTypes.setColumnString("a unique string");
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE + 1, resultList.size());

        resultList = testRealm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "a unique string").findAll();
        assertEquals(1, resultList.size());
        resultList = testRealm.where(AllTypes.class).equalTo(AllTypes.FIELD_FLOAT, 3.1415f).findAll();
        assertEquals(1, resultList.size());
    }

    @Test
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

    private enum Method {
        METHOD_BEGIN,
        METHOD_COMMIT,
        METHOD_CANCEL,
        METHOD_CLEAR,
        METHOD_DISTINCT,
        METHOD_CREATE_OBJECT,
        METHOD_COPY_TO_REALM,
        METHOD_COPY_TO_REALM_OR_UPDATE,
        METHOD_CREATE_ALL_FROM_JSON,
        METHOD_CREATE_OR_UPDATE_ALL_FROM_JSON,
        METHOD_CREATE_FROM_JSON,
        METHOD_CREATE_OR_UPDATE_FROM_JSON
    }

    // Calling methods on a wrong thread will fail.
    private boolean methodWrongThread(final Method method) throws InterruptedException, ExecutionException {
        if (method != Method.METHOD_BEGIN) {
            testRealm.beginTransaction();
            testRealm.createObject(Dog.class);
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
                        case METHOD_CLEAR:
                            testRealm.clear(AllTypes.class);
                            break;
                        case METHOD_DISTINCT:
                            testRealm.distinct(AllTypesPrimaryKey.class, "columnLong");
                            break;
                        case METHOD_CREATE_OBJECT:
                            testRealm.createObject(AllTypes.class);
                            break;
                        case METHOD_COPY_TO_REALM:
                            testRealm.copyToRealm(new AllTypes());
                            break;
                        case METHOD_COPY_TO_REALM_OR_UPDATE:
                            testRealm.copyToRealm(new AllTypesPrimaryKey());
                            break;
                        case METHOD_CREATE_ALL_FROM_JSON:
                            testRealm.createAllFromJson(AllTypes.class, "[{}]");
                            break;
                        case METHOD_CREATE_OR_UPDATE_ALL_FROM_JSON:
                            testRealm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, "[{\"columnLong\":1}]");
                            break;
                        case METHOD_CREATE_FROM_JSON:
                            testRealm.createObjectFromJson(AllTypes.class, "{}");
                            break;
                        case METHOD_CREATE_OR_UPDATE_FROM_JSON:
                            testRealm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, "{\"columnLong\":1}");
                            break;
                    }
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                } catch (RealmException jsonFailure) {
                    // TODO: Eew. Reconsider how our JSON methods reports failure. See https://github.com/realm/realm-java/issues/1594
                    return (jsonFailure.getMessage().equals("Could not map Json"));
                }
            }
        });

        boolean result = future.get();
        if (method != Method.METHOD_BEGIN) {
            testRealm.cancelTransaction();
        }
        return result;
    }

    @Test
    public void testMethodsThrowOnWrongThread() throws ExecutionException, InterruptedException {
        for (Method method : Method.values()) {
            assertTrue(method.toString(), methodWrongThread(method));
        }
    }

    // void commitTransaction()
    @Test
    public void testCommitTransaction() {
        populateTestRealm();

        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnBoolean(true);
        testRealm.commitTransaction();

        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE + 1, resultList.size());
    }

    @Test
    public void testCommitTransactionAfterCancelTransaction() {
        testRealm.beginTransaction();
        testRealm.cancelTransaction();
        try {
            testRealm.commitTransaction();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void testDoubleCommitThrows() {
        testRealm.beginTransaction();
        testRealm.commitTransaction();
        try {
            testRealm.commitTransaction();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
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


    @Test
    public void testExecuteTransactionNull() {
        try {
            testRealm.executeTransaction(null);
            fail("null transaction should throw");
        } catch (IllegalArgumentException ignored) {

        }
        assertFalse(testRealm.hasChanged());
    }

    @Test
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

    @Test
    public void testExecuteTransactionCancel() {
        final AtomicReference<RuntimeException> thrownException = new AtomicReference<>(null);

        assertEquals(0, testRealm.allObjects(Owner.class).size());
        try {
            testRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Owner owner = realm.createObject(Owner.class);
                    owner.setName("Owner");
                    thrownException.set(new RuntimeException("Boom"));
                    throw thrownException.get();
                }
            });
        } catch (RuntimeException e) {
            //noinspection ThrowableResultOfMethodCallIgnored
            assertTrue(e == thrownException.get());
        }
        assertEquals(0, testRealm.allObjects(Owner.class).size());
    }

    @Test
    public void testExecuteTransactionCancelledInExecuteThrowsRuntimeException() {
        assertEquals(0, testRealm.allObjects(Owner.class).size());
        TestHelper.TestLogger testLogger = new TestHelper.TestLogger();
        try {
            RealmLog.add(testLogger);
            testRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Owner owner = realm.createObject(Owner.class);
                    owner.setName("Owner");
                    realm.cancelTransaction();
                    throw new RuntimeException("Boom");
                }
            });
        } catch (RuntimeException ignored) {
            // Ensure that we pass a valuable error message to the logger for developers.
            assertEquals(testLogger.message, "Could not cancel transaction, not currently in a transaction.");
        } finally {
            RealmLog.remove(testLogger);
        }
        assertEquals(0, testRealm.allObjects(Owner.class).size());
    }

    // void clear(Class<?> classSpec)
    @Test
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

    @Test
    public void testShouldFailOutsideTransaction() {
        // These calls should fail outside a Transaction:
        try {
            testRealm.createObject(AllTypes.class);
            fail("Realm.createObject should fail outside transaction");
        } catch (IllegalStateException ignored) {
        }
        try {
            testRealm.remove(AllTypes.class, 0);
            fail("Realm.remove should fail outside transaction");
        } catch (IllegalStateException ignored) {
        }
    }

    private void createAndTestFilename(String language, String fileName) {
        RealmConfiguration realmConfig = TestHelper.createConfiguration(context, fileName);
        Realm.deleteRealm(realmConfig);
        Realm realm1 = Realm.getInstance(realmConfig);
        realm1.beginTransaction();
        Dog dog1 = realm1.createObject(Dog.class);
        dog1.setName("Rex");
        realm1.commitTransaction();
        realm1.close();

        File file = new File(context.getFilesDir(), fileName);
        assertTrue(language, file.exists());

        Realm realm2 = Realm.getInstance(realmConfig);
        Dog dog2 = realm2.allObjects(Dog.class).first();
        assertEquals(language, "Rex", dog2.getName());
        realm2.close();
    }

    @Test
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

    @Test
    public void testUTF8() {
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

    // This test is slow. Move it to another testsuite that runs once a day on Jenkins.
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

            testRealm.allObjects(StringOnly.class).get(0).getChars();

            testRealm.beginTransaction();
            testRealm.clear(StringOnly.class);
            testRealm.commitTransaction();
        }
    }

    @Test
    public void testReferenceCounting() {
        // At this point reference count should be one because of the setUp method
        try {
            testRealm.where(AllTypes.class).count();
        } catch (IllegalStateException e) {
            fail();
        }

        // Make sure the reference counter is per realm file
        RealmConfiguration anotherConfig = TestHelper.createConfiguration(context, "anotherRealm.realm");
        Realm.deleteRealm(anotherConfig);
        Realm otherRealm = Realm.getInstance(anotherConfig);

        // Raise the reference
        Realm realm = null;
        try {
            realm = Realm.getInstance(context);
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

    @Test
    public void testReferenceCountingDoubleClose() {
        testRealm.close();
        testRealm.close(); // Count down once too many. Counter is now potentially negative
        testRealm = Realm.getInstance(context);
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

    @Test
    public void testWriteCopyTo() throws IOException {
        RealmConfiguration configA = TestHelper.createConfiguration(context, "file1.realm");
        RealmConfiguration configB = TestHelper.createConfiguration(context, "file2.realm");
        Realm.deleteRealm(configA);
        Realm.deleteRealm(configB);

        Realm realm1 = null;
        try {
            realm1 = Realm.getInstance(configA);
            realm1.beginTransaction();
            AllTypes allTypes = realm1.createObject(AllTypes.class);
            allTypes.setColumnString("Hello World");
            realm1.commitTransaction();

            realm1.writeCopyTo(new File(context.getFilesDir(), "file2.realm"));
        } finally {
            if (realm1 != null) {
                realm1.close();
            }
        }

        // Copy is compacted i.e. smaller than original
        File file1 = new File(context.getFilesDir(), "file1.realm");
        File file2 = new File(context.getFilesDir(), "file2.realm");
        assertTrue(file1.length() >= file2.length());

        Realm realm2 = null;
        try {
            // Contents is copied too
            realm2 = Realm.getInstance(configB);
            RealmResults<AllTypes> results = realm2.allObjects(AllTypes.class);
            assertEquals(1, results.size());
            assertEquals("Hello World", results.first().getColumnString());
        } finally {
            if (realm2 != null) {
                realm2.close();
            }
        }
    }


    @Test
    public void testGetRealmAfterCompactRealm() {
        final RealmConfiguration configuration = testRealm.getConfiguration();
        testRealm.close();
        testRealm = null;
        assertTrue(Realm.compactRealm(configuration));
        testRealm = Realm.getInstance(configuration);
    }

    @Test
    public void testCompactRealmFileFailsIfOpen() throws IOException {
        assertFalse(Realm.compactRealm(TestHelper.createConfiguration(context)));
    }

    @Test
    public void testCompactEncryptedEmptyRealmFile() {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context)
                .name("enc.realm")
                .encryptionKey(TestHelper.getRandomKey())
                .build();
        Realm.deleteRealm(realmConfig);
        Realm realm = Realm.getInstance(realmConfig);
        realm.close();
        // TODO: remove try/catch block when compacting encrypted Realms is supported
        try {
            assertTrue(Realm.compactRealm(realmConfig));
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testCompactEncryptedPopulatedRealmFile() {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context)
                .name("enc.realm")
                .encryptionKey(TestHelper.getRandomKey())
                .build();
        Realm.deleteRealm(realmConfig);
        Realm realm = Realm.getInstance(realmConfig);

        populateTestRealm(realm, 100);
        realm.close();
        // TODO: remove try/catch block when compacting encrypted Realms is supported
        try {
            assertTrue(Realm.compactRealm(realmConfig));
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testCompactEmptyRealmFile() throws IOException {
        final String REALM_NAME = "test.realm";
        RealmConfiguration realmConfig = TestHelper.createConfiguration(context, REALM_NAME);
        Realm.deleteRealm(realmConfig);
        Realm realm = Realm.getInstance(realmConfig);
        realm.close();
        long before = new File(context.getFilesDir(), REALM_NAME).length();
        assertTrue(Realm.compactRealm(realmConfig));
        long after = new File(context.getFilesDir(), REALM_NAME).length();
        assertTrue(before >= after);
    }

    @Test
    public void testCompactPopulateRealmFile() throws IOException {
        final String REALM_NAME = "test.realm";
        RealmConfiguration realmConfig = TestHelper.createConfiguration(context, REALM_NAME);
        Realm.deleteRealm(realmConfig);
        Realm realm = Realm.getInstance(realmConfig);
        populateTestRealm(realm, 100);
        realm.close();
        long before = new File(context.getFilesDir(), REALM_NAME).length();
        assertTrue(Realm.compactRealm(realmConfig));
        long after = new File(context.getFilesDir(), REALM_NAME).length();
        assertTrue(before >= after);
    }

    @Test
    public void testCopyToRealmNullObjectThrows() {
        testRealm.beginTransaction();
        try {
            testRealm.copyToRealm((AllTypes) null);
            fail("Copying null objects into Realm should not be allowed");
        } catch (IllegalArgumentException ignored) {
        } finally {
            testRealm.cancelTransaction();
        }
    }

    @Test
    public void testCopyManagedObjectIsNoop() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnString("Test");
        testRealm.commitTransaction();

        testRealm.beginTransaction();
        AllTypes copiedAllTypes = testRealm.copyToRealm(allTypes);
        testRealm.commitTransaction();

        assertTrue(allTypes == copiedAllTypes);
    }

    @Test
    public void testCopManagedObjectToOtherRealm() {
        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnString("Test");
        testRealm.commitTransaction();

        RealmConfiguration realmConfig = TestHelper.createConfiguration(context, "other-realm");
        Realm.deleteRealm(realmConfig);
        Realm otherRealm = Realm.getInstance(realmConfig);
        otherRealm.beginTransaction();
        AllTypes copiedAllTypes = otherRealm.copyToRealm(allTypes);
        otherRealm.commitTransaction();

        assertNotSame(allTypes, copiedAllTypes); // Same object in different Realms is not the same
        assertEquals(allTypes.getColumnString(), copiedAllTypes.getColumnString()); // But data is still the same
        otherRealm.close();
    }

    @Test
    public void testCopyToRealmObject() {
        Date date = new Date();
        date.setTime(1000); // Remove ms. precision as Realm doesn't support it yet.
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
        assertEquals(allTypes.getColumnFloat(), realmTypes.getColumnFloat(), 0);
        assertEquals(allTypes.getColumnDouble(), realmTypes.getColumnDouble(), 0);
        assertEquals(allTypes.isColumnBoolean(), realmTypes.isColumnBoolean());
        assertEquals(allTypes.getColumnDate(), realmTypes.getColumnDate());
        assertArrayEquals(allTypes.getColumnBinary(), realmTypes.getColumnBinary());
        assertEquals(allTypes.getColumnRealmObject().getName(), dog.getName());
        assertEquals(list.size(), realmTypes.getColumnRealmList().size());
        assertEquals(list.get(0).getName(), realmTypes.getColumnRealmList().get(0).getName());
    }

    @Test
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

    @Test
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
    @Test
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
    @Test
    public void testCopyToRealmWithPrimaryKeySetValueDirectly() {
        testRealm.beginTransaction();
        testRealm.createObject(OwnerPrimaryKey.class);
        testRealm.copyToRealm(new OwnerPrimaryKey(1, "Foo"));
        testRealm.commitTransaction();
        assertEquals(2, testRealm.where(OwnerPrimaryKey.class).count());
    }

    @Test
    public void testCopyToRealmWithPrimaryAsNullThrows() {
        testRealm.beginTransaction();
        try {
            testRealm.copyToRealm(new PrimaryKeyAsString());
            fail();
        } catch (IllegalArgumentException expected) {
        } finally {
            testRealm.cancelTransaction();
        }
    }

    @Test
    public void testCopyToRealmDontCopyNestedRealmObjects() {
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

    @Test
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

    @Test
    public void testCopyToRealmOrUpdateNullThrows() {
        try {
            testRealm.copyToRealmOrUpdate((AllTypes) null);
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    @Test
    public void testCopyToRealmOrUpdateNullPrimaryKeyThrows() {
        testRealm.beginTransaction();
        try {
            testRealm.copyToRealmOrUpdate(new PrimaryKeyAsString());
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testCopyOrUpdateNoPrimaryKeyThrows() {
        try {
            testRealm.copyToRealmOrUpdate(new AllTypes());
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail();
    }

    @Test
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

    @Test
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
                obj.setColumnBoxedBoolean(true);
                realm.copyToRealm(obj);

                AllTypesPrimaryKey obj2 = new AllTypesPrimaryKey();
                obj2.setColumnString("Bar");
                obj2.setColumnLong(1);
                obj2.setColumnFloat(2.23F);
                obj2.setColumnDouble(2.234D);
                obj2.setColumnBoolean(true);
                obj2.setColumnBinary(new byte[]{2, 3, 4});
                obj2.setColumnDate(new Date(2000));
                obj2.setColumnRealmObject(new DogPrimaryKey(3, "Dog3"));
                obj2.setColumnRealmList(new RealmList<DogPrimaryKey>(new DogPrimaryKey(4, "Dog4")));
                obj2.setColumnBoxedBoolean(false);
                realm.copyToRealmOrUpdate(obj2);
            }
        });

        assertEquals(1, testRealm.allObjects(AllTypesPrimaryKey.class).size());
        AllTypesPrimaryKey obj = testRealm.allObjects(AllTypesPrimaryKey.class).first();

        // Check that the the only element has all its properties updated
        assertEquals("Bar", obj.getColumnString());
        assertEquals(1, obj.getColumnLong());
        assertEquals(2.23F, obj.getColumnFloat(), 0);
        assertEquals(2.234D, obj.getColumnDouble(), 0);
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{2, 3, 4}, obj.getColumnBinary());
        assertEquals(new Date(2000), obj.getColumnDate());
        assertEquals("Dog3", obj.getColumnRealmObject().getName());
        assertEquals(1, obj.getColumnRealmList().size());
        assertEquals("Dog4", obj.getColumnRealmList().get(0).getName());
        assertFalse(obj.getColumnBoxedBoolean());
    }

    @Test
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
    @Test
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
        assertNull(obj.getColumnString());
        assertEquals(1, obj.getColumnLong());
        assertEquals(0.0F, obj.getColumnFloat(), 0);
        assertEquals(0.0D, obj.getColumnDouble(), 0);
        assertEquals(false, obj.isColumnBoolean());
        assertNull(obj.getColumnBinary());
        assertNull(obj.getColumnDate());
        assertNull(obj.getColumnRealmObject());
        assertEquals(0, obj.getColumnRealmList().size());
    }


    // Tests that if references to objects are removed, the objects are still in the Realm
    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testOpeningOfEncryptedRealmWithDifferentKeyInstances() {
        byte[] key1 = TestHelper.getRandomKey(42);
        byte[] key2 = TestHelper.getRandomKey(42);

        // Make sure the key is the same, but in two different instances
        assertArrayEquals(key1, key2);
        assertTrue(key1 != key2);

        final String ENCRYPTED_REALM = "differentKeys.realm";
        Realm.deleteRealm(TestHelper.createConfiguration(context, ENCRYPTED_REALM));
        Realm realm1 = null;
        Realm realm2 = null;
        try {
            realm1 = Realm.getInstance(new RealmConfiguration.Builder(context)
                            .name(ENCRYPTED_REALM)
                            .encryptionKey(key1)
                            .build()
            );
            try {
                realm2 = Realm.getInstance(new RealmConfiguration.Builder(context)
                                .name(ENCRYPTED_REALM)
                                .encryptionKey(key2)
                                .build()
                );
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

    @Test
    public void testWriteEncryptedCopy() throws Exception {
        populateTestRealm();
        long before = testRealm.where(AllTypes.class).count();
        assertEquals(TEST_DATA_SIZE, before);

        // Configure test realms
        final String ENCRYPTED_REALM_FILE_NAME = "encryptedTestRealm.realm";
        final String RE_ENCRYPTED_REALM_FILE_NAME = "reEncryptedTestRealm.realm";
        final String DECRYPTED_REALM_FILE_NAME = "decryptedTestRealm.realm";

        RealmConfiguration encryptedRealmConfig = new RealmConfiguration.Builder(context)
                .name(ENCRYPTED_REALM_FILE_NAME)
                .encryptionKey(TestHelper.getRandomKey())
                .build();

        RealmConfiguration reEncryptedRealmConfig = new RealmConfiguration.Builder(context)
                .name(RE_ENCRYPTED_REALM_FILE_NAME)
                .encryptionKey(TestHelper.getRandomKey())
                .build();

        RealmConfiguration decryptedRealmConfig = new RealmConfiguration.Builder(context)
                .name(DECRYPTED_REALM_FILE_NAME)
                .build();

        // Delete old test Realms if present
        for (RealmConfiguration realmConfig : Arrays.asList(encryptedRealmConfig, reEncryptedRealmConfig, decryptedRealmConfig)) {
            if (!Realm.deleteRealm(realmConfig)) {
                fail();
            }
        }

        // Write encrypted copy from a unencrypted Realm
        File destination = new File(context.getFilesDir(), ENCRYPTED_REALM_FILE_NAME);
        try {
            testRealm.writeEncryptedCopyTo(destination, encryptedRealmConfig.getEncryptionKey());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Realm encryptedRealm = null;
        try {

            // Verify encrypted Realm and write new encrypted copy with a new key
            encryptedRealm = Realm.getInstance(encryptedRealmConfig);
            assertEquals(TEST_DATA_SIZE, encryptedRealm.where(AllTypes.class).count());

            destination = new File(reEncryptedRealmConfig.getPath());
            try {
                encryptedRealm.writeEncryptedCopyTo(destination, reEncryptedRealmConfig.getEncryptionKey());
            } catch (Exception e) {
                fail(e.getMessage());
            }

            // Verify re-encrypted copy
            Realm reEncryptedRealm = null;
            try {
                reEncryptedRealm = Realm.getInstance(reEncryptedRealmConfig);
                assertEquals(TEST_DATA_SIZE, reEncryptedRealm.where(AllTypes.class).count());
            } finally {
                if (reEncryptedRealm != null) {
                    reEncryptedRealm.close();
                    if (!Realm.deleteRealm(reEncryptedRealmConfig)) {
                        fail();
                    }
                }
            }

            // Write non-encrypted copy from the encrypted version
            destination = new File(decryptedRealmConfig.getPath());
            try {
                encryptedRealm.writeEncryptedCopyTo(destination, null);
            } catch (Exception e) {
                fail(e.getMessage());
            }

            // Verify decrypted Realm and cleanup
            Realm decryptedRealm = null;
            try {
                decryptedRealm = Realm.getInstance(decryptedRealmConfig);
                assertEquals(TEST_DATA_SIZE, decryptedRealm.where(AllTypes.class).count());
            } finally {
                if (decryptedRealm != null) {
                    decryptedRealm.close();
                    if (!Realm.deleteRealm(decryptedRealmConfig)) {
                        fail();
                    }
                }
            }
        } finally {
            if (encryptedRealm != null) {
                encryptedRealm.close();
                if (!Realm.deleteRealm(encryptedRealmConfig)) {
                    fail();
                }
            }
        }
    }

    @Test
    public void testOpenRealmFileDeletionShouldThrow() {
        final String OTHER_REALM_NAME = "yetAnotherRealm.realm";

        RealmConfiguration configA = TestHelper.createConfiguration(context);
        RealmConfiguration configB = TestHelper.createConfiguration(context, OTHER_REALM_NAME);

        // This instance is already cached because of the setUp() method so this deletion should throw
        try {
            Realm.deleteRealm(configA);
            fail();
        } catch (IllegalStateException ignored) {
        }

        // Create a new Realm file
        Realm yetAnotherRealm = Realm.getInstance(configB);

        // Deleting it should fail
        try {
            Realm.deleteRealm(configB);
            fail();
        } catch (IllegalStateException ignored) {
        }

        // But now that we close it deletion should work
        yetAnotherRealm.close();
        try {
            Realm.deleteRealm(configB);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
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

    @Test
    public void testDeleteNonRealmFile() throws IOException {
        File tmpFile = new File(context.getFilesDir(), "tmp");
        tmpFile.delete();
        assertTrue(tmpFile.createNewFile());
    }

    @Test
    public void testDeleteRealmFile() throws InterruptedException {
        File tempDir = new File(context.getFilesDir(), "delete_test_dir");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        assertTrue(tempDir.isDirectory());

        // Delete all files in the directory
        for (File file : tempDir.listFiles()) {
            file.delete();
        }

        final RealmConfiguration configuration = new RealmConfiguration.Builder(tempDir).build();

        final CountDownLatch readyToCloseLatch = new CountDownLatch(1);
        final CountDownLatch closedLatch = new CountDownLatch(1);

        Realm realm = Realm.getInstance(configuration);
        // Create another Realm to ensure the log files are generated
        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(configuration);
                try {
                    readyToCloseLatch.await();
                } catch (InterruptedException ignored) {
                }
                realm.close();
                closedLatch.countDown();
            }
        }).start();

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
        readyToCloseLatch.countDown();
        realm.close();
        closedLatch.await();

        // ATTENTION: log, log_a, log_b will be deleted when the other thread close the Realm peacefully. And we force
        // user to close all Realm instances before deleting. It would be difficult to simulate a case that log files
        // exist before deletion. Let's keep the case like this for now, we might allow user to delete Realm even there
        // are instances opened in the future.
        assertTrue(Realm.deleteRealm(configuration));

        // Directory should be empty now
        assertEquals(0, tempDir.listFiles().length);
    }

    // Test that all methods that require a transaction (ie. any function that mutates Realm data)
    @Test
    public void testMutableMethodsOutsideTransactions() throws JSONException, IOException {

        // Prepare standalone object data
        AllTypesPrimaryKey t = new AllTypesPrimaryKey();
        List<AllTypesPrimaryKey> ts = Arrays.asList(t, t);

        // Prepare JSON data
        String jsonObjStr = "{ \"columnLong\" : 1 }";
        JSONObject jsonObj = new JSONObject(jsonObjStr);
        InputStream jsonObjStream = TestHelper.stringToStream(jsonObjStr);
        InputStream jsonObjStream2 = TestHelper.stringToStream(jsonObjStr);

        String jsonArrStr = " [{ \"columnLong\" : 1 }] ";
        JSONArray jsonArr = new JSONArray(jsonArrStr);
        InputStream jsonArrStream = TestHelper.stringToStream(jsonArrStr);
        InputStream jsonArrStream2 = TestHelper.stringToStream(jsonArrStr);

        // Test all methods that should require a transaction
        try { testRealm.createObject(AllTypes.class);   fail(); } catch (IllegalStateException expected) {}
        try { testRealm.copyToRealm(t);                 fail(); } catch (IllegalStateException expected) {}
        try { testRealm.copyToRealm(ts);                fail(); } catch (IllegalStateException expected) {}
        try { testRealm.copyToRealmOrUpdate(t);         fail(); } catch (IllegalStateException expected) {}
        try { testRealm.copyToRealmOrUpdate(ts);        fail(); } catch (IllegalStateException expected) {}
        try { testRealm.remove(AllTypes.class, 0);      fail(); } catch (IllegalStateException expected) {}
        try { testRealm.clear(AllTypes.class);          fail(); } catch (IllegalStateException expected) {}

        try { testRealm.createObjectFromJson(AllTypesPrimaryKey.class, jsonObj);                fail(); } catch (RealmException expected) {}
        try { testRealm.createObjectFromJson(AllTypesPrimaryKey.class, jsonObjStr);             fail(); } catch (RealmException expected) {}
        try { testRealm.createObjectFromJson(NoPrimaryKeyNullTypes.class, jsonObjStream);          fail(); } catch (IllegalStateException expected) {}
        try { testRealm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, jsonObj);        fail(); } catch (IllegalStateException expected) {}
        try { testRealm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, jsonObjStr);     fail(); } catch (IllegalStateException expected) {}
        try { testRealm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, jsonObjStream2); fail(); } catch (IllegalStateException expected) {}

        try { testRealm.createAllFromJson(AllTypesPrimaryKey.class, jsonArr);                   fail(); } catch (RealmException expected) {}
        try { testRealm.createAllFromJson(AllTypesPrimaryKey.class, jsonArrStr);                fail(); } catch (RealmException expected) {}
        try { testRealm.createAllFromJson(NoPrimaryKeyNullTypes.class, jsonArrStream);             fail(); } catch (IllegalStateException expected) {}
        try { testRealm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, jsonArr);           fail(); } catch (RealmException expected) {}
        try { testRealm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, jsonArrStr);        fail(); } catch (RealmException expected) {}
        try { testRealm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, jsonArrStream2);    fail(); } catch (IllegalStateException expected) {}
    }

    // TODO: re-introduce this test mocking the ReferenceQueue instead of relying on the GC
/*    // Check that FinalizerRunnable can free native resources (phantom refs)
    public void testReferenceCleaning() throws NoSuchFieldException, IllegalAccessException {
        testRealm.close();

        RealmConfiguration config = new RealmConfiguration.Builder(getContext()).name("myown").build();
        Realm.deleteRealm(config);
        testRealm = Realm.getInstance(config);

        // Manipulate field accessibility to facilitate testing
        Field realmFileReference = BaseRealm.class.getDeclaredField("sharedGroupManager");
        realmFileReference.setAccessible(true);
        Field contextField = SharedGroup.class.getDeclaredField("context");
        contextField.setAccessible(true);
        Field rowReferencesField = io.realm.internal.Context.class.getDeclaredField("rowReferences");
        rowReferencesField.setAccessible(true);

        SharedGroupManager realmFile = (SharedGroupManager) realmFileReference.get(testRealm);
        assertNotNull(realmFile);

        io.realm.internal.Context context = (io.realm.internal.Context) contextField.get(realmFile.getSharedGroup());
        assertNotNull(context);

        Map<Reference<?>, Integer> rowReferences = (Map<Reference<?>, Integer>) rowReferencesField.get(context);
        assertNotNull(rowReferences);

        // insert some rows, then give the thread some time to cleanup
        // we have 8 reference so far let's add more
        final int numberOfPopulateTest = 1000;
        final int numberOfObjects = 20;
        final int totalNumberOfReferences = 8 + numberOfObjects * 2 * numberOfPopulateTest;

        long tic = System.currentTimeMillis();
        for (int i = 0; i < numberOfPopulateTest; i++) {
            populateTestRealm(testRealm, numberOfObjects);
        }
        long toc = System.currentTimeMillis();
        Log.d(RealmTest.class.getName(), "Insertion time: " + (toc - tic));

        final int MAX_GC_RETRIES = 5;
        int numberOfRetries = 0;
        Log.i("GCing", "Hoping for the best");
        while (rowReferences.size() > 0 && numberOfRetries < MAX_GC_RETRIES) {
            SystemClock.sleep(TimeUnit.SECONDS.toMillis(1)); //1s
            TestHelper.allocGarbage(0);
            numberOfRetries++;
            System.gc();
        }
        context.cleanNativeReferences();

        // we can't guarantee that all references have been GC'ed but we should detect a decrease
        boolean isDecreasing = rowReferences.size() < totalNumberOfReferences;
        if (!isDecreasing) {
            fail("Native resources are not being closed");

        } else {
            android.util.Log.d(RealmTest.class.getName(), "References freed : "
                    + (totalNumberOfReferences - rowReferences.size()) + " out of " + totalNumberOfReferences);
        }
    }*/

    @Test
    public void testCannotCreateDynamicRealmObject() {
        testRealm.beginTransaction();
        try {
            testRealm.createObject(DynamicRealmObject.class);
            fail();
        } catch (RealmException ignored) {
        }
    }

    // Test close Realm in another thread different from where it is created.
    @Test
    public void testCloseRealmInDifferentThread() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AssertionFailedError threadAssertionError[] = new AssertionFailedError[1];

        final Thread thatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    testRealm.close();
                    threadAssertionError[0] = new AssertionFailedError(
                            "Close realm in a different thread should throw IllegalStateException.");
                } catch (IllegalStateException ignored) {
                }
                latch.countDown();
            }
        });
        thatThread.start();

        // Timeout should never happen
        latch.await();
        if (threadAssertionError[0] != null) {
            throw threadAssertionError[0];
        }
        // After exception thrown in another thread, nothing should be changed to the realm in this thread.
        testRealm.checkIfValid();
        testRealm.close();
        testRealm = null;
    }

    @Test
    public void testRealmIsClosed() {
        assertFalse(testRealm.isClosed());
        testRealm.close();
        assertTrue(testRealm.isClosed());
    }

    // Test Realm#isClosed() in another thread different from where it is created.
    @Test
    public void testRealmIsClosedInDifferentThread() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AssertionFailedError threadAssertionError[] = new AssertionFailedError[1];

        final Thread thatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    testRealm.isClosed();
                    threadAssertionError[0] = new AssertionFailedError(
                            "Call isClosed() of Realm instance in a different thread should throw IllegalStateException.");
                } catch (IllegalStateException ignored) {
                }
                latch.countDown();
            }
        });
        thatThread.start();

        // Timeout should never happen
        latch.await();
        if (threadAssertionError[0] != null) {
            throw threadAssertionError[0];
        }
        // After exception thrown in another thread, nothing should be changed to the realm in this thread.
        testRealm.checkIfValid();
        assertFalse(testRealm.isClosed());
        testRealm.close();
    }

    // Realm validation & initialization is done once, still ColumnIndices
    // should be populated for the subsequent Realm sharing the same configuration
    // even if we skip initialization & validation
    @Test
    public void testColumnIndicesIsPopulatedWhenSkippingInitialization() throws Throwable {
        final RealmConfiguration realmConfiguration = TestHelper.createConfiguration(context, "columnIndices");
        Realm.deleteRealm(realmConfiguration);
        final Exception threadError[] = new Exception[1];
        final CountDownLatch bgRealmOpened = new CountDownLatch(1);
        final CountDownLatch mainThreadRealmDone = new CountDownLatch(1);
        final CountDownLatch bgRealmClosed = new CountDownLatch(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfiguration); // This will populate columnIndices
                try {
                    bgRealmOpened.countDown();
                    awaitOrFail(mainThreadRealmDone);
                    realm.close();
                    bgRealmClosed.countDown();
                } catch (Exception e) {
                    threadError[0] = e;
                } finally {
                    if (!realm.isClosed()) {
                        realm.close();
                    }
                }
            }
        }).start();

        awaitOrFail(bgRealmOpened);
        Realm realm = Realm.getInstance(realmConfiguration);
        realm.where(AllTypes.class).equalTo("columnString", "Foo").findAll(); // This would crash if columnIndices == null
        realm.close();
        mainThreadRealmDone.countDown();
        awaitOrFail(bgRealmClosed);
        if (threadError[0] != null) {
            throw threadError[0];
        }
    }

    @Test
    public void testProcessLocalListenersAfterRefresh() throws InterruptedException {
        // Used to validate the result
        final AtomicBoolean listenerWasCalled = new AtomicBoolean(false);
        final AtomicBoolean typeListenerWasCalled = new AtomicBoolean(false);

        // Used by the background thread to wait for the main thread to do the write operation
        final CountDownLatch bgThreadLatch = new CountDownLatch(1);
        final CountDownLatch bgClosedLatch = new CountDownLatch(1);

        Thread backgroundThread = new Thread() {
            @Override
            public void run() {
                // this will allow to register a listener.
                // we don't start looping to prevent the callback to be invoked via
                // the handler mechanism, the purpose of this test is to make sure refresh calls
                // the listeners.
                Looper.prepare();

                Realm bgRealm = Realm.getInstance(testConfig);
                RealmResults<Dog> dogs = bgRealm.where(Dog.class).findAll();
                try {
                    bgRealm.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            listenerWasCalled.set(true);
                        }
                    });
                    dogs.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            typeListenerWasCalled.set(true);
                        }
                    });

                    bgThreadLatch.await(); // Wait for the main thread to do a write operation
                    bgRealm.refresh(); // This should call the listener
                    assertTrue(listenerWasCalled.get());
                    assertTrue(typeListenerWasCalled.get());
                } catch (InterruptedException e) {
                    fail();
                } finally {
                    bgRealm.close();
                    bgClosedLatch.countDown();
                }
            }
        };
        backgroundThread.start();

        testRealm.beginTransaction();
        testRealm.createObject(Dog.class);
        testRealm.commitTransaction();
        bgThreadLatch.countDown();
        bgClosedLatch.await();
    }

    private void awaitOrFail(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                fail();
            }
        } catch (InterruptedException e) {
            fail();
        }
    }

    private void populateForDistinct(Realm realm, long numberOfBlocks, long numberOfObjects, boolean withNull) {
        realm.beginTransaction();
        for (int i = 0; i < numberOfObjects * numberOfBlocks; i++) {
            for (int j = 0; j < numberOfBlocks; j++) {
                AnnotationIndexTypes obj = realm.createObject(AnnotationIndexTypes.class);
                obj.setIndexBoolean(j % 2 == 0);
                obj.setIndexLong(j);
                obj.setIndexDate(withNull ? null : new Date(1000 * j));
                obj.setIndexString(withNull ? null : "Test " + j);
                obj.setNotIndexBoolean(j % 2 == 0);
                obj.setNotIndexLong(j);
                obj.setNotIndexDate(withNull ? null : new Date(1000 * j));
                obj.setNotIndexString(withNull ? null : "Test " + j);
            }
        }
        realm.commitTransaction();
    }

    // Realm.distinct(): requires indexing, and type = boolean, integer, date, string
    @Test
    public void testDistinct() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1

        populateForDistinct(testRealm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AnnotationIndexTypes> distinctBool = testRealm.distinct(AnnotationIndexTypes.class, "indexBoolean");
        assertEquals(2, distinctBool.size());

        for (String fieldName : new String[]{"Long", "Date", "String"}) {
            RealmResults<AnnotationIndexTypes> distinct = testRealm.distinct(AnnotationIndexTypes.class, "index" + fieldName);
            assertEquals("index" + fieldName, numberOfBlocks, distinct.size());
        }
    }

    @Test
    public void testDistinctWithNull() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1

        populateForDistinct(testRealm, numberOfBlocks, numberOfObjects, true);

        for (String fieldName : new String[]{"Date", "String"}) {
            RealmResults<AnnotationIndexTypes> distinct = testRealm.distinct(AnnotationIndexTypes.class, "index" + fieldName);
            assertEquals("index" + fieldName, 1, distinct.size());
        }
    }

    @Test
    public void testDistinctNotIndexedFields() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1

        populateForDistinct(testRealm, numberOfBlocks, numberOfObjects, false);

        for (String fieldName : new String[]{"Boolean", "Long", "Date", "String"}) {
            try {
                testRealm.distinct(AnnotationIndexTypes.class, "notIndex" + fieldName);
                fail("notIndex" + fieldName);
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }

    @Test
    public void testDistinctDoesNotExist() {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1

        populateForDistinct(testRealm, numberOfBlocks, numberOfObjects, false);

        try {
            testRealm.distinct(AnnotationIndexTypes.class, "doesNotExist");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testDistinctInvalidTypes() {
        populateTestRealm();

        for (String field : new String[]{"columnRealmObject", "columnRealmList", "columnDouble", "columnFloat"}) {
            try {
                testRealm.distinct(AllTypes.class, field);
                fail(field);
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }

    @Test
    public void testIsInTransaction() {
        assertFalse(testRealm.isInTransaction());
        testRealm.beginTransaction();
        assertTrue(testRealm.isInTransaction());
        testRealm.commitTransaction();
        assertFalse(testRealm.isInTransaction());
        testRealm.beginTransaction();
        assertTrue(testRealm.isInTransaction());
        testRealm.cancelTransaction();
        assertFalse(testRealm.isInTransaction());
    }

    // test for https://github.com/realm/realm-java/issues/1646
    @Test
    public void testClosingRealmWhileOtherThreadIsOpeningRealm() throws Exception {
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(1);

        final List<Exception> exception = new ArrayList<Exception>();

        new Thread() {
            @Override
            public void run() {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    exception.add(e);
                    return;
                }

                final Realm realm = Realm.getInstance(testConfig);
                try {
                    realm.where(AllTypes.class).equalTo("columnLong", 0L).findFirst();
                } catch (Exception e) {
                    exception.add(e);
                } finally {
                    endLatch.countDown();
                    realm.close();
                }
            }
        }.start();

        // prevent for another thread to enter Realm.createAndValidate().
        synchronized (BaseRealm.class) {
            startLatch.countDown();

            // wait for another thread's entering Realm.createAndValidate().
            SystemClock.sleep(100L);

            testRealm.close();
            testRealm = null;
        }

        endLatch.await();

        if (!exception.isEmpty()) {
            throw exception.get(0);
        }
    }

    // Bug reported https://github.com/realm/realm-java/issues/1728.
    // Root cause is validatedRealmFiles will be cleaned when any thread's Realm ref counter reach 0.
    @Test
    public void testOpenRealmWhileTransactionInAnotherThread() throws Exception {
        final CountDownLatch realmOpenedInBgLatch = new CountDownLatch(1);
        final CountDownLatch realmClosedInFgLatch = new CountDownLatch(1);
        final CountDownLatch transBeganInBgLatch = new CountDownLatch(1);
        final CountDownLatch fgFinishedLatch = new CountDownLatch(1);
        final CountDownLatch bgFinishedLatch = new CountDownLatch(1);
        final List<Exception> exception = new ArrayList<Exception>();

        // Step 1: testRealm is opened already.

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Step 2: Open realm in background thread.
                Realm realm = Realm.getInstance(testConfig);
                realmOpenedInBgLatch.countDown();
                try {
                    realmClosedInFgLatch.await();
                } catch (InterruptedException e) {
                    exception.add(e);
                    realm.close();
                    return;
                }

                // Step 4: Start transaction in background
                realm.beginTransaction();
                transBeganInBgLatch.countDown();
                try {
                    fgFinishedLatch.await();
                } catch (InterruptedException e) {
                    exception.add(e);
                }
                // Step 6: Cancel Transaction and close realm in background
                realm.cancelTransaction();
                realm.close();
                bgFinishedLatch.countDown();
            }
        });
        thread.start();

        realmOpenedInBgLatch.await();
        // Step 3: Close all realm instances in foreground thread.
        testRealm.close();
        realmClosedInFgLatch.countDown();
        transBeganInBgLatch.await();

        // Step 5: Get a new Realm instance in foreground
        testRealm = Realm.getInstance(testConfig);
        fgFinishedLatch.countDown();
        bgFinishedLatch.await();

        if (!exception.isEmpty()) {
            throw exception.get(0);
        }
    }

    @Test
    public void testCannotRefreshInTransaction() {
        testRealm.beginTransaction();
        try {
            testRealm.refresh();
            fail("Cannot refresh in a Transaction. Expected IllegalStateException to be thrown.");
        } catch (IllegalStateException e) {
            assertEquals(e.getMessage(), "Cannot refresh inside of a transaction.");
        } finally {
            testRealm.cancelTransaction();
        }
    }

    @Test
    public void testIsEmpty() {
        RealmConfiguration realmConfig = TestHelper.createConfiguration(context, "empty_test.realm");
        Realm.deleteRealm(realmConfig);
        Realm emptyRealm = Realm.getInstance(realmConfig);

        assertTrue(emptyRealm.isEmpty());

        emptyRealm.beginTransaction();
        PrimaryKeyAsLong obj = new PrimaryKeyAsLong();
        obj.setId(1);
        obj.setName("Foo");
        emptyRealm.copyToRealm(obj);
        assertFalse(emptyRealm.isEmpty());
        emptyRealm.cancelTransaction();

        assertTrue(emptyRealm.isEmpty());

        emptyRealm.beginTransaction();
        obj = new PrimaryKeyAsLong();
        obj.setId(1);
        obj.setName("Foo");
        emptyRealm.copyToRealm(obj);
        emptyRealm.commitTransaction();

        assertFalse(emptyRealm.isEmpty());

        emptyRealm.close();
    }

    @Test
    public void testCopyInvalidObjectFromRealmThrows() {
        testRealm.beginTransaction();
        AllTypes obj = testRealm.createObject(AllTypes.class);
        obj.removeFromRealm();
        testRealm.commitTransaction();

        try {
            testRealm.copyFromRealm(obj);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            testRealm.copyFromRealm(new AllTypes());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testCopyFromRealmWithInvalidDepth() {
        testRealm.beginTransaction();
        AllTypes obj = testRealm.createObject(AllTypes.class);
        testRealm.commitTransaction();

        try {
            testRealm.copyFromRealm(obj, -1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testCopyFromRealm() {
        populateTestRealm();
        AllTypes realmObject = testRealm.where(AllTypes.class).findAllSorted("columnLong").first();
        AllTypes standaloneObject = testRealm.copyFromRealm(realmObject);
        assertArrayEquals(realmObject.getColumnBinary(), standaloneObject.getColumnBinary());
        assertEquals(realmObject.getColumnString(), standaloneObject.getColumnString());
        assertEquals(realmObject.getColumnLong(), standaloneObject.getColumnLong());
        assertEquals(realmObject.getColumnFloat(), standaloneObject.getColumnFloat(), 0.00000000001);
        assertEquals(realmObject.getColumnDouble(), standaloneObject.getColumnDouble(), 0.00000000001);
        assertEquals(realmObject.isColumnBoolean(), standaloneObject.isColumnBoolean());
        assertEquals(realmObject.getColumnDate(), standaloneObject.getColumnDate());
    }

    @Test
    public void testCopyFromRealmDifferentObjects() {
        populateTestRealm();
        AllTypes realmObject = testRealm.where(AllTypes.class).findAllSorted("columnLong").first();
        AllTypes standaloneObject1 = testRealm.copyFromRealm(realmObject);
        AllTypes standaloneObject2 = testRealm.copyFromRealm(realmObject);
        assertFalse(standaloneObject1 == standaloneObject2);
        assertNotSame(standaloneObject1, standaloneObject2);
    }

    // Test that the object graph is copied as it is and no extra copies are made
    // 1) (A -> B/[B,C])
    // 2) (C -> B/[B,A])
    // A copy should result in only 3 distinct objects
    @Test
    public void testCopyFromRealmCyclicObjectGraph() {
        testRealm.beginTransaction();
        CyclicType objA = testRealm.createObject(CyclicType.class);
        objA.setName("A");
        CyclicType objB = testRealm.createObject(CyclicType.class);
        objB.setName("B");
        CyclicType objC = testRealm.createObject(CyclicType.class);
        objC.setName("C");
        objA.setObject(objB);
        objC.setObject(objB);
        objA.getObjects().add(objB);
        objA.getObjects().add(objC);
        objC.getObjects().add(objB);
        objC.getObjects().add(objA);
        testRealm.commitTransaction();

        CyclicType copyA = testRealm.copyFromRealm(objA);
        CyclicType copyB = copyA.getObject();
        CyclicType copyC = copyA.getObjects().get(1);

        assertEquals("A", copyA.getName());
        assertEquals("B", copyB.getName());
        assertEquals("C", copyC.getName());

        // Assert object equality on the object graph
        assertTrue(copyA.getObject() == copyC.getObject());
        assertTrue(copyA.getObjects().get(0) == copyC.getObjects().get(0));
        assertTrue(copyA == copyC.getObjects().get(1));
        assertTrue(copyC == copyA.getObjects().get(1));
    }

    // Test that for (A -> B -> C) for maxDepth = 1, result is (A -> B -> null)
    @Test
    public void testCopyFromRealmWithDepth() {
        testRealm.beginTransaction();
        CyclicType objA = testRealm.createObject(CyclicType.class);
        objA.setName("A");
        CyclicType objB = testRealm.createObject(CyclicType.class);
        objB.setName("B");
        CyclicType objC = testRealm.createObject(CyclicType.class);
        objC.setName("C");
        objA.setObject(objB);
        objC.setObject(objC);
        objA.getObjects().add(objB);
        objA.getObjects().add(objC);
        testRealm.commitTransaction();

        CyclicType copyA = testRealm.copyFromRealm(objA, 1);

        assertNull(copyA.getObject().getObject());
    }

    // Test that depth restriction is calculated from the top-most encountered object, i.e. it is possible for some
    // objects to exceed the depth limit.
    // A -> B -> C -> D -> E
    // A -> D -> E
    // D is both at depth 1 and 3. For maxDepth = 3, E should still be copied.
    @Test
    public void testCopyFromRealmWithDifferentDepths() {
        testRealm.beginTransaction();
        CyclicType objA = testRealm.createObject(CyclicType.class);
        objA.setName("A");
        CyclicType objB = testRealm.createObject(CyclicType.class);
        objB.setName("B");
        CyclicType objC = testRealm.createObject(CyclicType.class);
        objC.setName("C");
        CyclicType objD = testRealm.createObject(CyclicType.class);
        objD.setName("D");
        CyclicType objE = testRealm.createObject(CyclicType.class);
        objE.setName("E");
        objA.setObject(objB);
        objB.setObject(objC);
        objC.setObject(objD);
        objD.setObject(objE);
        objA.setOtherObject(objD);
        testRealm.commitTransaction();

        // object is filled before otherObject (because of field order - WARNING: Not guaranteed)
        // this means that the object will be encountered first time at max depth, so E will not be copied.
        // If the object cache does not handle this, otherObject will be wrong.
        CyclicType copyA = testRealm.copyFromRealm(objA, 3);
        assertEquals("E", copyA.getOtherObject().getObject().getName());
    }

    @Test
    public void testCopyListInvalidObjectFromRealmThrows() {
        testRealm.beginTransaction();
        AllTypes object = testRealm.createObject(AllTypes.class);
        List<AllTypes> list = new RealmList<AllTypes>(object);
        object.removeFromRealm();
        testRealm.commitTransaction();

        try {
            testRealm.copyFromRealm(list);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testCopyListFromRealmWithInvalidDepth() {
        RealmResults<AllTypes> results = testRealm.allObjects(AllTypes.class);
        try {
            testRealm.copyFromRealm(results, -1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Test that the same Realm objects in a list result in the same Java in-memory copy.
    // List: A -> [(B -> C), (B -> C)] should result in only 2 copied objects A and B and not A1, B1, A2, B2
    @Test
    public void testCopyListFromRealmSameElements() {
        testRealm.beginTransaction();
        CyclicType objA = testRealm.createObject(CyclicType.class);
        objA.setName("A");
        CyclicType objB = testRealm.createObject(CyclicType.class);
        objB.setName("B");
        CyclicType objC = testRealm.createObject(CyclicType.class);
        objC.setName("C");
        objB.setObject(objC);
        objA.getObjects().add(objB);
        objA.getObjects().add(objB);
        testRealm.commitTransaction();

        List<CyclicType> results = testRealm.copyFromRealm(objA.getObjects());
        assertEquals(2, results.size());
        assertEquals("B", results.get(0).getName());
        assertTrue(results.get(0) == results.get(1));
    }

    // Test if close can be called from Realm change listener when there is no other listeners
    @Test
    public void testCloseRealmInChangeListener() {
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        HandlerThread handlerThread = new HandlerThread("background");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final Realm realm = Realm.getInstance(testConfig);
                final RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (realm.where(AllTypes.class).count() == 1) {
                            realm.removeChangeListener(this);
                            realm.close();
                            signalTestFinished.countDown();
                        }
                    }
                };

                realm.addChangeListener(listener);

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.createObject(AllTypes.class);
                    }
                }, new Realm.Transaction.Callback());
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
    }

    // Test if close can be called from Realm change listener when there is a listener on empty Realm Object
    @Test
    public void testCloseRealmInChangeListenerWhenThereIsListenerOnEmptyObject() {
        //noinspection
        final Object[] keepStrongReferences = new Object[1];
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        HandlerThread handlerThread = new HandlerThread("background");
        handlerThread.start();

        final Handler handler = new Handler(handlerThread.getLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                final Realm realm = Realm.getInstance(testConfig);
                final RealmChangeListener dummyListener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                    }
                };
                final RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (realm.where(AllTypes.class).count() == 1) {
                            realm.removeChangeListener(this);
                            realm.close();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                };

                realm.addChangeListener(listener);

                // Change listener on Empty Object
                final AllTypes allTypes = realm.where(AllTypes.class).findFirstAsync();
                allTypes.addChangeListener(dummyListener);

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.createObject(AllTypes.class);
                    }
                }, new Realm.Transaction.Callback());
                keepStrongReferences[0] = allTypes;
            }
        });

        try {
            TestHelper.awaitOrFail(signalTestFinished);
        } finally {
            handlerThread.quit();
        }
    }

    // Test if close can be called from Realm change listener when there is an listener on non-empty Realm Object
    @Test
    public void testCloseRealmInChangeListenerWhenThereIsListenerOnObject() {
        //noinspection
        final Object[] keepStrongReferences = new Object[1];
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        HandlerThread handlerThread = new HandlerThread("background");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final Realm realm = Realm.getInstance(testConfig);
                final RealmChangeListener dummyListener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                    }
                };
                final RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (realm.where(AllTypes.class).count() == 2) {
                            realm.removeChangeListener(this);
                            realm.close();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                };

                realm.addChangeListener(listener);

                realm.beginTransaction();
                realm.createObject(AllTypes.class);
                realm.commitTransaction();
                // Step 1: Change listener on Realm Object
                final AllTypes allTypes = realm.where(AllTypes.class).findFirst();
                allTypes.addChangeListener(dummyListener);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.createObject(AllTypes.class);
                    }
                }, new Realm.Transaction.Callback());
                keepStrongReferences[0] = allTypes;
            }
        });
        try {
            TestHelper.awaitOrFail(signalTestFinished);
        } finally {
            handlerThread.quit();
        }
    }

    // Test if close can be called from Realm change listener when there is an listener on RealmResults
    @Test
    public void testCloseRealmInChangeListenerWhenThereIsListenerOnResults() {
        //noinspection
        final Object[] keepStrongReferences = new Object[1];
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        HandlerThread handlerThread = new HandlerThread("background");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final Realm realm = Realm.getInstance(testConfig);
                final RealmChangeListener dummyListener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                    }
                };
                final RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (realm.where(AllTypes.class).count() == 1) {
                            realm.removeChangeListener(this);
                            realm.close();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                };

                realm.addChangeListener(listener);

                // Step 1: Change listener on Realm results
                RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
                results.addChangeListener(dummyListener);

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.createObject(AllTypes.class);
                    }
                }, new Realm.Transaction.Callback());
                keepStrongReferences[0] = results;
            }
        });

        try {
            TestHelper.awaitOrFail(signalTestFinished);
        } finally {
            handlerThread.quit();
        }
    }

    public void testRemoveChangeListenerThrowExceptionOnNonLooperThread() {
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(testConfig);
                try {
                    realm.removeChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                        }
                    });
                    fail("Should not be able to invoke removeChangeListener");
                } catch (IllegalStateException e) {
                    signalTestFinished.countDown();
                } finally {
                    realm.close();
                }
            }
        });
        thread.start();

        try {
            TestHelper.awaitOrFail(signalTestFinished);
        } finally {
            thread.interrupt();
        }
    }

    public void testRemoveAllChangeListenersThrowExceptionOnNonLooperThread() {
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(testConfig);
                try {
                    realm.removeAllChangeListeners();
                    fail("Should not be able to invoke removeChangeListener");
                } catch (IllegalStateException e) {
                    signalTestFinished.countDown();
                } finally {
                    realm.close();
                }
            }
        });
        thread.start();

        try {
            TestHelper.awaitOrFail(signalTestFinished);
        } finally {
            thread.interrupt();
        }
    }
}
