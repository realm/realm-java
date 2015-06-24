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

/**
 * The LinkView class represent a core {@link ColumnType#LINK_LIST}.
 */
public class LinkView {

    private final Context context;
    final long nativeLinkViewPtr;
    final Table parent;
    final long columnIndexInParent;

    public LinkView(Context context, Table parent, long columnIndexInParent, long nativeLinkViewPtr) {
        this.context = context;
        this.parent = parent;
        this.columnIndexInParent = columnIndexInParent;
        this.nativeLinkViewPtr = nativeLinkViewPtr;
    }

    /**
     * Returns a non-checking Row. Incorrect use of this Row will cause a hard Realm Core crash (SIGSEGV).
     * Only use this method if you are sure that input parameters are valid, otherwise use {@link #getCheckedRow(long)}
     * which will throw appropriate exceptions if used incorrectly.
     *
     * @param index Index of row to fetch.
     * @return Unsafe row wrapper object.
     */
    public UncheckedRow getUncheckedRow(long index) {
        return UncheckedRow.get(context, this, index);
    }

    /**
     * Returns a wrapper for Row access. All access will be error checked at the JNI layer and will throw an
     * appropriate {@link RuntimeException} if used incorrectly.
     *
     * If error checking is done elsewhere, consider using {@link #getUncheckedRow(long)} for better performance.
     *
     * @param index Index of row to fetch.
     * @return Safe row wrapper object.
     */
    public CheckedRow getCheckedRow(long index) {
        return CheckedRow.get(context, this, index);
    }

    public long getTargetRowIndex(long pos) {
        return nativeGetTargetRowIndex(nativeLinkViewPtr, pos);
    }

    public void add(long rowIndex) {
        checkImmutable();
        nativeAdd(nativeLinkViewPtr, rowIndex);
    }

    public void insert(long pos, long rowIndex) {
        checkImmutable();
        nativeInsert(nativeLinkViewPtr, pos, rowIndex);
    }

    public void set(long pos, long rowIndex) {
        checkImmutable();
        nativeSet(nativeLinkViewPtr, pos, rowIndex);
    }

    public void move(long oldPos, long newPos) {
        checkImmutable();
        nativeMove(nativeLinkViewPtr, oldPos, newPos);
    }

    public void remove(long pos) {
        checkImmutable();
        nativeRemove(nativeLinkViewPtr, pos);
    }

    public void clear() {
        checkImmutable();
        nativeClear(nativeLinkViewPtr);
    }

    public long size() {
        return nativeSize(nativeLinkViewPtr);
    }

    public boolean isEmpty() {
        return nativeIsEmpty(nativeLinkViewPtr);
    }

    public TableQuery where() {
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        this.context.executeDelayedDisposal();
        long nativeQueryPtr = nativeWhere(nativeLinkViewPtr);
        try {
            return new TableQuery(this.context, this.parent, nativeQueryPtr);
        } catch (RuntimeException e) {
            TableQuery.nativeClose(nativeQueryPtr);
            throw e;
        }
    }

    /**
     * Returns the Table which all links point to.
     */
    public Table getTable() {
        return parent;
    }

    private void checkImmutable() {
        if (parent.isImmutable()) {
            throw new IllegalStateException("Changing Realm data can only be done from inside a transaction.");
        }
    }

    protected static native void nativeClose(long nativeLinkViewPtr);
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

    /**
     * Get the Table data for rows in this LinkView
     */
    public Table getTable() {
        return parent;
    }
}
