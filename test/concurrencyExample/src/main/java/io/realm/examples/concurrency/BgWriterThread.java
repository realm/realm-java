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

package io.realm.examples.concurrency;

import android.content.Context;
import android.util.Log;

import io.realm.Realm;
import io.realm.examples.concurrency.model.Person;

public class BgWriterThread extends Thread implements KillableThread {

    public static final String TAG = BgWriterThread.class.getName();

    private Realm   realm   = null;
    private Context context = null;

    public BgWriterThread(Context context) {
        this.context = context;
    }

    public void run() {
        realm = Realm.getInstance(context);
        int iterCount = 0;

        realm.beginTransaction();
        while (iterCount < 1000000 && running == true) {
            if ((iterCount % 1000) == 0) {
                Log.d(TAG, "WR_OPERATION#: " + iterCount + "," + Thread.currentThread().getName());
            }

            Person person = realm.createObject(Person.class);
            person.setName("New person");
            iterCount++;
        }
        realm.commitTransaction();
    }

    //This needs to be volatile since we're exposing a public method that could be invoked
    //from a different Thread (avoid Liveness pb)
    private boolean running = true;

    @Override
    public void terminate() {
        running = false;
    }
}
