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
import some.test.NullTypes;

public class NullTypesRealmProxy extends NullTypes implements RealmObjectProxy {

    private static long INDEX_FIELDSTRINGNOTNULL;
    private static long INDEX_FIELDSTRINGNULL;
    private static long INDEX_FIELDBOOLEANNOTNULL;
    private static long INDEX_FIELDBOOLEANNULL;
    private static long INDEX_FIELDBYTESNOTNULL;
    private static long INDEX_FIELDBYTESNULL;
    private static long INDEX_FIELDBYTENOTNULL;
    private static long INDEX_FIELDBYTENULL;
    private static long INDEX_FIELDSHORTNOTNULL;
    private static long INDEX_FIELDSHORTNULL;
    private static long INDEX_FIELDINTEGERNOTNULL;
    private static long INDEX_FIELDINTEGERNULL;
    private static long INDEX_FIELDLONGNOTNULL;
    private static long INDEX_FIELDLONGNULL;
    private static long INDEX_FIELDFLOATNOTNULL;
    private static long INDEX_FIELDFLOATNULL;
    private static long INDEX_FIELDDOUBLENOTNULL;
    private static long INDEX_FIELDDOUBLENULL;
    private static long INDEX_FIELDDATENOTNULL;
    private static long INDEX_FIELDDATENULL;
    private static Map<String, Long> columnIndices;
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
        FIELD_NAMES = Collections.unmodifiableList(fieldNames);
    }

    @Override
    public String getFieldStringNotNull() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(INDEX_FIELDSTRINGNOTNULL);
    }

    @Override
    public void setFieldStringNotNull(String value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set a non-nullable field fieldStringNotNull in NullTypes to null.");
        }
        row.setString(INDEX_FIELDSTRINGNOTNULL, (String) value);
    }

    @Override
    public String getFieldStringNull() {
        realm.checkIfValid();
        return (java.lang.String) row.getString(INDEX_FIELDSTRINGNULL);
    }

    @Override
    public void setFieldStringNull(String value) {
        realm.checkIfValid();
        row.setString(INDEX_FIELDSTRINGNULL, (String) value);
    }

    @Override
    public Boolean getFieldBooleanNotNull() {
        realm.checkIfValid();
        return (boolean) row.getBoolean(INDEX_FIELDBOOLEANNOTNULL);
    }

    @Override
    public void setFieldBooleanNotNull(Boolean value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set a non-nullable field fieldBooleanNotNull in NullTypes to null.");
        }
        row.setBoolean(INDEX_FIELDBOOLEANNOTNULL, (Boolean) value);
    }

    @Override
    public Boolean getFieldBooleanNull() {
        realm.checkIfValid();
        if (row.isNull(INDEX_FIELDBOOLEANNULL)) {
            return null;
        }
        return (boolean) row.getBoolean(INDEX_FIELDBOOLEANNULL);
    }

    @Override
    public void setFieldBooleanNull(Boolean value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(INDEX_FIELDBOOLEANNULL);
            return;
        }
        row.setBoolean(INDEX_FIELDBOOLEANNULL, (Boolean) value);
    }

    @Override
    public byte[] getFieldBytesNotNull() {
        realm.checkIfValid();
        return (byte[]) row.getBinaryByteArray(INDEX_FIELDBYTESNOTNULL);
    }

    @Override
    public void setFieldBytesNotNull(byte[] value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set a non-nullable field fieldBytesNotNull in NullTypes to null.");
        }
        row.setBinaryByteArray(INDEX_FIELDBYTESNOTNULL, (byte[]) value);
    }

    @Override
    public byte[] getFieldBytesNull() {
        realm.checkIfValid();
        if (row.isNull(INDEX_FIELDBYTESNULL)) {
            return null;
        }
        return (byte[]) row.getBinaryByteArray(INDEX_FIELDBYTESNULL);
    }

    @Override
    public void setFieldBytesNull(byte[] value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(INDEX_FIELDBYTESNULL);
            return;
        }
        row.setBinaryByteArray(INDEX_FIELDBYTESNULL, (byte[]) value);
    }

    @Override
    public Byte getFieldByteNotNull() {
        realm.checkIfValid();
        return (byte) row.getLong(INDEX_FIELDBYTENOTNULL);
    }

    @Override
    public void setFieldByteNotNull(Byte value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set a non-nullable field fieldByteNotNull in NullTypes to null.");
        }
        row.setLong(INDEX_FIELDBYTENOTNULL, (long) value);
    }

    @Override
    public Byte getFieldByteNull() {
        realm.checkIfValid();
        if (row.isNull(INDEX_FIELDBYTENULL)) {
            return null;
        }
        return (byte) row.getLong(INDEX_FIELDBYTENULL);
    }

    @Override
    public void setFieldByteNull(Byte value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(INDEX_FIELDBYTENULL);
            return;
        }
        row.setLong(INDEX_FIELDBYTENULL, (long) value);
    }

    @Override
    public Short getFieldShortNotNull() {
        realm.checkIfValid();
        return (short) row.getLong(INDEX_FIELDSHORTNOTNULL);
    }

    @Override
    public void setFieldShortNotNull(Short value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set a non-nullable field fieldShortNotNull in NullTypes to null.");
        }
        row.setLong(INDEX_FIELDSHORTNOTNULL, (long) value);
    }

    @Override
    public Short getFieldShortNull() {
        realm.checkIfValid();
        if (row.isNull(INDEX_FIELDSHORTNULL)) {
            return null;
        }
        return (short) row.getLong(INDEX_FIELDSHORTNULL);
    }

    @Override
    public void setFieldShortNull(Short value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(INDEX_FIELDSHORTNULL);
            return;
        }
        row.setLong(INDEX_FIELDSHORTNULL, (long) value);
    }

    @Override
    public Integer getFieldIntegerNotNull() {
        realm.checkIfValid();
        return (int) row.getLong(INDEX_FIELDINTEGERNOTNULL);
    }

    @Override
    public void setFieldIntegerNotNull(Integer value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set a non-nullable field fieldIntegerNotNull in NullTypes to null.");
        }
        row.setLong(INDEX_FIELDINTEGERNOTNULL, (long) value);
    }

    @Override
    public Integer getFieldIntegerNull() {
        realm.checkIfValid();
        if (row.isNull(INDEX_FIELDINTEGERNULL)) {
            return null;
        }
        return (int) row.getLong(INDEX_FIELDINTEGERNULL);
    }

    @Override
    public void setFieldIntegerNull(Integer value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(INDEX_FIELDINTEGERNULL);
            return;
        }
        row.setLong(INDEX_FIELDINTEGERNULL, (long) value);
    }

    @Override
    public Long getFieldLongNotNull() {
        realm.checkIfValid();
        return (long) row.getLong(INDEX_FIELDLONGNOTNULL);
    }

    @Override
    public void setFieldLongNotNull(Long value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set a non-nullable field fieldLongNotNull in NullTypes to null.");
        }
        row.setLong(INDEX_FIELDLONGNOTNULL, (long) value);
    }

    @Override
    public Long getFieldLongNull() {
        realm.checkIfValid();
        if (row.isNull(INDEX_FIELDLONGNULL)) {
            return null;
        }
        return (long) row.getLong(INDEX_FIELDLONGNULL);
    }

    @Override
    public void setFieldLongNull(Long value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(INDEX_FIELDLONGNULL);
            return;
        }
        row.setLong(INDEX_FIELDLONGNULL, (long) value);
    }

    @Override
    public Float getFieldFloatNotNull() {
        realm.checkIfValid();
        return (float) row.getFloat(INDEX_FIELDFLOATNOTNULL);
    }

    @Override
    public void setFieldFloatNotNull(Float value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set a non-nullable field fieldFloatNotNull in NullTypes to null.");
        }
        row.setFloat(INDEX_FIELDFLOATNOTNULL, (float) value);
    }

    @Override
    public Float getFieldFloatNull() {
        realm.checkIfValid();
        if (row.isNull(INDEX_FIELDFLOATNULL)) {
            return null;
        }
        return (float) row.getFloat(INDEX_FIELDFLOATNULL);
    }

    @Override
    public void setFieldFloatNull(Float value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(INDEX_FIELDFLOATNULL);
            return;
        }
        row.setFloat(INDEX_FIELDFLOATNULL, (float) value);
    }

    @Override
    public Double getFieldDoubleNotNull() {
        realm.checkIfValid();
        return (double) row.getDouble(INDEX_FIELDDOUBLENOTNULL);
    }

    @Override
    public void setFieldDoubleNotNull(Double value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set a non-nullable field fieldDoubleNotNull in NullTypes to null.");
        }
        row.setDouble(INDEX_FIELDDOUBLENOTNULL, (double) value);
    }

    @Override
    public Double getFieldDoubleNull() {
        realm.checkIfValid();
        if (row.isNull(INDEX_FIELDDOUBLENULL)) {
            return null;
        }
        return (double) row.getDouble(INDEX_FIELDDOUBLENULL);
    }

    @Override
    public void setFieldDoubleNull(Double value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(INDEX_FIELDDOUBLENULL);
            return;
        }
        row.setDouble(INDEX_FIELDDOUBLENULL, (double) value);
    }

    @Override
    public Date getFieldDateNotNull() {
        realm.checkIfValid();
        return (java.util.Date) row.getDate(INDEX_FIELDDATENOTNULL);
    }

    @Override
    public void setFieldDateNotNull(Date value) {
        realm.checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set a non-nullable field fieldDateNotNull in NullTypes to null.");
        }
        row.setDate(INDEX_FIELDDATENOTNULL, (Date) value);
    }

    @Override
    public Date getFieldDateNull() {
        realm.checkIfValid();
        if (row.isNull(INDEX_FIELDDATENULL)) {
            return null;
        }
        return (java.util.Date) row.getDate(INDEX_FIELDDATENULL);
    }

    @Override
    public void setFieldDateNull(Date value) {
        realm.checkIfValid();
        if (value == null) {
            row.setNull(INDEX_FIELDDATENULL);
            return;
        }
        row.setDate(INDEX_FIELDDATENULL, (Date) value);
    }

    public static Table initTable(ImplicitTransaction transaction) {
        if (!transaction.hasTable("class_NullTypes")) {
            Table table = transaction.getTable("class_NullTypes");
            table.addColumn(ColumnType.STRING, "fieldStringNotNull", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.STRING, "fieldStringNull", Table.NULLABLE);
            table.addColumn(ColumnType.BOOLEAN, "fieldBooleanNotNull", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.BOOLEAN, "fieldBooleanNull", Table.NULLABLE);
            table.addColumn(ColumnType.BINARY, "fieldBytesNotNull", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.BINARY, "fieldBytesNull", Table.NULLABLE);
            table.addColumn(ColumnType.INTEGER, "fieldByteNotNull", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.INTEGER, "fieldByteNull", Table.NULLABLE);
            table.addColumn(ColumnType.INTEGER, "fieldShortNotNull", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.INTEGER, "fieldShortNull", Table.NULLABLE);
            table.addColumn(ColumnType.INTEGER, "fieldIntegerNotNull", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.INTEGER, "fieldIntegerNull", Table.NULLABLE);
            table.addColumn(ColumnType.INTEGER, "fieldLongNotNull", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.INTEGER, "fieldLongNull", Table.NULLABLE);
            table.addColumn(ColumnType.FLOAT, "fieldFloatNotNull", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.FLOAT, "fieldFloatNull", Table.NULLABLE);
            table.addColumn(ColumnType.DOUBLE, "fieldDoubleNotNull", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.DOUBLE, "fieldDoubleNull", Table.NULLABLE);
            table.addColumn(ColumnType.DATE, "fieldDateNotNull", Table.NOT_NULLABLE);
            table.addColumn(ColumnType.DATE, "fieldDateNull", Table.NULLABLE);
            table.setPrimaryKey("");
            return table;
        }
        return transaction.getTable("class_NullTypes");
    }

    public static void validateTable(ImplicitTransaction transaction) {
        if (transaction.hasTable("class_NullTypes")) {
            Table table = transaction.getTable("class_NullTypes");
            if (table.getColumnCount() != 20) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Field count does not match - expected 20 but was " + table.getColumnCount());
            }
            Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>();
            for (long i = 0; i < 20; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            columnIndices = new HashMap<String, Long>();
            for (String fieldName : getFieldNames()) {
                long index = table.getColumnIndex(fieldName);
                if (index == -1) {
                    throw new RealmMigrationNeededException(transaction.getPath(), "Field '" + fieldName + "' not found for type NullTypes");
                }
                columnIndices.put(fieldName, index);
            }

            INDEX_FIELDSTRINGNOTNULL = table.getColumnIndex("fieldStringNotNull");
            INDEX_FIELDSTRINGNULL = table.getColumnIndex("fieldStringNull");
            INDEX_FIELDBOOLEANNOTNULL = table.getColumnIndex("fieldBooleanNotNull");
            INDEX_FIELDBOOLEANNULL = table.getColumnIndex("fieldBooleanNull");
            INDEX_FIELDBYTESNOTNULL = table.getColumnIndex("fieldBytesNotNull");
            INDEX_FIELDBYTESNULL = table.getColumnIndex("fieldBytesNull");
            INDEX_FIELDBYTENOTNULL = table.getColumnIndex("fieldByteNotNull");
            INDEX_FIELDBYTENULL = table.getColumnIndex("fieldByteNull");
            INDEX_FIELDSHORTNOTNULL = table.getColumnIndex("fieldShortNotNull");
            INDEX_FIELDSHORTNULL = table.getColumnIndex("fieldShortNull");
            INDEX_FIELDINTEGERNOTNULL = table.getColumnIndex("fieldIntegerNotNull");
            INDEX_FIELDINTEGERNULL = table.getColumnIndex("fieldIntegerNull");
            INDEX_FIELDLONGNOTNULL = table.getColumnIndex("fieldLongNotNull");
            INDEX_FIELDLONGNULL = table.getColumnIndex("fieldLongNull");
            INDEX_FIELDFLOATNOTNULL = table.getColumnIndex("fieldFloatNotNull");
            INDEX_FIELDFLOATNULL = table.getColumnIndex("fieldFloatNull");
            INDEX_FIELDDOUBLENOTNULL = table.getColumnIndex("fieldDoubleNotNull");
            INDEX_FIELDDOUBLENULL = table.getColumnIndex("fieldDoubleNull");
            INDEX_FIELDDATENOTNULL = table.getColumnIndex("fieldDateNotNull");
            INDEX_FIELDDATENULL = table.getColumnIndex("fieldDateNull");

            if (!columnTypes.containsKey("fieldStringNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldStringNotNull'");
            }
            if (columnTypes.get("fieldStringNotNull") != ColumnType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'fieldStringNotNull'");
            }
            if (table.isColumnNullable(INDEX_FIELDSTRINGNOTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Remove annotation @Required or @PrimaryKey from field 'fieldStringNotNull'");
            }
            if (!columnTypes.containsKey("fieldStringNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldStringNull'");
            }
            if (columnTypes.get("fieldStringNull") != ColumnType.STRING) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'String' for field 'fieldStringNull'");
            }
            if (!table.isColumnNullable(INDEX_FIELDSTRINGNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Add annotation @Required or @PrimaryKey to field 'fieldStringNull'");
            }
            if (!columnTypes.containsKey("fieldBooleanNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldBooleanNotNull'");
            }
            if (columnTypes.get("fieldBooleanNotNull") != ColumnType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Boolean' for field 'fieldBooleanNotNull'");
            }
            if (table.isColumnNullable(INDEX_FIELDBOOLEANNOTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Remove annotation @Required or @PrimaryKey from field 'fieldBooleanNotNull'");
            }
            if (!columnTypes.containsKey("fieldBooleanNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldBooleanNull'");
            }
            if (columnTypes.get("fieldBooleanNull") != ColumnType.BOOLEAN) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Boolean' for field 'fieldBooleanNull'");
            }
            if (!table.isColumnNullable(INDEX_FIELDBOOLEANNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Add annotation @Required or @PrimaryKey to field 'fieldBooleanNull'");
            }
            if (!columnTypes.containsKey("fieldBytesNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldBytesNotNull'");
            }
            if (columnTypes.get("fieldBytesNotNull") != ColumnType.BINARY) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'byte[]' for field 'fieldBytesNotNull'");
            }
            if (table.isColumnNullable(INDEX_FIELDBYTESNOTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Remove annotation @Required or @PrimaryKey from field 'fieldBytesNotNull'");
            }
            if (!columnTypes.containsKey("fieldBytesNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldBytesNull'");
            }
            if (columnTypes.get("fieldBytesNull") != ColumnType.BINARY) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'byte[]' for field 'fieldBytesNull'");
            }
            if (!table.isColumnNullable(INDEX_FIELDBYTESNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Add annotation @Required or @PrimaryKey to field 'fieldBytesNull'");
            }
            if (!columnTypes.containsKey("fieldByteNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldByteNotNull'");
            }
            if (columnTypes.get("fieldByteNotNull") != ColumnType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Byte' for field 'fieldByteNotNull'");
            }
            if (table.isColumnNullable(INDEX_FIELDBYTENOTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Remove annotation @Required or @PrimaryKey from field 'fieldByteNotNull'");
            }
            if (!columnTypes.containsKey("fieldByteNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldByteNull'");
            }
            if (columnTypes.get("fieldByteNull") != ColumnType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Byte' for field 'fieldByteNull'");
            }
            if (!table.isColumnNullable(INDEX_FIELDBYTENULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Add annotation @Required or @PrimaryKey to field 'fieldByteNull'");
            }
            if (!columnTypes.containsKey("fieldShortNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldShortNotNull'");
            }
            if (columnTypes.get("fieldShortNotNull") != ColumnType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Short' for field 'fieldShortNotNull'");
            }
            if (table.isColumnNullable(INDEX_FIELDSHORTNOTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Remove annotation @Required or @PrimaryKey from field 'fieldShortNotNull'");
            }
            if (!columnTypes.containsKey("fieldShortNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldShortNull'");
            }
            if (columnTypes.get("fieldShortNull") != ColumnType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Short' for field 'fieldShortNull'");
            }
            if (!table.isColumnNullable(INDEX_FIELDSHORTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Add annotation @Required or @PrimaryKey to field 'fieldShortNull'");
            }
            if (!columnTypes.containsKey("fieldIntegerNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldIntegerNotNull'");
            }
            if (columnTypes.get("fieldIntegerNotNull") != ColumnType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Integer' for field 'fieldIntegerNotNull'");
            }
            if (table.isColumnNullable(INDEX_FIELDINTEGERNOTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Remove annotation @Required or @PrimaryKey from field 'fieldIntegerNotNull'");
            }
            if (!columnTypes.containsKey("fieldIntegerNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldIntegerNull'");
            }
            if (columnTypes.get("fieldIntegerNull") != ColumnType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Integer' for field 'fieldIntegerNull'");
            }
            if (!table.isColumnNullable(INDEX_FIELDINTEGERNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Add annotation @Required or @PrimaryKey to field 'fieldIntegerNull'");
            }
            if (!columnTypes.containsKey("fieldLongNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldLongNotNull'");
            }
            if (columnTypes.get("fieldLongNotNull") != ColumnType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Long' for field 'fieldLongNotNull'");
            }
            if (table.isColumnNullable(INDEX_FIELDLONGNOTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Remove annotation @Required or @PrimaryKey from field 'fieldLongNotNull'");
            }
            if (!columnTypes.containsKey("fieldLongNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldLongNull'");
            }
            if (columnTypes.get("fieldLongNull") != ColumnType.INTEGER) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Long' for field 'fieldLongNull'");
            }
            if (!table.isColumnNullable(INDEX_FIELDLONGNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Add annotation @Required or @PrimaryKey to field 'fieldLongNull'");
            }
            if (!columnTypes.containsKey("fieldFloatNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldFloatNotNull'");
            }
            if (columnTypes.get("fieldFloatNotNull") != ColumnType.FLOAT) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Float' for field 'fieldFloatNotNull'");
            }
            if (table.isColumnNullable(INDEX_FIELDFLOATNOTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Remove annotation @Required or @PrimaryKey from field 'fieldFloatNotNull'");
            }
            if (!columnTypes.containsKey("fieldFloatNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldFloatNull'");
            }
            if (columnTypes.get("fieldFloatNull") != ColumnType.FLOAT) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Float' for field 'fieldFloatNull'");
            }
            if (!table.isColumnNullable(INDEX_FIELDFLOATNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Add annotation @Required or @PrimaryKey to field 'fieldFloatNull'");
            }
            if (!columnTypes.containsKey("fieldDoubleNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldDoubleNotNull'");
            }
            if (columnTypes.get("fieldDoubleNotNull") != ColumnType.DOUBLE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Double' for field 'fieldDoubleNotNull'");
            }
            if (table.isColumnNullable(INDEX_FIELDDOUBLENOTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Remove annotation @Required or @PrimaryKey from field 'fieldDoubleNotNull'");
            }
            if (!columnTypes.containsKey("fieldDoubleNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldDoubleNull'");
            }
            if (columnTypes.get("fieldDoubleNull") != ColumnType.DOUBLE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Double' for field 'fieldDoubleNull'");
            }
            if (!table.isColumnNullable(INDEX_FIELDDOUBLENULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Add annotation @Required or @PrimaryKey to field 'fieldDoubleNull'");
            }
            if (!columnTypes.containsKey("fieldDateNotNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldDateNotNull'");
            }
            if (columnTypes.get("fieldDateNotNull") != ColumnType.DATE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Date' for field 'fieldDateNotNull'");
            }
            if (table.isColumnNullable(INDEX_FIELDDATENOTNULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Remove annotation @Required or @PrimaryKey from field 'fieldDateNotNull'");
            }
            if (!columnTypes.containsKey("fieldDateNull")) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Missing field 'fieldDateNull'");
            }
            if (columnTypes.get("fieldDateNull") != ColumnType.DATE) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Invalid type 'Date' for field 'fieldDateNull'");
            }
            if (!table.isColumnNullable(INDEX_FIELDDATENULL)) {
                throw new RealmMigrationNeededException(transaction.getPath(), "Add annotation @Required or @PrimaryKey to field 'fieldDateNull'");
            }
        } else {
            throw new RealmMigrationNeededException(transaction.getPath(), "The NullTypes class is missing from the schema for this Realm.");
        }
    }

    public static String getTableName() { return "class_NullTypes"; }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    public static Map<String,Long> getColumnIndices() {
        return columnIndices;
    }

    public static NullTypes createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        NullTypes obj = realm.createObject(NullTypes.class);
        if (json.has("fieldStringNotNull")) {
            if (json.isNull("fieldStringNotNull")) {
                throw new IllegalArgumentException("Trying to set null on not-nullable fieldStringNotNull.");
            } else {
                obj.setFieldStringNotNull((String) json.getString("fieldStringNotNull"));
            }
        }
        if (json.has("fieldStringNull")) {
            if (json.isNull("fieldStringNull")) {
                obj.setFieldStringNull(null);
            } else {
                obj.setFieldStringNull((String) json.getString("fieldStringNull"));
            }
        }
        if (json.has("fieldBooleanNotNull")) {
            if (json.isNull("fieldBooleanNotNull")) {
                throw new IllegalArgumentException("Trying to set null on not-nullable fieldBooleanNotNull.");
            } else {
                obj.setFieldBooleanNotNull((boolean) json.getBoolean("fieldBooleanNotNull"));
            }
        }
        if (json.has("fieldBooleanNull")) {
            if (json.isNull("fieldBooleanNull")) {
                obj.setFieldBooleanNull(null);
            } else {
                obj.setFieldBooleanNull((boolean) json.getBoolean("fieldBooleanNull"));
            }
        }
        if (json.has("fieldBytesNotNull")) {
            if (json.isNull("fieldBytesNotNull")) {
                throw new IllegalArgumentException("Trying to set null on not-nullable fieldBytesNotNull.");
            } else {
                obj.setFieldBytesNotNull(JsonUtils.stringToBytes(json.getString("fieldBytesNotNull")));
            }
        }
        if (json.has("fieldBytesNull")) {
            if (json.isNull("fieldBytesNull")) {
                obj.setFieldBytesNull(null);
            } else {
                obj.setFieldBytesNull(JsonUtils.stringToBytes(json.getString("fieldBytesNull")));
            }
        }
        if (json.has("fieldByteNotNull")) {
            if (json.isNull("fieldByteNotNull")) {
                throw new IllegalArgumentException("Trying to set null on not-nullable fieldByteNotNull.");
            } else {
                obj.setFieldByteNotNull((byte) json.getInt("fieldByteNotNull"));
            }
        }
        if (json.has("fieldByteNull")) {
            if (json.isNull("fieldByteNull")) {
                obj.setFieldByteNull(null);
            } else {
                obj.setFieldByteNull((byte) json.getInt("fieldByteNull"));
            }
        }
        if (json.has("fieldShortNotNull")) {
            if (json.isNull("fieldShortNotNull")) {
                throw new IllegalArgumentException("Trying to set null on not-nullable fieldShortNotNull.");
            } else {
                obj.setFieldShortNotNull((short) json.getInt("fieldShortNotNull"));
            }
        }
        if (json.has("fieldShortNull")) {
            if (json.isNull("fieldShortNull")) {
                obj.setFieldShortNull(null);
            } else {
                obj.setFieldShortNull((short) json.getInt("fieldShortNull"));
            }
        }
        if (json.has("fieldIntegerNotNull")) {
            if (json.isNull("fieldIntegerNotNull")) {
                throw new IllegalArgumentException("Trying to set null on not-nullable fieldIntegerNotNull.");
            } else {
                obj.setFieldIntegerNotNull((int) json.getInt("fieldIntegerNotNull"));
            }
        }
        if (json.has("fieldIntegerNull")) {
            if (json.isNull("fieldIntegerNull")) {
                obj.setFieldIntegerNull(null);
            } else {
                obj.setFieldIntegerNull((int) json.getInt("fieldIntegerNull"));
            }
        }
        if (json.has("fieldLongNotNull")) {
            if (json.isNull("fieldLongNotNull")) {
                throw new IllegalArgumentException("Trying to set null on not-nullable fieldLongNotNull.");
            } else {
                obj.setFieldLongNotNull((long) json.getLong("fieldLongNotNull"));
            }
        }
        if (json.has("fieldLongNull")) {
            if (json.isNull("fieldLongNull")) {
                obj.setFieldLongNull(null);
            } else {
                obj.setFieldLongNull((long) json.getLong("fieldLongNull"));
            }
        }
        if (json.has("fieldFloatNotNull")) {
            if (json.isNull("fieldFloatNotNull")) {
                throw new IllegalArgumentException("Trying to set null on not-nullable fieldFloatNotNull.");
            } else {
                obj.setFieldFloatNotNull((float) json.getDouble("fieldFloatNotNull"));
            }
        }
        if (json.has("fieldFloatNull")) {
            if (json.isNull("fieldFloatNull")) {
                obj.setFieldFloatNull(null);
            } else {
                obj.setFieldFloatNull((float) json.getDouble("fieldFloatNull"));
            }
        }
        if (json.has("fieldDoubleNotNull")) {
            if (json.isNull("fieldDoubleNotNull")) {
                throw new IllegalArgumentException("Trying to set null on not-nullable fieldDoubleNotNull.");
            } else {
                obj.setFieldDoubleNotNull((double) json.getDouble("fieldDoubleNotNull"));
            }
        }
        if (json.has("fieldDoubleNull")) {
            if (json.isNull("fieldDoubleNull")) {
                obj.setFieldDoubleNull(null);
            } else {
                obj.setFieldDoubleNull((double) json.getDouble("fieldDoubleNull"));
            }
        }
        if (json.has("fieldDateNotNull")) {
            if (json.isNull("fieldDateNotNull")) {
                throw new IllegalArgumentException("Trying to set null on not-nullable fieldDateNotNull.");
            } else {
                Object timestamp = json.get("fieldDateNotNull");
                if (timestamp instanceof String) {
                    obj.setFieldDateNotNull(JsonUtils.stringToDate((String) timestamp));
                } else {
                    obj.setFieldDateNotNull(new Date(json.getLong("fieldDateNotNull")));
                }
            }
        }
        if (json.has("fieldDateNull")) {
            if (json.isNull("fieldDateNull")) {
                obj.setFieldDateNull(null);
            } else {
                Object timestamp = json.get("fieldDateNull");
                if (timestamp instanceof String) {
                    obj.setFieldDateNull(JsonUtils.stringToDate((String) timestamp));
                } else {
                    obj.setFieldDateNull(new Date(json.getLong("fieldDateNull")));
                }
            }
        }
        return obj;
    }

    public static NullTypes createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        NullTypes obj = realm.createObject(NullTypes.class);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("fieldStringNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set null on not-nullable fieldStringNotNull.");
                } else {
                    obj.setFieldStringNotNull((String) reader.nextString());
                }
            } else if (name.equals("fieldStringNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.setFieldStringNull(null);
                } else {
                    obj.setFieldStringNull((String) reader.nextString());
                }
            } else if (name.equals("fieldBooleanNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set null on not-nullable fieldBooleanNotNull.");
                } else {
                    obj.setFieldBooleanNotNull((boolean) reader.nextBoolean());
                }
            } else if (name.equals("fieldBooleanNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.setFieldBooleanNull(null);
                } else {
                    obj.setFieldBooleanNull((boolean) reader.nextBoolean());
                }
            } else if (name.equals("fieldBytesNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set null on not-nullable fieldBytesNotNull.");
                } else {
                    obj.setFieldBytesNotNull(JsonUtils.stringToBytes(reader.nextString()));
                }
            } else if (name.equals("fieldBytesNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.setFieldBytesNull(null);
                } else {
                    obj.setFieldBytesNull(JsonUtils.stringToBytes(reader.nextString()));
                }
            } else if (name.equals("fieldByteNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set null on not-nullable fieldByteNotNull.");
                } else {
                    obj.setFieldByteNotNull((byte) reader.nextInt());
                }
            } else if (name.equals("fieldByteNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.setFieldByteNull(null);
                } else {
                    obj.setFieldByteNull((byte) reader.nextInt());
                }
            } else if (name.equals("fieldShortNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set null on not-nullable fieldShortNotNull.");
                } else {
                    obj.setFieldShortNotNull((short) reader.nextInt());
                }
            } else if (name.equals("fieldShortNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.setFieldShortNull(null);
                } else {
                    obj.setFieldShortNull((short) reader.nextInt());
                }
            } else if (name.equals("fieldIntegerNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set null on not-nullable fieldIntegerNotNull.");
                } else {
                    obj.setFieldIntegerNotNull((int) reader.nextInt());
                }
            } else if (name.equals("fieldIntegerNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.setFieldIntegerNull(null);
                } else {
                    obj.setFieldIntegerNull((int) reader.nextInt());
                }
            } else if (name.equals("fieldLongNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set null on not-nullable fieldLongNotNull.");
                } else {
                    obj.setFieldLongNotNull((long) reader.nextLong());
                }
            } else if (name.equals("fieldLongNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.setFieldLongNull(null);
                } else {
                    obj.setFieldLongNull((long) reader.nextLong());
                }
            } else if (name.equals("fieldFloatNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set null on not-nullable fieldFloatNotNull.");
                } else {
                    obj.setFieldFloatNotNull((float) reader.nextDouble());
                }
            } else if (name.equals("fieldFloatNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.setFieldFloatNull(null);
                } else {
                    obj.setFieldFloatNull((float) reader.nextDouble());
                }
            } else if (name.equals("fieldDoubleNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set null on not-nullable fieldDoubleNotNull.");
                } else {
                    obj.setFieldDoubleNotNull((double) reader.nextDouble());
                }
            } else if (name.equals("fieldDoubleNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.setFieldDoubleNull(null);
                } else {
                    obj.setFieldDoubleNull((double) reader.nextDouble());
                }
            } else if (name.equals("fieldDateNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set null on not-nullable fieldDateNotNull.");
                } else if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        obj.setFieldDateNotNull(new Date(timestamp));
                    }
                } else {
                    obj.setFieldDateNotNull(JsonUtils.stringToDate(reader.nextString()));
                }
            } else if (name.equals("fieldDateNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    obj.setFieldDateNull(null);
                } else if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        obj.setFieldDateNull(new Date(timestamp));
                    }
                } else {
                    obj.setFieldDateNull(JsonUtils.stringToDate(reader.nextString()));
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

    public static NullTypes copy(Realm realm, NullTypes newObject, boolean update, Map<RealmObject,RealmObjectProxy> cache) {
        NullTypes realmObject = realm.createObject(NullTypes.class);
        cache.put(newObject, (RealmObjectProxy) realmObject);
        realmObject.setFieldStringNotNull(newObject.getFieldStringNotNull() != null ? newObject.getFieldStringNotNull() : "");
        realmObject.setFieldStringNull(newObject.getFieldStringNull());
        realmObject.setFieldBooleanNotNull(newObject.getFieldBooleanNotNull() != null ? newObject.getFieldBooleanNotNull() : false);
        realmObject.setFieldBooleanNull(newObject.getFieldBooleanNull());
        realmObject.setFieldBytesNotNull(newObject.getFieldBytesNotNull() != null ? newObject.getFieldBytesNotNull() : new byte[0]);
        realmObject.setFieldBytesNull(newObject.getFieldBytesNull());
        realmObject.setFieldByteNotNull(newObject.getFieldByteNotNull() != null ? newObject.getFieldByteNotNull() : 0);
        realmObject.setFieldByteNull(newObject.getFieldByteNull());
        realmObject.setFieldShortNotNull(newObject.getFieldShortNotNull() != null ? newObject.getFieldShortNotNull() : 0);
        realmObject.setFieldShortNull(newObject.getFieldShortNull());
        realmObject.setFieldIntegerNotNull(newObject.getFieldIntegerNotNull() != null ? newObject.getFieldIntegerNotNull() : 0);
        realmObject.setFieldIntegerNull(newObject.getFieldIntegerNull());
        realmObject.setFieldLongNotNull(newObject.getFieldLongNotNull() != null ? newObject.getFieldLongNotNull() : 0);
        realmObject.setFieldLongNull(newObject.getFieldLongNull());
        realmObject.setFieldFloatNotNull(newObject.getFieldFloatNotNull() != null ? newObject.getFieldFloatNotNull() : 0);
        realmObject.setFieldFloatNull(newObject.getFieldFloatNull());
        realmObject.setFieldDoubleNotNull(newObject.getFieldDoubleNotNull() != null ? newObject.getFieldDoubleNotNull() : 0);
        realmObject.setFieldDoubleNull(newObject.getFieldDoubleNull());
        realmObject.setFieldDateNotNull(newObject.getFieldDateNotNull() != null ? newObject.getFieldDateNotNull() : new Date(0));
        realmObject.setFieldDateNull(newObject.getFieldDateNull());
        return realmObject;
    }

    static NullTypes update(Realm realm, NullTypes realmObject, NullTypes newObject, Map<RealmObject, RealmObjectProxy> cache) {
        realmObject.setFieldStringNotNull(newObject.getFieldStringNotNull() != null ? newObject.getFieldStringNotNull() : "");
        realmObject.setFieldStringNull(newObject.getFieldStringNull());
        realmObject.setFieldBooleanNotNull(newObject.getFieldBooleanNotNull() != null ? newObject.getFieldBooleanNotNull() : false);
        realmObject.setFieldBooleanNull(newObject.getFieldBooleanNull());
        realmObject.setFieldBytesNotNull(newObject.getFieldBytesNotNull() != null ? newObject.getFieldBytesNotNull() : new byte[0]);
        realmObject.setFieldBytesNull(newObject.getFieldBytesNull());
        realmObject.setFieldByteNotNull(newObject.getFieldByteNotNull() != null ? newObject.getFieldByteNotNull() : 0);
        realmObject.setFieldByteNull(newObject.getFieldByteNull());
        realmObject.setFieldShortNotNull(newObject.getFieldShortNotNull() != null ? newObject.getFieldShortNotNull() : 0);
        realmObject.setFieldShortNull(newObject.getFieldShortNull());
        realmObject.setFieldIntegerNotNull(newObject.getFieldIntegerNotNull() != null ? newObject.getFieldIntegerNotNull() : 0);
        realmObject.setFieldIntegerNull(newObject.getFieldIntegerNull());
        realmObject.setFieldLongNotNull(newObject.getFieldLongNotNull() != null ? newObject.getFieldLongNotNull() : 0);
        realmObject.setFieldLongNull(newObject.getFieldLongNull());
        realmObject.setFieldFloatNotNull(newObject.getFieldFloatNotNull() != null ? newObject.getFieldFloatNotNull() : 0);
        realmObject.setFieldFloatNull(newObject.getFieldFloatNull());
        realmObject.setFieldDoubleNotNull(newObject.getFieldDoubleNotNull() != null ? newObject.getFieldDoubleNotNull() : 0);
        realmObject.setFieldDoubleNull(newObject.getFieldDoubleNull());
        realmObject.setFieldDateNotNull(newObject.getFieldDateNotNull() != null ? newObject.getFieldDateNotNull() : new Date(0));
        realmObject.setFieldDateNull(newObject.getFieldDateNull());
        return realmObject;
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("NullTypes = [");
        stringBuilder.append("{fieldStringNotNull:");
        stringBuilder.append(getFieldStringNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldStringNull:");
        stringBuilder.append(getFieldStringNull() != null ? getFieldStringNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBooleanNotNull:");
        stringBuilder.append(getFieldBooleanNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBooleanNull:");
        stringBuilder.append(getFieldBooleanNull() != null ? getFieldBooleanNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBytesNotNull:");
        stringBuilder.append(getFieldBytesNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBytesNull:");
        stringBuilder.append(getFieldBytesNull() != null ? getFieldBytesNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldByteNotNull:");
        stringBuilder.append(getFieldByteNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldByteNull:");
        stringBuilder.append(getFieldByteNull() != null ? getFieldByteNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldShortNotNull:");
        stringBuilder.append(getFieldShortNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldShortNull:");
        stringBuilder.append(getFieldShortNull() != null ? getFieldShortNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldIntegerNotNull:");
        stringBuilder.append(getFieldIntegerNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldIntegerNull:");
        stringBuilder.append(getFieldIntegerNull() != null ? getFieldIntegerNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldLongNotNull:");
        stringBuilder.append(getFieldLongNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldLongNull:");
        stringBuilder.append(getFieldLongNull() != null ? getFieldLongNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldFloatNotNull:");
        stringBuilder.append(getFieldFloatNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldFloatNull:");
        stringBuilder.append(getFieldFloatNull() != null ? getFieldFloatNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDoubleNotNull:");
        stringBuilder.append(getFieldDoubleNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDoubleNull:");
        stringBuilder.append(getFieldDoubleNull() != null ? getFieldDoubleNull() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDateNotNull:");
        stringBuilder.append(getFieldDateNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDateNull:");
        stringBuilder.append(getFieldDateNull() != null ? getFieldDateNull() : "null");
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
