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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nullable;

import io.realm.Mixed;
import io.realm.RealmSet;

public class OsSet implements NativeObject {

    private static final int VALUE_FOUND = 1;
    private static final int VALUE_NOT_FOUND = 0;

    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final long nativePtr;
    private final NativeContext context;
    private final OsSharedRealm osSharedRealm;

    public OsSet(UncheckedRow row, long columnKey) {
        this.osSharedRealm = row.getTable().getSharedRealm();
        this.nativePtr = nativeCreate(osSharedRealm.getNativePtr(), row.getNativePtr(), columnKey);
        this.context = osSharedRealm.context;
        context.addReference(this);
    }

    // Used to freeze sets
    private OsSet(OsSharedRealm osSharedRealm, long nativePtr) {
        this.osSharedRealm = osSharedRealm;
        this.nativePtr = nativePtr;
        this.context = osSharedRealm.context;
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

    public boolean isValid() {
        return nativeIsValid(nativePtr);
    }

    public Object getValueAtIndex(int position) {
        return nativeGetValueAtIndex(nativePtr, position);
    }

    public long size() {
        return nativeSize(nativePtr);
    }

    public boolean contains(@Nullable Object value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else if (value.getClass() == String.class) {
            return nativeContainsString(nativePtr, (String) value);
        } else {
            throw new UnsupportedOperationException("set contains - Hold your horses cowboy...");
        }
    }

    public void add(@Nullable Object value) {
        if (value == null) {
            nativeAddNull(nativePtr);
        } else if (value.getClass() == String.class) {
            nativeAddString(nativePtr, (String) value);
        } else {
            throw new UnsupportedOperationException("set add - Hold your horses cowboy...");
        }
    }

    public void addMixed(Mixed value) {
        // TODO
    }

    public boolean remove(@Nullable Object value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else if (value.getClass() == String.class) {
            indexAndFound = nativeRemoveString(nativePtr, (String) value);
        } else {
            throw new UnsupportedOperationException("set remove - Hold your horses cowboy...");
        }
        return indexAndFound[1] == 1;
    }

    public boolean isSubSetOf(long otherSetNativePtr) {
        return nativeIsSubSetOf(this.nativePtr, otherSetNativePtr);
    }

    public boolean containsAll(Collection<?> collection, Class<?> valueClass) {
        Object[] objects = collection.toArray();

        // It cannot be contained if it is not the same type
        if (objects[0].getClass() != valueClass) {
            return false;
        }

        if (valueClass == String.class) {
            String[] values = collection.toArray(new String[objects.length]);
            return nativeContainsAllString(nativePtr, values);
        } else {
            throw new UnsupportedOperationException("set containsAll - Hold your horses cowboy...");
        }
    }

    public void clear() {
        nativeClear(nativePtr);
    }

    public OsSet freeze(OsSharedRealm frozenSharedRealm) {
        long frozenNativePtr = nativeFreeze(this.nativePtr, frozenSharedRealm.getNativePtr());
        return new OsSet(frozenSharedRealm, frozenNativePtr);
    }

    private static native long nativeGetFinalizerPtr();

    private static native long nativeCreate(long sharedRealmPtr, long nativeRowPtr, long columnKey);

    private static native boolean nativeIsValid(long nativePtr);

    private static native Object nativeGetValueAtIndex(long nativePtr, int position);

    private static native long nativeSize(long nativePtr);

    private static native boolean nativeContainsNull(long nativePtr);

    private static native boolean nativeContainsString(long nativePtr, String value);

    private static native void nativeAddNull(long nativePtr);

    private static native void nativeAddString(long nativePtr, String value);

    private static native long[] nativeRemoveNull(long nativePtr);

    private static native long[] nativeRemoveString(long nativePtr, String value);

    private static native boolean nativeIsSubSetOf(long nativePtr, long otherSetNativePtr);

    private static native boolean nativeContainsAllString(long nativePtr, String[] otherSet);

    private static native void nativeClear(long nativePtr);

    private static native long nativeFreeze(long nativePtr, long frozenRealmPtr);
}
