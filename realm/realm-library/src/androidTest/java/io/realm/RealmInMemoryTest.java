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

import android.support.test.runner.AndroidJUnit4;

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.realm.entities.Dog;
import io.realm.exceptions.RealmFileException;
import io.realm.rule.TestRealmConfigurationFactory;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmInMemoryTest {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private final static String IDENTIFIER = "InMemRealmTest";

    private Realm testRealm;
    private RealmConfiguration inMemConf;

    @Before
    public void setUp() {
        RealmConfiguration onDiskConf = configFactory.createConfigurationBuilder()
                .name(IDENTIFIER)
                .build();
        inMemConf = configFactory.createConfigurationBuilder()
                .name(IDENTIFIER)
                .inMemory()
                .build();

        // Deletes the same name Realm file just in case.
        Realm.deleteRealm(onDiskConf);
        testRealm = Realm.getInstance(inMemConf);
    }

    @After
    public void tearDown() {
        if (testRealm != null) {
            testRealm.close();
        }
    }

    // Tests the in-memory Realm by creating one instance, adding a record, then closes the instance.
    // By the next time in-memory Realm instance with the same name created, it should be empty.
    @Test
    public void inMemoryRealm() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("DinoDog");
        testRealm.commitTransaction();

        assertEquals(1, testRealm.where(Dog.class).count());
        assertEquals("DinoDog", testRealm.where(Dog.class).findFirst().getName());

        testRealm.close();
        // After all references to the in-mem-realm closed,
        // in-mem-realm with same identifier should create a fresh new instance.
        testRealm = Realm.getInstance(inMemConf);
        assertEquals(0, testRealm.where(Dog.class).count());
    }

    // Two in-memory Realms with different names should not affect each other.
    @Test
    public void inMemoryRealmWithDifferentNames() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("DinoDog");
        testRealm.commitTransaction();

        // Creates the 2nd in-memory Realm with a different name. To make sure they are not affecting each other.
        RealmConfiguration inMemConf2 = configFactory.createConfigurationBuilder()
                .name(IDENTIFIER + "2")
                .inMemory()
                .build();
        Realm testRealm2 = Realm.getInstance(inMemConf2);
        testRealm2.beginTransaction();
        Dog dog2 = testRealm2.createObject(Dog.class);
        dog2.setName("UFODog");
        testRealm2.commitTransaction();

        assertEquals(1, testRealm.where(Dog.class).count());
        //noinspection ConstantConditions
        assertEquals("DinoDog", testRealm.where(Dog.class).findFirst().getName());
        assertEquals(1, testRealm2.where(Dog.class).count());
        //noinspection ConstantConditions
        assertEquals("UFODog", testRealm2.where(Dog.class).findFirst().getName());

        testRealm2.close();
    }

    // Tests deleteRealm called on a in-memory Realm instance.
    @Test
    public void delete() {
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

    // Tests if an in-memory Realm can be written to disk with/without encryption.
    @Test
    public void writeCopyTo() {
        byte[] key = TestHelper.getRandomKey();
        String fileName = IDENTIFIER + ".realm";
        String encFileName = IDENTIFIER + ".enc.realm";
        RealmConfiguration conf = configFactory.createConfigurationBuilder()
                .name(fileName)
                .build();
        RealmConfiguration encConf = configFactory.createConfigurationBuilder()
                .name(encFileName)
                .encryptionKey(key)
                .build();

        Realm.deleteRealm(conf);
        Realm.deleteRealm(encConf);

        testRealm.beginTransaction();
        Dog dog = testRealm.createObject(Dog.class);
        dog.setName("DinoDog");
        testRealm.commitTransaction();

        // Tests a normal Realm file.
        testRealm.writeCopyTo(new File(configFactory.getRoot(), fileName));
        Realm onDiskRealm = Realm.getInstance(conf);
        assertEquals(1, onDiskRealm.where(Dog.class).count());
        onDiskRealm.close();

        // Tests a encrypted Realm file.
        testRealm.writeEncryptedCopyTo(new File(configFactory.getRoot(), encFileName), key);
        onDiskRealm = Realm.getInstance(encConf);
        assertEquals(1, onDiskRealm.where(Dog.class).count());
        onDiskRealm.close();
        // Tests with a wrong key to see if it fails as expected.
        try {
            RealmConfiguration wrongKeyConf = configFactory.createConfigurationBuilder()
                    .name(encFileName)
                    .encryptionKey(TestHelper.getRandomKey(42))
                    .build();
            Realm.getInstance(wrongKeyConf);
            fail("Realm.getInstance should fail with RealmFileException");
        } catch (RealmFileException expected) {
            assertEquals(RealmFileException.Kind.ACCESS_ERROR, expected.getKind());
        }
    }

    // Test below scenario:
    // 1. Creates a in-memory Realm instance in the main thread.
    // 2. Creates a in-memory Realm with same name in another thread.
    // 3. Closes the in-memory Realm instance in the main thread and the Realm data should not be released since
    //    another instance is still held by the other thread.
    // 4. Closes the in-memory Realm instance and the Realm data should be released since no more instance with the
    //    specific name exists.
    @Test
    public void multiThread() throws InterruptedException, ExecutionException {
        final CountDownLatch workerCommittedLatch = new CountDownLatch(1);
        final CountDownLatch workerClosedLatch = new CountDownLatch(1);
        final CountDownLatch realmInMainClosedLatch = new CountDownLatch(1);
        final AssertionFailedError threadError[] = new AssertionFailedError[1];

        // Step 2.
        Thread workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(inMemConf);
                realm.beginTransaction();
                Dog dog = realm.createObject(Dog.class);
                dog.setName("DinoDog");
                realm.commitTransaction();

                try {
                    assertEquals(1, realm.where(Dog.class).count());
                } catch (AssertionFailedError afe) {
                    threadError[0] = afe;
                    realm.close();
                    return;
                }
                workerCommittedLatch.countDown();

                // Waits until Realm instance closed in main thread.
                try {
                    realmInMainClosedLatch.await(TestHelper.SHORT_WAIT_SECS, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    threadError[0] = new AssertionFailedError("Worker thread was interrupted.");
                    realm.close();
                    return;
                }

                realm.close();
                workerClosedLatch.countDown();
            }
        });
        workerThread.start();


        // Waits until the worker thread started.
        workerCommittedLatch.await(TestHelper.SHORT_WAIT_SECS, TimeUnit.SECONDS);
        if (threadError[0] != null) { throw threadError[0]; }

        // Refreshes will be ran in the next loop, manually refreshes it here.
        testRealm.waitForChange();
        assertEquals(1, testRealm.where(Dog.class).count());

        // Step 3.
        // Releases the main thread Realm reference, and the worker thread holds the reference still.
        testRealm.close();

        // Step 4.
        // Creates a new Realm reference in main thread and checks the data.
        testRealm = Realm.getInstance(inMemConf);
        assertEquals(1, testRealm.where(Dog.class).count());
        testRealm.close();

        // Let the worker thread continue.
        realmInMainClosedLatch.countDown();

        // Waits until the worker thread finished.
        workerClosedLatch.await(TestHelper.SHORT_WAIT_SECS, TimeUnit.SECONDS);
        if (threadError[0] != null) { throw threadError[0]; }

        // Since all previous Realm instances has been closed before, below will create a fresh new in-mem-realm instance.
        testRealm = Realm.getInstance(inMemConf);
        assertEquals(0, testRealm.where(Dog.class).count());
    }
}
