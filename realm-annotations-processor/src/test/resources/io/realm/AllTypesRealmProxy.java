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
        realm.assertThread();
        return (java.lang.String) row.getString(Realm.columnIndices.get("AllTypes").get("columnString"));
    }

    @Override
    public void setColumnString(String value) {
        realm.assertThread();
        row.setString(Realm.columnIndices.get("AllTypes").get("columnString"), (String) value);
    }

    @Override
    public long getColumnLong() {
        realm.assertThread();
        return (long) row.getLong(Realm.columnIndices.get("AllTypes").get("columnLong"));
    }

    @Override
    public void setColumnLong(long value) {
        realm.assertThread();
        row.setLong(Realm.columnIndices.get("AllTypes").get("columnLong"), (long) value);
    }

    @Override
    public float getColumnFloat() {
        realm.assertThread();
        return (float) row.getFloat(Realm.columnIndices.get("AllTypes").get("columnFloat"));
    }

    @Override
    public void setColumnFloat(float value) {
        realm.assertThread();
        row.setFloat(Realm.columnIndices.get("AllTypes").get("columnFloat"), (float) value);
    }

    @Override
    public double getColumnDouble() {
        realm.assertThread();
        return (double) row.getDouble(Realm.columnIndices.get("AllTypes").get("columnDouble"));
    }

    @Override
    public void setColumnDouble(double value) {
        realm.assertThread();
        row.setDouble(Realm.columnIndices.get("AllTypes").get("columnDouble"), (double) value);
    }

    @Override
    public boolean isColumnBoolean() {
        realm.assertThread();
        return (boolean) row.getBoolean(Realm.columnIndices.get("AllTypes").get("columnBoolean"));
    }

    @Override
    public void setColumnBoolean(boolean value) {
        realm.assertThread();
        row.setBoolean(Realm.columnIndices.get("AllTypes").get("columnBoolean"), (boolean) value);
    }

    @Override
    public java.util.Date getColumnDate() {
        realm.assertThread();
        return (java.util.Date) row.getDate(Realm.columnIndices.get("AllTypes").get("columnDate"));
    }

    @Override
    public void setColumnDate(java.util.Date value) {
        realm.assertThread();
        row.setDate(Realm.columnIndices.get("AllTypes").get("columnDate"), (Date) value);
    }

    @Override
    public byte[] getColumnBinary() {
        realm.assertThread();
        return (byte[]) row.getBinaryByteArray(Realm.columnIndices.get("AllTypes").get("columnBinary"));
    }

    @Override
    public void setColumnBinary(byte[] value) {
        realm.assertThread();
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
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnLong:");
        stringBuilder.append(getColumnLong());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnFloat:");
        stringBuilder.append(getColumnFloat());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDouble:");
        stringBuilder.append(getColumnDouble());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBoolean:");
        stringBuilder.append(isColumnBoolean());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDate:");
        stringBuilder.append(getColumnDate());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBinary:");
        stringBuilder.append(getColumnBinary());
        stringBuilder.append("}");
        stringBuilder.append("]");
        return stringBuilder.toString();
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllTypesRealmProxy aAllTypes = (AllTypesRealmProxy)o;

        String path = realm.getPath();
        String otherPath = aAllTypes.realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = row.getTable().getName();
        String otherTableName = aAllTypes.row.getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        return true;
    }

}