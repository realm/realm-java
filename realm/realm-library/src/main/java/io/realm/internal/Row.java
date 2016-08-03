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

import io.realm.RealmFieldType;

/**
 * Interface for Row objects that act as wrappers around the Realm Core Row object.
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

    LinkView getLinkList(long columnIndex);

    void setLong(long columnIndex, long value);

    void setBoolean(long columnIndex, boolean value);

    void setFloat(long columnIndex, float value);

    void setDouble(long columnIndex, double value);

    void setDate(long columnIndex, Date date);

    void setString(long columnIndex, String value);

    void setBinaryByteArray(long columnIndex, byte[] data);

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
     * Returns {@code true} if the field name exists.
     *
     * @param fieldName Field name to check.
     * @return {@code true} if field name exists, {@code false} otherwise.
     */
    boolean hasColumn(String fieldName);

    Row EMPTY_ROW = new Row() {
        private final static String UNLOADED_ROW_MESSAGE = "Can't access a row that hasn't been loaded or represents 'null', " +
                "make sure the instance is loaded and is valid by calling 'RealmObject.isLoaded() && RealmObject.isValid()'.";

        @Override
        public long getColumnCount() {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public String getColumnName(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public long getColumnIndex(String columnName) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public RealmFieldType getColumnType(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public Table getTable() {
            return null;
        }

        @Override
        public long getIndex() {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public long getLong(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public boolean getBoolean(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public float getFloat(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public double getDouble(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public Date getDate(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public String getString(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public byte[] getBinaryByteArray(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public long getLink(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public boolean isNullLink(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public boolean isNull(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public void setNull(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public LinkView getLinkList(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public void setLong(long columnIndex, long value) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public void setBoolean(long columnIndex, boolean value) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public void setFloat(long columnIndex, float value) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public void setDouble(long columnIndex, double value) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public void setDate(long columnIndex, Date date) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public void setString(long columnIndex, String value) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public void setBinaryByteArray(long columnIndex, byte[] data) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public void setLink(long columnIndex, long value) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public void nullifyLink(long columnIndex) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }

        @Override
        public boolean isAttached() {
            return false;
        }

        @Override
        public boolean hasColumn(String fieldName) {
            throw new IllegalStateException(UNLOADED_ROW_MESSAGE);
        }
    };
}
