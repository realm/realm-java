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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import io.realm.internal.log.RealmLog;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RxJavaTests extends AndroidTestCase {

    private RealmConfiguration realmConfig;
    private Realm realm;
    private Subscription subscription;

    @Override
    protected void setUp() throws Exception {
        realmConfig = new RealmConfiguration.Builder(getContext()).build();
        Realm.deleteRealm(realmConfig);
        realm = Realm.getInstance(realmConfig);
    }

    @Override
    protected void tearDown() throws Exception {
        realm.close();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public void testObjectEmittedOnSubscribe() {
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        obj.<AllTypes>asObservable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                assertTrue(rxObject == obj);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }

    public void testObjectEmittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        obj.<AllTypes>asObservable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                subscriberCalled.incrementAndGet();
            }
        });

        realm.beginTransaction();
        obj.setColumnLong(1);
        realm.commitTransaction();

        assertEquals(2, subscriberCalled.get());
    }

    public void testAsyncObjectEmittedOnSubscribe() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final AllTypes asyncObj = realm.where(AllTypes.class).findFirstAsync();
        asyncObj.<AllTypes>asObservable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                assertTrue(rxObject == asyncObj);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }

    public void testAsyncObjectEmittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();
        realm.where(AllTypes.class).findFirstAsync().<AllTypes>asObservable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                subscriberCalled.incrementAndGet();
            }
        });

        realm.beginTransaction();
        obj.setColumnLong(1);
        realm.commitTransaction();

        assertEquals(1, subscriberCalled.get());
    }

    public void testRealmResultsEmittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<AllTypes> results = realm.allObjects(AllTypes.class);
        results.asObservable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> rxResults) {
                assertTrue(rxResults == results);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }

    public void testResultsEmittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        realm.beginTransaction();
        RealmResults<AllTypes> results = realm.allObjects(AllTypes.class);
        realm.commitTransaction();

        results.asObservable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> allTypes) {
                subscriberCalled.incrementAndGet();
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        assertEquals(2, subscriberCalled.get());
    }

    public void testAsyncRealmResultsEmittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
        results.asObservable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> rxResults) {
                assertTrue(rxResults == results);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }

    public void testAsyncResultsEmittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        realm.where(AllTypes.class).findAllAsync().asObservable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> rxResults) {
                subscriberCalled.incrementAndGet();
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        assertEquals(1, subscriberCalled.get());
    }

    public void testRealmEmittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        realm.asObservable().subscribe(new Action1<Realm>() {
            @Override
            public void call(Realm rxRealm) {
                assertTrue(rxRealm == realm);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
    }

    public void testRealmEmittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        realm.asObservable().subscribe(new Action1<Realm>() {
            @Override
            public void call(Realm rxRealm) {
                subscriberCalled.incrementAndGet();
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        assertEquals(2, subscriberCalled.get());
    }

    public void testDynamicRealmEmittedOnSubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.createInstance(realm.getConfiguration());
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        dynamicRealm.asObservable().subscribe(new Action1<DynamicRealm>() {
            @Override
            public void call(DynamicRealm rxRealm) {
                assertTrue(rxRealm == dynamicRealm);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        dynamicRealm.close();
    }

    public void testDynamicRealmEmittedOnUpdate() {
        final DynamicRealm dynamicRealm = DynamicRealm.createInstance(realm.getConfiguration());
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        dynamicRealm.asObservable().subscribe(new Action1<DynamicRealm>() {
            @Override
            public void call(DynamicRealm rxRealm) {
                subscriberCalled.incrementAndGet();
            }
        });

        dynamicRealm.beginTransaction();
        dynamicRealm.createObject("AllTypes");
        dynamicRealm.commitTransaction();

        assertEquals(2, subscriberCalled.get());
        dynamicRealm.close();
    }

    public void testRealmQueryEmittedOnSubscribe() {
        final AtomicInteger queryResult = new AtomicInteger(-1);
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        subscription = query.asObservable().subscribe(new Action1<RealmQuery<AllTypes>>() {
            @Override
            public void call(RealmQuery<AllTypes> rxQuery) {
                queryResult.set(rxQuery.findAll().size());
            }
        });

        assertEquals(1, queryResult.get());
    }

    // Test that the RealmQuery can be executed on a custom Scheduler
    public void testRealmQueryOnDifferentScheduler() {
        final AtomicInteger queryResult = new AtomicInteger(-1);
        final CountDownLatch queryExecuted = new CountDownLatch(1);
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        final RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        subscription = query.asObservable()
                .subscribeOn(Schedulers.computation())
                .subscribe(new Action1<RealmQuery<AllTypes>>() {
                    @Override
                    public void call(RealmQuery<AllTypes> rxQuery) {
                        queryResult.set(rxQuery.findAll().size());
                        queryExecuted.countDown();

                    }
                });

        TestHelper.awaitOrFail(queryExecuted);
        assertEquals(1, queryResult.get());
    }

    // Test that the result of the query is still thread confined even if executed on another scheduler.
    public void testRealmQueryOnOtherSchedulerThreadConfined() {
        final CountDownLatch subscriptionFailed = new CountDownLatch(1);
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        final RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        subscription = query.asObservable()
                .map(new Func1<RealmQuery<AllTypes>, RealmResults<AllTypes>>() {
                    @Override
                    public RealmResults<AllTypes> call(RealmQuery<AllTypes> rxQuery) {
                        return rxQuery.findAll();
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.newThread())
                .map(new Func1<RealmResults<AllTypes>, Boolean>() {
                    @Override
                    public Boolean call(RealmResults<AllTypes> results) {
                        return results.size() == 1; // Throws
                    }
                })
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean results) {
                        fail("This should throw an error");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable ignore) {
                        subscriptionFailed.countDown();
                    }
                });
        TestHelper.awaitOrFail(subscriptionFailed);
    }

    // Make sure that further changes to the Realm query doesn't effect the state when it it was observed on.
    public void testRealmQueryObservableIsACopy() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class).setColumnLong(1);
        realm.createObject(AllTypes.class).setColumnLong(2);
        realm.commitTransaction();

        RealmQuery<AllTypes> query = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 1);
        Observable<RealmQuery<AllTypes>> rxQuery = query.asObservable();

        // Standard query uses all parts of the query being built
        RealmResults<AllTypes> normalResults = query.or().equalTo(AllTypes.FIELD_LONG, 2).findAll();
        assertEquals(2, normalResults.size());

        // Observable should only use first part of the query
        subscription = rxQuery.asObservable().map(new Func1<RealmQuery<AllTypes>, Integer>() {
            @Override
            public Integer call(RealmQuery<AllTypes> rxQuery) {
                return rxQuery.findAll().size();
            }
        })
        .subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer size) {
                assertEquals(1, size.intValue());
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                fail(throwable.toString());
            }
        });
    }

    // Test that the underlying Realm is closed when unsubscribing from the RealmQuery observable.
    public void testRealmQueryCloseRealmOnOnUnsubscribe() {
        final CountDownLatch subscriptionCompleted = new CountDownLatch(1);
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        final RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        subscription = query.asObservable()
                .subscribeOn(Schedulers.computation())
                .subscribe(new Action1<RealmQuery<AllTypes>>() {
                    @Override
                    public void call(RealmQuery<AllTypes> ignore) {

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        fail(throwable.toString());
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        subscriptionCompleted.countDown();
                    }
                });
        TestHelper.awaitOrFail(subscriptionCompleted);
        realm.close();

        // The subscription should have closed the Realm, so no other threads should keep a reference to it.
        // We can test this implicitly by trying to delete the Realm file.
        assertTrue(Realm.deleteRealm(realmConfig));
    }

    public void testUnsubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        Subscription subscription = realm.asObservable().subscribe(new Action1<Realm>() {
            @Override
            public void call(Realm rxRealm) {
                assertTrue(rxRealm == realm);
                subscribedNotified.set(true);
            }
        });
        assertEquals(1, realm.handlerController.changeListeners.size());
        subscription.unsubscribe();
        assertEquals(0, realm.handlerController.changeListeners.size());
    }

    public void testUnsubscribeFromOtherThreadFails() {
        final CountDownLatch unsubscribeCompleted = new CountDownLatch(1);
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final Subscription subscription = realm.asObservable().subscribe(new Action1<Realm>() {
            @Override
            public void call(Realm rxRealm) {
                assertTrue(rxRealm == realm);
                subscribedNotified.set(true);
            }
        });
        assertEquals(1, realm.handlerController.changeListeners.size());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    subscription.unsubscribe();
                    fail();
                } catch (IllegalStateException ignored) {
                } finally {
                    unsubscribeCompleted.countDown();
                }
            }
        }).start();
        TestHelper.awaitOrFail(unsubscribeCompleted);
        assertEquals(1, realm.handlerController.changeListeners.size());
    }

    public void testWrongGenericClassThrows() {
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        Observable<CyclicType> obs = obj.asObservable();
        obs.subscribe(new Action1<CyclicType>() {
            @Override
            public void call(CyclicType cyclicType) {
                fail();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable ignored) {
            }
        });
    }
}
