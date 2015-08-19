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
import android.os.SystemClock;
import android.test.InstrumentationTestCase;

import junit.framework.AssertionFailedError;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import io.realm.entities.Dog;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.Owner;
import io.realm.exceptions.RealmException;
import io.realm.internal.async.RetryPolicy;

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
    public void testFindAll() throws Throwable {
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
    public void testFindAllOnClosedRealm() throws Throwable {
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
    public void testMismatchedRealmVersion_should_fail_no_retry() throws Throwable {
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
    public void testMismatchedRealmVersion_should_converge_after_1_retry() throws Throwable {
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
    public void testMismatchedRealmVersion_should_fail_after_2_retries() throws Throwable {
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
    public void testMismatchedRealmVersion_should_converge_after_100_retries() throws Throwable {
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
    public void testCancelQuery() throws Throwable {
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
    public void testFindFirst() throws Throwable {
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
    public void testFindFirst_should_fail_no_retry() throws Throwable {
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
    public void testFindFirst_should_converge_after_1_retry() throws Throwable {
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

    public void testFindAllSorted() throws Throwable {
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

    public void testFindAllSorted_converge_3_retries() throws Throwable {
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


    public void testFindAllSorted_fail_no_retry() throws Throwable {
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


    public void testFindAllSortedMulti_converge_after_100_retries() throws Throwable {
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
                    for (int i = 0; i < 5;) {
                        AllTypes allTypes = realm.createObject(AllTypes.class);
                        allTypes.setColumnLong(i);
                        allTypes.setColumnString("data " + i % 3);

                        allTypes = realm.createObject(AllTypes.class);
                        allTypes.setColumnLong(i);
                        allTypes.setColumnString("data " + (++i  % 3));
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

    public void testFindAllMultiSorted() throws Throwable {
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
                    for (int i = 0; i < 5;) {
                        AllTypes allTypes = realm.createObject(AllTypes.class);
                        allTypes.setColumnLong(i);
                        allTypes.setColumnString("data " + i % 3);

                        allTypes = realm.createObject(AllTypes.class);
                        allTypes.setColumnLong(i);
                        allTypes.setColumnString("data " + (++i  % 3));
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

    public void testAsyncWriteTransaction() throws Throwable {
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

    public void testCancelAsyncWriteTransaction() throws Throwable {
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

    // *** refresh on worker thread
    // async query without any conflicts strategy
    public void testFindAll_refresh () throws Throwable {
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

    public void testHashCode () {
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


    public void testSalaryUpdate () {
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
