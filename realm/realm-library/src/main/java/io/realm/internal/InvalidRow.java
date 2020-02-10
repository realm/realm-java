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
 * Row wrapper that stubs all access with IllegalStateExceptions except for isValid. This can be used instead of
 * adding null checks everywhere when the underlying Row accessor in Realm's underlying storage engine is no longer
 * available.
 */
public enum InvalidRow implements Row {
    INSTANCE;

    @Override
    public long getColumnCount() {
        throw getStubException();
    }

    @Override
    public String[] getColumnNames() {
        throw getStubException();
    }

    @Override
    public long getColumnKey(String columnName) {
        throw getStubException();
    }

    @Override
    public RealmFieldType getColumnType(long columnKey) {
        throw getStubException();
    }

    @Override
    public Table getTable() {
        throw getStubException();
    }

    @Override
    public long getObjectKey() {
        throw getStubException();
    }

    @Override
    public long getLong(long columnKey) {
        throw getStubException();
    }

    @Override
    public boolean getBoolean(long columnKey) {
        throw getStubException();
    }

    @Override
    public float getFloat(long columnKey) {
        throw getStubException();
    }

    @Override
    public double getDouble(long columnKey) {
        throw getStubException();
    }

    @Override
    public Date getDate(long columnKey) {
        throw getStubException();
    }

    @Override
    public String getString(long columnKey) {
        throw getStubException();
    }

    @Override
    public byte[] getBinaryByteArray(long columnKey) {
        throw getStubException();
    }

    @Override
    public long getLink(long columnKey) {
        throw getStubException();
    }

    @Override
    public boolean isNullLink(long columnKey) {
        throw getStubException();
    }

    @Override
    public OsList getModelList(long columnKey) {
        throw getStubException();
    }

    @Override
    public OsList getValueList(long columnKey, RealmFieldType fieldType) {
        throw getStubException();
    }

    @Override
    public void setLong(long columnKey, long value) {
        throw getStubException();
    }

    @Override
    public void setBoolean(long columnKey, boolean value) {
        throw getStubException();
    }

    @Override
    public void setFloat(long columnKey, float value) {
        throw getStubException();
    }

    @Override
    public void setDouble(long columnKey, double value) {
        throw getStubException();
    }

    @Override
    public void setDate(long columnKey, Date date) {
        throw getStubException();
    }

    @Override
    public void setString(long columnKey, String value) {
        throw getStubException();
    }

    @Override
    public void setBinaryByteArray(long columnKey, byte[] data) {
        throw getStubException();
    }

    @Override
    public void setLink(long columnKey, long value) {
        throw getStubException();
    }

    @Override
    public void nullifyLink(long columnKey) {
        throw getStubException();
    }

    @Override
    public boolean isNull(long columnKey) {
        throw getStubException();
    }

    @Override
    public void setNull(long columnKey) {
        throw getStubException();
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void checkIfAttached() {
        throw getStubException();
    }

    @Override
    public boolean hasColumn(String fieldName) {
        throw getStubException();
    }

    @Override
    public Row freeze(OsSharedRealm frozenRealm) {
        return INSTANCE;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    private RuntimeException getStubException() {
        return new IllegalStateException("Object is no longer managed by Realm. Has it been deleted?");
    }
}
