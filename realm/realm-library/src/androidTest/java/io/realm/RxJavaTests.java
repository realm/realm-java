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
import java.util.concurrent.atomic.AtomicLong;

import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RxJavaTests {

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread() {
        @Override
        public void looperTearDown() {
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
    };
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm realm;
    private Subscription subscription;

    @Before
    public void setUp() throws Exception {
        // For non-LooperThread tests
        realm = Realm.getInstance(configFactory.createConfiguration());
    }

    @After
    public void tearDown() throws Exception {
        // For non-LooperThread tests
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    @UiThreadTest
    public void realmObject_emittedOnSubscribe() {
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        subscription = obj.<AllTypes>asObservable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                assertTrue(rxObject == obj);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.unsubscribe();
    }

    @Test
    @RunTestInLooperThread
    public void realmObject_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.realm;
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        subscription = obj.<AllTypes>asObservable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                if (subscriberCalled.incrementAndGet() == 2) {
                    looperThread.testComplete();
                }
            }
        });

        realm.beginTransaction();
        obj.setColumnLong(1);
        realm.commitTransaction();
    }

    @Test
    @UiThreadTest
    public void findFirst_emittedOnSubscribe() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class).setColumnLong(42);
        realm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        subscription = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 42).findFirst().<AllTypes>asObservable()
                .subscribe(new Action1<AllTypes>() {
                    @Override
                    public void call(AllTypes rxObject) {
                        subscribedNotified.set(true);
                    }
                });
        assertTrue(subscribedNotified.get());
        subscription.unsubscribe();
    }

    @Test
    @UiThreadTest
    public void findFirstAsync_emittedOnSubscribe() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final AllTypes asyncObj = realm.where(AllTypes.class).findFirstAsync();
        subscription = asyncObj.<AllTypes>asObservable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                assertTrue(rxObject == asyncObj);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.unsubscribe();
    }

    @Test
    @RunTestInLooperThread
    public void findFirstAsync_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.realm;
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();
        subscription = realm.where(AllTypes.class).findFirstAsync().<AllTypes>asObservable().subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes rxObject) {
                if (subscriberCalled.incrementAndGet() == 2) {
                    looperThread.testComplete();
                }
            }
        });

        realm.beginTransaction();
        obj.setColumnLong(1);
        realm.commitTransaction();
    }

    @Test
    @UiThreadTest
    public void realmResults_emittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<AllTypes> results = realm.allObjects(AllTypes.class);
        subscription = results.asObservable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> rxResults) {
                assertTrue(rxResults == results);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.unsubscribe();
    }

    @Test
    @UiThreadTest
    public void dynamicRealmResults_emittedOnSubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.createInstance(realm.getConfiguration());
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<DynamicRealmObject> results = dynamicRealm.allObjects(AllTypes.CLASS_NAME);
        results.asObservable().subscribe(new Action1<RealmResults<DynamicRealmObject>>() {
            @Override
            public void call(RealmResults<DynamicRealmObject> rxResults) {
                assertTrue(rxResults == results);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        dynamicRealm.close();
    }

    @Test
    @RunTestInLooperThread
    public void realmResults_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.realm;
        realm.beginTransaction();
        RealmResults<AllTypes> results = realm.allObjects(AllTypes.class);
        realm.commitTransaction();

        subscription = results.asObservable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> allTypes) {
                if (subscriberCalled.incrementAndGet() == 2) {
                    looperThread.testComplete();
                }
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void dynamicRealmResults_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        final DynamicRealm dynamicRealm = DynamicRealm.createInstance(looperThread.realmConfiguration);
        dynamicRealm.beginTransaction();
        RealmResults<DynamicRealmObject> results = dynamicRealm.allObjects(AllTypes.CLASS_NAME);
        dynamicRealm.commitTransaction();

        results.asObservable().subscribe(new Action1<RealmResults<DynamicRealmObject>>() {
            @Override
            public void call(RealmResults<DynamicRealmObject> allTypes) {
                if (subscriberCalled.incrementAndGet() == 2) {
                    dynamicRealm.close();
                    looperThread.testComplete();
                }
            }
        });

        dynamicRealm.beginTransaction();
        dynamicRealm.createObject(AllTypes.CLASS_NAME);
        dynamicRealm.commitTransaction();
    }

    @Test
    @UiThreadTest
    public void findAllAsync_emittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
        subscription = results.asObservable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> rxResults) {
                assertTrue(rxResults == results);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.unsubscribe();
    }

    @Test
    @RunTestInLooperThread
    public void findAllAsync_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.realm;
        subscription = realm.where(AllTypes.class).findAllAsync().asObservable().subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> rxResults) {
                if (subscriberCalled.incrementAndGet() == 2) {
                    looperThread.testComplete();
                }
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    @UiThreadTest
    public void realm_emittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        subscription = realm.asObservable().subscribe(new Action1<Realm>() {
            @Override
            public void call(Realm rxRealm) {
                assertTrue(rxRealm == realm);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.unsubscribe();
    }

    @Test
    @RunTestInLooperThread
    public void realm_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.realm;
        subscription = realm.asObservable().subscribe(new Action1<Realm>() {
            @Override
            public void call(Realm rxRealm) {
                if (subscriberCalled.incrementAndGet() == 2) {
                    looperThread.testComplete();
                }
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    @UiThreadTest
    public void dynamicRealm_emittedOnSubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        subscription = dynamicRealm.asObservable().subscribe(new Action1<DynamicRealm>() {
            @Override
            public void call(DynamicRealm rxRealm) {
                assertTrue(rxRealm == dynamicRealm);
                subscribedNotified.set(true);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        assertTrue(subscribedNotified.get());
        dynamicRealm.close();
        subscription.unsubscribe();
    }

    @Test
    @RunTestInLooperThread
    public void dynamicRealm_emittedOnUpdate() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.realmConfiguration);
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        subscription = dynamicRealm.asObservable().subscribe(new Action1<DynamicRealm>() {
            @Override
            public void call(DynamicRealm rxRealm) {
                if (subscriberCalled.incrementAndGet() == 2) {
                    dynamicRealm.close();
                    looperThread.testComplete();
                }
            }
        });

        dynamicRealm.beginTransaction();
        dynamicRealm.createObject("AllTypes");
        dynamicRealm.commitTransaction();
    }

    @Test
    @UiThreadTest
    public void unsubscribe_sameThread() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        subscription = realm.asObservable().subscribe(new Action1<Realm>() {
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
    public void unsubscribe_fromOtherThread() {
        final CountDownLatch unsubscribeCompleted = new CountDownLatch(1);
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final Subscription subscription = realm.asObservable().subscribe(new Action1<Realm>() {
            @Override
            public void call(Realm rxRealm) {
                assertTrue(rxRealm == realm);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
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
        // We cannot call subscription.unsubscribe() again, so manually close the extra Realm instance opened by
        // the Observable.
        realm.close();
    }

    @Test
    @UiThreadTest
    public void wrongGenericClassThrows() {
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

    @Test
    @UiThreadTest
    public void realm_closeInDoOnUnsubscribe() {
        Observable<Realm> observable = realm.asObservable()
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        realm.close();
                    }
                });

        subscription = observable.subscribe(new Action1<Realm>() {
            @Override
            public void call(Realm rxRealm) {
            }
        });

        subscription.unsubscribe();
        assertTrue(realm.isClosed());
    }

    @Test
    @UiThreadTest
    public void dynamicRealm_closeInDoOnUnsubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());

        Observable<DynamicRealm> observable = dynamicRealm.asObservable()
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        dynamicRealm.close();
                    }
                });

        subscription = observable.subscribe(new Action1<DynamicRealm>() {
            @Override
            public void call(DynamicRealm rxRealm) {
            }
        });

        subscription.unsubscribe();
        assertTrue(dynamicRealm.isClosed());
    }

    @Test
    @UiThreadTest
    public void realmResults_closeInDoOnUnsubscribe() {
        Observable<RealmResults<AllTypes>> observable = realm.allObjects(AllTypes.class).asObservable()
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        realm.close();
                    }
                });

        subscription = observable.subscribe(new Action1<RealmResults<AllTypes>>() {
            @Override
            public void call(RealmResults<AllTypes> allTypes) {
            }
        });

        subscription.unsubscribe();
        assertTrue(realm.isClosed());
    }

    @Test
    @UiThreadTest
    public void dynamicRealmResults_closeInDoOnUnsubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());

        Observable<RealmResults<DynamicRealmObject>> observable = dynamicRealm.allObjects(AllTypes.CLASS_NAME).asObservable()
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        dynamicRealm.close();
                    }
                });

        subscription = observable.subscribe(new Action1<RealmResults<DynamicRealmObject>>() {
            @Override
            public void call(RealmResults<DynamicRealmObject> allTypes) {
            }
        });

        subscription.unsubscribe();
        assertTrue(dynamicRealm.isClosed());
    }

    @Test
    @UiThreadTest
    public void realmObject_closeInDoOnUnsubscribe() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        Observable<AllTypes> observable = realm.allObjects(AllTypes.class).first().<AllTypes>asObservable()
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        realm.close();
                    }
                });

        subscription = observable.subscribe(new Action1<AllTypes>() {
            @Override
            public void call(AllTypes allTypes) {
            }
        });

        subscription.unsubscribe();
        assertTrue(realm.isClosed());
    }

    @Test
    @UiThreadTest
    public void dynamicRealmObject_closeInDoOnUnsubscribe() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());

        Observable<DynamicRealmObject> observable = dynamicRealm.allObjects(AllTypes.CLASS_NAME).first().<DynamicRealmObject>asObservable()
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        dynamicRealm.close();
                    }
                });

        subscription = observable.subscribe(new Action1<DynamicRealmObject>() {
            @Override
            public void call(DynamicRealmObject obj) {
            }
        });

        subscription.unsubscribe();
        assertTrue(dynamicRealm.isClosed());
    }

    // Tests that Observables keep strong references to their parent, so they are not accidentally GC'ed while
    // waiting for results from the async API's.
    @Test
    @RunTestInLooperThread
    public void realmResults_gcStressTest() {
        final int TEST_SIZE = 50;
        final AtomicLong innerCounter = new AtomicLong();
        final Realm realm = looperThread.realm;

        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(AllTypes.class).setColumnLong(i);
        }
        realm.commitTransaction();

        for (int i = 0; i < TEST_SIZE; i++) {
            // Don't keep a reference to the Observable
            realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, i).findAllAsync().asObservable()
                    .filter(new Func1<RealmResults<AllTypes>, Boolean>() {
                        @Override
                        public Boolean call(RealmResults<AllTypes> results) {
                            return results.isLoaded();
                        }
                    })
                    .take(1) // Unsubscribes from Realm
                    .subscribe(new Action1<RealmResults<AllTypes>>() {
                        @Override
                        public void call(RealmResults<AllTypes> result) {
                            // Not guaranteed, but can result in the GC of other RealmResults waiting for a result
                            Runtime.getRuntime().gc();
                            if (innerCounter.incrementAndGet() == TEST_SIZE) {
                                looperThread.testComplete();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            fail(throwable.toString());
                        }
                    });
        }
    }

    // Tests that Observables keep strong references to their parent, so they are not accidentally GC'ed while
    // waiting for results from the async API's.
    @Test
    @RunTestInLooperThread
    public void dynamicRealmResults_gcStressTest() {
        final int TEST_SIZE = 50;
        final AtomicLong innerCounter = new AtomicLong();
        final DynamicRealm realm = DynamicRealm.getInstance(looperThread.realmConfiguration);

        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(AllTypes.CLASS_NAME).set(AllTypes.FIELD_LONG, i);
        }
        realm.commitTransaction();

        for (int i = 0; i < TEST_SIZE; i++) {
            // Don't keep a reference to the Observable
            realm.where(AllTypes.CLASS_NAME).equalTo(AllTypes.FIELD_LONG, i).findAllAsync().asObservable()
                    .filter(new Func1<RealmResults<DynamicRealmObject>, Boolean>() {
                        @Override
                        public Boolean call(RealmResults<DynamicRealmObject> results) {
                            return results.isLoaded();
                        }
                    })
                    .take(1) // Unsubscribes from Realm
                    .subscribe(new Action1<RealmResults<DynamicRealmObject>>() {
                        @Override
                        public void call(RealmResults<DynamicRealmObject> result) {
                            // Not guaranteed, but can result in the GC of other RealmResults waiting for a result
                            Runtime.getRuntime().gc();
                            if (innerCounter.incrementAndGet() == TEST_SIZE) {
                                realm.close();
                                looperThread.testComplete();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            fail(throwable.toString());
                        }
                    });
        }
    }

    // Tests that Observables keep strong references to their parent, so they are not accidentally GC'ed while
    // waiting for results from the async API's.
    @Test
    @RunTestInLooperThread
    public void realmObject_gcStressTest() {
        final int TEST_SIZE = 50;
        final AtomicLong innerCounter = new AtomicLong();
        final Realm realm = looperThread.realm;

        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(AllTypes.class).setColumnLong(i);
        }
        realm.commitTransaction();

        for (int i = 0; i < TEST_SIZE; i++) {
            // Don't keep a reference to the Observable
            realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, i).findFirstAsync().<AllTypes>asObservable()
                    .filter(new Func1<AllTypes, Boolean>() {
                        @Override
                        public Boolean call(AllTypes obj) {
                            return obj.isLoaded();
                        }
                    })
                    .take(1) // Unsubscribes from Realm
                    .subscribe(new Action1<AllTypes>() {
                        @Override
                        public void call(AllTypes result) {
                            // Not guaranteed, but can result in the GC of other RealmResults waiting for a result
                            Runtime.getRuntime().gc();
                            if (innerCounter.incrementAndGet() == TEST_SIZE) {
                                looperThread.testComplete();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            fail(throwable.toString());
                        }
                    });
        }
    }

    // Tests that Observables keep strong references to their parent, so they are not accidentally GC'ed while
    // waiting for results from the async API's.
    @Test
    @RunTestInLooperThread
    public void dynamicRealmObject_gcStressTest() {
        final int TEST_SIZE = 50;
        final AtomicLong innerCounter = new AtomicLong();
        final DynamicRealm realm = DynamicRealm.getInstance(looperThread.realmConfiguration);

        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(AllTypes.CLASS_NAME).set(AllTypes.FIELD_LONG, i);
        }
        realm.commitTransaction();

        for (int i = 0; i < TEST_SIZE; i++) {
            // Don't keep a reference to the Observable
            realm.where(AllTypes.CLASS_NAME).equalTo(AllTypes.FIELD_LONG, i).findFirstAsync().<DynamicRealmObject>asObservable()
                    .filter(new Func1<DynamicRealmObject, Boolean>() {
                        @Override
                        public Boolean call(DynamicRealmObject obj) {
                            return obj.isLoaded();
                        }
                    })
                    .take(1) // Unsubscribes from Realm
                    .subscribe(new Action1<DynamicRealmObject>() {
                        @Override
                        public void call(DynamicRealmObject result) {
                            // Not guaranteed, but can result in the GC of other RealmResults waiting for a result
                            Runtime.getRuntime().gc();
                            if (innerCounter.incrementAndGet() == TEST_SIZE) {
                                realm.close();
                                looperThread.testComplete();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            fail(throwable.toString());
                        }
                    });
        }
    }

}
