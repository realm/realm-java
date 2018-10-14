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

package io.realm.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.internal.Util;
import io.realm.log.RealmLog;

/**
 * Helper class for multi-processes support testing.
 * @see io.realm.rule.RunWithRemoteService
 */
public abstract class RemoteTestService extends Service {
    // There is no easy way to dynamically ensure step IDs have same value for different processes. So, use the stupid
    // way.
    private static int BASE_MSG_ID = 0;
    protected static int BASE_SIMPLE_COMMIT = BASE_MSG_ID;
    protected static int BASE_A_LOT_COMMITS  = BASE_SIMPLE_COMMIT + 100;

    public static abstract class Step {
        public final int message;

        protected Step(int base, int id) {
            this.message = base + id;
            stepMap.put(this.message, this);
        }

        protected abstract void run();

        protected RemoteTestService getService() {
            return RemoteTestService.thiz;
        }

        // Pass a null to tell main process that everything is OK.
        // Otherwise, pass an error String which will be used by assertion in main process.
        private void response(String error) {
            try {
                Message msg = Message.obtain(null, message);
                if (error != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BUNDLE_KEY_ERROR, error);
                    msg.setData(bundle);
                }
                thiz.client.send(msg);
            } catch (RemoteException e) {
                RealmLog.error(e);
            }
        }
    }

    public static final String BUNDLE_KEY_ERROR = "error";
    @SuppressLint("UseSparseArrays")
    private static Map<Integer, Step> stepMap = new HashMap<Integer, Step>();
    public static RemoteTestService thiz;
    private final Messenger messenger = new Messenger(new IncomingHandler());
    private Messenger client;
    private File rootFolder;
    private Realm realm;

    public RemoteTestService() {
        if (thiz != null) {
            throw new RuntimeException("Only one instance is allowed!");
        }
        thiz = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            rootFolder = File.createTempFile(this.getClass().getSimpleName(), "");
        } catch (IOException e) {
            RealmLog.error(e);
        }
        //noinspection ResultOfMethodCallIgnored
        rootFolder.delete();
        //noinspection ResultOfMethodCallIgnored
        rootFolder.mkdir();

        Realm.init(getApplicationContext());
    }

    public File getRoot() {
        return rootFolder;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        recursiveDelete(rootFolder);
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            thiz.client = msg.replyTo;
            if (thiz.client == null) {
                throw new RuntimeException("Message with an empty client.");
            }
            Step step = stepMap.get(msg.what);
            Throwable throwable = null;
            if (step != null) {
                try {
                    step.run();
                } catch (Throwable t) {
                    throwable = t;
                } finally {
                    if (throwable != null) {
                        step.response(throwable.getMessage() + "\n" + Util.getStackTrace(throwable));
                    } else {
                        step.response(null);
                    }
                }
            } else {
                throw new RuntimeException("Cannot find corresponding step to message " + msg.what + ".");
            }
        }
    }

    private void recursiveDelete(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File each : files) {
                recursiveDelete(each);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    public Realm getRealm() {
        return realm;
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }
}
