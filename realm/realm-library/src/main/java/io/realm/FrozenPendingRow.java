/*
 * Copyright 2019 Realm Inc.
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
package io.realm;

import java.util.Date;

import io.realm.internal.InvalidRow;
import io.realm.internal.OsList;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.Row;
import io.realm.internal.Table;


/**
 * A PendingRow that has been frozen. This behaves in many ways similar
 * to a deleted Row, but will report {@link #isLoaded()} as {@code as false}.
 */
public enum FrozenPendingRow implements Row {
    INSTANCE;

    private static final String QUERY_NOT_RETURNED_MESSAGE =
            "This object was frozen while a query for it was still running.";

    @Override
    public long getColumnCount() {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public String[] getColumnNames() {
        throw new  IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public long getColumnKey(String columnName) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public RealmFieldType getColumnType(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public Table getTable() {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public long getObjectKey() {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public long getLong(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public boolean getBoolean(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public float getFloat(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public double getDouble(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public Date getDate(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public String getString(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public byte[] getBinaryByteArray(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public long getLink(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public boolean isNullLink(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public OsList getModelList(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public OsList getValueList(long columnKey, RealmFieldType fieldType) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setLong(long columnKey, long value) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setBoolean(long columnKey, boolean value) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setFloat(long columnKey, float value) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setDouble(long columnKey, double value) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setDate(long columnKey, Date date) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setString(long columnKey, String value) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setBinaryByteArray(long columnKey, byte[] data) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setLink(long columnKey, long value) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void nullifyLink(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public boolean isNull(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public void setNull(long columnKey) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void checkIfAttached() {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public boolean hasColumn(String fieldName) {
        throw new IllegalStateException(QUERY_NOT_RETURNED_MESSAGE);
    }

    @Override
    public Row freeze(OsSharedRealm frozenRealm) {
        return InvalidRow.INSTANCE;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }
}
