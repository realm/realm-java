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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
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
import io.realm.entities.PrimaryKeyAsBoxedByte;
import io.realm.entities.PrimaryKeyAsBoxedInteger;
import io.realm.entities.PrimaryKeyAsBoxedLong;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsLong;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.entities.PrimaryKeyMix;
import io.realm.entities.PrimaryKeyRequiredAsBoxedByte;
import io.realm.entities.PrimaryKeyRequiredAsBoxedInteger;
import io.realm.entities.PrimaryKeyRequiredAsBoxedLong;
import io.realm.entities.PrimaryKeyRequiredAsBoxedShort;
import io.realm.entities.PrimaryKeyRequiredAsString;
import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmIOException;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import io.realm.internal.log.RealmLog;
import io.realm.objectid.NullPrimaryKey;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmTests {
    private final static int TEST_DATA_SIZE = 10;

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Context context;
    private Realm realm;
    private List<String> columnData = new ArrayList<String>();
    private RealmConfiguration realmConfig;

    private void setColumnData() {
        columnData.add(0, AllTypes.FIELD_BOOLEAN);
        columnData.add(1, AllTypes.FIELD_DATE);
        columnData.add(2, AllTypes.FIELD_DOUBLE);
        columnData.add(3, AllTypes.FIELD_FLOAT);
        columnData.add(4, AllTypes.FIELD_STRING);
        columnData.add(5, AllTypes.FIELD_LONG);
    }

    @Before
    public void setUp() {
        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        context = InstrumentationRegistry.getInstrumentation().getContext();
        realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    private void populateTestRealm(Realm realm, int objects) {
        realm.beginTransaction();
        realm.deleteAll();
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
        populateTestRealm(realm, TEST_DATA_SIZE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getInstance_nullDir() {
        Realm.getInstance(new RealmConfiguration.Builder((File) null).build());
    }

    @Test
    public void getInstance_writeProtectedDir() {
        File folder = new File("/");
        thrown.expect(IllegalArgumentException.class);
        Realm.getInstance(new RealmConfiguration.Builder(folder).build());
    }

    @Test
    public void getInstance_writeProtectedFile() throws IOException {
        String REALM_FILE = "readonly.realm";
        File folder = configFactory.getRoot();
        File realmFile = new File(folder, REALM_FILE);
        assertFalse(realmFile.exists());
        assertTrue(realmFile.createNewFile());
        assertTrue(realmFile.setWritable(false));

        thrown.expect(RealmIOException.class);
        Realm.getInstance(new RealmConfiguration.Builder(folder).name(REALM_FILE).build());
    }

    @Test
    public void getInstance_twiceWhenRxJavaUnavailable() {
        // test for https://github.com/realm/realm-java/issues/2416

        // Though it's not a recommended way to create multiple configuration instance with the same parameter, it's legal.
        final RealmConfiguration configuration1 = configFactory.createConfiguration("no_RxJava.realm");
        TestHelper.emulateRxJavaUnavailable(configuration1);
        final RealmConfiguration configuration2 = configFactory.createConfiguration("no_RxJava.realm");
        TestHelper.emulateRxJavaUnavailable(configuration2);

        final Realm realm1 = Realm.getInstance(configuration1);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            final Realm realm2 = Realm.getInstance(configuration2);
            realm2.close();
        } finally {
            realm1.close();
        }
    }

    @Test
    public void checkIfValid() {
        // checkIfValid() must not throw any Exception against valid Realm instance.
        realm.checkIfValid();

        realm.close();
        try {
            realm.checkIfValid();
            fail("closed Realm instance must throw IllegalStateException.");
        } catch (IllegalStateException ignored) {
        }
        realm = null;
    }

    @Test
    @UiThreadTest
    public void internalRealmChangedHandlersRemoved() {
        realm.close(); // Clear handler created by testRealm in setUp()
        assertEquals(0, Realm.getHandlers().size());
        final String REALM_NAME = "test-internalhandlers";
        RealmConfiguration realmConfig = configFactory.createConfiguration(REALM_NAME);
        Realm.deleteRealm(realmConfig);

        // Open and close first instance of a Realm
        Realm realm = null;
        try {
            realm = Realm.getInstance(realmConfig);
            assertFalse(this.realm == realm);
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
    public void getInstance() {
        assertNotNull("Realm.getInstance unexpectedly returns null", realm);
        assertTrue("Realm.getInstance does not contain expected table", realm.contains(AllTypes.class));
    }

    @Test
    public void getInstance_context() {
        RealmConfiguration config = new RealmConfiguration.Builder(context).build();
        Realm.deleteRealm(config);

        Realm testRealm = Realm.getInstance(context);
        assertNotNull("Realm.getInstance unexpectedly returns null", testRealm);
        assertTrue("Realm.getInstance does not contain expected table", testRealm.contains(AllTypes.class));
        config = testRealm.getConfiguration();
        config.getRealmFolder().equals(context.getFilesDir());
        testRealm.close();
        Realm.deleteRealm(config);
    }

    @Test
    public void getInstance_nullContext() {
        Realm realm = null;
        try {
            realm = Realm.getInstance((Context) null); // throws when context.getFilesDir() is called;
            // has nothing to do with Realm
            fail("Should throw an exception");
        } catch (IllegalArgumentException ignored) {
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    // Private API
    @Test
    public void remove() {
        populateTestRealm();
        realm.beginTransaction();
        realm.remove(AllTypes.class, 0);
        realm.commitTransaction();

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE - 1, resultList.size());
    }

    // Private API
    @Test
    public void get() {
        populateTestRealm();
        AllTypes allTypes = realm.get(AllTypes.class, 0);
        assertNotNull(allTypes);
        assertEquals("test data 0", allTypes.getColumnString());
    }

    // Private API
    @Test
    public void contains() {
        assertTrue("contains returns false for table that should exists", realm.contains(Dog.class));
        assertFalse("contains returns true for non-existing table", realm.contains(null));
    }

    @Test
    public void where() {
        populateTestRealm();
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE, resultList.size());
    }

    // Note that this test is relying on the values set while initializing the test dataset
    // TODO Move to RealmQueryTests?
    @Test
    public void where_queryResults() throws IOException {
        populateTestRealm(realm, 159);
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 33).findAll();
        assertEquals(1, resultList.size());

        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 3333).findAll();
        assertEquals(0, resultList.size());

        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "test data 0").findAll();
        assertEquals(1, resultList.size());

        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "test data 0", Case.INSENSITIVE).findAll();
        assertEquals(1, resultList.size());

        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "Test data 0", Case.SENSITIVE).findAll();
        assertEquals(0, resultList.size());
    }

    // TODO Move to RealmQueryTests?
    @Test
    public void where_equalTo_wrongFieldTypeAsInput() throws IOException {
        populateTestRealm();
        setColumnData();

        for (int i = 0; i < columnData.size(); i++) {
            try {
                realm.where(AllTypes.class).equalTo(columnData.get(i), true).findAll();
                if (i != 0) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }

            try {
                realm.where(AllTypes.class).equalTo(columnData.get(i), new Date()).findAll();
                if (i != 1) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }

            try {
                realm.where(AllTypes.class).equalTo(columnData.get(i), 13.37d).findAll();
                if (i != 2) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }

            try {
                realm.where(AllTypes.class).equalTo(columnData.get(i), 13.3711f).findAll();
                if (i != 3) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }

            try {
                realm.where(AllTypes.class).equalTo(columnData.get(i), "test").findAll();
                if (i != 4) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }

            try {
                realm.where(AllTypes.class).equalTo(columnData.get(i), 1337).findAll();
                if (i != 5) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    // TODO Move to RealmQueryTests?
    @Test
    public void where_equalTo_invalidFieldName() throws IOException {
        try {
            realm.where(AllTypes.class).equalTo("invalidcolumnname", 33).findAll();
            fail("Invalid field name");
        } catch (Exception ignored) {
        }

        try {
            realm.where(AllTypes.class).equalTo("invalidcolumnname", "test").findAll();
            fail("Invalid field name");
        } catch (Exception ignored) {
        }

        try {
            realm.where(AllTypes.class).equalTo("invalidcolumnname", true).findAll();
            fail("Invalid field name");
        } catch (Exception ignored) {
        }

        try {
            realm.where(AllTypes.class).equalTo("invalidcolumnname", 3.1415d).findAll();
            fail("Invalid field name");
        } catch (Exception ignored) {
        }

        try {
            realm.where(AllTypes.class).equalTo("invalidcolumnname", 3.1415f).findAll();
            fail("Invalid field name");
        } catch (Exception ignored) {
        }
    }

    // TODO Move to RealmQueryTests?
    @Test
    public void where_equalTo_requiredFieldWithNullArgument() {
        // String
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_STRING_NOT_NULL, (String) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Boolean
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BOOLEAN_NOT_NULL, (String) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Byte
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_BYTE_NOT_NULL, (Byte) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Short
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_SHORT_NOT_NULL, (Short) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Integer
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_INTEGER_NOT_NULL, (Integer) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Long
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_LONG_NOT_NULL, (Long) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Float
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NOT_NULL, (Float) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Double
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_FLOAT_NOT_NULL, (Double) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }

        // Date
        try {
            realm.where(NullTypes.class).equalTo(NullTypes.FIELD_DATE_NOT_NULL, (Date) null).findAll();
            fail("Realm.where should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void beginTransaction() throws IOException {
        populateTestRealm();

        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnFloat(3.1415f);
        allTypes.setColumnString("a unique string");
        realm.commitTransaction();

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE + 1, resultList.size());

        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "a unique string").findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_FLOAT, 3.1415f).findAll();
        assertEquals(1, resultList.size());
    }

    @Test
    public void nestedTransaction() {
        realm.beginTransaction();
        try {
            realm.beginTransaction();
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Nested transactions are not allowed. Use commitTransaction() after each beginTransaction().", e.getMessage());
        }
        realm.commitTransaction();
    }

    private enum Method {
        METHOD_BEGIN,
        METHOD_COMMIT,
        METHOD_CANCEL,
        METHOD_DELETE_TYPE,
        METHOD_DELETE_ALL,
        METHOD_CREATE_OBJECT,
        METHOD_COPY_TO_REALM,
        METHOD_COPY_TO_REALM_OR_UPDATE,
        METHOD_CREATE_ALL_FROM_JSON,
        METHOD_CREATE_OR_UPDATE_ALL_FROM_JSON,
        METHOD_CREATE_FROM_JSON,
        METHOD_CREATE_OR_UPDATE_FROM_JSON
    }

    // Calling methods on a wrong thread will fail.
    private boolean runMethodOnWrongThread(final Method method) throws InterruptedException, ExecutionException {
        if (method != Method.METHOD_BEGIN) {
            realm.beginTransaction();
            realm.createObject(Dog.class);
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
                        case METHOD_DELETE_TYPE:
                            realm.delete(AllTypes.class);
                            break;
                        case METHOD_DELETE_ALL:
                            realm.deleteAll();
                            break;
                        case METHOD_CREATE_OBJECT:
                            realm.createObject(AllTypes.class);
                            break;
                        case METHOD_COPY_TO_REALM:
                            realm.copyToRealm(new AllTypes());
                            break;
                        case METHOD_COPY_TO_REALM_OR_UPDATE:
                            realm.copyToRealm(new AllTypesPrimaryKey());
                            break;
                        case METHOD_CREATE_ALL_FROM_JSON:
                            realm.createAllFromJson(AllTypes.class, "[{}]");
                            break;
                        case METHOD_CREATE_OR_UPDATE_ALL_FROM_JSON:
                            realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, "[{\"columnLong\":1}]");
                            break;
                        case METHOD_CREATE_FROM_JSON:
                            realm.createObjectFromJson(AllTypes.class, "{}");
                            break;
                        case METHOD_CREATE_OR_UPDATE_FROM_JSON:
                            realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, "{\"columnLong\":1}");
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
            realm.cancelTransaction();
        }
        return result;
    }

    @Test
    public void methodCalledOnWrongThread() throws ExecutionException, InterruptedException {
        for (Method method : Method.values()) {
            assertTrue(method.toString(), runMethodOnWrongThread(method));
        }
    }

    @Test
    public void commitTransaction() {
        populateTestRealm();

        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnBoolean(true);
        realm.commitTransaction();

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE + 1, resultList.size());
    }

    @Test(expected = IllegalStateException.class)
    public void commitTransaction_afterCancelTransaction() {
        realm.beginTransaction();
        realm.cancelTransaction();
        realm.commitTransaction();
    }

    @Test(expected = IllegalStateException.class)
    public void commitTransaction_twice() {
        realm.beginTransaction();
        realm.commitTransaction();
        realm.commitTransaction();
    }

    @Test
    public void cancelTransaction() {
        populateTestRealm();

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.cancelTransaction();
        assertEquals(TEST_DATA_SIZE, realm.where(AllTypes.class).count());

        try {
            realm.cancelTransaction();
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void executeTransaction_null() {
        try {
            realm.executeTransaction(null);
            fail("null transaction should throw");
        } catch (IllegalArgumentException ignored) {

        }
        assertFalse(realm.hasChanged());
    }

    @Test
    public void executeTransaction_success() {
        assertEquals(0, realm.where(Owner.class).count());
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Owner owner = realm.createObject(Owner.class);
                owner.setName("Owner");
            }
        });
        assertEquals(1, realm.where(Owner.class).count());
    }

    @Test
    public void executeTransaction_canceled() {
        final AtomicReference<RuntimeException> thrownException = new AtomicReference<>(null);

        assertEquals(0, realm.where(Owner.class).count());
        try {
            realm.executeTransaction(new Realm.Transaction() {
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
        assertEquals(0, realm.where(Owner.class).count());
    }

    @Test
    public void executeTransaction_cancelInsideClosureThrowsException() {
        assertEquals(0, realm.where(Owner.class).count());
        TestHelper.TestLogger testLogger = new TestHelper.TestLogger();
        try {
            RealmLog.add(testLogger);
            realm.executeTransaction(new Realm.Transaction() {
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
        assertEquals(0, realm.where(Owner.class).count());
    }

    @Test
    public void delete_type() {
        // ** delete non existing table should succeed
        realm.beginTransaction();
        realm.delete(AllTypes.class);
        realm.commitTransaction();

        // ** delete existing class, but leave other classes classes

        // Add two classes
        populateTestRealm();
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Castro");
        realm.commitTransaction();
        // Clear
        realm.beginTransaction();
        realm.delete(Dog.class);
        realm.commitTransaction();
        // Check one class is cleared but other class is still there
        RealmResults<AllTypes> resultListTypes = realm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE, resultListTypes.size());
        RealmResults<Dog> resultListDogs = realm.where(Dog.class).findAll();
        assertEquals(0, resultListDogs.size());

        // ** delete() must throw outside a transaction
        try {
            realm.delete(AllTypes.class);
            fail("Expected exception");
        } catch (IllegalStateException ignored) {
        }
    }

    private void createAndTestFilename(String language, String fileName) {
        RealmConfiguration realmConfig = configFactory.createConfiguration(fileName);
        Realm realm1 = Realm.getInstance(realmConfig);
        realm1.beginTransaction();
        Dog dog1 = realm1.createObject(Dog.class);
        dog1.setName("Rex");
        realm1.commitTransaction();
        realm1.close();

        File file = new File(realmConfig.getPath());
        assertTrue(language, file.exists());

        Realm realm2 = Realm.getInstance(realmConfig);
        Dog dog2 = realm2.where(Dog.class).findFirst();
        assertEquals(language, "Rex", dog2.getName());
        realm2.close();
    }

    // TODO Move to RealmConfigurationTests?
    @Test
    public void realmConfiguration_fileName() {
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
    public void utf8Tests() {
        realm.beginTransaction();
        realm.delete(AllTypes.class);
        realm.commitTransaction();

        String file = "assets/unicode_codepoints.csv";
        Scanner scanner = new Scanner(getClass().getClassLoader().getResourceAsStream(file), "UTF-8");
        int i = 0;
        String currentUnicode = null;
        try {
            realm.beginTransaction();
            while (scanner.hasNextLine()) {
                currentUnicode = scanner.nextLine();
                char[] chars = Character.toChars(Integer.parseInt(currentUnicode, 16));
                String codePoint = new String(chars);
                AllTypes o = realm.createObject(AllTypes.class);
                o.setColumnLong(i);
                o.setColumnString(codePoint);

                AllTypes realmType = realm.where(AllTypes.class).equalTo("columnLong", i).findFirst();
                if (i > 1) {
                    assertEquals("Codepoint: " + i + " / " + currentUnicode, codePoint,
                            realmType.getColumnString()); // codepoint 0 is NULL, ignore for now.
                }
                i++;
            }
            realm.commitTransaction();
        } catch (Exception e) {
            fail("Failure, Codepoint: " + i + " / " + currentUnicode + " " + e.getMessage());
        }
    }

    private List<String> getCharacterArray() {
        List<String> chars_array = new ArrayList<String>();
        String file = "assets/unicode_codepoints.csv";
        Scanner scanner = new Scanner(getClass().getClassLoader().getResourceAsStream(file), "UTF-8");
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
    // @Test TODO AndroidJUnit4 runner doesn't seem to respect the @Ignore annotation?
    @Ignore
    public void unicodeStrings() {
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
            realm.beginTransaction();
            StringOnly stringOnly = realm.createObject(StringOnly.class);
            stringOnly.setChars(test_char);
            realm.commitTransaction();

            realm.where(StringOnly.class).findFirst().getChars();

            realm.beginTransaction();
            realm.delete(StringOnly.class);
            realm.commitTransaction();
        }
    }

    @Test
    public void getInstance_referenceCounting() {
        // At this point reference count should be one because of the setUp method
        try {
            realm.where(AllTypes.class).count();
        } catch (IllegalStateException e) {
            fail();
        }

        // Make sure the reference counter is per realm file
        RealmConfiguration anotherConfig = configFactory.createConfiguration("anotherRealm.realm");
        Realm.deleteRealm(anotherConfig);
        Realm otherRealm = Realm.getInstance(anotherConfig);

        // Raise the reference
        Realm realm = null;
        try {
            realm = Realm.getInstance(configFactory.createConfiguration());
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

        this.realm.close();
        try {
            this.realm.where(AllTypes.class).count();
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
    public void getInstance_referenceCounting_doubleClose() {
        realm.close();
        realm.close(); // Count down once too many. Counter is now potentially negative
        realm = Realm.getInstance(configFactory.createConfiguration());
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        RealmResults<AllTypes> queryResult = realm.where(AllTypes.class).findAll();
        assertEquals(allTypes, queryResult.get(0));
        realm.commitTransaction();
        realm.close(); // This might not close the Realm if the reference count is wrong

        // This should now fail due to the Realm being fully closed.
        thrown.expect(IllegalStateException.class);
        allTypes.getColumnString();
    }

    @Test
    public void writeCopyTo() throws IOException {
        RealmConfiguration configA = configFactory.createConfiguration("file1.realm");
        RealmConfiguration configB = configFactory.createConfiguration("file2.realm");
        Realm.deleteRealm(configA);
        Realm.deleteRealm(configB);

        Realm realm1 = null;
        try {
            realm1 = Realm.getInstance(configA);
            realm1.beginTransaction();
            AllTypes allTypes = realm1.createObject(AllTypes.class);
            allTypes.setColumnString("Hello World");
            realm1.commitTransaction();

            realm1.writeCopyTo(new File(configB.getPath()));
        } finally {
            if (realm1 != null) {
                realm1.close();
            }
        }

        // Copy is compacted i.e. smaller than original
        File file1 = new File(configA.getPath());
        File file2 = new File(configB.getPath());
        assertTrue(file1.length() >= file2.length());

        Realm realm2 = null;
        try {
            // Contents is copied too
            realm2 = Realm.getInstance(configB);
            RealmResults<AllTypes> results = realm2.where(AllTypes.class).findAll();
            assertEquals(1, results.size());
            assertEquals("Hello World", results.first().getColumnString());
        } finally {
            if (realm2 != null) {
                realm2.close();
            }
        }
    }

    @Test
    public void compactRealm() {
        final RealmConfiguration configuration = realm.getConfiguration();
        realm.close();
        realm = null;
        assertTrue(Realm.compactRealm(configuration));
        realm = Realm.getInstance(configuration);
    }

    @Test
    public void compactRealm_failsIfOpen() {
        assertFalse(Realm.compactRealm(realm.getConfiguration()));
    }

    @Test
    public void compactRealm_encryptedEmptyRealm() {
        RealmConfiguration realmConfig = configFactory.createConfiguration("enc.realm", TestHelper.getRandomKey());
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
    public void compactRealm_encryptedPopulatedRealm() {
        RealmConfiguration realmConfig = configFactory.createConfiguration("enc.realm", TestHelper.getRandomKey());
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
    public void compactRealm_emptyRealm() throws IOException {
        final String REALM_NAME = "test.realm";
        RealmConfiguration realmConfig = configFactory.createConfiguration(REALM_NAME);
        Realm realm = Realm.getInstance(realmConfig);
        realm.close();
        long before = new File(realmConfig.getPath()).length();
        assertTrue(Realm.compactRealm(realmConfig));
        long after = new File(realmConfig.getPath()).length();
        assertTrue(before >= after);
    }

    @Test
    public void compactRealm_populatedRealm() throws IOException {
        final String REALM_NAME = "test.realm";
        RealmConfiguration realmConfig = configFactory.createConfiguration(REALM_NAME);
        Realm realm = Realm.getInstance(realmConfig);
        populateTestRealm(realm, 100);
        realm.close();
        long before = new File(realmConfig.getPath()).length();
        assertTrue(Realm.compactRealm(realmConfig));
        long after = new File(realmConfig.getPath()).length();
        assertTrue(before >= after);
    }

    @Test
    public void copyToRealm_null() {
        realm.beginTransaction();
        try {
            realm.copyToRealm((AllTypes) null);
            fail("Copying null objects into Realm should not be allowed");
        } catch (IllegalArgumentException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void copyToRealm_managedObject() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnString("Test");
        realm.commitTransaction();

        realm.beginTransaction();
        AllTypes copiedAllTypes = realm.copyToRealm(allTypes);
        realm.commitTransaction();

        assertTrue(allTypes == copiedAllTypes);
    }

    @Test
    public void copyToRealm_fromOtherRealm() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.setColumnString("Test");
        realm.commitTransaction();

        RealmConfiguration realmConfig = configFactory.createConfiguration("other-realm");
        Realm otherRealm = Realm.getInstance(realmConfig);
        otherRealm.beginTransaction();
        AllTypes copiedAllTypes = otherRealm.copyToRealm(allTypes);
        otherRealm.commitTransaction();

        assertNotSame(allTypes, copiedAllTypes); // Same object in different Realms is not the same
        assertEquals(allTypes.getColumnString(), copiedAllTypes.getColumnString()); // But data is still the same
        otherRealm.close();
    }

    @Test
    public void copyToRealm() {
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

        realm.beginTransaction();
        AllTypes realmTypes = realm.copyToRealm(allTypes);
        realm.commitTransaction();

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
    public void copyToRealm_cyclicObjectReferences() {
        CyclicType oneCyclicType = new CyclicType();
        oneCyclicType.setName("One");
        CyclicType anotherCyclicType = new CyclicType();
        anotherCyclicType.setName("Two");
        oneCyclicType.setObject(anotherCyclicType);
        anotherCyclicType.setObject(oneCyclicType);

        realm.beginTransaction();
        CyclicType realmObject = realm.copyToRealm(oneCyclicType);
        realm.commitTransaction();

        assertEquals("One", realmObject.getName());
        assertEquals("Two", realmObject.getObject().getName());
        assertEquals(2, realm.where(CyclicType.class).count());
    }

    @Test
    public void copyToRealm_cyclicListReferences() {
        CyclicType oneCyclicType = new CyclicType();
        oneCyclicType.setName("One");
        CyclicType anotherCyclicType = new CyclicType();
        anotherCyclicType.setName("Two");
        oneCyclicType.setObjects(new RealmList(anotherCyclicType));
        anotherCyclicType.setObjects(new RealmList(oneCyclicType));

        realm.beginTransaction();
        CyclicType realmObject = realm.copyToRealm(oneCyclicType);
        realm.commitTransaction();

        assertEquals("One", realmObject.getName());
        assertEquals(2, realm.where(CyclicType.class).count());
    }

    // Check that if a field has a null value it gets converted to the default value for that type
    @Test
    public void copyToRealm_convertsNullToDefaultValue() {
        realm.beginTransaction();
        AllTypes realmTypes = realm.copyToRealm(new AllTypes());
        realm.commitTransaction();

        assertEquals("", realmTypes.getColumnString());
        assertEquals(new Date(0), realmTypes.getColumnDate());
        assertArrayEquals(new byte[0], realmTypes.getColumnBinary());
    }

    // Check that using copyToRealm will set the primary key directly instead of first setting
    // it to the default value (which can fail).
    @Test
    public void copyToRealm_primaryKeyIsSetDirectly() {
        realm.beginTransaction();
        realm.createObject(OwnerPrimaryKey.class);
        realm.copyToRealm(new OwnerPrimaryKey(1, "Foo"));
        realm.commitTransaction();
        assertEquals(2, realm.where(OwnerPrimaryKey.class).count());
    }

    @Test
    public void copyToRealm_stringPrimaryKeyIsNull() {
        final long SECONDARY_FIELD_VALUE = 34992142L;
        TestHelper.addStringPrimaryKeyObjectToTestRealm(realm, (String) null, SECONDARY_FIELD_VALUE);

        RealmResults<PrimaryKeyAsString> results = realm.allObjects(PrimaryKeyAsString.class);
        assertEquals(1, results.size());
        assertEquals(null, results.first().getName());
        assertEquals(SECONDARY_FIELD_VALUE, results.first().getId());
    }

    @Test
    public void copyToRealm_boxedNumberPrimaryKeyIsNull() {
        final String SECONDARY_FIELD_VALUE = "nullNumberPrimaryKeyObj";
        final Class[] CLASSES = {PrimaryKeyAsBoxedByte.class, PrimaryKeyAsBoxedShort.class, PrimaryKeyAsBoxedInteger.class, PrimaryKeyAsBoxedLong.class};

        TestHelper.addBytePrimaryKeyObjectToTestRealm(realm,    (Byte) null,    SECONDARY_FIELD_VALUE);
        TestHelper.addShortPrimaryKeyObjectToTestRealm(realm,   (Short) null,   SECONDARY_FIELD_VALUE);
        TestHelper.addIntegerPrimaryKeyObjectToTestRealm(realm, (Integer) null, SECONDARY_FIELD_VALUE);
        TestHelper.addLongPrimaryKeyObjectToTestRealm(realm,    (Long) null,    SECONDARY_FIELD_VALUE);

        for (Class clazz : CLASSES) {
            RealmResults results = realm.allObjects(clazz);
            assertEquals(1, results.size());
            assertEquals(null, ((NullPrimaryKey)results.first()).getId());
            assertEquals(SECONDARY_FIELD_VALUE, ((NullPrimaryKey)results.first()).getName());
        }
    }

    @Test
    public void copyToRealm_duplicatedNullPrimaryKeyThrows() {
        final String[] PRIMARY_KEY_TYPES = {"String", "BoxedByte", "BoxedShort", "BoxedInteger", "BoxedLong"};

        TestHelper.addStringPrimaryKeyObjectToTestRealm(realm,  (String) null,  0);
        TestHelper.addBytePrimaryKeyObjectToTestRealm(realm,    (Byte) null,    (String) null);
        TestHelper.addShortPrimaryKeyObjectToTestRealm(realm,   (Short) null,   (String) null);
        TestHelper.addIntegerPrimaryKeyObjectToTestRealm(realm, (Integer) null, (String) null);
        TestHelper.addLongPrimaryKeyObjectToTestRealm(realm,    (Long) null,    (String) null);

        for (String className : PRIMARY_KEY_TYPES) {
            try {
                realm.beginTransaction();
                switch (className) {
                    case "String":
                        realm.copyToRealm(new PrimaryKeyAsString());
                        break;
                    case "BoxedByte":
                        realm.copyToRealm(new PrimaryKeyAsBoxedByte());
                        break;
                    case "BoxedShort":
                        realm.copyToRealm(new PrimaryKeyAsBoxedShort());
                        break;
                    case "BoxedInteger":
                        realm.copyToRealm(new PrimaryKeyAsBoxedInteger());
                        break;
                    case "BoxedLong":
                        realm.copyToRealm(new PrimaryKeyAsBoxedLong());
                        break;
                    default:
                }
                fail("Null value as primary key already exists.");
            } catch (RealmPrimaryKeyConstraintException expected) {
                assertEquals("Value already exists: null", expected.getMessage());
            } finally {
                realm.cancelTransaction();
            }
        }
    }

    @Test
    public void copyToRealm_doNotCopyReferencedObjectIfManaged() {
        realm.beginTransaction();

        // Child object is managed by Realm
        CyclicTypePrimaryKey childObj = realm.createObject(CyclicTypePrimaryKey.class);
        childObj.setName("Child");
        childObj.setId(1);

        // Parent object is a standalone object
        CyclicTypePrimaryKey parentObj = new CyclicTypePrimaryKey(2);
        parentObj.setObject(childObj);

        realm.copyToRealm(parentObj);
        realm.commitTransaction();

        assertEquals(2, realm.where(CyclicTypePrimaryKey.class).count());
    }

    @Test
    public void copyToRealm_list() {
        Dog dog1 = new Dog();
        dog1.setName("Dog 1");
        Dog dog2 = new Dog();
        dog2.setName("Dog 2");
        RealmList<Dog> list = new RealmList<Dog>();
        list.addAll(Arrays.asList(dog1, dog2));

        realm.beginTransaction();
        List<Dog> copiedList = new ArrayList<Dog>(realm.copyToRealm(list));
        realm.commitTransaction();

        assertEquals(2, copiedList.size());
        assertEquals(dog1.getName(), copiedList.get(0).getName());
        assertEquals(dog2.getName(), copiedList.get(1).getName());
    }

    @Test
    public void copyToRealm_objectInOtherThreadThrows() {
        final CountDownLatch bgThreadDoneLatch = new CountDownLatch(1);

        realm.beginTransaction();
        final Dog dog = realm.createObject(Dog.class);
        realm.commitTransaction();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Realm bgRealm = Realm.getInstance(realm.getConfiguration());
                bgRealm.beginTransaction();
                try {
                    bgRealm.copyToRealm(dog);
                    fail();
                } catch (IllegalArgumentException expected) {
                    assertEquals("Objects which belong to Realm instances in other threads cannot be copied into this" +
                                    " Realm instance.",
                            expected.getMessage());
                }
                bgRealm.cancelTransaction();
                bgRealm.close();
                bgThreadDoneLatch.countDown();
            }
        }).start();

        TestHelper.awaitOrFail(bgThreadDoneLatch);
    }

    @Test
    public void copyToRealmOrUpdate_null() {
        realm.beginTransaction();
        thrown.expect(IllegalArgumentException.class);
        realm.copyToRealmOrUpdate((AllTypes) null);
    }

    @Test
    public void copyToRealmOrUpdate_stringPrimaryKeyFieldIsNull() {
        final long SECONDARY_FIELD_VALUE = 2192841L;
        final long SECONDARY_FIELD_UPDATED = 44887612L;
        PrimaryKeyAsString nullPrimaryKeyObj = TestHelper.addStringPrimaryKeyObjectToTestRealm(realm, (String) null, SECONDARY_FIELD_VALUE);

        RealmResults<PrimaryKeyAsString> result = realm.allObjects(PrimaryKeyAsString.class);
        assertEquals(1, result.size());
        assertEquals(null, result.first().getName());
        assertEquals(SECONDARY_FIELD_VALUE, result.first().getId());

        // update objects
        realm.beginTransaction();
        nullPrimaryKeyObj.setId(SECONDARY_FIELD_UPDATED);
        realm.copyToRealmOrUpdate(nullPrimaryKeyObj);
        realm.commitTransaction();

        assertEquals(SECONDARY_FIELD_UPDATED, realm.allObjects(PrimaryKeyAsString.class).first().getId());
    }

    @Test
    public void copyToRealmOrUpdate_boxedBytePrimaryKeyFieldIsNull() {
        final String SECONDARY_FIELD_VALUE = "nullBytePrimaryKeyObj";
        final String SECONDARY_FIELD_UPDATED = "nullBytePrimaryKeyObjUpdated";
        PrimaryKeyAsBoxedByte nullPrimaryKeyObj = TestHelper.addBytePrimaryKeyObjectToTestRealm(realm, (Byte) null, SECONDARY_FIELD_VALUE);

        RealmResults<PrimaryKeyAsBoxedByte> result = realm.allObjects(PrimaryKeyAsBoxedByte.class);
        assertEquals(1, result.size());
        assertEquals(SECONDARY_FIELD_VALUE, result.first().getName());
        assertEquals(null, result.first().getId());

        // update objects
        realm.beginTransaction();
        nullPrimaryKeyObj.setName(SECONDARY_FIELD_UPDATED);
        realm.copyToRealmOrUpdate(nullPrimaryKeyObj);
        realm.commitTransaction();

        assertEquals(SECONDARY_FIELD_UPDATED, realm.allObjects(PrimaryKeyAsBoxedByte.class).first().getName());
    }

    @Test
    public void copyToRealmOrUpdate_boxedShortPrimaryKeyFieldIsNull() {
        final String SECONDARY_FIELD_VALUE = "nullShortPrimaryKeyObj";
        final String SECONDARY_FIELD_UPDATED = "nullShortPrimaryKeyObjUpdated";
        PrimaryKeyAsBoxedShort nullPrimaryKeyObj = TestHelper.addShortPrimaryKeyObjectToTestRealm(realm, (Short) null, SECONDARY_FIELD_VALUE);

        RealmResults<PrimaryKeyAsBoxedShort> result = realm.allObjects(PrimaryKeyAsBoxedShort.class);
        assertEquals(1, result.size());
        assertEquals(SECONDARY_FIELD_VALUE, result.first().getName());
        assertEquals(null, result.first().getId());

        // update objects
        realm.beginTransaction();
        nullPrimaryKeyObj.setName(SECONDARY_FIELD_UPDATED);
        realm.copyToRealmOrUpdate(nullPrimaryKeyObj);
        realm.commitTransaction();

        assertEquals(SECONDARY_FIELD_UPDATED, realm.allObjects(PrimaryKeyAsBoxedShort.class).first().getName());
    }

    @Test
    public void copyToRealmOrUpdate_boxedIntegerPrimaryKeyFieldIsNull() {
        final String SECONDARY_FIELD_VALUE = "nullIntegerPrimaryKeyObj";
        final String SECONDARY_FIELD_UPDATED = "nullIntegerPrimaryKeyObjUpdated";
        PrimaryKeyAsBoxedInteger nullPrimaryKeyObj = TestHelper.addIntegerPrimaryKeyObjectToTestRealm(realm, (Integer) null, SECONDARY_FIELD_VALUE);

        RealmResults<PrimaryKeyAsBoxedInteger> result = realm.allObjects(PrimaryKeyAsBoxedInteger.class);
        assertEquals(1, result.size());
        assertEquals(SECONDARY_FIELD_VALUE, result.first().getName());
        assertEquals(null, result.first().getId());

        // update objects
        realm.beginTransaction();
        nullPrimaryKeyObj.setName(SECONDARY_FIELD_UPDATED);
        realm.copyToRealmOrUpdate(nullPrimaryKeyObj);
        realm.commitTransaction();

        assertEquals(SECONDARY_FIELD_UPDATED, realm.allObjects(PrimaryKeyAsBoxedInteger.class).first().getName());
    }

    @Test
    public void copyToRealmOrUpdate_boxedLongPrimaryKeyFieldIsNull() {
        final String SECONDARY_FIELD_VALUE = "nullLongPrimaryKeyObj";
        final String SECONDARY_FIELD_UPDATED = "nullLongPrimaryKeyObjUpdated";
        PrimaryKeyAsBoxedLong nullPrimaryKeyObj = TestHelper.addLongPrimaryKeyObjectToTestRealm(realm, (Long) null, SECONDARY_FIELD_VALUE);

        RealmResults<PrimaryKeyAsBoxedLong> result = realm.allObjects(PrimaryKeyAsBoxedLong.class);
        assertEquals(1, result.size());
        assertEquals(SECONDARY_FIELD_VALUE, result.first().getName());
        assertEquals(null, result.first().getId());

        // update objects
        realm.beginTransaction();
        nullPrimaryKeyObj.setName(SECONDARY_FIELD_UPDATED);
        realm.copyToRealmOrUpdate(nullPrimaryKeyObj);
        realm.commitTransaction();

        assertEquals(SECONDARY_FIELD_UPDATED, realm.allObjects(PrimaryKeyAsBoxedLong.class).first().getName());
    }

    @Test
    public void copyToRealmOrUpdate_noPrimaryKeyField() {
        realm.beginTransaction();
        thrown.expect(IllegalArgumentException.class);
        realm.copyToRealmOrUpdate(new AllTypes());
    }

    @Test
    public void copyToRealmOrUpdate_addNewObjects() {
        realm.executeTransaction(new Realm.Transaction() {
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

        assertEquals(2, realm.where(PrimaryKeyAsLong.class).count());
    }

    @Test
    public void copyToRealmOrUpdate_updateExistingObject() {
        realm.executeTransaction(new Realm.Transaction() {
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

        assertEquals(1, realm.where(AllTypesPrimaryKey.class).count());
        AllTypesPrimaryKey obj = realm.where(AllTypesPrimaryKey.class).findFirst();

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
    public void copyToRealmOrUpdate_cyclicObject() {
        CyclicTypePrimaryKey oneCyclicType = new CyclicTypePrimaryKey(1);
        oneCyclicType.setName("One");
        CyclicTypePrimaryKey anotherCyclicType = new CyclicTypePrimaryKey(2);
        anotherCyclicType.setName("Two");
        oneCyclicType.setObject(anotherCyclicType);
        anotherCyclicType.setObject(oneCyclicType);

        realm.beginTransaction();
        realm.copyToRealm(oneCyclicType);
        realm.commitTransaction();

        oneCyclicType.setName("Three");
        anotherCyclicType.setName("Four");
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(oneCyclicType);
        realm.commitTransaction();

        assertEquals(2, realm.where(CyclicTypePrimaryKey.class).count());
        assertEquals("Three", realm.where(CyclicTypePrimaryKey.class).equalTo("id", 1).findFirst().getName());
    }


    // Checks that a standalone object with only default values can override data
    @Test
    public void copyToRealmOrUpdate_defaultValuesOverrideExistingData() {
        realm.executeTransaction(new Realm.Transaction() {
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

        assertEquals(1, realm.where(AllTypesPrimaryKey.class).count());

        AllTypesPrimaryKey obj = realm.where(AllTypesPrimaryKey.class).findFirst();
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
    public void copyToRealmOrUpdate_referencesNotDeleted() {
        realm.executeTransaction(new Realm.Transaction() {
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

        assertEquals(1, realm.where(AllTypesPrimaryKey.class).count());
        assertEquals(4, realm.where(DogPrimaryKey.class).count());
    }

    @Test
    public void copyToRealmOrUpdate_primaryKeyMixInObjectGraph() {
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

        realm.beginTransaction();
        PrimaryKeyMix realmObject = realm.copyToRealmOrUpdate(mixObject);
        realm.commitTransaction();

        assertEquals("Dog", realmObject.getCat().getScaredOfDog().getName());
        assertEquals("Dog", realmObject.getDogOwner().getDog().getName());
    }

    @Test
    public void copyToRealmOrUpdate_iterable() {
        realm.executeTransaction(new Realm.Transaction() {
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

        assertEquals(1, realm.where(PrimaryKeyAsLong.class).count());
        assertEquals("Baz", realm.where(PrimaryKeyAsLong.class).findFirst().getName());
    }

    // Tests that a collection of objects with references all gets copied.
    @Test
    public void copyToRealmOrUpdate_iterableChildObjects() {
        DogPrimaryKey dog = new DogPrimaryKey(1, "Snoop");

        AllTypesPrimaryKey allTypes1 = new AllTypesPrimaryKey();
        allTypes1.setColumnLong(1);
        allTypes1.setColumnRealmObject(dog);

        AllTypesPrimaryKey allTypes2 = new AllTypesPrimaryKey();
        allTypes1.setColumnLong(2);
        allTypes2.setColumnRealmObject(dog);

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(Arrays.asList(allTypes1, allTypes2));
        realm.commitTransaction();

        assertEquals(2, realm.where(AllTypesPrimaryKey.class).count());
        assertEquals(1, realm.where(DogPrimaryKey.class).count());
    }

    @Test
    public void copyToRealmOrUpdate_objectInOtherThreadThrows() {
        final CountDownLatch bgThreadDoneLatch = new CountDownLatch(1);

        realm.beginTransaction();
        final OwnerPrimaryKey ownerPrimaryKey = realm.createObject(OwnerPrimaryKey.class);
        realm.commitTransaction();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Realm bgRealm = Realm.getInstance(realm.getConfiguration());
                bgRealm.beginTransaction();
                try {
                    bgRealm.copyToRealm(ownerPrimaryKey);
                    fail();
                } catch (IllegalArgumentException expected) {
                    assertEquals("Objects which belong to Realm instances in other threads cannot be copied into this" +
                                    " Realm instance.",
                            expected.getMessage());
                }
                bgRealm.cancelTransaction();
                bgRealm.close();
                bgThreadDoneLatch.countDown();
            }
        }).start();

        TestHelper.awaitOrFail(bgThreadDoneLatch);
    }

    @Test
    public void copyToRealmOrUpdate_listHasObjectInOtherThreadThrows() {
        final CountDownLatch bgThreadDoneLatch = new CountDownLatch(1);
        final OwnerPrimaryKey ownerPrimaryKey = new OwnerPrimaryKey();

        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        realm.commitTransaction();
        ownerPrimaryKey.setDogs(new RealmList<Dog>(dog));

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Realm bgRealm = Realm.getInstance(realm.getConfiguration());
                bgRealm.beginTransaction();
                try {
                    bgRealm.copyToRealm(ownerPrimaryKey);
                    fail();
                } catch (IllegalArgumentException expected) {
                    assertEquals("Objects which belong to Realm instances in other threads cannot be copied into this" +
                                    " Realm instance.",
                            expected.getMessage());
                }
                bgRealm.cancelTransaction();
                bgRealm.close();
                bgThreadDoneLatch.countDown();
            }
        }).start();

        TestHelper.awaitOrFail(bgThreadDoneLatch);
    }

    @Test
    public void getInstance_differentEncryptionKeys() {
        byte[] key1 = TestHelper.getRandomKey(42);
        byte[] key2 = TestHelper.getRandomKey(42);

        // Make sure the key is the same, but in two different instances
        assertArrayEquals(key1, key2);
        assertTrue(key1 != key2);

        final String ENCRYPTED_REALM = "differentKeys.realm";
        Realm realm1 = null;
        Realm realm2 = null;
        try {
            realm1 = Realm.getInstance(configFactory.createConfiguration(ENCRYPTED_REALM, key1));
            try {
                realm2 = Realm.getInstance(configFactory.createConfiguration(ENCRYPTED_REALM, key2));
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
    public void writeEncryptedCopyTo() throws Exception {
        populateTestRealm();
        long before = realm.where(AllTypes.class).count();
        assertEquals(TEST_DATA_SIZE, before);

        // Configure test realms
        final String ENCRYPTED_REALM_FILE_NAME = "encryptedTestRealm.realm";
        final String RE_ENCRYPTED_REALM_FILE_NAME = "reEncryptedTestRealm.realm";
        final String DECRYPTED_REALM_FILE_NAME = "decryptedTestRealm.realm";

        RealmConfiguration encryptedRealmConfig = configFactory.createConfiguration(ENCRYPTED_REALM_FILE_NAME,
                TestHelper.getRandomKey());

        RealmConfiguration reEncryptedRealmConfig = configFactory.createConfiguration(RE_ENCRYPTED_REALM_FILE_NAME,
                TestHelper.getRandomKey());

        RealmConfiguration decryptedRealmConfig = configFactory.createConfiguration(DECRYPTED_REALM_FILE_NAME);

        // Write encrypted copy from a unencrypted Realm
        File destination = new File(encryptedRealmConfig.getPath());
        try {
            realm.writeEncryptedCopyTo(destination, encryptedRealmConfig.getEncryptionKey());
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
    public void deleteRealm_failures() {
        final String OTHER_REALM_NAME = "yetAnotherRealm.realm";

        RealmConfiguration configA = configFactory.createConfiguration();
        RealmConfiguration configB = configFactory.createConfiguration(OTHER_REALM_NAME);

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

    // TODO Does this test something meaningfull not tested elsewhere?
    @Test
    public void setter_updateField() throws Exception {
        realm.beginTransaction();

        // Create an owner with two dogs
        OwnerPrimaryKey owner = realm.createObject(OwnerPrimaryKey.class);
        owner.setId(1);
        owner.setName("Jack");
        Dog rex = realm.createObject(Dog.class);
        rex.setName("Rex");
        Dog fido = realm.createObject(Dog.class);
        fido.setName("Fido");
        owner.getDogs().add(rex);
        owner.getDogs().add(fido);
        assertEquals(2, owner.getDogs().size());

        // Changing the name of the owner should not affect the number of dogs
        owner.setName("Peter");
        assertEquals(2, owner.getDogs().size());

        // Updating the user should not affect it either. This is actually a no-op since owner is a Realm backed object
        OwnerPrimaryKey owner2 = realm.copyToRealmOrUpdate(owner);
        assertEquals(2, owner.getDogs().size());
        assertEquals(2, owner2.getDogs().size());

        realm.commitTransaction();
    }

    @Test
    public void deleteRealm() throws InterruptedException {
        File tempDir = new File(context.getFilesDir(), "delete_test_dir");
        if (!tempDir.exists()) {
            assertTrue(tempDir.mkdir());
        }

        assertTrue(tempDir.isDirectory());

        // Delete all files in the directory
        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File file : files) {
                assertTrue(file.delete());
            }
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
    public void callMutableMethodOutsideTransaction() throws JSONException, IOException {

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
        try { realm.createObject(AllTypes.class);   fail(); } catch (IllegalStateException expected) {}
        try { realm.copyToRealm(t);                 fail(); } catch (IllegalStateException expected) {}
        try { realm.copyToRealm(ts);                fail(); } catch (IllegalStateException expected) {}
        try { realm.copyToRealmOrUpdate(t);         fail(); } catch (IllegalStateException expected) {}
        try { realm.copyToRealmOrUpdate(ts);        fail(); } catch (IllegalStateException expected) {}
        try { realm.remove(AllTypes.class, 0);      fail(); } catch (IllegalStateException expected) {}
        try { realm.delete(AllTypes.class);         fail(); } catch (IllegalStateException expected) {}
        try { realm.deleteAll();                    fail(); } catch (IllegalStateException expected) {}

        try { realm.createObjectFromJson(AllTypesPrimaryKey.class, jsonObj);                fail(); } catch (IllegalStateException expected) {}
        try { realm.createObjectFromJson(AllTypesPrimaryKey.class, jsonObjStr);             fail(); } catch (IllegalStateException expected) {}
        try { realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, jsonObjStream);       fail(); } catch (IllegalStateException expected) {}
        try { realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, jsonObj);        fail(); } catch (IllegalStateException expected) {}
        try { realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, jsonObjStr);     fail(); } catch (IllegalStateException expected) {}
        try { realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, jsonObjStream2); fail(); } catch (IllegalStateException expected) {}

        try { realm.createAllFromJson(AllTypesPrimaryKey.class, jsonArr);                   fail(); } catch (IllegalStateException expected) {}
        try { realm.createAllFromJson(AllTypesPrimaryKey.class, jsonArrStr);                fail(); } catch (IllegalStateException expected) {}
        try { realm.createAllFromJson(NoPrimaryKeyNullTypes.class, jsonArrStream);          fail(); } catch (IllegalStateException expected) {}
        try { realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, jsonArr);           fail(); } catch (IllegalStateException expected) {}
        try { realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, jsonArrStr);        fail(); } catch (IllegalStateException expected) {}
        try { realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, jsonArrStream2);    fail(); } catch (IllegalStateException expected) {}
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
    public void createObject_cannotCreateDynamicRealmObject() {
        realm.beginTransaction();
        try {
            realm.createObject(DynamicRealmObject.class);
            fail();
        } catch (RealmException ignored) {
        }
    }

    @Test
    public void createObjectWithPrimaryKey() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 42);
        assertEquals(1, realm.where(AllJavaTypes.class).count());
        assertEquals(42, obj.getFieldLong());
    }

    @Test
    public void createObjectWithPrimaryKey_noPrimaryKeyField() {
        realm.beginTransaction();
        try {
            realm.createObject(AllTypes.class, 42);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void createObjectWithPrimaryKey_wrongValueType() {
        realm.beginTransaction();
        try {
            realm.createObject(AllJavaTypes.class, "fortyTwo");
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void createObjectWithPrimaryKey_valueAlreadyExists() {
        realm.beginTransaction();
        realm.createObject(AllJavaTypes.class, 42);
        try {
            realm.createObject(AllJavaTypes.class, 42);
            fail();
        } catch (RealmPrimaryKeyConstraintException ignored) {
        }
    }

    @Test
    public void createObjectWithPrimaryKey_null() {
        // Byte
        realm.beginTransaction();
        PrimaryKeyAsBoxedByte primaryKeyAsBoxedByte= realm.createObject(PrimaryKeyAsBoxedByte.class, null);
        realm.commitTransaction();
        assertEquals(1, realm.where(PrimaryKeyAsBoxedByte.class).count());
        assertNull(primaryKeyAsBoxedByte.getId());

        // Short
        realm.beginTransaction();
        PrimaryKeyAsBoxedShort primaryKeyAsBoxedShort = realm.createObject(PrimaryKeyAsBoxedShort.class, null);
        realm.commitTransaction();
        assertEquals(1, realm.where(PrimaryKeyAsBoxedShort.class).count());
        assertNull(primaryKeyAsBoxedShort.getId());

        // Integer
        realm.beginTransaction();
        PrimaryKeyAsBoxedInteger primaryKeyAsBoxedInteger = realm.createObject(PrimaryKeyAsBoxedInteger.class, null);
        realm.commitTransaction();
        assertEquals(1, realm.where(PrimaryKeyAsBoxedInteger.class).count());
        assertNull(primaryKeyAsBoxedInteger.getId());

        // Long
        realm.beginTransaction();
        PrimaryKeyAsBoxedLong primaryKeyAsBoxedLong = realm.createObject(PrimaryKeyAsBoxedLong.class, null);
        realm.commitTransaction();
        assertEquals(1, realm.where(PrimaryKeyAsBoxedLong.class).count());
        assertNull(primaryKeyAsBoxedLong.getId());

        // String
        realm.beginTransaction();
        PrimaryKeyAsString primaryKeyAsString = realm.createObject(PrimaryKeyAsString.class, null);
        realm.commitTransaction();
        assertEquals(1, realm.where(PrimaryKeyAsString.class).count());
        assertNull(primaryKeyAsString.getName());
    }

    @Test
    public void createObjectWithPrimaryKey_nullOnRequired() {
        realm.beginTransaction();

        // Byte
        try {
            realm.createObject(PrimaryKeyRequiredAsBoxedByte.class, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Short
        try {
            realm.createObject(PrimaryKeyRequiredAsBoxedShort.class, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Integer
        try {
            realm.createObject(PrimaryKeyRequiredAsBoxedInteger.class, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Long
        try {
            realm.createObject(PrimaryKeyRequiredAsBoxedLong.class, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // String
        try {
            realm.createObject(PrimaryKeyRequiredAsString.class, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        realm.cancelTransaction();
    }

    @Test
    public void createObjectWithPrimaryKey_nullDuplicated() {
        realm.beginTransaction();

        // Byte
        realm.createObject(PrimaryKeyAsBoxedByte.class, null);
        try {
            realm.createObject(PrimaryKeyAsBoxedByte.class, null);
            fail();
        } catch (RealmPrimaryKeyConstraintException ignored) {
        }

        // Short
        realm.createObject(PrimaryKeyAsBoxedShort.class, null);
        try {
            realm.createObject(PrimaryKeyAsBoxedShort.class, null);
            fail();
        } catch (RealmPrimaryKeyConstraintException ignored) {
        }

        // Integer
        realm.createObject(PrimaryKeyAsBoxedInteger.class, null);
        try {
            realm.createObject(PrimaryKeyAsBoxedInteger.class, null);
            fail();
        } catch (RealmPrimaryKeyConstraintException ignored) {
        }

        // Long
        realm.createObject(PrimaryKeyAsBoxedLong.class, null);
        try {
            realm.createObject(PrimaryKeyAsBoxedLong.class, null);
            fail();
        } catch (RealmPrimaryKeyConstraintException ignored) {
        }

        // String
        realm.createObject(PrimaryKeyAsString.class, null);
        try {
            realm.createObject(PrimaryKeyAsString.class, null);
            fail();
        } catch (RealmPrimaryKeyConstraintException ignored) {
        }

        realm.cancelTransaction();
    }

    // Test close Realm in another thread different from where it is created.
    @Test
    public void close_differentThread() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AssertionFailedError threadAssertionError[] = new AssertionFailedError[1];

        final Thread thatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    realm.close();
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
        realm.checkIfValid();
        realm.close();
        realm = null;
    }

    @Test
    public void isClosed() {
        assertFalse(realm.isClosed());
        realm.close();
        assertTrue(realm.isClosed());
    }

    // Test Realm#isClosed() in another thread different from where it is created.
    @Test
    public void isClosed_differentThread() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AssertionFailedError threadAssertionError[] = new AssertionFailedError[1];

        final Thread thatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    realm.isClosed();
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
        realm.checkIfValid();
        assertFalse(realm.isClosed());
        realm.close();
    }

    // Realm validation & initialization is done once, still ColumnIndices
    // should be populated for the subsequent Realm sharing the same configuration
    // even if we skip initialization & validation
    @Test
    public void columnIndicesIsPopulatedWhenSkippingInitialization() throws Throwable {
        final RealmConfiguration realmConfiguration = configFactory.createConfiguration("columnIndices");
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
                    TestHelper.awaitOrFail(mainThreadRealmDone);
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

        TestHelper.awaitOrFail(bgRealmOpened);
        Realm realm = Realm.getInstance(realmConfiguration);
        realm.where(AllTypes.class).equalTo("columnString", "Foo").findAll(); // This would crash if columnIndices == null
        realm.close();
        mainThreadRealmDone.countDown();
        TestHelper.awaitOrFail(bgRealmClosed);
        if (threadError[0] != null) {
            throw threadError[0];
        }
    }

    // This test assures that calling refresh will not trigger local listeners until after the Looper receives a
    // REALM_CHANGE message
    @Test
    public void processRefreshLocalListenersAfterLooperQueueStart() throws Throwable {
        // Used to validate the result
        final AtomicBoolean listenerWasCalled = new AtomicBoolean(false);
        final AtomicBoolean typeListenerWasCalled = new AtomicBoolean(false);

        // Used by the background thread to wait for the main thread to do the write operation
        final CountDownLatch bgThreadLatch = new CountDownLatch(1);
        final CountDownLatch bgClosedLatch = new CountDownLatch(2);
        final CountDownLatch bgThreadReadyLatch = new CountDownLatch(1);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);

        final Looper[] looper = new Looper[1];
        final Throwable[] throwable = new Throwable[1];

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                // this will allow to register a listener.
                // we don't start looping to prevent the callback to be invoked via
                // the handler mechanism, the purpose of this test is to make sure refresh calls
                // the listeners.
                Looper.prepare();
                looper[0] = Looper.myLooper();

                Realm bgRealm = Realm.getInstance(realmConfig);
                RealmResults<Dog> dogs = bgRealm.where(Dog.class).findAll();
                try {
                    bgRealm.addChangeListener(new RealmChangeListener<Realm>() {
                        @Override
                        public void onChange(Realm object) {
                            listenerWasCalled.set(true);
                            bgClosedLatch.countDown();
                        }
                    });
                    dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
                        @Override
                        public void onChange(RealmResults<Dog> object) {
                            typeListenerWasCalled.set(true);
                            bgClosedLatch.countDown();
                        }
                    });

                    bgThreadReadyLatch.countDown();
                    bgThreadLatch.await(); // Wait for the main thread to do a write operation
                    bgRealm.refresh(); // This should call the listener
                    assertFalse(listenerWasCalled.get());
                    assertFalse(typeListenerWasCalled.get());

                    Looper.loop();

                } catch (Throwable e) {
                    throwable[0] = e;

                } finally {
                    bgRealm.close();
                    signalClosedRealm.countDown();
                }
            }
        });

        // Wait until bgThread finishes adding listener to the RealmResults. Otherwise same TableView version won't
        // trigger the listener.
        bgThreadReadyLatch.await();
        realm.beginTransaction();
        realm.createObject(Dog.class);
        realm.commitTransaction();
        bgThreadLatch.countDown();
        bgClosedLatch.await();

        TestHelper.exitOrThrow(executorService, bgClosedLatch, signalClosedRealm, looper, throwable);

        assertTrue(listenerWasCalled.get());
        assertTrue(typeListenerWasCalled.get());
    }

    @Test
    public void isInTransaction() {
        assertFalse(realm.isInTransaction());
        realm.beginTransaction();
        assertTrue(realm.isInTransaction());
        realm.commitTransaction();
        assertFalse(realm.isInTransaction());
        realm.beginTransaction();
        assertTrue(realm.isInTransaction());
        realm.cancelTransaction();
        assertFalse(realm.isInTransaction());
    }

    // test for https://github.com/realm/realm-java/issues/1646
    @Test
    public void closingRealmWhileOtherThreadIsOpeningRealm() throws Exception {
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

                final Realm realm = Realm.getInstance(realmConfig);
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

            realm.close();
            realm = null;
        }

        endLatch.await();

        if (!exception.isEmpty()) {
            throw exception.get(0);
        }
    }

    // Bug reported https://github.com/realm/realm-java/issues/1728.
    // Root cause is validatedRealmFiles will be cleaned when any thread's Realm ref counter reach 0.
    @Test
    public void openRealmWhileTransactionInAnotherThread() throws Exception {
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
                Realm realm = Realm.getInstance(realmConfig);
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
        realm.close();
        realmClosedInFgLatch.countDown();
        transBeganInBgLatch.await();

        // Step 5: Get a new Realm instance in foreground
        realm = Realm.getInstance(realmConfig);
        fgFinishedLatch.countDown();
        bgFinishedLatch.await();

        if (!exception.isEmpty()) {
            throw exception.get(0);
        }
    }

    @Test
    public void refresh_insideTransactionThrows() {
        realm.beginTransaction();
        thrown.expect(IllegalStateException.class);
        realm.refresh();
    }

    @Test
    public void isEmpty() {
        RealmConfiguration realmConfig = configFactory.createConfiguration("empty_test.realm");
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
    public void copyFromRealm_invalidObjectThrows() {
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        obj.deleteFromRealm();
        realm.commitTransaction();

        try {
            realm.copyFromRealm(obj);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            realm.copyFromRealm(new AllTypes());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void copyFromRealm_invalidDepthThrows() {
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();
        thrown.expect(IllegalArgumentException.class);
        realm.copyFromRealm(obj, -1);
    }

    @Test
    public void copyFromRealm() {
        populateTestRealm();
        AllTypes realmObject = realm.where(AllTypes.class).findAllSorted("columnLong").first();
        AllTypes standaloneObject = realm.copyFromRealm(realmObject);
        assertArrayEquals(realmObject.getColumnBinary(), standaloneObject.getColumnBinary());
        assertEquals(realmObject.getColumnString(), standaloneObject.getColumnString());
        assertEquals(realmObject.getColumnLong(), standaloneObject.getColumnLong());
        assertEquals(realmObject.getColumnFloat(), standaloneObject.getColumnFloat(), 0.00000000001);
        assertEquals(realmObject.getColumnDouble(), standaloneObject.getColumnDouble(), 0.00000000001);
        assertEquals(realmObject.isColumnBoolean(), standaloneObject.isColumnBoolean());
        assertEquals(realmObject.getColumnDate(), standaloneObject.getColumnDate());
    }

    @Test
    public void copyFromRealm_newCopyEachTime() {
        populateTestRealm();
        AllTypes realmObject = realm.where(AllTypes.class).findAllSorted("columnLong").first();
        AllTypes standaloneObject1 = realm.copyFromRealm(realmObject);
        AllTypes standaloneObject2 = realm.copyFromRealm(realmObject);
        assertFalse(standaloneObject1 == standaloneObject2);
        assertNotSame(standaloneObject1, standaloneObject2);
    }

    // Test that the object graph is copied as it is and no extra copies are made
    // 1) (A -> B/[B,C])
    // 2) (C -> B/[B,A])
    // A copy should result in only 3 distinct objects
    @Test
    public void copyFromRealm_cyclicObjectGraph() {
        realm.beginTransaction();
        CyclicType objA = realm.createObject(CyclicType.class);
        objA.setName("A");
        CyclicType objB = realm.createObject(CyclicType.class);
        objB.setName("B");
        CyclicType objC = realm.createObject(CyclicType.class);
        objC.setName("C");
        objA.setObject(objB);
        objC.setObject(objB);
        objA.getObjects().add(objB);
        objA.getObjects().add(objC);
        objC.getObjects().add(objB);
        objC.getObjects().add(objA);
        realm.commitTransaction();

        CyclicType copyA = realm.copyFromRealm(objA);
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
    public void copyFromRealm_checkMaxDepth() {
        realm.beginTransaction();
        CyclicType objA = realm.createObject(CyclicType.class);
        objA.setName("A");
        CyclicType objB = realm.createObject(CyclicType.class);
        objB.setName("B");
        CyclicType objC = realm.createObject(CyclicType.class);
        objC.setName("C");
        objA.setObject(objB);
        objC.setObject(objC);
        objA.getObjects().add(objB);
        objA.getObjects().add(objC);
        realm.commitTransaction();

        CyclicType copyA = realm.copyFromRealm(objA, 1);

        assertNull(copyA.getObject().getObject());
    }

    // Test that depth restriction is calculated from the top-most encountered object, i.e. it is possible for some
    // objects to exceed the depth limit.
    // A -> B -> C -> D -> E
    // A -> D -> E
    // D is both at depth 1 and 3. For maxDepth = 3, E should still be copied.
    @Test
    public void copyFromRealm_sameObjectDifferentDepths() {
        realm.beginTransaction();
        CyclicType objA = realm.createObject(CyclicType.class);
        objA.setName("A");
        CyclicType objB = realm.createObject(CyclicType.class);
        objB.setName("B");
        CyclicType objC = realm.createObject(CyclicType.class);
        objC.setName("C");
        CyclicType objD = realm.createObject(CyclicType.class);
        objD.setName("D");
        CyclicType objE = realm.createObject(CyclicType.class);
        objE.setName("E");
        objA.setObject(objB);
        objB.setObject(objC);
        objC.setObject(objD);
        objD.setObject(objE);
        objA.setOtherObject(objD);
        realm.commitTransaction();

        // object is filled before otherObject (because of field order - WARNING: Not guaranteed)
        // this means that the object will be encountered first time at max depth, so E will not be copied.
        // If the object cache does not handle this, otherObject will be wrong.
        CyclicType copyA = realm.copyFromRealm(objA, 3);
        assertEquals("E", copyA.getOtherObject().getObject().getName());
    }

    @Test
    public void copyFromRealm_list_invalidListThrows() {
        realm.beginTransaction();
        AllTypes object = realm.createObject(AllTypes.class);
        List<AllTypes> list = new RealmList<AllTypes>(object);
        object.deleteFromRealm();
        realm.commitTransaction();

        thrown.expect(IllegalArgumentException.class);
        realm.copyFromRealm(list);
    }

    @Test
    public void copyFromRealm_list_invalidDepthThrows() {
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        thrown.expect(IllegalArgumentException.class);
        realm.copyFromRealm(results, -1);
    }

    // Test that the same Realm objects in a list result in the same Java in-memory copy.
    // List: A -> [(B -> C), (B -> C)] should result in only 2 copied objects A and B and not A1, B1, A2, B2
    @Test
    public void copyFromRealm_list_sameElements() {
        realm.beginTransaction();
        CyclicType objA = realm.createObject(CyclicType.class);
        objA.setName("A");
        CyclicType objB = realm.createObject(CyclicType.class);
        objB.setName("B");
        CyclicType objC = realm.createObject(CyclicType.class);
        objC.setName("C");
        objB.setObject(objC);
        objA.getObjects().add(objB);
        objA.getObjects().add(objB);
        realm.commitTransaction();

        List<CyclicType> results = realm.copyFromRealm(objA.getObjects());
        assertEquals(2, results.size());
        assertEquals("B", results.get(0).getName());
        assertTrue(results.get(0) == results.get(1));
    }

    @Test
    public void copyFromRealm_dynamicRealmObjectThrows() {
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();
        DynamicRealmObject dObj = new DynamicRealmObject(obj);

        try {
            realm.copyFromRealm(dObj);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void copyFromRealm_dynamicRealmListThrows() {
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        dynamicRealm.beginTransaction();
        RealmList<DynamicRealmObject> dynamicList = dynamicRealm.createObject(AllTypes.CLASS_NAME).getList(AllTypes.FIELD_REALMLIST);
        DynamicRealmObject dObj = dynamicRealm.createObject(Dog.CLASS_NAME);
        dynamicList.add(dObj);
        dynamicRealm.commitTransaction();
        try {
            realm.copyFromRealm(dynamicList);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            dynamicRealm.close();
        }
    }

    // Test if close can be called from Realm change listener when there is no other listeners
    @Test
    public void closeRealmInChangeListener() {
        realm.close();
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        HandlerThread handlerThread = new HandlerThread("background");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final Realm realm = Realm.getInstance(realmConfig);
                final RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
                    @Override
                    public void onChange(Realm object) {
                        if (realm.where(AllTypes.class).count() == 1) {
                            realm.removeChangeListener(this);
                            realm.close();
                            signalTestFinished.countDown();
                        }
                    }
                };

                realm.addChangeListener(listener);

                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.createObject(AllTypes.class);
                    }
                });
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
    }

    // Test if close can be called from Realm change listener when there is a listener on empty Realm Object
    @Test
    @RunTestInLooperThread
    public void closeRealmInChangeListenerWhenThereIsListenerOnEmptyObject() {
        final Realm realm = Realm.getInstance(looperThread.createConfiguration());
        final RealmChangeListener<AllTypes> dummyListener = new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object) {
            }
        };

        // Change listener on Realm
        final RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                if (realm.where(AllTypes.class).count() == 1) {
                    realm.removeChangeListener(this);
                    realm.close();
                    looperThread.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            looperThread.testComplete();
                        }
                    });
                }
            }
        };
        realm.addChangeListener(listener);

        // Change listener on Empty Object
        final AllTypes allTypes = realm.where(AllTypes.class).findFirstAsync();
        allTypes.addChangeListener(dummyListener);

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(AllTypes.class);
            }
        });
    }

    // Test if close can be called from Realm change listener when there is an listener on non-empty Realm Object
    @Test
    @RunTestInLooperThread
    public void closeRealmInChangeListenerWhenThereIsListenerOnObject() {
        final Realm realm = Realm.getInstance(looperThread.createConfiguration());
        final RealmChangeListener<AllTypes> dummyListener = new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object) {
            }
        };
        final RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                if (realm.where(AllTypes.class).count() == 2) {
                    realm.removeChangeListener(this);
                    realm.close();

                    // End test after next looper event to ensure that all listeners were called.
                    looperThread.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            looperThread.testComplete();
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
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(AllTypes.class);
            }
        });
    }

    // Test if close can be called from Realm change listener when there is an listener on RealmResults
    @Test
    @RunTestInLooperThread
    public void closeRealmInChangeListenerWhenThereIsListenerOnResults() {
        final Realm realm = Realm.getInstance(looperThread.createConfiguration());
        final RealmChangeListener<RealmResults<AllTypes>> dummyListener = new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
            }
        };
        final RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                if (realm.where(AllTypes.class).count() == 1) {
                    realm.removeChangeListener(this);
                    realm.close();
                    looperThread.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            looperThread.testComplete();
                        }
                    });
                }
            }
        };

        realm.addChangeListener(listener);

        // Step 1: Change listener on Realm results
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        results.addChangeListener(dummyListener);

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(AllTypes.class);
            }
        });
    }

    @Test
    public void removeChangeListenerThrowExceptionOnNonLooperThread() {
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);
                try {
                    realm.removeChangeListener(new RealmChangeListener<Realm>() {
                        @Override
                        public void onChange(Realm object) {
                        }
                    });
                    fail("Should not be able to invoke removeChangeListener");
                } catch (IllegalStateException ignored) {
                } finally {
                    realm.close();
                    signalTestFinished.countDown();
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

    @Test
    public void removeAllChangeListenersThrowExceptionOnNonLooperThread() {
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);
                try {
                    realm.removeAllChangeListeners();
                    fail("Should not be able to invoke removeChangeListener");
                } catch (IllegalStateException ignored) {
                } finally {
                    realm.close();
                    signalTestFinished.countDown();
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

    @Test
    public void deleteAll() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.createObject(Owner.class).setCat(realm.createObject(Cat.class));
        realm.commitTransaction();

        assertEquals(1, realm.where(AllTypes.class).count());
        assertEquals(1, realm.where(Owner.class).count());
        assertEquals(1, realm.where(Cat.class).count());

        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();

        assertEquals(0, realm.where(AllTypes.class).count());
        assertEquals(0, realm.where(Owner.class).count());
        assertEquals(0, realm.where(Cat.class).count());
        assertTrue(realm.isEmpty());
    }
}
