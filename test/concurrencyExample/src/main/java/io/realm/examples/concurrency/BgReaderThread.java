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

package io.realm.examples.service;

import android.content.Context;
import android.util.Log;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.examples.service.model.Person;

public class BgReaderThread extends Thread implements KillableThread {

    public static final String TAG = BgReaderThread.class.getName();

    private Context context = null;
    private Realm realm = null;

    public BgReaderThread(Context context) {
        this.context = context;
    }

    public void run() {
        realm = Realm.getInstance(context);

        while (running) {
            try {
                RealmQuery realmQuery = realm.where(Person.class);
                List<Person> list = realmQuery.findAll();
                Log.d(TAG, "First item: " + realmQuery.findFirst());
            } catch (Exception e) {
                e.printStackTrace();
                terminate();
            }
        }
    }

    private boolean running = true;

    @Override
    public void terminate() {
        running = false;
    }
}
