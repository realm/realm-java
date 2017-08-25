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

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;
import io.realm.rx.CollectionChange;
import io.realm.rx.ObjectChange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
            if (subscription != null && !subscription.isDisposed()) {
                subscription.dispose();
            }
        }
    };
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm realm;
    private Disposable subscription;

    @Before
    public void setUp() throws Exception {
        // For non-LooperThread tests.
        realm = Realm.getInstance(configFactory.createConfiguration());
    }

    @After
    public void tearDown() throws Exception {
        // For non-LooperThread tests.
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
        subscription = obj.<AllTypes>asFlowable().subscribe(new Consumer <AllTypes>() {
            @Override
            public void accept(AllTypes rxObject) throws Exception {
                assertTrue(rxObject == obj);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @UiThreadTest
    public void realmObject_emitChangesetOnSubscribe() {
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        subscription = obj.asChangesetObservable().subscribe(new Consumer<ObjectChange<RealmObject>>() {
            @Override
            public void accept(ObjectChange<RealmObject> change) throws Exception {
                assertTrue(change.getObject() == obj);
                assertNull(change.getChangeset());
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @UiThreadTest
    public void dynamicRealmObject_emitChangesetOnSubscribe() {
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        dynamicRealm.beginTransaction();
        final DynamicRealmObject obj = dynamicRealm.createObject(AllTypes.CLASS_NAME);
        dynamicRealm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        subscription = obj.asChangesetObservable().subscribe(new Consumer<ObjectChange<RealmObject>>() {
            @Override
            public void accept(ObjectChange<RealmObject> change) throws Exception {
                assertTrue(change.getObject() == obj);
                assertNull(change.getChangeset());
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
        dynamicRealm.close();
    }

    @Test
    @RunTestInLooperThread
    public void realmObject_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        subscription = obj.<AllTypes>asFlowable().subscribe(new Consumer<AllTypes>() {
            @Override
            public void accept(AllTypes allTypes) throws Exception {
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
    @RunTestInLooperThread
    public void realmObject_emittedChangesetOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        subscription = obj.asChangesetObservable().subscribe(new Consumer<ObjectChange<RealmObject>>() {
            @Override
            public void accept(ObjectChange<RealmObject> change) throws Exception {
                if (subscriberCalled.incrementAndGet() == 2) {
                    assertNotNull(change.getChangeset());
                    assertTrue(change.getChangeset().isFieldChanged(AllTypes.FIELD_LONG));
                    looperThread.testComplete();
                }
            }
        });

        realm.beginTransaction();
        obj.setColumnLong(1);
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void dynamicRealmObject_emittedChangesetOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        DynamicRealm realm = DynamicRealm.getInstance(looperThread.getConfiguration());
        looperThread.closeAfterTest(realm);
        realm.beginTransaction();
        final DynamicRealmObject obj = realm.createObject(AllTypes.CLASS_NAME);
        realm.commitTransaction();

        subscription = obj.asChangesetObservable().subscribe(new Consumer<ObjectChange<RealmObject>>() {
            @Override
            public void accept(ObjectChange<RealmObject> change) throws Exception {
                if (subscriberCalled.incrementAndGet() == 2) {
                    assertNotNull(change.getChangeset());
                    assertTrue(change.getChangeset().isFieldChanged(AllTypes.FIELD_LONG));
                    looperThread.testComplete();
                }
            }
        });
        realm.beginTransaction();
        obj.setLong(AllTypes.FIELD_LONG, 1);
        realm.commitTransaction();
    }

    @Test
    @UiThreadTest
    public void findFirst_emittedOnSubscribe() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class).setColumnLong(42);
        realm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        subscription = realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, 42).findFirst().<AllTypes>asFlowable()
                .subscribe(new Consumer <AllTypes>() {
                    @Override
                    public void accept(AllTypes allTypes) throws Exception {
                        subscribedNotified.set(true);
                    }
                });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @UiThreadTest
    public void findFirstAsync_emittedOnSubscribe() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final AllTypes asyncObj = realm.where(AllTypes.class).findFirstAsync();
        subscription = asyncObj.<AllTypes>asFlowable().subscribe(new Consumer<AllTypes>() {
            @Override
            public void accept(AllTypes rxObject) throws Exception {
                assertTrue(rxObject == asyncObj);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @RunTestInLooperThread
    public void findFirstAsync_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();
        subscription = realm.where(AllTypes.class).findFirstAsync().<AllTypes>asFlowable().subscribe(new Consumer<AllTypes>() {
            @Override
            public void accept(AllTypes rxObject) throws Exception {
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
    @RunTestInLooperThread
    public void findFirstAsync_emittedOnDelete() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        final Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        subscription = realm.where(AllTypes.class).findFirstAsync().<AllTypes>asFlowable().subscribe(new Consumer<AllTypes>() {
            @Override
            public void accept(AllTypes rxObject) throws Exception {
                switch (subscriberCalled.incrementAndGet()) {
                    case 1:
                        assertFalse(rxObject.isLoaded());
                        break;
                    case 2:
                        assertTrue(rxObject.isLoaded());
                        assertTrue(rxObject.isValid());
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.delete(AllTypes.class);
                            }
                        });
                        break;
                    case 3:
                        assertTrue(rxObject.isLoaded());
                        assertFalse(rxObject.isValid());
                        looperThread.testComplete();
                        break;
                    default:
                        fail();
                }
            }
        });
    }

    @Test
    @UiThreadTest
    public void realmResults_emittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        subscription = results.asFlowable().subscribe(new Consumer<RealmResults<AllTypes>>() {
            @Override
            @SuppressWarnings("ReferenceEquality")
            public void accept(RealmResults<AllTypes> rxResults) throws Exception {
                assertTrue(rxResults == results);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @UiThreadTest
    public void realmResults_emittedChangesetOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        subscription = results.asChangesetObservable().subscribe(new Consumer<CollectionChange<RealmResults<AllTypes>>>() {
            @Override
            public void accept(CollectionChange<RealmResults<AllTypes>> change) throws Exception {
                assertEquals(results, change.getCollection());
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @UiThreadTest
    public void realmList_emittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        realm.beginTransaction();
        final RealmList<Dog> list = realm.createObject(AllTypes.class).getColumnRealmList();
        realm.commitTransaction();
        subscription = list.asFlowable().subscribe(new Consumer<RealmList<Dog>>() {
            @Override
            @SuppressWarnings("ReferenceEquality")
            public void accept(RealmList<Dog> rxList) throws Exception {
                assertTrue(rxList == list);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @UiThreadTest
    public void realmList_emittedChangesetOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        realm.beginTransaction();
        final RealmList<Dog> list = realm.createObject(AllTypes.class).getColumnRealmList();
        realm.commitTransaction();
        subscription = list.asChangesetObservable().subscribe(new Consumer<CollectionChange<RealmList<Dog>>>() {
            @Override
            public void accept(CollectionChange<RealmList<Dog>> change) throws Exception {
                assertEquals(list, change.getCollection());
                assertNull(change.getChangeset());
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @UiThreadTest
    public void dynamicRealmResults_emittedOnSubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<DynamicRealmObject> results = dynamicRealm.where(AllTypes.CLASS_NAME).findAll();
        subscription = results.asFlowable().subscribe(new Consumer<RealmResults<DynamicRealmObject>>() {
            @Override
            @SuppressWarnings("ReferenceEquality")
            public void accept(RealmResults<DynamicRealmObject> rxResults) throws Exception {
                assertTrue(rxResults == results);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        dynamicRealm.close();
        subscription.dispose();
    }

    @Test
    @UiThreadTest
    public void dynamicRealmResults_emittedChangesetOnSubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<DynamicRealmObject> results = dynamicRealm.where(AllTypes.CLASS_NAME).findAll();
        subscription = results.asChangesetObservable().subscribe(new Consumer<CollectionChange<RealmResults<DynamicRealmObject>>>() {
            @Override
            public void accept(CollectionChange<RealmResults<DynamicRealmObject>> change) throws Exception {
                assertEquals(results, change.getCollection());
                assertNull(change.getChangeset());
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        dynamicRealm.close();
        subscription.dispose();
    }

    @Test
    @RunTestInLooperThread
    public void realmResults_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        realm.commitTransaction();

        subscription = results.asFlowable().subscribe(new Consumer<RealmResults<AllTypes>>() {
            @Override
            public void accept(RealmResults<AllTypes> allTypes) throws Exception {
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
    public void realmResults_emittedChangesetOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        realm.commitTransaction();

        subscription = results.asChangesetObservable().subscribe(new Consumer<CollectionChange<RealmResults<AllTypes>>>() {
            @Override
            public void accept(CollectionChange<RealmResults<AllTypes>> change) throws Exception {
                if (subscriberCalled.incrementAndGet() == 2) {
                    assertEquals(1, change.getChangeset().getInsertions().length);
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
    public void realmList_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        final RealmList<Dog> list = realm.createObject(AllTypes.class).getColumnRealmList();
        realm.commitTransaction();

        subscription = list.asFlowable().subscribe(new Consumer<RealmList<Dog>>() {
            @Override
            public void accept(RealmList<Dog> dogs) throws Exception {
                if (subscriberCalled.incrementAndGet() == 2) {
                    assertEquals(1, list.size());
                    looperThread.testComplete();
                }
            }
        });

        realm.beginTransaction();
        list.add(new Dog());
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void realmList_emittedChangesetOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.getRealm();
        realm.beginTransaction();
        final RealmList<Dog> list = realm.createObject(AllTypes.class).getColumnRealmList();
        realm.commitTransaction();

        subscription = list.asChangesetObservable().subscribe(new Consumer<CollectionChange<RealmList<Dog>>>() {
            @Override
            public void accept(CollectionChange<RealmList<Dog>> change) throws Exception {
                if (subscriberCalled.incrementAndGet() == 2) {
                    assertEquals(1, list.size());
                    assertEquals(1, change.getChangeset().getInsertions().length);
                    looperThread.testComplete();
                }
            }
        });

        realm.beginTransaction();
        list.add(new Dog());
        realm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void dynamicRealmResults_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.getConfiguration());
        dynamicRealm.beginTransaction();
        RealmResults<DynamicRealmObject> results = dynamicRealm.where(AllTypes.CLASS_NAME).findAll();
        dynamicRealm.commitTransaction();

        subscription = results.asFlowable().subscribe(new Consumer<RealmResults<DynamicRealmObject>>() {
            @Override
            public void accept(RealmResults<DynamicRealmObject> dynamicRealmObjects) throws Exception {
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
    @RunTestInLooperThread
    public void dynamicRealmResults_emittedChangesetOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.getConfiguration());
        looperThread.closeAfterTest(dynamicRealm);
        dynamicRealm.beginTransaction();
        RealmResults<DynamicRealmObject> results = dynamicRealm.where(AllTypes.CLASS_NAME).findAll();
        dynamicRealm.commitTransaction();

        subscription = results.asChangesetObservable().subscribe(new Consumer<CollectionChange<RealmResults<DynamicRealmObject>>>() {
            @Override
            public void accept(CollectionChange<RealmResults<DynamicRealmObject>> change) throws Exception {
                if (subscriberCalled.incrementAndGet() == 2) {
                    assertEquals(1, change.getChangeset().getInsertions().length);
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
        subscription = results.asFlowable().subscribe(new Consumer<RealmResults<AllTypes>>() {
            @Override
            @SuppressWarnings("ReferenceEquality")
            public void accept(RealmResults<AllTypes> rxResults) throws Exception {
                assertTrue(rxResults == results);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @RunTestInLooperThread
    public void findAllAsync_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.getRealm();
        subscription = realm.where(AllTypes.class).findAllAsync().asFlowable().subscribe(new Consumer<RealmResults<AllTypes>>() {
            @Override
            public void accept(RealmResults<AllTypes> allTypes) throws Exception {
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
        subscription = realm.asFlowable().subscribe(new Consumer<Realm>() {
            @Override
            public void accept(Realm rxRealm) throws Exception {
                assertTrue(rxRealm == realm);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @RunTestInLooperThread
    public void realm_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.getRealm();
        subscription = realm.asFlowable().subscribe(new Consumer<Realm>() {
            @Override
            public void accept(Realm realm) throws Exception {
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
        subscription = dynamicRealm.asFlowable().subscribe(new Consumer<DynamicRealm>() {
            @Override
            public void accept(DynamicRealm rxRealm) throws Exception {
                assertTrue(rxRealm == dynamicRealm);
                subscribedNotified.set(true);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
                fail();
            }
        });

        assertTrue(subscribedNotified.get());
        dynamicRealm.close();
        subscription.dispose();
    }

    @Test
    @RunTestInLooperThread
    public void dynamicRealm_emittedOnUpdate() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.getConfiguration());
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        subscription = dynamicRealm.asFlowable().subscribe(new Consumer<DynamicRealm>() {
            @Override
            public void accept(DynamicRealm dynamicRealm) throws Exception {
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
        subscription = realm.asFlowable().subscribe(new Consumer<Realm>() {
            @Override
            public void accept(Realm rxRealm) throws Exception {
                assertTrue(rxRealm == realm);
                subscribedNotified.set(true);
            }
        });
        assertEquals(1, realm.sharedRealm.realmNotifier.getListenersListSize());
        subscription.dispose();
        assertEquals(0, realm.sharedRealm.realmNotifier.getListenersListSize());
    }

    @Test
    @UiThreadTest
    public void unsubscribe_fromOtherThread() {
        final CountDownLatch unsubscribeCompleted = new CountDownLatch(1);
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final Disposable subscription = realm.asFlowable().subscribe(new Consumer<Realm>() {
            @Override
            public void accept(Realm rxRealm) throws Exception {
                assertTrue(rxRealm == realm);
                subscribedNotified.set(true);
            }
        });
        assertTrue(subscribedNotified.get());
        assertEquals(1, realm.sharedRealm.realmNotifier.getListenersListSize());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    subscription.dispose();
                    fail();
                } catch (IllegalStateException ignored) {
                } finally {
                    unsubscribeCompleted.countDown();
                }
            }
        }).start();
        TestHelper.awaitOrFail(unsubscribeCompleted);
        assertEquals(1, realm.sharedRealm.realmNotifier.getListenersListSize());
        // We cannot call subscription.dispose() again, so manually close the extra Realm instance opened by
        // the Observable.
        realm.close();
    }

    @Test
    @UiThreadTest
    public void wrongGenericClassThrows() {
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        Flowable<CyclicType> obs = obj.asFlowable();
        @SuppressWarnings("unused")
        Disposable subscription = obs.subscribe(new Consumer<CyclicType>() {
            @Override
            public void accept(CyclicType cyclicType) throws Exception {
                fail();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable ignored) throws Exception {
            }
        });
    }

    @Test
    @UiThreadTest
    public void realm_closeInDoOnUnsubscribe() {
        Flowable<Realm> observable = realm.asFlowable()
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        realm.close();
                    }
                });

        subscription = observable.subscribe(new Consumer<Realm>() {
            @Override
            public void accept(Realm realm) throws Exception {
                assertEquals(2, Realm.getLocalInstanceCount(realm.getConfiguration()));
            }
        });

        subscription.dispose();
        assertTrue(realm.isClosed());
    }

    @Test
    @UiThreadTest
    public void dynamicRealm_closeInDoOnUnsubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());

        Flowable<DynamicRealm> observable = dynamicRealm.asFlowable()
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        dynamicRealm.close();
                    }
                });

        subscription = observable.subscribe(new Consumer<DynamicRealm>() {
            @Override
            public void accept(DynamicRealm ignored) throws Exception {
            }
        });

        subscription.dispose();
        assertTrue(dynamicRealm.isClosed());
    }

    @Test
    @UiThreadTest
    public void realmResults_closeInDoOnUnsubscribe() {
        Flowable<RealmResults<AllTypes>> observable = realm.where(AllTypes.class).findAll().asFlowable()
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        realm.close();
                    }
                });

        subscription = observable.subscribe(new Consumer<RealmResults<AllTypes>>() {
            @Override
            public void accept(RealmResults<AllTypes> ignored) throws Exception {
            }
        });

        subscription.dispose();
        assertTrue(realm.isClosed());
    }

    @Test
    @UiThreadTest
    public void realmList_closeInDoOnUnsubscribe() {
        realm.beginTransaction();
        RealmList<Dog> list = realm.createObject(AllTypes.class).getColumnRealmList();
        realm.commitTransaction();

        Flowable<RealmList<Dog>> observable = list.asFlowable().doOnCancel(new Action() {
            @Override
            public void run() throws Exception {
                realm.close();
            }
        });
        subscription = observable.subscribe(new Consumer<RealmList<Dog>>() {
            @Override
            public void accept(RealmList<Dog> ignored) throws Exception {
            }
        });

        subscription.dispose();
        assertTrue(realm.isClosed());
    }

    @Test
    @UiThreadTest
    public void dynamicRealmResults_closeInDoOnUnsubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());

        Flowable<RealmResults<DynamicRealmObject>> flowable = dynamicRealm.where(AllTypes.CLASS_NAME).findAll().asFlowable()
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        dynamicRealm.close();
                    }
                });

        subscription = flowable.subscribe(new Consumer<RealmResults<DynamicRealmObject>>() {
            @Override
            public void accept(RealmResults<DynamicRealmObject> ignored) throws Exception {
            }
        });

        subscription.dispose();
        assertTrue(dynamicRealm.isClosed());
    }

    @Test
    @UiThreadTest
    public void realmObject_closeInDoOnUnsubscribe() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();

        Flowable<AllTypes> flowable = realm.where(AllTypes.class).findFirst().<AllTypes>asFlowable()
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        realm.close();
                    }
                });

        subscription = flowable.subscribe(new Consumer<AllTypes>() {
            @Override
            public void accept(AllTypes ignored) throws Exception {
            }
        });

        subscription.dispose();
        assertTrue(realm.isClosed());
    }

    @Test
    @UiThreadTest
    public void dynamicRealmObject_closeInDoOnUnsubscribe() {
        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());

        Flowable<DynamicRealmObject> flowable = dynamicRealm.where(AllTypes.CLASS_NAME).findFirst().<DynamicRealmObject>asFlowable()
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        dynamicRealm.close();
                    }
                });

        subscription = flowable.subscribe(new Consumer<DynamicRealmObject>() {
            @Override
            public void accept(DynamicRealmObject ignored) throws Exception {
            }
        });

        subscription.dispose();
        assertTrue(dynamicRealm.isClosed());
    }

    // Tests that Observables keep strong references to their parent, so they are not accidentally GC'ed while
    // waiting for results from the async API's.
    @Test
    @RunTestInLooperThread
    @SuppressWarnings("CheckReturnValue")
    public void realmResults_gcStressTest() {
        final int TEST_SIZE = 50;
        final AtomicLong innerCounter = new AtomicLong();
        final Realm realm = looperThread.getRealm();

        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(AllTypes.class).setColumnLong(i);
        }
        realm.commitTransaction();

        for (int i = 0; i < TEST_SIZE; i++) {
            // Doesn't keep a reference to the Observable.
            realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, i).findAllAsync().asFlowable()
                    .filter(new Predicate<RealmResults<AllTypes>>() {
                        @Override
                        public boolean test(RealmResults<AllTypes> results) throws Exception {
                            return results.isLoaded();
                        }
                    })
                    .take(1) // Unsubscribes from Realm.
                    .subscribe(new Consumer<RealmResults<AllTypes>>() {
                        @Override
                        public void accept(RealmResults<AllTypes> allTypes) throws Exception {
                            // Not guaranteed, but can result in the GC of other RealmResults waiting for a result.
                            Runtime.getRuntime().gc();
                            if (innerCounter.incrementAndGet() == TEST_SIZE) {
                                looperThread.testComplete();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            fail(throwable.toString());
                        }
                    });
        }
    }

    // Tests that Observables keep strong references to their parent, so they are not accidentally GC'ed while
    // waiting for results from the async API's.
    @Test
    @RunTestInLooperThread
    @SuppressWarnings("CheckReturnValue")
    public void dynamicRealmResults_gcStressTest() {
        final int TEST_SIZE = 50;
        final AtomicLong innerCounter = new AtomicLong();
        final DynamicRealm realm = DynamicRealm.getInstance(looperThread.getConfiguration());

        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(AllTypes.CLASS_NAME).set(AllTypes.FIELD_LONG, i);
        }
        realm.commitTransaction();

        for (int i = 0; i < TEST_SIZE; i++) {
            // Doesn't keep a reference to the Observable.
            realm.where(AllTypes.CLASS_NAME).equalTo(AllTypes.FIELD_LONG, i).findAllAsync().asFlowable()
                    .filter(new Predicate<RealmResults<DynamicRealmObject>>() {
                        @Override
                        public boolean test(RealmResults<DynamicRealmObject> results) throws Exception {
                            return results.isLoaded();
                        }
                    })
                    .take(1) // Unsubscribes from Realm.
                    .subscribe(new Consumer<RealmResults<DynamicRealmObject>>() {
                        @Override
                        public void accept(RealmResults<DynamicRealmObject> dynamicRealmObjects) throws Exception {
                            // Not guaranteed, but can result in the GC of other RealmResults waiting for a result.
                            Runtime.getRuntime().gc();
                            if (innerCounter.incrementAndGet() == TEST_SIZE) {
                                realm.close();
                                looperThread.testComplete();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            fail(throwable.toString());
                        }
                    });
        }
    }

    // Tests that Observables keep strong references to their parent, so they are not accidentally GC'ed while
    // waiting for results from the async API's.
    @Test
    @RunTestInLooperThread
    @SuppressWarnings("CheckReturnValue")
    public void realmObject_gcStressTest() {
        final int TEST_SIZE = 50;
        final AtomicLong innerCounter = new AtomicLong();
        final Realm realm = looperThread.getRealm();

        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(AllTypes.class).setColumnLong(i);
        }
        realm.commitTransaction();

        for (int i = 0; i < TEST_SIZE; i++) {
            // Doesn't keep a reference to the Observable.
            realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, i).findFirstAsync().<AllTypes>asFlowable()
                    .filter(new Predicate<AllTypes>() {
                        @Override
                        public boolean test(AllTypes obj) throws Exception {
                            return obj.isLoaded();
                        }
                    })
                    .take(1) // Unsubscribes from Realm.
                    .subscribe(new Consumer<AllTypes>() {
                        @Override
                        public void accept(AllTypes allTypes) throws Exception {
                            // Not guaranteed, but can result in the GC of other RealmResults waiting for a result.
                            Runtime.getRuntime().gc();
                            if (innerCounter.incrementAndGet() == TEST_SIZE) {
                                looperThread.testComplete();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            fail(throwable.toString());
                        }
                    });
        }
    }

    // Tests that Observables keep strong references to their parent, so they are not accidentally GC'ed while
    // waiting for results from the async API's.
    @Test
    @RunTestInLooperThread
    @SuppressWarnings("CheckReturnValue")
    public void dynamicRealmObject_gcStressTest() {
        final int TEST_SIZE = 50;
        final AtomicLong innerCounter = new AtomicLong();
        final DynamicRealm realm = DynamicRealm.getInstance(looperThread.getConfiguration());

        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(AllTypes.CLASS_NAME).set(AllTypes.FIELD_LONG, i);
        }
        realm.commitTransaction();

        for (int i = 0; i < TEST_SIZE; i++) {
            // Doesn't keep a reference to the Observable.
            realm.where(AllTypes.CLASS_NAME).equalTo(AllTypes.FIELD_LONG, i).findFirstAsync().<DynamicRealmObject>asFlowable()
                    .filter(new Predicate<DynamicRealmObject>() {
                        @Override
                        public boolean test(DynamicRealmObject obj) throws Exception {
                            return obj.isLoaded();
                        }
                    })
                    .take(1) // Unsubscribes from Realm.
                    .subscribe(new Consumer<DynamicRealmObject>() {
                        @Override
                        public void accept(DynamicRealmObject dynamicRealmObject) throws Exception {
                            // Not guaranteed, but can result in the GC of other RealmResults waiting for a result.
                            Runtime.getRuntime().gc();
                            if (innerCounter.incrementAndGet() == TEST_SIZE) {
                                realm.close();
                                looperThread.testComplete();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            fail(throwable.toString());
                        }
                    });
        }
    }

}
