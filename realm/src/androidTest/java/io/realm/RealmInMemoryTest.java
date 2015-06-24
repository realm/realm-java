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
package io.realm;

import android.os.StrictMode;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.entities.Dog;

public class RealmInMemoryTest extends AndroidTestCase {

    private final static String IDENTIFIER = "InMemRealmTest";

    private Realm testRealm;
    private RealmConfiguration inMemConf;

    @Override
    protected void setUp() throws Exception {
        RealmConfiguration onDiskConf = new RealmConfiguration.Builder(getContext())
                .name(IDENTIFIER)
                .build();
        inMemConf = new RealmConfiguration.Builder(getContext())
                .name(IDENTIFIER)
                .inMemory()
                .build();

        // Delete the same name Realm file just in case
        Realm.deleteRealm(onDiskConf);
        testRealm = Realm.getInstance(inMemConf);
    }

    @Override
    protected void tearDown() throws Exception {
        if (testRealm != null) {
            testRealm.close();
        }
    }

    // Testing the in-memory Realm by Creating one instance, adding a record, then close the instance.
    // By the next time in-memory Realm instance with the same name created, it should be empty.
    // Use StrictMode to check no disk IO would happen in VM to this thread.
    public void testInMemoryRealm() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .penaltyDeath()
                .build());

        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("DinoDog");
        testRealm.commitTransaction();

        assertEquals(testRealm.allObjects(Dog.class).size(), 1);
        assertEquals(testRealm.allObjects(Dog.class).first().getName(), "DinoDog");

        testRealm.close();
        // After all references to the in-mem-realm closed,
        // in-mem-realm with same identifier should create a fresh new instance.
        testRealm = Realm.getInstance(inMemConf);
        assertEquals(testRealm.allObjects(Dog.class).size(), 0);

        StrictMode.enableDefaults();
    }

    // Two in-memory Realms with different names should not affect each other.
    public void testInMemoryRealmWithDifferentNames() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("DinoDog");
        testRealm.commitTransaction();

        // Create the 2nd in-memory Realm with a different name. To make sure they are not affecting each other.
        RealmConfiguration inMemConf2 = new RealmConfiguration.Builder(getContext())
                .name(IDENTIFIER + "2")
                .inMemory()
                .build();
        Realm testRealm2 = Realm.getInstance(inMemConf2);
        testRealm2.beginTransaction();
        Dog dog2 = testRealm2.createObject(Dog.class);
        dog2.setName("UFODog");
        testRealm2.commitTransaction();

        assertEquals(testRealm.allObjects(Dog.class).size(), 1);
        assertEquals(testRealm.allObjects(Dog.class).first().getName(), "DinoDog");
        assertEquals(testRealm2.allObjects(Dog.class).size(), 1);
        assertEquals(testRealm2.allObjects(Dog.class).first().getName(), "UFODog");

        testRealm2.close();
    }

    // Test deleteRealm called on a in-memory Realm instance
    public void testDelete() {
        RealmConfiguration configuration = testRealm.getConfiguration();
        try {
            Realm.deleteRealm(configuration);
            fail("Realm.deleteRealm should fail with illegal state");
        } catch (IllegalStateException ignored) {
        }

        // Nothing should happen when delete a closed in-mem-realm.
        testRealm.close();
        testRealm = null;
        assertTrue(Realm.deleteRealm(configuration));
    }

    // Test if an in-memory Realm can be written to disk with/without encryption
    public void testWriteCopyTo() throws IOException {
        byte[] key = TestHelper.getRandomKey();
        String fileName = IDENTIFIER + ".realm";
        String encFileName = IDENTIFIER + ".enc.realm";
        RealmConfiguration conf = new RealmConfiguration.Builder(getContext())
                .name(fileName)
                .build();
        RealmConfiguration encConf = new RealmConfiguration.Builder(getContext())
                .name(encFileName)
                .encryptionKey(key)
                .build();

        Realm.deleteRealm(conf);
        Realm.deleteRealm(encConf);

        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("DinoDog");
        testRealm.commitTransaction();

        // Test a normal Realm file
        testRealm.writeCopyTo(new File(getContext().getFilesDir(), fileName));
        Realm onDiskRealm = Realm.getInstance(conf);
        assertEquals(onDiskRealm.allObjects(Dog.class).size(), 1);
        onDiskRealm.close();

        // Test a encrypted Realm file
        testRealm.writeEncryptedCopyTo(new File(getContext().getFilesDir(), encFileName), key);
        onDiskRealm = Realm.getInstance(encConf);
        assertEquals(onDiskRealm.allObjects(Dog.class).size(), 1);
        onDiskRealm.close();
        // Test with a wrong key to see if it fails as expected.
        try {
            RealmConfiguration wrongKeyConf = new RealmConfiguration.Builder(getContext())
                    .name(encFileName)
                    .encryptionKey(TestHelper.getRandomKey(42))
                    .build();
            Realm.getInstance(wrongKeyConf);
            fail("Realm.getInstance should fail with illegal argument");
        } catch (IllegalArgumentException ignored) {
        }
    }

    // Test below scenario:
    // 1. Create a in-memory Realm instance in the main thread.
    // 2. Create a in-memory Realm with same name in another thread.
    // 3. Close the in-memory Realm instance in the main thread and the Realm data should not be released since
    //    another instance is still held by the other thread.
    // 4. Close the in-memory Realm instance and the Realm data should be released since no more instance with the
    //    specific name exists.
    public void testMultiThread() throws InterruptedException, ExecutionException {
        final AtomicBoolean isWorkerThreadReady = new AtomicBoolean(false);
        final AtomicBoolean isWorkerThreadFinished = new AtomicBoolean(false);
        final AtomicBoolean isRealmInMainThreadClosed = new AtomicBoolean(false);

        // Step 2.
        Thread workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(inMemConf);
                realm.beginTransaction();
                Dog dog = realm.createObject(Dog.class);
                dog.setName("DinoDog");
                realm.commitTransaction();
                isWorkerThreadReady.set(true);

                assertEquals(realm.allObjects(Dog.class).size(), 1);

                // Wait until Realm instance closed in main thread
                try {
                    while (!isRealmInMainThreadClosed.get()) {
                        Thread.sleep(5);
                    }
                } catch (InterruptedException e) {
                    fail("Worker thread was interrupted.");
                }

                realm.close();
                isWorkerThreadFinished.set(true);
            }
        });
        workerThread.start();


        // Wait until the worker thread started
        while (!isWorkerThreadReady.get()) {
            Thread.sleep(5);
        }

        // refresh will be ran in the next loop, manually refresh it here.
        testRealm.refresh();
        assertEquals(testRealm.allObjects(Dog.class).size(), 1);

        // Step 3.
        // Release the main thread Realm reference, and the worker thread hold the reference still
        testRealm.close();
        // Step 4.
        isRealmInMainThreadClosed.set(true);

        // Create a new Realm reference in main thread and checking the data.
        testRealm = Realm.getInstance(inMemConf);
        assertEquals(testRealm.allObjects(Dog.class).size(), 1);
        testRealm.close();

        // Wait until the worker thread finished
        while (!isWorkerThreadFinished.get()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                fail("Worker thread was interrupted");
            }
        }
        // Since all previous Realm instances has been closed before, below will create a fresh new in-mem-realm instance
        testRealm = Realm.getInstance(inMemConf);
        assertEquals(testRealm.allObjects(Dog.class).size(), 0);
    }
}
