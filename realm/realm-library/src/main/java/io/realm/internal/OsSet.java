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

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.SetChangeSet;
import io.realm.internal.core.NativeMixedCollection;

public class OsSet implements NativeObject {

    public enum ExternalCollectionOperation {
        CONTAINS_ALL,
        ADD_ALL,
        REMOVE_ALL,
        RETAIN_ALL
    }

    public static final int NOT_FOUND = -1;             // This means something was not found in OS

    private static final int VALUE_NOT_FOUND = 0;       // comes from a native boolean
    private static final int VALUE_FOUND = 1;           // comes from a native boolean

    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private final long nativePtr;
    private final NativeContext context;
    private final OsSharedRealm osSharedRealm;
    private final Table targetTable;                // TODO: not sure this is needed

    public OsSet(UncheckedRow row, long columnKey) {
        this.osSharedRealm = row.getTable().getSharedRealm();
        long[] pointers = nativeCreate(osSharedRealm.getNativePtr(), row.getNativePtr(), columnKey);
        this.nativePtr = pointers[0];
        if (pointers[1] != NOT_FOUND) {
            this.targetTable = new Table(osSharedRealm, pointers[1]);
        } else {
            this.targetTable = null;
        }
        this.context = osSharedRealm.context;
        context.addReference(this);
    }

    // Used to freeze sets
    private OsSet(OsSharedRealm osSharedRealm, long nativePtr, Table targetTable) {
        this.osSharedRealm = osSharedRealm;
        this.nativePtr = nativePtr;
        this.targetTable = targetTable;
        this.context = osSharedRealm.context;
        context.addReference(this);
    }

//    public OsSet(UncheckedRow row, long columnKey) {
//        this.osSharedRealm = row.getTable().getSharedRealm();
//        this.nativePtr = nativeCreate(osSharedRealm.getNativePtr(), row.getNativePtr(), columnKey);
//        this.context = osSharedRealm.context;
//        context.addReference(this);
//    }
//
//    // Used to freeze sets
//    private OsSet(OsSharedRealm osSharedRealm, long nativePtr) {
//        this.osSharedRealm = osSharedRealm;
//        this.nativePtr = nativePtr;
//        this.context = osSharedRealm.context;
//        context.addReference(this);
//    }

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
    // Boolean operations
    // ----------------------------------------------------

    public boolean contains(@Nullable Boolean value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {
            return nativeContainsBoolean(nativePtr, value);
        }
    }

    public boolean add(@Nullable Boolean value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddBoolean(nativePtr, value);
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable Boolean value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveBoolean(nativePtr, value);
        }
        return indexAndFound[1] == VALUE_FOUND;
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

    public boolean contains(@Nullable Long value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {
            return nativeContainsLong(nativePtr, value);
        }
    }

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
    // Float operations
    // ----------------------------------------------------

    public boolean contains(@Nullable Float value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {
            return nativeContainsFloat(nativePtr, value);
        }
    }

