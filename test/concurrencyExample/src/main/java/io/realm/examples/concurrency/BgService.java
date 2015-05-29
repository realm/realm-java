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

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.examples.concurrency.model.Person;

public class BgService extends IntentService {

    public static final String TAG = BgService.class.getName();

    //This needs to be a volatile, since #onHandleIntent runs on a 'HandlerThread'
    //and #onDestroy is a a lifecycle callback invoked from the 'main Thread' (avoid liveness pb)
    private volatile boolean serviceQuitting = false;

    public static final String REALM_FILE_EXTRA = "RealmFileExtra";

    private Realm realm = null;

    public BgService() {
        super(BgSpawningService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Starting intent...");

        realm = Realm.getInstance(this);
        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d(TAG, "RECEIVED_NOTIFICATION: " + Thread.currentThread().toString());
            }
        });

        while(!serviceQuitting) {
            int iterCount = 0;
            realm.beginTransaction();
            while (iterCount < 20 && serviceQuitting == false) {
                if ((iterCount % 1000) == 0) {
                    Log.d(TAG, "WR_OPERATION#: " + iterCount + "," + Thread.currentThread().getName());
                }

                Person person = realm.createObject(Person.class);
                person.setName("New person");
                iterCount++;
            }
            realm.commitTransaction();
        }

        Log.d(TAG, "Service has quit");
    }

    @Override
    public void onDestroy() {
        this.serviceQuitting = true;
    }

}
