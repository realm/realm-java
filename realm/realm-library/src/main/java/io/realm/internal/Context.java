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

package io.realm.internal;

import java.lang.ref.ReferenceQueue;

// Currently we free native objects in two threads, the SharedGroup is freed in the caller thread, others are freed in
// PhantomDaemon thread. The only reason we have this class is that there is a concurrency problem when freeing a row
// which hold the last reference to a table -- freeing row will be locked on a mutex and the same mutex will be freed
// when the corresponding table gets freed. So this class can go away when core doesn't hold the mutex itself as a
// member of table.
public class Context {
    private final static ReferenceQueue<NativeObject> referenceQueue = new ReferenceQueue<NativeObject>();
    private final static Thread phantomThread = new Thread(new FinalizerRunnable(referenceQueue));

    static {
        phantomThread.setName("RealmPhantomDaemon");
        phantomThread.start();
    }

    void addReference(NativeObject referent) {
        new NativeObjectReference(this, referent, referenceQueue);
    }
}
