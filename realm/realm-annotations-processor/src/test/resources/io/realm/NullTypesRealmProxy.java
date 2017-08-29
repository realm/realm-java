package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.LinkView;
import io.realm.internal.OsObject;
import io.realm.internal.OsObjectSchemaInfo;
import io.realm.internal.OsSchemaInfo;
import io.realm.internal.Property;
import io.realm.internal.ProxyUtils;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.SharedRealm;
import io.realm.internal.Table;
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

@SuppressWarnings("all")
public class NullTypesRealmProxy extends some.test.NullTypes
        implements RealmObjectProxy, NullTypesRealmProxyInterface {

    static final class NullTypesColumnInfo extends ColumnInfo {
        long fieldStringNotNullIndex;
        long fieldStringNullIndex;
        long fieldBooleanNotNullIndex;
        long fieldBooleanNullIndex;
        long fieldBytesNotNullIndex;
        long fieldBytesNullIndex;
        long fieldByteNotNullIndex;
        long fieldByteNullIndex;
        long fieldShortNotNullIndex;
        long fieldShortNullIndex;
        long fieldIntegerNotNullIndex;
        long fieldIntegerNullIndex;
        long fieldLongNotNullIndex;
        long fieldLongNullIndex;
        long fieldFloatNotNullIndex;
        long fieldFloatNullIndex;
        long fieldDoubleNotNullIndex;
        long fieldDoubleNullIndex;
        long fieldDateNotNullIndex;
        long fieldDateNullIndex;
        long fieldObjectNullIndex;

        NullTypesColumnInfo(OsSchemaInfo schemaInfo) {
            super(21);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("NullTypes");
            this.fieldStringNotNullIndex = addColumnDetails("fieldStringNotNull", objectSchemaInfo);
            this.fieldStringNullIndex = addColumnDetails("fieldStringNull", objectSchemaInfo);
            this.fieldBooleanNotNullIndex = addColumnDetails("fieldBooleanNotNull", objectSchemaInfo);
            this.fieldBooleanNullIndex = addColumnDetails("fieldBooleanNull", objectSchemaInfo);
            this.fieldBytesNotNullIndex = addColumnDetails("fieldBytesNotNull", objectSchemaInfo);
            this.fieldBytesNullIndex = addColumnDetails("fieldBytesNull", objectSchemaInfo);
            this.fieldByteNotNullIndex = addColumnDetails("fieldByteNotNull", objectSchemaInfo);
            this.fieldByteNullIndex = addColumnDetails("fieldByteNull", objectSchemaInfo);
            this.fieldShortNotNullIndex = addColumnDetails("fieldShortNotNull", objectSchemaInfo);
            this.fieldShortNullIndex = addColumnDetails("fieldShortNull", objectSchemaInfo);
            this.fieldIntegerNotNullIndex = addColumnDetails("fieldIntegerNotNull", objectSchemaInfo);
            this.fieldIntegerNullIndex = addColumnDetails("fieldIntegerNull", objectSchemaInfo);
            this.fieldLongNotNullIndex = addColumnDetails("fieldLongNotNull", objectSchemaInfo);
            this.fieldLongNullIndex = addColumnDetails("fieldLongNull", objectSchemaInfo);
            this.fieldFloatNotNullIndex = addColumnDetails("fieldFloatNotNull", objectSchemaInfo);
            this.fieldFloatNullIndex = addColumnDetails("fieldFloatNull", objectSchemaInfo);
            this.fieldDoubleNotNullIndex = addColumnDetails("fieldDoubleNotNull", objectSchemaInfo);
            this.fieldDoubleNullIndex = addColumnDetails("fieldDoubleNull", objectSchemaInfo);
            this.fieldDateNotNullIndex = addColumnDetails("fieldDateNotNull", objectSchemaInfo);
            this.fieldDateNullIndex = addColumnDetails("fieldDateNull", objectSchemaInfo);
            this.fieldObjectNullIndex = addColumnDetails("fieldObjectNull", objectSchemaInfo);
        }

        NullTypesColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new NullTypesColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final NullTypesColumnInfo src = (NullTypesColumnInfo) rawSrc;
            final NullTypesColumnInfo dst = (NullTypesColumnInfo) rawDst;
            dst.fieldStringNotNullIndex = src.fieldStringNotNullIndex;
            dst.fieldStringNullIndex = src.fieldStringNullIndex;
            dst.fieldBooleanNotNullIndex = src.fieldBooleanNotNullIndex;
            dst.fieldBooleanNullIndex = src.fieldBooleanNullIndex;
            dst.fieldBytesNotNullIndex = src.fieldBytesNotNullIndex;
            dst.fieldBytesNullIndex = src.fieldBytesNullIndex;
            dst.fieldByteNotNullIndex = src.fieldByteNotNullIndex;
            dst.fieldByteNullIndex = src.fieldByteNullIndex;
            dst.fieldShortNotNullIndex = src.fieldShortNotNullIndex;
            dst.fieldShortNullIndex = src.fieldShortNullIndex;
            dst.fieldIntegerNotNullIndex = src.fieldIntegerNotNullIndex;
            dst.fieldIntegerNullIndex = src.fieldIntegerNullIndex;
            dst.fieldLongNotNullIndex = src.fieldLongNotNullIndex;
            dst.fieldLongNullIndex = src.fieldLongNullIndex;
            dst.fieldFloatNotNullIndex = src.fieldFloatNotNullIndex;
            dst.fieldFloatNullIndex = src.fieldFloatNullIndex;
            dst.fieldDoubleNotNullIndex = src.fieldDoubleNotNullIndex;
            dst.fieldDoubleNullIndex = src.fieldDoubleNullIndex;
            dst.fieldDateNotNullIndex = src.fieldDateNotNullIndex;
            dst.fieldDateNullIndex = src.fieldDateNullIndex;
            dst.fieldObjectNullIndex = src.fieldObjectNullIndex;
        }
    }

    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();
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

    private NullTypesColumnInfo columnInfo;
    private ProxyState<some.test.NullTypes> proxyState;

    NullTypesRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (NullTypesColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<some.test.NullTypes>(this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$fieldStringNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.fieldStringNotNullIndex);
    }

    @Override
    public void realmSet$fieldStringNotNull(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'fieldStringNotNull' to null.");
            }
            row.getTable().setString(columnInfo.fieldStringNotNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldStringNotNull' to null.");
        }
        proxyState.getRow$realm().setString(columnInfo.fieldStringNotNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$fieldStringNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.fieldStringNullIndex);
    }

    @Override
    public void realmSet$fieldStringNull(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldStringNullIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setString(columnInfo.fieldStringNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldStringNullIndex);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.fieldStringNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Boolean realmGet$fieldBooleanNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.fieldBooleanNotNullIndex);
    }

    @Override
    public void realmSet$fieldBooleanNotNull(Boolean value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'fieldBooleanNotNull' to null.");
            }
            row.getTable().setBoolean(columnInfo.fieldBooleanNotNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldBooleanNotNull' to null.");
        }
        proxyState.getRow$realm().setBoolean(columnInfo.fieldBooleanNotNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Boolean realmGet$fieldBooleanNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldBooleanNullIndex)) {
            return null;
        }
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.fieldBooleanNullIndex);
    }

    @Override
    public void realmSet$fieldBooleanNull(Boolean value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldBooleanNullIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setBoolean(columnInfo.fieldBooleanNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldBooleanNullIndex);
            return;
        }
        proxyState.getRow$realm().setBoolean(columnInfo.fieldBooleanNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public byte[] realmGet$fieldBytesNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (byte[]) proxyState.getRow$realm().getBinaryByteArray(columnInfo.fieldBytesNotNullIndex);
    }

    @Override
    public void realmSet$fieldBytesNotNull(byte[] value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'fieldBytesNotNull' to null.");
            }
            row.getTable().setBinaryByteArray(columnInfo.fieldBytesNotNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldBytesNotNull' to null.");
        }
        proxyState.getRow$realm().setBinaryByteArray(columnInfo.fieldBytesNotNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public byte[] realmGet$fieldBytesNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (byte[]) proxyState.getRow$realm().getBinaryByteArray(columnInfo.fieldBytesNullIndex);
    }

    @Override
    public void realmSet$fieldBytesNull(byte[] value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldBytesNullIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setBinaryByteArray(columnInfo.fieldBytesNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldBytesNullIndex);
            return;
        }
        proxyState.getRow$realm().setBinaryByteArray(columnInfo.fieldBytesNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Byte realmGet$fieldByteNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (byte) proxyState.getRow$realm().getLong(columnInfo.fieldByteNotNullIndex);
    }

    @Override
    public void realmSet$fieldByteNotNull(Byte value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'fieldByteNotNull' to null.");
            }
            row.getTable().setLong(columnInfo.fieldByteNotNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldByteNotNull' to null.");
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldByteNotNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Byte realmGet$fieldByteNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldByteNullIndex)) {
            return null;
        }
        return (byte) proxyState.getRow$realm().getLong(columnInfo.fieldByteNullIndex);
    }

    @Override
    public void realmSet$fieldByteNull(Byte value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldByteNullIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setLong(columnInfo.fieldByteNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldByteNullIndex);
            return;
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldByteNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Short realmGet$fieldShortNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (short) proxyState.getRow$realm().getLong(columnInfo.fieldShortNotNullIndex);
    }

    @Override
    public void realmSet$fieldShortNotNull(Short value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'fieldShortNotNull' to null.");
            }
            row.getTable().setLong(columnInfo.fieldShortNotNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldShortNotNull' to null.");
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldShortNotNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Short realmGet$fieldShortNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldShortNullIndex)) {
            return null;
        }
        return (short) proxyState.getRow$realm().getLong(columnInfo.fieldShortNullIndex);
    }

    @Override
    public void realmSet$fieldShortNull(Short value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldShortNullIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setLong(columnInfo.fieldShortNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldShortNullIndex);
            return;
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldShortNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Integer realmGet$fieldIntegerNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.fieldIntegerNotNullIndex);
    }

    @Override
    public void realmSet$fieldIntegerNotNull(Integer value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'fieldIntegerNotNull' to null.");
            }
            row.getTable().setLong(columnInfo.fieldIntegerNotNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldIntegerNotNull' to null.");
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldIntegerNotNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Integer realmGet$fieldIntegerNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldIntegerNullIndex)) {
            return null;
        }
        return (int) proxyState.getRow$realm().getLong(columnInfo.fieldIntegerNullIndex);
    }

    @Override
    public void realmSet$fieldIntegerNull(Integer value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldIntegerNullIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setLong(columnInfo.fieldIntegerNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldIntegerNullIndex);
            return;
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldIntegerNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Long realmGet$fieldLongNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (long) proxyState.getRow$realm().getLong(columnInfo.fieldLongNotNullIndex);
    }

    @Override
    public void realmSet$fieldLongNotNull(Long value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'fieldLongNotNull' to null.");
            }
            row.getTable().setLong(columnInfo.fieldLongNotNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldLongNotNull' to null.");
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldLongNotNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Long realmGet$fieldLongNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldLongNullIndex)) {
            return null;
        }
        return (long) proxyState.getRow$realm().getLong(columnInfo.fieldLongNullIndex);
    }

    @Override
    public void realmSet$fieldLongNull(Long value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldLongNullIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setLong(columnInfo.fieldLongNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldLongNullIndex);
            return;
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldLongNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Float realmGet$fieldFloatNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.fieldFloatNotNullIndex);
    }

    @Override
    public void realmSet$fieldFloatNotNull(Float value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'fieldFloatNotNull' to null.");
            }
            row.getTable().setFloat(columnInfo.fieldFloatNotNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldFloatNotNull' to null.");
        }
        proxyState.getRow$realm().setFloat(columnInfo.fieldFloatNotNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Float realmGet$fieldFloatNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldFloatNullIndex)) {
            return null;
        }
        return (float) proxyState.getRow$realm().getFloat(columnInfo.fieldFloatNullIndex);
    }

    @Override
    public void realmSet$fieldFloatNull(Float value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldFloatNullIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setFloat(columnInfo.fieldFloatNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldFloatNullIndex);
            return;
        }
        proxyState.getRow$realm().setFloat(columnInfo.fieldFloatNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Double realmGet$fieldDoubleNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (double) proxyState.getRow$realm().getDouble(columnInfo.fieldDoubleNotNullIndex);
    }

    @Override
    public void realmSet$fieldDoubleNotNull(Double value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'fieldDoubleNotNull' to null.");
            }
            row.getTable().setDouble(columnInfo.fieldDoubleNotNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldDoubleNotNull' to null.");
        }
        proxyState.getRow$realm().setDouble(columnInfo.fieldDoubleNotNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Double realmGet$fieldDoubleNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldDoubleNullIndex)) {
            return null;
        }
        return (double) proxyState.getRow$realm().getDouble(columnInfo.fieldDoubleNullIndex);
    }

    @Override
    public void realmSet$fieldDoubleNull(Double value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldDoubleNullIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setDouble(columnInfo.fieldDoubleNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldDoubleNullIndex);
            return;
        }
        proxyState.getRow$realm().setDouble(columnInfo.fieldDoubleNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Date realmGet$fieldDateNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.util.Date) proxyState.getRow$realm().getDate(columnInfo.fieldDateNotNullIndex);
    }

    @Override
    public void realmSet$fieldDateNotNull(Date value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'fieldDateNotNull' to null.");
            }
            row.getTable().setDate(columnInfo.fieldDateNotNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldDateNotNull' to null.");
        }
        proxyState.getRow$realm().setDate(columnInfo.fieldDateNotNullIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Date realmGet$fieldDateNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldDateNullIndex)) {
            return null;
        }
        return (java.util.Date) proxyState.getRow$realm().getDate(columnInfo.fieldDateNullIndex);
    }

    @Override
    public void realmSet$fieldDateNull(Date value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldDateNullIndex, row.getIndex(), true);
                return;
            }
            row.getTable().setDate(columnInfo.fieldDateNullIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldDateNullIndex);
            return;
        }
        proxyState.getRow$realm().setDate(columnInfo.fieldDateNullIndex, value);
    }

    @Override
    public some.test.NullTypes realmGet$fieldObjectNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNullLink(columnInfo.fieldObjectNullIndex)) {
            return null;
        }
        return proxyState.getRealm$realm().get(some.test.NullTypes.class, proxyState.getRow$realm().getLink(columnInfo.fieldObjectNullIndex), false, Collections.<String>emptyList());
    }

    @Override
    public void realmSet$fieldObjectNull(some.test.NullTypes value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldObjectNull")) {
                return;
            }
            if (value != null && !RealmObject.isManaged(value)) {
                value = ((Realm) proxyState.getRealm$realm()).copyToRealm(value);
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                // Table#nullifyLink() does not support default value. Just using Row.
                row.nullifyLink(columnInfo.fieldObjectNullIndex);
                return;
            }
            if (!RealmObject.isValid(value)) {
                throw new IllegalArgumentException("'value' is not a valid managed object.");
            }
            if (((RealmObjectProxy) value).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm()) {
                throw new IllegalArgumentException("'value' belongs to a different Realm.");
            }
            row.getTable().setLink(columnInfo.fieldObjectNullIndex, row.getIndex(), ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getIndex(), true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().nullifyLink(columnInfo.fieldObjectNullIndex);
            return;
        }
        if (!(RealmObject.isManaged(value) && RealmObject.isValid(value))) {
            throw new IllegalArgumentException("'value' is not a valid managed object.");
        }
        if (((RealmObjectProxy) value).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm()) {
            throw new IllegalArgumentException("'value' belongs to a different Realm.");
        }
        proxyState.getRow$realm().setLink(columnInfo.fieldObjectNullIndex, ((RealmObjectProxy)value).realmGet$proxyState().getRow$realm().getIndex());
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("NullTypes");
        builder.addPersistedProperty("fieldStringNotNull", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("fieldStringNull", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("fieldBooleanNotNull", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("fieldBooleanNull", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("fieldBytesNotNull", RealmFieldType.BINARY, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("fieldBytesNull", RealmFieldType.BINARY, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("fieldByteNotNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("fieldByteNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("fieldShortNotNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("fieldShortNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("fieldIntegerNotNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("fieldIntegerNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("fieldLongNotNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("fieldLongNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("fieldFloatNotNull", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("fieldFloatNull", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("fieldDoubleNotNull", RealmFieldType.DOUBLE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("fieldDoubleNull", RealmFieldType.DOUBLE, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("fieldDateNotNull", RealmFieldType.DATE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("fieldDateNull", RealmFieldType.DATE, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedLinkProperty("fieldObjectNull", RealmFieldType.OBJECT, "NullTypes");
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static NullTypesColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new NullTypesColumnInfo(schemaInfo);
    }

    public static String getTableName() {
        return "class_NullTypes";
    }

    public static List<String> getFieldNames() {
        return FIELD_NAMES;
    }

    @SuppressWarnings("cast")
    public static some.test.NullTypes createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        final List<String> excludeFields = new ArrayList<String>(1);
        if (json.has("fieldObjectNull")) {
            excludeFields.add("fieldObjectNull");
        }
        some.test.NullTypes obj = realm.createObjectInternal(some.test.NullTypes.class, true, excludeFields);
        final NullTypesRealmProxyInterface objProxy = (NullTypesRealmProxyInterface) obj;
        if (json.has("fieldStringNotNull")) {
            if (json.isNull("fieldStringNotNull")) {
                objProxy.realmSet$fieldStringNotNull(null);
            } else {
                objProxy.realmSet$fieldStringNotNull((String) json.getString("fieldStringNotNull"));
            }
        }
        if (json.has("fieldStringNull")) {
            if (json.isNull("fieldStringNull")) {
                objProxy.realmSet$fieldStringNull(null);
            } else {
                objProxy.realmSet$fieldStringNull((String) json.getString("fieldStringNull"));
            }
        }
        if (json.has("fieldBooleanNotNull")) {
            if (json.isNull("fieldBooleanNotNull")) {
                objProxy.realmSet$fieldBooleanNotNull(null);
            } else {
                objProxy.realmSet$fieldBooleanNotNull((boolean) json.getBoolean("fieldBooleanNotNull"));
            }
        }
        if (json.has("fieldBooleanNull")) {
            if (json.isNull("fieldBooleanNull")) {
                objProxy.realmSet$fieldBooleanNull(null);
            } else {
                objProxy.realmSet$fieldBooleanNull((boolean) json.getBoolean("fieldBooleanNull"));
            }
        }
        if (json.has("fieldBytesNotNull")) {
            if (json.isNull("fieldBytesNotNull")) {
                objProxy.realmSet$fieldBytesNotNull(null);
            } else {
                objProxy.realmSet$fieldBytesNotNull(JsonUtils.stringToBytes(json.getString("fieldBytesNotNull")));
            }
        }
        if (json.has("fieldBytesNull")) {
            if (json.isNull("fieldBytesNull")) {
                objProxy.realmSet$fieldBytesNull(null);
            } else {
                objProxy.realmSet$fieldBytesNull(JsonUtils.stringToBytes(json.getString("fieldBytesNull")));
            }
        }
        if (json.has("fieldByteNotNull")) {
            if (json.isNull("fieldByteNotNull")) {
                objProxy.realmSet$fieldByteNotNull(null);
            } else {
                objProxy.realmSet$fieldByteNotNull((byte) json.getInt("fieldByteNotNull"));
            }
        }
        if (json.has("fieldByteNull")) {
            if (json.isNull("fieldByteNull")) {
                objProxy.realmSet$fieldByteNull(null);
            } else {
                objProxy.realmSet$fieldByteNull((byte) json.getInt("fieldByteNull"));
            }
        }
        if (json.has("fieldShortNotNull")) {
            if (json.isNull("fieldShortNotNull")) {
                objProxy.realmSet$fieldShortNotNull(null);
            } else {
                objProxy.realmSet$fieldShortNotNull((short) json.getInt("fieldShortNotNull"));
            }
        }
        if (json.has("fieldShortNull")) {
            if (json.isNull("fieldShortNull")) {
                objProxy.realmSet$fieldShortNull(null);
            } else {
                objProxy.realmSet$fieldShortNull((short) json.getInt("fieldShortNull"));
            }
        }
        if (json.has("fieldIntegerNotNull")) {
            if (json.isNull("fieldIntegerNotNull")) {
                objProxy.realmSet$fieldIntegerNotNull(null);
            } else {
                objProxy.realmSet$fieldIntegerNotNull((int) json.getInt("fieldIntegerNotNull"));
            }
        }
        if (json.has("fieldIntegerNull")) {
            if (json.isNull("fieldIntegerNull")) {
                objProxy.realmSet$fieldIntegerNull(null);
            } else {
                objProxy.realmSet$fieldIntegerNull((int) json.getInt("fieldIntegerNull"));
            }
        }
        if (json.has("fieldLongNotNull")) {
            if (json.isNull("fieldLongNotNull")) {
                objProxy.realmSet$fieldLongNotNull(null);
            } else {
                objProxy.realmSet$fieldLongNotNull((long) json.getLong("fieldLongNotNull"));
            }
        }
        if (json.has("fieldLongNull")) {
            if (json.isNull("fieldLongNull")) {
                objProxy.realmSet$fieldLongNull(null);
            } else {
                objProxy.realmSet$fieldLongNull((long) json.getLong("fieldLongNull"));
            }
        }
        if (json.has("fieldFloatNotNull")) {
            if (json.isNull("fieldFloatNotNull")) {
                objProxy.realmSet$fieldFloatNotNull(null);
            } else {
                objProxy.realmSet$fieldFloatNotNull((float) json.getDouble("fieldFloatNotNull"));
            }
        }
        if (json.has("fieldFloatNull")) {
            if (json.isNull("fieldFloatNull")) {
                objProxy.realmSet$fieldFloatNull(null);
            } else {
                objProxy.realmSet$fieldFloatNull((float) json.getDouble("fieldFloatNull"));
            }
        }
        if (json.has("fieldDoubleNotNull")) {
            if (json.isNull("fieldDoubleNotNull")) {
                objProxy.realmSet$fieldDoubleNotNull(null);
            } else {
                objProxy.realmSet$fieldDoubleNotNull((double) json.getDouble("fieldDoubleNotNull"));
            }
        }
        if (json.has("fieldDoubleNull")) {
            if (json.isNull("fieldDoubleNull")) {
                objProxy.realmSet$fieldDoubleNull(null);
            } else {
                objProxy.realmSet$fieldDoubleNull((double) json.getDouble("fieldDoubleNull"));
            }
        }
        if (json.has("fieldDateNotNull")) {
            if (json.isNull("fieldDateNotNull")) {
                objProxy.realmSet$fieldDateNotNull(null);
            } else {
                Object timestamp = json.get("fieldDateNotNull");
                if (timestamp instanceof String) {
                    objProxy.realmSet$fieldDateNotNull(JsonUtils.stringToDate((String) timestamp));
                } else {
                    objProxy.realmSet$fieldDateNotNull(new Date(json.getLong("fieldDateNotNull")));
                }
            }
        }
        if (json.has("fieldDateNull")) {
            if (json.isNull("fieldDateNull")) {
                objProxy.realmSet$fieldDateNull(null);
            } else {
                Object timestamp = json.get("fieldDateNull");
                if (timestamp instanceof String) {
                    objProxy.realmSet$fieldDateNull(JsonUtils.stringToDate((String) timestamp));
                } else {
                    objProxy.realmSet$fieldDateNull(new Date(json.getLong("fieldDateNull")));
                }
            }
        }
        if (json.has("fieldObjectNull")) {
            if (json.isNull("fieldObjectNull")) {
                objProxy.realmSet$fieldObjectNull(null);
            } else {
                some.test.NullTypes fieldObjectNullObj = NullTypesRealmProxy.createOrUpdateUsingJsonObject(realm, json.getJSONObject("fieldObjectNull"), update);
                objProxy.realmSet$fieldObjectNull(fieldObjectNullObj);
            }
        }
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.NullTypes createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        final some.test.NullTypes obj = new some.test.NullTypes();
        final NullTypesRealmProxyInterface objProxy = (NullTypesRealmProxyInterface) obj;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (false) {
            } else if (name.equals("fieldStringNotNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldStringNotNull((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldStringNotNull(null);
                }
            } else if (name.equals("fieldStringNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldStringNull((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldStringNull(null);
                }
            } else if (name.equals("fieldBooleanNotNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldBooleanNotNull((boolean) reader.nextBoolean());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldBooleanNotNull(null);
                }
            } else if (name.equals("fieldBooleanNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldBooleanNull((boolean) reader.nextBoolean());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldBooleanNull(null);
                }
            } else if (name.equals("fieldBytesNotNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldBytesNotNull(JsonUtils.stringToBytes(reader.nextString()));
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldBytesNotNull(null);
                }
            } else if (name.equals("fieldBytesNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldBytesNull(JsonUtils.stringToBytes(reader.nextString()));
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldBytesNull(null);
                }
            } else if (name.equals("fieldByteNotNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldByteNotNull((byte) reader.nextInt());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldByteNotNull(null);
                }
            } else if (name.equals("fieldByteNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldByteNull((byte) reader.nextInt());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldByteNull(null);
                }
            } else if (name.equals("fieldShortNotNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldShortNotNull((short) reader.nextInt());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldShortNotNull(null);
                }
            } else if (name.equals("fieldShortNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldShortNull((short) reader.nextInt());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldShortNull(null);
                }
            } else if (name.equals("fieldIntegerNotNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldIntegerNotNull((int) reader.nextInt());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldIntegerNotNull(null);
                }
            } else if (name.equals("fieldIntegerNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldIntegerNull((int) reader.nextInt());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldIntegerNull(null);
                }
            } else if (name.equals("fieldLongNotNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldLongNotNull((long) reader.nextLong());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldLongNotNull(null);
                }
            } else if (name.equals("fieldLongNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldLongNull((long) reader.nextLong());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldLongNull(null);
                }
            } else if (name.equals("fieldFloatNotNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldFloatNotNull((float) reader.nextDouble());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldFloatNotNull(null);
                }
            } else if (name.equals("fieldFloatNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldFloatNull((float) reader.nextDouble());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldFloatNull(null);
                }
            } else if (name.equals("fieldDoubleNotNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldDoubleNotNull((double) reader.nextDouble());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldDoubleNotNull(null);
                }
            } else if (name.equals("fieldDoubleNull")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$fieldDoubleNull((double) reader.nextDouble());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$fieldDoubleNull(null);
                }
            } else if (name.equals("fieldDateNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$fieldDateNotNull(null);
                } else if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        objProxy.realmSet$fieldDateNotNull(new Date(timestamp));
                    }
                } else {
                    objProxy.realmSet$fieldDateNotNull(JsonUtils.stringToDate(reader.nextString()));
                }
            } else if (name.equals("fieldDateNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$fieldDateNull(null);
                } else if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        objProxy.realmSet$fieldDateNull(new Date(timestamp));
                    }
                } else {
                    objProxy.realmSet$fieldDateNull(JsonUtils.stringToDate(reader.nextString()));
                }
            } else if (name.equals("fieldObjectNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$fieldObjectNull(null);
                } else {
                    some.test.NullTypes fieldObjectNullObj = NullTypesRealmProxy.createUsingJsonStream(realm, reader);
                    objProxy.realmSet$fieldObjectNull(fieldObjectNullObj);
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return realm.copyToRealm(obj);
    }

    public static some.test.NullTypes copyOrUpdate(Realm realm, some.test.NullTypes object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null) {
            final BaseRealm otherRealm = ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm();
            if (otherRealm.threadId != realm.threadId) {
                throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
            }
            if (otherRealm.getPath().equals(realm.getPath())) {
                return object;
            }
        }
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (some.test.NullTypes) cachedRealmObject;
        }

        return copy(realm, object, update, cache);
    }

    public static some.test.NullTypes copy(Realm realm, some.test.NullTypes newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.NullTypes) cachedRealmObject;
        }

        // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
        some.test.NullTypes realmObject = realm.createObjectInternal(some.test.NullTypes.class, false, Collections.<String>emptyList());
        cache.put(newObject, (RealmObjectProxy) realmObject);

        NullTypesRealmProxyInterface realmObjectSource = (NullTypesRealmProxyInterface) newObject;
        NullTypesRealmProxyInterface realmObjectCopy = (NullTypesRealmProxyInterface) realmObject;

        realmObjectCopy.realmSet$fieldStringNotNull(realmObjectSource.realmGet$fieldStringNotNull());
        realmObjectCopy.realmSet$fieldStringNull(realmObjectSource.realmGet$fieldStringNull());
        realmObjectCopy.realmSet$fieldBooleanNotNull(realmObjectSource.realmGet$fieldBooleanNotNull());
        realmObjectCopy.realmSet$fieldBooleanNull(realmObjectSource.realmGet$fieldBooleanNull());
        realmObjectCopy.realmSet$fieldBytesNotNull(realmObjectSource.realmGet$fieldBytesNotNull());
        realmObjectCopy.realmSet$fieldBytesNull(realmObjectSource.realmGet$fieldBytesNull());
        realmObjectCopy.realmSet$fieldByteNotNull(realmObjectSource.realmGet$fieldByteNotNull());
        realmObjectCopy.realmSet$fieldByteNull(realmObjectSource.realmGet$fieldByteNull());
        realmObjectCopy.realmSet$fieldShortNotNull(realmObjectSource.realmGet$fieldShortNotNull());
        realmObjectCopy.realmSet$fieldShortNull(realmObjectSource.realmGet$fieldShortNull());
        realmObjectCopy.realmSet$fieldIntegerNotNull(realmObjectSource.realmGet$fieldIntegerNotNull());
        realmObjectCopy.realmSet$fieldIntegerNull(realmObjectSource.realmGet$fieldIntegerNull());
        realmObjectCopy.realmSet$fieldLongNotNull(realmObjectSource.realmGet$fieldLongNotNull());
        realmObjectCopy.realmSet$fieldLongNull(realmObjectSource.realmGet$fieldLongNull());
        realmObjectCopy.realmSet$fieldFloatNotNull(realmObjectSource.realmGet$fieldFloatNotNull());
        realmObjectCopy.realmSet$fieldFloatNull(realmObjectSource.realmGet$fieldFloatNull());
        realmObjectCopy.realmSet$fieldDoubleNotNull(realmObjectSource.realmGet$fieldDoubleNotNull());
        realmObjectCopy.realmSet$fieldDoubleNull(realmObjectSource.realmGet$fieldDoubleNull());
        realmObjectCopy.realmSet$fieldDateNotNull(realmObjectSource.realmGet$fieldDateNotNull());
        realmObjectCopy.realmSet$fieldDateNull(realmObjectSource.realmGet$fieldDateNull());

        some.test.NullTypes fieldObjectNullObj = realmObjectSource.realmGet$fieldObjectNull();
        if (fieldObjectNullObj == null) {
            realmObjectCopy.realmSet$fieldObjectNull(null);
        } else {
            some.test.NullTypes cachefieldObjectNull = (some.test.NullTypes) cache.get(fieldObjectNullObj);
            if (cachefieldObjectNull != null) {
                realmObjectCopy.realmSet$fieldObjectNull(cachefieldObjectNull);
            } else {
                realmObjectCopy.realmSet$fieldObjectNull(NullTypesRealmProxy.copyOrUpdate(realm, fieldObjectNullObj, update, cache));
            }
        }
        return realmObject;
    }

    public static long insert(Realm realm, some.test.NullTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.NullTypes.class);
        long tableNativePtr = table.getNativePtr();
        NullTypesColumnInfo columnInfo = (NullTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.NullTypes.class);
        long rowIndex = OsObject.createRow(table);
        cache.put(object, rowIndex);
        String realmGet$fieldStringNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldStringNotNull();
        if (realmGet$fieldStringNotNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, realmGet$fieldStringNotNull, false);
        }
        String realmGet$fieldStringNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldStringNull();
        if (realmGet$fieldStringNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, realmGet$fieldStringNull, false);
        }
        Boolean realmGet$fieldBooleanNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNotNull();
        if (realmGet$fieldBooleanNotNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, realmGet$fieldBooleanNotNull, false);
        }
        Boolean realmGet$fieldBooleanNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNull();
        if (realmGet$fieldBooleanNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, realmGet$fieldBooleanNull, false);
        }
        byte[] realmGet$fieldBytesNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBytesNotNull();
        if (realmGet$fieldBytesNotNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, realmGet$fieldBytesNotNull, false);
        }
        byte[] realmGet$fieldBytesNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBytesNull();
        if (realmGet$fieldBytesNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, realmGet$fieldBytesNull, false);
        }
        Number realmGet$fieldByteNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldByteNotNull();
        if (realmGet$fieldByteNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, realmGet$fieldByteNotNull.longValue(), false);
        }
        Number realmGet$fieldByteNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldByteNull();
        if (realmGet$fieldByteNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, realmGet$fieldByteNull.longValue(), false);
        }
        Number realmGet$fieldShortNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldShortNotNull();
        if (realmGet$fieldShortNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, realmGet$fieldShortNotNull.longValue(), false);
        }
        Number realmGet$fieldShortNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldShortNull();
        if (realmGet$fieldShortNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, realmGet$fieldShortNull.longValue(), false);
        }
        Number realmGet$fieldIntegerNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNotNull();
        if (realmGet$fieldIntegerNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, realmGet$fieldIntegerNotNull.longValue(), false);
        }
        Number realmGet$fieldIntegerNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNull();
        if (realmGet$fieldIntegerNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, realmGet$fieldIntegerNull.longValue(), false);
        }
        Number realmGet$fieldLongNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldLongNotNull();
        if (realmGet$fieldLongNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, realmGet$fieldLongNotNull.longValue(), false);
        }
        Number realmGet$fieldLongNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldLongNull();
        if (realmGet$fieldLongNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, realmGet$fieldLongNull.longValue(), false);
        }
        Float realmGet$fieldFloatNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldFloatNotNull();
        if (realmGet$fieldFloatNotNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, realmGet$fieldFloatNotNull, false);
        }
        Float realmGet$fieldFloatNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldFloatNull();
        if (realmGet$fieldFloatNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, realmGet$fieldFloatNull, false);
        }
        Double realmGet$fieldDoubleNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNotNull();
        if (realmGet$fieldDoubleNotNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, realmGet$fieldDoubleNotNull, false);
        }
        Double realmGet$fieldDoubleNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNull();
        if (realmGet$fieldDoubleNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, realmGet$fieldDoubleNull, false);
        }
        java.util.Date realmGet$fieldDateNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDateNotNull();
        if (realmGet$fieldDateNotNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, realmGet$fieldDateNotNull.getTime(), false);
        }
        java.util.Date realmGet$fieldDateNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDateNull();
        if (realmGet$fieldDateNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNullIndex, rowIndex, realmGet$fieldDateNull.getTime(), false);
        }

        some.test.NullTypes fieldObjectNullObj = ((NullTypesRealmProxyInterface) object).realmGet$fieldObjectNull();
        if (fieldObjectNullObj != null) {
            Long cachefieldObjectNull = cache.get(fieldObjectNullObj);
            if (cachefieldObjectNull == null) {
                cachefieldObjectNull = NullTypesRealmProxy.insert(realm, fieldObjectNullObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.fieldObjectNullIndex, rowIndex, cachefieldObjectNull, false);
        }
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.NullTypes.class);
        long tableNativePtr = table.getNativePtr();
        NullTypesColumnInfo columnInfo = (NullTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.NullTypes.class);
        some.test.NullTypes object = null;
        while (objects.hasNext()) {
            object = (some.test.NullTypes) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            long rowIndex = OsObject.createRow(table);
            cache.put(object, rowIndex);
            String realmGet$fieldStringNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldStringNotNull();
            if (realmGet$fieldStringNotNull != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, realmGet$fieldStringNotNull, false);
            }
            String realmGet$fieldStringNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldStringNull();
            if (realmGet$fieldStringNull != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, realmGet$fieldStringNull, false);
            }
            Boolean realmGet$fieldBooleanNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNotNull();
            if (realmGet$fieldBooleanNotNull != null) {
                Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, realmGet$fieldBooleanNotNull, false);
            }
            Boolean realmGet$fieldBooleanNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNull();
            if (realmGet$fieldBooleanNull != null) {
                Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, realmGet$fieldBooleanNull, false);
            }
            byte[] realmGet$fieldBytesNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBytesNotNull();
            if (realmGet$fieldBytesNotNull != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, realmGet$fieldBytesNotNull, false);
            }
            byte[] realmGet$fieldBytesNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBytesNull();
            if (realmGet$fieldBytesNull != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, realmGet$fieldBytesNull, false);
            }
            Number realmGet$fieldByteNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldByteNotNull();
            if (realmGet$fieldByteNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, realmGet$fieldByteNotNull.longValue(), false);
            }
            Number realmGet$fieldByteNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldByteNull();
            if (realmGet$fieldByteNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, realmGet$fieldByteNull.longValue(), false);
            }
            Number realmGet$fieldShortNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldShortNotNull();
            if (realmGet$fieldShortNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, realmGet$fieldShortNotNull.longValue(), false);
            }
            Number realmGet$fieldShortNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldShortNull();
            if (realmGet$fieldShortNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, realmGet$fieldShortNull.longValue(), false);
            }
            Number realmGet$fieldIntegerNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNotNull();
            if (realmGet$fieldIntegerNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, realmGet$fieldIntegerNotNull.longValue(), false);
            }
            Number realmGet$fieldIntegerNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNull();
            if (realmGet$fieldIntegerNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, realmGet$fieldIntegerNull.longValue(), false);
            }
            Number realmGet$fieldLongNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldLongNotNull();
            if (realmGet$fieldLongNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, realmGet$fieldLongNotNull.longValue(), false);
            }
            Number realmGet$fieldLongNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldLongNull();
            if (realmGet$fieldLongNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, realmGet$fieldLongNull.longValue(), false);
            }
            Float realmGet$fieldFloatNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldFloatNotNull();
            if (realmGet$fieldFloatNotNull != null) {
                Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, realmGet$fieldFloatNotNull, false);
            }
            Float realmGet$fieldFloatNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldFloatNull();
            if (realmGet$fieldFloatNull != null) {
                Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, realmGet$fieldFloatNull, false);
            }
            Double realmGet$fieldDoubleNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNotNull();
            if (realmGet$fieldDoubleNotNull != null) {
                Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, realmGet$fieldDoubleNotNull, false);
            }
            Double realmGet$fieldDoubleNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNull();
            if (realmGet$fieldDoubleNull != null) {
                Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, realmGet$fieldDoubleNull, false);
            }
            java.util.Date realmGet$fieldDateNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDateNotNull();
            if (realmGet$fieldDateNotNull != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, realmGet$fieldDateNotNull.getTime(), false);
            }
            java.util.Date realmGet$fieldDateNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDateNull();
            if (realmGet$fieldDateNull != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNullIndex, rowIndex, realmGet$fieldDateNull.getTime(), false);
            }

            some.test.NullTypes fieldObjectNullObj = ((NullTypesRealmProxyInterface) object).realmGet$fieldObjectNull();
            if (fieldObjectNullObj != null) {
                Long cachefieldObjectNull = cache.get(fieldObjectNullObj);
                if (cachefieldObjectNull == null) {
                    cachefieldObjectNull = NullTypesRealmProxy.insert(realm, fieldObjectNullObj, cache);
                }
                table.setLink(columnInfo.fieldObjectNullIndex, rowIndex, cachefieldObjectNull, false);
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.NullTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.NullTypes.class);
        long tableNativePtr = table.getNativePtr();
        NullTypesColumnInfo columnInfo = (NullTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.NullTypes.class);
        long rowIndex = OsObject.createRow(table);
        cache.put(object, rowIndex);
        String realmGet$fieldStringNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldStringNotNull();
        if (realmGet$fieldStringNotNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, realmGet$fieldStringNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, false);
        }
        String realmGet$fieldStringNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldStringNull();
        if (realmGet$fieldStringNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, realmGet$fieldStringNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, false);
        }
        Boolean realmGet$fieldBooleanNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNotNull();
        if (realmGet$fieldBooleanNotNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, realmGet$fieldBooleanNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, false);
        }
        Boolean realmGet$fieldBooleanNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNull();
        if (realmGet$fieldBooleanNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, realmGet$fieldBooleanNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, false);
        }
        byte[] realmGet$fieldBytesNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBytesNotNull();
        if (realmGet$fieldBytesNotNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, realmGet$fieldBytesNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, false);
        }
        byte[] realmGet$fieldBytesNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBytesNull();
        if (realmGet$fieldBytesNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, realmGet$fieldBytesNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, false);
        }
        Number realmGet$fieldByteNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldByteNotNull();
        if (realmGet$fieldByteNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, realmGet$fieldByteNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, false);
        }
        Number realmGet$fieldByteNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldByteNull();
        if (realmGet$fieldByteNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, realmGet$fieldByteNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, false);
        }
        Number realmGet$fieldShortNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldShortNotNull();
        if (realmGet$fieldShortNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, realmGet$fieldShortNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, false);
        }
        Number realmGet$fieldShortNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldShortNull();
        if (realmGet$fieldShortNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, realmGet$fieldShortNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, false);
        }
        Number realmGet$fieldIntegerNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNotNull();
        if (realmGet$fieldIntegerNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, realmGet$fieldIntegerNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, false);
        }
        Number realmGet$fieldIntegerNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNull();
        if (realmGet$fieldIntegerNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, realmGet$fieldIntegerNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, false);
        }
        Number realmGet$fieldLongNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldLongNotNull();
        if (realmGet$fieldLongNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, realmGet$fieldLongNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, false);
        }
        Number realmGet$fieldLongNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldLongNull();
        if (realmGet$fieldLongNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, realmGet$fieldLongNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, false);
        }
        Float realmGet$fieldFloatNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldFloatNotNull();
        if (realmGet$fieldFloatNotNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, realmGet$fieldFloatNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, false);
        }
        Float realmGet$fieldFloatNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldFloatNull();
        if (realmGet$fieldFloatNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, realmGet$fieldFloatNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, false);
        }
        Double realmGet$fieldDoubleNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNotNull();
        if (realmGet$fieldDoubleNotNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, realmGet$fieldDoubleNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, false);
        }
        Double realmGet$fieldDoubleNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNull();
        if (realmGet$fieldDoubleNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, realmGet$fieldDoubleNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, false);
        }
        java.util.Date realmGet$fieldDateNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDateNotNull();
        if (realmGet$fieldDateNotNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, realmGet$fieldDateNotNull.getTime(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, false);
        }
        java.util.Date realmGet$fieldDateNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDateNull();
        if (realmGet$fieldDateNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNullIndex, rowIndex, realmGet$fieldDateNull.getTime(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNullIndex, rowIndex, false);
        }

        some.test.NullTypes fieldObjectNullObj = ((NullTypesRealmProxyInterface) object).realmGet$fieldObjectNull();
        if (fieldObjectNullObj != null) {
            Long cachefieldObjectNull = cache.get(fieldObjectNullObj);
            if (cachefieldObjectNull == null) {
                cachefieldObjectNull = NullTypesRealmProxy.insertOrUpdate(realm, fieldObjectNullObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.fieldObjectNullIndex, rowIndex, cachefieldObjectNull, false);
        } else {
            Table.nativeNullifyLink(tableNativePtr, columnInfo.fieldObjectNullIndex, rowIndex);
        }
        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.NullTypes.class);
        long tableNativePtr = table.getNativePtr();
        NullTypesColumnInfo columnInfo = (NullTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.NullTypes.class);
        some.test.NullTypes object = null;
        while (objects.hasNext()) {
            object = (some.test.NullTypes) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            long rowIndex = OsObject.createRow(table);
            cache.put(object, rowIndex);
            String realmGet$fieldStringNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldStringNotNull();
            if (realmGet$fieldStringNotNull != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, realmGet$fieldStringNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, false);
            }
            String realmGet$fieldStringNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldStringNull();
            if (realmGet$fieldStringNull != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, realmGet$fieldStringNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, false);
            }
            Boolean realmGet$fieldBooleanNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNotNull();
            if (realmGet$fieldBooleanNotNull != null) {
                Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, realmGet$fieldBooleanNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, false);
            }
            Boolean realmGet$fieldBooleanNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNull();
            if (realmGet$fieldBooleanNull != null) {
                Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, realmGet$fieldBooleanNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, false);
            }
            byte[] realmGet$fieldBytesNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBytesNotNull();
            if (realmGet$fieldBytesNotNull != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, realmGet$fieldBytesNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, false);
            }
            byte[] realmGet$fieldBytesNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldBytesNull();
            if (realmGet$fieldBytesNull != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, realmGet$fieldBytesNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, false);
            }
            Number realmGet$fieldByteNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldByteNotNull();
            if (realmGet$fieldByteNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, realmGet$fieldByteNotNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, false);
            }
            Number realmGet$fieldByteNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldByteNull();
            if (realmGet$fieldByteNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, realmGet$fieldByteNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, false);
            }
            Number realmGet$fieldShortNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldShortNotNull();
            if (realmGet$fieldShortNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, realmGet$fieldShortNotNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, false);
            }
            Number realmGet$fieldShortNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldShortNull();
            if (realmGet$fieldShortNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, realmGet$fieldShortNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, false);
            }
            Number realmGet$fieldIntegerNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNotNull();
            if (realmGet$fieldIntegerNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, realmGet$fieldIntegerNotNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, false);
            }
            Number realmGet$fieldIntegerNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNull();
            if (realmGet$fieldIntegerNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, realmGet$fieldIntegerNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, false);
            }
            Number realmGet$fieldLongNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldLongNotNull();
            if (realmGet$fieldLongNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, realmGet$fieldLongNotNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, false);
            }
            Number realmGet$fieldLongNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldLongNull();
            if (realmGet$fieldLongNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, realmGet$fieldLongNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, false);
            }
            Float realmGet$fieldFloatNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldFloatNotNull();
            if (realmGet$fieldFloatNotNull != null) {
                Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, realmGet$fieldFloatNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, false);
            }
            Float realmGet$fieldFloatNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldFloatNull();
            if (realmGet$fieldFloatNull != null) {
                Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, realmGet$fieldFloatNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, false);
            }
            Double realmGet$fieldDoubleNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNotNull();
            if (realmGet$fieldDoubleNotNull != null) {
                Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, realmGet$fieldDoubleNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, false);
            }
            Double realmGet$fieldDoubleNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNull();
            if (realmGet$fieldDoubleNull != null) {
                Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, realmGet$fieldDoubleNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, false);
            }
            java.util.Date realmGet$fieldDateNotNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDateNotNull();
            if (realmGet$fieldDateNotNull != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, realmGet$fieldDateNotNull.getTime(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, false);
            }
            java.util.Date realmGet$fieldDateNull = ((NullTypesRealmProxyInterface) object).realmGet$fieldDateNull();
            if (realmGet$fieldDateNull != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNullIndex, rowIndex, realmGet$fieldDateNull.getTime(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNullIndex, rowIndex, false);
            }

            some.test.NullTypes fieldObjectNullObj = ((NullTypesRealmProxyInterface) object).realmGet$fieldObjectNull();
            if (fieldObjectNullObj != null) {
                Long cachefieldObjectNull = cache.get(fieldObjectNullObj);
                if (cachefieldObjectNull == null) {
                    cachefieldObjectNull = NullTypesRealmProxy.insertOrUpdate(realm, fieldObjectNullObj, cache);
                }
                Table.nativeSetLink(tableNativePtr, columnInfo.fieldObjectNullIndex, rowIndex, cachefieldObjectNull, false);
            } else {
                Table.nativeNullifyLink(tableNativePtr, columnInfo.fieldObjectNullIndex, rowIndex);
            }
        }
    }

    public static some.test.NullTypes createDetachedCopy(some.test.NullTypes realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.NullTypes unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new some.test.NullTypes();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.NullTypes) cachedObject.object;
            }
            unmanagedObject = (some.test.NullTypes) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        NullTypesRealmProxyInterface unmanagedCopy = (NullTypesRealmProxyInterface) unmanagedObject;
        NullTypesRealmProxyInterface realmSource = (NullTypesRealmProxyInterface) realmObject;
        unmanagedCopy.realmSet$fieldStringNotNull(realmSource.realmGet$fieldStringNotNull());
        unmanagedCopy.realmSet$fieldStringNull(realmSource.realmGet$fieldStringNull());
        unmanagedCopy.realmSet$fieldBooleanNotNull(realmSource.realmGet$fieldBooleanNotNull());
        unmanagedCopy.realmSet$fieldBooleanNull(realmSource.realmGet$fieldBooleanNull());
        unmanagedCopy.realmSet$fieldBytesNotNull(realmSource.realmGet$fieldBytesNotNull());
        unmanagedCopy.realmSet$fieldBytesNull(realmSource.realmGet$fieldBytesNull());
        unmanagedCopy.realmSet$fieldByteNotNull(realmSource.realmGet$fieldByteNotNull());
        unmanagedCopy.realmSet$fieldByteNull(realmSource.realmGet$fieldByteNull());
        unmanagedCopy.realmSet$fieldShortNotNull(realmSource.realmGet$fieldShortNotNull());
        unmanagedCopy.realmSet$fieldShortNull(realmSource.realmGet$fieldShortNull());
        unmanagedCopy.realmSet$fieldIntegerNotNull(realmSource.realmGet$fieldIntegerNotNull());
        unmanagedCopy.realmSet$fieldIntegerNull(realmSource.realmGet$fieldIntegerNull());
        unmanagedCopy.realmSet$fieldLongNotNull(realmSource.realmGet$fieldLongNotNull());
        unmanagedCopy.realmSet$fieldLongNull(realmSource.realmGet$fieldLongNull());
        unmanagedCopy.realmSet$fieldFloatNotNull(realmSource.realmGet$fieldFloatNotNull());
        unmanagedCopy.realmSet$fieldFloatNull(realmSource.realmGet$fieldFloatNull());
        unmanagedCopy.realmSet$fieldDoubleNotNull(realmSource.realmGet$fieldDoubleNotNull());
        unmanagedCopy.realmSet$fieldDoubleNull(realmSource.realmGet$fieldDoubleNull());
        unmanagedCopy.realmSet$fieldDateNotNull(realmSource.realmGet$fieldDateNotNull());
        unmanagedCopy.realmSet$fieldDateNull(realmSource.realmGet$fieldDateNull());

        // Deep copy of fieldObjectNull
        unmanagedCopy.realmSet$fieldObjectNull(NullTypesRealmProxy.createDetachedCopy(realmSource.realmGet$fieldObjectNull(), currentDepth + 1, maxDepth, cache));
        return unmanagedObject;
    }

    @Override
    @SuppressWarnings("ArrayToString")
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("NullTypes = proxy[");
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
    public ProxyState<?> realmGet$proxyState() {
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
        NullTypesRealmProxy aNullTypes = (NullTypesRealmProxy)o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aNullTypes.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aNullTypes.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aNullTypes.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }

}
