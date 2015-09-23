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
 *
 */

package io.realm.internal;

import java.util.Date;

/**
 * Interface for Row objects that act as wrappers around the Realm Core Row object.
 */
public interface Row {

    long getColumnCount();

    /**
     * Returns the name of a column identified by columnIndex. Notice that the
     * index is zero based.
     *
     * @param columnIndex the column index
     * @return the name of the column
     */
    String getColumnName(long columnIndex);

    /**
     * Returns the 0-based index of a column based on the name.
     *
     * @param columnName column name
     * @return the index, -1 if not found
     */
    long getColumnIndex(String columnName);

    /**
     * Get the type of a column identified by the columnIndex.
     *
     * @param columnIndex index of the column.
     * @return Type of the particular column.
     */
    ColumnType getColumnType(long columnIndex);

    Table getTable();

    long getIndex();

    long getLong(long columnIndex);

    boolean getBoolean(long columnIndex);

    float getFloat(long columnIndex);

    double getDouble(long columnIndex);

    Date getDate(long columnIndex);

    String getString(long columnIndex);

    byte[] getBinaryByteArray(long columnIndex);

    Mixed getMixed(long columnIndex);

    ColumnType getMixedType(long columnIndex);

    long getLink(long columnIndex);

    boolean isNullLink(long columnIndex);

    LinkView getLinkList(long columnIndex);

    void setLong(long columnIndex, long value);

    void setBoolean(long columnIndex, boolean value);

    void setFloat(long columnIndex, float value);

    void setDouble(long columnIndex, double value);

    void setDate(long columnIndex, Date date);

    void setString(long columnIndex, String value);

    void setBinaryByteArray(long columnIndex, byte[] data);

    void setMixed(long columnIndex, Mixed data);

    void setLink(long columnIndex, long value);

    void nullifyLink(long columnIndex);

    /**
     * Checks if the row is still valid.
     * @return Returns true {@code true} if the row is still valid and attached to the underlying
     * data. {@code false} otherwise.
     */
    boolean isAttached();

    /**
     * Returns {@code true} if the field name exists.
     * @param fieldName Field name to check.
     * @return {@code true} if field name exists, {@code false} otherwise.
     */
    boolean hasColumn(String fieldName);
}
