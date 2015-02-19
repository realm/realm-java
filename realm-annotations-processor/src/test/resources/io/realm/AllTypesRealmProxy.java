package io.realm;


import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmObject;
import io.realm.exceptions.RealmException;
import io.realm.internal.ColumnType;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
import io.realm.internal.android.JsonUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import some.test.AllTypes;

public class AllTypesRealmProxy extends AllTypes {

    @Override
    public String getColumnString() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(Realm.columnIndices.get("AllTypes").get("columnString"));
    }

    @Override
    public void setColumnString(String value) {
        realm.checkIfValid();
        row.setString(Realm.columnIndices.get("AllTypes").get("columnString"), (String) value);
    }

    @Override
    public long getColumnLong() {
        realm.checkIfValid();
        return (long) row.getLong(Realm.columnIndices.get("AllTypes").get("columnLong"));
    }

    @Override
    public void setColumnLong(long value) {
        realm.checkIfValid();
        row.setLong(Realm.columnIndices.get("AllTypes").get("columnLong"), (long) value);
    }

    @Override
    public float getColumnFloat() {
        realm.checkIfValid();
        return (float) row.getFloat(Realm.columnIndices.get("AllTypes").get("columnFloat"));
    }

    @Override
    public void setColumnFloat(float value) {
        realm.checkIfValid();
        row.setFloat(Realm.columnIndices.get("AllTypes").get("columnFloat"), (float) value);
    }

    @Override
    public double getColumnDouble() {
        realm.checkIfValid();
        return (double) row.getDouble(Realm.columnIndices.get("AllTypes").get("columnDouble"));
    }

    @Override
    public void setColumnDouble(double value) {
        realm.checkIfValid();
        row.setDouble(Realm.columnIndices.get("AllTypes").get("columnDouble"), (double) value);
    }

    @Override
    public boolean isColumnBoolean() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(Realm.columnIndices.get("AllTypes").get("columnBoolean"));
    }

    @Override
    public void setColumnBoolean(boolean value) {
        realm.checkIfValid();
        row.setBoolean(Realm.columnIndices.get("AllTypes").get("columnBoolean"), (boolean) value);
    }

    @Override
    public Date getColumnDate() {
        realm.checkIfValid();
        return (java.util.Date) row.getDate(Realm.columnIndices.get("AllTypes").get("columnDate"));
    }

    @Override
    public void setColumnDate(Date value) {
        realm.checkIfValid();
        row.setDate(Realm.columnIndices.get("AllTypes").get("columnDate"), (Date) value);
    }

    @Override
    public byte[] getColumnBinary() {
        realm.checkIfValid();
        return (byte[]) row.getBinaryByteArray(Realm.columnIndices.get("AllTypes").get("columnBinary"));
    }

    @Override
    public void setColumnBinary(byte[] value) {
        realm.checkIfValid();
        row.setBinaryByteArray(Realm.columnIndices.get("AllTypes").get("columnBinary"), (byte[]) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if(!transaction.hasTable("class_AllTypes")) {
            Table table = transaction.getTable("class_AllTypes");
            table.addColumn(ColumnType.STRING, "columnString");
            table.addColumn(ColumnType.INTEGER, "columnLong");
            table.addColumn(ColumnType.FLOAT, "columnFloat");
            table.addColumn(ColumnType.DOUBLE, "columnDouble");
            table.addColumn(ColumnType.BOOLEAN, "columnBoolean");
            table.addColumn(ColumnType.DATE, "columnDate");
            table.addColumn(ColumnType.BINARY, "columnBinary");
            table.setPrimaryKey("");
            return table;
        }
        return transaction.getTable("class_AllTypes");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if(transaction.hasTable("class_AllTypes")) {
            Table table = transaction.getTable("class_AllTypes");
            if(table.getColumnCount() != 7) {
                throw new IllegalStateException("Column count does not match");
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for(long i = 0; i < 7; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }
            if (!columnTypes.containsKey("columnString")) {
                throw new IllegalStateException("Missing column 'columnString'");
            }
            if (columnTypes.get("columnString") != ColumnType.STRING) {
                throw new IllegalStateException("Invalid type 'String' for column 'columnString'");
            }
            if (!columnTypes.containsKey("columnLong")) {
                throw new IllegalStateException("Missing column 'columnLong'");
            }
            if (columnTypes.get("columnLong") != ColumnType.INTEGER) {
                throw new IllegalStateException("Invalid type 'long' for column 'columnLong'");
            }
            if (!columnTypes.containsKey("columnFloat")) {
                throw new IllegalStateException("Missing column 'columnFloat'");
            }
            if (columnTypes.get("columnFloat") != ColumnType.FLOAT) {
                throw new IllegalStateException("Invalid type 'float' for column 'columnFloat'");
            }
            if (!columnTypes.containsKey("columnDouble")) {
                throw new IllegalStateException("Missing column 'columnDouble'");
            }
            if (columnTypes.get("columnDouble") != ColumnType.DOUBLE) {
                throw new IllegalStateException("Invalid type 'double' for column 'columnDouble'");
            }
            if (!columnTypes.containsKey("columnBoolean")) {
                throw new IllegalStateException("Missing column 'columnBoolean'");
            }
            if (columnTypes.get("columnBoolean") != ColumnType.BOOLEAN) {
                throw new IllegalStateException("Invalid type 'boolean' for column 'columnBoolean'");
            }
            if (!columnTypes.containsKey("columnDate")) {
                throw new IllegalStateException("Missing column 'columnDate'");
            }
            if (columnTypes.get("columnDate") != ColumnType.DATE) {
                throw new IllegalStateException("Invalid type 'Date' for column 'columnDate'");
            }
            if (!columnTypes.containsKey("columnBinary")) {
                throw new IllegalStateException("Missing column 'columnBinary'");
            }
            if (columnTypes.get("columnBinary") != ColumnType.BINARY) {
                throw new IllegalStateException("Invalid type 'byte[]' for column 'columnBinary'");
            }
        }
    }

    public static List<String> getFieldNames() {
        return Arrays.asList("columnString", "columnLong", "columnFloat", "columnDouble", "columnBoolean", "columnDate", "columnBinary");
    }

    public static void populateUsingJsonObject(AllTypes obj, JSONObject json)
            throws JSONException {
        boolean standalone = obj.realm == null;
        if (json.has("columnString")) {
            obj.setColumnString((String) json.getString("columnString"));
        }
        if (json.has("columnLong")) {
            obj.setColumnLong((long) json.getLong("columnLong"));
        }
        if (json.has("columnFloat")) {
            obj.setColumnFloat((float) json.getDouble("columnFloat"));
        }
        if (json.has("columnDouble")) {
            obj.setColumnDouble((double) json.getDouble("columnDouble"));
        }
        if (json.has("columnBoolean")) {
            obj.setColumnBoolean((boolean) json.getBoolean("columnBoolean"));
        }
        if (json.has("columnDate")) {
            long timestamp = json.optLong("columnDate", -1);
            if (timestamp > -1) {
                obj.setColumnDate(new Date(timestamp));
            } else {
                String jsonDate = json.getString("columnDate");
                obj.setColumnDate(JsonUtils.stringToDate(jsonDate));
            }
        }
        if (json.has("columnBinary")) {
            obj.setColumnBinary(JsonUtils.stringToBytes(json.getString("columnBinary")));
        }
    }

    public static void populateUsingJsonStream(AllTypes obj, JsonReader reader)
            throws IOException {
        boolean standalone = obj.realm == null;
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
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    public static AllTypes copyOrUpdate(Realm realm, AllTypes object, boolean update, Map<RealmObject, RealmObject> cache) {
        return copy(realm, object, update, cache);
    }

    public static AllTypes copy(Realm realm, AllTypes newObject, boolean update, Map<RealmObject, RealmObject> cache) {
        AllTypes realmObject = realm.createObject(AllTypes.class);
        cache.put(newObject, realmObject);
        realmObject.setColumnString(newObject.getColumnString() != null ? newObject.getColumnString() : "");
        realmObject.setColumnLong(newObject.getColumnLong());
        realmObject.setColumnFloat(newObject.getColumnFloat());
        realmObject.setColumnDouble(newObject.getColumnDouble());
        realmObject.setColumnBoolean(newObject.isColumnBoolean());
        realmObject.setColumnDate(newObject.getColumnDate() != null ? newObject.getColumnDate() : new Date(0));
        realmObject.setColumnBinary(newObject.getColumnBinary() != null ? newObject.getColumnBinary() : new byte[0]);
        return realmObject;
    }

    static AllTypes update(Realm realm, AllTypes realmObject, AllTypes newObject, Map<RealmObject, RealmObject> cache) {
        realmObject.setColumnString(newObject.getColumnString() != null ? newObject.getColumnString() : "");
        realmObject.setColumnLong(newObject.getColumnLong());
        realmObject.setColumnFloat(newObject.getColumnFloat());
        realmObject.setColumnDouble(newObject.getColumnDouble());
        realmObject.setColumnBoolean(newObject.isColumnBoolean());
        realmObject.setColumnDate(newObject.getColumnDate() != null ? newObject.getColumnDate() : new Date(0));
        realmObject.setColumnBinary(newObject.getColumnBinary() != null ? newObject.getColumnBinary() : new byte[0]);
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
        stringBuilder.append(getColumnBinary());
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