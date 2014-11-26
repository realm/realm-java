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

public class SimpleRealmProxy extends Simple {

    @Override
    public String getName() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(Realm.columnIndices.get("Simple").get("name"));
    }

    @Override
    public void setName(String value) {
        realm.checkIfValid();
        row.setString(Realm.columnIndices.get("Simple").get("name"), (String) value);
    }

    @Override
    public int getAge() {
        realm.checkIfValid();
        return (int) row.getLong(Realm.columnIndices.get("Simple").get("age"));
    }

    @Override
    public void setAge(int value) {
        realm.checkIfValid();
        row.setLong(Realm.columnIndices.get("Simple").get("age"), (long) value);
    }

    @Override
    public int getObject_id() {
        realm.checkIfValid();
        return (int) row.getLong(Realm.columnIndices.get("Simple").get("object_id"));
    }

    @Override
    public void setObject_id(int value) {
        realm.checkIfValid();
        row.setLong(Realm.columnIndices.get("Simple").get("object_id"), (long) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if (!transaction.hasTable("class_Simple")) {
            Table table = transaction.getTable("class_Simple");
            table.addColumn(ColumnType.STRING, "name");
            table.addColumn(ColumnType.INTEGER, "age");
            table.addColumn(ColumnType.INTEGER, "object_id");
            return table;
        }
        return transaction.getTable("class_Simple");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if (transaction.hasTable("class_Simple")) {
            Table table = transaction.getTable("class_Simple");
            if (table.getColumnCount() != 3) {
                throw new IllegalStateException("Column count does not match");
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for (long i = 0; i < 3; i++) {
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
            if (!columnTypes.containsKey("object_id")) {
                throw new IllegalStateException("Missing column 'object_id'");
            }
            if (columnTypes.get("object_id") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'int' for column 'object_id'");
            }
        }
    }

    public static List<String> getFieldNames() {
        return Arrays.asList("name", "age", "object_id");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Simple = [");
        stringBuilder.append("{name:");
        stringBuilder.append(getName());
        stringBuilder.append("} ");
        stringBuilder.append("{age:");
        stringBuilder.append(getAge());
        stringBuilder.append("} ");
        stringBuilder.append("{object_id:");
        stringBuilder.append(getObject_id());
        stringBuilder.append("} ");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        int result = 17;
        String aString_0 = getName();
        result = 31 * result + (aString_0 != null ? aString_0.hashCode() : 0);
        result = 31 * result + getAge();
        result = 31 * result + getObject_id();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleRealmProxy aSimple = (SimpleRealmProxy)o;
        if (getName() != null ? !getName().equals(aSimple.getName()) : aSimple.getName() != null) return false;
        if (getAge() != aSimple.getAge()) return false;
        if (getObject_id() != aSimple.getObject_id()) return false;
        return true;
    }

}
