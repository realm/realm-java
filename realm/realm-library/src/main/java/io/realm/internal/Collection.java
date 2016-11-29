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

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.RealmChangeListener;

public class Collection implements NativeObject {

    public static class Listener {
        private final RealmChangeListener realmChangeListener;
        private final WeakReference<Object> objectRef;

        public Listener(RealmChangeListener realmChangeListener, Object objectRef) {
            this.realmChangeListener = realmChangeListener;
            this.objectRef = new WeakReference<Object>(objectRef);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof Listener) {
                Listener anotherListener = (Listener) obj;
                return realmChangeListener.equals(anotherListener.realmChangeListener) &&
                        objectRef.equals(anotherListener.objectRef);
            }
            return false;
        }
    }

    private static class NotificationToken implements NativeObject {
        private long nativePtr;
        private static final long nativeFinalizerPtr = nativeNotificationTokenGetFinalizerPtr();

        NotificationToken(long nativePtr) {
            this.nativePtr = nativePtr;
            Context.sharedContext.addReference(this);
        }

        @Override
        public long getNativePtr() {
            return nativePtr;
        }

        @Override
        public long getNativeFinalizerPtr() {
            return nativeFinalizerPtr;
        }

        public void close() {
            nativeNotificationTokenClose(nativePtr);
            nativePtr = 0;
        }
    }

    private final long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();
    private final SharedRealm sharedRealm;
    private final Context context;
    private final TableQuery query;
    private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private NotificationToken notificationToken = null;

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
        this.sharedRealm = sharedRealm;
        this.context = sharedRealm.context;
        this.query = query;

        this.nativePtr = nativeCreateResults(sharedRealm.getNativePtr(), query.getNativePtr(),
                sortDescriptor == null ? 0 : sortDescriptor.getNativePtr(),
                distinctDescriptor == null ? 0 : distinctDescriptor.getNativePtr());
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

    public int size() {
        long size = nativeSize(nativePtr);
        return (size > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) size;
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

    public void addListener(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        if (notificationToken == null) {
            notificationToken = new NotificationToken(nativeAddListener(nativePtr));
        }
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty() && notificationToken != null) {
            notificationToken.close();
            notificationToken = null;
        }
    }

    public void removeAllListeners() {
        listeners.clear();
        if (notificationToken != null) {
            notificationToken.close();
            notificationToken = null;
        }
    }

    // Called by JNI
    @SuppressWarnings("unused")
    private void notifyChangeListeners() {
        if (!listeners.isEmpty()) {
            for (Listener listener : listeners) {
                Object obj = listener.objectRef.get();
                if (obj == null) {
                    listeners.remove(listener);
                    continue;
                }
                //noinspection unchecked
                listener.realmChangeListener.onChange(obj);
            }
        }
    }

    private static native long nativeGetFinalizerPtr();
    private static native long nativeCreateResults(long sharedRealmNativePtr, long queryNativePtr,
                                                   long sortDescNativePtr, long distinctDescNativePtr);
    private static native long nativeCreateSnapshot(long nativePtr);
    private static native long nativeGetRow(long nativePtr, int index);
    private static native boolean nativeContains(long nativePtr, long nativeRowPtr);
    private static native void nativeClear(long nativePtr);
    private static native long nativeSize(long nativePtr);
    private static native Object nativeAggregate(long nativePtr, long columnIndex, byte aggregateFunc);
    private static native long nativeSort(long nativePtr, long sortDescNativePtr);
    private native long nativeAddListener(long nativePtr);
    private static native long nativeNotificationTokenGetFinalizerPtr();
    private static native long nativeNotificationTokenClose(long nativePtr);
    private static native long nativeWhere(long nativePtr);
}
