package io.realm;


import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmFieldType;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.android.JsonUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import some.test.AllTypes;

public class AllTypesRealmProxy extends AllTypes
        implements RealmObjectProxy, AllTypesRealmProxyInterface {

    static final class AllTypesColumnInfo extends ColumnInfo {

        public final long columnStringIndex;
        public final long columnLongIndex;
        public final long columnFloatIndex;
        public final long columnDoubleIndex;
        public final long columnBooleanIndex;
        public final long columnDateIndex;
        public final long columnBinaryIndex;
        public final long columnObjectIndex;
        public final long columnRealmListIndex;

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
    }

    private final AllTypesColumnInfo columnInfo;
    private final ProxyState proxyState;
    private RealmList<AllTypes> columnRealmListRealmList;
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

    AllTypesRealmProxy(ColumnInfo columnInfo) {
        this.columnInfo = (AllTypesColumnInfo) columnInfo;
        this.proxyState = new ProxyState(AllTypes.class, this);
    }

    @SuppressWarnings("cast")
    public String realmGet$columnString() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.columnStringIndex);
    }

    public void realmSet$columnString(String value) {
        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.columnStringIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.columnStringIndex, value);
    }

    @SuppressWarnings("cast")
    public long realmGet$columnLong() {
        proxyState.getRealm$realm().checkIfValid();
        return (long) proxyState.getRow$realm().getLong(columnInfo.columnLongIndex);
    }

    public void realmSet$columnLong(long value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.columnLongIndex, value);
    }

    @SuppressWarnings("cast")
    public float realmGet$columnFloat() {
        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.columnFloatIndex);
    }

    public void realmSet$columnFloat(float value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setFloat(columnInfo.columnFloatIndex, value);
    }

    @SuppressWarnings("cast")
    public double realmGet$columnDouble() {
        proxyState.getRealm$realm().checkIfValid();
        return (double) proxyState.getRow$realm().getDouble(columnInfo.columnDoubleIndex);
    }

    public void realmSet$columnDouble(double value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setDouble(columnInfo.columnDoubleIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGet$columnBoolean() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.columnBooleanIndex);
    }

    public void realmSet$columnBoolean(boolean value) {
        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.columnBooleanIndex, value);
    }

    @SuppressWarnings("cast")
    public Date realmGet$columnDate() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.util.Date) proxyState.getRow$realm().getDate(columnInfo.columnDateIndex);
    }

    public void realmSet$columnDate(Date value) {
        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field columnDate to null.");
        }
        proxyState.getRow$realm().setDate(columnInfo.columnDateIndex, value);
    }

    @SuppressWarnings("cast")
    public byte[] realmGet$columnBinary() {
        proxyState.getRealm$realm().checkIfValid();
        return (byte[]) proxyState.getRow$realm().getBinaryByteArray(columnInfo.columnBinaryIndex);
    }

    public void realmSet$columnBinary(byte[] value) {
        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field columnBinary to null.");
        }
        proxyState.getRow$realm().setBinaryByteArray(columnInfo.columnBinaryIndex, value);
    }

    public AllTypes realmGet$columnObject() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNullLink(columnInfo.columnObjectIndex)) {
            return null;
        }
        return proxyState.getRealm$realm().get(some.test.AllTypes.class, proxyState.getRow$realm().getLink(columnInfo.columnObjectIndex));
    }

    public void realmSet$columnObject(AllTypes value) {
        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().nullifyLink(columnInfo.columnObjectIndex);
            return;
        }
        if (!RealmObject.isValid(value)) {
            throw new IllegalArgumentException("'value' is not a valid managed object.");
        }
        if (((RealmObjectProxy)value).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm()) {
            throw new IllegalArgumentException("'value' belongs to a different Realm.");
        }
        proxyState.getRow$realm().setLink(columnInfo.columnObjectIndex, ((RealmObjectProxy)value).realmGet$proxyState().getRow$realm().getIndex());
    }

    public RealmList<AllTypes> realmGet$columnRealmList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnRealmListRealmList != null) {
            return columnRealmListRealmList;
        } else {
            LinkView linkView = proxyState.getRow$realm().getLinkList(columnInfo.columnRealmListIndex);
            columnRealmListRealmList = new RealmList<AllTypes>(AllTypes.class, linkView, proxyState.getRealm$realm());
            return columnRealmListRealmList;
        }
    }

    public void realmSet$columnRealmList(RealmList<AllTypes> value) {
        proxyState.getRealm$realm().checkIfValid();
        LinkView links = proxyState.getRow$realm().getLinkList(columnInfo.columnRealmListIndex);
        links.clear();
        if (value == null) {
            return;
        }
        for (RealmModel linkedObject : (RealmList<? extends RealmModel>) value) {
            if (!RealmObject.isValid(linkedObject)) {
                throw new IllegalArgumentException("Each element of 'value' must be a valid managed object.");
            }
            if (((RealmObjectProxy)linkedObject).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm()) {
                throw new IllegalArgumentException("Each element of 'value' must belong to the same Realm.");
            }
            links.add(((RealmObjectProxy)linkedObject).realmGet$proxyState().getRow$realm().getIndex());
        }
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if (!transaction.hasTable("class_AllTypes")) {
            Table table = transaction.getTable("class_AllTypes");
            table.addColumn(RealmFieldType.STRING, "columnString", Table.NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "columnLong", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "columnFloat", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.DOUBLE, "columnDouble", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "columnBoolean", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.DATE, "columnDate", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BINARY, "columnBinary", Table.NOT_NULLABLE);
            if (!transaction.hasTable("class_AllTypes")) {
                AllTypesRealmProxy.initTable(transaction);
            }
            table.addColumnLink(RealmFieldType.OBJECT, "columnObject", transaction.getTable("class_AllTypes"));
            if (!transaction.hasTable("class_AllTypes")) {
                AllTypesRealmProxy.initTable(transaction);
            }
            table.addColumnLink(RealmFieldType.LIST, "columnRealmList", transaction.getTable("class_AllTypes"));
            table.addSearchIndex(table.getColumnIndex("columnString"));
            table.setPrimaryKey("columnString");
            return table;
        }
        return transaction.getTable("class_AllTypes");
    }

    public static AllTypesColumnInfo validateTable(ImplicitTransaction transaction) {
        if (transaction.hasTable("class_AllTypes")) {
            Table table = transaction.getTable("class_AllTypes");
            if (table.getColumnCount() != 9) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field count does not match - expected 9 but was " + table.getColumnCount());
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < 9; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final AllTypesColumnInfo columnInfo = new AllTypesColumnInfo(transaction.getPath(), table);

            if (!columnTypes.containsKey("columnString")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnString' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnString") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'columnString' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.columnStringIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(),"@PrimaryKey field 'columnString' does not support null values in the existing Realm file. Migrate using RealmObjectSchema.setNullable(), or mark the field as @Required.");
            }
            if (table.getPrimaryKey() != table.getColumnIndex("columnString")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Primary key not defined for field 'columnString' in existing Realm file. Add @PrimaryKey.");
            }
            if (!table.hasSearchIndex(table.getColumnIndex("columnString"))) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Index not defined for field 'columnString' in existing Realm file. Either set @Index or migrate using io.realm.internal.Table.removeSearchIndex().");
            }
            if (!columnTypes.containsKey("columnLong")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnLong' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnLong") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'long' for field 'columnLong' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnLongIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnLong' does support null values in the existing Realm file. Use corresponding boxed type for field 'columnLong' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("columnFloat")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnFloat' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnFloat") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'float' for field 'columnFloat' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnFloatIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnFloat' does support null values in the existing Realm file. Use corresponding boxed type for field 'columnFloat' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("columnDouble")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnDouble' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnDouble") != RealmFieldType.DOUBLE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'double' for field 'columnDouble' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnDoubleIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnDouble' does support null values in the existing Realm file. Use corresponding boxed type for field 'columnDouble' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("columnBoolean")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnBoolean' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnBoolean") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'columnBoolean' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnBooleanIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnBoolean' does support null values in the existing Realm file. Use corresponding boxed type for field 'columnBoolean' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("columnDate")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnDate' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnDate") != RealmFieldType.DATE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Date' for field 'columnDate' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnDateIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnDate' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'columnDate' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("columnBinary")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnBinary' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnBinary") != RealmFieldType.BINARY) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'byte[]' for field 'columnBinary' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnBinaryIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnBinary' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'columnBinary' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("columnObject")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnObject' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnObject") != RealmFieldType.OBJECT) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'AllTypes' for field 'columnObject'");
            }
            if (!transaction.hasTable("class_AllTypes")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing class 'class_AllTypes' for field 'columnObject'");
            }
            Table table_7 = transaction.getTable("class_AllTypes");
            if (!table.getLinkTarget(columnInfo.columnObjectIndex).hasSameSchema(table_7)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid RealmObject for field 'columnObject': '" + table.getLinkTarget(columnInfo.columnObjectIndex).getName() + "' expected - was '" + table_7.getName() + "'");
            }
            if (!columnTypes.containsKey("columnRealmList")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnRealmList'");
            }
            if (columnTypes.get("columnRealmList") != RealmFieldType.LIST) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'AllTypes' for field 'columnRealmList'");
            }
            if (!transaction.hasTable("class_AllTypes")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing class 'class_AllTypes' for field 'columnRealmList'");
            }
            Table table_8 = transaction.getTable("class_AllTypes");
            if (!table.getLinkTarget(columnInfo.columnRealmListIndex).hasSameSchema(table_8)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid RealmList type for field 'columnRealmList': '" + table.getLinkTarget(columnInfo.columnRealmListIndex).getName() + "' expected - was '" + table_8.getName() + "'");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(transaction.getPath(), "The AllTypes class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_AllTypes";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static AllTypes createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        AllTypes obj = null;
        if (update) {
            Table table = realm.getTable(AllTypes.class);
            long pkColumnIndex = table.getPrimaryKey();
            long rowIndex = TableOrView.NO_MATCH;
            if (json.isNull("columnString")) {
                rowIndex = table.findFirstNull(pkColumnIndex);
            } else {
                rowIndex = table.findFirstString(pkColumnIndex, json.getString("columnString"));
            }
            if (rowIndex != TableOrView.NO_MATCH) {
                obj = new AllTypesRealmProxy(realm.schema.getColumnInfo(AllTypes.class));
                ((RealmObjectProxy)obj).realmGet$proxyState().setRealm$realm(realm);
                ((RealmObjectProxy)obj).realmGet$proxyState().setRow$realm(table.getUncheckedRow(rowIndex));
            }
        }
        if (obj == null) {
            if (json.has("columnString")) {
                if (json.isNull("columnString")) {
                    obj = (AllTypesRealmProxy) realm.createObject(AllTypes.class, null);
                } else {
                    obj = (AllTypesRealmProxy) realm.createObject(AllTypes.class, json.getString("columnString"));
                }
            } else {
                obj = (AllTypesRealmProxy) realm.createObject(AllTypes.class);
            }
        }
        if (json.has("columnString")) {
            if (json.isNull("columnString")) {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnString(null);
            } else {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnString((String) json.getString("columnString"));
            }
        }
        if (json.has("columnLong")) {
            if (json.isNull("columnLong")) {
                throw new IllegalArgumentException("Trying to set non-nullable field columnLong to null.");
            } else {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnLong((long) json.getLong("columnLong"));
            }
        }
        if (json.has("columnFloat")) {
            if (json.isNull("columnFloat")) {
                throw new IllegalArgumentException("Trying to set non-nullable field columnFloat to null.");
            } else {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnFloat((float) json.getDouble("columnFloat"));
            }
        }
        if (json.has("columnDouble")) {
            if (json.isNull("columnDouble")) {
                throw new IllegalArgumentException("Trying to set non-nullable field columnDouble to null.");
            } else {
                ((AllTypesRealmProxyInterface) obj).realmSet$columnDouble((double) json.getDouble("columnDouble"));
            }
        }
        if (json.has("columnBoolean")) {
            if (json.isNull("columnBoolean")) {
                throw new IllegalArgumentException("Trying to set non-nullable field columnBoolean to null.");
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
    public static AllTypes createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        AllTypes obj = realm.createObject(AllTypes.class);
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
            } else if (name.equals("columnLong")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field columnLong to null.");
                } else {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnLong((long) reader.nextLong());
                }
            } else if (name.equals("columnFloat")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field columnFloat to null.");
                } else {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnFloat((float) reader.nextDouble());
                }
            } else if (name.equals("columnDouble")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field columnDouble to null.");
                } else {
                    ((AllTypesRealmProxyInterface) obj).realmSet$columnDouble((double) reader.nextDouble());
                }
            } else if (name.equals("columnBoolean")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field columnBoolean to null.");
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
        return obj;
    }

    public static AllTypes copyOrUpdate(Realm realm, AllTypes object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        AllTypes realmObject = null;
        boolean canUpdate = update;
        if (canUpdate) {
            Table table = realm.getTable(AllTypes.class);
            long pkColumnIndex = table.getPrimaryKey();
            String value = ((AllTypesRealmProxyInterface) object).realmGet$columnString();
            long rowIndex = TableOrView.NO_MATCH;
            if (value == null) {
                rowIndex = table.findFirstNull(pkColumnIndex);
            } else {
                rowIndex = table.findFirstString(pkColumnIndex, value);
            }
            if (rowIndex != TableOrView.NO_MATCH) {
                realmObject = new AllTypesRealmProxy(realm.schema.getColumnInfo(AllTypes.class));
                ((RealmObjectProxy)realmObject).realmGet$proxyState().setRealm$realm(realm);
                ((RealmObjectProxy)realmObject).realmGet$proxyState().setRow$realm(table.getUncheckedRow(rowIndex));
                cache.put(object, (RealmObjectProxy) realmObject);
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

    public static AllTypes copy(Realm realm, AllTypes newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        AllTypes realmObject = realm.createObject(AllTypes.class, ((AllTypesRealmProxyInterface) newObject).realmGet$columnString());
        cache.put(newObject, (RealmObjectProxy) realmObject);
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnString(((AllTypesRealmProxyInterface) newObject).realmGet$columnString());
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

        RealmList<AllTypes> columnRealmListList = ((AllTypesRealmProxyInterface) newObject).realmGet$columnRealmList();
        if (columnRealmListList != null) {
            RealmList<AllTypes> columnRealmListRealmList = ((AllTypesRealmProxyInterface) realmObject).realmGet$columnRealmList();
            for (int i = 0; i < columnRealmListList.size(); i++) {
                AllTypes columnRealmListItem = columnRealmListList.get(i);
                AllTypes cachecolumnRealmList = (AllTypes) cache.get(columnRealmListItem);
                if (cachecolumnRealmList != null) {
                    columnRealmListRealmList.add(cachecolumnRealmList);
                } else {
                    columnRealmListRealmList.add(AllTypesRealmProxy.copyOrUpdate(realm, columnRealmListList.get(i), update, cache));
                }
            }
        }

        return realmObject;
    }

    public static AllTypes createDetachedCopy(AllTypes realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        AllTypes standaloneObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (AllTypes)cachedObject.object;
            } else {
                standaloneObject = (AllTypes)cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            standaloneObject = new AllTypes();
            cache.put(realmObject, new RealmObjectProxy.CacheData(currentDepth, standaloneObject));
        }
        ((AllTypesRealmProxyInterface) standaloneObject).realmSet$columnString(((AllTypesRealmProxyInterface) realmObject).realmGet$columnString());
        ((AllTypesRealmProxyInterface) standaloneObject).realmSet$columnLong(((AllTypesRealmProxyInterface) realmObject).realmGet$columnLong());
        ((AllTypesRealmProxyInterface) standaloneObject).realmSet$columnFloat(((AllTypesRealmProxyInterface) realmObject).realmGet$columnFloat());
        ((AllTypesRealmProxyInterface) standaloneObject).realmSet$columnDouble(((AllTypesRealmProxyInterface) realmObject).realmGet$columnDouble());
        ((AllTypesRealmProxyInterface) standaloneObject).realmSet$columnBoolean(((AllTypesRealmProxyInterface) realmObject).realmGet$columnBoolean());
        ((AllTypesRealmProxyInterface) standaloneObject).realmSet$columnDate(((AllTypesRealmProxyInterface) realmObject).realmGet$columnDate());
        ((AllTypesRealmProxyInterface) standaloneObject).realmSet$columnBinary(((AllTypesRealmProxyInterface) realmObject).realmGet$columnBinary());

        // Deep copy of columnObject
        ((AllTypesRealmProxyInterface) standaloneObject).realmSet$columnObject(AllTypesRealmProxy.createDetachedCopy(((AllTypesRealmProxyInterface) realmObject).realmGet$columnObject(), currentDepth + 1, maxDepth, cache));

        // Deep copy of columnRealmList
        if (currentDepth == maxDepth) {
            ((AllTypesRealmProxyInterface) standaloneObject).realmSet$columnRealmList(null);
        } else {
            RealmList<AllTypes> managedcolumnRealmListList = ((AllTypesRealmProxyInterface) realmObject).realmGet$columnRealmList();
            RealmList<AllTypes> standalonecolumnRealmListList = new RealmList<AllTypes>();
            ((AllTypesRealmProxyInterface) standaloneObject).realmSet$columnRealmList(standalonecolumnRealmListList);
            int nextDepth = currentDepth + 1;
            int size = managedcolumnRealmListList.size();
            for (int i = 0; i < size; i++) {
                AllTypes item = AllTypesRealmProxy.createDetachedCopy(managedcolumnRealmListList.get(i), nextDepth, maxDepth, cache);
                standalonecolumnRealmListList.add(item);
            }
        }
        return standaloneObject;
    }

    static AllTypes update(Realm realm, AllTypes realmObject, AllTypes newObject, Map<RealmModel, RealmObjectProxy> cache) {
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnLong(((AllTypesRealmProxyInterface) newObject).realmGet$columnLong());
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnFloat(((AllTypesRealmProxyInterface) newObject).realmGet$columnFloat());
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnDouble(((AllTypesRealmProxyInterface) newObject).realmGet$columnDouble());
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnBoolean(((AllTypesRealmProxyInterface) newObject).realmGet$columnBoolean());
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnDate(((AllTypesRealmProxyInterface) newObject).realmGet$columnDate());
        ((AllTypesRealmProxyInterface) realmObject).realmSet$columnBinary(((AllTypesRealmProxyInterface) newObject).realmGet$columnBinary());
        AllTypes columnObjectObj = ((AllTypesRealmProxyInterface) newObject).realmGet$columnObject();
        if (columnObjectObj != null) {
            AllTypes cachecolumnObject = (AllTypes) cache.get(columnObjectObj);
            if (cachecolumnObject != null) {
                ((AllTypesRealmProxyInterface) realmObject).realmSet$columnObject(cachecolumnObject);
            } else {
                ((AllTypesRealmProxyInterface) realmObject).realmSet$columnObject(AllTypesRealmProxy.copyOrUpdate(realm, columnObjectObj, true, cache));
            }
        } else {
            ((AllTypesRealmProxyInterface) realmObject).realmSet$columnObject(null);
        }
        RealmList<AllTypes> columnRealmListList = ((AllTypesRealmProxyInterface) newObject).realmGet$columnRealmList();
        RealmList<AllTypes> columnRealmListRealmList = ((AllTypesRealmProxyInterface) realmObject).realmGet$columnRealmList();
        columnRealmListRealmList.clear();
        if (columnRealmListList != null) {
            for (int i = 0; i < columnRealmListList.size(); i++) {
                AllTypes columnRealmListItem = columnRealmListList.get(i);
                AllTypes cachecolumnRealmList = (AllTypes) cache.get(columnRealmListItem);
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
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aAllTypes.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aAllTypes.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }

}
