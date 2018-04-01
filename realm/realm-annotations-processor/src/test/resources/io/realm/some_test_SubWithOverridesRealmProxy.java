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
public class some_test_SubWithOverridesRealmProxy extends some.test.SubWithOverrides
        implements RealmObjectProxy, some_test_SubWithOverridesRealmProxyInterface {

    static final class SubWithOverridesColumnInfo extends ColumnInfo {
        long nameIndex;
        long idIndex;
        long childIndex;

        SubWithOverridesColumnInfo(OsSchemaInfo schemaInfo) {
            super(3);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("SubWithOverrides");
            this.nameIndex = addColumnDetails("name", "name", objectSchemaInfo);
            this.idIndex = addColumnDetails("id", "id", objectSchemaInfo);
            this.childIndex = addColumnDetails("child", "child", objectSchemaInfo);
        }

        SubWithOverridesColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new SubWithOverridesColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final SubWithOverridesColumnInfo src = (SubWithOverridesColumnInfo) rawSrc;
            final SubWithOverridesColumnInfo dst = (SubWithOverridesColumnInfo) rawDst;
            dst.nameIndex = src.nameIndex;
            dst.idIndex = src.idIndex;
            dst.childIndex = src.childIndex;
        }
    }

    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private SubWithOverridesColumnInfo columnInfo;
    private ProxyState<some.test.SubWithOverrides> proxyState;

    some_test_SubWithOverridesRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (SubWithOverridesColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<some.test.SubWithOverrides>(this);
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
    public long realmGet$id() {
        proxyState.getRealm$realm().checkIfValid();
        return (long) proxyState.getRow$realm().getLong(columnInfo.idIndex);
    }

    @Override
    public void realmSet$id(long value) {
        if (proxyState.isUnderConstruction()) {
            // default value of the primary key is always ignored.
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        throw new io.realm.exceptions.RealmException("Primary key field 'id' cannot be changed after object was created.");
    }

    @Override
    public some.test.Sub realmGet$child() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNullLink(columnInfo.childIndex)) {
            return null;
        }
        return proxyState.getRealm$realm().get(some.test.Sub.class, proxyState.getRow$realm().getLink(columnInfo.childIndex), false, Collections.<String>emptyList());
    }

    @Override
    public void realmSet$child(some.test.Sub value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("child")) {
                return;
            }
            if (value != null && !RealmObject.isManaged(value)) {
                value = ((Realm) proxyState.getRealm$realm()).copyToRealm(value);
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                // Table#nullifyLink() does not support default value. Just using Row.
                row.nullifyLink(columnInfo.childIndex);
                return;
            }
            proxyState.checkValidObject(value);
            row.getTable().setLink(columnInfo.childIndex, row.getIndex(), ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getIndex(), true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().nullifyLink(columnInfo.childIndex);
            return;
        }
        proxyState.checkValidObject(value);
        proxyState.getRow$realm().setLink(columnInfo.childIndex, ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getIndex());
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("SubWithOverrides", 3, 0);
        builder.addPersistedProperty("name", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("id", RealmFieldType.INTEGER, Property.PRIMARY_KEY, Property.INDEXED, Property.REQUIRED);
        builder.addPersistedLinkProperty("child", RealmFieldType.OBJECT, "Sub");
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static SubWithOverridesColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new SubWithOverridesColumnInfo(schemaInfo);
    }

    public static String getSimpleClassName() {
        return "SubWithOverrides";
    }

    public static final class ClassNameHelper {
        public static final String INTERNAL_CLASS_NAME = "SubWithOverrides";
    }

    @SuppressWarnings("cast")
    public static some.test.SubWithOverrides createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        final List<String> excludeFields = new ArrayList<String>(1);
        some.test.SubWithOverrides obj = null;
        if (update) {
            Table table = realm.getTable(some.test.SubWithOverrides.class);
            SubWithOverridesColumnInfo columnInfo = (SubWithOverridesColumnInfo) realm.getSchema().getColumnInfo(some.test.SubWithOverrides.class);
            long pkColumnIndex = columnInfo.idIndex;
            long rowIndex = Table.NO_MATCH;
            if (!json.isNull("id")) {
                rowIndex = table.findFirstLong(pkColumnIndex, json.getLong("id"));
            }
            if (rowIndex != Table.NO_MATCH) {
                final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
                try {
                    objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.getSchema().getColumnInfo(some.test.SubWithOverrides.class), false, Collections.<String> emptyList());
                    obj = new io.realm.some_test_SubWithOverridesRealmProxy();
                } finally {
                    objectContext.clear();
                }
            }
        }
        if (obj == null) {
            if (json.has("child")) {
                excludeFields.add("child");
            }
            if (json.has("id")) {
                if (json.isNull("id")) {
                    obj = (io.realm.some_test_SubWithOverridesRealmProxy) realm.createObjectInternal(some.test.SubWithOverrides.class, null, true, excludeFields);
                } else {
                    obj = (io.realm.some_test_SubWithOverridesRealmProxy) realm.createObjectInternal(some.test.SubWithOverrides.class, json.getLong("id"), true, excludeFields);
                }
            } else {
                throw new IllegalArgumentException("JSON object doesn't have the primary key field 'id'.");
            }
        }

        final some_test_SubWithOverridesRealmProxyInterface objProxy = (some_test_SubWithOverridesRealmProxyInterface) obj;
        if (json.has("name")) {
            if (json.isNull("name")) {
                objProxy.realmSet$name(null);
            } else {
                objProxy.realmSet$name((String) json.getString("name"));
            }
        }
        if (json.has("child")) {
            if (json.isNull("child")) {
                objProxy.realmSet$child(null);
            } else {
                some.test.Sub childObj = some_test_SubRealmProxy.createOrUpdateUsingJsonObject(realm, json.getJSONObject("child"), update);
                objProxy.realmSet$child(childObj);
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.SubWithOverrides createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        boolean jsonHasPrimaryKey = false;
        final some.test.SubWithOverrides obj = new some.test.SubWithOverrides();
        final some_test_SubWithOverridesRealmProxyInterface objProxy = (some_test_SubWithOverridesRealmProxyInterface) obj;
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
            } else if (name.equals("id")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$id((long) reader.nextLong());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'id' to null.");
                }
                jsonHasPrimaryKey = true;
            } else if (name.equals("child")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$child(null);
                } else {
                    some.test.Sub childObj = some_test_SubRealmProxy.createUsingJsonStream(realm, reader);
                    objProxy.realmSet$child(childObj);
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        if (!jsonHasPrimaryKey) {
            throw new IllegalArgumentException("JSON object doesn't have the primary key field 'id'.");
        }
        return realm.copyToRealm(obj);
    }

    public static some.test.SubWithOverrides copyOrUpdate(Realm realm, some.test.SubWithOverrides object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
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
            return (some.test.SubWithOverrides) cachedRealmObject;
        }

        some.test.SubWithOverrides realmObject = null;
        boolean canUpdate = update;
        if (canUpdate) {
            Table table = realm.getTable(some.test.SubWithOverrides.class);
            SubWithOverridesColumnInfo columnInfo = (SubWithOverridesColumnInfo) realm.getSchema().getColumnInfo(some.test.SubWithOverrides.class);
            long pkColumnIndex = columnInfo.idIndex;
            long rowIndex = table.findFirstLong(pkColumnIndex, ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id());
            if (rowIndex == Table.NO_MATCH) {
                canUpdate = false;
            } else {
                try {
                    objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.getSchema().getColumnInfo(some.test.SubWithOverrides.class), false, Collections.<String> emptyList());
                    realmObject = new io.realm.some_test_SubWithOverridesRealmProxy();
                    cache.put(object, (RealmObjectProxy) realmObject);
                } finally {
                    objectContext.clear();
                }
            }
        }

        return (canUpdate) ? update(realm, realmObject, object, cache) : copy(realm, object, update, cache);
    }

    public static some.test.SubWithOverrides copy(Realm realm, some.test.SubWithOverrides newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.SubWithOverrides) cachedRealmObject;
        }

        // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
        some.test.SubWithOverrides realmObject = realm.createObjectInternal(some.test.SubWithOverrides.class, ((some_test_SubWithOverridesRealmProxyInterface) newObject).realmGet$id(), false, Collections.<String>emptyList());
        cache.put(newObject, (RealmObjectProxy) realmObject);

        some_test_SubWithOverridesRealmProxyInterface realmObjectSource = (some_test_SubWithOverridesRealmProxyInterface) newObject;
        some_test_SubWithOverridesRealmProxyInterface realmObjectCopy = (some_test_SubWithOverridesRealmProxyInterface) realmObject;

        realmObjectCopy.realmSet$name(realmObjectSource.realmGet$name());

        some.test.Sub childObj = realmObjectSource.realmGet$child();
        if (childObj == null) {
            realmObjectCopy.realmSet$child(null);
        } else {
            some.test.Sub cachechild = (some.test.Sub) cache.get(childObj);
            if (cachechild != null) {
                realmObjectCopy.realmSet$child(cachechild);
            } else {
                realmObjectCopy.realmSet$child(some_test_SubRealmProxy.copyOrUpdate(realm, childObj, update, cache));
            }
        }
        return realmObject;
    }

    public static long insert(Realm realm, some.test.SubWithOverrides object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.SubWithOverrides.class);
        long tableNativePtr = table.getNativePtr();
        SubWithOverridesColumnInfo columnInfo = (SubWithOverridesColumnInfo) realm.getSchema().getColumnInfo(some.test.SubWithOverrides.class);
        long pkColumnIndex = columnInfo.idIndex;
        long rowIndex = Table.NO_MATCH;
        Object primaryKeyValue = ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id();
        if (primaryKeyValue != null) {
            rowIndex = Table.nativeFindFirstInt(tableNativePtr, pkColumnIndex, ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id());
        }
        if (rowIndex == Table.NO_MATCH) {
            rowIndex = OsObject.createRowWithPrimaryKey(table, pkColumnIndex, ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id());
        } else {
            Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
        }
        cache.put(object, rowIndex);
        String realmGet$name = ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$name();
        if (realmGet$name != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.nameIndex, rowIndex, realmGet$name, false);
        }

        some.test.Sub childObj = ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$child();
        if (childObj != null) {
            Long cachechild = cache.get(childObj);
            if (cachechild == null) {
                cachechild = some_test_SubRealmProxy.insert(realm, childObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.childIndex, rowIndex, cachechild, false);
        }
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.SubWithOverrides.class);
        long tableNativePtr = table.getNativePtr();
        SubWithOverridesColumnInfo columnInfo = (SubWithOverridesColumnInfo) realm.getSchema().getColumnInfo(some.test.SubWithOverrides.class);
        long pkColumnIndex = columnInfo.idIndex;
        some.test.SubWithOverrides object = null;
        while (objects.hasNext()) {
            object = (some.test.SubWithOverrides) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            long rowIndex = Table.NO_MATCH;
            Object primaryKeyValue = ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id();
            if (primaryKeyValue != null) {
                rowIndex = Table.nativeFindFirstInt(tableNativePtr, pkColumnIndex, ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id());
            }
            if (rowIndex == Table.NO_MATCH) {
                rowIndex = OsObject.createRowWithPrimaryKey(table, pkColumnIndex, ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id());
            } else {
                Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
            }
            cache.put(object, rowIndex);
            String realmGet$name = ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$name();
            if (realmGet$name != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.nameIndex, rowIndex, realmGet$name, false);
            }

            some.test.Sub childObj = ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$child();
            if (childObj != null) {
                Long cachechild = cache.get(childObj);
                if (cachechild == null) {
                    cachechild = some_test_SubRealmProxy.insert(realm, childObj, cache);
                }
                table.setLink(columnInfo.childIndex, rowIndex, cachechild, false);
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.SubWithOverrides object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.SubWithOverrides.class);
        long tableNativePtr = table.getNativePtr();
        SubWithOverridesColumnInfo columnInfo = (SubWithOverridesColumnInfo) realm.getSchema().getColumnInfo(some.test.SubWithOverrides.class);
        long pkColumnIndex = columnInfo.idIndex;
        long rowIndex = Table.NO_MATCH;
        Object primaryKeyValue = ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id();
        if (primaryKeyValue != null) {
            rowIndex = Table.nativeFindFirstInt(tableNativePtr, pkColumnIndex, ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id());
        }
        if (rowIndex == Table.NO_MATCH) {
            rowIndex = OsObject.createRowWithPrimaryKey(table, pkColumnIndex, ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id());
        }
        cache.put(object, rowIndex);
        String realmGet$name = ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$name();
        if (realmGet$name != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.nameIndex, rowIndex, realmGet$name, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.nameIndex, rowIndex, false);
        }

        some.test.Sub childObj = ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$child();
        if (childObj != null) {
            Long cachechild = cache.get(childObj);
            if (cachechild == null) {
                cachechild = some_test_SubRealmProxy.insertOrUpdate(realm, childObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.childIndex, rowIndex, cachechild, false);
        } else {
            Table.nativeNullifyLink(tableNativePtr, columnInfo.childIndex, rowIndex);
        }
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.SubWithOverrides.class);
        long tableNativePtr = table.getNativePtr();
        SubWithOverridesColumnInfo columnInfo = (SubWithOverridesColumnInfo) realm.getSchema().getColumnInfo(some.test.SubWithOverrides.class);
        long pkColumnIndex = columnInfo.idIndex;
        some.test.SubWithOverrides object = null;
        while (objects.hasNext()) {
            object = (some.test.SubWithOverrides) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            long rowIndex = Table.NO_MATCH;
            Object primaryKeyValue = ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id();
            if (primaryKeyValue != null) {
                rowIndex = Table.nativeFindFirstInt(tableNativePtr, pkColumnIndex, ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id());
            }
            if (rowIndex == Table.NO_MATCH) {
                rowIndex = OsObject.createRowWithPrimaryKey(table, pkColumnIndex, ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$id());
            }
            cache.put(object, rowIndex);
            String realmGet$name = ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$name();
            if (realmGet$name != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.nameIndex, rowIndex, realmGet$name, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.nameIndex, rowIndex, false);
            }

            some.test.Sub childObj = ((some_test_SubWithOverridesRealmProxyInterface) object).realmGet$child();
            if (childObj != null) {
                Long cachechild = cache.get(childObj);
                if (cachechild == null) {
                    cachechild = some_test_SubRealmProxy.insertOrUpdate(realm, childObj, cache);
                }
                Table.nativeSetLink(tableNativePtr, columnInfo.childIndex, rowIndex, cachechild, false);
            } else {
                Table.nativeNullifyLink(tableNativePtr, columnInfo.childIndex, rowIndex);
            }
        }
    }

    public static some.test.SubWithOverrides createDetachedCopy(some.test.SubWithOverrides realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.SubWithOverrides unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new some.test.SubWithOverrides();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.SubWithOverrides) cachedObject.object;
            }
            unmanagedObject = (some.test.SubWithOverrides) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        some_test_SubWithOverridesRealmProxyInterface unmanagedCopy = (some_test_SubWithOverridesRealmProxyInterface) unmanagedObject;
        some_test_SubWithOverridesRealmProxyInterface realmSource = (some_test_SubWithOverridesRealmProxyInterface) realmObject;
        unmanagedCopy.realmSet$name(realmSource.realmGet$name());
        unmanagedCopy.realmSet$id(realmSource.realmGet$id());

        // Deep copy of child
        unmanagedCopy.realmSet$child(some_test_SubRealmProxy.createDetachedCopy(realmSource.realmGet$child(), currentDepth + 1, maxDepth, cache));

        return unmanagedObject;
    }

    static some.test.SubWithOverrides update(Realm realm, some.test.SubWithOverrides realmObject, some.test.SubWithOverrides newObject, Map<RealmModel, RealmObjectProxy> cache) {
        some_test_SubWithOverridesRealmProxyInterface realmObjectTarget = (some_test_SubWithOverridesRealmProxyInterface) realmObject;
        some_test_SubWithOverridesRealmProxyInterface realmObjectSource = (some_test_SubWithOverridesRealmProxyInterface) newObject;
        realmObjectTarget.realmSet$name(realmObjectSource.realmGet$name());
        some.test.Sub childObj = realmObjectSource.realmGet$child();
        if (childObj == null) {
            realmObjectTarget.realmSet$child(null);
        } else {
            some.test.Sub cachechild = (some.test.Sub) cache.get(childObj);
            if (cachechild != null) {
                realmObjectTarget.realmSet$child(cachechild);
            } else {
                realmObjectTarget.realmSet$child(some_test_SubRealmProxy.copyOrUpdate(realm, childObj, true, cache));
            }
        }
        return realmObject;
    }

    @Override
    @SuppressWarnings("ArrayToString")
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("SubWithOverrides = proxy[");
        stringBuilder.append("{name:");
        stringBuilder.append(realmGet$name() != null ? realmGet$name() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{id:");
        stringBuilder.append(realmGet$id());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{child:");
        stringBuilder.append(realmGet$child() != null ? "Sub" : "null");
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
        some_test_SubWithOverridesRealmProxy aSubWithOverrides = (some_test_SubWithOverridesRealmProxy)o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aSubWithOverrides.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aSubWithOverrides.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aSubWithOverrides.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }
}
