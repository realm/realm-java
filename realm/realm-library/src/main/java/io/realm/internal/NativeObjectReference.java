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
 */
final class NativeObjectReference extends PhantomReference<NativeObject> {

    // The pointer to the native object to be handled
    private final long nativePtr;
    private final long nativeFinalizerPtr;
    // Use boxed type to avoid box/un-box when access the freeIndexList
    final Integer refIndex;

    NativeObjectReference(NativeObject referent,
                          ReferenceQueue<? super NativeObject> referenceQueue,
                          Integer index) {
        super(referent, referenceQueue);
        this.nativePtr = referent.getNativePointer();
        this.nativeFinalizerPtr = referent.getNativeFinalizer();
        refIndex = index;
    }

    /**
     * To dealloc native resources.
     */
    void cleanup() {
        nativeCleanUp(nativeFinalizerPtr, nativePtr);
    }

    private static native void nativeCleanUp(long nativeDestructor, long nativePointer);
}
