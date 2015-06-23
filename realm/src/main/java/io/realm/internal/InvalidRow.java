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
 * Row wrapper that stubs all access with IllegalStateExceptions. This can be used instead of adding null checks
 * everywhere when the underlying Row accessor in Realm Core is no longer available.
 */
public enum InvalidRow implements Row {
    INSTANCE;

    @Override
    public long getColumnCount() {
        throw getStubException();
    }

    @Override
    public String getColumnName(long columnIndex) {
        throw getStubException();
    }

    @Override
    public long getColumnIndex(String columnName) {
        throw getStubException();
    }

    @Override
    public ColumnType getColumnType(long columnIndex) {
        throw getStubException();
    }

    @Override
    public Table getTable() {
        throw getStubException();
    }

    @Override
    public long getIndex() {
        throw getStubException();
    }

    @Override
    public long getLong(long columnIndex) {
        throw getStubException();
    }

    @Override
    public boolean getBoolean(long columnIndex) {
        throw getStubException();
    }

    @Override
    public float getFloat(long columnIndex) {
        throw getStubException();
    }

    @Override
    public double getDouble(long columnIndex) {
        throw getStubException();
    }

    @Override
    public Date getDate(long columnIndex) {
        throw getStubException();
    }

    @Override
    public String getString(long columnIndex) {
        throw getStubException();
    }

    @Override
    public byte[] getBinaryByteArray(long columnIndex) {
        throw getStubException();
    }

    @Override
    public Mixed getMixed(long columnIndex) {
        throw getStubException();
    }

    @Override
    public ColumnType getMixedType(long columnIndex) {
        throw getStubException();
    }

    @Override
    public long getLink(long columnIndex) {
        throw getStubException();
    }

    @Override
    public boolean isNullLink(long columnIndex) {
        throw getStubException();
    }

    @Override
    public LinkView getLinkList(long columnIndex) {
        throw getStubException();
    }

    @Override
    public void setLong(long columnIndex, long value) {
        throw getStubException();
    }

    @Override
    public void setBoolean(long columnIndex, boolean value) {
        throw getStubException();
    }

    @Override
    public void setFloat(long columnIndex, float value) {
        throw getStubException();
    }

    @Override
    public void setDouble(long columnIndex, double value) {
        throw getStubException();
    }

    @Override
    public void setDate(long columnIndex, Date date) {
        throw getStubException();
    }

    @Override
    public void setString(long columnIndex, String value) {
        throw getStubException();
    }

    @Override
    public void setBinaryByteArray(long columnIndex, byte[] data) {
        throw getStubException();
    }

    @Override
    public void setMixed(long columnIndex, Mixed data) {
        throw getStubException();
    }

    @Override
    public void setLink(long columnIndex, long value) {
        throw getStubException();
    }

    @Override
    public void nullifyLink(long columnIndex) {
        throw getStubException();
    }

    @Override
    public boolean isAttached() {
        throw getStubException();
    }

    @Override
    public boolean hasField(String fieldName) {
        throw getStubException();
    }

    @Override
    public long getNativePointer() {
        throw getStubException();
    }

    private RuntimeException getStubException() {
        return new IllegalStateException("Object is no longer managed by Realm. Has it been deleted?");
    }
}
