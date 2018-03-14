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
public class some_test_SimpleRealmProxy extends some.test.Simple
        implements RealmObjectProxy, some_test_SimpleRealmProxyInterface {

    static final class SimpleColumnInfo extends ColumnInfo {
        long nameIndex;
        long ageIndex;

        SimpleColumnInfo(OsSchemaInfo schemaInfo) {
            super(2);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("Simple");
            this.nameIndex = addColumnDetails("name", "name", objectSchemaInfo);
            this.ageIndex = addColumnDetails("age", "age", objectSchemaInfo);
        }

        SimpleColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new SimpleColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final SimpleColumnInfo src = (SimpleColumnInfo) rawSrc;
            final SimpleColumnInfo dst = (SimpleColumnInfo) rawDst;
            dst.nameIndex = src.nameIndex;
            dst.ageIndex = src.ageIndex;
        }
    }

    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private SimpleColumnInfo columnInfo;
    private ProxyState<some.test.Simple> proxyState;

    some_test_SimpleRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (SimpleColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<some.test.Simple>(this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$name() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.nameIndex);
    }

    @Override
    public void realmSet$name(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.nameIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setString(columnInfo.nameIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.nameIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.nameIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public int realmGet$age() {
        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.ageIndex);
    }

    @Override
    public void realmSet$age(int value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setLong(columnInfo.ageIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.ageIndex, value);
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("Simple", 2, 0);
        builder.addPersistedProperty("name", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("age", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static SimpleColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new SimpleColumnInfo(schemaInfo);
    }

    public static String getSimpleClassName() {
        return "Simple";
    }

    public static final class ClassNameHelper {
        public static final String INTERNAL_CLASS_NAME = "Simple";
    }

    @SuppressWarnings("cast")
    public static some.test.Simple createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        some.test.Simple obj = realm.createObjectInternal(some.test.Simple.class, true, excludeFields);

        final some_test_SimpleRealmProxyInterface objProxy = (some_test_SimpleRealmProxyInterface) obj;
        if (json.has("name")) {
            if (json.isNull("name")) {
                objProxy.realmSet$name(null);
            } else {
                objProxy.realmSet$name((String) json.getString("name"));
            }
        }
        if (json.has("age")) {
            if (json.isNull("age")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'age' to null.");
            } else {
                objProxy.realmSet$age((int) json.getInt("age"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.Simple createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        final some.test.Simple obj = new some.test.Simple();
        final some_test_SimpleRealmProxyInterface objProxy = (some_test_SimpleRealmProxyInterface) obj;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (false) {
            } else if (name.equals("name")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$name((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$name(null);
                }
            } else if (name.equals("age")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$age((int) reader.nextInt());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'age' to null.");
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return realm.copyToRealm(obj);
    }

    public static some.test.Simple copyOrUpdate(Realm realm, some.test.Simple object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
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
            return (some.test.Simple) cachedRealmObject;
        }

        return copy(realm, object, update, cache);
    }

    public static some.test.Simple copy(Realm realm, some.test.Simple newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.Simple) cachedRealmObject;
        }

        // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
        some.test.Simple realmObject = realm.createObjectInternal(some.test.Simple.class, false, Collections.<String>emptyList());
        cache.put(newObject, (RealmObjectProxy) realmObject);

        some_test_SimpleRealmProxyInterface realmObjectSource = (some_test_SimpleRealmProxyInterface) newObject;
        some_test_SimpleRealmProxyInterface realmObjectCopy = (some_test_SimpleRealmProxyInterface) realmObject;

        realmObjectCopy.realmSet$name(realmObjectSource.realmGet$name());
        realmObjectCopy.realmSet$age(realmObjectSource.realmGet$age());
        return realmObject;
    }

    public static long insert(Realm realm, some.test.Simple object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.Simple.class);
        long tableNativePtr = table.getNativePtr();
        SimpleColumnInfo columnInfo = (SimpleColumnInfo) realm.getSchema().getColumnInfo(some.test.Simple.class);
        long rowIndex = OsObject.createRow(table);
        cache.put(object, rowIndex);
        String realmGet$name = ((some_test_SimpleRealmProxyInterface) object).realmGet$name();
        if (realmGet$name != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.nameIndex, rowIndex, realmGet$name, false);
        }
        Table.nativeSetLong(tableNativePtr, columnInfo.ageIndex, rowIndex, ((some_test_SimpleRealmProxyInterface) object).realmGet$age(), false);
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.Simple.class);
        long tableNativePtr = table.getNativePtr();
        SimpleColumnInfo columnInfo = (SimpleColumnInfo) realm.getSchema().getColumnInfo(some.test.Simple.class);
        some.test.Simple object = null;
        while (objects.hasNext()) {
            object = (some.test.Simple) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            long rowIndex = OsObject.createRow(table);
            cache.put(object, rowIndex);
            String realmGet$name = ((some_test_SimpleRealmProxyInterface) object).realmGet$name();
            if (realmGet$name != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.nameIndex, rowIndex, realmGet$name, false);
            }
            Table.nativeSetLong(tableNativePtr, columnInfo.ageIndex, rowIndex, ((some_test_SimpleRealmProxyInterface) object).realmGet$age(), false);
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.Simple object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.Simple.class);
        long tableNativePtr = table.getNativePtr();
        SimpleColumnInfo columnInfo = (SimpleColumnInfo) realm.getSchema().getColumnInfo(some.test.Simple.class);
        long rowIndex = OsObject.createRow(table);
        cache.put(object, rowIndex);
        String realmGet$name = ((some_test_SimpleRealmProxyInterface) object).realmGet$name();
        if (realmGet$name != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.nameIndex, rowIndex, realmGet$name, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.nameIndex, rowIndex, false);
        }
        Table.nativeSetLong(tableNativePtr, columnInfo.ageIndex, rowIndex, ((some_test_SimpleRealmProxyInterface) object).realmGet$age(), false);
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.Simple.class);
        long tableNativePtr = table.getNativePtr();
        SimpleColumnInfo columnInfo = (SimpleColumnInfo) realm.getSchema().getColumnInfo(some.test.Simple.class);
        some.test.Simple object = null;
        while (objects.hasNext()) {
            object = (some.test.Simple) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            long rowIndex = OsObject.createRow(table);
            cache.put(object, rowIndex);
            String realmGet$name = ((some_test_SimpleRealmProxyInterface) object).realmGet$name();
            if (realmGet$name != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.nameIndex, rowIndex, realmGet$name, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.nameIndex, rowIndex, false);
            }
            Table.nativeSetLong(tableNativePtr, columnInfo.ageIndex, rowIndex, ((some_test_SimpleRealmProxyInterface) object).realmGet$age(), false);
        }
    }

    public static some.test.Simple createDetachedCopy(some.test.Simple realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.Simple unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new some.test.Simple();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.Simple) cachedObject.object;
            }
            unmanagedObject = (some.test.Simple) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        some_test_SimpleRealmProxyInterface unmanagedCopy = (some_test_SimpleRealmProxyInterface) unmanagedObject;
        some_test_SimpleRealmProxyInterface realmSource = (some_test_SimpleRealmProxyInterface) realmObject;
        unmanagedCopy.realmSet$name(realmSource.realmGet$name());
        unmanagedCopy.realmSet$age(realmSource.realmGet$age());

        return unmanagedObject;
    }

    @Override
    public ProxyState<?> realmGet$proxyState() {
        return proxyState;
    }

}
