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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.StringOnly;
import io.realm.exceptions.RealmFileException;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmCacheTests {

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private RealmConfiguration defaultConfig;
    private Context context;

    @Before
    public void setUp() {
        defaultConfig = configFactory.createConfiguration();
        context = InstrumentationRegistry.getInstrumentation().getContext();
    }

    // Tests that the closed Realm isn't kept in the Realm instance cache.
    @Test
    public void typedRealmCacheIsCleared() {
        Realm typedRealm = Realm.getInstance(defaultConfig);
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(defaultConfig);

        typedRealm.close(); // Still a dynamic instance open, but the typed Realm cache must still be cleared.
        dynamicRealm.close();

        Realm typedRealm1 = Realm.getInstance(defaultConfig);
        try {
            assertFalse(typedRealm == typedRealm1); // Must be different instance.
            // If cache isn't cleared this would crash because of a closed shared group.
            assertEquals(0, typedRealm1.where(AllTypes.class).count());
        } finally {
            typedRealm1.close();
        }
    }

    // Tests that the closed DynamicRealms isn't kept in the DynamicRealm instance cache.
    @Test
    public void dynamicRealmCacheIsCleared() {
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

    @Test
    public void getInstanceClearsCacheWhenFailed() {
        String REALM_NAME = "invalid_cache.realm";
        RealmConfiguration configA = configFactory.createConfiguration(REALM_NAME,
                TestHelper.getRandomKey(42));
        RealmConfiguration configB = configFactory.createConfiguration(REALM_NAME,
                TestHelper.getRandomKey(43));

        Realm realm = Realm.getInstance(configA); // Creates starting Realm with key 1.
        realm.close();
        try {
            Realm.getInstance(configB); // Tries to open with key 2.
            fail();
        } catch (RealmFileException expected) {
            assertEquals(RealmFileException.Kind.ACCESS_ERROR, expected.getKind());
            // Deletes Realm so key 2 works. This should work as a Realm shouldn't be cached
            // if initialization failed.
            assertTrue(Realm.deleteRealm(configA));
            realm = Realm.getInstance(configB);
            realm.close();
        }
    }

    @Test
    public void realmCache() {
        Realm realm = Realm.getInstance(defaultConfig);
        Realm newRealm = Realm.getInstance(defaultConfig);
        try {
            assertEquals(realm, newRealm);
        } finally {
            realm.close();
            newRealm.close();
        }
    }

    // We should not cache wrong configurations.
    @Test
    public void dontCacheWrongConfigurations() throws IOException {
        Realm testRealm;
        String REALM_NAME = "encrypted.realm";

        RealmConfiguration wrongConfig = configFactory.createConfigurationBuilder()
                .name(REALM_NAME)
                .encryptionKey(TestHelper.SHA512("foo"))
                .schema(StringOnly.class)
                .build();

        RealmConfiguration rightConfig = configFactory.createConfigurationBuilder()
                .name(REALM_NAME)
                .encryptionKey(TestHelper.SHA512("realm"))
                .schema(StringOnly.class)
                .build();

        // Create the realm with proper key.
        testRealm = Realm.getInstance(rightConfig);
        assertNotNull(testRealm);
        testRealm.close();

        // Opens Realm with wrong key.
        try {
            Realm.getInstance(wrongConfig);
            fail();
        } catch (RealmFileException expected) {
            assertEquals(RealmFileException.Kind.ACCESS_ERROR, expected.getKind());
        }

        // Tries again with proper key.
        testRealm = Realm.getInstance(rightConfig);
        assertNotNull(testRealm);
        testRealm.close();
    }

    @Test
    public void deletingRealmAlsoClearsConfigurationCache() throws IOException {
        String REALM_NAME = "encrypted.realm";
        byte[] oldPassword = TestHelper.SHA512("realm");
        byte[] newPassword = TestHelper.SHA512("realm-copy");

        RealmConfiguration config = configFactory.createConfigurationBuilder()
                .name(REALM_NAME)
                .encryptionKey(oldPassword)
                .schema(StringOnly.class)
                .build();

        // 1. Writes a copy of the encrypted Realm to a new file.
        Realm testRealm = Realm.getInstance(config);
        File copiedRealm = new File(config.getRealmDirectory(), "encrypted-copy.realm");
        if (copiedRealm.exists()) {
            assertTrue(copiedRealm.delete());
        }
        testRealm.writeEncryptedCopyTo(copiedRealm, newPassword);
        testRealm.close();

        // 2. Deletes the old Realm.
        assertTrue(Realm.deleteRealm(config));

        // 3. Renames the new file to the old file name.
        assertTrue(copiedRealm.renameTo(new File(config.getRealmDirectory(), REALM_NAME)));

        // 4. Tries to open the file again with the new password.
        // If the configuration cache wasn't cleared this would fail as we would detect two
        // configurations with 2 different passwords pointing to the same file.
        RealmConfiguration newConfig = configFactory.createConfigurationBuilder()
                .name(REALM_NAME)
                .encryptionKey(newPassword)
                .schema(StringOnly.class)
                .build();

        testRealm = Realm.getInstance(newConfig);
        assertNotNull(testRealm);
        testRealm.close();
    }

    // Tests that if the same Realm file is opened on multiple threads, we only need to validate the
    // schema on the first thread
    // When there is a transaction holding by a typed Realm in one thread, getInstance from the
    // other thread should not be blocked since we have cached the schemas already.
    @Test
    public void getInstance_shouldNotBeBlockedByTransactionInAnotherThread()
            throws InterruptedException {
        Realm realm = Realm.getInstance(defaultConfig);
        final CountDownLatch latch = new CountDownLatch(1);
        realm.beginTransaction();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(defaultConfig);
                realm.close();
                latch.countDown();
            }
        });
        thread.start();
        TestHelper.awaitOrFail(latch);
        realm.cancelTransaction();
        realm.close();
    }

    @Test
    public void differentThreadsDifferentInstance() throws InterruptedException {
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

        TestHelper.awaitOrFail(closeLatch);
        RealmCache.invokeWithGlobalRefCount(defaultConfig, new TestHelper.ExpectedCountCallback(1));
        realmA.close();
        RealmCache.invokeWithGlobalRefCount(defaultConfig, new TestHelper.ExpectedCountCallback(0));
    }

    @Test
    public void getInstance_differentConfigurationsShouldNotBlockEachOther() throws InterruptedException {
        final CountDownLatch bgThreadStarted = new CountDownLatch(1);
        final CountDownLatch realm2CreatedLatch = new CountDownLatch(1);

        final RealmConfiguration config1 = configFactory.createConfigurationBuilder()
                .name("config1.realm")
                .schema(AllJavaTypes.class)
                .initialData(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        bgThreadStarted.countDown();
                        TestHelper.awaitOrFail(realm2CreatedLatch);
                    }
                })
                .build();

        RealmConfiguration config2 = configFactory.createConfigurationBuilder()
                .name("config2.realm")
                .schema(AllJavaTypes.class)
                .build();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(config1);
                realm.close();
            }
        });
        thread.start();

        TestHelper.awaitOrFail(bgThreadStarted);
        Realm realm = Realm.getInstance(config2);
        realm2CreatedLatch.countDown();
        realm.close();
        thread.join();
    }

    @Test
    public void releaseCacheInOneThread() {
        // Tests release typed Realm instance.
        Realm realmA = RealmCache.createRealmOrGetFromCache(defaultConfig, Realm.class);
        Realm realmB = RealmCache.createRealmOrGetFromCache(defaultConfig, Realm.class);
        realmA.close();
        assertNotNull(realmA.sharedRealm);
        realmB.close();
        assertNull(realmB.sharedRealm);
        // No crash but warning in the log.
        realmB.close();

        // Tests release dynamic Realm instance.
        DynamicRealm dynamicRealmA = RealmCache.createRealmOrGetFromCache(defaultConfig,
                DynamicRealm.class);
        DynamicRealm dynamicRealmB = RealmCache.createRealmOrGetFromCache(defaultConfig,
                DynamicRealm.class);
        dynamicRealmA.close();
        assertNotNull(dynamicRealmA.sharedRealm);
        dynamicRealmB.close();
        assertNull(dynamicRealmB.sharedRealm);
        // No crash but warning in the log.
        dynamicRealmB.close();

        // Tests both typed Realm and dynamic Realm in same thread.
        realmA = RealmCache.createRealmOrGetFromCache(defaultConfig, Realm.class);
        dynamicRealmA = RealmCache.createRealmOrGetFromCache(defaultConfig, DynamicRealm.class);
        realmA.close();
        assertNull(realmA.sharedRealm);
        dynamicRealmA.close();
        assertNull(realmA.sharedRealm);
    }

    @Test
    @RunTestInLooperThread
    public void getInstanceAsync_typedRealm() {
        final RealmConfiguration configuration = looperThread.createConfiguration();
        final AtomicBoolean realmCreated = new AtomicBoolean(false);
        Realm.getInstanceAsync(configuration, new Realm.Callback() {
            @Override
            public void onSuccess(Realm realm) {
                realmCreated.set(true);
                assertEquals(1, Realm.getLocalInstanceCount(configuration));
                realm.close();
                looperThread.testComplete();
            }
        });
        assertFalse(realmCreated.get());
    }

    @Test
    @RunTestInLooperThread
    public void getInstanceAsync_dynamicRealm() {
        final RealmConfiguration configuration = looperThread.createConfiguration();
        final AtomicBoolean realmCreated = new AtomicBoolean(false);
        DynamicRealm.getInstanceAsync(configuration, new DynamicRealm.Callback() {
            @Override
            public void onSuccess(DynamicRealm realm) {
                realmCreated.set(true);
                assertEquals(1, Realm.getLocalInstanceCount(configuration));
                realm.close();
                looperThread.testComplete();
            }
        });
        assertFalse(realmCreated.get());
    }

    @Test
    @RunTestInLooperThread
    public void getInstanceAsync_callbackDeliveredInFollowingEventLoopWhenLocalCacheExist() {
        final RealmConfiguration configuration = looperThread.createConfiguration();
        final AtomicBoolean realmCreated = new AtomicBoolean(false);
        final Realm localRealm = Realm.getInstance(configuration);
        Realm.getInstanceAsync(configuration, new Realm.Callback() {
            @Override
            public void onSuccess(Realm realm) {
                realmCreated.set(true);
                assertEquals(2, Realm.getLocalInstanceCount(configuration));
                assertSame(realm, localRealm);
                realm.close();
                localRealm.close();
                looperThread.testComplete();
            }
        });
        assertFalse(realmCreated.get());
    }

    @Test
    @RunTestInLooperThread
    public void getInstanceAsync_callbackDeliveredInFollowingEventLoopWhenGlobalCacheExist() throws InterruptedException {
        final RealmConfiguration configuration = looperThread.createConfiguration();
        final AtomicBoolean realmCreated = new AtomicBoolean(false);
        final CountDownLatch globalRealmCreated = new CountDownLatch(1);
        final CountDownLatch getAsyncFinishedLatch = new CountDownLatch(1);

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(configuration);
                globalRealmCreated.countDown();
                TestHelper.awaitOrFail(getAsyncFinishedLatch);
                realm.close();
            }
        });
        thread.start();

        TestHelper.awaitOrFail(globalRealmCreated);
        Realm.getInstanceAsync(configuration, new Realm.Callback() {
            @Override
            public void onSuccess(Realm realm) {
                realmCreated.set(true);
                assertEquals(1, Realm.getLocalInstanceCount(configuration));
                realm.close();
                getAsyncFinishedLatch.countDown();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    fail();
                }
                looperThread.testComplete();
            }
        });
        assertFalse(realmCreated.get());
    }

    @Test
    @RunTestInLooperThread
    public void getInstanceAsync_typedRealmShouldStillBeInitializedInBGIfOnlyDynamicRealmExists() {
        final RealmConfiguration configuration = looperThread.createConfiguration();
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(configuration);
        final AtomicBoolean realmCreated = new AtomicBoolean(false);

        Realm.getInstanceAsync(configuration, new Realm.Callback() {
            @Override
            public void onSuccess(Realm realm) {
                realmCreated.set(false);
                assertEquals(2, Realm.getLocalInstanceCount(configuration));
                dynamicRealm.close();
                realm.close();
                looperThread.testComplete();
            }
        });
        // Callback should not be called immediately since we need to create column indices cache in bg thread.
        // Only a local dynamic Realm instance existing at this time.
        assertFalse(realmCreated.get());
        assertEquals(1, Realm.getLocalInstanceCount(configuration));
    }

    @Test
    @RunTestInLooperThread
    public void getInstanceAsync_onError() {
        final RealmConfiguration configuration =
                looperThread.createConfigurationBuilder()
                .assetFile("NotExistingFile")
                .build();
        Realm.getInstanceAsync(configuration, new Realm.Callback() {
            @Override
            public void onSuccess(Realm realm) {
                fail();
            }

            @Override
            public void onError(Throwable exception) {
                assertTrue(exception instanceof RealmFileException);
                looperThread.testComplete();
            }
        });
    }

    // If the async task is canceled before the posted event to create Realm instance in caller thread, the event should
    // just be ignored.
    @Test
    @RunTestInLooperThread
    public void getInstanceAsync_cancelBeforePostShouldNotCreateRealmInstanceOnTheCallerThread() {
        final AtomicReference<RealmAsyncTask> realmAsyncTasks = new AtomicReference<>();
        final Runnable finishedRunnable = new Runnable() {
            @Override
            public void run() {
                looperThread.testComplete();
            }
        };
        final RealmConfiguration configuration = looperThread.createConfigurationBuilder()
                .name("will_be_canceled")
                .initialData(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        // The BG thread started to initial the first Realm instance. Post an event to the caller's
                        // queue to cancel the task before the event to create the Realm instance in caller thread.
                        looperThread.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                assertNotNull(realmAsyncTasks.get());
                                realmAsyncTasks.get().cancel();
                                // Wait the async task to be terminated.
                                TestHelper.waitRealmThreadExecutorFinish();
                                // Finish the test.
                                looperThread.postRunnable(finishedRunnable);
                            }
                        });
                    }
                })
                .build();

        realmAsyncTasks.set(Realm.getInstanceAsync(configuration, new Realm.Callback() {
            @Override
            public void onSuccess(Realm realm) {
                fail();
            }
        }));
    }

    // The DynamicRealm and Realm with the same Realm path should share the same RealmCache
    @Test
    public void typedRealmAndDynamicRealmShareTheSameCache() {
        final String DB_NAME = "same_name.realm";
        RealmConfiguration config1 = configFactory.createConfigurationBuilder()
                .name(DB_NAME)
                .build();

        RealmConfiguration config2 = configFactory.createConfigurationBuilder()
                .name(DB_NAME)
                .initialData(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        // Because of config1 doesn't have initialData block, these two configurations are not the same.
                        // So if a Realm is created with config1, then create another Realm with config2 should just
                        // fail before executing this block.
                        fail();
                    }
                })
                .build();

        DynamicRealm dynamicRealm = DynamicRealm.getInstance(config1);
        Realm realm = null;
        try {
            realm = Realm.getInstance(config2);
            fail();
        } catch (IllegalArgumentException ignored) {
        } finally {
            dynamicRealm.close();
            if (realm != null) {
                realm.close();
            }
        }
    }
}
