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

import java.lang.Thread;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.*;
import io.realm.exceptions.RealmException;
import io.realm.internal.SharedGroup;
import io.realm.internal.async.RetryPolicy;
import io.realm.proxy.HandlerProxy;

public class RealmAsyncQueryTests extends InstrumentationTestCase {
    private final static int NO_RETRY = 0;
    private static final int RETRY_ONCE = 1;
    private static final int RETRY_TWICE = 2;
    private static final int RETRY_NUMBER_NOT_APPLICABLE = Integer.MAX_VALUE;

    private final static int NO_ADVANCED_READ = 0;
    private static final int ADVANCE_ONE_READ = 1;
    private static final int ADVANCE_THREE_READ = 3;
    private static final int ADVANCE_HUNDRED_READ = 100;

    // async query without any conflicts strategy
    public void _testFindAll() throws Throwable {
        setDebugModeForAsyncRealmQuery(NO_ADVANCED_READ, RetryPolicy.MODE_NO_RETRY, NO_RETRY);
        // we need to control precisely which Looper/Thread our Realm
        // will operate on. This is unfortunately not possible when using the
        // current Instrumentation#InstrumentationThread, because InstrumentationTestRunner#onStart
        // Call Looper.prepare() for us and surprisingly doesn't call Looper#loop(), this is problematic
        // as the async query callback will not run (because the Handler is sending Runnables to a Looper
        // that didn't loop.
        //
        // On the other hand, using a dedicated 'ExecutorService' will allow us to fine grain control the
        // desired behaviour
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_all");
                    populateTestRealm(realm, 10);

                    // async query (will run on different thread)
                    realm.where(AllTypes.class)
                            .between("columnLong", 0, 9)
                            .findAll(new RealmResults.QueryCallback<AllTypes>() {
                                @Override
                                public void onSuccess(RealmResults<AllTypes> results) {
                                    try {
                                        assertEquals(10, results.size());

                                        // make sure access to RealmObject will not throw an Exception
                                        for (int i = 0, size = results.size(); i < size; i++) {
                                            assertEquals(i, results.get(i).getColumnLong());
                                        }

                                    } catch (AssertionFailedError e) {
                                        threadAssertionError[0] = e;

                                    } finally {
                                        // whatever happened, make sure to notify the waiting TestCase Thread
                                        signalCallbackFinished.countDown();
                                    }
                                }

                                @Override
                                public void onError(Exception t) {
                                    try {
                                        threadAssertionError[0] = t;
                                        t.printStackTrace();
                                    } finally {
                                        signalCallbackFinished.countDown();
                                    }
                                }
                            });

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
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
        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    // async query on closed Realm
    public void _testFindAllOnClosedRealm() throws Throwable {
        setDebugModeForAsyncRealmQuery(ADVANCE_ONE_READ, RetryPolicy.MODE_INDEFINITELY, RETRY_NUMBER_NOT_APPLICABLE);
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_all_closed_realm");
                    populateTestRealm(realm, 10);

                    // async query (will run on different thread)
                    realm.where(AllTypes.class)
                            .between("columnLong", 0, 9)
                            .findAll(new RealmResults.DebugRealmResultsQueryCallback<AllTypes>() {
                                @Override
                                public void onSuccess(RealmResults<AllTypes> results) {
                                    signalCallbackFinished.countDown();
                                }

                                @Override
                                public void onError(Exception t) {
                                    t.printStackTrace();
                                    threadAssertionError[0] = t;
                                    signalCallbackFinished.countDown();
                                }

                                @Override
                                public void onBackgroundQueryCompleted(Realm realm) {
                                    realm.close();
                                }
                            });

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }
                }
            }
        });

        // wait until the callback of our async query proceed
        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null == threadAssertionError[0] || !(threadAssertionError[0] instanceof RuntimeException)) {
            fail("Expecting RuntimeException: Unspecified exception. Detached accessor in io_realm_internal_TableQuery.cpp");
        }
    }

    // async query that should fail, because the caller thread has advanced
    // the version of the Realm, which is different from the one used by
    // the background thread. Since no retry policy was defined, we should fail.
    public void _testMismatchedRealmVersion_should_fail_no_retry() throws Throwable {
        // simulate one advanced read just after the worker thread has finished querying the Realm
        // without any retry we should crash, because the background Realm used to perform the query
        // return a TableView using a different version of the caller Realm (now more advanced)
        setDebugModeForAsyncRealmQuery(ADVANCE_ONE_READ, RetryPolicy.MODE_NO_RETRY, NO_RETRY);

        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_should_fail_no_retry");
                    populateTestRealm(realm, 10);

                    realm.where(AllTypes.class)
                            .between("columnLong", 0, 9)
                            .findAll(new RealmResults.DebugRealmResultsQueryCallback<AllTypes>() {
                                @Override
                                public void onSuccess(RealmResults<AllTypes> results) {
                                    signalCallbackFinished.countDown();
                                }

                                @Override
                                public void onError(Exception t) {
                                    threadAssertionError[0] = t;
                                    signalCallbackFinished.countDown();
                                }

                                @Override
                                public void onBackgroundQueryCompleted(Realm realm) {
                                    // triggered on the background thread to alter the caller's Realm state
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.clear(AllTypes.class);
                                        }
                                    });
                                }
                            });

                    Looper.loop();

                } catch (Exception e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;
                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        // opening Realm crashed, not even callbacks get the chance to be called
                        signalCallbackFinished.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                }
            }
        });

        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null == threadAssertionError[0] || !(threadAssertionError[0] instanceof RealmException)) {
            fail("Expecting RuntimeException: Unspecified exception." +
                    " Handover failed due to version mismatch in io_realm_internal_TableQuery.cpp");
        }
    }

    // async query that converge after 1 retry
    public void _testMismatchedRealmVersion_should_converge_after_1_retry() throws Throwable {
        // simulate one advanced read just after the worker thread has finished querying the Realm
        setDebugModeForAsyncRealmQuery(ADVANCE_ONE_READ, RetryPolicy.MODE_MAX_RETRY, RETRY_ONCE);

        final CountDownLatch signalCallbackFinishedLatch = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("should_converge_after_1_retry");
                    populateTestRealm(realm, 10);
                    realm.where(AllTypes.class)
                            .between("columnLong", 0, 9)
                            .findAll(new RealmResults.DebugRealmResultsQueryCallback<AllTypes>() {
                                @Override
                                public void onSuccess(RealmResults<AllTypes> results) {
                                    try {
                                        assertEquals(3, results.size());

                                        for (int i = 0, size = results.size(); i < size; i++) {
                                            assertEquals(i, results.get(i).getColumnLong());
                                        }

                                    } catch (AssertionFailedError e) {
                                        threadAssertionError[0] = e;

                                    } finally {
                                        signalCallbackFinishedLatch.countDown();
                                    }
                                }

                                @Override
                                public void onError(Exception t) {
                                    try {
                                        threadAssertionError[0] = t;
                                    } finally {
                                        signalCallbackFinishedLatch.countDown();
                                    }
                                }

                                @Override
                                public void onBackgroundQueryCompleted(Realm realm) {
                                    populateTestRealm(realm, 3);// this is already inside a transaction
                                }
                            });

                    Looper.loop();//ready to receive callback

                } catch (Exception e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;
                } finally {
                    if (signalCallbackFinishedLatch.getCount() > 0) {
                        signalCallbackFinishedLatch.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                }
            }
        });

        signalCallbackFinishedLatch.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            throw threadAssertionError[0];
        }
    }

    // this should crash because the number of retries is less than the number of modifications
    public void _testMismatchedRealmVersion_should_fail_after_2_retries() throws Throwable {
        // simulate 3 modification to the caller's Realm for each result coming from the background thread
        setDebugModeForAsyncRealmQuery(ADVANCE_THREE_READ, RetryPolicy.MODE_MAX_RETRY, RETRY_TWICE);

        final CountDownLatch signalCallbackFinishedLatch = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_should_crash_after_2_retries");
                    populateTestRealm(realm, 10);
                    realm.where(AllTypes.class)
                            .between("columnLong", 0, 9)
                            .findAll(new RealmResults.DebugRealmResultsQueryCallback<AllTypes>() {
                                @Override
                                public void onSuccess(RealmResults<AllTypes> results) {
                                    try {
                                        signalCallbackFinishedLatch.countDown();

                                    } catch (AssertionFailedError e) {
                                        threadAssertionError[0] = e;

                                    } finally {
                                        // whatever happened, make sure to notify the waiting TestCase Thread
                                        signalCallbackFinishedLatch.countDown();
                                    }
                                }

                                @Override
                                public void onError(Exception t) {
                                    try {
                                        threadAssertionError[0] = t;
                                    } finally {
                                        signalCallbackFinishedLatch.countDown();
                                    }
                                }

                                @Override
                                public void onBackgroundQueryCompleted(Realm realm) {
                                    populateTestRealm(realm, 3);
                                }
                            });

                    Looper.loop();

                } catch (Exception e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;
                } finally {
                    if (signalCallbackFinishedLatch.getCount() > 0) {
                        signalCallbackFinishedLatch.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                }
            }
        });

        signalCallbackFinishedLatch.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null == threadAssertionError[0] || !(threadAssertionError[0] instanceof RealmException)) {
            fail("Expecting RuntimeException: Unspecified exception." +
                    " Handover failed due to version mismatch in io_realm_internal_TableQuery.cpp");
        }
    }

    // keep retrying until the caller thread & the background results converge
    public void _testMismatchedRealmVersion_should_converge_after_100_retries() throws Throwable {
        setDebugModeForAsyncRealmQuery(ADVANCE_HUNDRED_READ, RetryPolicy.MODE_INDEFINITELY, RETRY_NUMBER_NOT_APPLICABLE);

        final CountDownLatch signalCallbackFinishedLatch = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final AtomicInteger numberOfEntries = new AtomicInteger(10);
        final Random random = new Random(System.currentTimeMillis());
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_should_converge_after_100_retries");
                    populateTestRealm(realm, numberOfEntries.get());
                    realm.where(AllTypes.class)
                            .between("columnLong", 0, 9)
                            .findAll(new RealmResults.DebugRealmResultsQueryCallback<AllTypes>() {
                                @Override
                                public void onSuccess(RealmResults<AllTypes> results) {
                                    try {
                                        if (numberOfEntries.get() > 10) {
                                            assertEquals(10, results.size());
                                        } else {
                                            assertEquals(numberOfEntries.get(), results.size());
                                        }

                                        for (int i = 0, size = results.size(); i < size; i++) {
                                            assertEquals(i, results.get(i).getColumnLong());
                                        }

                                    } catch (AssertionFailedError e) {
                                        threadAssertionError[0] = e;

                                    } finally {
                                        signalCallbackFinishedLatch.countDown();
                                    }
                                }

                                @Override
                                public void onError(Exception t) {
                                    try {
                                        threadAssertionError[0] = t;
                                    } finally {
                                        signalCallbackFinishedLatch.countDown();
                                    }
                                }

                                @Override
                                public void onBackgroundQueryCompleted(Realm realm) {
                                    numberOfEntries.set(random.nextInt(100));
                                    populateTestRealm(realm, numberOfEntries.get());
                                }

                            });

                    Looper.loop();

                } catch (Exception e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;
                } finally {
                    if (signalCallbackFinishedLatch.getCount() > 0) {
                        signalCallbackFinishedLatch.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                }
            }
        });

        signalCallbackFinishedLatch.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            throw threadAssertionError[0];
        }
    }

    // cancel a pending query
    public void _testCancelQuery() throws Throwable {
        setDebugModeForAsyncRealmQuery(ADVANCE_THREE_READ, RetryPolicy.MODE_INDEFINITELY, RETRY_NUMBER_NOT_APPLICABLE);

        final AtomicInteger retryNumber = new AtomicInteger(0);
        final CountDownLatch signalQueryRunning = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        final RealmQuery.Request[] asyncRequest = new RealmQuery.Request[1];
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_cancel_query");
                    populateTestRealm(realm, 10);

                    asyncRequest[0] = realm.where(AllTypes.class)
                            .between("columnLong", 0, 9)
                            .findAll(new RealmResults.DebugRealmResultsQueryCallback<AllTypes>() {
                                @Override
                                public void onSuccess(RealmResults<AllTypes> results) {
                                    threadAssertionError[0] = new AssertionFailedError("onSuccess called on a cancelled query");
                                }

                                @Override
                                public void onError(Exception t) {
                                    threadAssertionError[0] = new AssertionFailedError("onError called on a cancelled query");
                                }

                                @Override
                                public void onBackgroundQueryCompleted(Realm realm) {
                                    populateTestRealm(realm, 1);
                                    // after 2 retries we cancel the query
                                    if (retryNumber.incrementAndGet() == 2) {
                                        asyncRequest[0].cancel();
                                        signalQueryRunning.countDown();
                                    }
                                }
                            });

                    Looper.loop();

                } catch (Exception e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalQueryRunning.getCount() > 0) {
                        signalQueryRunning.countDown();
                    }
                    if (realm != null) {
                        realm.close();
                    }
                }
            }
        });

        signalQueryRunning.await();
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        assertTrue(asyncRequest[0].isCancelled());

        looper[0].quit();
        executorService.shutdownNow();

        if (null != threadAssertionError[0]) {
            throw threadAssertionError[0];
        }
    }

    // *** findFirst *** //

    // Async query to find one RealmObject without any conflicts strategy
    public void _testFindFirst() throws Throwable {
        setDebugModeForAsyncRealmQuery(NO_ADVANCED_READ, RetryPolicy.MODE_NO_RETRY, NO_RETRY);

        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_first");
                    realm.beginTransaction();
                    final Owner owner1 = realm.createObject(Owner.class);
                    owner1.setName("Owner 1");
                    final Dog dog1 = realm.createObject(Dog.class);
                    dog1.setName("Dog 1");
                    dog1.setWeight(1);
                    final Dog dog2 = realm.createObject(Dog.class);
                    dog2.setName("Dog 2");
                    dog2.setWeight(2);
                    owner1.getDogs().add(dog1);
                    owner1.getDogs().add(dog2);

                    final Owner owner2 = realm.createObject(Owner.class);
                    owner2.setName("Owner 2");
                    final Dog dog3 = realm.createObject(Dog.class);
                    dog3.setName("Dog 3");
                    dog3.setWeight(1);
                    final Dog dog4 = realm.createObject(Dog.class);
                    dog4.setName("Dog 4");
                    dog4.setWeight(2);
                    owner2.getDogs().add(dog3);
                    owner2.getDogs().add(dog4);
                    realm.commitTransaction();

                    realm.where(Owner.class)
                            .equalTo("name", "Owner 2")
                            .findFirst(new RealmObject.QueryCallback<Owner>() {
                                @Override
                                public void onSuccess(Owner result) {
                                    try {
                                        RealmList<Dog> dogs = result.getDogs();
                                        Dog dog = dogs.where().equalTo("name", "Dog 4").findFirst();
                                        assertEquals(dog4, dog);

                                    } catch (AssertionFailedError e) {
                                        threadAssertionError[0] = e;

                                    } finally {
                                        // whatever happened, make sure to notify the waiting TestCase Thread
                                        signalCallbackFinished.countDown();
                                    }
                                }

                                @Override
                                public void onError(Exception t) {
                                    try {
                                        threadAssertionError[0] = t;
                                        t.printStackTrace();
                                    } finally {
                                        signalCallbackFinished.countDown();
                                    }
                                }
                            });

                    Looper.loop();

                } catch (Exception e) {
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
        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    // async query to find one RealmObject without any conflicts strategy.
    // since no retry policy was defined it should crash
    public void _testFindFirst_should_fail_no_retry() throws Throwable {
        setDebugModeForAsyncRealmQuery(1, RetryPolicy.MODE_NO_RETRY, NO_RETRY);

        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_first_fail_no_retry");
                    realm.beginTransaction();
                    final Owner owner1 = realm.createObject(Owner.class);
                    owner1.setName("Owner 1");
                    final Dog dog1 = realm.createObject(Dog.class);
                    dog1.setName("Dog 1");
                    dog1.setWeight(1);
                    final Dog dog2 = realm.createObject(Dog.class);
                    dog2.setName("Dog 2");
                    dog2.setWeight(2);
                    owner1.getDogs().add(dog1);
                    owner1.getDogs().add(dog2);

                    final Owner owner2 = realm.createObject(Owner.class);
                    owner2.setName("Owner 2");
                    final Dog dog3 = realm.createObject(Dog.class);
                    dog3.setName("Dog 3");
                    dog3.setWeight(1);
                    final Dog dog4 = realm.createObject(Dog.class);
                    dog4.setName("Dog 4");
                    dog4.setWeight(2);
                    owner2.getDogs().add(dog3);
                    owner2.getDogs().add(dog4);
                    realm.commitTransaction();

                    realm.where(Owner.class)
                            .equalTo("name", "Owner 2")
                            .findFirst(new RealmObject.DebugRealmObjectQueryCallback<Owner>() {
                                @Override
                                public void onSuccess(Owner result) {
                                    signalCallbackFinished.countDown();
                                }

                                @Override
                                public void onError(Exception t) {
                                    try {
                                        threadAssertionError[0] = t;
                                        t.printStackTrace();
                                    } finally {
                                        signalCallbackFinished.countDown();
                                    }
                                }

                                @Override
                                public void onBackgroundQueryCompleted(Realm realm) {
                                    // triggered on the background thread to alter the caller's Realm state
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.createObject(Owner.class);
                                            realm.createObject(Dog.class);
                                        }
                                    });
                                }
                            });

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
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
        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null == threadAssertionError[0] || !(threadAssertionError[0] instanceof RealmException)) {
            fail("Expecting RuntimeException: Unspecified exception." +
                    " Handover failed due to version mismatch in io_realm_internal_TableQuery.cpp");
        }
    }

    // async query to find one RealmObject converge after 1 retry
    public void _testFindFirst_should_converge_after_1_retry() throws Throwable {
        setDebugModeForAsyncRealmQuery(NO_ADVANCED_READ, RetryPolicy.MODE_NO_RETRY, NO_RETRY);

        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_first_converge_after_1_retry");
                    realm.beginTransaction();
                    final Owner owner1 = realm.createObject(Owner.class);
                    owner1.setName("Owner 1");
                    final Dog dog1 = realm.createObject(Dog.class);
                    dog1.setName("Dog 1");
                    dog1.setWeight(1);
                    final Dog dog2 = realm.createObject(Dog.class);
                    dog2.setName("Dog 2");
                    dog2.setWeight(2);
                    owner1.getDogs().add(dog1);
                    owner1.getDogs().add(dog2);

                    final Owner owner2 = realm.createObject(Owner.class);
                    owner2.setName("Owner 2");
                    final Dog dog3 = realm.createObject(Dog.class);
                    dog3.setName("Dog 3");
                    dog3.setWeight(1);
                    final Dog dog4 = realm.createObject(Dog.class);
                    dog4.setName("Dog 4");
                    dog4.setWeight(2);
                    owner2.getDogs().add(dog3);
                    owner2.getDogs().add(dog4);
                    realm.commitTransaction();

                    realm.where(Owner.class)
                            .equalTo("name", "Owner 2")
                            .findFirst(new RealmObject.DebugRealmObjectQueryCallback<Owner>() {
                                @Override
                                public void onSuccess(Owner result) {
                                    try {
                                        RealmList<Dog> dogs = result.getDogs();
                                        Dog dog = dogs.where().equalTo("name", "Dog 4").findFirst();
                                        assertEquals(dog4, dog);

                                    } catch (AssertionFailedError e) {
                                        threadAssertionError[0] = e;

                                    } finally {
                                        // whatever happened, make sure to notify the waiting TestCase Thread
                                        signalCallbackFinished.countDown();
                                    }
                                }

                                @Override
                                public void onError(Exception t) {
                                    try {
                                        threadAssertionError[0] = t;
                                        t.printStackTrace();
                                    } finally {
                                        signalCallbackFinished.countDown();
                                    }
                                }

                                @Override
                                public void onBackgroundQueryCompleted(Realm realm) {
                                    // triggered on the background thread to alter the caller's Realm state
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realm.createObject(Owner.class);
                                            realm.createObject(Dog.class);
                                        }
                                    });
                                }

                            });

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
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
        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    // *** findAllSorted *** //

    public void _testFindAllSorted() throws Throwable {
        setDebugModeForAsyncRealmQuery(NO_ADVANCED_READ, RetryPolicy.MODE_NO_RETRY, NO_RETRY);
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_all_sorted");
                    realm.beginTransaction();
                    for (int i = 0; i < 10; i++) {
                        AllTypes allTypes = realm.createObject(AllTypes.class);
                        allTypes.setColumnLong(i);
                        allTypes.setColumnString("data " + i % 3);
                    }
                    realm.commitTransaction();

                    realm.where(AllTypes.class)
                            .findAllSorted("columnLong",
                                    RealmResults.SORT_ORDER_DESCENDING,
                                    new RealmResults.QueryCallback<AllTypes>() {

                                        @Override
                                        public void onSuccess(RealmResults<AllTypes> sortedList) {
                                            try {
                                                assertEquals(10, sortedList.size());
                                                assertEquals(9, sortedList.first().getColumnLong());
                                                assertEquals(0, sortedList.last().getColumnLong());


                                            } catch (AssertionFailedError e) {
                                                threadAssertionError[0] = e;

                                            } finally {
                                                // whatever happened, make sure to notify the waiting TestCase Thread
                                                signalCallbackFinished.countDown();
                                            }
                                        }

                                        @Override
                                        public void onError(Exception t) {
                                            try {
                                                threadAssertionError[0] = t;
                                                t.printStackTrace();
                                            } finally {
                                                signalCallbackFinished.countDown();
                                            }
                                        }
                                    });

                    Looper.loop();

                } catch (Exception e) {
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
        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    public void _testFindAllSorted_converge_3_retries() throws Throwable {
        setDebugModeForAsyncRealmQuery(ADVANCE_THREE_READ, RetryPolicy.MODE_MAX_RETRY, ADVANCE_THREE_READ);
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_all_sorted_converge_3_retries");
                    realm.beginTransaction();
                    for (int i = 0; i < 10; i++) {
                        AllTypes allTypes = realm.createObject(AllTypes.class);
                        allTypes.setColumnLong(i);
                        allTypes.setColumnString("data " + i % 3);
                    }
                    realm.commitTransaction();

                    realm.where(AllTypes.class)
                            .findAllSorted("columnLong",
                                    RealmResults.SORT_ORDER_ASCENDING,
                                    new RealmResults.DebugRealmResultsQueryCallback<AllTypes>() {

                                        @Override
                                        public void onSuccess(RealmResults<AllTypes> sortedList) {
                                            try {
                                                assertEquals(10, sortedList.size());
                                                assertEquals(0, sortedList.first().getColumnLong());
                                                assertEquals(9, sortedList.last().getColumnLong());


                                            } catch (AssertionFailedError e) {
                                                threadAssertionError[0] = e;

                                            } finally {
                                                // whatever happened, make sure to notify the waiting TestCase Thread
                                                signalCallbackFinished.countDown();
                                            }
                                        }

                                        @Override
                                        public void onError(Exception t) {
                                            try {
                                                threadAssertionError[0] = t;
                                                t.printStackTrace();
                                            } finally {
                                                signalCallbackFinished.countDown();
                                            }
                                        }

                                        @Override
                                        public void onBackgroundQueryCompleted(Realm realm) {
                                            // triggered on the background thread to alter the caller's Realm state
                                            realm.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    realm.createObject(Owner.class);
                                                    realm.createObject(Dog.class);
                                                }
                                            });
                                        }

                                    });

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
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
        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }


    public void _testFindAllSorted_fail_no_retry() throws Throwable {
        setDebugModeForAsyncRealmQuery(ADVANCE_ONE_READ, RetryPolicy.MODE_NO_RETRY, NO_RETRY);
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_all_sorted_no_retry");
                    realm.beginTransaction();
                    for (int i = 0; i < 10; i++) {
                        AllTypes allTypes = realm.createObject(AllTypes.class);
                        allTypes.setColumnLong(i);
                        allTypes.setColumnString("data " + i % 3);
                    }
                    realm.commitTransaction();

                    realm.where(AllTypes.class)
                            .findAllSorted("columnLong",
                                    RealmResults.SORT_ORDER_DESCENDING,
                                    new RealmResults.DebugRealmResultsQueryCallback<AllTypes>() {
                                        @Override
                                        public void onSuccess(RealmResults<AllTypes> result) {
                                            signalCallbackFinished.countDown();
                                        }

                                        @Override
                                        public void onError(Exception t) {
                                            try {
                                                threadAssertionError[0] = t;
                                                t.printStackTrace();
                                            } finally {
                                                signalCallbackFinished.countDown();
                                            }
                                        }

                                        @Override
                                        public void onBackgroundQueryCompleted(Realm realm) {
                                            // triggered on the background thread to alter the caller's Realm state
                                            realm.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    realm.createObject(Owner.class);
                                                    realm.createObject(Dog.class);
                                                }
                                            });
                                        }
                                    });

                    Looper.loop();

                } catch (Exception e) {
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
        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null == threadAssertionError[0] || !(threadAssertionError[0] instanceof RealmException)) {
            fail("Expecting RuntimeException: Unspecified exception." +
                    " Handover failed due to version mismatch in io_realm_internal_TableQuery.cpp");
        }
    }


    public void _testFindAllSortedMulti_converge_after_100_retries() throws Throwable {
        setDebugModeForAsyncRealmQuery(ADVANCE_HUNDRED_READ, RetryPolicy.MODE_INDEFINITELY, RETRY_NUMBER_NOT_APPLICABLE);
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_all_sorted_multi_100_retries");
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

                    realm.where(AllTypes.class)
                            .findAllSorted(new String[]{"columnLong", "columnString"},
                                    new boolean[]{RealmResults.SORT_ORDER_DESCENDING, RealmResults.SORT_ORDER_ASCENDING},
                                    new RealmResults.DebugRealmResultsQueryCallback<AllTypes>() {
                                        @Override
                                        public void onSuccess(RealmResults<AllTypes> sortedList) {
                                            try {
                                                assertEquals(10, sortedList.size());
                                                assertEquals(4, sortedList.first().getColumnLong());
                                                assertEquals(0, sortedList.last().getColumnLong());

                                                assertEquals(4, sortedList.get(0).getColumnLong());
                                                assertEquals("data 1", sortedList.get(0).getColumnString());
                                                assertEquals(4, sortedList.get(1).getColumnLong());
                                                assertEquals("data 2", sortedList.get(1).getColumnString());

                                                assertEquals(3, sortedList.get(2).getColumnLong());
                                                assertEquals("data 0", sortedList.get(2).getColumnString());
                                                assertEquals(3, sortedList.get(3).getColumnLong());
                                                assertEquals("data 1", sortedList.get(3).getColumnString());

                                                assertEquals(2, sortedList.get(4).getColumnLong());
                                                assertEquals("data 0", sortedList.get(4).getColumnString());
                                                assertEquals(2, sortedList.get(5).getColumnLong());
                                                assertEquals("data 2", sortedList.get(5).getColumnString());

                                                assertEquals(1, sortedList.get(6).getColumnLong());
                                                assertEquals("data 1", sortedList.get(6).getColumnString());
                                                assertEquals(1, sortedList.get(7).getColumnLong());
                                                assertEquals("data 2", sortedList.get(7).getColumnString());

                                                assertEquals(0, sortedList.get(8).getColumnLong());
                                                assertEquals("data 0", sortedList.get(8).getColumnString());
                                                assertEquals(0, sortedList.get(9).getColumnLong());
                                                assertEquals("data 1", sortedList.get(9).getColumnString());

                                            } catch (AssertionFailedError e) {
                                                threadAssertionError[0] = e;

                                            } finally {
                                                // whatever happened, make sure to notify the waiting TestCase Thread
                                                signalCallbackFinished.countDown();
                                            }
                                        }

                                        @Override
                                        public void onError(Exception t) {
                                            try {
                                                threadAssertionError[0] = t;
                                                t.printStackTrace();
                                            } finally {
                                                signalCallbackFinished.countDown();
                                            }
                                        }

                                        @Override
                                        public void onBackgroundQueryCompleted(Realm realm) {
                                            // triggered on the background thread to alter the caller's Realm state
                                            realm.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    realm.createObject(Owner.class);
                                                    realm.createObject(Dog.class);
                                                }
                                            });
                                        }
                                    });

                    Looper.loop();

                } catch (Exception e) {
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
        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            throw threadAssertionError[0];
        }
    }

    public void _testFindAllMultiSorted() throws Throwable {
        setDebugModeForAsyncRealmQuery(NO_ADVANCED_READ, RetryPolicy.MODE_NO_RETRY, NO_RETRY);
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_all_sorted_multi");
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

                    realm.where(AllTypes.class)
                            .findAllSorted(new String[]{"columnLong", "columnString"},
                                    new boolean[]{RealmResults.SORT_ORDER_DESCENDING, RealmResults.SORT_ORDER_ASCENDING},
                                    new RealmResults.QueryCallback<AllTypes>() {

                                        @Override
                                        public void onSuccess(RealmResults<AllTypes> sortedList) {
                                            try {
                                                assertEquals(10, sortedList.size());
                                                assertEquals(4, sortedList.first().getColumnLong());
                                                assertEquals(0, sortedList.last().getColumnLong());

                                                assertEquals(4, sortedList.get(0).getColumnLong());
                                                assertEquals("data 1", sortedList.get(0).getColumnString());
                                                assertEquals(4, sortedList.get(1).getColumnLong());
                                                assertEquals("data 2", sortedList.get(1).getColumnString());

                                                assertEquals(3, sortedList.get(2).getColumnLong());
                                                assertEquals("data 0", sortedList.get(2).getColumnString());
                                                assertEquals(3, sortedList.get(3).getColumnLong());
                                                assertEquals("data 1", sortedList.get(3).getColumnString());

                                                assertEquals(2, sortedList.get(4).getColumnLong());
                                                assertEquals("data 0", sortedList.get(4).getColumnString());
                                                assertEquals(2, sortedList.get(5).getColumnLong());
                                                assertEquals("data 2", sortedList.get(5).getColumnString());

                                                assertEquals(1, sortedList.get(6).getColumnLong());
                                                assertEquals("data 1", sortedList.get(6).getColumnString());
                                                assertEquals(1, sortedList.get(7).getColumnLong());
                                                assertEquals("data 2", sortedList.get(7).getColumnString());

                                                assertEquals(0, sortedList.get(8).getColumnLong());
                                                assertEquals("data 0", sortedList.get(8).getColumnString());
                                                assertEquals(0, sortedList.get(9).getColumnLong());
                                                assertEquals("data 1", sortedList.get(9).getColumnString());

                                            } catch (AssertionFailedError e) {
                                                threadAssertionError[0] = e;

                                            } finally {
                                                // whatever happened, make sure to notify the waiting TestCase Thread
                                                signalCallbackFinished.countDown();
                                            }
                                        }

                                        @Override
                                        public void onError(Exception t) {
                                            try {
                                                threadAssertionError[0] = t;
                                                t.printStackTrace();
                                            } finally {
                                                signalCallbackFinished.countDown();
                                            }
                                        }
                                    });

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
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
        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    // *** Async write transaction *** //

    public void _testAsyncWriteTransaction() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Realm[] realm = new Realm[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                try {
                    realm[0] = openRealmInstance("test_async_write_transaction");
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
                        public void onError(Throwable e) {
                            signalCallbackFinished.countDown();
                        }
                    });

                    Looper.loop();

                } catch (Exception e) {
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
        signalCallbackFinished.await();
        looper[0].quit();
        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    public void _testCancelAsyncWriteTransaction() throws Throwable {
        final CountDownLatch signalTransactionStarted = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Realm[] realm = new Realm[1];
        final RealmQuery.Request[] request = new RealmQuery.Request[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                try {
                    realm[0] = openRealmInstance("test_cancel_async_write_transaction");
                    request[0] = realm[0].executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            signalTransactionStarted.countDown();
                            for (int i = 0; i < 10000; i++) {
                                Owner owner = realm.createObject(Owner.class);
                                owner.setName("Owner " + i);
                            }
                            SystemClock.sleep(100);

                        }
                    }, new Realm.Transaction.Callback() {
                        @Override
                        public void onSuccess() {
                            threadAssertionError[0] = new AssertionFailedError("Transaction should not be completed");
                        }

                        @Override
                        public void onError(Throwable e) {
                            threadAssertionError[0] = new AssertionFailedError("Transaction should not call onError");
                            e.printStackTrace();
                        }
                    });

                    Looper.loop();

                } catch (Exception e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalTransactionStarted.getCount() > 0) {
                        signalTransactionStarted.countDown();
                    }
                    if (realm.length > 0 && realm[0] != null) {
                        realm[0].close();
                    }
                }
            }
        });

        // wait until the async transaction starts
        signalTransactionStarted.await();
        request[0].cancel();
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        assertTrue(request[0].isCancelled());
        // nothing was committed
        Realm ownerRealm = Realm.getInstance(realm[0].getConfiguration());
        assertEquals(0, ownerRealm.allObjects(Owner.class).size());
        ownerRealm.close();

        looper[0].quit();
        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    // **********************************
    // *** promises based async query ***
    // **********************************

    // finding element [0-4] asynchronously then wait for the promise to be loaded
    // no use of notification callback
    public void testFindAllAsync() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_all");
                    final Handler handler = realm.getHandler();
                    final Realm.PausableThreadPoolExecutor asyncQueryExecutor = (Realm.PausableThreadPoolExecutor) realm.asyncQueryExecutor;
                    asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                            .between("columnLong", 0, 4)
                            .findAllAsync();

                    assertFalse(realmResults.isLoaded());
                    assertEquals(0, realmResults.size());

                    asyncQueryExecutor.resume();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                assertTrue(realmResults.isLoaded());
                                assertEquals(5, realmResults.size());
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                        }
                    }, 16);// give the looper a chance to process REALM_COMPLETED_ASYNC_QUERY message

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
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
        signalCallbackFinished.await(7, TimeUnit.SECONDS);


        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    // finding element [0-4] asynchronously then wait for the promise to be loaded
    // using a callback to be notified when the data is loaded
    public void testFindAllAsyncNotif() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Realm realm = null;
                try {
                    realm = openRealmInstance("testFindAllAsyncNotif");
                    final Realm.PausableThreadPoolExecutor asyncQueryExecutor = (Realm.PausableThreadPoolExecutor) realm.asyncQueryExecutor;
                    asyncQueryExecutor.pause();

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
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }

                        }
                    });

                    assertFalse(realmResults.isLoaded());
                    assertEquals(0, realmResults.size());

                    asyncQueryExecutor.resume();

                    Looper.loop();

                } catch (Exception e) {
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
        signalCallbackFinished.await(7, TimeUnit.SECONDS);

        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    // Transforming an async query into sync by calling load
    public void testForceLoadAsync() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Realm realm = null;
                try {
                    realm = openRealmInstance("testForceLoadAsync");
                    final Realm.PausableThreadPoolExecutor asyncQueryExecutor = (Realm.PausableThreadPoolExecutor) realm.asyncQueryExecutor;
                    asyncQueryExecutor.pause();

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

                    asyncQueryExecutor.resume();
                    realmResults.load();

                    assertTrue(realmResults.isLoaded());
                    assertEquals(5, realmResults.size());

                    Looper.loop();

                } catch (Exception e) {
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
        signalCallbackFinished.await(7, TimeUnit.SECONDS);

        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    //TODO: remove, experiment to check the implementation of our HandlerProxy
    public void HandlerProxy() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_all");
                    // partial mocking ofr our Handler
                    final Handler handler = new HandlerProxy(realm.getHandler()) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case Realm.REALM_COMPLETED_ASYNC_QUERY: {
                                    System.out.println("REALM_COMPLETED_ASYNC_QUERY");
                                    // block intentionally the call
                                    try {
                                        Thread.sleep(7000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                case Realm.REALM_UPDATE_ASYNC_QUERIES: {
                                    System.out.println("REALM_UPDATE_ASYNC_QUERIES");
                                    break;
                                }
                            }
                            return false;
                        }
                    };
                    realm.setHandler(handler);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handler.sendMessage(handler.obtainMessage(Realm.REALM_COMPLETED_ASYNC_QUERY));
                        }
                    }, 5000);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handler.sendMessage(handler.obtainMessage(Realm.REALM_UPDATE_ASYNC_QUERIES));
                        }
                    }, 10000);

                    Looper.loop();

                } catch (Exception e) {
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
//        signalCallbackFinished.await(20, TimeUnit.MINUTES);
        signalCallbackFinished.await();


        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
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
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    final AtomicInteger numberOfIntercept = new AtomicInteger(1);
                    final Realm realm = openRealmInstance("testFindAllAsyncRetry");
                    // partial mocking ofr our Handler
                    final Handler handler = new HandlerProxy(realm.getHandler()) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case Realm.REALM_COMPLETED_ASYNC_QUERY: {
                                    if (numberOfIntercept.getAndDecrement() > 0) {
                                        // We advance the Realm so we can simulate a retry
                                        // This is intercepted on the worker thread, we need to use
                                        // the Realm om the original thread
                                        postAtFront(new Runnable() {
                                            @Override
                                            public void run() {
                                                realm.beginTransaction();
                                                realm.clear(AllTypes.class);
                                                realm.commitTransaction();
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

                    final Realm.PausableThreadPoolExecutor asyncQueryExecutor = (Realm.PausableThreadPoolExecutor) realm.asyncQueryExecutor;
                    asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                            .between("columnLong", 0, 4)
                            .findAllAsync();

                    assertFalse(realmResults.isLoaded());
                    assertEquals(0, realmResults.size());

                    asyncQueryExecutor.resume();

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

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }

                }
            }
        });

        // wait until the callback of our async query proceed
        signalCallbackFinished.await(7, TimeUnit.SECONDS);

        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    public void testBgRealmVersionID () throws Throwable {
        io.realm.internal.Util.setDebugLevel(5);
        final Throwable[] threadAssertionError = new Throwable[1];
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Realm realm = openRealmInstance("testBgRealmVersionID");
        final RealmConfiguration configuration = realm.getConfiguration();
        populateTestRealm(realm, 10);
        final SharedGroup.VersionID versionID = realm.sharedGroup.getVersion();

        new Thread() {
            @Override
            public void run() {
                final Realm realm = Realm.getInstance(configuration);
                SharedGroup.VersionID versionIdBg = realm.sharedGroup.getVersion();
                try {
                    assertEquals(0, versionID.compareTo(versionIdBg));
                } catch (AssertionFailedError e) {
                    threadAssertionError[0] = e;
                } finally {
                    realm.close();
                    signalCallbackFinished.countDown();
                }
            }
        }.start();

        signalCallbackFinished.await(7, TimeUnit.SECONDS);
        realm.close();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    // UC:
    //   1- insert 10 objects
    //   2- start 2 async queries to find all objects [0-9] & objects[0-4]
    //   3- assert both RealmResults are empty (Worker Thread didn't complete)
    //   4- the queries will complete with the same version as the caller thread
    //   5- using a background thread update the Realm
    //   6- now REALM_CHANGED will trigger a REALM_UPDATE_ASYNC_QUERIES that should update
    //   7- callbacks are notified with the latest results (called twice overall)
    public void testFindAllAsyncBatchUpdate() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(2);
        final Throwable[] threadAssertionError = new Throwable[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final AtomicInteger numberOfNotificationsQuery1 = new AtomicInteger(0);
        final AtomicInteger numberOfNotificationsQuery2 = new AtomicInteger(0);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    final Realm realm = openRealmInstance("testFindAllAsyncBatchUpdate");
                    final AtomicInteger numberOfIntercept = new AtomicInteger(1);
                    final Handler handler = new HandlerProxy(realm.getHandler()) {
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
                                                        Realm bgRealm = Realm.getInstance(realm.getConfiguration());
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
                    final Realm.PausableThreadPoolExecutor asyncQueryExecutor = (Realm.PausableThreadPoolExecutor) realm.asyncQueryExecutor;
                    asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    // queries will not run i parallel (since our Executor is single threaded)
                    // should save the pointer
                    final RealmResults<AllTypes> realmResults1 = realm.where(AllTypes.class)
                            .findAllAsync();
                    final RealmResults<AllTypes> realmResults2 = realm.where(AllTypes.class)
                            .between("columnLong", 0, 4).findAllAsync();

                    assertFalse(realmResults1.isLoaded());
                    assertFalse(realmResults2.isLoaded());
                    assertEquals(0, realmResults1.size());
                    assertEquals(0, realmResults2.size());

                    asyncQueryExecutor.resume();


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

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }

                }
            }
        });

        signalCallbackFinished.await(7, TimeUnit.SECONDS);
        assertEquals(2, numberOfNotificationsQuery1.get());
        assertEquals(2, numberOfNotificationsQuery2.get());

        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    //TODO test GC'd in REALM_COMPLETED_ASYNC_QUERY
    //TODO test removed callback
    //TODO test cancel pending query
    public void testFindAllCallerIsAdvanced() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final AtomicInteger numberOfInvocation = new AtomicInteger(0);
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    final AtomicInteger numberOfIntercept = new AtomicInteger(1);
                    final Realm realm = openRealmInstance("testFindAllCallerIsAdvanced");
                    final CountDownLatch updateCallerThread = new CountDownLatch(1);
                    // partial mocking ofr our Handler
                    final Handler handler = new HandlerProxy(realm.getHandler()) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case Realm.REALM_UPDATE_ASYNC_QUERIES: {
                                    // re-prioritise batch update to complete before the waiting

                                    // posting this as a runnable guarantee that  REALM_UPDATE_ASYNC_QUERIES
                                    // logic complete before resuming the awaiting REALM_COMPLETED_ASYNC_QUERY
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateCallerThread.countDown();
                                        }
                                    });

                                    // this will give a chance to the blocked REALM_COMPLETED_ASYNC_QUERY
                                    // to proceed. note, that the result of this REALM_COMPLETED_ASYNC_QUERY
                                    // will be ignored (should not trigger a callback)
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            signalCallbackFinished.countDown();
                                        }
                                    });
                                    break;
                                }
                                case Realm.REALM_COMPLETED_ASYNC_QUERY: {

                                    if (numberOfIntercept.getAndDecrement() > 0) {

                                        // We advance the Realm so we can simulate a retry
                                        // This is intercepted on the worker thread, we need to use
                                        // the Realm om the original thread
                                        postAtFront(new Runnable() {
                                            @Override
                                            public void run() {
                                                // this should trigger the update of all
                                                // async queries
                                                realm.beginTransaction();
                                                realm.createObject(AllTypes.class);
                                                realm.commitTransaction();
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
                    realm.setHandler(handler);

                    final Realm.PausableThreadPoolExecutor asyncQueryExecutor = (Realm.PausableThreadPoolExecutor) realm.asyncQueryExecutor;
                    asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
                            .between("columnLong", 0, 4)
                            .findAllAsync();

                    assertFalse(realmResults.isLoaded());
                    assertEquals(0, realmResults.size());

                    asyncQueryExecutor.resume();

                    realmResults.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                assertEquals(1, numberOfInvocation.incrementAndGet());
                                assertTrue(realmResults.isLoaded());
                                assertEquals(6, realmResults.size());
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            }
                        }
                    });

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }

                }
            }
        });

        // wait until the callback of our async query proceed
        signalCallbackFinished.await(7, TimeUnit.SECONDS);
        assertEquals(1, numberOfInvocation.get());

        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    private void waitForMessages(final MessageQueue messageQueue) {
        try {
            Class<?>[] classArray = new Class<?>[1];
            classArray[0] = boolean.class;

            Method quit = MessageQueue.class.getDeclaredMethod("quit", classArray);
            quit.setAccessible(true);
            quit.invoke(messageQueue, true);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    final Realm realm = openRealmInstance("testFindAllCallerThreadBehind");
                    final AtomicInteger numberOfCompletedAsyncQuery = new AtomicInteger(0);
                    final AtomicInteger numberOfInterceptedChangeMessage = new AtomicInteger(0);
                    final Handler handler = new HandlerProxy(realm.getHandler()) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case Realm.REALM_CHANGED: {
                                    // should only intercept the first REALM_CHANGED coming from the
                                    // background update thread

                                    // swallow this message, so the caller thread
                                    // remain behind the worker thread. This has as
                                    // a consequence to ignore the delivered result & wait for the
                                    // upcoming REALM_CHANGED to batch update all asycn queries
                                    return numberOfInterceptedChangeMessage.getAndIncrement() == 0;
                                }
                                case Realm.REALM_COMPLETED_ASYNC_QUERY: {
                                    if (numberOfCompletedAsyncQuery.incrementAndGet() == 2) {
                                        // both queries has completed now (& their result should be ignored)
                                        // now send the REALM_CHANGED event that should batch update all queries
                                        sendEmptyMessage(Realm.REALM_CHANGED);
                                    }
                                }
                            }
                            return false;
                        }
                    };
                    realm.setHandler(handler);
                    final Realm.PausableThreadPoolExecutor asyncQueryExecutor = (Realm.PausableThreadPoolExecutor) realm.asyncQueryExecutor;
                    asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    // queries will not run i parallel (since our Executor is single threaded)
                    // should save the pointer
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
                            Realm bgRealm = Realm.getInstance(realm.getConfiguration());
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
                    asyncQueryExecutor.resume();

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

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }

                }
            }
        });


        signalCallbackFinished.await(7, TimeUnit.SECONDS);

        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    //*** find first *** //

    public void testFindFirstAsync() throws  Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                Realm realm = null;
                try {
                    realm = openRealmInstance("testFindFirstAsync");
                    final Realm.PausableThreadPoolExecutor asyncQueryExecutor = (Realm.PausableThreadPoolExecutor) realm.asyncQueryExecutor;
                    final Handler handler = realm.getHandler();
                    populateTestRealm(realm, 10);
//                    realm.beginTransaction();
                    final AllTypes firstAsync = realm.where(AllTypes.class).findFirstAsync();

////                    final RealmResults<AllTypes> realmResults = realm.where(AllTypes.class)
////                            .between("columnLong", 0, 4)
////                            .findAllAsync();
//                    AllTypes firstAsync = realm.createObject(AllTypes.class);
//                    assertFalse(firstAsync.isLoaded());
//                    assertEquals("", firstAsync.getColumnString());
//                    assertEquals(0,firstAsync.getColumnLong());
//                    assertEquals(0f,firstAsync.getColumnFloat());
//                    assertEquals(0d, firstAsync.getColumnDouble());
//                    assertFalse(firstAsync.isColumnBoolean());
//                    assertNotNull(firstAsync.getColumnBinary());
//                    assertEquals(0, firstAsync.getColumnBinary().length);
//                    assertNull(firstAsync.getColumnRealmObject());
//                    assertNotNull(firstAsync.getColumnRealmList());
//                    assertEquals(0, firstAsync.getColumnRealmList().size());
//                    realm.commitTransaction();
//                    assertEquals(0, realmResults.size());

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                assertTrue(firstAsync.isLoaded());
                                //TODO assert value are correct for empty & populated object + test RealmList & RealmObject
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                        }
                    }, 16);// give the looper a chance to process REALM_COMPLETED_ASYNC_QUERY message


