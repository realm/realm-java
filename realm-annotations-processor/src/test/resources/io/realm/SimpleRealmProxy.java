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

public class SimpleRealmProxy extends Simple {

    @Override
    public String getName() {
        realm.assertThread();
        return (java.lang.String) row.getString(Realm.columnIndices.get("Simple").get("name"));
    }

    @Override
    public void setName(String value) {
        realm.assertThread();
        row.setString(Realm.columnIndices.get("Simple").get("name"), (String) value);
    }

    @Override
    public int getAge() {
        realm.assertThread();
        return (int) row.getLong(Realm.columnIndices.get("Simple").get("age"));
    }

    @Override
    public void setAge(int value) {
        realm.assertThread();
        row.setLong(Realm.columnIndices.get("Simple").get("age"), (long) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if (!transaction.hasTable("class_Simple")) {
            Table table = transaction.getTable("class_Simple");
            table.addColumn(ColumnType.STRING, "name");
            table.addColumn(ColumnType.INTEGER, "age");
            return table;
        }
        return transaction.getTable("class_Simple");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if (transaction.hasTable("class_Simple")) {
            Table table = transaction.getTable("class_Simple");
            if (table.getColumnCount() != 2) {
                throw new IllegalStateException("Column count does not match");
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for (long i = 0; i < 2; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }
            if (!columnTypes.containsKey("name")) {
                throw new IllegalStateException("Missing column 'name'");
            }
            if (columnTypes.get("name") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'name'");
            }
            if (!columnTypes.containsKey("age")) {
                throw new IllegalStateException("Missing column 'age'");
            }
            if (columnTypes.get("age") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'int' for column 'age'");
            }
        }
    }

    public static List<String> getFieldNames() {
        return Arrays.asList("name", "age");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Simple = [");
        stringBuilder.append("{name:");
        stringBuilder.append(getName());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{age:");
        stringBuilder.append(getAge());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{object_id:");
        stringBuilder.append(getObject_id());
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
        SimpleRealmProxy aSimple = (SimpleRealmProxy)o;

        String path = realm.getPath();
        String otherPath = aSimple.realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = row.getTable().getName();
        String otherTableName = aSimple.row.getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        return true;
    }
}
