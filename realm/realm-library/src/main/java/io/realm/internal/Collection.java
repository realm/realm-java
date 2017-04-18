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

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.NoSuchElementException;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmChangeListener;


/**
 * Java wrapper of Object Store Results class.
 * It is the backend of binding's query results, link lists and back links.
 */
@Keep
public class Collection implements NativeObject {

    private static final String CLOSED_REALM_MESSAGE =
            "This Realm instance has already been closed, making it unusable.";

    private static class CollectionObserverPair<T> extends ObserverPairList.ObserverPair<T, Object> {
        public CollectionObserverPair(T observer, Object listener) {
            super(observer, listener);
        }

        public void onChange(T observer, OrderedCollectionChangeSet changes) {
            if (listener instanceof OrderedRealmCollectionChangeListener) {
                //noinspection unchecked
                ((OrderedRealmCollectionChangeListener<T>) listener).onChange(observer, changes);
            } else if (listener instanceof RealmChangeListener) {
                //noinspection unchecked
                ((RealmChangeListener<T>) listener).onChange(observer);
            } else {
                throw new RuntimeException("Unsupported listener type: " + listener);
            }
        }
    }

    private static class RealmChangeListenerWrapper<T> implements OrderedRealmCollectionChangeListener<T> {
        private final RealmChangeListener<T> listener;

        RealmChangeListenerWrapper(RealmChangeListener<T> listener) {
            this.listener = listener;
        }

        @Override
        public void onChange(T collection, OrderedCollectionChangeSet changes) {
            listener.onChange(collection);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof RealmChangeListenerWrapper &&
                    listener == ((RealmChangeListenerWrapper) obj).listener;
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }

    private static class Callback implements ObserverPairList.Callback<CollectionObserverPair> {
        private final OrderedCollectionChangeSet changeSet;

        Callback(OrderedCollectionChangeSet changeSet) {
            this.changeSet = changeSet;
        }

        @Override
        public void onCalled(CollectionObserverPair pair, Object observer) {
            //noinspection unchecked
            pair.onChange(observer, changeSet);
        }
    }

    // Custom Collection iterator. It ensures that we only iterate on a Realm collection that hasn't changed.
    public static abstract class Iterator<T> implements java.util.Iterator<T> {
        Collection iteratorCollection;
        protected int pos = -1;

