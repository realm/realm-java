package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.ImportFlag;
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
import io.realm.internal.objectstore.OsObjectBuilder;
import io.realm.log.RealmLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("all")
public class some_test_EmbeddedClassRealmProxy extends some.test.EmbeddedClass
        implements RealmObjectProxy, some_test_EmbeddedClassRealmProxyInterface {

    static final class EmbeddedClassColumnInfo extends ColumnInfo {
        long nameColKey;
        long ageColKey;

        EmbeddedClassColumnInfo(OsSchemaInfo schemaInfo) {
            super(2);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("EmbeddedClass");
            this.nameColKey = addColumnDetails("name", "name", objectSchemaInfo);
            this.ageColKey = addColumnDetails("age", "age", objectSchemaInfo);
        }

        EmbeddedClassColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new EmbeddedClassColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final EmbeddedClassColumnInfo src = (EmbeddedClassColumnInfo) rawSrc;
            final EmbeddedClassColumnInfo dst = (EmbeddedClassColumnInfo) rawDst;
            dst.nameColKey = src.nameColKey;
            dst.ageColKey = src.ageColKey;
        }
    }

    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private EmbeddedClassColumnInfo columnInfo;
    private ProxyState<some.test.EmbeddedClass> proxyState;

    some_test_EmbeddedClassRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (EmbeddedClassColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<some.test.EmbeddedClass>(this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$name() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.nameColKey);
    }

    @Override
    public void realmSet$name(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.nameColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.nameColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.nameColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.nameColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public int realmGet$age() {
        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.ageColKey);
    }

    @Override
    public void realmSet$age(int value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setLong(columnInfo.ageColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.ageColKey, value);
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("EmbeddedClass", true, 2, 0);
        builder.addPersistedProperty("", "name", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("", "age", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static EmbeddedClassColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new EmbeddedClassColumnInfo(schemaInfo);
    }

    public static String getSimpleClassName() {
        return "EmbeddedClass";
    }

    public static final class ClassNameHelper {
        public static final String INTERNAL_CLASS_NAME = "EmbeddedClass";
    }

    @SuppressWarnings("cast")
    public static some.test.EmbeddedClass createOrUpdateEmbeddedUsingJsonObject(Realm realm, RealmModel parent, String parentProperty, JSONObject json, boolean update)
            throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        some.test.EmbeddedClass obj = realm.createEmbeddedObject(some.test.EmbeddedClass.class, parent, parentProperty);

        final some_test_EmbeddedClassRealmProxyInterface objProxy = (some_test_EmbeddedClassRealmProxyInterface) obj;
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
    public static some.test.EmbeddedClass createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        final some.test.EmbeddedClass obj = new some.test.EmbeddedClass();
        final some_test_EmbeddedClassRealmProxyInterface objProxy = (some_test_EmbeddedClassRealmProxyInterface) obj;
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
        return obj;
    }

    static some_test_EmbeddedClassRealmProxy newProxyInstance(BaseRealm realm, Row row) {
        // Ignore default values to avoid creating unexpected objects from RealmModel/RealmList fields
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        objectContext.set(realm, row, realm.getSchema().getColumnInfo(some.test.EmbeddedClass.class), false, Collections.<String>emptyList());
        io.realm.some_test_EmbeddedClassRealmProxy obj = new io.realm.some_test_EmbeddedClassRealmProxy();
        objectContext.clear();
        return obj;
    }

    public static some.test.EmbeddedClass copyOrUpdate(Realm realm, EmbeddedClassColumnInfo columnInfo, some.test.EmbeddedClass object, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null) {
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
            return (some.test.EmbeddedClass) cachedRealmObject;
        }

        return copy(realm, columnInfo, object, update, cache, flags);
    }

    public static some.test.EmbeddedClass copy(Realm realm, EmbeddedClassColumnInfo columnInfo, some.test.EmbeddedClass newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.EmbeddedClass) cachedRealmObject;
        }

        some_test_EmbeddedClassRealmProxyInterface unmanagedSource = (some_test_EmbeddedClassRealmProxyInterface) newObject;

        Table table = realm.getTable(some.test.EmbeddedClass.class);
        OsObjectBuilder builder = new OsObjectBuilder(table, flags);

        // Add all non-"object reference" fields
        builder.addString(columnInfo.nameColKey, unmanagedSource.realmGet$name());
        builder.addInteger(columnInfo.ageColKey, unmanagedSource.realmGet$age());

        // Create the underlying object and cache it before setting any object/objectlist references
        // This will allow us to break any circular dependencies by using the object cache.
        Row row = builder.createNewObject();
        io.realm.some_test_EmbeddedClassRealmProxy managedCopy = newProxyInstance(realm, row);
        cache.put(newObject, managedCopy);

        return managedCopy;
    }

    public static long insert(Realm realm, Table parentObjectTable, long parentColumnKey, long parentObjectKey, some.test.EmbeddedClass object, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.EmbeddedClass.class);
        long tableNativePtr = table.getNativePtr();
        EmbeddedClassColumnInfo columnInfo = (EmbeddedClassColumnInfo) realm.getSchema().getColumnInfo(some.test.EmbeddedClass.class);
        long objKey = OsObject.createEmbeddedObject(parentObjectTable, parentObjectKey, parentColumnKey);
        cache.put(object, objKey);
        String realmGet$name = ((some_test_EmbeddedClassRealmProxyInterface) object).realmGet$name();
        if (realmGet$name != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.nameColKey, objKey, realmGet$name, false);
        }
        Table.nativeSetLong(tableNativePtr, columnInfo.ageColKey, objKey, ((some_test_EmbeddedClassRealmProxyInterface) object).realmGet$age(), false);
        return objKey;
    }

    public static void insert(Realm realm, Table parentObjectTable, long parentColumnKey, long parentObjectKey, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.EmbeddedClass.class);
        long tableNativePtr = table.getNativePtr();
        EmbeddedClassColumnInfo columnInfo = (EmbeddedClassColumnInfo) realm.getSchema().getColumnInfo(some.test.EmbeddedClass.class);
        some.test.EmbeddedClass object = null;
        while (objects.hasNext()) {
            object = (some.test.EmbeddedClass) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            long objKey = OsObject.createEmbeddedObject(parentObjectTable, parentObjectKey, parentColumnKey);
            cache.put(object, objKey);
            String realmGet$name = ((some_test_EmbeddedClassRealmProxyInterface) object).realmGet$name();
            if (realmGet$name != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.nameColKey, objKey, realmGet$name, false);
            }
            Table.nativeSetLong(tableNativePtr, columnInfo.ageColKey, objKey, ((some_test_EmbeddedClassRealmProxyInterface) object).realmGet$age(), false);
        }
    }

    public static long insertOrUpdate(Realm realm, Table parentObjectTable, long parentColumnKey, long parentObjectKey, some.test.EmbeddedClass object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(some.test.EmbeddedClass.class);
        long tableNativePtr = table.getNativePtr();
        EmbeddedClassColumnInfo columnInfo = (EmbeddedClassColumnInfo) realm.getSchema().getColumnInfo(some.test.EmbeddedClass.class);
        long objKey = OsObject.createEmbeddedObject(parentObjectTable, parentObjectKey, parentColumnKey);
        cache.put(object, objKey);
        String realmGet$name = ((some_test_EmbeddedClassRealmProxyInterface) object).realmGet$name();
        if (realmGet$name != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.nameColKey, objKey, realmGet$name, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.nameColKey, objKey, false);
        }
        Table.nativeSetLong(tableNativePtr, columnInfo.ageColKey, objKey, ((some_test_EmbeddedClassRealmProxyInterface) object).realmGet$age(), false);
        return objKey;
    }

    public static void insertOrUpdate(Realm realm, Table parentObjectTable, long parentColumnKey, long parentObjectKey, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.EmbeddedClass.class);
        long tableNativePtr = table.getNativePtr();
        EmbeddedClassColumnInfo columnInfo = (EmbeddedClassColumnInfo) realm.getSchema().getColumnInfo(some.test.EmbeddedClass.class);
        some.test.EmbeddedClass object = null;
        while (objects.hasNext()) {
            object = (some.test.EmbeddedClass) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            long objKey = OsObject.createEmbeddedObject(parentObjectTable, parentObjectKey, parentColumnKey);
            cache.put(object, objKey);
            String realmGet$name = ((some_test_EmbeddedClassRealmProxyInterface) object).realmGet$name();
            if (realmGet$name != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.nameColKey, objKey, realmGet$name, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.nameColKey, objKey, false);
            }
            Table.nativeSetLong(tableNativePtr, columnInfo.ageColKey, objKey, ((some_test_EmbeddedClassRealmProxyInterface) object).realmGet$age(), false);
        }
    }

    public static some.test.EmbeddedClass createDetachedCopy(some.test.EmbeddedClass realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.EmbeddedClass unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new some.test.EmbeddedClass();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.EmbeddedClass) cachedObject.object;
            }
            unmanagedObject = (some.test.EmbeddedClass) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        some_test_EmbeddedClassRealmProxyInterface unmanagedCopy = (some_test_EmbeddedClassRealmProxyInterface) unmanagedObject;
        some_test_EmbeddedClassRealmProxyInterface realmSource = (some_test_EmbeddedClassRealmProxyInterface) realmObject;
        unmanagedCopy.realmSet$name(realmSource.realmGet$name());
        unmanagedCopy.realmSet$age(realmSource.realmGet$age());

        return unmanagedObject;
    }

    static some.test.EmbeddedClass update(Realm realm, EmbeddedClassColumnInfo columnInfo, some.test.EmbeddedClass realmObject, some.test.EmbeddedClass newObject, Map<RealmModel, RealmObjectProxy> cache, Set<ImportFlag> flags) {
        some_test_EmbeddedClassRealmProxyInterface realmObjectTarget = (some_test_EmbeddedClassRealmProxyInterface) realmObject;
        some_test_EmbeddedClassRealmProxyInterface realmObjectSource = (some_test_EmbeddedClassRealmProxyInterface) newObject;
        Table table = realm.getTable(some.test.EmbeddedClass.class);
        OsObjectBuilder builder = new OsObjectBuilder(table, flags);
        builder.addString(columnInfo.nameColKey, realmObjectSource.realmGet$name());
        builder.addInteger(columnInfo.ageColKey, realmObjectSource.realmGet$age());

        builder.updateExistingEmbeddedObject((RealmObjectProxy) realmObject);
        return realmObject;
    }

    public static void updateEmbeddedObject(Realm realm, some.test.EmbeddedClass unmanagedObject, some.test.EmbeddedClass managedObject, Map<RealmModel, RealmObjectProxy> cache, Set<ImportFlag> flags) {
        update(realm, (some_test_EmbeddedClassRealmProxy.EmbeddedClassColumnInfo) realm.getSchema().getColumnInfo(some.test.EmbeddedClass.class), managedObject, unmanagedObject, cache, flags);
    }

    @Override
    @SuppressWarnings("ArrayToString")
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("EmbeddedClass = proxy[");
        stringBuilder.append("{name:");
        stringBuilder.append(realmGet$name() != null ? realmGet$name() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{age:");
        stringBuilder.append(realmGet$age());
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
        long objKey = proxyState.getRow$realm().getObjectKey();

        int result = 17;
        result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0);
        result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0);
        result = 31 * result + (int) (objKey ^ (objKey >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        some_test_EmbeddedClassRealmProxy aEmbeddedClass = (some_test_EmbeddedClassRealmProxy)o;

        BaseRealm realm = proxyState.getRealm$realm();
        BaseRealm otherRealm = aEmbeddedClass.proxyState.getRealm$realm();
        String path = realm.getPath();
        String otherPath = otherRealm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;
        if (realm.isFrozen() != otherRealm.isFrozen()) return false;
        if (!realm.sharedRealm.getVersionID().equals(otherRealm.sharedRealm.getVersionID())) {
            return false;
        }

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aEmbeddedClass.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getObjectKey() != aEmbeddedClass.proxyState.getRow$realm().getObjectKey()) return false;

        return true;
    }
}
