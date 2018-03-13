/*
 * Copyright 2017 Realm Inc.
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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmChangeListener;
import io.realm.internal.sync.OsSubscription;


/**
 * Java wrapper of Object Store Results class.
 * It is the backend of binding's query results and back links.
 */
public class OsResults implements NativeObject, ObservableCollection {

    private static final String CLOSED_REALM_MESSAGE =
            "This Realm instance has already been closed, making it unusable.";

    // Custom OsResults iterator. It ensures that we only iterate on a Realm OsResults that hasn't changed.
    public static abstract class Iterator<T> implements java.util.Iterator<T> {
        OsResults iteratorOsResults;
        protected int pos = -1;

        public Iterator(OsResults osResults) {
            if (osResults.sharedRealm.isClosed()) {
                throw new IllegalStateException(CLOSED_REALM_MESSAGE);
            }

            this.iteratorOsResults = osResults;

            if (osResults.isSnapshot) {
                // No need to detach a snapshot.
                return;
            }

            if (osResults.sharedRealm.isInTransaction()) {
                detach();
            } else {
                iteratorOsResults.sharedRealm.addIterator(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            checkValid();
            return pos + 1 < iteratorOsResults.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nullable
        public T next() {
            checkValid();
            pos++;
            if (pos >= iteratorOsResults.size()) {
                throw new NoSuchElementException("Cannot access index " + pos + " when size is " + iteratorOsResults.size() +
                        ". Remember to check hasNext() before using next().");
            }
            return get(pos);
        }

        /**
         * Not supported by Realm collection iterators.
         */
        @Override
        @Deprecated
        public void remove() {
            throw new UnsupportedOperationException("remove() is not supported by RealmResults iterators.");
        }

        void detach() {
            iteratorOsResults = iteratorOsResults.createSnapshot();
        }

        // The iterator becomes invalid after receiving a remote change notification. In Java, the destruction of
        // iterator totally depends on GC. If we just detach those iterators when remote change notification received
        // like what realm-cocoa does, we will have a massive overhead since all the iterators created in the previous
        // event loop need to be detached.
        void invalidate() {
            iteratorOsResults = null;
        }

        void checkValid() {
            if (iteratorOsResults == null) {
                throw new ConcurrentModificationException(
                        "No outside changes to a Realm is allowed while iterating a living Realm collection.");
            }
        }

        @Nullable
        T get(int pos) {
            return convertRowToObject(iteratorOsResults.getUncheckedRow(pos));
        }

        // Returns the RealmModel by given row in this list. This has to be implemented in the upper layer since
        // we don't have information about the object types in the internal package.
        protected abstract T convertRowToObject(UncheckedRow row);
    }

    // Custom Realm collection list iterator.
    public static abstract class ListIterator<T> extends Iterator<T> implements java.util.ListIterator<T> {

        public ListIterator(OsResults osResults, int start) {
            super(osResults);
            if (start >= 0 && start <= iteratorOsResults.size()) {
                pos = start - 1;
            } else {
                throw new IndexOutOfBoundsException("Starting location must be a valid index: [0, "
                        + (iteratorOsResults.size() - 1) + "]. Yours was " + start);
            }
        }

        /**
         * Unsupported by Realm collection iterators.
         */
        @Override
        @Deprecated
        public void add(@Nullable T object) {
            throw new UnsupportedOperationException("Adding an element is not supported. Use Realm.createObject() instead.");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasPrevious() {
            checkValid();
            return pos >= 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int nextIndex() {
            checkValid();
            return pos + 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nullable
        public T previous() {
            checkValid();
            try {
                T obj = get(pos);
                pos--;
                return obj;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException("Cannot access index less than zero. This was " + pos +
                        ". Remember to check hasPrevious() before using previous().");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int previousIndex() {
            checkValid();
            return pos;
        }

        /**
         * Unsupported by RealmResults iterators.
         */
        @Override
        @Deprecated
        public void set(@Nullable T object) {
            throw new UnsupportedOperationException("Replacing an element is not supported.");
        }
    }

    private final long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();
    private final OsSharedRealm sharedRealm;
    private final NativeContext context;
    private final Table table;
    protected boolean loaded;
    private boolean isSnapshot = false;

    protected final ObserverPairList<CollectionObserverPair> observerPairs =
            new ObserverPairList<CollectionObserverPair>();

    // Public for static checking in JNI
    @SuppressWarnings("WeakerAccess")
    public static final byte AGGREGATE_FUNCTION_MINIMUM = 1;
    @SuppressWarnings("WeakerAccess")
    public static final byte AGGREGATE_FUNCTION_MAXIMUM = 2;
    @SuppressWarnings("WeakerAccess")
    public static final byte AGGREGATE_FUNCTION_AVERAGE = 3;
    @SuppressWarnings("WeakerAccess")
    public static final byte AGGREGATE_FUNCTION_SUM = 4;

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

    @SuppressWarnings("WeakerAccess")
    public static final byte MODE_EMPTY = 0;
    @SuppressWarnings("WeakerAccess")
    public static final byte MODE_TABLE = 1;
    @SuppressWarnings("WeakerAccess")
    public static final byte MODE_QUERY = 2;
    @SuppressWarnings("WeakerAccess")
    public static final byte MODE_LINKVIEW = 3;
    @SuppressWarnings("WeakerAccess")
    public static final byte MODE_TABLEVIEW = 4;

    public enum Mode {
        EMPTY,          // Backed by nothing (for missing tables)
        TABLE,          // Backed directly by a Table
        QUERY,          // Backed by a query that has not yet been turned into a TableView
        LINKVIEW,       // Backed directly by a LinkView
        TABLEVIEW;      // Backed by a TableView created from a Query

        static Mode getByValue(byte value) {
            switch (value) {
                case MODE_EMPTY:
                    return EMPTY;
                case MODE_TABLE:
                    return TABLE;
                case MODE_QUERY:
                    return QUERY;
                case MODE_LINKVIEW:
                    return LINKVIEW;
                case MODE_TABLEVIEW:
                    return TABLEVIEW;
                default:
                    throw new IllegalArgumentException("Invalid value: " + value);
            }
        }
    }

    public static OsResults createForBacklinks(OsSharedRealm realm, UncheckedRow row, Table srcTable,
                                               String srcFieldName) {
        long backlinksPtr = nativeCreateResultsFromBacklinks(
                realm.getNativePtr(),
                row.getNativePtr(),
                srcTable.getNativePtr(),
                srcTable.getColumnIndex(srcFieldName));
        return new OsResults(realm, srcTable, backlinksPtr);
    }

    public static OsResults createFromQuery(OsSharedRealm sharedRealm, TableQuery query,
                                            @Nullable SortDescriptor sortDescriptor,
                                            @Nullable SortDescriptor distinctDescriptor) {
        query.validateQuery();
        long ptr = nativeCreateResults(sharedRealm.getNativePtr(), query.getNativePtr(),
                sortDescriptor,
                distinctDescriptor);
        return new OsResults(sharedRealm, query.getTable(), ptr);
    }

    public static OsResults createFromQuery(OsSharedRealm sharedRealm, TableQuery query) {
        return createFromQuery(sharedRealm, query, null, null);
    }

    OsResults(OsSharedRealm sharedRealm, Table table, long nativePtr) {
        this.sharedRealm = sharedRealm;
        this.context = sharedRealm.context;
        this.table = table;
        this.nativePtr = nativePtr;
        this.context.addReference(this);
        this.loaded = getMode() != Mode.QUERY;
    }

    public OsResults createSnapshot() {
        if (isSnapshot) {
            return this;
        }
        OsResults osResults = new OsResults(sharedRealm, table, nativeCreateSnapshot(nativePtr));
        osResults.isSnapshot = true;
        return osResults;
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
        return table.getUncheckedRowByPointer(nativeGetRow(nativePtr, index));
    }

    public UncheckedRow firstUncheckedRow() {
        long rowPtr = nativeFirstRow(nativePtr);
        if (rowPtr != 0) {
            return table.getUncheckedRowByPointer(rowPtr);
        }
        return null;
    }

    public UncheckedRow lastUncheckedRow() {
        long rowPtr = nativeLastRow(nativePtr);
        if (rowPtr != 0) {
            return table.getUncheckedRowByPointer(rowPtr);
        }
        return null;
    }

    public Table getTable() {
        return table;
    }

    public TableQuery where() {
        long nativeQueryPtr = nativeWhere(nativePtr);
        return new TableQuery(this.context, this.table, nativeQueryPtr);
    }

    public Number aggregateNumber(Aggregate aggregateMethod, long columnIndex) {
        return (Number) nativeAggregate(nativePtr, columnIndex, aggregateMethod.getValue());
    }

    public Date aggregateDate(Aggregate aggregateMethod, long columnIndex) {
        return (Date) nativeAggregate(nativePtr, columnIndex, aggregateMethod.getValue());
    }

    public long size() {
        return nativeSize(nativePtr);
    }

    public void clear() {
        nativeClear(nativePtr);
    }

    public OsResults sort(SortDescriptor sortDescriptor) {
        return new OsResults(sharedRealm, table, nativeSort(nativePtr, sortDescriptor));
    }

    public OsResults distinct(SortDescriptor distinctDescriptor) {
        return new OsResults(sharedRealm, table, nativeDistinct(nativePtr, distinctDescriptor));
    }

    public boolean contains(UncheckedRow row) {
        return nativeContains(nativePtr, row.getNativePtr());
    }

    public int indexOf(UncheckedRow row) {
        long index = nativeIndexOf(nativePtr, row.getNativePtr());
        return (index > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) index;
    }

    public void delete(long index) {
        nativeDelete(nativePtr, index);
    }

    public boolean deleteFirst() {
        return nativeDeleteFirst(nativePtr);
    }

    public boolean deleteLast() {
        return nativeDeleteLast(nativePtr);
    }

    public <T> void addListener(T observer, OrderedRealmCollectionChangeListener<T> listener) {
        if (observerPairs.isEmpty()) {
            nativeStartListening(nativePtr);
        }
        CollectionObserverPair<T> collectionObserverPair = new CollectionObserverPair<T>(observer, listener);
        observerPairs.add(collectionObserverPair);
    }

    public <T> void addListener(T observer, RealmChangeListener<T> listener) {
        addListener(observer, new RealmChangeListenerWrapper<T>(listener));
    }

    public <T> void removeListener(T observer, OrderedRealmCollectionChangeListener<T> listener) {
        observerPairs.remove(observer, listener);
        if (observerPairs.isEmpty()) {
            nativeStopListening(nativePtr);
        }
    }

    public <T> void removeListener(T observer, RealmChangeListener<T> listener) {
        removeListener(observer, new RealmChangeListenerWrapper<T>(listener));
    }

    public void removeAllListeners() {
        observerPairs.clear();
        nativeStopListening(nativePtr);
    }

    public boolean isValid() {
        return nativeIsValid(nativePtr);
    }

    // Called by JNI
    @Override
    public void notifyChangeListeners(long nativeChangeSetPtr) {
        // Object Store compute the change set between the SharedGroup versions when the query created and the latest.
        // So it is possible it deliver a non-empty change set for the first async query returns.
        OsCollectionChangeSet changeset = (nativeChangeSetPtr == 0)
                ? new EmptyLoadChangeSet(null, sharedRealm.isPartial())
                : new OsCollectionChangeSet(nativeChangeSetPtr, !isLoaded(), null, sharedRealm.isPartial());

        // Happens e.g. if a synchronous query is created, a change listener is added and then
        // a transaction is started on the same thread. This will trigger all notifications
        // and deliver an empty changeset.
        if (changeset.isEmpty() && isLoaded()) {
            return;
        }
        loaded = true;
        observerPairs.foreach(new Callback(changeset));
    }

    public Mode getMode() {
        return Mode.getByValue(nativeGetMode(nativePtr));
    }

    // The Results with mode QUERY will be evaluated asynchronously in Object Store. But we do have to support "sync"
    // query by Java like RealmQuery.findAll().
    // The flag is used for following cases:
    // 1. When Results is created, loaded will be set to false if the mode is QUERY. For other modes, loaded will be set
    //    to true.
    // 2. For sync query (RealmQuery.findAll()), load() should be called after the Results creation. Then query will be
    //    evaluated immediately and then loaded will be set to true (And the mode will be changed to TABLEVIEW in OS).
    // 3. For async query, when load() gets called with loaded not set, the listener should be triggered with empty
    //    change set since it is considered as query first returned.
    // 4. If the listener triggered with empty change set after load() called for async queries, it is treated as the
    //    same case as 2).
    public boolean isLoaded() {
        return loaded;
    }

    public void load() {
        if (loaded) {
            return;
        }
        nativeEvaluateQueryIfNeeded(nativePtr, false);
        notifyChangeListeners(0);
    }

    private static native long nativeGetFinalizerPtr();

    protected static native long nativeCreateResults(long sharedRealmNativePtr, long queryNativePtr,
                                                     @Nullable SortDescriptor sortDesc, @Nullable SortDescriptor distinctDesc);

    private static native long nativeCreateSnapshot(long nativePtr);

    private static native long nativeGetRow(long nativePtr, int index);

    private static native long nativeFirstRow(long nativePtr);

    private static native long nativeLastRow(long nativePtr);

    private static native boolean nativeContains(long nativePtr, long nativeRowPtr);

    private static native void nativeClear(long nativePtr);

    private static native long nativeSize(long nativePtr);

    private static native Object nativeAggregate(long nativePtr, long columnIndex, byte aggregateFunc);

    private static native long nativeSort(long nativePtr, SortDescriptor sortDesc);

    private static native long nativeDistinct(long nativePtr, SortDescriptor distinctDesc);

    private static native boolean nativeDeleteFirst(long nativePtr);

    private static native boolean nativeDeleteLast(long nativePtr);

    private static native void nativeDelete(long nativePtr, long index);

    // Non-static, we need this OsResults object in JNI.
    private native void nativeStartListening(long nativePtr);

    private native void nativeStopListening(long nativePtr);

    private static native long nativeWhere(long nativePtr);

    private static native long nativeIndexOf(long nativePtr, long rowNativePtr);

    private static native boolean nativeIsValid(long nativePtr);

    private static native byte nativeGetMode(long nativePtr);

    private static native long nativeCreateResultsFromBacklinks(long sharedRealmNativePtr, long rowNativePtr, long srcTableNativePtr, long srColIndex);

    private static native void nativeEvaluateQueryIfNeeded(long nativePtr, boolean wantsNotifications);

}
