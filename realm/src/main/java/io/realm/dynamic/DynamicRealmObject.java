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
package io.realm.dynamic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.internal.CheckedRow;
import io.realm.internal.ColumnType;
import io.realm.internal.Row;
import io.realm.internal.TableOrView;
import io.realm.internal.UncheckedRow;

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
        this.row = (row instanceof CheckedRow) ? (CheckedRow) row : ((UncheckedRow) row).convertToChecked();
    }

    public boolean getBoolean(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
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
        checkLinkedField(fieldName);
        return row.getLong(columnIndex);
    }

    public float getFloat(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkLinkedField(fieldName);
        return row.getFloat(columnIndex);
    }

    public double getDouble(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkLinkedField(fieldName);
        return row.getDouble(columnIndex);
    }

    public byte[] getBytes(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkLinkedField(fieldName);
        return row.getBinaryByteArray(columnIndex);
    }

    public String getString(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkLinkedField(fieldName);
        return row.getString(columnIndex);
    }

    public Date getDate(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkLinkedField(fieldName);
        return row.getDate(columnIndex);
    }

    /**
     * Checks if the value of a given is {@code null}.
     *
     * @param fieldName Name of field. Use "." as separator to access fields in linked objects.
     * @return {@code true} if field value is null, {@code false} otherwise.
     */
    public boolean isNull(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.isNullLink(columnIndex); // TODO Add support for other types
    }

    public DynamicRealmObject getRealmObject(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        checkLinkedField(fieldName);
        long linkRowIndex = row.getLink(columnIndex);
        CheckedRow linkRow = row.getTable().getCheckedRow(linkRowIndex);
        return new DynamicRealmObject(realm, linkRow);
    }

    public DynamicRealmList getRealmList(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
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

    public String[] getFieldNames() {
        String[] keys = new String[(int) row.getColumnCount()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = row.getColumnName(i);
        }
        return keys;
    }

    private void checkLinkedField(String fieldName) {
        if (fieldName.contains("\\.")) {
            throw new IllegalArgumentException("Fetching data across links not supported yet.");
        }
    }

    @Override
    public int hashCode() {
        String realmName = realm.getPath();
        String tableName = row.getTable().getName();
        long rowIndex = row.getIndex();

        int result = 17;
        result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0);
        result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0);
        result = 31 * result + (int) (rowIndex ^ (rowIndex >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DynamicRealmObject other = (DynamicRealmObject) o;

        String path = realm.getPath();
        String otherPath = other.realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) {
            return false;
        }

        String tableName = row.getTable().getName();
        String otherTableName = other.row.getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) {
            return false;
        }

        if (row.getIndex() != other.row.getIndex()) {
            return false;
        }

        return true;
    }

    public void setInt(int i) {

    }

    public void setInt(Integer j) {

    }

    @Override
    public String toString() {
        return super.toString(); // TODO How to iterate across all fields?
    }
    
}
