/*
 * Copyright 2014 Realm Inc.
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
import android.os.Looper;
import android.test.AndroidTestCase;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.Dog;

public class NotificationsTest extends AndroidTestCase {
    public void testFailureOnNonLooperThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    @SuppressWarnings("UnusedDeclaration") Realm realm = Realm.getInstance(getContext());
                    fail("The Realm instantiations should have thrown an exception");
                } catch (IllegalStateException ignored) {}
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testNotifications() {
        final AtomicBoolean changed = new AtomicBoolean(false);

        Thread listenerThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Realm realm = Realm.getInstance(getContext());
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        changed.set(true);
                        Looper.myLooper().quit();
                    }
                });
                Looper.loop();
            }
        };
        listenerThread.start();

        Thread writerThread = new Thread() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(getContext(), false);
                realm.beginTransaction();
                Dog dog = realm.createObject(Dog.class);
                dog.setName("Rex");
                realm.commitTransaction();
            }
        };
        writerThread.start();

        try {
            writerThread.join();
            listenerThread.join(2000);
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(true, changed.get());
    }

    public void testNotificationsPlusSelfReceive() {
        final AtomicInteger counter = new AtomicInteger(0);
        final Queue<Handler> handlers = new ConcurrentLinkedQueue<Handler>();

        Thread listenerThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Realm realm = Realm.getInstance(getContext());
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        counter.incrementAndGet();
                        Looper.myLooper().quit();
                    }
                });
                Looper.loop();
            }
        };
        listenerThread.start();

        Thread writerThread = new Thread() {

            @Override
            public void run() {
                Looper.prepare();
                Realm realm = Realm.getInstance(getContext());
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        counter.incrementAndGet();
                    }
                });
                Handler handler = new Handler();
                handlers.add(handler);
                Looper.loop();
            }
        };
        writerThread.start();
        while (handlers.isEmpty()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                fail();
            }
        }
        Handler handler = handlers.poll();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(getContext());
                realm.beginTransaction();
                Dog dog = realm.createObject(Dog.class);
                dog.setName("Rex");
                realm.commitTransaction();
                Looper.myLooper().quit();
            }
        });

        try {
            writerThread.join();
            listenerThread.join(2000);
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(2, counter.get());
    }

    public void testFailingSetAutoRefreshOnNonLooperThread() {
        final AtomicBoolean done = new AtomicBoolean(false);
        Thread thread = new Thread() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(getContext(), false);
                boolean autoRefresh = realm.isAutoRefresh();
                assertFalse(autoRefresh);
                try {
                    realm.setAutoRefresh(true);
                    fail();
                } catch (IllegalStateException ignored) {}
                done.set(true);
            }
        };
        thread.start();
        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(done.get());
    }

    public void testSetAutoRefreshOnHandlerThread() {
        final AtomicBoolean done = new AtomicBoolean(false);
        HandlerThread thread = new HandlerThread("TestThread") {
            @Override
            protected void onLooperPrepared() {
                Realm realm = Realm.getInstance(getContext());
                assertTrue(realm.isAutoRefresh());
                realm.setAutoRefresh(false);
                assertFalse(realm.isAutoRefresh());
                realm.setAutoRefresh(true);
                assertTrue(realm.isAutoRefresh());
                done.set(true);
            }
        };
        thread.start();
        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(done.get());
    }

    public void testAutoUpdateRealmResults() {
        final int TEST_SIZE = 10;
        final AtomicInteger counter = new AtomicInteger(0);
        final Queue<Handler> handlers = new ConcurrentLinkedQueue<Handler>();

        Thread listenerThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Realm r = Realm.getInstance(getContext());
                final RealmResults<Dog> dogs = r.allObjects(Dog.class);
                r.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                    counter.addAndGet(dogs.size());
                    Looper.myLooper().quit();
                    }
                });
                Looper.loop();
            }
        };
        listenerThread.start();

        Realm realm = Realm.getInstance(getContext());
        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            Dog dog = realm.createObject(Dog.class);
            dog.setName("Rex "+i);
        }
        realm.commitTransaction();

        try {
            listenerThread.join(2000);
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(TEST_SIZE, counter.get());
    }
}
