package io.realm;


import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmFieldType;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.android.JsonUtils;
import java.io.IOException;
import java.util.ArrayList;
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
    implements RealmObjectProxy, BooleansRealmProxyInterface {

    static final class BooleansColumnInfo extends ColumnInfo {

        public final long doneIndex;
        public final long isReadyIndex;
        public final long mCompletedIndex;
        public final long anotherBooleanIndex;

        BooleansColumnInfo(String path, Table table) {
            final Map<String, Long> indicesMap = new HashMap<String, Long>(4);
            this.doneIndex = getValidColumnIndex(path, table, "Booleans", "done");
            indicesMap.put("done", this.doneIndex);

            this.isReadyIndex = getValidColumnIndex(path, table, "Booleans", "isReady");
            indicesMap.put("isReady", this.isReadyIndex);

            this.mCompletedIndex = getValidColumnIndex(path, table, "Booleans", "mCompleted");
            indicesMap.put("mCompleted", this.mCompletedIndex);

            this.anotherBooleanIndex = getValidColumnIndex(path, table, "Booleans", "anotherBoolean");
            indicesMap.put("anotherBoolean", this.anotherBooleanIndex);

            setIndicesMap(indicesMap);
        }
    }

    private final BooleansColumnInfo columnInfo;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("done");
        fieldNames.add("isReady");
        fieldNames.add("mCompleted");
        fieldNames.add("anotherBoolean");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    BooleansRealmProxy(ColumnInfo columnInfo) {
        this.columnInfo = (BooleansColumnInfo) columnInfo;
    }

    @SuppressWarnings("cast")
    public boolean realmGet$done() {
        ((RealmObject) this).realm.checkIfValid();
        return (boolean) ((RealmObject) this).row.getBoolean(columnInfo.doneIndex);
    }

    public void realmSet$done(boolean value) {
        ((RealmObject) this).realm.checkIfValid();
        ((RealmObject) this).row.setBoolean(columnInfo.doneIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$isReady() {
        ((RealmObject) this).realm.checkIfValid();
        return (boolean) ((RealmObject) this).row.getBoolean(columnInfo.isReadyIndex);
    }

    public void realmSet$isReady(boolean value) {
        ((RealmObject) this).realm.checkIfValid();
        ((RealmObject) this).row.setBoolean(columnInfo.isReadyIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$mCompleted() {
        ((RealmObject) this).realm.checkIfValid();
        return (boolean) ((RealmObject) this).row.getBoolean(columnInfo.mCompletedIndex);
    }

    public void realmSet$mCompleted(boolean value) {
        ((RealmObject) this).realm.checkIfValid();
        ((RealmObject) this).row.setBoolean(columnInfo.mCompletedIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$anotherBoolean() {
        ((RealmObject) this).realm.checkIfValid();
        return (boolean) ((RealmObject) this).row.getBoolean(columnInfo.anotherBooleanIndex);
    }

    public void realmSet$anotherBoolean(boolean value) {
        ((RealmObject) this).realm.checkIfValid();
        ((RealmObject) this).row.setBoolean(columnInfo.anotherBooleanIndex, value);
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

    public static BooleansColumnInfo validateTable(ImplicitTransaction transaction) {
        if (transaction.hasTable("class_Booleans")) {
            Table table = transaction.getTable("class_Booleans");
            if (table.getColumnCount() != 4) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field count does not match - expected 4 but was " + table.getColumnCount());
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < 4; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final BooleansColumnInfo columnInfo = new BooleansColumnInfo(transaction.getPath(), table);

            if (!columnTypes.containsKey("done")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'done' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("done") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'done' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.doneIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'done' does support null values in the existing Realm file. Use corresponding boxed type for field 'done' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("isReady")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'isReady' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("isReady") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'isReady' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.isReadyIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'isReady' does support null values in the existing Realm file. Use corresponding boxed type for field 'isReady' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("mCompleted")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'mCompleted' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("mCompleted") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'mCompleted' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.mCompletedIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'mCompleted' does support null values in the existing Realm file. Use corresponding boxed type for field 'mCompleted' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("anotherBoolean")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'anotherBoolean' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("anotherBoolean") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'anotherBoolean' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.anotherBooleanIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'anotherBoolean' does support null values in the existing Realm file. Use corresponding boxed type for field 'anotherBoolean' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            return columnInfo;
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

    @SuppressWarnings("cast")
    public static Booleans createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        Booleans obj = realm.createObject(Booleans.class);
        if (json.has("done")) {
            if (json.isNull("done")) {
                throw new IllegalArgumentException("Trying to set non-nullable field done to null.");
            } else {
                ((BooleansRealmProxyInterface) obj).realmSet$done((boolean) json.getBoolean("done"));
            }
        }
        if (json.has("isReady")) {
            if (json.isNull("isReady")) {
                throw new IllegalArgumentException("Trying to set non-nullable field isReady to null.");
            } else {
                ((BooleansRealmProxyInterface) obj).realmSet$isReady((boolean) json.getBoolean("isReady"));
            }
        }
        if (json.has("mCompleted")) {
            if (json.isNull("mCompleted")) {
                throw new IllegalArgumentException("Trying to set non-nullable field mCompleted to null.");
            } else {
                ((BooleansRealmProxyInterface) obj).realmSet$mCompleted((boolean) json.getBoolean("mCompleted"));
            }
        }
        if (json.has("anotherBoolean")) {
            if (json.isNull("anotherBoolean")) {
                throw new IllegalArgumentException("Trying to set non-nullable field anotherBoolean to null.");
            } else {
                ((BooleansRealmProxyInterface) obj).realmSet$anotherBoolean((boolean) json.getBoolean("anotherBoolean"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
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
                    ((BooleansRealmProxyInterface) obj).realmSet$done((boolean) reader.nextBoolean());
                }
            } else if (name.equals("isReady")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field isReady to null.");
                } else {
                    ((BooleansRealmProxyInterface) obj).realmSet$isReady((boolean) reader.nextBoolean());
                }
            } else if (name.equals("mCompleted")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field mCompleted to null.");
                } else {
                    ((BooleansRealmProxyInterface) obj).realmSet$mCompleted((boolean) reader.nextBoolean());
                }
            } else if (name.equals("anotherBoolean")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field anotherBoolean to null.");
                } else {
                    ((BooleansRealmProxyInterface) obj).realmSet$anotherBoolean((boolean) reader.nextBoolean());
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return obj;
    }

    public static Booleans copyOrUpdate(Realm realm, Booleans object, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        if (((RealmObject) object).realm != null && ((RealmObject) object).realm.getPath().equals(realm.getPath())) {
            return object;
        }
        return copy(realm, object, update, cache);
    }

    public static Booleans copy(Realm realm, Booleans newObject, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        Booleans realmObject = realm.createObject(Booleans.class);
        cache.put(newObject, (RealmObjectProxy) realmObject);
        ((BooleansRealmProxyInterface) realmObject).realmSet$done(((BooleansRealmProxyInterface) newObject).realmGet$done());
        ((BooleansRealmProxyInterface) realmObject).realmSet$isReady(((BooleansRealmProxyInterface) newObject).realmGet$isReady());
        ((BooleansRealmProxyInterface) realmObject).realmSet$mCompleted(((BooleansRealmProxyInterface) newObject).realmGet$mCompleted());
        ((BooleansRealmProxyInterface) realmObject).realmSet$anotherBoolean(((BooleansRealmProxyInterface) newObject).realmGet$anotherBoolean());
        return realmObject;
    }

    public static Booleans createDetachedCopy(Booleans realmObject, int currentDepth, int maxDepth, Map<RealmObject, CacheData<RealmObject>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<Booleans> cachedObject = (CacheData) cache.get(realmObject);
        Booleans standaloneObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return cachedObject.object;
            } else {
                standaloneObject = cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            standaloneObject = new Booleans();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmObject>(currentDepth, standaloneObject));
        }
        ((BooleansRealmProxyInterface) standaloneObject).realmSet$done(((BooleansRealmProxyInterface) realmObject).realmGet$done());
        ((BooleansRealmProxyInterface) standaloneObject).realmSet$isReady(((BooleansRealmProxyInterface) realmObject).realmGet$isReady());
        ((BooleansRealmProxyInterface) standaloneObject).realmSet$mCompleted(((BooleansRealmProxyInterface) realmObject).realmGet$mCompleted());
        ((BooleansRealmProxyInterface) standaloneObject).realmSet$anotherBoolean(((BooleansRealmProxyInterface) realmObject).realmGet$anotherBoolean());
        return standaloneObject;
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("Booleans = [");
        stringBuilder.append("{done:");
        stringBuilder.append(realmGet$done());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{isReady:");
        stringBuilder.append(realmGet$isReady());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{mCompleted:");
        stringBuilder.append(realmGet$mCompleted());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{anotherBoolean:");
        stringBuilder.append(realmGet$anotherBoolean());
        stringBuilder.append("}");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        String realmName = ((RealmObject) this).realm.getPath();
        String tableName = ((RealmObject) this).row.getTable().getName();
        long rowIndex = ((RealmObject) this).row.getIndex();

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

        String path = ((RealmObject) this).realm.getPath();
        String otherPath = ((RealmObject) aBooleans).realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = ((RealmObject) this).row.getTable().getName();
        String otherTableName = ((RealmObject) aBooleans).row.getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (((RealmObject) this).row.getIndex() != ((RealmObject) aBooleans).row.getIndex()) return false;

        return true;
    }

}