    public boolean add(@Nullable Float value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddFloat(nativePtr, value);
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable Float value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveFloat(nativePtr, value);
        }
        return indexAndFound[1] == VALUE_FOUND;
    }

    // ----------------------------------------------------
    // Double operations
    // ----------------------------------------------------

    public boolean contains(@Nullable Double value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {
            return nativeContainsDouble(nativePtr, value);
        }
    }

    public boolean add(@Nullable Double value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddDouble(nativePtr, value);
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable Double value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveDouble(nativePtr, value);
        }
        return indexAndFound[1] == VALUE_FOUND;
    }

    // ----------------------------------------------------
    // Binary operations
    // ----------------------------------------------------

    public boolean contains(@Nullable byte[] value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {
            return nativeContainsBinary(nativePtr, value);
        }
    }

    public boolean add(@Nullable byte[] value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddBinary(nativePtr, value);
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable byte[] value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveBinary(nativePtr, value);
        }
        return indexAndFound[1] == VALUE_FOUND;
    }

    // ----------------------------------------------------
    // Date operations
    // ----------------------------------------------------

    public boolean contains(@Nullable Date value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {
            return nativeContainsDate(nativePtr, value.getTime());
        }
    }

    public boolean add(@Nullable Date value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddDate(nativePtr, value.getTime());
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable Date value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveDate(nativePtr, value.getTime());
        }
        return indexAndFound[1] == VALUE_FOUND;
    }

    // ----------------------------------------------------
    // Decimal128 operations
    // ----------------------------------------------------

    public boolean contains(@Nullable Decimal128 value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {
            return nativeContainsDecimal128(nativePtr, value.getLow(), value.getHigh());
        }
    }

    public boolean add(@Nullable Decimal128 value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddDecimal128(nativePtr, value.getLow(), value.getHigh());
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable Decimal128 value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveDecimal128(nativePtr, value.getLow(), value.getHigh());
        }
        return indexAndFound[1] == VALUE_FOUND;
    }

    // ----------------------------------------------------
    // ObjectId operations
    // ----------------------------------------------------

    public boolean contains(@Nullable ObjectId value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {
            return nativeContainsObjectId(nativePtr, value.toString());
        }
    }

    public boolean add(@Nullable ObjectId value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddObjectId(nativePtr, value.toString());
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable ObjectId value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveObjectId(nativePtr, value.toString());
        }
        return indexAndFound[1] == VALUE_FOUND;
    }

    // ----------------------------------------------------
    // UUID operations
    // ----------------------------------------------------

    public boolean contains(@Nullable UUID value) {
        if (value == null) {
            return nativeContainsNull(nativePtr);
        } else {
            return nativeContainsUUID(nativePtr, value.toString());
        }
    }

    public boolean add(@Nullable UUID value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeAddNull(nativePtr);
        } else {
            indexAndFound = nativeAddUUID(nativePtr, value.toString());
        }
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean remove(@Nullable UUID value) {
        long[] indexAndFound;
        if (value == null) {
            indexAndFound = nativeRemoveNull(nativePtr);
        } else {
            indexAndFound = nativeRemoveUUID(nativePtr, value.toString());
        }
        return indexAndFound[1] == VALUE_FOUND;
    }

    // ----------------------------------------------------
    // Realm model operations
    // ----------------------------------------------------

    public boolean containsRow(long rowPtr) {
        return nativeContainsRow(nativePtr, rowPtr);
    }

    public boolean addRow(long rowPtr) {
        long[] indexAndFound = nativeAddRow(nativePtr, rowPtr);
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean removeRow(long rowPtr) {
        long[] indexAndFound = nativeRemoveRow(nativePtr, rowPtr);
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public long getRow(int index) {
        return nativeGetRow(nativePtr, index);
    }

    // ----------------------------------------------------
    // Mixed operations
    // ----------------------------------------------------

    public boolean containsMixed(long mixedPtr) {
        return nativeContainsMixed(nativePtr, mixedPtr);
    }

    public boolean addMixed(long mixedPtr) {
        long[] indexAndFound = nativeAddMixed(nativePtr, mixedPtr);
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public boolean removeMixed(long mixedPtr) {
        long[] indexAndFound = nativeRemoveMixed(nativePtr, mixedPtr);
        return indexAndFound[1] != VALUE_NOT_FOUND;
    }

    public long getMixed(int index) {
        return nativeGetMixed(nativePtr, index);
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
        return new OsSet(frozenSharedRealm, frozenNativePtr, targetTable);
    }

    // ----------------------------------------------------
    // Change listeners
    // ----------------------------------------------------

    public void startListening(ObservableSet observableSet) {
        nativeStartListening(nativePtr, observableSet);
    }

    public void stopListening() {
        nativeStopListening(nativePtr);
    }

    public <T> void notifyChangeListeners(long nativeChangeSetPtr,
                                          ObserverPairList<ObservableSet.SetObserverPair<T>> setObserverPairs) {
        OsCollectionChangeSet collectionChangeSet = new OsCollectionChangeSet(nativeChangeSetPtr, false);
        SetChangeSet setChangeSet = new SetChangeSet(collectionChangeSet);
        if (setChangeSet.isEmpty()) {
            // First time "query" returns. Do nothing.
            return;
        }
        setObserverPairs.foreach(new ObservableSet.Callback<>(setChangeSet));
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

    private static native long[] nativeCreate(long sharedRealmPtr, long nativeRowPtr, long columnKey);

    private static native boolean nativeIsValid(long nativePtr);

    private static native Object nativeGetValueAtIndex(long nativePtr, int position);

    private static native long nativeSize(long nativePtr);

    private static native boolean nativeContainsNull(long nativePtr);

    private static native boolean nativeContainsBoolean(long nativePtr, boolean value);

    private static native boolean nativeContainsString(long nativePtr, String value);

    private static native boolean nativeContainsLong(long nativePtr, long value);

    private static native boolean nativeContainsFloat(long nativePtr, float value);

    private static native boolean nativeContainsDouble(long nativePtr, double value);

    private static native boolean nativeContainsBinary(long nativePtr, byte[] value);

    private static native boolean nativeContainsDate(long nativePtr, long value);

    private static native boolean nativeContainsDecimal128(long nativePtr, long lowValue, long highValue);

    private static native boolean nativeContainsObjectId(long nativePtr, String value);

    private static native boolean nativeContainsUUID(long nativePtr, String value);

    private static native boolean nativeContainsRow(long nativePtr, long rowPtr);

    private static native boolean nativeContainsMixed(long nativePtr, long mixedPtr);

    private static native long[] nativeAddNull(long nativePtr);

    private static native long[] nativeAddBoolean(long nativePtr, boolean value);

    private static native long[] nativeAddString(long nativePtr, String value);

    private static native long[] nativeAddLong(long nativePtr, long value);

    private static native long[] nativeAddFloat(long nativePtr, float value);

    private static native long[] nativeAddDouble(long nativePtr, double value);

    private static native long[] nativeAddBinary(long nativePtr, byte[] value);

    private static native long[] nativeAddDate(long nativePtr, long value);

    private static native long[] nativeAddDecimal128(long nativePtr, long lowValue, long highValue);

    private static native long[] nativeAddObjectId(long nativePtr, String value);

    private static native long[] nativeAddUUID(long nativePtr, String value);

    private static native long[] nativeAddRow(long nativePtr, long rowPtr);

    private static native long[] nativeAddMixed(long nativePtr, long mixed_ptr);

    private static native long[] nativeRemoveNull(long nativePtr);

    private static native long[] nativeRemoveBoolean(long nativePtr, boolean value);

    private static native long[] nativeRemoveString(long nativePtr, String value);

    private static native long[] nativeRemoveLong(long nativePtr, long value);

    private static native long[] nativeRemoveFloat(long nativePtr, float value);

    private static native long[] nativeRemoveDouble(long nativePtr, double value);

    private static native long[] nativeRemoveBinary(long nativePtr, byte[] value);

    private static native long[] nativeRemoveDate(long nativePtr, long value);

    private static native long[] nativeRemoveDecimal128(long nativePtr, long lowValue, long highValue);

    private static native long[] nativeRemoveObjectId(long nativePtr, String value);

    private static native long[] nativeRemoveUUID(long nativePtr, String value);

    private static native long[] nativeRemoveRow(long nativePtr, long rowPtr);

    private static native long[] nativeRemoveMixed(long nativePtr, long mixedPtr);

    private static native long nativeGetRow(long nativePtr, int index);

    private static native long nativeGetMixed(long nativePtr, int index);

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

    private static native void nativeStartListening(long nativePtr, ObservableSet observableSet);

    private static native void nativeStopListening(long nativePtr);
}
