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

public class SimpleRealmProxy extends some.test.Simple
        implements RealmObjectProxy, SimpleRealmProxyInterface {

    static final class SimpleColumnInfo extends ColumnInfo {

        public final long nameIndex;
        public final long ageIndex;

        SimpleColumnInfo(String path, Table table) {
            final Map<String, Long> indicesMap = new HashMap<String, Long>(2);
            this.nameIndex = getValidColumnIndex(path, table, "Simple", "name");
            indicesMap.put("name", this.nameIndex);

            this.ageIndex = getValidColumnIndex(path, table, "Simple", "age");
            indicesMap.put("age", this.ageIndex);

            setIndicesMap(indicesMap);
        }
    }

    private final SimpleColumnInfo columnInfo;
    private final ProxyState proxyState;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("name");
        fieldNames.add("age");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    SimpleRealmProxy(ColumnInfo columnInfo) {
        this.columnInfo = (SimpleColumnInfo) columnInfo;
        this.proxyState = new ProxyState(some.test.Simple.class, this);
    }

    @SuppressWarnings("cast")
    public String realmGet$name() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.nameIndex);
    }

    public void realmSet$name(String value) {
        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.nameIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.nameIndex, value);
    }

    @SuppressWarnings("cast")
    public int realmGet$age() {
        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.ageIndex);
    }

    public void realmSet$age(int value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.ageIndex, value);
    }

    public static Table initTable(SharedRealm sharedRealm) {
        if (!sharedRealm.hasTable("class_Simple")) {
            Table table = sharedRealm.getTable("class_Simple");
            table.addColumn(RealmFieldType.STRING, "name", Table.NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "age", Table.NOT_NULLABLE);
            table.setPrimaryKey("");
            return table;
        }
        return sharedRealm.getTable("class_Simple");
    }

    public static SimpleColumnInfo validateTable(SharedRealm sharedRealm) {
        if (sharedRealm.hasTable("class_Simple")) {
            Table table = sharedRealm.getTable("class_Simple");
            if (table.getColumnCount() != 2) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count does not match - expected 2 but was " + table.getColumnCount());
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < 2; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final SimpleColumnInfo columnInfo = new SimpleColumnInfo(sharedRealm.getPath(), table);

            if (!columnTypes.containsKey("name")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'name' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("name") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'name' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.nameIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'name' is required. Either set @Required to field 'name' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("age")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'age' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("age") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'int' for field 'age' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.ageIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'age' does support null values in the existing Realm file. Use corresponding boxed type for field 'age' or migrate using RealmObjectSchema.setNullable().");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(sharedRealm.getPath(), "The 'Simple' class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_Simple";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static some.test.Simple createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        some.test.Simple obj = realm.createObject(some.test.Simple.class);
        if (json.has("name")) {
            if (json.isNull("name")) {
                ((SimpleRealmProxyInterface) obj).realmSet$name(null);
            } else {
                ((SimpleRealmProxyInterface) obj).realmSet$name((String) json.getString("name"));
            }
        }
        if (json.has("age")) {
            if (json.isNull("age")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'age' to null.");
            } else {
                ((SimpleRealmProxyInterface) obj).realmSet$age((int) json.getInt("age"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.Simple createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        some.test.Simple obj = realm.createObject(some.test.Simple.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((SimpleRealmProxyInterface) obj).realmSet$name(null);
                } else {
                    ((SimpleRealmProxyInterface) obj).realmSet$name((String) reader.nextString());
                }
            } else if (name.equals("age")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'age' to null.");
                } else {
                    ((SimpleRealmProxyInterface) obj).realmSet$age((int) reader.nextInt());
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return obj;
    }

    public static some.test.Simple copyOrUpdate(Realm realm, some.test.Simple object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (some.test.Simple) cachedRealmObject;
        } else {
            return copy(realm, object, update, cache);
        }
    }

    public static some.test.Simple copy(Realm realm, some.test.Simple newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.Simple) cachedRealmObject;
        } else {
            some.test.Simple realmObject = realm.createObject(some.test.Simple.class);
            cache.put(newObject, (RealmObjectProxy) realmObject);
            ((SimpleRealmProxyInterface) realmObject).realmSet$name(((SimpleRealmProxyInterface) newObject).realmGet$name());
            ((SimpleRealmProxyInterface) realmObject).realmSet$age(((SimpleRealmProxyInterface) newObject).realmGet$age());
            return realmObject;
        }
    }

    public static long insert(Realm realm, some.test.Simple object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.Simple.class);
        long tableNativePtr = table.getNativeTablePointer();
        SimpleColumnInfo columnInfo = (SimpleColumnInfo) realm.schema.getColumnInfo(some.test.Simple.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        String realmGet$name = ((SimpleRealmProxyInterface)object).realmGet$name();
        if (realmGet$name != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.nameIndex, rowIndex, realmGet$name);
        }
        Table.nativeSetLong(tableNativePtr, columnInfo.ageIndex, rowIndex, ((SimpleRealmProxyInterface)object).realmGet$age());
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.Simple.class);
        long tableNativePtr = table.getNativeTablePointer();
        SimpleColumnInfo columnInfo = (SimpleColumnInfo) realm.schema.getColumnInfo(some.test.Simple.class);
        some.test.Simple object = null;
        while (objects.hasNext()) {
            object = (some.test.Simple) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                String realmGet$name = ((SimpleRealmProxyInterface)object).realmGet$name();
                if (realmGet$name != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.nameIndex, rowIndex, realmGet$name);
                }
                Table.nativeSetLong(tableNativePtr, columnInfo.ageIndex, rowIndex, ((SimpleRealmProxyInterface)object).realmGet$age());
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.Simple object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.Simple.class);
        long tableNativePtr = table.getNativeTablePointer();
        SimpleColumnInfo columnInfo = (SimpleColumnInfo) realm.schema.getColumnInfo(some.test.Simple.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        String realmGet$name = ((SimpleRealmProxyInterface)object).realmGet$name();
        if (realmGet$name != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.nameIndex, rowIndex, realmGet$name);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.nameIndex, rowIndex);
        }
        Table.nativeSetLong(tableNativePtr, columnInfo.ageIndex, rowIndex, ((SimpleRealmProxyInterface)object).realmGet$age());
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.Simple.class);
        long tableNativePtr = table.getNativeTablePointer();
        SimpleColumnInfo columnInfo = (SimpleColumnInfo) realm.schema.getColumnInfo(some.test.Simple.class);
        some.test.Simple object = null;
        while (objects.hasNext()) {
            object = (some.test.Simple) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                String realmGet$name = ((SimpleRealmProxyInterface)object).realmGet$name();
                if (realmGet$name != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.nameIndex, rowIndex, realmGet$name);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.nameIndex, rowIndex);
                }
                Table.nativeSetLong(tableNativePtr, columnInfo.ageIndex, rowIndex, ((SimpleRealmProxyInterface)object).realmGet$age());
            }
        }
    }

    public static some.test.Simple createDetachedCopy(some.test.Simple realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.Simple unmanagedObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.Simple)cachedObject.object;
            } else {
                unmanagedObject = (some.test.Simple)cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            unmanagedObject = new some.test.Simple();
            cache.put(realmObject, new RealmObjectProxy.CacheData(currentDepth, unmanagedObject));
        }
        ((SimpleRealmProxyInterface) unmanagedObject).realmSet$name(((SimpleRealmProxyInterface) realmObject).realmGet$name());
        ((SimpleRealmProxyInterface) unmanagedObject).realmSet$age(((SimpleRealmProxyInterface) realmObject).realmGet$age());
        return unmanagedObject;
    }

    @Override
    public ProxyState realmGet$proxyState() {
        return proxyState;
    }

}
