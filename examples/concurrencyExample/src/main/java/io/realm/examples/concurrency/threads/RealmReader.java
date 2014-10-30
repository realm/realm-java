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

package io.realm.examples.concurrency.threads;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.examples.concurrency.model.Person;

public class RealmReader extends Thread implements KillableThread {

    public static final String TAG = RealmReader.class.getName();

    private Context context;
    private int mReadCount = 0;

    public RealmReader(Context context) {
        this.context = context;
    }

    public void run() {
        Looper.prepare();
        final Realm realm = Realm.getInstance(context, true);

        realm.addChangeListener(new RealmChangeListener() {

            @Override
            public void onChange() {
                long peopleNumber = realm.where(Person.class).count();
                if (peopleNumber % 10 == 0) {
                    Log.d(TAG, "Found count " + peopleNumber);
                }

            }
        });
        Looper.loop();
    }

    @Override
    public void terminate() {
        Looper.myLooper().quit();
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getReadCount() {
        return mReadCount;
    }

    public void setReadCount(int mReadCount) {
        this.mReadCount = mReadCount;
    }
}
