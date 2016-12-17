package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.LinkView;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.SharedRealm;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
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

public class AllTypesRealmProxy extends some.test.AllTypes
        implements RealmObjectProxy, AllTypesRealmProxyInterface {

    static final class AllTypesColumnInfo extends ColumnInfo
            implements Cloneable {

        public long columnStringIndex;
        public long columnLongIndex;
        public long columnFloatIndex;
        public long columnDoubleIndex;
        public long columnBooleanIndex;
        public long columnDateIndex;
        public long columnBinaryIndex;
        public long columnObjectIndex;
        public long columnRealmListIndex;

        AllTypesColumnInfo(String path, Table table) {
            final Map<String, Long> indicesMap = new HashMap<String, Long>(9);
            this.columnStringIndex = getValidColumnIndex(path, table, "AllTypes", "columnString");
            indicesMap.put("columnString", this.columnStringIndex);
            this.columnLongIndex = getValidColumnIndex(path, table, "AllTypes", "columnLong");
            indicesMap.put("columnLong", this.columnLongIndex);
            this.columnFloatIndex = getValidColumnIndex(path, table, "AllTypes", "columnFloat");
            indicesMap.put("columnFloat", this.columnFloatIndex);
            this.columnDoubleIndex = getValidColumnIndex(path, table, "AllTypes", "columnDouble");
            indicesMap.put("columnDouble", this.columnDoubleIndex);
            this.columnBooleanIndex = getValidColumnIndex(path, table, "AllTypes", "columnBoolean");
            indicesMap.put("columnBoolean", this.columnBooleanIndex);
            this.columnDateIndex = getValidColumnIndex(path, table, "AllTypes", "columnDate");
            indicesMap.put("columnDate", this.columnDateIndex);
            this.columnBinaryIndex = getValidColumnIndex(path, table, "AllTypes", "columnBinary");
            indicesMap.put("columnBinary", this.columnBinaryIndex);
            this.columnObjectIndex = getValidColumnIndex(path, table, "AllTypes", "columnObject");
            indicesMap.put("columnObject", this.columnObjectIndex);
            this.columnRealmListIndex = getValidColumnIndex(path, table, "AllTypes", "columnRealmList");
            indicesMap.put("columnRealmList", this.columnRealmListIndex);

            setIndicesMap(indicesMap);
        }

        @Override
        public final void copyColumnInfoFrom(ColumnInfo other) {
            final AllTypesColumnInfo otherInfo = (AllTypesColumnInfo) other;
            this.columnStringIndex = otherInfo.columnStringIndex;
            this.columnLongIndex = otherInfo.columnLongIndex;
            this.columnFloatIndex = otherInfo.columnFloatIndex;
            this.columnDoubleIndex = otherInfo.columnDoubleIndex;
            this.columnBooleanIndex = otherInfo.columnBooleanIndex;
            this.columnDateIndex = otherInfo.columnDateIndex;
            this.columnBinaryIndex = otherInfo.columnBinaryIndex;
            this.columnObjectIndex = otherInfo.columnObjectIndex;
            this.columnRealmListIndex = otherInfo.columnRealmListIndex;

            setIndicesMap(otherInfo.getIndicesMap());
        }

        @Override
        public final AllTypesColumnInfo clone() {
            return (AllTypesColumnInfo) super.clone();
        }

    }
    private AllTypesColumnInfo columnInfo;
    private ProxyState proxyState;
    private RealmList<some.test.AllTypes> columnRealmListRealmList;
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
        fieldNames.add("columnObject");
        fieldNames.add("columnRealmList");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    AllTypesRealmProxy() {
        if (proxyState == null) {
            injectObjectContext();
        }
        proxyState.setConstructionFinished();
    }

    private void injectObjectContext() {
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (AllTypesColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState(some.test.AllTypes.class, this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @SuppressWarnings("cast")
    public String realmGet$columnString() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.columnStringIndex);
    }

    public void realmSet$columnString(String value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        if (proxyState.isUnderConstruction()) {
            // default value of the primary key is always ignored.
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        throw new io.realm.exceptions.RealmException("Primary key field 'columnString' cannot be changed after object was created.");
    }

    @SuppressWarnings("cast")
    public long realmGet$columnLong() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (long) proxyState.getRow$realm().getLong(columnInfo.columnLongIndex);
    }

    public void realmSet$columnLong(long value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public float realmGet$columnFloat() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.columnFloatIndex);
    }

    public void realmSet$columnFloat(float value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public double realmGet$columnDouble() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (double) proxyState.getRow$realm().getDouble(columnInfo.columnDoubleIndex);
    }

    public void realmSet$columnDouble(double value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public boolean realmGet$columnBoolean() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.columnBooleanIndex);
    }

    public void realmSet$columnBoolean(boolean value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Date realmGet$columnDate() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.util.Date) proxyState.getRow$realm().getDate(columnInfo.columnDateIndex);
    }

    public void realmSet$columnDate(Date value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public byte[] realmGet$columnBinary() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (byte[]) proxyState.getRow$realm().getBinaryByteArray(columnInfo.columnBinaryIndex);
    }

    public void realmSet$columnBinary(byte[] value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    public some.test.AllTypes realmGet$columnObject() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNullLink(columnInfo.columnObjectIndex)) {
            return null;
        }
        return proxyState.getRealm$realm().get(some.test.AllTypes.class, proxyState.getRow$realm().getLink(columnInfo.columnObjectIndex), false, Collections.<String>emptyList());
    }

    public void realmSet$columnObject(some.test.AllTypes value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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
        if (((RealmObjectProxy)value).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm()) {
            throw new IllegalArgumentException("'value' belongs to a different Realm.");
        }
        proxyState.getRow$realm().setLink(columnInfo.columnObjectIndex, ((RealmObjectProxy)value).realmGet$proxyState().getRow$realm().getIndex());
    }

    public RealmList<some.test.AllTypes> realmGet$columnRealmList() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    public void realmSet$columnRealmList(RealmList<some.test.AllTypes> value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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
            if (((RealmObjectProxy)linkedObject).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm()) {
                throw new IllegalArgumentException("Each element of 'value' must belong to the same Realm.");
            }
            links.add(((RealmObjectProxy)linkedObject).realmGet$proxyState().getRow$realm().getIndex());
        }
    }

    public static RealmObjectSchema createRealmObjectSchema(RealmSchema realmSchema) {
        if (!realmSchema.contains("AllTypes")) {
            RealmObjectSchema realmObjectSchema = realmSchema.create("AllTypes");
            realmObjectSchema.add(new Property("columnString", RealmFieldType.STRING, Property.PRIMARY_KEY, Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("columnLong", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("columnFloat", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("columnDouble", RealmFieldType.DOUBLE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("columnBoolean", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("columnDate", RealmFieldType.DATE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("columnBinary", RealmFieldType.BINARY, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            if (!realmSchema.contains("AllTypes")) {
                AllTypesRealmProxy.createRealmObjectSchema(realmSchema);
            }
            realmObjectSchema.add(new Property("columnObject", RealmFieldType.OBJECT, realmSchema.get("AllTypes")));
            if (!realmSchema.contains("AllTypes")) {
                AllTypesRealmProxy.createRealmObjectSchema(realmSchema);
            }
            realmObjectSchema.add(new Property("columnRealmList", RealmFieldType.LIST, realmSchema.get("AllTypes")));
            return realmObjectSchema;
        }
        return realmSchema.get("AllTypes");
    }

    public static Table initTable(SharedRealm sharedRealm) {
        if (!sharedRealm.hasTable("class_AllTypes")) {
            Table table = sharedRealm.getTable("class_AllTypes");
            table.addColumn(RealmFieldType.STRING, "columnString", Table.NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "columnLong", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "columnFloat", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.DOUBLE, "columnDouble", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "columnBoolean", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.DATE, "columnDate", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BINARY, "columnBinary", Table.NOT_NULLABLE);
            if (!sharedRealm.hasTable("class_AllTypes")) {
                AllTypesRealmProxy.initTable(sharedRealm);
            }
            table.addColumnLink(RealmFieldType.OBJECT, "columnObject", sharedRealm.getTable("class_AllTypes"));
            if (!sharedRealm.hasTable("class_AllTypes")) {
                AllTypesRealmProxy.initTable(sharedRealm);
            }
            table.addColumnLink(RealmFieldType.LIST, "columnRealmList", sharedRealm.getTable("class_AllTypes"));
            table.addSearchIndex(table.getColumnIndex("columnString"));
            table.setPrimaryKey("columnString");
            return table;
        }
        return sharedRealm.getTable("class_AllTypes");
    }

    public static AllTypesColumnInfo validateTable(SharedRealm sharedRealm, boolean allowExtraColumns) {
        if (sharedRealm.hasTable("class_AllTypes")) {
            Table table = sharedRealm.getTable("class_AllTypes");
            final long columnCount = table.getColumnCount();
            if (columnCount != 9) {
                if (columnCount < 9) {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is less than expected - expected 9 but was " + columnCount);
                }
                if (allowExtraColumns) {
                    RealmLog.debug("Field count is more than expected - expected 9 but was %1$d", columnCount);
                } else {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is more than expected - expected 9 but was " + columnCount);
                }
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < columnCount; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final AllTypesColumnInfo columnInfo = new AllTypesColumnInfo(sharedRealm.getPath(), table);

            if (!columnTypes.containsKey("columnString")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'columnString' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnString") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'columnString' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.columnStringIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(),"@PrimaryKey field 'columnString' does not support null values in the existing Realm file. Migrate using RealmObjectSchema.setNullable(), or mark the field as @Required.");
            }
            if (table.getPrimaryKey() != table.getColumnIndex("columnString")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Primary key not defined for field 'columnString' in existing Realm file. Add @PrimaryKey.");
            }
            if (!table.hasSearchIndex(table.getColumnIndex("columnString"))) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Index not defined for field 'columnString' in existing Realm file. Either set @Index or migrate using io.realm.internal.Table.removeSearchIndex().");
            }
            if (!columnTypes.containsKey("columnLong")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'columnLong' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnLong") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'long' for field 'columnLong' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnLongIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'columnLong' does support null values in the existing Realm file. Use corresponding boxed type for field 'columnLong' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("columnFloat")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'columnFloat' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnFloat") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'float' for field 'columnFloat' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnFloatIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'columnFloat' does support null values in the existing Realm file. Use corresponding boxed type for field 'columnFloat' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("columnDouble")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'columnDouble' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnDouble") != RealmFieldType.DOUBLE) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'double' for field 'columnDouble' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnDoubleIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'columnDouble' does support null values in the existing Realm file. Use corresponding boxed type for field 'columnDouble' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("columnBoolean")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'columnBoolean' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnBoolean") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'boolean' for field 'columnBoolean' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnBooleanIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'columnBoolean' does support null values in the existing Realm file. Use corresponding boxed type for field 'columnBoolean' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("columnDate")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'columnDate' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnDate") != RealmFieldType.DATE) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Date' for field 'columnDate' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnDateIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'columnDate' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'columnDate' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("columnBinary")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'columnBinary' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnBinary") != RealmFieldType.BINARY) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'byte[]' for field 'columnBinary' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnBinaryIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'columnBinary' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'columnBinary' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("columnObject")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'columnObject' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnObject") != RealmFieldType.OBJECT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'AllTypes' for field 'columnObject'");
            }
            if (!sharedRealm.hasTable("class_AllTypes")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing class 'class_AllTypes' for field 'columnObject'");
            }
            Table table_7 = sharedRealm.getTable("class_AllTypes");
            if (!table.getLinkTarget(columnInfo.columnObjectIndex).hasSameSchema(table_7)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid RealmObject for field 'columnObject': '" + table.getLinkTarget(columnInfo.columnObjectIndex).getName() + "' expected - was '" + table_7.getName() + "'");
            }
            if (!columnTypes.containsKey("columnRealmList")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'columnRealmList'");
            }
            if (columnTypes.get("columnRealmList") != RealmFieldType.LIST) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'AllTypes' for field 'columnRealmList'");
            }
            if (!sharedRealm.hasTable("class_AllTypes")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing class 'class_AllTypes' for field 'columnRealmList'");
            }
            Table table_8 = sharedRealm.getTable("class_AllTypes");
            if (!table.getLinkTarget(columnInfo.columnRealmListIndex).hasSameSchema(table_8)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid RealmList type for field 'columnRealmList': '" + table.getLinkTarget(columnInfo.columnRealmListIndex).getName() + "' expected - was '" + table_8.getName() + "'");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(sharedRealm.getPath(), "The 'AllTypes' class is missing from the schema for this Realm.");
        }
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
            long rowIndex = TableOrView.NO_MATCH;
            if (json.isNull("columnString")) {
                rowIndex = table.findFirstNull(pkColumnIndex);
            } else {
                rowIndex = table.findFirstString(pkColumnIndex, json.getString("columnString"));
            }
            if (rowIndex != TableOrView.NO_MATCH) {
                final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
                try {
                    objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.schema.getColumnInfo(some.test.AllTypes.class), false, Collections.<String> emptyList());
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
        if (json.has("columnLong")) {
            if (json.isNull("columnLong")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnLong' to null.");
            } else {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnLong((long) json.getLong("columnLong"));
            }
        }
        if (json.has("columnFloat")) {
            if (json.isNull("columnFloat")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnFloat' to null.");
            } else {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnFloat((float) json.getDouble("columnFloat"));
            }
        }
        if (json.has("columnDouble")) {
            if (json.isNull("columnDouble")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnDouble' to null.");
            } else {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnDouble((double) json.getDouble("columnDouble"));
            }
        }
        if (json.has("columnBoolean")) {
            if (json.isNull("columnBoolean")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnBoolean' to null.");
            } else {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnBoolean((boolean) json.getBoolean("columnBoolean"));
            }
        }
        if (json.has("columnDate")) {
            if (json.isNull("columnDate")) {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnDate(null);
            } else {
                Object timestamp = json.get("columnDate");
                if (timestamp instanceof String) {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnDate(JsonUtils.stringToDate((String) timestamp));
                } else {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnDate(new Date(json.getLong("columnDate")));
                }
            }
        }
        if (json.has("columnBinary")) {
            if (json.isNull("columnBinary")) {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnBinary(null);
            } else {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnBinary(JsonUtils.stringToBytes(json.getString("columnBinary")));
            }
        }
        if (json.has("columnObject")) {
            if (json.isNull("columnObject")) {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnObject(null);
            } else {
                some.test.AllTypes columnObjectObj = AllTypesRealmProxy.createOrUpdateUsingJsonObject(realm, json.getJSONObject("columnObject"), update);
                ((AllTypesRealmProxyInterface) obj).realmSet$columnObject(columnObjectObj);
            }
        }
        if (json.has("columnRealmList")) {
            if (json.isNull("columnRealmList")) {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnRealmList(null);
            } else {
                ((AllTypesRealmProxyInterface) obj).realmGet$columnRealmList().clear();
                JSONArray array = json.getJSONArray("columnRealmList");
                for (int i = 0; i < array.length(); i++) {
                    some.test.AllTypes item = AllTypesRealmProxy.createOrUpdateUsingJsonObject(realm, array.getJSONObject(i), update);
                    ((AllTypesRealmProxyInterface) obj).realmGet$columnRealmList().add(item);
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
        some.test.AllTypes obj = new some.test.AllTypes();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("columnString")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnString(null);
                } else {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnString((String) reader.nextString());
                }
                jsonHasPrimaryKey = true;
            } else if (name.equals("columnLong")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'columnLong' to null.");
                } else {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnLong((long) reader.nextLong());
                }
            } else if (name.equals("columnFloat")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'columnFloat' to null.");
                } else {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnFloat((float) reader.nextDouble());
                }
            } else if (name.equals("columnDouble")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'columnDouble' to null.");
                } else {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnDouble((double) reader.nextDouble());
                }
            } else if (name.equals("columnBoolean")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'columnBoolean' to null.");
                } else {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnBoolean((boolean) reader.nextBoolean());
                }
            } else if (name.equals("columnDate")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnDate(null);
                } else if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        ((AllTypesRealmProxyInterface) obj).realmSet$columnDate(new Date(timestamp));
                    }
                } else {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnDate(JsonUtils.stringToDate(reader.nextString()));
                }
            } else if (name.equals("columnBinary")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnBinary(null);
                } else {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnBinary(JsonUtils.stringToBytes(reader.nextString()));
                }
            } else if (name.equals("columnObject")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnObject(null);
                } else {
                    some.test.AllTypes columnObjectObj = AllTypesRealmProxy.createUsingJsonStream(realm, reader);
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnObject(columnObjectObj);
                }
            } else if (name.equals("columnRealmList")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnRealmList(null);
                } else {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnRealmList(new RealmList<some.test.AllTypes>());
                    reader.beginArray();
                    while (reader.hasNext()) {
                        some.test.AllTypes item = AllTypesRealmProxy.createUsingJsonStream(realm, reader);
                        ((AllTypesRealmProxyInterface) obj).realmGet$columnRealmList().add(item);
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
        obj = realm.copyToRealm(obj);
        return obj;
    }

    public static some.test.AllTypes copyOrUpdate(Realm realm, some.test.AllTypes object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (some.test.AllTypes) cachedRealmObject;
        } else {
            some.test.AllTypes realmObject = null;
            boolean canUpdate = update;
            if (canUpdate) {
                Table table = realm.getTable(some.test.AllTypes.class);
                long pkColumnIndex = table.getPrimaryKey();
                String value = ((AllTypesRealmProxyInterface) object).realmGet$columnString();
                long rowIndex = TableOrView.NO_MATCH;
                if (value == null) {
                    rowIndex = table.findFirstNull(pkColumnIndex);
                } else {
                    rowIndex = table.findFirstString(pkColumnIndex, value);
                }
                if (rowIndex != TableOrView.NO_MATCH) {
                    try {
                        objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.schema.getColumnInfo(some.test.AllTypes.class), false, Collections.<String> emptyList());
                        realmObject = new io.realm.AllTypesRealmProxy();
                        cache.put(object, (RealmObjectProxy) realmObject);
                    } finally {
                        objectContext.clear();
                    }
                } else {
                    canUpdate = false;
                }
            }

            if (canUpdate) {
                return update(realm, realmObject, object, cache);
            } else {
                return copy(realm, object, update, cache);
            }
        }
    }

    public static some.test.AllTypes copy(Realm realm, some.test.AllTypes newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.AllTypes) cachedRealmObject;
        } else {
            // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
            some.test.AllTypes realmObject = realm.createObjectInternal(some.test.AllTypes.class, ((AllTypesRealmProxyInterface) newObject).realmGet$columnString(), false, Collections.<String>emptyList());
            cache.put(newObject, (RealmObjectProxy) realmObject);
            ((AllTypesRealmProxyInterface) realmObject).realmSet$columnLong(((AllTypesRealmProxyInterface) newObject).realmGet$columnLong());
            ((AllTypesRealmProxyInterface) realmObject).realmSet$columnFloat(((AllTypesRealmProxyInterface) newObject).realmGet$columnFloat());
            ((AllTypesRealmProxyInterface) realmObject).realmSet$columnDouble(((AllTypesRealmProxyInterface) newObject).realmGet$columnDouble());
            ((AllTypesRealmProxyInterface) realmObject).realmSet$columnBoolean(((AllTypesRealmProxyInterface) newObject).realmGet$columnBoolean());
            ((AllTypesRealmProxyInterface) realmObject).realmSet$columnDate(((AllTypesRealmProxyInterface) newObject).realmGet$columnDate());
            ((AllTypesRealmProxyInterface) realmObject).realmSet$columnBinary(((AllTypesRealmProxyInterface) newObject).realmGet$columnBinary());

            some.test.AllTypes columnObjectObj = ((AllTypesRealmProxyInterface) newObject).realmGet$columnObject();
            if (columnObjectObj != null) {
                some.test.AllTypes cachecolumnObject = (some.test.AllTypes) cache.get(columnObjectObj);
                if (cachecolumnObject != null) {
                    ((AllTypesRealmProxyInterface) realmObject).realmSet$columnObject(cachecolumnObject);
                } else {
                    ((AllTypesRealmProxyInterface) realmObject).realmSet$columnObject(AllTypesRealmProxy.copyOrUpdate(realm, columnObjectObj, update, cache));
                }
            } else {
                ((AllTypesRealmProxyInterface) realmObject).realmSet$columnObject(null);
            }

            RealmList<some.test.AllTypes> columnRealmListList = ((AllTypesRealmProxyInterface) newObject).realmGet$columnRealmList();
            if (columnRealmListList != null) {
                RealmList<some.test.AllTypes> columnRealmListRealmList = ((AllTypesRealmProxyInterface) realmObject).realmGet$columnRealmList();
                for (int i = 0; i < columnRealmListList.size(); i++) {
                    some.test.AllTypes columnRealmListItem = columnRealmListList.get(i);
                    some.test.AllTypes cachecolumnRealmList = (some.test.AllTypes) cache.get(columnRealmListItem);
                    if (cachecolumnRealmList != null) {
                        columnRealmListRealmList.add(cachecolumnRealmList);
                    } else {
                        columnRealmListRealmList.add(AllTypesRealmProxy.copyOrUpdate(realm, columnRealmListList.get(i), update, cache));
                    }
                }
            }

            return realmObject;
        }
    }

    public static long insert(Realm realm, some.test.AllTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativeTablePointer();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.schema.getColumnInfo(some.test.AllTypes.class);
        long pkColumnIndex = table.getPrimaryKey();
        String primaryKeyValue = ((AllTypesRealmProxyInterface) object).realmGet$columnString();
        long rowIndex = TableOrView.NO_MATCH;
        if (primaryKeyValue == null) {
            rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
        } else {
            rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
        }
        if (rowIndex == TableOrView.NO_MATCH) {
            rowIndex = table.addEmptyRowWithPrimaryKey(primaryKeyValue, false);
        } else {
            Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
        }
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.columnLongIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnLong(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnFloat(), false);
        Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnDouble(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnBoolean(), false);
        java.util.Date realmGet$columnDate = ((AllTypesRealmProxyInterface)object).realmGet$columnDate();
        if (realmGet$columnDate != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateIndex, rowIndex, realmGet$columnDate.getTime(), false);
        }
        byte[] realmGet$columnBinary = ((AllTypesRealmProxyInterface)object).realmGet$columnBinary();
        if (realmGet$columnBinary != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, realmGet$columnBinary, false);
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
        long tableNativePtr = table.getNativeTablePointer();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.schema.getColumnInfo(some.test.AllTypes.class);
        long pkColumnIndex = table.getPrimaryKey();
        some.test.AllTypes object = null;
        while (objects.hasNext()) {
            object = (some.test.AllTypes) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                String primaryKeyValue = ((AllTypesRealmProxyInterface) object).realmGet$columnString();
                long rowIndex = TableOrView.NO_MATCH;
                if (primaryKeyValue == null) {
                    rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
                } else {
                    rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
                }
                if (rowIndex == TableOrView.NO_MATCH) {
                    rowIndex = table.addEmptyRowWithPrimaryKey(primaryKeyValue, false);
                } else {
                    Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
                }
                cache.put(object, rowIndex);
                Table.nativeSetLong(tableNativePtr, columnInfo.columnLongIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnLong(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnFloat(), false);
                Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnDouble(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnBoolean(), false);
                java.util.Date realmGet$columnDate = ((AllTypesRealmProxyInterface)object).realmGet$columnDate();
                if (realmGet$columnDate != null) {
                    Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateIndex, rowIndex, realmGet$columnDate.getTime(), false);
                }
                byte[] realmGet$columnBinary = ((AllTypesRealmProxyInterface)object).realmGet$columnBinary();
                if (realmGet$columnBinary != null) {
                    Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, realmGet$columnBinary, false);
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
    }

    public static long insertOrUpdate(Realm realm, some.test.AllTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativeTablePointer();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.schema.getColumnInfo(some.test.AllTypes.class);
        long pkColumnIndex = table.getPrimaryKey();
        String primaryKeyValue = ((AllTypesRealmProxyInterface) object).realmGet$columnString();
        long rowIndex = TableOrView.NO_MATCH;
        if (primaryKeyValue == null) {
            rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
        } else {
            rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
        }
        if (rowIndex == TableOrView.NO_MATCH) {
            rowIndex = table.addEmptyRowWithPrimaryKey(primaryKeyValue, false);
        }
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.columnLongIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnLong(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnFloat(), false);
        Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnDouble(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnBoolean(), false);
        java.util.Date realmGet$columnDate = ((AllTypesRealmProxyInterface)object).realmGet$columnDate();
        if (realmGet$columnDate != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateIndex, rowIndex, realmGet$columnDate.getTime(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnDateIndex, rowIndex, false);
        }
        byte[] realmGet$columnBinary = ((AllTypesRealmProxyInterface)object).realmGet$columnBinary();
        if (realmGet$columnBinary != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, realmGet$columnBinary, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, false);
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
        long tableNativePtr = table.getNativeTablePointer();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.schema.getColumnInfo(some.test.AllTypes.class);
        long pkColumnIndex = table.getPrimaryKey();
        some.test.AllTypes object = null;
        while (objects.hasNext()) {
            object = (some.test.AllTypes) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                String primaryKeyValue = ((AllTypesRealmProxyInterface) object).realmGet$columnString();
                long rowIndex = TableOrView.NO_MATCH;
                if (primaryKeyValue == null) {
                    rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
                } else {
                    rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
                }
                if (rowIndex == TableOrView.NO_MATCH) {
                    rowIndex = table.addEmptyRowWithPrimaryKey(primaryKeyValue, false);
                }
                cache.put(object, rowIndex);
                Table.nativeSetLong(tableNativePtr, columnInfo.columnLongIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnLong(), false);
                Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnFloat(), false);
                Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnDouble(), false);
                Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanIndex, rowIndex, ((AllTypesRealmProxyInterface)object).realmGet$columnBoolean(), false);
                java.util.Date realmGet$columnDate = ((AllTypesRealmProxyInterface)object).realmGet$columnDate();
                if (realmGet$columnDate != null) {
                    Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateIndex, rowIndex, realmGet$columnDate.getTime(), false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.columnDateIndex, rowIndex, false);
                }
                byte[] realmGet$columnBinary = ((AllTypesRealmProxyInterface)object).realmGet$columnBinary();
                if (realmGet$columnBinary != null) {
                    Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, realmGet$columnBinary, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, false);
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
    }

    public static some.test.AllTypes createDetachedCopy(some.test.AllTypes realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.AllTypes unmanagedObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.AllTypes)cachedObject.object;
            } else {
                unmanagedObject = (some.test.AllTypes)cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            unmanagedObject = new some.test.AllTypes();
            cache.put(realmObject, new RealmObjectProxy.CacheData(currentDepth, unmanagedObject));
        }
        ((AllTypesRealmProxyInterface) unmanagedObject).realmSet$columnString(((AllTypesRealmProxyInterface) realmObject).realmGet$columnString());
        ((AllTypesRealmProxyInterface) unmanagedObject).realmSet$columnLong(((AllTypesRealmProxyInterface) realmObject).realmGet$columnLong());
        ((AllTypesRealmProxyInterface) unmanagedObject).realmSet$columnFloat(((AllTypesRealmProxyInterface) realmObject).realmGet$columnFloat());
        ((AllTypesRealmProxyInterface) unmanagedObject).realmSet$columnDouble(((AllTypesRealmProxyInterface) realmObject).realmGet$columnDouble());
        ((AllTypesRealmProxyInterface) unmanagedObject).realmSet$columnBoolean(((AllTypesRealmProxyInterface) realmObject).realmGet$columnBoolean());
        ((AllTypesRealmProxyInterface) unmanagedObject).realmSet$columnDate(((AllTypesRealmProxyInterface) realmObject).realmGet$columnDate());
        ((AllTypesRealmProxyInterface) unmanagedObject).realmSet$columnBinary(((AllTypesRealmProxyInterface) realmObject).realmGet$columnBinary());

        // Deep copy of columnObject
        ((AllTypesRealmProxyInterface) unmanagedObject).realmSet$columnObject(AllTypesRealmProxy.createDetachedCopy(((AllTypesRealmProxyInterface) realmObject).realmGet$columnObject(), currentDepth + 1, maxDepth, cache));

        // Deep copy of columnRealmList
        if (currentDepth == maxDepth) {
            ((AllTypesRealmProxyInterface) unmanagedObject).realmSet$columnRealmList(null);
        } else {
            RealmList<some.test.AllTypes> managedcolumnRealmListList = ((AllTypesRealmProxyInterface) realmObject).realmGet$columnRealmList();
            RealmList<some.test.AllTypes> unmanagedcolumnRealmListList = new RealmList<some.test.AllTypes>();
            ((AllTypesRealmProxyInterface) unmanagedObject).realmSet$columnRealmList(unmanagedcolumnRealmListList);
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
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnLong(((AllTypesRealmProxyInterface) newObject).realmGet$columnLong());
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnFloat(((AllTypesRealmProxyInterface) newObject).realmGet$columnFloat());
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnDouble(((AllTypesRealmProxyInterface) newObject).realmGet$columnDouble());
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnBoolean(((AllTypesRealmProxyInterface) newObject).realmGet$columnBoolean());
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnDate(((AllTypesRealmProxyInterface) newObject).realmGet$columnDate());
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnBinary(((AllTypesRealmProxyInterface) newObject).realmGet$columnBinary());
        some.test.AllTypes columnObjectObj = ((AllTypesRealmProxyInterface) newObject).realmGet$columnObject();
        if (columnObjectObj != null) {
            some.test.AllTypes cachecolumnObject = (some.test.AllTypes) cache.get(columnObjectObj);
            if (cachecolumnObject != null) {
                ((AllTypesRealmProxyInterface) realmObject).realmSet$columnObject(cachecolumnObject);
            } else {
                ((AllTypesRealmProxyInterface) realmObject).realmSet$columnObject(AllTypesRealmProxy.copyOrUpdate(realm, columnObjectObj, true, cache));
            }
        } else {
            ((AllTypesRealmProxyInterface) realmObject).realmSet$columnObject(null);
        }
        RealmList<some.test.AllTypes> columnRealmListList = ((AllTypesRealmProxyInterface) newObject).realmGet$columnRealmList();
        RealmList<some.test.AllTypes> columnRealmListRealmList = ((AllTypesRealmProxyInterface) realmObject).realmGet$columnRealmList();
        columnRealmListRealmList.clear();
        if (columnRealmListList != null) {
            for (int i = 0; i < columnRealmListList.size(); i++) {
                some.test.AllTypes columnRealmListItem = columnRealmListList.get(i);
                some.test.AllTypes cachecolumnRealmList = (some.test.AllTypes) cache.get(columnRealmListItem);
                if (cachecolumnRealmList != null) {
                    columnRealmListRealmList.add(cachecolumnRealmList);
                } else {
                    columnRealmListRealmList.add(AllTypesRealmProxy.copyOrUpdate(realm, columnRealmListList.get(i), true, cache));
                }
            }
        }
        return realmObject;
    }

    @Override
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("AllTypes = [");
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
    public ProxyState realmGet$proxyState() {
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
