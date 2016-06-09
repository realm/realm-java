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
public class LinkView extends NativeObject {

    private final Context context;
    final Table parent;
    final long columnIndexInParent;

    public LinkView(Context context, Table parent, long columnIndexInParent, long nativeLinkViewPtr) {
        this.context = context;
        this.parent = parent;
        this.columnIndexInParent = columnIndexInParent;
        this.nativePointer = nativeLinkViewPtr;

        context.executeDelayedDisposal();
        context.addReference(NativeObjectReference.TYPE_LINK_VIEW, this);
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
     *
     * If error checking is done elsewhere, consider using {@link #getUncheckedRow(long)} for better performance.
     *
     * @param index the index of row to fetch.
     * @return the safe row wrapper object.
     */
    public CheckedRow getCheckedRow(long index) {
        return CheckedRow.get(context, this, index);
    }

    public long getTargetRowIndex(long pos) {
        return nativeGetTargetRowIndex(nativePointer, pos);
    }

    public void add(long rowIndex) {
        checkImmutable();
        nativeAdd(nativePointer, rowIndex);
    }

    public void insert(long pos, long rowIndex) {
        checkImmutable();
        nativeInsert(nativePointer, pos, rowIndex);
    }

    public void set(long pos, long rowIndex) {
        checkImmutable();
        nativeSet(nativePointer, pos, rowIndex);
    }

    public void move(long oldPos, long newPos) {
        checkImmutable();
        nativeMove(nativePointer, oldPos, newPos);
    }

    public void remove(long pos) {
        checkImmutable();
        nativeRemove(nativePointer, pos);
    }

    public void clear() {
        checkImmutable();
        nativeClear(nativePointer);
    }

    public boolean contains(long tableRowIndex) {
        long index = nativeFind(nativePointer, tableRowIndex);
        return (index != TableOrView.NO_MATCH);
    }

    public long size() {
        return nativeSize(nativePointer);
    }

    public boolean isEmpty() {
        return nativeIsEmpty(nativePointer);
    }

    public TableQuery where() {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        this.context.executeDelayedDisposal();
        long nativeQueryPtr = nativeWhere(nativePointer);
        try {
            return new TableQuery(this.context, this.parent, nativeQueryPtr);
        } catch (RuntimeException e) {
            TableQuery.nativeClose(nativeQueryPtr);
            throw e;
        }
    }

    public boolean isAttached() {
        return nativeIsAttached(nativePointer);
    }

    /**
     * Returns the {@link Table} which all links point to.
     */
    public Table getTable() {
        return parent;
    }

    /**
     * Remove all target rows pointed to by links in this link view, and clear this link view.
     */
    public void removeAllTargetRows() {
        checkImmutable();
        nativeRemoveAllTargetRows(nativePointer);
    }

    /**
     * Removes target row from both the Realm and the LinkView.
     */
    public void removeTargetRow(int index) {
        checkImmutable();
        nativeRemoveTargetRow(nativePointer, index);
    }

    public Table getTargetTable() {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeTablePointer = nativeGetTargetTable(nativePointer);
        try {
            // Copy context reference from parent
            return new Table(context, this.parent, nativeTablePointer);
        } catch (RuntimeException e) {
            Table.nativeClose(nativeTablePointer);
            throw e;
        }
    }

    private void checkImmutable() {
        if (parent.isImmutable()) {
            throw new IllegalStateException("Changing Realm data can only be done from inside a write transaction.");
        }
    }

    static native void nativeClose(long nativeLinkViewPtr);
    native long nativeGetRow(long nativeLinkViewPtr, long pos);
    private native long nativeGetTargetRowIndex(long nativeLinkViewPtr, long pos);
    private native void nativeAdd(long nativeLinkViewPtr, long rowIndex);
    private native void nativeInsert(long nativeLinkViewPtr, long pos, long rowIndex);
    private native void nativeSet(long nativeLinkViewPtr, long pos, long rowIndex);
    private native void nativeMove(long nativeLinkViewPtr, long oldPos, long newPos);
    private native void nativeRemove(long nativeLinkViewPtr, long pos);
    private native void nativeClear(long nativeLinkViewPtr);
    private native long nativeSize(long nativeLinkViewPtr);
    private native boolean nativeIsEmpty(long nativeLinkViewPtr);
    protected native long nativeWhere(long nativeLinkViewPtr);
    private native boolean nativeIsAttached(long nativeLinkViewPtr);
    private native long nativeFind(long nativeLinkViewPtr, long targetRowIndex);
    private native void nativeRemoveTargetRow(long nativeLinkViewPtr, long rowIndex);
    private native void nativeRemoveAllTargetRows(long nativeLinkViewPtr);
    private native long nativeGetTargetTable(long nativeLinkViewPtr);
}
