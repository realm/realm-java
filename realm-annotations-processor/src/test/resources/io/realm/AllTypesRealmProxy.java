package io.realm;


import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmObject;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.android.JsonUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static long INDEX_COLUMNSTRING;
    private static long INDEX_COLUMNLONG;
    private static long INDEX_COLUMNFLOAT;
    private static long INDEX_COLUMNDOUBLE;
    private static long INDEX_COLUMNBOOLEAN;
    private static long INDEX_COLUMNDATE;
    private static long INDEX_COLUMNBINARY;
    private static long INDEX_COLUMNOBJECT;
    private static long INDEX_COLUMNREALMLIST;
    private static Map<String, Long> columnIndices;
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

    @Override
    public String getColumnString() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(INDEX_COLUMNSTRING);
    }

    @Override
    public void setColumnString(String value) {
        realm.checkIfValid();
        row.setString(INDEX_COLUMNSTRING, (String) value);
    }

    @Override
    public long getColumnLong() {
        realm.checkIfValid();
        return (long) row.getLong(INDEX_COLUMNLONG);
    }

    @Override
    public void setColumnLong(long value) {
        realm.checkIfValid();
        row.setLong(INDEX_COLUMNLONG, (long) value);
    }

    @Override
    public float getColumnFloat() {
        realm.checkIfValid();
        return (float) row.getFloat(INDEX_COLUMNFLOAT);
    }

    @Override
    public void setColumnFloat(float value) {
        realm.checkIfValid();
        row.setFloat(INDEX_COLUMNFLOAT, (float) value);
    }

    @Override
    public double getColumnDouble() {
        realm.checkIfValid();
        return (double) row.getDouble(INDEX_COLUMNDOUBLE);
    }

    @Override
    public void setColumnDouble(double value) {
        realm.checkIfValid();
        row.setDouble(INDEX_COLUMNDOUBLE, (double) value);
    }

    @Override
    public boolean isColumnBoolean() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(INDEX_COLUMNBOOLEAN);
    }

    @Override
    public void setColumnBoolean(boolean value) {
        realm.checkIfValid();
        row.setBoolean(INDEX_COLUMNBOOLEAN, (boolean) value);
    }

    @Override
    public Date getColumnDate() {
        realm.checkIfValid();
        return (java.util.Date) row.getDate(INDEX_COLUMNDATE);
    }

    @Override
    public void setColumnDate(Date value) {
        realm.checkIfValid();
        row.setDate(INDEX_COLUMNDATE, (Date) value);
    }

    @Override
    public byte[] getColumnBinary() {
        realm.checkIfValid();
        return (byte[]) row.getBinaryByteArray(INDEX_COLUMNBINARY);
    }

    @Override
    public void setColumnBinary(byte[] value) {
        realm.checkIfValid();
        row.setBinaryByteArray(INDEX_COLUMNBINARY, (byte[]) value);
    }

    @Override
    public AllTypes getColumnObject() {
        if (row.isNullLink(INDEX_COLUMNOBJECT)) {
            return null;
        }
        return realm.get(some.test.AllTypes.class, row.getLink(INDEX_COLUMNOBJECT));
    }

    @Override
    public void setColumnObject(AllTypes value) {
        if (value == null) {
            row.nullifyLink(INDEX_COLUMNOBJECT);
            return;
        }
        row.setLink(INDEX_COLUMNOBJECT, value.row.getIndex());
    }

    @Override
    public RealmList<AllTypes> getColumnRealmList() {
        return new RealmList<AllTypes>(AllTypes.class, row.getLinkList(INDEX_COLUMNREALMLIST), realm);
    }

    @Override
    public void setColumnRealmList(RealmList<AllTypes> value) {
        LinkView links = row.getLinkList(INDEX_COLUMNREALMLIST);
        if (value == null) {
            return;
        }
        links.clear();
        for (RealmObject linkedObject : (RealmList<? extends RealmObject>) value) {
            links.add(linkedObject.row.getIndex());
        }
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if(!transaction.hasTable("class_AllTypes")) {
            Table table = transaction.getTable("class_AllTypes");
            table.addColumn(ColumnType.STRING, "columnString", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.INTEGER, "columnLong", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.FLOAT, "columnFloat", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.DOUBLE, "columnDouble", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.BOOLEAN, "columnBoolean", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.DATE, "columnDate", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.BINARY, "columnBinary", Table.NULLABLE);
            if (!transaction.hasTable("class_AllTypes")) {
                AllTypesRealmProxy.initTable(transaction);
            }
            table.addColumnLink(ColumnType.LINK, "columnObject", transaction.getTable("class_AllTypes"));
            if (!transaction.hasTable("class_AllTypes")) {
                AllTypesRealmProxy.initTable(transaction);
            }
            table.addColumnLink(ColumnType.LINK_LIST, "columnRealmList", transaction.getTable("class_AllTypes"));
            table.addSearchIndex(table.getColumnIndex("columnString"));
            table.setPrimaryKey("columnString");
            return table;
        }
        return transaction.getTable("class_AllTypes");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if (transaction.hasTable("class_AllTypes")) {
            Table table = transaction.getTable("class_AllTypes");
            if (table.getColumnCount() != 9) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field count does not match - expected 9 but was " + table.getColumnCount());
            }

            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for (long i = 0; i < 9; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            columnIndices = new HashMap<String, Long>();
            for (String fieldName : getFieldNames()) {
                long index = table.getColumnIndex(fieldName);
                if (index == -1) {
                    throw new RealmMigrationNeededException(transaction.getPath(), "Field '" + fieldName + "' not found for type AllTypes");
                }
                columnIndices.put(fieldName, index);
            }
            INDEX_COLUMNSTRING = table.getColumnIndex("columnString");
            INDEX_COLUMNLONG = table.getColumnIndex("columnLong");
            INDEX_COLUMNFLOAT = table.getColumnIndex("columnFloat");
            INDEX_COLUMNDOUBLE = table.getColumnIndex("columnDouble");
            INDEX_COLUMNBOOLEAN = table.getColumnIndex("columnBoolean");
            INDEX_COLUMNDATE = table.getColumnIndex("columnDate");
            INDEX_COLUMNBINARY = table.getColumnIndex("columnBinary");
            INDEX_COLUMNOBJECT = table.getColumnIndex("columnObject");
            INDEX_COLUMNREALMLIST = table.getColumnIndex("columnRealmList");

            if (!columnTypes.containsKey("columnString")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnString'");
            }
            if (columnTypes.get("columnString") != ColumnType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'columnString'");
            }
            if (table.getPrimaryKey() != table.getColumnIndex("columnString")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Primary key not defined for field 'columnString'");
            }
            if (!table.hasSearchIndex(table.getColumnIndex("columnString"))) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Index not defined for field 'columnString'");
            }
            if (!columnTypes.containsKey("columnLong")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnLong'");
            }
            if (columnTypes.get("columnLong") != ColumnType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'long' for field 'columnLong'");
            }
            if (!columnTypes.containsKey("columnFloat")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnFloat'");
            }
            if (columnTypes.get("columnFloat") != ColumnType.FLOAT) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'float' for field 'columnFloat'");
            }
            if (!columnTypes.containsKey("columnDouble")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnDouble'");
            }
            if (columnTypes.get("columnDouble") != ColumnType.DOUBLE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'double' for field 'columnDouble'");
            }
            if (!columnTypes.containsKey("columnBoolean")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnBoolean'");
            }
            if (columnTypes.get("columnBoolean") != ColumnType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'boolean' for field 'columnBoolean'");
            }
            if (!columnTypes.containsKey("columnDate")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnDate'");
            }
            if (columnTypes.get("columnDate") != ColumnType.DATE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Date' for field 'columnDate'");
            }
            if (!columnTypes.containsKey("columnBinary")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnBinary'");
            }
            if (columnTypes.get("columnBinary") != ColumnType.BINARY) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'byte[]' for field 'columnBinary'");
            }
            if (!columnTypes.containsKey("columnObject")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnObject'");
            }
            if (columnTypes.get("columnObject") != ColumnType.LINK) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'AllTypes' for field 'columnObject'");
            }
            if (!transaction.hasTable("class_AllTypes")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing class 'class_AllTypes' for field 'columnObject'");
            }

            Table table_7 = transaction.getTable("class_AllTypes");
            if (!table.getLinkTarget(INDEX_COLUMNOBJECT).equals(table_7)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid RealmObject for field 'columnObject': '" +
                table.getLinkTarget(INDEX_COLUMNOBJECT).getName() + "' expected - was '" +
                table_7.getName() + "'");
            }
            if (!columnTypes.containsKey("columnRealmList")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'columnRealmList'");
            }
            if (columnTypes.get("columnRealmList") != ColumnType.LINK_LIST) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'AllTypes' for field 'columnRealmList'");
            }
            if (!transaction.hasTable("class_AllTypes")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing class 'class_AllTypes' for field 'columnRealmList'");
            }
            Table table_8 = transaction.getTable("class_AllTypes");
            if (!table.getLinkTarget(INDEX_COLUMNREALMLIST).equals(table_8)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid RealmList type for field 'columnRealmList': '" +
                        table.getLinkTarget(INDEX_COLUMNREALMLIST).getName() + "' expected - was '" +
                        table_8.getName() + "'");
            }
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

    public static Map<String,Long> getColumnIndices() {
        return columnIndices;
    }

    public static AllTypes createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        AllTypes obj = null;
        if (update) {
            Table table = realm.getTable(AllTypes.class);
            long pkColumnIndex = table.getPrimaryKey();
            if (!json.isNull("columnString")) {
                long rowIndex = table.findFirstString(pkColumnIndex, json.getString("columnString"));
                if (rowIndex != TableOrView.NO_MATCH) {
                    obj = new AllTypesRealmProxy();
                    obj.realm = realm;
                    obj.row = table.getRow(rowIndex);
                }
            }
        }
        if (obj == null) {
            obj = realm.createObject(AllTypes.class);
        }
        if (!json.isNull("columnString")) {
            obj.setColumnString((String) json.getString("columnString"));
        }
        if (!json.isNull("columnLong")) {
            obj.setColumnLong((long) json.getLong("columnLong"));
        }
        if (!json.isNull("columnFloat")) {
            obj.setColumnFloat((float) json.getDouble("columnFloat"));
        }
        if (!json.isNull("columnDouble")) {
            obj.setColumnDouble((double) json.getDouble("columnDouble"));
        }
        if (!json.isNull("columnBoolean")) {
            obj.setColumnBoolean((boolean) json.getBoolean("columnBoolean"));
        }
        if (!json.isNull("columnDate")) {
            Object timestamp = json.get("columnDate");
            if (timestamp instanceof String) {
                obj.setColumnDate(JsonUtils.stringToDate((String) timestamp));
            } else {
                obj.setColumnDate(new Date(json.getLong("columnDate")));
            }
        }
        if (!json.isNull("columnBinary")) {
            obj.setColumnBinary(JsonUtils.stringToBytes(json.getString("columnBinary")));
        }
        if (!json.isNull("columnObject")) {
            some.test.AllTypes columnObjectObj = AllTypesRealmProxy.createOrUpdateUsingJsonObject(realm, json.getJSONObject("columnObject"), update);
            obj.setColumnObject(columnObjectObj);
        }
        if (!json.isNull("columnRealmList")) {
            obj.getColumnRealmList().clear();
            JSONArray array = json.getJSONArray("columnRealmList");
            for (int i = 0; i < array.length(); i++) {
                some.test.AllTypes item = AllTypesRealmProxy.createOrUpdateUsingJsonObject(realm, array.getJSONObject(i), update);
                obj.getColumnRealmList().add(item);
            }
        }
        return obj;
    }

    public static AllTypes createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        AllTypes obj = realm.createObject(AllTypes.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("columnString") && reader.peek() != JsonToken.NULL) {
                obj.setColumnString((String) reader.nextString());
            } else if (name.equals("columnLong")  && reader.peek() != JsonToken.NULL) {
                obj.setColumnLong((long) reader.nextLong());
            } else if (name.equals("columnFloat")  && reader.peek() != JsonToken.NULL) {
                obj.setColumnFloat((float) reader.nextDouble());
            } else if (name.equals("columnDouble")  && reader.peek() != JsonToken.NULL) {
                obj.setColumnDouble((double) reader.nextDouble());
            } else if (name.equals("columnBoolean")  && reader.peek() != JsonToken.NULL) {
                obj.setColumnBoolean((boolean) reader.nextBoolean());
            } else if (name.equals("columnDate")  && reader.peek() != JsonToken.NULL) {
                if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        obj.setColumnDate(new Date(timestamp));
                    }
                } else {
                    obj.setColumnDate(JsonUtils.stringToDate(reader.nextString()));
                }
            } else if (name.equals("columnBinary")  && reader.peek() != JsonToken.NULL) {
                obj.setColumnBinary(JsonUtils.stringToBytes(reader.nextString()));
            } else if (name.equals("columnObject")  && reader.peek() != JsonToken.NULL) {
                some.test.AllTypes columnObjectObj = AllTypesRealmProxy.createUsingJsonStream(realm, reader);
                obj.setColumnObject(columnObjectObj);
            } else if (name.equals("columnRealmList")  && reader.peek() != JsonToken.NULL) {
                reader.beginArray();
                while (reader.hasNext()) {
                    some.test.AllTypes item = AllTypesRealmProxy.createUsingJsonStream(realm, reader);
                    obj.getColumnRealmList().add(item);
                }
                reader.endArray();
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
            if (object.getColumnString() == null) {
                throw new IllegalArgumentException("Primary key value must not be null.");
            }
            long rowIndex = table.findFirstString(pkColumnIndex, object.getColumnString());
            if (rowIndex != TableOrView.NO_MATCH) {
                realmObject = new AllTypesRealmProxy();
                realmObject.realm = realm;
                realmObject.row = table.getRow(rowIndex);
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

    public static AllTypes copy(Realm realm, AllTypes newObject, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        AllTypes realmObject = realm.createObject(AllTypes.class, newObject.getColumnString());
        cache.put(newObject, (RealmObjectProxy) realmObject);
        realmObject.setColumnString(newObject.getColumnString() != null ? newObject.getColumnString() : "");
        realmObject.setColumnLong(newObject.getColumnLong());
        realmObject.setColumnFloat(newObject.getColumnFloat());
        realmObject.setColumnDouble(newObject.getColumnDouble());
        realmObject.setColumnBoolean(newObject.isColumnBoolean());
        realmObject.setColumnDate(newObject.getColumnDate() != null ? newObject.getColumnDate() : new Date(0));
        realmObject.setColumnBinary(newObject.getColumnBinary());

        some.test.AllTypes columnObjectObj = newObject.getColumnObject();
        if (columnObjectObj != null) {
            some.test.AllTypes cachecolumnObject = (some.test.AllTypes) cache.get(columnObjectObj);
            if (cachecolumnObject != null) {
                realmObject.setColumnObject(cachecolumnObject);
            } else {
                realmObject.setColumnObject(AllTypesRealmProxy.copyOrUpdate(realm, columnObjectObj, update, cache));
            }
        }

        RealmList<AllTypes> columnRealmListList = newObject.getColumnRealmList();
        if (columnRealmListList != null) {
            RealmList<AllTypes> columnRealmListRealmList = realmObject.getColumnRealmList();
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

    static AllTypes update(Realm realm, AllTypes realmObject, AllTypes newObject, Map<RealmObject, RealmObjectProxy> cache) {
        realmObject.setColumnLong(newObject.getColumnLong());
        realmObject.setColumnFloat(newObject.getColumnFloat());
        realmObject.setColumnDouble(newObject.getColumnDouble());
        realmObject.setColumnBoolean(newObject.isColumnBoolean());
        realmObject.setColumnDate(newObject.getColumnDate() != null ? newObject.getColumnDate() : new Date(0));
        realmObject.setColumnBinary(newObject.getColumnBinary());
        AllTypes columnObjectObj = newObject.getColumnObject();
        if (columnObjectObj != null) {
            AllTypes cachecolumnObject = (AllTypes) cache.get(columnObjectObj);
            if (cachecolumnObject != null) {
                realmObject.setColumnObject(cachecolumnObject);
            } else {
                realmObject.setColumnObject(AllTypesRealmProxy.copyOrUpdate(realm, columnObjectObj, true, cache));
            }
        } else {
            realmObject.setColumnObject(null);
        }
        RealmList<AllTypes> columnRealmListList = newObject.getColumnRealmList();
        RealmList<AllTypes> columnRealmListRealmList = realmObject.getColumnRealmList();
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
        stringBuilder.append(getColumnString());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnLong:");
        stringBuilder.append(getColumnLong());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnFloat:");
        stringBuilder.append(getColumnFloat());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDouble:");
        stringBuilder.append(getColumnDouble());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBoolean:");
        stringBuilder.append(isColumnBoolean());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDate:");
        stringBuilder.append(getColumnDate());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBinary:");
        stringBuilder.append(getColumnBinary() != null ? getColumnBinary() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnObject:");
        stringBuilder.append(getColumnObject() != null ? "AllTypes" : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnRealmList:");
        stringBuilder.append("RealmList<AllTypes>[").append(getColumnRealmList().size()).append("]");
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
 
