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

package io.realm.internal;

import java.util.Date;

import javax.annotation.Nullable;

import io.realm.RealmFieldType;


/**
 * Interface for Row objects that act as wrappers around the Realm Core Row object.
 * <p>
 * When the actual class which implements this interface is {@link CheckedRow}, all methods in this
 * interface always validate their parameters and throw an appropriate exception if invalid.
 * For example, methods which accept a column name check the existence of the column and throw
 * {@link IllegalArgumentException} if not found.
 */
public interface Row {

    long getColumnCount();

    /**
     * Returns the name of a column identified by columnIndex. Notice that the index is zero based.
     *
     * @param columnIndex the column index.
     * @return the name of the column.
     */
    String getColumnName(long columnIndex);

    /**
     * Returns the 0-based index of a column based on the name.
     *
     * @param columnName column name
     * @return the index, {@code -1} if not found
     */
    long getColumnIndex(String columnName);

    /**
     * Gets the type of a column identified by the columnIndex.
     *
     * @param columnIndex index of the column.
     * @return the type of the particular column.
     */
    RealmFieldType getColumnType(long columnIndex);

    Table getTable();

    /**
     * Returns the index in the original source table, not the tableview.
     */
    long getIndex();

    long getLong(long columnIndex);

    boolean getBoolean(long columnIndex);

    float getFloat(long columnIndex);

    double getDouble(long columnIndex);

    Date getDate(long columnIndex);

    String getString(long columnIndex);

    byte[] getBinaryByteArray(long columnIndex);

    long getLink(long columnIndex);

    boolean isNullLink(long columnIndex);

    OsList getModelList(long columnIndex);

    OsList getValueList(long columnIndex, RealmFieldType fieldType);

    void setLong(long columnIndex, long value);

    void setBoolean(long columnIndex, boolean value);

    void setFloat(long columnIndex, float value);

    void setDouble(long columnIndex, double value);

    void setDate(long columnIndex, Date date);

    void setString(long columnIndex, @Nullable String value);

    void setBinaryByteArray(long columnIndex, @Nullable byte[] data);

    void setLink(long columnIndex, long value);

    void nullifyLink(long columnIndex);

    boolean isNull(long columnIndex);

    void setNull(long columnIndex);

    /**
     * Checks if the row is still valid.
     *
     * @return {@code true} if the row is still valid and attached to the underlying data. {@code false} otherwise.
     */
    boolean isAttached();

    /**
     * Throws {@link IllegalStateException} if the row is not attached.
     */
    void checkIfAttached();

    /**
     * Returns {@code true} if the field name exists.
     *
     * @param fieldName field name to check.
     * @return {@code true} if field name exists, {@code false} otherwise.
     */
    boolean hasColumn(String fieldName);
}
