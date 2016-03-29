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
public final class NativeObjectReference extends PhantomReference<NativeObject> {

    // Using int here instead of enum to make it faster since the cleanup needs to be called
    // in a loop to dealloc every native reference.
    public static final int TYPE_LINK_VIEW = 0;
    public static final int TYPE_ROW = 1;

    // The pointer to the native object to be handled
    final long nativePointer;
    final int type;
    // Use boxed type to avoid box/un-box when access the freeIndexList
    final Integer refIndex;

    NativeObjectReference(int type,
                          NativeObject referent,
                          ReferenceQueue<? super NativeObject> referenceQueue,
                          Integer index) {
        super(referent, referenceQueue);
        this.type = type;
        this.nativePointer = referent.nativePointer;
        refIndex = index;
    }

    /**
     * To dealloc native resources.
     */
    void cleanup() {
        switch (type) {
            case TYPE_LINK_VIEW:
                LinkView.nativeClose(nativePointer);
                break;
            case TYPE_ROW:
                UncheckedRow.nativeClose(nativePointer);
                break;
            default:
                // Cannot get here.
                throw new IllegalStateException("Unknown native reference type " + type + ".");
        }
    }
}
