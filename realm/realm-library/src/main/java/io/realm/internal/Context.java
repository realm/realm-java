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

public class Context {

    private final static ReferenceQueue<NativeObject> referenceQueue = new ReferenceQueue<NativeObject>();
    private final static Thread phantomThread = new Thread(new FinalizerRunnable(referenceQueue));

    static {
        phantomThread.start();
    }

    synchronized void addReference(NativeObject referent) {
        //referencesPool.add(new NativeObjectReference(this, referent, referenceQueue, referencesPool.getFreeIndex()));
        new NativeObjectReference(this, referent, referenceQueue);
    }

    synchronized void executeDelayedDisposal() {
        //cleanNativeReferences();
    }

    public void cleanNativeReferences() {
        NativeObjectReference reference = (NativeObjectReference) referenceQueue.poll();
        while (reference != null) {
            // Dealloc the native resources
            reference.cleanup();
            // Inline referencesPool.remove() to make it faster.
            // referencesPool.pool.set(index, null); is not really needed. Make it faster by not
            // setting the slot to null.
            reference = (NativeObjectReference) referenceQueue.poll();
        }
    }
}
