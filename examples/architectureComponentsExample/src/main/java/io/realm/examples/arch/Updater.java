/*
 * Copyright 2017 Realm Inc.
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

import android.arch.lifecycle.ViewModel;
import android.os.SystemClock;
import android.support.annotation.MainThread;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.arch.model.Person;


public class Updater extends ViewModel {

    private volatile Thread thread;

    @MainThread
    public void start() {
        if (thread != null) {
            // already running
            return;
        }

        thread = new IncrementThread();
        thread.start();
    }

    @MainThread
    public void stop() {
        thread = null;
    }

   @Override
    protected void onCleared() {
       stop();
    }

    final class IncrementThread extends Thread {
        IncrementThread() {
            super("increment thread");
        }

        @Override
        public void run() {
            Realm realm = Realm.getDefaultInstance();
            //noinspection TryFinallyCanBeTryWithResources
            try {
                final RealmResults<Person> all = realm.where(Person.class).findAll();
                Realm.Transaction transaction = new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (Person person : all) {
                            person.age = person.age + 1;
                        }
                    }
                };

                while (thread == this) {
                    realm.executeTransaction(transaction);
                    SystemClock.sleep(1000L);
                }
            } finally {
                realm.close();
            }
        }
    }
}

