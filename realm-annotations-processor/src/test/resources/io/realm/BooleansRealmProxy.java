package io.realm;


import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmFieldType;
import io.realm.RealmObject;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmMigrationNeededException;
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
import some.test.Booleans;

public class BooleansRealmProxy extends Booleans
        implements RealmObjectProxy {

    private static long INDEX_DONE;
    private static long INDEX_ISREADY;
    private static long INDEX_MCOMPLETED;
    private static long INDEX_ANOTHERBOOLEAN;
    private static Map<String, Long> columnIndices;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("done");
        fieldNames.add("isReady");
        fieldNames.add("mCompleted");
        fieldNames.add("anotherBoolean");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    @Override
    public boolean isDone() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(INDEX_DONE);
    }

    @Override
    public void setDone(boolean value) {
        realm.checkIfValid();
        row.setBoolean(INDEX_DONE, (boolean) value);
    }

    @Override
    public boolean isReady() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(INDEX_ISREADY);
    }

    @Override
    public void setReady(boolean value) {
        realm.checkIfValid();
        row.setBoolean(INDEX_ISREADY, (boolean) value);
    }

    @Override
    public boolean ismCompleted() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(INDEX_MCOMPLETED);
    }

    @Override
    public void setmCompleted(boolean value) {
        realm.checkIfValid();
        row.setBoolean(INDEX_MCOMPLETED, (boolean) value);
    }

    @Override
    public boolean getAnotherBoolean() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(INDEX_ANOTHERBOOLEAN);
    }

    @Override
    public void setAnotherBoolean(boolean value) {
        realm.checkIfValid();
        row.setBoolean(INDEX_ANOTHERBOOLEAN, (boolean) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if (!transaction.hasTable("class_Booleans")) {
            Table table = transaction.getTable("class_Booleans");
            table.addColumn(RealmFieldType.BOOLEAN, "done", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "isReady", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "mCompleted", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "anotherBoolean", Table.NOT_NULLABLE);
            table.setPrimaryKey("");
            return table;
        }
        return transaction.getTable("class_Booleans");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if (transaction.hasTable("class_Booleans")) {
            Table table = transaction.getTable("class_Booleans");
            if (table.getColumnCount() != 4) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field count does not match - expected 4 but was " + table.getColumnCount());
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < 4; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            columnIndices = new HashMap<String, Long>();
            for (String fieldName : getFieldNames()) {
                long index = table.getColumnIndex(fieldName);
                if (index == -1) {
                    throw new RealmMigrationNeededException(transaction.getPath(), "Field '" + fieldName + "' not found for type Booleans");
                }
                columnIndices.put(fieldName, index);
            }
            INDEX_DONE = table.getColumnIndex("done");
            INDEX_ISREADY = table.getColumnIndex("isReady");
            INDEX_MCOMPLETED = table.getColumnIndex("mCompleted");
            INDEX_ANOTHERBOOLEAN = table.getColumnIndex("anotherBoolean");

            if (!columnTypes.containsKey("done")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'done' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("done") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'done' in existing Realm file.");
            }
            if (table.isColumnNullable(INDEX_DONE)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'done' does support null values in the existing Realm file. Use corresponding boxed type for field 'done' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("isReady")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'isReady' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("isReady") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'isReady' in existing Realm file.");
            }
            if (table.isColumnNullable(INDEX_ISREADY)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'isReady' does support null values in the existing Realm file. Use corresponding boxed type for field 'isReady' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("mCompleted")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'mCompleted' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("mCompleted") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'mCompleted' in existing Realm file.");
            }
            if (table.isColumnNullable(INDEX_MCOMPLETED)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'mCompleted' does support null values in the existing Realm file. Use corresponding boxed type for field 'mCompleted' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("anotherBoolean")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'anotherBoolean' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("anotherBoolean") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'anotherBoolean' in existing Realm file.");
            }
            if (table.isColumnNullable(INDEX_ANOTHERBOOLEAN)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'anotherBoolean' does support null values in the existing Realm file. Use corresponding boxed type for field 'anotherBoolean' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
        } else {
            throw new RealmMigrationNeededException(transaction.getPath(), "The Booleans class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_Booleans";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    public static Map<String,Long> getColumnIndices() {
        return columnIndices;
    }

    public static Booleans createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        Booleans obj = realm.createObject(Booleans.class);
        if (json.has("done")) {
            if (json.isNull("done")) {
                throw new IllegalArgumentException("Trying to set non-nullable field done to null.");
            } else {
                obj.setDone((boolean) json.getBoolean("done"));
            }
        }
        if (json.has("isReady")) {
            if (json.isNull("isReady")) {
                throw new IllegalArgumentException("Trying to set non-nullable field isReady to null.");
            } else {
                obj.setReady((boolean) json.getBoolean("isReady"));
            }
        }
        if (json.has("mCompleted")) {
            if (json.isNull("mCompleted")) {
                throw new IllegalArgumentException("Trying to set non-nullable field mCompleted to null.");
            } else {
                obj.setmCompleted((boolean) json.getBoolean("mCompleted"));
            }
        }
        if (json.has("anotherBoolean")) {
            if (json.isNull("anotherBoolean")) {
                throw new IllegalArgumentException("Trying to set non-nullable field anotherBoolean to null.");
            } else {
                obj.setAnotherBoolean((boolean) json.getBoolean("anotherBoolean"));
            }
        }
        return obj;
    }

    public static Booleans createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        Booleans obj = realm.createObject(Booleans.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("done")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field done to null.");
                } else {
                    obj.setDone((boolean) reader.nextBoolean());
                }
            } else if (name.equals("isReady")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field isReady to null.");
                } else {
                    obj.setReady((boolean) reader.nextBoolean());
                }
            } else if (name.equals("mCompleted")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field mCompleted to null.");
                } else {
                    obj.setmCompleted((boolean) reader.nextBoolean());
                }
            } else if (name.equals("anotherBoolean")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field anotherBoolean to null.");
                } else {
                    obj.setAnotherBoolean((boolean) reader.nextBoolean());
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return obj;
    }

    public static Booleans copyOrUpdate(Realm realm, Booleans object, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        if (object.realm != null && object.realm.getPath().equals(realm.getPath())) {
            return object;
        }
        return copy(realm, object, update, cache);
    }

    public static Booleans copy(Realm realm, Booleans newObject, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        Booleans realmObject = realm.createObject(Booleans.class);
        cache.put(newObject, (RealmObjectProxy) realmObject);
        realmObject.setDone(newObject.isDone());
        realmObject.setReady(newObject.isReady());
        realmObject.setmCompleted(newObject.ismCompleted());
        realmObject.setAnotherBoolean(newObject.getAnotherBoolean());
        return realmObject;
    }

    static Booleans update(Realm realm, Booleans realmObject, Booleans newObject, Map<RealmObject, RealmObjectProxy> cache) {
        realmObject.setDone(newObject.isDone());
        realmObject.setReady(newObject.isReady());
        realmObject.setmCompleted(newObject.ismCompleted());
        realmObject.setAnotherBoolean(newObject.getAnotherBoolean());
        return realmObject;
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "Invalid object";
        }
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
        stringBuilder.append(",");
        stringBuilder.append("{anotherBoolean:");
        stringBuilder.append(getAnotherBoolean());
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
