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

package io.realm.dynamic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.internal.ColumnType;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;

/**
 * Object for interacting with a RealmObject using dynamic names.
 *
 * @see io.realm.RealmMigration
 */
public class DynamicRealmObject {

     Realm realm;
     Row row;

    /**
     * Creates a dynamic Realm object based on a row entry.
     */
    public DynamicRealmObject(Realm realm, Row row) {
        this.realm = realm;
        this.row = row;
    }

    public boolean getBoolean(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkFieldExists(columnIndex, fieldName);
        checkColumnType(ColumnType.BOOLEAN, columnIndex, fieldName);
        checkLinkedField(fieldName);
        return row.getBoolean(columnIndex);
    }

    public int getInt(String fieldName) {
        return (int) getLong(fieldName);
    }

    public short getShort(String fieldName) {
        return (short) getLong(fieldName);
    }

    public long getLong(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkFieldExists(columnIndex, fieldName);
        checkColumnType(ColumnType.INTEGER, columnIndex, fieldName);
        checkLinkedField(fieldName);
        return row.getLong(columnIndex);
    }

    public float getFloat(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkFieldExists(columnIndex, fieldName);
        checkColumnType(ColumnType.FLOAT, columnIndex, fieldName);
        checkLinkedField(fieldName);
        return row.getFloat(columnIndex);
    }

    public double getDouble(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkFieldExists(columnIndex, fieldName);
        checkColumnType(ColumnType.DOUBLE, columnIndex, fieldName);
        checkLinkedField(fieldName);
        return row.getDouble(columnIndex);
    }

    public byte[] getBytes(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkFieldExists(columnIndex, fieldName);
        checkColumnType(ColumnType.BINARY, columnIndex, fieldName);
        checkLinkedField(fieldName);
        return row.getBinaryByteArray(columnIndex);
    }

    public String getString(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkFieldExists(columnIndex, fieldName);
        checkColumnType(ColumnType.STRING, columnIndex, fieldName);
        checkLinkedField(fieldName);
        return row.getString(columnIndex);
    }

    public Date getDate(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkFieldExists(columnIndex, fieldName);
        checkColumnType(ColumnType.DATE, columnIndex, fieldName);
        checkLinkedField(fieldName);
        return row.getDate(columnIndex);
    }

    public DynamicRealmObject getRealmObject(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkFieldExists(columnIndex, fieldName);
        checkColumnType(ColumnType.LINK, columnIndex, fieldName);
        checkLinkedField(fieldName);
        long linkRowIndex = row.getLink(columnIndex);
        Row linkRow = row.getTable().getRow(linkRowIndex);
        return new DynamicRealmObject(realm, linkRow);
    }

    public DynamicRealmList getRealmList(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkFieldExists(columnIndex, fieldName);
        checkColumnType(ColumnType.LINK_LIST, columnIndex, fieldName);
        checkLinkedField(fieldName);
        return new DynamicRealmList(row.getLinkList(columnIndex), realm);
    }

    public List<String> getFields() {
        List<String> fields = new ArrayList<String>();
        long columns = row.getColumnCount();
        for (int i = 0; i < columns; i++) {
            fields.add(row.getColumnName(i));
        }
        return fields;
    }

    public boolean hasField(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return false;
        }
        return row.getColumnIndex(fieldName) != TableOrView.NO_MATCH;
    }

    public String[] getKeys() {
        String[] keys = new String[(int) row.getColumnCount()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = row.getColumnName(i);
        }
        return keys;
    }

    private void checkFieldExists(long columnIndex, String fieldName) {
        if (columnIndex == TableOrView.NO_MATCH) {
            throw new IllegalArgumentException("'" + fieldName + "' doesn't exist on " + row.getTable().getName());
        }
    }

    private void checkColumnType(ColumnType expectedColumnType, long columnIndex, String fieldName) {
        ColumnType columnType = row.getColumnType(columnIndex);
        if (columnType != expectedColumnType) {
            throw new IllegalArgumentException(fieldName + " is not the expected type. It is a " + columnType);
        }
    }

    private void checkLinkedField(String fieldName) {
        if (fieldName.contains("\\.")) {
            throw new IllegalArgumentException("Fetching data across links not supported yet.");
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode(); // TODO
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicRealmObject other = (DynamicRealmObject) o;

        String path = realm.getPath();
        String otherPath = other.realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = row.getTable().getName();
        String otherTableName = other.row.getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (row.getIndex() != other.row.getIndex()) return false;

        return true;
    }

    @Override
    public String toString() {
        return super.toString(); // TODO
    }
}