/*
 * Copyright 2021 Realm Inc.
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

/**
 * TODO
 */
public class OsMapChangeSet implements NativeObject {

    public static final int EMPTY_CHANGESET = 0;

    private static long finalizerPtr = nativeGetFinalizerPtr();

    private final long nativePtr;

    public OsMapChangeSet(long nativePtr) {
        this.nativePtr = nativePtr;
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return finalizerPtr;
    }

    public boolean isEmpty() {
        return nativePtr == OsMapChangeSet.EMPTY_CHANGESET;
    }

    public long getDeletionCount() {
        return nativeGetDeletionCount(nativePtr);
    }

    public String[] getStringKeyInsertions() {
        return nativeGetStringKeyInsertions(nativePtr);
    }

    public String[] getStringKeyModifications() {
        return nativeGetStringKeyModifications(nativePtr);
    }

    // TODO: add more insertions and modifications methods for other types of keys ad-hoc

    private static native long nativeGetFinalizerPtr();

    private static native long nativeGetDeletionCount(long nativePtr);

    private static native String[] nativeGetStringKeyInsertions(long nativePtr);

    private static native String[] nativeGetStringKeyModifications(long nativePtr);
}
