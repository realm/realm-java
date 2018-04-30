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

import android.os.SystemClock;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.Owner;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;
import io.realm.util.RealmBackgroundTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmAsyncQueryTests {
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();


    // ****************************
    // ****  Async transaction  ***
    // ****************************

    // Starts asynchronously a transaction to insert one element.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync() throws Throwable {
        final Realm realm = looperThread.getRealm();
        assertEquals(0, realm.where(Owner.class).count());

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Owner owner = realm.createObject(Owner.class);
                owner.setName("Owner");
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                assertEquals(1, realm.where(Owner.class).count());
                assertEquals("Owner", realm.where(Owner.class).findFirst().getName());
                looperThread.testComplete();
            }
        }, new Realm.Transaction.OnError() {

            @Override
            public void onError(Throwable error) {
                fail(error.getMessage());
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_onSuccess() throws Throwable {
        final Realm realm = looperThread.getRealm();
        assertEquals(0, realm.where(Owner.class).count());

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Owner owner = realm.createObject(Owner.class);
                owner.setName("Owner");
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                assertEquals(1, realm.where(Owner.class).count());
                assertEquals("Owner", realm.where(Owner.class).findFirst().getName());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_onSuccessCallerRealmClosed() throws Throwable {
        final Realm realm = looperThread.getRealm();
        assertEquals(0, realm.where(Owner.class).count());

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Owner owner = realm.createObject(Owner.class);
                owner.setName("Owner");
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                assertTrue(realm.isClosed());
                Realm newRealm = Realm.getInstance(looperThread.getConfiguration());
                assertEquals(1, newRealm.where(Owner.class).count());
                assertEquals("Owner", newRealm.where(Owner.class).findFirst().getName());
                newRealm.close();
                looperThread.testComplete();
            }
        });
        realm.close();
    }

    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_onError() throws Throwable {
        final Realm realm = looperThread.getRealm();
        final RuntimeException runtimeException = new RuntimeException("Oh! What a Terrible Failure");
        assertEquals(0, realm.where(Owner.class).count());

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                throw runtimeException;
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                assertEquals(0, realm.where(Owner.class).count());
                assertNull(realm.where(Owner.class).findFirst());
                assertEquals(runtimeException, error);
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_onErrorCallerRealmClosed() throws Throwable {
        final Realm realm = looperThread.getRealm();
        final RuntimeException runtimeException = new RuntimeException("Oh! What a Terrible Failure");
        assertEquals(0, realm.where(Owner.class).count());

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                throw runtimeException;
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                assertTrue(realm.isClosed());
                Realm newRealm = Realm.getInstance(looperThread.getConfiguration());
                assertEquals(0, newRealm.where(Owner.class).count());
                assertNull(newRealm.where(Owner.class).findFirst());
                assertEquals(runtimeException, error);
                newRealm.close();
                looperThread.testComplete();
            }
        });
        realm.close();
    }

    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_NoCallbacks() throws Throwable {
        final Realm realm = looperThread.getRealm();
        assertEquals(0, realm.where(Owner.class).count());

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Owner owner = realm.createObject(Owner.class);
                owner.setName("Owner");
            }
        });
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                assertEquals("Owner", realm.where(Owner.class).findFirst().getName());
                looperThread.testComplete();
            }
        });
    }

    // Tests that an async transaction that throws when call cancelTransaction manually.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_cancelTransactionInside() throws Throwable {
        final TestHelper.TestLogger testLogger = new TestHelper.TestLogger(LogLevel.DEBUG);
        RealmLog.add(testLogger);

        final Realm realm = looperThread.getRealm();

        assertEquals(0, realm.where(Owner.class).count());

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Owner owner = realm.createObject(Owner.class);
                owner.setName("Owner");
                realm.cancelTransaction();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                fail("Should not reach success if runtime exception is thrown in callback.");
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // Ensure we are giving developers quality messages in the logs.
                assertTrue(testLogger.message.contains(
                        "Exception has been thrown: Can't commit a non-existing write transaction"));
                assertTrue(error instanceof IllegalStateException);
                RealmLog.remove(testLogger);
                looperThread.testComplete();
            }
        });
    }

    // Tests if the background Realm is closed when transaction success returned.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_realmClosedOnSuccess() {
        final AtomicInteger counter = new AtomicInteger(100);
        final Realm realm = looperThread.getRealm();
        final RealmCache.Callback cacheCallback = new RealmCache.Callback() {
            @Override
            public void onResult(int count) {
                assertEquals(1, count);
                if (counter.decrementAndGet() == 0) {
                    realm.close();
                    looperThread.testComplete();
                }
            }
        };
        final Realm.Transaction.OnSuccess transactionCallback = new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                RealmCache.invokeWithGlobalRefCount(realm.getConfiguration(), cacheCallback);
                if (counter.get() == 0) {
                    // Finishes testing.
                    return;
                }
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                    }
                }, this);
            }
        };

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
            }
        }, transactionCallback);
    }

    // Tests if the background Realm is closed when transaction error returned.
    @Test
    @RunTestInLooperThread
    public void executeTransaction_async_realmClosedOnError() {
        final AtomicInteger counter = new AtomicInteger(100);
        final Realm realm = looperThread.getRealm();
        final RealmCache.Callback cacheCallback = new RealmCache.Callback() {
            @Override
            public void onResult(int count) {
                assertEquals(1, count);
                if (counter.decrementAndGet() == 0) {
                    realm.close();
                    looperThread.testComplete();
                }
            }
        };
        final Realm.Transaction.OnError transactionCallback = new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                RealmCache.invokeWithGlobalRefCount(realm.getConfiguration(), cacheCallback);
                if (counter.get() == 0) {
                    // Finishes testing.
                    return;
                }
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        throw new RuntimeException("Dummy exception");
                    }
                }, this);
            }
        };

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                throw new RuntimeException("Dummy exception");
            }
        }, transactionCallback);
    }

    // Test case for https://github.com/realm/realm-java/issues/1893
    // Ensures that onSuccess is called with the correct Realm version for async transaction.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_asyncQuery() {
        final Realm realm = looperThread.getRealm();
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
        assertEquals(0, results.size());

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(AllTypes.class);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                assertEquals(1, realm.where(AllTypes.class).count());
                // We cannot guarantee the async results get delivered from OS.
                if (results.isLoaded()) {
                    assertEquals(1, results.size());
                } else {
                    assertEquals(0, results.size());
                }
                looperThread.testComplete();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                fail();
            }
        });
    }

    @Test
    public void executeTransactionAsync_onSuccessOnNonLooperThreadThrows() {
        Realm realm = Realm.getInstance(configFactory.createConfiguration());
        thrown.expect(IllegalStateException.class);
        try {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                }
            });
        } finally {
            realm.close();
        }
    }

    @Test
    public void executeTransactionAsync_onErrorOnNonLooperThreadThrows() {
        Realm realm = Realm.getInstance(configFactory.createConfiguration());
        thrown.expect(IllegalStateException.class);
        try {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                }
            });
        } finally {
            realm.close();
        }
    }

    // https://github.com/realm/realm-java/issues/4595#issuecomment-298830411
    // onSuccess might commit another transaction which will call didChange. So before calling async transaction
    // callbacks, the callback should be cleared.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_callbacksShouldBeClearedBeforeCalling()
            throws NoSuchFieldException, IllegalAccessException {
        final AtomicInteger callbackCounter = new AtomicInteger(0);
        final Realm foregroundRealm = looperThread.getRealm();

        // Use single thread executor
        TestHelper.replaceRealmThreadExecutor(RealmThreadPoolExecutor.newSingleThreadExecutor());

        // To reproduce the issue, the posted callback needs to arrived before the Object Store did_change called.
        // We just disable the auto refresh here then the did_change won't be called.
        foregroundRealm.setAutoRefresh(false);
        foregroundRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(AllTypes.class);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // This will be called first and only once
                assertEquals(0, callbackCounter.getAndIncrement());

                // This transaction should never trigger the onSuccess.
                foregroundRealm.beginTransaction();
                foregroundRealm.createObject(AllTypes.class);
                foregroundRealm.commitTransaction();
            }
        });

        foregroundRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(AllTypes.class);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // This will be called 2nd and only once
                assertEquals(1, callbackCounter.getAndIncrement());
                looperThread.testComplete();
            }
        });

        // Wait for all async tasks finish to ensure the async transaction posted callback will arrive first.
        TestHelper.resetRealmThreadExecutor();
        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                // Manually call refresh, so the did_change will be triggered.
                foregroundRealm.sharedRealm.refresh();
                foregroundRealm.setAutoRefresh(true);
            }
        });
    }

    // ************************************
    // *** promises based async queries ***
    // ************************************

    // Finds element [0-4] asynchronously then waits for the promise to be loaded.
    @Test
    @RunTestInLooperThread
    public void findAllAsync() throws Throwable {
        final Realm realm = looperThread.getRealm();
        populateTestRealm(realm, 10);
        final RealmResults<AllTypes> results = realm.where(AllTypes.class)
                .between("columnLong", 0, 4)
                .findAllAsync();

        assertFalse(results.isLoaded());
        assertEquals(0, results.size());

        looperThread.keepStrongReference(results);
        results.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertTrue(results.isLoaded());
                assertEquals(5, results.size());
                assertTrue(results.get(0).isValid());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void accessingRealmListOnUnloadedRealmObjectShouldThrow() {
        Realm realm = looperThread.getRealm();
        populateTestRealm(realm, 10);
        final AllTypes results = realm.where(AllTypes.class)
                .equalTo("columnLong", 0)
                .findFirstAsync();

        assertFalse(results.isLoaded());
        try {
            results.getColumnRealmList();
            fail("Accessing property on an empty row");
        } catch (IllegalStateException ignored) {
        }
        looperThread.testComplete();
    }

    @Test
    public void unmanagedObjectAsyncBehaviour() {
        Dog dog = new Dog();
        dog.setName("Akamaru");
        dog.setAge(10);

        assertTrue(dog.isLoaded());
        assertTrue(dog.isValid());
        assertFalse(dog.isManaged());
    }

    @Test
    public void findAllAsync_throwsOnNonLooperThread() throws Throwable {
        Realm realm = Realm.getInstance(configFactory.createConfiguration());
        try {
            realm.where(AllTypes.class).findAllAsync();
            fail();
        } catch (IllegalStateException ignored) {
        } finally {
            realm.close();
        }
    }

    // finding elements [0-4] asynchronously then wait for the promise to be loaded
    // using a callback to be notified when the data is loaded
    @Test
    @RunTestInLooperThread
    public void findAllAsync_withNotification() throws Throwable {
        Realm realm = looperThread.getRealm();
        populateTestRealm(realm, 10);
        final RealmResults<AllTypes> results = realm.where(AllTypes.class)
                .between("columnLong", 0, 4)
                .findAllAsync();

        results.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertTrue(results.isLoaded());
                assertEquals(5, results.size());
                assertTrue(results.get(4).isValid());
                looperThread.testComplete();
            }
        });
        looperThread.keepStrongReference(results);

        assertFalse(results.isLoaded());
        assertEquals(0, results.size());
    }

    // Transforms an async query into sync by calling load to force
    // the blocking behaviour.
    @Test
    @RunTestInLooperThread
    public void findAllAsync_forceLoad() throws Throwable {
        Realm realm = looperThread.getRealm();
        populateTestRealm(realm, 10);
        final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                .between("columnLong", 0, 4)
                .findAllAsync();

        looperThread.keepStrongReference(realmResults);
        // Notification should be called as well.
        realmResults.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertTrue(realmResults.isLoaded());
                assertEquals(5, realmResults.size());
                looperThread.testComplete();

            }
        });

        assertFalse(realmResults.isLoaded());
        assertEquals(0, realmResults.size());

        boolean successful = realmResults.load();

        assertTrue(successful);
        assertTrue(realmResults.isLoaded());
        assertEquals(5, realmResults.size());
    }

    // **********************************
    // *** 'findFirst' async queries  ***
    // **********************************

    // Similar UC as #testFindAllAsync using 'findFirst'.
    @Test
    @RunTestInLooperThread
    public void findFirstAsync() {
        Realm realm = looperThread.getRealm();
        populateTestRealm(realm, 10);

        final AllTypes asyncObj = realm.where(AllTypes.class).findFirstAsync();
        assertFalse(asyncObj.isValid());
        assertFalse(asyncObj.isLoaded());
        looperThread.keepStrongReference(asyncObj);
        asyncObj.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object) {
                assertTrue(asyncObj.isLoaded());
                assertTrue(asyncObj.isValid());
                looperThread.testComplete();
            }
        });
    }

    // When there is no object match the query condition, findFirstAsync should return with an invalid row.
    @Test
    @RunTestInLooperThread
    public void findFirstAsync_initialEmptyRow() throws Throwable {
        Realm realm = looperThread.getRealm();
        final AllTypes firstAsync = realm.where(AllTypes.class).findFirstAsync();
        looperThread.keepStrongReference(firstAsync);
        firstAsync.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object) {
                assertTrue(firstAsync.isLoaded());
                assertFalse(firstAsync.isValid());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void findFirstAsync_updatedIfSyncRealmObjectIsUpdated() throws Throwable {
        populateTestRealm(looperThread.getRealm(), 1);
        AllTypes firstSync = looperThread.getRealm().where(AllTypes.class).findFirst();
        assertEquals(0, firstSync.getColumnLong());
        assertEquals("test data 0", firstSync.getColumnString());

        final AllTypes firstAsync = looperThread.getRealm().where(AllTypes.class).findFirstAsync();
        assertTrue(firstAsync.load());
        assertTrue(firstAsync.isLoaded());
        assertTrue(firstAsync.isValid());
        assertEquals(0, firstAsync.getColumnLong());
        assertEquals("test data 0", firstAsync.getColumnString());

        looperThread.keepStrongReference(firstAsync);
        firstAsync.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object) {
                assertEquals("Galacticon", firstAsync.getColumnString());
                looperThread.testComplete();
            }
        });

        looperThread.getRealm().beginTransaction();
        firstSync.setColumnString("Galacticon");
        looperThread.getRealm().commitTransaction();
    }

    // Finds elements [0-4] asynchronously then waits for the promise to be loaded
    // using a callback to be notified when the data is loaded.
    @Test
    @RunTestInLooperThread
    public void findFirstAsync_withNotification() throws Throwable {
        Realm realm = looperThread.getRealm();
        populateTestRealm(realm, 10);
        final AllTypes realmResults = realm.where(AllTypes.class)
                .between("columnLong", 4, 9)
                .findFirstAsync();

        looperThread.keepStrongReference(realmResults);
        realmResults.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object) {
                assertTrue(realmResults.isLoaded());
                assertTrue(realmResults.isValid());
                assertEquals("test data 4", realmResults.getColumnString());
                looperThread.testComplete();
            }
        });

        assertFalse(realmResults.isLoaded());
        assertFalse(realmResults.isValid());
        try {
            realmResults.setColumnString("should fail");
            fail("Accessing an unloaded object should throw");
        } catch (IllegalStateException ignored) {
        }
    }

    // load should trigger the listener with empty change set.
    @Test
    @RunTestInLooperThread
    public void findFirstAsync_forceLoad() throws Throwable {
        final AtomicBoolean listenerCalled = new AtomicBoolean(false);
        Realm realm = looperThread.getRealm();
        populateTestRealm(realm, 10);
        final AllTypes realmResults = realm.where(AllTypes.class)
                .between("columnLong", 4, 9)
                .findFirstAsync();

        assertFalse(realmResults.isLoaded());

        realmResults.addChangeListener(new RealmObjectChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel object, ObjectChangeSet changeSet) {
                assertNull(changeSet);
                assertFalse(listenerCalled.get());
                listenerCalled.set(true);
            }
        });

        assertTrue(realmResults.load());
        assertTrue(realmResults.isLoaded());
        assertEquals("test data 4", realmResults.getColumnString());

        assertTrue(listenerCalled.get());
        looperThread.testComplete();
    }

    // For issue https://github.com/realm/realm-java/issues/4495
    @Test
    @RunTestInLooperThread
    public void findFirstAsync_twoListenersOnSameInvalidObjectsCauseNPE() {
        final Realm realm = looperThread.getRealm();
        final AllTypes allTypes = realm.where(AllTypes.class).findFirstAsync();
        final AtomicBoolean firstListenerCalled = new AtomicBoolean(false);
        looperThread.keepStrongReference(allTypes);

        allTypes.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes element) {
                allTypes.removeChangeListener(this);
                assertFalse(firstListenerCalled.getAndSet(true));
                if (!element.isValid()) {
                    realm.beginTransaction();
                    realm.createObject(AllTypes.class);
                    realm.commitTransaction();
                }
            }
        });

        allTypes.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes element) {
                allTypes.removeChangeListener(this);
                assertTrue(firstListenerCalled.get());
                assertFalse(element.isValid());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void findFirstAsync_withSorting() {
        Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        realm.insert(new Dog("Milo"));
        realm.insert(new Dog("Fido"));
        realm.insert(new Dog("Bella"));
        realm.commitTransaction();

        Dog dog = realm.where(Dog.class).sort("name").findFirstAsync();
        dog.addChangeListener((Dog d) -> {
            assertTrue(d.isValid());
            assertEquals("Bella", d.getName());
            looperThread.testComplete();
        });
        looperThread.keepStrongReference(dog);
    }

    // **************************************
    // *** 'findAllSorted' async queries  ***
    // **************************************

    // similar UC as #testFindAllAsync using 'findAllSorted'
    @Test
    @RunTestInLooperThread
    public void sort_async() throws Throwable {
        final Realm realm = looperThread.getRealm();
        populateTestRealm(realm, 10);

        final RealmResults<AllTypes> results = realm.where(AllTypes.class)
                .between("columnLong", 0, 4)
                .sort("columnString", Sort.DESCENDING)
                .findAllAsync();

        assertFalse(results.isLoaded());
        assertEquals(0, results.size());

        looperThread.keepStrongReference(results);
        results.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertTrue(results.isLoaded());
                assertEquals(5, results.size());
                for (int i = 0; i < 5; i++) {
                    int iteration = (4 - i);
                    assertEquals("test data " + iteration, results.get(4 - iteration).getColumnString());
                }
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void combiningAsyncAndSync() {
        populateTestRealm(looperThread.getRealm(), 10);

        final RealmResults<AllTypes> allTypesAsync = looperThread.getRealm().where(AllTypes.class).greaterThan("columnLong", 5).findAllAsync();
        final RealmResults<AllTypes> allTypesSync = allTypesAsync.where().greaterThan("columnLong", 3).findAll();

        // Call where() on an async results will load query. But to maintain the pre version 2.4.0 behaviour of
        // RealmResults.load(), we still treat it as a not loaded results.
        assertEquals(0, allTypesAsync.size());
        assertEquals(4, allTypesSync.size()); // columnLong > 5 && columnLong > 3
        allTypesAsync.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertEquals(4, allTypesAsync.size());
                assertEquals(4, allTypesSync.size());
                looperThread.testComplete();
            }
        });
        looperThread.keepStrongReference(allTypesAsync);
    }

    // Keeps advancing the Realm by sending 1 commit for each frame (16ms).
    // The async queries should keep up with the modification.
    @Test
    @RunTestInLooperThread
    public void stressTestBackgroundCommits() throws Throwable {
        final int NUMBER_OF_COMMITS = 100;
        final CountDownLatch bgRealmClosed = new CountDownLatch(1);
        final long[] latestLongValue = new long[1];
        final float[] latestFloatValue = new float[1];

        // Starts a background thread that pushes a commit every 16ms.
        final Thread backgroundThread = new Thread() {
            @Override
            public void run() {
                Random random = new Random(System.currentTimeMillis());
                Realm backgroundThreadRealm = Realm.getInstance(looperThread.getRealm().getConfiguration());
                for (int i = 0; i < NUMBER_OF_COMMITS; i++) {
                    backgroundThreadRealm.beginTransaction();
                    AllTypes object = backgroundThreadRealm.createObject(AllTypes.class);
                    latestLongValue[0] = random.nextInt(100);
                    latestFloatValue[0] = random.nextFloat();
                    object.setColumnFloat(latestFloatValue[0]);
                    object.setColumnLong(latestLongValue[0]);
                    backgroundThreadRealm.commitTransaction();

                    // Waits 16ms. Before adding the next commit.
                    SystemClock.sleep(16);
                }
                backgroundThreadRealm.close();
                bgRealmClosed.countDown();
            }
        };

        final RealmResults<AllTypes> allAsync = looperThread.getRealm().where(AllTypes.class).findAllAsync();
        allAsync.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertTrue(allAsync.isLoaded());
                if (allAsync.size() == NUMBER_OF_COMMITS) {
                    AllTypes lastInserted = looperThread.getRealm().where(AllTypes.class)
                            .equalTo("columnLong", latestLongValue[0])
                            .equalTo("columnFloat", latestFloatValue[0])
                            .findFirst();
                    assertNotNull(lastInserted);
                    TestHelper.awaitOrFail(bgRealmClosed);
                    looperThread.testComplete();
                }
            }
        });
        looperThread.keepStrongReference(allAsync);

        looperThread.postRunnableDelayed(new Runnable() {
            @Override
            public void run() {
                backgroundThread.start();
            }
        }, 16);
    }

    @Test
    @RunTestInLooperThread
    public void distinct_async() throws Throwable {
        Realm realm = looperThread.getRealm();
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        final RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class).distinct("indexBoolean").findAllAsync();
        final RealmResults<AnnotationIndexTypes> distinctLong = realm.where(AnnotationIndexTypes.class).distinct("indexLong").findAllAsync();
        final RealmResults<AnnotationIndexTypes> distinctDate = realm.where(AnnotationIndexTypes.class).distinct("indexDate").findAllAsync();
        final RealmResults<AnnotationIndexTypes> distinctString = realm.where(AnnotationIndexTypes.class).distinct("indexString").findAllAsync();

        assertFalse(distinctBool.isLoaded());
        assertTrue(distinctBool.isValid());
        assertTrue(distinctBool.isEmpty());

        assertFalse(distinctLong.isLoaded());
        assertTrue(distinctLong.isValid());
        assertTrue(distinctLong.isEmpty());

        assertFalse(distinctDate.isLoaded());
        assertTrue(distinctDate.isValid());
        assertTrue(distinctDate.isEmpty());

        assertFalse(distinctString.isLoaded());
        assertTrue(distinctString.isValid());
        assertTrue(distinctString.isEmpty());

        final Runnable changeListenerDone = new Runnable() {
            final AtomicInteger signalCallbackFinished = new AtomicInteger(4);
            @Override
            public void run() {
                if (signalCallbackFinished.decrementAndGet() == 0) {
                    looperThread.testComplete();
                }
            }
        };

        looperThread.keepStrongReference(distinctBool);
        looperThread.keepStrongReference(distinctLong);
        looperThread.keepStrongReference(distinctDate);
        looperThread.keepStrongReference(distinctString);
        distinctBool.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(2, distinctBool.size());
                changeListenerDone.run();
            }
        });

        distinctLong.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(numberOfBlocks, distinctLong.size());
                changeListenerDone.run();
            }
        });

        distinctDate.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(numberOfBlocks, distinctDate.size());
                changeListenerDone.run();
            }
        });

        distinctString.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(numberOfBlocks, distinctString.size());
                changeListenerDone.run();
            }
        });
    }

    @Test
    @RunTestInLooperThread()
    public void distinct_async_rememberQueryParams() {
        final Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        final int TEST_SIZE = 10;
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(AllJavaTypes.class, i);
        }
        realm.commitTransaction();

        RealmResults<AllJavaTypes> results = realm.where(AllJavaTypes.class)
                .notEqualTo(AllJavaTypes.FIELD_ID, TEST_SIZE / 2)
                .distinct(AllJavaTypes.FIELD_ID)
                .findAllAsync();

        results.addChangeListener(new RealmChangeListener<RealmResults<AllJavaTypes>>() {
            @Override
            public void onChange(RealmResults<AllJavaTypes> results) {
                assertEquals(TEST_SIZE - 1, results.size());
                assertEquals(0, results.where().equalTo(AllJavaTypes.FIELD_ID, TEST_SIZE / 2).count());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync_notIndexedFields() throws Throwable {
        Realm realm = looperThread.getRealm();
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        final RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class)
                .distinct(AnnotationIndexTypes.FIELD_NOT_INDEX_BOOL)
                .findAllAsync();
        final RealmResults<AnnotationIndexTypes> distinctLong = realm.where(AnnotationIndexTypes.class)
                .distinct(AnnotationIndexTypes.FIELD_NOT_INDEX_LONG)
                .findAllAsync();
        final RealmResults<AnnotationIndexTypes> distinctDate = realm.where(AnnotationIndexTypes.class)
                .distinct(AnnotationIndexTypes.FIELD_NOT_INDEX_DATE)
                .findAllAsync();
        final RealmResults<AnnotationIndexTypes> distinctString = realm.where(AnnotationIndexTypes.class)
                .distinct(AnnotationIndexTypes.FIELD_INDEX_STRING)
                .findAllAsync();

        assertFalse(distinctBool.isLoaded());
        assertTrue(distinctBool.isValid());
        assertTrue(distinctBool.isEmpty());

        assertFalse(distinctLong.isLoaded());
        assertTrue(distinctLong.isValid());
        assertTrue(distinctLong.isEmpty());

        assertFalse(distinctDate.isLoaded());
        assertTrue(distinctDate.isValid());
        assertTrue(distinctDate.isEmpty());

        assertFalse(distinctString.isLoaded());
        assertTrue(distinctString.isValid());
        assertTrue(distinctString.isEmpty());

        final Runnable changeListenerDone = new Runnable() {
            final AtomicInteger signalCallbackFinished = new AtomicInteger(4);
            @Override
            public void run() {
                if (signalCallbackFinished.decrementAndGet() == 0) {
                    looperThread.testComplete();
                }
            }
        };

        looperThread.keepStrongReference(distinctBool);
        looperThread.keepStrongReference(distinctLong);
        looperThread.keepStrongReference(distinctDate);
        looperThread.keepStrongReference(distinctString);
        distinctBool.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(2, distinctBool.size());
                changeListenerDone.run();
            }
        });

        distinctLong.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(numberOfBlocks, distinctLong.size());
                changeListenerDone.run();
            }
        });

        distinctDate.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(numberOfBlocks, distinctDate.size());
                changeListenerDone.run();
            }
        });

        distinctString.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                assertEquals(numberOfBlocks, distinctString.size());
                changeListenerDone.run();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync_noneExistingField() throws Throwable {
        Realm realm = looperThread.getRealm();
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        try {
            realm.where(AnnotationIndexTypes.class).distinct("doesNotExist").findAllAsync();
            fail();
        } catch (IllegalArgumentException ignored) {
            looperThread.testComplete();
        }
    }

    @Test
    @RunTestInLooperThread
    public void batchUpdateDifferentTypeOfQueries() {
        final Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        for (int i = 0; i < 5; ) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
            allTypes.setColumnString("data " + i % 3);

            allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
            allTypes.setColumnString("data " + (++i % 3));
        }
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // Must be greater than 1
        realm.commitTransaction();
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AllTypes> findAllAsync = realm.where(AllTypes.class).findAllAsync();
        RealmResults<AllTypes> findAllSorted = realm.where(AllTypes.class).sort("columnString", Sort.ASCENDING).findAllAsync();
        RealmResults<AllTypes> findAllSortedMulti = realm.where(AllTypes.class).sort(new String[]{"columnString", "columnLong"},
                new Sort[]{Sort.ASCENDING, Sort.DESCENDING}).findAllAsync();
        RealmResults<AnnotationIndexTypes> findDistinct = realm.where(AnnotationIndexTypes.class).distinct("indexString").findAllAsync();

        looperThread.keepStrongReference(findAllAsync);
        looperThread.keepStrongReference(findAllSorted);
        looperThread.keepStrongReference(findAllSortedMulti);
        looperThread.keepStrongReference(findDistinct);

        final CountDownLatch queriesCompleted = new CountDownLatch(4);
        final CountDownLatch bgRealmClosedLatch = new CountDownLatch(1);
        final AtomicInteger batchUpdateCompleted = new AtomicInteger(0);
        final AtomicInteger findAllAsyncInvocation = new AtomicInteger(0);
        final AtomicInteger findAllSortedInvocation = new AtomicInteger(0);
        final AtomicInteger findAllSortedMultiInvocation = new AtomicInteger(0);
        final AtomicInteger findDistinctInvocation = new AtomicInteger(0);

        findAllAsync.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                switch (findAllAsyncInvocation.incrementAndGet()) {
                    case 1: {
                        queriesCompleted.countDown();
                        break;
                    }
                    case 2: {
                        if (batchUpdateCompleted.incrementAndGet() == 4) {
                            looperThread.testComplete(bgRealmClosedLatch);
                        }
                        break;
                    }
                }
            }
        });

        findAllSorted.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                switch (findAllSortedInvocation.incrementAndGet()) {
                    case 1: {
                        queriesCompleted.countDown();
                        break;
                    }
                    case 2: {
                        if (batchUpdateCompleted.incrementAndGet() == 4) {
                            looperThread.testComplete(bgRealmClosedLatch);
                        }
                        break;
                    }
                }
            }
        });

        findAllSortedMulti.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                switch (findAllSortedMultiInvocation.incrementAndGet()) {
                    case 1: {
                        queriesCompleted.countDown();
                        break;
                    }
                    case 2: {
                        if (batchUpdateCompleted.incrementAndGet() == 4) {
                            looperThread.testComplete(bgRealmClosedLatch);
                        }
                        break;
                    }
                }
            }
        });

        findDistinct.addChangeListener(new RealmChangeListener<RealmResults<AnnotationIndexTypes>>() {
            @Override
            public void onChange(RealmResults<AnnotationIndexTypes> object) {
                switch (findDistinctInvocation.incrementAndGet()) {
                    case 1: {
                        queriesCompleted.countDown();
                        break;
                    }
                    case 2: {
                        if (batchUpdateCompleted.incrementAndGet() == 4) {
                            looperThread.testComplete(bgRealmClosedLatch);
                        }
                        break;
                    }
                }
            }
        });

        // Waits for the queries to complete then sends a commit from
        // another thread to trigger a batch update of the 4 queries.
        new Thread() {
            @Override
            public void run() {
                TestHelper.awaitOrFail(queriesCompleted);
                Realm bgRealm = Realm.getInstance(realm.getConfiguration());

                bgRealm.beginTransaction();
                bgRealm.createObject(AllTypes.class);
                bgRealm.createObject(AnnotationIndexTypes.class);
                bgRealm.commitTransaction();

                bgRealm.close();
                bgRealmClosedLatch.countDown();
            }
        }.start();
    }

    // This test makes sure that Async queries update when using link.
    @Test
    @RunTestInLooperThread
    public void queryingLinkHandover() throws Throwable {
        final AtomicInteger numberOfInvocations = new AtomicInteger(0);
        final Realm realm = looperThread.getRealm();

        final RealmResults<Dog> allAsync = realm.where(Dog.class).equalTo("owner.name", "kiba").findAllAsync();
        looperThread.keepStrongReference(allAsync);
        allAsync.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> object) {
                switch (numberOfInvocations.incrementAndGet()) {
                    case 1:
                        assertEquals(0, allAsync.size());
                        assertTrue(allAsync.isLoaded());
                        assertTrue(allAsync.isValid());
                        assertTrue(allAsync.isEmpty());
                        new RealmBackgroundTask(realm.getConfiguration()) {
                            @Override
                            public void doInBackground(Realm realm) {
                                realm.beginTransaction();
                                Dog dog = realm.createObject(Dog.class);
                                dog.setAge(10);
                                dog.setName("Akamaru");
                                Owner kiba = realm.createObject(Owner.class);
                                kiba.setName("kiba");
                                dog.setOwner(kiba);
                                realm.commitTransaction();
                            }
                        }.awaitOrFail();
                        break;

                    case 2:
                        assertEquals(1, realm.where(Dog.class).count());
                        assertEquals(1, realm.where(Owner.class).count());
                        assertEquals(1, allAsync.size());
                        assertTrue(allAsync.isLoaded());
                        assertTrue(allAsync.isValid());
                        assertFalse(allAsync.isEmpty());
                        assertEquals(1, allAsync.size());
                        assertEquals("Akamaru", allAsync.get(0).getName());
                        assertEquals("kiba", allAsync.get(0).getOwner().getName());
                        looperThread.testComplete();
                        break;
                }
            }
        });
    }

    // Test case for https://github.com/realm/realm-java/issues/2417
    // Ensures that a UnreachableVersion exception during handover doesn't crash the app or cause a segfault.
    // NOTE: This test is not checking the same thing after the OS results integration. Just keep it for an additional
    // test for async.
    @Test
    @RunTestInLooperThread
    public void badVersion_syncTransaction() throws NoSuchFieldException, IllegalAccessException {
        final AtomicInteger listenerCount = new AtomicInteger(0);
        Realm realm = looperThread.getRealm();

        // 1. Makes sure that async query is not started.
        final RealmResults<AllTypes> result = realm.where(AllTypes.class).sort(AllTypes.FIELD_STRING).findAllAsync();
        looperThread.keepStrongReference(result);
        result.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertTrue(result.isValid());
                assertTrue(result.isLoaded());
                switch (listenerCount.getAndIncrement()) {
                    case 0:
                        // Triggered by beginTransaction
                        assertEquals(0, result.size());
                        break;
                    case 1:
                        // 4. The commit in #2, should result in a refresh being triggered, which means this callback will
                        // be notified once the updated async queries has run.
                        assertEquals(1, result.size());
                        looperThread.testComplete();
                        break;
                    default:
                        fail();
                        break;
                }
            }
        });

        // 2. Advances the caller Realm, invalidating the version in the handover object.
        realm.beginTransaction();
        assertTrue(result.isLoaded());
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        // 3. The async query should now (hopefully) fail with a BadVersion.
        // NOTE: Step 3 is from the original test. After integration of Object Store Results, it has been loaded already
        //       when beginTransaction.
        result.load();
    }

    // This test reproduces the issue in https://secure.helpscout.net/conversation/244053233/6163/?folderId=366141
    // First it creates 512 async queries, then trigger a transaction to make the queries gets update with
    // nativeBatchUpdateQueries. It should not exceed the limits of local ref map size in JNI.
    // NOTE: This test is not checking the same thing after the OS results integration. Just keep it for an additional
    // test for async.
    @Test
    @RunTestInLooperThread
    public void batchUpdate_localRefIsDeletedInLoopOfNativeBatchUpdateQueries() {
        final Realm realm = looperThread.getRealm();
        // For Android, the size of local ref map is 512. Uses 1024 for more pressure.
        final int TEST_COUNT = 1024;
        final AtomicBoolean updatesTriggered = new AtomicBoolean(false);
        // The first time onChange gets called for every results.
        final AtomicInteger firstOnChangeCounter = new AtomicInteger(0);
        // The second time onChange gets called for every results which is triggered by the transaction.
        final AtomicInteger secondOnChangeCounter = new AtomicInteger(0);

        final RealmChangeListener<RealmResults<AllTypes>> listener = new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> element) {
                if (updatesTriggered.get())  {
                    // Step 4: Test finished after all results's onChange gets called the 2nd time.
                    int count  = secondOnChangeCounter.addAndGet(1);
                    if (count == TEST_COUNT) {
                        realm.removeAllChangeListeners();
                        looperThread.testComplete();
                    }
                } else {
                    int count  = firstOnChangeCounter.addAndGet(1);
                    if (count == TEST_COUNT) {
                        // Step 3: Commits the transaction to trigger queries updates.
                        updatesTriggered.set(true);
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.createObject(AllTypes.class);
                            }
                        });
                    } else {
                        // Step 2: Creates 2nd - TEST_COUNT queries.
                        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
                        results.addChangeListener(this);
                        looperThread.keepStrongReference(results);
                    }
                }
            }
        };
        // Step 1. Creates first async to kick the test start.
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
        results.addChangeListener(listener);
        looperThread.keepStrongReference(results);
    }

    // *** Helper methods ***

    private void populateTestRealm(final Realm testRealm, int objects) {
        testRealm.setAutoRefresh(false);
        testRealm.beginTransaction();
        testRealm.deleteAll();
        for (int i = 0; i < objects; ++i) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date());
            allTypes.setColumnDouble(Math.PI);
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
        testRealm.setAutoRefresh(true);
    }

    private void populateForDistinct(Realm realm, long numberOfBlocks, long numberOfObjects, boolean withNull) {
        realm.beginTransaction();
        for (int i = 0; i < numberOfObjects * numberOfBlocks; i++) {
            for (int j = 0; j < numberOfBlocks; j++) {
                AnnotationIndexTypes obj = realm.createObject(AnnotationIndexTypes.class);
                obj.setIndexBoolean(j % 2 == 0);
                obj.setIndexLong(j);
                obj.setIndexDate(withNull ? null : new Date(1000L * j));
                obj.setIndexString(withNull ? null : "Test " + j);
                obj.setNotIndexBoolean(j % 2 == 0);
                obj.setNotIndexLong(j);
                obj.setNotIndexDate(withNull ? null : new Date(1000L * j));
                obj.setNotIndexString(withNull ? null : "Test " + j);
            }
        }
        realm.commitTransaction();
    }
}
