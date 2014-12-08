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

package io.realm.examples.service.threads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.examples.service.model.Person;

public class RealmReader extends Thread implements KillableThread {

    public static final String TAG = RealmReader.class.getName();
    private static final int KILL = 672783478;

    private Context context;
    private int mReadCount = 0;
    private Handler handler;

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
        
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == KILL) {
                    Looper.myLooper().quit();
                }
            }
        };
        Looper.loop();
    }

    @Override
    public void terminate() {
        handler.sendEmptyMessage(KILL);
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getReadCount() {
        return mReadCount;
    }

    public void setReadCount(int mReadCount) {
        this.mReadCount = mReadCount;
    }
}
