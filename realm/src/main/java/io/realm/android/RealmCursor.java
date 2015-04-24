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

package io.realm.android;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import io.realm.internal.ColumnType;
import io.realm.internal.TableOrView;

/**
 * A cursor implementation that exposes {@link io.realm.RealmQuery} results as a cursor.
 *
 * It is possible to traverse links using dot notation to access data in referenced objects, ie.
 * {@code cursor.getInt("foo.bar"} will return the integer from the {@code bar} field in the {@code foo} object from the
 * search result objects.
 *
 * Many Android framework classes require the presences of an "_id" field. Instead of adding such a field to your
 * model class it is instead possible to use {@link #setIdAlias(String)}.
 *
 * A RealmCursor has the same thread restrictions as RealmResults, so it is not possible to move a RealmCursor between
 * threads.
 *
 * @see RealmResults.getCursor()
 */
public class RealmCursor implements Cursor {

    private TableOrView table;
    private int rowIndex;
    private boolean closed;
    private long idColumnIndex = -1; // Column index for the "_id" field. -1 means it hasn't been set
    private final DataSetObservable dataSetObservable = new DataSetObservable();

    /**
     * Creates a cursor based on a search result.
     * @param table
     */
    public RealmCursor(TableOrView table) {
        this.table = table;
        this.rowIndex = -1;
    }

    @Override
    public int getCount() {
        return (int) table.size();
    }

    @Override
    public int getPosition() {
        return rowIndex;
    }

    @Override
    public final boolean move(int offset) {
        return moveToPosition(rowIndex + offset);
    }

    @Override
    public boolean moveToPosition(int position) {
        // Verify position is not post the end of the cursor.
        final int count = getCount();
        if (position >= count) {
            rowIndex = count;
            return false;
        }

        // Verify position is not before the start of the cursor.
        if (position < 0) {
            rowIndex = -1;
            return false;
        }

        rowIndex = position;
        return true;
    }

    @Override
    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    @Override
    public boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    @Override
    public boolean moveToNext() {
        return moveToPosition(rowIndex + 1);
    }

    @Override
    public boolean moveToPrevious() {
        return moveToPosition(rowIndex - 1);
    }

    @Override
    public boolean isFirst() {
        return rowIndex == 0 && getCount() != 0;
    }

    @Override
    public boolean isLast() {
        int count = getCount();
        return rowIndex == (count - 1) && count != 0;
    }

    @Override
    public boolean isBeforeFirst() {
        return false;
    }

    @Override
    public boolean isAfterLast() {
        if (getCount() == 0) {
            return true;
        }
        return rowIndex == getCount();
    }

    @Override
    public int getColumnIndex(String columnName) {
        return (int) table.getColumnIndex(columnName);
    }

    @Override
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        int index = (int) table.getColumnIndex(columnName);
        if (index == TableOrView.NO_MATCH) {
            throw new IllegalArgumentException(columnName + " not found in this cursor.");
        }
        return index;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return table.getColumnName(columnIndex);
    }

    @Override
    public String[] getColumnNames() {
        return new String[0];
    }

    @Override
    public int getColumnCount() {
        return (int) table.getColumnCount();
    }

    @Override
    public byte[] getBlob(int columnIndex) {
        return table.getBinaryByteArray(rowIndex, columnIndex);
    }

    @Override
    public String getString(int columnIndex) {
        return table.getString(rowIndex, columnIndex);
    }

    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        // TODO
    }

    @Override
    public short getShort(int columnIndex) {
        return (short) table.getLong(columnIndex, rowIndex);
    }

    @Override
    public int getInt(int columnIndex) {
        return (int) table.getLong(columnIndex, rowIndex);
    }

    @Override
    public long getLong(int columnIndex) {
        return table.getLong(columnIndex, rowIndex);
    }

    @Override
    public float getFloat(int columnIndex) {
        return table.getFloat(columnIndex, rowIndex);
    }

    @Override
    public double getDouble(int columnIndex) {
        return table.getDouble(columnIndex, rowIndex);
    }

    /**
     * Returns the SQLite Cursor type for a column index. Realm has more types than SQLite and some of the cannot be
     * mapped to any meaningful SQLite type. These will return -1.
     *
     * TODO How should we handle the type mismatch between SQLite and Realm.

     * @param columnIndex
     * @return
     */
    @Override
    public int getType(int columnIndex) {
        ColumnType realmType = table.getColumnType(columnIndex);
        switch (realmType) {
            case BOOLEAN: return Cursor.FIELD_TYPE_INTEGER;
            case INTEGER: return Cursor.FIELD_TYPE_INTEGER;
            case FLOAT: return Cursor.FIELD_TYPE_FLOAT;
            case DOUBLE: return Cursor.FIELD_TYPE_FLOAT;
            case STRING: return Cursor.FIELD_TYPE_STRING;
            case BINARY: return Cursor.FIELD_TYPE_BLOB;
            case DATE: // TODO Support Date somehow?
            case TABLE:
            case MIXED:
            case LINK:
            case LINK_LIST:
            default:
                return -1;
        }
    }

    @Override
    public boolean isNull(int columnIndex) {
        throw new UnsupportedOperationException("Null not yet supported by Realm");
    }

    @Override
    public void deactivate() {
        throw new UnsupportedOperationException("As requery() is not supported, neither is deactivate()");
    }

    @Override
    public boolean requery() {
        throw new UnsupportedOperationException("Request a new cursor from the RealmResults or call Realm.refresh() instead");
    }

    @Override
    public void close() {
        closed = true;
        table = null; // Instead of closing the table, we just release it. Original RealmResults would also be closed otherwise.
        dataSetObservable.notifyInvalidated();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        throw new UnsupportedOperationException("Content observers not supported");
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        throw new UnsupportedOperationException("Content observers not supported");
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        dataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        dataSetObservable.unregisterObserver(observer);
    }

    @Override
    public void setNotificationUri(ContentResolver cr, Uri uri) {
        throw new UnsupportedOperationException("Notification URI's are not supported by RealmCursor");
    }

    @Override
    public Uri getNotificationUri() {
        throw new UnsupportedOperationException("Notification URI's are not supported by RealmCursor");
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        return false; // Realm TableViews doesn't support access from multiple processes.
    }

    @Override
    public Bundle getExtras() {
        return Bundle.EMPTY;
    }

    @Override
    public Bundle respond(Bundle extras) {
        return Bundle.EMPTY;
    }

    /**
     * Map a field name to also act as "_id". This is required by a number of Android framework classes that uses
     * cursors. This field must be an integer. If a field already exists in the model class with the name "_id" calling
     * this will have no effect.
     *
     * @param fieldName Name of field name that should also act as "_id" field.
     * @return True if the alias was successfully set, false otherwise.
     *
     * @see http://developer.android.com/reference/android/widget/CursorAdapter.html
     */
    public boolean setIdAlias(String fieldName) {
        // TODO Check if fieldName exists
        // TODO Check if "_id" already exists as fieldName
        // TODO Otherwise set alias
        return false;
    }
}
