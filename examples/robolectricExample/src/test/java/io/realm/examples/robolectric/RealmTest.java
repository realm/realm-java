/*
 * Copyright 2015 Realm Inc.
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

package io.realm.examples.robolectric;

import android.app.Activity;
import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmIOException;
import io.realm.examples.robolectric.entities.AllTypes;
import io.realm.examples.robolectric.entities.Dog;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class RealmTest {
    protected final static int TEST_DATA_SIZE = 10;

    protected List<String> columnData = new ArrayList<String>();

    private final static String FIELD_STRING = "columnString";
    private final static String FIELD_LONG = "columnLong";
    private final static String FIELD_FLOAT = "columnFloat";
    private final static String FIELD_DOUBLE = "columnDouble";
    private final static String FIELD_BOOLEAN = "columnBoolean";
    private final static String FIELD_DATE = "columnDate";

    private Realm testRealm;
    private Activity context;
    private RealmConfiguration testConfig;

    private Context getContext() {
        return context;
    }

    @BeforeClass
    public static void beforeClassSetUp() {
        System.setProperty("java.library.path", "./robolectricLibs");
        ShadowLog.stream = System.out;
    }

    @Before
    public void setUp() throws Exception {
        context = Robolectric.setupActivity(MainActivity.class);
        testConfig = TestHelper.createConfiguration(getContext());
        Realm.deleteRealm(testConfig);
        testRealm = Realm.getInstance(testConfig);
    }

    @After
    public void tearDown() throws Exception {
        if (testRealm != null) {
            testRealm.close();
        }
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
        String REALM_FILE = "readonly.testRealm";
        File folder = getContext().getFilesDir();
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
    public void testGetInstanceClearsCacheWhenFailed() {
        String REALM_NAME = "invalid_cache.testRealm";
        RealmConfiguration configA = TestHelper.createConfiguration(getContext(), REALM_NAME, TestHelper.getRandomKey(42));
        RealmConfiguration configB = TestHelper.createConfiguration(getContext(), REALM_NAME, TestHelper.getRandomKey(43));

        Realm.deleteRealm(configA);
        Realm realm = Realm.getInstance(configA); // Create starting Realm with key1
        realm.close();
        try {
            Realm.getInstance(configB); // Try to open with key 2
        } catch (IllegalArgumentException expected) {
            // Delete Realm so key 2 works. This should work as a Realm shouldn't be cached
            // if initialization failed.
            assertTrue(Realm.deleteRealm(configA));
            realm = Realm.getInstance(configB);
            realm.close();
        }
    }

    @Test
    public void testRealmCache() {
        Realm newRealm = Realm.getInstance(getContext());
        try {
            assertEquals(testRealm, newRealm);
        } finally {
            newRealm.close();
        }
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

    @Test
    public void testCommitTransactionAfterCancelTransaction () {
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
        assertFalse(Realm.compactRealm(TestHelper.createConfiguration(getContext())));
    }

    @Test
    public void testCompactEncryptedEmptyRealmFile() {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext())
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
    public void testCompactEmptyRealmFile() throws IOException {
        final String REALM_NAME = "test.realm";
        RealmConfiguration realmConfig = TestHelper.createConfiguration(getContext(), REALM_NAME);
        Realm.deleteRealm(realmConfig);
        Realm realm = Realm.getInstance(realmConfig);
        realm.close();
        long before = new File(getContext().getFilesDir(), REALM_NAME).length();
        assertTrue(Realm.compactRealm(realmConfig));
        long after = new File(getContext().getFilesDir(), REALM_NAME).length();
        assertTrue(before >= after);
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
    public void testOpeningOfEncryptedRealmWithDifferentKeyInstances() {
        byte[] key1 = TestHelper.getRandomKey(42);
        byte[] key2 = TestHelper.getRandomKey(42);

        // Make sure the key is the same, but in two different instances
        assertArrayEquals(key1, key2);
        assertTrue(key1 != key2);

        final String ENCRYPTED_REALM = "differentKeys.realm";
        Realm.deleteRealm(TestHelper.createConfiguration(getContext(), ENCRYPTED_REALM));
        Realm realm1 = null;
        Realm realm2 = null;
        try {
            realm1 = Realm.getInstance(new RealmConfiguration.Builder(getContext())
                            .name(ENCRYPTED_REALM)
                            .encryptionKey(key1)
                            .build()
            );
            try {
                realm2 = Realm.getInstance(new RealmConfiguration.Builder(getContext())
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
    public void testOpenRealmFileDeletionShouldThrow() {
        final String OTHER_REALM_NAME = "yetAnotherRealm.realm";

        RealmConfiguration configA = TestHelper.createConfiguration(getContext());
        RealmConfiguration configB = TestHelper.createConfiguration(getContext(), OTHER_REALM_NAME);

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
    public void testDeleteNonRealmFile() throws IOException {
        File tmpFile = new File(getContext().getFilesDir(), "tmp");
        tmpFile.delete();
        assertTrue(tmpFile.createNewFile());
    }

    @Test
    public void testCannotCreateDynamicRealmObject() {
        testRealm.beginTransaction();
        try {
            testRealm.createObject(DynamicRealmObject.class);
            fail();
        } catch (RealmException ignored) {
        }
    }

    @Test
    public void testRealmIsClosed() {
        assertFalse(testRealm.isClosed());
        testRealm.close();
        assertTrue(testRealm.isClosed());
    }

    @Test
    public void testValidateSchemasOverThreads() throws InterruptedException, TimeoutException, ExecutionException {
        final RealmConfiguration config = TestHelper.createConfiguration(getContext(), "foo");
        Realm.deleteRealm(config);

        final CountDownLatch bgThreadLocked = new CountDownLatch(1);
        final CountDownLatch mainThreadDone = new CountDownLatch(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(config);
                realm.beginTransaction();
                bgThreadLocked.countDown();
                try {
                    mainThreadDone.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                }
                realm.close();
            }
        }).start();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Realm realm = Realm.getInstance(config);
                realm.close();
                mainThreadDone.countDown();
                return true;
            }
        });

        bgThreadLocked.await(2, TimeUnit.SECONDS);
        assertTrue(future.get(10, TimeUnit.SECONDS));
    }

    @Test
    public void testProcessLocalListenersAfterRefresh() throws InterruptedException {
        // Used to validate the result
        final AtomicBoolean listenerWasCalled = new AtomicBoolean(false);

        // Used by the background thread to wait for the main thread to do the write operation
        final CountDownLatch bgThreadLatch = new CountDownLatch(1);
        final CountDownLatch bgClosedLatch = new CountDownLatch(1);

        Thread backgroundThread = new Thread() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(testConfig);
                try {
                    bgRealm.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            listenerWasCalled.set(true);
                        }
                    });
                    bgThreadLatch.await(); // Wait for the main thread to do a write operation
                    bgRealm.refresh(); // This should call the listener
                    assertTrue(listenerWasCalled.get());
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
        awaitOrFail(bgClosedLatch);
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
}
