/*
 * Copyright 2018 Realm Inc.
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

package io.realm.examples.arch;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.support.annotation.MainThread;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.arch.model.Person;


public class BackgroundTask {
    private static final Object lock = new Object();

    private static final String TAG = "BackgroundTask";

    private boolean isStarted;

    private volatile Thread thread;

    @MainThread
    public boolean isStarted() {
        return isStarted;
    }

    @MainThread
    public void start() {
        synchronized (lock) {
            if (isStarted) {
                return;
            }
            thread = new IncrementThread();
            thread.start();
            isStarted = true;
            Log.i(TAG, "Background job started.");
        }
    }

    @MainThread
    public void stop() {
        synchronized (lock) {
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
            isStarted = false;
        }
    }

    private static final class IncrementThread extends Thread {
        IncrementThread() {
            super("Aging thread");
        }

        @Override
        @SuppressLint("NewApi")
        public void run() {
            try (Realm realm = Realm.getDefaultInstance()) {
                final RealmResults<Person> persons = realm.where(Person.class).findAll();
                Realm.Transaction transaction = (Realm r) -> {
                    for (Person person : persons) {
                        person.setAge(person.getAge() + 1); // updates the Persons in the Realm.
                    }
                };

                while (!isInterrupted()) {
                    realm.executeTransaction(transaction);
                    SystemClock.sleep(1000L);
                }
            }
            Log.i(TAG, "Background job stopped.");
        }
    }
}

