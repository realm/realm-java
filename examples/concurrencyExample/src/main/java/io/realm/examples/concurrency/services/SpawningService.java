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

package io.realm.examples.concurrency.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.examples.concurrency.threads.KillableThread;
import io.realm.examples.concurrency.threads.RealmReader;
import io.realm.examples.concurrency.threads.RealmWriter;

public class SpawningService extends Service {

    public static final String TAG = SpawningService.class.getName();
    public static final String REALM_INSERT_COUNT_EXTRA = "RealmInsertCountExtra";
    public static final String REALM_READ_COUNT_EXTRA = "RealmReadCountExtra";

    private List<KillableThread> allThreads = new ArrayList<KillableThread>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.i(TAG, "The intent is null");
            throw new AssertionError();
        }

        int insertCount = intent.getIntExtra(REALM_INSERT_COUNT_EXTRA, 0);
        int readCount   = intent.getIntExtra(REALM_READ_COUNT_EXTRA, 0);

        try {
            RealmWriter writerThread = new RealmWriter(this);
            writerThread.setInsertCount(insertCount);
            allThreads.add(writerThread);
            writerThread.start();

            RealmReader readerThread = new RealmReader(this);
            readerThread.setReadCount(readCount);
            allThreads.add(readerThread);
            readerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            quit();
        }

        //Service should stay active as long as Activity is active
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onDestroy() {
        quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void quit() {
        for (KillableThread t : allThreads) {
            t.terminate();
        }
    }

}