//                    firstAsync.load();
//                    assertTrue(firstAsync.isLoaded());
                    Looper.loop();// ready to receive callback

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
//        signalCallbackFinished.await(7, TimeUnit.SECONDS);
        signalCallbackFinished.await();


        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    public void testFindFirstAsyncNotif() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Realm realm = null;
                try {
                    realm = openRealmInstance("testFindFirstAsyncNotif");
                    final Realm.PausableThreadPoolExecutor asyncQueryExecutor = (Realm.PausableThreadPoolExecutor) realm.asyncQueryExecutor;
                    asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final AllTypes realmResults = realm.where(AllTypes.class)
                            .between("columnLong", 4, 9)
                            .findFirstAsync();

                    realmResults.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            try {
                                assertTrue(realmResults.isLoaded());
                                assertEquals("test data 4", realmResults.getColumnString());
                            } catch (AssertionFailedError e) {
                                threadAssertionError[0] = e;
                            } finally {
                                signalCallbackFinished.countDown();
                            }
                        }
                    });

                    assertFalse(realmResults.isLoaded());
                    assertEquals("", realmResults.getColumnString());

                    asyncQueryExecutor.resume();

                    Looper.loop();

                } catch (Exception e) {
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
//        signalCallbackFinished.await(7, TimeUnit.SECONDS);
        signalCallbackFinished.await();

        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    public void testForceLoadFindFirstAsync() throws Throwable {
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Realm realm = null;
                try {
                    realm = openRealmInstance("testForceLoadAsync");
                    final Realm.PausableThreadPoolExecutor asyncQueryExecutor = (Realm.PausableThreadPoolExecutor) realm.asyncQueryExecutor;
                    asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final AllTypes realmResults = realm.where(AllTypes.class)
                            .between("columnLong", 4, 9)
                            .findFirstAsync();

                    assertFalse(realmResults.isLoaded());
                    assertEquals("", realmResults.getColumnString());

                    asyncQueryExecutor.resume();
                    realmResults.load();

                    assertTrue(realmResults.isLoaded());
                    assertEquals("test data 4", realmResults.getColumnString());
                    signalCallbackFinished.countDown();
                    Looper.loop();

                } catch (Exception e) {
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
        signalCallbackFinished.await(7, TimeUnit.SECONDS);

        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
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
    public void testFindFirstAsyncRetry() throws Throwable {
        io.realm.internal.Util.setDebugLevel(5);
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Throwable[] threadAssertionError = new Throwable[1];
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    final AtomicInteger numberOfIntercept = new AtomicInteger(1);
                    final Realm realm = openRealmInstance("testFindFirstAsyncRetry");
                    // partial mocking ofr our Handler
                    final Handler handler = new HandlerProxy(realm.getHandler()) {
                        @Override
                        public boolean onInterceptMessage(int what) {
                            switch (what) {
                                case Realm.REALM_COMPLETED_ASYNC_FIND_FIRST: {
                                    if (numberOfIntercept.getAndDecrement() > 0) {
                                        // We advance the Realm so we can simulate a retry
                                        // This is intercepted on the worker thread, we need to use
                                        // the Realm om the original thread
                                        postAtFront(new Runnable() {
                                            @Override
                                            public void run() {
                                                realm.beginTransaction();
                                                realm.clear(AllTypes.class);
                                                AllTypes object = realm.createObject(AllTypes.class);

                                                object.setColumnString("The Endless River");
                                                object.setColumnLong(5);//TODO the test crash if we choose unvailable indice (not betwe 4-6)
                                                // TODO need to handle result -1 (object not found so we set an empty row again)
                                                realm.commitTransaction();
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

                    final Realm.PausableThreadPoolExecutor asyncQueryExecutor = (Realm.PausableThreadPoolExecutor) realm.asyncQueryExecutor;
                    asyncQueryExecutor.pause();

                    populateTestRealm(realm, 10);
                    final AllTypes realmResults = realm.where(AllTypes.class)
                            .between("columnLong", 4, 6)
                            .findFirstAsync();

                    assertFalse(realmResults.isLoaded());
                    assertEquals("", realmResults.getColumnString());

                    asyncQueryExecutor.resume();

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

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
                    e.printStackTrace();
                    threadAssertionError[0] = e;

                } finally {
                    if (signalCallbackFinished.getCount() > 0) {
                        signalCallbackFinished.countDown();
                    }

                }
            }
        });

        // wait until the callback of our async query proceed
//        signalCallbackFinished.await(7, TimeUnit.SECONDS);
        signalCallbackFinished.await();

        executorService.shutdownNow();
        if (null != threadAssertionError[0]) {
            // throw any assertion errors happened in the background thread
            throw threadAssertionError[0];
        }
    }

    // Demonstrate the inconsistency we can have when reusing the same instance of
    // RealmQuery to perform 2 different queries
    public void LoadingSyncQueries () {
        final Realm realm = openRealmInstance("test_find_all");
        populateTestRealm(realm, 10);
        final RealmQuery<AllTypes> realmQuery = realm.where(AllTypes.class);
        final RealmResults<AllTypes> realmResults1 = realmQuery.between("columnLong", 0, 4).findAll();
        final RealmResults<AllTypes> realmResults2 = realmQuery.findAll();
        assertTrue(realmResults1.isLoaded());
        assertTrue(realmResults2.isLoaded());
        assertEquals(5, realmResults1.size());
        assertEquals(10, realmResults2.size());// this fail return 5
    }


//    public void testMockito() {
//        // mock creation
//        List mockedList = mock(List.class);
//
//// using mock object - it does not throw any "unexpected interaction" exception
//        mockedList.add("one");
////        mockedList.clear();
//
//// selective, explicit, highly readable verification
////        verify(mockedList).clear();
//        verify(mockedList).add("one");
//        assertEquals("one", mockedList.get(0));
//    }

    // *** refresh on worker thread
    // async query without any conflicts strategy
    public void _testFindAll_refresh() throws Throwable {
        setDebugModeForAsyncRealmQuery(NO_ADVANCED_READ, RetryPolicy.MODE_NO_RETRY, NO_RETRY);
        final CountDownLatch signalCallbackFinished = new CountDownLatch(1);
        final Looper[] looper = new Looper[1];
        final Throwable[] threadAssertionError = new Throwable[1];
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                looper[0] = Looper.myLooper();
                Realm realm = null;
                try {
                    realm = openRealmInstance("test_find_all");
                    populateTestRealm(realm, 10);

                    // async query (will run on different thread)
                    final Realm finalRealm = realm;
                    realm.where(AllTypes.class)
                            .between("columnLong", 0, 9)
                            .findAll(new RealmResults.QueryCallback<AllTypes>() {
                                @Override
                                public void onSuccess(RealmResults<AllTypes> results) {
                                    try {
                                        assertEquals(10, results.size());

                                        // make sure access to RealmObject will not throw an Exception
                                        for (int i = 0, size = results.size(); i < size; i++) {
                                            assertEquals(i, results.get(i).getColumnLong());
                                        }

                                    } catch (AssertionFailedError e) {
                                        threadAssertionError[0] = e;

                                    } finally {
                                        // call commit
                                        finalRealm.getHandler().sendEmptyMessage(14930352);
                                    }
                                }

                                @Override
                                public void onError(Exception t) {
                                    try {
                                        threadAssertionError[0] = t;
                                        t.printStackTrace();
                                    } finally {
                                        signalCallbackFinished.countDown();
                                    }
                                }
                            });

                    Looper.loop();// ready to receive callback

                } catch (Exception e) {
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
        signalCallbackFinished.await(20, TimeUnit.MINUTES);
        looper[0].quit();
        executorService.shutdownNow();
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

    // we could have avoided using reflection to inject this behaviour if we were using a DI
    private void setDebugModeForAsyncRealmQuery(int numberOfAdvancedReadSimulation, int retryPolicyMode, int maxNumberOfRetries) {
        try {
            Field debugFlagField = RealmQuery.class.getDeclaredField("IS_DEBUG");
            Field nbAdvancedReadSimulationField = RealmQuery.class.getDeclaredField("NB_ADVANCE_READ_SIMULATION");
            Field nbNumberRetriesPolicyField = RealmQuery.class.getDeclaredField("MAX_NUMBER_RETRIES_POLICY");
            Field retryPolicyModeField = RealmQuery.class.getDeclaredField("RETRY_POLICY_MODE");

            debugFlagField.setAccessible(true);
            nbAdvancedReadSimulationField.setAccessible(true);
            nbNumberRetriesPolicyField.setAccessible(true);
            retryPolicyModeField.setAccessible(true);

            if (numberOfAdvancedReadSimulation > 0) {
                debugFlagField.set(null, true);
                nbAdvancedReadSimulationField.set(null, numberOfAdvancedReadSimulation);
                nbNumberRetriesPolicyField.set(null, maxNumberOfRetries);
                retryPolicyModeField.set(null, retryPolicyMode);

            } else {// reset to defaults
                debugFlagField.set(null, false);
                nbAdvancedReadSimulationField.set(null, 0);
                nbNumberRetriesPolicyField.set(null, 0);
                retryPolicyModeField.set(null, RetryPolicy.MODE_INDEFINITELY);
            }

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

//    private Realm.PausableThreadPoolExecutor getAsyncQueryExecutor (Realm realm) throws NoSuchFieldException {
//        Field asyncQueryExecutorField = RealmQuery.class.getDeclaredField("asyncQueryExecutor");
//        asyncQueryExecutorField.setAccessible(true);
//    }

    public void testRefreshObject () {
        Realm realm = openRealmInstance("testRefreshObject");
        realm.beginTransaction();
        Dog dog1 = realm.createObject(Dog.class);
        dog1.setName("Dog1");
        dog1.setAge(10);
        Dog dog2 = realm.createObject(Dog.class);
        dog2.setName("Dog2");
        dog2.setAge(15);
        Dog dog3 = realm.createObject(Dog.class);
        dog3.setName("Dog3");
        dog3.setAge(20);

        realm.commitTransaction();

        Dog dog = realm.where(Dog.class).lessThanOrEqualTo("age", 10).findFirst();
        assertEquals("Dog1", dog.getName());
        assertEquals(10, dog.getAge());

        realm.beginTransaction();
        RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
        dogs.get(0).setAge(dogs.get(0).getAge()+1);
//        dogs.get(1).setAge(dogs.get(1).getAge()+1);
//        dogs.get(2).setAge(dogs.get(2).getAge()+1);
        realm.commitTransaction();


        Dog dogAfterUpdate = realm.where(Dog.class).lessThanOrEqualTo("age", 10).findFirst();
        assertNull(dogAfterUpdate);

        assertEquals("Dog1", dog.getName());//!!! this instance should not be attached
        assertEquals(11, dog.getAge());

    }

    public void _testHashCode() {
        Realm realm = openRealmInstance("hashcode");
        realm.beginTransaction();
        Dog dog1 = realm.createObject(Dog.class);
        dog1.setName("Dog1");
        Dog dog2 = realm.createObject(Dog.class);
        dog2.setName("Dog2");
        Dog dog3 = realm.createObject(Dog.class);
        dog3.setName("Dog3");

        realm.commitTransaction();

        RealmResults<Dog> dogs = realm.where(Dog.class).findAll();

//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                Dog dog = realm.createObject(Dog.class);
//                dog.setName("Dog4");
//            }
//        });
//
//        assertEquals("Dog1", dogs.get(0).getName());
//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                realm.clear(Dog.class);
//            }
//        });
//        assertEquals("Dog4", dogs.get(0).getName());
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.clear(Dog.class);
            }
        });

        assertTrue(dogs.get(0).isValid());
        assertTrue(dogs.get(1).isValid());
        assertTrue(dogs.get(2).isValid());

        assertEquals("Dog2", dogs.get(1).getName());//THROWS INDEX OUT OF BOUNDS
        assertEquals("Dog3", dogs.get(2).getName());
        realm.close();

    }


    public void _testSalaryUpdate() {
        Realm realm = openRealmInstance("imperative_view");

        realm.beginTransaction();
        Dog dog1 = realm.createObject(Dog.class);
        dog1.setName("Dog1");
        dog1.setAge(10);

        Dog dog2 = realm.createObject(Dog.class);
        dog2.setName("Dog2");
        dog2.setAge(15);

        Dog dog3 = realm.createObject(Dog.class);
        dog3.setName("Dog3");
        dog3.setAge(20);

        realm.commitTransaction();

        // dogs.age >= 15, we have only two results dog2 & dog3
        RealmResults<Dog> dogs = realm.where(Dog.class).greaterThanOrEqualTo("age", 15).findAll();
        assertEquals(2, dogs.size());

        // we introduce a new dog with age 17
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Dog dog2 = realm.createObject(Dog.class);
                dog2.setName("Dog4");
                dog2.setAge(17);
            }
        });

        // *** this should be 2 not 3 since dogs is an imperative view
        assertEquals(3, dogs.size());

    }


}
