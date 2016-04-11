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
package io.realm.examples.realmadapters;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.Realm;

public class WorkerThread extends Thread {

    public Handler workerHandler;
    private CountDownLatch realmOpen;

    @Override
    public void run() {
        Realm realm = null;
        try {
            Looper.prepare();
            realm = Realm.getDefaultInstance();
            realmOpen = new CountDownLatch(1);
            workerHandler = new WorkerHandler(realm);
            Looper.loop();
        } finally {
            if (realm != null) {
                realm.close();
                realmOpen.countDown();
            }
        }
    }

    public void quit() {
        workerHandler.getLooper().quit();
        try {
            realmOpen.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
