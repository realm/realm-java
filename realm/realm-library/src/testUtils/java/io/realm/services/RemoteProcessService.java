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
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesModelModule;

/**
 * Helper service for multi-processes support testing.
 * @deprecated use {@link RemoteTestService} instead.
 */
public class RemoteProcessService extends Service {

    public abstract static class Step {
        public final int message;
        private Step(int message) {
            this.message = message;
            stepMap.put(message, this);
        }

        abstract void run();

        // Passes a null to tell main process that everything is OK.
        // Otherwise, passes a error String which will be used by assertion in main process.
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

    private final Messenger messenger = new Messenger(new IncomingHandler());
    private Messenger client;

    public RemoteProcessService() {
        if (thiz != null) {
            throw new RuntimeException("Only one instance is allowed!");
        }
        thiz = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
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

    // Calls this function to return the String of current class and line number.
    private static String currentLine() {
        StackTraceElement element = new Throwable().getStackTrace()[1];
        return element.getClassName() + " line " + element.getLineNumber() + ": ";
    }

    public final static Step stepCreateInitialRealm_A = new Step(10) {

        @Override
        void run() {
            thiz.testRealm = Realm.getInstance(getConfiguration());
            int expected = 1;
            long got = thiz.testRealm.where(AllTypes.class).count();
            if (expected == got) {
                response(null);
            } else {
                response(currentLine() + "expected: " + expected + ", but got " + got);
            }
            thiz.testRealm.close();
        }
    };

    public final static Step stepExitProcess_A = new Step(20) {

        @Override
        void run() {
            thiz.testRealm = Realm.getInstance(getConfiguration());
            thiz.testRealm.close();
            response(null);
            Runtime.getRuntime().exit(0);
        }
    };

    private static RealmConfiguration getConfiguration() {
        return new RealmConfiguration.Builder().modules(new AllTypesModelModule()).build();
    }

}
