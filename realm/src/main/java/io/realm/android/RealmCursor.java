/*
 * Copyright 2015 Realm Inc.
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
import android.database.CursorIndexOutOfBoundsException;
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
 * This class exposes {@link io.realm.RealmResults} as a cursor.
 * <p>
 * A RealmCursor has the same thread restrictions as RealmResults, so it is not possible to move a RealmCursor between
 * threads.
 * <p>
 * A RealmCursor is currently limited to only being able to display data from a single RealmClass, i.e. it is not
 * possible to follow links to other objects.
 */
public class RealmCursor implements Cursor {

    private static final String DEFAULT_ID_COLUMN = "_id";

    // Needed because constants are only available from API 11
    private static final int FIELD_TYPE_NULL;
    private static final int FIELD_TYPE_INTEGER;
    private static final int FIELD_TYPE_FLOAT;
    private static final int FIELD_TYPE_STRING;
    private static final int FIELD_TYPE_BLOB;
    static {
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            FIELD_TYPE_NULL = Cursor.FIELD_TYPE_NULL;
            FIELD_TYPE_INTEGER = Cursor.FIELD_TYPE_INTEGER;
            FIELD_TYPE_FLOAT = Cursor.FIELD_TYPE_FLOAT;
            FIELD_TYPE_STRING = Cursor.FIELD_TYPE_STRING;
            FIELD_TYPE_BLOB = Cursor.FIELD_TYPE_BLOB;
        } else {
            FIELD_TYPE_NULL = 0;
            FIELD_TYPE_INTEGER = 1;
            FIELD_TYPE_FLOAT = 2;
            FIELD_TYPE_STRING = 3;
            FIELD_TYPE_BLOB = 4;
        }
    }

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
     * Exposes a query result as a cursor. Use {@link RealmResults#getCursor()} instead of this
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

    /**
     * Returns the numbers of rows in the cursor.
     *
     * @return the number of rows in the cursor.
     */
    @Override
    public int getCount() {
        checkClosed();
        return (int) table.size();
    }

    /**
     * Returns the current position of the cursor in the row set.
     * The value is zero-based. When the row set is first returned the cursor
     * will be at position -1, which is before the first row. After the
     * last row is returned another call to {@link #moveToNext()} will leave the cursor past
     * the last entry, at a position of count().
     *
     * @return the current cursor position.
     */
    @Override
    public int getPosition() {
        checkClosed();
        return rowIndex;
    }

    /**
     * Moves the cursor by a relative amount, forward or backward, from the
     * current position. Positive offsets move forwards, negative offsets move
     * backwards. If the calculated position is outside of the bounds of the result
     * set then the new position will be pinned to -1 or {@link #getCount()} depending
     * on whether the value is off the front or end of the set, respectively.
     *
     * <p>This method will return true if the requested destination is
     * reachable, otherwise, it returns false. For example, if the cursor is currently
     * at the second entry in the result set and move(-5) is called,
     * the position will be pinned at -1, and false will be returned.
     *
     * @param offset the offset to be applied from the current position.
     * @return whether the requested move fully succeeded.
     */
    @Override
    public final boolean move(int offset) {
        checkClosed();
        return moveToPosition(rowIndex + offset);
    }

    /**
     * Moves the cursor to an absolute position. The valid
     * range of values is -1 &lt;= position &lt;= count.
     *
     * <p>This method will return true if the request destination is reachable,
     * otherwise, it returns false.
     *
     * @param position the zero-based position to move to.
     * @return whether the requested move fully succeeded.
     */
    @Override
    public boolean moveToPosition(int position) {
        checkClosed();

        // Verify position is not beyond the end of the cursor.
        // This also captures moveToPosition(0) for the empty cursor.
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

    /**
     * Moves the cursor to the first row.
     *
     * <p>This method will return false if the cursor is empty.
     *
     * @return whether the move succeeded.
     */
    @Override
    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    /**
     * Moves the cursor to the last row.
     *
     * <p>This method will return false if the cursor is empty.
     *
     * @return whether the move succeeded.
     */
    @Override
    public boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    /**
     * Moves the cursor to the next row.
     *
     * <p>This method will return false if the cursor is already past the
     * last entry in the result set.
     *
     * @return whether the move succeeded.
     */
    @Override
    public boolean moveToNext() {
        return moveToPosition(rowIndex + 1);
    }

    /**
     * Moves the cursor to the previous row.
     *
     * <p>This method will return false if the cursor is already before the
     * first entry in the result set.
     *
     * @return whether the move succeeded.
     */
    @Override
    public boolean moveToPrevious() {
        return moveToPosition(rowIndex - 1);
    }

    /**
     * Returns whether the cursor is pointing to the first row.
     *
     * @return whether the cursor is pointing at the first entry.
     */
    @Override
    public boolean isFirst() {
        checkClosed();
        return rowIndex == 0 && getCount() != 0;
    }

    /**
     * Returns whether the cursor is pointing to the last row.
     *
     * @return whether the cursor is pointing at the last entry.
     */
    @Override
    public boolean isLast() {
        checkClosed();
        int count = getCount();
        return rowIndex == (count - 1) && count != 0;
    }

    /**
     * Returns whether the cursor is pointing to the position before the first
     * row.
     *
     * @return whether the cursor is before the first result.
     */
    @Override
    public boolean isBeforeFirst() {
        checkClosed();
        if (getCount() == 0) {
            return true;
        }
        return rowIndex == -1;
    }

    /**
     * Returns whether the cursor is pointing to the position after the last
     * row.
     *
     * @return whether the cursor is after the last result.
     */
    @Override
    public boolean isAfterLast() {
        checkClosed();
        if (getCount() == 0) {
            return true;
        }
        return rowIndex == getCount();
    }

    /**
     * Returns the zero-based index for the given field name, or -1 if the field doesn't exist.
     * If you expect the field to exist use {@link #getColumnIndexOrThrow(String)} instead, which
     * will make the error more clear.
     *
     * @param fieldName the name of the target field.
     * @return the zero-based column index for the given column field, or -1 if
     * the field name does not exist.
     * @see #getColumnIndexOrThrow(String)
     */
    @Override
    public int getColumnIndex(String fieldName) {
        checkClosed();
        if (DEFAULT_ID_COLUMN.equals(fieldName)) {
            return (int) idColumnIndex;
        }
        return (int) table.getColumnIndex(fieldName);
    }

    /**
     * Returns the zero-based index for the given field name, or throws
     * {@link IllegalArgumentException} if the field doesn't exist. If you're not sure if
     * a field will exist or not, then use {@link #getColumnIndex(String)} and check for -1. This
     * is more efficient than catching the exceptions.
     *
     * @param fieldName the name of the target column.
     * @return the zero-based column index for the given field name
     * @see #getColumnIndex(String)
     * @throws IllegalArgumentException if the column does not exist
     */
    @Override
    public int getColumnIndexOrThrow(String fieldName) throws IllegalArgumentException {
        checkClosed();

        int index;
        if (DEFAULT_ID_COLUMN.equals(fieldName)) {
            index = (int) idColumnIndex;
        } else {
            index = (int) table.getColumnIndex(fieldName);
        }
        if (index == TableOrView.NO_MATCH) {
            throw new IllegalArgumentException(fieldName + " not found in this cursor.");
        }
        return index;
    }

    /**
     * Returns the column name at the given zero-based column index.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the column name for the given column index.
     */
    @Override
    public String getColumnName(int columnIndex) {
        checkClosed();
        return table.getColumnName(columnIndex);
    }

    /**
     * Returns a string array holding the names of all of the columns in the
     * result set in the order in which they were listed in the result.
     *
     * @return the names of the columns returned in this query.
     */
    @Override
    public String[] getColumnNames() {
        checkClosed();
        int columns = (int) table.getColumnCount();
        String[] columnNames = new String[columns];
        for (int i = 0; i < columns; i++) {
            columnNames[i] = table.getColumnName(i);
        }
        return columnNames;
    }

    /**
     * Return total number of columns
     * @return number of columns
     */
    @Override
    public int getColumnCount() {
        checkClosed();
        return (int) table.getColumnCount();
    }

    /**
     * Returns the value of the requested column as a byte array.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a byte array.
     * @throws IllegalArgumentException if the requested column is not a byte array.
     */
    @Override
    public byte[] getBlob(int columnIndex) {
        checkClosed();
        checkPosition();
        return table.getBinaryByteArray(columnIndex, rowIndex);
    }

    /**
     * Returns the value of the requested column as a String.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a String.
     * @throws IllegalArgumentException if the requested column is not a String.
     */
    @Override
    public String getString(int columnIndex) {
        checkClosed();
        checkPosition();
        return table.getString(columnIndex, rowIndex);
    }

    /**
     * Retrieves the requested column text and stores it in the buffer provided.
     * If the buffer size is not sufficient, a new char buffer will be allocated
     * and assigned to CharArrayBuffer.data
     * @param columnIndex the zero-based index of the target column.
     *        if the target column is null, return buffer
     * @param buffer the buffer to copy the text into.
     * @throws IllegalArgumentException if the requested column is not a String.
     */
    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        checkClosed();
        checkPosition();
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

    /**
     * Returns the value of the requested column as a short.
     *
     * <p>Integer values outside the range [<code>Short.MIN_VALUE</code>,
     * <code>Short.MAX_VALUE</code>] will overflow.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a short.
     * @throws IllegalArgumentException if the requested column is not a short, int or long.
     */
    @Override
    public short getShort(int columnIndex) {
        checkClosed();
        checkPosition();
        return (short) mapRealmTypeToCursor(columnIndex, false);
    }

    /**
     * Returns the value of the requested column as an int.
     *
     * <p>Integer values outside the range [<code>Integer.MIN_VALUE</code>,
     * <code>Integer.MAX_VALUE</code>] will overflow.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as an int.
     * @throws IllegalArgumentException if the requested column is not a short, int or long.
     */
    @Override
    public int getInt(int columnIndex) {
        checkClosed();
        checkPosition();
        return (int) mapRealmTypeToCursor(columnIndex, false);
    }

    /**
     * Returns the value of the requested column as a long.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a long.
     * @throws IllegalArgumentException if the requested column is not a short, int or long.
     */
    @Override
    public long getLong(int columnIndex) {
        checkClosed();
        checkPosition();
        return mapRealmTypeToCursor(columnIndex, true);
    }

    private long mapRealmTypeToCursor(int columnIndex, boolean acceptLong) {
        ColumnType type = table.getColumnType(columnIndex);
        switch (type) {
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

    /**
     * Returns the value of the requested column as a float. Note that {@link #getType(int)} will return
     * {@link Cursor#FIELD_TYPE_FLOAT} for both {@code float} and {@code double} columns.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a float.
     *
     * @throws IllegalArgumentException if the requested column is not a float.
     */
    @Override
    public float getFloat(int columnIndex) {
        checkClosed();
        checkPosition();
        return table.getFloat(columnIndex, rowIndex);
    }

    /**
     * Returns the value of the requested column as a double. Note that {@link #getType(int)} will return
     * {@link Cursor#FIELD_TYPE_FLOAT} for both {@code float} and {@code double} columns.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a double.
     * @throws IllegalArgumentException if the requested column is not a double.
     */
    @Override
    public double getDouble(int columnIndex) {
        checkClosed();
        checkPosition();
        return table.getDouble(columnIndex, rowIndex);
    }

    /**
     * Returns the SQLite Cursor type for a column index. Realm has more types than SQLite and some of them cannot be
     * mapped to any meaningful SQLite type. These will return -1 and cannot be accessed using a cursor.
     *
     * The following types are currently not supported:
     * <ul>
     *   <li>{@link ColumnType#TABLE}</li>
     *   <li>{@link ColumnType#MIXED}</li>
     *   <li>{@link ColumnType#LINK}</li>
     *   <li>{@link ColumnType#LINK_LIST}</li>
     * </ul>
     *
     * <p>
     * Some realm types are mapped to conform to the Cursor interface. These mappings are described below:
     * <ol>
     *   <li>boolean : Will return Cursor.FIELD_TYPE_INTEGER. {0 = false, 1 = true}, use getShort()/getInt()/getLong()</li>
     *   <li>double : Will return Cursor.FIELD_TYPE_FLOAT, but must be fetched using use getDouble()</li>
     *   <li>Date : Will return Cursor.FIELD_TYPE_INTEGER. Time in milliseconds since epoch, use getLong()</li>
     *</ol>
     *
     * @param columnIndex Get the data type for this column index or -1 if the column isn't accessible.
     * @return One of the {@code FIELD_TYPE_*}'s described in {@link Cursor}.
     */
    @Override
    public int getType(int columnIndex) {
        checkClosed();
        ColumnType realmType = table.getColumnType(columnIndex);
        switch (realmType) {
            case BOOLEAN: return FIELD_TYPE_INTEGER;
            case INTEGER: return FIELD_TYPE_INTEGER;
            case FLOAT: return FIELD_TYPE_FLOAT;
            case DOUBLE: return FIELD_TYPE_FLOAT;
            case STRING: return FIELD_TYPE_STRING;
            case BINARY: return FIELD_TYPE_BLOB;
            case DATE: return FIELD_TYPE_INTEGER;
            case TABLE:
            case MIXED:
            case LINK:
            case LINK_LIST:
            default:
                return -1;
        }
    }

    /**
     * Realm currently doesn't support null. Calling this method will always throw an exception.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean isNull(int columnIndex) {
        ColumnType realmType = table.getColumnType(columnIndex);
        switch(realmType) {
            case LINK: return table.isNullLink(columnIndex, rowIndex);
            case LINK_LIST:
            case BOOLEAN:
            case INTEGER:
            case FLOAT:
            case DOUBLE:
            case STRING:
            case BINARY:
            case DATE:
            case TABLE:
            case MIXED:
            default:
            throw new UnsupportedOperationException("isNull not yet supported by Realm for this type: " + realmType);
        }
    }

    /**
     * Realm doesn't support {@link #deactivate()}. Calling this will throw an exception.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void deactivate() {
        throw new UnsupportedOperationException("As requery() is not supported, neither is deactivate()");
    }

    /**
     * Realm doesn't support {@link #requery()}. Calling this will throw an exception.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean requery() {
        throw new UnsupportedOperationException("Request a new cursor from the RealmResults or call Realm.refresh() instead");
    }

    /**
     * Calling this method will close this cursor. The original RealmResults will not be affected. Trying to access any data in
     * the cursor after calling this is illegal an the behavior is undefined.
     */
    @Override
    public void close() {
        closed = true;
        table = null; // Instead of closing the table, we just release it. Original RealmResults would also be closed otherwise.
        realm.removeChangeListener(changeListener);
        dataSetObservable.notifyInvalidated();
    }

    /**
     * Return true if the cursor is closed
     * @return true if the cursor is closed.
     */
    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * Realm doesn't support {@link ContentObserver}. Calling this will throw an exception.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void registerContentObserver(ContentObserver observer) {
        throw new UnsupportedOperationException("Content observers not supported");
    }

    /**
     * Realm doesn't support {@link ContentObserver}. Calling this will throw an exception.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        throw new UnsupportedOperationException("Content observers not supported");
    }

    /**
     * Registers an observer which is called when changes happen to the contents
     * of the this cursors data set, for example, when the data set is changed via
     * {@link #requery()}, {@link #deactivate()}, or {@link #close()}.
     *
     * @param observer the object that gets notified when the cursors data set changes.
     * @see #unregisterDataSetObserver(DataSetObserver)
     */
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        dataSetObservable.registerObserver(observer);
    }

    /**
     * Unregisters an observer which has previously been registered with this
     * cursor via {@link #registerContentObserver}.
     *
     * @param observer the object to unregister.
     * @see #registerDataSetObserver(DataSetObserver)
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        dataSetObservable.unregisterObserver(observer);
    }

    /**
     * Realm doesn't support {@link ContentResolver}. Calling this will throw an exception.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void setNotificationUri(ContentResolver cr, Uri uri) {
        throw new UnsupportedOperationException("Notification URIs are not supported by RealmCursor");
    }

    /**
     * Realm doesn't support {@link ContentResolver}. Calling this will throw an exception.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public Uri getNotificationUri() {
        throw new UnsupportedOperationException("Notification URIs are not supported by RealmCursor");
    }

    /**
     * RealmCursors are not accessible across processes. This method will always return false.
     *
     * @return Will always return {@code false}.
     * @see Cursor#getWantsAllOnMoveCalls()
     */
    @Override
    public boolean getWantsAllOnMoveCalls() {
        return false; // Realm TableViews don't support access from multiple processes.
    }

    /**
     * Realm doesn't support extra metadata in the form of a bundle. This method will always return the empty bundle.
     *
     * @return {@code Bundle.EMPTY}
     */
    @Override
    public Bundle getExtras() {
        return Bundle.EMPTY;
    }

    /**
     * Realm doesn't support extra metadata in the form of a bundle. This method will always return the empty bundle.
     *
     * @return {@code Bundle.EMPTY}
     */
    @Override
    public Bundle respond(Bundle extras) {
        return Bundle.EMPTY;
    }

    /**
     * MMaps a field name to act as the "_id" column too. Such a column is required by a number of Android framework
     * classes that uses cursors. The field must be able to be mapped to a long so {@link #getLong(int)} can return a
     * result. If a field already exists in the model class with the name "_id" calling this method will throw an
     * {@link IllegalArgumentException}.
     *
     * @param fieldName Field name found in the model class that should also act as the "_id" field.
     * @throws IllegalArgumentException If the field name doesn't exist, is of the wrong type or a field named
     * {@code _id} already exists.
     *
     * @see <a href="http://developer.android.com/reference/android/widget/CursorAdapter.html">CursorAdapter</a>
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

    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("Trying to access a closed cursor");
        }
    }

    private void checkPosition() {
        int size = getCount();
        if (-1 == rowIndex || size == rowIndex) {
            throw new CursorIndexOutOfBoundsException(rowIndex, size);
        }
    }
}
