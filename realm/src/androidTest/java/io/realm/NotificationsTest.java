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

import android.os.Looper;
import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.entities.Dog;

public class NotificationsTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        Realm.realmsCache.remove();
        Realm.deleteRealmFile(getContext());
    }

    public void testFailureOnNonLooperThread() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    @SuppressWarnings("UnusedDeclaration") Realm realm = Realm.getInstance(getContext());
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                }
            }
        });

        Boolean result = future.get();
        assertTrue(result);
    }

    public void testNotifications() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        final AtomicInteger changed = new AtomicInteger(0);

        List<Callable<Void>> callables = new ArrayList<Callable<Void>>();
        callables.add(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Looper.prepare();
                Realm realm = Realm.getInstance(getContext()); // This does not sent a message to itself [0]
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        changed.incrementAndGet();
                    }
                });
                Looper.loop();
                return null;
            }
        });
        callables.add(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Realm realm = Realm.getInstance(getContext(), false); // This will notify the other thread [1]
                realm.beginTransaction();
                Dog dog = realm.createObject(Dog.class);
                dog.setName("Rex");
                realm.commitTransaction(); // This will notify the other thread[2]
                return null;
            }
        });

        executorService.invokeAll(callables, 2, TimeUnit.SECONDS);

        assertEquals(2, changed.get());
    }

    public void testFailingSetAutoRefreshOnNonLooperThread() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Realm realm = Realm.getInstance(getContext(), false);
                boolean autoRefresh = realm.isAutoRefresh();
                assertFalse(autoRefresh);
                try {
                    realm.setAutoRefresh(true);
                    return false;
                } catch (IllegalStateException ignored) {
                    return true;
                }
            }
        });
        assertTrue(future.get());
    }

    public void testSetAutoRefreshOnHandlerThread() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Looper.prepare();
                Realm realm = Realm.getInstance(getContext());
                assertTrue(realm.isAutoRefresh());
                realm.setAutoRefresh(false);
                assertFalse(realm.isAutoRefresh());
                realm.setAutoRefresh(true);
                assertTrue(realm.isAutoRefresh());
                return true;
            }
        });
        assertTrue(future.get());
    }

    public void testNotificationsNumber () throws InterruptedException, ExecutionException {
        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicBoolean isReady = new AtomicBoolean(false);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Looper.prepare();
                Realm realm = Realm.getInstance(getContext());
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        counter.incrementAndGet();
                    }
                });
                isReady.set(true);
                Looper.loop();
                return true;
            }
        });

        // Wait until the looper is started
        while (!isReady.get()) {
            Thread.sleep(5);
        }
        Thread.sleep(100); 

        Realm realm = Realm.getInstance(getContext(), false);
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Rex");
        realm.commitTransaction();

        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException ignore) {}

        assertEquals(2, counter.get());
    }

    public void testAutoUpdateRealmResults() throws InterruptedException, ExecutionException {
        final int TEST_SIZE = 10;
        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicBoolean isReady = new AtomicBoolean(false);
        final Map<Integer, Integer> results = new ConcurrentHashMap<Integer, Integer>();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Looper.prepare();
                Realm.deleteRealmFile(getContext());
                final Realm realm = Realm.getInstance(getContext());
                final RealmResults<Dog> dogs = realm.allObjects(Dog.class);
                assertEquals(0, dogs.size());
                realm.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        int c = counter.incrementAndGet();
                        results.put(c, dogs.size());
                    }
                });
                isReady.set(true);
                Looper.loop();
                return true;
            }
        });

        // Wait until the looper is started
        while (!isReady.get()) {
            Thread.sleep(5);
        }
        Thread.sleep(100);

        Realm realm = Realm.getInstance(getContext());
        realm.beginTransaction();
        for (int i = 0; i < TEST_SIZE; i++) {
            Dog dog = realm.createObject(Dog.class);
            dog.setName("Rex " + i);
        }
        realm.commitTransaction();
        assertEquals(TEST_SIZE, realm.allObjects(Dog.class).size());

        try {
            future.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException ignore) {}

        assertEquals(2, results.size());

        assertTrue(results.containsKey(1));
        assertEquals(0, results.get(1).intValue());
        assertTrue(results.containsKey(2));
        assertEquals(10, results.get(2).intValue());

        assertEquals(2, counter.get());
    }
}
