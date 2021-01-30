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
import io.realm.internal.Property;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.android.JsonUtils;
import io.realm.internal.core.NativeMixed;
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
public class some_test_EmbeddedClassSimpleParentRealmProxy extends some.test.EmbeddedClassSimpleParent
        implements RealmObjectProxy, some_test_EmbeddedClassSimpleParentRealmProxyInterface {

    static final class EmbeddedClassSimpleParentColumnInfo extends ColumnInfo {
        long idColKey;
        long childColKey;
        long childrenColKey;

        EmbeddedClassSimpleParentColumnInfo(OsSchemaInfo schemaInfo) {
            super(3);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("EmbeddedClassSimpleParent");
            this.idColKey = addColumnDetails("id", "id", objectSchemaInfo);
            this.childColKey = addColumnDetails("child", "child", objectSchemaInfo);
            this.childrenColKey = addColumnDetails("children", "children", objectSchemaInfo);
        }

        EmbeddedClassSimpleParentColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new EmbeddedClassSimpleParentColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final EmbeddedClassSimpleParentColumnInfo src = (EmbeddedClassSimpleParentColumnInfo) rawSrc;
            final EmbeddedClassSimpleParentColumnInfo dst = (EmbeddedClassSimpleParentColumnInfo) rawDst;
            dst.idColKey = src.idColKey;
            dst.childColKey = src.childColKey;
            dst.childrenColKey = src.childrenColKey;
        }
    }

    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private EmbeddedClassSimpleParentColumnInfo columnInfo;
    private ProxyState<some.test.EmbeddedClassSimpleParent> proxyState;
    private RealmList<some.test.EmbeddedClass> childrenRealmList;

    some_test_EmbeddedClassSimpleParentRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (EmbeddedClassSimpleParentColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<some.test.EmbeddedClassSimpleParent>(this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$id() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.idColKey);
    }

    @Override
    public void realmSet$id(String value) {
        if (proxyState.isUnderConstruction()) {
            // default value of the primary key is always ignored.
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        throw new io.realm.exceptions.RealmException("Primary key field 'id' cannot be changed after object was created.");
    }

    @Override
    public some.test.EmbeddedClass realmGet$child() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNullLink(columnInfo.childColKey)) {
            return null;
        }
        return proxyState.getRealm$realm().get(some.test.EmbeddedClass.class, proxyState.getRow$realm().getLink(columnInfo.childColKey), false, Collections.<String>emptyList());
    }

    @Override
    public void realmSet$child(some.test.EmbeddedClass value) {
        Realm realm = (Realm) proxyState.getRealm$realm();
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("child")) {
                return;
            }
            if (value != null && !RealmObject.isManaged(value)) {
                some.test.EmbeddedClass proxyObject = realm.createEmbeddedObject(some.test.EmbeddedClass.class, this, "child");
                some_test_EmbeddedClassRealmProxy.updateEmbeddedObject(realm, value, proxyObject, new HashMap<RealmModel, RealmObjectProxy>(), Collections.EMPTY_SET);
                value = proxyObject;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                // Table#nullifyLink() does not support default value. Just using Row.
                row.nullifyLink(columnInfo.childColKey);
                return;
            }
            proxyState.checkValidObject(value);
            row.getTable().setLink(columnInfo.childColKey, row.getObjectKey(), ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getObjectKey(), true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().nullifyLink(columnInfo.childColKey);
            return;
        }
        if (RealmObject.isManaged(value)) {
            proxyState.checkValidObject(value);
        }
        some.test.EmbeddedClass proxyObject = realm.createEmbeddedObject(some.test.EmbeddedClass.class, this, "child");
        some_test_EmbeddedClassRealmProxy.updateEmbeddedObject(realm, value, proxyObject, new HashMap<RealmModel, RealmObjectProxy>(), Collections.EMPTY_SET);
    }

    @Override
    public RealmList<some.test.EmbeddedClass> realmGet$children() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (childrenRealmList != null) {
            return childrenRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getModelList(columnInfo.childrenColKey);
            childrenRealmList = new RealmList<some.test.EmbeddedClass>(some.test.EmbeddedClass.class, osList, proxyState.getRealm$realm());
            return childrenRealmList;
        }
    }

    @Override
    public void realmSet$children(RealmList<some.test.EmbeddedClass> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("children")) {
                return;
            }
            // if the list contains unmanaged RealmObjects, convert them to managed.
            if (value != null && !value.isManaged()) {
                final Realm realm = (Realm) proxyState.getRealm$realm();
                final RealmList<some.test.EmbeddedClass> original = value;
                value = new RealmList<some.test.EmbeddedClass>();
                for (some.test.EmbeddedClass item : original) {
                    if (item == null || RealmObject.isManaged(item)) {
                        value.add(item);
                    } else {
                        value.add(realm.copyToRealm(item));
                    }
                }
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getModelList(columnInfo.childrenColKey);
        // For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.
        if (value != null && value.size() == osList.size()) {
            int objects = value.size();
            for (int i = 0; i < objects; i++) {
                some.test.EmbeddedClass linkedObject = value.get(i);
                proxyState.checkValidObject(linkedObject);
                osList.setRow(i, ((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getObjectKey());
            }
        } else {
            osList.removeAll();
            if (value == null) {
                return;
            }
            int objects = value.size();
            for (int i = 0; i < objects; i++) {
                some.test.EmbeddedClass linkedObject = value.get(i);
                proxyState.checkValidObject(linkedObject);
                osList.addRow(((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getObjectKey());
            }
        }
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("EmbeddedClassSimpleParent", false, 3, 0);
        builder.addPersistedProperty("id", RealmFieldType.STRING, Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedLinkProperty("child", RealmFieldType.OBJECT, "EmbeddedClass");
        builder.addPersistedLinkProperty("children", RealmFieldType.LIST, "EmbeddedClass");
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static EmbeddedClassSimpleParentColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new EmbeddedClassSimpleParentColumnInfo(schemaInfo);
    }

    public static String getSimpleClassName() {
        return "EmbeddedClassSimpleParent";
    }

    public static final class ClassNameHelper {
        public static final String INTERNAL_CLASS_NAME = "EmbeddedClassSimpleParent";
    }

    @SuppressWarnings("cast")
    public static some.test.EmbeddedClassSimpleParent createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        final List<String> excludeFields = new ArrayList<String>(2);
        some.test.EmbeddedClassSimpleParent obj = null;
        if (update) {
            Table table = realm.getTable(some.test.EmbeddedClassSimpleParent.class);
            EmbeddedClassSimpleParentColumnInfo columnInfo = (EmbeddedClassSimpleParentColumnInfo) realm.getSchema().getColumnInfo(some.test.EmbeddedClassSimpleParent.class);
            long pkColumnKey = columnInfo.idColKey;
            long objKey = Table.NO_MATCH;
            if (json.isNull("id")) {
                objKey = table.findFirstNull(pkColumnKey);
            } else {
                objKey = table.findFirstString(pkColumnKey, json.getString("id"));
            }
            if (objKey != Table.NO_MATCH) {
                final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
                try {
                    objectContext.set(realm, table.getUncheckedRow(objKey), realm.getSchema().getColumnInfo(some.test.EmbeddedClassSimpleParent.class), false, Collections.<String> emptyList());
                    obj = new io.realm.some_test_EmbeddedClassSimpleParentRealmProxy();
                } finally {
                    objectContext.clear();
                }
            }
        }
        if (obj == null) {
            if (json.has("child")) {
                excludeFields.add("child");
            }
            if (json.has("children")) {
                excludeFields.add("children");
            }
            if (json.has("id")) {
                if (json.isNull("id")) {
                    obj = (io.realm.some_test_EmbeddedClassSimpleParentRealmProxy) realm.createObjectInternal(some.test.EmbeddedClassSimpleParent.class, null, true, excludeFields);
                } else {
                    obj = (io.realm.some_test_EmbeddedClassSimpleParentRealmProxy) realm.createObjectInternal(some.test.EmbeddedClassSimpleParent.class, json.getString("id"), true, excludeFields);
                }
            } else {
                throw new IllegalArgumentException("JSON object doesn't have the primary key field 'id'.");
            }
        }

        final some_test_EmbeddedClassSimpleParentRealmProxyInterface objProxy = (some_test_EmbeddedClassSimpleParentRealmProxyInterface) obj;
        if (json.has("child")) {
            if (json.isNull("child")) {
                objProxy.realmSet$child(null);
            } else {
                some_test_EmbeddedClassRealmProxy.createOrUpdateEmbeddedUsingJsonObject(realm, (RealmModel)objProxy, "child", json.getJSONObject("child"), update);
            }
        }
        if (json.has("children")) {
            if (json.isNull("children")) {
                objProxy.realmSet$children(null);
            } else {
                objProxy.realmGet$children().clear();
                JSONArray array = json.getJSONArray("children");
                for (int i = 0; i < array.length(); i++) {
                    some_test_EmbeddedClassRealmProxy.createOrUpdateEmbeddedUsingJsonObject(realm, (RealmModel)objProxy, "children", array.getJSONObject(i), update);
                }
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.EmbeddedClassSimpleParent createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        boolean jsonHasPrimaryKey = false;
        final some.test.EmbeddedClassSimpleParent obj = new some.test.EmbeddedClassSimpleParent();
        final some_test_EmbeddedClassSimpleParentRealmProxyInterface objProxy = (some_test_EmbeddedClassSimpleParentRealmProxyInterface) obj;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (false) {
            } else if (name.equals("id")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$id((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$id(null);
                }
                jsonHasPrimaryKey = true;
            } else if (name.equals("child")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$child(null);
                } else {
                    some.test.EmbeddedClass childObj = some_test_EmbeddedClassRealmProxy.createUsingJsonStream(realm, reader);
                    objProxy.realmSet$child(childObj);
                }
            } else if (name.equals("children")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$children(null);
                } else {
                    objProxy.realmSet$children(new RealmList<some.test.EmbeddedClass>());
                    reader.beginArray();
                    while (reader.hasNext()) {
                        some.test.EmbeddedClass item = some_test_EmbeddedClassRealmProxy.createUsingJsonStream(realm, reader);
                        objProxy.realmGet$children().add(item);
                    }
                    reader.endArray();
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

    static some_test_EmbeddedClassSimpleParentRealmProxy newProxyInstance(BaseRealm realm, Row row) {
        // Ignore default values to avoid creating unexpected objects from RealmModel/RealmList fields
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        objectContext.set(realm, row, realm.getSchema().getColumnInfo(some.test.EmbeddedClassSimpleParent.class), false, Collections.<String>emptyList());
        io.realm.some_test_EmbeddedClassSimpleParentRealmProxy obj = new io.realm.some_test_EmbeddedClassSimpleParentRealmProxy();
        objectContext.clear();
        return obj;
    }

    public static some.test.EmbeddedClassSimpleParent copyOrUpdate(Realm realm, EmbeddedClassSimpleParentColumnInfo columnInfo, some.test.EmbeddedClassSimpleParent object, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
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
            return (some.test.EmbeddedClassSimpleParent) cachedRealmObject;
        }

        some.test.EmbeddedClassSimpleParent realmObject = null;
        boolean canUpdate = update;
        if (canUpdate) {
            Table table = realm.getTable(some.test.EmbeddedClassSimpleParent.class);
            long pkColumnKey = columnInfo.idColKey;
            String value = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$id();
            long objKey = Table.NO_MATCH;
            if (value == null) {
                objKey = table.findFirstNull(pkColumnKey);
            } else {
                objKey = table.findFirstString(pkColumnKey, value);
            }
            if (objKey == Table.NO_MATCH) {
                canUpdate = false;
            } else {
                try {
                    objectContext.set(realm, table.getUncheckedRow(objKey), columnInfo, false, Collections.<String> emptyList());
                    realmObject = new io.realm.some_test_EmbeddedClassSimpleParentRealmProxy();
                    cache.put(object, (RealmObjectProxy) realmObject);
                } finally {
                    objectContext.clear();
                }
            }
        }

        return (canUpdate) ? update(realm, columnInfo, realmObject, object, cache, flags) : copy(realm, columnInfo, object, update, cache, flags);
    }

    public static some.test.EmbeddedClassSimpleParent copy(Realm realm, EmbeddedClassSimpleParentColumnInfo columnInfo, some.test.EmbeddedClassSimpleParent newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.EmbeddedClassSimpleParent) cachedRealmObject;
        }

        some_test_EmbeddedClassSimpleParentRealmProxyInterface unmanagedSource = (some_test_EmbeddedClassSimpleParentRealmProxyInterface) newObject;

        Table table = realm.getTable(some.test.EmbeddedClassSimpleParent.class);
        OsObjectBuilder builder = new OsObjectBuilder(table, flags);

        // Add all non-"object reference" fields
        builder.addString(columnInfo.idColKey, unmanagedSource.realmGet$id());

        // Create the underlying object and cache it before setting any object/objectlist references
        // This will allow us to break any circular dependencies by using the object cache.
        Row row = builder.createNewObject();
        io.realm.some_test_EmbeddedClassSimpleParentRealmProxy managedCopy = newProxyInstance(realm, row);
        cache.put(newObject, managedCopy);

        // Finally add all fields that reference other Realm Objects, either directly or through a list
        some.test.EmbeddedClass childObj = unmanagedSource.realmGet$child();
        if (childObj == null) {
            managedCopy.realmSet$child(null);
        } else {
            some.test.EmbeddedClass cachechild = (some.test.EmbeddedClass) cache.get(childObj);
            if (cachechild != null) {
                throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: cachechild.toString()");
            } else {
                long objKey = ((RealmObjectProxy) managedCopy).realmGet$proxyState().getRow$realm().createEmbeddedObject(columnInfo.childColKey, RealmFieldType.OBJECT);
                Row linkedObjectRow = realm.getTable(some.test.EmbeddedClass.class).getUncheckedRow(objKey);
                some.test.EmbeddedClass linkedObject = some_test_EmbeddedClassRealmProxy.newProxyInstance(realm, linkedObjectRow);
                cache.put(childObj, (RealmObjectProxy) linkedObject);
                some_test_EmbeddedClassRealmProxy.updateEmbeddedObject(realm, childObj, linkedObject, cache, flags);
            }
        }

        RealmList<some.test.EmbeddedClass> childrenUnmanagedList = unmanagedSource.realmGet$children();
        if (childrenUnmanagedList != null) {
            RealmList<some.test.EmbeddedClass> childrenManagedList = managedCopy.realmGet$children();
            childrenManagedList.clear();
            for (int i = 0; i < childrenUnmanagedList.size(); i++) {
                some.test.EmbeddedClass childrenUnmanagedItem = childrenUnmanagedList.get(i);
                some.test.EmbeddedClass cachechildren = (some.test.EmbeddedClass) cache.get(childrenUnmanagedItem);
                if (cachechildren != null) {
                    throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: cachechildren.toString()");
                } else {
                    long objKey = childrenManagedList.getOsList().createAndAddEmbeddedObject();
                    Row linkedObjectRow = realm.getTable(some.test.EmbeddedClass.class).getUncheckedRow(objKey);
                    some.test.EmbeddedClass linkedObject = some_test_EmbeddedClassRealmProxy.newProxyInstance(realm, linkedObjectRow);
                    cache.put(childrenUnmanagedItem, (RealmObjectProxy) linkedObject);
                    some_test_EmbeddedClassRealmProxy.updateEmbeddedObject(realm, childrenUnmanagedItem, linkedObject, new HashMap<RealmModel, RealmObjectProxy>(), Collections.EMPTY_SET);
                }
            }
        }

        return managedCopy;
    }

    public static long insert(Realm realm, some.test.EmbeddedClassSimpleParent object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(some.test.EmbeddedClassSimpleParent.class);
        long tableNativePtr = table.getNativePtr();
        EmbeddedClassSimpleParentColumnInfo columnInfo = (EmbeddedClassSimpleParentColumnInfo) realm.getSchema().getColumnInfo(some.test.EmbeddedClassSimpleParent.class);
        long pkColumnKey = columnInfo.idColKey;
        String primaryKeyValue = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$id();
        long objKey = Table.NO_MATCH;
        if (primaryKeyValue == null) {
            objKey = Table.nativeFindFirstNull(tableNativePtr, pkColumnKey);
        } else {
            objKey = Table.nativeFindFirstString(tableNativePtr, pkColumnKey, primaryKeyValue);
        }
        if (objKey == Table.NO_MATCH) {
            objKey = OsObject.createRowWithPrimaryKey(table, pkColumnKey, primaryKeyValue);
        } else {
            Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
        }
        cache.put(object, objKey);

        some.test.EmbeddedClass childObj = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$child();
        if (childObj != null) {
            Long cachechild = cache.get(childObj);
            if (cachechild != null) {
                throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: " + cachechild.toString());
            } else {
                cachechild = some_test_EmbeddedClassRealmProxy.insert(realm, table, columnInfo.childColKey, objKey, childObj, cache);
            }
        }

        RealmList<some.test.EmbeddedClass> childrenList = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$children();
        if (childrenList != null) {
            OsList childrenOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.childrenColKey);
            for (some.test.EmbeddedClass childrenItem : childrenList) {
                Long cacheItemIndexchildren = cache.get(childrenItem);
                if (cacheItemIndexchildren != null) {
                    throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: " + cacheItemIndexchildren.toString());
                } else {
                    cacheItemIndexchildren = some_test_EmbeddedClassRealmProxy.insert(realm, table, columnInfo.childrenColKey, objKey, childrenItem, cache);
                }
            }
        }
        return objKey;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.EmbeddedClassSimpleParent.class);
        long tableNativePtr = table.getNativePtr();
        EmbeddedClassSimpleParentColumnInfo columnInfo = (EmbeddedClassSimpleParentColumnInfo) realm.getSchema().getColumnInfo(some.test.EmbeddedClassSimpleParent.class);
        long pkColumnKey = columnInfo.idColKey;
        some.test.EmbeddedClassSimpleParent object = null;
        while (objects.hasNext()) {
            object = (some.test.EmbeddedClassSimpleParent) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            String primaryKeyValue = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$id();
            long objKey = Table.NO_MATCH;
            if (primaryKeyValue == null) {
                objKey = Table.nativeFindFirstNull(tableNativePtr, pkColumnKey);
            } else {
                objKey = Table.nativeFindFirstString(tableNativePtr, pkColumnKey, primaryKeyValue);
            }
            if (objKey == Table.NO_MATCH) {
                objKey = OsObject.createRowWithPrimaryKey(table, pkColumnKey, primaryKeyValue);
            } else {
                Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
            }
            cache.put(object, objKey);

            some.test.EmbeddedClass childObj = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$child();
            if (childObj != null) {
                Long cachechild = cache.get(childObj);
                if (cachechild != null) {
                    throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: " + cachechild.toString());
                } else {
                    cachechild = some_test_EmbeddedClassRealmProxy.insert(realm, table, columnInfo.childColKey, objKey, childObj, cache);
                }
            }

            RealmList<some.test.EmbeddedClass> childrenList = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$children();
            if (childrenList != null) {
                OsList childrenOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.childrenColKey);
                for (some.test.EmbeddedClass childrenItem : childrenList) {
                    Long cacheItemIndexchildren = cache.get(childrenItem);
                    if (cacheItemIndexchildren != null) {
                        throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: " + cacheItemIndexchildren.toString());
                    } else {
                        cacheItemIndexchildren = some_test_EmbeddedClassRealmProxy.insert(realm, table, columnInfo.childrenColKey, objKey, childrenItem, cache);
                    }
                }
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.EmbeddedClassSimpleParent object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(some.test.EmbeddedClassSimpleParent.class);
        long tableNativePtr = table.getNativePtr();
        EmbeddedClassSimpleParentColumnInfo columnInfo = (EmbeddedClassSimpleParentColumnInfo) realm.getSchema().getColumnInfo(some.test.EmbeddedClassSimpleParent.class);
        long pkColumnKey = columnInfo.idColKey;
        String primaryKeyValue = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$id();
        long objKey = Table.NO_MATCH;
        if (primaryKeyValue == null) {
            objKey = Table.nativeFindFirstNull(tableNativePtr, pkColumnKey);
        } else {
            objKey = Table.nativeFindFirstString(tableNativePtr, pkColumnKey, primaryKeyValue);
        }
        if (objKey == Table.NO_MATCH) {
            objKey = OsObject.createRowWithPrimaryKey(table, pkColumnKey, primaryKeyValue);
        }
        cache.put(object, objKey);

        some.test.EmbeddedClass childObj = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$child();
        if (childObj != null) {
            Long cachechild = cache.get(childObj);
            if (cachechild != null) {
                throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: " + cachechild.toString());
            } else {
                cachechild = some_test_EmbeddedClassRealmProxy.insertOrUpdate(realm, table, columnInfo.childColKey, objKey, childObj, cache);
            }
        } else {
            Table.nativeNullifyLink(tableNativePtr, columnInfo.childColKey, objKey);
        }

        OsList childrenOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.childrenColKey);
        RealmList<some.test.EmbeddedClass> childrenList = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$children();
        childrenOsList.removeAll();
        if (childrenList != null) {
            for (some.test.EmbeddedClass childrenItem : childrenList) {
                Long cacheItemIndexchildren = cache.get(childrenItem);
                if (cacheItemIndexchildren != null) {
                    throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: " + cacheItemIndexchildren.toString());
                } else {
                    cacheItemIndexchildren = some_test_EmbeddedClassRealmProxy.insertOrUpdate(realm, table, columnInfo.childrenColKey, objKey, childrenItem, cache);
                }
            }
        }

        return objKey;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.EmbeddedClassSimpleParent.class);
        long tableNativePtr = table.getNativePtr();
        EmbeddedClassSimpleParentColumnInfo columnInfo = (EmbeddedClassSimpleParentColumnInfo) realm.getSchema().getColumnInfo(some.test.EmbeddedClassSimpleParent.class);
        long pkColumnKey = columnInfo.idColKey;
        some.test.EmbeddedClassSimpleParent object = null;
        while (objects.hasNext()) {
            object = (some.test.EmbeddedClassSimpleParent) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            String primaryKeyValue = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$id();
            long objKey = Table.NO_MATCH;
            if (primaryKeyValue == null) {
                objKey = Table.nativeFindFirstNull(tableNativePtr, pkColumnKey);
            } else {
                objKey = Table.nativeFindFirstString(tableNativePtr, pkColumnKey, primaryKeyValue);
            }
            if (objKey == Table.NO_MATCH) {
                objKey = OsObject.createRowWithPrimaryKey(table, pkColumnKey, primaryKeyValue);
            }
            cache.put(object, objKey);

            some.test.EmbeddedClass childObj = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$child();
            if (childObj != null) {
                Long cachechild = cache.get(childObj);
                if (cachechild != null) {
                    throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: " + cachechild.toString());
                } else {
                    cachechild = some_test_EmbeddedClassRealmProxy.insertOrUpdate(realm, table, columnInfo.childColKey, objKey, childObj, cache);
                }
            } else {
                Table.nativeNullifyLink(tableNativePtr, columnInfo.childColKey, objKey);
            }

            OsList childrenOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.childrenColKey);
            RealmList<some.test.EmbeddedClass> childrenList = ((some_test_EmbeddedClassSimpleParentRealmProxyInterface) object).realmGet$children();
            if (childrenList != null && childrenList.size() == childrenOsList.size()) {
                // For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.
                int objectCount = childrenList.size();
                for (int i = 0; i < objectCount; i++) {
                    some.test.EmbeddedClass childrenItem = childrenList.get(i);
                    Long cacheItemIndexchildren = cache.get(childrenItem);
                    if (cacheItemIndexchildren != null) {
                        throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: " + cacheItemIndexchildren.toString());
                    } else {
                        cacheItemIndexchildren = some_test_EmbeddedClassRealmProxy.insertOrUpdate(realm, table, columnInfo.childrenColKey, objKey, childrenItem, cache);
                    }
                }
            } else {
                childrenOsList.removeAll();
                if (childrenList != null) {
                    for (some.test.EmbeddedClass childrenItem : childrenList) {
                        Long cacheItemIndexchildren = cache.get(childrenItem);
                        if (cacheItemIndexchildren != null) {
                            throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: " + cacheItemIndexchildren.toString());
                        } else {
                            cacheItemIndexchildren = some_test_EmbeddedClassRealmProxy.insertOrUpdate(realm, table, columnInfo.childrenColKey, objKey, childrenItem, cache);
                        }
                    }
                }
            }

        }
    }

    public static some.test.EmbeddedClassSimpleParent createDetachedCopy(some.test.EmbeddedClassSimpleParent realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.EmbeddedClassSimpleParent unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new some.test.EmbeddedClassSimpleParent();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.EmbeddedClassSimpleParent) cachedObject.object;
            }
            unmanagedObject = (some.test.EmbeddedClassSimpleParent) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        some_test_EmbeddedClassSimpleParentRealmProxyInterface unmanagedCopy = (some_test_EmbeddedClassSimpleParentRealmProxyInterface) unmanagedObject;
        some_test_EmbeddedClassSimpleParentRealmProxyInterface realmSource = (some_test_EmbeddedClassSimpleParentRealmProxyInterface) realmObject;
        unmanagedCopy.realmSet$id(realmSource.realmGet$id());

        // Deep copy of child
        unmanagedCopy.realmSet$child(some_test_EmbeddedClassRealmProxy.createDetachedCopy(realmSource.realmGet$child(), currentDepth + 1, maxDepth, cache));

        // Deep copy of children
        if (currentDepth == maxDepth) {
            unmanagedCopy.realmSet$children(null);
        } else {
            RealmList<some.test.EmbeddedClass> managedchildrenList = realmSource.realmGet$children();
            RealmList<some.test.EmbeddedClass> unmanagedchildrenList = new RealmList<some.test.EmbeddedClass>();
            unmanagedCopy.realmSet$children(unmanagedchildrenList);
            int nextDepth = currentDepth + 1;
            int size = managedchildrenList.size();
            for (int i = 0; i < size; i++) {
                some.test.EmbeddedClass item = some_test_EmbeddedClassRealmProxy.createDetachedCopy(managedchildrenList.get(i), nextDepth, maxDepth, cache);
                unmanagedchildrenList.add(item);
            }
        }

        return unmanagedObject;
    }

    static some.test.EmbeddedClassSimpleParent update(Realm realm, EmbeddedClassSimpleParentColumnInfo columnInfo, some.test.EmbeddedClassSimpleParent realmObject, some.test.EmbeddedClassSimpleParent newObject, Map<RealmModel, RealmObjectProxy> cache, Set<ImportFlag> flags) {
        some_test_EmbeddedClassSimpleParentRealmProxyInterface realmObjectTarget = (some_test_EmbeddedClassSimpleParentRealmProxyInterface) realmObject;
        some_test_EmbeddedClassSimpleParentRealmProxyInterface realmObjectSource = (some_test_EmbeddedClassSimpleParentRealmProxyInterface) newObject;
        Table table = realm.getTable(some.test.EmbeddedClassSimpleParent.class);
        OsObjectBuilder builder = new OsObjectBuilder(table, flags);
        builder.addString(columnInfo.idColKey, realmObjectSource.realmGet$id());

        some.test.EmbeddedClass childObj = realmObjectSource.realmGet$child();
        if (childObj == null) {
            builder.addNull(columnInfo.childColKey);
        } else {
            // Embedded objects are created directly instead of using the builder.
            some.test.EmbeddedClass cachechild = (some.test.EmbeddedClass) cache.get(childObj);
            if (cachechild != null) {
                throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: cachechild.toString()");
            }

            long objKey = ((RealmObjectProxy) realmObject).realmGet$proxyState().getRow$realm().createEmbeddedObject(columnInfo.childColKey, RealmFieldType.OBJECT);
            Row row = realm.getTable(some.test.EmbeddedClass.class).getUncheckedRow(objKey);
            some.test.EmbeddedClass proxyObject = some_test_EmbeddedClassRealmProxy.newProxyInstance(realm, row);
            cache.put(childObj, (RealmObjectProxy) proxyObject);
            some_test_EmbeddedClassRealmProxy.updateEmbeddedObject(realm, childObj, proxyObject, cache, flags);
        }

        RealmList<some.test.EmbeddedClass> childrenUnmanagedList = realmObjectSource.realmGet$children();
        if (childrenUnmanagedList != null) {
            RealmList<some.test.EmbeddedClass> childrenManagedCopy = new RealmList<some.test.EmbeddedClass>();
            OsList targetList = realmObjectTarget.realmGet$children().getOsList();
            targetList.deleteAll();
            for (int i = 0; i < childrenUnmanagedList.size(); i++) {
                some.test.EmbeddedClass childrenUnmanagedItem = childrenUnmanagedList.get(i);
                some.test.EmbeddedClass cachechildren = (some.test.EmbeddedClass) cache.get(childrenUnmanagedItem);
                if (cachechildren != null) {
                    throw new IllegalArgumentException("Embedded objects can only have one parent pointing to them. This object was already copied, so another object is pointing to it: cachechildren.toString()");
                } else {
                    long objKey = targetList.createAndAddEmbeddedObject();
                    Row row = realm.getTable(some.test.EmbeddedClass.class).getUncheckedRow(objKey);
                    some.test.EmbeddedClass proxyObject = some_test_EmbeddedClassRealmProxy.newProxyInstance(realm, row);
                    cache.put(childrenUnmanagedItem, (RealmObjectProxy) proxyObject);
                    childrenManagedCopy.add(proxyObject);
                    some_test_EmbeddedClassRealmProxy.updateEmbeddedObject(realm, childrenUnmanagedItem, proxyObject, new HashMap<RealmModel, RealmObjectProxy>(), Collections.EMPTY_SET);
                }
            }
        } else {
            builder.addObjectList(columnInfo.childrenColKey, new RealmList<some.test.EmbeddedClass>());
        }

        builder.updateExistingTopLevelObject();
        return realmObject;
    }

    @Override
    @SuppressWarnings("ArrayToString")
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("EmbeddedClassSimpleParent = proxy[");
        stringBuilder.append("{id:");
        stringBuilder.append(realmGet$id() != null ? realmGet$id() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{child:");
        stringBuilder.append(realmGet$child() != null ? "EmbeddedClass" : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{children:");
        stringBuilder.append("RealmList<EmbeddedClass>[").append(realmGet$children().size()).append("]");
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
        some_test_EmbeddedClassSimpleParentRealmProxy aEmbeddedClassSimpleParent = (some_test_EmbeddedClassSimpleParentRealmProxy)o;

        BaseRealm realm = proxyState.getRealm$realm();
        BaseRealm otherRealm = aEmbeddedClassSimpleParent.proxyState.getRealm$realm();
        String path = realm.getPath();
        String otherPath = otherRealm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;
        if (realm.isFrozen() != otherRealm.isFrozen()) return false;
        if (!realm.sharedRealm.getVersionID().equals(otherRealm.sharedRealm.getVersionID())) {
            return false;
        }

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aEmbeddedClassSimpleParent.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getObjectKey() != aEmbeddedClassSimpleParent.proxyState.getRow$realm().getObjectKey()) return false;

        return true;
    }
}
