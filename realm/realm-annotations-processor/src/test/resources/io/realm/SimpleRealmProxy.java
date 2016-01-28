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
import some.test.Simple;

public class SimpleRealmProxy extends Simple
    implements RealmObjectProxy {

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
    public String realmGetter$name() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(columnInfo.nameIndex);
    }

    public void realmSetter$name(String value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(columnInfo.nameIndex);
            return;
        }
        row.setString(columnInfo.nameIndex, value);
    }

    @SuppressWarnings("cast")
    public int realmGetter$age() {
        realm.checkIfValid();
        return (int) row.getLong(columnInfo.ageIndex);
    }

    public void realmSetter$age(int value) {
        realm.checkIfValid();
        row.setLong(columnInfo.ageIndex, value);
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
        SimpleRealmProxy obj = (SimpleRealmProxy) realm.createObject(Simple.class);
        if (json.has("name")) {
            if (json.isNull("name")) {
                obj.realmSetter$name(null);
            } else {
                obj.realmSetter$name((String) json.getString("name"));
            }
        }
        if (json.has("age")) {
            if (json.isNull("age")) {
                throw new IllegalArgumentException("Trying to set non-nullable field age to null.");
            } else {
                obj.realmSetter$age((int) json.getInt("age"));
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    public static Simple createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        SimpleRealmProxy obj = (SimpleRealmProxy) realm.createObject(Simple.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$name(null);
                } else {
                    obj.realmSetter$name((String) reader.nextString());
                }
            } else if (name.equals("age")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field age to null.");
                } else {
                    obj.realmSetter$age((int) reader.nextInt());
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return obj;
    }

    public static Simple copyOrUpdate(Realm realm, Simple object, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        if (object.realm != null && object.realm.getPath().equals(realm.getPath())) {
            return object;
        }
        return copy(realm, object, update, cache);
    }

    public static Simple copy(Realm realm, Simple from, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        final boolean isStandalone = !(from instanceof SimpleRealmProxy);
        Class<? extends Simple> clazz;
        Field field = null;
        if (isStandalone) {
            clazz = from.getClass();
        } else {
            clazz = null;
        }
        SimpleRealmProxy to;

        to = (SimpleRealmProxy) realm.createObject(Simple.class);
        cache.put(from, (RealmObjectProxy) to);

        try {
            if (isStandalone) {
                field = clazz.getDeclaredField("name");
                field.setAccessible(true);
                to.realmSetter$name((java.lang.String) field.get(from));
            } else {
                to.realmSetter$name(((SimpleRealmProxy) from).realmGetter$name());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("age");
                field.setAccessible(true);
                to.realmSetter$age((int) field.get(from));
            } else {
                to.realmSetter$age(((SimpleRealmProxy) from).realmGetter$age());
            }
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        return to;
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
        Class<?> clazz = standaloneObject.getClass();
        Field field = null;
        try {
            field = clazz.getDeclaredField("name");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((SimpleRealmProxy) realmObject).realmGetter$name());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("age");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.setInt(standaloneObject, ((SimpleRealmProxy) realmObject).realmGetter$age());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        return standaloneObject;
    }

}