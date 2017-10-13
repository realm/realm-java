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

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;


/**
 * This class is used for holding the reference to the native pointers present in NativeObjects.
 * This is required as phantom references cannot access the original objects for this value.
 * The phantom references will be stored in a double linked list to avoid the reference itself gets GCed. When the
 * referent get GCed, the reference will be added to the ReferenceQueue. Loop in the daemon thread will retrieve the
 * phantom reference from the ReferenceQueue then dealloc the referent and remove the reference from the double linked
 * list. See {@link FinalizerRunnable} for more implementation details.
 */
final class NativeObjectReference extends PhantomReference<NativeObject> {

    // Linked list to keep the reference of the PhantomReference
    private static class ReferencePool {
        NativeObjectReference head;

        synchronized void add(NativeObjectReference ref) {
            ref.prev = null;
            ref.next = head;
            if (head != null) {
                head.prev = ref;
            }
            head = ref;
        }

        synchronized void remove(NativeObjectReference ref) {
            NativeObjectReference next = ref.next;
            NativeObjectReference prev = ref.prev;
            ref.next = null;
            ref.prev = null;
            if (prev != null) {
                prev.next = next;
            } else {
                head = next;
            }
            if (next != null) {
                next.prev = prev;
            }
        }
    }

    // The pointer to the native object to be handled
    private final long nativePtr;
    // The pointer to the native finalize function
    private final long nativeFinalizerPtr;
    private final NativeContext context;
    private NativeObjectReference prev;
    private NativeObjectReference next;

    private static ReferencePool referencePool = new ReferencePool();

    NativeObjectReference(NativeContext context,
            NativeObject referent,
            ReferenceQueue<? super NativeObject> referenceQueue) {
        super(referent, referenceQueue);
        this.nativePtr = referent.getNativePtr();
        this.nativeFinalizerPtr = referent.getNativeFinalizerPtr();
        this.context = context;
        referencePool.add(this);
    }

    /**
     * To dealloc native resources.
     */
    void cleanup() {
        synchronized (context) {
            nativeCleanUp(nativeFinalizerPtr, nativePtr);
        }
        // Remove the PhantomReference from the pool to free it.
        referencePool.remove(this);
    }

    /**
     * Calls the native finalizer function to free the given native pointer.
     */
    private static native void nativeCleanUp(long nativeFinalizer, long nativePointer);
}
