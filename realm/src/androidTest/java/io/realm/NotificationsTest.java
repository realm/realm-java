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

import android.test.AndroidTestCase;
import android.util.Log;

import io.realm.entities.Dog;
import io.realm.internal.android.LooperThread;

public class NotificationsTest extends AndroidTestCase {

    private static int changes;

    private static synchronized void initializeChanges() {
        changes = 0;
    }

    private static synchronized void incrementChanges() {
        changes++;
    }

    private static synchronized int getChanges() {
        return changes;
    }

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        initializeChanges();
        Realm realm = Realm.getInstance(getContext());
        realm.removeAllChangeListeners();
        realm.beginTransaction();
        realm.clear(Dog.class);
        realm.commitTransaction();
    }


    public void testMessageToDeadThread() {
        Realm realm = Realm.getInstance(getContext());

        // Number of handlers before
        final int handlersBefore = LooperThread.handlers.size();

        // Make sure the Looper Thread is alive
        LooperThread looperThread = LooperThread.getInstance();
        assertTrue(looperThread.isAlive());

        Thread thread = new Thread() {
            @Override
            public void run() {
                Realm r = Realm.getInstance(getContext());
                assertFalse(handlersBefore == LooperThread.handlers.size());
                r.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        Log.i("Notification Test", "Notification Received");
                    }
                });
            }
        };
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            fail();
        }
        assertFalse(thread.isAlive()); // Make sure the thread is dead
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Rex");
        realm.commitTransaction();

        // Give some time to log the exception
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(0, looperThread.exceptions.size());
    }

    void testMessageToActive() {
        Realm realm = Realm.getInstance(getContext());

        // Number of handlers before
        final int handlersBefore = LooperThread.handlers.size();

        // Make sure the Looper Thread is alive
        LooperThread looperThread = LooperThread.getInstance();
        assertTrue(looperThread.isAlive());

        // A thread is created, and that thread add a change listener
        Thread thread = new Thread() {
            @Override
            public void run() {
                Realm r = Realm.getInstance(getContext());
                assertFalse(handlersBefore == LooperThread.handlers.size());
                assertEquals(0, getChanges()); // to changes are yet received
                r.addChangeListener(new RealmChangeListener() {
                    @Override
                    public void onChange() {
                        incrementChanges();
                    }
                });
            }
        };
        thread.start();

        assertEquals(0, getChanges());
        realm.beginTransaction();
        realm.clear(Dog.class);
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Rex");
        realm.commitTransaction();

        try {
            thread.join();
        } catch (InterruptedException ignored) {
            fail();
        }
        assertEquals(1, getChanges());
    }

    // all existing change listeners are removed and no new ones are registered
    // => no changes will be received when an object is added to the realm
    void testNoChangeListeners() {
        Realm realm = Realm.getInstance(getContext());

        realm.removeAllChangeListeners();
        realm.beginTransaction();
        realm.clear(Dog.class);
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Rex");
        realm.commitTransaction();
        assertEquals(0, getChanges());
    }

    void testRemoveAllChangeListeners() {
        Realm realm = Realm.getInstance(getContext());

        // create a change listener
        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                incrementChanges();
            }
        });

        // and remove it => to changes will be received
        realm.removeAllChangeListeners();

        realm.beginTransaction();
        realm.clear(Dog.class);
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Rex");
        realm.commitTransaction();
        assertEquals(0, getChanges());
    }

    void testChangeFromOtherThread() {
        incrementChanges();
        Realm realm = Realm.getInstance(getContext());

        // register a change listener
        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                incrementChanges();
            }
        });

        // make a change on another thread
        Thread thread = new Thread() {
            @Override
            public void run() {
                Realm r = Realm.getInstance(getContext());
                r.beginTransaction();
                Dog dog = r.createObject(Dog.class);
                dog.setName("Rex");
                r.commitTransaction();
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            fail();
        }

        // see if change has been received
        assertEquals(1, getChanges());
    }


    // thread A create thread B
    // thread A adds a new object
    // thread B is automatically updated
    public void testAutorefreshToThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                // wait for the main thread to create object
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    fail();
                }
                Realm r = Realm.getInstance(getContext());
                RealmResults<Dog> dogs = r.allObjects(Dog.class);
                assertEquals(1, dogs.size());
                assertEquals("Rex", dogs.first().getName());
                incrementChanges();
            }
        };
        thread.start();

        Realm realm = Realm.getInstance(getContext());
        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Rex");
        realm.commitTransaction();

        try {
            thread.join();
        } catch (InterruptedException ignored) {
            fail();
        }

        assertEquals(1, getChanges());
    }

    // thread A creates thread B
    // thread B adds an object
    // thread A must be automatically be updated
    public void testAutorefreshFromThread() {
        Realm realm = Realm.getInstance(getContext());

        Thread thread = new Thread() {
            @Override
            public void run() {
                Realm r = Realm.getInstance(getContext());
                r.beginTransaction();
                Dog dog = r.createObject(Dog.class);
                dog.setName("Rex");
                r.commitTransaction();
                incrementChanges();
            }
        };

        RealmResults<Dog> dogs1 = realm.allObjects(Dog.class);
        assertEquals(0, dogs1.size());
        assertEquals(0, getChanges());

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException ignored) {
            fail();
        }

        assertEquals(1, getChanges());
        RealmResults<Dog> dogs2 = realm.allObjects(Dog.class);
        assertEquals(1, dogs2.size());
        assertEquals("Rex", dogs2.first().getName());
    }

    // A RealmResults is updated if the realm is changes
    public void testUpdateResultsToThread() {
        Realm realm = Realm.getInstance(getContext());

        Thread thread = new Thread() {
            @Override
            public void run() {
                Realm r = Realm.getInstance(getContext());
                RealmResults<Dog> dogs = r.allObjects(Dog.class);
                assertEquals(0, dogs.size());
                // wait 1 sec and see if updated
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    fail();
                }
                assertEquals(1, dogs.size());
            }
        };
        thread.start();

        realm.beginTransaction();
        Dog dog = realm.createObject(Dog.class);
        dog.setName("Rex");
        realm.commitTransaction();

        try {
            thread.join();
        } catch (InterruptedException ignored) {
            fail();
        }
    }

    public void testUpdateResultsFromThread() {
        Realm realm = Realm.getInstance(getContext());

        Thread thread = new Thread() {
            @Override
            public void run() {
                Realm r = Realm.getInstance(getContext());
                r.beginTransaction();
                Dog dog = r.createObject(Dog.class);
                dog.setName("Rex");
                r.commitTransaction();
            }
        };

        RealmResults<Dog> dogs = realm.allObjects(Dog.class);
        assertEquals(0, dogs.size());

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException ignored) {}

        assertEquals(1, dogs.size());
        assertEquals("Rex", dogs.first().getName());
    }
}