package io.realm.internal;

import java.util.Date;

import javax.annotation.Nullable;

import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmChangeListener;

/**
 * Java wrapper of Object Store List class. This backs managed versions of RealmList.
 */
public class OsList implements NativeObject, ObservableCollection {

    private final long nativePtr;
    private final NativeContext context;
    private final Table targetTable;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();
    private final ObserverPairList<CollectionObserverPair> observerPairs =
            new ObserverPairList<CollectionObserverPair>();

    public OsList(UncheckedRow row, long columnIndex) {
        OsSharedRealm sharedRealm = row.getTable().getSharedRealm();
        long[] ptrs = nativeCreate(sharedRealm.getNativePtr(), row.getNativePtr(), columnIndex);

        this.nativePtr = ptrs[0];
        this.context = sharedRealm.context;
        context.addReference(this);

        if (ptrs[1] != 0) {
            targetTable = new Table(sharedRealm, ptrs[1]);
        } else {
            targetTable = null;
        }
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public UncheckedRow getUncheckedRow(long index) {
        return targetTable.getUncheckedRowByPointer(nativeGetRow(nativePtr, index));
    }

    public void addRow(long targetRowIndex) {
        nativeAddRow(nativePtr, targetRowIndex);
    }

    public void insertRow(long pos, long targetRowIndex) {
        nativeInsertRow(nativePtr, pos, targetRowIndex);
    }

    public void setRow(long pos, long targetRowIndex) {
        nativeSetRow(nativePtr, pos, targetRowIndex);
    }

    public void addNull() {
        nativeAddNull(nativePtr);
    }

    public void insertNull(long pos) {
        nativeInsertNull(nativePtr, pos);
    }

    public void setNull(long pos) {
        nativeSetNull(nativePtr, pos);
    }

    public void addLong(long value) {
        nativeAddLong(nativePtr, value);
    }

    public void insertLong(long pos, long value) {
        nativeInsertLong(nativePtr, pos, value);
    }

    public void setLong(long pos, long value) {
        nativeSetLong(nativePtr, pos, value);
    }

    public void addDouble(double value) {
        nativeAddDouble(nativePtr, value);
    }

    public void insertDouble(long pos, double value) {
        nativeInsertDouble(nativePtr, pos, value);
    }

    public void setDouble(long pos, double value) {
        nativeSetDouble(nativePtr, pos, value);
    }

    public void addFloat(float value) {
        nativeAddFloat(nativePtr, value);
    }

    public void insertFloat(long pos, float value) {
        nativeInsertFloat(nativePtr, pos, value);
    }

    public void setFloat(long pos, float value) {
        nativeSetFloat(nativePtr, pos, value);
    }

    public void addBoolean(boolean value) {
        nativeAddBoolean(nativePtr, value);
    }

    public void insertBoolean(long pos, boolean value) {
        nativeInsertBoolean(nativePtr, pos, value);
    }

    public void setBoolean(long pos, boolean value) {
        nativeSetBoolean(nativePtr, pos, value);
    }

    public void addBinary(@Nullable byte[] value) {
        nativeAddBinary(nativePtr, value);
    }

    public void insertBinary(long pos, @Nullable byte[] value) {
        nativeInsertBinary(nativePtr, pos, value);
    }

    public void setBinary(long pos, @Nullable byte[] value) {
        nativeSetBinary(nativePtr, pos, value);
    }

    public void addString(@Nullable String value) {
        nativeAddString(nativePtr, value);
    }

    public void insertString(long pos, @Nullable String value) {
        nativeInsertString(nativePtr, pos, value);
    }

    public void setString(long pos, @Nullable String value) {
        nativeSetString(nativePtr, pos, value);
    }

    public void addDate(@Nullable Date value) {
        if (value == null) {
            nativeAddNull(nativePtr);
        } else {
            nativeAddDate(nativePtr, value.getTime());
        }
    }

    public void insertDate(long pos, @Nullable Date value) {
        if (value == null) {
            nativeInsertNull(nativePtr, pos);
        } else {
            nativeInsertDate(nativePtr, pos, value.getTime());
        }
    }

    public void setDate(long pos, @Nullable Date value) {
        if (value == null) {
            nativeSetNull(nativePtr, pos);
        } else {
            nativeSetDate(nativePtr, pos, value.getTime());
        }
    }

    @Nullable
    public Object getValue(long pos) {
        return nativeGetValue(nativePtr, pos);
    }

    public void move(long sourceIndex, long targetIndex) {
        nativeMove(nativePtr, sourceIndex, targetIndex);
    }

    public void remove(long index) {
        nativeRemove(nativePtr, index);
    }

    public void removeAll() {
        nativeRemoveAll(nativePtr);
    }

    public long size() {
        return nativeSize(nativePtr);
    }

    public boolean isEmpty() {
        return nativeSize(nativePtr) <= 0;
    }

    /**
     * @return a {@link TableQuery} based on this list.
     */
    public TableQuery getQuery() {
        return new TableQuery(context, targetTable, nativeGetQuery(nativePtr));
    }

    public boolean isValid() {
        return nativeIsValid(nativePtr);
    }

    public void delete(long index) {
        nativeDelete(nativePtr, index);
    }

    public void deleteAll() {
        nativeDeleteAll(nativePtr);
    }

    public Table getTargetTable() {
        return targetTable;
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

    // Called by JNI
    @Override
    public void notifyChangeListeners(long nativeChangeSetPtr) {
        OsCollectionChangeSet changeset = new OsCollectionChangeSet(nativeChangeSetPtr, false);
        if (changeset.isEmpty()) {
            // First time "query" returns. Do nothing.
            return;
        }
        observerPairs.foreach(new Callback(changeset));
    }

    private static native long nativeGetFinalizerPtr();

    // TODO: nativeTablePtr is not necessary. It is used to create FieldDescriptor which should be generated from
    // OsSchemaInfo.
    // Returns {nativeListPtr, nativeTablePtr}
    private static native long[] nativeCreate(long nativeSharedRealmPtr, long nativeRowPtr, long columnIndex);

    private static native long nativeGetRow(long nativePtr, long index);

    private static native void nativeAddRow(long nativePtr, long targetRowIndex);

    private static native void nativeInsertRow(long nativePtr, long pos, long targetRowIndex);

    private static native void nativeSetRow(long nativePtr, long pos, long targetRowIndex);

    private static native void nativeMove(long nativePtr, long sourceIndex, long targetIndex);

    private static native void nativeRemove(long nativePtr, long index);

    private static native void nativeRemoveAll(long nativePtr);

    private static native long nativeSize(long nativePtr);

    private static native long nativeGetQuery(long nativePtr);

    private static native boolean nativeIsValid(long nativePtr);

    private static native void nativeDelete(long nativePtr, long index);

    private static native void nativeDeleteAll(long nativePtr);

    private static native void nativeAddNull(long nativePtr);

    private static native void nativeInsertNull(long nativePtr, long pos);

    private static native void nativeSetNull(long nativePtr, long pos);

    private static native void nativeAddLong(long nativePtr, long value);

    private static native void nativeInsertLong(long nativePtr, long pos, long value);

    private static native void nativeSetLong(long nativePtr, long pos, long value);

    private static native void nativeAddDouble(long nativePtr, double value);

    private static native void nativeInsertDouble(long nativePtr, long pos, double value);

    private static native void nativeSetDouble(long nativePtr, long pos, double value);

    private static native void nativeAddFloat(long nativePtr, float value);

    private static native void nativeInsertFloat(long nativePtr, long pos, float value);

    private static native void nativeSetFloat(long nativePtr, long pos, float value);

    private static native void nativeAddBoolean(long nativePtr, boolean value);

    private static native void nativeInsertBoolean(long nativePtr, long pos, boolean value);

    private static native void nativeSetBoolean(long nativePtr, long pos, boolean value);

    private static native void nativeAddBinary(long nativePtr, @Nullable byte[] value);

    private static native void nativeInsertBinary(long nativePtr, long pos, @Nullable byte[] value);

    private static native void nativeSetBinary(long nativePtr, long pos, @Nullable byte[] value);

    private static native void nativeAddDate(long nativePtr, long value);

    private static native void nativeInsertDate(long nativePtr, long pos, long value);

    private static native void nativeSetDate(long nativePtr, long pos, long value);

    private static native void nativeAddString(long nativePtr, @Nullable String value);

    private static native void nativeInsertString(long nativePtr, long pos, @Nullable String value);

    private static native void nativeSetString(long nativePtr, long pos, @Nullable String value);

    private static native Object nativeGetValue(long nativePtr, long pos);

    private native void nativeStartListening(long nativePtr);

    private native void nativeStopListening(long nativePtr);
}
