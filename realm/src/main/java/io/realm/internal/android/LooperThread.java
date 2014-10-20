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

package io.realm.internal.android;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This thread continuously receives and dispatches messages regarding changes in realms.
 */
public class LooperThread extends Thread {
    // Message types
    public static final int REALM_CHANGED  = 3;
    public static final Map<Handler, Integer> handlers = new ConcurrentHashMap<Handler, Integer>();

    public Handler handler;
    public List<RuntimeException> exceptions = new ArrayList<RuntimeException>();

    private static final String TAG = LooperThread.class.getName();
    private static LooperThread instance;

    // private because it's a singleton
    private LooperThread() {}

    // thread safe static constructor
    public static LooperThread getInstance() {
        if (instance == null) {
            synchronized (LooperThread.class) {
                if (instance == null) {
                    instance = new LooperThread();
                }
            }
        }
        return instance;
    }

    @Override
    public void run() {
        Looper.prepare();

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.arg1 == REALM_CHANGED) {
                    for (Map.Entry<Handler, Integer> entry : handlers.entrySet()) {
                        if (entry.getValue() == message.arg2) {
                            Handler currentHandler = entry.getKey();
                            if (currentHandler.getLooper().getThread().isAlive() &&
                                !currentHandler.hasMessages(REALM_CHANGED))
                            {
                                try {
                                    currentHandler.sendEmptyMessage(REALM_CHANGED);
                                } catch (RuntimeException e) {
                                    exceptions.add(e);
                                    Log.w(TAG, e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        };

        Looper.loop();
    }
}