        public Iterator(Collection collection) {
            if (collection.sharedRealm.isClosed()) {
                throw new IllegalStateException(CLOSED_REALM_MESSAGE);
            }

            this.iteratorCollection = collection;

            if (collection.isSnapshot) {
                // No need to detach a snapshot.
                return;
            }

            if (collection.sharedRealm.isInTransaction()) {
                detach();
            } else {
                iteratorCollection.sharedRealm.addIterator(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            checkValid();
            return pos + 1 < iteratorCollection.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public T next() {
            checkValid();
            pos++;
            if (pos >= iteratorCollection.size()) {
                throw new NoSuchElementException("Cannot access index " + pos + " when size is " + iteratorCollection.size() +
                        ". Remember to check hasNext() before using next().");
            }
            return get(pos);
        }

        /**
         * Not supported by Realm collection iterators.
         *
         * @throws UnsupportedOperationException
         */
        @Override
        @Deprecated
        public void remove() {
            throw new UnsupportedOperationException("remove() is not supported by RealmResults iterators.");
        }

        void detach() {
            iteratorCollection = iteratorCollection.createSnapshot();
        }

        // The iterator becomes invalid after receiving a remote change notification. In Java, the destruction of
        // iterator totally depends on GC. If we just detach those iterators when remote change notification received
        // like what realm-cocoa does, we will have a massive overhead since all the iterators created in the previous
        // event loop need to be detached.
        void invalidate() {
            iteratorCollection = null;
        }

        void checkValid() {
            if (iteratorCollection == null) {
                throw new ConcurrentModificationException(
                        "No outside changes to a Realm is allowed while iterating a living Realm collection.");
            }
        }

        T get(int pos) {
            return convertRowToObject(iteratorCollection.getUncheckedRow(pos));
        }

        // Returns the RealmModel by given row in this list. This has to be implemented in the upper layer since
        // we don't have information about the object types in the internal package.
        protected abstract T convertRowToObject(UncheckedRow row);
    }

    // Custom Realm collection list iterator.
    public static abstract class ListIterator<T> extends Iterator<T> implements java.util.ListIterator<T> {

        public ListIterator(Collection collection, int start) {
            super(collection);
            if (start >= 0 && start <= iteratorCollection.size()) {
                pos = start - 1;
            } else {
                throw new IndexOutOfBoundsException("Starting location must be a valid index: [0, "
                        + (iteratorCollection.size() - 1) + "]. Yours was " + start);
            }
        }

        /**
         * Unsupported by Realm collection iterators.
         *
         * @throws UnsupportedOperationException
         */
        @Override
        @Deprecated
        public void add(T object) {
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
         *
         * @throws UnsupportedOperationException
         */
        @Override
        @Deprecated
        public void set(T object) {
            throw new UnsupportedOperationException("Replacing and element is not supported.");
        }
    }

    private final long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();
    private final SharedRealm sharedRealm;
    private final Context context;
    private final Table table;
    private boolean loaded;
    private boolean isSnapshot = false;
    private final ObserverPairList<CollectionObserverPair> observerPairs =
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

    public static Collection createBacklinksCollection(SharedRealm realm, UncheckedRow row, Table srcTable, String srcFieldName) {
        long backlinksPtr = nativeCreateResultsFromBacklinks(
                realm.getNativePtr(),
                row.getNativePtr(),
                srcTable.getNativePtr(),
                srcTable.getColumnIndex(srcFieldName));
        return new Collection(realm, srcTable, backlinksPtr, true);
    }

    public Collection(SharedRealm sharedRealm, TableQuery query,
            SortDescriptor sortDescriptor, SortDescriptor distinctDescriptor) {
        query.validateQuery();

        this.nativePtr = nativeCreateResults(sharedRealm.getNativePtr(), query.getNativePtr(),
                sortDescriptor,
                distinctDescriptor);

        this.sharedRealm = sharedRealm;
        this.context = sharedRealm.context;
        this.table = query.getTable();
        this.context.addReference(this);
        this.loaded = false;
    }

    public Collection(SharedRealm sharedRealm, TableQuery query, SortDescriptor sortDescriptor) {
        this(sharedRealm, query, sortDescriptor, null);
    }

    public Collection(SharedRealm sharedRealm, TableQuery query) {
        this(sharedRealm, query, null, null);
    }

    public Collection(SharedRealm sharedRealm, LinkView linkView, SortDescriptor sortDescriptor) {
        this.nativePtr = nativeCreateResultsFromLinkView(sharedRealm.getNativePtr(), linkView.getNativePtr(),
                sortDescriptor);

        this.sharedRealm = sharedRealm;
        this.context = sharedRealm.context;
        this.table = linkView.getTable();
        this.context.addReference(this);
        // Collection created from LinkView is loaded by default. So that the listener will be triggered first time
        // with empty change set.
        this.loaded = true;
    }

    private Collection(SharedRealm sharedRealm, Table table, long nativePtr) {
        this(sharedRealm, table, nativePtr, false);
    }

    private Collection(SharedRealm sharedRealm, Table table, long nativePtr, boolean loaded) {
        this.sharedRealm = sharedRealm;
        this.context = sharedRealm.context;
        this.table = table;
        this.nativePtr = nativePtr;
        this.context.addReference(this);
        this.loaded = loaded;
    }

    public Collection createSnapshot() {
        if (isSnapshot) {
            return this;
        }
        Collection collection = new Collection(sharedRealm, table, nativeCreateSnapshot(nativePtr));
        collection.isSnapshot = true;
        return collection;
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

    public Collection sort(SortDescriptor sortDescriptor) {
        return new Collection(sharedRealm, table, nativeSort(nativePtr, sortDescriptor));
    }

    public Collection distinct(SortDescriptor distinctDescriptor) {
        return new Collection(sharedRealm, table, nativeDistinct(nativePtr, distinctDescriptor));
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
    @SuppressWarnings("unused")
    private void notifyChangeListeners(long nativeChangeSetPtr) {
        if (nativeChangeSetPtr == 0 && isLoaded()) {
            return;
        }
        boolean wasLoaded = loaded;
        loaded = true;
        // Object Store compute the change set between the SharedGroup versions when the query created and the latest.
        // So it is possible it deliver a non-empty change set for the first async query returns. In this case, we
        // return an empty change set to user since it is considered as the first time async query returns.
        observerPairs.foreach(new Callback(nativeChangeSetPtr == 0 || !wasLoaded ?
                null : new CollectionChangeSet(nativeChangeSetPtr)));
    }

    public Mode getMode() {
        return Mode.getByValue(nativeGetMode(nativePtr));
    }

    // The Results of Object Store will be queried asynchronously in nature. But we do have to support "sync" query by
    // Java like RealmQuery.findAll().
    // The flag is used for following cases:
    // 1. For sync query, loaded will be set to true when collection is created. So we will bypass the first trigger of
    //    listener if it comes with empty change set from Object Store since we assume user already got the query
    //    result.
    // 2. For async query, when load() gets called with loaded not set, the listener should be triggered with empty
    //    change set since it is considered as query first returned.
    // 3. If the listener triggered with empty change set after load() called for async queries, it is treated as the
    //    same case as 1).
    // TODO: Results built from a LinkView has not been considered yet. Maybe it should bet set as loaded when create.
    public boolean isLoaded() {
        return loaded;
    }

    public void load() {
        if (loaded) {
            return;
        }
        notifyChangeListeners(0);
    }

    private static native long nativeGetFinalizerPtr();

    private static native long nativeCreateResults(long sharedRealmNativePtr, long queryNativePtr,
            SortDescriptor sortDesc, SortDescriptor distinctDesc);

    private static native long nativeCreateResultsFromLinkView(long sharedRealmNativePtr, long linkViewPtr,
            SortDescriptor sortDesc);

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

    // Non-static, we need this Collection object in JNI.
    private native void nativeStartListening(long nativePtr);

    private native void nativeStopListening(long nativePtr);

    private static native long nativeWhere(long nativePtr);

    private static native long nativeIndexOf(long nativePtr, long rowNativePtr);

    private static native long nativeIndexOfBySourceRowIndex(long nativePtr, long sourceRowIndex);

    private static native boolean nativeIsValid(long nativePtr);

    private static native byte nativeGetMode(long nativePtr);

    private static native long nativeCreateResultsFromBacklinks(long sharedRealmNativePtr, long rowNativePtr, long srcTableNativePtr, long srColIndex);
}
