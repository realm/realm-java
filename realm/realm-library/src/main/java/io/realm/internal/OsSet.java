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

import java.util.Collection;

import javax.annotation.Nullable;

public class OsSet implements NativeObject {

    public enum ExternalCollectionOperation {
        CONTAINS_ALL,
        ADD_ALL,
        REMOVE_ALL,
        RETAIN_ALL
    }

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

    // ----------------------------------------------------
    // String operations
    // ----------------------------------------------------

    public boolean contains(@Nullable String value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {
            return nativeContainsString(nativePtr, (String) value);
        }
    }

    public boolean containsAllString(Collection<?> collection) {
        // Every set contains the empty set, in this case an empty collection
        if (collection.size() == 0) {
            return true;
        }

        // It cannot be contained if it is empty or not the same type
        Object[] objects = collection.toArray();
        if (objects[0].getClass() != String.class) {
            return false;
        }

        String[] values = collection.toArray(new String[objects.length]);
        return nativeContainsAllString(nativePtr, values);
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

    public <E> boolean addAllString(Collection<? extends E> collection) {
        // Nothing changes if collection is empty
        if (collection.isEmpty()) {
            return false;
        }

        int size = collection.size();
        String[] values = collection.toArray(new String[size]);
        return collectionFunnelString(values, ExternalCollectionOperation.ADD_ALL);
    }

    public boolean remove(@Nullable String value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveString(nativePtr, (String) value);
        }
        return indexAndFound[1] == 1;       // 1 means true, i.e. it was found
    }

    public <E> boolean removeAllString(Collection<? extends E> collection) {
        // Nothing changes if collection is empty
        if (collection.isEmpty()) {
            return false;
        }

        int size = collection.size();
        String[] values = collection.toArray(new String[size]);
        return collectionFunnelString(values, ExternalCollectionOperation.REMOVE_ALL);
    }

    public <E> boolean retainAllString(Collection<? extends E> collection) {
        // Intersection with an empty collection results into an empty set
        if (collection.isEmpty()) {
            nativeClear(nativePtr);
            return true;
        }

        int size = collection.size();
        String[] values = collection.toArray(new String[size]);
        return collectionFunnelString(values, ExternalCollectionOperation.RETAIN_ALL);
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

    private void checkCollectionType(Object[] values, Class<?> valueClass) {
        // Throw if collection and set are not the same type
        Class<?> itemClass = values[0].getClass();
        if (itemClass != valueClass) {
            throw new IllegalArgumentException("Invalid collection type. Set and collection must contain the same type of elements.");
        }
    }

    private boolean collectionFunnelString(String[] values, ExternalCollectionOperation operation) {
        checkCollectionType(values, String.class);
        switch (operation) {
            case ADD_ALL:
                return nativeAddAllString(nativePtr, values);
            case REMOVE_ALL:
                return nativeRemoveAllString(nativePtr, values);
            case RETAIN_ALL:
                return nativeRetainAllString(nativePtr, values);
            default:
                throw new IllegalStateException("Unexpected value: " + operation);
        }
    }

    private static native long nativeGetFinalizerPtr();

    private static native long nativeCreate(long sharedRealmPtr, long nativeRowPtr, long columnKey);

    private static native boolean nativeIsValid(long nativePtr);

    private static native Object nativeGetValueAtIndex(long nativePtr, int position);

    private static native long nativeSize(long nativePtr);

    private static native boolean nativeContainsNull(long nativePtr);

    private static native boolean nativeContainsString(long nativePtr, String value);

    private static native long[] nativeAddNull(long nativePtr);

    private static native long[] nativeAddString(long nativePtr, String value);

    private static native long[] nativeRemoveNull(long nativePtr);

    private static native long[] nativeRemoveString(long nativePtr, String value);

    private static native boolean nativeContainsAllString(long nativePtr, String[] otherSet);

    private static native boolean nativeContainsAll(long nativePtr, long otherRealmSetNativePtr);

    private static native boolean nativeUnion(long nativePtr, long otherRealmSetNativePtr);

    private static native boolean nativeAddAllString(long nativePtr, String[] otherSet);

    private static native boolean nativeAsymmetricDifference(long nativePtr, long otherRealmSetNativePtr);

    private static native boolean nativeRemoveAllString(long nativePtr, String[] otherSet);

    private static native boolean nativeIntersect(long nativePtr, long otherRealmSetNativePtr);

    private static native boolean nativeRetainAllString(long nativePtr, String[] otherSet);

    private static native void nativeClear(long nativePtr);

    private static native long nativeFreeze(long nativePtr, long frozenRealmPtr);
}
