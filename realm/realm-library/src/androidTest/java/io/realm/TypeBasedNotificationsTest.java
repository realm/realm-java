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
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.Dog;
import io.realm.proxy.HandlerProxy;

public class TypeBasedNotificationsTest extends AndroidTestCase {
    private HandlerThread handlerThread;
    private Handler handler;
    private CountDownLatch signalTestFinished;
    private AtomicInteger globalCommitInvocations;
    private AtomicInteger typebasedCommitInvocations;
    private RealmConfiguration configuration;
    private Realm realm;

    @Override
    protected void setUp() throws Exception {
        handlerThread = new HandlerThread("LooperThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        signalTestFinished = new CountDownLatch(1);
        globalCommitInvocations = new AtomicInteger(0);
        typebasedCommitInvocations = new AtomicInteger(0);
        configuration = new RealmConfiguration.Builder(getContext()).build();
        Realm.deleteRealm(configuration);
    }

    @Override
    protected void tearDown() throws Exception {
        final CountDownLatch cleanup = new CountDownLatch(1);
        if (realm != null && handler.getLooper().getThread().isAlive()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (!realm.isClosed()) {
                        realm.close();
                    }
                    Realm.deleteRealm(configuration);
                    handlerThread.quit();
                    realm = null;
                    cleanup.countDown();
                }
            });
            TestHelper.awaitOrFail(cleanup);
        }
    }

    public void test_callback_on_new_object_sync() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (globalCommitInvocations.incrementAndGet() == 3) {
                            realm.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                });

                realm.beginTransaction();
                final Dog akamaru = realm.createObject(Dog.class);
                akamaru.setName("Akamaru");
                realm.commitTransaction();

                akamaru.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        assertEquals("Akamaru", akamaru.getName());
                        typebasedCommitInvocations.incrementAndGet();
                    }
                });

                // this commit should not trigger the type based callback
                // it will re-run the query in the background though
                realm.beginTransaction();
                realm.commitTransaction();

                realm.beginTransaction();
                akamaru.setAge(17);
                realm.commitTransaction();
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
        assertEquals(1, typebasedCommitInvocations.get());
    }


    // ********************************************************************************* //
    // UC 1.
    // Callback should be invoked after a relevant commit (one that should impact the
    // query from which we obtained our RealmObject or RealmResults)
    // ********************************************************************************* //
    // UC 1 for Sync RealmObject
    public void test_callback_with_relevant_commit_realmobject_sync() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (globalCommitInvocations.incrementAndGet() == 3) {
                            realm.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                });

                realm.beginTransaction();
                Dog akamaru = realm.createObject(Dog.class);
                akamaru.setName("Akamaru");
                realm.commitTransaction();

                final Dog dog = realm.where(Dog.class).findFirst();
                dog.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        assertEquals("Akamaru", dog.getName());
                        typebasedCommitInvocations.incrementAndGet();
                    }
                });

                // this commit should not trigger the type based callback
                // it will re-run the query in the background though
                realm.beginTransaction();
                realm.commitTransaction();

                realm.beginTransaction();
                akamaru.setAge(17);
                realm.commitTransaction();
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
        assertEquals(1, typebasedCommitInvocations.get());
    }

    // UC 1 Async RealmObject
    public void test_callback_with_relevant_commit_realmobject_async() {
        // prevent GC, everything inside a Runnable will be eligible for GC
        // as soon as the looper execute it, including any RealmObject/RealmResults
        // and it's listeners (even though they're stored as strong reference)
        final Dog[] dogs = new Dog[2];
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        globalCommitInvocations.incrementAndGet();
                    }
                });

                final Dog dog = realm.where(Dog.class).findFirstAsync();
                dogs[1] = dog;
                assertTrue(dog.load());
                dog.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        switch (typebasedCommitInvocations.incrementAndGet()) {
                            case 1: {
                                assertEquals("Akamaru", dog.getName());
                                realm.handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // trigger second callback invocation
                                        realm.beginTransaction();
                                        dogs[0].setAge(17);
                                        realm.commitTransaction();
                                    }
                                });
                                break;
                            }
                            case 2: {
                                assertEquals("Akamaru", dog.getName());
                                assertEquals(17, dog.getAge());
                                // posting as an event will give the handler a chance
                                // to deliver the notification for globalCommitInvocations
                                // otherwise, test will exit before the callback get a chance to be invoked
                                realm.handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        signalTestFinished.countDown();
                                    }
                                });
                                break;
                            }
                        }
                    }
                });

                realm.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        realm.beginTransaction();
                        realm.commitTransaction();
                    }
                });

                realm.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // trigger first callback invocation
                        realm.beginTransaction();
                        dogs[0] = realm.createObject(Dog.class);
                        dogs[0].setName("Akamaru");
                        realm.commitTransaction();
                    }
                });
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
        assertEquals(3, globalCommitInvocations.get());
        assertEquals(2, typebasedCommitInvocations.get());
    }

    // UC 1 Async RealmObject
    public void test_callback_with_relevant_commit_from_different_looper_realmobject_async() {
        final HandlerThread looperThread1 = new HandlerThread("looperThread1");
        final HandlerThread looperThread2 = new HandlerThread("looperThread2");
        final HandlerThread looperThread3 = new HandlerThread("looperThread3");
        looperThread1.start();
        looperThread2.start();
        looperThread3.start();
        final Dog[] dogs = new Dog[1];
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        globalCommitInvocations.incrementAndGet();
                    }
                });

                final Dog dog = realm.where(Dog.class).findFirstAsync();
                dogs[0] = dog;
                assertTrue(dog.load());
                dog.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        switch (typebasedCommitInvocations.incrementAndGet()) {
                            case 1: {
                                assertEquals("Akamaru", dog.getName());
                                realm.handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // trigger second callback invocation
                                        new Handler(looperThread3.getLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Realm realmLooperThread3 = Realm.getInstance(realm.getConfiguration());
                                                realmLooperThread3.beginTransaction();
                                                realmLooperThread3.where(Dog.class).findFirst().setAge(17);
                                                realmLooperThread3.commitTransaction();
                                                realmLooperThread3.close();
                                            }
                                        });
                                    }
                                });
                                break;
                            }
                            case 2: {
                                assertEquals("Akamaru", dog.getName());
                                assertEquals(17, dog.getAge());
                                // posting as an event will give the handler a chance
                                // to deliver the notification for globalCommitInvocations
                                // otherwise, test will exit before the callback get a chance to be invoked
                                realm.handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        signalTestFinished.countDown();
                                    }
                                });
                                break;
                            }
                        }
                    }
                });

                new Handler(looperThread1.getLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Realm realmLooperThread1 = Realm.getInstance(realm.getConfiguration());
                        realmLooperThread1.beginTransaction();
                        realmLooperThread1.commitTransaction();
                        realmLooperThread1.close();
                    }
                });

                new Handler(looperThread2.getLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Realm realmLooperThread2 = Realm.getInstance(realm.getConfiguration());
                        // trigger first callback invocation
                        realmLooperThread2.beginTransaction();
                        Dog dog = realmLooperThread2.createObject(Dog.class);
                        dog.setName("Akamaru");
                        realmLooperThread2.commitTransaction();
                        realmLooperThread2.close();
                    }
                });
            }
        });
        try {
            TestHelper.awaitOrFail(signalTestFinished);
            assertEquals(3, globalCommitInvocations.get());
            assertEquals(2, typebasedCommitInvocations.get());
        } finally {
            looperThread1.quit();
            looperThread2.quit();
            looperThread3.quit();
        }
    }

    // UC 1 Async RealmObject
    public void test_callback_with_relevant_commit_from_different_non_looper_realmobject_async() throws Throwable {
        final Throwable[] backgroundException = new Throwable[1];
        final Dog[] dogs = new Dog[1];
        final CountDownLatch waitForInsert = new CountDownLatch(1);

        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        globalCommitInvocations.incrementAndGet();
                    }
                });

                final Dog dog = realm.where(Dog.class).findFirstAsync();
                dogs[0] = dog;
                assertTrue(dog.load());
                dog.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        switch (typebasedCommitInvocations.incrementAndGet()) {
                            case 1: {
                                assertEquals("Akamaru", dog.getName());
                                realm.handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // trigger second callback invocation
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    waitForInsert.await();
                                                    Realm realmNonLooperThread3 = Realm.getInstance(realm.getConfiguration());
                                                    realmNonLooperThread3.beginTransaction();
                                                    realmNonLooperThread3.where(Dog.class).findFirst().setAge(17);
                                                    realmNonLooperThread3.commitTransaction();
                                                    realmNonLooperThread3.close();
                                                } catch (Throwable e) {
                                                    backgroundException[0] = e;
                                                }

                                            }
                                        }.start();
                                    }
                                }, TimeUnit.SECONDS.toMillis(0));
                                break;
                            }
                            case 2: {
                                assertEquals("Akamaru", dog.getName());
                                assertEquals(17, dog.getAge());
                                // posting as an event will give the handler a chance
                                // to deliver the notification for globalCommitInvocations
                                // otherwise, test will exit before the callback get a chance to be invoked
                                realm.handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        signalTestFinished.countDown();
                                    }
                                });
                                break;
                            }
                        }
                    }
                });

                new Thread() {
                    @Override
                    public void run() {
                        Realm realmNonLooperThread1 = Realm.getInstance(realm.getConfiguration());
                        realmNonLooperThread1.beginTransaction();
                        realmNonLooperThread1.commitTransaction();
                        realmNonLooperThread1.close();
                    }
                }.start();

                new Thread() {
                    @Override
                    public void run() {
                        Realm realmNonLooperThread2 = Realm.getInstance(realm.getConfiguration());
                        // trigger first callback invocation
                        realmNonLooperThread2.beginTransaction();
                        Dog dog = realmNonLooperThread2.createObject(Dog.class);
                        dog.setName("Akamaru");
                        realmNonLooperThread2.commitTransaction();
                        realmNonLooperThread2.close();

                        waitForInsert.countDown();
                    }
                }.start();
            }
        });

        try {
            TestHelper.awaitOrFail(signalTestFinished);
            assertEquals(3, globalCommitInvocations.get());
            assertEquals(2, typebasedCommitInvocations.get());
        } finally {
            if (backgroundException[0] != null) {
                throw backgroundException[0];
            }
        }

    }

    // UC 1 Sync RealmResults
    public void test_callback_with_relevant_commit_realmresults_sync() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (globalCommitInvocations.incrementAndGet() == 3) {
                            realm.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                });

                realm.beginTransaction();
                Dog akamaru = realm.createObject(Dog.class);
                akamaru.setName("Akamaru");
                realm.commitTransaction();

                final RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
                dogs.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        assertEquals(1, dogs.size());
                        assertEquals("Akamaru", dogs.get(0).getName());
                        typebasedCommitInvocations.incrementAndGet();
                    }
                });

                realm.beginTransaction();
                realm.commitTransaction();

                realm.beginTransaction();
                akamaru.setAge(17);
                realm.commitTransaction();
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
        assertEquals(1, typebasedCommitInvocations.get());
    }

    // UC 1 Async RealmResults
    public void test_callback_with_relevant_commit_realmresults_async() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (globalCommitInvocations.incrementAndGet() == 3) {
                            realm.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                });

                final RealmResults<Dog> dogs = realm.where(Dog.class).findAllAsync();
                assertTrue(dogs.load());
                dogs.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        assertEquals(1, dogs.size());
                        assertEquals("Akamaru", dogs.get(0).getName());
                        typebasedCommitInvocations.incrementAndGet();
                    }
                });

                realm.beginTransaction();
                realm.commitTransaction();

                realm.beginTransaction();
                Dog akamaru = realm.createObject(Dog.class);
                akamaru.setName("Akamaru");
                realm.commitTransaction();

                realm.beginTransaction();
                akamaru.setAge(17);
                realm.commitTransaction();

            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
        assertEquals(2, typebasedCommitInvocations.get());
    }

    // ********************************************************************************* //
    // UC 2.
    // Multiple callbacks should be invoked after a relevant commit
    // ********************************************************************************* //
    // UC 2 for Sync RealmObject
    public void test_multiple_callbacks_should_be_invoked_realmobject_sync() {
        final int NUMBER_OF_LISTENERS = 7;
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (globalCommitInvocations.incrementAndGet() == 3) {
                            realm.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                };

                realm.addChangeListener(listener);

                realm.beginTransaction();
                Dog akamaru = realm.createObject(Dog.class);
                realm.commitTransaction();

                Dog dog = realm.where(Dog.class).findFirst();
                for (int i = 0; i < NUMBER_OF_LISTENERS; i++) {
                    dog.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            typebasedCommitInvocations.incrementAndGet();
                        }
                    });
                }

                realm.beginTransaction();
                realm.commitTransaction();

                realm.beginTransaction();
                akamaru.setAge(17);
                realm.commitTransaction();
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
        assertEquals(NUMBER_OF_LISTENERS, typebasedCommitInvocations.get());
    }

    // UC 2 Async RealmObject
    public void test_multiple_callbacks_should_be_invoked_realmobject_async() {
        final int NUMBER_OF_LISTENERS = 7;
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (globalCommitInvocations.incrementAndGet() == 3) {
                            realm.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                };

                realm.addChangeListener(listener);

                realm.beginTransaction();
                Dog akamaru = realm.createObject(Dog.class);
                realm.commitTransaction();

                Dog dog = realm.where(Dog.class).findFirstAsync();
                assertTrue(dog.load());
                for (int i = 0; i < NUMBER_OF_LISTENERS; i++) {
                    dog.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            typebasedCommitInvocations.incrementAndGet();
                        }
                    });
                }

                realm.beginTransaction();
                realm.commitTransaction();

                realm.beginTransaction();
                akamaru.setAge(17);
                realm.commitTransaction();
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
        assertEquals(NUMBER_OF_LISTENERS, typebasedCommitInvocations.get());
    }

    // UC 2 Sync RealmResults
    public void test_multiple_callbacks_should_be_invoked_realmresults_sync() {
        final int NUMBER_OF_LISTENERS = 7;
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (globalCommitInvocations.incrementAndGet() == 3) {
                            realm.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                };

                realm.addChangeListener(listener);

                realm.beginTransaction();
                Dog akamaru = realm.createObject(Dog.class);
                realm.commitTransaction();

                RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
                for (int i = 0; i < NUMBER_OF_LISTENERS; i++) {
                    dogs.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            typebasedCommitInvocations.incrementAndGet();
                        }
                    });
                }

                realm.beginTransaction();
                realm.commitTransaction();

                realm.beginTransaction();
                akamaru.setAge(17);
                realm.commitTransaction();
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
        assertEquals(NUMBER_OF_LISTENERS, typebasedCommitInvocations.get());
    }

    // UC 2 Async RealmResults
    public void test_multiple_callbacks_should_be_invoked_realmresults_async() {
        final int NUMBER_OF_LISTENERS = 7;
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                RealmChangeListener listener = new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (globalCommitInvocations.incrementAndGet() == 3) {
                            realm.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                };

                realm.addChangeListener(listener);

                realm.beginTransaction();
                Dog akamaru = realm.createObject(Dog.class);
                realm.commitTransaction();

                RealmResults<Dog> dogs = realm.where(Dog.class).findAllAsync();
                assertTrue(dogs.load());
                for (int i = 0; i < NUMBER_OF_LISTENERS; i++) {
                    dogs.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            typebasedCommitInvocations.incrementAndGet();
                        }
                    });
                }

                realm.beginTransaction();
                realm.commitTransaction();

                realm.beginTransaction();
                akamaru.setAge(17);
                realm.commitTransaction();
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
        assertEquals(NUMBER_OF_LISTENERS, typebasedCommitInvocations.get());
    }

    // ********************************************************************************* //
    // UC 3.
    // Callback should be invoked when a non Looper thread commits
    // ********************************************************************************* //

    // UC 3 for Sync RealmObject
    public void test_non_looper_thread_commit_realmobject_sync() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (globalCommitInvocations.incrementAndGet() == 2) {
                            realm.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                });

                realm.beginTransaction();
                realm.createObject(Dog.class);
                realm.commitTransaction();

                Dog dog = realm.where(Dog.class).findFirst();
                dog.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        typebasedCommitInvocations.incrementAndGet();
                    }
                });

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Realm bgRealm = Realm.getInstance(realm.getConfiguration());
                        bgRealm.beginTransaction();
                        bgRealm.createObject(Dog.class);
                        bgRealm.commitTransaction();
                        bgRealm.close();
                    }
                };
                thread.start();
                try {
                    thread.join();
                    // this will give the posted notification a chance to execute
                    // keep this Runnable alive (waiting for the commit to arrive)
                    final int MAX_RETRIES = 60;
                    int numberOfSleep = 0;
                    while (numberOfSleep++ < MAX_RETRIES
                            && typebasedCommitInvocations.incrementAndGet() != 1) {
                        Thread.sleep(16);
                    }
                    assertEquals(1, typebasedCommitInvocations.get());
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
        assertEquals(1, typebasedCommitInvocations.get());
    }

    // UC 3 Async RealmObject
    public void test_non_looper_thread_commit_realmobject_async() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (globalCommitInvocations.incrementAndGet() == 2) {
                            realm.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                });

                realm.beginTransaction();
                realm.createObject(Dog.class);
                realm.commitTransaction();

                Dog dog = realm.where(Dog.class).findFirstAsync();
                assertTrue(dog.load());
                dog.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        typebasedCommitInvocations.incrementAndGet();
                    }
                });

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Realm bgRealm = Realm.getInstance(realm.getConfiguration());
                        bgRealm.beginTransaction();
                        bgRealm.createObject(Dog.class);
                        bgRealm.commitTransaction();
                        bgRealm.close();
                    }
                };
                thread.start();
                try {
                    thread.join();

                    final int MAX_RETRIES = 60;
                    int numberOfSleep = 0;
                    while (numberOfSleep++ < MAX_RETRIES
                            && typebasedCommitInvocations.incrementAndGet() != 1) {
                        Thread.sleep(16);
                    }
                    assertEquals(1, typebasedCommitInvocations.get());
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
        assertEquals(1, typebasedCommitInvocations.get());
    }

    // UC 3 Sync RealmResults
    public void test_non_looper_thread_commit_realmresults_sync() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (globalCommitInvocations.incrementAndGet() == 2) {
                            realm.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                });

                realm.beginTransaction();
                realm.createObject(Dog.class);
                realm.commitTransaction();

                final RealmResults<Dog> dogs = realm.where(Dog.class).findAll();
                dogs.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        typebasedCommitInvocations.incrementAndGet();
                        assertEquals(2, dogs.size());
                    }
                });

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Realm bgRealm = Realm.getInstance(realm.getConfiguration());
                        bgRealm.beginTransaction();
                        bgRealm.createObject(Dog.class);
                        bgRealm.commitTransaction();
                        bgRealm.close();
                    }
                };
                thread.start();
                try {
                    thread.join();

                    final int MAX_RETRIES = 60;
                    int numberOfSleep = 0;
                    while (numberOfSleep++ < MAX_RETRIES
                            && typebasedCommitInvocations.incrementAndGet() != 1) {
                        Thread.sleep(16);
                    }
                    assertEquals(1, typebasedCommitInvocations.get());
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
    }

    // UC 3 Async RealmResults
    public void test_non_looper_thread_commit_realmresults_async() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        if (globalCommitInvocations.incrementAndGet() == 2) {
                            realm.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    signalTestFinished.countDown();
                                }
                            });
                        }
                    }
                });

                realm.beginTransaction();
                realm.createObject(Dog.class);
                realm.commitTransaction();

                final RealmResults<Dog> dogs = realm.where(Dog.class).findAllAsync();
                assertTrue(dogs.load());
                dogs.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        typebasedCommitInvocations.incrementAndGet();
                        assertEquals(2, dogs.size());
                    }
                });

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Realm bgRealm = Realm.getInstance(realm.getConfiguration());
                        bgRealm.beginTransaction();
                        bgRealm.createObject(Dog.class);
                        bgRealm.commitTransaction();
                        bgRealm.close();
                    }
                };
                thread.start();
                try {
                    thread.join();

                    final int MAX_RETRIES = 60;
                    int numberOfSleep = 0;
                    while (numberOfSleep++ < MAX_RETRIES
                            && typebasedCommitInvocations.incrementAndGet() != 1) {
                        Thread.sleep(16);
                    }
                    assertEquals(1, typebasedCommitInvocations.get());
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }

            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
        assertEquals(1, typebasedCommitInvocations.get());
    }

    // ****************************************************************************************** //
    // UC 4.
    // Callback should throw if registered on a non Looper thread.
    // no tests for async RealmObject & RealmResults, since those already require a Looper thread
    // ***************************************************************************************** //

    // UC 4 for Realm
    public void test_should_throw_on_non_looper_thread_realm() {
        new Thread() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(configuration);
                try {
                    bgRealm.beginTransaction();
                    bgRealm.createObject(Dog.class);
                    bgRealm.commitTransaction();

                    bgRealm.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            fail("Callback should not be registered and invoked on a non-Looper thread");
                        }
                    });
                    fail("Callback should not be registered and invoked on a non-Looper thread");
                } catch (IllegalStateException ignored) {

                } finally {
                    bgRealm.close();
                    signalTestFinished.countDown();
                }
            }
        }.start();
        TestHelper.awaitOrFail(signalTestFinished);
    }

    // UC 4 for RealmObject
    public void test_should_throw_on_non_looper_thread_realmobject() {
        new Thread() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(configuration);
                try {
                    bgRealm.beginTransaction();
                    bgRealm.createObject(Dog.class);
                    bgRealm.commitTransaction();

                    Dog dog = bgRealm.where(Dog.class).findFirst();
                    dog.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            fail("Callback should not be registered and invoked on a non-Looper thread");
                        }
                    });
                    fail("Callback should not be registered and invoked on a non-Looper thread");
                } catch (IllegalStateException ignored) {

                } finally {
                    bgRealm.close();
                    signalTestFinished.countDown();
                }
            }
        }.start();
        TestHelper.awaitOrFail(signalTestFinished);
    }

    // UC 4 RealmObject
    public void test_should_throw_on_non_looper_thread_realmresults() {
        new Thread() {
            @Override
            public void run() {
                Realm bgRealm = Realm.getInstance(configuration);
                try {
                    bgRealm.beginTransaction();
                    bgRealm.createObject(Dog.class);
                    bgRealm.commitTransaction();

                    RealmResults<Dog> dogs = bgRealm.where(Dog.class).findAll();
                    dogs.addChangeListener(new RealmChangeListener() {
                        @Override
                        public void onChange() {
                            fail("Callback should not be registered and invoked on a non-Looper thread");
                        }
                    });
                    fail("Callback should not be registered and invoked on a non-Looper thread");
                } catch (IllegalStateException ignored) {

                } finally {
                    bgRealm.close();
                    signalTestFinished.countDown();
                }
            }
        }.start();
        TestHelper.awaitOrFail(signalTestFinished);
    }

    // ****************************************************************************************** //
    // UC 5.
    // Callback should be notified if we call refresh (even without getting the REALM_CHANGE yet)
    // ***************************************************************************************** //

    public void test_refresh_should_notify_callbacks_realmobject_sync() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);

                // Swallow all REALM_CHANGED events to test the behaviour of refresh
                final Handler handler = new HandlerProxy(realm.handler) {
                    @Override
                    public boolean onInterceptMessage(int what) {
                        switch (what) {
                            case HandlerController.REALM_CHANGED: {
                                return true;
                            }
                        }
                        return false;
                    }
                };
                realm.setHandler(handler);

                realm.beginTransaction();
                realm.createObject(Dog.class);
                realm.commitTransaction();

                final Dog dog = realm.where(Dog.class).findFirst();
                assertNull(dog.getName());

                dog.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        assertEquals("Akamaru", dog.getName());
                        signalTestFinished.countDown();
                    }
                });

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Realm bgRealm = Realm.getInstance(configuration);
                        bgRealm.beginTransaction();
                        bgRealm.where(Dog.class).findFirst().setName("Akamaru");
                        bgRealm.commitTransaction();
                        bgRealm.close();
                    }
                };
                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }

                realm.refresh();
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
    }

    public void test_refresh_should_notify_callbacks_realmobject_async() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);

                // Swallow all REALM_CHANGED events to test the behaviour of refresh
                final Handler handler = new HandlerProxy(realm.handler) {
                    @Override
                    public boolean onInterceptMessage(int what) {
                        switch (what) {
                            case HandlerController.REALM_CHANGED: {
                                return true;
                            }
                        }
                        return false;
                    }
                };
                realm.setHandler(handler);

                final Dog dog = realm.where(Dog.class).findFirstAsync();
                assertTrue(dog.load());

                dog.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        assertEquals("Akamaru", dog.getName());
                        signalTestFinished.countDown();
                    }
                });

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Realm bgRealm = Realm.getInstance(configuration);
                        bgRealm.beginTransaction();
                        Dog akamaru = bgRealm.createObject(Dog.class);
                        akamaru.setName("Akamaru");
                        bgRealm.commitTransaction();
                        bgRealm.close();
                    }
                };
                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }

                realm.refresh();
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
    }

    public void test_refresh_should_notify_callbacks_realmresults_sync() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);

                // Swallow all REALM_CHANGED events to test the behaviour of refresh
                final Handler handler = new HandlerProxy(realm.handler) {
                    @Override
                    public boolean onInterceptMessage(int what) {
                        switch (what) {
                            case HandlerController.REALM_CHANGED: {
                                return true;
                            }
                        }
                        return false;
                    }
                };
                realm.setHandler(handler);

                final RealmResults<Dog> dogs = realm.where(Dog.class).findAll();

                dogs.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        assertEquals("Akamaru", dogs.get(0).getName());
                        signalTestFinished.countDown();
                    }
                });

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Realm bgRealm = Realm.getInstance(configuration);
                        bgRealm.beginTransaction();
                        Dog akamaru = bgRealm.createObject(Dog.class);
                        akamaru.setName("Akamaru");
                        bgRealm.commitTransaction();
                        bgRealm.close();
                    }
                };
                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }

                realm.refresh();
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
    }

    public void test_refresh_should_notify_callbacks_realmresults_async() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);

                // Swallow all REALM_CHANGED events to test the behaviour of refresh
                final Handler handler = new HandlerProxy(realm.handler) {
                    @Override
                    public boolean onInterceptMessage(int what) {
                        switch (what) {
                            case HandlerController.REALM_CHANGED: {
                                return true;
                            }
                        }
                        return false;
                    }
                };
                realm.setHandler(handler);

                final RealmResults<Dog> dogs = realm.where(Dog.class).findAllAsync();
                assertTrue(dogs.load());

                dogs.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        assertEquals("Akamaru", dogs.get(0).getName());
                        signalTestFinished.countDown();
                    }
                });

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Realm bgRealm = Realm.getInstance(configuration);
                        bgRealm.beginTransaction();
                        Dog akamaru = bgRealm.createObject(Dog.class);
                        akamaru.setName("Akamaru");
                        bgRealm.commitTransaction();
                        bgRealm.close();
                    }
                };
                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }

                realm.refresh();
            }
        });
        TestHelper.awaitOrFail(signalTestFinished);
    }

    // mixed async RealmObject & RealmResults
    public void test_refresh_should_notify_callbacks_mixed() {
        final CountDownLatch listenerWasCalledOnRealmObject = new CountDownLatch(1);
        final CountDownLatch listenerWasCalledOnRealmResults = new CountDownLatch(1);

        handler.post(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(configuration);

                // Swallow all REALM_CHANGED events to test the behaviour of an explicit refresh
                final Handler handler = new HandlerProxy(realm.handler) {
                    @Override
                    public boolean onInterceptMessage(int what) {
                        switch (what) {
                            case HandlerController.REALM_CHANGED: {
                                return true;
                            }
                        }
                        return false;
                    }
                };
                realm.setHandler(handler);

                Dog dog = realm.where(Dog.class).findFirstAsync();
                RealmResults<Dog> dogs = realm.where(Dog.class).findAllAsync();

                assertTrue(dog.load());
                assertTrue(dogs.load());

                dog.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        listenerWasCalledOnRealmObject.countDown();
                    }
                });

                dogs.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        listenerWasCalledOnRealmResults.countDown();
                    }
                });

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Realm bgRealm = Realm.getInstance(configuration);
                        bgRealm.beginTransaction();
                        Dog akamaru = bgRealm.createObject(Dog.class);
                        akamaru.setName("Akamaru");
                        bgRealm.commitTransaction();
                        bgRealm.close();
                    }
                };
                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }

                realm.refresh();
            }
        });
        TestHelper.awaitOrFail(listenerWasCalledOnRealmObject);
        TestHelper.awaitOrFail(listenerWasCalledOnRealmResults);
    }
}
