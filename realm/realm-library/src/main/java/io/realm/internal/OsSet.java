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

import javax.annotation.Nullable;

import io.realm.internal.core.NativeMixedCollection;

public class OsSet implements NativeObject {

    public enum ExternalCollectionOperation {
        CONTAINS_ALL,
        ADD_ALL,
        REMOVE_ALL,
        RETAIN_ALL
    }

    private static final int VALUE_NOT_FOUND = 0;       // comes from a native boolean
    private static final int VALUE_FOUND = 1;           // comes from a native boolean

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

    public boolean collectionFunnel(NativeMixedCollection collection,
                                    ExternalCollectionOperation operation) {
        switch (operation) {
            case CONTAINS_ALL:
                return nativeContainsAllMixedCollection(nativePtr, collection.getNativePtr());
            case ADD_ALL:
                return nativeAddAllMixedCollection(nativePtr, collection.getNativePtr());
            case REMOVE_ALL:
                return nativeRemoveAllMixedCollection(nativePtr, collection.getNativePtr());
            case RETAIN_ALL:
                return retainAllInternal(collection);
            default:
                throw new IllegalStateException("Unexpected value: " + operation);
        }
    }

    // ----------------------------------------------------
    // String operations
    // ----------------------------------------------------

    public boolean contains(@Nullable String value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {
            return nativeContainsString(nativePtr, value);
        }
    }

    public boolean add(@Nullable String value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddString(nativePtr, value);
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable String value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveString(nativePtr, value);
        }
        return indexAndFound[1] == VALUE_FOUND;
    }

    // ----------------------------------------------------
    // Integer operations
    // ----------------------------------------------------

    public boolean contains(@Nullable Long value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {

            return nativeContainsLong(nativePtr, value);
        }
    }

    public boolean add(@Nullable Integer value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddLong(nativePtr, value.longValue());
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable Integer value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveLong(nativePtr, value.longValue());
        }
        return indexAndFound[1] == VALUE_FOUND;
    }

    // ----------------------------------------------------
    // Long operations
    // ----------------------------------------------------

    public boolean add(@Nullable Long value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddLong(nativePtr, value);
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable Long value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveLong(nativePtr, value);
        }
        return indexAndFound[1] == VALUE_FOUND;
    }

    // ----------------------------------------------------
    // Short operations
    // ----------------------------------------------------

    public boolean add(@Nullable Short value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddLong(nativePtr, value.longValue());
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable Short value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveLong(nativePtr, value.longValue());
        }
        return indexAndFound[1] == VALUE_FOUND;
    }

    // ----------------------------------------------------
    // Byte operations
    // ----------------------------------------------------

    public boolean add(@Nullable Byte value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddLong(nativePtr, value.longValue());
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable Byte value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveLong(nativePtr, value.longValue());
        }
        return indexAndFound[1] == VALUE_FOUND;
    }

    // ----------------------------------------------------
    // Set operations
    // ----------------------------------------------------

    public boolean containsAll(OsSet otherRealmSet) {
        return nativeContainsAll(nativePtr, otherRealmSet.getNativePtr());
    }

    public boolean union(OsSet otherRealmSet) {
        return nativeUnion(nativePtr, otherRealmSet.getNativePtr());
    }

    public boolean asymmetricDifference(OsSet otherSet) {
        return nativeAsymmetricDifference(nativePtr, otherSet.getNativePtr());
    }

    public boolean intersect(OsSet otherSet) {
        return nativeIntersect(nativePtr, otherSet.getNativePtr());
    }

    public void clear() {
        nativeClear(nativePtr);
    }

    public OsSet freeze(OsSharedRealm frozenSharedRealm) {
        long frozenNativePtr = nativeFreeze(this.nativePtr, frozenSharedRealm.getNativePtr());
        return new OsSet(frozenSharedRealm, frozenNativePtr);
    }

    // ----------------------------------------------------
    // Private stuff
    // ----------------------------------------------------

    private boolean retainAllInternal(NativeMixedCollection collection) {
        // If this set is empty the intersection is also the empty set and nothing changes
        if (this.size() == 0) {
            return false;
        }

        // If the other set is empty the intersection is also the empty set
        if (collection.getSize() == 0) {
            this.clear();
            return true;
        }

        return nativeRetainAllMixedCollection(nativePtr, collection.getNativePtr());
    }

    private static native long nativeGetFinalizerPtr();

    private static native long nativeCreate(long sharedRealmPtr, long nativeRowPtr, long columnKey);

    private static native boolean nativeIsValid(long nativePtr);

    private static native Object nativeGetValueAtIndex(long nativePtr, int position);

    private static native long nativeSize(long nativePtr);

    private static native boolean nativeContainsNull(long nativePtr);

    private static native boolean nativeContainsString(long nativePtr, String value);

    private static native boolean nativeContainsLong(long nativePtr, long value);

    private static native long[] nativeAddNull(long nativePtr);

    private static native long[] nativeAddString(long nativePtr, String value);

    private static native long[] nativeAddLong(long nativePtr, long value);

    private static native long[] nativeRemoveNull(long nativePtr);

    private static native long[] nativeRemoveString(long nativePtr, String value);

    private static native long[] nativeRemoveLong(long nativePtr, long value);

    private static native boolean nativeContainsAllMixedCollection(long nativePtr, long mixedCollectionPtr);

    private static native boolean nativeContainsAll(long nativePtr, long otherRealmSetNativePtr);

    private static native boolean nativeUnion(long nativePtr, long otherRealmSetNativePtr);

    private static native boolean nativeAddAllMixedCollection(long nativePtr, long mixedCollectionPtr);

    private static native boolean nativeAsymmetricDifference(long nativePtr, long otherRealmSetNativePtr);

    private static native boolean nativeRemoveAllMixedCollection(long nativePtr, long mixedCollectionPtr);

    private static native boolean nativeIntersect(long nativePtr, long otherRealmSetNativePtr);

    private static native boolean nativeRetainAllMixedCollection(long nativePtr, long mixedCollectionPtr);

    private static native void nativeClear(long nativePtr);

    private static native long nativeFreeze(long nativePtr, long frozenRealmPtr);
}
