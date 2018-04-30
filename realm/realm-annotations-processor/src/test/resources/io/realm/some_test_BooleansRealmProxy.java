package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.ProxyUtils;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.OsList;
import io.realm.internal.OsObject;
import io.realm.internal.OsObjectSchemaInfo;
import io.realm.internal.OsSchemaInfo;
import io.realm.internal.Property;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
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

@SuppressWarnings("all")
public class some_test_BooleansRealmProxy extends some.test.Booleans
        implements RealmObjectProxy, some_test_BooleansRealmProxyInterface {

    static final class BooleansColumnInfo extends ColumnInfo {
        long doneIndex;
        long isReadyIndex;
        long mCompletedIndex;
        long anotherBooleanIndex;

        BooleansColumnInfo(OsSchemaInfo schemaInfo) {
            super(4);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("Booleans");
            this.doneIndex = addColumnDetails("done", "done", objectSchemaInfo);
            this.isReadyIndex = addColumnDetails("isReady", "isReady", objectSchemaInfo);
            this.mCompletedIndex = addColumnDetails("mCompleted", "mCompleted", objectSchemaInfo);
            this.anotherBooleanIndex = addColumnDetails("anotherBoolean", "anotherBoolean", objectSchemaInfo);
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

    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private BooleansColumnInfo columnInfo;
    private ProxyState<some.test.Booleans> proxyState;

    some_test_BooleansRealmProxy() {
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
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("Booleans", 4, 0);
        builder.addPersistedProperty("done", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("isReady", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("mCompleted", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("anotherBoolean", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static BooleansColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new BooleansColumnInfo(schemaInfo);
    }

    public static String getSimpleClassName() {
        return "Booleans";
    }

    public static final class ClassNameHelper {
        public static final String INTERNAL_CLASS_NAME = "Booleans";
    }

    @SuppressWarnings("cast")
    public static some.test.Booleans createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        some.test.Booleans obj = realm.createObjectInternal(some.test.Booleans.class, true, excludeFields);

        final some_test_BooleansRealmProxyInterface objProxy = (some_test_BooleansRealmProxyInterface) obj;
        if (json.has("done")) {
            if (json.isNull("done")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'done' to null.");
            } else {
                objProxy.realmSet$done((boolean) json.getBoolean("done"));
            }
        }
        if (json.has("isReady")) {
            if (json.isNull("isReady")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'isReady' to null.");
            } else {
                objProxy.realmSet$isReady((boolean) json.getBoolean("isReady"));
            }
        }
        if (json.has("mCompleted")) {
            if (json.isNull("mCompleted")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'mCompleted' to null.");
            } else {
                objProxy.realmSet$mCompleted((boolean) json.getBoolean("mCompleted"));
            }
        }
        if (json.has("anotherBoolean")) {
            if (json.isNull("anotherBoolean")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'anotherBoolean' to null.");
            } else {
                objProxy.realmSet$anotherBoolean((boolean) json.getBoolean("anotherBoolean"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.Booleans createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        final some.test.Booleans obj = new some.test.Booleans();
        final some_test_BooleansRealmProxyInterface objProxy = (some_test_BooleansRealmProxyInterface) obj;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (false) {
            } else if (name.equals("done")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$done((boolean) reader.nextBoolean());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'done' to null.");
                }
            } else if (name.equals("isReady")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$isReady((boolean) reader.nextBoolean());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'isReady' to null.");
                }
            } else if (name.equals("mCompleted")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$mCompleted((boolean) reader.nextBoolean());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'mCompleted' to null.");
                }
            } else if (name.equals("anotherBoolean")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$anotherBoolean((boolean) reader.nextBoolean());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'anotherBoolean' to null.");
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return realm.copyToRealm(obj);
    }

    public static some.test.Booleans copyOrUpdate(Realm realm, some.test.Booleans object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null) {
            final BaseRealm otherRealm = ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm();
            if (otherRealm.threadId != realm.threadId) {
                throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
            }
            if (otherRealm.getPath().equals(realm.getPath())) {
                return object;
            }
        }
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (some.test.Booleans) cachedRealmObject;
        }

        return copy(realm, object, update, cache);
    }

    public static some.test.Booleans copy(Realm realm, some.test.Booleans newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.Booleans) cachedRealmObject;
        }

        // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
        some.test.Booleans realmObject = realm.createObjectInternal(some.test.Booleans.class, false, Collections.<String>emptyList());
        cache.put(newObject, (RealmObjectProxy) realmObject);

        some_test_BooleansRealmProxyInterface realmObjectSource = (some_test_BooleansRealmProxyInterface) newObject;
        some_test_BooleansRealmProxyInterface realmObjectCopy = (some_test_BooleansRealmProxyInterface) realmObject;

        realmObjectCopy.realmSet$done(realmObjectSource.realmGet$done());
        realmObjectCopy.realmSet$isReady(realmObjectSource.realmGet$isReady());
        realmObjectCopy.realmSet$mCompleted(realmObjectSource.realmGet$mCompleted());
        realmObjectCopy.realmSet$anotherBoolean(realmObjectSource.realmGet$anotherBoolean());
        return realmObject;
    }

    public static long insert(Realm realm, some.test.Booleans object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.Booleans.class);
        long tableNativePtr = table.getNativePtr();
        BooleansColumnInfo columnInfo = (BooleansColumnInfo) realm.getSchema().getColumnInfo(some.test.Booleans.class);
        long rowIndex = OsObject.createRow(table);
        cache.put(object, rowIndex);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.doneIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$done(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isReadyIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$isReady(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.mCompletedIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$mCompleted(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.anotherBooleanIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$anotherBoolean(), false);
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.Booleans.class);
        long tableNativePtr = table.getNativePtr();
        BooleansColumnInfo columnInfo = (BooleansColumnInfo) realm.getSchema().getColumnInfo(some.test.Booleans.class);
        some.test.Booleans object = null;
        while (objects.hasNext()) {
            object = (some.test.Booleans) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            long rowIndex = OsObject.createRow(table);
            cache.put(object, rowIndex);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.doneIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$done(), false);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.isReadyIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$isReady(), false);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.mCompletedIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$mCompleted(), false);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.anotherBooleanIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$anotherBoolean(), false);
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.Booleans object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.Booleans.class);
        long tableNativePtr = table.getNativePtr();
        BooleansColumnInfo columnInfo = (BooleansColumnInfo) realm.getSchema().getColumnInfo(some.test.Booleans.class);
        long rowIndex = OsObject.createRow(table);
        cache.put(object, rowIndex);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.doneIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$done(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.isReadyIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$isReady(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.mCompletedIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$mCompleted(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.anotherBooleanIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$anotherBoolean(), false);
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.Booleans.class);
        long tableNativePtr = table.getNativePtr();
        BooleansColumnInfo columnInfo = (BooleansColumnInfo) realm.getSchema().getColumnInfo(some.test.Booleans.class);
        some.test.Booleans object = null;
        while (objects.hasNext()) {
            object = (some.test.Booleans) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            long rowIndex = OsObject.createRow(table);
            cache.put(object, rowIndex);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.doneIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$done(), false);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.isReadyIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$isReady(), false);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.mCompletedIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$mCompleted(), false);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.anotherBooleanIndex, rowIndex, ((some_test_BooleansRealmProxyInterface) object).realmGet$anotherBoolean(), false);
        }
    }

    public static some.test.Booleans createDetachedCopy(some.test.Booleans realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.Booleans unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new some.test.Booleans();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.Booleans) cachedObject.object;
            }
            unmanagedObject = (some.test.Booleans) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        some_test_BooleansRealmProxyInterface unmanagedCopy = (some_test_BooleansRealmProxyInterface) unmanagedObject;
        some_test_BooleansRealmProxyInterface realmSource = (some_test_BooleansRealmProxyInterface) realmObject;
        unmanagedCopy.realmSet$done(realmSource.realmGet$done());
        unmanagedCopy.realmSet$isReady(realmSource.realmGet$isReady());
        unmanagedCopy.realmSet$mCompleted(realmSource.realmGet$mCompleted());
        unmanagedCopy.realmSet$anotherBoolean(realmSource.realmGet$anotherBoolean());

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
        some_test_BooleansRealmProxy aBooleans = (some_test_BooleansRealmProxy)o;

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
