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

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.internal.ColumnType;
import io.realm.internal.TableOrView;

/**
 * This classes exposes {@link io.realm.RealmResults} as a cursor.
 *
 * It is possible to traverse links using dot notation to access data in referenced objects, ie.
 * {@code cursor.getInt("foo.bar"} will return the integer from the {@code bar} field in the {@code foo} object
 * referenced the class represented by the RealmResult.
 *
 * Many Android framework classes require the presences of an "_id" field. Instead of adding such a field to your
 * model class it is instead possible to use {@link #setIdColumn(String)}.
 *
 * A RealmCursor has the same thread restrictions as RealmResults, so it is not possible to move a RealmCursor between
 * threads.
 *
 * TODO How to handle RealmList? getString("realmList[0].name)"?
 * TODO How to iterate a RealmList?
 */
public class RealmCursor implements Cursor {

    private static final String DEFAULT_ID_COLUMN = "_id";

    private final Realm realm;
    private TableOrView table;
    private int rowIndex;
    private boolean closed;
    private long idColumnIndex = -1; // Column index for the "_id" field. -1 means it hasn't been set
    private final DataSetObservable dataSetObservable = new DataSetObservable();
    private RealmChangeListener changeListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            if (dataSetObservable != null) {
                dataSetObservable.notifyChanged(); // TODO Should be replaced with something more fine grained.
            }
        }
    };

    /**
     * Exposes a RealmResults object as a cursor. Use {@link RealmResults#getCursor()} instead of this
     * constructor
     *
     * @param table Table view representing the query results.
     */
    public RealmCursor(Realm realm, TableOrView table) {
        this.table = table;
        this.rowIndex = -1;
        this.realm = realm;
        realm.addChangeListener(changeListener);
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
        if (getCount() == 0) {
            return true;
        }
        return rowIndex == -1;
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
        if (idColumnIndex >= 0) {
            return (int) idColumnIndex;
        } else {
            return (int) table.getColumnIndex(columnName);
        }
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
        int columns = (int) table.getColumnCount();
        String[] columnNames = new String[columns];
        for (int i = 0; i < columns; i++) {
            columnNames[i] = table.getColumnName(i);
        }
        return columnNames;
    }

    @Override
    public int getColumnCount() {
        return (int) table.getColumnCount();
    }

    @Override
    public byte[] getBlob(int columnIndex) {
        return table.getBinaryByteArray(columnIndex, rowIndex);
    }

    @Override
    public String getString(int columnIndex) {
        return table.getString(columnIndex, rowIndex);
    }

    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        String result = getString(columnIndex);
        if (result != null) {
            char[] data = buffer.data;
            if (data == null || data.length < result.length()) {
                buffer.data = result.toCharArray();
            } else {
                result.getChars(0, result.length(), data, 0);
            }
            buffer.sizeCopied = result.length();
        } else {
            buffer.sizeCopied = 0;
        }
    }

    @Override
    public short getShort(int columnIndex) {
        return (short) mapRealmTypeToCursor(columnIndex, false);
    }

    @Override
    public int getInt(int columnIndex) {
        return (int) mapRealmTypeToCursor(columnIndex, false);
    }

    @Override
    public long getLong(int columnIndex) {
        return mapRealmTypeToCursor(columnIndex, true);
    }

    private long mapRealmTypeToCursor(int columnIndex, boolean acceptLong) {
        ColumnType type = table.getColumnType(columnIndex);
        switch(type) {
            case BOOLEAN: return table.getBoolean(columnIndex, rowIndex) ? 1 : 0;
            case INTEGER: return table.getLong(columnIndex, rowIndex);
            case DATE:
                if (!acceptLong) {
                    throw new IllegalArgumentException("Use getLong() instead to get date values.");
                } else {
                    return table.getDate(columnIndex, rowIndex).getTime();
                }
            default:
                throw new IllegalArgumentException(String.format("Column at index  %s is not a Boolean or an Integer but a %s",
                        columnIndex, type));
        }
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
     * Returns the SQLite Cursor type for a column index. Realm has more types than SQLite and some of them cannot be
     * mapped to any meaningful SQLite type. These will return -1 and are cannot be accessed using a cursor.
     * <p>
     * Some realm types are mapped to conform to the Cursor interface. These mappings are described below:
     * <ol>
     *   <li>Boolean -> {0 = false, 1 = true}, use getShort()/getInt()/getLong()</li>
     *   <li>Date -> Time in milliseconds since epoch, use getLong()</li>
     *</ol>
     *
     * @param columnIndex Get the data type from the this column index.
     * @return One of the {@code FIELD_TYPE_*}'s described in {@link Cursor}
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
            case DATE: return Cursor.FIELD_TYPE_INTEGER;
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
        realm.removeChangeListener(changeListener);
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
     * Map a field name to also act as the "_id" column. Such a column is required by a number of Android framework
     * classes that uses cursors. The field must be able to mapped to a long so {@link #getLong(int)} can return a
     * result. If a field already exists in the model class with the name "_id" calling this method will throw an
     * {@link IllegalArgumentException}.
     *
     * @param fieldName Field name found in the model class that should also act as the "_id" field.
     * @throws IllegalArgumentException If the field name doesn't exist, is of the wrong type or a field named
     * {@code _id} already exists.
     *
     * @see http://developer.android.com/reference/android/widget/CursorAdapter.html
     */
    public void setIdColumn(String fieldName) {

        // Check that field name exists
        int idIndex = getColumnIndex(fieldName);
        if (idIndex == TableOrView.NO_MATCH) {
            throw new IllegalArgumentException("Field name doesn't exist: " + fieldName);
        }

        // Check that type is correct
        int idType = getType(idIndex);
        if (idType != Cursor.FIELD_TYPE_INTEGER) {
            throw new IllegalArgumentException(fieldName + " cannot be mapped to a long.");
        }

        // Check that _id is not already a field, in which case it is not possible to override it.
        if (getColumnIndex(DEFAULT_ID_COLUMN) != TableOrView.NO_MATCH) {
            throw new IllegalArgumentException(String.format("%s is already a field name in the model class '%s' and " +
                            "cannot be overridden.", DEFAULT_ID_COLUMN, table.getTable().getName()));
        }

       this.idColumnIndex = idIndex;
    }
}
