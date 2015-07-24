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
    private static long INDEX_FIELDBOOLEANNOTNULL;
    private static long INDEX_FIELDBOOLEANNULL;
    private static Map<String, Long> columnIndices;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("fieldStringNotNull");
        fieldNames.add("fieldStringNull");
        fieldNames.add("fieldBooleanNotNull");
        fieldNames.add("fieldBooleanNull");
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
        // FIXME: Check if we should enable this.
        // if (value == null) {
        //    throw new IllegalArgumentException("booleanNotNull is not nullable.");
        //}
        row.setString(INDEX_FIELDSTRINGNULL, (String) value);
    }

    @Override
    public Boolean getFieldBooleanNotNull() {
        realm.checkIfValid();
        return (java.lang.Boolean) row.getBoolean(INDEX_FIELDBOOLEANNOTNULL);
    }

    @Override
    public void setFieldBooleanNotNull(Boolean value) {
        realm.checkIfValid();
        row.setBoolean(INDEX_FIELDBOOLEANNOTNULL, (Boolean) value);
    }

    @Override
    public Boolean getFieldBooleanNull() {
        realm.checkIfValid();
        if (row.isNull(INDEX_FIELDBOOLEANNULL)) {
            return null;
        }
        return (java.lang.Boolean) row.getBoolean(INDEX_FIELDBOOLEANNULL);
    }

    @Override
    public void setFieldBooleanNull(Boolean value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(INDEX_FIELDBOOLEANNULL);
            return;
        }
        row.setBoolean(INDEX_FIELDBOOLEANNULL, (Boolean) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if (!transaction.hasTable("class_NullTypes")) {
            Table table = transaction.getTable("class_NullTypes");
            table.addColumn(ColumnType.STRING, "fieldStringNotNull", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.STRING, "fieldStringNull", Table.NULLABLE);
            table.addColumn(ColumnType.BOOLEAN, "fieldBooleanNotNull", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.BOOLEAN, "fieldBooleanNull", Table.NULLABLE);
            table.setPrimaryKey("");
            return table;
        }
        return transaction.getTable("class_NullTypes");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if (transaction.hasTable("class_NullTypes")) {
            Table table = transaction.getTable("class_NullTypes");
            if (table.getColumnCount() != 4) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field count does not match - expected 4 but was " + table.getColumnCount());
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for (long i = 0; i < 4; i++) {
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
            INDEX_FIELDBOOLEANNOTNULL = table.getColumnIndex("fieldBooleanNotNull");
            INDEX_FIELDBOOLEANNULL = table.getColumnIndex("fieldBooleanNull");

            if (!columnTypes.containsKey("fieldStringNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldStringNotNull'");
            }
            if (columnTypes.get("fieldStringNotNull") != ColumnType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'fieldStringNotNull'");
            }
            if (table.isColumnNullable(INDEX_FIELDSTRINGNOTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Remove annotation @Required or @PrimaryKey from field 'fieldStringNotNull'");
            }
            if (!columnTypes.containsKey("fieldStringNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldStringNull'");
            }
            if (columnTypes.get("fieldStringNull") != ColumnType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'fieldStringNull'");
            }
            if (!table.isColumnNullable(INDEX_FIELDSTRINGNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Add annotation @Required or @PrimaryKey to field 'fieldStringNull'");
            }
            if (!columnTypes.containsKey("fieldBooleanNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldBooleanNotNull'");
            }
            if (columnTypes.get("fieldBooleanNotNull") != ColumnType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Boolean' for field 'fieldBooleanNotNull'");
            }
            if (table.isColumnNullable(INDEX_FIELDBOOLEANNOTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Remove annotation @Required or @PrimaryKey from field 'fieldBooleanNotNull'");
            }
            if (!columnTypes.containsKey("fieldBooleanNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldBooleanNull'");
            }
            if (columnTypes.get("fieldBooleanNull") != ColumnType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Boolean' for field 'fieldBooleanNull'");
            }
            if (!table.isColumnNullable(INDEX_FIELDBOOLEANNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Add annotation @Required or @PrimaryKey to field 'fieldBooleanNull'");
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
        if (json.has("fieldStringNotNull")) {
            if (json.isNull("fieldStringNotNull")) {
                obj.setFieldStringNotNull(null);
            } else {
                obj.setFieldStringNotNull((String) json.getString("fieldStringNotNull"));
            }
        }
        if (json.has("fieldStringNull")) {
            if (json.isNull("fieldStringNull")) {
                obj.setFieldStringNull(null);
            } else {
                obj.setFieldStringNull((String) json.getString("fieldStringNull"));
            }
        }
        if (!json.isNull("fieldBooleanNotNull")) {
            obj.setFieldBooleanNotNull((Boolean) json.getBoolean("fieldBooleanNotNull"));
        }
        if (!json.isNull("fieldBooleanNull")) {
            obj.setFieldBooleanNull((Boolean) json.getBoolean("fieldBooleanNull"));
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
            } else if (name.equals("fieldBooleanNotNull")  && reader.peek() != JsonToken.NULL) {
                obj.setFieldBooleanNotNull((Boolean) reader.nextBoolean());
            } else if (name.equals("fieldBooleanNull")  && reader.peek() != JsonToken.NULL) {
                obj.setFieldBooleanNull((Boolean) reader.nextBoolean());
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
        realmObject.setFieldBooleanNotNull(newObject.getFieldBooleanNotNull() !=null ? newObject.getFieldBooleanNotNull() : false);
        realmObject.setFieldBooleanNull(newObject.getFieldBooleanNull());
        return realmObject;
    }

    static NullTypes update(Realm realm, NullTypes realmObject, NullTypes newObject, Map<RealmObject, RealmObjectProxy> cache) {
        realmObject.setFieldStringNotNull(newObject.getFieldStringNotNull() != null ? newObject.getFieldStringNotNull() : "");
        realmObject.setFieldStringNull(newObject.getFieldStringNull());
        realmObject.setFieldBooleanNotNull(newObject.getFieldBooleanNotNull() != null ? newObject.getFieldBooleanNotNull() : false);
        realmObject.setFieldBooleanNull(newObject.getFieldBooleanNull());
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
        stringBuilder.append(getFieldStringNull() != null ? getFieldStringNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBooleanNotNull:");
        stringBuilder.append(getFieldBooleanNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBooleanNull:");
        stringBuilder.append(getFieldBooleanNull() != null ? getFieldBooleanNull() : "null");
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
