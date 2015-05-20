package io.realm;


import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmObject;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.RealmObjectProxy;
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

public class NullTypesRealmProxy extends NullTypes implements RealmObjectProxy {

    private static long INDEX_FIELDSTRINGNOTNULL;
    private static long INDEX_FIELDSTRINGNULL;
    private static Map<String, Long> columnIndices;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("fieldStringNotNull");
        fieldNames.add("fieldStringNull");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    @Override
    public String getFieldStringNotNull() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(INDEX_FIELDSTRINGNOTNULL);
    }

    @Override
    public void setFieldStringNotNull(String value) {
        realm.checkIfValid();
        row.setString(INDEX_FIELDSTRINGNOTNULL, (String) value);
    }

    @Override
    public String getFieldStringNull() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(INDEX_FIELDSTRINGNULL);
    }

    @Override
    public void setFieldStringNull(String value) {
        realm.checkIfValid();
        row.setString(INDEX_FIELDSTRINGNULL, (String) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if (!transaction.hasTable("class_NullTypes")) {
            Table table = transaction.getTable("class_NullTypes");
            table.addColumn(ColumnType.STRING, "fieldStringNotNull", false);
            table.addColumn(ColumnType.STRING, "fieldStringNull", true);
            table.setPrimaryKey("");
            return table;
        }
        return transaction.getTable("class_NullTypes");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if (transaction.hasTable("class_NullTypes")) {
            Table table = transaction.getTable("class_NullTypes");
            if (table.getColumnCount() != 2) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field count does not match - expected 2 but was " + table.getColumnCount());
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for(long i = 0; i < 2; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            columnIndices = new HashMap<String, Long>();
            for (String fieldName : getFieldNames()) {
                long index = table.getColumnIndex(fieldName);
                if (index == -1) {
                    throw new RealmMigrationNeededException(transaction.getPath(), "Field '" + fieldName + "' not found for type NullTypes");
                }
                columnIndices.put(fieldName, index);
            }

            INDEX_FIELDSTRINGNOTNULL = table.getColumnIndex("fieldStringNotNull");
            INDEX_FIELDSTRINGNULL = table.getColumnIndex("fieldStringNull");

            if (!columnTypes.containsKey("fieldStringNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldStringNotNull'");
            }
            if (columnTypes.get("fieldStringNotNull") != ColumnType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'fieldStringNotNull'");
            }
            if (!columnTypes.containsKey("fieldStringNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldStringNull'");
            }
            if (columnTypes.get("fieldStringNull") != ColumnType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'fieldStringNull'");
            }
        } else {
            throw new RealmMigrationNeededException(transaction.getPath(), "The NullTypes class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() { return "class_NullTypes"; }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    public static Map<String,Long> getColumnIndices() {
        return columnIndices;
    }

    public static NullTypes createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        NullTypes obj = realm.createObject(NullTypes.class);
        if (!json.isNull("fieldStringNotNull")) {
            obj.setFieldStringNotNull((String) json.getString("fieldStringNotNull"));
        }
        if (!json.isNull("fieldStringNull")) {
            obj.setFieldStringNull((String) json.getString("fieldStringNull"));
        }
        return obj;
    }

    public static NullTypes createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        NullTypes obj = realm.createObject(NullTypes.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("fieldStringNotNull") && reader.peek() != JsonToken.NULL) {
                obj.setFieldStringNotNull((String) reader.nextString());
            } else if (name.equals("fieldStringNull") && reader.peek() != JsonToken.NULL) {
                obj.setFieldStringNull((String) reader.nextString());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return obj;
    }

    public static NullTypes copyOrUpdate(Realm realm, NullTypes object, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        if (object.realm != null && object.realm.getPath().equals(realm.getPath())) {
            return object;
        }
        return copy(realm, object, update, cache);
    }

    public static NullTypes copy(Realm realm, NullTypes newObject, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        NullTypes realmObject = realm.createObject(NullTypes.class);
        cache.put(newObject, (RealmObjectProxy) realmObject);
        realmObject.setFieldStringNotNull(newObject.getFieldStringNotNull() != null ? newObject.getFieldStringNotNull() : "");
        realmObject.setFieldStringNull(newObject.getFieldStringNull());
        return realmObject;
    }

    static NullTypes update(Realm realm, NullTypes realmObject, NullTypes newObject, Map<RealmObject, RealmObjectProxy> cache) {
        realmObject.setFieldStringNotNull(newObject.getFieldStringNotNull() != null ? newObject.getFieldStringNotNull() : "");
        realmObject.setFieldStringNull(newObject.getFieldStringNull());
        return realmObject;
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("NullTypes = [");
        stringBuilder.append("{fieldStringNotNull:");
        stringBuilder.append(getFieldStringNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldStringNull:");
        stringBuilder.append(getFieldStringNull());
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