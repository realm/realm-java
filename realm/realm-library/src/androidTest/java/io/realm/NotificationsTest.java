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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
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
import io.realm.log.LogLevel;
import io.realm.log.RealmLogger;
import io.realm.log.RealmLog;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class NotificationsTest {

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm realm;
    private RealmConfiguration realmConfig;

    @Before
    public void setUp() {
        realmConfig = configFactory.createConfiguration();
    }

    @After
    public void tearDown() {
        Realm.asyncTaskExecutor.resume();
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void failingSetAutoRefreshOnNonLooperThread() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Realm realm = Realm.getInstance(realmConfig);
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
        RealmCache.invokeWithGlobalRefCount(realmConfig, new TestHelper.ExpectedCountCallback(0));
    }

    @Test
    public void setAutoRefresh_onHandlerThread() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Looper.prepare();
                Realm realm = Realm.getInstance(realmConfig);
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
        RealmCache.invokeWithGlobalRefCount(realmConfig, new TestHelper.ExpectedCountCallback(0));
    }

    @Test
    @UiThreadTest
    public void removeChangeListener() throws InterruptedException, ExecutionException {
        final AtomicInteger counter = new AtomicInteger(0);
        RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                counter.incrementAndGet();
            }
        };

        realm = Realm.getInstance(realmConfig);
        realm.addChangeListener(listener);
        realm.removeChangeListener(listener);

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        assertEquals(0, counter.get());
    }

    @Test
    @RunTestInLooperThread
    public void addChangeListener_duplicatedListener() {
        final AtomicInteger counter = new AtomicInteger(0);
        RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                counter.incrementAndGet();
            }
        };

        Realm realm = looperThread.realm;
        realm.addChangeListener(listener);
        realm.addChangeListener(listener);
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                assertEquals(1, counter.get());
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    public void notificationsNumber() throws InterruptedException, ExecutionException {
        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicBoolean isReady = new AtomicBoolean(false);
        final Looper[] looper = new Looper[1];
        final AtomicBoolean isRealmOpen = new AtomicBoolean(true);
        final RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
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
                    realm = Realm.getInstance(realmConfig);
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
        realm = Realm.getInstance(realmConfig);
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
        RealmCache.invokeWithGlobalRefCount(realmConfig, new TestHelper.ExpectedCountCallback(0));
    }

    @Test
    public void closeClearingHandlerMessages() throws InterruptedException, TimeoutException, ExecutionException {
        final int TEST_SIZE = 10;
        final CountDownLatch backgroundLooperStarted = new CountDownLatch(1);
        final CountDownLatch addHandlerMessages = new CountDownLatch(1);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Looper.prepare(); // Fake background thread with a looper, eg. a IntentService
                Realm realm = Realm.getInstance(realmConfig);
                backgroundLooperStarted.countDown();

                // Random operation in the client code
                final RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
                if (dogs.size() != 0) {
                    return false;
                }
                addHandlerMessages.await(1, TimeUnit.SECONDS); // Wait for main thread to add update messages

                // Create a Handler for the thread now. All message and references for the notification handler will be
                // cleared once we call close().
                Handler threadHandler = new Handler(Looper.myLooper());
                realm.close(); // Close native resources + associated handlers.

                // Looper now reads the update message from the main thread if the Handler was not
                // cleared. This will cause an IllegalStateException and should not happen.
                // If it works correctly. The looper will just block on an empty message queue.
                // This is normal behavior but is bad for testing, so we add a custom quit message
                // at the end so we can evaluate results faster.
                // 500 ms delay is to make sure the notification daemon thread gets time to send notification.
                threadHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TestHelper.quitLooperOrFail();
                    }
                }, 500);

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
        Realm realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            Dog dog = realm.createObject(Dog.class);
            dog.setName("Rex " + i);
        }
        realm.commitTransaction();
        assertEquals(TEST_SIZE, realm.where(Dog.class).count());
        realm.close();
        addHandlerMessages.countDown();

        // Check that messages was properly cleared
        // It looks like getting this future sometimes takes a while for some reason. Setting to
        // 10s. now.
        Boolean result = future.get(10, TimeUnit.SECONDS);
        assertTrue(result);
    }

    @Test
    @RunTestInLooperThread
    public void commitTransaction_delayChangeListenerOnSameThread() {
        final AtomicInteger success = new AtomicInteger(0);
        Realm realm = looperThread.realm;
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                assertEquals(1, success.get());
                looperThread.testComplete();
            }
        });
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
        assertEquals(0, success.getAndIncrement());
    }

    @Test
    @RunTestInLooperThread
    public void emptyCommitTriggerChangeListener() {
        final RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                looperThread.testComplete();
            }
        };
        Realm realm = looperThread.realm;
        realm.addChangeListener(listener);
        realm.beginTransaction();
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void addRemoveListenerConcurrency() {
        final Realm realm = looperThread.realm;
        final AtomicInteger counter1 = new AtomicInteger(0);
        final AtomicInteger counter2 = new AtomicInteger(0);
        final AtomicInteger counter3 = new AtomicInteger(0);

        // At least we need 2 listeners existing in the list to make sure
        // the iterator.next get called

        // This one will be added when listener2's onChange called
        final RealmChangeListener<Realm> listener1 = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                // Step 7: Last listener called. Should only be called once
                counter1.incrementAndGet();

                // after listener2.onChange
                // Since duplicated entries will be ignored, we still have:
                // [listener2, listener1]
                assertEquals(1, counter1.get());
                assertEquals(2, counter2.get());
                assertEquals(1, counter3.get());
                looperThread.testComplete();
            }
        };

        // This one will be existing in the list all the time
        final RealmChangeListener<Realm> listener2 = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                // Step 3: Listener2 called
                // Listener state [listener2, listener3, listener1]
                // Listener 1 will not be called this time around
                counter2.incrementAndGet();
                realm.addChangeListener(listener1);
            }
        };

        // This one will be removed after first transaction
        RealmChangeListener<Realm> listener3 = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                // Step 4: Listener3 called
                // Listener state [listener2, listener1]
                counter3.incrementAndGet();
                realm.removeChangeListener(this);

                // Step 5: Assert proper state
                // [listener2, listener1]
                assertEquals(0, counter1.get());
                assertEquals(1, counter2.get());
                assertEquals(1, counter3.get());

                // Step 6: Trigger next round of changes on [listener2, listener1]
                realm.beginTransaction();
                realm.createObject(AllTypes.class);
                realm.commitTransaction();
            }
        };

        // Step 1: Add initial listeners
        // Listener state [listener2, listener3]
        realm.addChangeListener(listener2);
        realm.addChangeListener(listener3);

        // Step 2: Trigger change listeners
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void weakReferenceListener() throws InterruptedException {
        final AtomicInteger weakCounter = new AtomicInteger(0);
        final AtomicInteger strongCounter = new AtomicInteger(0);

        final Realm realm = looperThread.realm;

        // Setup weak listener
        RealmChangeListener<Realm> weakListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                weakCounter.incrementAndGet();
            }
        };
        realm.handlerController.addChangeListenerAsWeakReference(weakListener);
        assertEquals(1, realm.handlerController.weakChangeListeners.size());

        // This is not a weak listener so will be called. When this is triggered the weak references have not been
        // removed yet. So make another change to ensure that they really are removed before validating.
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                int count = strongCounter.incrementAndGet();
                if (count == 1) {
                    realm.beginTransaction();
                    realm.createObject(AllTypes.class);
                    realm.commitTransaction();
                } else if (count == 2) {
                    assertEquals(0, weakCounter.get());
                    assertEquals(0, realm.handlerController.weakChangeListeners.size());
                    looperThread.testComplete();
                }
            }
        });

        // Hack: There is no guaranteed way to release the WeakReference, just clear it.
        for (WeakReference<RealmChangeListener<? extends BaseRealm>> weakRef : realm.handlerController.weakChangeListeners) {
            weakRef.clear();
        }

        // Trigger change listeners
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }


    // Test that that a WeakReferenceListener can be removed.
    // This test is not a proper GC test, but just ensures that listeners can be removed from the list of weak listeners
    // without throwing an exception.
    @Test
    @RunTestInLooperThread
    public void removingWeakReferenceListener() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        final Realm realm = looperThread.realm;
        RealmChangeListener<Realm> listenerA = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                counter.incrementAndGet();
            }
        };
        RealmChangeListener<Realm> listenerB = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                assertEquals(0, counter.get());
                assertEquals(1, realm.handlerController.weakChangeListeners.size());
                looperThread.testComplete();
            }
        };
        realm.handlerController.addChangeListenerAsWeakReference(listenerA);

        // There is no guaranteed way to release the WeakReference,
        // just clear it.
        for (WeakReference<RealmChangeListener<? extends BaseRealm>> weakRef : realm.handlerController.weakChangeListeners) {
            weakRef.clear();
        }

        realm.handlerController.addChangeListenerAsWeakReference(listenerB);

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void realmNotificationOrder() {
        // Tests that global notifications are called in the order they are added
        // Test both ways to check accidental ordering from unordered collections.
        final AtomicInteger listenerACalled = new AtomicInteger(0);
        final AtomicInteger listenerBCalled = new AtomicInteger(0);
        final Realm realm = looperThread.realm;

        final RealmChangeListener<Realm> listenerA = new RealmChangeListener<Realm>() {

            @Override
            public void onChange(Realm object) {
                int called = listenerACalled.incrementAndGet();
                if (called == 2) {
                    assertEquals(2, listenerBCalled.get());
                    looperThread.testComplete();
                }
            }
        };
        final RealmChangeListener<Realm> listenerB = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                listenerBCalled.incrementAndGet();
                if (listenerACalled.get() == 1) {
                    // 2. Reverse order
                    realm.removeAllChangeListeners();
                    realm.addChangeListener(this);
                    realm.addChangeListener(listenerA);
                    realm.beginTransaction();
                    realm.commitTransaction();

                }
            }
        };

        // 1. Add initial ordering
        realm.addChangeListener(listenerA);
        realm.addChangeListener(listenerB);

        realm.beginTransaction();
        realm.commitTransaction();
    }

    // Tests that if the same configuration is used on 2 different Looper threads that each gets its own Handler. This
    // prevents commitTransaction from accidentally posting messages to Handlers which might reference a closed Realm.
    @Test
    public void doNotUseClosedHandler() throws InterruptedException {
        final CountDownLatch handlerNotified = new CountDownLatch(1);
        final CountDownLatch backgroundThread1Started = new CountDownLatch(1);
        final CountDownLatch backgroundThread2Closed = new CountDownLatch(1);

        // Create Handler on Thread1 by opening a Realm instance
        new Thread("thread1") {

            @Override
            public void run() {
                Looper.prepare();
                final Realm realm = Realm.getInstance(realmConfig);
                RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
                    @Override
                    public void onChange(Realm object) {
                        realm.close();
                        handlerNotified.countDown();
                    }
                };
                realm.addChangeListener(listener);
                backgroundThread1Started.countDown();
                Looper.loop();
            }
        }.start();

        // Create Handler on Thread2 for the same Realm path and close the Realm instance again.
        new Thread("thread2") {
            @Override
            public void run() {
                Looper.prepare();
                Realm realm = Realm.getInstance(realmConfig);
                RealmChangeListener<Realm> listener = new RealmChangeListener<Realm>() {
                    @Override
                    public void onChange(Realm object) {
                        fail("This handler should not be notified");
                    }
                };
                realm.addChangeListener(listener);
                realm.close();
                backgroundThread2Closed.countDown();
                Looper.loop();
            }

        }.start();

        TestHelper.awaitOrFail(backgroundThread1Started);
        TestHelper.awaitOrFail(backgroundThread2Closed);
        Realm realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        realm.commitTransaction();
        // Any REALM_CHANGED message should now only reach the open Handler on Thread1
        try {
            // TODO: Waiting 5 seconds is not a reliable condition. Figure out a better way for this.
            if (!handlerNotified.await(5, TimeUnit.SECONDS)) {
                fail("Handler didn't receive message");
            }
        } finally {
            realm.close();
        }
    }

    // Test that we handle a Looper thread quiting it's looper before it is done executing the current loop ( = Realm.close()
    // isn't called yet).
    @Test
    public void looperThreadQuitsLooperEarly() throws InterruptedException {
        final CountDownLatch backgroundLooperStartedAndStopped = new CountDownLatch(1);
        final CountDownLatch mainThreadCommitCompleted = new CountDownLatch(1);
        final CountDownLatch backgroundThreadStopped = new CountDownLatch(1);

        // Start background looper and let it hang
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare(); // Fake background thread with a looper, eg. a IntentService

                Realm realm = Realm.getInstance(realmConfig);
                realm.setAutoRefresh(false);
                TestHelper.quitLooperOrFail();
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
        Realm realm = Realm.getInstance(realmConfig);
        RealmLogger logger = TestHelper.getFailureLogger(Log.WARN);
        RealmLog.add(logger);

        realm.beginTransaction();
        realm.commitTransaction(); // If the Handler on the background is notified it will trigger a Log warning.
        mainThreadCommitCompleted.countDown();
        TestHelper.awaitOrFail(backgroundThreadStopped);

        realm.close();
        RealmLog.remove(logger);
    }

    @Test
    public void handlerThreadShouldReceiveNotification() throws ExecutionException, InterruptedException {
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
                final Realm backgroundRealm = Realm.getInstance(realmConfig);
                backgroundRealm.addChangeListener(new RealmChangeListener<Realm>() {
                    @Override
                    public void onChange(Realm object) {
                        backgroundRealm.close();
                        numberOfInvocation.countDown();
                    }
                });
                backgroundThreadReady.countDown();
            }
        });
        TestHelper.awaitOrFail(backgroundThreadReady);
        // At this point the background thread started & registered the listener

        Realm realm = Realm.getInstance(realmConfig);
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
    public void nonLooperThreadShouldNotifyLooperThreadAboutCommit() {
        final CountDownLatch mainThreadReady = new CountDownLatch(1);
        final CountDownLatch backgroundThreadClosed = new CountDownLatch(1);
        final CountDownLatch numberOfInvocation = new CountDownLatch(1);
        Thread thread = new Thread() {
            @Override
            public void run() {
                TestHelper.awaitOrFail(mainThreadReady);
                Realm realm = Realm.getInstance(realmConfig);
                realm.beginTransaction();
                realm.createObject(AllTypes.class);
                realm.commitTransaction();
                realm.close();
                backgroundThreadClosed.countDown();
            }
        };
        thread.start();

        HandlerThread mainThread = new HandlerThread("mainThread");
        mainThread.start();
        Handler handler = new Handler(mainThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final Realm mainRealm = Realm.getInstance(realmConfig);
                mainRealm.addChangeListener(new RealmChangeListener<Realm>() {
                    @Override
                    public void onChange(Realm object) {
                        mainRealm.close();
                        numberOfInvocation.countDown();
                    }
                });
                mainThreadReady.countDown();
            }
        });

        TestHelper.awaitOrFail(numberOfInvocation);
        TestHelper.awaitOrFail(backgroundThreadClosed);
        mainThread.quit();
    }

    // The presence of async RealmResults block any `REALM_CHANGE` notification causing historically the Realm
    // to advance to the latest version. We make sure in this test that all Realm listeners will be notified
    // regardless of the presence of an async RealmResults that will delay the `REALM_CHANGE` sometimes
    @Test
    @RunTestInLooperThread
    public void asyncRealmResultsShouldNotBlockBackgroundCommitNotification() {
        final Realm realm = looperThread.realm;
        final RealmResults<Dog> dogs = realm.where(Dog.class).findAllAsync();
        final AtomicBoolean resultsListenerDone = new AtomicBoolean(false);
        final AtomicBoolean realmListenerDone = new AtomicBoolean(false);

        looperThread.keepStrongReference.add(dogs);
        assertTrue(dogs.load());
        assertEquals(0, dogs.size());
        dogs.addChangeListener(new RealmChangeListener<RealmResults<Dog>>() {
            @Override
            public void onChange(RealmResults<Dog> results) {
                if (dogs.size() == 2) {
                    // Results has the latest changes
                    resultsListenerDone.set(true);
                    if (realmListenerDone.get()) {
                        looperThread.testComplete();
                    }
                }
            }
        });

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                if (dogs.size() == 1) {
                    // Step 2. Create the second dog
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.createObject(Dog.class);
                        }
                    });
                } else if (dogs.size() == 2) {
                    // Realm listener can see the latest changes
                    realmListenerDone.set(true);
                    if (resultsListenerDone.get()) {
                        looperThread.testComplete();
                    }
                }
            }
        });

        // Step 1. Create the first dog
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(Dog.class);
            }
        });
    }

    // The presence of async RealmResults blocks any `REALM_CHANGE` notification . We make sure in this test that all
    // Realm listeners will be notified regardless of the presence of an async RealmObject. RealmObjects are special
    // in the sense that once you got a row accessor to that object, it is automatically up to date as soon as you
    // call advance_read().
    @Test
    @RunTestInLooperThread
    public void asyncRealmObjectShouldNotBlockBackgroundCommitNotification() {
        final AtomicInteger numberOfRealmCallbackInvocation = new AtomicInteger(0);
        final CountDownLatch signalClosedRealm = new CountDownLatch(1);
        final Realm realm = looperThread.realm;
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(final Realm realm) {
                switch (numberOfRealmCallbackInvocation.incrementAndGet()) {
                    case 1: {
                        // first commit
                        Dog dog = realm.where(Dog.class).findFirstAsync();
                        assertTrue(dog.load());
                        dog.addChangeListener(new RealmChangeListener<Dog>() {
                            @Override
                            public void onChange(Dog dog) {
                            }
                        });

                        new Thread() {
                            @Override
                            public void run() {
                                Realm threadRealm = Realm.getInstance(realm.getConfiguration());
                                threadRealm.beginTransaction();
                                threadRealm.createObject(Dog.class);
                                threadRealm.commitTransaction();
                                threadRealm.close();
                                signalClosedRealm.countDown();
                            }
                        }.start();
                        break;
                    }
                    case 2: {
                        // finish test
                        TestHelper.awaitOrFail(signalClosedRealm);
                        looperThread.testComplete();
                        break;
                    }
                }
            }
        });

        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                realm.beginTransaction();
                realm.createObject(Dog.class);
                realm.commitTransaction();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void realmListenerAddedAfterCommit() {
        Realm realm = looperThread.realm;
        realm.beginTransaction();
        realm.commitTransaction();

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm object) {
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void realmResultsListenerAddedAfterCommit() {
        Realm realm = looperThread.realm;
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        looperThread.keepStrongReference.add(results);
        results.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> object) {
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void realmObjectListenerAddedAfterCommit() {
        Realm realm = looperThread.realm;
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        realm.beginTransaction();
        obj.setColumnLong(42);
        realm.commitTransaction();

        obj.addChangeListener(new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes object) {
                looperThread.testComplete();
            }
        });
    }

    public static class PopulateOneAllTypes implements RunInLooperThread.RunnableBefore {

        @Override
        public void run(RealmConfiguration realmConfig) {
            Realm realm = Realm.getInstance(realmConfig);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.createObject(AllTypes.class);
                }
            });
            realm.close();
        }
    }

    @Test
    @RunTestInLooperThread(before = PopulateOneAllTypes.class)
    public void realmListener_realmResultShouldBeSynced() {
        final Realm realm = looperThread.realm;
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        assertEquals(1, results.size());

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AllTypes allTypes = realm.where(AllTypes.class).findFirst();
                assertNotNull(allTypes);
                allTypes.deleteFromRealm();
                assertEquals(0, realm.where(AllTypes.class).count());
            }
        });

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                // Change event triggered by deletion in async transaction.
                assertEquals(0, realm.where(AllTypes.class).count());
                assertEquals(0, results.size());
                looperThread.testComplete();
            }
        });
    }

    // FIXME check if the SharedRealm Changed in handleAsyncTransactionCompleted and reenable this test.
    // We precisely depend on the order of triggering change listeners right now.
    // So it should be:
    // 1. Synced object listener
    // 2. Synced results listener
    // 3. Global listener
    // Async listeners are not concerned by this test. Since they are triggered by different event and no advance read
    // involved.
    // If this case fails on your code, think twice before changing the test!
    // https://github.com/realm/realm-java/issues/2408 is related to this test!
    @Test
    @Ignore("Listener on Realm might be trigger more times, ignore for now")
    @RunTestInLooperThread
    public void callingOrdersOfListeners() {
        final Realm realm = looperThread.realm;
        final AtomicInteger count = new AtomicInteger(0);

        final RealmChangeListener<RealmResults<AllTypes>> syncedResultsListener =
                new RealmChangeListener<RealmResults<AllTypes>>() {
                    @Override
                    public void onChange(RealmResults<AllTypes> element) {
                        // First called
                        assertEquals(0, count.getAndIncrement());
                    }
                };

        final RealmChangeListener<AllTypes> syncedObjectListener = new RealmChangeListener<AllTypes>() {
            @Override
            public void onChange(AllTypes element) {
                // Second called
                assertEquals(1, count.getAndIncrement());
            }
        };
        final RealmChangeListener<Realm> globalListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                // third called
                assertEquals(2, count.getAndIncrement());
                looperThread.testComplete();
            }
        };


        realm.beginTransaction();
        final AllTypes allTypes = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        // We need to create one objects first and let the pass the first change event
        final RealmChangeListener<Realm> initListener = new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                looperThread.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        // Clear the change listeners
                        realm.removeAllChangeListeners();

                        // Now we can start testing
                        allTypes.addChangeListener(syncedObjectListener);
                        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
                        results.addChangeListener(syncedResultsListener);
                        realm.addChangeListener(globalListener);

                        // Now we trigger those listeners
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                AllTypes allTypes = realm.where(AllTypes.class).findFirst();
                                assertNotNull(allTypes);
                                allTypes.setColumnLong(42);
                            }
                        });
                    }
                });
            }
        };
        realm.addChangeListener(initListener);
    }

    // See https://github.com/realm/realm-android-adapters/issues/48
    // Step 1: Populate the db
    // Step 2: Post a runnable to caller thread.
    //         Event Queue: |Posted Runnable| <- TOP
    // Step 3: Delete object which will make the results contain an invalid object at this moment
    //         Right Event Queue: |LOCAL_COMMIT   |   Wrong Event Queue: |Posted Runnable           |  <- TOP
    //                            |Posted Runnable|                      |REALM_CHANGED/LOCAL_COMMIT|
    // Step 4: Posted runnable called.
    @Test
    @RunTestInLooperThread(/*step1*/ before = PopulateOneAllTypes.class)
    public void realmListener_localChangeShouldBeSendAtFrontOfTheQueue() {
        final Realm realm = looperThread.realm;
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        assertEquals(1, results.size());

        // Step 2
        // The transaction later will trigger the results sync, and it should be run before this runnable.
        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                // Step 4
                assertEquals(0, results.size());
                realm.close();
                looperThread.testComplete();
            }
        });

        // Step 3
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AllTypes allTypes = realm.where(AllTypes.class).findFirst();
                assertNotNull(allTypes);
                allTypes.deleteFromRealm();
                assertEquals(0, realm.where(AllTypes.class).count());
                assertFalse(results.get(0).isValid());
            }
        });
    }

    // See https://github.com/realm/realm-android-adapters/issues/48
    // Step 1: Populate the db
    // Step 2: Create a async query, and wait until it finishes
    // Step 3: Post a runnable to caller thread.
    //         Event Queue: |Posted Runnable| <- TOP
    // Step 4: Delete object which will make the results contain a invalid object at this moment
    //         Right Event Queue: |LOCAL_COMMIT   |   Wrong Event Queue: |Posted Runnable           |  <- TOP
    //                            |Posted Runnable|                      |REALM_CHANGED/LOCAL_COMMIT|
    // Step 5: Posted runnable called.
    @Test
    @RunTestInLooperThread(/*step1*/before = PopulateOneAllTypes.class)
    public void realmListener_localChangeShouldBeSendAtFrontOfTheQueueWithLoadedAsync() {
        final AtomicBoolean changedFirstTime = new AtomicBoolean(false);
        final Realm realm = looperThread.realm;
        final RealmResults<AllTypes> asyncResults = realm.where(AllTypes.class).findAllAsync();
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();

        assertEquals(1, results.size());

        looperThread.keepStrongReference.add(asyncResults);
        asyncResults.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> element) {
                if (!changedFirstTime.get()) {
                    // Step 2
                    // The transaction later will trigger the results sync, and it should be run before this runnable.
                    looperThread.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            // Step 5
                            assertEquals(0, asyncResults.size());
                            assertEquals(0, results.size());
                            looperThread.testComplete();
                        }
                    });

                    // Step 3
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            AllTypes allTypes = realm.where(AllTypes.class).findFirst();
                            assertNotNull(allTypes);
                            allTypes.deleteFromRealm();
                            assertEquals(0, realm.where(AllTypes.class).count());
                            assertFalse(results.get(0).isValid());
                        }
                    });
                    changedFirstTime.set(true);
                }
            }
        });
    }

    // See https://github.com/realm/realm-android-adapters/issues/48
    // Step 1: Populate the db
    // Step 2: Create a async query, and pause it
    // Step 3: Post a runnable to caller thread.
    //         Event Queue: |Posted Runnable| <- TOP
    // Step 4: Delete object which will make the results contain a invalid object at this moment
    //         Right Event Queue: |LOCAL_COMMIT   |   Wrong Event Queue: |Posted Runnable           |  <- TOP
    //                            |Posted Runnable|                      |REALM_CHANGED/LOCAL_COMMIT|
    // Step 5: Posted runnable called.
    //
    @Test
    @RunTestInLooperThread(/*step1*/before = PopulateOneAllTypes.class)
    public void realmListener_localChangeShouldBeSendAtFrontOfTheQueueWithPausedAsync() {
        final Realm realm = looperThread.realm;

        Realm.asyncTaskExecutor.pause();
        final RealmResults<AllTypes> asyncResults = realm.where(AllTypes.class).findAllAsync();
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();

        assertEquals(1, results.size());

        // Step 2
        // The transaction later will trigger the results sync, and it should be run before this runnable.
        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                // Step 5
                assertFalse(asyncResults.isLoaded());
                assertEquals(0, results.size());
                looperThread.testComplete();
            }
        });

        // Step 3
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AllTypes allTypes = realm.where(AllTypes.class).findFirst();
                assertNotNull(allTypes);
                allTypes.deleteFromRealm();
                assertEquals(0, realm.where(AllTypes.class).count());
                assertFalse(results.get(0).isValid());
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void warnIfMixingSyncWritesAndAsyncQueries() {
        final Realm realm = looperThread.realm;
        final AtomicBoolean warningLogged = new AtomicBoolean(false);
        final TestHelper.TestLogger testLogger = new TestHelper.TestLogger() {
            @Override
            public void log(int level, String tag, Throwable throwable, String message) {
                assertTrue(message.contains("Mixing asynchronous queries with local writes should be avoided."));
                if (level == LogLevel.WARN) {
                    warningLogged.set(true);
                }
            }
        };
        RealmLog.add(testLogger);

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
        looperThread.keepStrongReference.add(results);
        results.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> element) {
                RealmLog.remove(testLogger);
                assertTrue(warningLogged.get());
                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void accessingSyncRealmResultInsideAsyncResultListener() {
        final Realm realm = looperThread.realm;
        final AtomicInteger asyncResultCallback = new AtomicInteger(0);

        final RealmResults<AllTypes> syncResults = realm.where(AllTypes.class).findAll();

        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
        looperThread.keepStrongReference.add(results);
        results.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> results) {
                switch (asyncResultCallback.incrementAndGet()) {
                    case 1:
                        // Called when first async query completes
                        assertEquals(0, results.size());
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.createObject(AllTypes.class);
                            }
                        });
                        break;

                    case 2:
                        // Called after async transaction completes, A REALM_CHANGED event has been triggered,
                        // async queries have rerun, and listeners are triggered again
                        assertEquals(1, results.size());
                        assertEquals(1, syncResults.size()); // If syncResults is not in sync yet, this will fail.
                        looperThread.testComplete();
                        break;
                }
            }
        });
    }

    // If RealmResults are updated just before their change listener are notified, one change listener might
    // reference another RealmResults that have been advance_read, but not yet called sync_if_needed.
    // This can result in accessing detached rows and other errors.
    @Test
    @RunTestInLooperThread
    public void accessingSyncRealmResultsInsideAnotherResultListener() {
        final Realm realm = looperThread.realm;
        final RealmResults<AllTypes> syncResults1 = realm.where(AllTypes.class).findAll();
        final RealmResults<AllTypes> syncResults2 = realm.where(AllTypes.class).findAll();

        looperThread.keepStrongReference.add(syncResults1);
        syncResults1.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
            @Override
            public void onChange(RealmResults<AllTypes> element) {
                assertEquals(1, syncResults1.size());
                assertEquals(1, syncResults2.size()); // If syncResults2 is not in sync yet, this will fail.
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread(threadName = "IntentService[1]")
    public void listenersNotAllowedOnIntentServiceThreads() {
        final Realm realm = looperThread.realm;
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();

        // Global listener
        try {
            realm.addChangeListener(new RealmChangeListener<Realm>() {
                @Override
                public void onChange(Realm element) {

                }
            });
            fail();
        } catch (IllegalStateException ignored) {
        }

        // RealmResults listener
        try {
            results.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
                @Override
                public void onChange(RealmResults<AllTypes> element) {

                }
            });
            fail();
        } catch (IllegalStateException ignored) {
        }

        // Object listener
        try {
            obj.addChangeListener(new RealmChangeListener<RealmModel>() {
                @Override
                public void onChange(RealmModel element) {

                }
            });
            fail();
        } catch (IllegalStateException ignored) {
        }

        looperThread.testComplete();
    }

    @Test
    public void listenersNotAllowedOnNonLooperThreads() {
        realm = Realm.getInstance(realmConfig);
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();

        // Global listener
        try {
            realm.addChangeListener(new RealmChangeListener<Realm>() {
                @Override
                public void onChange(Realm element) {

                }
            });
            fail();
        } catch (IllegalStateException ignored) {
        }

        // RealmResults listener
        try {
            results.addChangeListener(new RealmChangeListener<RealmResults<AllTypes>>() {
                @Override
                public void onChange(RealmResults<AllTypes> element) {

                }
            });
            fail();
        } catch (IllegalStateException ignored) {
        }

        // Object listener
        try {
            obj.addChangeListener(new RealmChangeListener<RealmModel>() {
                @Override
                public void onChange(RealmModel element) {

                }
            });
            fail();
        } catch (IllegalStateException ignored) {
        }
    }
}
