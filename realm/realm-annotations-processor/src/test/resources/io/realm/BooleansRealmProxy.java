package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.LinkView;
import io.realm.internal.OsObject;
import io.realm.internal.OsObjectSchemaInfo;
import io.realm.internal.Property;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.SharedRealm;
import io.realm.internal.Table;
import io.realm.internal.android.JsonUtils;
import io.realm.log.RealmLog;
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
        long doneIndex;
        long isReadyIndex;
        long mCompletedIndex;
        long anotherBooleanIndex;

        BooleansColumnInfo(SharedRealm realm, Table table) {
            super(4);
            this.doneIndex = addColumnDetails(table, "done", RealmFieldType.BOOLEAN);
            this.isReadyIndex = addColumnDetails(table, "isReady", RealmFieldType.BOOLEAN);
            this.mCompletedIndex = addColumnDetails(table, "mCompleted", RealmFieldType.BOOLEAN);
            this.anotherBooleanIndex = addColumnDetails(table, "anotherBoolean", RealmFieldType.BOOLEAN);
        }

        BooleansColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new BooleansColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final BooleansColumnInfo src = (BooleansColumnInfo) rawSrc;
            final BooleansColumnInfo dst = (BooleansColumnInfo) rawDst;
            dst.doneIndex = src.doneIndex;
            dst.isReadyIndex = src.isReadyIndex;
            dst.mCompletedIndex = src.mCompletedIndex;
            dst.anotherBooleanIndex = src.anotherBooleanIndex;
        }
    }

    private BooleansColumnInfo columnInfo;
    private ProxyState<some.test.Booleans> proxyState;
    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("done");
        fieldNames.add("isReady");
        fieldNames.add("mCompleted");
        fieldNames.add("anotherBoolean");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    BooleansRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (BooleansColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<some.test.Booleans>(this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @Override
    @SuppressWarnings("cast")
    public boolean realmGet$done() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.doneIndex);
    }

    @Override
    public void realmSet$done(boolean value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.doneIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.doneIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public boolean realmGet$isReady() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.isReadyIndex);
    }

    @Override
    public void realmSet$isReady(boolean value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.isReadyIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.isReadyIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public boolean realmGet$mCompleted() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.mCompletedIndex);
    }

    @Override
    public void realmSet$mCompleted(boolean value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.mCompletedIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.mCompletedIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public boolean realmGet$anotherBoolean() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.anotherBooleanIndex);
    }

    @Override
    public void realmSet$anotherBoolean(boolean value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.anotherBooleanIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.anotherBooleanIndex, value);
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo info = new OsObjectSchemaInfo("Booleans");;
        info.add("done", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        info.add("isReady", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        info.add("mCompleted", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        info.add("anotherBoolean", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        return info;
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static BooleansColumnInfo validateTable(SharedRealm sharedRealm, boolean allowExtraColumns) {
        if (!sharedRealm.hasTable("class_Booleans")) {
            throw new RealmMigrationNeededException(sharedRealm.getPath(), "The 'Booleans' class is missing from the schema for this Realm.");
        }
        Table table = sharedRealm.getTable("class_Booleans");
        final long columnCount = table.getColumnCount();
        if (columnCount != 4) {
            if (columnCount < 4) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is less than expected - expected 4 but was " + columnCount);
            }
            if (allowExtraColumns) {
                RealmLog.debug("Field count is more than expected - expected 4 but was %1$d", columnCount);
            } else {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is more than expected - expected 4 but was " + columnCount);
            }
        }
        Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
        for (long i = 0; i < columnCount; i++) {
            columnTypes.put(table.getColumnName(i), table.getColumnType(i));
        }

        final BooleansColumnInfo columnInfo = new BooleansColumnInfo(sharedRealm, table);

        if (table.hasPrimaryKey()) {
            throw new RealmMigrationNeededException(sharedRealm.getPath(), "Primary Key defined for field " + table.getColumnName(table.getPrimaryKey()) + " was removed.");
        }

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
        final List<String> excludeFields = Collections.<String> emptyList();
        some.test.Booleans obj = realm.createObjectInternal(some.test.Booleans.class, true, excludeFields);
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
        some.test.Booleans obj = new some.test.Booleans();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (false) {
            } else if (name.equals("done")) {
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
        obj = realm.copyToRealm(obj);
        return obj;
    }

    public static some.test.Booleans copyOrUpdate(Realm realm, some.test.Booleans object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
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
            // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
            some.test.Booleans realmObject = realm.createObjectInternal(some.test.Booleans.class, false, Collections.<String>emptyList());
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
        long tableNativePtr = table.getNativePtr();
        BooleansColumnInfo columnInfo = (BooleansColumnInfo) realm.schema.getColumnInfo(some.test.Booleans.class);
        long rowIndex = OsObject.createRow(realm.sharedRealm, table);
        cache.put(object, rowIndex);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.doneIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$done(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isReadyIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$isReady(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.mCompletedIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$mCompleted(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.anotherBooleanIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$anotherBoolean(), false);
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.Booleans.class);
        long tableNativePtr = table.getNativePtr();
        BooleansColumnInfo columnInfo = (BooleansColumnInfo) realm.schema.getColumnInfo(some.test.Booleans.class);
        some.test.Booleans object = null;
        while (objects.hasNext()) {
            object = (some.test.Booleans) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = OsObject.createRow(realm.sharedRealm, table);
                cache.put(object, rowIndex);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.doneIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$done(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isReadyIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$isReady(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.mCompletedIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$mCompleted(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.anotherBooleanIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$anotherBoolean(), false);
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.Booleans object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.Booleans.class);
        long tableNativePtr = table.getNativePtr();
        BooleansColumnInfo columnInfo = (BooleansColumnInfo) realm.schema.getColumnInfo(some.test.Booleans.class);
        long rowIndex = OsObject.createRow(realm.sharedRealm, table);
        cache.put(object, rowIndex);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.doneIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$done(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isReadyIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$isReady(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.mCompletedIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$mCompleted(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.anotherBooleanIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$anotherBoolean(), false);
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.Booleans.class);
        long tableNativePtr = table.getNativePtr();
        BooleansColumnInfo columnInfo = (BooleansColumnInfo) realm.schema.getColumnInfo(some.test.Booleans.class);
        some.test.Booleans object = null;
        while (objects.hasNext()) {
            object = (some.test.Booleans) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = OsObject.createRow(realm.sharedRealm, table);
                cache.put(object, rowIndex);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.doneIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$done(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.isReadyIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$isReady(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.mCompletedIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$mCompleted(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.anotherBooleanIndex, rowIndex, ((BooleansRealmProxyInterface)object).realmGet$anotherBoolean(), false);
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
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        }
        ((BooleansRealmProxyInterface) unmanagedObject).realmSet$done(((BooleansRealmProxyInterface) realmObject).realmGet$done());
        ((BooleansRealmProxyInterface) unmanagedObject).realmSet$isReady(((BooleansRealmProxyInterface) realmObject).realmGet$isReady());
        ((BooleansRealmProxyInterface) unmanagedObject).realmSet$mCompleted(((BooleansRealmProxyInterface) realmObject).realmGet$mCompleted());
        ((BooleansRealmProxyInterface) unmanagedObject).realmSet$anotherBoolean(((BooleansRealmProxyInterface) realmObject).realmGet$anotherBoolean());
        return unmanagedObject;
    }

    @Override
    @SuppressWarnings("ArrayToString")
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("Booleans = proxy[");
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
    public ProxyState<?> realmGet$proxyState() {
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
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aBooleans.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aBooleans.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }

}
