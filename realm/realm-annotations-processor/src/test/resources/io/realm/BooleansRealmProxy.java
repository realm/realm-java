package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmFieldType;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.LinkView;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.SharedRealm;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.android.JsonUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BooleansRealmProxy extends some.test.Booleans
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
    private final ProxyState proxyState;
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
        this.proxyState = new ProxyState(some.test.Booleans.class, this);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$done() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.doneIndex);
    }

    public void realmSet$done(boolean value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.doneIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$isReady() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.isReadyIndex);
    }

    public void realmSet$isReady(boolean value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.isReadyIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$mCompleted() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.mCompletedIndex);
    }

    public void realmSet$mCompleted(boolean value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.mCompletedIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$anotherBoolean() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.anotherBooleanIndex);
    }

    public void realmSet$anotherBoolean(boolean value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.anotherBooleanIndex, value);
    }

    public static Table initTable(SharedRealm sharedRealm) {
        if (!sharedRealm.hasTable("class_Booleans")) {
            Table table = sharedRealm.getTable("class_Booleans");
            table.addColumn(RealmFieldType.BOOLEAN, "done", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "isReady", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "mCompleted", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "anotherBoolean", Table.NOT_NULLABLE);
            table.setPrimaryKey("");
            return table;
        }
        return sharedRealm.getTable("class_Booleans");
    }

    public static BooleansColumnInfo validateTable(SharedRealm sharedRealm) {
        if (sharedRealm.hasTable("class_Booleans")) {
            Table table = sharedRealm.getTable("class_Booleans");
            if (table.getColumnCount() != 4) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count does not match - expected 4 but was " + table.getColumnCount());
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < 4; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final BooleansColumnInfo columnInfo = new BooleansColumnInfo(sharedRealm.getPath(), table);

            if (!columnTypes.containsKey("done")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'done' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("done") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'boolean' for field 'done' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.doneIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'done' does support null values in the existing Realm file. Use corresponding boxed type for field 'done' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("isReady")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'isReady' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("isReady") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'boolean' for field 'isReady' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.isReadyIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'isReady' does support null values in the existing Realm file. Use corresponding boxed type for field 'isReady' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("mCompleted")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'mCompleted' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("mCompleted") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'boolean' for field 'mCompleted' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.mCompletedIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'mCompleted' does support null values in the existing Realm file. Use corresponding boxed type for field 'mCompleted' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("anotherBoolean")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'anotherBoolean' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("anotherBoolean") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'boolean' for field 'anotherBoolean' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.anotherBooleanIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'anotherBoolean' does support null values in the existing Realm file. Use corresponding boxed type for field 'anotherBoolean' or migrate using RealmObjectSchema.setNullable().");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(sharedRealm.getPath(), "The 'Booleans' class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_Booleans";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static some.test.Booleans createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        some.test.Booleans obj = realm.createObject(some.test.Booleans.class);
        if (json.has("done")) {
            if (json.isNull("done")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'done' to null.");
            } else {
                ((BooleansRealmProxyInterface) obj).realmSet$done((boolean) json.getBoolean("done"));
            }
        }
        if (json.has("isReady")) {
            if (json.isNull("isReady")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'isReady' to null.");
            } else {
                ((BooleansRealmProxyInterface) obj).realmSet$isReady((boolean) json.getBoolean("isReady"));
            }
        }
        if (json.has("mCompleted")) {
            if (json.isNull("mCompleted")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'mCompleted' to null.");
            } else {
                ((BooleansRealmProxyInterface) obj).realmSet$mCompleted((boolean) json.getBoolean("mCompleted"));
            }
        }
        if (json.has("anotherBoolean")) {
            if (json.isNull("anotherBoolean")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'anotherBoolean' to null.");
            } else {
                ((BooleansRealmProxyInterface) obj).realmSet$anotherBoolean((boolean) json.getBoolean("anotherBoolean"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.Booleans createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        some.test.Booleans obj = realm.createObject(some.test.Booleans.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("done")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'done' to null.");
                } else {
                    ((BooleansRealmProxyInterface) obj).realmSet$done((boolean) reader.nextBoolean());
                }
            } else if (name.equals("isReady")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'isReady' to null.");
                } else {
                    ((BooleansRealmProxyInterface) obj).realmSet$isReady((boolean) reader.nextBoolean());
                }
            } else if (name.equals("mCompleted")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'mCompleted' to null.");
                } else {
                    ((BooleansRealmProxyInterface) obj).realmSet$mCompleted((boolean) reader.nextBoolean());
                }
            } else if (name.equals("anotherBoolean")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'anotherBoolean' to null.");
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

    public static some.test.Booleans copyOrUpdate(Realm realm, some.test.Booleans object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (some.test.Booleans) cachedRealmObject;
        } else {
            return copy(realm, object, update, cache);
        }
    }

    public static some.test.Booleans copy(Realm realm, some.test.Booleans newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.Booleans) cachedRealmObject;
        } else {
            some.test.Booleans realmObject = realm.createObject(some.test.Booleans.class);
            cache.put(newObject, (RealmObjectProxy) realmObject);
            ((BooleansRealmProxyInterface) realmObject).realmSet$done(((BooleansRealmProxyInterface) newObject).realmGet$done());
            ((BooleansRealmProxyInterface) realmObject).realmSet$isReady(((BooleansRealmProxyInterface) newObject).realmGet$isReady());
            ((BooleansRealmProxyInterface) realmObject).realmSet$mCompleted(((BooleansRealmProxyInterface) newObject).realmGet$mCompleted());
            ((BooleansRealmProxyInterface) realmObject).realmSet$anotherBoolean(((BooleansRealmProxyInterface) newObject).realmGet$anotherBoolean());
            return realmObject;
        }
    }

    public static long insert(Realm realm, some.test.Booleans object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.Booleans.class);
        long tableNativePtr = table.getNativeTablePointer();
        BooleansColumnInfo columnInfo = (BooleansColumnInfo) realm.schema.getColumnInfo(some.test.Booleans.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.doneIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$done());
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isReadyIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$isReady());
        Table.nativeSetBoolean(tableNativePtr, columnInfo.mCompletedIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$mCompleted());
        Table.nativeSetBoolean(tableNativePtr, columnInfo.anotherBooleanIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$anotherBoolean());
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.Booleans.class);
        long tableNativePtr = table.getNativeTablePointer();
        BooleansColumnInfo columnInfo = (BooleansColumnInfo) realm.schema.getColumnInfo(some.test.Booleans.class);
        some.test.Booleans object = null;
        while (objects.hasNext()) {
            object = (some.test.Booleans) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.doneIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$done());
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isReadyIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$isReady());
                Table.nativeSetBoolean(tableNativePtr, columnInfo.mCompletedIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$mCompleted());
                Table.nativeSetBoolean(tableNativePtr, columnInfo.anotherBooleanIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$anotherBoolean());
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.Booleans object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.Booleans.class);
        long tableNativePtr = table.getNativeTablePointer();
        BooleansColumnInfo columnInfo = (BooleansColumnInfo) realm.schema.getColumnInfo(some.test.Booleans.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.doneIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$done());
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isReadyIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$isReady());
        Table.nativeSetBoolean(tableNativePtr, columnInfo.mCompletedIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$mCompleted());
        Table.nativeSetBoolean(tableNativePtr, columnInfo.anotherBooleanIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$anotherBoolean());
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.Booleans.class);
        long tableNativePtr = table.getNativeTablePointer();
        BooleansColumnInfo columnInfo = (BooleansColumnInfo) realm.schema.getColumnInfo(some.test.Booleans.class);
        some.test.Booleans object = null;
        while (objects.hasNext()) {
            object = (some.test.Booleans) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.doneIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$done());
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isReadyIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$isReady());
                Table.nativeSetBoolean(tableNativePtr, columnInfo.mCompletedIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$mCompleted());
                Table.nativeSetBoolean(tableNativePtr, columnInfo.anotherBooleanIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$anotherBoolean());
            }
        }
    }

    public static some.test.Booleans createDetachedCopy(some.test.Booleans realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.Booleans unmanagedObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.Booleans)cachedObject.object;
            } else {
                unmanagedObject = (some.test.Booleans)cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            unmanagedObject = new some.test.Booleans();
            cache.put(realmObject, new RealmObjectProxy.CacheData(currentDepth, unmanagedObject));
        }
        ((BooleansRealmProxyInterface) unmanagedObject).realmSet$done(((BooleansRealmProxyInterface) realmObject).realmGet$done());
        ((BooleansRealmProxyInterface) unmanagedObject).realmSet$isReady(((BooleansRealmProxyInterface) realmObject).realmGet$isReady());
        ((BooleansRealmProxyInterface) unmanagedObject).realmSet$mCompleted(((BooleansRealmProxyInterface) realmObject).realmGet$mCompleted());
        ((BooleansRealmProxyInterface) unmanagedObject).realmSet$anotherBoolean(((BooleansRealmProxyInterface) realmObject).realmGet$anotherBoolean());
        return unmanagedObject;
    }

    @Override
    public String toString() {
        if (!RealmObject.isValid(this)) {
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
    public ProxyState realmGet$proxyState() {
        return proxyState;
    }

    @Override
    public int hashCode() {
        String realmName = proxyState.getRealm$realm().getPath();
        String tableName = proxyState.getRow$realm().getTable().getName();
        long rowIndex = proxyState.getRow$realm().getIndex();

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

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aBooleans.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aBooleans.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aBooleans.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }

}
