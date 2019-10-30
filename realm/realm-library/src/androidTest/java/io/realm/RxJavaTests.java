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

import android.os.Looper;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.CyclicType;
import io.realm.entities.Dog;
import io.realm.internal.util.Pair;
import io.realm.log.RealmLog;
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
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm realm;
    private Disposable subscription;

    @Before
    public void setUp() throws Exception {
        // For non-LooperThread tests.
        realm = Realm.getInstance(configFactory.createConfiguration());
        looperThread.runAfterTest(() -> {
            if (subscription != null && !subscription.isDisposed()) {
                subscription.dispose();
            }
        });
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
        subscription = obj.<AllTypes>asFlowable().subscribe(rxObject -> {
            assertTrue(rxObject.isFrozen());
            assertTrue(rxObject.equals(obj));
            subscribedNotified.set(true);
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @UiThreadTest
    public void realmObject_emitChangesetOnSubscribe() {
        realm.beginTransaction();
        final AllJavaTypes obj = realm.createObject(AllJavaTypes.class);
        realm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        subscription = obj.<AllJavaTypes>asChangesetObservable().subscribe(change -> {
            assertTrue(change.getObject().isFrozen());
            assertEquals(change.getObject().getFieldId(), obj.getFieldId());
            assertNull(change.getChangeset());
            subscribedNotified.set(true);
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @UiThreadTest
    public void dynamicRealmObject_emitChangesetOnSubscribe() {
        DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        dynamicRealm.beginTransaction();
        final DynamicRealmObject obj = dynamicRealm.createObject(AllJavaTypes.CLASS_NAME);
        dynamicRealm.commitTransaction();

        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        subscription = obj.<DynamicRealmObject>asChangesetObservable().subscribe(change -> {
            assertTrue(change.getObject().isFrozen());
            assertEquals(change.getObject().getLong(AllJavaTypes.FIELD_ID), obj.getLong(AllJavaTypes.FIELD_ID));
            assertNull(change.getChangeset());
            subscribedNotified.set(true);
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

        subscription = obj.<AllTypes>asFlowable().subscribe(allTypes -> {
            assertTrue(allTypes.isFrozen());
            if (subscriberCalled.incrementAndGet() == 2) {
                looperThread.testComplete();
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

        subscription = obj.asChangesetObservable().subscribe(change -> {
            assertTrue(change.getObject().isFrozen());
            if (subscriberCalled.incrementAndGet() == 2) {
                assertNotNull(change.getChangeset());
                assertTrue(change.getChangeset().isFieldChanged(AllTypes.FIELD_LONG));
                looperThread.testComplete();
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

        subscription = obj.asChangesetObservable().subscribe(change -> {
            assertTrue(change.getObject().isFrozen());
            if (subscriberCalled.incrementAndGet() == 2) {
                assertNotNull(change.getChangeset());
                assertTrue(change.getChangeset().isFieldChanged(AllTypes.FIELD_LONG));
                looperThread.testComplete();
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
                .subscribe(allTypes -> {
                    assertTrue(allTypes.isFrozen());
                    subscribedNotified.set(true);
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
        subscription = asyncObj.<AllTypes>asFlowable().subscribe(rxObject -> {
            assertTrue(rxObject.isFrozen());
            assertTrue(rxObject == asyncObj);
            subscribedNotified.set(true);
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
        subscription = realm.where(AllTypes.class).findFirstAsync().<AllTypes>asFlowable().subscribe(rxObject -> {
            assertTrue(rxObject.isFrozen());
            if (subscriberCalled.incrementAndGet() == 2) {
                looperThread.testComplete();
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

        subscription = realm.where(AllTypes.class).findFirstAsync().<AllTypes>asFlowable().subscribe(rxObject -> {
            assertTrue(rxObject.isFrozen());
            switch (subscriberCalled.incrementAndGet()) {
                case 1:
                    assertFalse(rxObject.isLoaded());
                    break;
                case 2:
                    assertTrue(rxObject.isLoaded());
                    assertTrue(rxObject.isValid());
                    realm.executeTransactionAsync(r -> r.delete(AllTypes.class));
                    break;
                case 3:
                    assertTrue(rxObject.isLoaded());
                    assertFalse(rxObject.isValid());
                    looperThread.testComplete();
                    break;
                default:
                    fail();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void realmResults_emittedOnSubscribe() {
        Realm realm = looperThread.getRealm();
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        subscription = results.asFlowable().subscribe(rxResults -> {
            assertTrue(rxResults.isFrozen());
            looperThread.testComplete();
        });
    }

    @Test
    @UiThreadTest
    public void realmResults_emittedChangesetOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        subscription = results.asChangesetObservable().subscribe(change -> {
            assertTrue(change.getCollection().isFrozen());
            assertEquals(results, change.getCollection());
            subscribedNotified.set(true);
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
        subscription = list.asFlowable().subscribe(rxList -> {
            assertTrue(rxList.isFrozen());
            assertTrue(rxList.equals(list));
            subscribedNotified.set(true);
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
        subscription = list.asChangesetObservable().subscribe(change -> {
            assertTrue(change.getCollection().isFrozen());
            assertEquals(list, change.getCollection());
            assertNull(change.getChangeset());
            subscribedNotified.set(true);
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
        subscription = results.asFlowable().subscribe(rxResults -> {
            assertTrue(rxResults.isFrozen());
            assertTrue(rxResults.equals(results));
            subscribedNotified.set(true);
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
        subscription = results.asChangesetObservable().subscribe(change -> {
            assertTrue(change.getCollection().isFrozen());
            assertEquals(results, change.getCollection());
            assertNull(change.getChangeset());
            subscribedNotified.set(true);
        });
        assertTrue(subscribedNotified.get());
        dynamicRealm.close();
        subscription.dispose();
    }

    @Test
    @RunTestInLooperThread
    public void realmResults_emittedOnUpdate() {
        Realm realm = looperThread.getRealm();
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();

        subscription = results.asFlowable().subscribe(rxResults -> {
            assertTrue(rxResults.isFrozen());
            if (rxResults.size() == 1) {
                looperThread.testComplete();
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
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();

        subscription = results.asChangesetObservable().subscribe(change -> {
            assertTrue(change.getCollection().isFrozen());
            if (subscriberCalled.incrementAndGet() == 2) {
                assertEquals(1, change.getChangeset().getInsertions().length);
                looperThread.testComplete();
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

        subscription = list.asFlowable().subscribe(dogs -> {
            assertTrue(dogs.isFrozen());
            if (subscriberCalled.incrementAndGet() == 2) {
                assertEquals(1, list.size());
                looperThread.testComplete();
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

        subscription = list.asChangesetObservable().subscribe(change -> {
            assertTrue(change.getCollection().isFrozen());
            if (subscriberCalled.incrementAndGet() == 2) {
                assertEquals(1, list.size());
                assertEquals(1, change.getChangeset().getInsertions().length);
                looperThread.testComplete();
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

        subscription = results.asFlowable().subscribe(rxObject -> {
            assertTrue(rxObject.isFrozen());
            if (subscriberCalled.incrementAndGet() == 2) {
                dynamicRealm.close();
                looperThread.testComplete();
            }
        });

        dynamicRealm.beginTransaction();
        dynamicRealm.createObject(AllTypes.CLASS_NAME);
        dynamicRealm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void dynamicRealmResults_emittedChangesetOnUpdate() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(looperThread.getConfiguration());
        looperThread.closeAfterTest(dynamicRealm);
        RealmResults<DynamicRealmObject> results = dynamicRealm.where(AllTypes.CLASS_NAME).findAll();

        subscription = results.asChangesetObservable().subscribe(change -> {
            if (change.getCollection().isLoaded()) {
                assertEquals(1, change.getChangeset().getInsertions().length);
                looperThread.testComplete();
            }
        });

        dynamicRealm.beginTransaction();
        dynamicRealm.createObject(AllTypes.CLASS_NAME);
        dynamicRealm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    public void findAllAsync_emittedOnSubscribe() {
        Realm realm = looperThread.getRealm();
        final RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();
        subscription = results.asFlowable().subscribe(rxResults -> {
            assertTrue(rxResults.isFrozen());
            looperThread.testComplete();
        });
    }

    @Test
    @RunTestInLooperThread
    public void findAllAsync_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.getRealm();
        subscription = realm.where(AllTypes.class).findAllAsync().asFlowable().subscribe(rxResults -> {
            assertTrue(rxResults.isFrozen());
            if (subscriberCalled.incrementAndGet() == 2) {
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    @UiThreadTest
    @SuppressWarnings("ReferenceEquality")
    public void realm_emittedOnSubscribe() {
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        subscription = realm.asFlowable().subscribe(rxRealm -> {
            assertTrue(rxRealm.isFrozen());
            assertTrue(rxRealm == realm);
            subscribedNotified.set(true);
        });
        assertTrue(subscribedNotified.get());
        subscription.dispose();
    }

    @Test
    @RunTestInLooperThread
    public void realm_emittedOnUpdate() {
        final AtomicInteger subscriberCalled = new AtomicInteger(0);
        Realm realm = looperThread.getRealm();
        subscription = realm.asFlowable().subscribe(rxRealm -> {
            assertTrue(rxRealm.isFrozen());
            if (subscriberCalled.incrementAndGet() == 2) {
                looperThread.testComplete();
            }
        });

        realm.beginTransaction();
        realm.createObject(AllTypes.class);
        realm.commitTransaction();
    }

    @Test
    @UiThreadTest
    @SuppressWarnings("ReferenceEquality")
    public void dynamicRealm_emittedOnSubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
        final AtomicBoolean subscribedNotified = new AtomicBoolean(false);
        subscription = dynamicRealm.asFlowable().subscribe(rxRealm -> {
            assertTrue(rxRealm.isFrozen());
            assertTrue(rxRealm == dynamicRealm);
            subscribedNotified.set(true);
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
        subscription = dynamicRealm.asFlowable().subscribe(rxRealm -> {
            assertTrue(rxRealm.isFrozen());
            if (subscriberCalled.incrementAndGet() == 2) {
                rxRealm.close();
                looperThread.testComplete();
            }
        });

        dynamicRealm.beginTransaction();
        dynamicRealm.createObject("AllTypes");
        dynamicRealm.commitTransaction();
    }

    @Test
    @RunTestInLooperThread
    @SuppressWarnings("ReferenceEquality")
    public void unsubscribe_sameThread() {
        Realm realm = looperThread.getRealm();
        subscription = realm.asFlowable()
                .doOnCancel(() -> {
                    looperThread.testComplete();
                })
                .subscribe(rxRealm -> {
            assertTrue(rxRealm.isFrozen());
            assertEquals(rxRealm.getPath(), realm.getPath());
            assertEquals(rxRealm.sharedRealm.getVersionID(), realm.sharedRealm.getVersionID());
        });
        subscription.dispose();
    }

    @Test
    @UiThreadTest
    @SuppressWarnings("ReferenceEquality")
    public void unsubscribe_fromOtherThread() {
        Realm realm = looperThread.getRealm();
        subscription = realm.asFlowable()
            .doFinally(looperThread::testComplete)
            .subscribe(rxRealm -> {
                assertTrue(rxRealm.isFrozen());
                assertEquals(rxRealm.getPath(), realm.getPath());
                assertEquals(rxRealm.sharedRealm.getVersionID(), realm.sharedRealm.getVersionID());
                looperThread.postRunnable(() -> {
                    Thread t = new Thread(() -> subscription.dispose());
                    t.start();
                    looperThread.keepStrongReference(t);
                });
            });
    }

    @Test
    @UiThreadTest
    public void wrongGenericClassThrows() {
        realm.beginTransaction();
        final AllTypes obj = realm.createObject(AllTypes.class);
        realm.commitTransaction();

        Flowable<CyclicType> obs = obj.asFlowable();
        @SuppressWarnings("unused")
        Disposable subscription = obs.subscribe(
                cyclicType -> fail(),
                ignored -> {}
        );
    }

    @Test
    @UiThreadTest
    public void realm_closeInDoOnUnsubscribe() {
        Flowable<Realm> observable = realm.asFlowable()
                .doOnCancel(() -> realm.close());

        subscription = observable.subscribe(realm -> assertEquals(2, Realm.getLocalInstanceCount(realm.getConfiguration())));
        subscription.dispose();
        assertTrue(realm.isClosed());
    }

    @Test
    @UiThreadTest
    public void dynamicRealm_closeInDoOnUnsubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());

        Flowable<DynamicRealm> observable = dynamicRealm.asFlowable()
                .doOnCancel(() -> dynamicRealm.close());

        subscription = observable.subscribe(ignored -> {});
        subscription.dispose();
        assertTrue(dynamicRealm.isClosed());
    }

    @Test
    @RunTestInLooperThread
    public void realmResults_closeInDoOnUnsubscribe() {
        Realm realm = looperThread.getRealm();
        Flowable<RealmResults<AllTypes>> observable = realm.where(AllTypes.class).findAll().asFlowable()
                .doOnCancel(() -> realm.close());

        subscription = observable.subscribe(ignored -> {});
        subscription.dispose();
        assertTrue(realm.isClosed());
        looperThread.testComplete();
    }

    @Test
    @UiThreadTest
    public void realmList_closeInDoOnUnsubscribe() {
        realm.beginTransaction();
        RealmList<Dog> list = realm.createObject(AllTypes.class).getColumnRealmList();
        realm.commitTransaction();

        Flowable<RealmList<Dog>> observable = list.asFlowable().doOnCancel(() -> realm.close());
        subscription = observable.subscribe(ignored -> {});
        subscription.dispose();
        assertTrue(realm.isClosed());
    }

    @Test
    @UiThreadTest
    public void dynamicRealmResults_closeInDoOnUnsubscribe() {
        final DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());

        Flowable<RealmResults<DynamicRealmObject>> flowable = dynamicRealm.where(AllTypes.CLASS_NAME).findAll().asFlowable()
                .doOnCancel(() -> dynamicRealm.close());

        subscription = flowable.subscribe(ignored -> {});
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
                .doOnCancel(() -> realm.close());

        subscription = flowable.subscribe(ignored -> {});
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
                .doOnCancel(() -> dynamicRealm.close());
        subscription = flowable.subscribe(ignored -> {});
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
            realm.where(AllTypes.class).equalTo(AllTypes.FIELD_LONG, i).findAllAsync().asFlowable()
                    .filter(results -> results.isLoaded())
                    .take(1) // Unsubscribes from Realm.
                    .subscribe(allTypes -> {
                        // Not guaranteed, but can result in the GC of other RealmResults waiting for a result.
                        Runtime.getRuntime().gc();
                        if (innerCounter.incrementAndGet() == TEST_SIZE) {
                            looperThread.testComplete();
                        }
                    }, throwable -> fail(throwable.toString()));
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
        looperThread.closeAfterTest(realm);

        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(AllTypes.CLASS_NAME).set(AllTypes.FIELD_LONG, i);
        }
        realm.commitTransaction();

        for (int i = 0; i < TEST_SIZE; i++) {
            // Doesn't keep a reference to the Observable.
            realm.where(AllTypes.CLASS_NAME).equalTo(AllTypes.FIELD_LONG, i).findAllAsync().asFlowable()
                    .filter(results -> results.isLoaded())
                    .take(1) // Unsubscribes from Realm.
                    .subscribe(dynamicRealmObjects -> {
                        // Not guaranteed, but can result in the GC of other RealmResults waiting for a result.
                        Runtime.getRuntime().gc();
                        if (innerCounter.incrementAndGet() == TEST_SIZE) {
                            looperThread.testComplete();
                        }
                    }, throwable -> fail(throwable.toString()));
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
                    .filter(obj -> obj.isLoaded())
                    .take(1) // Unsubscribes from Realm.
                    .subscribe(allTypes -> {
                        // Not guaranteed, but can result in the GC of other RealmResults waiting for a result.
                        Runtime.getRuntime().gc();
                        if (innerCounter.incrementAndGet() == TEST_SIZE) {
                            looperThread.testComplete();
                        }
                    }, throwable -> fail(throwable.toString()));
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
        looperThread.closeAfterTest(realm);

        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(AllTypes.CLASS_NAME).set(AllTypes.FIELD_LONG, i);
        }
        realm.commitTransaction();

        for (int i = 0; i < TEST_SIZE; i++) {
            // Doesn't keep a reference to the Observable.
            realm.where(AllTypes.CLASS_NAME).equalTo(AllTypes.FIELD_LONG, i).findFirstAsync().<DynamicRealmObject>asFlowable()
                    .filter(obj -> obj.isLoaded())
                    .take(1) // Unsubscribes from Realm.
                    .subscribe(dynamicRealmObject -> {
                        // Not guaranteed, but can result in the GC of other RealmResults waiting for a result.
                        Runtime.getRuntime().gc();
                        if (innerCounter.incrementAndGet() == TEST_SIZE) {
                            looperThread.testComplete();
                        }
                    }, throwable -> fail(throwable.toString()));
        }
    }

    @Test
    @RunTestInLooperThread
    public void realmResults_readableAcrossThreads() {
        // FIXME: Make a test like this for all from methods
        final long TEST_SIZE = 10;
        Realm realm = looperThread.getRealm();

        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            realm.createObject(AllTypes.class).setColumnLong(1);
        }
        realm.commitTransaction();

        subscription = realm.where(AllTypes.class).sort(AllTypes.FIELD_LONG).findAllAsync().asFlowable()
                .subscribeOn(Schedulers.io())
                .filter(RealmResults::isLoaded)
                .map(results -> {
                    RealmLog.error(Thread.currentThread().getName());
                    return new Pair<>(results.size(), results);
                })
                .observeOn(Schedulers.computation())
                .subscribe(
                        pair -> {
                            RealmLog.error(Thread.currentThread().getName());
                            assertEquals(TEST_SIZE, pair.first.intValue());
                            assertEquals(TEST_SIZE, pair.second.size());
                            looperThread.testComplete();
                        }
                );
    }
}
