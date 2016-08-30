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

import android.os.Handler;
import android.os.SystemClock;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import dk.ilios.spanner.All;
import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.Owner;
import io.realm.instrumentation.MockActivityManager;
import io.realm.internal.HandlerControllerConstants;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.internal.log.RealmLog;
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


    // ****************************
    // ****  Async transaction  ***
    // ****************************

    // start asynchronously a transaction to insert one element
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync() throws Throwable {
        final Realm realm = looperThread.realm;
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
        final Realm realm = looperThread.realm;
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
    public void executeTransactionAsync_onError() throws Throwable {
        final Realm realm = looperThread.realm;
        assertEquals(0, realm.where(Owner.class).count());

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                throw new RuntimeException("Oh! What a Terrible Failure");
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                assertEquals(0, realm.where(Owner.class).count());
                assertNull(realm.where(Owner.class).findFirst());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_NoCallbacks() throws Throwable {
        final Realm realm = looperThread.realm;
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

    // Test that an async transaction that throws an exception propagate it properly to the user.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_exceptionHandling() throws Throwable {
        final TestHelper.TestLogger testLogger = new TestHelper.TestLogger();
        RealmLog.add(testLogger);

        final Realm realm = looperThread.realm;

        assertEquals(0, realm.where(Owner.class).count());

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Owner owner = realm.createObject(Owner.class);
                owner.setName("Owner");
                realm.cancelTransaction(); // Cancel the transaction then throw
                throw new RuntimeException("Boom");
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
                assertEquals("Could not cancel transaction, not currently in a transaction.", testLogger.message);
                RealmLog.remove(testLogger);
                looperThread.testComplete();
            }
        });
    }

    // Test if the background Realm is closed when transaction success returned.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_realmClosedOnSuccess() {
        final AtomicInteger counter = new AtomicInteger(100);
        final Realm realm = looperThread.realm;
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
                    // Finish testing
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

    // Test if the background Realm is closed when transaction error returned.
    @Test
    @RunTestInLooperThread
    public void executeTransaction_async_realmClosedOnError() {
        final AtomicInteger counter = new AtomicInteger(100);
        final Realm realm = looperThread.realm;
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
                    // Finish testing
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
    // Ensure that onSuccess is called with the correct Realm version for async transaction.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_asyncQuery() {
        final Realm realm = looperThread.realm;
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
                assertEquals(1, results.size());
                looperThread.testComplete();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                fail();
            }
        });
    }

    // ************************************
    // *** promises based async queries ***
    // ************************************

    // finding element [0-4] asynchronously then wait for the promise to be loaded.
    @Test
    @RunTestInLooperThread
    public void findAllAsync() throws Throwable {
        final Realm realm = looperThread.realm;
        populateTestRealm(realm, 10);
        final RealmResults<AllTypes> results = realm.where(AllTypes.class)
                .between("columnLong", 0, 4)
                .findAllAsync();

        assertFalse(results.isLoaded());
        assertEquals(0, results.size());

        looperThread.keepStrongReference.add(results);
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
        Realm realm = looperThread.realm;
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
        } catch (IllegalStateException ignored) {
        } finally {
            realm.close();
        }
    }

    @Test
    @RunTestInLooperThread
    public void findAllAsync_reusingQuery() throws Throwable {
        Realm realm = looperThread.realm;
        populateTestRealm(realm, 10);

        RealmQuery<AllTypes> query = realm.where(AllTypes.class)
                .between("columnLong", 0, 4);
        RealmResults<AllTypes> queryAllSync = query.findAll();
        RealmResults<AllTypes> allAsync = query.findAllAsync();

        assertTrue(allAsync.load());
        assertEquals(allAsync, queryAllSync);

        // the RealmQuery already has an argumentHolder, can't reuse it
        try {
            query.findAllSorted("columnLong");
            fail("Should throw an exception, can not reuse RealmQuery");
        } catch (IllegalStateException ignored) {
            looperThread.testComplete();
        }
    }

    // finding elements [0-4] asynchronously then wait for the promise to be loaded
    // using a callback to be notified when the data is loaded
    @Test
    @RunTestInLooperThread
    public void findAllAsync_withNotification() throws Throwable {
        Realm realm = looperThread.realm;
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

        assertFalse(results.isLoaded());
        assertEquals(0, results.size());
    }

    // transforming an async query into sync by calling load to force
    // the blocking behaviour
    @Test
    @RunTestInLooperThread
    public void findAllAsync_forceLoad() throws Throwable {
        Realm realm = looperThread.realm;
        populateTestRealm(realm, 10);
        final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                .between("columnLong", 0, 4)
                .findAllAsync();

        looperThread.keepStrongReference.add(realmResults);
        // notification should be called as well
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

    // UC:
    //   1- insert 10 objects
    //   2- start an async query to find object [0-4]
    //   3- assert current RealmResults is empty (Worker Thread didn't complete)
    //   4- when the worker thread complete, advance the Realm
    //   5- the caller thread is ahead of the result provided by the worker thread
    //   6- retry automatically the async query
    //   7- the returned RealmResults is now in the same version as the caller thread
    //   8- the notification should be called once (when we retry automatically we shouldn't
    //      notify the user).
    @Test
    @RunTestInLooperThread
    public void findAllAsync_retry() throws Throwable {
        final AtomicInteger numberOfIntercept = new AtomicInteger(0);
        final AtomicInteger numberOfInvocation = new AtomicInteger(0);
        final Realm realm = looperThread.realm;

        // 1. Populate initial data
        realm.setAutoRefresh(false);
        populateTestRealm(realm, 10);
        realm.setAutoRefresh(true);

        // 2. Configure handler interceptor
        final Handler handler = new HandlerProxy(realm.handlerController) {
            @Override
            public boolean onInterceptInMessage(int what) {
                // Intercepts in order: [QueryComplete, RealmChanged, QueryUpdated]
                int intercepts = numberOfIntercept.incrementAndGet();
                switch (what) {
                    // 5. Intercept all messages from other threads. On the first complete, we advance the tread
                    // which will cause the async query to rerun instead of triggering the change listener.
                    case HandlerControllerConstants.COMPLETED_ASYNC_REALM_RESULTS:
                        if (intercepts == 1) {
                            // We advance the Realm so we can simulate a retry
                            realm.beginTransaction();
                            realm.delete(AllTypes.class);
                            realm.commitTransaction();
                        }
                }
                return false;
            }
        };
        realm.setHandler(handler);

        // 3. Create a async query
        final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                .between("columnLong", 0, 4)
                .findAllAsync();

        // 4. Ensure that query isn't loaded yet
        assertFalse(realmResults.isLoaded());
        assertEquals(0, realmResults.size());

        // 6. Callback triggered after retry has completed
        looperThread.keepStrongReference.add(realmResults);
        realmResults.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertEquals(3, numberOfIntercept.get());
                assertEquals(1, numberOfInvocation.incrementAndGet());
                assertTrue(realmResults.isLoaded());
                assertEquals(0, realmResults.size());
                looperThread.testComplete();
            }
        });
    }

    // UC:
    //   1- insert 10 objects
    //   2- start 2 async queries to find all objects [0-9] & objects[0-4]
    //   3- assert both RealmResults are empty (Worker Thread didn't complete)
    //   4- the queries will complete with the same version as the caller thread
    //   5- using a background thread update the Realm
    //   6- now REALM_CHANGED will trigger a COMPLETED_UPDATE_ASYNC_QUERIES that should update all queries
    //   7- callbacks are notified with the latest results (called twice overall)
    @Test
    @RunTestInLooperThread
    public void findAllAsync_batchUpdate() throws Throwable {
        final AtomicInteger numberOfNotificationsQuery1 = new AtomicInteger(0);
        final AtomicInteger numberOfNotificationsQuery2 = new AtomicInteger(0);
        final AtomicInteger numberOfIntercept = new AtomicInteger(0);
        final Realm realm = looperThread.realm;
        populateTestRealm(realm, 10);

        // 1. Configure Handler interceptor
        Handler handler = new HandlerProxy(realm.handlerController) {
            @Override
            public boolean onInterceptInMessage(int what) {
                int intercepts = numberOfIntercept.getAndIncrement();
                if (what == HandlerControllerConstants.COMPLETED_ASYNC_REALM_RESULTS && intercepts == 1) {
                    // 4. The first time the async queries complete we start an update from
                    // another background thread. This will cause queries to rerun when the
                    // background thread notifies this thread.
                    new RealmBackgroundTask(looperThread.realmConfiguration) {
                        @Override
                        public void doInBackground(Realm realm) {
                            realm.beginTransaction();
                            realm.where(AllTypes.class)
                                    .equalTo(AllTypes.FIELD_LONG, 4)
                                    .findFirst()
                                    .setColumnString("modified");
                            realm.createObject(AllTypes.class);
                            realm.createObject(AllTypes.class);
                            realm.commitTransaction();
                        }
                    }.awaitOrFail();
                }
                return false;
            }
        };
        realm.setHandler(handler);

        // 2. Create 2 async queries and check they are not loaded
        final RealmResults<AllTypes> realmResults1 = realm.where(AllTypes.class).findAllAsync();
        final RealmResults<AllTypes> realmResults2 = realm.where(AllTypes.class).between("columnLong", 0, 4).findAllAsync();

        assertFalse(realmResults1.isLoaded());
        assertFalse(realmResults2.isLoaded());
        assertEquals(0, realmResults1.size());
        assertEquals(0, realmResults2.size());

        // 3. Change listeners will be called twice. Once when the first query completely and then
        // when the background thread has completed, notifying this thread to rerun and then receive
        // the updated results.
        final Runnable signalCallbackDone = new Runnable() {
            private AtomicInteger signalCallbackFinished = new AtomicInteger(2);
            @Override
            public void run() {
                if (signalCallbackFinished.decrementAndGet() == 0) {
                    assertEquals(4, numberOfIntercept.get());
                    assertEquals(2, numberOfNotificationsQuery1.get());
                    assertEquals(2, numberOfNotificationsQuery2.get());
                    looperThread.testComplete();
                }
            }
        };

        looperThread.keepStrongReference.add(realmResults1);
        looperThread.keepStrongReference.add(realmResults2);

        realmResults1.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                switch (numberOfNotificationsQuery1.incrementAndGet()) {
                    case 1: // first callback invocation
                        assertTrue(realmResults1.isLoaded());
                        assertEquals(10, realmResults1.size());
                        assertEquals("test data 4", realmResults1.get(4).getColumnString());
                        break;

                    case 2: // second callback
                        assertTrue(realmResults1.isLoaded());
                        assertEquals(12, realmResults1.size());
                        assertEquals("modified", realmResults1.get(4).getColumnString());
                        signalCallbackDone.run();
                        break;
                }
            }
        });


        realmResults2.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                switch (numberOfNotificationsQuery2.incrementAndGet()) {
                    case 1: // first callback invocation
                        assertTrue(realmResults2.isLoaded());
                        assertEquals(5, realmResults2.size());
                        assertEquals("test data 4", realmResults2.get(4).getColumnString());
                        break;

                    case 2: // second callback
                        assertTrue(realmResults2.isLoaded());
                        assertEquals(7, realmResults2.size());
                        assertEquals("modified", realmResults2.get(4).getColumnString());
                        signalCallbackDone.run();
                        break;
                }
            }
        });
    }

    // simulate a use case, when the caller thread advance read, while the background thread
    // is operating on a previous version, this should retry the query on the worker thread
    // to deliver the results once (using the latest version of the Realm)
    @Test
    @RunTestInLooperThread
    public void findAllAsync_callerIsAdvanced() throws Throwable {
        final AtomicInteger numberOfIntercept = new AtomicInteger(0);
        final Realm realm = looperThread.realm;
        populateTestRealm(realm, 10);

        // Configure handler interceptor
        final Handler handler = new HandlerProxy(realm.handlerController) {
            @Override
            public boolean onInterceptInMessage(int what) {
                // Intercepts in order [QueryCompleted, RealmChanged, QueryUpdated]
                int intercepts = numberOfIntercept.incrementAndGet();
                switch (what) {
                    case HandlerControllerConstants.COMPLETED_ASYNC_REALM_RESULTS: {
                        // we advance the Realm so we can simulate a retry
                        if (intercepts == 1) {
                            realm.beginTransaction();
                            realm.createObject(AllTypes.class).setColumnLong(0);
                            realm.commitTransaction();
                        }
                    }
                }
                return false;
            }
        };
        realm.setHandler(handler);

        // Create async query and verify it has not been loaded.
        final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                .between("columnLong", 0, 4)
                .findAllAsync();

        assertFalse(realmResults.isLoaded());
        assertEquals(0, realmResults.size());

        looperThread.keepStrongReference.add(realmResults);

        // Add change listener that should only be called once
        realmResults.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertEquals(3, numberOfIntercept.get());
                assertTrue(realmResults.isLoaded());
                assertEquals(6, realmResults.size());
                looperThread.testComplete();
            }
        });
    }

    // UC:
    //   1- insert 10 objects
    //   2- start 2 async queries to find all objects [0-9] & objects[0-4]
    //   3- assert both RealmResults are empty (Worker Thread didn't complete)
    //   4- start a third thread to insert 2 more elements
    //   5- the third thread signal a REALM_CHANGE that should update all async queries
    //   6- when the results from step [2] completes they should be ignored, since a pending
    //      update (using the latest realm) for all async queries is in progress
    //   7- onChange notification will be triggered once
    @Test
    @RunTestInLooperThread
    public void findAllAsync_callerThreadBehind() throws Throwable {
        final AtomicInteger numberOfCompletedAsyncQuery = new AtomicInteger(0);
        final AtomicInteger numberOfInterceptedChangeMessage = new AtomicInteger(0);
        final AtomicInteger maxNumberOfNotificationsQuery1 = new AtomicInteger(1);
        final AtomicInteger maxNumberOfNotificationsQuery2 = new AtomicInteger(1);
        final Realm realm = looperThread.realm;
        populateTestRealm(realm, 10);

        // Configure Handler Interceptor
        final Handler handler = new HandlerProxy(realm.handlerController) {
            @Override
            public boolean onInterceptInMessage(int what) {
                switch (what) {
                    case HandlerControllerConstants.REALM_CHANGED: {
                        // should only intercept the first REALM_CHANGED coming from the
                        // background update thread

                        // swallow this message, so the caller thread
                        // remain behind the worker thread. This has as
                        // a consequence to ignore the delivered result & wait for the
                        // upcoming REALM_CHANGED to batch update all async queries
                        return numberOfInterceptedChangeMessage.getAndIncrement() == 0;
                    }
                    case HandlerControllerConstants.COMPLETED_ASYNC_REALM_RESULTS: {
                        if (numberOfCompletedAsyncQuery.incrementAndGet() == 2) {
                            // both queries have completed now (& their results should be ignored)
                            // now send the REALM_CHANGED event that should batch update all queries
                            sendEmptyMessage(HandlerControllerConstants.REALM_CHANGED);
                        }
                    }
                }
                return false;
            }
        };
        realm.setHandler(handler);
        Realm.asyncTaskExecutor.pause();

        // Create async queries and check they haven't completed
        final RealmResults<AllTypes> realmResults1 = realm.where(AllTypes.class)
                .findAllAsync();
        final RealmResults<AllTypes> realmResults2 = realm.where(AllTypes.class)
                .between("columnLong", 0, 4).findAllAsync();

        assertFalse(realmResults1.isLoaded());
        assertFalse(realmResults2.isLoaded());
        assertEquals(0, realmResults1.size());
        assertEquals(0, realmResults2.size());

        // advance the Realm from a background thread
        new RealmBackgroundTask(looperThread.realmConfiguration) {
            @Override
            public void doInBackground(Realm realm) {
                realm.beginTransaction();
                realm.where(AllTypes.class).equalTo("columnLong", 4).findFirst().setColumnString("modified");
                realm.createObject(AllTypes.class);
                realm.createObject(AllTypes.class);
                realm.commitTransaction();
            }
        }.awaitOrFail();
        Realm.asyncTaskExecutor.resume();

        // Setup change listeners
        final Runnable signalCallbackDone = new Runnable() {
            private AtomicInteger signalCallbackFinished = new AtomicInteger(2);
            @Override
            public void run() {
                if (signalCallbackFinished.decrementAndGet() == 0) {
                    assertEquals(0, maxNumberOfNotificationsQuery1.get());
                    assertEquals(0, maxNumberOfNotificationsQuery2.get());
                    looperThread.testComplete();
                }
            }
        };

        looperThread.keepStrongReference.add(realmResults1);
        looperThread.keepStrongReference.add(realmResults2);

        realmResults1.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertTrue(maxNumberOfNotificationsQuery1.getAndDecrement() > 0);
                assertTrue(realmResults1.isLoaded());
                assertEquals(12, realmResults1.size());
                assertEquals("modified", realmResults1.get(4).getColumnString());
                signalCallbackDone.run();
            }
        });

        realmResults2.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertTrue(maxNumberOfNotificationsQuery2.getAndDecrement() > 0);
                assertTrue(realmResults2.isLoaded());
                assertEquals(7, realmResults2.size());// the 2 add rows has columnLong == 0
                assertEquals("modified", realmResults2.get(4).getColumnString());
                signalCallbackDone.run();
            }
        });
    }

    // **********************************
    // *** 'findFirst' async queries  ***
    // **********************************

    // similar UC as #testFindAllAsync using 'findFirst'
    @Test
    @RunTestInLooperThread
    public void findFirstAsync() {
        Realm realm = looperThread.realm;
        populateTestRealm(realm, 10);

        final AllTypes asyncObj = realm.where(AllTypes.class).findFirstAsync();
        assertFalse(asyncObj.isValid());
        assertFalse(asyncObj.isLoaded());
        looperThread.keepStrongReference.add(asyncObj);
        asyncObj.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object) {
                assertTrue(asyncObj.isLoaded());
                assertTrue(asyncObj.isValid());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void findFirstAsync_initalEmptyRow() throws Throwable {
        Realm realm = looperThread.realm;
        final AllTypes firstAsync = realm.where(AllTypes.class).findFirstAsync();
        looperThread.keepStrongReference.add(firstAsync);
        firstAsync.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object) {
                assertTrue(firstAsync.load());
                assertTrue(firstAsync.isLoaded());
                assertTrue(firstAsync.isValid());
                assertEquals(0, firstAsync.getColumnLong());
                looperThread.testComplete();
            }
        });
        assertTrue(firstAsync.load());
        assertTrue(firstAsync.isLoaded());
        assertFalse(firstAsync.isValid());

        realm.beginTransaction();
        realm.createObject(AllTypes.class).setColumnLong(0);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void findFirstAsync_updatedIfsyncRealmObjectIsUpdated() throws Throwable {
        populateTestRealm(looperThread.realm, 1);
        AllTypes firstSync = looperThread.realm.where(AllTypes.class).findFirst();
        assertEquals(0, firstSync.getColumnLong());
        assertEquals("test data 0", firstSync.getColumnString());

        final AllTypes firstAsync = looperThread.realm.where(AllTypes.class).findFirstAsync();
        assertTrue(firstAsync.load());
        assertTrue(firstAsync.isLoaded());
        assertTrue(firstAsync.isValid());
        assertEquals(0, firstAsync.getColumnLong());
        assertEquals("test data 0", firstAsync.getColumnString());

        looperThread.keepStrongReference.add(firstAsync);
        firstAsync.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object) {
                assertEquals("Galacticon", firstAsync.getColumnString());
                looperThread.testComplete();
            }
        });

        looperThread.realm.beginTransaction();
        firstSync.setColumnString("Galacticon");
        looperThread.realm.commitTransaction();
    }

    // finding elements [0-4] asynchronously then wait for the promise to be loaded
    // using a callback to be notified when the data is loaded
    @Test
    @RunTestInLooperThread
    public void findFirstAsync_withNotification() throws Throwable {
        Realm realm = looperThread.realm;
        populateTestRealm(realm, 10);
        final AllTypes realmResults = realm.where(AllTypes.class)
                .between("columnLong", 4, 9)
                .findFirstAsync();

        looperThread.keepStrongReference.add(realmResults);
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

    // similar UC as #testForceLoadAsync using 'findFirst'
    @Test
    @RunTestInLooperThread
    public void findFirstAsync_forceLoad() throws Throwable {
        Realm Realm = looperThread.realm;
        populateTestRealm(Realm, 10);
        final AllTypes realmResults = Realm.where(AllTypes.class)
                .between("columnLong", 4, 9)
                .findFirstAsync();

        assertFalse(realmResults.isLoaded());

        assertTrue(realmResults.load());
        assertTrue(realmResults.isLoaded());
        assertEquals("test data 4", realmResults.getColumnString());

        looperThread.testComplete();
    }

    // similar UC as #testFindAllAsyncRetry using 'findFirst'
    // UC:
    //   1- insert 10 objects
    //   2- start an async query to find object [0-4]
    //   3- assert current RealmResults is empty (Worker Thread didn't complete)
    //   4- when the worker thread complete, advance the Realm
    //   5- the caller thread is ahead of the result provided by the worker thread
    //   6- retry automatically the async query
    //   7- the returned RealmResults is now in the same version as the caller thread
    //   8- the notification should be called once (when we retry automatically we shouldn't
    //      notify the user).
    @Test
    @RunTestInLooperThread
    public void findFirstAsync_retry() throws Throwable {
        final AtomicInteger numberOfIntercept = new AtomicInteger(0);
        final Realm realm = looperThread.realm;
        populateTestRealm(realm, 10);

        // Configure interceptor handler
        final Handler handler = new HandlerProxy(realm.handlerController) {
            @Override
            public boolean onInterceptInMessage(int what) {
                int intercepts = numberOfIntercept.incrementAndGet();
                switch (what) {
                    case HandlerControllerConstants.COMPLETED_ASYNC_REALM_OBJECT: {
                        if (intercepts == 1) {
                            // we advance the Realm so we can simulate a retry
                            realm.beginTransaction();
                            realm.delete(AllTypes.class);
                            AllTypes object = realm.createObject(AllTypes.class);
                            object.setColumnString("The Endless River");
                            object.setColumnLong(5);
                            realm.commitTransaction();
                        }
                    }
                }
                return false;
            }
        };
        realm.setHandler(handler);

        // Create a async query and verify it is not still loaded.
        final AllTypes realmResults = realm.where(AllTypes.class)
                .between("columnLong", 4, 6)
                .findFirstAsync();

        assertFalse(realmResults.isLoaded());

        try {
            realmResults.getColumnString();
            fail("Accessing property on an empty row");
        } catch (IllegalStateException ignored) {
        }

        // Add change listener that should only be called once after the retry completed.
        looperThread.keepStrongReference.add(realmResults);
        realmResults.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object) {
                assertEquals(3, numberOfIntercept.get());
                assertTrue(realmResults.isLoaded());
                assertEquals(5, realmResults.getColumnLong());
                assertEquals("The Endless River", realmResults.getColumnString());
                looperThread.testComplete();
            }
        });
    }

    // **************************************
    // *** 'findAllSorted' async queries  ***
    // **************************************

    // similar UC as #testFindAllAsync using 'findAllSorted'
    @Test
    @RunTestInLooperThread
    public void findAllSortedAsync() throws Throwable {
        final Realm realm = looperThread.realm;
        populateTestRealm(realm, 10);

        final RealmResults<AllTypes> results = realm.where(AllTypes.class)
                .between("columnLong", 0, 4)
                .findAllSortedAsync("columnString", Sort.DESCENDING);

        assertFalse(results.isLoaded());
        assertEquals(0, results.size());

        looperThread.keepStrongReference.add(results);
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


    // finding elements [4-8] asynchronously then wait for the promise to be loaded
    // using a callback to be notified when the data is loaded
    @Test
    @RunTestInLooperThread
    public void findAllSortedAsync_retry() throws Throwable {
        final AtomicInteger numberOfIntercept = new AtomicInteger(0);
        final Realm realm = looperThread.realm;

        // 1. Populate the Realm without triggering a RealmChangeEvent.
        realm.setAutoRefresh(false);
        populateTestRealm(realm, 10);
        realm.setAutoRefresh(true);

        // 2. Configure proxy handler to intercept messages
        final Handler handler = new HandlerProxy(realm.handlerController) {
            @Override
            public boolean onInterceptInMessage(int what) {
                // In order [QueryCompleted, RealmChanged, QueryUpdated]
                int intercepts = numberOfIntercept.incrementAndGet();
                switch (what) {
                    case HandlerControllerConstants.COMPLETED_ASYNC_REALM_RESULTS: {
                        if (intercepts == 1) {
                            // We advance the Realm so we can simulate a retry before listeners are
                            // called.
                            realm.beginTransaction();
                            realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 8).findFirst().deleteFromRealm();
                            realm.commitTransaction();
                        }
                        break;
                    }
                }
                return false;
            }
        };
        realm.setHandler(handler);

        // 3. This will add a task to the paused asyncTaskExecutor
        final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                .between("columnLong", 4, 8)
                .findAllSortedAsync("columnString", Sort.ASCENDING);

        assertFalse(realmResults.isLoaded());
        assertEquals(0, realmResults.size());

        // 4. Intercepting the query completed event the first time will
        // cause a commit that should cause the findAllSortedAsync to be re-run.
        // This change listener should only be called with the final result.
        looperThread.keepStrongReference.add(realmResults);
        realmResults.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertEquals(3, numberOfIntercept.get());
                looperThread.testComplete();
            }
        });
    }

    // similar UC as #testFindAllAsyncBatchUpdate using 'findAllSorted'
    // UC:
    //   1- insert 10 objects
    //   2- start 2 async queries to find all objects [0-9] & objects[0-4]
    //   3- assert both RealmResults are empty (Worker Thread didn't complete)
    //   4- the queries will complete with the same version as the caller thread
    //   5- using a background thread update the Realm
    //   6- now REALM_CHANGED will trigger a COMPLETED_UPDATE_ASYNC_QUERIES that should update all queries
    //   7- callbacks are notified with the latest results (called twice overall)
    @Test
    @RunTestInLooperThread
    public void findAllSortedAsync_batchUpdate() {
        final AtomicInteger numberOfNotificationsQuery1 = new AtomicInteger(0);
        final AtomicInteger numberOfNotificationsQuery2 = new AtomicInteger(0);
        final AtomicInteger numberOfIntercept = new AtomicInteger(0);
        Realm realm = looperThread.realm;

        // 1. Add initial 10 objects
        realm.setAutoRefresh(false);
        populateTestRealm(realm, 10);
        realm.setAutoRefresh(true);

        // 2. Configure interceptor
        final Handler handler = new HandlerProxy(realm.handlerController) {
            @Override
            public boolean onInterceptInMessage(int what) {
                switch (what) {
                    case HandlerControllerConstants.COMPLETED_ASYNC_REALM_RESULTS: {
                        if (numberOfIntercept.incrementAndGet() == 1) {
                            // 6. The first time the async queries complete we start an update from
                            // another background thread. This will cause queries to rerun when the
                            // background thread notifies this thread.
                            final CountDownLatch bgThreadLatch = new CountDownLatch(1);
                            new Thread() {
                                @Override
                                public void run() {
                                    Realm bgRealm = Realm.getInstance(looperThread.realmConfiguration);
                                    bgRealm.beginTransaction();
                                    bgRealm.where(AllTypes.class).equalTo("columnLong", 4).findFirst().setColumnString("modified");
                                    bgRealm.createObject(AllTypes.class);
                                    bgRealm.createObject(AllTypes.class);
                                    bgRealm.commitTransaction();
                                    bgRealm.close();
                                    bgThreadLatch.countDown();
                                }
                            }.start();
                            TestHelper.awaitOrFail(bgThreadLatch);
                        }
                    }
                    break;
                }
                return false;
            }
        };
        realm.setHandler(handler);

        // 3. Create 2 async queries
        final RealmResults<AllTypes> realmResults1 = realm.where(AllTypes.class)
                .findAllSortedAsync("columnString", Sort.ASCENDING);
        final RealmResults<AllTypes> realmResults2 = realm.where(AllTypes.class)
                .between("columnLong", 0, 4)
                .findAllSortedAsync("columnString", Sort.DESCENDING);

        // 4. Assert that queries have not finished
        assertFalse(realmResults1.isLoaded());
        assertFalse(realmResults2.isLoaded());
        assertEquals(0, realmResults1.size());
        assertEquals(0, realmResults2.size());

        // 5. Change listeners will be called twice. Once when the first query completely and then
        // when the background thread has completed, notifying this thread to rerun and then receive
        // the updated results.
        final Runnable signalCallbackDone = new Runnable() {
            private AtomicInteger signalCallbackFinished = new AtomicInteger(2);
            @Override
            public void run() {
                if (signalCallbackFinished.decrementAndGet() == 0) {
                    assertEquals(2, numberOfNotificationsQuery1.get());
                    assertEquals(2, numberOfNotificationsQuery2.get());
                    looperThread.testComplete();
                }
            }
        };

        looperThread.keepStrongReference.add(realmResults1);
        looperThread.keepStrongReference.add(realmResults2);

        realmResults1.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                switch (numberOfNotificationsQuery1.incrementAndGet()) {
                    case 1: { // first callback invocation
                        assertTrue(realmResults1.isLoaded());
                        assertEquals(10, realmResults1.size());
                        assertEquals("test data 4", realmResults1.get(4).getColumnString());
                        break;
                    }
                    case 2: { // second callback
                        assertTrue(realmResults1.isLoaded());
                        assertEquals(12, realmResults1.size());
                        assertEquals("modified", realmResults1.get(2).getColumnString());
                        signalCallbackDone.run();
                        break;
                    }
                }
            }
        });

        realmResults2.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                switch (numberOfNotificationsQuery2.incrementAndGet()) {
                    case 1: { // first callback invocation
                        assertTrue(realmResults2.isLoaded());
                        assertEquals(5, realmResults2.size());
                        assertEquals("test data 4", realmResults2.get(0).getColumnString());
                        break;
                    }
                    case 2: { // second callback
                        assertTrue(realmResults2.isLoaded());
                        assertEquals(7, realmResults2.size());
                        assertEquals("modified", realmResults2.get(4).getColumnString());
                        signalCallbackDone.run();
                        break;
                    }
                }
            }
        });
    }

    // similar UC as #testFindAllAsyncBatchUpdate using 'findAllSortedMulti'
    // UC:
    //   1- insert 10 objects
    //   2- start 2 async queries to find all objects [0-9] & objects[0-4]
    //   3- assert both RealmResults are empty (Worker Thread didn't complete)
    //   4- the queries will complete with the same version as the caller thread
    //   5- using a background thread update the Realm
    //   6- now REALM_CHANGED will trigger a COMPLETED_UPDATE_ASYNC_QUERIES that should update all queries
    //   7- callbacks are notified with the latest results (called twice overall)
    @Test
    @RunTestInLooperThread
    public void findAllSortedAsync_multipleFields_batchUpdate() throws Throwable {
        final AtomicInteger numberOfNotificationsQuery1 = new AtomicInteger(0);
        final AtomicInteger numberOfNotificationsQuery2 = new AtomicInteger(0);
        final AtomicInteger numberOfIntercept = new AtomicInteger(0);
        Realm realm = looperThread.realm;

        // 1. Add initial objects
        realm.setAutoRefresh(false);
        realm.beginTransaction();
        for (int i = 0; i < 5; ) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
            allTypes.setColumnString("data " + i % 3);

            allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
            allTypes.setColumnString("data " + (++i % 3));
        }
        realm.commitTransaction();
        realm.setAutoRefresh(true);

        // 2. Configure interceptor
        final Handler handler = new HandlerProxy(realm.handlerController) {
            @Override
            public boolean onInterceptInMessage(int what) {
                int intercepts = numberOfIntercept.incrementAndGet();
                if (what == HandlerControllerConstants.COMPLETED_ASYNC_REALM_RESULTS && intercepts == 1) {
                    // 6. The first time the async queries complete we start an update from
                    // another background thread. This will cause queries to rerun when the
                    // background thread notifies this thread.
                    new RealmBackgroundTask(looperThread.realmConfiguration) {
                        @Override
                        public void doInBackground(Realm realm) {
                            realm.beginTransaction();
                            realm.where(AllTypes.class)
                                    .equalTo("columnString", "data 1")
                                    .equalTo("columnLong", 0)
                                    .findFirst().setColumnDouble(Math.PI);
                            AllTypes allTypes = realm.createObject(AllTypes.class);
                            allTypes.setColumnLong(2);
                            allTypes.setColumnString("data " + 5);

                            allTypes = realm.createObject(AllTypes.class);
                            allTypes.setColumnLong(0);
                            allTypes.setColumnString("data " + 5);
                            realm.commitTransaction();
                        }
                    }.awaitOrFail();
                }
                return false;
            }
        };
        realm.setHandler(handler);

        // 3. Create 2 async queries
        final RealmResults<AllTypes> realmResults1 = realm.where(AllTypes.class)
                .findAllSortedAsync("columnString", Sort.ASCENDING, "columnLong", Sort.DESCENDING);
        final RealmResults<AllTypes> realmResults2 = realm.where(AllTypes.class)
                .between("columnLong", 0, 5)
                .findAllSortedAsync("columnString", Sort.DESCENDING, "columnLong", Sort.ASCENDING);

        // 4. Assert that queries have not finished
        assertFalse(realmResults1.isLoaded());
        assertFalse(realmResults2.isLoaded());
        assertEquals(0, realmResults1.size());
        assertEquals(0, realmResults2.size());
        assertFalse(realmResults1.isLoaded());
        assertFalse(realmResults2.isLoaded());
        assertEquals(0, realmResults1.size());
        assertEquals(0, realmResults2.size());

        // 5. Change listeners will be called twice. Once when the first query completely and then
        // when the background thread has completed, notifying this thread to rerun and then receive
        // the updated results.
        final Runnable signalCallbackDone = new Runnable() {
            private AtomicInteger signalCallbackFinished = new AtomicInteger(2);
            @Override
            public void run() {
                if (signalCallbackFinished.decrementAndGet() == 0) {
                    assertEquals(4, numberOfIntercept.get());
                    assertEquals(2, numberOfNotificationsQuery1.get());
                    assertEquals(2, numberOfNotificationsQuery2.get());
                    looperThread.testComplete();
                }
            }
        };

        looperThread.keepStrongReference.add(realmResults1);
        looperThread.keepStrongReference.add(realmResults2);

        realmResults1.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                switch (numberOfNotificationsQuery1.incrementAndGet()) {
                    case 1: // first callback invocation
                        assertTrue(realmResults1.isLoaded());
                        assertEquals(10, realmResults1.size());

                        assertEquals("data 0", realmResults1.get(0).getColumnString());
                        assertEquals(3, realmResults1.get(0).getColumnLong());
                        assertEquals("data 0", realmResults1.get(1).getColumnString());
                        assertEquals(2, realmResults1.get(1).getColumnLong());
                        assertEquals("data 0", realmResults1.get(2).getColumnString());
                        assertEquals(0, realmResults1.get(2).getColumnLong());

                        assertEquals("data 1", realmResults1.get(3).getColumnString());
                        assertEquals(4, realmResults1.get(3).getColumnLong());
                        assertEquals("data 1", realmResults1.get(4).getColumnString());
                        assertEquals(3, realmResults1.get(4).getColumnLong());
                        assertEquals("data 1", realmResults1.get(5).getColumnString());
                        assertEquals(1, realmResults1.get(5).getColumnLong());
                        assertEquals("data 1", realmResults1.get(6).getColumnString());
                        assertEquals(0, realmResults1.get(6).getColumnLong());

                        assertEquals("data 2", realmResults1.get(7).getColumnString());
                        assertEquals(4, realmResults1.get(7).getColumnLong());
                        assertEquals("data 2", realmResults1.get(8).getColumnString());
                        assertEquals(2, realmResults1.get(8).getColumnLong());
                        assertEquals("data 2", realmResults1.get(9).getColumnString());
                        assertEquals(1, realmResults1.get(9).getColumnLong());
                        break;

                    case 2: // second callback
                        assertTrue(realmResults1.isLoaded());
                        assertEquals(12, realmResults1.size());
                        //first
                        assertEquals("data 0", realmResults1.get(0).getColumnString());
                        assertEquals(3, realmResults1.get(0).getColumnLong());

                        //last
                        assertEquals("data 5", realmResults1.get(11).getColumnString());
                        assertEquals(0, realmResults1.get(11).getColumnLong());

                        signalCallbackDone.run();
                        break;
                }
            }
        });

        realmResults2.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                switch (numberOfNotificationsQuery2.incrementAndGet()) {
                    case 1: // first callback invocation
                        assertTrue(realmResults2.isLoaded());
                        assertEquals(10, realmResults2.size());

                        assertEquals("data 2", realmResults2.get(0).getColumnString());
                        assertEquals(1, realmResults2.get(0).getColumnLong());
                        assertEquals("data 2", realmResults2.get(1).getColumnString());
                        assertEquals(2, realmResults2.get(1).getColumnLong());
                        assertEquals("data 2", realmResults2.get(2).getColumnString());
                        assertEquals(4, realmResults2.get(2).getColumnLong());

                        assertEquals("data 1", realmResults2.get(3).getColumnString());
                        assertEquals(0, realmResults2.get(3).getColumnLong());
                        assertEquals("data 1", realmResults2.get(4).getColumnString());
                        assertEquals(1, realmResults2.get(4).getColumnLong());
                        assertEquals("data 1", realmResults2.get(5).getColumnString());
                        assertEquals(3, realmResults2.get(5).getColumnLong());
                        assertEquals("data 1", realmResults2.get(6).getColumnString());
                        assertEquals(4, realmResults2.get(6).getColumnLong());

                        assertEquals("data 0", realmResults2.get(7).getColumnString());
                        assertEquals(0, realmResults2.get(7).getColumnLong());
                        assertEquals("data 0", realmResults2.get(8).getColumnString());
                        assertEquals(2, realmResults2.get(8).getColumnLong());
                        assertEquals("data 0", realmResults2.get(9).getColumnString());
                        assertEquals(3, realmResults2.get(9).getColumnLong());
                        break;

                    case 2: // second callback
                        assertTrue(realmResults2.isLoaded());
                        assertEquals(12, realmResults2.size());

                        assertEquals("data 5", realmResults2.get(0).getColumnString());
                        assertEquals(0, realmResults2.get(0).getColumnLong());

                        assertEquals("data 0", realmResults2.get(11).getColumnString());
                        assertEquals(3, realmResults2.get(11).getColumnLong());

                        assertEquals("data 1", realmResults2.get(5).getColumnString());
                        assertEquals(Math.PI, realmResults2.get(5).getColumnDouble(), 0.000000000001D);

                        signalCallbackDone.run();
                        break;
                }
            }
        });
    }

    // make sure the notification listener does not leak the enclosing class
    // if unregistered properly.
    @Test
    @RunTestInLooperThread
    public void listenerShouldNotLeak() {
        populateTestRealm(looperThread.realm, 10);

        // simulate the ActivityManager by creating 1 instance responsible
        // of attaching an onChange listener, then simulate a configuration
        // change (ex: screen rotation), this change will create a new instance.
        // we make sure that the GC enqueue the reference of the destroyed instance
        // which indicate no memory leak
        MockActivityManager mockActivityManager =
                MockActivityManager.newInstance(looperThread.realm.getConfiguration());

        mockActivityManager.sendConfigurationChange();

        assertEquals(1, mockActivityManager.numberOfInstances());
        // remove GC'd reference & assert that one instance should remain
        Iterator<Map.Entry<WeakReference<RealmResults<?>>, RealmQuery<?>>> iterator =
                looperThread.realm.handlerController.asyncRealmResults.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WeakReference<RealmResults<?>>, RealmQuery<?>> entry = iterator.next();
            RealmResults<?> weakReference = entry.getKey().get();
            if (weakReference == null) {
                iterator.remove();
            }
        }

        assertEquals(1, looperThread.realm.handlerController.asyncRealmResults.size());
        mockActivityManager.onStop();// to close the Realm
        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void combiningAsyncAndSync() {
        populateTestRealm(looperThread.realm, 10);

        Realm.asyncTaskExecutor.pause();
        final RealmResults<AllTypes> allTypesAsync = looperThread.realm.where(AllTypes.class).greaterThan("columnLong", 5).findAllAsync();
        final RealmResults<AllTypes> allTypesSync = allTypesAsync.where().greaterThan("columnLong", 3).findAll();

        assertEquals(0, allTypesAsync.size());
        assertEquals(6, allTypesSync.size());
        allTypesAsync.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertEquals(4, allTypesAsync.size());
                assertEquals(6, allTypesSync.size());
                looperThread.testComplete();
            }
        });
        Realm.asyncTaskExecutor.resume();
        looperThread.keepStrongReference.add(allTypesAsync);
    }

    // keep advancing the Realm by sending 1 commit for each frame (16ms)
    // the async queries should keep up with the modification
    @Test
    @RunTestInLooperThread
    public void stressTestBackgroundCommits() throws Throwable {
        final int NUMBER_OF_COMMITS = 100;
        final CountDownLatch bgRealmClosed = new CountDownLatch(1);
        final long[] latestLongValue = new long[1];
        final float[] latestFloatValue = new float[1];

        // start a background thread that pushes a commit every 16ms
        final Thread backgroundThread = new Thread() {
            @Override
            public void run() {
                Random random = new Random(System.currentTimeMillis());
                Realm backgroundThreadRealm = Realm.getInstance(looperThread.realm.getConfiguration());
                for (int i = 0; i < NUMBER_OF_COMMITS; i++) {
                    backgroundThreadRealm.beginTransaction();
                    AllTypes object = backgroundThreadRealm.createObject(AllTypes.class);
                    latestLongValue[0] = random.nextInt(100);
                    latestFloatValue[0] = random.nextFloat();
                    object.setColumnFloat(latestFloatValue[0]);
                    object.setColumnLong(latestLongValue[0]);
                    backgroundThreadRealm.commitTransaction();

                    // Wait 16ms. before adding the next commit.
                    SystemClock.sleep(16);
                }
                backgroundThreadRealm.close();
                bgRealmClosed.countDown();
            }
        };

        final RealmResults<AllTypes> allAsync = looperThread.realm.where(AllTypes.class).findAllAsync();
        allAsync.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                assertTrue(allAsync.isLoaded());
                if (allAsync.size() == NUMBER_OF_COMMITS) {
                    AllTypes lastInserted = looperThread.realm.where(AllTypes.class)
                            .equalTo("columnLong", latestLongValue[0])
                            .equalTo("columnFloat", latestFloatValue[0])
                            .findFirst();
                    assertNotNull(lastInserted);
                    TestHelper.awaitOrFail(bgRealmClosed);
                    looperThread.testComplete();
                }
            }
        });
        looperThread.keepStrongReference.add(allAsync);

        looperThread.postRunnableDelayed(new Runnable() {
            @Override
            public void run() {
                backgroundThread.start();
            }
        }, 16);
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync() throws Throwable {
        Realm realm = looperThread.realm;
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        final RealmResults<AnnotationIndexTypes> distinctBool = realm.where(AnnotationIndexTypes.class).distinctAsync("indexBoolean");
        final RealmResults<AnnotationIndexTypes> distinctLong = realm.where(AnnotationIndexTypes.class).distinctAsync("indexLong");
        final RealmResults<AnnotationIndexTypes> distinctDate = realm.where(AnnotationIndexTypes.class).distinctAsync("indexDate");
        final RealmResults<AnnotationIndexTypes> distinctString = realm.where(AnnotationIndexTypes.class).distinctAsync("indexString");

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

        looperThread.keepStrongReference.add(distinctBool);
        looperThread.keepStrongReference.add(distinctLong);
        looperThread.keepStrongReference.add(distinctDate);
        looperThread.keepStrongReference.add(distinctString);
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
    public void distinctAsync_notIndexedFields() throws Throwable {
        Realm realm = looperThread.realm;
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        for (String fieldName : new String[]{"Boolean", "Long", "Date", "String"}) {
            try {
                realm.where(AnnotationIndexTypes.class).distinctAsync("notIndex" + fieldName);
                fail("notIndex" + fieldName);
            } catch (IllegalArgumentException ignored) {
            }
        }

        looperThread.testComplete();
    }

    @Test
    @RunTestInLooperThread
    public void distinctAsync_noneExistingField() throws Throwable {
        Realm realm = looperThread.realm;
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        try {
            realm.where(AnnotationIndexTypes.class).distinctAsync("doesNotExist");
            fail();
        } catch (IllegalArgumentException ignored) {
            looperThread.testComplete();
        }
    }

    @Test
    @RunTestInLooperThread
    public void batchUpdateDifferentTypeOfQueries() {
        final Realm realm = looperThread.realm;
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
        final long numberOfObjects = 10; // must be greater than 1
        realm.commitTransaction();
        populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AllTypes> findAllAsync = realm.where(AllTypes.class).findAllAsync();
        RealmResults<AllTypes> findAllSorted = realm.where(AllTypes.class).findAllSortedAsync("columnString", Sort.ASCENDING);
        RealmResults<AllTypes> findAllSortedMulti = realm.where(AllTypes.class).findAllSortedAsync(new String[]{"columnString", "columnLong"},
                new Sort[]{Sort.ASCENDING, Sort.DESCENDING});
        RealmResults<AnnotationIndexTypes> findDistinct = realm.where(AnnotationIndexTypes.class).distinctAsync("indexString");

        looperThread.keepStrongReference.add(findAllAsync);
        looperThread.keepStrongReference.add(findAllSorted);
        looperThread.keepStrongReference.add(findAllSortedMulti);
        looperThread.keepStrongReference.add(findDistinct);

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

        // wait for the queries to completes then send a commit from
        // another thread to trigger a batch update of the 4 queries
        new Thread() {
            @Override
            public void run() {
                try {
                    queriesCompleted.await();
                    Realm bgRealm = Realm.getInstance(realm.getConfiguration());

                    bgRealm.beginTransaction();
                    bgRealm.createObject(AllTypes.class);
                    bgRealm.createObject(AnnotationIndexTypes.class);
                    bgRealm.commitTransaction();

                    bgRealm.close();
                    bgRealmClosedLatch.countDown();
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }
            }
        }.start();
    }

    // this test make sure that Async queries update when using link
    @Test
    @RunTestInLooperThread
    public void queryingLinkHandover() throws Throwable {
        final AtomicInteger numberOfInvocations = new AtomicInteger(0);
        final Realm realm = looperThread.realm;

        final RealmResults<Dog> allAsync = realm.where(Dog.class).equalTo("owner.name", "kiba").findAllAsync();
        looperThread.keepStrongReference.add(allAsync);
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

    // Make sure we don't get the run into the IllegalStateException
    // (Caller thread behind the worker thread)
    // Scenario:
    // - Caller thread is in version 1, start an asyncFindFirst
    // - Another thread advance the Realm, now the latest version = 2
    // - The worker thread should query against version 1 not version 2
    // otherwise the caller thread wouldn't be able to import the result
    // - The notification mechanism will guarantee that the REALM_CHANGE triggered by
    // the background thread, will update the caller thread (advancing it to version 2)
    @Test
    @RunTestInLooperThread
    public void testFindFirstUsesCallerThreadVersion() throws Throwable {
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);

        populateTestRealm(looperThread.realm, 10);
        Realm.asyncTaskExecutor.pause();

        final AllTypes firstAsync = looperThread.realm.where(AllTypes.class).findFirstAsync();
        looperThread.keepStrongReference.add(firstAsync);
        firstAsync.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object) {
                assertNotNull(firstAsync);
                assertEquals("test data 0", firstAsync.getColumnString());
                looperThread.testComplete(signalClosedRealm);
            }
        });

        // advance the background Realm
        new Thread() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(looperThread.realmConfiguration);
                // Advancing the Realm without generating notifications
                bgRealm.sharedRealm.beginTransaction();
                bgRealm.sharedRealm.commitTransaction();
                Realm.asyncTaskExecutor.resume();
                bgRealm.close();
                signalClosedRealm.countDown();
            }
        }.start();
    }

    // Test case for https://github.com/realm/realm-java/issues/2417
    // Ensure that a UnreachableVersion exception during handover doesn't crash the app or cause a segfault.
    @Test
    @UiThreadTest
    public void badVersion_findAll() throws NoSuchFieldException, IllegalAccessException {
        TestHelper.replaceRealmThreadExecutor(RealmThreadPoolExecutor.newSingleThreadExecutor());
        RealmConfiguration config = configFactory.createConfiguration();
        Realm realm = Realm.getInstance(config);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        boolean result = realm.where(AllTypes.class).findAllAsync().load();
        try {
            assertFalse(result);
        } finally {
            realm.close();
        }
        TestHelper.resetRealmThreadExecutor();
    }

    // Test case for https://github.com/realm/realm-java/issues/2417
    // Ensure that a UnreachableVersion exception during handover doesn't crash the app or cause a segfault.
    @Test
    @UiThreadTest
    public void badVersion_findAllSortedAsync() throws NoSuchFieldException, IllegalAccessException {
        TestHelper.replaceRealmThreadExecutor(RealmThreadPoolExecutor.newSingleThreadExecutor());
        RealmConfiguration config = configFactory.createConfiguration();
        Realm realm = Realm.getInstance(config);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.where(AllTypes.class)
                .findAllSortedAsync(AllTypes.FIELD_STRING, Sort.ASCENDING, AllTypes.FIELD_LONG, Sort.DESCENDING)
                .load();
        realm.close();
        TestHelper.resetRealmThreadExecutor();
    }

    // Test case for https://github.com/realm/realm-java/issues/2417
    // Ensure that a UnreachableVersion exception during handover doesn't crash the app or cause a segfault.
    @Test
    @UiThreadTest
    public void badVersion_distinct() throws NoSuchFieldException, IllegalAccessException {
        TestHelper.replaceRealmThreadExecutor(RealmThreadPoolExecutor.newSingleThreadExecutor());
        RealmConfiguration config = configFactory.createConfiguration();
        Realm realm = Realm.getInstance(config);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
        realm.where(AllJavaTypes.class)
                .distinctAsync(AllJavaTypes.FIELD_STRING)
                .load();

        realm.close();
        TestHelper.resetRealmThreadExecutor();
    }

    // Test case for https://github.com/realm/realm-java/issues/2417
    // Ensure that a UnreachableVersion exception during handover doesn't crash the app or cause a segfault.
    @Test
    @RunTestInLooperThread
    public void badVersion_syncTransaction() throws NoSuchFieldException, IllegalAccessException {
        TestHelper.replaceRealmThreadExecutor(RealmThreadPoolExecutor.newSingleThreadExecutor());
        Realm realm = looperThread.realm;

        // 1. Make sure that async query is not started
        final RealmResults<AllTypes> result = realm.where(AllTypes.class).findAllSortedAsync(AllTypes.FIELD_STRING);
        looperThread.keepStrongReference.add(result);
        result.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                // 4. The commit in #2, should result in a refresh being triggered, which means this callback will
                // be notified once the updated async queries has run.
                // with the correct
                assertTrue(result.isValid());
                assertTrue(result.isLoaded());
                assertEquals(1, result.size());
                looperThread.testComplete();
            }
        });

        // 2. Advance the calle Realm, invalidating the version in the handover object
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        // 3. The async query should now (hopefully) fail with a BadVersion
        result.load();
        TestHelper.resetRealmThreadExecutor();
    }

    // handlerController#emptyAsyncRealmObject is accessed from different threads
    // make sure that we iterate over it safely without any race condition (ConcurrentModification)
    @Test
    @UiThreadTest
    public void concurrentModificationEmptyAsyncRealmObject() {
        RealmConfiguration config  = configFactory.createConfiguration();
        final Realm realm = Realm.getInstance(config);
        Dog dog1 = new Dog();
        dog1.setName("Dog 1");

        Dog dog2 = new Dog();
        dog2.setName("Dog 2");

        realm.beginTransaction();
        dog1 = realm.copyToRealm(dog1);
        dog2 = realm.copyToRealm(dog2);
        realm.commitTransaction();

        final WeakReference<RealmObjectProxy> weakReference1 = new WeakReference<RealmObjectProxy>((RealmObjectProxy)dog1);
        final WeakReference<RealmObjectProxy> weakReference2 = new WeakReference<RealmObjectProxy>((RealmObjectProxy)dog2);

        final RealmQuery<Dog> dummyQuery = RealmQuery.createQuery(realm, Dog.class);
        // Initialize the emptyAsyncRealmObject map, to make sure that iterating is safe
        // even if we modify the map from a background thread (in case of an empty findFirstAsync)
        realm.handlerController.emptyAsyncRealmObject.put(weakReference1, dummyQuery);

        final CountDownLatch dogAddFromBg = new CountDownLatch(1);
        Iterator<Map.Entry<WeakReference<RealmObjectProxy>, RealmQuery<? extends RealmModel>>> iterator = realm.handlerController.emptyAsyncRealmObject.entrySet().iterator();
        AtomicBoolean fireOnce = new AtomicBoolean(true);
        while (iterator.hasNext()) {
            Dog next = (Dog) iterator.next().getKey().get();
            // add a new Dog from a background thread
            if (fireOnce.compareAndSet(true, false)) {
                new Thread() {
                    @Override
                    public void run() {
                        // add a WeakReference to simulate an empty row using a findFirstAsync
                        // this is added on an Executor thread, hence the dedicated thread
                        realm.handlerController.emptyAsyncRealmObject.put(weakReference2, dummyQuery);
                        dogAddFromBg.countDown();
                    }
                }.start();
                TestHelper.awaitOrFail(dogAddFromBg);
            }
            assertEquals("Dog 1", next.getName());
            assertFalse(iterator.hasNext());
        }
        realm.close();
    }

    // handlerController#realmObjects is accessed from different threads
    // make sure that we iterate over it safely without any race condition (ConcurrentModification)
    @Test
    @UiThreadTest
    public void concurrentModificationRealmObjects() {
        RealmConfiguration config  = configFactory.createConfiguration();
        final Realm realm = Realm.getInstance(config);
        Dog dog1 = new Dog();
        dog1.setName("Dog 1");

        Dog dog2 = new Dog();
        dog2.setName("Dog 2");

        realm.beginTransaction();
        dog1 = realm.copyToRealm(dog1);
        dog2 = realm.copyToRealm(dog2);
        realm.commitTransaction();

        final WeakReference<RealmObjectProxy> weakReference1 = new WeakReference<RealmObjectProxy>((RealmObjectProxy)dog1);
        final WeakReference<RealmObjectProxy> weakReference2 = new WeakReference<RealmObjectProxy>((RealmObjectProxy)dog2);

        realm.handlerController.realmObjects.put(weakReference1, Boolean.TRUE);

        final CountDownLatch dogAddFromBg = new CountDownLatch(1);
        Iterator<Map.Entry<WeakReference<RealmObjectProxy>, Object>> iterator = realm.handlerController.realmObjects.entrySet().iterator();
        AtomicBoolean fireOnce = new AtomicBoolean(true);
        while (iterator.hasNext()) {
            Dog next = (Dog) iterator.next().getKey().get();
            // add a new Dog from a background thread
            if (fireOnce.compareAndSet(true, false)) {
                new Thread() {
                    @Override
                    public void run() {
                        realm.handlerController.realmObjects.put(weakReference2, Boolean.TRUE);
                        dogAddFromBg.countDown();
                    }
                }.start();
                TestHelper.awaitOrFail(dogAddFromBg);
            }
            assertEquals("Dog 1", next.getName());
            assertFalse(iterator.hasNext());
        }

        realm.close();
    }

    // This test reproduce the issue in https://secure.helpscout.net/conversation/244053233/6163/?folderId=366141
    // First it creates 512 async queries, then trigger a transaction to make the queries gets update with
    // nativeBatchUpdateQueries. It should not exceed the limits of local ref map size in JNI.
    @Test
    @RunTestInLooperThread
    public void batchUpdate_localRefIsDeletedInLoopOfNativeBatchUpdateQueries() {
        final Realm realm = looperThread.realm;
        // For Android, the size of local ref map is 512. Use 1024 for more pressure.
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
                        // Step 3: Commit the transaction to trigger queries updates.
                        updatesTriggered.set(true);
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.createObject(AllTypes.class);
                            }
                        });
                    } else {
                        // Step 2: Create 2nd - TEST_COUNT queries.
                        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
                        results.addChangeListener(this);
                        looperThread.keepStrongReference.add(results);
                    }
                }
            }
        };
        // Step 1. Create first async to kick the test start.
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
        results.addChangeListener(listener);
        looperThread.keepStrongReference.add(results);
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
            allTypes.setColumnDouble(3.1415);
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
}
