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
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.realm.entities.AllTypes;
import io.realm.entities.StringOnly;

public class RealmCacheTest extends AndroidTestCase {

    private RealmConfiguration defaultConfig;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        defaultConfig = TestHelper.createConfiguration(getContext());
        Realm.deleteRealm(defaultConfig);
    }

    // Test that the closed Realm isn't kept in the Realm instance cache
    public void testRealmCacheIsCleared() {
        Realm typedRealm = Realm.getInstance(defaultConfig);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(defaultConfig);

        typedRealm.close(); // Still a dynamic instance open, but the typed Realm cache must still be cleared.
        dynamicRealm.close();

        Realm typedRealm1 = Realm.getInstance(defaultConfig);
        try {
            assertFalse(typedRealm == typedRealm1); // Must be different instance
            // If cache isn't cleared this would crash because of a closed shared group.
            assertEquals(0, typedRealm1.where(AllTypes.class).count());
        } finally {
            typedRealm1.close();
        }
    }

    // Test that the closed DynamicRealms isn't kept in the DynamicRealm instance cache
    public void testDynamicRealmCacheIsCleared() {
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(defaultConfig);
        Realm typedRealm = Realm.getInstance(defaultConfig);

        dynamicRealm.close(); // Still an instance open, but DynamicRealm cache must still be cleared.
        typedRealm.close();

        DynamicRealm dynamicRealm1 = DynamicRealm.getInstance(defaultConfig);
        try {
            // If cache isn't cleared this would crash because of a closed shared group.
            assertFalse(dynamicRealm == dynamicRealm1);
            assertEquals(0, dynamicRealm1.getVersion());
        } finally {
            dynamicRealm1.close();
        }
    }

    public void testGetInstanceClearsCacheWhenFailed() {
        String REALM_NAME = "invalid_cache.realm";
        RealmConfiguration configA = TestHelper.createConfiguration(getContext(), REALM_NAME, TestHelper.getRandomKey(42));
        RealmConfiguration configB = TestHelper.createConfiguration(getContext(), REALM_NAME, TestHelper.getRandomKey(43));

        Realm.deleteRealm(configA);
        Realm realm = Realm.getInstance(configA); // Create starting Realm with key1
        realm.close();
        try {
            Realm.getInstance(configB); // Try to open with key 2
        } catch (IllegalArgumentException ignored) {
            // Delete Realm so key 2 works. This should work as a Realm shouldn't be cached
            // if initialization failed.
            assertTrue(Realm.deleteRealm(configA));
            realm = Realm.getInstance(configB);
            realm.close();
        }
    }

    public void testRealmCache() {
        Realm realm = Realm.getInstance(defaultConfig);
        Realm newRealm = Realm.getInstance(defaultConfig);
        try {
            assertEquals(realm, newRealm);
        } finally {
            realm.close();
            newRealm.close();
        }
    }

    // We should not cache wrong configurations
    public void testDontCacheWrongConfigurations() throws IOException {
        Realm testRealm;
        String REALM_NAME = "encrypted.realm";
        TestHelper.copyRealmFromAssets(getContext(), REALM_NAME, REALM_NAME);
        RealmMigration realmMigration = TestHelper.prepareMigrationToNullSupportStep();

        RealmConfiguration wrongConfig = new RealmConfiguration.Builder(getContext())
                .name(REALM_NAME)
                .encryptionKey(TestHelper.SHA512("foo"))
                .migration(realmMigration)
                .schema(StringOnly.class)
                .build();

        RealmConfiguration rightConfig = new RealmConfiguration.Builder(getContext())
                .name(REALM_NAME)
                .encryptionKey(TestHelper.SHA512("realm"))
                .migration(realmMigration)
                .schema(StringOnly.class)
                .build();

        // Open Realm with wrong key
        try {
            Realm.getInstance(wrongConfig);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        // Try again with proper key
        testRealm = Realm.getInstance(rightConfig);
        assertNotNull(testRealm);
        testRealm.close();
    }

    public void testDeletingRealmAlsoClearsConfigurationCache() throws IOException {
        String REALM_NAME = "encrypted.realm";
        byte[] oldPassword = TestHelper.SHA512("realm");
        byte[] newPassword = TestHelper.SHA512("realm-copy");

        TestHelper.copyRealmFromAssets(getContext(), REALM_NAME, REALM_NAME);
        RealmMigration realmMigration = TestHelper.prepareMigrationToNullSupportStep();

        RealmConfiguration config = new RealmConfiguration.Builder(getContext())
                .name(REALM_NAME)
                .encryptionKey(oldPassword)
                .migration(realmMigration)
                .schema(StringOnly.class)
                .build();

        // 1. Write a copy of the encrypted Realm to a new file
        Realm testRealm = Realm.getInstance(config);
        File copiedRealm = new File(config.getRealmFolder(), "encrypted-copy.realm");
        if (copiedRealm.exists()) {
            assertTrue(copiedRealm.delete());
        }
        testRealm.writeEncryptedCopyTo(copiedRealm, newPassword);
        testRealm.close();

        // 2. Delete the old Realm.
        Realm.deleteRealm(config);

        // 3. Rename the new file to the old file name.
        assertTrue(copiedRealm.renameTo(new File(config.getRealmFolder(), REALM_NAME)));

        // 4. Try to open the file again with the new password
        // If the configuration cache wasn't cleared this would fail as we would detect two
        // configurations with 2 different passwords pointing to the same file.
        RealmConfiguration newConfig = new RealmConfiguration.Builder(getContext())
                .name(REALM_NAME)
                .encryptionKey(newPassword)
                .migration(realmMigration)
                .schema(StringOnly.class)
                .build();

        testRealm = Realm.getInstance(newConfig);
        assertNotNull(testRealm);
        testRealm.close();
    }

    // Tests that if the same Realm file is opened on multiple threads, we only need to validate the schema on the first thread.
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

    public void testDifferentThreadsDifferentInstance() throws InterruptedException {
        final CountDownLatch closeLatch = new CountDownLatch(1);

        final Realm realmA = Realm.getInstance(defaultConfig);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realmB = Realm.getInstance(defaultConfig);
                assertFalse(realmA == realmB);
                RealmCache.invokeWithGlobalRefCount(defaultConfig,
                        new TestHelper.ExpectedCountCallback(2));
                realmB.close();
                closeLatch.countDown();
            }
        });
        thread.start();

        closeLatch.await();
        RealmCache.invokeWithGlobalRefCount(defaultConfig, new TestHelper.ExpectedCountCallback(1));
        realmA.close();
        RealmCache.invokeWithGlobalRefCount(defaultConfig, new TestHelper.ExpectedCountCallback(0));
    }

    public void testReleaseCacheInOneThread() {
        // Test release typed Realm instance
        Realm realmA = RealmCache.createRealmOrGetFromCache(defaultConfig, Realm.class);
        Realm realmB = RealmCache.createRealmOrGetFromCache(defaultConfig, Realm.class);
        RealmCache.release(realmA);
        assertNotNull(realmA.sharedGroupManager);
        RealmCache.release(realmB);
        assertNull(realmB.sharedGroupManager);
        // No crash but warning in the log
        RealmCache.release(realmB);

        // Test release dynamic Realm instance
        DynamicRealm dynamicRealmA = RealmCache.createRealmOrGetFromCache(defaultConfig,
                DynamicRealm.class);
        DynamicRealm dynamicRealmB = RealmCache.createRealmOrGetFromCache(defaultConfig,
                DynamicRealm.class);
        RealmCache.release(dynamicRealmA);
        assertNotNull(dynamicRealmA.sharedGroupManager);
        RealmCache.release(dynamicRealmB);
        assertNull(dynamicRealmB.sharedGroupManager);
        // No crash but warning in the log
        RealmCache.release(dynamicRealmB);

        // Test both typed Realm and dynamic Realm in same thread
        realmA = RealmCache.createRealmOrGetFromCache(defaultConfig, Realm.class);
        dynamicRealmA = RealmCache.createRealmOrGetFromCache(defaultConfig, DynamicRealm.class);
        RealmCache.release(realmA);
        assertNull(realmA.sharedGroupManager);
        RealmCache.release(dynamicRealmA);
        assertNull(realmA.sharedGroupManager);
    }
}
