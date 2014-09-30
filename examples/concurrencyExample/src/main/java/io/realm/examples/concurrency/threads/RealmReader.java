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
import android.util.Log;

import io.realm.Realm;
import io.realm.examples.concurrency.model.Person;

public class RealmReader extends Thread implements KillableThread {

    public static final String TAG = RealmReader.class.getName();

    private Realm realm = null;
    private Context context = null;

    private boolean mRunning = true;

    private int mReadCount = 0;

    public RealmReader(Context context) {
        this.context = context;
    }

    public void run() {
        realm = Realm.getInstance(context);

        int loopCount = 0;

        while (loopCount < mReadCount && mRunning) {
            Person person = realm.where(Person.class)
                    .beginsWith("name", "Foo")
                    .between("age", 20, 50).findFirst();

            if(loopCount % 1000 == 0) {
                Log.d(TAG, "Found: " + person);
            }
            loopCount++;
        }
    }

    @Override
    public void terminate() {
        mRunning = false;
    }

    public int getReadCount() {
        return mReadCount;
    }

    public void setReadCount(int mReadCount) {
        this.mReadCount = mReadCount;
    }
}
