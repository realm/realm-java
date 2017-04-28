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

import io.realm.RealmFieldType;


/**
 * The LinkView class represents a core {@link RealmFieldType#LIST}.
 */
public class LinkView implements NativeObject {

    private final Context context;
    final Table parent;
    final long columnIndexInParent;
    private final long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    public LinkView(Context context, Table parent, long columnIndexInParent, long nativeLinkViewPtr) {
        this.context = context;
        this.parent = parent;
        this.columnIndexInParent = columnIndexInParent;
        this.nativePtr = nativeLinkViewPtr;

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

    /**
     * Returns a non-checking {@link Row}. Incorrect use of this Row will cause a hard Realm Core crash (SIGSEGV).
     * Only use this method if you are sure that input parameters are valid, otherwise use {@link #getCheckedRow(long)}
     * which will throw appropriate exceptions if used incorrectly.
     *
     * @param index the index of row to fetch.
     * @return the unsafe row wrapper object.
     */
    public UncheckedRow getUncheckedRow(long index) {
        return UncheckedRow.getByRowIndex(context, this, index);
    }

    /**
     * Returns a wrapper for {@link Row} access. All access will be error checked at the JNI layer and will throw an
     * appropriate {@link RuntimeException} if used incorrectly.
     * <p>
     * If error checking is done elsewhere, consider using {@link #getUncheckedRow(long)} for better performance.
     *
     * @param index the index of row to fetch.
     * @return the safe row wrapper object.
     */
    public CheckedRow getCheckedRow(long index) {
        return CheckedRow.get(context, this, index);
    }

    /**
     * Returns the row index in the underlying table.
     */
    public long getTargetRowIndex(long linkViewIndex) {
        return nativeGetTargetRowIndex(nativePtr, linkViewIndex);
    }

    public void add(long rowIndex) {
        checkImmutable();
        nativeAdd(nativePtr, rowIndex);
    }

    public void insert(long pos, long rowIndex) {
        checkImmutable();
        nativeInsert(nativePtr, pos, rowIndex);
    }

    public void set(long pos, long rowIndex) {
        checkImmutable();
        nativeSet(nativePtr, pos, rowIndex);
    }

    public void move(long oldPos, long newPos) {
        checkImmutable();
        nativeMove(nativePtr, oldPos, newPos);
    }

    public void remove(long pos) {
        checkImmutable();
        nativeRemove(nativePtr, pos);
    }

    public void clear() {
        checkImmutable();
        nativeClear(nativePtr);
    }

    public boolean contains(long tableRowIndex) {
        long index = nativeFind(nativePtr, tableRowIndex);
        return (index != Table.NO_MATCH);
    }

    public long size() {
        return nativeSize(nativePtr);
    }

    public boolean isEmpty() {
        return nativeIsEmpty(nativePtr);
    }

    public TableQuery where() {
        long nativeQueryPtr = nativeWhere(nativePtr);
        return new TableQuery(this.context, this.getTargetTable(), nativeQueryPtr);
    }

    public boolean isAttached() {
        return nativeIsAttached(nativePtr);
    }

    /**
     * Removes all target rows pointed to by links in this link view, and clear this link view.
     */
    public void removeAllTargetRows() {
        checkImmutable();
        nativeRemoveAllTargetRows(nativePtr);
    }

    /**
     * Removes target row from both the Realm and the LinkView.
     */
    public void removeTargetRow(int index) {
        checkImmutable();
        nativeRemoveTargetRow(nativePtr, index);
    }

    public Table getTargetTable() {
        long nativeTablePointer = nativeGetTargetTable(nativePtr);
        return new Table(this.parent, nativeTablePointer);
    }

    private void checkImmutable() {
        if (parent.isImmutable()) {
            throw new IllegalStateException("Changing Realm data can only be done from inside a write transaction.");
        }
    }

    native long nativeGetRow(long nativeLinkViewPtr, long pos);

    private native long nativeGetTargetRowIndex(long nativeLinkViewPtr, long linkViewIndex);

    public static native void nativeAdd(long nativeLinkViewPtr, long rowIndex);

    private native void nativeInsert(long nativeLinkViewPtr, long pos, long rowIndex);

    private native void nativeSet(long nativeLinkViewPtr, long pos, long rowIndex);

    private native void nativeMove(long nativeLinkViewPtr, long oldPos, long newPos);

    private native void nativeRemove(long nativeLinkViewPtr, long pos);

    public static native void nativeClear(long nativeLinkViewPtr);

    private native long nativeSize(long nativeLinkViewPtr);

    private native boolean nativeIsEmpty(long nativeLinkViewPtr);

    protected native long nativeWhere(long nativeLinkViewPtr);

    private native boolean nativeIsAttached(long nativeLinkViewPtr);

    private native long nativeFind(long nativeLinkViewPtr, long targetRowIndex);

    private native void nativeRemoveTargetRow(long nativeLinkViewPtr, long rowIndex);

    private native void nativeRemoveAllTargetRows(long nativeLinkViewPtr);

    private native long nativeGetTargetTable(long nativeLinkViewPtr);

    private static native long nativeGetFinalizerPtr();
}
