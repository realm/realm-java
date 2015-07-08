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

package io.realm.internal;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This runnable performs the garbage collection of the references stored in the reference queue.
 */
public class FinalizerRunnable implements Runnable {

    // Store the row references. Without this, objects would be garbage collected immediately so don't remove this! ;)
    // A ConcurrentHashMap is used in lack of a ConcurrentHashSet in the Java API.
    // ConcurrentLinkedQueue was tried instead, but the removal operation turned out to be too slow.
    static final Map<Reference<?>, Boolean> references = new ConcurrentHashMap<Reference<?>, Boolean>();

    // This is the actual reference queue in which the garbage collector will insert the row instances ready to be
    // cleaned up
    static final ReferenceQueue<NativeObject> referenceQueue = new ReferenceQueue<NativeObject>();

    @Override
    public void run() {

        NativeObjectReference reference;
        while (true) {
            try {
                reference = (NativeObjectReference) referenceQueue.remove();
                references.remove(reference);
                UncheckedRow.nativeClose(reference.nativePointer);
            } catch (InterruptedException e) {
                //restore interrupted exception
                Thread.currentThread().interrupt();
            }
        }
    }
}
