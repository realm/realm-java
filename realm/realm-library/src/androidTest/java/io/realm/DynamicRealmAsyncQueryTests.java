/*
 * Copyright 2020 Realm Inc.
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

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.UiThreadTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.AllTypes;
import io.realm.entities.Owner;
import io.realm.internal.async.RealmThreadPoolExecutor;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DynamicRealmAsyncQueryTests {

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();
    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private RealmConfiguration config;

    @Before
    public void setUp() {
        config = new RealmConfiguration.Builder().build();

        // Initializes schema. DynamicRealm will not do that, so let a normal Realm create the file first.
        Realm.getInstance(config).close();
    }

    // ****************************
    // ****  Async transaction  ***
    // ****************************

    // Starts asynchronously a transaction to insert one element.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync() {
        final DynamicRealm realm = DynamicRealm.getInstance(config);
        assertEquals(0, realm.where(Owner.CLASS_NAME).count());

        realm.executeTransactionAsync(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                DynamicRealmObject owner = realm.createObject(Owner.CLASS_NAME);
                owner.setString(Owner.FIELD_NAME, "Owner");
            }
        }, new DynamicRealm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                assertEquals(1, realm.where(Owner.CLASS_NAME).count());
                assertEquals("Owner", realm.where(Owner.CLASS_NAME).findFirst().getString(Owner.FIELD_NAME));

                realm.close();
                Realm.deleteRealm(config);

                looperThread.testComplete();
            }
        }, new DynamicRealm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                fail(error.getMessage());
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_onSuccess() {
        final DynamicRealm realm = DynamicRealm.getInstance(config);
        assertEquals(0, realm.where(Owner.CLASS_NAME).count());

        realm.executeTransactionAsync(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                DynamicRealmObject owner = realm.createObject(Owner.CLASS_NAME);
                owner.setString(Owner.FIELD_NAME, "Owner");
            }
        }, new DynamicRealm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                assertEquals(1, realm.where(Owner.CLASS_NAME).count());
                assertEquals("Owner", realm.where(Owner.CLASS_NAME).findFirst().getString(Owner.FIELD_NAME));

                realm.close();
                Realm.deleteRealm(config);

                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_onSuccessCallerRealmClosed() {
        final DynamicRealm realm = DynamicRealm.getInstance(config);
        assertEquals(0, realm.where(Owner.CLASS_NAME).count());

        realm.executeTransactionAsync(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                DynamicRealmObject owner = realm.createObject(Owner.CLASS_NAME);
                owner.setString(Owner.FIELD_NAME, "Owner");
            }
        }, new DynamicRealm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                assertTrue(realm.isClosed());
                DynamicRealm newRealm = DynamicRealm.getInstance(config);
                assertEquals(1, newRealm.where(Owner.CLASS_NAME).count());
                assertEquals("Owner", newRealm.where(Owner.CLASS_NAME).findFirst().getString(Owner.FIELD_NAME));

                newRealm.close();
                Realm.deleteRealm(config);

                looperThread.testComplete();
            }
        });

        realm.close();
    }

    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_onError() {
        final DynamicRealm realm = DynamicRealm.getInstance(config);
        final RuntimeException runtimeException = new RuntimeException("Oh! What a Terrible Failure");
        assertEquals(0, realm.where(Owner.CLASS_NAME).count());

        realm.executeTransactionAsync(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                throw runtimeException;
            }
        }, new DynamicRealm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                assertEquals(0, realm.where(Owner.CLASS_NAME).count());
                assertNull(realm.where(Owner.CLASS_NAME).findFirst());
                assertEquals(runtimeException, error);

                realm.close();
                Realm.deleteRealm(config);

                looperThread.testComplete();
            }
        });
    }

    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_onErrorCallerRealmClosed() {
        final DynamicRealm realm = DynamicRealm.getInstance(config);
        final RuntimeException runtimeException = new RuntimeException("Oh! What a Terrible Failure");
        assertEquals(0, realm.where(Owner.CLASS_NAME).count());

        realm.executeTransactionAsync(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                throw runtimeException;
            }
        }, new DynamicRealm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                assertTrue(realm.isClosed());
                DynamicRealm newRealm = DynamicRealm.getInstance(config);
                assertEquals(0, newRealm.where(Owner.CLASS_NAME).count());
                assertNull(newRealm.where(Owner.CLASS_NAME).findFirst());
                assertEquals(runtimeException, error);

                newRealm.close();
                Realm.deleteRealm(config);

                looperThread.testComplete();
            }
        });
        realm.close();
    }

    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_NoCallbacks() {
        final DynamicRealm realm = DynamicRealm.getInstance(config);
        assertEquals(0, realm.where(Owner.CLASS_NAME).count());

        realm.executeTransactionAsync(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                DynamicRealmObject owner = realm.createObject(Owner.CLASS_NAME);
                owner.setString(Owner.FIELD_NAME, "Owner");
            }
        });
        realm.addChangeListener(new RealmChangeListener<DynamicRealm>() {
            @Override
            public void onChange(DynamicRealm otherRealm) {
                assertEquals("Owner", realm.where(Owner.CLASS_NAME).findFirst().getString(Owner.FIELD_NAME));

                realm.close();
                Realm.deleteRealm(config);

                looperThread.testComplete();
            }
        });
    }

    // Tests that an async transaction that throws when call cancelTransaction manually.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_cancelTransactionInside() {
        final TestHelper.TestLogger testLogger = new TestHelper.TestLogger(LogLevel.DEBUG);
        RealmLog.add(testLogger);

        final DynamicRealm realm = DynamicRealm.getInstance(config);

        assertEquals(0, realm.where(Owner.CLASS_NAME).count());

        realm.executeTransactionAsync(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                DynamicRealmObject owner = realm.createObject(Owner.CLASS_NAME);
                owner.setString(Owner.FIELD_NAME, "Owner");
                realm.cancelTransaction();
            }
        }, new DynamicRealm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                fail("Should not reach success if runtime exception is thrown in callback.");
            }
        }, new DynamicRealm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // Ensure we are giving developers quality messages in the logs.
                assertTrue(testLogger.message.contains(
                        "Exception has been thrown: Can't commit a non-existing write transaction"));
                assertTrue(error instanceof IllegalStateException);
                RealmLog.remove(testLogger);

                realm.close();
                Realm.deleteRealm(config);

                looperThread.testComplete();
            }
        });
    }

    // Tests if the background Realm is closed when transaction success returned.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_realmClosedOnSuccess() {
        final AtomicInteger counter = new AtomicInteger(100);
        final DynamicRealm realm = DynamicRealm.getInstance(config);
        final RealmCache.Callback cacheCallback = new RealmCache.Callback() {
            @Override
            public void onResult(int count) {
                assertEquals(1, count);
                if (counter.decrementAndGet() == 0) {
                    realm.close();
                    Realm.deleteRealm(config);

                    looperThread.testComplete();
                }
            }
        };
        final DynamicRealm.Transaction.OnSuccess transactionCallback = new DynamicRealm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                RealmCache.invokeWithGlobalRefCount(realm.getConfiguration(), cacheCallback);
                if (counter.get() == 0) {
                    // Finishes testing.
                    return;
                }
                realm.executeTransactionAsync(new DynamicRealm.Transaction() {
                    @Override
                    public void execute(DynamicRealm realm) {
                        // no-op
                    }
                }, this);
            }
        };

        realm.executeTransactionAsync(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                // no-op
            }
        }, transactionCallback);
    }

    // Tests if the background Realm is closed when transaction error returned.
    @Test
    @RunTestInLooperThread
    public void executeTransaction_async_realmClosedOnError() {
        final AtomicInteger counter = new AtomicInteger(100);
        final DynamicRealm realm = DynamicRealm.getInstance(config);
        final RealmCache.Callback cacheCallback = new RealmCache.Callback() {
            @Override
            public void onResult(int count) {
                assertEquals(1, count);
                if (counter.decrementAndGet() == 0) {
                    realm.close();
                    Realm.deleteRealm(config);

                    looperThread.testComplete();
                }
            }
        };
        final DynamicRealm.Transaction.OnError transactionCallback = new DynamicRealm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                RealmCache.invokeWithGlobalRefCount(realm.getConfiguration(), cacheCallback);
                if (counter.get() == 0) {
                    // Finishes testing.
                    return;
                }
                realm.executeTransactionAsync(new DynamicRealm.Transaction() {
                    @Override
                    public void execute(DynamicRealm realm) {
                        throw new RuntimeException("Dummy exception");
                    }
                }, this);
            }
        };

        realm.executeTransactionAsync(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                throw new RuntimeException("Dummy exception");
            }
        }, transactionCallback);
    }

    // Test case for https://github.com/realm/realm-java/issues/1893
    // Ensures that onSuccess is called with the correct Realm version for async transaction.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_asyncQuery() {
        final DynamicRealm realm = DynamicRealm.getInstance(config);
        final RealmResults<DynamicRealmObject> results = realm.where(AllTypes.CLASS_NAME).findAllAsync();
        assertEquals(0, results.size());

        realm.executeTransactionAsync(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                realm.createObject(AllTypes.CLASS_NAME);
            }
        }, new DynamicRealm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                assertEquals(1, realm.where(AllTypes.CLASS_NAME).count());
                // We cannot guarantee the async results get delivered from OS.
                if (results.isLoaded()) {
                    assertEquals(1, results.size());
                } else {
                    assertEquals(0, results.size());
                }

                realm.close();
                Realm.deleteRealm(config);

                looperThread.testComplete();
            }
        }, new DynamicRealm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                fail();
            }
        });
    }

    @Test
    public void executeTransactionAsync_onSuccessOnNonLooperThreadThrows() {
        try (DynamicRealm realm = DynamicRealm.getInstance(config)) {
            thrown.expect(IllegalStateException.class);
            realm.executeTransactionAsync(new DynamicRealm.Transaction() {
                @Override
                public void execute(DynamicRealm realm) {
                    // no-op
                }
            }, new DynamicRealm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    // no-op
                }
            });
        } finally {
            Realm.deleteRealm(config);
        }
    }

    @Test
    public void executeTransactionAsync_onErrorOnNonLooperThreadThrows() {
        try (DynamicRealm realm = DynamicRealm.getInstance(config)) {
            thrown.expect(IllegalStateException.class);
            realm.executeTransactionAsync(new DynamicRealm.Transaction() {
                @Override
                public void execute(DynamicRealm realm) {
                    // no-op
                }
            }, new DynamicRealm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    // no-op
                }
            });
        } finally {
            Realm.deleteRealm(config);
        }
    }

    // https://github.com/realm/realm-java/issues/4595#issuecomment-298830411
    // onSuccess might commit another transaction which will call didChange. So before calling async transaction
    // callbacks, the callback should be cleared.
    @Test
    @RunTestInLooperThread
    public void executeTransactionAsync_callbacksShouldBeClearedBeforeCalling()
            throws NoSuchFieldException, IllegalAccessException {
        final AtomicInteger callbackCounter = new AtomicInteger(0);
        final DynamicRealm foregroundRealm = DynamicRealm.getInstance(config);

        // Use single thread executor
        TestHelper.replaceRealmThreadExecutor(RealmThreadPoolExecutor.newSingleThreadExecutor());

        // To reproduce the issue, the posted callback needs to arrived before the Object Store did_change called.
        // We just disable the auto refresh here then the did_change won't be called.
        foregroundRealm.setAutoRefresh(false);
        foregroundRealm.executeTransactionAsync(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                realm.createObject(AllTypes.CLASS_NAME);
            }
        }, new DynamicRealm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // This will be called first and only once
                assertEquals(0, callbackCounter.getAndIncrement());

                // This transaction should never trigger the onSuccess.
                foregroundRealm.beginTransaction();
                foregroundRealm.createObject(AllTypes.CLASS_NAME);
                foregroundRealm.commitTransaction();
            }
        });

        foregroundRealm.executeTransactionAsync(new DynamicRealm.Transaction() {
            @Override
            public void execute(DynamicRealm realm) {
                realm.createObject(AllTypes.CLASS_NAME);
            }
        }, new DynamicRealm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // This will be called 2nd and only once
                assertEquals(1, callbackCounter.getAndIncrement());

                foregroundRealm.close();

                looperThread.testComplete();
            }
        });

        // Wait for all async tasks finish to ensure the async transaction posted callback will arrive first.
        TestHelper.resetRealmThreadExecutor();
        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                // Manually call refresh, so the did_change will be triggered.
                foregroundRealm.sharedRealm.refresh();
            }
        });
    }
}
