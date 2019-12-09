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
public class some_test_NamePolicyModuleDefaultsRealmProxy extends some.test.NamePolicyModuleDefaults
        implements RealmObjectProxy, some_test_NamePolicyModuleDefaultsRealmProxyInterface {

    static final class NamePolicyModuleDefaultsColumnInfo extends ColumnInfo {
        long firstNameColKey;
        long lastNameColKey;

        NamePolicyModuleDefaultsColumnInfo(OsSchemaInfo schemaInfo) {
            super(2);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("NamePolicyModuleDefaults");
            this.firstNameColKey = addColumnDetails("firstName", "FirstName", objectSchemaInfo);
            this.lastNameColKey = addColumnDetails("lastName", "LastName", objectSchemaInfo);
        }

        NamePolicyModuleDefaultsColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new NamePolicyModuleDefaultsColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final NamePolicyModuleDefaultsColumnInfo src = (NamePolicyModuleDefaultsColumnInfo) rawSrc;
            final NamePolicyModuleDefaultsColumnInfo dst = (NamePolicyModuleDefaultsColumnInfo) rawDst;
            dst.firstNameColKey = src.firstNameColKey;
            dst.lastNameColKey = src.lastNameColKey;
        }
    }

    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private NamePolicyModuleDefaultsColumnInfo columnInfo;
    private ProxyState<some.test.NamePolicyModuleDefaults> proxyState;

    some_test_NamePolicyModuleDefaultsRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (NamePolicyModuleDefaultsColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<some.test.NamePolicyModuleDefaults>(this);
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
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("NamePolicyModuleDefaults", 2, 0);
        builder.addPersistedProperty("FirstName", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("LastName", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static NamePolicyModuleDefaultsColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new NamePolicyModuleDefaultsColumnInfo(schemaInfo);
    }

    public static String getSimpleClassName() {
        return "NamePolicyModuleDefaults";
    }

    public static final class ClassNameHelper {
        public static final String INTERNAL_CLASS_NAME = "NamePolicyModuleDefaults";
    }

    @SuppressWarnings("cast")
    public static some.test.NamePolicyModuleDefaults createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        some.test.NamePolicyModuleDefaults obj = realm.createObjectInternal(some.test.NamePolicyModuleDefaults.class, true, excludeFields);

        final some_test_NamePolicyModuleDefaultsRealmProxyInterface objProxy = (some_test_NamePolicyModuleDefaultsRealmProxyInterface) obj;
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
    public static some.test.NamePolicyModuleDefaults createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        final some.test.NamePolicyModuleDefaults obj = new some.test.NamePolicyModuleDefaults();
        final some_test_NamePolicyModuleDefaultsRealmProxyInterface objProxy = (some_test_NamePolicyModuleDefaultsRealmProxyInterface) obj;
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

    private static some_test_NamePolicyModuleDefaultsRealmProxy newProxyInstance(BaseRealm realm, Row row) {
        // Ignore default values to avoid creating unexpected objects from RealmModel/RealmList fields
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        objectContext.set(realm, row, realm.getSchema().getColumnInfo(some.test.NamePolicyModuleDefaults.class), false, Collections.<String>emptyList());
        io.realm.some_test_NamePolicyModuleDefaultsRealmProxy obj = new io.realm.some_test_NamePolicyModuleDefaultsRealmProxy();
        objectContext.clear();
        return obj;
    }

    public static some.test.NamePolicyModuleDefaults copyOrUpdate(Realm realm, NamePolicyModuleDefaultsColumnInfo columnInfo, some.test.NamePolicyModuleDefaults object, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
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
            return (some.test.NamePolicyModuleDefaults) cachedRealmObject;
        }

        return copy(realm, columnInfo, object, update, cache, flags);
    }

    public static some.test.NamePolicyModuleDefaults copy(Realm realm, NamePolicyModuleDefaultsColumnInfo columnInfo, some.test.NamePolicyModuleDefaults newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.NamePolicyModuleDefaults) cachedRealmObject;
        }

        some_test_NamePolicyModuleDefaultsRealmProxyInterface realmObjectSource = (some_test_NamePolicyModuleDefaultsRealmProxyInterface) newObject;

        Table table = realm.getTable(some.test.NamePolicyModuleDefaults.class);
        OsObjectBuilder builder = new OsObjectBuilder(table, flags);

        // Add all non-"object reference" fields
        builder.addString(columnInfo.firstNameColKey, realmObjectSource.realmGet$firstName());
        builder.addString(columnInfo.lastNameColKey, realmObjectSource.realmGet$lastName());

        // Create the underlying object and cache it before setting any object/objectlist references
        // This will allow us to break any circular dependencies by using the object cache.
        Row row = builder.createNewObject();
        io.realm.some_test_NamePolicyModuleDefaultsRealmProxy realmObjectCopy = newProxyInstance(realm, row);
        cache.put(newObject, realmObjectCopy);

        return realmObjectCopy;
    }

    public static long insert(Realm realm, some.test.NamePolicyModuleDefaults object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(some.test.NamePolicyModuleDefaults.class);
        long tableNativePtr = table.getNativePtr();
        NamePolicyModuleDefaultsColumnInfo columnInfo = (NamePolicyModuleDefaultsColumnInfo) realm.getSchema().getColumnInfo(some.test.NamePolicyModuleDefaults.class);
        long colKey = OsObject.createRow(table);
        cache.put(object, colKey);
        String realmGet$firstName = ((some_test_NamePolicyModuleDefaultsRealmProxyInterface) object).realmGet$firstName();
        if (realmGet$firstName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.firstNameColKey, colKey, realmGet$firstName, false);
        }
        String realmGet$lastName = ((some_test_NamePolicyModuleDefaultsRealmProxyInterface) object).realmGet$lastName();
        if (realmGet$lastName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.lastNameColKey, colKey, realmGet$lastName, false);
        }
        return colKey;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.NamePolicyModuleDefaults.class);
        long tableNativePtr = table.getNativePtr();
        NamePolicyModuleDefaultsColumnInfo columnInfo = (NamePolicyModuleDefaultsColumnInfo) realm.getSchema().getColumnInfo(some.test.NamePolicyModuleDefaults.class);
        some.test.NamePolicyModuleDefaults object = null;
        while (objects.hasNext()) {
            object = (some.test.NamePolicyModuleDefaults) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            long colKey = OsObject.createRow(table);
            cache.put(object, colKey);
            String realmGet$firstName = ((some_test_NamePolicyModuleDefaultsRealmProxyInterface) object).realmGet$firstName();
            if (realmGet$firstName != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.firstNameColKey, colKey, realmGet$firstName, false);
            }
            String realmGet$lastName = ((some_test_NamePolicyModuleDefaultsRealmProxyInterface) object).realmGet$lastName();
            if (realmGet$lastName != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.lastNameColKey, colKey, realmGet$lastName, false);
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.NamePolicyModuleDefaults object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(some.test.NamePolicyModuleDefaults.class);
        long tableNativePtr = table.getNativePtr();
        NamePolicyModuleDefaultsColumnInfo columnInfo = (NamePolicyModuleDefaultsColumnInfo) realm.getSchema().getColumnInfo(some.test.NamePolicyModuleDefaults.class);
        long colKey = OsObject.createRow(table);
        cache.put(object, colKey);
        String realmGet$firstName = ((some_test_NamePolicyModuleDefaultsRealmProxyInterface) object).realmGet$firstName();
        if (realmGet$firstName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.firstNameColKey, colKey, realmGet$firstName, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.firstNameColKey, colKey, false);
        }
        String realmGet$lastName = ((some_test_NamePolicyModuleDefaultsRealmProxyInterface) object).realmGet$lastName();
        if (realmGet$lastName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.lastNameColKey, colKey, realmGet$lastName, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.lastNameColKey, colKey, false);
        }
        return colKey;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.NamePolicyModuleDefaults.class);
        long tableNativePtr = table.getNativePtr();
        NamePolicyModuleDefaultsColumnInfo columnInfo = (NamePolicyModuleDefaultsColumnInfo) realm.getSchema().getColumnInfo(some.test.NamePolicyModuleDefaults.class);
        some.test.NamePolicyModuleDefaults object = null;
        while (objects.hasNext()) {
            object = (some.test.NamePolicyModuleDefaults) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            long colKey = OsObject.createRow(table);
            cache.put(object, colKey);
            String realmGet$firstName = ((some_test_NamePolicyModuleDefaultsRealmProxyInterface) object).realmGet$firstName();
            if (realmGet$firstName != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.firstNameColKey, colKey, realmGet$firstName, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.firstNameColKey, colKey, false);
            }
            String realmGet$lastName = ((some_test_NamePolicyModuleDefaultsRealmProxyInterface) object).realmGet$lastName();
            if (realmGet$lastName != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.lastNameColKey, colKey, realmGet$lastName, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.lastNameColKey, colKey, false);
            }
        }
    }

    public static some.test.NamePolicyModuleDefaults createDetachedCopy(some.test.NamePolicyModuleDefaults realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.NamePolicyModuleDefaults unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new some.test.NamePolicyModuleDefaults();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.NamePolicyModuleDefaults) cachedObject.object;
            }
            unmanagedObject = (some.test.NamePolicyModuleDefaults) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        some_test_NamePolicyModuleDefaultsRealmProxyInterface unmanagedCopy = (some_test_NamePolicyModuleDefaultsRealmProxyInterface) unmanagedObject;
        some_test_NamePolicyModuleDefaultsRealmProxyInterface realmSource = (some_test_NamePolicyModuleDefaultsRealmProxyInterface) realmObject;
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
        StringBuilder stringBuilder = new StringBuilder("NamePolicyModuleDefaults = proxy[");
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
        long colKey = proxyState.getRow$realm().getObjectKey();

        int result = 17;
        result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0);
        result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0);
        result = 31 * result + (int) (colKey ^ (colKey >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        some_test_NamePolicyModuleDefaultsRealmProxy aNamePolicyModuleDefaults = (some_test_NamePolicyModuleDefaultsRealmProxy)o;

        BaseRealm realm = proxyState.getRealm$realm();
        BaseRealm otherRealm = aNamePolicyModuleDefaults.proxyState.getRealm$realm();
        String path = realm.getPath();
        String otherPath = otherRealm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;
        if (realm.isFrozen() != otherRealm.isFrozen()) return false;
        if (!realm.sharedRealm.getVersionID().equals(otherRealm.sharedRealm.getVersionID())) {
            return false;
        }

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aNamePolicyModuleDefaults.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getObjectKey() != aNamePolicyModuleDefaults.proxyState.getRow$realm().getObjectKey()) return false;

        return true;
    }
}
