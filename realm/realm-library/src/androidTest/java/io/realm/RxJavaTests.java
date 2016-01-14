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

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import io.realm.rule.TestRealmConfigurationFactory;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RxJavaTests {

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @Rule
    public final TestRealmConfigurationFactory configurationFactory = new TestRealmConfigurationFactory();

    private RealmConfiguration realmConfig;
    private Realm realm;
    private Subscription subscription;

    @Before
    public void setUp() throws Exception {
        realmConfig = configurationFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() throws Exception {
        realm.close();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @Test
    @UiThreadTest
    public void realmObject_asObservable_emittedOnSubscribe() {
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

    @Test
    @UiThreadTest
    public void realmObject_asObservable_emittedOnUpdate() {
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

    @Test
    @UiThreadTest
    public void asyncRealmObject_asObservable_emittedOnSubscribe() {
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

    @Test
    @UiThreadTest
    public void asyncRealmObject_asObservable_emittedOnUpdate() {
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

    @Test
    @UiThreadTest
    public void realmResults_asObservable_emittedOnSubscribe() {
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

    @Test
    @UiThreadTest
    public void realmResults_asObservable_emittedOnUpdate() {
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

    @Test
    @UiThreadTest
    public void asyncRealmResults_asObservable_emittedOnSubscribe() {
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

    @Test
    @UiThreadTest
    public void asyncRealmResults_asObservable_emittedOnUpdate() {
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

    @Test
    @UiThreadTest
    public void realm_asObservable_emittedOnSubscribe() {
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

    @Test
    @UiThreadTest
    public void realm_asObservable_emittedOnUpdate() {
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

    @Test
    @UiThreadTest
    public void dynamicRealm_asObservable_emittedOnSubscribe() {
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

    @Test
    @UiThreadTest
    public void dynamicRealm_asObservable_emittedOnUpdate() {
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

    @Test
    @UiThreadTest
    public void realmQuery_asObservable_emittedOnSubscribe() {
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
    @Test
    @UiThreadTest
    public void realmQuery_asObservable_subscribeOnDifferentScheduler() {
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
    @Test
    @UiThreadTest
    public void realmQuery_asObservable_resultAccessedOnOtherScheduler() {
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
                    public void call(Throwable expected) {
                        subscriptionFailed.countDown();
                    }
                });
        TestHelper.awaitOrFail(subscriptionFailed);
    }

    // Make sure that further changes to the Realm query doesn't effect the state when it it was observed on.
    @Test
    @UiThreadTest
    public void realmQuery_asObservable_isACopy() {
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

    @Test
    @UiThreadTest
    public void realmQuery_asObservable_backingRealmClosedWhenUnsubscribing() {
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

    @Test
    @UiThreadTest
    public void realm_asObservable_unsubscribe() {
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

    @Test
    @UiThreadTest
    public void realm_asObservable_unsubscribingFromOtherThreadFails() {
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

    @Test
    @UiThreadTest
    public void realmObject_asObservable_wrongGenericClassThrows() {
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
