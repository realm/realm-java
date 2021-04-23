package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.ImportFlag;
import io.realm.ProxyUtils;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.NativeContext;
import io.realm.internal.OsList;
import io.realm.internal.OsMap;
import io.realm.internal.OsObject;
import io.realm.internal.OsObjectSchemaInfo;
import io.realm.internal.OsSchemaInfo;
import io.realm.internal.OsSet;
import io.realm.internal.Property;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.android.JsonUtils;
import io.realm.internal.core.NativeRealmAny;
import io.realm.internal.objectstore.OsObjectBuilder;
import io.realm.log.RealmLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("all")
public class some_test_NamePolicyRealmAnyClassSettingsRealmProxy extends some.test.NamePolicyRealmAnyClassSettings
        implements RealmObjectProxy, some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface {

    static final class NamePolicyRealmAnyClassSettingsColumnInfo extends ColumnInfo {
        long firstNameColKey;
        long lastNameColKey;

        NamePolicyRealmAnyClassSettingsColumnInfo(OsSchemaInfo schemaInfo) {
            super(2);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("customName");
            this.firstNameColKey = addColumnDetails("firstName", "first_name", objectSchemaInfo);
            this.lastNameColKey = addColumnDetails("lastName", "LastName", objectSchemaInfo);
        }

        NamePolicyRealmAnyClassSettingsColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new NamePolicyRealmAnyClassSettingsColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final NamePolicyRealmAnyClassSettingsColumnInfo src = (NamePolicyRealmAnyClassSettingsColumnInfo) rawSrc;
            final NamePolicyRealmAnyClassSettingsColumnInfo dst = (NamePolicyRealmAnyClassSettingsColumnInfo) rawDst;
            dst.firstNameColKey = src.firstNameColKey;
            dst.lastNameColKey = src.lastNameColKey;
        }
    }

    private static final String NO_ALIAS = "";
    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private NamePolicyRealmAnyClassSettingsColumnInfo columnInfo;
    private ProxyState<some.test.NamePolicyRealmAnyClassSettings> proxyState;

    some_test_NamePolicyRealmAnyClassSettingsRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (NamePolicyRealmAnyClassSettingsColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<some.test.NamePolicyRealmAnyClassSettings>(this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$firstName() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.firstNameColKey);
    }

    @Override
    public void realmSet$firstName(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.firstNameColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.firstNameColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.firstNameColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.firstNameColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$lastName() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.lastNameColKey);
    }

    @Override
    public void realmSet$lastName(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.lastNameColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.lastNameColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.lastNameColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.lastNameColKey, value);
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("NamePolicyRealmAnyClassSettings", "customName", false, 2, 0);
        builder.addPersistedProperty("firstName", "first_name", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("lastName", "LastName", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static NamePolicyRealmAnyClassSettingsColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new NamePolicyRealmAnyClassSettingsColumnInfo(schemaInfo);
    }

    public static String getSimpleClassName() {
        return "customName";
    }

    public static final class ClassNameHelper {
        public static final String INTERNAL_CLASS_NAME = "customName";
    }

    @SuppressWarnings("cast")
    public static some.test.NamePolicyRealmAnyClassSettings createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        some.test.NamePolicyRealmAnyClassSettings obj = realm.createObjectInternal(some.test.NamePolicyRealmAnyClassSettings.class, true, excludeFields);

        final some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface objProxy = (some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) obj;
        if (json.has("firstName")) {
            if (json.isNull("firstName")) {
                objProxy.realmSet$firstName(null);
            } else {
                objProxy.realmSet$firstName((String) json.getString("firstName"));
            }
        }
        if (json.has("lastName")) {
            if (json.isNull("lastName")) {
                objProxy.realmSet$lastName(null);
            } else {
                objProxy.realmSet$lastName((String) json.getString("lastName"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.NamePolicyRealmAnyClassSettings createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        final some.test.NamePolicyRealmAnyClassSettings obj = new some.test.NamePolicyRealmAnyClassSettings();
        final some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface objProxy = (some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) obj;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (false) {
            } else if (name.equals("firstName")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$firstName((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$firstName(null);
                }
            } else if (name.equals("lastName")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$lastName((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$lastName(null);
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return realm.copyToRealm(obj);
    }

    static some_test_NamePolicyRealmAnyClassSettingsRealmProxy newProxyInstance(BaseRealm realm, Row row) {
        // Ignore default values to avoid creating unexpected objects from RealmModel/RealmList fields
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        objectContext.set(realm, row, realm.getSchema().getColumnInfo(some.test.NamePolicyRealmAnyClassSettings.class), false, Collections.<String>emptyList());
        io.realm.some_test_NamePolicyRealmAnyClassSettingsRealmProxy obj = new io.realm.some_test_NamePolicyRealmAnyClassSettingsRealmProxy();
        objectContext.clear();
        return obj;
    }

    public static some.test.NamePolicyRealmAnyClassSettings copyOrUpdate(Realm realm, NamePolicyRealmAnyClassSettingsColumnInfo columnInfo, some.test.NamePolicyRealmAnyClassSettings object, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
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
            return (some.test.NamePolicyRealmAnyClassSettings) cachedRealmObject;
        }

        return copy(realm, columnInfo, object, update, cache, flags);
    }

    public static some.test.NamePolicyRealmAnyClassSettings copy(Realm realm, NamePolicyRealmAnyClassSettingsColumnInfo columnInfo, some.test.NamePolicyRealmAnyClassSettings newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.NamePolicyRealmAnyClassSettings) cachedRealmObject;
        }

        some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface unmanagedSource = (some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) newObject;

        Table table = realm.getTable(some.test.NamePolicyRealmAnyClassSettings.class);
        OsObjectBuilder builder = new OsObjectBuilder(table, flags);

        // Add all non-"object reference" fields
        builder.addString(columnInfo.firstNameColKey, unmanagedSource.realmGet$firstName());
        builder.addString(columnInfo.lastNameColKey, unmanagedSource.realmGet$lastName());

        // Create the underlying object and cache it before setting any object/objectlist references
        // This will allow us to break any circular dependencies by using the object cache.
        Row row = builder.createNewObject();
        io.realm.some_test_NamePolicyRealmAnyClassSettingsRealmProxy managedCopy = newProxyInstance(realm, row);
        cache.put(newObject, managedCopy);

        return managedCopy;
    }

    public static long insert(Realm realm, some.test.NamePolicyRealmAnyClassSettings object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(some.test.NamePolicyRealmAnyClassSettings.class);
        long tableNativePtr = table.getNativePtr();
        NamePolicyRealmAnyClassSettingsColumnInfo columnInfo = (NamePolicyRealmAnyClassSettingsColumnInfo) realm.getSchema().getColumnInfo(some.test.NamePolicyRealmAnyClassSettings.class);
        long objKey = OsObject.createRow(table);
        cache.put(object, objKey);
        String realmGet$firstName = ((some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) object).realmGet$firstName();
        if (realmGet$firstName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.firstNameColKey, objKey, realmGet$firstName, false);
        }
        String realmGet$lastName = ((some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) object).realmGet$lastName();
        if (realmGet$lastName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.lastNameColKey, objKey, realmGet$lastName, false);
        }
        return objKey;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.NamePolicyRealmAnyClassSettings.class);
        long tableNativePtr = table.getNativePtr();
        NamePolicyRealmAnyClassSettingsColumnInfo columnInfo = (NamePolicyRealmAnyClassSettingsColumnInfo) realm.getSchema().getColumnInfo(some.test.NamePolicyRealmAnyClassSettings.class);
        some.test.NamePolicyRealmAnyClassSettings object = null;
        while (objects.hasNext()) {
            object = (some.test.NamePolicyRealmAnyClassSettings) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            long objKey = OsObject.createRow(table);
            cache.put(object, objKey);
            String realmGet$firstName = ((some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) object).realmGet$firstName();
            if (realmGet$firstName != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.firstNameColKey, objKey, realmGet$firstName, false);
            }
            String realmGet$lastName = ((some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) object).realmGet$lastName();
            if (realmGet$lastName != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.lastNameColKey, objKey, realmGet$lastName, false);
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.NamePolicyRealmAnyClassSettings object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(some.test.NamePolicyRealmAnyClassSettings.class);
        long tableNativePtr = table.getNativePtr();
        NamePolicyRealmAnyClassSettingsColumnInfo columnInfo = (NamePolicyRealmAnyClassSettingsColumnInfo) realm.getSchema().getColumnInfo(some.test.NamePolicyRealmAnyClassSettings.class);
        long objKey = OsObject.createRow(table);
        cache.put(object, objKey);
        String realmGet$firstName = ((some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) object).realmGet$firstName();
        if (realmGet$firstName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.firstNameColKey, objKey, realmGet$firstName, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.firstNameColKey, objKey, false);
        }
        String realmGet$lastName = ((some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) object).realmGet$lastName();
        if (realmGet$lastName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.lastNameColKey, objKey, realmGet$lastName, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.lastNameColKey, objKey, false);
        }
        return objKey;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.NamePolicyRealmAnyClassSettings.class);
        long tableNativePtr = table.getNativePtr();
        NamePolicyRealmAnyClassSettingsColumnInfo columnInfo = (NamePolicyRealmAnyClassSettingsColumnInfo) realm.getSchema().getColumnInfo(some.test.NamePolicyRealmAnyClassSettings.class);
        some.test.NamePolicyRealmAnyClassSettings object = null;
        while (objects.hasNext()) {
            object = (some.test.NamePolicyRealmAnyClassSettings) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            long objKey = OsObject.createRow(table);
            cache.put(object, objKey);
            String realmGet$firstName = ((some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) object).realmGet$firstName();
            if (realmGet$firstName != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.firstNameColKey, objKey, realmGet$firstName, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.firstNameColKey, objKey, false);
            }
            String realmGet$lastName = ((some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) object).realmGet$lastName();
            if (realmGet$lastName != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.lastNameColKey, objKey, realmGet$lastName, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.lastNameColKey, objKey, false);
            }
        }
    }

    public static some.test.NamePolicyRealmAnyClassSettings createDetachedCopy(some.test.NamePolicyRealmAnyClassSettings realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.NamePolicyRealmAnyClassSettings unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new some.test.NamePolicyRealmAnyClassSettings();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.NamePolicyRealmAnyClassSettings) cachedObject.object;
            }
            unmanagedObject = (some.test.NamePolicyRealmAnyClassSettings) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface unmanagedCopy = (some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) unmanagedObject;
        some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface realmSource = (some_test_NamePolicyRealmAnyClassSettingsRealmProxyInterface) realmObject;
        Realm objectRealm = (Realm) ((RealmObjectProxy) realmObject).realmGet$proxyState().getRealm$realm();
        unmanagedCopy.realmSet$firstName(realmSource.realmGet$firstName());
        unmanagedCopy.realmSet$lastName(realmSource.realmGet$lastName());

        return unmanagedObject;
    }

    @Override
    @SuppressWarnings("ArrayToString")
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("NamePolicyRealmAnyClassSettings = proxy[");
        stringBuilder.append("{firstName:");
        stringBuilder.append(realmGet$firstName() != null ? realmGet$firstName() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{lastName:");
        stringBuilder.append(realmGet$lastName() != null ? realmGet$lastName() : "null");
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
        some_test_NamePolicyRealmAnyClassSettingsRealmProxy aNamePolicyRealmAnyClassSettings = (some_test_NamePolicyRealmAnyClassSettingsRealmProxy)o;

        BaseRealm realm = proxyState.getRealm$realm();
        BaseRealm otherRealm = aNamePolicyRealmAnyClassSettings.proxyState.getRealm$realm();
        String path = realm.getPath();
        String otherPath = otherRealm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;
        if (realm.isFrozen() != otherRealm.isFrozen()) return false;
        if (!realm.sharedRealm.getVersionID().equals(otherRealm.sharedRealm.getVersionID())) {
            return false;
        }

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aNamePolicyRealmAnyClassSettings.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getObjectKey() != aNamePolicyRealmAnyClassSettings.proxyState.getRow$realm().getObjectKey()) return false;

        return true;
    }
}
