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
import some.test.NullTypes;

public class NullTypesRealmProxy extends NullTypes
    implements RealmObjectProxy, NullTypesRealmProxyInterface {

    static final class NullTypesColumnInfo extends ColumnInfo {

        public final long fieldStringNotNullIndex;
        public final long fieldStringNullIndex;
        public final long fieldBooleanNotNullIndex;
        public final long fieldBooleanNullIndex;
        public final long fieldBytesNotNullIndex;
        public final long fieldBytesNullIndex;
        public final long fieldByteNotNullIndex;
        public final long fieldByteNullIndex;
        public final long fieldShortNotNullIndex;
        public final long fieldShortNullIndex;
        public final long fieldIntegerNotNullIndex;
        public final long fieldIntegerNullIndex;
        public final long fieldLongNotNullIndex;
        public final long fieldLongNullIndex;
        public final long fieldFloatNotNullIndex;
        public final long fieldFloatNullIndex;
        public final long fieldDoubleNotNullIndex;
        public final long fieldDoubleNullIndex;
        public final long fieldDateNotNullIndex;
        public final long fieldDateNullIndex;
        public final long fieldObjectNullIndex;

        NullTypesColumnInfo(String path, Table table) {
            final Map<String, Long> indicesMap = new HashMap<String, Long>(21);
            this.fieldStringNotNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldStringNotNull");
            indicesMap.put("fieldStringNotNull", this.fieldStringNotNullIndex);

            this.fieldStringNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldStringNull");
            indicesMap.put("fieldStringNull", this.fieldStringNullIndex);

            this.fieldBooleanNotNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldBooleanNotNull");
            indicesMap.put("fieldBooleanNotNull", this.fieldBooleanNotNullIndex);

            this.fieldBooleanNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldBooleanNull");
            indicesMap.put("fieldBooleanNull", this.fieldBooleanNullIndex);

            this.fieldBytesNotNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldBytesNotNull");
            indicesMap.put("fieldBytesNotNull", this.fieldBytesNotNullIndex);

            this.fieldBytesNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldBytesNull");
            indicesMap.put("fieldBytesNull", this.fieldBytesNullIndex);

            this.fieldByteNotNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldByteNotNull");
            indicesMap.put("fieldByteNotNull", this.fieldByteNotNullIndex);

            this.fieldByteNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldByteNull");
            indicesMap.put("fieldByteNull", this.fieldByteNullIndex);

            this.fieldShortNotNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldShortNotNull");
            indicesMap.put("fieldShortNotNull", this.fieldShortNotNullIndex);

            this.fieldShortNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldShortNull");
            indicesMap.put("fieldShortNull", this.fieldShortNullIndex);

            this.fieldIntegerNotNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldIntegerNotNull");
            indicesMap.put("fieldIntegerNotNull", this.fieldIntegerNotNullIndex);

            this.fieldIntegerNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldIntegerNull");
            indicesMap.put("fieldIntegerNull", this.fieldIntegerNullIndex);

            this.fieldLongNotNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldLongNotNull");
            indicesMap.put("fieldLongNotNull", this.fieldLongNotNullIndex);

            this.fieldLongNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldLongNull");
            indicesMap.put("fieldLongNull", this.fieldLongNullIndex);

            this.fieldFloatNotNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldFloatNotNull");
            indicesMap.put("fieldFloatNotNull", this.fieldFloatNotNullIndex);

            this.fieldFloatNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldFloatNull");
            indicesMap.put("fieldFloatNull", this.fieldFloatNullIndex);

            this.fieldDoubleNotNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldDoubleNotNull");
            indicesMap.put("fieldDoubleNotNull", this.fieldDoubleNotNullIndex);

            this.fieldDoubleNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldDoubleNull");
            indicesMap.put("fieldDoubleNull", this.fieldDoubleNullIndex);

            this.fieldDateNotNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldDateNotNull");
            indicesMap.put("fieldDateNotNull", this.fieldDateNotNullIndex);

            this.fieldDateNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldDateNull");
            indicesMap.put("fieldDateNull", this.fieldDateNullIndex);

            this.fieldObjectNullIndex = getValidColumnIndex(path, table, "NullTypes", "fieldObjectNull");
            indicesMap.put("fieldObjectNull", this.fieldObjectNullIndex);

            setIndicesMap(indicesMap);
        }
    }

    private final NullTypesColumnInfo columnInfo;
    private static final List<String> FIELD_NAMES;
    static {
        List<String> fieldNames = new ArrayList<String>();
        fieldNames.add("fieldStringNotNull");
        fieldNames.add("fieldStringNull");
        fieldNames.add("fieldBooleanNotNull");
        fieldNames.add("fieldBooleanNull");
        fieldNames.add("fieldBytesNotNull");
        fieldNames.add("fieldBytesNull");
        fieldNames.add("fieldByteNotNull");
        fieldNames.add("fieldByteNull");
        fieldNames.add("fieldShortNotNull");
        fieldNames.add("fieldShortNull");
        fieldNames.add("fieldIntegerNotNull");
        fieldNames.add("fieldIntegerNull");
        fieldNames.add("fieldLongNotNull");
        fieldNames.add("fieldLongNull");
        fieldNames.add("fieldFloatNotNull");
        fieldNames.add("fieldFloatNull");
        fieldNames.add("fieldDoubleNotNull");
        fieldNames.add("fieldDoubleNull");
        fieldNames.add("fieldDateNotNull");
        fieldNames.add("fieldDateNull");
        fieldNames.add("fieldObjectNull");
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    NullTypesRealmProxy(ColumnInfo columnInfo) {
        this.columnInfo = (NullTypesColumnInfo) columnInfo;
    }

    @SuppressWarnings("cast")
    public String realmGet$fieldStringNotNull() {
        ((RealmObject) this).realm.checkIfValid();
        return (java.lang.String) ((RealmObject) this).row.getString(columnInfo.fieldStringNotNullIndex);
    }

    public void realmSet$fieldStringNotNull(String value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldStringNotNull to null.");
        }
        ((RealmObject) this).row.setString(columnInfo.fieldStringNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public String realmGet$fieldStringNull() {
        ((RealmObject) this).realm.checkIfValid();
        return (java.lang.String) ((RealmObject) this).row.getString(columnInfo.fieldStringNullIndex);
    }

    public void realmSet$fieldStringNull(String value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            ((RealmObject) this).row.setNull(columnInfo.fieldStringNullIndex);
            return;
        }
        ((RealmObject) this).row.setString(columnInfo.fieldStringNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Boolean realmGet$fieldBooleanNotNull() {
        ((RealmObject) this).realm.checkIfValid();
        return (boolean) ((RealmObject) this).row.getBoolean(columnInfo.fieldBooleanNotNullIndex);
    }

    public void realmSet$fieldBooleanNotNull(Boolean value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldBooleanNotNull to null.");
        }
        ((RealmObject) this).row.setBoolean(columnInfo.fieldBooleanNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Boolean realmGet$fieldBooleanNull() {
        ((RealmObject) this).realm.checkIfValid();
        if (((RealmObject) this).row.isNull(columnInfo.fieldBooleanNullIndex)) {
            return null;
        }
        return (boolean) ((RealmObject) this).row.getBoolean(columnInfo.fieldBooleanNullIndex);
    }

    public void realmSet$fieldBooleanNull(Boolean value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            ((RealmObject) this).row.setNull(columnInfo.fieldBooleanNullIndex);
            return;
        }
        ((RealmObject) this).row.setBoolean(columnInfo.fieldBooleanNullIndex, value);
    }

    @SuppressWarnings("cast")
    public byte[] realmGet$fieldBytesNotNull() {
        ((RealmObject) this).realm.checkIfValid();
        return (byte[]) ((RealmObject) this).row.getBinaryByteArray(columnInfo.fieldBytesNotNullIndex);
    }

    public void realmSet$fieldBytesNotNull(byte[] value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldBytesNotNull to null.");
        }
        ((RealmObject) this).row.setBinaryByteArray(columnInfo.fieldBytesNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public byte[] realmGet$fieldBytesNull() {
        ((RealmObject) this).realm.checkIfValid();
        return (byte[]) ((RealmObject) this).row.getBinaryByteArray(columnInfo.fieldBytesNullIndex);
    }

    public void realmSet$fieldBytesNull(byte[] value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            ((RealmObject) this).row.setNull(columnInfo.fieldBytesNullIndex);
            return;
        }
        ((RealmObject) this).row.setBinaryByteArray(columnInfo.fieldBytesNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Byte realmGet$fieldByteNotNull() {
        ((RealmObject) this).realm.checkIfValid();
        return (byte) ((RealmObject) this).row.getLong(columnInfo.fieldByteNotNullIndex);
    }

    public void realmSet$fieldByteNotNull(Byte value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldByteNotNull to null.");
        }
        ((RealmObject) this).row.setLong(columnInfo.fieldByteNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Byte realmGet$fieldByteNull() {
        ((RealmObject) this).realm.checkIfValid();
        if (((RealmObject) this).row.isNull(columnInfo.fieldByteNullIndex)) {
            return null;
        }
        return (byte) ((RealmObject) this).row.getLong(columnInfo.fieldByteNullIndex);
    }

    public void realmSet$fieldByteNull(Byte value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            ((RealmObject) this).row.setNull(columnInfo.fieldByteNullIndex);
            return;
        }
        ((RealmObject) this).row.setLong(columnInfo.fieldByteNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Short realmGet$fieldShortNotNull() {
        ((RealmObject) this).realm.checkIfValid();
        return (short) ((RealmObject) this).row.getLong(columnInfo.fieldShortNotNullIndex);
    }

    public void realmSet$fieldShortNotNull(Short value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldShortNotNull to null.");
        }
        ((RealmObject) this).row.setLong(columnInfo.fieldShortNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Short realmGet$fieldShortNull() {
        ((RealmObject) this).realm.checkIfValid();
        if (((RealmObject) this).row.isNull(columnInfo.fieldShortNullIndex)) {
            return null;
        }
        return (short) ((RealmObject) this).row.getLong(columnInfo.fieldShortNullIndex);
    }

    public void realmSet$fieldShortNull(Short value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            ((RealmObject) this).row.setNull(columnInfo.fieldShortNullIndex);
            return;
        }
        ((RealmObject) this).row.setLong(columnInfo.fieldShortNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Integer realmGet$fieldIntegerNotNull() {
        ((RealmObject) this).realm.checkIfValid();
        return (int) ((RealmObject) this).row.getLong(columnInfo.fieldIntegerNotNullIndex);
    }

    public void realmSet$fieldIntegerNotNull(Integer value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldIntegerNotNull to null.");
        }
        ((RealmObject) this).row.setLong(columnInfo.fieldIntegerNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Integer realmGet$fieldIntegerNull() {
        ((RealmObject) this).realm.checkIfValid();
        if (((RealmObject) this).row.isNull(columnInfo.fieldIntegerNullIndex)) {
            return null;
        }
        return (int) ((RealmObject) this).row.getLong(columnInfo.fieldIntegerNullIndex);
    }

    public void realmSet$fieldIntegerNull(Integer value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            ((RealmObject) this).row.setNull(columnInfo.fieldIntegerNullIndex);
            return;
        }
        ((RealmObject) this).row.setLong(columnInfo.fieldIntegerNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Long realmGet$fieldLongNotNull() {
        ((RealmObject) this).realm.checkIfValid();
        return (long) ((RealmObject) this).row.getLong(columnInfo.fieldLongNotNullIndex);
    }

    public void realmSet$fieldLongNotNull(Long value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldLongNotNull to null.");
        }
        ((RealmObject) this).row.setLong(columnInfo.fieldLongNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Long realmGet$fieldLongNull() {
        ((RealmObject) this).realm.checkIfValid();
        if (((RealmObject) this).row.isNull(columnInfo.fieldLongNullIndex)) {
            return null;
        }
        return (long) ((RealmObject) this).row.getLong(columnInfo.fieldLongNullIndex);
    }

    public void realmSet$fieldLongNull(Long value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            ((RealmObject) this).row.setNull(columnInfo.fieldLongNullIndex);
            return;
        }
        ((RealmObject) this).row.setLong(columnInfo.fieldLongNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Float realmGet$fieldFloatNotNull() {
        ((RealmObject) this).realm.checkIfValid();
        return (float) ((RealmObject) this).row.getFloat(columnInfo.fieldFloatNotNullIndex);
    }

    public void realmSet$fieldFloatNotNull(Float value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldFloatNotNull to null.");
        }
        ((RealmObject) this).row.setFloat(columnInfo.fieldFloatNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Float realmGet$fieldFloatNull() {
        ((RealmObject) this).realm.checkIfValid();
        if (((RealmObject) this).row.isNull(columnInfo.fieldFloatNullIndex)) {
            return null;
        }
        return (float) ((RealmObject) this).row.getFloat(columnInfo.fieldFloatNullIndex);
    }

    public void realmSet$fieldFloatNull(Float value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            ((RealmObject) this).row.setNull(columnInfo.fieldFloatNullIndex);
            return;
        }
        ((RealmObject) this).row.setFloat(columnInfo.fieldFloatNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Double realmGet$fieldDoubleNotNull() {
        ((RealmObject) this).realm.checkIfValid();
        return (double) ((RealmObject) this).row.getDouble(columnInfo.fieldDoubleNotNullIndex);
    }

    public void realmSet$fieldDoubleNotNull(Double value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldDoubleNotNull to null.");
        }
        ((RealmObject) this).row.setDouble(columnInfo.fieldDoubleNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Double realmGet$fieldDoubleNull() {
        ((RealmObject) this).realm.checkIfValid();
        if (((RealmObject) this).row.isNull(columnInfo.fieldDoubleNullIndex)) {
            return null;
        }
        return (double) ((RealmObject) this).row.getDouble(columnInfo.fieldDoubleNullIndex);
    }

    public void realmSet$fieldDoubleNull(Double value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            ((RealmObject) this).row.setNull(columnInfo.fieldDoubleNullIndex);
            return;
        }
        ((RealmObject) this).row.setDouble(columnInfo.fieldDoubleNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Date realmGet$fieldDateNotNull() {
        ((RealmObject) this).realm.checkIfValid();
        return (java.util.Date) ((RealmObject) this).row.getDate(columnInfo.fieldDateNotNullIndex);
    }

    public void realmSet$fieldDateNotNull(Date value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldDateNotNull to null.");
        }
        ((RealmObject) this).row.setDate(columnInfo.fieldDateNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Date realmGet$fieldDateNull() {
        ((RealmObject) this).realm.checkIfValid();
        if (((RealmObject) this).row.isNull(columnInfo.fieldDateNullIndex)) {
            return null;
        }
        return (java.util.Date) ((RealmObject) this).row.getDate(columnInfo.fieldDateNullIndex);
    }

    public void realmSet$fieldDateNull(Date value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            ((RealmObject) this).row.setNull(columnInfo.fieldDateNullIndex);
            return;
        }
        ((RealmObject) this).row.setDate(columnInfo.fieldDateNullIndex, value);
    }

    public NullTypes realmGet$fieldObjectNull() {
        ((RealmObject) this).realm.checkIfValid();
        if (((RealmObject) this).row.isNullLink(columnInfo.fieldObjectNullIndex)) {
            return null;
        }
        return ((RealmObject) this).realm.get(some.test.NullTypes.class, ((RealmObject) this).row.getLink(columnInfo.fieldObjectNullIndex));
    }

    public void realmSet$fieldObjectNull(NullTypes value) {
        ((RealmObject) this).realm.checkIfValid();
        if (value == null) {
            ((RealmObject) this).row.nullifyLink(columnInfo.fieldObjectNullIndex);
            return;
        }
        if (!value.isValid()) {
            throw new IllegalArgumentException("'value' is not a valid managed object.");
        }
        if (value.realm != this.realm) {
            throw new IllegalArgumentException("'value' belongs to a different Realm.");
        }
        ((RealmObject) this).row.setLink(columnInfo.fieldObjectNullIndex, ((RealmObject) value).row.getIndex());
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if (!transaction.hasTable("class_NullTypes")) {
            Table table = transaction.getTable("class_NullTypes");
            table.addColumn(RealmFieldType.STRING, "fieldStringNotNull", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.STRING, "fieldStringNull", Table.NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "fieldBooleanNotNull", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BOOLEAN, "fieldBooleanNull", Table.NULLABLE);
            table.addColumn(RealmFieldType.BINARY, "fieldBytesNotNull", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.BINARY, "fieldBytesNull", Table.NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "fieldByteNotNull", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "fieldByteNull", Table.NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "fieldShortNotNull", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "fieldShortNull", Table.NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "fieldIntegerNotNull", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "fieldIntegerNull", Table.NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "fieldLongNotNull", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.INTEGER, "fieldLongNull", Table.NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "fieldFloatNotNull", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.FLOAT, "fieldFloatNull", Table.NULLABLE);
            table.addColumn(RealmFieldType.DOUBLE, "fieldDoubleNotNull", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.DOUBLE, "fieldDoubleNull", Table.NULLABLE);
            table.addColumn(RealmFieldType.DATE, "fieldDateNotNull", Table.NOT_NULLABLE);
            table.addColumn(RealmFieldType.DATE, "fieldDateNull", Table.NULLABLE);
            if (!transaction.hasTable("class_NullTypes")) {
                NullTypesRealmProxy.initTable(transaction);
            }
            table.addColumnLink(RealmFieldType.OBJECT, "fieldObjectNull", transaction.getTable("class_NullTypes"));
            table.setPrimaryKey("");
            return table;
        }
        return transaction.getTable("class_NullTypes");
    }

    public static NullTypesColumnInfo validateTable(ImplicitTransaction transaction) {
        if (transaction.hasTable("class_NullTypes")) {
            Table table = transaction.getTable("class_NullTypes");
            if (table.getColumnCount() != 21) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field count does not match - expected 21 but was " + table.getColumnCount());
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < 21; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final NullTypesColumnInfo columnInfo = new NullTypesColumnInfo(transaction.getPath(), table);

            if (!columnTypes.containsKey("fieldStringNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldStringNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldStringNotNull") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'fieldStringNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldStringNotNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldStringNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldStringNotNull' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("fieldStringNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldStringNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldStringNull") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'fieldStringNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldStringNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldStringNull' is required. Either set @Required to field 'fieldStringNull' or migrate using io.realm.internal.Table.convertColumnToNullable().");
            }
            if (!columnTypes.containsKey("fieldBooleanNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldBooleanNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldBooleanNotNull") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Boolean' for field 'fieldBooleanNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldBooleanNotNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldBooleanNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldBooleanNotNull' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("fieldBooleanNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldBooleanNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldBooleanNull") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Boolean' for field 'fieldBooleanNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldBooleanNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(),"Field 'fieldBooleanNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldBooleanNull' or migrate using io.realm.internal.Table.convertColumnToNullable().");
            }
            if (!columnTypes.containsKey("fieldBytesNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldBytesNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldBytesNotNull") != RealmFieldType.BINARY) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'byte[]' for field 'fieldBytesNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldBytesNotNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldBytesNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldBytesNotNull' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("fieldBytesNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldBytesNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldBytesNull") != RealmFieldType.BINARY) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'byte[]' for field 'fieldBytesNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldBytesNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldBytesNull' is required. Either set @Required to field 'fieldBytesNull' or migrate using io.realm.internal.Table.convertColumnToNullable().");
            }
            if (!columnTypes.containsKey("fieldByteNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldByteNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldByteNotNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Byte' for field 'fieldByteNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldByteNotNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldByteNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldByteNotNull' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("fieldByteNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldByteNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldByteNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Byte' for field 'fieldByteNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldByteNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(),"Field 'fieldByteNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldByteNull' or migrate using io.realm.internal.Table.convertColumnToNullable().");
            }
            if (!columnTypes.containsKey("fieldShortNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldShortNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldShortNotNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Short' for field 'fieldShortNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldShortNotNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldShortNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldShortNotNull' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("fieldShortNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldShortNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldShortNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Short' for field 'fieldShortNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldShortNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(),"Field 'fieldShortNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldShortNull' or migrate using io.realm.internal.Table.convertColumnToNullable().");
            }
            if (!columnTypes.containsKey("fieldIntegerNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldIntegerNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldIntegerNotNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Integer' for field 'fieldIntegerNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldIntegerNotNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldIntegerNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldIntegerNotNull' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("fieldIntegerNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldIntegerNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldIntegerNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Integer' for field 'fieldIntegerNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldIntegerNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(),"Field 'fieldIntegerNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldIntegerNull' or migrate using io.realm.internal.Table.convertColumnToNullable().");
            }
            if (!columnTypes.containsKey("fieldLongNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldLongNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldLongNotNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Long' for field 'fieldLongNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldLongNotNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldLongNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldLongNotNull' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("fieldLongNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldLongNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldLongNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Long' for field 'fieldLongNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldLongNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(),"Field 'fieldLongNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldLongNull' or migrate using io.realm.internal.Table.convertColumnToNullable().");
            }
            if (!columnTypes.containsKey("fieldFloatNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldFloatNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldFloatNotNull") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Float' for field 'fieldFloatNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldFloatNotNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldFloatNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldFloatNotNull' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("fieldFloatNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldFloatNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldFloatNull") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Float' for field 'fieldFloatNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldFloatNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(),"Field 'fieldFloatNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldFloatNull' or migrate using io.realm.internal.Table.convertColumnToNullable().");
            }
            if (!columnTypes.containsKey("fieldDoubleNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldDoubleNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldDoubleNotNull") != RealmFieldType.DOUBLE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Double' for field 'fieldDoubleNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldDoubleNotNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldDoubleNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldDoubleNotNull' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("fieldDoubleNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldDoubleNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldDoubleNull") != RealmFieldType.DOUBLE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Double' for field 'fieldDoubleNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldDoubleNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(),"Field 'fieldDoubleNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldDoubleNull' or migrate using io.realm.internal.Table.convertColumnToNullable().");
            }
            if (!columnTypes.containsKey("fieldDateNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldDateNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldDateNotNull") != RealmFieldType.DATE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Date' for field 'fieldDateNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldDateNotNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldDateNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldDateNotNull' or migrate using io.realm.internal.Table.convertColumnToNotNullable().");
            }
            if (!columnTypes.containsKey("fieldDateNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldDateNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldDateNull") != RealmFieldType.DATE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Date' for field 'fieldDateNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldDateNullIndex)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field 'fieldDateNull' is required. Either set @Required to field 'fieldDateNull' or migrate using io.realm.internal.Table.convertColumnToNullable().");
            }
            if (!columnTypes.containsKey("fieldObjectNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldObjectNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldObjectNull") != RealmFieldType.OBJECT) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'NullTypes' for field 'fieldObjectNull'");
            }
            if (!transaction.hasTable("class_NullTypes")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing class 'class_NullTypes' for field 'fieldObjectNull'");
            }
            Table table_20 = transaction.getTable("class_NullTypes");
            if (!table.getLinkTarget(columnInfo.fieldObjectNullIndex).hasSameSchema(table_20)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid RealmObject for field 'fieldObjectNull': '" + table.getLinkTarget(columnInfo.fieldObjectNullIndex).getName() + "' expected - was '" + table_20.getName() + "'");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(transaction.getPath(), "The NullTypes class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() {
        return "class_NullTypes";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static NullTypes createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        NullTypes obj = realm.createObject(NullTypes.class);
        if (json.has("fieldStringNotNull")) {
            if (json.isNull("fieldStringNotNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldStringNotNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldStringNotNull((String) json.getString("fieldStringNotNull"));
            }
        }
        if (json.has("fieldStringNull")) {
            if (json.isNull("fieldStringNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldStringNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldStringNull((String) json.getString("fieldStringNull"));
            }
        }
        if (json.has("fieldBooleanNotNull")) {
            if (json.isNull("fieldBooleanNotNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldBooleanNotNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldBooleanNotNull((boolean) json.getBoolean("fieldBooleanNotNull"));
            }
        }
        if (json.has("fieldBooleanNull")) {
            if (json.isNull("fieldBooleanNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldBooleanNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldBooleanNull((boolean) json.getBoolean("fieldBooleanNull"));
            }
        }
        if (json.has("fieldBytesNotNull")) {
            if (json.isNull("fieldBytesNotNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldBytesNotNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldBytesNotNull(JsonUtils.stringToBytes(json.getString("fieldBytesNotNull")));
            }
        }
        if (json.has("fieldBytesNull")) {
            if (json.isNull("fieldBytesNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldBytesNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldBytesNull(JsonUtils.stringToBytes(json.getString("fieldBytesNull")));
            }
        }
        if (json.has("fieldByteNotNull")) {
            if (json.isNull("fieldByteNotNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldByteNotNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldByteNotNull((byte) json.getInt("fieldByteNotNull"));
            }
        }
        if (json.has("fieldByteNull")) {
            if (json.isNull("fieldByteNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldByteNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldByteNull((byte) json.getInt("fieldByteNull"));
            }
        }
        if (json.has("fieldShortNotNull")) {
            if (json.isNull("fieldShortNotNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldShortNotNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldShortNotNull((short) json.getInt("fieldShortNotNull"));
            }
        }
        if (json.has("fieldShortNull")) {
            if (json.isNull("fieldShortNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldShortNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldShortNull((short) json.getInt("fieldShortNull"));
            }
        }
        if (json.has("fieldIntegerNotNull")) {
            if (json.isNull("fieldIntegerNotNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldIntegerNotNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldIntegerNotNull((int) json.getInt("fieldIntegerNotNull"));
            }
        }
        if (json.has("fieldIntegerNull")) {
            if (json.isNull("fieldIntegerNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldIntegerNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldIntegerNull((int) json.getInt("fieldIntegerNull"));
            }
        }
        if (json.has("fieldLongNotNull")) {
            if (json.isNull("fieldLongNotNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldLongNotNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldLongNotNull((long) json.getLong("fieldLongNotNull"));
            }
        }
        if (json.has("fieldLongNull")) {
            if (json.isNull("fieldLongNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldLongNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldLongNull((long) json.getLong("fieldLongNull"));
            }
        }
        if (json.has("fieldFloatNotNull")) {
            if (json.isNull("fieldFloatNotNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldFloatNotNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldFloatNotNull((float) json.getDouble("fieldFloatNotNull"));
            }
        }
        if (json.has("fieldFloatNull")) {
            if (json.isNull("fieldFloatNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldFloatNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldFloatNull((float) json.getDouble("fieldFloatNull"));
            }
        }
        if (json.has("fieldDoubleNotNull")) {
            if (json.isNull("fieldDoubleNotNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldDoubleNotNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldDoubleNotNull((double) json.getDouble("fieldDoubleNotNull"));
            }
        }
        if (json.has("fieldDoubleNull")) {
            if (json.isNull("fieldDoubleNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldDoubleNull(null);
            } else {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldDoubleNull((double) json.getDouble("fieldDoubleNull"));
            }
        }
        if (json.has("fieldDateNotNull")) {
            if (json.isNull("fieldDateNotNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldDateNotNull(null);
            } else {
                Object timestamp = json.get("fieldDateNotNull");
                if (timestamp instanceof String) {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldDateNotNull(JsonUtils.stringToDate((String) timestamp));
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldDateNotNull(new Date(json.getLong("fieldDateNotNull")));
                }
            }
        }
        if (json.has("fieldDateNull")) {
            if (json.isNull("fieldDateNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldDateNull(null);
            } else {
                Object timestamp = json.get("fieldDateNull");
                if (timestamp instanceof String) {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldDateNull(JsonUtils.stringToDate((String) timestamp));
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldDateNull(new Date(json.getLong("fieldDateNull")));
                }
            }
        }
        if (json.has("fieldObjectNull")) {
            if (json.isNull("fieldObjectNull")) {
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldObjectNull(null);
            } else {
                some.test.NullTypes fieldObjectNullObj = NullTypesRealmProxy.createOrUpdateUsingJsonObject(realm, json.getJSONObject("fieldObjectNull"), update);
                ((NullTypesRealmProxyInterface) obj).realmSet$fieldObjectNull(fieldObjectNullObj);
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    public static NullTypes createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        NullTypes obj = realm.createObject(NullTypes.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("fieldStringNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldStringNotNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldStringNotNull((String) reader.nextString());
                }
            } else if (name.equals("fieldStringNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldStringNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldStringNull((String) reader.nextString());
                }
            } else if (name.equals("fieldBooleanNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldBooleanNotNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldBooleanNotNull((boolean) reader.nextBoolean());
                }
            } else if (name.equals("fieldBooleanNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldBooleanNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldBooleanNull((boolean) reader.nextBoolean());
                }
            } else if (name.equals("fieldBytesNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldBytesNotNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldBytesNotNull(JsonUtils.stringToBytes(reader.nextString()));
                }
            } else if (name.equals("fieldBytesNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldBytesNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldBytesNull(JsonUtils.stringToBytes(reader.nextString()));
                }
            } else if (name.equals("fieldByteNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldByteNotNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldByteNotNull((byte) reader.nextInt());
                }
            } else if (name.equals("fieldByteNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldByteNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldByteNull((byte) reader.nextInt());
                }
            } else if (name.equals("fieldShortNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldShortNotNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldShortNotNull((short) reader.nextInt());
                }
            } else if (name.equals("fieldShortNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldShortNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldShortNull((short) reader.nextInt());
                }
            } else if (name.equals("fieldIntegerNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldIntegerNotNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldIntegerNotNull((int) reader.nextInt());
                }
            } else if (name.equals("fieldIntegerNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldIntegerNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldIntegerNull((int) reader.nextInt());
                }
            } else if (name.equals("fieldLongNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldLongNotNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldLongNotNull((long) reader.nextLong());
                }
            } else if (name.equals("fieldLongNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldLongNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldLongNull((long) reader.nextLong());
                }
            } else if (name.equals("fieldFloatNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldFloatNotNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldFloatNotNull((float) reader.nextDouble());
                }
            } else if (name.equals("fieldFloatNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldFloatNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldFloatNull((float) reader.nextDouble());
                }
            } else if (name.equals("fieldDoubleNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldDoubleNotNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldDoubleNotNull((double) reader.nextDouble());
                }
            } else if (name.equals("fieldDoubleNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldDoubleNull(null);
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldDoubleNull((double) reader.nextDouble());
                }
            } else if (name.equals("fieldDateNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldDateNotNull(null);
                } else if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        ((NullTypesRealmProxyInterface) obj).realmSet$fieldDateNotNull(new Date(timestamp));
                    }
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldDateNotNull(JsonUtils.stringToDate(reader.nextString()));
                }
            } else if (name.equals("fieldDateNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldDateNull(null);
                } else if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        ((NullTypesRealmProxyInterface) obj).realmSet$fieldDateNull(new Date(timestamp));
                    }
                } else {
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldDateNull(JsonUtils.stringToDate(reader.nextString()));
                }
            } else if (name.equals("fieldObjectNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldObjectNull(null);
                } else {
                    some.test.NullTypes fieldObjectNullObj = NullTypesRealmProxy.createUsingJsonStream(realm, reader);
                    ((NullTypesRealmProxyInterface) obj).realmSet$fieldObjectNull(fieldObjectNullObj);
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return obj;
    }

    public static NullTypes copyOrUpdate(Realm realm, NullTypes object, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        if (((RealmObject) object).realm != null && ((RealmObject) object).realm.getPath().equals(realm.getPath())) {
            return object;
        }
        return copy(realm, object, update, cache);
    }

    public static NullTypes copy(Realm realm, NullTypes newObject, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        NullTypes realmObject = realm.createObject(NullTypes.class);
        cache.put(newObject, (RealmObjectProxy) realmObject);
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldStringNotNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldStringNotNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldStringNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldStringNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldBooleanNotNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldBooleanNotNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldBooleanNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldBooleanNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldBytesNotNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldBytesNotNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldBytesNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldBytesNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldByteNotNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldByteNotNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldByteNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldByteNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldShortNotNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldShortNotNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldShortNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldShortNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldIntegerNotNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldIntegerNotNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldIntegerNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldIntegerNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldLongNotNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldLongNotNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldLongNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldLongNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldFloatNotNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldFloatNotNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldFloatNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldFloatNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldDoubleNotNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldDoubleNotNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldDoubleNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldDoubleNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldDateNotNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldDateNotNull());
        ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldDateNull(((NullTypesRealmProxyInterface) newObject).realmGet$fieldDateNull());

        some.test.NullTypes fieldObjectNullObj = ((NullTypesRealmProxyInterface) newObject).realmGet$fieldObjectNull();
        if (fieldObjectNullObj != null) {
            some.test.NullTypes cachefieldObjectNull = (some.test.NullTypes) cache.get(fieldObjectNullObj);
            if (cachefieldObjectNull != null) {
                ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldObjectNull(cachefieldObjectNull);
            } else {
                ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldObjectNull(NullTypesRealmProxy.copyOrUpdate(realm, fieldObjectNullObj, update, cache));
            }
        } else {
            ((NullTypesRealmProxyInterface) realmObject).realmSet$fieldObjectNull(null);
        }
        return realmObject;
    }

    public static NullTypes createDetachedCopy(NullTypes realmObject, int currentDepth, int maxDepth, Map<RealmObject, CacheData<RealmObject>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<NullTypes> cachedObject = (CacheData) cache.get(realmObject);
        NullTypes standaloneObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return cachedObject.object;
            } else {
                standaloneObject = cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            standaloneObject = new NullTypes();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmObject>(currentDepth, standaloneObject));
        }
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldStringNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldStringNotNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldStringNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldStringNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldBooleanNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldBooleanNotNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldBooleanNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldBooleanNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldBytesNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldBytesNotNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldBytesNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldBytesNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldByteNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldByteNotNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldByteNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldByteNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldShortNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldShortNotNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldShortNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldShortNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldIntegerNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldIntegerNotNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldIntegerNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldIntegerNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldLongNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldLongNotNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldLongNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldLongNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldFloatNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldFloatNotNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldFloatNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldFloatNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldDoubleNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldDoubleNotNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldDoubleNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldDoubleNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldDateNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldDateNotNull());
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldDateNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldDateNull());

        // Deep copy of fieldObjectNull
        ((NullTypesRealmProxyInterface) standaloneObject).realmSet$fieldObjectNull(NullTypesRealmProxy.createDetachedCopy(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldObjectNull(), currentDepth + 1, maxDepth, cache));
        return standaloneObject;
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("NullTypes = [");
        stringBuilder.append("{fieldStringNotNull:");
        stringBuilder.append(realmGet$fieldStringNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldStringNull:");
        stringBuilder.append(realmGet$fieldStringNull() != null ? realmGet$fieldStringNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBooleanNotNull:");
        stringBuilder.append(realmGet$fieldBooleanNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBooleanNull:");
        stringBuilder.append(realmGet$fieldBooleanNull() != null ? realmGet$fieldBooleanNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBytesNotNull:");
        stringBuilder.append(realmGet$fieldBytesNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBytesNull:");
        stringBuilder.append(realmGet$fieldBytesNull() != null ? realmGet$fieldBytesNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldByteNotNull:");
        stringBuilder.append(realmGet$fieldByteNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldByteNull:");
        stringBuilder.append(realmGet$fieldByteNull() != null ? realmGet$fieldByteNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldShortNotNull:");
        stringBuilder.append(realmGet$fieldShortNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldShortNull:");
        stringBuilder.append(realmGet$fieldShortNull() != null ? realmGet$fieldShortNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldIntegerNotNull:");
        stringBuilder.append(realmGet$fieldIntegerNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldIntegerNull:");
        stringBuilder.append(realmGet$fieldIntegerNull() != null ? realmGet$fieldIntegerNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldLongNotNull:");
        stringBuilder.append(realmGet$fieldLongNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldLongNull:");
        stringBuilder.append(realmGet$fieldLongNull() != null ? realmGet$fieldLongNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldFloatNotNull:");
        stringBuilder.append(realmGet$fieldFloatNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldFloatNull:");
        stringBuilder.append(realmGet$fieldFloatNull() != null ? realmGet$fieldFloatNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDoubleNotNull:");
        stringBuilder.append(realmGet$fieldDoubleNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDoubleNull:");
        stringBuilder.append(realmGet$fieldDoubleNull() != null ? realmGet$fieldDoubleNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDateNotNull:");
        stringBuilder.append(realmGet$fieldDateNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDateNull:");
        stringBuilder.append(realmGet$fieldDateNull() != null ? realmGet$fieldDateNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldObjectNull:");
        stringBuilder.append(realmGet$fieldObjectNull() != null ? "NullTypes" : "null");
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
        NullTypesRealmProxy aNullTypes = (NullTypesRealmProxy)o;

        String path = ((RealmObject) this).realm.getPath();
        String otherPath = ((RealmObject) aNullTypes).realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = ((RealmObject) this).row.getTable().getName();
        String otherTableName = ((RealmObject) aNullTypes).row.getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (((RealmObject) this).row.getIndex() != ((RealmObject) aNullTypes).row.getIndex()) return false;

        return true;
    }

}
