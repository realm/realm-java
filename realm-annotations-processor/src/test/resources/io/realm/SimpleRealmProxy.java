package io.realm;


import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmObject;
import io.realm.exceptions.RealmException;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.android.JsonUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import some.test.Simple;

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

    public static Table initTable(ImplicitTransaction transaction) {
        if(!transaction.hasTable("class_Simple")) {
            Table table = transaction.getTable("class_Simple");
            table.addColumn(ColumnType.STRING, "name");
            table.addColumn(ColumnType.INTEGER, "age");
            table.setPrimaryKey("");
            return table;
        }
        return transaction.getTable("class_Simple");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if(transaction.hasTable("class_Simple")) {
            Table table = transaction.getTable("class_Simple");
            if(table.getColumnCount() != 2) {
                throw new IllegalStateException("Column count does not match");
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for(long i = 0; i < 2; i++) {
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

    public static void populateUsingJsonObject(Simple obj, JSONObject json)
            throws JSONException {
        boolean standalone = obj.realm == null;
        if (!json.isNull("name")) {
            obj.setName((String) json.getString("name"));
        }
        if (!json.isNull("age")) {
            obj.setAge((int) json.getInt("age"));
        }
    }

    public static void populateUsingJsonStream(Simple obj, JsonReader reader)
            throws IOException {
        boolean standalone = obj.realm == null;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name") && reader.peek() != JsonToken.NULL) {
                obj.setName((String) reader.nextString());
            } else if (name.equals("age")  && reader.peek() != JsonToken.NULL) {
                obj.setAge((int) reader.nextInt());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    public static Simple copyOrUpdate(Realm realm, Simple object, boolean update, Map<RealmObject, RealmObject> cache) {
        return copy(realm, object, update, cache);
    }

    public static Simple copy(Realm realm, Simple newObject, boolean update, Map<RealmObject, RealmObject> cache) {
        Simple realmObject = realm.createObject(Simple.class);
        cache.put(newObject, realmObject);
        realmObject.setName(newObject.getName() != null ? newObject.getName() : "");
        realmObject.setAge(newObject.getAge());
        return realmObject;
    }

    static Simple update(Realm realm, Simple realmObject, Simple newObject, Map<RealmObject, RealmObject> cache) {
        realmObject.setName(newObject.getName() != null ? newObject.getName() : "");
        realmObject.setAge(newObject.getAge());
        return realmObject;
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("Simple = [");
        stringBuilder.append("{name:");
        stringBuilder.append(getName());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{age:");
        stringBuilder.append(getAge());
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

        if (row.getIndex() != aSimple.row.getIndex()) return false;

        return true;
    }

}
