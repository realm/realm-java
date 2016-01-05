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
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RxJavaTests {
    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private RealmConfiguration realmConfig;
    private Realm realm;

    @Before
    public void setUp() throws Exception {
        realmConfig = TestHelper.createConfiguration(tempDir.getRoot(), Realm.DEFAULT_REALM_NAME);
        Realm.deleteRealm(realmConfig);
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() throws Exception {
        realm.close();
    }

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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

    @Test
    @UiThreadTest
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
