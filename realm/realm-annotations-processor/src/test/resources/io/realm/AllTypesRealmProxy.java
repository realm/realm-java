package io.realm;


import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmFieldType;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.android.JsonUtils;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import some.test.AllTypes;

public class AllTypesRealmProxy extends AllTypes
    implements RealmObjectProxy {

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
    }

    @SuppressWarnings("cast")
    public String realmGetter$columnString() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(columnInfo.columnStringIndex);
    }

    public void realmSetter$columnString(String value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field columnString to null.");
        }
        row.setString(columnInfo.columnStringIndex, value);
    }

    @SuppressWarnings("cast")
    public long realmGetter$columnLong() {
        realm.checkIfValid();
        return (long) row.getLong(columnInfo.columnLongIndex);
    }

    public void realmSetter$columnLong(long value) {
        realm.checkIfValid();
        row.setLong(columnInfo.columnLongIndex, value);
    }

    @SuppressWarnings("cast")
    public float realmGetter$columnFloat() {
        realm.checkIfValid();
        return (float) row.getFloat(columnInfo.columnFloatIndex);
    }

    public void realmSetter$columnFloat(float value) {
        realm.checkIfValid();
        row.setFloat(columnInfo.columnFloatIndex, value);
    }

    @SuppressWarnings("cast")
    public double realmGetter$columnDouble() {
        realm.checkIfValid();
        return (double) row.getDouble(columnInfo.columnDoubleIndex);
    }

    public void realmSetter$columnDouble(double value) {
        realm.checkIfValid();
        row.setDouble(columnInfo.columnDoubleIndex, value);
    }

    @SuppressWarnings("cast")
    public boolean realmGetter$columnBoolean() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(columnInfo.columnBooleanIndex);
    }

    public void realmSetter$columnBoolean(boolean value) {
        realm.checkIfValid();
        row.setBoolean(columnInfo.columnBooleanIndex, value);
    }

    @SuppressWarnings("cast")
    public Date realmGetter$columnDate() {
        realm.checkIfValid();
        return (java.util.Date) row.getDate(columnInfo.columnDateIndex);
    }

    public void realmSetter$columnDate(Date value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field columnDate to null.");
        }
        row.setDate(columnInfo.columnDateIndex, value);
    }

    @SuppressWarnings("cast")
    public byte[] realmGetter$columnBinary() {
        realm.checkIfValid();
        return (byte[]) row.getBinaryByteArray(columnInfo.columnBinaryIndex);
    }

    public void realmSetter$columnBinary(byte[] value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field columnBinary to null.");
        }
        row.setBinaryByteArray(columnInfo.columnBinaryIndex, value);
    }

    public AllTypes realmGetter$columnObject() {
        realm.checkIfValid();
        if (row.isNullLink(columnInfo.columnObjectIndex)) {
            return null;
        }
        return realm.get(some.test.AllTypes.class, row.getLink(columnInfo.columnObjectIndex));
    }

    public void realmSetter$columnObject(AllTypes value) {
        realm.checkIfValid();
        if (value == null) {
            row.nullifyLink(columnInfo.columnObjectIndex);
            return;
        }
        if (!value.isValid()) {
            throw new IllegalArgumentException("'value' is not a valid managed object.");
        }
        if (value.realm != this.realm) {
            throw new IllegalArgumentException("'value' belongs to a different Realm.");
        }
        row.setLink(columnInfo.columnObjectIndex, value.row.getIndex());
    }

    public RealmList<AllTypes> realmGetter$columnRealmList() {
        realm.checkIfValid();
        // use the cached value if available
        if (columnRealmListRealmList != null) {
            return columnRealmListRealmList;
        } else {
            LinkView linkView = row.getLinkList(columnInfo.columnRealmListIndex);
            columnRealmListRealmList = new RealmList<AllTypes>(AllTypes.class, linkView, realm);
            return columnRealmListRealmList;
        }
    }

    public void realmSetter$columnRealmList(RealmList<AllTypes> value) {
        realm.checkIfValid();
        LinkView links = row.getLinkList(columnInfo.columnRealmListIndex);
        links.clear();
        if (value == null) {
            return;
        }
        for (RealmObject linkedObject : (RealmList<? extends RealmObject>) value) {
            if (!linkedObject.isValid()) {
                throw new IllegalArgumentException("Each element of 'value' must be a valid managed object.");
            }
            if (linkedObject.realm != this.realm) {
                throw new IllegalArgumentException("Each element of 'value' must belong to the same Realm.");
            }
            links.add(linkedObject.row.getIndex());
        }
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if (!transaction.hasTable("class_AllTypes")) {
            Table table = transaction.getTable("class_AllTypes");
            table.addColumn(RealmFieldType.STRING, "columnString", Table.NOT_NULLABLE);
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
            if (table.isColumnNullable(columnInfo.columnStringIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnString' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'columnString' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
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
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnLong' does support null values in the existing Realm file. Use corresponding boxed type for field 'columnLong' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("columnFloat")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnFloat' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnFloat") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'float' for field 'columnFloat' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnFloatIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnFloat' does support null values in the existing Realm file. Use corresponding boxed type for field 'columnFloat' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("columnDouble")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnDouble' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnDouble") != RealmFieldType.DOUBLE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'double' for field 'columnDouble' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnDoubleIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnDouble' does support null values in the existing Realm file. Use corresponding boxed type for field 'columnDouble' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("columnBoolean")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnBoolean' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnBoolean") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'columnBoolean' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnBooleanIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnBoolean' does support null values in the existing Realm file. Use corresponding boxed type for field 'columnBoolean' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("columnDate")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnDate' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnDate") != RealmFieldType.DATE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Date' for field 'columnDate' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnDateIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnDate' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'columnDate' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("columnBinary")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnBinary' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("columnBinary") != RealmFieldType.BINARY) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'byte[]' for field 'columnBinary' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.columnBinaryIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'columnBinary' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'columnBinary' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
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
        AllTypesRealmProxy obj = null;
        if (update) {
            Table table = realm.getTable(AllTypes.class);
            long pkColumnIndex = table.getPrimaryKey();
            if (!json.isNull("columnString")) {
                long rowIndex = table.findFirstString(pkColumnIndex, json.getString("columnString"));
                if (rowIndex != TableOrView.NO_MATCH) {
                    obj = new AllTypesRealmProxy(realm.schema.getColumnInfo(AllTypes.class));
                    obj.realm = realm;
                    obj.row = table.getUncheckedRow(rowIndex);
                }
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
                obj.realmSetter$columnString(null);
            } else {
                obj.realmSetter$columnString((String) json.getString("columnString"));
            }
        }
        if (json.has("columnLong")) {
            if (json.isNull("columnLong")) {
                throw new IllegalArgumentException("Trying to set non-nullable field columnLong to null.");
            } else {
                obj.realmSetter$columnLong((long) json.getLong("columnLong"));
            }
        }
        if (json.has("columnFloat")) {
            if (json.isNull("columnFloat")) {
                throw new IllegalArgumentException("Trying to set non-nullable field columnFloat to null.");
            } else {
                obj.realmSetter$columnFloat((float) json.getDouble("columnFloat"));
            }
        }
        if (json.has("columnDouble")) {
            if (json.isNull("columnDouble")) {
                throw new IllegalArgumentException("Trying to set non-nullable field columnDouble to null.");
            } else {
                obj.realmSetter$columnDouble((double) json.getDouble("columnDouble"));
            }
        }
        if (json.has("columnBoolean")) {
            if (json.isNull("columnBoolean")) {
                throw new IllegalArgumentException("Trying to set non-nullable field columnBoolean to null.");
            } else {
                obj.realmSetter$columnBoolean((boolean) json.getBoolean("columnBoolean"));
            }
        }
        if (json.has("columnDate")) {
            if (json.isNull("columnDate")) {
                obj.realmSetter$columnDate(null);
            } else {
                Object timestamp = json.get("columnDate");
                if (timestamp instanceof String) {
                    obj.realmSetter$columnDate(JsonUtils.stringToDate((String) timestamp));
                } else {
                    obj.realmSetter$columnDate(new Date(json.getLong("columnDate")));
                }
            }
        }
        if (json.has("columnBinary")) {
            if (json.isNull("columnBinary")) {
                obj.realmSetter$columnBinary(null);
            } else {
                obj.realmSetter$columnBinary(JsonUtils.stringToBytes(json.getString("columnBinary")));
            }
        }
        if (json.has("columnObject")) {
            if (json.isNull("columnObject")) {
                obj.realmSetter$columnObject(null);
            } else {
                some.test.AllTypes columnObjectObj = AllTypesRealmProxy.createOrUpdateUsingJsonObject(realm, json.getJSONObject("columnObject"), update);
                obj.realmSetter$columnObject(columnObjectObj);
            }
        }
        if (json.has("columnRealmList")) {
            if (json.isNull("columnRealmList")) {
                obj.realmSetter$columnRealmList(null);
            } else {
                obj.realmGetter$columnRealmList().clear();
                JSONArray array = json.getJSONArray("columnRealmList");
                for (int i = 0; i < array.length(); i++) {
                    some.test.AllTypes item = AllTypesRealmProxy.createOrUpdateUsingJsonObject(realm, array.getJSONObject(i), update);
                    obj.realmGetter$columnRealmList().add(item);
                }
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    public static AllTypes createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        AllTypesRealmProxy obj = (AllTypesRealmProxy) realm.createObject(AllTypes.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("columnString")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$columnString(null);
                } else {
                    obj.realmSetter$columnString((String) reader.nextString());
                }
            } else if (name.equals("columnLong")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field columnLong to null.");
                } else {
                    obj.realmSetter$columnLong((long) reader.nextLong());
                }
            } else if (name.equals("columnFloat")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field columnFloat to null.");
                } else {
                    obj.realmSetter$columnFloat((float) reader.nextDouble());
                }
            } else if (name.equals("columnDouble")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field columnDouble to null.");
                } else {
                    obj.realmSetter$columnDouble((double) reader.nextDouble());
                }
            } else if (name.equals("columnBoolean")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field columnBoolean to null.");
                } else {
                    obj.realmSetter$columnBoolean((boolean) reader.nextBoolean());
                }
            } else if (name.equals("columnDate")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$columnDate(null);
                } else if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        obj.realmSetter$columnDate(new Date(timestamp));
                    }
                } else {
                    obj.realmSetter$columnDate(JsonUtils.stringToDate(reader.nextString()));
                }
            } else if (name.equals("columnBinary")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$columnBinary(null);
                } else {
                    obj.realmSetter$columnBinary(JsonUtils.stringToBytes(reader.nextString()));
                }
            } else if (name.equals("columnObject")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$columnObject(null);
                } else {
                    some.test.AllTypes columnObjectObj = AllTypesRealmProxy.createUsingJsonStream(realm, reader);
                    obj.realmSetter$columnObject(columnObjectObj);
                }
            } else if (name.equals("columnRealmList")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$columnRealmList(null);
                } else {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        some.test.AllTypes item = AllTypesRealmProxy.createUsingJsonStream(realm, reader);
                        obj.realmGetter$columnRealmList().add(item);
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

    public static AllTypes copyOrUpdate(Realm realm, AllTypes object, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        if (object.realm != null && object.realm.getPath().equals(realm.getPath())) {
            return object;
        }
        AllTypes realmObject = null;
        boolean canUpdate = update;
        if (canUpdate) {
            Table table = realm.getTable(AllTypes.class);
            long pkColumnIndex = table.getPrimaryKey();
            String primaryKey;
            if (object.realm != null) {
                primaryKey = ((AllTypesRealmProxy)object).realmGetter$columnString();
            } else {
                Field field = null;;
                Class<? extends AllTypes> clazz = object.getClass();
                try {
                    field = clazz.getDeclaredField("columnString");
                } catch (NoSuchFieldException e) {
                    throw new RealmException(e.getMessage());
                }
                field.setAccessible(true);
                try {
                    primaryKey = (String) field.get(object);
                } catch (IllegalAccessException e) {
                    throw new RealmException(e.getMessage());
                }
            }
            if (primaryKey == null) {
                throw new IllegalArgumentException("Primary key value must not be null.");
            }
            long rowIndex = table.findFirstString(pkColumnIndex, primaryKey);
            if (rowIndex != TableOrView.NO_MATCH) {
                realmObject = new AllTypesRealmProxy(realm.schema.getColumnInfo(AllTypes.class));
                realmObject.realm = realm;
                realmObject.row = table.getUncheckedRow(rowIndex);
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

    public static AllTypes copy(Realm realm, AllTypes from, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        final boolean isStandalone = !(from instanceof AllTypesRealmProxy);
        Class<? extends AllTypes> clazz;
        Field field = null;
        if (isStandalone) {
            clazz = from.getClass();
        } else {
            clazz = null;
        }
        AllTypesRealmProxy to;

        if (isStandalone) {
            try {
                field = clazz.getDeclaredField("columnString");
            } catch (NoSuchFieldException e) {
                throw new RealmException(e.getMessage());
            }
            field.setAccessible(true);
            Object primaryKey;
            try {
                primaryKey = field.get(from);
            } catch (IllegalAccessException e) {
                throw new RealmException(e.getMessage());
            }
            to = (AllTypesRealmProxy) realm.createObject(AllTypes.class, primaryKey);
        } else {
            to = (AllTypesRealmProxy) realm.createObject(AllTypes.class, ((AllTypesRealmProxy) from).realmGetter$columnString());
        }
        cache.put(from, (RealmObjectProxy) to);

        try {
            if (isStandalone) {
                field = clazz.getDeclaredField("columnString");
                field.setAccessible(true);
                to.realmSetter$columnString((java.lang.String) field.get(from));
            } else {
                to.realmSetter$columnString(((AllTypesRealmProxy) from).realmGetter$columnString());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("columnLong");
                field.setAccessible(true);
                to.realmSetter$columnLong((long) field.get(from));
            } else {
                to.realmSetter$columnLong(((AllTypesRealmProxy) from).realmGetter$columnLong());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("columnFloat");
                field.setAccessible(true);
                to.realmSetter$columnFloat((float) field.get(from));
            } else {
                to.realmSetter$columnFloat(((AllTypesRealmProxy) from).realmGetter$columnFloat());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("columnDouble");
                field.setAccessible(true);
                to.realmSetter$columnDouble((double) field.get(from));
            } else {
                to.realmSetter$columnDouble(((AllTypesRealmProxy) from).realmGetter$columnDouble());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("columnBoolean");
                field.setAccessible(true);
                to.realmSetter$columnBoolean((boolean) field.get(from));
            } else {
                to.realmSetter$columnBoolean(((AllTypesRealmProxy) from).realmGetter$columnBoolean());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("columnDate");
                field.setAccessible(true);
                to.realmSetter$columnDate((java.util.Date) field.get(from));
            } else {
                to.realmSetter$columnDate(((AllTypesRealmProxy) from).realmGetter$columnDate());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("columnBinary");
                field.setAccessible(true);
                to.realmSetter$columnBinary((byte[]) field.get(from));
            } else {
                to.realmSetter$columnBinary(((AllTypesRealmProxy) from).realmGetter$columnBinary());
            }

             {
                some.test.AllTypes columnObjectObj;
                if (isStandalone) {
                    field = clazz.getDeclaredField("columnObject");
                    field.setAccessible(true);
                    columnObjectObj = (some.test.AllTypes) field.get(from);
                } else {
                    columnObjectObj = ((AllTypesRealmProxy) from).realmGetter$columnObject();
                }
                if (columnObjectObj != null) {
                    some.test.AllTypes cachecolumnObject = (some.test.AllTypes) cache.get(columnObjectObj);
                    if (cachecolumnObject != null) {
                        to.realmSetter$columnObject(cachecolumnObject);
                    } else {
                        to.realmSetter$columnObject(AllTypesRealmProxy.copyOrUpdate(realm, columnObjectObj, update, cache));
                    }
                } else {
                    to.realmSetter$columnObject(null);
                }
            }

             {
                RealmList<AllTypes> columnRealmListList;
                if (isStandalone) {
                    field = clazz.getDeclaredField("columnRealmList");
                    field.setAccessible(true);
                    columnRealmListList = (RealmList<AllTypes>) field.get(from);
                } else {
                    columnRealmListList = ((AllTypesRealmProxy) from).realmGetter$columnRealmList();
                }
                if (columnRealmListList != null) {
                    RealmList<AllTypes> columnRealmListRealmList = to.realmGetter$columnRealmList();
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
            }
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        return to;
    }

    public static AllTypes createDetachedCopy(AllTypes realmObject, int currentDepth, int maxDepth, Map<RealmObject, CacheData<RealmObject>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<AllTypes> cachedObject = (CacheData) cache.get(realmObject);
        AllTypes standaloneObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return cachedObject.object;
            } else {
                standaloneObject = cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            standaloneObject = new AllTypes();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmObject>(currentDepth, standaloneObject));
        }
        Class<?> clazz = standaloneObject.getClass();
        Field field = null;
        try {
            field = clazz.getDeclaredField("columnString");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((AllTypesRealmProxy) realmObject).realmGetter$columnString());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("columnLong");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.setLong(standaloneObject, ((AllTypesRealmProxy) realmObject).realmGetter$columnLong());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("columnFloat");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.setFloat(standaloneObject, ((AllTypesRealmProxy) realmObject).realmGetter$columnFloat());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("columnDouble");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.setDouble(standaloneObject, ((AllTypesRealmProxy) realmObject).realmGetter$columnDouble());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("columnBoolean");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.setBoolean(standaloneObject, ((AllTypesRealmProxy) realmObject).realmGetter$columnBoolean());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("columnDate");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((AllTypesRealmProxy) realmObject).realmGetter$columnDate());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("columnBinary");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((AllTypesRealmProxy) realmObject).realmGetter$columnBinary());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("columnObject");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);

        // Deep copy of columnObject
        try {
            field.set(standaloneObject, AllTypesRealmProxy.createDetachedCopy( ((AllTypesRealmProxy) realmObject).realmGetter$columnObject(), currentDepth + 1, maxDepth, cache));
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("columnRealmList");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);

        // Deep copy of columnRealmList
        if (currentDepth == maxDepth) {
            try {
                field.set(standaloneObject, null);
            } catch (IllegalAccessException e) {
                throw new RealmException(e.getMessage());
            }
        } else {
            RealmList<AllTypes> managedcolumnRealmListList = ((AllTypesRealmProxy) realmObject).realmGetter$columnRealmList();
            RealmList<AllTypes> standalonecolumnRealmListList = new RealmList<AllTypes>();
            try {
                field.set(standaloneObject, standalonecolumnRealmListList);
            } catch (IllegalAccessException e) {
                throw new RealmException(e.getMessage());
            }
            int nextDepth = currentDepth + 1;
            int size = managedcolumnRealmListList.size();
            for (int i = 0; i < size; i++) {
                AllTypes item = AllTypesRealmProxy.createDetachedCopy(managedcolumnRealmListList.get(i), nextDepth, maxDepth, cache);
                standalonecolumnRealmListList.add(item);
            }
        }
        return standaloneObject;
    }

    static AllTypes update(Realm realm, AllTypes realmObject, AllTypes newObject, Map<RealmObject, RealmObjectProxy> cache) {
        if (newObject.realm == null) {
            Field field;
            Class<? extends AllTypes> clazz = newObject.getClass();
            try {
                field = clazz.getDeclaredField("columnLong");
            } catch (NoSuchFieldException e) {
                throw new RealmException(e.getMessage());
            }
            field.setAccessible(true);
            try {
                ((AllTypesRealmProxy) realmObject).realmSetter$columnLong(((long) field.get(newObject)));
            } catch (IllegalAccessException e) {
                throw new RealmException(e.getMessage());
            }
        } else {
            ((AllTypesRealmProxy) realmObject).realmSetter$columnLong(((AllTypesRealmProxy) newObject).realmGetter$columnLong());
        }
        if (newObject.realm == null) {
            Field field;
            Class<? extends AllTypes> clazz = newObject.getClass();
            try {
                field = clazz.getDeclaredField("columnFloat");
            } catch (NoSuchFieldException e) {
                throw new RealmException(e.getMessage());
            }
            field.setAccessible(true);
            try {
                ((AllTypesRealmProxy) realmObject).realmSetter$columnFloat(((float) field.get(newObject)));
            } catch (IllegalAccessException e) {
                throw new RealmException(e.getMessage());
            }
        } else {
            ((AllTypesRealmProxy) realmObject).realmSetter$columnFloat(((AllTypesRealmProxy) newObject).realmGetter$columnFloat());
        }
        if (newObject.realm == null) {
            Field field;
            Class<? extends AllTypes> clazz = newObject.getClass();
            try {
                field = clazz.getDeclaredField("columnDouble");
            } catch (NoSuchFieldException e) {
                throw new RealmException(e.getMessage());
            }
            field.setAccessible(true);
            try {
                ((AllTypesRealmProxy) realmObject).realmSetter$columnDouble(((double) field.get(newObject)));
            } catch (IllegalAccessException e) {
                throw new RealmException(e.getMessage());
            }
        } else {
            ((AllTypesRealmProxy) realmObject).realmSetter$columnDouble(((AllTypesRealmProxy) newObject).realmGetter$columnDouble());
        }
        if (newObject.realm == null) {
            Field field;
            Class<? extends AllTypes> clazz = newObject.getClass();
            try {
                field = clazz.getDeclaredField("columnBoolean");
            } catch (NoSuchFieldException e) {
                throw new RealmException(e.getMessage());
            }
            field.setAccessible(true);
            try {
                ((AllTypesRealmProxy) realmObject).realmSetter$columnBoolean(((boolean) field.get(newObject)));
            } catch (IllegalAccessException e) {
                throw new RealmException(e.getMessage());
            }
        } else {
            ((AllTypesRealmProxy) realmObject).realmSetter$columnBoolean(((AllTypesRealmProxy) newObject).realmGetter$columnBoolean());
        }
        if (newObject.realm == null) {
            Field field;
            Class<? extends AllTypes> clazz = newObject.getClass();
            try {
                field = clazz.getDeclaredField("columnDate");
            } catch (NoSuchFieldException e) {
                throw new RealmException(e.getMessage());
            }
            field.setAccessible(true);
            try {
                ((AllTypesRealmProxy) realmObject).realmSetter$columnDate(((Date) field.get(newObject)));
            } catch (IllegalAccessException e) {
                throw new RealmException(e.getMessage());
            }
        } else {
            ((AllTypesRealmProxy) realmObject).realmSetter$columnDate(((AllTypesRealmProxy) newObject).realmGetter$columnDate());
        }
        if (newObject.realm == null) {
            Field field;
            Class<? extends AllTypes> clazz = newObject.getClass();
            try {
                field = clazz.getDeclaredField("columnBinary");
            } catch (NoSuchFieldException e) {
                throw new RealmException(e.getMessage());
            }
            field.setAccessible(true);
            try {
                ((AllTypesRealmProxy) realmObject).realmSetter$columnBinary(((byte[]) field.get(newObject)));
            } catch (IllegalAccessException e) {
                throw new RealmException(e.getMessage());
            }
        } else {
            ((AllTypesRealmProxy) realmObject).realmSetter$columnBinary(((AllTypesRealmProxy) newObject).realmGetter$columnBinary());
        }
        AllTypes columnObjectObj = newObject.realmGetter$columnObject();
        if (columnObjectObj != null) {
            AllTypes cachecolumnObject = (AllTypes) cache.get(columnObjectObj);
            if (cachecolumnObject != null) {
                ((AllTypesRealmProxy) realmObject).realmSetter$columnObject(cachecolumnObject);
            } else {
                ((AllTypesRealmProxy) realmObject).realmSetter$columnObject(AllTypesRealmProxy.copyOrUpdate(realm, columnObjectObj, true, cache));
            }
        } else {
            ((AllTypesRealmProxy) realmObject).realmSetter$columnObject(null);
        }
        RealmList<AllTypes> columnRealmListList = newObject.realmGetter$columnRealmList();
        RealmList<AllTypes> columnRealmListRealmList = realmObject.realmGetter$columnRealmList();
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
        if (!isValid()) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("AllTypes = [");
        stringBuilder.append("{columnString:");
        stringBuilder.append(realmGetter$columnString());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnLong:");
        stringBuilder.append(realmGetter$columnLong());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnFloat:");
        stringBuilder.append(realmGetter$columnFloat());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDouble:");
        stringBuilder.append(realmGetter$columnDouble());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBoolean:");
        stringBuilder.append(realmGetter$columnBoolean());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDate:");
        stringBuilder.append(realmGetter$columnDate());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBinary:");
        stringBuilder.append(realmGetter$columnBinary());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnObject:");
        stringBuilder.append(realmGetter$columnObject() != null ? "AllTypes" : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnRealmList:");
        stringBuilder.append("RealmList<AllTypes>[").append(realmGetter$columnRealmList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        String realmName = realm.getPath();
        String tableName = row.getTable().getName();
        long rowIndex = row.getIndex();

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

        String path = realm.getPath();
        String otherPath = aAllTypes.realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = row.getTable().getName();
        String otherTableName = aAllTypes.row.getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (row.getIndex() != aAllTypes.row.getIndex()) return false;

        return true;
    }

}