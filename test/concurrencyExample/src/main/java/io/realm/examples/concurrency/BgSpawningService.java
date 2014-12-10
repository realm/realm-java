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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BgSpawningService extends Service {

    public static final String TAG = BgSpawningService.class.getName();

    private Boolean serviceQuitting = false;

    public static final String REALM_FILE_EXTRA = "RealmFileExtra";

    private File realmPath = null;

    private List<KillableThread> allThreads = null;

    BgWriterThread wT = null;
    BgReaderThread rT = null;

    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.realmPath = (File)intent.getSerializableExtra(REALM_FILE_EXTRA);
        try {
            allThreads = new ArrayList<KillableThread>();
            wT = new BgWriterThread(this);
            allThreads.add(wT);
            wT.start();
            rT = new BgReaderThread(this);
            allThreads.add(rT);
            rT.start();
        } catch (Exception e) {
            e.printStackTrace();
            quit();
        }
        return START_STICKY;
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
        this.serviceQuitting = true;
        for (KillableThread t : allThreads) {
            t.terminate();
        }
    }

}
