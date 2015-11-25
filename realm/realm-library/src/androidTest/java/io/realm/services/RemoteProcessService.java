/*
 * Copyright 2015 Realm Inc.
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

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.entities.AllTypes;

/**
 * Helper service for multi-processes support testing.
 */
public class RemoteProcessService extends Service {

    public abstract static class Step {
        public final int message;
        private Step(int message) {
            this.message = message;
            if (stepMap.containsKey(message)) {
                throw new RuntimeException("Duplicated message " + message + " in stepMap!");
            }
            stepMap.put(message, this);
        }

        abstract void run();

        // Pass a null to tell main process that everything is OK.
        // Otherwise, pass a error String which will be used by assertion in main process.
        protected void response(String error) {
            try {
                Message msg = Message.obtain(null, message);
                if (error != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BUNDLE_KEY_ERROR, error);
                    msg.setData(bundle);
                }
                thiz.client.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static final String BUNDLE_KEY_ERROR = "error";
    private static Map<Integer, Step> stepMap = new HashMap<Integer, Step>();

    private static RemoteProcessService thiz;
    private Realm testRealm;
    private RealmChangeListener listener;

    private final Messenger messenger = new Messenger(new IncomingHandler());
    private Messenger client;

    public RemoteProcessService() {
        if (thiz != null) {
            throw new RuntimeException("Only one instance is allowed!");
        }
        thiz = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
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
            if (step != null) {
                step.run();
            } else {
                throw new RuntimeException("Cannot find corresponding step to message " + msg.what + ".");
            }
        }
    }

    // Call this function to return the String of current class and line number.
    private static String currentLine() {
        StackTraceElement element = new Throwable().getStackTrace()[1];
        return element.getClassName() + " line " + element.getLineNumber() + ": ";
    }

    // ======== testCreateInitialRealm ========
    public final static Step stepCreateInitialRealm_A = new Step(10) {
        @Override
        void run() {
            thiz.testRealm = Realm.getInstance(thiz);
            int expected = 1;
            int got = thiz.testRealm.allObjects(AllTypes.class).size();
            if (expected == got) {
                response(null);
            } else {
                response(currentLine() + "expected: " + expected + ", but got " + got);
            }
            thiz.testRealm.close();
        }
    };

    // ======== testExitProcess ========
    public final static Step stepExitProcess_A = new Step(20) {
        @Override
        void run() {
            thiz.testRealm = Realm.getInstance(thiz);
            thiz.testRealm.close();
            response(null);
            Runtime.getRuntime().exit(0);
        }
    };

    // ======== testNotification ========
    public final static Step stepNotification_A = new Step(30) {
        @Override
        void run() {
            thiz.testRealm = Realm.getInstance(thiz);
            thiz.testRealm.beginTransaction();
            thiz.testRealm.createObject(AllTypes.class);
            thiz.testRealm.commitTransaction();
            response(null);
        }
    };

    public final static Step stepNotification_B = new Step(31) {
        @Override
        void run() {
            thiz.listener = new RealmChangeListener() {
                @Override
                public void onChange() {
                    int expected = 2;
                    int got = thiz.testRealm.allObjects(AllTypes.class).size();
                    thiz.testRealm.removeAllChangeListeners();
                    thiz.testRealm.close();
                    if (expected == got) {
                        response(null);
                    } else {
                        response(currentLine() + "expected: " + expected + ", but got " + got);
                    }
                }
            };

            thiz.testRealm.addChangeListener(thiz.listener);
            // Starting loop to wait for the change listener
            Looper.loop();
        }
    };

    // ======== testSetAutoRefresh ========
    public final static Step stepSetAutoRefresh_A = new Step(40) {
        @Override
        void run() {
            thiz.testRealm = Realm.getInstance(thiz);
            thiz.testRealm.beginTransaction();
            thiz.testRealm.createObject(AllTypes.class);
            thiz.testRealm.commitTransaction();
            try {
                // Sleep to give some time that main process can have a chance handle the REALM_CHANGED before the
                // response to trigger next step.
                Thread.sleep(100);
            } catch (InterruptedException e) {
                response(e.getMessage());
                return;
            }
            response(null);
        }
    };

    public final static Step stepSetAutoRefresh_B = new Step(41) {
        @Override
        void run() {
            thiz.testRealm.beginTransaction();
            thiz.testRealm.createObject(AllTypes.class);
            thiz.testRealm.commitTransaction();
            thiz.testRealm.close();
            response(null);
        }
    };

    // ======== testCompact ========
    public final static Step stepCompact_A = new Step(50) {
        @Override
        void run() {
            thiz.testRealm = Realm.getInstance(thiz);
            response(null);
        }
    };
    public final static Step stepCompact_B = new Step(51) {
        @Override
        void run() {
            thiz.testRealm.beginTransaction();
            response(null);
        }
    };
    public final static Step stepCompact_C = new Step(52) {
        @Override
        void run() {
            thiz.testRealm.cancelTransaction();
            thiz.testRealm.close();
            response(null);
        }
    };

    // ======== testDelete ========
    public final static Step stepDelete_A = new Step(60) {
        @Override
        void run() {
            RealmConfiguration configuration = new RealmConfiguration.Builder(thiz).build();
            File file = new File(configuration.getPath());
            if (!file.exists()) {
                response(configuration.getPath() + " doesn't exist!");
                return;
            }
            if (!Realm.deleteRealm(configuration)) {
                response("deleteRealm on " + configuration.getPath() + " failed!");
                return;
            }

            response(null);
        }
    };
}
