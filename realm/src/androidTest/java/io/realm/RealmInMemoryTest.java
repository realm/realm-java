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

import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.entities.Dog;

public class RealmInMemoryTest extends AndroidTestCase {

    private final static String IDENTIFIER = "InMemRealTest";

    protected Realm testRealm;

    @Override
    protected void setUp() throws Exception {

        // Delete the same name realm file just in case
        RealmConfiguration configuration = new RealmConfiguration.Builder(getContext())
                .name(IDENTIFIER).build();
        Realm.deleteRealm(configuration);

        testRealm = Realm.getInMemoryInstance(getContext(), IDENTIFIER);
    }

    @Override
    protected void tearDown() throws Exception {
        if (testRealm != null) {
            testRealm.close();
        }
    }

    public void testInMemoryRealm() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("DinoDog");
        testRealm.commitTransaction();

        assertEquals(testRealm.allObjects(Dog.class).size(), 1);
        assertEquals(testRealm.allObjects(Dog.class).first().getName(), "DinoDog");
        testRealm.close();

        // After all references to the in-mem-realm destroyed,
        // in-mem-realm with same identifier should create a fresh new instance.
        testRealm = Realm.getInMemoryInstance(getContext(), IDENTIFIER);
        assertEquals(testRealm.allObjects(Dog.class).size(), 0);
    }

    public void testSameIdentifierRealmOnDisk() {
        // Create in-mem-realm (testRealm created in setUp) first, then create on-disk-realm with the same name
        try {
            Realm.getInstance(
                    new RealmConfiguration.Builder(getContext()).name(IDENTIFIER).build());
            fail("Realm.getInstance should fail with illegal argument");
        } catch (IllegalArgumentException iae) { //NOPMD
        }

        // Create on-disk-realm first, then create in-mem-realm with the same name
        testRealm.close();
        testRealm = null;
        Realm realmOnDisk = Realm.getInstance(
                new RealmConfiguration.Builder(getContext()).name(IDENTIFIER).build());
        try {
            testRealm = Realm.getInMemoryInstance(getContext(), IDENTIFIER);
            fail("Realm.getInMemoryInstance should fail with illegal argument");
        } catch (IllegalArgumentException iae) { //NOPMD
        }

        realmOnDisk.close();
    }

    public void testDelete() {
        RealmConfiguration configuration = testRealm.getConfiguration();
        try {
            Realm.deleteRealm(configuration);
            fail("Realm.deleteRealm should fail with illegal state");
        } catch (IllegalStateException iae) { //NOPMD
        }

        // Nothing should happen when delete a closed in-mem-realm.
        testRealm.close();
        testRealm = null;
        Realm.deleteRealm(configuration);
    }

    public void testWriteCopyTo() throws IOException {
        String fileName = IDENTIFIER + ".realm";
        String encFileName = IDENTIFIER + ".enc.realm";
        byte[] key = TestHelper.getRandomKey();

        Realm.deleteRealm(new RealmConfiguration.Builder(getContext()).name(fileName).build());
        Realm.deleteRealm(new RealmConfiguration.Builder(getContext()).name(encFileName).build());

        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("DinoDog");
        testRealm.commitTransaction();

        // Test a normal realm file
        testRealm.writeCopyTo(new File(getContext().getFilesDir(), fileName));
        Realm onDiskRealm = Realm.getInstance(new RealmConfiguration.Builder(getContext()).name(fileName).build());
        assertEquals(onDiskRealm.allObjects(Dog.class).size(), 1);
        onDiskRealm.close();

        // Test a encrypted realm file
        testRealm.writeEncryptedCopyTo(new File(getContext().getFilesDir(), encFileName), key);
        onDiskRealm = Realm.getInstance(new RealmConfiguration.Builder(getContext()).name(encFileName)
                .encryptionKey(key).build());
        assertEquals(onDiskRealm.allObjects(Dog.class).size(), 1);
        onDiskRealm.close();
        // Test with a wrong key
        try {
            Realm.getInstance(new RealmConfiguration.Builder(getContext()).name(encFileName)
                    .encryptionKey(TestHelper.getRandomKey(42)).build());
            fail("Realm.getInstance should fail with illegal argument");
        } catch (IllegalArgumentException iae) { //NOPMD
        }
    }

    public void testCreateWithConfiguration() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("DinoDog");
        testRealm.commitTransaction();

        RealmConfiguration configuration = new RealmConfiguration.Builder(getContext())
                .name(IDENTIFIER).inMemory().build();
        Realm realm = Realm.getInstance(configuration);

        assertEquals(realm.allObjects(Dog.class).first().getName(), "DinoDog");
        realm.close();
    }

    public void testMultiThread() throws InterruptedException, ExecutionException {
        final AtomicBoolean isWorkerThreadReady = new AtomicBoolean(false);
        final AtomicBoolean isWorkerThreadFinished = new AtomicBoolean(false);
        final AtomicBoolean isRealmInMainThreadClosed = new AtomicBoolean(false);

        Thread workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInMemoryInstance(getContext(), IDENTIFIER);
                realm.beginTransaction();
                Dog dog = realm.createObject(Dog.class);
                dog.setName("DinoDog");
                realm.commitTransaction();
                isWorkerThreadReady.set(true);

                assertEquals(realm.allObjects(Dog.class).size(), 1);

                // Wait until realm instance closed in main thread
                try {
                    while (!isRealmInMainThreadClosed.get()) {
                        Thread.sleep(5);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

        // Release the main thread realm reference, and the worker thread hold the reference still
        testRealm.close();
        isRealmInMainThreadClosed.set(true);

        // Create a new realm reference in main thread and checking the data.
        testRealm = Realm.getInMemoryInstance(getContext(), IDENTIFIER);
        assertEquals(testRealm.allObjects(Dog.class).size(), 1);
        testRealm.close();

        // Wait until the worker thread finished
        while (!isWorkerThreadFinished.get()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Since all previous realm instances has been closed before, below will create a fresh new in-mem-realm instance
        testRealm = Realm.getInMemoryInstance(getContext(), IDENTIFIER);
        assertEquals(testRealm.allObjects(Dog.class).size(), 0);
    }
}
