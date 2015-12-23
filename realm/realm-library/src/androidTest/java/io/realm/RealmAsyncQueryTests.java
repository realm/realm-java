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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationIndexTypes;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.Owner;
import io.realm.instrumentation.MockActivityManager;
import io.realm.internal.log.RealmLog;
import io.realm.proxy.HandlerProxy;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmAsyncQueryTests {
    private Context context;

    @Rule
    public final RunInLooperThread workerThread = new RunInLooperThread();

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getContext();
    }

    // ****************************
    // ****  Async transaction  ***
    // ****************************

    // start asynchronously a transaction to insert one element
    @Test
    @RunTestInLooperThread
    public void testAsyncTransaction() throws Throwable {
        assertEquals(0, workerThread.realm.allObjects(Owner.class).size());

        workerThread.realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Owner owner = realm.createObject(Owner.class);
                owner.setName("Owner");
            }
        }, new Realm.Transaction.Callback() {
            @Override
            public void onSuccess() {
                assertEquals(1, workerThread.realm.allObjects(Owner.class).size());
                assertEquals("Owner", workerThread.realm.where(Owner.class).findFirst().getName());
                workerThread.signalTestCompleted.countDown();
            }

            @Override
            public void onError(Exception e) {
                workerThread.signalTestCompleted.countDown();
                fail(e.getMessage());
            }
        });
    }

    @Test
    public void testAsyncTransactionThatThrowsRuntimeException() throws Throwable {
        final TestHelper.TestLogger testLogger = new TestHelper.TestLogger();

        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Realm[] realm = new Realm[1];
        final Throwable[] threadAssertionError = new Throwable[1];// to catch both Exception & AssertionError
        final Looper[] backgroundLooper = new Looper[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();

                try {
                    RealmLog.add(testLogger);
                    realm[0] = openRealmInstance("testAsyncTransaction");

                    assertEquals(0, realm[0].allObjects(Owner.class).size());

                    realm[0].executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Owner owner = realm.createObject(Owner.class);
                            owner.setName("Owner");
                            realm.cancelTransaction(); // Cancel the transaction then throw
                            throw new RuntimeException("Boom");
                        }
                    }, new Realm.Transaction.Callback() {
                        @Override
                        public void onSuccess() {
                            try {
                                fail("Should not reach success if runtime exception is thrown in callback.");
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            try {
                                // Ensure we are giving developers quality messages in the logs.
                                assertEquals(testLogger.message, "Could not cancel transaction, not currently in a transaction.");
                            } catch (AssertionFailedError afe) {
                                threadAssertionError[0] = afe;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    threadAssertionError[0] = e;

                } finally {
                    RealmLog.remove(testLogger);
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm.length > 0 && realm[0] != null) {
                        realm[0].close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }

    // Test if the background Realm is closed when transaction success returned.
    @Test
    public void testClosedBeforeAsyncTransactionSuccess() {
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        HandlerThread handlerThread = new HandlerThread("background");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final AtomicInteger counter = new AtomicInteger(100);
                final Realm realm = openRealmInstance("testClosedBeforeAsyncTransactionSuccess");
                final RealmCache.Callback cacheCallback = new RealmCache.Callback() {
                    @Override
                    public void onResult(int count) {
                        assertEquals(1, count);
                        if (counter.decrementAndGet() == 0) {
                            realm.close();
                            signalTestFinished.countDown();
                        }
                    }
                };
                final Realm.Transaction.Callback transactionCallback = new Realm.Transaction.Callback() {
                    @Override
                    public void onSuccess() {
                        RealmCache.invokeWithGlobalRefCount(realm.getConfiguration(), cacheCallback);
                        if (counter.get() == 0) {
                            // Finish testing
                            return;
                        }
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                            }
                        }, this);
                    }
                };

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                    }
                }, transactionCallback);
            }
        });
        try {
            TestHelper.awaitOrFail(signalTestFinished);
        } finally {
            handlerThread.quit();
        }
    }

    // Test if the background Realm is closed when transaction error returned.
    @Test
    public void testClosedBeforeAsyncTransactionError() {
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
        HandlerThread handlerThread = new HandlerThread("background");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final AtomicInteger counter = new AtomicInteger(100);
                final Realm realm = openRealmInstance("testClosedBeforeAsyncTransactionSuccess");
                final RealmCache.Callback cacheCallback = new RealmCache.Callback() {
                    @Override
                    public void onResult(int count) {
                        assertEquals(1, count);
                        if (counter.decrementAndGet() == 0) {
                            realm.close();
                            signalTestFinished.countDown();
                        }
                    }
                };
                final Realm.Transaction.Callback transactionCallback = new Realm.Transaction.Callback() {
                    @Override
                    public void onError(Exception e) {
                        RealmCache.invokeWithGlobalRefCount(realm.getConfiguration(), cacheCallback);
                        if (counter.get() == 0) {
                            // Finish testing
                            return;
                        }
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                throw new RuntimeException("Dummy exception");
                            }
                        }, this);
                    }
                };

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        throw new RuntimeException("Dummy exception");
                    }
                }, transactionCallback);
            }
        });
        try {
            TestHelper.awaitOrFail(signalTestFinished);
        } finally {
            handlerThread.quit();
        }
    }

    // ************************************
    // *** promises based async queries ***
    // ************************************

    // finding element [0-4] asynchronously then wait for the promise to be loaded.
    // no use of notification callback
    @Test
    public void testFindAllAsync() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final RealmResults[] results = new RealmResults[1];
        final Looper[] backgroundLooper = new Looper[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();

                // register IdleHandler to quit the Looper once all messages have proceeded
                // Let the first queueIdle invocation pass, because it occurs before the first message is received.
                // WARNING: when debugging the 'queueIdle' will be called more often (because of the break points)
                //          making the countdown latch to be invoked earlier.
                final boolean[] isFirstIdle = {true};
                Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                    @Override
                    public boolean queueIdle() {
                        if (isFirstIdle[0]) {
                            isFirstIdle[0] = false;
                            return true;

                        } else {
                            // Last message (i.e COMPLETED_ASYNC_REALM_RESULTS was processed)
                            try {
                                assertTrue(results[0].isLoaded());
                                assertEquals(5, results[0].size());
                                assertTrue(results[0].get(0).isValid());
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                            return false; // unregister from the future IdleHandler events
                        }
                    }
                });
                Realm realm = null;
                try {
                    realm = openRealmInstance("testFindAllAsync");
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                            .between("columnLong", 0, 4)
                            .findAllAsync();
                    results[0] = realmResults;
                    assertFalse(realmResults.isLoaded());
                    assertEquals(0, realmResults.size());

                    Realm.asyncQueryExecutor.resume();

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }

    @Test
    @RunTestInLooperThread
    public void testAccessingRealmListOnUnloadedRealmObjectShouldThrow() {
        Realm.asyncQueryExecutor.pause();

        populateTestRealm(workerThread.realm, 10);
        final AllTypes alltypes1 = workerThread.realm.where(AllTypes.class)
                .equalTo("columnLong", 0)
                .findFirstAsync();

        assertFalse(alltypes1.isLoaded());
        try {
            alltypes1.getColumnRealmList();
            fail("Accessing property on an empty row");
        } catch (IllegalStateException ignored) {
        }

        Realm.asyncQueryExecutor.resume();
        workerThread.signalTestCompleted.countDown();
        Looper.loop();

    }

    @Test
    public void testStandaloneObjectAsyncBehaviour() {
        Dog dog = new Dog();
        dog.setName("Akamaru");
        dog.setAge(10);

        assertTrue(dog.isLoaded());
        assertFalse(dog.isValid());
    }

    @Test
    public void testAsyncQueryOnNonLooperThreadShouldThrow() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Realm realm = null;
                try {
                    realm = openRealmInstance("testAsyncQueryOnNonLooperThreadShouldThrow");
                    populateTestRealm(realm, 10);

                    try {
                        final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                                .between("columnLong", 0, 4)
                                .findAllAsync();
                        fail("Should not be able to use async query without a Looper thread");
                    } catch (IllegalStateException ignore) {
                        signalCallbackFinished.countDown();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                }
            }
        });

        // wait until the callback of our async query proceed
        TestHelper.awaitOrFail(signalCallbackFinished);

        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    @Test
    @RunTestInLooperThread
    public void testReusingQuery() throws Throwable {
        populateTestRealm(workerThread.realm, 10);

        RealmQuery<AllTypes> query = workerThread.realm.where(AllTypes.class)
                .between("columnLong", 0, 4);
        RealmResults<AllTypes> queryAllSync = query.findAll();
        RealmResults<AllTypes> allAsync = query.findAllAsync();

        assertTrue(allAsync.load());
        assertEquals(allAsync, queryAllSync);

        // the RealmQuery already has an argumentHolder, can't reuse it
        try {
            RealmResults<AllTypes> allAsyncSorted = query.findAllSorted("columnLong");
            fail("Should throw an exception, can not reuse RealmQuery");
        } catch (IllegalStateException ignored) {
            workerThread.signalTestCompleted.countDown();
        }
    }

    // finding elements [0-4] asynchronously then wait for the promise to be loaded
    // using a callback to be notified when the data is loaded
    @Test
    @RunTestInLooperThread
    public void testFindAllAsyncWithNotification() throws Throwable {
        Realm.asyncQueryExecutor.pause();

        populateTestRealm(workerThread.realm, 10);
        final RealmResults<AllTypes> realmResults = workerThread.realm.where(AllTypes.class)
                .between("columnLong", 0, 4)
                .findAllAsync();

        realmResults.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertTrue(realmResults.isLoaded());
                assertEquals(5, realmResults.size());
                assertTrue(realmResults.get(4).isValid());
                workerThread.signalTestCompleted.countDown();
            }
        });

        assertFalse(realmResults.isLoaded());
        assertEquals(0, realmResults.size());

        Realm.asyncQueryExecutor.resume();
    }

    // transforming an async query into sync by calling load to force
    // the blocking behaviour
    @Test
    @RunTestInLooperThread
    public void testForceLoadAsync() throws Throwable {
        Realm.asyncQueryExecutor.pause();

        populateTestRealm(workerThread.realm, 10);
        final RealmResults<AllTypes> realmResults = workerThread.realm.where(AllTypes.class)
                .between("columnLong", 0, 4)
                .findAllAsync();

        // notification should be called as well
        realmResults.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertTrue(realmResults.isLoaded());
                assertEquals(5, realmResults.size());
                workerThread.signalTestCompleted.countDown();

            }
        });

        assertFalse(realmResults.isLoaded());
        assertEquals(0, realmResults.size());

        Realm.asyncQueryExecutor.resume();
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
    public void testFindAllAsyncRetry() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();
                final Realm[] realm = new Realm[1];
                try {
                    final AtomicInteger numberOfIntercept = new AtomicInteger(1);
                    realm[0] = openRealmInstance("testFindAllAsyncRetry");
                    final Handler handler = new HandlerProxy(realm[0].handler) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case HandlerController.COMPLETED_ASYNC_REALM_RESULTS: {
                                    if (numberOfIntercept.getAndDecrement() > 0) {
                                        // We advance the Realm so we can simulate a retry
                                        // This is intercepted on the worker thread, we need to use
                                        // the Realm on the original thread
                                        postAtFront(new Runnable() {
                                            @Override
                                            public void run() {
                                                realm[0].beginTransaction();
                                                realm[0].clear(AllTypes.class);
                                                realm[0].commitTransaction();
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                            return false;
                        }
                    };
                    realm[0].setHandler(handler);

                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm[0], 10);
                    final RealmResults<AllTypes> realmResults = realm[0].where(AllTypes.class)
                            .between("columnLong", 0, 4)
                            .findAllAsync();

                    assertFalse(realmResults.isLoaded());
                    assertEquals(0, realmResults.size());

                    Realm.asyncQueryExecutor.resume();

                    final AtomicInteger numberOfInvocation = new AtomicInteger(0);
                    realmResults.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                assertEquals(1, numberOfInvocation.incrementAndGet());
                                assertTrue(realmResults.isLoaded());
                                assertEquals(0, realmResults.size());
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm[0] != null) {
                        realm[0].close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
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
    public void testFindAllAsyncBatchUpdate() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(2);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final AtomicInteger numberOfNotificationsQuery1 = new AtomicInteger(0);
        final AtomicInteger numberOfNotificationsQuery2 = new AtomicInteger(0);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("testFindAllAsyncBatchUpdate");
                    final RealmConfiguration realmConfiguration = realm.getConfiguration();
                    final AtomicInteger numberOfIntercept = new AtomicInteger(1);
                    assertNotNull(realm.handler);
                    final Handler handler = new HandlerProxy(realm.handler) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case HandlerController.COMPLETED_ASYNC_REALM_RESULTS: {
                                    if (numberOfIntercept.getAndDecrement() > 0) {
                                        // We advance the Realm so we can simulate a retry
                                        // This is intercepted on the worker thread, we need to use
                                        // the Realm on the caller thread
                                        postAtFront(new Runnable() {
                                            @Override
                                            public void run() {
                                                final CountDownLatch bgThreadLatch = new CountDownLatch(1);
                                                new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Realm bgRealm = Realm.getInstance(realmConfiguration);
                                                        bgRealm.beginTransaction();
                                                        bgRealm.where(AllTypes.class).equalTo("columnLong", 4).findFirst().setColumnString("modified");
                                                        bgRealm.createObject(AllTypes.class);
                                                        bgRealm.createObject(AllTypes.class);
                                                        bgRealm.commitTransaction();
                                                        bgRealm.close();
                                                        bgThreadLatch.countDown();
                                                    }
                                                }.start();
                                                try {
                                                    bgThreadLatch.await();
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                    threadAssertionError[0] = e;
                                                }
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                            return false;
                        }
                    };
                    realm.setHandler(handler);
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final RealmResults<AllTypes> realmResults1 = realm.where(AllTypes.class)
                            .findAllAsync();
                    final RealmResults<AllTypes> realmResults2 = realm.where(AllTypes.class)
                            .between("columnLong", 0, 4).findAllAsync();

                    assertFalse(realmResults1.isLoaded());
                    assertFalse(realmResults2.isLoaded());
                    assertEquals(0, realmResults1.size());
                    assertEquals(0, realmResults2.size());

                    Realm.asyncQueryExecutor.resume();

                    realmResults1.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
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
                                        assertEquals("modified", realmResults1.get(4).getColumnString());
                                        signalCallbackFinished.countDown();
                                        break;
                                    }
                                    default: {
                                        throw new AssertionFailedError("Callback called more than twice");
                                    }
                                }
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    realmResults2.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                switch (numberOfNotificationsQuery2.incrementAndGet()) {
                                    case 1: { // first callback invocation
                                        assertTrue(realmResults2.isLoaded());
                                        assertEquals(5, realmResults2.size());
                                        assertEquals("test data 4", realmResults2.get(4).getColumnString());
                                        break;
                                    }
                                    case 2: { // second callback
                                        assertTrue(realmResults2.isLoaded());
                                        assertEquals(7, realmResults2.size());
                                        assertEquals("modified", realmResults2.get(4).getColumnString());
                                        signalCallbackFinished.countDown();
                                        break;
                                    }
                                    default: {
                                        throw new AssertionFailedError("Callback called more than twice");
                                    }
                                }
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);

        assertEquals(2, numberOfNotificationsQuery1.get());
        assertEquals(2, numberOfNotificationsQuery2.get());
    }

    // simulate a use case, when the caller thread advance read, while the background thread
    // is operating on a previous version, this should retry the query on the worker thread
    // to deliver the results once (using the latest version of the Realm)
    @Test
    public void testFindAllCallerIsAdvanced() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final CountDownLatch callbackInvokedFinished = new CountDownLatch(1);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final AtomicInteger numberOfInvocation = new AtomicInteger(0);
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();
                final boolean[] isFirstIdle = {true};
                Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                    @Override
                    public boolean queueIdle() {
                        if (isFirstIdle[0]) {
                            isFirstIdle[0] = false;
                            return true;

                        } else {
                            signalCallbackFinished.countDown();
                            return false; // unregister from the future IdleHandler events
                        }
                    }
                });
                final Realm[] realm = new Realm[1];
                try {
                    final AtomicInteger numberOfIntercept = new AtomicInteger(1);
                    realm[0] = openRealmInstance("testFindAllCallerIsAdvanced");
                    final CountDownLatch updateCallerThread = new CountDownLatch(1);
                    final Handler handler = new HandlerProxy(realm[0].handler) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case HandlerController.COMPLETED_UPDATE_ASYNC_QUERIES: {
                                    // posting this as a runnable guarantee that  COMPLETED_UPDATE_ASYNC_QUERIES
                                    // logic complete before resuming the awaiting COMPLETED_ASYNC_REALM_RESULTS
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateCallerThread.countDown();
                                        }
                                    });
                                    break;
                                }
                                case HandlerController.COMPLETED_ASYNC_REALM_RESULTS: {
                                    if (numberOfIntercept.getAndDecrement() > 0) {
                                        // we advance the Realm so we can simulate a retry
                                        // this is intercepted on the worker thread, we need to use
                                        // the Realm on the original thread
                                        postAtFront(new Runnable() {
                                            @Override
                                            public void run() {
                                                // this should trigger the update of all
                                                // async queries
                                                realm[0].beginTransaction();
                                                realm[0].createObject(AllTypes.class);
                                                realm[0].commitTransaction();
                                                sendEmptyMessage(HandlerController.REALM_CHANGED);
                                            }
                                        });

                                        // make this worker thread wait, until we finish
                                        // updating all queries from another thread
                                        try {
                                            updateCallerThread.await();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                            threadAssertionError[0] = e;
                                        }
                                    }
                                    break;
                                }
                            }
                            return false;
                        }
                    };
                    realm[0].setHandler(handler);
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm[0], 10);
                    final RealmResults<AllTypes> realmResults = realm[0].where(AllTypes.class)
                            .between("columnLong", 0, 4)
                            .findAllAsync();

                    assertFalse(realmResults.isLoaded());
                    assertEquals(0, realmResults.size());

                    Realm.asyncQueryExecutor.resume();

                    realmResults.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                assertEquals(1, numberOfInvocation.incrementAndGet());
                                assertTrue(realmResults.isLoaded());
                                assertEquals(6, realmResults.size());
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                callbackInvokedFinished.countDown();
                            }
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (realm[0] != null) {
                        realm[0].close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        // wait until the callback of our async query proceed
        TestHelper.awaitOrFail(signalCallbackFinished);
        TestHelper.awaitOrFail(callbackInvokedFinished);


        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }

        TestHelper.awaitOrFail(signalClosedRealm);

        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }

        assertEquals(1, numberOfInvocation.get());
        executorService.shutdownNow();
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
    public void testFindAllCallerThreadBehind() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(2);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("testFindAllCallerThreadBehind");
                    final RealmConfiguration realmConfiguration = realm.getConfiguration();
                    final AtomicInteger numberOfCompletedAsyncQuery = new AtomicInteger(0);
                    final AtomicInteger numberOfInterceptedChangeMessage = new AtomicInteger(0);
                    final Handler handler = new HandlerProxy(realm.handler) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case HandlerController.REALM_CHANGED: {
                                    // should only intercept the first REALM_CHANGED coming from the
                                    // background update thread

                                    // swallow this message, so the caller thread
                                    // remain behind the worker thread. This has as
                                    // a consequence to ignore the delivered result & wait for the
                                    // upcoming REALM_CHANGED to batch update all async queries
                                    return numberOfInterceptedChangeMessage.getAndIncrement() == 0;
                                }
                                case HandlerController.COMPLETED_ASYNC_REALM_RESULTS: {
                                    if (numberOfCompletedAsyncQuery.incrementAndGet() == 2) {
                                        // both queries have completed now (& their results should be ignored)
                                        // now send the REALM_CHANGED event that should batch update all queries
                                        sendEmptyMessage(HandlerController.REALM_CHANGED);
                                    }
                                }
                            }
                            return false;
                        }
                    };
                    realm.setHandler(handler);
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final RealmResults<AllTypes> realmResults1 = realm.where(AllTypes.class)
                            .findAllAsync();
                    final RealmResults<AllTypes> realmResults2 = realm.where(AllTypes.class)
                            .between("columnLong", 0, 4).findAllAsync();

                    assertFalse(realmResults1.isLoaded());
                    assertFalse(realmResults2.isLoaded());
                    assertEquals(0, realmResults1.size());
                    assertEquals(0, realmResults2.size());

                    // advance the Realm from a background thread
                    final CountDownLatch bgThreadLatch = new CountDownLatch(1);
                    new Thread() {
                        @Override
                        public void run() {
                            Realm bgRealm = Realm.getInstance(realmConfiguration);
                            bgRealm.beginTransaction();
                            bgRealm.where(AllTypes.class).equalTo("columnLong", 4).findFirst().setColumnString("modified");
                            bgRealm.createObject(AllTypes.class);
                            bgRealm.createObject(AllTypes.class);
                            bgRealm.commitTransaction();
                            bgRealm.close();
                            bgThreadLatch.countDown();
                        }
                    }.start();
                    bgThreadLatch.await();
                    Realm.asyncQueryExecutor.resume();

                    final AtomicInteger maxNumberOfNotificationsQuery1 = new AtomicInteger(1);
                    final AtomicInteger maxNumberOfNotificationsQuery2 = new AtomicInteger(1);
                    realmResults1.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                assertTrue(maxNumberOfNotificationsQuery1.getAndDecrement() > 0);
                                assertTrue(realmResults1.isLoaded());
                                assertEquals(12, realmResults1.size());
                                assertEquals("modified", realmResults1.get(4).getColumnString());

                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    realmResults2.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                assertTrue(maxNumberOfNotificationsQuery2.getAndDecrement() > 0);
                                assertTrue(realmResults2.isLoaded());
                                assertEquals(7, realmResults2.size());// the 2 add rows has columnLong == 0
                                assertEquals("modified", realmResults2.get(4).getColumnString());

                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }

    // **********************************
    // *** 'findFirst' async queries  ***
    // **********************************

    // similar UC as #testFindAllAsync using 'findFirst'
    @Test
    public void testFindFirstAsync() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final RealmObject[] result = new RealmObject[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();

                final boolean[] isFirstIdle = {true};
                Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                    @Override
                    public boolean queueIdle() {
                        if (isFirstIdle[0]) {
                            isFirstIdle[0] = false;
                            return true;

                        } else {
                            // Last message (i.e COMPLETED_ASYNC_REALM_RESULTS was processed)
                            try {
                                assertTrue(result[0].isLoaded());
                                //TODO assert value are correct for empty & populated object + test RealmList & RealmObject
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                            return false; // unregister from the future IdleHandler events
                        }
                    }
                });

                Realm realm = null;
                try {
                    realm = openRealmInstance("testFindFirstAsync");
                    populateTestRealm(realm, 10);

                    final AllTypes firstAsync = realm.where(AllTypes.class).findFirstAsync();
                    result[0] = firstAsync;

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }

    @Test
    @RunTestInLooperThread
    public void testFindFirstAsyncWithInitialEmptyRow() throws Throwable {
        final AllTypes firstAsync = workerThread.realm.where(AllTypes.class).findFirstAsync();
        firstAsync.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertTrue(firstAsync.load());
                assertTrue(firstAsync.isLoaded());
                assertTrue(firstAsync.isValid());
                assertEquals(0, firstAsync.getColumnLong());
                workerThread.signalTestCompleted.countDown();
            }
        });
        assertTrue(firstAsync.load());
        assertTrue(firstAsync.isLoaded());
        assertFalse(firstAsync.isValid());

        populateTestRealm(workerThread.realm, 10);
    }

    @Test
    @RunTestInLooperThread
    public void testFindFirstAsyncUpdatedIfSyncRealmObjectIsUpdated() throws Throwable {
        populateTestRealm(workerThread.realm, 1);
        AllTypes firstSync = workerThread.realm.where(AllTypes.class).findFirst();
        assertEquals(0, firstSync.getColumnLong());
        assertEquals("test data 0", firstSync.getColumnString());

        final AllTypes firstAsync = workerThread.realm.where(AllTypes.class).findFirstAsync();
        assertTrue(firstAsync.load());
        assertTrue(firstAsync.isLoaded());
        assertTrue(firstAsync.isValid());
        assertEquals(0, firstAsync.getColumnLong());
        assertEquals("test data 0", firstAsync.getColumnString());

        firstAsync.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertEquals("Galacticon", firstAsync.getColumnString());
                workerThread.signalTestCompleted.countDown();
            }
        });

        workerThread.realm.beginTransaction();
        firstSync.setColumnString("Galacticon");
        workerThread.realm.commitTransaction();
    }

    // finding elements [0-4] asynchronously then wait for the promise to be loaded
    // using a callback to be notified when the data is loaded
    @Test
    @RunTestInLooperThread
    public void testFindFirstAsyncWithNotification() throws Throwable {
        Realm.asyncQueryExecutor.pause();

        populateTestRealm(workerThread.realm, 10);
        final AllTypes realmResults = workerThread.realm.where(AllTypes.class)
                .between("columnLong", 4, 9)
                .findFirstAsync();

        realmResults.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertTrue(realmResults.isLoaded());
                assertTrue(realmResults.isValid());
                assertEquals("test data 4", realmResults.getColumnString());
                workerThread.signalTestCompleted.countDown();
            }
        });

        assertFalse(realmResults.isLoaded());
        assertFalse(realmResults.isValid());
        try {
            realmResults.setColumnString("should fail");
            fail("Accessing an unloaded object should throw");
        } catch (IllegalStateException ignored) {
        }

        Realm.asyncQueryExecutor.resume();
    }

    // similar UC as #testForceLoadAsync using 'findFirst'
    @Test
    @RunTestInLooperThread
    public void testForceLoadFindFirstAsync() throws Throwable {
        Realm.asyncQueryExecutor.pause();

        populateTestRealm(workerThread.realm, 10);
        final AllTypes realmResults = workerThread.realm.where(AllTypes.class)
                .between("columnLong", 4, 9)
                .findFirstAsync();

        assertFalse(realmResults.isLoaded());

        Realm.asyncQueryExecutor.resume();

        assertTrue(realmResults.load());
        assertTrue(realmResults.isLoaded());
        assertEquals("test data 4", realmResults.getColumnString());

        workerThread.signalTestCompleted.countDown();
    }

    // similar UC as #testFindAllAsyncRetry using 'findFirst'
    @Test
    public void testFindFirstAsyncRetry() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();
                final Realm[] realm = new Realm[1];
                try {
                    final AtomicInteger numberOfIntercept = new AtomicInteger(1);
                    realm[0] = openRealmInstance("testFindFirstAsyncRetry");
                    final Handler handler = new HandlerProxy(realm[0].handler) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case HandlerController.COMPLETED_ASYNC_REALM_OBJECT: {
                                    if (numberOfIntercept.getAndDecrement() > 0) {
                                        // we advance the Realm so we can simulate a retry
                                        // this is intercepted on the worker thread, we need to use
                                        // the Realm on the original thread
                                        postAtFront(new Runnable() {
                                            @Override
                                            public void run() {
                                                realm[0].beginTransaction();
                                                realm[0].clear(AllTypes.class);
                                                AllTypes object = realm[0].createObject(AllTypes.class);

                                                object.setColumnString("The Endless River");
                                                object.setColumnLong(5);
                                                realm[0].commitTransaction();
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                            return false;
                        }
                    };
                    realm[0].setHandler(handler);
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm[0], 10);
                    final AllTypes realmResults = realm[0].where(AllTypes.class)
                            .between("columnLong", 4, 6)
                            .findFirstAsync();
                    assertFalse(realmResults.isLoaded());
                    try {
                        realmResults.getColumnString();
                        fail("Accessing property on an empty row");
                    } catch (IllegalStateException ignored) {
                    }

                    Realm.asyncQueryExecutor.resume();

                    final AtomicInteger numberOfInvocation = new AtomicInteger(0);
                    realmResults.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                assertEquals(1, numberOfInvocation.incrementAndGet());
                                assertTrue(realmResults.isLoaded());
                                assertEquals(5, realmResults.getColumnLong());
                                assertEquals("The Endless River", realmResults.getColumnString());
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }

                        }
                    });
                    Looper.loop();
                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm[0] != null) {
                        realm[0].close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }

    // **************************************
    // *** 'findAllSorted' async queries  ***
    // **************************************

    // similar UC as #testFindAllAsync using 'findAllSorted'
    @Test
    public void testFindAllSortedAsync() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final RealmResults[] result = new RealmResults[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();

                final boolean[] isFirstIdle = {true};
                Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                    @Override
                    public boolean queueIdle() {
                        if (isFirstIdle[0]) {
                            isFirstIdle[0] = false;
                            return true;

                        } else {
                            // Last message (i.e COMPLETED_ASYNC_REALM_RESULTS was processed)
                            try {
                                assertTrue(result[0].isLoaded());
                                assertEquals(5, result[0].size());
                                RealmResults<AllTypes> allTypes = (RealmResults<AllTypes>) result[0];
                                for (int i = 0; i < 5; i++) {
                                    int iteration = (4 - i);
                                    assertEquals("test data " + iteration, allTypes.get(4 - iteration).getColumnString());
                                }
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                            return false; // unregister from the future IdleHandler events
                        }
                    }
                });

                Realm realm = null;
                try {
                    realm = openRealmInstance("testFindAllSortedAsync");
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                            .between("columnLong", 0, 4)
                            .findAllSortedAsync("columnString", Sort.DESCENDING);

                    result[0] = realmResults;

                    assertFalse(realmResults.isLoaded());
                    assertEquals(0, realmResults.size());

                    Realm.asyncQueryExecutor.resume();

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }


    // finding elements [4-8] asynchronously then wait for the promise to be loaded
    // using a callback to be notified when the data is loaded
    @Test
    public void testFindAllSortedAsyncRetry() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();

                final Realm[] realm = new Realm[1];
                try {
                    final AtomicInteger numberOfIntercept = new AtomicInteger(1);
                    realm[0] = openRealmInstance("testFindAllSortedAsyncRetry");
                    final Handler handler = new HandlerProxy(realm[0].handler) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case HandlerController.COMPLETED_ASYNC_REALM_RESULTS: {
                                    if (numberOfIntercept.getAndDecrement() > 0) {
                                        // We advance the Realm so we can simulate a retry
                                        // This is intercepted on the worker thread, we need to use
                                        // the Realm on the original thread
                                        postAtFront(new Runnable() {
                                            @Override
                                            public void run() {
                                                realm[0].beginTransaction();
                                                realm[0].clear(AllTypes.class);
                                                realm[0].commitTransaction();
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                            return false;
                        }
                    };
                    realm[0].setHandler(handler);
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm[0], 10);
                    final RealmResults<AllTypes> realmResults = realm[0].where(AllTypes.class)
                            .between("columnLong", 4, 8)
                            .findAllSortedAsync("columnString", Sort.ASCENDING);

                    assertFalse(realmResults.isLoaded());
                    assertEquals(0, realmResults.size());

                    Realm.asyncQueryExecutor.resume();

                    final AtomicInteger numberOfInvocation = new AtomicInteger(0);
                    realmResults.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                assertEquals(1, numberOfInvocation.incrementAndGet());
                                assertTrue(realmResults.isLoaded());
                                assertEquals(0, realmResults.size());
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm[0] != null) {
                        realm[0].close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }

    // similar UC as #testFindAllAsyncBatchUpdate using 'findAllSorted'
    @Test
    public void testFindAllSortedAsyncBatchUpdate() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(2);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final AtomicInteger numberOfNotificationsQuery1 = new AtomicInteger(0);
        final AtomicInteger numberOfNotificationsQuery2 = new AtomicInteger(0);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();

                Realm realm = null;
                try {
                    realm = openRealmInstance("testFindAllSortedAsyncBatchUpdate");
                    final RealmConfiguration realmConfiguration = realm.getConfiguration();
                    final AtomicInteger numberOfIntercept = new AtomicInteger(1);
                    final Handler handler = new HandlerProxy(realm.handler) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case HandlerController.COMPLETED_ASYNC_REALM_RESULTS: {
                                    if (numberOfIntercept.getAndDecrement() > 0) {
                                        // We advance the Realm so we can simulate a retry
                                        // This is intercepted on the worker thread, we need to use
                                        // the Realm on the caller thread
                                        postAtFront(new Runnable() {
                                            @Override
                                            public void run() {
                                                final CountDownLatch bgThreadLatch = new CountDownLatch(1);
                                                new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Realm bgRealm = Realm.getInstance(realmConfiguration);
                                                        bgRealm.beginTransaction();
                                                        bgRealm.where(AllTypes.class).equalTo("columnLong", 4).findFirst().setColumnString("modified");
                                                        bgRealm.createObject(AllTypes.class);
                                                        bgRealm.createObject(AllTypes.class);
                                                        bgRealm.commitTransaction();
                                                        bgRealm.close();
                                                        bgThreadLatch.countDown();
                                                    }
                                                }.start();
                                                try {
                                                    bgThreadLatch.await();
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                    threadAssertionError[0] = e;
                                                }
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                            return false;
                        }
                    };
                    realm.setHandler(handler);
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final RealmResults<AllTypes> realmResults1 = realm.where(AllTypes.class)
                            .findAllSortedAsync("columnString", Sort.ASCENDING);
                    final RealmResults<AllTypes> realmResults2 = realm.where(AllTypes.class)
                            .between("columnLong", 0, 4)
                            .findAllSortedAsync("columnString", Sort.DESCENDING);

                    assertFalse(realmResults1.isLoaded());
                    assertFalse(realmResults2.isLoaded());
                    assertEquals(0, realmResults1.size());
                    assertEquals(0, realmResults2.size());

                    Realm.asyncQueryExecutor.resume();


                    realmResults1.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
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
                                        signalCallbackFinished.countDown();
                                        break;
                                    }
                                    default: {
                                        throw new AssertionFailedError("Callback called more than twice");
                                    }
                                }
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    realmResults2.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
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
                                        signalCallbackFinished.countDown();
                                        break;
                                    }
                                    default: {
                                        throw new AssertionFailedError("Callback called more than twice");
                                    }
                                }
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
        assertEquals(2, numberOfNotificationsQuery1.get());
        assertEquals(2, numberOfNotificationsQuery2.get());
    }

    // similar UC as #testFindAllAsyncBatchUpdate using 'findAllSortedMulti'
    @Test
    public void testFindAllSortedMultiAsyncBatchUpdate() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(2);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final AtomicInteger numberOfNotificationsQuery1 = new AtomicInteger(0);
        final AtomicInteger numberOfNotificationsQuery2 = new AtomicInteger(0);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("testFindAllSortedMultiAsyncBatchUpdate");
                    final RealmConfiguration realmConfiguration = realm.getConfiguration();
                    final AtomicInteger numberOfIntercept = new AtomicInteger(1);
                    final Handler handler = new HandlerProxy(realm.handler) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case HandlerController.COMPLETED_ASYNC_REALM_RESULTS: {
                                    if (numberOfIntercept.getAndDecrement() > 0) {
                                        // We advance the Realm so we can simulate a retry
                                        // This is intercepted on the worker thread, we need to use
                                        // the Realm on the caller thread
                                        postAtFront(new Runnable() {
                                            @Override
                                            public void run() {
                                                final CountDownLatch bgThreadLatch = new CountDownLatch(1);
                                                new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Realm bgRealm = Realm.getInstance(realmConfiguration);
                                                        bgRealm.beginTransaction();
                                                        bgRealm.where(AllTypes.class)
                                                                .equalTo("columnString", "data 1")
                                                                .equalTo("columnLong", 0)
                                                                .findFirst().setColumnDouble(Math.PI);
                                                        AllTypes allTypes = bgRealm.createObject(AllTypes.class);
                                                        allTypes.setColumnLong(2);
                                                        allTypes.setColumnString("data " + 5);

                                                        allTypes = bgRealm.createObject(AllTypes.class);
                                                        allTypes.setColumnLong(0);
                                                        allTypes.setColumnString("data " + 5);
                                                        bgRealm.commitTransaction();
                                                        bgRealm.close();
                                                        bgThreadLatch.countDown();
                                                    }
                                                }.start();
                                                try {
                                                    bgThreadLatch.await();
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                    threadAssertionError[0] = e;
                                                }
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                            return false;
                        }
                    };
                    realm.setHandler(handler);
                    Realm.asyncQueryExecutor.pause();
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
                    final RealmResults<AllTypes> realmResults1 = realm.where(AllTypes.class)
                            .findAllSortedAsync(new String[]{"columnString", "columnLong"},
                                    new Sort[]{Sort.ASCENDING, Sort.DESCENDING});
                    final RealmResults<AllTypes> realmResults2 = realm.where(AllTypes.class)
                            .between("columnLong", 0, 5)
                            .findAllSortedAsync(new String[]{"columnString", "columnLong"},
                                    new Sort[]{Sort.DESCENDING, Sort.ASCENDING});

                    assertFalse(realmResults1.isLoaded());
                    assertFalse(realmResults2.isLoaded());
                    assertEquals(0, realmResults1.size());
                    assertEquals(0, realmResults2.size());

                    Realm.asyncQueryExecutor.resume();

                    realmResults1.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                switch (numberOfNotificationsQuery1.incrementAndGet()) {
                                    case 1: { // first callback invocation
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
                                    }
                                    case 2: { // second callback
                                        assertTrue(realmResults1.isLoaded());
                                        assertEquals(12, realmResults1.size());
                                        //first
                                        assertEquals("data 0", realmResults1.get(0).getColumnString());
                                        assertEquals(3, realmResults1.get(0).getColumnLong());

                                        //last
                                        assertEquals("data 5", realmResults1.get(11).getColumnString());
                                        assertEquals(0, realmResults1.get(11).getColumnLong());

                                        signalCallbackFinished.countDown();
                                        break;
                                    }
                                    default: {
                                        throw new AssertionFailedError("Callback called more than twice");
                                    }
                                }
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    realmResults2.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                switch (numberOfNotificationsQuery2.incrementAndGet()) {
                                    case 1: { // first callback invocation
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
                                    }
                                    case 2: { // second callback
                                        assertTrue(realmResults2.isLoaded());
                                        assertEquals(12, realmResults2.size());

                                        assertEquals("data 5", realmResults2.get(0).getColumnString());
                                        assertEquals(0, realmResults2.get(0).getColumnLong());

                                        assertEquals("data 0", realmResults2.get(11).getColumnString());
                                        assertEquals(3, realmResults2.get(11).getColumnLong());

                                        assertEquals("data 1", realmResults2.get(5).getColumnString());
                                        assertEquals(Math.PI, realmResults2.get(5).getColumnDouble(), 0.000000000001D);

                                        signalCallbackFinished.countDown();
                                        break;
                                    }
                                    default: {
                                        throw new AssertionFailedError("Callback called more than twice");
                                    }
                                }
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
        assertEquals(2, numberOfNotificationsQuery1.get());
        assertEquals(2, numberOfNotificationsQuery2.get());
    }

    // make sure the notification listener does not leak the enclosing class
    // if unregistered properly.
    @Test
    @RunTestInLooperThread
    public void testListenerShouldNotLeak() {
        populateTestRealm(workerThread.realm, 10);

        // simulate the ActivityManager by creating 1 instance responsible
        // of attaching an onChange listener, then simulate a configuration
        // change (ex: screen rotation), this change will create a new instance.
        // we make sure that the GC enqueue the reference of the destroyed instance
        // which indicate no memory leak
        MockActivityManager mockActivityManager =
                MockActivityManager.newInstance(workerThread.realm.getConfiguration());

        mockActivityManager.sendConfigurationChange();

        assertEquals(1, mockActivityManager.numberOfInstances());
        // remove GC'd reference & assert that one instance should remain
        Iterator<Map.Entry<WeakReference<RealmResults<?>>, RealmQuery<?>>> iterator =
                workerThread.realm.handlerController.asyncRealmResults.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WeakReference<RealmResults<?>>, RealmQuery<?>> entry = iterator.next();
            RealmResults<?> weakReference = entry.getKey().get();
            if (weakReference == null) {
                iterator.remove();
            }
        }

        assertEquals(1, workerThread.realm.handlerController.asyncRealmResults.size());
        mockActivityManager.onStop();// to close the Realm
        workerThread.signalTestCompleted.countDown();
    }

    @Test
    @RunTestInLooperThread
    public void testCombiningAsyncAndSync() {
        populateTestRealm(workerThread.realm, 10);

        Realm.asyncQueryExecutor.pause();
        final RealmResults<AllTypes> allTypesAsync = workerThread.realm.where(AllTypes.class).greaterThan("columnLong", 5).findAllAsync();
        final RealmResults<AllTypes> allTypesSync = allTypesAsync.where().greaterThan("columnLong", 3).findAll();

        assertEquals(0, allTypesAsync.size());
        assertEquals(6, allTypesSync.size());
        allTypesAsync.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertEquals(4, allTypesAsync.size());
                assertEquals(6, allTypesSync.size());
                workerThread.signalTestCompleted.countDown();
            }
        });
        Realm.asyncQueryExecutor.resume();
    }

    // keep advancing the Realm by sending 1 commit for each frame (16ms)
    // the async queries should keep up with the modification
    @Test
    @RunTestInLooperThread
    public void testStressTestBackgroundCommits() throws Throwable {
        final int NUMBER_OF_COMMITS = 100;
        final long[] latestLongValue = new long[1];
        final float[] latestFloatValue = new float[1];
        // start a background thread that pushes a commit every 16ms
        final Thread backgroundThread = new Thread() {
            @Override
            public void run() {
                Random random = new Random(System.currentTimeMillis());
                Realm backgroundThreadRealm = Realm.getInstance(workerThread.realm.getConfiguration());
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
            }
        };

        final RealmResults<AllTypes> allAsync = workerThread.realm.where(AllTypes.class).findAllAsync();
        allAsync.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                assertTrue(allAsync.isLoaded());
                if (allAsync.size() == NUMBER_OF_COMMITS) {

                    AllTypes lastInserted = workerThread.realm.where(AllTypes.class)
                            .equalTo("columnLong", latestLongValue[0])
                            .equalTo("columnFloat", latestFloatValue[0])
                            .findFirst();

                    assertNotNull(lastInserted);
                    workerThread.signalTestCompleted.countDown();
                }
            }
        });
        workerThread.keepStrongReference.add(allAsync);

        workerThread.realm.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                backgroundThread.start();
            }
        }, 16);
    }

    @Test
    public void testAsyncDistinct() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(4);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();

                Realm realm = null;
                try {
                    Realm.asyncQueryExecutor.pause();
                    realm = openRealmInstance("testAsyncDistinct");
                    final long numberOfBlocks = 25;
                    final long numberOfObjects = 10; // must be greater than 1

                    populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

                    final RealmResults<AnnotationIndexTypes> distinctBool = realm.distinctAsync(AnnotationIndexTypes.class, "indexBoolean");
                    final RealmResults<AnnotationIndexTypes> distinctLong = realm.distinctAsync(AnnotationIndexTypes.class, "indexLong");
                    final RealmResults<AnnotationIndexTypes> distinctDate = realm.distinctAsync(AnnotationIndexTypes.class, "indexDate");
                    final RealmResults<AnnotationIndexTypes> distinctString = realm.distinctAsync(AnnotationIndexTypes.class, "indexString");

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

                    Realm.asyncQueryExecutor.resume();

                    distinctBool.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            assertEquals(2, distinctBool.size());
                            signalCallbackFinished.countDown();
                        }
                    });

                    distinctLong.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            assertEquals(numberOfBlocks, distinctLong.size());
                            signalCallbackFinished.countDown();
                        }
                    });

                    distinctDate.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            assertEquals(numberOfBlocks, distinctDate.size());
                            signalCallbackFinished.countDown();
                        }
                    });

                    distinctString.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            assertEquals(numberOfBlocks, distinctString.size());
                            signalCallbackFinished.countDown();
                        }
                    });

                    Looper.loop();
                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }

    @Test
    public void testAsyncDistinctNotIndexedFields() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(4);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();

                Realm realm = null;
                try {
                    realm = openRealmInstance("testAsyncDistinctNotIndexedFields");
                    final long numberOfBlocks = 25;
                    final long numberOfObjects = 10; // must be greater than 1

                    populateForDistinct(realm, numberOfBlocks, numberOfObjects, false);

                    for (String fieldName : new String[]{"Boolean", "Long", "Date", "String"}) {
                        try {
                            realm.distinctAsync(AnnotationIndexTypes.class, "notIndex" + fieldName);
                            fail("notIndex" + fieldName);
                        } catch (IllegalArgumentException ignored) {
                            signalCallbackFinished.countDown();
                        }
                    }

                    Looper.loop();
                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }

    @Test
    @RunTestInLooperThread
    public void testAsyncDistinctFieldDoesNotExist() throws Throwable {
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        populateForDistinct(workerThread.realm, numberOfBlocks, numberOfObjects, false);

        try {
            workerThread.realm.distinctAsync(AnnotationIndexTypes.class, "doesNotExist");
            fail();
        } catch (IllegalArgumentException ignored) {
            workerThread.signalTestCompleted.countDown();
        }
    }

    @Test
    @RunTestInLooperThread
    public void testBatchUpdateDifferentTypeOfQueries() {
        workerThread.realm.beginTransaction();
        for (int i = 0; i < 5; ) {
            AllTypes allTypes = workerThread.realm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
            allTypes.setColumnString("data " + i % 3);

            allTypes = workerThread.realm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
            allTypes.setColumnString("data " + (++i % 3));
        }
        final long numberOfBlocks = 25;
        final long numberOfObjects = 10; // must be greater than 1
        workerThread.realm.commitTransaction();
        populateForDistinct(workerThread.realm, numberOfBlocks, numberOfObjects, false);

        RealmResults<AllTypes> findAllAsync = workerThread.realm.where(AllTypes.class).findAllAsync();
        RealmResults<AllTypes> findAllSorted = workerThread.realm.where(AllTypes.class).findAllSortedAsync("columnString", Sort.ASCENDING);
        RealmResults<AllTypes> findAllSortedMulti = workerThread.realm.where(AllTypes.class).findAllSortedAsync(new String[]{"columnString", "columnLong"},
                new Sort[]{Sort.ASCENDING, Sort.DESCENDING});
        RealmResults<AnnotationIndexTypes> findDistinct = workerThread.realm.distinctAsync(AnnotationIndexTypes.class, "indexString");

        workerThread.keepStrongReference.add(findAllAsync);
        workerThread.keepStrongReference.add(findAllSorted);
        workerThread.keepStrongReference.add(findAllSortedMulti);
        workerThread.keepStrongReference.add(findDistinct);

        final CountDownLatch queriesCompleted = new CountDownLatch(4);
        final AtomicInteger batchUpdateCompleted = new AtomicInteger(0);
        final AtomicInteger findAllAsyncInvocation = new AtomicInteger(0);
        final AtomicInteger findAllSortedInvocation = new AtomicInteger(0);
        final AtomicInteger findAllSortedMultiInvocation = new AtomicInteger(0);
        final AtomicInteger findDistinctInvocation = new AtomicInteger(0);

        findAllAsync.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                switch (findAllAsyncInvocation.incrementAndGet()) {
                    case 1: {
                        queriesCompleted.countDown();
                        break;
                    }
                    case 2: {
                        if (batchUpdateCompleted.incrementAndGet() == 4) {
                            workerThread.signalTestCompleted.countDown();
                        }
                        break;
                    }
                }
            }
        });

        findAllSorted.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                switch (findAllSortedInvocation.incrementAndGet()) {
                    case 1: {
                        queriesCompleted.countDown();
                        break;
                    }
                    case 2: {
                        if (batchUpdateCompleted.incrementAndGet() == 4) {
                            workerThread.signalTestCompleted.countDown();
                        }
                        break;
                    }
                }
            }
        });

        findAllSortedMulti.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                switch (findAllSortedMultiInvocation.incrementAndGet()) {
                    case 1: {
                        queriesCompleted.countDown();
                        break;
                    }
                    case 2: {
                        if (batchUpdateCompleted.incrementAndGet() == 4) {
                            workerThread.signalTestCompleted.countDown();
                        }
                        break;
                    }
                }
            }
        });

        findDistinct.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                switch (findDistinctInvocation.incrementAndGet()) {
                    case 1: {
                        queriesCompleted.countDown();
                        break;
                    }
                    case 2: {
                        if (batchUpdateCompleted.incrementAndGet() == 4) {
                            workerThread.signalTestCompleted.countDown();
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
                    Realm bgRealm = Realm.getInstance(workerThread.realm.getConfiguration());

                    bgRealm.beginTransaction();
                    bgRealm.createObject(AllTypes.class);
                    bgRealm.createObject(AnnotationIndexTypes.class);
                    bgRealm.commitTransaction();

                    bgRealm.close();
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }
            }
        }.start();
    }

    // this test make sure that Async queries update when using link
    public void testQueryingLinkHandover() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final AtomicInteger numberOfInvocations = new AtomicInteger(0);
        final Realm[] realm = new Realm[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        final Looper[] backgroundLooper = new Looper[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();

                try {
                    realm[0] = openRealmInstance("testQueryingLinkHandover");

                    final RealmResults<Dog> allAsync = realm[0].where(Dog.class).equalTo("owner.name", "kiba").findAllAsync();
                    allAsync.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            switch (numberOfInvocations.incrementAndGet()) {
                                case 1: {
                                    assertEquals(0, allAsync.size());
                                    assertTrue(allAsync.isLoaded());
                                    assertTrue(allAsync.isValid());
                                    assertTrue(allAsync.isEmpty());
                                    final CountDownLatch wait = new CountDownLatch(1);
                                    final RealmConfiguration configuration = realm[0].getConfiguration();
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            Realm instance = Realm.getInstance(configuration);
                                            instance.beginTransaction();
                                            Dog dog = instance.createObject(Dog.class);
                                            dog.setAge(10);
                                            dog.setName("Akamaru");
                                            Owner kiba = instance.createObject(Owner.class);
                                            kiba.setName("kiba");
                                            dog.setOwner(kiba);
                                            instance.commitTransaction();
                                            wait.countDown();
                                        }
                                    }.start();
                                    try {
                                        wait.await();
                                    } catch (InterruptedException e) {
                                        fail(e.getMessage());
                                    }
                                    break;
                                }
                                case 2: {
                                    assertEquals(1, realm[0].allObjects(Dog.class).size());
                                    assertEquals(1, realm[0].allObjects(Owner.class).size());
                                    assertEquals(1, allAsync.size());
                                    assertTrue(allAsync.isLoaded());
                                    assertTrue(allAsync.isValid());
                                    assertFalse(allAsync.isEmpty());
                                    assertEquals(1, allAsync.size());
                                    assertEquals("Akamaru", allAsync.get(0).getName());
                                    assertEquals("kiba", allAsync.get(0).getOwner().getName());
                                    signalCallbackFinished.countDown();
                                    break;
                                }
                                default:
                                    throw new IllegalStateException("invalid number of invocation");
                            }
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                    if (realm.length > 0 && realm[0] != null) {
                        realm[0].close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalCallbackFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }

    // *** Helper methods ***

    // This could be done from #setUp but then we can't control
    // which Looper we want to associate this Realm instance with
    private Realm openRealmInstance(String name) {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(context)
                .name(name)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.deleteRealm(realmConfiguration);
        return Realm.getInstance(realmConfiguration);
    }

    private void populateTestRealm(final Realm testRealm, int objects) {
        testRealm.beginTransaction();
        testRealm.allObjects(AllTypes.class).clear();
        testRealm.allObjects(NonLatinFieldNames.class).clear();
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
        testRealm.refresh();
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
