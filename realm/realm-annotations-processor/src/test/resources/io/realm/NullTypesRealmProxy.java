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
import some.test.NullTypes;

public class NullTypesRealmProxy extends NullTypes
    implements RealmObjectProxy {

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
    public String realmGetter$fieldStringNotNull() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(columnInfo.fieldStringNotNullIndex);
    }

    public void realmSetter$fieldStringNotNull(String value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldStringNotNull to null.");
        }
        row.setString(columnInfo.fieldStringNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public String realmGetter$fieldStringNull() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(columnInfo.fieldStringNullIndex);
    }

    public void realmSetter$fieldStringNull(String value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(columnInfo.fieldStringNullIndex);
            return;
        }
        row.setString(columnInfo.fieldStringNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Boolean realmGetter$fieldBooleanNotNull() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(columnInfo.fieldBooleanNotNullIndex);
    }

    public void realmSetter$fieldBooleanNotNull(Boolean value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldBooleanNotNull to null.");
        }
        row.setBoolean(columnInfo.fieldBooleanNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Boolean realmGetter$fieldBooleanNull() {
        realm.checkIfValid();
        if (row.isNull(columnInfo.fieldBooleanNullIndex)) {
            return null;
        }
        return (boolean) row.getBoolean(columnInfo.fieldBooleanNullIndex);
    }

    public void realmSetter$fieldBooleanNull(Boolean value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(columnInfo.fieldBooleanNullIndex);
            return;
        }
        row.setBoolean(columnInfo.fieldBooleanNullIndex, value);
    }

    @SuppressWarnings("cast")
    public byte[] realmGetter$fieldBytesNotNull() {
        realm.checkIfValid();
        return (byte[]) row.getBinaryByteArray(columnInfo.fieldBytesNotNullIndex);
    }

    public void realmSetter$fieldBytesNotNull(byte[] value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldBytesNotNull to null.");
        }
        row.setBinaryByteArray(columnInfo.fieldBytesNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public byte[] realmGetter$fieldBytesNull() {
        realm.checkIfValid();
        return (byte[]) row.getBinaryByteArray(columnInfo.fieldBytesNullIndex);
    }

    public void realmSetter$fieldBytesNull(byte[] value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(columnInfo.fieldBytesNullIndex);
            return;
        }
        row.setBinaryByteArray(columnInfo.fieldBytesNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Byte realmGetter$fieldByteNotNull() {
        realm.checkIfValid();
        return (byte) row.getLong(columnInfo.fieldByteNotNullIndex);
    }

    public void realmSetter$fieldByteNotNull(Byte value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldByteNotNull to null.");
        }
        row.setLong(columnInfo.fieldByteNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Byte realmGetter$fieldByteNull() {
        realm.checkIfValid();
        if (row.isNull(columnInfo.fieldByteNullIndex)) {
            return null;
        }
        return (byte) row.getLong(columnInfo.fieldByteNullIndex);
    }

    public void realmSetter$fieldByteNull(Byte value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(columnInfo.fieldByteNullIndex);
            return;
        }
        row.setLong(columnInfo.fieldByteNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Short realmGetter$fieldShortNotNull() {
        realm.checkIfValid();
        return (short) row.getLong(columnInfo.fieldShortNotNullIndex);
    }

    public void realmSetter$fieldShortNotNull(Short value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldShortNotNull to null.");
        }
        row.setLong(columnInfo.fieldShortNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Short realmGetter$fieldShortNull() {
        realm.checkIfValid();
        if (row.isNull(columnInfo.fieldShortNullIndex)) {
            return null;
        }
        return (short) row.getLong(columnInfo.fieldShortNullIndex);
    }

    public void realmSetter$fieldShortNull(Short value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(columnInfo.fieldShortNullIndex);
            return;
        }
        row.setLong(columnInfo.fieldShortNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Integer realmGetter$fieldIntegerNotNull() {
        realm.checkIfValid();
        return (int) row.getLong(columnInfo.fieldIntegerNotNullIndex);
    }

    public void realmSetter$fieldIntegerNotNull(Integer value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldIntegerNotNull to null.");
        }
        row.setLong(columnInfo.fieldIntegerNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Integer realmGetter$fieldIntegerNull() {
        realm.checkIfValid();
        if (row.isNull(columnInfo.fieldIntegerNullIndex)) {
            return null;
        }
        return (int) row.getLong(columnInfo.fieldIntegerNullIndex);
    }

    public void realmSetter$fieldIntegerNull(Integer value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(columnInfo.fieldIntegerNullIndex);
            return;
        }
        row.setLong(columnInfo.fieldIntegerNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Long realmGetter$fieldLongNotNull() {
        realm.checkIfValid();
        return (long) row.getLong(columnInfo.fieldLongNotNullIndex);
    }

    public void realmSetter$fieldLongNotNull(Long value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldLongNotNull to null.");
        }
        row.setLong(columnInfo.fieldLongNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Long realmGetter$fieldLongNull() {
        realm.checkIfValid();
        if (row.isNull(columnInfo.fieldLongNullIndex)) {
            return null;
        }
        return (long) row.getLong(columnInfo.fieldLongNullIndex);
    }

    public void realmSetter$fieldLongNull(Long value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(columnInfo.fieldLongNullIndex);
            return;
        }
        row.setLong(columnInfo.fieldLongNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Float realmGetter$fieldFloatNotNull() {
        realm.checkIfValid();
        return (float) row.getFloat(columnInfo.fieldFloatNotNullIndex);
    }

    public void realmSetter$fieldFloatNotNull(Float value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldFloatNotNull to null.");
        }
        row.setFloat(columnInfo.fieldFloatNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Float realmGetter$fieldFloatNull() {
        realm.checkIfValid();
        if (row.isNull(columnInfo.fieldFloatNullIndex)) {
            return null;
        }
        return (float) row.getFloat(columnInfo.fieldFloatNullIndex);
    }

    public void realmSetter$fieldFloatNull(Float value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(columnInfo.fieldFloatNullIndex);
            return;
        }
        row.setFloat(columnInfo.fieldFloatNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Double realmGetter$fieldDoubleNotNull() {
        realm.checkIfValid();
        return (double) row.getDouble(columnInfo.fieldDoubleNotNullIndex);
    }

    public void realmSetter$fieldDoubleNotNull(Double value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldDoubleNotNull to null.");
        }
        row.setDouble(columnInfo.fieldDoubleNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Double realmGetter$fieldDoubleNull() {
        realm.checkIfValid();
        if (row.isNull(columnInfo.fieldDoubleNullIndex)) {
            return null;
        }
        return (double) row.getDouble(columnInfo.fieldDoubleNullIndex);
    }

    public void realmSetter$fieldDoubleNull(Double value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(columnInfo.fieldDoubleNullIndex);
            return;
        }
        row.setDouble(columnInfo.fieldDoubleNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Date realmGetter$fieldDateNotNull() {
        realm.checkIfValid();
        return (java.util.Date) row.getDate(columnInfo.fieldDateNotNullIndex);
    }

    public void realmSetter$fieldDateNotNull(Date value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field fieldDateNotNull to null.");
        }
        row.setDate(columnInfo.fieldDateNotNullIndex, value);
    }

    @SuppressWarnings("cast")
    public Date realmGetter$fieldDateNull() {
        realm.checkIfValid();
        if (row.isNull(columnInfo.fieldDateNullIndex)) {
            return null;
        }
        return (java.util.Date) row.getDate(columnInfo.fieldDateNullIndex);
    }

    public void realmSetter$fieldDateNull(Date value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(columnInfo.fieldDateNullIndex);
            return;
        }
        row.setDate(columnInfo.fieldDateNullIndex, value);
    }

    public NullTypes realmGetter$fieldObjectNull() {
        realm.checkIfValid();
        if (row.isNullLink(columnInfo.fieldObjectNullIndex)) {
            return null;
        }
        return realm.get(some.test.NullTypes.class, row.getLink(columnInfo.fieldObjectNullIndex));
    }

    public void realmSetter$fieldObjectNull(NullTypes value) {
        realm.checkIfValid();
        if (value == null) {
            row.nullifyLink(columnInfo.fieldObjectNullIndex);
            return;
        }
        if (!value.isValid()) {
            throw new IllegalArgumentException("'value' is not a valid managed object.");
        }
        if (value.realm != this.realm) {
            throw new IllegalArgumentException("'value' belongs to a different Realm.");
        }
        row.setLink(columnInfo.fieldObjectNullIndex, value.row.getIndex());
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
        NullTypesRealmProxy obj = (NullTypesRealmProxy) realm.createObject(NullTypes.class);
        if (json.has("fieldStringNotNull")) {
            if (json.isNull("fieldStringNotNull")) {
                obj.realmSetter$fieldStringNotNull(null);
            } else {
                obj.realmSetter$fieldStringNotNull((String) json.getString("fieldStringNotNull"));
            }
        }
        if (json.has("fieldStringNull")) {
            if (json.isNull("fieldStringNull")) {
                obj.realmSetter$fieldStringNull(null);
            } else {
                obj.realmSetter$fieldStringNull((String) json.getString("fieldStringNull"));
            }
        }
        if (json.has("fieldBooleanNotNull")) {
            if (json.isNull("fieldBooleanNotNull")) {
                obj.realmSetter$fieldBooleanNotNull(null);
            } else {
                obj.realmSetter$fieldBooleanNotNull((boolean) json.getBoolean("fieldBooleanNotNull"));
            }
        }
        if (json.has("fieldBooleanNull")) {
            if (json.isNull("fieldBooleanNull")) {
                obj.realmSetter$fieldBooleanNull(null);
            } else {
                obj.realmSetter$fieldBooleanNull((boolean) json.getBoolean("fieldBooleanNull"));
            }
        }
        if (json.has("fieldBytesNotNull")) {
            if (json.isNull("fieldBytesNotNull")) {
                obj.realmSetter$fieldBytesNotNull(null);
            } else {
                obj.realmSetter$fieldBytesNotNull(JsonUtils.stringToBytes(json.getString("fieldBytesNotNull")));
            }
        }
        if (json.has("fieldBytesNull")) {
            if (json.isNull("fieldBytesNull")) {
                obj.realmSetter$fieldBytesNull(null);
            } else {
                obj.realmSetter$fieldBytesNull(JsonUtils.stringToBytes(json.getString("fieldBytesNull")));
            }
        }
        if (json.has("fieldByteNotNull")) {
            if (json.isNull("fieldByteNotNull")) {
                obj.realmSetter$fieldByteNotNull(null);
            } else {
                obj.realmSetter$fieldByteNotNull((byte) json.getInt("fieldByteNotNull"));
            }
        }
        if (json.has("fieldByteNull")) {
            if (json.isNull("fieldByteNull")) {
                obj.realmSetter$fieldByteNull(null);
            } else {
                obj.realmSetter$fieldByteNull((byte) json.getInt("fieldByteNull"));
            }
        }
        if (json.has("fieldShortNotNull")) {
            if (json.isNull("fieldShortNotNull")) {
                obj.realmSetter$fieldShortNotNull(null);
            } else {
                obj.realmSetter$fieldShortNotNull((short) json.getInt("fieldShortNotNull"));
            }
        }
        if (json.has("fieldShortNull")) {
            if (json.isNull("fieldShortNull")) {
                obj.realmSetter$fieldShortNull(null);
            } else {
                obj.realmSetter$fieldShortNull((short) json.getInt("fieldShortNull"));
            }
        }
        if (json.has("fieldIntegerNotNull")) {
            if (json.isNull("fieldIntegerNotNull")) {
                obj.realmSetter$fieldIntegerNotNull(null);
            } else {
                obj.realmSetter$fieldIntegerNotNull((int) json.getInt("fieldIntegerNotNull"));
            }
        }
        if (json.has("fieldIntegerNull")) {
            if (json.isNull("fieldIntegerNull")) {
                obj.realmSetter$fieldIntegerNull(null);
            } else {
                obj.realmSetter$fieldIntegerNull((int) json.getInt("fieldIntegerNull"));
            }
        }
        if (json.has("fieldLongNotNull")) {
            if (json.isNull("fieldLongNotNull")) {
                obj.realmSetter$fieldLongNotNull(null);
            } else {
                obj.realmSetter$fieldLongNotNull((long) json.getLong("fieldLongNotNull"));
            }
        }
        if (json.has("fieldLongNull")) {
            if (json.isNull("fieldLongNull")) {
                obj.realmSetter$fieldLongNull(null);
            } else {
                obj.realmSetter$fieldLongNull((long) json.getLong("fieldLongNull"));
            }
        }
        if (json.has("fieldFloatNotNull")) {
            if (json.isNull("fieldFloatNotNull")) {
                obj.realmSetter$fieldFloatNotNull(null);
            } else {
                obj.realmSetter$fieldFloatNotNull((float) json.getDouble("fieldFloatNotNull"));
            }
        }
        if (json.has("fieldFloatNull")) {
            if (json.isNull("fieldFloatNull")) {
                obj.realmSetter$fieldFloatNull(null);
            } else {
                obj.realmSetter$fieldFloatNull((float) json.getDouble("fieldFloatNull"));
            }
        }
        if (json.has("fieldDoubleNotNull")) {
            if (json.isNull("fieldDoubleNotNull")) {
                obj.realmSetter$fieldDoubleNotNull(null);
            } else {
                obj.realmSetter$fieldDoubleNotNull((double) json.getDouble("fieldDoubleNotNull"));
            }
        }
        if (json.has("fieldDoubleNull")) {
            if (json.isNull("fieldDoubleNull")) {
                obj.realmSetter$fieldDoubleNull(null);
            } else {
                obj.realmSetter$fieldDoubleNull((double) json.getDouble("fieldDoubleNull"));
            }
        }
        if (json.has("fieldDateNotNull")) {
            if (json.isNull("fieldDateNotNull")) {
                obj.realmSetter$fieldDateNotNull(null);
            } else {
                Object timestamp = json.get("fieldDateNotNull");
                if (timestamp instanceof String) {
                    obj.realmSetter$fieldDateNotNull(JsonUtils.stringToDate((String) timestamp));
                } else {
                    obj.realmSetter$fieldDateNotNull(new Date(json.getLong("fieldDateNotNull")));
                }
            }
        }
        if (json.has("fieldDateNull")) {
            if (json.isNull("fieldDateNull")) {
                obj.realmSetter$fieldDateNull(null);
            } else {
                Object timestamp = json.get("fieldDateNull");
                if (timestamp instanceof String) {
                    obj.realmSetter$fieldDateNull(JsonUtils.stringToDate((String) timestamp));
                } else {
                    obj.realmSetter$fieldDateNull(new Date(json.getLong("fieldDateNull")));
                }
            }
        }
        if (json.has("fieldObjectNull")) {
            if (json.isNull("fieldObjectNull")) {
                obj.realmSetter$fieldObjectNull(null);
            } else {
                some.test.NullTypes fieldObjectNullObj = NullTypesRealmProxy.createOrUpdateUsingJsonObject(realm, json.getJSONObject("fieldObjectNull"), update);
                obj.realmSetter$fieldObjectNull(fieldObjectNullObj);
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    public static NullTypes createUsingJsonStream(Realm realm, JsonReader reader)
        throws IOException {
        NullTypesRealmProxy obj = (NullTypesRealmProxy) realm.createObject(NullTypes.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("fieldStringNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldStringNotNull(null);
                } else {
                    obj.realmSetter$fieldStringNotNull((String) reader.nextString());
                }
            } else if (name.equals("fieldStringNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldStringNull(null);
                } else {
                    obj.realmSetter$fieldStringNull((String) reader.nextString());
                }
            } else if (name.equals("fieldBooleanNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldBooleanNotNull(null);
                } else {
                    obj.realmSetter$fieldBooleanNotNull((boolean) reader.nextBoolean());
                }
            } else if (name.equals("fieldBooleanNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldBooleanNull(null);
                } else {
                    obj.realmSetter$fieldBooleanNull((boolean) reader.nextBoolean());
                }
            } else if (name.equals("fieldBytesNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldBytesNotNull(null);
                } else {
                    obj.realmSetter$fieldBytesNotNull(JsonUtils.stringToBytes(reader.nextString()));
                }
            } else if (name.equals("fieldBytesNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldBytesNull(null);
                } else {
                    obj.realmSetter$fieldBytesNull(JsonUtils.stringToBytes(reader.nextString()));
                }
            } else if (name.equals("fieldByteNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldByteNotNull(null);
                } else {
                    obj.realmSetter$fieldByteNotNull((byte) reader.nextInt());
                }
            } else if (name.equals("fieldByteNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldByteNull(null);
                } else {
                    obj.realmSetter$fieldByteNull((byte) reader.nextInt());
                }
            } else if (name.equals("fieldShortNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldShortNotNull(null);
                } else {
                    obj.realmSetter$fieldShortNotNull((short) reader.nextInt());
                }
            } else if (name.equals("fieldShortNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldShortNull(null);
                } else {
                    obj.realmSetter$fieldShortNull((short) reader.nextInt());
                }
            } else if (name.equals("fieldIntegerNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldIntegerNotNull(null);
                } else {
                    obj.realmSetter$fieldIntegerNotNull((int) reader.nextInt());
                }
            } else if (name.equals("fieldIntegerNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldIntegerNull(null);
                } else {
                    obj.realmSetter$fieldIntegerNull((int) reader.nextInt());
                }
            } else if (name.equals("fieldLongNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldLongNotNull(null);
                } else {
                    obj.realmSetter$fieldLongNotNull((long) reader.nextLong());
                }
            } else if (name.equals("fieldLongNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldLongNull(null);
                } else {
                    obj.realmSetter$fieldLongNull((long) reader.nextLong());
                }
            } else if (name.equals("fieldFloatNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldFloatNotNull(null);
                } else {
                    obj.realmSetter$fieldFloatNotNull((float) reader.nextDouble());
                }
            } else if (name.equals("fieldFloatNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldFloatNull(null);
                } else {
                    obj.realmSetter$fieldFloatNull((float) reader.nextDouble());
                }
            } else if (name.equals("fieldDoubleNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldDoubleNotNull(null);
                } else {
                    obj.realmSetter$fieldDoubleNotNull((double) reader.nextDouble());
                }
            } else if (name.equals("fieldDoubleNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldDoubleNull(null);
                } else {
                    obj.realmSetter$fieldDoubleNull((double) reader.nextDouble());
                }
            } else if (name.equals("fieldDateNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldDateNotNull(null);
                } else if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        obj.realmSetter$fieldDateNotNull(new Date(timestamp));
                    }
                } else {
                    obj.realmSetter$fieldDateNotNull(JsonUtils.stringToDate(reader.nextString()));
                }
            } else if (name.equals("fieldDateNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldDateNull(null);
                } else if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        obj.realmSetter$fieldDateNull(new Date(timestamp));
                    }
                } else {
                    obj.realmSetter$fieldDateNull(JsonUtils.stringToDate(reader.nextString()));
                }
            } else if (name.equals("fieldObjectNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.realmSetter$fieldObjectNull(null);
                } else {
                    some.test.NullTypes fieldObjectNullObj = NullTypesRealmProxy.createUsingJsonStream(realm, reader);
                    obj.realmSetter$fieldObjectNull(fieldObjectNullObj);
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return obj;
    }

    public static NullTypes copyOrUpdate(Realm realm, NullTypes object, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        if (object.realm != null && object.realm.getPath().equals(realm.getPath())) {
            return object;
        }
        return copy(realm, object, update, cache);
    }

    public static NullTypes copy(Realm realm, NullTypes from, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        final boolean isStandalone = !(from instanceof NullTypesRealmProxy);
        Class<? extends NullTypes> clazz;
        Field field = null;
        if (isStandalone) {
            clazz = from.getClass();
        } else {
            clazz = null;
        }
        NullTypesRealmProxy to;

        to = (NullTypesRealmProxy) realm.createObject(NullTypes.class);
        cache.put(from, (RealmObjectProxy) to);

        try {
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldStringNotNull");
                field.setAccessible(true);
                to.realmSetter$fieldStringNotNull((java.lang.String) field.get(from));
            } else {
                to.realmSetter$fieldStringNotNull(((NullTypesRealmProxy) from).realmGetter$fieldStringNotNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldStringNull");
                field.setAccessible(true);
                to.realmSetter$fieldStringNull((java.lang.String) field.get(from));
            } else {
                to.realmSetter$fieldStringNull(((NullTypesRealmProxy) from).realmGetter$fieldStringNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldBooleanNotNull");
                field.setAccessible(true);
                to.realmSetter$fieldBooleanNotNull((java.lang.Boolean) field.get(from));
            } else {
                to.realmSetter$fieldBooleanNotNull(((NullTypesRealmProxy) from).realmGetter$fieldBooleanNotNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldBooleanNull");
                field.setAccessible(true);
                to.realmSetter$fieldBooleanNull((java.lang.Boolean) field.get(from));
            } else {
                to.realmSetter$fieldBooleanNull(((NullTypesRealmProxy) from).realmGetter$fieldBooleanNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldBytesNotNull");
                field.setAccessible(true);
                to.realmSetter$fieldBytesNotNull((byte[]) field.get(from));
            } else {
                to.realmSetter$fieldBytesNotNull(((NullTypesRealmProxy) from).realmGetter$fieldBytesNotNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldBytesNull");
                field.setAccessible(true);
                to.realmSetter$fieldBytesNull((byte[]) field.get(from));
            } else {
                to.realmSetter$fieldBytesNull(((NullTypesRealmProxy) from).realmGetter$fieldBytesNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldByteNotNull");
                field.setAccessible(true);
                to.realmSetter$fieldByteNotNull((java.lang.Byte) field.get(from));
            } else {
                to.realmSetter$fieldByteNotNull(((NullTypesRealmProxy) from).realmGetter$fieldByteNotNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldByteNull");
                field.setAccessible(true);
                to.realmSetter$fieldByteNull((java.lang.Byte) field.get(from));
            } else {
                to.realmSetter$fieldByteNull(((NullTypesRealmProxy) from).realmGetter$fieldByteNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldShortNotNull");
                field.setAccessible(true);
                to.realmSetter$fieldShortNotNull((java.lang.Short) field.get(from));
            } else {
                to.realmSetter$fieldShortNotNull(((NullTypesRealmProxy) from).realmGetter$fieldShortNotNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldShortNull");
                field.setAccessible(true);
                to.realmSetter$fieldShortNull((java.lang.Short) field.get(from));
            } else {
                to.realmSetter$fieldShortNull(((NullTypesRealmProxy) from).realmGetter$fieldShortNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldIntegerNotNull");
                field.setAccessible(true);
                to.realmSetter$fieldIntegerNotNull((java.lang.Integer) field.get(from));
            } else {
                to.realmSetter$fieldIntegerNotNull(((NullTypesRealmProxy) from).realmGetter$fieldIntegerNotNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldIntegerNull");
                field.setAccessible(true);
                to.realmSetter$fieldIntegerNull((java.lang.Integer) field.get(from));
            } else {
                to.realmSetter$fieldIntegerNull(((NullTypesRealmProxy) from).realmGetter$fieldIntegerNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldLongNotNull");
                field.setAccessible(true);
                to.realmSetter$fieldLongNotNull((java.lang.Long) field.get(from));
            } else {
                to.realmSetter$fieldLongNotNull(((NullTypesRealmProxy) from).realmGetter$fieldLongNotNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldLongNull");
                field.setAccessible(true);
                to.realmSetter$fieldLongNull((java.lang.Long) field.get(from));
            } else {
                to.realmSetter$fieldLongNull(((NullTypesRealmProxy) from).realmGetter$fieldLongNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldFloatNotNull");
                field.setAccessible(true);
                to.realmSetter$fieldFloatNotNull((java.lang.Float) field.get(from));
            } else {
                to.realmSetter$fieldFloatNotNull(((NullTypesRealmProxy) from).realmGetter$fieldFloatNotNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldFloatNull");
                field.setAccessible(true);
                to.realmSetter$fieldFloatNull((java.lang.Float) field.get(from));
            } else {
                to.realmSetter$fieldFloatNull(((NullTypesRealmProxy) from).realmGetter$fieldFloatNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldDoubleNotNull");
                field.setAccessible(true);
                to.realmSetter$fieldDoubleNotNull((java.lang.Double) field.get(from));
            } else {
                to.realmSetter$fieldDoubleNotNull(((NullTypesRealmProxy) from).realmGetter$fieldDoubleNotNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldDoubleNull");
                field.setAccessible(true);
                to.realmSetter$fieldDoubleNull((java.lang.Double) field.get(from));
            } else {
                to.realmSetter$fieldDoubleNull(((NullTypesRealmProxy) from).realmGetter$fieldDoubleNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldDateNotNull");
                field.setAccessible(true);
                to.realmSetter$fieldDateNotNull((java.util.Date) field.get(from));
            } else {
                to.realmSetter$fieldDateNotNull(((NullTypesRealmProxy) from).realmGetter$fieldDateNotNull());
            }
            if (isStandalone) {
                field = clazz.getDeclaredField("fieldDateNull");
                field.setAccessible(true);
                to.realmSetter$fieldDateNull((java.util.Date) field.get(from));
            } else {
                to.realmSetter$fieldDateNull(((NullTypesRealmProxy) from).realmGetter$fieldDateNull());
            }

             {
                some.test.NullTypes fieldObjectNullObj;
                if (isStandalone) {
                    field = clazz.getDeclaredField("fieldObjectNull");
                    field.setAccessible(true);
                    fieldObjectNullObj = (some.test.NullTypes) field.get(from);
                } else {
                    fieldObjectNullObj = ((NullTypesRealmProxy) from).realmGetter$fieldObjectNull();
                }
                if (fieldObjectNullObj != null) {
                    some.test.NullTypes cachefieldObjectNull = (some.test.NullTypes) cache.get(fieldObjectNullObj);
                    if (cachefieldObjectNull != null) {
                        to.realmSetter$fieldObjectNull(cachefieldObjectNull);
                    } else {
                        to.realmSetter$fieldObjectNull(NullTypesRealmProxy.copyOrUpdate(realm, fieldObjectNullObj, update, cache));
                    }
                } else {
                    to.realmSetter$fieldObjectNull(null);
                }
            }
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        return to;
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
        Class<?> clazz = standaloneObject.getClass();
        Field field = null;
        try {
            field = clazz.getDeclaredField("fieldStringNotNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldStringNotNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldStringNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldStringNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldBooleanNotNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldBooleanNotNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldBooleanNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldBooleanNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldBytesNotNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldBytesNotNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldBytesNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldBytesNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldByteNotNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldByteNotNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldByteNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldByteNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldShortNotNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldShortNotNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldShortNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldShortNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldIntegerNotNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldIntegerNotNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldIntegerNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldIntegerNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldLongNotNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldLongNotNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldLongNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldLongNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldFloatNotNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldFloatNotNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldFloatNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldFloatNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldDoubleNotNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldDoubleNotNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldDoubleNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldDoubleNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldDateNotNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldDateNotNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldDateNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);
        try {
            field.set(standaloneObject, ((NullTypesRealmProxy) realmObject).realmGetter$fieldDateNull());
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        try {
            field = clazz.getDeclaredField("fieldObjectNull");
        } catch (NoSuchFieldException e) {
            throw new RealmException(e.getMessage());
        }
        field.setAccessible(true);

        // Deep copy of fieldObjectNull
        try {
            field.set(standaloneObject, NullTypesRealmProxy.createDetachedCopy( ((NullTypesRealmProxy) realmObject).realmGetter$fieldObjectNull(), currentDepth + 1, maxDepth, cache));
        } catch (IllegalAccessException e) {
            throw new RealmException(e.getMessage());
        }
        return standaloneObject;
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("NullTypes = [");
        stringBuilder.append("{fieldStringNotNull:");
        stringBuilder.append(realmGetter$fieldStringNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldStringNull:");
        stringBuilder.append(realmGetter$fieldStringNull() != null ? realmGetter$fieldStringNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBooleanNotNull:");
        stringBuilder.append(realmGetter$fieldBooleanNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBooleanNull:");
        stringBuilder.append(realmGetter$fieldBooleanNull() != null ? realmGetter$fieldBooleanNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBytesNotNull:");
        stringBuilder.append(realmGetter$fieldBytesNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBytesNull:");
        stringBuilder.append(realmGetter$fieldBytesNull() != null ? realmGetter$fieldBytesNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldByteNotNull:");
        stringBuilder.append(realmGetter$fieldByteNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldByteNull:");
        stringBuilder.append(realmGetter$fieldByteNull() != null ? realmGetter$fieldByteNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldShortNotNull:");
        stringBuilder.append(realmGetter$fieldShortNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldShortNull:");
        stringBuilder.append(realmGetter$fieldShortNull() != null ? realmGetter$fieldShortNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldIntegerNotNull:");
        stringBuilder.append(realmGetter$fieldIntegerNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldIntegerNull:");
        stringBuilder.append(realmGetter$fieldIntegerNull() != null ? realmGetter$fieldIntegerNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldLongNotNull:");
        stringBuilder.append(realmGetter$fieldLongNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldLongNull:");
        stringBuilder.append(realmGetter$fieldLongNull() != null ? realmGetter$fieldLongNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldFloatNotNull:");
        stringBuilder.append(realmGetter$fieldFloatNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldFloatNull:");
        stringBuilder.append(realmGetter$fieldFloatNull() != null ? realmGetter$fieldFloatNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDoubleNotNull:");
        stringBuilder.append(realmGetter$fieldDoubleNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDoubleNull:");
        stringBuilder.append(realmGetter$fieldDoubleNull() != null ? realmGetter$fieldDoubleNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDateNotNull:");
        stringBuilder.append(realmGetter$fieldDateNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDateNull:");
        stringBuilder.append(realmGetter$fieldDateNull() != null ? realmGetter$fieldDateNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldObjectNull:");
        stringBuilder.append(realmGetter$fieldObjectNull() != null ? "NullTypes" : "null");
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
        NullTypesRealmProxy aNullTypes = (NullTypesRealmProxy)o;

        String path = realm.getPath();
        String otherPath = aNullTypes.realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;;

        String tableName = row.getTable().getName();
        String otherTableName = aNullTypes.row.getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (row.getIndex() != aNullTypes.row.getIndex()) return false;

        return true;
    }

}