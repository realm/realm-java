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

package io.realm;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.Row;
import io.realm.internal.Table;
import java.util.*;
import some.test.*;

public class AllTypesRealmProxy extends AllTypes {

    @Override
    public String getColumnString() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(Realm.columnIndices.get("AllTypes").get("columnString"));
    }

    @Override
    public void setColumnString(String value) {
        realm.checkIfValid();
        row.setString(Realm.columnIndices.get("AllTypes").get("columnString"), (String) value);
    }

    @Override
    public long getColumnLong() {
        realm.checkIfValid();
        return (long) row.getLong(Realm.columnIndices.get("AllTypes").get("columnLong"));
    }

    @Override
    public void setColumnLong(long value) {
        realm.checkIfValid();
        row.setLong(Realm.columnIndices.get("AllTypes").get("columnLong"), (long) value);
    }

    @Override
    public float getColumnFloat() {
        realm.checkIfValid();
        return (float) row.getFloat(Realm.columnIndices.get("AllTypes").get("columnFloat"));
    }

    @Override
    public void setColumnFloat(float value) {
        realm.checkIfValid();
        row.setFloat(Realm.columnIndices.get("AllTypes").get("columnFloat"), (float) value);
    }

    @Override
    public double getColumnDouble() {
        realm.checkIfValid();
        return (double) row.getDouble(Realm.columnIndices.get("AllTypes").get("columnDouble"));
    }

    @Override
    public void setColumnDouble(double value) {
        realm.checkIfValid();
        row.setDouble(Realm.columnIndices.get("AllTypes").get("columnDouble"), (double) value);
    }

    @Override
    public boolean isColumnBoolean() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(Realm.columnIndices.get("AllTypes").get("columnBoolean"));
    }

    @Override
    public void setColumnBoolean(boolean value) {
        realm.checkIfValid();
        row.setBoolean(Realm.columnIndices.get("AllTypes").get("columnBoolean"), (boolean) value);
    }

    @Override
    public java.util.Date getColumnDate() {
        realm.checkIfValid();
        return (java.util.Date) row.getDate(Realm.columnIndices.get("AllTypes").get("columnDate"));
    }

    @Override
    public void setColumnDate(java.util.Date value) {
        realm.checkIfValid();
        row.setDate(Realm.columnIndices.get("AllTypes").get("columnDate"), (Date) value);
    }

    @Override
    public byte[] getColumnBinary() {
        realm.checkIfValid();
        return (byte[]) row.getBinaryByteArray(Realm.columnIndices.get("AllTypes").get("columnBinary"));
    }

    @Override
    public void setColumnBinary(byte[] value) {
        realm.checkIfValid();
        row.setBinaryByteArray(Realm.columnIndices.get("AllTypes").get("columnBinary"), (byte[]) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if(!transaction.hasTable("class_AllTypes")) {
            Table table = transaction.getTable("class_AllTypes");
            table.addColumn(ColumnType.STRING, "columnString");
            table.addColumn(ColumnType.INTEGER, "columnLong");
            table.addColumn(ColumnType.FLOAT, "columnFloat");
            table.addColumn(ColumnType.DOUBLE, "columnDouble");
            table.addColumn(ColumnType.BOOLEAN, "columnBoolean");
            table.addColumn(ColumnType.DATE, "columnDate");
            table.addColumn(ColumnType.BINARY, "columnBinary");
            return table;
        }
        return transaction.getTable("class_AllTypes");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if(transaction.hasTable("class_AllTypes")) {
            Table table = transaction.getTable("class_AllTypes");
            if(table.getColumnCount() != 7) {
                throw new IllegalStateException("Column count does not match");
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for(long i = 0; i < 7; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }
            if (!columnTypes.containsKey("columnString")) {
                throw new IllegalStateException("Missing column 'columnString'");
            }
            if (columnTypes.get("columnString") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'columnString'");
            }
            if (!columnTypes.containsKey("columnLong")) {
                throw new IllegalStateException("Missing column 'columnLong'");
            }
            if (columnTypes.get("columnLong") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'columnLong'");
            }
            if (!columnTypes.containsKey("columnFloat")) {
                throw new IllegalStateException("Missing column 'columnFloat'");
            }
            if (columnTypes.get("columnFloat") != ColumnType.FLOAT) {
                throw new IllegalStateException("Invalid type 'float' for column 'columnFloat'");
            }
            if (!columnTypes.containsKey("columnDouble")) {
                throw new IllegalStateException("Missing column 'columnDouble'");
            }
            if (columnTypes.get("columnDouble") != ColumnType.DOUBLE) {
                throw new IllegalStateException("Invalid type 'double' for column 'columnDouble'");
            }
            if (!columnTypes.containsKey("columnBoolean")) {
                throw new IllegalStateException("Missing column 'columnBoolean'");
            }
            if (columnTypes.get("columnBoolean") != ColumnType.BOOLEAN) {
                throw new IllegalStateException("Invalid type 'boolean' for column 'columnBoolean'");
            }
            if (!columnTypes.containsKey("columnDate")) {
                throw new IllegalStateException("Missing column 'columnDate'");
            }
            if (columnTypes.get("columnDate") != ColumnType.DATE) {
                throw new IllegalStateException("Invalid type 'Date' for column 'columnDate'");
            }
            if (!columnTypes.containsKey("columnBinary")) {
                throw new IllegalStateException("Missing column 'columnBinary'");
            }
            if (columnTypes.get("columnBinary") != ColumnType.BINARY) {
                throw new IllegalStateException("Invalid type 'byte[]' for column 'columnBinary'");
            }
        }
    }

    public static List<String> getFieldNames() {
        return Arrays.asList("columnString", "columnLong", "columnFloat", "columnDouble", "columnBoolean", "columnDate", "columnBinary");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("AllTypes = [");
        stringBuilder.append("{columnString:");
        stringBuilder.append(getColumnString());
        stringBuilder.append("} ");
        stringBuilder.append("{columnLong:");
        stringBuilder.append(getColumnLong());
        stringBuilder.append("} ");
        stringBuilder.append("{columnFloat:");
        stringBuilder.append(getColumnFloat());
        stringBuilder.append("} ");
        stringBuilder.append("{columnDouble:");
        stringBuilder.append(getColumnDouble());
        stringBuilder.append("} ");
        stringBuilder.append("{columnBoolean:");
        stringBuilder.append(isColumnBoolean());
        stringBuilder.append("} ");
        stringBuilder.append("{columnDate:");
        stringBuilder.append(getColumnDate());
        stringBuilder.append("} ");
        stringBuilder.append("{columnBinary:");
        stringBuilder.append(getColumnBinary());
        stringBuilder.append("} ");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        int result = 17;
        String aString_0 = getColumnString();
        result = 31 * result + (aString_0 != null ? aString_0.hashCode() : 0);
        long aLong_1 = getColumnLong();
        result = 31 * result + (int) (aLong_1 ^ (aLong_1 >>> 32));
        float aFloat_2 = getColumnFloat();
        result = 31 * result + (aFloat_2 != +0.0f ? Float.floatToIntBits(aFloat_2) : 0);
        long temp_3 = Double.doubleToLongBits(getColumnDouble());
        result = 31 * result + (int) (temp_3 ^ (temp_3 >>> 32));
        result = 31 * result + (isColumnBoolean() ? 1 : 0);
        java.util.Date temp_5 = getColumnDate();
        result = 31 * result + (temp_5 != null ? temp_5.hashCode() : 0);
        byte[] aByteArray_6 = getColumnBinary();
        result = 31 * result + (aByteArray_6 != null ? Arrays.hashCode(aByteArray_6) : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllTypesRealmProxy aAllTypes = (AllTypesRealmProxy)o;
        if (getColumnString() != null ? !getColumnString().equals(aAllTypes.getColumnString()) : aAllTypes.getColumnString() != null) return false;
        if (getColumnLong() != aAllTypes.getColumnLong()) return false;
        if (Float.compare(getColumnFloat(), aAllTypes.getColumnFloat()) != 0) return false;
        if (Double.compare(getColumnDouble(), aAllTypes.getColumnDouble()) != 0) return false;
        if (isColumnBoolean() != aAllTypes.isColumnBoolean()) return false;
        if (getColumnDate() != null ? !getColumnDate().equals(aAllTypes.getColumnDate()) : aAllTypes.getColumnDate() != null) return false;
        if (!Arrays.equals(getColumnBinary(), aAllTypes.getColumnBinary())) return false;
        return true;
    }

}
