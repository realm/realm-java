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

import io.realm.MapChangeSet;
import io.realm.Mixed;

/**
 * TODO
 */
public class OsMapChangeSet implements MapChangeSet, NativeObject {

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

    @Override
    public int[] getDeletions() {
        // TODO
        return new int[0];
    }

    @Override
    public Mixed[] getInsertions() {
        // TODO
        return new Mixed[0];
    }

    @Override
    public Mixed[] getModifications() {
        // TODO
        return new Mixed[0];
    }

    private static native long nativeGetFinalizerPtr();
}
