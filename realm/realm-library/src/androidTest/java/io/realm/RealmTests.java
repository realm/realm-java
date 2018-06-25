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
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.AssertionFailedError;

import org.hamcrest.CoreMatchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.Cat;
import io.realm.entities.CyclicType;
import io.realm.entities.CyclicTypePrimaryKey;
import io.realm.entities.DefaultValueConstructor;
import io.realm.entities.DefaultValueFromOtherConstructor;
import io.realm.entities.DefaultValueOfField;
import io.realm.entities.DefaultValueOverwriteNullLink;
import io.realm.entities.DefaultValueSetter;
import io.realm.entities.Dog;
import io.realm.entities.DogPrimaryKey;
import io.realm.entities.NoPrimaryKeyNullTypes;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.NullTypes;
import io.realm.entities.Object4957;
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
import io.realm.entities.RandomPrimaryKey;
import io.realm.entities.StringAndInt;
import io.realm.entities.StringOnly;
import io.realm.entities.StringOnlyReadOnly;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmFileException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.Table;
import io.realm.internal.util.Pair;
import io.realm.log.RealmLog;
import io.realm.objectid.NullPrimaryKey;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;
import io.realm.util.RealmThread;

import static io.realm.TestHelper.testNoObjectFound;
import static io.realm.TestHelper.testOneObjectFound;
import static io.realm.internal.test.ExtraTests.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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
    public final TemporaryFolder tmpFolder = new TemporaryFolder();
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
            allTypes.setColumnBinary(new byte[] {1, 2, 3});
            allTypes.setColumnDate(new Date());
            allTypes.setColumnDouble(Math.PI);
            allTypes.setColumnFloat(1.234567F + i);

            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            NonLatinFieldNames nonLatinFieldNames = realm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
            nonLatinFieldNames.set베타(1.234567F + i);
            nonLatinFieldNames.setΒήτα(1.234567F + i);
        }
        realm.commitTransaction();
    }

    private void populateTestRealm() {
        populateTestRealm(realm, TEST_DATA_SIZE);
    }

    @Test
    public void getInstance_writeProtectedFile() throws IOException {
        String REALM_FILE = "readonly.realm";
        File folder = configFactory.getRoot();
        File realmFile = new File(folder, REALM_FILE);
        assertFalse(realmFile.exists());
        assertTrue(realmFile.createNewFile());
        assertTrue(realmFile.setWritable(false));

        try {
            Realm.getInstance(configFactory.createConfigurationBuilder()
                    .directory(folder)
                    .name(REALM_FILE)
                    .build());
            fail();
        } catch (RealmFileException expected) {
            assertEquals(RealmFileException.Kind.PERMISSION_DENIED, expected.getKind());
        }
    }

    @Test
    public void getInstance_writeProtectedFileWithContext() throws IOException {
        String REALM_FILE = "readonly.realm";
        File folder = configFactory.getRoot();
        File realmFile = new File(folder, REALM_FILE);
        assertFalse(realmFile.exists());
        assertTrue(realmFile.createNewFile());
        assertTrue(realmFile.setWritable(false));

        try {
            Realm.getInstance(configFactory.createConfigurationBuilder().directory(folder).name(REALM_FILE).build());
            fail();
        } catch (RealmFileException expected) {
            assertEquals(RealmFileException.Kind.PERMISSION_DENIED, expected.getKind());
        }
    }

    @Test
    public void getInstance_twiceWhenRxJavaUnavailable() {
        // Test for https://github.com/realm/realm-java/issues/2416

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
    public void getInstance() {
        assertNotNull("Realm.getInstance unexpectedly returns null", realm);
        assertTrue("Realm.getInstance does not contain expected table", realm.getSchema().contains(AllTypes.CLASS_NAME));
    }

    @Test
    public void where() {
        populateTestRealm();
        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE, resultList.size());
    }

    @Test
    public void where_throwsIfClassArgIsNotASubtype() {
        try {
            realm.where(RealmObject.class);
            fail();
        } catch (IllegalArgumentException ignore) {
        }

        try {
            realm.where(RealmModel.class);
            fail();
        } catch (IllegalArgumentException ignore) {
        }
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
                realm.where(AllTypes.class).equalTo(columnData.get(i), 13.37D).findAll();
                if (i != 2) {
                    fail("Realm.where should fail with illegal argument");
                }
            } catch (IllegalArgumentException ignored) {
            }

            try {
                realm.where(AllTypes.class).equalTo(columnData.get(i), 13.3711F).findAll();
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
            realm.where(AllTypes.class).equalTo("invalidcolumnname", Math.PI).findAll();
            fail("Invalid field name");
        } catch (Exception ignored) {
        }

        try {
            realm.where(AllTypes.class).equalTo("invalidcolumnname", Math.PI).findAll();
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
        allTypes.setColumnFloat(3.14F);
        allTypes.setColumnString("a unique string");
        realm.commitTransaction();

        RealmResults<AllTypes> resultList = realm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE + 1, resultList.size());

        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_STRING, "a unique string").findAll();
        assertEquals(1, resultList.size());
        resultList = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_FLOAT, 3.14F).findAll();
        assertEquals(1, resultList.size());
    }

    @Test
    public void nestedTransaction() {
        realm.beginTransaction();
        try {
            realm.beginTransaction();
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().startsWith("The Realm is already in a write transaction"));
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
        METHOD_CREATE_OBJECT_WITH_PRIMARY_KEY,
        METHOD_COPY_TO_REALM,
        METHOD_COPY_TO_REALM_OR_UPDATE,
        METHOD_CREATE_ALL_FROM_JSON,
        METHOD_CREATE_OR_UPDATE_ALL_FROM_JSON,
        METHOD_CREATE_FROM_JSON,
        METHOD_CREATE_OR_UPDATE_FROM_JSON,
        METHOD_INSERT_COLLECTION,
        METHOD_INSERT_OBJECT,
        METHOD_INSERT_OR_UPDATE_COLLECTION,
        METHOD_INSERT_OR_UPDATE_OBJECT
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
                        case METHOD_CREATE_OBJECT_WITH_PRIMARY_KEY:
                            realm.createObject(AllJavaTypes.class, 1L);
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
                            realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, "[{\"columnLong\":1," +
                                    " \"columnBoolean\": true}]");
                            break;
                        case METHOD_CREATE_FROM_JSON:
                            realm.createObjectFromJson(AllTypes.class, "{}");
                            break;
                        case METHOD_CREATE_OR_UPDATE_FROM_JSON:
                            realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, "{\"columnLong\":1," +
                                    " \"columnBoolean\": true}");
                            break;
                        case METHOD_INSERT_COLLECTION:
                            realm.insert(Arrays.asList(new AllTypes(), new AllTypes()));
                            break;
                        case METHOD_INSERT_OBJECT:
                            realm.insert(new AllTypes());
                            break;
                        case METHOD_INSERT_OR_UPDATE_COLLECTION:
                            realm.insert(Arrays.asList(new AllTypesPrimaryKey(), new AllTypesPrimaryKey()));
                            break;
                        case METHOD_INSERT_OR_UPDATE_OBJECT:
                            realm.insertOrUpdate(new AllTypesPrimaryKey());
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
        OsSharedRealm.VersionID oldVersion = realm.sharedRealm.getVersionID();
        try {
            realm.executeTransaction(null);
            fail("null transaction should throw");
        } catch (IllegalArgumentException ignored) {
        }
        OsSharedRealm.VersionID newVersion = realm.sharedRealm.getVersionID();
        assertEquals(oldVersion, newVersion);
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
        final AtomicReference<RuntimeException> thrownException = new AtomicReference<RuntimeException>(null);

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
            // Ensures that we pass a valuable error message to the logger for developers.
            assertEquals("Could not cancel transaction, not currently in a transaction.", testLogger.message);
        } finally {
            RealmLog.remove(testLogger);
        }
        assertEquals(0, realm.where(Owner.class).count());
    }

    @Test
    public void delete_type() {
        // ** Deletes non existing table should succeed.
        realm.beginTransaction();
        realm.delete(AllTypes.class);
        realm.commitTransaction();

        // ** Deletes existing class, but leaves other classes classes.

        // Adds two classes.
        populateTestRealm();
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Castro");
        realm.commitTransaction();
        // Clears.
        realm.beginTransaction();
        realm.delete(Dog.class);
        realm.commitTransaction();
        // Checks one class is cleared but other class is still there.
        RealmResults<AllTypes> resultListTypes = realm.where(AllTypes.class).findAll();
        assertEquals(TEST_DATA_SIZE, resultListTypes.size());
        RealmResults<Dog> resultListDogs = realm.where(Dog.class).findAll();
        assertEquals(0, resultListDogs.size());

        // ** delete() must throw outside a transaction.
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

                if (i > 1) {
                    assertEquals("Codepoint: " + i + " / " + currentUnicode, codePoint,
                            o.getColumnString()); // codepoint 0 is NULL, ignore for now.
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

    // The test writes and reads random Strings.
    @Test
    public void unicodeStrings() {
        List<String> charsArray = getCharacterArray();
        // Change seed value for new random values.
        long seed = 20;
        Random random = new Random(seed);

        StringBuilder testChar = new StringBuilder();
        realm.beginTransaction();
        for (int i = 0; i < 1000; i++) {
            testChar.setLength(0);
            int length = random.nextInt(25);

            for (int j = 0; j < length; j++) {
                testChar.append(charsArray.get(random.nextInt(27261)));
            }
            StringOnly stringOnly = realm.createObject(StringOnly.class);

            // tests setter
            stringOnly.setChars(testChar.toString());

            // tests getter
            realm.where(StringOnly.class).findFirst().getChars();

            realm.delete(StringOnly.class);
        }
        realm.cancelTransaction();
    }

    @Test
    public void getInstance_referenceCounting() {
        // At this point reference count should be one because of the setUp method.
        try {
            realm.where(AllTypes.class).count();
        } catch (IllegalStateException e) {
            fail();
        }

        // Makes sure the reference counter is per realm file.
        RealmConfiguration anotherConfig = configFactory.createConfiguration("anotherRealm.realm");
        Realm.deleteRealm(anotherConfig);
        Realm otherRealm = Realm.getInstance(anotherConfig);

        // Raises the reference.
        Realm realm = null;
        try {
            realm = Realm.getInstance(configFactory.createConfiguration());
        } finally {
            if (realm != null) { realm.close(); }
        }

        try {
            // This should not fail because the reference is now 1.
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
        realm.close(); // Counts down once too many. Counter is now potentially negative.
        realm = Realm.getInstance(configFactory.createConfiguration());
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        RealmResults<AllTypes> queryResult = realm.where(AllTypes.class).findAll();
        assertEquals(allTypes, queryResult.get(0));
        realm.commitTransaction();
        realm.close(); // This might not close the Realm if the reference count is wrong.

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

        // Copy is compacted i.e. smaller than original.
        File file1 = new File(configA.getPath());
        File file2 = new File(configB.getPath());
        assertTrue(file1.length() >= file2.length());

        Realm realm2 = null;
        try {
            // Contents is copied too.
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
        assertTrue(Realm.compactRealm(realmConfig));
        realm = Realm.getInstance(realmConfig);
        assertFalse(realm.isClosed());
        assertTrue(realm.isEmpty());
        realm.close();
    }

    @Test
    public void compactRealm_encryptedPopulatedRealm() {
        final int DATA_SIZE = 100;
        RealmConfiguration realmConfig = configFactory.createConfiguration("enc.realm", TestHelper.getRandomKey());
        Realm realm = Realm.getInstance(realmConfig);

        populateTestRealm(realm, DATA_SIZE);
        realm.close();
        assertTrue(Realm.compactRealm(realmConfig));
        realm = Realm.getInstance(realmConfig);
        assertFalse(realm.isClosed());
        assertEquals(DATA_SIZE, realm.where(AllTypes.class).count());
        realm.close();
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
    public void compactRealm_onExternalStorage() {
        final File externalFilesDir = context.getExternalFilesDir(null);
        final RealmConfiguration config = configFactory.createConfigurationBuilder()
                .directory(externalFilesDir)
                .name("external.realm")
                .build();
        Realm.deleteRealm(config);
        Realm realm = Realm.getInstance(config);
        realm.close();
        assertTrue(Realm.compactRealm(config));
        realm = Realm.getInstance(config);
        realm.close();
        Realm.deleteRealm(config);
    }

    private void populateTestRealmForCompact(Realm realm, int sizeInMB) {
        byte[] oneMBData = new byte[1024 * 1024];
        realm.beginTransaction();
        for (int i = 0; i < sizeInMB; i++) {
            realm.createObject(AllTypes.class).setColumnBinary(oneMBData);
        }
        realm.commitTransaction();
    }

    private Pair<Long, Long> populateTestRealmAndCompactOnLaunch(CompactOnLaunchCallback compactOnLaunch) {
        return populateTestRealmAndCompactOnLaunch(compactOnLaunch, 1);
    }

    private Pair<Long, Long> populateTestRealmAndCompactOnLaunch(CompactOnLaunchCallback compactOnLaunch, int sizeInMB) {
        final String REALM_NAME = "test.realm";
        RealmConfiguration realmConfig = configFactory.createConfiguration(REALM_NAME);
        Realm realm = Realm.getInstance(realmConfig);
        populateTestRealmForCompact(realm, sizeInMB);
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        realm.close();
        long before = new File(realmConfig.getPath()).length();
        if (compactOnLaunch != null) {
            realmConfig = configFactory.createConfigurationBuilder()
                    .name(REALM_NAME)
                    .compactOnLaunch(compactOnLaunch)
                    .build();
        } else {
            realmConfig = configFactory.createConfigurationBuilder()
                    .name(REALM_NAME)
                    .compactOnLaunch()
                    .build();
        }
        realm = Realm.getInstance(realmConfig);
        realm.close();
        long after = new File(realmConfig.getPath()).length();
        return new Pair(before, after);
    }

    @Test
    public void compactOnLaunch_shouldCompact() throws IOException {
        Pair<Long, Long> results = populateTestRealmAndCompactOnLaunch(new CompactOnLaunchCallback() {
            @Override
            public boolean shouldCompact(long totalBytes, long usedBytes) {
                assertTrue(totalBytes > usedBytes);
                return true;
            }
        });
        assertTrue(results.first > results.second);
    }

    @Test
    public void compactOnLaunch_shouldNotCompact() throws IOException {
        Pair<Long, Long> results = populateTestRealmAndCompactOnLaunch(new CompactOnLaunchCallback() {
            @Override
            public boolean shouldCompact(long totalBytes, long usedBytes) {
                assertTrue(totalBytes > usedBytes);
                return false;
            }
        });
        assertEquals(results.first, results.second);
    }

    @Test
    public void compactOnLaunch_multipleThread() throws IOException {
        final String REALM_NAME = "test.realm";
        final AtomicInteger compactOnLaunchCount = new AtomicInteger(0);

        final RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .name(REALM_NAME)
                .compactOnLaunch(new CompactOnLaunchCallback() {
                    @Override
                    public boolean shouldCompact(long totalBytes, long usedBytes) {
                        compactOnLaunchCount.incrementAndGet();
                        return true;
                    }
                })
                .build();
        Realm realm = Realm.getInstance(realmConfig);
        realm.close();
        // WARNING: We need to init the schema first and close the Realm to make sure the relevant logic works in Object
        // Store. See https://github.com/realm/realm-object-store/blob/master/src/shared_realm.cpp#L58
        // Called once.
        assertEquals(1, compactOnLaunchCount.get());

        realm = Realm.getInstance(realmConfig);
        // Called 2 more times. The PK table migration logic (the old PK bug) needs to open/close the Realm once.
        assertEquals(3, compactOnLaunchCount.get());

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(realmConfig);
                bgRealm.close();
                // compactOnLaunch should not be called anymore!
                assertEquals(3, compactOnLaunchCount.get());
            }
        });
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            fail();
        }

        realm.close();

        assertEquals(3, compactOnLaunchCount.get());
    }

    @Test
    public void compactOnLaunch_insufficientAmount() throws IOException {
        Pair<Long, Long> results = populateTestRealmAndCompactOnLaunch(new CompactOnLaunchCallback() {
            @Override
            public boolean shouldCompact(long totalBytes, long usedBytes) {
                final long thresholdSize = 50 * 1024 * 1024;
                return (totalBytes > thresholdSize) && (((double) usedBytes / (double) totalBytes) < 0.5);
            }
        }, 1);
        final long thresholdSize = 50 * 1024 * 1024;
        assertTrue(results.first < thresholdSize);
        assertEquals(results.first, results.second);
    }

    @Test
    public void compactOnLaunch_throwsInTheCallback() {
        final RuntimeException exception = new RuntimeException();
        final RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .name("compactThrowsTest")
                .compactOnLaunch(new CompactOnLaunchCallback() {
                    @Override
                    public boolean shouldCompact(long totalBytes, long usedBytes) {
                        throw exception;
                    }
                })
                .build();
        Realm realm = null;
        try {
            realm = Realm.getInstance(realmConfig);
            fail();
        } catch (RuntimeException expected) {
            assertSame(exception, expected);
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Test
    public void defaultCompactOnLaunch() throws IOException {
        Pair<Long, Long> results = populateTestRealmAndCompactOnLaunch(null, 50);
        final long thresholdSize = 50 * 1024 * 1024;
        assertTrue(results.first > thresholdSize);
        assertTrue(results.first > results.second);
    }

    @Test
    public void defaultCompactOnLaunch_onlyCallback() {
        DefaultCompactOnLaunchCallback callback = new DefaultCompactOnLaunchCallback();
        final long thresholdSize = 50 * 1024 * 1024;
        final long big = thresholdSize + 1024;
        assertFalse(callback.shouldCompact(big, (long) (big * 0.6)));
        assertTrue(callback.shouldCompact(big, (long) (big * 0.3)));
        final long small = thresholdSize - 1024;
        assertFalse(callback.shouldCompact(small, (long) (small * 0.6)));
        assertFalse(callback.shouldCompact(small, (long) (small * 0.3)));
    }

    @Test
    public void defaultCompactOnLaunch_insufficientAmount() throws IOException {
        Pair<Long, Long> results = populateTestRealmAndCompactOnLaunch(null, 1);
        final long thresholdSize = 50 * 1024 * 1024;
        assertTrue(results.first < thresholdSize);
        assertEquals(results.first, results.second);
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

        assertNotSame(allTypes, copiedAllTypes); // Same object in different Realms is not the same.
        assertEquals(allTypes.getColumnString(), copiedAllTypes.getColumnString()); // But data is still the same.
        otherRealm.close();
    }

    @Test
    public void copyToRealm() {
        Date date = new Date();
        Dog dog = new Dog();
        dog.setName("Fido");
        RealmList<Dog> list = new RealmList<Dog>();
        list.add(dog);

        AllTypes allTypes = new AllTypes();
        allTypes.setColumnString("String");
        allTypes.setColumnLong(1L);
        allTypes.setColumnFloat(1F);
        allTypes.setColumnDouble(1D);
        allTypes.setColumnBoolean(true);
        allTypes.setColumnDate(date);
        allTypes.setColumnBinary(new byte[] {1, 2, 3});
        allTypes.setColumnRealmObject(dog);
        allTypes.setColumnRealmList(list);

        allTypes.setColumnStringList(new RealmList<String>("1"));
        allTypes.setColumnBinaryList(new RealmList<byte[]>(new byte[] {1}));
        allTypes.setColumnBooleanList(new RealmList<Boolean>(true));
        allTypes.setColumnLongList(new RealmList<Long>(1L));
        allTypes.setColumnDoubleList(new RealmList<Double>(1D));
        allTypes.setColumnFloatList(new RealmList<Float>(1F));
        allTypes.setColumnDateList(new RealmList<Date>(new Date(1L)));

        realm.beginTransaction();
        AllTypes realmTypes = realm.copyToRealm(allTypes);
        realm.commitTransaction();

        assertNotSame(allTypes, realmTypes); // Objects should not be considered equal.
        assertEquals(allTypes.getColumnString(), realmTypes.getColumnString()); // But they contain the same data.
        assertEquals(allTypes.getColumnLong(), realmTypes.getColumnLong());
        assertEquals(allTypes.getColumnFloat(), realmTypes.getColumnFloat(), 0);
        assertEquals(allTypes.getColumnDouble(), realmTypes.getColumnDouble(), 0);
        assertEquals(allTypes.isColumnBoolean(), realmTypes.isColumnBoolean());
        assertEquals(allTypes.getColumnDate(), realmTypes.getColumnDate());
        assertArrayEquals(allTypes.getColumnBinary(), realmTypes.getColumnBinary());
        assertEquals(allTypes.getColumnRealmObject().getName(), dog.getName());
        assertEquals(list.size(), realmTypes.getColumnRealmList().size());
        //noinspection ConstantConditions
        assertEquals(list.get(0).getName(), realmTypes.getColumnRealmList().get(0).getName());
        assertEquals(1, realmTypes.getColumnStringList().size());
        assertEquals("1", realmTypes.getColumnStringList().get(0));
        assertEquals(1, realmTypes.getColumnBooleanList().size());
        assertEquals(true, realmTypes.getColumnBooleanList().get(0));
        assertEquals(1, realmTypes.getColumnBinaryList().size());
        assertArrayEquals(new byte[] {1}, realmTypes.getColumnBinaryList().get(0));
        assertEquals(1, realmTypes.getColumnLongList().size());
        assertEquals((Long) 1L, realmTypes.getColumnLongList().get(0));
        assertEquals(1, realmTypes.getColumnDoubleList().size());
        assertEquals((Double) 1D, realmTypes.getColumnDoubleList().get(0));
        assertEquals(1, realmTypes.getColumnFloatList().size());
        assertEquals((Float) 1F, realmTypes.getColumnFloatList().get(0));
        assertEquals(1, realmTypes.getColumnDateList().size());
        assertEquals(new Date(1), realmTypes.getColumnDateList().get(0));
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

        // Tests copyToRealm overload that uses the Iterator.
        // Makes sure we reuse the same graph cache Map to avoid duplicates.
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();

        assertEquals(0, realm.where(CyclicType.class).count());

        realm.beginTransaction();
        List<CyclicType> cyclicTypes = realm.copyToRealm(Arrays.asList(oneCyclicType, anotherCyclicType));
        realm.commitTransaction();
        assertEquals(2, cyclicTypes.size());
        assertEquals("One", cyclicTypes.get(0).getName());
        assertEquals("Two", cyclicTypes.get(1).getName());
        assertEquals(2, realm.where(CyclicType.class).count());
    }

    @Test
    public void copyToRealm_cyclicObjectReferencesWithPK() {
        CyclicTypePrimaryKey oneCyclicType = new CyclicTypePrimaryKey(1, "One");
        CyclicTypePrimaryKey anotherCyclicType = new CyclicTypePrimaryKey(2, "Two");
        oneCyclicType.setObject(anotherCyclicType);
        anotherCyclicType.setObject(oneCyclicType);

        realm.beginTransaction();
        CyclicTypePrimaryKey realmObject = realm.copyToRealm(oneCyclicType);
        realm.commitTransaction();

        assertEquals("One", realmObject.getName());
        assertEquals("Two", realmObject.getObject().getName());
        assertEquals(2, realm.where(CyclicTypePrimaryKey.class).count());

        // Tests copyToRealm overload that uses the Iterator.
        // Makes sure we reuse the same graph cache Map to avoid duplicates.
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();

        assertEquals(0, realm.where(CyclicTypePrimaryKey.class).count());

        realm.beginTransaction();
        List<CyclicTypePrimaryKey> cyclicTypes = realm.copyToRealm(Arrays.asList(oneCyclicType, anotherCyclicType));
        realm.commitTransaction();
        assertEquals(2, cyclicTypes.size());
        assertEquals("One", cyclicTypes.get(0).getName());
        assertEquals("Two", cyclicTypes.get(1).getName());
        assertEquals(2, realm.where(CyclicTypePrimaryKey.class).count());
    }

    @Test
    public void copyToRealm_cyclicListReferences() {
        CyclicType oneCyclicType = new CyclicType();
        oneCyclicType.setName("One");
        CyclicType anotherCyclicType = new CyclicType();
        anotherCyclicType.setName("Two");
        oneCyclicType.setObjects(new RealmList<>(anotherCyclicType));
        anotherCyclicType.setObjects(new RealmList<>(oneCyclicType));

        realm.beginTransaction();
        CyclicType realmObject = realm.copyToRealm(oneCyclicType);
        realm.commitTransaction();

        assertEquals("One", realmObject.getName());
        assertEquals(2, realm.where(CyclicType.class).count());
    }

    // Checks that if a field has a null value, it gets converted to the default value for that type.
    @Test
    public void copyToRealm_convertsNullToDefaultValue() {
        realm.beginTransaction();
        AllTypes realmTypes = realm.copyToRealm(new AllTypes());
        realm.commitTransaction();

        assertEquals("", realmTypes.getColumnString());
        assertEquals(new Date(0), realmTypes.getColumnDate());
        assertArrayEquals(new byte[0], realmTypes.getColumnBinary());

        assertNotNull(realmTypes.getColumnRealmList());
        assertNotNull(realmTypes.getColumnStringList());
        assertNotNull(realmTypes.getColumnBinaryList());
        assertNotNull(realmTypes.getColumnBooleanList());
        assertNotNull(realmTypes.getColumnLongList());
        assertNotNull(realmTypes.getColumnDoubleList());
        assertNotNull(realmTypes.getColumnFloatList());
        assertNotNull(realmTypes.getColumnDateList());
    }

    // Check that using copyToRealm will set the primary key directly instead of first setting
    // it to the default value (which can fail).
    @Test
    public void copyToRealm_primaryKeyIsSetDirectly() {
        realm.beginTransaction();
        realm.createObject(OwnerPrimaryKey.class, 0);
        realm.copyToRealm(new OwnerPrimaryKey(1, "Foo"));
        realm.commitTransaction();
        assertEquals(2, realm.where(OwnerPrimaryKey.class).count());
    }

    @Test
    public void copyToRealm_stringPrimaryKeyIsNull() {
        final long SECONDARY_FIELD_VALUE = 34992142L;
        TestHelper.addStringPrimaryKeyObjectToTestRealm(realm, (String) null, SECONDARY_FIELD_VALUE);

        RealmResults<PrimaryKeyAsString> results = realm.where(PrimaryKeyAsString.class).findAll();
        assertEquals(1, results.size());
        assertEquals(null, results.first().getName());
        assertEquals(SECONDARY_FIELD_VALUE, results.first().getId());
    }

    @Test
    public void copyToRealm_boxedNumberPrimaryKeyIsNull() {
        final String SECONDARY_FIELD_VALUE = "nullNumberPrimaryKeyObj";
        final Class[] CLASSES = {PrimaryKeyAsBoxedByte.class, PrimaryKeyAsBoxedShort.class, PrimaryKeyAsBoxedInteger.class, PrimaryKeyAsBoxedLong.class};

        TestHelper.addBytePrimaryKeyObjectToTestRealm(realm, (Byte) null, SECONDARY_FIELD_VALUE);
        TestHelper.addShortPrimaryKeyObjectToTestRealm(realm, (Short) null, SECONDARY_FIELD_VALUE);
        TestHelper.addIntegerPrimaryKeyObjectToTestRealm(realm, (Integer) null, SECONDARY_FIELD_VALUE);
        TestHelper.addLongPrimaryKeyObjectToTestRealm(realm, (Long) null, SECONDARY_FIELD_VALUE);

        for (Class clazz : CLASSES) {
            RealmResults results = realm.where(clazz).findAll();
            assertEquals(1, results.size());
            assertEquals(null, ((NullPrimaryKey) results.first()).getId());
            assertEquals(SECONDARY_FIELD_VALUE, ((NullPrimaryKey) results.first()).getName());
        }
    }

    @Test
    public void copyToRealm_duplicatedNullPrimaryKeyThrows() {
        final String[] PRIMARY_KEY_TYPES = {"String", "BoxedByte", "BoxedShort", "BoxedInteger", "BoxedLong"};

        TestHelper.addStringPrimaryKeyObjectToTestRealm(realm, (String) null, 0);
        TestHelper.addBytePrimaryKeyObjectToTestRealm(realm, (Byte) null, (String) null);
        TestHelper.addShortPrimaryKeyObjectToTestRealm(realm, (Short) null, (String) null);
        TestHelper.addIntegerPrimaryKeyObjectToTestRealm(realm, (Integer) null, (String) null);
        TestHelper.addLongPrimaryKeyObjectToTestRealm(realm, (Long) null, (String) null);

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
                assertTrue("Exception message is: " + expected.getMessage(),
                        expected.getMessage().contains("Primary key value already exists: 'null' ."));
            } finally {
                realm.cancelTransaction();
            }
        }
    }

    @Test
    public void copyToRealm_doNotCopyReferencedObjectIfManaged() {
        realm.beginTransaction();

        // Child object is managed by Realm.
        CyclicTypePrimaryKey childObj = realm.createObject(CyclicTypePrimaryKey.class, 1);
        childObj.setName("Child");

        // Parent object is an unmanaged object.
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

        RealmResults<PrimaryKeyAsString> result = realm.where(PrimaryKeyAsString.class).findAll();
        assertEquals(1, result.size());
        assertEquals(null, result.first().getName());
        assertEquals(SECONDARY_FIELD_VALUE, result.first().getId());

        // Updates objects.
        realm.beginTransaction();
        nullPrimaryKeyObj.setId(SECONDARY_FIELD_UPDATED);
        realm.copyToRealmOrUpdate(nullPrimaryKeyObj);
        realm.commitTransaction();

        assertEquals(SECONDARY_FIELD_UPDATED, realm.where(PrimaryKeyAsString.class).findFirst().getId());
    }

    @Test
    public void copyToRealmOrUpdate_boxedBytePrimaryKeyFieldIsNull() {
        final String SECONDARY_FIELD_VALUE = "nullBytePrimaryKeyObj";
        final String SECONDARY_FIELD_UPDATED = "nullBytePrimaryKeyObjUpdated";
        PrimaryKeyAsBoxedByte nullPrimaryKeyObj = TestHelper.addBytePrimaryKeyObjectToTestRealm(realm, (Byte) null, SECONDARY_FIELD_VALUE);

        RealmResults<PrimaryKeyAsBoxedByte> result = realm.where(PrimaryKeyAsBoxedByte.class).findAll();
        assertEquals(1, result.size());
        assertEquals(SECONDARY_FIELD_VALUE, result.first().getName());
        assertEquals(null, result.first().getId());

        // Updates objects.
        realm.beginTransaction();
        nullPrimaryKeyObj.setName(SECONDARY_FIELD_UPDATED);
        realm.copyToRealmOrUpdate(nullPrimaryKeyObj);
        realm.commitTransaction();

        assertEquals(SECONDARY_FIELD_UPDATED, realm.where(PrimaryKeyAsBoxedByte.class).findFirst().getName());
    }

    @Test
    public void copyToRealmOrUpdate_boxedShortPrimaryKeyFieldIsNull() {
        final String SECONDARY_FIELD_VALUE = "nullShortPrimaryKeyObj";
        final String SECONDARY_FIELD_UPDATED = "nullShortPrimaryKeyObjUpdated";
        PrimaryKeyAsBoxedShort nullPrimaryKeyObj = TestHelper.addShortPrimaryKeyObjectToTestRealm(realm, (Short) null, SECONDARY_FIELD_VALUE);

        RealmResults<PrimaryKeyAsBoxedShort> result = realm.where(PrimaryKeyAsBoxedShort.class).findAll();
        assertEquals(1, result.size());
        assertEquals(SECONDARY_FIELD_VALUE, result.first().getName());
        assertEquals(null, result.first().getId());

        // Updates objects.
        realm.beginTransaction();
        nullPrimaryKeyObj.setName(SECONDARY_FIELD_UPDATED);
        realm.copyToRealmOrUpdate(nullPrimaryKeyObj);
        realm.commitTransaction();

        assertEquals(SECONDARY_FIELD_UPDATED, realm.where(PrimaryKeyAsBoxedShort.class).findFirst().getName());
    }

    @Test
    public void copyToRealmOrUpdate_boxedIntegerPrimaryKeyFieldIsNull() {
        final String SECONDARY_FIELD_VALUE = "nullIntegerPrimaryKeyObj";
        final String SECONDARY_FIELD_UPDATED = "nullIntegerPrimaryKeyObjUpdated";
        PrimaryKeyAsBoxedInteger nullPrimaryKeyObj = TestHelper.addIntegerPrimaryKeyObjectToTestRealm(realm, (Integer) null, SECONDARY_FIELD_VALUE);

        RealmResults<PrimaryKeyAsBoxedInteger> result = realm.where(PrimaryKeyAsBoxedInteger.class).findAll();
        assertEquals(1, result.size());
        assertEquals(SECONDARY_FIELD_VALUE, result.first().getName());
        assertEquals(null, result.first().getId());

        // Updates objects.
        realm.beginTransaction();
        nullPrimaryKeyObj.setName(SECONDARY_FIELD_UPDATED);
        realm.copyToRealmOrUpdate(nullPrimaryKeyObj);
        realm.commitTransaction();

        assertEquals(SECONDARY_FIELD_UPDATED, realm.where(PrimaryKeyAsBoxedInteger.class).findFirst().getName());
    }

    @Test
    public void copyToRealmOrUpdate_boxedLongPrimaryKeyFieldIsNull() {
        final String SECONDARY_FIELD_VALUE = "nullLongPrimaryKeyObj";
        final String SECONDARY_FIELD_UPDATED = "nullLongPrimaryKeyObjUpdated";
        PrimaryKeyAsBoxedLong nullPrimaryKeyObj = TestHelper.addLongPrimaryKeyObjectToTestRealm(realm, (Long) null, SECONDARY_FIELD_VALUE);

        RealmResults<PrimaryKeyAsBoxedLong> result = realm.where(PrimaryKeyAsBoxedLong.class).findAll();
        assertEquals(1, result.size());
        assertEquals(SECONDARY_FIELD_VALUE, result.first().getName());
        assertEquals(null, result.first().getId());

        // Updates objects.
        realm.beginTransaction();
        nullPrimaryKeyObj.setName(SECONDARY_FIELD_UPDATED);
        realm.copyToRealmOrUpdate(nullPrimaryKeyObj);
        realm.commitTransaction();

        assertEquals(SECONDARY_FIELD_UPDATED, realm.where(PrimaryKeyAsBoxedLong.class).findFirst().getName());
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
                obj.setColumnBinary(new byte[] {1, 2, 3});
                obj.setColumnDate(new Date(1000));
                obj.setColumnRealmObject(new DogPrimaryKey(1, "Dog1"));
                obj.setColumnRealmList(new RealmList<DogPrimaryKey>(new DogPrimaryKey(2, "Dog2")));
                obj.setColumnBoxedBoolean(true);
                obj.setColumnStringList(new RealmList<>("1"));
                obj.setColumnBooleanList(new RealmList<>(false));
                obj.setColumnBinaryList(new RealmList<>(new byte[] {1}));
                obj.setColumnLongList(new RealmList<>(1L));
                obj.setColumnDoubleList(new RealmList<>(1D));
                obj.setColumnFloatList(new RealmList<>(1F));
                obj.setColumnDateList(new RealmList<>(new Date(1L)));
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
                obj2.setColumnBoxedBoolean(false);
                obj2.setColumnStringList(new RealmList<>("2", "3"));
                obj2.setColumnBooleanList(new RealmList<>(true, false));
                obj2.setColumnBinaryList(new RealmList<>(new byte[] {2}, new byte[] {3}));
                obj2.setColumnLongList(new RealmList<>(2L, 3L));
                obj2.setColumnDoubleList(new RealmList<>(2D, 3D));
                obj2.setColumnFloatList(new RealmList<>(2F, 3F));
                obj2.setColumnDateList(new RealmList<>(new Date(2L), new Date(3L)));
                realm.copyToRealmOrUpdate(obj2);
            }
        });

        assertEquals(1, realm.where(AllTypesPrimaryKey.class).count());
        AllTypesPrimaryKey obj = realm.where(AllTypesPrimaryKey.class).findFirst();

        // Checks that the the only element has all its properties updated.
        assertEquals("Bar", obj.getColumnString());
        assertEquals(1, obj.getColumnLong());
        assertEquals(2.23F, obj.getColumnFloat(), 0);
        assertEquals(2.234D, obj.getColumnDouble(), 0);
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[] {2, 3, 4}, obj.getColumnBinary());
        assertEquals(new Date(2000), obj.getColumnDate());
        assertEquals("Dog3", obj.getColumnRealmObject().getName());
        assertEquals(1, obj.getColumnRealmList().size());
        assertEquals("Dog4", obj.getColumnRealmList().get(0).getName());
        assertFalse(obj.getColumnBoxedBoolean());
        assertEquals(2, obj.getColumnStringList().size());
        assertEquals("2", obj.getColumnStringList().get(0));
        assertEquals("3", obj.getColumnStringList().get(1));
        assertEquals(2, obj.getColumnBooleanList().size());
        assertEquals(true, obj.getColumnBooleanList().get(0));
        assertEquals(false, obj.getColumnBooleanList().get(1));
        assertEquals(2, obj.getColumnBinaryList().size());
        assertArrayEquals(new byte[] {2}, obj.getColumnBinaryList().get(0));
        assertArrayEquals(new byte[] {3}, obj.getColumnBinaryList().get(1));
        assertEquals(2, obj.getColumnLongList().size());
        assertEquals((Long) 2L, obj.getColumnLongList().get(0));
        assertEquals((Long) 3L, obj.getColumnLongList().get(1));
        assertEquals(2, obj.getColumnDoubleList().size());
        assertEquals((Double) 2D, obj.getColumnDoubleList().get(0));
        assertEquals((Double) 3D, obj.getColumnDoubleList().get(1));
        assertEquals(2, obj.getColumnFloatList().size());
        assertEquals((Float) 2F, obj.getColumnFloatList().get(0));
        assertEquals((Float) 3F, obj.getColumnFloatList().get(1));
        assertEquals(2, obj.getColumnDateList().size());
        assertEquals(new Date(2L), obj.getColumnDateList().get(0));
        assertEquals(new Date(3L), obj.getColumnDateList().get(1));
    }

    @Test
    public void copyToRealmOrUpdate_overrideOwnList() {
        realm.beginTransaction();
        AllJavaTypes managedObj = realm.createObject(AllJavaTypes.class, 1);
        managedObj.getFieldList().add(managedObj);
        AllJavaTypes unmanagedObj = realm.copyFromRealm(managedObj);
        unmanagedObj.setFieldList(managedObj.getFieldList());

        managedObj = realm.copyToRealmOrUpdate(unmanagedObj);
        assertEquals(1, managedObj.getFieldList().size());
        assertEquals(1, managedObj.getFieldList().first().getFieldId());
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


    // Checks that an unmanaged object with only default values can override data.
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
                obj.setColumnBinary(new byte[] {1, 2, 3});
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


    // Tests that if references to objects are removed, the objects are still in the Realm.
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
        final OwnerPrimaryKey ownerPrimaryKey = realm.createObject(OwnerPrimaryKey.class, 0);
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

    // Test to reproduce issue https://github.com/realm/realm-java/issues/4957
    @Test
    public void copyToRealmOrUpdate_bug4957() {
        Object4957 listElement = new Object4957();
        listElement.setId(1);

        Object4957 parent = new Object4957();
        parent.setId(0);
        parent.getChildList().add(listElement);

        // parentCopy has same fields as the parent does. But they are not the same object.
        Object4957 parentCopy = new Object4957();
        parentCopy.setId(0);
        parentCopy.getChildList().add(listElement);

        parent.setChild(parentCopy);
        parentCopy.setChild(parentCopy);

        realm.beginTransaction();
        Object4957 managedParent = realm.copyToRealmOrUpdate(parent);
        realm.commitTransaction();
        // The original bug fails here. It resulted the listElement has been added to the list twice.
        // Because of the parent and parentCopy are not the same object, proxy will miss the cache to know the object
        // has been created before. But it does know they share the same PK value.
        assertEquals(1, managedParent.getChildList().size());

        // insertOrUpdate doesn't have the problem!
        realm.beginTransaction();
        realm.deleteAll();
        realm.insertOrUpdate(parent);
        realm.commitTransaction();
        managedParent = realm.where(Object4957.class).findFirst();
        assertEquals(1, managedParent.getChildList().size());
    }

    @Test
    public void getInstance_differentEncryptionKeys() {
        byte[] key1 = TestHelper.getRandomKey(42);
        byte[] key2 = TestHelper.getRandomKey(42);

        // Makes sure the key is the same, but in two different instances.
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
                fail("Unexpected exception: " + e);
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

        // Configures test realms.
        final String ENCRYPTED_REALM_FILE_NAME = "encryptedTestRealm.realm";
        final String RE_ENCRYPTED_REALM_FILE_NAME = "reEncryptedTestRealm.realm";
        final String DECRYPTED_REALM_FILE_NAME = "decryptedTestRealm.realm";

        RealmConfiguration encryptedRealmConfig = configFactory.createConfiguration(ENCRYPTED_REALM_FILE_NAME,
                TestHelper.getRandomKey());

        RealmConfiguration reEncryptedRealmConfig = configFactory.createConfiguration(RE_ENCRYPTED_REALM_FILE_NAME,
                TestHelper.getRandomKey());

        RealmConfiguration decryptedRealmConfig = configFactory.createConfiguration(DECRYPTED_REALM_FILE_NAME);

        // Writes encrypted copy from a unencrypted Realm.
        File destination = new File(encryptedRealmConfig.getPath());
        realm.writeEncryptedCopyTo(destination, encryptedRealmConfig.getEncryptionKey());

        Realm encryptedRealm = null;
        try {

            // Verifies encrypted Realm and writes new encrypted copy with a new key.
            encryptedRealm = Realm.getInstance(encryptedRealmConfig);
            assertEquals(TEST_DATA_SIZE, encryptedRealm.where(AllTypes.class).count());

            destination = new File(reEncryptedRealmConfig.getPath());
            encryptedRealm.writeEncryptedCopyTo(destination, reEncryptedRealmConfig.getEncryptionKey());

            // Verifies re-encrypted copy.
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

            // Writes non-encrypted copy from the encrypted version.
            destination = new File(decryptedRealmConfig.getPath());
            encryptedRealm.writeEncryptedCopyTo(destination, null);

            // Verifies decrypted Realm and cleans up.
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
    public void writeEncryptedCopyTo_wrongKeyLength() {
        byte[] wrongLengthKey = new byte[42];
        File destination = new File(configFactory.getRoot(), "wrong_key.realm");
        thrown.expect(IllegalArgumentException.class);
        realm.writeEncryptedCopyTo(destination, wrongLengthKey);
    }

    @Test
    public void deleteRealm_failures() {
        final String OTHER_REALM_NAME = "yetAnotherRealm.realm";

        RealmConfiguration configA = configFactory.createConfiguration();
        RealmConfiguration configB = configFactory.createConfiguration(OTHER_REALM_NAME);

        // This instance is already cached because of the setUp() method so this deletion should throw.
        try {
            Realm.deleteRealm(configA);
            fail();
        } catch (IllegalStateException ignored) {
        }

        // Creates a new Realm file.
        Realm yetAnotherRealm = Realm.getInstance(configB);

        // Deleting it should fail.
        try {
            Realm.deleteRealm(configB);
            fail();
        } catch (IllegalStateException ignored) {
        }

        // But now that we close it deletion should work.
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

        // Creates an owner with two dogs.
        OwnerPrimaryKey owner = realm.createObject(OwnerPrimaryKey.class, 1);
        owner.setName("Jack");
        Dog rex = realm.createObject(Dog.class);
        rex.setName("Rex");
        Dog fido = realm.createObject(Dog.class);
        fido.setName("Fido");
        owner.getDogs().add(rex);
        owner.getDogs().add(fido);
        assertEquals(2, owner.getDogs().size());

        // Changing the name of the owner should not affect the number of dogs.
        owner.setName("Peter");
        assertEquals(2, owner.getDogs().size());

        // Updating the user should not affect it either. This is actually a no-op since owner is a Realm backed object.
        OwnerPrimaryKey owner2 = realm.copyToRealmOrUpdate(owner);
        assertEquals(2, owner.getDogs().size());
        assertEquals(2, owner2.getDogs().size());

        realm.commitTransaction();
    }

    @Test
    public void deleteRealm() throws InterruptedException {
        File tempDir = new File(configFactory.getRoot(), "delete_test_dir");
        File tempDirRenamed = new File(configFactory.getRoot(), "delete_test_dir_2");
        assertTrue(tempDir.mkdir());

        final RealmConfiguration configuration = configFactory.createConfigurationBuilder()
                .directory(tempDir)
                .build();

        final CountDownLatch bgThreadReadyLatch = new CountDownLatch(1);
        final CountDownLatch readyToCloseLatch = new CountDownLatch(1);
        final CountDownLatch closedLatch = new CountDownLatch(1);

        Realm realm = Realm.getInstance(configuration);
        // Creates another Realm to ensure the log files are generated.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(configuration);
                bgThreadReadyLatch.countDown();
                TestHelper.awaitOrFail(readyToCloseLatch);
                realm.close();
                closedLatch.countDown();
            }
        }).start();

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        // Waits for bg thread's opening the same Realm.
        TestHelper.awaitOrFail(bgThreadReadyLatch);

        // A core upgrade might change the location of the files
        assertTrue(tempDir.renameTo(tempDirRenamed));
        readyToCloseLatch.countDown();

        realm.close();
        TestHelper.awaitOrFail(closedLatch);
        // Now we get log files back!
        assertTrue(tempDirRenamed.renameTo(tempDir));

        assertTrue(Realm.deleteRealm(configuration));

        assertEquals(1, tempDir.listFiles().length);

        // Lock file should never be deleted
        File lockFile = new File(configuration.getPath() + ".lock");
        assertTrue(lockFile.exists());
    }

    // Tests that all methods that require a transaction. (ie. any function that mutates Realm data)
    @Test
    public void callMutableMethodOutsideTransaction() throws JSONException, IOException {

        // Prepares unmanaged object data.
        AllTypesPrimaryKey t = new AllTypesPrimaryKey();
        List<AllTypesPrimaryKey> ts = Arrays.asList(t, t);

        // Prepares JSON data.
        String jsonObjStr = "{ \"columnLong\" : 1 }";
        JSONObject jsonObj = new JSONObject(jsonObjStr);
        InputStream jsonObjStream = TestHelper.stringToStream(jsonObjStr);
        InputStream jsonObjStream2 = TestHelper.stringToStream(jsonObjStr);

        String jsonArrStr = " [{ \"columnLong\" : 1 }] ";
        JSONArray jsonArr = new JSONArray(jsonArrStr);
        InputStream jsonArrStream = TestHelper.stringToStream(jsonArrStr);
        InputStream jsonArrStream2 = TestHelper.stringToStream(jsonArrStr);

        // Tests all methods that should require a transaction.
        try {
            realm.createObject(AllTypes.class);
            fail();
        } catch (IllegalStateException expected) {}
        try {
            realm.copyToRealm(t);
            fail();
        } catch (IllegalStateException expected) {}
        try {
            realm.copyToRealm(ts);
            fail();
        } catch (IllegalStateException expected) {}
        try {
            realm.copyToRealmOrUpdate(t);
            fail();
        } catch (IllegalStateException expected) {}
        try {
            realm.copyToRealmOrUpdate(ts);
            fail();
        } catch (IllegalStateException expected) {}
        try {
            realm.delete(AllTypes.class);
            fail();
        } catch (IllegalStateException expected) {}
        try {
            realm.deleteAll();
            fail();
        } catch (IllegalStateException expected) {}

        try {
            realm.createObjectFromJson(AllTypesPrimaryKey.class, jsonObj);
            fail();
        } catch (IllegalStateException expected) {}
        try {
            realm.createObjectFromJson(AllTypesPrimaryKey.class, jsonObjStr);
            fail();
        } catch (IllegalStateException expected) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                realm.createObjectFromJson(NoPrimaryKeyNullTypes.class, jsonObjStream);
                fail();
            } catch (IllegalStateException expected) {}
        }
        try {
            realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, jsonObj);
            fail();
        } catch (IllegalStateException expected) {}
        try {
            realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, jsonObjStr);
            fail();
        } catch (IllegalStateException expected) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                realm.createOrUpdateObjectFromJson(AllTypesPrimaryKey.class, jsonObjStream2);
                fail();
            } catch (IllegalStateException expected) {}
        }

        try {
            realm.createAllFromJson(AllTypesPrimaryKey.class, jsonArr);
            fail();
        } catch (IllegalStateException expected) {}
        try {
            realm.createAllFromJson(AllTypesPrimaryKey.class, jsonArrStr);
            fail();
        } catch (IllegalStateException expected) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                realm.createAllFromJson(NoPrimaryKeyNullTypes.class, jsonArrStream);
                fail();
            } catch (IllegalStateException expected) {}
        }
        try {
            realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, jsonArr);
            fail();
        } catch (IllegalStateException expected) {}
        try {
            realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, jsonArrStr);
            fail();
        } catch (IllegalStateException expected) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                realm.createOrUpdateAllFromJson(AllTypesPrimaryKey.class, jsonArrStream2);
                fail();
            } catch (IllegalStateException expected) {}
        }
    }

    @Test
    public void createObject_cannotCreateDynamicRealmObject() {
        realm.beginTransaction();
        try {
            realm.createObject(DynamicRealmObject.class);
            fail();
        } catch (RealmException ignored) {
        }
    }

    @Test(expected = RealmException.class)
    public void createObject_absentPrimaryKeyThrows() {
        realm.beginTransaction();
        realm.createObject(DogPrimaryKey.class);
    }

    @Test
    public void createObjectWithPrimaryKey() {
        realm.beginTransaction();
        AllJavaTypes obj = realm.createObject(AllJavaTypes.class, 42);
        assertEquals(1, realm.where(AllJavaTypes.class).count());
        assertEquals(42, obj.getFieldId());
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
        PrimaryKeyAsBoxedByte primaryKeyAsBoxedByte = realm.createObject(PrimaryKeyAsBoxedByte.class, null);
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

    @Test
    public void createObject_defaultValueFromModelField() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // Creates a DefaultValueOfField with non-default primary key value.
                realm.createObject(DefaultValueOfField.class,
                        DefaultValueOfField.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE * 3);
            }
        });
        final String createdRandomString = DefaultValueOfField.lastRandomStringValue;

        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_STRING,
                DefaultValueOfField.FIELD_STRING_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_RANDOM_STRING, createdRandomString);
        testOneObjectFound(realm, DefaultValueOfField.class, DefaultValueOfField.FIELD_SHORT,
                DefaultValueOfField.FIELD_SHORT_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_INT,
                DefaultValueOfField.FIELD_INT_DEFAULT_VALUE);
        // Default value for pk must be ignored.
        testNoObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_LONG_PRIMARY_KEY,
                DefaultValueOfField.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_LONG_PRIMARY_KEY,
                DefaultValueOfField.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE * 3);
        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_LONG,
                DefaultValueOfField.FIELD_LONG_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_BYTE,
                DefaultValueOfField.FIELD_BYTE_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_FLOAT,
                DefaultValueOfField.FIELD_FLOAT_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_DOUBLE,
                DefaultValueOfField.FIELD_DOUBLE_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_BOOLEAN,
                DefaultValueOfField.FIELD_BOOLEAN_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_DATE,
                DefaultValueOfField.FIELD_DATE_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_BINARY,
                DefaultValueOfField.FIELD_BINARY_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_OBJECT + "." + RandomPrimaryKey.FIELD_INT,
                RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueOfField.class,
                DefaultValueOfField.FIELD_LIST + "." + RandomPrimaryKey.FIELD_INT,
                RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE);
    }

    @Test
    public void createObject_overwriteNullifiedLinkWithDefaultValue() {
        final DefaultValueOverwriteNullLink created;
        realm.beginTransaction();
        created = realm.createObject(DefaultValueOverwriteNullLink.class);
        realm.commitTransaction();

        assertEquals(created.getExpectedKeyOfFieldObject(), created.getFieldObject().getFieldRandomPrimaryKey());
    }

    @Test
    public void createObject_defaultValueFromModelConstructor() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // Creates a DefaultValueConstructor with non-default primary key value.
                realm.createObject(DefaultValueConstructor.class,
                        DefaultValueConstructor.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE * 3);
            }
        });
        final String createdRandomString = DefaultValueConstructor.lastRandomStringValue;

        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_STRING,
                DefaultValueConstructor.FIELD_STRING_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_RANDOM_STRING,
                createdRandomString);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_SHORT,
                DefaultValueConstructor.FIELD_SHORT_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_INT,
                DefaultValueConstructor.FIELD_INT_DEFAULT_VALUE);
        // Default value for pk must be ignored.
        testNoObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_LONG_PRIMARY_KEY,
                DefaultValueConstructor.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_LONG_PRIMARY_KEY,
                DefaultValueConstructor.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE * 3);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_LONG,
                DefaultValueConstructor.FIELD_LONG_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_BYTE,
                DefaultValueConstructor.FIELD_BYTE_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_FLOAT,
                DefaultValueConstructor.FIELD_FLOAT_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_DOUBLE,
                DefaultValueConstructor.FIELD_DOUBLE_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_BOOLEAN,
                DefaultValueConstructor.FIELD_BOOLEAN_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_DATE, DefaultValueConstructor.FIELD_DATE_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_BINARY,
                DefaultValueConstructor.FIELD_BINARY_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_OBJECT + "." + RandomPrimaryKey.FIELD_INT,
                RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueConstructor.class,
                DefaultValueConstructor.FIELD_LIST + "." + RandomPrimaryKey.FIELD_INT,
                RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE);
    }

    @Test
    public void createObject_defaultValueSetterInConstructor() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // Creates a DefaultValueSetter with non-default primary key value.
                realm.createObject(DefaultValueSetter.class,
                        DefaultValueSetter.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE * 3);
            }
        });
        final String createdRandomString = DefaultValueSetter.lastRandomStringValue;

        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_STRING,
                DefaultValueSetter.FIELD_STRING_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_RANDOM_STRING,
                createdRandomString);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_SHORT,
                DefaultValueSetter.FIELD_SHORT_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_INT,
                DefaultValueSetter.FIELD_INT_DEFAULT_VALUE);
        // Default value for pk must be ignored.
        testNoObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_LONG_PRIMARY_KEY,
                DefaultValueSetter.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_LONG_PRIMARY_KEY,
                DefaultValueSetter.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE * 3);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_LONG,
                DefaultValueSetter.FIELD_LONG_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_BYTE,
                DefaultValueSetter.FIELD_BYTE_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_FLOAT,
                DefaultValueSetter.FIELD_FLOAT_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_DOUBLE,
                DefaultValueSetter.FIELD_DOUBLE_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_BOOLEAN,
                DefaultValueSetter.FIELD_BOOLEAN_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_DATE,
                DefaultValueSetter.FIELD_DATE_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_BINARY,
                DefaultValueSetter.FIELD_BINARY_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_OBJECT + "." + RandomPrimaryKey.FIELD_INT,
                RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_LIST + "." + RandomPrimaryKey.FIELD_INT,
                RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE);
        testOneObjectFound(realm, DefaultValueSetter.class,
                DefaultValueSetter.FIELD_LIST + "." + RandomPrimaryKey.FIELD_INT,
                RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 1);
    }

    @Test
    public void createObject_defaultValueFromOtherConstructor() {
        realm.beginTransaction();
        DefaultValueFromOtherConstructor obj = realm.createObject(DefaultValueFromOtherConstructor.class);
        realm.commitTransaction();

        assertEquals(42, obj.getFieldLong());
    }

    @Test
    public void copyToRealm_defaultValuesAreIgnored() {
        final String fieldIgnoredValue = DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE + ".modified";
        final String fieldStringValue = DefaultValueOfField.FIELD_STRING_DEFAULT_VALUE + ".modified";
        final String fieldRandomStringValue = "non-random";
        final short fieldShortValue = (short) (DefaultValueOfField.FIELD_SHORT_DEFAULT_VALUE + 1);
        final int fieldIntValue = DefaultValueOfField.FIELD_INT_DEFAULT_VALUE + 1;
        final long fieldLongPrimaryKeyValue = DefaultValueOfField.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE + 1;
        final long fieldLongValue = DefaultValueOfField.FIELD_LONG_DEFAULT_VALUE + 1;
        final byte fieldByteValue = (byte) (DefaultValueOfField.FIELD_BYTE_DEFAULT_VALUE + 1);
        final float fieldFloatValue = DefaultValueOfField.FIELD_FLOAT_DEFAULT_VALUE + 1;
        final double fieldDoubleValue = DefaultValueOfField.FIELD_DOUBLE_DEFAULT_VALUE + 1;
        final boolean fieldBooleanValue = !DefaultValueOfField.FIELD_BOOLEAN_DEFAULT_VALUE;
        final Date fieldDateValue = new Date(DefaultValueOfField.FIELD_DATE_DEFAULT_VALUE.getTime() + 1);
        final byte[] fieldBinaryValue = {(byte) (DefaultValueOfField.FIELD_BINARY_DEFAULT_VALUE[0] - 1)};
        final int fieldObjectIntValue = RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 1;
        final int fieldListIntValue = RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 2;

        final DefaultValueOfField managedObj;
        realm.beginTransaction();
        {
            final DefaultValueOfField obj = new DefaultValueOfField();
            obj.setFieldIgnored(fieldIgnoredValue);
            obj.setFieldString(fieldStringValue);
            obj.setFieldRandomString(fieldRandomStringValue);
            obj.setFieldShort(fieldShortValue);
            obj.setFieldInt(fieldIntValue);
            obj.setFieldLongPrimaryKey(fieldLongPrimaryKeyValue);
            obj.setFieldLong(fieldLongValue);
            obj.setFieldByte(fieldByteValue);
            obj.setFieldFloat(fieldFloatValue);
            obj.setFieldDouble(fieldDoubleValue);
            obj.setFieldBoolean(fieldBooleanValue);
            obj.setFieldDate(fieldDateValue);
            obj.setFieldBinary(fieldBinaryValue);

            final RandomPrimaryKey fieldObjectValue = new RandomPrimaryKey();
            fieldObjectValue.setFieldInt(fieldObjectIntValue);
            obj.setFieldObject(fieldObjectValue);

            final RealmList<RandomPrimaryKey> list = new RealmList<RandomPrimaryKey>();
            final RandomPrimaryKey listItem = new RandomPrimaryKey();
            listItem.setFieldInt(fieldListIntValue);
            list.add(listItem);
            obj.setFieldList(list);

            obj.setFieldStringList(new RealmList<>("2", "3"));
            obj.setFieldBooleanList(new RealmList<>(true, false));
            obj.setFieldBinaryList(new RealmList<>(new byte[] {2}, new byte[] {3}));
            obj.setFieldLongList(new RealmList<>(2L, 3L));
            obj.setFieldIntegerList(new RealmList<>(2, 3));
            obj.setFieldShortList(new RealmList<>((short) 2, (short) 3));
            obj.setFieldByteList(new RealmList<>((byte) 2, (byte) 3));
            obj.setFieldDoubleList(new RealmList<>(2D, 3D));
            obj.setFieldFloatList(new RealmList<>(2F, 3F));
            obj.setFieldDateList(new RealmList<>(new Date(2L), new Date(3L)));

            managedObj = realm.copyToRealm(obj);
        }
        realm.commitTransaction();

        assertEquals(DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE/*not fieldIgnoredValue*/,
                managedObj.getFieldIgnored());
        assertEquals(fieldStringValue, managedObj.getFieldString());
        assertEquals(fieldRandomStringValue, managedObj.getFieldRandomString());
        assertEquals(fieldShortValue, managedObj.getFieldShort());
        assertEquals(fieldIntValue, managedObj.getFieldInt());
        assertEquals(fieldLongPrimaryKeyValue, managedObj.getFieldLongPrimaryKey());
        assertEquals(fieldLongValue, managedObj.getFieldLong());
        assertEquals(fieldByteValue, managedObj.getFieldByte());
        assertEquals(fieldFloatValue, managedObj.getFieldFloat(), 0F);
        assertEquals(fieldDoubleValue, managedObj.getFieldDouble(), 0D);
        assertEquals(fieldBooleanValue, managedObj.isFieldBoolean());
        assertEquals(fieldDateValue, managedObj.getFieldDate());
        assertArrayEquals(fieldBinaryValue, managedObj.getFieldBinary());
        assertEquals(fieldObjectIntValue, managedObj.getFieldObject().getFieldInt());
        assertEquals(1, managedObj.getFieldList().size());
        assertEquals(fieldListIntValue, managedObj.getFieldList().first().getFieldInt());

        assertEquals(2, managedObj.getFieldStringList().size());
        assertEquals("2", managedObj.getFieldStringList().get(0));
        assertEquals("3", managedObj.getFieldStringList().get(1));
        assertEquals(2, managedObj.getFieldBooleanList().size());
        assertEquals(true, managedObj.getFieldBooleanList().get(0));
        assertEquals(false, managedObj.getFieldBooleanList().get(1));
        assertEquals(2, managedObj.getFieldBinaryList().size());
        assertArrayEquals(new byte[] {2}, managedObj.getFieldBinaryList().get(0));
        assertArrayEquals(new byte[] {3}, managedObj.getFieldBinaryList().get(1));
        assertEquals(2, managedObj.getFieldLongList().size());
        assertEquals((Long) 2L, managedObj.getFieldLongList().get(0));
        assertEquals((Long) 3L, managedObj.getFieldLongList().get(1));
        assertEquals(2, managedObj.getFieldIntegerList().size());
        assertEquals((Integer) 2, managedObj.getFieldIntegerList().get(0));
        assertEquals((Integer) 3, managedObj.getFieldIntegerList().get(1));
        assertEquals(2, managedObj.getFieldShortList().size());
        assertEquals((Short) (short) 2, managedObj.getFieldShortList().get(0));
        assertEquals((Short) (short) 3, managedObj.getFieldShortList().get(1));
        assertEquals(2, managedObj.getFieldByteList().size());
        assertEquals((Byte) (byte) 2, managedObj.getFieldByteList().get(0));
        assertEquals((Byte) (byte) 3, managedObj.getFieldByteList().get(1));
        assertEquals(2, managedObj.getFieldDoubleList().size());
        assertEquals((Double) 2D, managedObj.getFieldDoubleList().get(0));
        assertEquals((Double) 3D, managedObj.getFieldDoubleList().get(1));
        assertEquals(2, managedObj.getFieldFloatList().size());
        assertEquals((Float) 2F, managedObj.getFieldFloatList().get(0));
        assertEquals((Float) 3F, managedObj.getFieldFloatList().get(1));
        assertEquals(2, managedObj.getFieldDateList().size());
        assertEquals(new Date(2L), managedObj.getFieldDateList().get(0));
        assertEquals(new Date(3L), managedObj.getFieldDateList().get(1));

        // Makes sure that excess object by default value is not created.
        assertEquals(2, realm.where(RandomPrimaryKey.class).count());
    }

    @Test
    public void copyFromRealm_defaultValuesAreIgnored() {
        final DefaultValueOfField managedObj;
        realm.beginTransaction();
        {
            final DefaultValueOfField obj = new DefaultValueOfField();
            obj.setFieldIgnored(DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE + ".modified");
            obj.setFieldString(DefaultValueOfField.FIELD_STRING_DEFAULT_VALUE + ".modified");
            obj.setFieldRandomString("non-random");
            obj.setFieldShort((short) (DefaultValueOfField.FIELD_SHORT_DEFAULT_VALUE + 1));
            obj.setFieldInt(DefaultValueOfField.FIELD_INT_DEFAULT_VALUE + 1);
            obj.setFieldLongPrimaryKey(DefaultValueOfField.FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE + 1);
            obj.setFieldLong(DefaultValueOfField.FIELD_LONG_DEFAULT_VALUE + 1);
            obj.setFieldByte((byte) (DefaultValueOfField.FIELD_BYTE_DEFAULT_VALUE + 1));
            obj.setFieldFloat(DefaultValueOfField.FIELD_FLOAT_DEFAULT_VALUE + 1);
            obj.setFieldDouble(DefaultValueOfField.FIELD_DOUBLE_DEFAULT_VALUE + 1);
            obj.setFieldBoolean(!DefaultValueOfField.FIELD_BOOLEAN_DEFAULT_VALUE);
            obj.setFieldDate(new Date(DefaultValueOfField.FIELD_DATE_DEFAULT_VALUE.getTime() + 1));
            obj.setFieldBinary(new byte[] {(byte) (DefaultValueOfField.FIELD_BINARY_DEFAULT_VALUE[0] - 1)});

            final RandomPrimaryKey fieldObjectValue = new RandomPrimaryKey();
            fieldObjectValue.setFieldInt(RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 1);
            obj.setFieldObject(fieldObjectValue);

            final RealmList<RandomPrimaryKey> list = new RealmList<RandomPrimaryKey>();
            final RandomPrimaryKey listItem = new RandomPrimaryKey();
            listItem.setFieldInt(RandomPrimaryKey.FIELD_INT_DEFAULT_VALUE + 2);
            list.add(listItem);
            obj.setFieldList(list);

            obj.setFieldStringList(new RealmList<>("2", "3"));
            obj.setFieldBooleanList(new RealmList<>(true, false));
            obj.setFieldBinaryList(new RealmList<>(new byte[] {2}, new byte[] {3}));
            obj.setFieldLongList(new RealmList<>(2L, 3L));
            obj.setFieldIntegerList(new RealmList<>(2, 3));
            obj.setFieldShortList(new RealmList<>((short) 2, (short) 3));
            obj.setFieldByteList(new RealmList<>((byte) 2, (byte) 3));
            obj.setFieldDoubleList(new RealmList<>(2D, 3D));
            obj.setFieldFloatList(new RealmList<>(2F, 3F));
            obj.setFieldDateList(new RealmList<>(new Date(2L), new Date(3L)));

            managedObj = realm.copyToRealm(obj);
        }
        realm.commitTransaction();

        final DefaultValueOfField copy = realm.copyFromRealm(managedObj);

        assertEquals(DefaultValueOfField.FIELD_IGNORED_DEFAULT_VALUE, copy.getFieldIgnored());
        assertEquals(managedObj.getFieldString(), copy.getFieldString());
        assertEquals(managedObj.getFieldRandomString(), copy.getFieldRandomString());
        assertEquals(managedObj.getFieldShort(), copy.getFieldShort());
        assertEquals(managedObj.getFieldInt(), copy.getFieldInt());
        assertEquals(managedObj.getFieldLongPrimaryKey(), copy.getFieldLongPrimaryKey());
        assertEquals(managedObj.getFieldLong(), copy.getFieldLong());
        assertEquals(managedObj.getFieldByte(), copy.getFieldByte());
        assertEquals(managedObj.getFieldFloat(), copy.getFieldFloat(), 0F);
        assertEquals(managedObj.getFieldDouble(), copy.getFieldDouble(), 0D);
        assertEquals(managedObj.isFieldBoolean(), copy.isFieldBoolean());
        assertEquals(managedObj.getFieldDate(), copy.getFieldDate());
        assertArrayEquals(managedObj.getFieldBinary(), copy.getFieldBinary());
        assertEquals(managedObj.getFieldObject().getFieldInt(), copy.getFieldObject().getFieldInt());
        assertEquals(1, copy.getFieldList().size());
        //noinspection ConstantConditions
        assertEquals(managedObj.getFieldList().first().getFieldInt(), copy.getFieldList().first().getFieldInt());

        assertEquals(2, managedObj.getFieldStringList().size());
        assertEquals("2", managedObj.getFieldStringList().get(0));
        assertEquals("3", managedObj.getFieldStringList().get(1));
        assertEquals(2, managedObj.getFieldBooleanList().size());
        assertEquals(true, managedObj.getFieldBooleanList().get(0));
        assertEquals(false, managedObj.getFieldBooleanList().get(1));
        assertEquals(2, managedObj.getFieldBinaryList().size());
        assertArrayEquals(new byte[] {2}, managedObj.getFieldBinaryList().get(0));
        assertArrayEquals(new byte[] {3}, managedObj.getFieldBinaryList().get(1));
        assertEquals(2, managedObj.getFieldLongList().size());
        assertEquals((Long) 2L, managedObj.getFieldLongList().get(0));
        assertEquals((Long) 3L, managedObj.getFieldLongList().get(1));
        assertEquals(2, managedObj.getFieldIntegerList().size());
        assertEquals((Integer) 2, managedObj.getFieldIntegerList().get(0));
        assertEquals((Integer) 3, managedObj.getFieldIntegerList().get(1));
        assertEquals(2, managedObj.getFieldShortList().size());
        assertEquals((Short) (short) 2, managedObj.getFieldShortList().get(0));
        assertEquals((Short) (short) 3, managedObj.getFieldShortList().get(1));
        assertEquals(2, managedObj.getFieldByteList().size());
        assertEquals((Byte) (byte) 2, managedObj.getFieldByteList().get(0));
        assertEquals((Byte) (byte) 3, managedObj.getFieldByteList().get(1));
        assertEquals(2, managedObj.getFieldDoubleList().size());
        assertEquals((Double) 2D, managedObj.getFieldDoubleList().get(0));
        assertEquals((Double) 3D, managedObj.getFieldDoubleList().get(1));
        assertEquals(2, managedObj.getFieldFloatList().size());
        assertEquals((Float) 2F, managedObj.getFieldFloatList().get(0));
        assertEquals((Float) 3F, managedObj.getFieldFloatList().get(1));
        assertEquals(2, managedObj.getFieldDateList().size());
        assertEquals(new Date(2L), managedObj.getFieldDateList().get(0));
        assertEquals(new Date(3L), managedObj.getFieldDateList().get(1));
    }

    // Tests close Realm in another thread different from where it is created.
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

        // Timeout should never happen.
        TestHelper.awaitOrFail(latch);
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

    // Tests Realm#isClosed() in another thread different from where it is created.
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

        // Timeout should never happen.
        TestHelper.awaitOrFail(latch);
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
    // even if we skip initialization & validation.
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
                Realm realm = Realm.getInstance(realmConfiguration); // This will populate columnIndices.
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
        realm.where(AllTypes.class).equalTo("columnString", "Foo").findAll(); // This would crash if columnIndices == null.
        realm.close();
        mainThreadRealmDone.countDown();
        TestHelper.awaitOrFail(bgRealmClosed);
        if (threadError[0] != null) {
            throw threadError[0];
        }
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

    // Test for https://github.com/realm/realm-java/issues/1646
    @Test
    public void closingRealmWhileOtherThreadIsOpeningRealm() throws Exception {
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(1);

        final List<Exception> exception = new ArrayList<Exception>();

        new Thread() {
            @Override
            public void run() {
                try {
                    startLatch.await(TestHelper.STANDARD_WAIT_SECS, TimeUnit.SECONDS);
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

        // Prevents for another thread to enter Realm.createAndValidate().
        synchronized (BaseRealm.class) {
            startLatch.countDown();

            // Waits for another thread's entering Realm.createAndValidate().
            SystemClock.sleep(100L);

            realm.close();
            realm = null;
        }

        TestHelper.awaitOrFail(endLatch);

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
                // Step 2: Opens realm in background thread.
                Realm realm = Realm.getInstance(realmConfig);
                realmOpenedInBgLatch.countDown();
                try {
                    realmClosedInFgLatch.await(TestHelper.STANDARD_WAIT_SECS, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    exception.add(e);
                    realm.close();
                    return;
                }

                // Step 4: Starts transaction in background.
                realm.beginTransaction();
                transBeganInBgLatch.countDown();
                try {
                    fgFinishedLatch.await(TestHelper.STANDARD_WAIT_SECS, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    exception.add(e);
                }
                // Step 6: Cancels Transaction and closes realm in background.
                realm.cancelTransaction();
                realm.close();
                bgFinishedLatch.countDown();
            }
        });
        thread.start();

        TestHelper.awaitOrFail(realmOpenedInBgLatch);
        // Step 3: Closes all realm instances in foreground thread.
        realm.close();
        realmClosedInFgLatch.countDown();
        TestHelper.awaitOrFail(transBeganInBgLatch);

        // Step 5: Gets a new Realm instance in foreground.
        realm = Realm.getInstance(realmConfig);
        fgFinishedLatch.countDown();
        TestHelper.awaitOrFail(bgFinishedLatch);

        if (!exception.isEmpty()) {
            throw exception.get(0);
        }
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
        AllTypes realmObject = realm.where(AllTypes.class).sort("columnLong").findAll().first();
        AllTypes unmanagedObject = realm.copyFromRealm(realmObject);
        assertArrayEquals(realmObject.getColumnBinary(), unmanagedObject.getColumnBinary());
        assertEquals(realmObject.getColumnString(), unmanagedObject.getColumnString());
        assertEquals(realmObject.getColumnLong(), unmanagedObject.getColumnLong());
        assertEquals(realmObject.getColumnFloat(), unmanagedObject.getColumnFloat(), 0.00000000001);
        assertEquals(realmObject.getColumnDouble(), unmanagedObject.getColumnDouble(), 0.00000000001);
        assertEquals(realmObject.isColumnBoolean(), unmanagedObject.isColumnBoolean());
        assertEquals(realmObject.getColumnDate(), unmanagedObject.getColumnDate());
    }

    @Test
    public void copyFromRealm_newCopyEachTime() {
        populateTestRealm();
        AllTypes realmObject = realm.where(AllTypes.class).sort("columnLong").findAll().first();
        AllTypes unmanagedObject1 = realm.copyFromRealm(realmObject);
        AllTypes unmanagedObject2 = realm.copyFromRealm(realmObject);
        assertFalse(unmanagedObject1 == unmanagedObject2);
        assertNotSame(unmanagedObject1, unmanagedObject2);
    }

    // Tests that the object graph is copied as it is and no extra copies are made.
    // 1) (A -> B/[B,C])
    // 2) (C -> B/[B,A])
    // A copy should result in only 3 distinct objects.
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

        // Asserts object equality on the object graph.
        assertTrue(copyA.getObject() == copyC.getObject());
        assertTrue(copyA.getObjects().get(0) == copyC.getObjects().get(0));
        assertTrue(copyA == copyC.getObjects().get(1));
        assertTrue(copyC == copyA.getObjects().get(1));
    }

    // Tests that for (A -> B -> C) for maxDepth = 1, result is (A -> B -> null).
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

    // Tests that depth restriction is calculated from the top-most encountered object, i.e. it is possible for some
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

        // Object is filled before otherObject. (because of field order - WARNING: Not guaranteed)
        // This means that the object will be encountered first time at max depth, so E will not be copied.
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

    // Tests that the same Realm objects in a list result in the same Java in-memory copy.
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

    // Tests if close can be called from Realm change listener when there is no other listeners.
    @Test
    @RunTestInLooperThread
    public void closeRealmInChangeListener() {
        final Realm realm = looperThread.getRealm();
        final RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                if (realm.where(AllTypes.class).count() == 1) {
                    realm.removeChangeListener(this);
                    realm.close();
                    looperThread.testComplete();
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

    // Tests if close can be called from Realm change listener when there is a listener on empty Realm Object.
    @Test
    @RunTestInLooperThread
    public void closeRealmInChangeListenerWhenThereIsListenerOnEmptyObject() {
        final Realm realm = looperThread.getRealm();
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

    // Tests if close can be called from Realm change listener when there is an listener on non-empty Realm Object.
    @Test
    @RunTestInLooperThread
    public void closeRealmInChangeListenerWhenThereIsListenerOnObject() {
        final Realm realm = looperThread.getRealm();
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

                    // Ends test after next looper event to ensure that all listeners were called.
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

        // Change listener on Realm Object.
        final AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        allTypes.addChangeListener(dummyListener);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(AllTypes.class);
            }
        });
    }

    // Tests if close can be called from Realm change listener when there is an listener on RealmResults.
    @Test
    @RunTestInLooperThread
    public void closeRealmInChangeListenerWhenThereIsListenerOnResults() {
        final Realm realm = looperThread.getRealm();
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

        // Change listener on Realm results.
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
    @RunTestInLooperThread
    public void addChangeListener_throwOnAddingNullListenerFromLooperThread() {
        final Realm realm = looperThread.getRealm();

        try {
            realm.addChangeListener(null);
            fail("adding null change listener must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        } finally {
            looperThread.testComplete();
        }
    }

    @Test
    public void addChangeListener_throwOnAddingNullListenerFromNonLooperThread() throws Throwable {
        TestHelper.executeOnNonLooperThread(new TestHelper.Task() {
            @Override
            public void run() throws Exception {
                final Realm realm = Realm.getInstance(realmConfig);

                //noinspection TryFinallyCanBeTryWithResources
                try {
                    realm.addChangeListener(null);
                    fail("adding null change listener must throw an exception.");
                } catch (IllegalArgumentException ignore) {
                } finally {
                    realm.close();
                }
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void removeChangeListener_throwOnRemovingNullListenerFromLooperThread() {
        final Realm realm = looperThread.getRealm();

        try {
            realm.removeChangeListener(null);
            fail("removing null change listener must throw an exception.");
        } catch (IllegalArgumentException ignore) {
        } finally {
            looperThread.testComplete();
        }
    }

    @Test
    public void removeChangeListener_throwOnRemovingNullListenerFromNonLooperThread() throws Throwable {
        TestHelper.executeOnNonLooperThread(new TestHelper.Task() {
            @Override
            public void run() throws Exception {
                final Realm realm = Realm.getInstance(realmConfig);

                //noinspection TryFinallyCanBeTryWithResources
                try {
                    realm.removeChangeListener(null);
                    fail("removing null change listener must throw an exception.");
                } catch (IllegalArgumentException ignore) {
                } finally {
                    realm.close();
                }
            }
        });
    }

    @Test
    public void removeChangeListenerThrowExceptionOnWrongThread() {
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        Realm realm = Realm.getInstance(realmConfig);
        Thread thread = new Thread(() -> {
            try {
                realm.removeChangeListener(object -> {});
                fail("Should not be able to invoke removeChangeListener");
            } catch (IllegalStateException ignored) {
            } finally {
                signalTestFinished.countDown();
            }
        });
        thread.start();
        try {
            TestHelper.awaitOrFail(signalTestFinished);
        } finally {
            thread.interrupt();
            realm.close();
        }
    }

    @Test
    public void removeAllChangeListenersThrowExceptionOnWrongThreadThread() {
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        Realm realm = Realm.getInstance(realmConfig);
        Thread thread = new Thread(() -> {
            try {
                realm.removeAllChangeListeners();
                fail("Should not be able to invoke removeChangeListener");
            } catch (IllegalStateException ignored) {
            } finally {
                signalTestFinished.countDown();
            }
        });
        thread.start();

        try {
            TestHelper.awaitOrFail(signalTestFinished);
        } finally {
            thread.interrupt();
            realm.close();
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

    // Test for https://github.com/realm/realm-java/issues/5745
    @Test
    public void deleteAll_realmWithMoreTables() {
        realm.close();
        RealmConfiguration config1 = configFactory.createConfigurationBuilder()
                .name("deleteAllTest.realm")
                .schema(StringOnly.class, StringAndInt.class)
                .build();
        realm = Realm.getInstance(config1);
        realm.executeTransaction(r -> {
            r.createObject(StringOnly.class);
            r.createObject(StringAndInt.class);
        });
        realm.close();

        RealmConfiguration config2 = configFactory.createConfigurationBuilder()
                .name("deleteAllTest.realm")
                .schema(StringOnly.class)
                .build();

        realm = Realm.getInstance(config2);
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        assertTrue(realm.isEmpty());
        realm.close();

        // deleteAll() will only delete tables part of the schema, so reopening with the old
        // should reveal the old data
        realm = Realm.getInstance(config1);
        assertFalse(realm.isEmpty());
        assertEquals(1, realm.where(StringAndInt.class).count());
    }


    @Test
    public void waitForChange_emptyDataChange() throws InterruptedException {
        final CountDownLatch bgRealmOpened = new CountDownLatch(1);
        final CountDownLatch bgRealmClosed = new CountDownLatch(1);
        final AtomicBoolean bgRealmChangeResult = new AtomicBoolean(false);
        final AtomicLong bgRealmWaitForChangeResult = new AtomicLong(0);

        // Waits in background.
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);
                bgRealmOpened.countDown();
                bgRealmChangeResult.set(realm.waitForChange());
                bgRealmWaitForChangeResult.set(realm.where(AllTypes.class).count());
                realm.close();
                bgRealmClosed.countDown();
            }
        });
        thread.start();

        TestHelper.awaitOrFail(bgRealmOpened);
        realm.beginTransaction();
        realm.commitTransaction();
        TestHelper.awaitOrFail(bgRealmClosed);
        assertTrue(bgRealmChangeResult.get());
        assertEquals(0, bgRealmWaitForChangeResult.get());
    }

    @Test
    public void waitForChange_withDataChange() throws InterruptedException {
        final CountDownLatch bgRealmOpened = new CountDownLatch(1);
        final CountDownLatch bgRealmClosed = new CountDownLatch(1);
        final AtomicBoolean bgRealmChangeResult = new AtomicBoolean(false);
        final AtomicLong bgRealmWaitForChangeResult = new AtomicLong(0);

        // Waits in background.
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);
                bgRealmOpened.countDown();
                bgRealmChangeResult.set(realm.waitForChange());
                bgRealmWaitForChangeResult.set(realm.where(AllTypes.class).count());
                realm.close();
                bgRealmClosed.countDown();
            }
        });
        thread.start();

        TestHelper.awaitOrFail(bgRealmOpened);
        populateTestRealm();
        TestHelper.awaitOrFail(bgRealmClosed);
        assertTrue(bgRealmChangeResult.get());
        assertEquals(TEST_DATA_SIZE, bgRealmWaitForChangeResult.get());
    }

    @Test
    public void waitForChange_syncBackgroundRealmResults() throws InterruptedException {
        final CountDownLatch bgRealmOpened = new CountDownLatch(1);
        final CountDownLatch bgRealmClosed = new CountDownLatch(1);
        final AtomicBoolean bgRealmChangeResult = new AtomicBoolean(false);
        final AtomicLong bgRealmResultSize = new AtomicLong(0);

        // Wait in background
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);
                RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
                // First makes sure the results is empty.
                bgRealmResultSize.set(results.size());
                bgRealmOpened.countDown();
                bgRealmChangeResult.set(realm.waitForChange());
                bgRealmResultSize.set(results.size());
                realm.close();
                bgRealmClosed.countDown();
            }
        });
        thread.start();

        TestHelper.awaitOrFail(bgRealmOpened);
        // Background result should be empty.
        assertEquals(0, bgRealmResultSize.get());
        populateTestRealm();
        TestHelper.awaitOrFail(bgRealmClosed);
        assertTrue(bgRealmChangeResult.get());
        // Once RealmResults are synchronized after waitForChange, the result size should be what we expect.
        assertEquals(TEST_DATA_SIZE, bgRealmResultSize.get());
    }

    @Test
    public void stopWaitForChange() throws InterruptedException {
        final CountDownLatch bgRealmOpened = new CountDownLatch(1);
        final CountDownLatch bgRealmClosed = new CountDownLatch(1);
        final AtomicBoolean bgRealmChangeResult = new AtomicBoolean(true);
        final AtomicReference<Realm> bgRealm = new AtomicReference<Realm>();

        // Waits in background.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);
                bgRealm.set(realm);
                bgRealmOpened.countDown();
                bgRealmChangeResult.set(realm.waitForChange());
                realm.close();
                bgRealmClosed.countDown();
            }
        }).start();

        TestHelper.awaitOrFail(bgRealmOpened);
        Thread.sleep(200);
        bgRealm.get().stopWaitForChange();
        TestHelper.awaitOrFail(bgRealmClosed);
        assertFalse(bgRealmChangeResult.get());
    }

    // Tests if waitForChange doesn't blocks once stopWaitForChange has been called before.
    @Test
    public void waitForChange_stopWaitForChangeDisablesWaiting() throws InterruptedException {
        final CountDownLatch bgRealmOpened = new CountDownLatch(1);
        final CountDownLatch bgRealmStopped = new CountDownLatch(1);
        final CountDownLatch bgRealmClosed = new CountDownLatch(1);
        final AtomicBoolean bgRealmFirstWaitResult = new AtomicBoolean(true);
        final AtomicBoolean bgRealmSecondWaitResult = new AtomicBoolean(false);
        final AtomicReference<Realm> bgRealm = new AtomicReference<Realm>();

        // Waits in background.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);
                bgRealm.set(realm);
                bgRealmOpened.countDown();
                bgRealmFirstWaitResult.set(realm.waitForChange());
                bgRealmStopped.countDown();
                bgRealmSecondWaitResult.set(realm.waitForChange());
                realm.close();
                bgRealmClosed.countDown();
            }
        }).start();

        TestHelper.awaitOrFail(bgRealmOpened);
        bgRealm.get().stopWaitForChange();
        TestHelper.awaitOrFail(bgRealmStopped);
        assertFalse(bgRealmFirstWaitResult.get());
        TestHelper.awaitOrFail(bgRealmClosed);
        assertFalse(bgRealmSecondWaitResult.get());
    }

    // Tests if waitForChange still blocks if stopWaitForChange has been called for a realm in a different thread.
    @Test
    public void waitForChange_blockSpecificThreadOnly() throws InterruptedException {
        final CountDownLatch bgRealmsOpened = new CountDownLatch(2);
        final CountDownLatch bgRealmsClosed = new CountDownLatch(2);
        final AtomicBoolean bgRealmFirstWaitResult = new AtomicBoolean(true);
        final AtomicBoolean bgRealmSecondWaitResult = new AtomicBoolean(false);
        final AtomicLong bgRealmWaitForChangeResult = new AtomicLong(0);
        final AtomicReference<Realm> bgRealm = new AtomicReference<Realm>();

        // Waits in background.
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);
                bgRealm.set(realm);
                bgRealmsOpened.countDown();
                bgRealmFirstWaitResult.set(realm.waitForChange());
                realm.close();
                bgRealmsClosed.countDown();
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);
                bgRealmsOpened.countDown();
                bgRealmSecondWaitResult.set(realm.waitForChange());
                bgRealmWaitForChangeResult.set(realm.where(AllTypes.class).count());
                realm.close();
                bgRealmsClosed.countDown();
            }
        });
        thread1.start();
        thread2.start();

        TestHelper.awaitOrFail(bgRealmsOpened);
        bgRealm.get().stopWaitForChange();
        // Waits for Thread 2 to wait.
        Thread.sleep(500);
        populateTestRealm();
        TestHelper.awaitOrFail(bgRealmsClosed);
        assertFalse(bgRealmFirstWaitResult.get());
        assertTrue(bgRealmSecondWaitResult.get());
        assertEquals(TEST_DATA_SIZE, bgRealmWaitForChangeResult.get());
    }

    // Checks if waitForChange() does not respond to Thread.interrupt().
    @Test
    public void waitForChange_interruptingThread() throws InterruptedException {
        final CountDownLatch bgRealmOpened = new CountDownLatch(1);
        final CountDownLatch bgRealmClosed = new CountDownLatch(1);
        final AtomicReference<Boolean> bgRealmWaitResult = new AtomicReference<Boolean>();
        final AtomicReference<Realm> bgRealm = new AtomicReference<Realm>();

        // Waits in background.
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);
                bgRealm.set(realm);
                bgRealmOpened.countDown();
                bgRealmWaitResult.set(realm.waitForChange());
                realm.close();
                bgRealmClosed.countDown();
            }
        });
        thread.start();

        TestHelper.awaitOrFail(bgRealmOpened);
        // Makes sure background thread goes to wait.
        Thread.sleep(500);
        // Interrupting a thread should neither cause any side effect nor terminate the Background Realm from waiting.
        thread.interrupt();
        assertTrue(thread.isInterrupted());
        assertEquals(null, bgRealmWaitResult.get());

        // Now we'll stop realm from waiting.
        bgRealm.get().stopWaitForChange();
        TestHelper.awaitOrFail(bgRealmClosed);
        assertFalse(bgRealmWaitResult.get());
    }

    @Test
    public void waitForChange_onLooperThread() throws Throwable {
        final CountDownLatch bgRealmClosed = new CountDownLatch(1);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Realm realm = Realm.getInstance(realmConfig);
                try {
                    realm.waitForChange();
                    fail();
                } catch (IllegalStateException ignored) {
                } finally {
                    realm.close();
                    bgRealmClosed.countDown();
                }
            }
        });
        thread.start();

        TestHelper.awaitOrFail(bgRealmClosed);
    }

    // Cannot wait inside of a transaction.
    @Test(expected = IllegalStateException.class)
    public void waitForChange_illegalWaitInsideTransaction() {
        realm.beginTransaction();
        realm.waitForChange();
    }

    @Test
    public void waitForChange_stopWaitingOnClosedRealmThrows() throws InterruptedException {
        final CountDownLatch bgRealmClosed = new CountDownLatch(1);
        final AtomicReference<Realm> bgRealm = new AtomicReference<Realm>();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(realmConfig);
                bgRealm.set(realm);
                realm.close();
                bgRealmClosed.countDown();
            }
        });
        thread.start();

        TestHelper.awaitOrFail(bgRealmClosed);
        try {
            bgRealm.get().stopWaitForChange();
            fail("Cannot stop a closed Realm from waiting");
        } catch (IllegalStateException expected) {
        }
    }

    // waitForChange & stopWaitForChange within a simple Thread wrapper.
    @Test
    public void waitForChange_runWithRealmThread() throws InterruptedException {
        final CountDownLatch bgRealmStarted = new CountDownLatch(1);
        final CountDownLatch bgRealmFished = new CountDownLatch(1);
        final AtomicBoolean bgRealmChangeResult = new AtomicBoolean(false);
        final AtomicLong bgRealmResultSize = new AtomicLong(0);

        RealmThread thread = new RealmThread(realmConfig, new RealmThread.RealmRunnable() {
            @Override
            public void run(Realm realm) {
                bgRealmStarted.countDown();
                bgRealmChangeResult.set(realm.waitForChange());
                bgRealmResultSize.set(realm.where(AllTypes.class).count());
                realm.close();
                bgRealmFished.countDown();
            }
        });
        thread.start();

        TestHelper.awaitOrFail(bgRealmStarted);
        populateTestRealm();
        TestHelper.awaitOrFail(bgRealmFished);
        assertTrue(bgRealmChangeResult.get());
        assertEquals(TEST_DATA_SIZE, bgRealmResultSize.get());
    }

    @Test
    public void waitForChange_endRealmThread() throws InterruptedException {
        final CountDownLatch bgRealmStarted = new CountDownLatch(1);
        final CountDownLatch bgRealmFished = new CountDownLatch(1);
        final AtomicBoolean bgRealmChangeResult = new AtomicBoolean(true);

        RealmThread thread = new RealmThread(realmConfig, new RealmThread.RealmRunnable() {
            @Override
            public void run(Realm realm) {
                bgRealmStarted.countDown();
                bgRealmChangeResult.set(realm.waitForChange());
                realm.close();
                bgRealmFished.countDown();
            }
        });
        thread.start();

        TestHelper.awaitOrFail(bgRealmStarted);
        thread.end();
        TestHelper.awaitOrFail(bgRealmFished);
        assertFalse(bgRealmChangeResult.get());
    }

    // Check if the column indices cache is refreshed if the index of a defined column is changed by another Realm
    // instance.
    @Test
    public void nonAdditiveSchemaChangesWhenTypedRealmExists() throws InterruptedException {
        final String TEST_CHARS = "TEST_CHARS";
        final RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                .schema(StringOnly.class)
                .name("schemaChangeTest")
                .build();
        Realm realm = Realm.getInstance(realmConfig);
        io_realm_entities_StringOnlyRealmProxy.StringOnlyColumnInfo columnInfo
                = (io_realm_entities_StringOnlyRealmProxy.StringOnlyColumnInfo) realm.getSchema().getColumnInfo(StringOnly.class);
        assertEquals(0, columnInfo.charsIndex);

        realm.beginTransaction();
        StringOnly stringOnly = realm.createObject(StringOnly.class);
        stringOnly.setChars(TEST_CHARS);
        realm.commitTransaction();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Here we try to change the column index of FIELD_CHARS from 0 to 1.
                DynamicRealm realm = DynamicRealm.getInstance(realmConfig);
                realm.beginTransaction();
                RealmObjectSchema stringOnlySchema = realm.getSchema().get(StringOnly.CLASS_NAME);
                assertEquals(0, stringOnlySchema.getColumnIndex(StringOnly.FIELD_CHARS));
                Table table = stringOnlySchema.getTable();
                // Please notice that we cannot do it by removing/adding a column since it is not allowed by Object
                // Store. Do it by using the internal API insertColumn.
                table.insertColumn(0, RealmFieldType.INTEGER, "NewColumn");
                assertEquals(1, stringOnlySchema.getColumnIndex(StringOnly.FIELD_CHARS));
                realm.commitTransaction();
                realm.close();
            }
        });
        thread.start();
        thread.join();
        realm.refresh();

        // The columnInfo object never changes, only the indexes it references will.
        assertSame(columnInfo, realm.getSchema().getColumnInfo(StringOnly.class));
        assertEquals(TEST_CHARS, stringOnly.getChars());
        assertEquals(1, columnInfo.charsIndex);
        realm.close();
    }

    @Test
    public void getGlobalInstanceCount() {
        final CountDownLatch bgDone = new CountDownLatch(1);

        final RealmConfiguration config = configFactory.createConfiguration("globalCountTest");
        assertEquals(0, Realm.getGlobalInstanceCount(config));

        // Opens thread local Realm.
        Realm realm = Realm.getInstance(config);
        assertEquals(1, Realm.getGlobalInstanceCount(config));

        // Opens thread local DynamicRealm.
        DynamicRealm dynRealm = DynamicRealm.getInstance(config);
        assertEquals(2, Realm.getGlobalInstanceCount(config));

        // Opens Realm in another thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(config);
                assertEquals(3, Realm.getGlobalInstanceCount(config));
                realm.close();
                assertEquals(2, Realm.getGlobalInstanceCount(config));
                bgDone.countDown();
            }
        }).start();

        TestHelper.awaitOrFail(bgDone);
        dynRealm.close();
        assertEquals(1, Realm.getGlobalInstanceCount(config));
        realm.close();
        assertEquals(0, Realm.getGlobalInstanceCount(config));
    }

    @Test
    public void getLocalInstanceCount() {
        final RealmConfiguration config = configFactory.createConfiguration("localInstanceCount");
        assertEquals(0, Realm.getLocalInstanceCount(config));

        // Opens thread local Realm.
        Realm realm = Realm.getInstance(config);
        assertEquals(1, Realm.getLocalInstanceCount(config));

        // Opens thread local DynamicRealm.
        DynamicRealm dynRealm = DynamicRealm.getInstance(config);
        assertEquals(2, Realm.getLocalInstanceCount(config));

        dynRealm.close();
        assertEquals(1, Realm.getLocalInstanceCount(config));
        realm.close();
        assertEquals(0, Realm.getLocalInstanceCount(config));
    }

    @Test
    public void namedPipeDirForExternalStorage() {

        // Test for https://github.com/realm/realm-java/issues/3140
        realm.close();
        realm = null;

        final File namedPipeDir = OsSharedRealm.getTemporaryDirectory();
        assertTrue(namedPipeDir.isDirectory());
        TestHelper.deleteRecursively(namedPipeDir);
        //noinspection ResultOfMethodCallIgnored
        namedPipeDir.mkdirs();

        final File externalFilesDir = context.getExternalFilesDir(null);
        final RealmConfiguration config = configFactory.createConfigurationBuilder()
                .directory(externalFilesDir)
                .name("external.realm")
                .build();
        Realm.deleteRealm(config);

        // Test if it works when the namedPipeDir is empty.
        Realm realmOnExternalStorage = Realm.getInstance(config);
        realmOnExternalStorage.close();

        assertTrue(namedPipeDir.isDirectory());

        Assume.assumeTrue("SELinux is not enforced on this device.", TestHelper.isSelinuxEnforcing());

        // Only checks the fifo file created by call, since all Realm instances share the same fifo created by
        // external_commit_helper which might not be created in the newly created dir if there are Realm instances
        // are not deleted when TestHelper.deleteRecursively(namedPipeDir) called.
        File[] files = namedPipeDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("realm_.*cv");
            }
        });
        assertEquals(2, files.length);

        // Tests if it works when the namedPipeDir and the named pipe files already exist.
        realmOnExternalStorage = Realm.getInstance(config);
        realmOnExternalStorage.close();
    }

    @Test(expected = IllegalStateException.class)
    public void getInstanceAsync_nonLooperThreadShouldThrow() {
        Realm.getInstanceAsync(realmConfig, new Realm.Callback() {
            @Override
            public void onSuccess(Realm realm) {
                fail();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void getInstanceAsync_nullConfigShouldThrow() {
        thrown.expect(IllegalArgumentException.class);
        Realm.getInstanceAsync(null, new Realm.Callback() {
            @Override
            public void onSuccess(Realm realm) {
                fail();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void getInstanceAsync_nullCallbackShouldThrow() {
        thrown.expect(IllegalArgumentException.class);
        Realm.getInstanceAsync(realmConfig, null);
    }

    // Verify that the logic for waiting for the users file dir to be come available isn't totally broken
    // This is pretty hard to test, so forced to break encapsulation in this case.
    @Test
    public void init_waitForFilesDir() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        java.lang.reflect.Method m = Realm.class.getDeclaredMethod("checkFilesDirAvailable", Context.class);
        m.setAccessible(true);

        // A) Check it fails if getFilesDir is never created
        Context mockContext = mock(Context.class);
        when(mockContext.getFilesDir()).thenReturn(null);

        try {
            m.invoke(null, mockContext);
            fail();
        } catch (InvocationTargetException e) {
            assertEquals(IllegalStateException.class, e.getCause().getClass());
        }

        // B) Check we return if the filesDir becomes available after a while
        mockContext = mock(Context.class);
        when(mockContext.getFilesDir()).then(new Answer<File>() {
            int calls = 0;
            File userFolder = tmpFolder.newFolder();

            @Override
            public File answer(InvocationOnMock invocationOnMock) throws Throwable {
                calls++;
                return (calls > 5) ? userFolder : null; // Start returning the correct folder after 5 attempts
            }
        });

        assertNull(m.invoke(null, mockContext));
    }

    @Test
    @RunTestInLooperThread
    public void refresh_triggerNotifications() {
        final CountDownLatch bgThreadDone = new CountDownLatch(1);
        final AtomicBoolean listenerCalled = new AtomicBoolean(false);
        Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        assertEquals(0, results.size());
        results.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> results) {
                assertEquals(1, results.size());
                listenerCalled.set(true);
            }
        });

        // Advance the Realm on a background while blocking this thread. When we refresh, it should trigger
        // the listener.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(looperThread.getConfiguration());
                realm.beginTransaction();
                realm.createObject(AllTypes.class);
                realm.commitTransaction();
                realm.close();
                bgThreadDone.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(bgThreadDone);

        realm.refresh();
        assertTrue(listenerCalled.get());
        looperThread.testComplete();
    }

    @Test
    public void refresh_nonLooperThreadAdvances() {
        final CountDownLatch bgThreadDone = new CountDownLatch(1);
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        assertEquals(0, results.size());

        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(RealmTests.this.realm.getConfiguration());
                realm.beginTransaction();
                realm.createObject(AllTypes.class);
                realm.commitTransaction();
                realm.close();
                bgThreadDone.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(bgThreadDone);

        realm.refresh();
        assertEquals(1, results.size());
    }

    @Test
    @RunTestInLooperThread
    public void refresh_forceSynchronousNotifications() {
        final CountDownLatch bgThreadDone = new CountDownLatch(1);
        final AtomicBoolean listenerCalled = new AtomicBoolean(false);
        Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
        results.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> results) {
                // Will be forced synchronous
                assertEquals(1, results.size());
                listenerCalled.set(true);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(looperThread.getConfiguration());
                realm.beginTransaction();
                realm.createObject(AllTypes.class);
                realm.commitTransaction();
                realm.close();
                bgThreadDone.countDown();
            }
        }).start();
        TestHelper.awaitOrFail(bgThreadDone);

        realm.refresh();
        assertTrue(listenerCalled.get());
        looperThread.testComplete();
    }

    @Test
    public void refresh_insideTransactionThrows() {
        realm.beginTransaction();
        try {
            realm.refresh();
            fail();
        } catch (IllegalStateException ignored) {
        }
        realm.cancelTransaction();
    }

    @Test
    public void beginTransaction_readOnlyThrows() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .name("readonly.realm")
                .schema(StringOnlyReadOnly.class)
                .assetFile("readonly.realm")
                .readOnly()
                .build();
        Realm realm = Realm.getInstance(config);
        try {
            realm.beginTransaction();
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(),
                    CoreMatchers.containsString("Can't perform transactions on read-only Realms."));
        } finally {
            realm.close();
        }
    }

    @Test
    public void getInstance_wrongSchemaInReadonlyThrows() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .name("readonly.realm")
                .schema(StringOnlyReadOnly.class, AllJavaTypes.class)
                .assetFile("readonly.realm")
                .readOnly()
                .build();

        // This will throw because the Realm doesn't have the correct schema, and a new file cannot be re-created
        // because it is read only.
        try {
            realm = Realm.getInstance(config);
            fail();
        } catch (RealmMigrationNeededException ignored) {
        }
    }

    // https://github.com/realm/realm-java/issues/5570
    @Test
    public void getInstance_migrationExceptionThrows_migrationBlockDefiend_realmInstancesShouldBeClosed() {
        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .name("readonly.realm")
                .schema(StringOnlyReadOnly.class, AllJavaTypes.class)
                .schemaVersion(2)
                .assetFile("readonly.realm")
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                    }
                })
                .build();

        try {
            realm = Realm.getInstance(config);
            fail();
        } catch (RealmMigrationNeededException ignored) {
            // No Realm instance should be opened at this time.
            Realm.deleteRealm(config);
        }
    }
}
