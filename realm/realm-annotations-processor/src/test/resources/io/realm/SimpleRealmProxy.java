package io.realm;


import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmFieldType;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.ImplicitTransaction;
import io.realm.internal.LinkView;
import io.realm.internal.RealmObjectProxy;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import some.test.Simple;

public class SimpleRealmProxy extends Simple
    implements RealmObjectProxy, SimpleRealmProxyInterface {

    static final class SimpleColumnInfo extends ColumnInfo {

        public final long nameIndex;
        public final long ageIndex;

        SimpleColumnInfo(String path, Table table) {
            final Map<String, Long> indicesMap = new HashMap<String, Long>(2);
            this.nameIndex = getValidColumnIndex(path, table, "Simple", "name");
            indicesMap.put("name", this.nameIndex);

            this.ageIndex = getValidColumnIndex(path, table, "Simple", "age");
            indicesMap.put("age", this.ageIndex);

            setIndicesMap(indicesMap);
        }
    }

    private final SimpleColumnInfo columnInfo;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("name");
        fieldNames.add("age");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    SimpleRealmProxy(ColumnInfo columnInfo) {
        this.columnInfo = (SimpleColumnInfo) columnInfo;
    }

    @SuppressWarnings("cast")
    public String realmGet$name() {
        ((RealmObject) this).realm.checkIfValid();
        return (java.lang.String) ((RealmObject) this).row.getString(columnInfo.nameIndex);
    }

    public void realmSet$name(String value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            ((RealmObject) this).row.setNull(columnInfo.nameIndex);
            return;
        }
        ((RealmObject) this).row.setString(columnInfo.nameIndex, value);
    }

    @SuppressWarnings("cast")
    public int realmGet$age() {
        ((RealmObject) this).realm.checkIfValid();
        return (int) ((RealmObject) this).row.getLong(columnInfo.ageIndex);
    }

    public void realmSet$age(int value) {
        ((RealmObject) this).realm.checkIfValid();
        ((RealmObject) this).row.setLong(columnInfo.ageIndex, value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if (!transaction.hasTable("class_Simple")) {
            Table table = transaction.getTable("class_Simple");
            table.addColumn(RealmFieldType.STRING, "name", Table.NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "age", Table.NOT_NULLABLE);
            table.setPrimaryKey("");
            return table;
        }
        return transaction.getTable("class_Simple");
    }

    public static SimpleColumnInfo validateTable(ImplicitTransaction transaction) {
        if (transaction.hasTable("class_Simple")) {
            Table table = transaction.getTable("class_Simple");
            if (table.getColumnCount() != 2) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field count does not match - expected 2 but was " + table.getColumnCount());
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < 2; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final SimpleColumnInfo columnInfo = new SimpleColumnInfo(transaction.getPath(), table);

            if (!columnTypes.containsKey("name")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'name' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("name") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'name' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.nameIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'name' is required. Either set @Required to field 'name' or migrate using io.realm.internal.Table.convertColumnToNullable().");
            }
            if (!columnTypes.containsKey("age")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'age' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("age") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'int' for field 'age' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.ageIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'age' does support null values in the existing Realm file. Use corresponding boxed type for field 'age' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(transaction.getPath(), "The Simple class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_Simple";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static Simple createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        Simple obj = realm.createObject(Simple.class);
        if (json.has("name")) {
            if (json.isNull("name")) {
                ((SimpleRealmProxyInterface) obj).realmSet$name(null);
            } else {
                ((SimpleRealmProxyInterface) obj).realmSet$name((String) json.getString("name"));
            }
        }
        if (json.has("age")) {
            if (json.isNull("age")) {
                throw new IllegalArgumentException("Trying to set non-nullable field age to null.");
            } else {
                ((SimpleRealmProxyInterface) obj).realmSet$age((int) json.getInt("age"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    public static Simple createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        Simple obj = realm.createObject(Simple.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((SimpleRealmProxyInterface) obj).realmSet$name(null);
                } else {
                    ((SimpleRealmProxyInterface) obj).realmSet$name((String) reader.nextString());
                }
            } else if (name.equals("age")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field age to null.");
                } else {
                    ((SimpleRealmProxyInterface) obj).realmSet$age((int) reader.nextInt());
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return obj;
    }

    public static Simple copyOrUpdate(Realm realm, Simple object, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        if (((RealmObject) object).realm != null && ((RealmObject) object).realm.getPath().equals(realm.getPath())) {
            return object;
        }
        return copy(realm, object, update, cache);
    }

    public static Simple copy(Realm realm, Simple newObject, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        Simple realmObject = realm.createObject(Simple.class);
        cache.put(newObject, (RealmObjectProxy) realmObject);
        ((SimpleRealmProxyInterface) realmObject).realmSet$name(((SimpleRealmProxyInterface) newObject).realmGet$name());
        ((SimpleRealmProxyInterface) realmObject).realmSet$age(((SimpleRealmProxyInterface) newObject).realmGet$age());
        return realmObject;
    }

    public static Simple createDetachedCopy(Simple realmObject, int currentDepth, int maxDepth, Map<RealmObject, CacheData<RealmObject>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<Simple> cachedObject = (CacheData) cache.get(realmObject);
        Simple standaloneObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return cachedObject.object;
            } else {
                standaloneObject = cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            standaloneObject = new Simple();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmObject>(currentDepth, standaloneObject));
        }
        ((SimpleRealmProxyInterface) standaloneObject).realmSet$name(((SimpleRealmProxyInterface) realmObject).realmGet$name());
        ((SimpleRealmProxyInterface) standaloneObject).realmSet$age(((SimpleRealmProxyInterface) realmObject).realmGet$age());
        return standaloneObject;
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("Simple = [");
        stringBuilder.append("{name:");
        stringBuilder.append(realmGet$name() != null ? realmGet$name() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{age:");
        stringBuilder.append(realmGet$age());
        stringBuilder.append("}");
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        String realmName = ((RealmObject) this).realm.getPath();
        String tableName = ((RealmObject) this).row.getTable().getName();
        long rowIndex = ((RealmObject) this).row.getIndex();

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
        SimpleRealmProxy aSimple = (SimpleRealmProxy)o;

        String path = ((RealmObject) this).realm.getPath();
        String otherPath = ((RealmObject) aSimple).realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = ((RealmObject) this).row.getTable().getName();
        String otherTableName = ((RealmObject) aSimple).row.getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (((RealmObject) this).row.getIndex() != ((RealmObject) aSimple).row.getIndex()) return false;

        return true;
    }

}
