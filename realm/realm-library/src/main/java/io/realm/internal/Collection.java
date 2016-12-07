/*
 * Copyright 2014 Realm Inc.
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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.RealmChangeListener;

/**
 * Java wrapper of OS Results class.
 * It is supposed to be the backend of binding's query results, link list and back links.
 */
@KeepMember
public final class Collection implements NativeObject {

    private static class CollectionObserverPair<T> extends ObserverPair<T, RealmChangeListener<T>>{
        public CollectionObserverPair(T observer, RealmChangeListener<T> listener) {
            super(observer, listener);
        }

        public void onChange() {
            T observer = observerRef.get();
            if (observer != null) {

                listener.onChange(observerRef.get());
            }
        }
    }

    private final long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();
    private final SharedRealm sharedRealm;
    private final Context context;
    private final TableQuery query;
    private final List<CollectionObserverPair> observerPairs = new CopyOnWriteArrayList<CollectionObserverPair>();

    // Public for static checking in JNI
    @SuppressWarnings("WeakerAccess")
    public static final byte AGGREGATE_FUNCTION_MINIMUM = 1;
    @SuppressWarnings("WeakerAccess")
    public static final byte AGGREGATE_FUNCTION_MAXIMUM = 2;
    @SuppressWarnings("WeakerAccess")
    public static final byte AGGREGATE_FUNCTION_AVERAGE = 3;
    @SuppressWarnings("WeakerAccess")
    public static final byte AGGREGATE_FUNCTION_SUM     = 4;

    public enum Aggregate {
        MINIMUM(AGGREGATE_FUNCTION_MINIMUM),
        MAXIMUM(AGGREGATE_FUNCTION_MAXIMUM),
        AVERAGE(AGGREGATE_FUNCTION_AVERAGE),
        SUM(AGGREGATE_FUNCTION_SUM);

        private final byte value;

        Aggregate(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    public Collection(SharedRealm sharedRealm, TableQuery query,
                      SortDescriptor sortDescriptor, SortDescriptor distinctDescriptor) {
        query.validateQuery();

        this.nativePtr = nativeCreateResults(sharedRealm.getNativePtr(), query.getNativePtr(),
                sortDescriptor == null ? 0 : sortDescriptor.getNativePtr(),
                distinctDescriptor == null ? 0 : distinctDescriptor.getNativePtr());

        this.sharedRealm = sharedRealm;
        this.context = sharedRealm.context;
        this.query = query;
        this.context.addReference(this);
    }

    public Collection(SharedRealm sharedRealm, TableQuery query,
                      SortDescriptor sortDescriptor) {
        this(sharedRealm, query, sortDescriptor, null);
    }

    public Collection(SharedRealm sharedRealm, TableQuery query) {
        this(sharedRealm, query, null, null);
    }

    private Collection(SharedRealm sharedRealm, TableQuery query, long nativePtr) {
        query.validateQuery();
        this.sharedRealm = sharedRealm;
        this.context = sharedRealm.context;
        this.query = query;
        this.nativePtr = nativePtr;

        this.context.addReference(this);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public UncheckedRow getUncheckedRow(int index) {
        return UncheckedRow.getByRowPointer(query.table, nativeGetRow(nativePtr, index));
    }

    public UncheckedRow firstUncheckedRow() {
        return UncheckedRow.getByRowPointer(query.table, nativeFirstRow(nativePtr));
    }

    public UncheckedRow lastUncheckedRow() {
        return UncheckedRow.getByRowPointer(query.table, nativeLastRow(nativePtr));
    }

    public Table getTable() {
        return query.getTable();
    }

    public TableQuery where() {
        long nativeQueryPtr = nativeWhere(nativePtr);
        return new TableQuery(this.context, this.getTable(), nativeQueryPtr);
    }

    public Object aggregate(Aggregate aggregateMethod, long columnIndex) {
        return nativeAggregate(nativePtr, columnIndex, aggregateMethod.getValue());
    }

    public long size() {
        return nativeSize(nativePtr);
    }

    public void clear() {
        nativeClear(nativePtr);
    }

    public Collection sort(SortDescriptor sortDescriptor) {
        return new Collection(sharedRealm, query, nativeSort(nativePtr, sortDescriptor.getNativePtr()));
    }

    public boolean contains(UncheckedRow row) {
        return nativeContains(nativePtr, row.getNativePtr());
    }

    public int indexOf(UncheckedRow row) {
        long index = nativeIndexOf(nativePtr, row.getNativePtr());
        return (index > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) index;
    }

    public int indexOf(long sourceRowIndex) {
        long index = nativeIndexOfBySourceRowIndex(nativePtr, sourceRowIndex);
        return (index > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) index;
    }

    public <T> void addListener(T observer, RealmChangeListener<T> listener) {
        if (observerPairs.isEmpty()) {
            nativeStartListening(nativePtr);
        }
        CollectionObserverPair<T> collectionObserverPair = new CollectionObserverPair<T>(observer, listener);
        if (!observerPairs.contains(collectionObserverPair)) {
            observerPairs.add(collectionObserverPair);
        }
    }

    public <T> void removeListener(T observer, RealmChangeListener<T> listener) {
        CollectionObserverPair<T> collectionObserverPair = new CollectionObserverPair<T>(observer, listener);
        observerPairs.remove(collectionObserverPair);
        if (observerPairs.isEmpty()) {
            nativeStopListening(nativePtr);
        }
    }

    public void removeAllListeners() {
        observerPairs.clear();
        nativeStopListening(nativePtr);
    }

    // Called by JNI
    @KeepMember
    @SuppressWarnings("unused")
    private void notifyChangeListeners() {
        for (CollectionObserverPair pair: observerPairs) {
            Object object = pair.observerRef.get();
            if (object != null) {
                pair.onChange();
            } else {
                observerPairs.remove(pair);
            }
        }
    }

    private static native long nativeGetFinalizerPtr();
    private static native long nativeCreateResults(long sharedRealmNativePtr, long queryNativePtr,
                                                   long sortDescNativePtr, long distinctDescNativePtr);
    @SuppressWarnings("unused") // Not used for now
    private static native long nativeCreateSnapshot(long nativePtr);
    private static native long nativeGetRow(long nativePtr, int index);
    private static native long nativeFirstRow(long nativePtr);
    private static native long nativeLastRow(long nativePtr);
    private static native boolean nativeContains(long nativePtr, long nativeRowPtr);
    private static native void nativeClear(long nativePtr);
    private static native long nativeSize(long nativePtr);
    private static native Object nativeAggregate(long nativePtr, long columnIndex, byte aggregateFunc);
    private static native long nativeSort(long nativePtr, long sortDescNativePtr);
    // Non-static, we need this Collection object in JNI.
    private native void nativeStartListening(long nativePtr);
    private native void nativeStopListening(long nativePtr);
    private static native long nativeWhere(long nativePtr);
    private static native long nativeIndexOf(long nativePtr, long rowNativePtr);
    private static native long nativeIndexOfBySourceRowIndex(long nativePtr, long sourceRowIndex);
}
