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

public class BooleansRealmProxy extends Booleans {

    @Override
    public boolean isDone() {
        realm.assertThread();
        return (boolean) row.getBoolean(Realm.columnIndices.get("Booleans").get("done"));
    }

    @Override
    public void setDone(boolean value) {
        realm.assertThread();
        row.setBoolean(Realm.columnIndices.get("Booleans").get("done"), (boolean) value);
    }

    @Override
    public boolean isReady() {
        realm.assertThread();
        return (boolean) row.getBoolean(Realm.columnIndices.get("Booleans").get("isReady"));
    }

    @Override
    public void setReady(boolean value) {
        realm.assertThread();
        row.setBoolean(Realm.columnIndices.get("Booleans").get("isReady"), (boolean) value);
    }

    @Override
    public boolean ismCompleted() {
        realm.assertThread();
        return (boolean) row.getBoolean(Realm.columnIndices.get("Booleans").get("mCompleted"));
    }

    @Override
    public void setmCompleted(boolean value) {
        realm.assertThread();
        row.setBoolean(Realm.columnIndices.get("Booleans").get("mCompleted"), (boolean) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if(!transaction.hasTable("class_Booleans")) {
            Table table = transaction.getTable("class_Booleans");
            table.addColumn(ColumnType.BOOLEAN, "done");
            table.addColumn(ColumnType.BOOLEAN, "isReady");
            table.addColumn(ColumnType.BOOLEAN, "mCompleted");
            return table;
        }
        return transaction.getTable("class_Booleans");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if(transaction.hasTable("class_Booleans")) {
            Table table = transaction.getTable("class_Booleans");
            if(table.getColumnCount() != 3) {
                throw new IllegalStateException("Column count does not match");
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for(long i = 0; i < 3; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }
            if (!columnTypes.containsKey("done")) {
                throw new IllegalStateException("Missing column 'done'");
            }
            if (columnTypes.get("done") != ColumnType.BOOLEAN) {
                throw new IllegalStateException("Invalid type 'boolean' for column 'done'");
            }
            if (!columnTypes.containsKey("isReady")) {
                throw new IllegalStateException("Missing column 'isReady'");
            }
            if (columnTypes.get("isReady") != ColumnType.BOOLEAN) {
                throw new IllegalStateException("Invalid type 'boolean' for column 'isReady'");
            }
            if (!columnTypes.containsKey("mCompleted")) {
                throw new IllegalStateException("Missing column 'mCompleted'");
            }
            if (columnTypes.get("mCompleted") != ColumnType.BOOLEAN) {
                throw new IllegalStateException("Invalid type 'boolean' for column 'mCompleted'");
            }
        }
    }

    public static List<String> getFieldNames() {
        return Arrays.asList("done", "isReady", "mCompleted");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Booleans = [");
        stringBuilder.append("{done:");
        stringBuilder.append(isDone());
        stringBuilder.append("} ");
        stringBuilder.append("{isReady:");
        stringBuilder.append(isReady());
        stringBuilder.append("} ");
        stringBuilder.append("{mCompleted:");
        stringBuilder.append(ismCompleted());
        stringBuilder.append("} ");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (isDone() ? 1 : 0);
        result = 31 * result + (isReady() ? 1 : 0);
        result = 31 * result + (ismCompleted() ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleansRealmProxy aBooleans = (BooleansRealmProxy)o;
        if (isDone() != aBooleans.isDone()) return false;
        if (isReady() != aBooleans.isReady()) return false;
        if (ismCompleted() != aBooleans.ismCompleted()) return false;
        return true;
    }

}
