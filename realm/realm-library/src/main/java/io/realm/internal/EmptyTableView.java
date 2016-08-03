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

package io.realm.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.realm.RealmFieldType;

public final class EmptyTableView implements TableOrView {

    private final String className;
    private final long columnCount;
    private final List<RealmFieldType> fieldTypes;
    private final List<String> fieldNames;

    public EmptyTableView(TableOrView tableOrView) {
        this.className = tableOrView.getTable().getName();
        this.columnCount = tableOrView.getColumnCount();
        ArrayList<RealmFieldType> types = new ArrayList();
        ArrayList<String> names = new ArrayList();
        for (long i = 0; i < columnCount; i++) {
            types.add(getColumnType(i));
            names.add(getColumnName(i));
        }
        fieldTypes = Collections.unmodifiableList(types);
        fieldNames = Collections.unmodifiableList(names);
    }

    // this is to follow JNI column index check behavior
    private void checkIfValidColumnIndex(long columnIndex) {
        if (columnIndex < 0) {
            throw new IndexOutOfBoundsException("columnIndex is less than 0.");
        }
        if (columnCount <= columnIndex) {
            throw new IndexOutOfBoundsException("columnIndex > available columns.");
        }
    }

    @Override
    public void clear() {}

    @Override
    public Table getTable() {
        throw new IllegalStateException("No result can be found.");
    }

    @Override
    public void close() {}

    @Override
    public long size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void remove(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result can be removed");
    }

    @Override
    public void removeLast() {
        throw new IndexOutOfBoundsException("No result can be removed");
    }

    @Override
    public long getColumnCount() {
        return columnCount;
    }

    @Override
    public String getColumnName(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        return fieldNames.get((int)columnIndex);
    }

    @Override
    public long getColumnIndex(String columnName) {
        if (columnName == null) {
            throw new IllegalArgumentException("Column name can not be null.");
        }
        for (int index = 0; index < columnCount; index++) {
            if (fieldNames.get(index).equals(columnName)) {
                return index;
            }
        }
        return NO_MATCH;
    }

    @Override
    public RealmFieldType getColumnType(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        return fieldTypes.get((int)columnIndex);
    }

    @Override
    public long getLong(long columnIndex, long rowIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public boolean getBoolean(long columnIndex, long rowIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public float getFloat(long columnIndex, long rowIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public double getDouble(long columnIndex, long rowIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public String getString(long columnIndex, long rowIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public Date getDate(long columnIndex, long rowIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public byte[] getBinaryByteArray(long columnIndex, long rowIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public long getLink(long columnIndex, long rowIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public void setLong(long columnIndex, long rowIndex, long value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IllegalStateException("No value can be set.");
    }

    @Override
    public void setBoolean(long columnIndex, long rowIndex, boolean value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IllegalStateException("No value can be set.");
    }

    @Override
    public void setFloat(long columnIndex, long rowIndex, float value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IllegalStateException("No value can be set.");
    }

    @Override
    public void setDouble(long columnIndex, long rowIndex, double value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IllegalStateException("No value can be set.");
    }

    @Override
    public void setString(long columnIndex, long rowIndex, String value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IllegalStateException("No string can be set.");
    }

    @Override
    public void setBinaryByteArray(long columnIndex, long rowIndex, byte[] data) {
        checkIfValidColumnIndex(columnIndex);
        throw new IllegalStateException("No data can be set.");
    }

    @Override
    public void setDate(long columnIndex, long rowIndex, Date date) {
        checkIfValidColumnIndex(columnIndex);
        throw new IllegalStateException("No date can be set.");
    }

    @Override
    public boolean isNullLink(long columnIndex, long rowIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public void nullifyLink(long columnIndex, long rowIndex) {
        checkIfValidColumnIndex(columnIndex);
    }

    @Override
    public void setLink(long columnIndex, long rowIndex, long value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IllegalStateException("No link to be pivoted.");
    }

    @Override
    public long sumLong(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public Long maximumLong(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public Long minimumLong(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public double averageLong(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public double sumFloat(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public Float maximumFloat(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public Float minimumFloat(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public double averageFloat(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public double sumDouble(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public Double maximumDouble(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public Double minimumDouble(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public double averageDouble(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public Date maximumDate(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public Date minimumDate(long columnIndex) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public long findFirstLong(long columnIndex, long value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public long findFirstBoolean(long columnIndex, boolean value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public long findFirstFloat(long columnIndex, float value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public long findFirstDouble(long columnIndex, double value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public long findFirstDate(long columnIndex, Date value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public long findFirstString(long columnIndex, String value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public long lowerBoundLong(long columnIndex, long value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public long upperBoundLong(long columnIndex, long value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public TableView findAllLong(long columnIndex, long value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public TableView findAllBoolean(long columnIndex, boolean value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public TableView findAllFloat(long columnIndex, float value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public TableView findAllDouble(long columnIndex, double value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public TableView findAllString(long columnIndex, String value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    /**
     * According to <a href=http://www.ietf.org/rfc/rfc4627.txt>RFC4627</a> and
     * <a href=http://www.ecma-international.org/ecma-262/5.1/>ECMA-262</a> all types
     * of JSON values (string, number, and null) be accepted as a valid JSON object.
     * Thus, {@code null} is used as a minimal JSON value string.
     */
    @Override
    public String toJson() {
        return null;
    }

    @Override
    public String toString() {
        return "The TableView is empty";
    }

    @Override
    public TableQuery where() {
        throw new IllegalStateException("No query to be made.");
    }

    @Override
    public long sourceRowIndex(long rowIndex) {
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public long count(long columnIndex, String value) {
        checkIfValidColumnIndex(columnIndex);
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public long getVersion() {
        return 0;
    }

    @Override
    public void removeFirst() {
        throw new IndexOutOfBoundsException("No result to be found.");
    }

    @Override
    public Table pivot(long stringCol, long intCol, PivotType pivotType) {
        if (! this.getColumnType(stringCol).equals(RealmFieldType.STRING ))
            throw new UnsupportedOperationException("Group by column must be of type String");
        if (! this.getColumnType(intCol).equals(RealmFieldType.INTEGER ))
            throw new UnsupportedOperationException("Aggregation column must be of type Int");
        throw new IllegalStateException("No result to be pivoted.");
    }

    @Override
    public long syncIfNeeded() {
        return 0;
    }

    @Override
    public boolean isAttached() {
        return false;
    }
}
