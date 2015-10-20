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
import android.os.Looper;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.test.InstrumentationTestCase;

import junit.framework.AssertionFailedError;

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
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.Owner;
import io.realm.instrumentation.MockActivityManager;
import io.realm.proxy.HandlerProxy;

public class RealmAsyncQueryTests extends InstrumentationTestCase {
    // ****************************
    // ****  Async transaction  ***
    // ****************************

    // start asynchronously a transaction to insert one element
    public void testAsyncTransaction() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                    realm[0] = openRealmInstance("testAsyncTransaction");

                    assertEquals(0, realm[0].allObjects(Owner.class).size());

                    realm[0].executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Owner owner = realm.createObject(Owner.class);
                            owner.setName("Owner");
                        }
                    }, new Realm.Transaction.Callback() {
                        @Override
                        public void onSuccess() {
                            try {
                                assertEquals(1, realm[0].allObjects(Owner.class).size());
                                assertEquals("Owner", realm[0].where(Owner.class).findFirst().getName());
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            threadAssertionError[0] = e;
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
                    if (realm.length > 0 && realm[0] != null) {
                        realm[0].close();
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
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }
    }

    // ************************************
    // *** promises based async queries ***
    // ************************************

    // finding element [0-4] asynchronously then wait for the promise to be loaded.
    // no use of notification callback
    public void testFindAllAsync() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                            // Last message (i.e REALM_COMPLETED_ASYNC_QUERY was processed)
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
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }
    }

    public void testUnloadedRealmListsShouldBeTheSameInstance () throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                    realm = openRealmInstance("testUnloadedRealmListsShouldBeTheSameInstance");
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final AllTypes alltypes1 = realm.where(AllTypes.class)
                            .equalTo("columnLong", 0)
                            .findFirstAsync();

                    final AllTypes alltypes2 = realm.where(AllTypes.class)
                            .equalTo("columnLong", 4)
                            .findFirstAsync();

                    assertFalse(alltypes1.isLoaded());
                    assertNotNull(alltypes1.getColumnRealmList());
                    assertEquals(0, alltypes1.getColumnRealmList().size());

                    assertFalse(alltypes2.isLoaded());
                    assertNotNull(alltypes2.getColumnRealmList());
                    assertEquals(0, alltypes2.getColumnRealmList().size());

                    assertEquals(alltypes1.getColumnRealmList(), alltypes2.getColumnRealmList());
                    assertTrue(alltypes1.getColumnRealmList() == alltypes2.getColumnRealmList());

                    Realm.asyncQueryExecutor.resume();
                    signalCallbackFinished.countDown();
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
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }
    }

    public void testStandloneObjectAsyncBehaviour () {
        Dog dog = new Dog();
        dog.setName("Akamaru");
        dog.setAge(10);

        assertTrue(dog.isLoaded());
        assertFalse(dog.isValid());
    }

    public void testAsyncQueryOnNonLooperThreadShouldThrow () throws Throwable {
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

    public void testReusingQuery() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                    realm = openRealmInstance("testResusingQuery");
                    populateTestRealm(realm, 10);

                    RealmQuery<AllTypes> query = realm.where(AllTypes.class)
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
                        signalCallbackFinished.countDown();
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
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }
    }

    // finding elements [0-4] asynchronously then wait for the promise to be loaded
    // using a callback to be notified when the data is loaded
    public void testFindAllAsyncWithNotification() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Looper[] backgroundLooper = new Looper[1];
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                backgroundLooper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("testFindAllAsyncWithNotification");
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                            .between("columnLong", 0, 4)
                            .findAllAsync();

                    realmResults.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                assertTrue(realmResults.isLoaded());
                                assertEquals(5, realmResults.size());
                                assertTrue(realmResults.get(4).isValid());
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }

                        }
                    });

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
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }
    }

    // transforming an async query into sync by calling load to force
    // the blocking behaviour
    public void testForceLoadAsync() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                    realm = openRealmInstance("testForceLoadAsync");
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                            .between("columnLong", 0, 4)
                            .findAllAsync();

                    // notification should be called as well
                    realmResults.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                assertTrue(realmResults.isLoaded());
                                assertEquals(5, realmResults.size());
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            }
                            signalCallbackFinished.countDown();
                        }
                    });

                    assertFalse(realmResults.isLoaded());
                    assertEquals(0, realmResults.size());

                    Realm.asyncQueryExecutor.resume();
                    boolean successful = realmResults.load();

                    assertTrue(successful);
                    assertTrue(realmResults.isLoaded());
                    assertEquals(5, realmResults.size());

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
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }
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
    public void testFindAllAsyncRetry() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                                case Realm.REALM_COMPLETED_ASYNC_QUERY: {
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
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }
    }

    // UC:
    //   1- insert 10 objects
    //   2- start 2 async queries to find all objects [0-9] & objects[0-4]
    //   3- assert both RealmResults are empty (Worker Thread didn't complete)
    //   4- the queries will complete with the same version as the caller thread
    //   5- using a background thread update the Realm
    //   6- now REALM_CHANGED will trigger a REALM_UPDATE_ASYNC_QUERIES that should update all queries
    //   7- callbacks are notified with the latest results (called twice overall)
    public void testFindAllAsyncBatchUpdate() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(2);
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
                                case Realm.REALM_COMPLETED_ASYNC_QUERY: {
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
                }
            }
        });

        TestHelper.awaitOrFail(signalCallbackFinished);
        assertEquals(2, numberOfNotificationsQuery1.get());
        assertEquals(2, numberOfNotificationsQuery2.get());

        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }
    }

    // simulate a use case, when the caller thread advance read, while the background thread
    // is operating on a previous version, this should retry the query on the worker thread
    // to deliver the results once (using the latest version of the Realm)
    public void testFindAllCallerIsAdvanced() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final CountDownLatch callbackInvokedFinished = new CountDownLatch(1);
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
                                case Realm.REALM_UPDATE_ASYNC_QUERIES: {
                                    // posting this as a runnable guarantee that  REALM_UPDATE_ASYNC_QUERIES
                                    // logic complete before resuming the awaiting REALM_COMPLETED_ASYNC_QUERY
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateCallerThread.countDown();
                                        }
                                    });
                                    break;
                                }
                                case Realm.REALM_COMPLETED_ASYNC_QUERY: {
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
                                                sendEmptyMessage(Realm.REALM_CHANGED);
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
                }
            }
        });

        // wait until the callback of our async query proceed
        TestHelper.awaitOrFail(signalCallbackFinished);
        TestHelper.awaitOrFail(callbackInvokedFinished);

        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
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
    public void testFindAllCallerThreadBehind() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(2);
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
                                case Realm.REALM_CHANGED: {
                                    // should only intercept the first REALM_CHANGED coming from the
                                    // background update thread

                                    // swallow this message, so the caller thread
                                    // remain behind the worker thread. This has as
                                    // a consequence to ignore the delivered result & wait for the
                                    // upcoming REALM_CHANGED to batch update all async queries
                                    return numberOfInterceptedChangeMessage.getAndIncrement() == 0;
                                }
                                case Realm.REALM_COMPLETED_ASYNC_QUERY: {
                                    if (numberOfCompletedAsyncQuery.incrementAndGet() == 2) {
                                        // both queries have completed now (& their results should be ignored)
                                        // now send the REALM_CHANGED event that should batch update all queries
                                        sendEmptyMessage(Realm.REALM_CHANGED);
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
                }
            }
        });

        TestHelper.awaitOrFail(signalCallbackFinished);

        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }

        executorService.shutdownNow();
    }

    // **********************************
    // *** 'findFirst' async queries  ***
    // **********************************

    // similar UC as #testFindAllAsync using 'findFirst'
    public void testFindFirstAsync() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                            // Last message (i.e REALM_COMPLETED_ASYNC_QUERY was processed)
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
                }
            }
        });

        // wait until the callback of our async query proceed
        TestHelper.awaitOrFail(signalCallbackFinished);
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }
        executorService.shutdownNow();
    }

    // finding elements [0-4] asynchronously then wait for the promise to be loaded
    // using a callback to be notified when the data is loaded
    public void testFindFirstAsyncWithNotification() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                    realm = openRealmInstance("testFindFirstAsyncWithNotification");
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final AllTypes realmResults = realm.where(AllTypes.class)
                            .between("columnLong", 4, 9)
                            .findFirstAsync();

                    realmResults.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                assertTrue(realmResults.isLoaded());
                                assertTrue(realmResults.isValid());
                                assertEquals("test data 4", realmResults.getColumnString());
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    assertFalse(realmResults.isLoaded());
                    assertFalse(realmResults.isValid());
                    assertEquals("", realmResults.getColumnString());
                    try {
                        realmResults.setColumnString("should fail");
                        fail("Accessing an unloaded object should throw");
                    } catch (IllegalStateException ignore) {
                    }

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
                }
            }
        });

        // wait until the callback of our async query proceed
        TestHelper.awaitOrFail(signalCallbackFinished);

        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }

        executorService.shutdownNow();
    }

    // similar UC as #testForceLoadAsync using 'findFirst'
    public void testForceLoadFindFirstAsync() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                    realm = openRealmInstance("testForceLoadFindFirstAsync");
                    Realm.asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final AllTypes realmResults = realm.where(AllTypes.class)
                            .between("columnLong", 4, 9)
                            .findFirstAsync();

                    assertFalse(realmResults.isLoaded());
                    assertEquals("", realmResults.getColumnString());

                    Realm.asyncQueryExecutor.resume();

                    assertTrue(realmResults.load());
                    assertTrue(realmResults.isLoaded());
                    assertEquals("test data 4", realmResults.getColumnString());

                    signalCallbackFinished.countDown();
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
                }
            }
        });

        // wait until the callback of our async query proceed
        TestHelper.awaitOrFail(signalCallbackFinished);

        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }

        executorService.shutdownNow();
    }

    // similar UC as #testFindAllAsyncRetry using 'findFirst'
    public void testFindFirstAsyncRetry() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                                case Realm.REALM_COMPLETED_ASYNC_FIND_FIRST: {
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
                    assertEquals("", realmResults.getColumnString());

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
                }
            }
        });

        // wait until the callback of our async query proceed
        TestHelper.awaitOrFail(signalCallbackFinished);
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }

        executorService.shutdownNow();
    }


    // **************************************
    // *** 'findAllSorted' async queries  ***
    // **************************************

    // similar UC as #testFindAllAsync using 'findAllSorted'
    public void testFindAllSortedAsync() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                            // Last message (i.e REALM_COMPLETED_ASYNC_QUERY was processed)
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
                            .findAllSortedAsync("columnString", RealmResults.SORT_ORDER_DESCENDING);

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
                }
            }
        });

        // wait until the callback of our async query proceed
        TestHelper.awaitOrFail(signalCallbackFinished);

        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }

        executorService.shutdownNow();
    }


    // finding elements [4-8] asynchronously then wait for the promise to be loaded
    // using a callback to be notified when the data is loaded
    public void testFindAllSortedAsyncRetry() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                                case Realm.REALM_COMPLETED_ASYNC_QUERY: {
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
                            .findAllSortedAsync("columnString", RealmResults.SORT_ORDER_ASCENDING);

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
                }
            }
        });

        // wait until the callback of our async query proceed
        TestHelper.awaitOrFail(signalCallbackFinished);

        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }

        executorService.shutdownNow();
    }

    // similar UC as #testFindAllAsyncBatchUpdate using 'findAllSorted'
    public void testFindAllSortedAsyncBatchUpdate() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(2);
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
                                case Realm.REALM_COMPLETED_ASYNC_QUERY: {
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
                            .findAllSortedAsync("columnString", RealmResults.SORT_ORDER_ASCENDING);
                    final RealmResults<AllTypes> realmResults2 = realm.where(AllTypes.class)
                            .between("columnLong", 0, 4)
                            .findAllSortedAsync("columnString", RealmResults.SORT_ORDER_DESCENDING);

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
                }
            }
        });

        TestHelper.awaitOrFail(signalCallbackFinished);
        assertEquals(2, numberOfNotificationsQuery1.get());
        assertEquals(2, numberOfNotificationsQuery2.get());

        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }

        executorService.shutdownNow();
    }

    // similar UC as #testFindAllAsyncBatchUpdate using 'findAllSortedMulti'
    public void testFindAllSortedMultiAsyncBatchUpdate() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(2);
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
                                case Realm.REALM_COMPLETED_ASYNC_QUERY: {
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
                                    new boolean[]{RealmResults.SORT_ORDER_ASCENDING, RealmResults.SORT_ORDER_DESCENDING});
                    final RealmResults<AllTypes> realmResults2 = realm.where(AllTypes.class)
                            .between("columnLong", 0, 5)
                            .findAllSortedAsync(new String[]{"columnString", "columnLong"},
                                    new boolean[]{RealmResults.SORT_ORDER_DESCENDING, RealmResults.SORT_ORDER_ASCENDING});

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
                }
            }
        });

        TestHelper.awaitOrFail(signalCallbackFinished);
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }

        assertEquals(2, numberOfNotificationsQuery1.get());
        assertEquals(2, numberOfNotificationsQuery2.get());

        executorService.shutdownNow();
    }

    // make sure the notification listener does not leak the enclosing class
    // if unregistered properly.
    public void testListenerShouldNotLeak() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
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
                    realm = openRealmInstance("testListenerShouldNotLeak");
                    populateTestRealm(realm, 10);

                    // simulate the ActivityManager by creating 1 instance responsible
                    // of attaching an onChange listener, then simulate a configuration
                    // change (ex: screen rotation), this change will create a new instance.
                    // we make sure that the GC enqueue the reference of the destroyed instance
                    // which indicate no memory leak
                    MockActivityManager mockActivityManager =
                            MockActivityManager.newInstance(realm.getConfiguration());

                    mockActivityManager.sendConfigurationChange();

                    try {
                        assertEquals(1, mockActivityManager.numberOfInstances());
                        // remove GC'd reference & assert that one instance should remain
                        Iterator<Map.Entry<WeakReference<RealmResults<?>>, RealmQuery<?>>> iterator =
                                realm.handlerController.asyncRealmResults.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<WeakReference<RealmResults<?>>, RealmQuery<?>> entry = iterator.next();
                            RealmResults<?> weakReference = entry.getKey().get();
                            if (weakReference == null) {
                                iterator.remove();
                            }
                        }

                        assertEquals(1, realm.handlerController.asyncRealmResults.size());

                    } catch (AssertionFailedError e) {
                        threadAssertionError[0] = e;
                    } finally {
                        signalCallbackFinished.countDown();
                        mockActivityManager.onStop();
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
                }
            }
        });

        // wait until the callback of our async query proceed
        TestHelper.awaitOrFail(signalCallbackFinished);

        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }

        executorService.shutdownNow();
    }

    // keep advancing the Realm by sending 1 commit for each frame (16ms)
    // the async queries should keep up with the modification
    public void testStressTestBackgroundCommits() throws Throwable {
        final int NUMBER_OF_COMMITS = 1000;
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
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
                    realm[0] = openRealmInstance("testStressTestBackgroundCommits");
                    final RealmConfiguration configuration = realm[0].getConfiguration();
                    final long[] latestLongValue = new long[1];
                    final float[] latestFloatValue = new float[1];
                    // start a background thread that pushes a commit every 500ms
                    final Thread backgroundThread = new Thread() {
                        @Override
                        public void run() {
                            Random random = new Random(System.currentTimeMillis());
                            Realm backgroundThreadRealm = Realm.getInstance(configuration);
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

                    final RealmResults<AllTypes> allAsync = realm[0].where(AllTypes.class).findAllAsync();
                    allAsync.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            assertTrue(allAsync.isLoaded());
                            if (allAsync.size() == NUMBER_OF_COMMITS) {

                                AllTypes lastInserted = realm[0].where(AllTypes.class)
                                        .equalTo("columnLong", latestLongValue[0])
                                        .equalTo("columnFloat", latestFloatValue[0])
                                        .findFirst();

                                assertNotNull(lastInserted);
                                signalTestFinished.countDown();
                            }
                        }
                    });

                    realm[0].handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            backgroundThread.start();
                        }
                    }, 16);

                    Looper.loop();

                } catch (Throwable e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalTestFinished.getCount() > 0) {
                        signalTestFinished.countDown();
                    }
                    if (realm[0] != null) {
                        realm[0].close();
                    }
                }
            }
        });

        TestHelper.awaitOrFail(signalTestFinished, 120);
        executorService.shutdownNow();
        if (backgroundLooper[0] != null) {
            // failing to quit the looper will not execute the finally block responsible
            // of closing the Realm
            backgroundLooper[0].quit();
        }
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    // *** Helper methods ***

    // This could be done from #setUp but then we can't control
    // which Looper we want to associate this Realm instance with
    private Realm openRealmInstance(String name) {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(getInstrumentation().getTargetContext())
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

}
