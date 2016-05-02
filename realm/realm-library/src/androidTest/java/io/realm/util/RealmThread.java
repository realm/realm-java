/*
 * Copyright 2016 Realm Inc.
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

package io.realm.util;

import io.realm.Realm;
import io.realm.RealmConfiguration;

// this is a simple example of how Realm can be encapsulated within a Thread wrapper.
public class RealmThread extends Thread {

    public interface RealmRunnable {
        void run(final Realm realm);
    }

    private final RealmConfiguration realmConfig;
    private final RealmRunnable task;
    private Realm realm;

    public RealmThread(RealmConfiguration realmConfig, RealmRunnable task) {
        // ideally, you can create a realm instance with default configuration
        this.realmConfig = realmConfig;
        this.task = task;
    }

    @Override
    public void run() {
        final Realm realm = Realm.getInstance(this.realmConfig);
        this.realm = realm;
        if (task != null) {
            task.run(realm);
        }
        if (!realm.isClosed()) {
            realm.close();
        }
        this.realm = null;
    }

    public void end() {
        if (!this.isAlive()) {
            throw new IllegalStateException("RealmThread is not running.");
        }
        if (this.realm == null) {
            throw new IllegalStateException("A Realm assigned to this Thread is closed.");
        }
        this.realm.stopWaitForChange();
    }
}
