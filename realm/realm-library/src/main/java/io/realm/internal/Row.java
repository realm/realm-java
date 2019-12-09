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
     * Returns all the column names of the tables.
     *
     * @return array of column names.
     */
    String[] getColumnNames();

    /**
     * Returns the column key from a column name.
     *
     * @param columnName column name
     * @return the column key
     */
    long getColumnKey(String columnName);

    /**
     * Gets the type of a column identified by the columnKey.
     *
     * @param columnKey column key.
     * @return the type of the particular column.
     */
    RealmFieldType getColumnType(long columnKey);

    Table getTable();

    /**
     * Returns the object key in the original source table, not the tableview.
     */
    long getObjectKey();

    long getLong(long columnKey);

    boolean getBoolean(long columnKey);

    float getFloat(long columnKey);

    double getDouble(long columnKey);

    Date getDate(long columnKey);

    String getString(long columnKey);

    byte[] getBinaryByteArray(long columnKey);

    long getLink(long columnKey);

    boolean isNullLink(long columnKey);

    OsList getModelList(long columnKey);

    OsList getValueList(long columnKey, RealmFieldType fieldType);

    void setLong(long columnKey, long value);

    void setBoolean(long columnKey, boolean value);

    void setFloat(long columnKey, float value);

    void setDouble(long columnKey, double value);

    void setDate(long columnKey, Date date);

    void setString(long columnKey, @Nullable String value);

    void setBinaryByteArray(long columnKey, @Nullable byte[] data);

    void setLink(long columnKey, long value);

    void nullifyLink(long columnKey);

    boolean isNull(long columnKey);

    void setNull(long columnKey);

    /**
     * Checks if the row is still valid.
     *
     * @return {@code true} if the row is still valid and attached to the underlying data. {@code false} otherwise.
     */
    boolean isValid();

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

    /**
     * Returns a frozen copy of this Row.
     */
    Row freeze(OsSharedRealm frozenRealm);

    /**
     * Return whether the row is considered to be loaded, i.e. it doesn't represent a query in flight.
     *
     */
    boolean isLoaded();
}
