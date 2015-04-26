package io.realm;


import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmObject;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.android.JsonUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import some.test.NullTypes;

public class NullTypesRealmProxy extends NullTypes {
    private static long INDEX_FIELDSTRING;
    private static Map<String, Long> columnIndices;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("fieldString");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    @Override
    public String getFieldString() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(INDEX_FIELDSTRING);
    }

    @Override
    public void setFieldString(String value) {
        realm.checkIfValid();
        row.setString(INDEX_FIELDSTRING, (String) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if (!transaction.hasTable("class_NullTypes")) {
            Table table = transaction.getTable("class_NullTypes");
            table.addColumn(ColumnType.STRING, "fieldString", true);
            table.setPrimaryKey("");
            return table;
        }
        return transaction.getTable("class_NullTypes");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if(transaction.hasTable("class_NullTypes")) {
            Table table = transaction.getTable("class_NullTypes");
            if(table.getColumnCount() != 1) {
                throw new IllegalStateException("Column count does not match");
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for(long i = 0; i < 1; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }
            if (!columnTypes.containsKey("fieldString")) {
                throw new IllegalStateException("Missing column 'fieldString'");
            }
            if (columnTypes.get("fieldString") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'fieldString'");
            }
            columnIndices = new HashMap<String, Long>();
            for (String fieldName : getFieldNames()) {
                long index = table.getColumnIndex(fieldName);
                if (index == -1) {
                    throw new RealmMigrationNeededException("Field '" + fieldName + "' not found for type NullTypes");
                }
                columnIndices.put(fieldName, index);
            }
            INDEX_FIELDSTRING = table.getColumnIndex("fieldString");
        } else {
            throw new RealmMigrationNeededException("The NullTypes class is missing from the schema for this Realm.");
        }
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    public static Map<String,Long> getColumnIndices() {
        return columnIndices;
    }

    public static NullTypes createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        NullTypes obj = realm.createObject(NullTypes.class);
        if (!json.isNull("fieldString")) {
            obj.setFieldString((String) json.getString("fieldString"));
        }
        return obj;
    }

    public static NullTypes createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        NullTypes obj = realm.createObject(NullTypes.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("fieldString") && reader.peek() != JsonToken.NULL) {
                obj.setFieldString((String) reader.nextString());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return obj;
    }

    public static NullTypes copyOrUpdate(Realm realm, NullTypes object, boolean update, Map<RealmObject,RealmObject> cache) {
        if (object.realm != null && object.realm.getId() == realm.getId()) {
            return object;
        }
        return copy(realm, object, update, cache);
    }

    public static NullTypes copy(Realm realm, NullTypes newObject, boolean update, Map<RealmObject,RealmObject> cache) {
        NullTypes realmObject = realm.createObject(NullTypes.class);
        cache.put(newObject, realmObject);
        realmObject.setFieldString(newObject.getFieldString() != null ? newObject.getFieldString() : "");
        return realmObject;
    }

    static NullTypes update(Realm realm, NullTypes realmObject, NullTypes newObject, Map<RealmObject, RealmObject> cache) {
        realmObject.setFieldString(newObject.getFieldString() != null ? newObject.getFieldString() : "");
        return realmObject;
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("NullTypes = [");
        stringBuilder.append("{fieldString:");
        stringBuilder.append(getFieldString());
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
        NullTypesRealmProxy aNullTypes = (NullTypesRealmProxy)o;

        String path = realm.getPath();
        String otherPath = aNullTypes.realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = row.getTable().getName();
        String otherTableName = aNullTypes.row.getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (row.getIndex() != aNullTypes.row.getIndex()) return false;

        return true;
    }

}