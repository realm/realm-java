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
public class some_test_NamePolicyMixedClassSettingsRealmProxy extends some.test.NamePolicyMixedClassSettings
        implements RealmObjectProxy, some_test_NamePolicyMixedClassSettingsRealmProxyInterface {

    static final class NamePolicyMixedClassSettingsColumnInfo extends ColumnInfo {
        long firstNameIndex;
        long lastNameIndex;

        NamePolicyMixedClassSettingsColumnInfo(OsSchemaInfo schemaInfo) {
            super(2);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("customName");
            this.firstNameIndex = addColumnDetails("firstName", "first_name", objectSchemaInfo);
            this.lastNameIndex = addColumnDetails("lastName", "LastName", objectSchemaInfo);
        }

        NamePolicyMixedClassSettingsColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new NamePolicyMixedClassSettingsColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final NamePolicyMixedClassSettingsColumnInfo src = (NamePolicyMixedClassSettingsColumnInfo) rawSrc;
            final NamePolicyMixedClassSettingsColumnInfo dst = (NamePolicyMixedClassSettingsColumnInfo) rawDst;
            dst.firstNameIndex = src.firstNameIndex;
            dst.lastNameIndex = src.lastNameIndex;
        }
    }

    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private NamePolicyMixedClassSettingsColumnInfo columnInfo;
    private ProxyState<some.test.NamePolicyMixedClassSettings> proxyState;

    some_test_NamePolicyMixedClassSettingsRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (NamePolicyMixedClassSettingsColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<some.test.NamePolicyMixedClassSettings>(this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$firstName() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.firstNameIndex);
    }

    @Override
    public void realmSet$firstName(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.firstNameIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setString(columnInfo.firstNameIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.firstNameIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.firstNameIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$lastName() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.lastNameIndex);
    }

    @Override
    public void realmSet$lastName(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.lastNameIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setString(columnInfo.lastNameIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.lastNameIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.lastNameIndex, value);
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("customName", 2, 0);
        builder.addPersistedProperty("first_name", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("LastName", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static NamePolicyMixedClassSettingsColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new NamePolicyMixedClassSettingsColumnInfo(schemaInfo);
    }

    public static String getSimpleClassName() {
        return "customName";
    }

    public static final class ClassNameHelper {
        public static final String INTERNAL_CLASS_NAME = "customName";
    }

    @SuppressWarnings("cast")
    public static some.test.NamePolicyMixedClassSettings createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        final List<String> excludeFields = Collections.<String> emptyList();
        some.test.NamePolicyMixedClassSettings obj = realm.createObjectInternal(some.test.NamePolicyMixedClassSettings.class, true, excludeFields);

        final some_test_NamePolicyMixedClassSettingsRealmProxyInterface objProxy = (some_test_NamePolicyMixedClassSettingsRealmProxyInterface) obj;
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
    public static some.test.NamePolicyMixedClassSettings createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        final some.test.NamePolicyMixedClassSettings obj = new some.test.NamePolicyMixedClassSettings();
        final some_test_NamePolicyMixedClassSettingsRealmProxyInterface objProxy = (some_test_NamePolicyMixedClassSettingsRealmProxyInterface) obj;
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

    public static some.test.NamePolicyMixedClassSettings copyOrUpdate(Realm realm, some.test.NamePolicyMixedClassSettings object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
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
            return (some.test.NamePolicyMixedClassSettings) cachedRealmObject;
        }

        return copy(realm, object, update, cache);
    }

    public static some.test.NamePolicyMixedClassSettings copy(Realm realm, some.test.NamePolicyMixedClassSettings newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.NamePolicyMixedClassSettings) cachedRealmObject;
        }

        // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
        some.test.NamePolicyMixedClassSettings realmObject = realm.createObjectInternal(some.test.NamePolicyMixedClassSettings.class, false, Collections.<String>emptyList());
        cache.put(newObject, (RealmObjectProxy) realmObject);

        some_test_NamePolicyMixedClassSettingsRealmProxyInterface realmObjectSource = (some_test_NamePolicyMixedClassSettingsRealmProxyInterface) newObject;
        some_test_NamePolicyMixedClassSettingsRealmProxyInterface realmObjectCopy = (some_test_NamePolicyMixedClassSettingsRealmProxyInterface) realmObject;

        realmObjectCopy.realmSet$firstName(realmObjectSource.realmGet$firstName());
        realmObjectCopy.realmSet$lastName(realmObjectSource.realmGet$lastName());
        return realmObject;
    }

    public static long insert(Realm realm, some.test.NamePolicyMixedClassSettings object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.NamePolicyMixedClassSettings.class);
        long tableNativePtr = table.getNativePtr();
        NamePolicyMixedClassSettingsColumnInfo columnInfo = (NamePolicyMixedClassSettingsColumnInfo) realm.getSchema().getColumnInfo(some.test.NamePolicyMixedClassSettings.class);
        long rowIndex = OsObject.createRow(table);
        cache.put(object, rowIndex);
        String realmGet$firstName = ((some_test_NamePolicyMixedClassSettingsRealmProxyInterface) object).realmGet$firstName();
        if (realmGet$firstName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.firstNameIndex, rowIndex, realmGet$firstName, false);
        }
        String realmGet$lastName = ((some_test_NamePolicyMixedClassSettingsRealmProxyInterface) object).realmGet$lastName();
        if (realmGet$lastName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.lastNameIndex, rowIndex, realmGet$lastName, false);
        }
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.NamePolicyMixedClassSettings.class);
        long tableNativePtr = table.getNativePtr();
        NamePolicyMixedClassSettingsColumnInfo columnInfo = (NamePolicyMixedClassSettingsColumnInfo) realm.getSchema().getColumnInfo(some.test.NamePolicyMixedClassSettings.class);
        some.test.NamePolicyMixedClassSettings object = null;
        while (objects.hasNext()) {
            object = (some.test.NamePolicyMixedClassSettings) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            long rowIndex = OsObject.createRow(table);
            cache.put(object, rowIndex);
            String realmGet$firstName = ((some_test_NamePolicyMixedClassSettingsRealmProxyInterface) object).realmGet$firstName();
            if (realmGet$firstName != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.firstNameIndex, rowIndex, realmGet$firstName, false);
            }
            String realmGet$lastName = ((some_test_NamePolicyMixedClassSettingsRealmProxyInterface) object).realmGet$lastName();
            if (realmGet$lastName != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.lastNameIndex, rowIndex, realmGet$lastName, false);
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.NamePolicyMixedClassSettings object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.NamePolicyMixedClassSettings.class);
        long tableNativePtr = table.getNativePtr();
        NamePolicyMixedClassSettingsColumnInfo columnInfo = (NamePolicyMixedClassSettingsColumnInfo) realm.getSchema().getColumnInfo(some.test.NamePolicyMixedClassSettings.class);
        long rowIndex = OsObject.createRow(table);
        cache.put(object, rowIndex);
        String realmGet$firstName = ((some_test_NamePolicyMixedClassSettingsRealmProxyInterface) object).realmGet$firstName();
        if (realmGet$firstName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.firstNameIndex, rowIndex, realmGet$firstName, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.firstNameIndex, rowIndex, false);
        }
        String realmGet$lastName = ((some_test_NamePolicyMixedClassSettingsRealmProxyInterface) object).realmGet$lastName();
        if (realmGet$lastName != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.lastNameIndex, rowIndex, realmGet$lastName, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.lastNameIndex, rowIndex, false);
        }
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.NamePolicyMixedClassSettings.class);
        long tableNativePtr = table.getNativePtr();
        NamePolicyMixedClassSettingsColumnInfo columnInfo = (NamePolicyMixedClassSettingsColumnInfo) realm.getSchema().getColumnInfo(some.test.NamePolicyMixedClassSettings.class);
        some.test.NamePolicyMixedClassSettings object = null;
        while (objects.hasNext()) {
            object = (some.test.NamePolicyMixedClassSettings) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            long rowIndex = OsObject.createRow(table);
            cache.put(object, rowIndex);
            String realmGet$firstName = ((some_test_NamePolicyMixedClassSettingsRealmProxyInterface) object).realmGet$firstName();
            if (realmGet$firstName != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.firstNameIndex, rowIndex, realmGet$firstName, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.firstNameIndex, rowIndex, false);
            }
            String realmGet$lastName = ((some_test_NamePolicyMixedClassSettingsRealmProxyInterface) object).realmGet$lastName();
            if (realmGet$lastName != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.lastNameIndex, rowIndex, realmGet$lastName, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.lastNameIndex, rowIndex, false);
            }
        }
    }

    public static some.test.NamePolicyMixedClassSettings createDetachedCopy(some.test.NamePolicyMixedClassSettings realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.NamePolicyMixedClassSettings unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new some.test.NamePolicyMixedClassSettings();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.NamePolicyMixedClassSettings) cachedObject.object;
            }
            unmanagedObject = (some.test.NamePolicyMixedClassSettings) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        some_test_NamePolicyMixedClassSettingsRealmProxyInterface unmanagedCopy = (some_test_NamePolicyMixedClassSettingsRealmProxyInterface) unmanagedObject;
        some_test_NamePolicyMixedClassSettingsRealmProxyInterface realmSource = (some_test_NamePolicyMixedClassSettingsRealmProxyInterface) realmObject;
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
        StringBuilder stringBuilder = new StringBuilder("NamePolicyMixedClassSettings = proxy[");
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
        some_test_NamePolicyMixedClassSettingsRealmProxy aNamePolicyMixedClassSettings = (some_test_NamePolicyMixedClassSettingsRealmProxy)o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aNamePolicyMixedClassSettings.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aNamePolicyMixedClassSettings.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aNamePolicyMixedClassSettings.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }
}
