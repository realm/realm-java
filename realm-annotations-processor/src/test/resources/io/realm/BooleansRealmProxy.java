package io.realm;

import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmObject;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.Table;
import io.realm.internal.android.JsonUtils;
import java.io.IOException;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import some.test.*;

public class BooleansRealmProxy extends Booleans {

    @Override
    public boolean isDone() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(Realm.columnIndices.get("Booleans").get("done"));
    }

    @Override
    public void setDone(boolean value) {
        realm.checkIfValid();
        row.setBoolean(Realm.columnIndices.get("Booleans").get("done"), (boolean) value);
    }

    @Override
    public boolean isReady() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(Realm.columnIndices.get("Booleans").get("isReady"));
    }

    @Override
    public void setReady(boolean value) {
        realm.checkIfValid();
        row.setBoolean(Realm.columnIndices.get("Booleans").get("isReady"), (boolean) value);
    }

    @Override
    public boolean ismCompleted() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(Realm.columnIndices.get("Booleans").get("mCompleted"));
    }

    @Override
    public void setmCompleted(boolean value) {
        realm.checkIfValid();
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

    void populateUsingJsonObject(JSONObject json)
            throws JSONException {
        if (json.has("done")) {
            setDone((boolean) json.getBoolean("done"));
        }
        if (json.has("isReady")) {
            setReady((boolean) json.getBoolean("isReady"));
        }
        if (json.has("mCompleted")) {
            setmCompleted((boolean) json.getBoolean("mCompleted"));
        }
    }

    void populateUsingJsonStream(JsonReader reader)
            throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("done") && reader.peek() != JsonToken.NULL) {
                setDone((boolean) reader.nextBoolean());
            } else if (name.equals("isReady")  && reader.peek() != JsonToken.NULL) {
                setReady((boolean) reader.nextBoolean());
            } else if (name.equals("mCompleted")  && reader.peek() != JsonToken.NULL) {
                setmCompleted((boolean) reader.nextBoolean());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Booleans = [");
        stringBuilder.append("{done:");
        stringBuilder.append(isDone());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{isReady:");
        stringBuilder.append(isReady());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{mCompleted:");
        stringBuilder.append(ismCompleted());
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
        BooleansRealmProxy aBooleans = (BooleansRealmProxy)o;

        String path = realm.getPath();
        String otherPath = aBooleans.realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = row.getTable().getName();
        String otherTableName = aBooleans.row.getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (row.getIndex() != aBooleans.row.getIndex()) return false;

        return true;
    }

}
