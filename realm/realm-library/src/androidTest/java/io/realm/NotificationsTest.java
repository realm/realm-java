/*
 * Copyright 2014 Realm Inc.
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
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.internal.log.Logger;
import io.realm.internal.log.RealmLog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class NotificationsTest {

    private Realm realm;
    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    private Context context;

    @Before
    public void setUp() throws Exception {
        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        context = InstrumentationRegistry.getInstrumentation().getContext();

        Realm.deleteRealm(TestHelper.createConfiguration(context));
    }

    @After
    public void tearDown() throws Exception {
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void testFailingSetAutoRefreshOnNonLooperThread() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Realm realm = Realm.getInstance(context);
                boolean autoRefresh = realm.isAutoRefresh();
                assertFalse(autoRefresh);
                try {
                    realm.setAutoRefresh(true);
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                } finally {
                    realm.close();
                }
            }
        });

        assertTrue(future.get());
        RealmCache.invokeWithGlobalRefCount(new RealmConfiguration.Builder(context).build(),
                new TestHelper.ExpectedCountCallback(0));
    }

    @Test
    public void testSetAutoRefreshOnHandlerThread() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Looper.prepare();
                Realm realm = Realm.getInstance(context);
                assertTrue(realm.isAutoRefresh());
                realm.setAutoRefresh(false);
                assertFalse(realm.isAutoRefresh());
                realm.setAutoRefresh(true);
                assertTrue(realm.isAutoRefresh());
                realm.close();
                return true;
            }
        });
        assertTrue(future.get());
        RealmCache.invokeWithGlobalRefCount(new RealmConfiguration.Builder(context).build(),
                new TestHelper.ExpectedCountCallback(0));
    }

    @Test
    @UiThreadTest
    public void testRemoveNotifications() throws InterruptedException, ExecutionException {
        final AtomicInteger counter = new AtomicInteger(0);
        RealmChangeListener listener = new RealmChangeListener() {
            @Override
            public void onChange() {
                counter.incrementAndGet();
            }
        };

        realm = Realm.getInstance(context);
        realm.addChangeListener(listener);
        realm.removeChangeListener(listener);

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        assertEquals(0, counter.get());
    }

    @Test
    @UiThreadTest
    public void testAddDuplicatedListener() {
        final AtomicInteger counter = new AtomicInteger(0);
        RealmChangeListener listener = new RealmChangeListener() {
            @Override
            public void onChange() {
                counter.incrementAndGet();
            }
        };

        realm = Realm.getInstance(context);
        realm.addChangeListener(listener);
        realm.addChangeListener(listener);

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        assertEquals(1, counter.get());
    }

    @Test
    public void testNotificationsNumber() throws InterruptedException, ExecutionException {
        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicBoolean isReady = new AtomicBoolean(false);
        final Looper[] looper = new Looper[1];
        final AtomicBoolean isRealmOpen = new AtomicBoolean(true);
        final RealmChangeListener listener = new RealmChangeListener() {
            @Override
            public void onChange() {
                counter.incrementAndGet();
            }
        };

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Realm realm = null;
                try {
                    Looper.prepare();
                    looper[0] = Looper.myLooper();
                    realm = Realm.getInstance(context);
                    realm.addChangeListener(listener);
                    isReady.set(true);
                    Looper.loop();
                } finally {
                    if (realm != null) {
                        realm.close();
                        isRealmOpen.set(false);
                    }
                }
                return true;
            }
        });

        // Wait until the looper in the background thread is started
        while (!isReady.get()) {
            Thread.sleep(5);
        }
        Thread.sleep(100);

        // Trigger OnRealmChanged on background thread
        realm = Realm.getInstance(context);
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Rex");
        realm.commitTransaction();
        realm.close();

        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException ignore) {
        } finally {
            looper[0].quit();
        }

        // Wait until the Looper thread is actually closed
        while (isRealmOpen.get()) {
            Thread.sleep(5);
        }

        assertEquals(1, counter.get());
        RealmCache.invokeWithGlobalRefCount(new RealmConfiguration.Builder(context).build(),
                new TestHelper.ExpectedCountCallback(0));
    }

    @Test
    public void testAutoUpdateRealmResults() throws InterruptedException, ExecutionException {
        final int TEST_SIZE = 10;
        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicBoolean isReady = new AtomicBoolean(false);
        final AtomicBoolean isRealmOpen = new AtomicBoolean(true);
        final Map<Integer, Integer> results = new ConcurrentHashMap<Integer, Integer>();
        final Looper[] looper = new Looper[1];
        final RealmChangeListener listener[] = new RealmChangeListener[1];

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = Realm.getInstance(context);
                    final RealmResults<Dog> dogs = realm.allObjects(Dog.class);
                    assertEquals(0, dogs.size());
                    listener[0] = new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            int c = counter.incrementAndGet();
                            results.put(c, dogs.size());
                        }
                    };
                    realm.addChangeListener(listener[0]);
                    isReady.set(true);
                    Looper.loop();
                } finally {
                    if (realm != null) {
                        realm.close();
                        isRealmOpen.set(false);
                    }
                }
                return true;
            }
        });

        // Wait until the looper is started
        while (!isReady.get()) {
            Thread.sleep(5);
        }
        Thread.sleep(100);

        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            Dog dog = realm.createObject(Dog.class);
            dog.setName("Rex " + i);
        }
        realm.commitTransaction();
        assertEquals(TEST_SIZE, realm.allObjects(Dog.class).size());
        realm.close();

        try {
            future.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException ignore) {
        } finally {
            looper[0].quit();
        }

        // Wait until the Looper thread is actually closed
        while (isRealmOpen.get()) {
            Thread.sleep(5);
        }

        assertEquals(1, results.size());

        assertTrue(results.containsKey(1));
        assertEquals(TEST_SIZE, results.get(1).intValue());

        assertEquals(1, counter.get());
        RealmCache.invokeWithGlobalRefCount(new RealmConfiguration.Builder(context).build(),
                new TestHelper.ExpectedCountCallback(0));
    }

    // TODO Disabled until we can figure out why this times out so often on the build server
    public void DISABLEDtestCloseClearingHandlerMessages() throws InterruptedException, TimeoutException, ExecutionException {
        final int TEST_SIZE = 10;
        final CountDownLatch backgroundLooperStarted = new CountDownLatch(1);
        final CountDownLatch addHandlerMessages = new CountDownLatch(1);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Looper.prepare(); // Fake background thread with a looper, eg. a IntentService
                Realm realm = Realm.getInstance(context);
                backgroundLooperStarted.countDown();

                // Random operation in the client code
                final RealmResults<Dog> dogs = realm.allObjects(Dog.class);
                if (dogs.size() != 0) {
                    return false;
                }
                addHandlerMessages.await(1, TimeUnit.SECONDS); // Wait for main thread to add update messages

                // Find the current Handler for the thread now. All message and references will be
                // cleared once we call close().
                Handler threadHandler = realm.handler;
                realm.close(); // Close native resources + associated handlers.

                // Looper now reads the update message from the main thread if the Handler was not
                // cleared. This will cause an IllegalStateException and should not happen.
                // If it works correctly. The looper will just block on an empty message queue.
                // This is normal behavior but is bad for testing, so we add a custom quit message
                // at the end so we can evaluate results faster.
                threadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Looper.myLooper().quit();
                    }
                });

                try {
                    Looper.loop();
                } catch (IllegalStateException e) {
                    return false;
                }
                return true;
            }
        });

        // Wait until the looper is started on a background thread
        backgroundLooperStarted.await(1, TimeUnit.SECONDS);

        // Execute a transaction that will trigger a Realm update
        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            Dog dog = realm.createObject(Dog.class);
            dog.setName("Rex " + i);
        }
        realm.commitTransaction();
        assertEquals(TEST_SIZE, realm.allObjects(Dog.class).size());
        realm.close();
        addHandlerMessages.countDown();

        // Check that messages was properly cleared
        // It looks like getting this future sometimes takes a while for some reason. Setting to
        // 10s. now.
        Boolean result = future.get(10, TimeUnit.SECONDS);
        assertTrue(result);
    }

    @Test
    @UiThreadTest
    public void testHandlerNotRemovedToSoon() {
        RealmConfiguration realmConfig = TestHelper.createConfiguration(context, "private-realm");
        Realm.deleteRealm(realmConfig);
        Realm instance1 = Realm.getInstance(realmConfig);
        Realm instance2 = Realm.getInstance(realmConfig);
        assertEquals(instance1.getPath(), instance2.getPath());
        assertNotNull(instance1.handler);

        // If multiple instances are open on the same thread, don't remove handler on that thread
        // until last instance is closed.
        instance2.close();
        assertNotNull(instance1.handler);
        instance1.close();
        assertNull(instance1.handler);
    }

    @Test
    @UiThreadTest
    public void testImmediateNotificationsOnSameThread() {
        final AtomicBoolean success = new AtomicBoolean(false);
        final RealmChangeListener listener[] = new RealmChangeListener[1];
        realm = Realm.getInstance(context);
        listener[0] = new RealmChangeListener() {
            @Override
            public void onChange() {
                // Listener should only be called once
                assertFalse(success.get());
                success.set(true);
            }
        };
        realm.addChangeListener(listener[0]);
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
        assertTrue(success.get());
    }

    @Test
    @UiThreadTest
    public void testEmptyCommitTriggerChangeListener() {
        final AtomicBoolean success = new AtomicBoolean(false);
        final RealmChangeListener listener = new RealmChangeListener() {
            @Override
            public void onChange() {
                success.set(true);
            }
        };
        realm = Realm.getInstance(context);
        realm.addChangeListener(listener);
        realm.beginTransaction();
        realm.commitTransaction();
        assertTrue(success.get());
    }

    @Test
    @UiThreadTest
    public void testAddRemoveListenerConcurrency() {
        final AtomicInteger counter1 = new AtomicInteger(0);
        final AtomicInteger counter2 = new AtomicInteger(0);
        final AtomicInteger counter3 = new AtomicInteger(0);

        // At least we need 2 listeners existing in the list to make sure
        // the iterator.next get called

        // This one will be added when listener2's onChange called
        final RealmChangeListener listener1 = new RealmChangeListener() {
            @Override
            public void onChange() {
                counter1.incrementAndGet();
            }
        };

        // This one will be existing in the list all the time
        final RealmChangeListener listener2 = new RealmChangeListener() {
            @Override
            public void onChange() {
                counter2.incrementAndGet();
                realm.addChangeListener(listener1);
            }
        };

        // This one will be removed after first transaction
        RealmChangeListener listener3 = new RealmChangeListener() {
            @Override
            public void onChange() {
                counter3.incrementAndGet();
                realm.removeChangeListener(this);
            }
        };

        realm = Realm.getInstance(context);
        realm.addChangeListener(listener2);
        realm.addChangeListener(listener3);

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        // [listener2, listener3]
        realm.commitTransaction();
        // after listener2.onChange
        // [listener2, listener3, listener1]
        // after listener3.onChange
        // [listener2, listener1]
        assertEquals(0, counter1.get());
        assertEquals(1, counter2.get());
        assertEquals(1, counter3.get());

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        // [listener2, listener1]
        realm.commitTransaction();
        // after listener2.onChange
        // Since duplicated entries will be ignored, we still have:
        // [listener2, listener1]

        assertEquals(1, counter1.get());
        assertEquals(2, counter2.get());
        assertEquals(1, counter3.get());
    }

    @Test
    @UiThreadTest
    public void testWeakReferenceListener() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        realm = Realm.getInstance(context);
        RealmChangeListener listener = new RealmChangeListener() {
            @Override
            public void onChange() {
                counter.incrementAndGet();
            }
        };
        realm.handlerController.addChangeListenerAsWeakReference(listener);

        // There is no guaranteed way to release the WeakReference,
        // just clear it.
        for (WeakReference<RealmChangeListener> weakRef : realm.handlerController.weakChangeListeners) {
            weakRef.clear();
        }

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        assertEquals(0, counter.get());
        assertEquals(0, realm.handlerController.weakChangeListeners.size());
    }


    // Test that that a WeakReferenceListener can be removed.
    // This test is not a proper GC test, but just ensures that listeners can be removed from the list of weak listeners
    // without throwing an exception.
    @Test
    @UiThreadTest
    public void testRemovingWeakReferenceListener() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        realm = Realm.getInstance(context);
        RealmChangeListener listenerA = new RealmChangeListener() {
            @Override
            public void onChange() {
                counter.incrementAndGet();
            }
        };
        RealmChangeListener listenerB = new RealmChangeListener() {
            @Override
            public void onChange() {
                counter.incrementAndGet();
            }
        };
        realm.handlerController.addChangeListenerAsWeakReference(listenerA);

        // There is no guaranteed way to release the WeakReference,
        // just clear it.
        for (WeakReference<RealmChangeListener> weakRef : realm.handlerController.weakChangeListeners) {
            weakRef.clear();
        }

        realm.handlerController.addChangeListenerAsWeakReference(listenerB);

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        assertEquals(1, counter.get());
        assertEquals(1, realm.handlerController.weakChangeListeners.size());
    }


    // Tests that if the same configuration is used on 2 different Looper threads that each gets its own Handler. This
    // prevents commitTransaction from accidentally posting messages to Handlers which might reference a closed Realm.
    @Test
    public void testDoNotUseClosedHandler() throws InterruptedException {
        final RealmConfiguration realmConfiguration = TestHelper.createConfiguration(context);
        final AssertionFailedError[] threadAssertionError = new AssertionFailedError[1]; // Keep track of errors in test threads.
        Realm.deleteRealm(realmConfiguration);

        final CountDownLatch handlerNotified = new CountDownLatch(1);
        final CountDownLatch backgroundThreadClosed = new CountDownLatch(1);

        // Create Handler on Thread1 by opening a Realm instance
        new Thread("thread1") {

            @Override
            public void run() {
                Looper.prepare();
                final Realm realm = Realm.getInstance(realmConfiguration);
                RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        realm.close();
                        handlerNotified.countDown();
                    }
                };
                realm.addChangeListener(listener);
                Looper.loop();
            }
        }.start();

        // Create Handler on Thread2 for the same Realm path and close the Realm instance again.
        new Thread("thread2") {
            @Override
            public void run() {
                Looper.prepare();
                Realm realm = Realm.getInstance(realmConfiguration);
                RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        try {
                            fail("This handler should not be notified");
                        } catch (AssertionFailedError e) {
                            threadAssertionError[0] = e;
                            handlerNotified.countDown(); // Make sure that that await() doesn't fail instead.
                        }
                    }
                };
                realm.addChangeListener(listener);
                realm.close();
                backgroundThreadClosed.countDown();
                Looper.loop();
            }

        }.start();

        // Any REALM_CHANGED message should now only reach the open Handler on Thread1
        backgroundThreadClosed.await();
        Realm realm = Realm.getInstance(realmConfiguration);
        realm.beginTransaction();
        realm.commitTransaction();
        try {
            if (!handlerNotified.await(5, TimeUnit.SECONDS)) {
                fail("Handler didn't receive message");
            }
        } finally {
            realm.close();
        }

        if (threadAssertionError[0] != null) {
            throw threadAssertionError[0];
        }
    }

    // Test that we handle a Looper thread quiting it's looper before it is done executing the current loop ( = Realm.close()
    // isn't called yet).
    @Test
    public void testLooperThreadQuitsLooperEarly() throws InterruptedException {
        RealmConfiguration config = TestHelper.createConfiguration(context);
        Realm.deleteRealm(config);

        final CountDownLatch backgroundLooperStartedAndStopped = new CountDownLatch(1);
        final CountDownLatch mainThreadCommitCompleted = new CountDownLatch(1);
        final CountDownLatch backgroundThreadStopped = new CountDownLatch(1);

        // Start background looper and let it hang
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare(); // Fake background thread with a looper, eg. a IntentService

                Realm realm = Realm.getInstance(context);
                realm.setAutoRefresh(false);
                Looper.myLooper().quit();
                backgroundLooperStartedAndStopped.countDown();
                try {
                    mainThreadCommitCompleted.await();
                } catch (InterruptedException e) {
                    fail("Thread interrupted"); // This will prevent backgroundThreadStopped from being called.
                }
                realm.close();
                backgroundThreadStopped.countDown();
            }
        });

        // Create a commit on another thread
        TestHelper.awaitOrFail(backgroundLooperStartedAndStopped);
        Realm realm = Realm.getInstance(config);
        Logger logger = TestHelper.getFailureLogger(Log.WARN);
        RealmLog.add(logger);

        realm.beginTransaction();
        realm.commitTransaction(); // If the Handler on the background is notified it will trigger a Log warning.
        mainThreadCommitCompleted.countDown();
        TestHelper.awaitOrFail(backgroundThreadStopped);

        realm.close();
        RealmLog.remove(logger);
    }

    @Test
    public void testHandlerThreadShouldReceiveNotification() throws ExecutionException, InterruptedException {
        final AssertionFailedError[] assertionFailedErrors = new AssertionFailedError[1];
        final CountDownLatch backgroundThreadReady = new CountDownLatch(1);
        final CountDownLatch numberOfInvocation = new CountDownLatch(1);

        HandlerThread handlerThread = new HandlerThread("handlerThread");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    assertEquals("handlerThread", Thread.currentThread().getName());
                } catch (AssertionFailedError e) {
                    assertionFailedErrors[0] = e;
                }
                final Realm backgroundRealm = Realm.getInstance(context);
                backgroundRealm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        backgroundRealm.close();
                        numberOfInvocation.countDown();
                    }
                });
                backgroundThreadReady.countDown();
            }
        });
        TestHelper.awaitOrFail(backgroundThreadReady);
        // At this point the background thread started & registered the listener

        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        TestHelper.awaitOrFail(numberOfInvocation);
        realm.close();
        handlerThread.quit();
        if (assertionFailedErrors[0] != null) {
            throw assertionFailedErrors[0];
        }
    }

    @Test
    public void testNonLooperThreadShouldNotifyLooperThreadAboutCommit() throws Throwable {
        final CountDownLatch mainThreadReady = new CountDownLatch(1);
        final CountDownLatch numberOfInvocation = new CountDownLatch(1);
        Thread thread = new Thread() {
            @Override
            public void run() {
                TestHelper.awaitOrFail(mainThreadReady);
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.createObject(AllTypes.class);
                realm.commitTransaction();
                realm.close();
            }
        };
        thread.start();

        HandlerThread mainThread = new HandlerThread("mainThread");
        mainThread.start();
        Handler handler = new Handler(mainThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final Realm mainRealm = Realm.getInstance(context);
                mainRealm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        mainRealm.close();
                        numberOfInvocation.countDown();
                    }
                });
                mainThreadReady.countDown();
            }
        });

        TestHelper.awaitOrFail(numberOfInvocation);
        mainThread.quit();
    }

    // The presence of async RealmResults block any `REALM_CHANGE` notification causing historically the Realm
    // to advance to the latest version. We make sure in this test that all Realm listeners will be notified
    // regardless of the presence of an async RealmResults that will delay the `REALM_CHANGE` sometimes
    @Test
    public void testAsyncRealmResultsShouldNotBlockBackgroundCommitNotification() throws Throwable {
        final AtomicInteger numberOfRealmCallbackInvocation = new AtomicInteger(0);
        final AtomicInteger numberOfAsyncRealmResultsCallbackInvocation = new AtomicInteger(0);
        final CountDownLatch signalTestFinished = new CountDownLatch(2);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
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
                    realm[0] = Realm.getInstance(context);
                    realm[0].addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            RealmResults<Dog> dogs = null; // to keep it as a strong reference
                            switch (numberOfRealmCallbackInvocation.incrementAndGet()) {
                                case 1: {
                                    // first commit
                                    dogs = realm[0].where(Dog.class).findAllAsync();
                                    assertTrue(dogs.load());
                                    dogs.addChangeListener(new RealmChangeListener() {
                                        @Override
                                        public void onChange() {
                                            numberOfAsyncRealmResultsCallbackInvocation.incrementAndGet();
                                        }
                                    });

                                    new Thread() {
                                        @Override
                                        public void run() {
                                            Realm realm = Realm.getInstance(context);
                                            realm.beginTransaction();
                                            realm.createObject(Dog.class);
                                            realm.commitTransaction();
                                            realm.close();
                                            signalTestFinished.countDown();
                                        }
                                    }.start();
                                    break;
                                }
                                case 2: {
                                    // finish test
                                    signalTestFinished.countDown();
                                    break;
                                }
                            }
                        }
                    });

                    realm[0].handler.post(new Runnable() {
                        @Override
                        public void run() {
                            realm[0].beginTransaction();
                            realm[0].createObject(Dog.class);
                            realm[0].commitTransaction();
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    threadAssertionError[0] = e;

                } finally {
                    if (signalTestFinished.getCount() > 0) {
                        signalTestFinished.countDown();
                    }
                    if (realm.length > 0 && realm[0] != null) {
                        realm[0].close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalTestFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }

    // The presence of async RealmResults block any `REALM_CHANGE` notification causing historically the Realm
    // to advance to the latest version. We make sure in this test that all Realm listeners will be notified
    // regardless of the presence of an async RealmObject that will delay the `REALM_CHANGE` sometimes
    @Test
    public void testAsyncRealmObjectShouldNotBlockBackgroundCommitNotification() throws Throwable {
        final AtomicInteger numberOfRealmCallbackInvocation = new AtomicInteger(0);
        final AtomicInteger numberOfAsyncRealmObjectCallbackInvocation = new AtomicInteger(0);
        final CountDownLatch signalTestFinished = new CountDownLatch(1);
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
                    realm[0] = Realm.getInstance(context);
                    realm[0].addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            Dog dog = null; // to keep it as a strong reference
                            switch (numberOfRealmCallbackInvocation.incrementAndGet()) {
                                case 1: {
                                    // first commit
                                    dog = realm[0].where(Dog.class).findFirstAsync();
                                    assertTrue(dog.load());
                                    dog.addChangeListener(new RealmChangeListener() {
                                        @Override
                                        public void onChange() {
                                            numberOfAsyncRealmObjectCallbackInvocation.incrementAndGet();
                                        }
                                    });

                                    new Thread() {
                                        @Override
                                        public void run() {
                                            Realm realm = Realm.getInstance(context);
                                            realm.beginTransaction();
                                            realm.createObject(Dog.class);
                                            realm.commitTransaction();
                                            realm.close();
                                        }
                                    }.start();
                                    break;
                                }
                                case 2: {
                                    // finish test
                                    signalTestFinished.countDown();
                                    break;
                                }
                            }
                        }
                    });

                    realm[0].handler.post(new Runnable() {
                        @Override
                        public void run() {
                            realm[0].beginTransaction();
                            realm[0].createObject(Dog.class);
                            realm[0].commitTransaction();
                        }
                    });

                    Looper.loop();

                } catch (Throwable e) {
                    threadAssertionError[0] = e;

                } finally {
                    if (signalTestFinished.getCount() > 0) {
                        signalTestFinished.countDown();
                    }
                    if (realm.length > 0 && realm[0] != null) {
                        realm[0].close();
                    }
                    signalClosedRealm.countDown();
                }
            }
        });

        TestHelper.exitOrThrow(executorService, signalTestFinished, signalClosedRealm, backgroundLooper, threadAssertionError);
    }
}
