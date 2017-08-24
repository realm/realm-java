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
import io.realm.internal.OsSchemaInfo;
import io.realm.internal.Property;
import io.realm.internal.ProxyUtils;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.SharedRealm;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
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
public class AllTypesRealmProxy extends some.test.AllTypes
    implements RealmObjectProxy, AllTypesRealmProxyInterface {

    static final class AllTypesColumnInfo extends ColumnInfo {
        long columnStringIndex;
        long columnLongIndex;
        long columnFloatIndex;
        long columnDoubleIndex;
        long columnBooleanIndex;
        long columnDateIndex;
        long columnBinaryIndex;
        long columnMutableRealmIntegerIndex;
        long columnObjectIndex;
        long columnRealmListIndex;

        AllTypesColumnInfo(OsSchemaInfo schemaInfo) {
            super(10);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("AllTypes");
            this.columnStringIndex = addColumnDetails("columnString", objectSchemaInfo);
            this.columnLongIndex = addColumnDetails("columnLong", objectSchemaInfo);
            this.columnFloatIndex = addColumnDetails("columnFloat", objectSchemaInfo);
            this.columnDoubleIndex = addColumnDetails("columnDouble", objectSchemaInfo);
            this.columnBooleanIndex = addColumnDetails("columnBoolean", objectSchemaInfo);
            this.columnDateIndex = addColumnDetails("columnDate", objectSchemaInfo);
            this.columnBinaryIndex = addColumnDetails("columnBinary", objectSchemaInfo);
            this.columnMutableRealmIntegerIndex = addColumnDetails("columnMutableRealmInteger", objectSchemaInfo);
            this.columnObjectIndex = addColumnDetails("columnObject", objectSchemaInfo);
            this.columnRealmListIndex = addColumnDetails("columnRealmList", objectSchemaInfo);
            addBacklinkDetails(schemaInfo, "parentObjects", "AllTypes", "columnObject");
        }

        AllTypesColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new AllTypesColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final AllTypesColumnInfo src = (AllTypesColumnInfo) rawSrc;
            final AllTypesColumnInfo dst = (AllTypesColumnInfo) rawDst;
            dst.columnStringIndex = src.columnStringIndex;
            dst.columnLongIndex = src.columnLongIndex;
            dst.columnFloatIndex = src.columnFloatIndex;
            dst.columnDoubleIndex = src.columnDoubleIndex;
            dst.columnBooleanIndex = src.columnBooleanIndex;
            dst.columnDateIndex = src.columnDateIndex;
            dst.columnBinaryIndex = src.columnBinaryIndex;
            dst.columnMutableRealmIntegerIndex = src.columnMutableRealmIntegerIndex;
            dst.columnObjectIndex = src.columnObjectIndex;
            dst.columnRealmListIndex = src.columnRealmListIndex;
        }
    }

    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("columnString");
        fieldNames.add("columnLong");
        fieldNames.add("columnFloat");
        fieldNames.add("columnDouble");
        fieldNames.add("columnBoolean");
        fieldNames.add("columnDate");
        fieldNames.add("columnBinary");
        fieldNames.add("columnMutableRealmInteger");
        fieldNames.add("columnObject");
        fieldNames.add("columnRealmList");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    private AllTypesColumnInfo columnInfo;
    private ProxyState<some.test.AllTypes> proxyState;
    private final MutableRealmInteger.Managed columnMutableRealmIntegerMutableRealmInteger = new MutableRealmInteger.Managed<some.test.AllTypes>() {
                @Override protected ProxyState<some.test.AllTypes> getProxyState() { return proxyState; }
                @Override protected long getColumnIndex() { return columnInfo.columnMutableRealmIntegerIndex; }
            };
    private RealmList<some.test.AllTypes> columnRealmListRealmList;
    private RealmResults<some.test.AllTypes> parentObjectsBacklinks;

    AllTypesRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (AllTypesColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<some.test.AllTypes>(this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$columnString() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.columnStringIndex);
    }

    @Override
    public void realmSet$columnString(String value) {
        if (proxyState.isUnderConstruction()) {
            // default value of the primary key is always ignored.
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        throw new io.realm.exceptions.RealmException("Primary key field 'columnString' cannot be changed after object was created.");
    }

    @Override
    @SuppressWarnings("cast")
    public long realmGet$columnLong() {
        proxyState.getRealm$realm().checkIfValid();
        return (long) proxyState.getRow$realm().getLong(columnInfo.columnLongIndex);
    }

    @Override
    public void realmSet$columnLong(long value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setLong(columnInfo.columnLongIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.columnLongIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public float realmGet$columnFloat() {
        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.columnFloatIndex);
    }

    @Override
    public void realmSet$columnFloat(float value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setFloat(columnInfo.columnFloatIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setFloat(columnInfo.columnFloatIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public double realmGet$columnDouble() {
        proxyState.getRealm$realm().checkIfValid();
        return (double) proxyState.getRow$realm().getDouble(columnInfo.columnDoubleIndex);
    }

    @Override
    public void realmSet$columnDouble(double value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setDouble(columnInfo.columnDoubleIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setDouble(columnInfo.columnDoubleIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public boolean realmGet$columnBoolean() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.columnBooleanIndex);
    }

    @Override
    public void realmSet$columnBoolean(boolean value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.columnBooleanIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.columnBooleanIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Date realmGet$columnDate() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.util.Date) proxyState.getRow$realm().getDate(columnInfo.columnDateIndex);
    }

    @Override
    public void realmSet$columnDate(Date value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnDate' to null.");
            }
            row.getTable().setDate(columnInfo.columnDateIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'columnDate' to null.");
        }
        proxyState.getRow$realm().setDate(columnInfo.columnDateIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public byte[] realmGet$columnBinary() {
        proxyState.getRealm$realm().checkIfValid();
        return (byte[]) proxyState.getRow$realm().getBinaryByteArray(columnInfo.columnBinaryIndex);
    }

    @Override
    public void realmSet$columnBinary(byte[] value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnBinary' to null.");
            }
            row.getTable().setBinaryByteArray(columnInfo.columnBinaryIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'columnBinary' to null.");
        }
        proxyState.getRow$realm().setBinaryByteArray(columnInfo.columnBinaryIndex, value);
    }

    @Override
    public MutableRealmInteger realmGet$columnMutableRealmInteger() {
        proxyState.getRealm$realm().checkIfValid();
        return this.columnMutableRealmIntegerMutableRealmInteger;
    }

    @Override
    public some.test.AllTypes realmGet$columnObject() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNullLink(columnInfo.columnObjectIndex)) {
            return null;
        }
        return proxyState.getRealm$realm().get(some.test.AllTypes.class, proxyState.getRow$realm().getLink(columnInfo.columnObjectIndex), false, Collections.<String>emptyList());
    }

    @Override
    public void realmSet$columnObject(some.test.AllTypes value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnObject")) {
                return;
            }
            if (value != null && !RealmObject.isManaged(value)) {
                value = ((Realm) proxyState.getRealm$realm()).copyToRealm(value);
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                // Table#nullifyLink() does not support default value. Just using Row.
                row.nullifyLink(columnInfo.columnObjectIndex);
                return;
            }
            if (!RealmObject.isValid(value)) {
                throw new IllegalArgumentException("'value' is not a valid managed object.");
            }
            if (((RealmObjectProxy) value).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm()) {
                throw new IllegalArgumentException("'value' belongs to a different Realm.");
            }
            row.getTable().setLink(columnInfo.columnObjectIndex, row.getIndex(), ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getIndex(), true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().nullifyLink(columnInfo.columnObjectIndex);
            return;
        }
        if (!(RealmObject.isManaged(value) && RealmObject.isValid(value))) {
            throw new IllegalArgumentException("'value' is not a valid managed object.");
        }
        if (((RealmObjectProxy) value).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm()) {
            throw new IllegalArgumentException("'value' belongs to a different Realm.");
        }
        proxyState.getRow$realm().setLink(columnInfo.columnObjectIndex, ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getIndex());
    }

    @Override
    public RealmList<some.test.AllTypes> realmGet$columnRealmList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnRealmListRealmList != null) {
            return columnRealmListRealmList;
        } else {
            LinkView linkView = proxyState.getRow$realm().getLinkList(columnInfo.columnRealmListIndex);
            columnRealmListRealmList = new RealmList<some.test.AllTypes>(some.test.AllTypes.class, linkView, proxyState.getRealm$realm());
            return columnRealmListRealmList;
        }
    }

    @Override
    public void realmSet$columnRealmList(RealmList<some.test.AllTypes> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnRealmList")) {
                return;
            }
            if (value != null && !value.isManaged()) {
                final Realm realm = (Realm) proxyState.getRealm$realm();
                final RealmList<some.test.AllTypes> original = value;
                value = new RealmList<some.test.AllTypes>();
                for (some.test.AllTypes item : original) {
                    if (item == null || RealmObject.isManaged(item)) {
                        value.add(item);
                    } else {
                        value.add(realm.copyToRealm(item));
                    }
                }
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        LinkView links = proxyState.getRow$realm().getLinkList(columnInfo.columnRealmListIndex);
        links.clear();
        if (value == null) {
            return;
        }
        for (RealmModel linkedObject : (RealmList<? extends RealmModel>) value) {
            if (!(RealmObject.isManaged(linkedObject) && RealmObject.isValid(linkedObject))) {
                throw new IllegalArgumentException("Each element of 'value' must be a valid managed object.");
            }
            if (((RealmObjectProxy) linkedObject).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm()) {
                throw new IllegalArgumentException("Each element of 'value' must belong to the same Realm.");
            }
            links.add(((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getIndex());
        }
    }

    @Override
    public RealmResults<some.test.AllTypes> realmGet$parentObjects() {
        BaseRealm realm = proxyState.getRealm$realm();
        realm.checkIfValid();
        proxyState.getRow$realm().checkIfAttached();
        if (parentObjectsBacklinks == null) {
            parentObjectsBacklinks = RealmResults.createBacklinkResults(realm, proxyState.getRow$realm(), some.test.AllTypes.class, "columnObject");
        }
        return parentObjectsBacklinks;
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("AllTypes");
        builder.addPersistedProperty("columnString", RealmFieldType.STRING, Property.PRIMARY_KEY, Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("columnLong", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnFloat", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnDouble", RealmFieldType.DOUBLE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnBoolean", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnDate", RealmFieldType.DATE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnBinary", RealmFieldType.BINARY, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnMutableRealmInteger", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedLinkProperty("columnObject", RealmFieldType.OBJECT, "AllTypes");
        builder.addPersistedLinkProperty("columnRealmList", RealmFieldType.LIST, "AllTypes");
        builder.addComputedLinkProperty("parentObjects", "AllTypes", "columnObject");
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static AllTypesColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new AllTypesColumnInfo(schemaInfo);
    }

    public static String getTableName() {
        return "class_AllTypes";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static some.test.AllTypes createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
        throws JSONException {
        final List<String> excludeFields = new ArrayList<String>(2);
        some.test.AllTypes obj = null;
        if (update) {
            Table table = realm.getTable(some.test.AllTypes.class);
            long pkColumnIndex = table.getPrimaryKey();
            long rowIndex = Table.NO_MATCH;
            if (json.isNull("columnString")) {
                rowIndex = table.findFirstNull(pkColumnIndex);
            } else {
                rowIndex = table.findFirstString(pkColumnIndex, json.getString("columnString"));
            }
            if (rowIndex != Table.NO_MATCH) {
                final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
                try {
                    objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.getSchema().getColumnInfo(some.test.AllTypes.class), false, Collections.<String> emptyList());
                    obj = new io.realm.AllTypesRealmProxy();
                } finally {
                    objectContext.clear();
                }
            }
        }
        if (obj == null) {
            if (json.has("columnObject")) {
                excludeFields.add("columnObject");
            }
            if (json.has("columnRealmList")) {
                excludeFields.add("columnRealmList");
            }
            if (json.has("columnString")) {
                if (json.isNull("columnString")) {
                    obj = (io.realm.AllTypesRealmProxy) realm.createObjectInternal(some.test.AllTypes.class, null, true, excludeFields);
                } else {
                    obj = (io.realm.AllTypesRealmProxy) realm.createObjectInternal(some.test.AllTypes.class, json.getString("columnString"), true, excludeFields);
                }
            } else {
                throw new IllegalArgumentException("JSON object doesn't have the primary key field 'columnString'.");
            }
        }

        final AllTypesRealmProxyInterface objProxy = (AllTypesRealmProxyInterface) obj;
        if (json.has("columnLong")) {
            if (json.isNull("columnLong")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnLong' to null.");
            } else {
                objProxy.realmSet$columnLong((long) json.getLong("columnLong"));
            }
        }
        if (json.has("columnFloat")) {
            if (json.isNull("columnFloat")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnFloat' to null.");
            } else {
                objProxy.realmSet$columnFloat((float) json.getDouble("columnFloat"));
            }
        }
        if (json.has("columnDouble")) {
            if (json.isNull("columnDouble")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnDouble' to null.");
            } else {
                objProxy.realmSet$columnDouble((double) json.getDouble("columnDouble"));
            }
        }
        if (json.has("columnBoolean")) {
            if (json.isNull("columnBoolean")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnBoolean' to null.");
            } else {
                objProxy.realmSet$columnBoolean((boolean) json.getBoolean("columnBoolean"));
            }
        }
        if (json.has("columnDate")) {
            if (json.isNull("columnDate")) {
                objProxy.realmSet$columnDate(null);
            } else {
                Object timestamp = json.get("columnDate");
                if (timestamp instanceof String) {
                    objProxy.realmSet$columnDate(JsonUtils.stringToDate((String) timestamp));
                } else {
                    objProxy.realmSet$columnDate(new Date(json.getLong("columnDate")));
                }
            }
        }
        if (json.has("columnBinary")) {
            if (json.isNull("columnBinary")) {
                objProxy.realmSet$columnBinary(null);
            } else {
                objProxy.realmSet$columnBinary(JsonUtils.stringToBytes(json.getString("columnBinary")));
            }
        }
        if (json.has("columnMutableRealmInteger")) {
            objProxy.realmGet$columnMutableRealmInteger().set((json.isNull("columnMutableRealmInteger")) ? null : json.getLong("columnMutableRealmInteger"));
        }
        if (json.has("columnObject")) {
            if (json.isNull("columnObject")) {
                objProxy.realmSet$columnObject(null);
            } else {
                some.test.AllTypes columnObjectObj = AllTypesRealmProxy.createOrUpdateUsingJsonObject(realm, json.getJSONObject("columnObject"), update);
                objProxy.realmSet$columnObject(columnObjectObj);
            }
        }
        if (json.has("columnRealmList")) {
            if (json.isNull("columnRealmList")) {
                objProxy.realmSet$columnRealmList(null);
            } else {
                objProxy.realmGet$columnRealmList().clear();
                JSONArray array = json.getJSONArray("columnRealmList");
                for (int i = 0; i < array.length(); i++) {
                    some.test.AllTypes item = AllTypesRealmProxy.createOrUpdateUsingJsonObject(realm, array.getJSONObject(i), update);
                    objProxy.realmGet$columnRealmList().add(item);
                }
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.AllTypes createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        boolean jsonHasPrimaryKey = false;
        final some.test.AllTypes obj = new some.test.AllTypes();
        final AllTypesRealmProxyInterface objProxy = (AllTypesRealmProxyInterface) obj;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (false) {
            } else if (name.equals("columnString")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$columnString((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$columnString(null);
                }
                jsonHasPrimaryKey = true;
            } else if (name.equals("columnLong")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$columnLong((long) reader.nextLong());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'columnLong' to null.");
                }
            } else if (name.equals("columnFloat")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$columnFloat((float) reader.nextDouble());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'columnFloat' to null.");
                }
            } else if (name.equals("columnDouble")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$columnDouble((double) reader.nextDouble());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'columnDouble' to null.");
                }
            } else if (name.equals("columnBoolean")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$columnBoolean((boolean) reader.nextBoolean());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'columnBoolean' to null.");
                }
            } else if (name.equals("columnDate")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$columnDate(null);
                } else if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        objProxy.realmSet$columnDate(new Date(timestamp));
                    }
                } else {
                    objProxy.realmSet$columnDate(JsonUtils.stringToDate(reader.nextString()));
                }
            } else if (name.equals("columnBinary")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$columnBinary(JsonUtils.stringToBytes(reader.nextString()));
                } else {
                    reader.skipValue();
                    objProxy.realmSet$columnBinary(null);
                }
            } else if (name.equals("columnMutableRealmInteger")) {
                Long val = null;
                if (reader.peek() != JsonToken.NULL) {
                    val = reader.nextLong()
                } else {
                    reader.skipValue();
                }
                objProxy.realmGet$columnMutableRealmInteger().set(val);
            } else if (name.equals("columnObject")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$columnObject(null);
                } else {
                    some.test.AllTypes columnObjectObj = AllTypesRealmProxy.createUsingJsonStream(realm, reader);
                    objProxy.realmSet$columnObject(columnObjectObj);
                }
            } else if (name.equals("columnRealmList")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$columnRealmList(null);
                } else {
                    objProxy.realmSet$columnRealmList(new RealmList<some.test.AllTypes>());
                    reader.beginArray();
                    while (reader.hasNext()) {
                        some.test.AllTypes item = AllTypesRealmProxy.createUsingJsonStream(realm, reader);
                        objProxy.realmGet$columnRealmList().add(item);
                    }
                    reader.endArray();
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        if (!jsonHasPrimaryKey) {
            throw new IllegalArgumentException("JSON object doesn't have the primary key field 'columnString'.");
        }
        return realm.copyToRealm(obj);
    }

    public static some.test.AllTypes copyOrUpdate(Realm realm, some.test.AllTypes object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
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
            return (some.test.AllTypes) cachedRealmObject;
        }

        some.test.AllTypes realmObject = null;
        boolean canUpdate = update;
        if (canUpdate) {
            Table table = realm.getTable(some.test.AllTypes.class);
            long pkColumnIndex = table.getPrimaryKey();
            String value = ((AllTypesRealmProxyInterface) object).realmGet$columnString();
            long rowIndex = Table.NO_MATCH;
            if (value == null) {
                rowIndex = table.findFirstNull(pkColumnIndex);
            } else {
                rowIndex = table.findFirstString(pkColumnIndex, value);
            }
            if (rowIndex == Table.NO_MATCH) {
                canUpdate = false;
            } else {
                try {
                    objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.getSchema().getColumnInfo(some.test.AllTypes.class), false, Collections.<String> emptyList());
                    realmObject = new io.realm.AllTypesRealmProxy();
                    cache.put(object, (RealmObjectProxy) realmObject);
                } finally {
                    objectContext.clear();
                }
            }
        }

        return (canUpdate) ? update(realm, realmObject, object, cache) : copy(realm, object, update, cache);
    }

    public static some.test.AllTypes copy(Realm realm, some.test.AllTypes newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.AllTypes) cachedRealmObject;
        }

        // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
        some.test.AllTypes realmObject = realm.createObjectInternal(some.test.AllTypes.class, ((AllTypesRealmProxyInterface) newObject).realmGet$columnString(), false, Collections.<String>emptyList());
        cache.put(newObject, (RealmObjectProxy) realmObject);

        AllTypesRealmProxyInterface realmObjectSource = (AllTypesRealmProxyInterface) newObject;
        AllTypesRealmProxyInterface realmObjectCopy = (AllTypesRealmProxyInterface) realmObject;

        realmObjectCopy.realmSet$columnLong(realmObjectSource.realmGet$columnLong());
        realmObjectCopy.realmSet$columnFloat(realmObjectSource.realmGet$columnFloat());
        realmObjectCopy.realmSet$columnDouble(realmObjectSource.realmGet$columnDouble());
        realmObjectCopy.realmSet$columnBoolean(realmObjectSource.realmGet$columnBoolean());
        realmObjectCopy.realmSet$columnDate(realmObjectSource.realmGet$columnDate());
        realmObjectCopy.realmSet$columnBinary(realmObjectSource.realmGet$columnBinary());

        realmObjectCopy.realmGet$columnMutableRealmInteger().set(realmObjectSource.realmGet$columnMutableRealmInteger().get());

        some.test.AllTypes columnObjectObj = realmObjectSource.realmGet$columnObject();
        if (columnObjectObj == null) {
            realmObjectCopy.realmSet$columnObject(null);
        } else {
            some.test.AllTypes cachecolumnObject = (some.test.AllTypes) cache.get(columnObjectObj);
            if (cachecolumnObject != null) {
                realmObjectCopy.realmSet$columnObject(cachecolumnObject);
            } else {
                realmObjectCopy.realmSet$columnObject(AllTypesRealmProxy.copyOrUpdate(realm, columnObjectObj, update, cache));
            }
        }

        RealmList<some.test.AllTypes> columnRealmListList = realmObjectSource.realmGet$columnRealmList();
        if (columnRealmListList != null) {
            RealmList<some.test.AllTypes> columnRealmListRealmList = realmObjectCopy.realmGet$columnRealmList();
            columnRealmListRealmList.clear();
            for (int i = 0; i < columnRealmListList.size(); i++) {
                some.test.AllTypes columnRealmListItem = columnRealmListList.get(i);
                some.test.AllTypes cachecolumnRealmList = (some.test.AllTypes) cache.get(columnRealmListItem);
                if (cachecolumnRealmList != null) {
                    columnRealmListRealmList.add(cachecolumnRealmList);
                } else {
                    columnRealmListRealmList.add(AllTypesRealmProxy.copyOrUpdate(realm, columnRealmListItem, update, cache));
                }
            }
        }

        return realmObject;
    }

    public static long insert(Realm realm, some.test.AllTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativePtr();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
        long pkColumnIndex = table.getPrimaryKey();
        String primaryKeyValue = ((AllTypesRealmProxyInterface) object).realmGet$columnString();
        long rowIndex = Table.NO_MATCH;
        if (primaryKeyValue == null) {
            rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
        } else {
            rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
        }
        if (rowIndex == Table.NO_MATCH) {
            rowIndex = OsObject.createRowWithPrimaryKey(table, primaryKeyValue);
        } else {
            Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
        }
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.columnLongIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnLong(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnFloat(), false);
        Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnDouble(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnBoolean(), false);
        java.util.Date realmGet$columnDate = ((AllTypesRealmProxyInterface) object).realmGet$columnDate();
        if (realmGet$columnDate != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateIndex, rowIndex, realmGet$columnDate.getTime(), false);
        }
        byte[] realmGet$columnBinary = ((AllTypesRealmProxyInterface) object).realmGet$columnBinary();
        if (realmGet$columnBinary != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, realmGet$columnBinary, false);
        }
        Long realmGet$columnMutableRealmInteger = ((AllTypesRealmProxyInterface) object).realmGet$columnMutableRealmInteger().get();
        if (realmGet$columnMutableRealmInteger != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.columnMutableRealmIntegerIndex, rowIndex, realmGet$columnMutableRealmInteger.longValue(), false);
        }

        some.test.AllTypes columnObjectObj = ((AllTypesRealmProxyInterface) object).realmGet$columnObject();
        if (columnObjectObj != null) {
            Long cachecolumnObject = cache.get(columnObjectObj);
            if (cachecolumnObject == null) {
                cachecolumnObject = AllTypesRealmProxy.insert(realm, columnObjectObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.columnObjectIndex, rowIndex, cachecolumnObject, false);
        }

        RealmList<some.test.AllTypes> columnRealmListList = ((AllTypesRealmProxyInterface) object).realmGet$columnRealmList();
        if (columnRealmListList != null) {
            long columnRealmListNativeLinkViewPtr = Table.nativeGetLinkView(tableNativePtr, columnInfo.columnRealmListIndex, rowIndex);
            for (some.test.AllTypes columnRealmListItem : columnRealmListList) {
                Long cacheItemIndexcolumnRealmList = cache.get(columnRealmListItem);
                if (cacheItemIndexcolumnRealmList == null) {
                    cacheItemIndexcolumnRealmList = AllTypesRealmProxy.insert(realm, columnRealmListItem, cache);
                }
                LinkView.nativeAdd(columnRealmListNativeLinkViewPtr, cacheItemIndexcolumnRealmList);
            }
        }
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativePtr();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
        long pkColumnIndex = table.getPrimaryKey();
        some.test.AllTypes object = null;
        while (objects.hasNext()) {
            object = (some.test.AllTypes) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            String primaryKeyValue = ((AllTypesRealmProxyInterface) object).realmGet$columnString();
            long rowIndex = Table.NO_MATCH;
            if (primaryKeyValue == null) {
                rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
            } else {
                rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
            }
            if (rowIndex == Table.NO_MATCH) {
                rowIndex = OsObject.createRowWithPrimaryKey(table, primaryKeyValue);
            } else {
                Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
            }
            cache.put(object, rowIndex);
            Table.nativeSetLong(tableNativePtr, columnInfo.columnLongIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnLong(), false);
            Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnFloat(), false);
            Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnDouble(), false);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnBoolean(), false);
            java.util.Date realmGet$columnDate = ((AllTypesRealmProxyInterface) object).realmGet$columnDate();
            if (realmGet$columnDate != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateIndex, rowIndex, realmGet$columnDate.getTime(), false);
            }
            byte[] realmGet$columnBinary = ((AllTypesRealmProxyInterface) object).realmGet$columnBinary();
            if (realmGet$columnBinary != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, realmGet$columnBinary, false);
            }
            Long realmGet$columnMutableRealmInteger = ((AllTypesRealmProxyInterface) object).realmGet$columnMutableRealmInteger().get();
            if (realmGet$columnMutableRealmInteger != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.columnMutableRealmIntegerIndex, rowIndex, realmGet$columnMutableRealmInteger.longValue(), false);
            }

            some.test.AllTypes columnObjectObj = ((AllTypesRealmProxyInterface) object).realmGet$columnObject();
            if (columnObjectObj != null) {
                Long cachecolumnObject = cache.get(columnObjectObj);
                if (cachecolumnObject == null) {
                    cachecolumnObject = AllTypesRealmProxy.insert(realm, columnObjectObj, cache);
                }
                table.setLink(columnInfo.columnObjectIndex, rowIndex, cachecolumnObject, false);
            }

            RealmList<some.test.AllTypes> columnRealmListList = ((AllTypesRealmProxyInterface) object).realmGet$columnRealmList();
            if (columnRealmListList != null) {
                long columnRealmListNativeLinkViewPtr = Table.nativeGetLinkView(tableNativePtr, columnInfo.columnRealmListIndex, rowIndex);
                for (some.test.AllTypes columnRealmListItem : columnRealmListList) {
                    Long cacheItemIndexcolumnRealmList = cache.get(columnRealmListItem);
                    if (cacheItemIndexcolumnRealmList == null) {
                        cacheItemIndexcolumnRealmList = AllTypesRealmProxy.insert(realm, columnRealmListItem, cache);
                    }
                    LinkView.nativeAdd(columnRealmListNativeLinkViewPtr, cacheItemIndexcolumnRealmList);
                }
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.AllTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativePtr();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
        long pkColumnIndex = table.getPrimaryKey();
        String primaryKeyValue = ((AllTypesRealmProxyInterface) object).realmGet$columnString();
        long rowIndex = Table.NO_MATCH;
        if (primaryKeyValue == null) {
            rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
        } else {
            rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
        }
        if (rowIndex == Table.NO_MATCH) {
            rowIndex = OsObject.createRowWithPrimaryKey(table, primaryKeyValue);
        }
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.columnLongIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnLong(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnFloat(), false);
        Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnDouble(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnBoolean(), false);
        java.util.Date realmGet$columnDate = ((AllTypesRealmProxyInterface) object).realmGet$columnDate();
        if (realmGet$columnDate != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateIndex, rowIndex, realmGet$columnDate.getTime(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnDateIndex, rowIndex, false);
        }
        byte[] realmGet$columnBinary = ((AllTypesRealmProxyInterface) object).realmGet$columnBinary();
        if (realmGet$columnBinary != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, realmGet$columnBinary, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, false);
        }
        Long realmGet$columnMutableRealmInteger = ((AllTypesRealmProxyInterface) object).realmGet$columnMutableRealmInteger().get();
        if (realmGet$columnMutableRealmInteger != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.columnMutableRealmIntegerIndex, rowIndex, realmGet$columnMutableRealmInteger.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnMutableRealmIntegerIndex, rowIndex, false);
        }

        some.test.AllTypes columnObjectObj = ((AllTypesRealmProxyInterface) object).realmGet$columnObject();
        if (columnObjectObj != null) {
            Long cachecolumnObject = cache.get(columnObjectObj);
            if (cachecolumnObject == null) {
                cachecolumnObject = AllTypesRealmProxy.insertOrUpdate(realm, columnObjectObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.columnObjectIndex, rowIndex, cachecolumnObject, false);
        } else {
            Table.nativeNullifyLink(tableNativePtr, columnInfo.columnObjectIndex, rowIndex);
        }

        long columnRealmListNativeLinkViewPtr = Table.nativeGetLinkView(tableNativePtr, columnInfo.columnRealmListIndex, rowIndex);
        LinkView.nativeClear(columnRealmListNativeLinkViewPtr);
        RealmList<some.test.AllTypes> columnRealmListList = ((AllTypesRealmProxyInterface) object).realmGet$columnRealmList();
        if (columnRealmListList != null) {
            for (some.test.AllTypes columnRealmListItem : columnRealmListList) {
                Long cacheItemIndexcolumnRealmList = cache.get(columnRealmListItem);
                if (cacheItemIndexcolumnRealmList == null) {
                    cacheItemIndexcolumnRealmList = AllTypesRealmProxy.insertOrUpdate(realm, columnRealmListItem, cache);
                }
                LinkView.nativeAdd(columnRealmListNativeLinkViewPtr, cacheItemIndexcolumnRealmList);
            }
        }

        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativePtr();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
        long pkColumnIndex = table.getPrimaryKey();
        some.test.AllTypes object = null;
        while (objects.hasNext()) {
            object = (some.test.AllTypes) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            String primaryKeyValue = ((AllTypesRealmProxyInterface) object).realmGet$columnString();
            long rowIndex = Table.NO_MATCH;
            if (primaryKeyValue == null) {
                rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
            } else {
                rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
            }
            if (rowIndex == Table.NO_MATCH) {
                rowIndex = OsObject.createRowWithPrimaryKey(table, primaryKeyValue);
            }
            cache.put(object, rowIndex);
            Table.nativeSetLong(tableNativePtr, columnInfo.columnLongIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnLong(), false);
            Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnFloat(), false);
            Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnDouble(), false);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanIndex, rowIndex, ((AllTypesRealmProxyInterface) object).realmGet$columnBoolean(), false);
            java.util.Date realmGet$columnDate = ((AllTypesRealmProxyInterface) object).realmGet$columnDate();
            if (realmGet$columnDate != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateIndex, rowIndex, realmGet$columnDate.getTime(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnDateIndex, rowIndex, false);
            }
            byte[] realmGet$columnBinary = ((AllTypesRealmProxyInterface) object).realmGet$columnBinary();
            if (realmGet$columnBinary != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, realmGet$columnBinary, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, false);
            }
            Long realmGet$columnMutableRealmInteger = ((AllTypesRealmProxyInterface) object).realmGet$columnMutableRealmInteger().get();
            if (realmGet$columnMutableRealmInteger != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.columnMutableRealmIntegerIndex, rowIndex, realmGet$columnMutableRealmInteger.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnMutableRealmIntegerIndex, rowIndex, false);
            }

            some.test.AllTypes columnObjectObj = ((AllTypesRealmProxyInterface) object).realmGet$columnObject();
            if (columnObjectObj != null) {
                Long cachecolumnObject = cache.get(columnObjectObj);
                if (cachecolumnObject == null) {
                    cachecolumnObject = AllTypesRealmProxy.insertOrUpdate(realm, columnObjectObj, cache);
                }
                Table.nativeSetLink(tableNativePtr, columnInfo.columnObjectIndex, rowIndex, cachecolumnObject, false);
            } else {
                Table.nativeNullifyLink(tableNativePtr, columnInfo.columnObjectIndex, rowIndex);
            }

            long columnRealmListNativeLinkViewPtr = Table.nativeGetLinkView(tableNativePtr, columnInfo.columnRealmListIndex, rowIndex);
            LinkView.nativeClear(columnRealmListNativeLinkViewPtr);
            RealmList<some.test.AllTypes> columnRealmListList = ((AllTypesRealmProxyInterface) object).realmGet$columnRealmList();
            if (columnRealmListList != null) {
                for (some.test.AllTypes columnRealmListItem : columnRealmListList) {
                    Long cacheItemIndexcolumnRealmList = cache.get(columnRealmListItem);
                    if (cacheItemIndexcolumnRealmList == null) {
                        cacheItemIndexcolumnRealmList = AllTypesRealmProxy.insertOrUpdate(realm, columnRealmListItem, cache);
                    }
                    LinkView.nativeAdd(columnRealmListNativeLinkViewPtr, cacheItemIndexcolumnRealmList);
                }
            }

        }
    }

    public static some.test.AllTypes createDetachedCopy(some.test.AllTypes realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.AllTypes unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new some.test.AllTypes();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.AllTypes) cachedObject.object;
            }
            unmanagedObject = (some.test.AllTypes) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        AllTypesRealmProxyInterface unmanagedCopy = (AllTypesRealmProxyInterface) unmanagedObject;
        AllTypesRealmProxyInterface realmSource = (AllTypesRealmProxyInterface) realmObject;
        unmanagedCopy.realmSet$columnString(realmSource.realmGet$columnString());
        unmanagedCopy.realmSet$columnLong(realmSource.realmGet$columnLong());
        unmanagedCopy.realmSet$columnFloat(realmSource.realmGet$columnFloat());
        unmanagedCopy.realmSet$columnDouble(realmSource.realmGet$columnDouble());
        unmanagedCopy.realmSet$columnBoolean(realmSource.realmGet$columnBoolean());
        unmanagedCopy.realmSet$columnDate(realmSource.realmGet$columnDate());
        unmanagedCopy.realmSet$columnBinary(realmSource.realmGet$columnBinary());
        unmanagedCopy.realmGet$columnMutableRealmInteger().set(realmSource.realmGet$columnMutableRealmInteger().get());

        // Deep copy of columnObject
        unmanagedCopy.realmSet$columnObject(AllTypesRealmProxy.createDetachedCopy(realmSource.realmGet$columnObject(), currentDepth + 1, maxDepth, cache));

        // Deep copy of columnRealmList
        if (currentDepth == maxDepth) {
            unmanagedCopy.realmSet$columnRealmList(null);
        } else {
            RealmList<some.test.AllTypes> managedcolumnRealmListList = realmSource.realmGet$columnRealmList();
            RealmList<some.test.AllTypes> unmanagedcolumnRealmListList = new RealmList<some.test.AllTypes>();
            unmanagedCopy.realmSet$columnRealmList(unmanagedcolumnRealmListList);
            int nextDepth = currentDepth + 1;
            int size = managedcolumnRealmListList.size();
            for (int i = 0; i < size; i++) {
                some.test.AllTypes item = AllTypesRealmProxy.createDetachedCopy(managedcolumnRealmListList.get(i), nextDepth, maxDepth, cache);
                unmanagedcolumnRealmListList.add(item);
            }
        }
        return unmanagedObject;
    }

    static some.test.AllTypes update(Realm realm, some.test.AllTypes realmObject, some.test.AllTypes newObject, Map<RealmModel, RealmObjectProxy> cache) {
        AllTypesRealmProxyInterface realmObjectTarget = (AllTypesRealmProxyInterface) realmObject;
        AllTypesRealmProxyInterface realmObjectSource = (AllTypesRealmProxyInterface) newObject;
        realmObjectTarget.realmSet$columnLong(realmObjectSource.realmGet$columnLong());
        realmObjectTarget.realmSet$columnFloat(realmObjectSource.realmGet$columnFloat());
        realmObjectTarget.realmSet$columnDouble(realmObjectSource.realmGet$columnDouble());
        realmObjectTarget.realmSet$columnBoolean(realmObjectSource.realmGet$columnBoolean());
        realmObjectTarget.realmSet$columnDate(realmObjectSource.realmGet$columnDate());
        realmObjectTarget.realmSet$columnBinary(realmObjectSource.realmGet$columnBinary());
        realmObjectTarget.realmGet$columnMutableRealmInteger().set(realmObjectSource.realmGet$columnMutableRealmInteger().get());
        some.test.AllTypes columnObjectObj = realmObjectSource.realmGet$columnObject();
        if (columnObjectObj == null) {
            realmObjectTarget.realmSet$columnObject(null);
        } else {
            some.test.AllTypes cachecolumnObject = (some.test.AllTypes) cache.get(columnObjectObj);
            if (cachecolumnObject != null) {
                realmObjectTarget.realmSet$columnObject(cachecolumnObject);
            } else {
                realmObjectTarget.realmSet$columnObject(AllTypesRealmProxy.copyOrUpdate(realm, columnObjectObj, true, cache));
            }
        }
        RealmList<some.test.AllTypes> columnRealmListList = realmObjectSource.realmGet$columnRealmList();
        RealmList<some.test.AllTypes> columnRealmListRealmList = realmObjectTarget.realmGet$columnRealmList();
        columnRealmListRealmList.clear();
        if (columnRealmListList != null) {
            for (int i = 0; i < columnRealmListList.size(); i++) {
                some.test.AllTypes columnRealmListItem = columnRealmListList.get(i);
                some.test.AllTypes cachecolumnRealmList = (some.test.AllTypes) cache.get(columnRealmListItem);
                if (cachecolumnRealmList != null) {
                    columnRealmListRealmList.add(cachecolumnRealmList);
                } else {
                    columnRealmListRealmList.add(AllTypesRealmProxy.copyOrUpdate(realm, columnRealmListItem, true, cache));
                }
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
        StringBuilder stringBuilder = new StringBuilder("AllTypes = proxy[");
        stringBuilder.append("{columnString:");
        stringBuilder.append(realmGet$columnString() != null ? realmGet$columnString() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnLong:");
        stringBuilder.append(realmGet$columnLong());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnFloat:");
        stringBuilder.append(realmGet$columnFloat());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDouble:");
        stringBuilder.append(realmGet$columnDouble());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBoolean:");
        stringBuilder.append(realmGet$columnBoolean());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDate:");
        stringBuilder.append(realmGet$columnDate());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBinary:");
        stringBuilder.append(realmGet$columnBinary());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnMutableRealmInteger:");
        stringBuilder.append(realmGet$columnMutableRealmInteger().get());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnObject:");
        stringBuilder.append(realmGet$columnObject() != null ? "AllTypes" : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnRealmList:");
        stringBuilder.append("RealmList<AllTypes>[").append(realmGet$columnRealmList().size()).append("]");
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
        AllTypesRealmProxy aAllTypes = (AllTypesRealmProxy)o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aAllTypes.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aAllTypes.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aAllTypes.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }
}
