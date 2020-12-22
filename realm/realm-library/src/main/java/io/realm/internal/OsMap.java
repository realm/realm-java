/*
 * Copyright 2020 Realm Inc.
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

import javax.annotation.Nullable;

/**
 * FIXME
 */
public class OsMap implements NativeObject {

    private final long nativePtr;
    private final NativeContext context;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    public OsMap(UncheckedRow row, long columnKey) {
        OsSharedRealm sharedRealm = row.getTable().getSharedRealm();
        this.nativePtr = nativeCreate(sharedRealm.getNativePtr(), row.getNativePtr(), columnKey);
        this.context = sharedRealm.context;
        context.addReference(this);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public long size() {
        return nativeSize(nativePtr);
    }

    public void clear() {
        nativeClear(nativePtr);
    }

    public void put(Object key, boolean value) {
        // TODO: handle other types of keys and avoid typecasting directly to string in phase 2
        nativePutBoolean(nativePtr, (String) key, value);
    }

    public void put(Object key, OsMixed value) {
        // TODO: handle other types of keys and avoid typecasting directly to string in phase 2
        nativePutMixed(nativePtr, (String) key, value.getNativePtr());
    }

    public void remove(Object key) {
        // TODO: handle other types of keys and avoid typecasting directly to string in phase 2
        nativeRemove(nativePtr, (String) key);
    }

    @Nullable
    public Object get(Object key) {
        // TODO: handle other types of keys and avoid typecasting directly to string in phase 2
        return nativeGetValue(nativePtr, (String) key);
    }

    private static native long nativeGetFinalizerPtr();

    private static native long nativeCreate(long nativeSharedRealmPtr, long nativeRowPtr, long columnKey);

    private static native Object nativeGetValue(long nativePtr, String key);

    private static native void nativePutMixed(long nativePtr, String key, long nativeObjectPtr);

    private static native void nativePutBoolean(long nativePtr, String key, boolean value);

    private static native long nativeSize(long nativePtr);

    private static native void nativeClear(long nativePtr);

    private static native void nativeRemove(long nativePtr, String key);
}
