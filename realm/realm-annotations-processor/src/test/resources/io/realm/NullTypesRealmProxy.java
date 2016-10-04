package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.LinkView;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Row;
import io.realm.internal.SharedRealm;
import io.realm.internal.Table;
import io.realm.internal.TableOrView;
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

public class NullTypesRealmProxy extends some.test.NullTypes
        implements RealmObjectProxy, NullTypesRealmProxyInterface {

    static final class NullTypesColumnInfo extends ColumnInfo
            implements Cloneable {

        public long fieldStringNotNullIndex;
        public long fieldStringNullIndex;
        public long fieldBooleanNotNullIndex;
        public long fieldBooleanNullIndex;
        public long fieldBytesNotNullIndex;
        public long fieldBytesNullIndex;
        public long fieldByteNotNullIndex;
        public long fieldByteNullIndex;
        public long fieldShortNotNullIndex;
        public long fieldShortNullIndex;
        public long fieldIntegerNotNullIndex;
        public long fieldIntegerNullIndex;
        public long fieldLongNotNullIndex;
        public long fieldLongNullIndex;
        public long fieldFloatNotNullIndex;
        public long fieldFloatNullIndex;
        public long fieldDoubleNotNullIndex;
        public long fieldDoubleNullIndex;
        public long fieldDateNotNullIndex;
        public long fieldDateNullIndex;
        public long fieldObjectNullIndex;

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

        @Override
        public final void copyColumnInfoFrom(ColumnInfo other) {
            final NullTypesColumnInfo otherInfo = (NullTypesColumnInfo) other;
            this.fieldStringNotNullIndex = otherInfo.fieldStringNotNullIndex;
            this.fieldStringNullIndex = otherInfo.fieldStringNullIndex;
            this.fieldBooleanNotNullIndex = otherInfo.fieldBooleanNotNullIndex;
            this.fieldBooleanNullIndex = otherInfo.fieldBooleanNullIndex;
            this.fieldBytesNotNullIndex = otherInfo.fieldBytesNotNullIndex;
            this.fieldBytesNullIndex = otherInfo.fieldBytesNullIndex;
            this.fieldByteNotNullIndex = otherInfo.fieldByteNotNullIndex;
            this.fieldByteNullIndex = otherInfo.fieldByteNullIndex;
            this.fieldShortNotNullIndex = otherInfo.fieldShortNotNullIndex;
            this.fieldShortNullIndex = otherInfo.fieldShortNullIndex;
            this.fieldIntegerNotNullIndex = otherInfo.fieldIntegerNotNullIndex;
            this.fieldIntegerNullIndex = otherInfo.fieldIntegerNullIndex;
            this.fieldLongNotNullIndex = otherInfo.fieldLongNotNullIndex;
            this.fieldLongNullIndex = otherInfo.fieldLongNullIndex;
            this.fieldFloatNotNullIndex = otherInfo.fieldFloatNotNullIndex;
            this.fieldFloatNullIndex = otherInfo.fieldFloatNullIndex;
            this.fieldDoubleNotNullIndex = otherInfo.fieldDoubleNotNullIndex;
            this.fieldDoubleNullIndex = otherInfo.fieldDoubleNullIndex;
            this.fieldDateNotNullIndex = otherInfo.fieldDateNotNullIndex;
            this.fieldDateNullIndex = otherInfo.fieldDateNullIndex;
            this.fieldObjectNullIndex = otherInfo.fieldObjectNullIndex;

            setIndicesMap(otherInfo.getIndicesMap());
        }

        @Override
        public final NullTypesColumnInfo clone() {
            return (NullTypesColumnInfo) super.clone();
        }

    }
    private NullTypesColumnInfo columnInfo;
    private ProxyState proxyState;
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

    NullTypesRealmProxy() {
        if (proxyState == null) {
            injectObjectContext();
        }
        proxyState.setConstructionFinished();
    }

    private void injectObjectContext() {
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (NullTypesColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState(some.test.NullTypes.class, this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @SuppressWarnings("cast")
    public String realmGet$fieldStringNotNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.fieldStringNotNullIndex);
    }

    public void realmSet$fieldStringNotNull(String value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public String realmGet$fieldStringNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.fieldStringNullIndex);
    }

    public void realmSet$fieldStringNull(String value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Boolean realmGet$fieldBooleanNotNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.fieldBooleanNotNullIndex);
    }

    public void realmSet$fieldBooleanNotNull(Boolean value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Boolean realmGet$fieldBooleanNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldBooleanNullIndex)) {
            return null;
        }
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.fieldBooleanNullIndex);
    }

    public void realmSet$fieldBooleanNull(Boolean value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public byte[] realmGet$fieldBytesNotNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (byte[]) proxyState.getRow$realm().getBinaryByteArray(columnInfo.fieldBytesNotNullIndex);
    }

    public void realmSet$fieldBytesNotNull(byte[] value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public byte[] realmGet$fieldBytesNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (byte[]) proxyState.getRow$realm().getBinaryByteArray(columnInfo.fieldBytesNullIndex);
    }

    public void realmSet$fieldBytesNull(byte[] value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Byte realmGet$fieldByteNotNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (byte) proxyState.getRow$realm().getLong(columnInfo.fieldByteNotNullIndex);
    }

    public void realmSet$fieldByteNotNull(Byte value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Byte realmGet$fieldByteNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldByteNullIndex)) {
            return null;
        }
        return (byte) proxyState.getRow$realm().getLong(columnInfo.fieldByteNullIndex);
    }

    public void realmSet$fieldByteNull(Byte value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Short realmGet$fieldShortNotNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (short) proxyState.getRow$realm().getLong(columnInfo.fieldShortNotNullIndex);
    }

    public void realmSet$fieldShortNotNull(Short value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Short realmGet$fieldShortNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldShortNullIndex)) {
            return null;
        }
        return (short) proxyState.getRow$realm().getLong(columnInfo.fieldShortNullIndex);
    }

    public void realmSet$fieldShortNull(Short value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Integer realmGet$fieldIntegerNotNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.fieldIntegerNotNullIndex);
    }

    public void realmSet$fieldIntegerNotNull(Integer value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Integer realmGet$fieldIntegerNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldIntegerNullIndex)) {
            return null;
        }
        return (int) proxyState.getRow$realm().getLong(columnInfo.fieldIntegerNullIndex);
    }

    public void realmSet$fieldIntegerNull(Integer value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Long realmGet$fieldLongNotNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (long) proxyState.getRow$realm().getLong(columnInfo.fieldLongNotNullIndex);
    }

    public void realmSet$fieldLongNotNull(Long value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Long realmGet$fieldLongNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldLongNullIndex)) {
            return null;
        }
        return (long) proxyState.getRow$realm().getLong(columnInfo.fieldLongNullIndex);
    }

    public void realmSet$fieldLongNull(Long value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Float realmGet$fieldFloatNotNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.fieldFloatNotNullIndex);
    }

    public void realmSet$fieldFloatNotNull(Float value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Float realmGet$fieldFloatNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldFloatNullIndex)) {
            return null;
        }
        return (float) proxyState.getRow$realm().getFloat(columnInfo.fieldFloatNullIndex);
    }

    public void realmSet$fieldFloatNull(Float value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Double realmGet$fieldDoubleNotNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (double) proxyState.getRow$realm().getDouble(columnInfo.fieldDoubleNotNullIndex);
    }

    public void realmSet$fieldDoubleNotNull(Double value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Double realmGet$fieldDoubleNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldDoubleNullIndex)) {
            return null;
        }
        return (double) proxyState.getRow$realm().getDouble(columnInfo.fieldDoubleNullIndex);
    }

    public void realmSet$fieldDoubleNull(Double value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Date realmGet$fieldDateNotNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        return (java.util.Date) proxyState.getRow$realm().getDate(columnInfo.fieldDateNotNullIndex);
    }

    public void realmSet$fieldDateNotNull(Date value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    @SuppressWarnings("cast")
    public Date realmGet$fieldDateNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldDateNullIndex)) {
            return null;
        }
        return (java.util.Date) proxyState.getRow$realm().getDate(columnInfo.fieldDateNullIndex);
    }

    public void realmSet$fieldDateNull(Date value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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

    public some.test.NullTypes realmGet$fieldObjectNull() {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNullLink(columnInfo.fieldObjectNullIndex)) {
            return null;
        }
        return proxyState.getRealm$realm().get(some.test.NullTypes.class, proxyState.getRow$realm().getLink(columnInfo.fieldObjectNullIndex), false, Collections.<String>emptyList());
    }

    public void realmSet$fieldObjectNull(some.test.NullTypes value) {
        if (proxyState == null) {
            // Called from model's constructor. Inject context.
            injectObjectContext();
        }

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
                // Table#nullifyLink() does not support default value. Just use Row.
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
        if (((RealmObjectProxy)value).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm()) {
            throw new IllegalArgumentException("'value' belongs to a different Realm.");
        }
        proxyState.getRow$realm().setLink(columnInfo.fieldObjectNullIndex, ((RealmObjectProxy)value).realmGet$proxyState().getRow$realm().getIndex());
    }

    public static RealmObjectSchema createRealmObjectSchema(RealmSchema realmSchema) {
        if (!realmSchema.contains("NullTypes")) {
            RealmObjectSchema realmObjectSchema = realmSchema.create("NullTypes");
            realmObjectSchema.add(new Property("fieldStringNotNull", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldStringNull", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldBooleanNotNull", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldBooleanNull", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldBytesNotNull", RealmFieldType.BINARY, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldBytesNull", RealmFieldType.BINARY, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldByteNotNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldByteNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldShortNotNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldShortNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldIntegerNotNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldIntegerNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldLongNotNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldLongNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldFloatNotNull", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldFloatNull", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldDoubleNotNull", RealmFieldType.DOUBLE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldDoubleNull", RealmFieldType.DOUBLE, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldDateNotNull", RealmFieldType.DATE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED));
            realmObjectSchema.add(new Property("fieldDateNull", RealmFieldType.DATE, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED));
            if (!realmSchema.contains("NullTypes")) {
                NullTypesRealmProxy.createRealmObjectSchema(realmSchema);
            }
            realmObjectSchema.add(new Property("fieldObjectNull", RealmFieldType.OBJECT, realmSchema.get("NullTypes")));
            return realmObjectSchema;
        }
        return realmSchema.get("NullTypes");
    }

    @SuppressWarnings("UnusedParameters")
    public static Table initTable(RealmProxyMediator mediator, SharedRealm sharedRealm) {
        if (!sharedRealm.hasTable("class_NullTypes")) {
            Table table = sharedRealm.getTable("class_NullTypes");
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
            if (!sharedRealm.hasTable("class_NullTypes")) {
                mediator.createTable(some.test.NullTypes.class, sharedRealm);
            }
            table.addColumnLink(RealmFieldType.OBJECT, "fieldObjectNull", sharedRealm.getTable("class_NullTypes"));
            table.setPrimaryKey("");
            return table;
        }
        return sharedRealm.getTable("class_NullTypes");
    }

    public static NullTypesColumnInfo validateTable(SharedRealm sharedRealm, boolean allowExtraColumns) {
        if (sharedRealm.hasTable("class_NullTypes")) {
            Table table = sharedRealm.getTable("class_NullTypes");
            final long columnCount = table.getColumnCount();
            if (columnCount != 21) {
                if (columnCount < 21) {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is less than expected - expected 21 but was " + columnCount);
                }
                if (allowExtraColumns) {
                    RealmLog.debug("Field count is more than expected - expected 21 but was %1$d", columnCount);
                } else {
                    throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field count is more than expected - expected 21 but was " + columnCount);
                }
            }
            Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>();
            for (long i = 0; i < 21; i++) {
                columnTypes.put(table.getColumnName(i), table.getColumnType(i));
            }

            final NullTypesColumnInfo columnInfo = new NullTypesColumnInfo(sharedRealm.getPath(), table);

            if (!columnTypes.containsKey("fieldStringNotNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldStringNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldStringNotNull") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'fieldStringNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldStringNotNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldStringNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldStringNotNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldStringNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldStringNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldStringNull") != RealmFieldType.STRING) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'String' for field 'fieldStringNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldStringNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldStringNull' is required. Either set @Required to field 'fieldStringNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldBooleanNotNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldBooleanNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldBooleanNotNull") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Boolean' for field 'fieldBooleanNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldBooleanNotNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldBooleanNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldBooleanNotNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldBooleanNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldBooleanNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldBooleanNull") != RealmFieldType.BOOLEAN) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Boolean' for field 'fieldBooleanNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldBooleanNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(),"Field 'fieldBooleanNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldBooleanNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldBytesNotNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldBytesNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldBytesNotNull") != RealmFieldType.BINARY) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'byte[]' for field 'fieldBytesNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldBytesNotNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldBytesNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldBytesNotNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldBytesNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldBytesNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldBytesNull") != RealmFieldType.BINARY) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'byte[]' for field 'fieldBytesNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldBytesNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldBytesNull' is required. Either set @Required to field 'fieldBytesNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldByteNotNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldByteNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldByteNotNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Byte' for field 'fieldByteNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldByteNotNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldByteNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldByteNotNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldByteNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldByteNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldByteNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Byte' for field 'fieldByteNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldByteNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(),"Field 'fieldByteNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldByteNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldShortNotNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldShortNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldShortNotNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Short' for field 'fieldShortNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldShortNotNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldShortNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldShortNotNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldShortNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldShortNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldShortNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Short' for field 'fieldShortNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldShortNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(),"Field 'fieldShortNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldShortNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldIntegerNotNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldIntegerNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldIntegerNotNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Integer' for field 'fieldIntegerNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldIntegerNotNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldIntegerNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldIntegerNotNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldIntegerNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldIntegerNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldIntegerNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Integer' for field 'fieldIntegerNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldIntegerNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(),"Field 'fieldIntegerNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldIntegerNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldLongNotNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldLongNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldLongNotNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Long' for field 'fieldLongNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldLongNotNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldLongNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldLongNotNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldLongNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldLongNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldLongNull") != RealmFieldType.INTEGER) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Long' for field 'fieldLongNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldLongNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(),"Field 'fieldLongNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldLongNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldFloatNotNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldFloatNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldFloatNotNull") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Float' for field 'fieldFloatNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldFloatNotNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldFloatNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldFloatNotNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldFloatNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldFloatNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldFloatNull") != RealmFieldType.FLOAT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Float' for field 'fieldFloatNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldFloatNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(),"Field 'fieldFloatNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldFloatNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldDoubleNotNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldDoubleNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldDoubleNotNull") != RealmFieldType.DOUBLE) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Double' for field 'fieldDoubleNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldDoubleNotNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldDoubleNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldDoubleNotNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldDoubleNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldDoubleNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldDoubleNull") != RealmFieldType.DOUBLE) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Double' for field 'fieldDoubleNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldDoubleNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(),"Field 'fieldDoubleNull' does not support null values in the existing Realm file. Either set @Required, use the primitive type for field 'fieldDoubleNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldDateNotNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldDateNotNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldDateNotNull") != RealmFieldType.DATE) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Date' for field 'fieldDateNotNull' in existing Realm file.");
            }
            if (table.isColumnNullable(columnInfo.fieldDateNotNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldDateNotNull' does support null values in the existing Realm file. Remove @Required or @PrimaryKey from field 'fieldDateNotNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldDateNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldDateNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldDateNull") != RealmFieldType.DATE) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'Date' for field 'fieldDateNull' in existing Realm file.");
            }
            if (!table.isColumnNullable(columnInfo.fieldDateNullIndex)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Field 'fieldDateNull' is required. Either set @Required to field 'fieldDateNull' or migrate using RealmObjectSchema.setNullable().");
            }
            if (!columnTypes.containsKey("fieldObjectNull")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing field 'fieldObjectNull' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().");
            }
            if (columnTypes.get("fieldObjectNull") != RealmFieldType.OBJECT) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid type 'NullTypes' for field 'fieldObjectNull'");
            }
            if (!sharedRealm.hasTable("class_NullTypes")) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Missing class 'class_NullTypes' for field 'fieldObjectNull'");
            }
            Table table_20 = sharedRealm.getTable("class_NullTypes");
            if (!table.getLinkTarget(columnInfo.fieldObjectNullIndex).hasSameSchema(table_20)) {
                throw new RealmMigrationNeededException(sharedRealm.getPath(), "Invalid RealmObject for field 'fieldObjectNull': '" + table.getLinkTarget(columnInfo.fieldObjectNullIndex).getName() + "' expected - was '" + table_20.getName() + "'");
            }
            return columnInfo;
        } else {
            throw new RealmMigrationNeededException(sharedRealm.getPath(), "The 'NullTypes' class is missing from the schema for this Realm.");
        }
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
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.NullTypes createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        some.test.NullTypes obj = new some.test.NullTypes();
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
        obj = realm.copyToRealm(obj);
        return obj;
    }

    public static some.test.NullTypes copyOrUpdate(Realm realm, some.test.NullTypes object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId) {
            throw new IllegalArgumentException("Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.");
        }
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return object;
        }
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        RealmObjectProxy cachedRealmObject = cache.get(object);
        if (cachedRealmObject != null) {
            return (some.test.NullTypes) cachedRealmObject;
        } else {
            return copy(realm, object, update, cache);
        }
    }

    public static some.test.NullTypes copy(Realm realm, some.test.NullTypes newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.NullTypes) cachedRealmObject;
        } else {
            // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
            some.test.NullTypes realmObject = realm.createObjectInternal(some.test.NullTypes.class, false, Collections.<String>emptyList());
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
    }

    public static long insert(Realm realm, some.test.NullTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.NullTypes.class);
        long tableNativePtr = table.getNativeTablePointer();
        NullTypesColumnInfo columnInfo = (NullTypesColumnInfo) realm.schema.getColumnInfo(some.test.NullTypes.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        String realmGet$fieldStringNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldStringNotNull();
        if (realmGet$fieldStringNotNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, realmGet$fieldStringNotNull, false);
        }
        String realmGet$fieldStringNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldStringNull();
        if (realmGet$fieldStringNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, realmGet$fieldStringNull, false);
        }
        Boolean realmGet$fieldBooleanNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBooleanNotNull();
        if (realmGet$fieldBooleanNotNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, realmGet$fieldBooleanNotNull, false);
        }
        Boolean realmGet$fieldBooleanNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBooleanNull();
        if (realmGet$fieldBooleanNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, realmGet$fieldBooleanNull, false);
        }
        byte[] realmGet$fieldBytesNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBytesNotNull();
        if (realmGet$fieldBytesNotNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, realmGet$fieldBytesNotNull, false);
        }
        byte[] realmGet$fieldBytesNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBytesNull();
        if (realmGet$fieldBytesNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, realmGet$fieldBytesNull, false);
        }
        Number realmGet$fieldByteNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldByteNotNull();
        if (realmGet$fieldByteNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, realmGet$fieldByteNotNull.longValue(), false);
        }
        Number realmGet$fieldByteNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldByteNull();
        if (realmGet$fieldByteNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, realmGet$fieldByteNull.longValue(), false);
        }
        Number realmGet$fieldShortNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldShortNotNull();
        if (realmGet$fieldShortNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, realmGet$fieldShortNotNull.longValue(), false);
        }
        Number realmGet$fieldShortNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldShortNull();
        if (realmGet$fieldShortNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, realmGet$fieldShortNull.longValue(), false);
        }
        Number realmGet$fieldIntegerNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldIntegerNotNull();
        if (realmGet$fieldIntegerNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, realmGet$fieldIntegerNotNull.longValue(), false);
        }
        Number realmGet$fieldIntegerNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldIntegerNull();
        if (realmGet$fieldIntegerNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, realmGet$fieldIntegerNull.longValue(), false);
        }
        Number realmGet$fieldLongNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldLongNotNull();
        if (realmGet$fieldLongNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, realmGet$fieldLongNotNull.longValue(), false);
        }
        Number realmGet$fieldLongNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldLongNull();
        if (realmGet$fieldLongNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, realmGet$fieldLongNull.longValue(), false);
        }
        Float realmGet$fieldFloatNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldFloatNotNull();
        if (realmGet$fieldFloatNotNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, realmGet$fieldFloatNotNull, false);
        }
        Float realmGet$fieldFloatNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldFloatNull();
        if (realmGet$fieldFloatNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, realmGet$fieldFloatNull, false);
        }
        Double realmGet$fieldDoubleNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDoubleNotNull();
        if (realmGet$fieldDoubleNotNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, realmGet$fieldDoubleNotNull, false);
        }
        Double realmGet$fieldDoubleNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDoubleNull();
        if (realmGet$fieldDoubleNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, realmGet$fieldDoubleNull, false);
        }
        java.util.Date realmGet$fieldDateNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDateNotNull();
        if (realmGet$fieldDateNotNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, realmGet$fieldDateNotNull.getTime(), false);
        }
        java.util.Date realmGet$fieldDateNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDateNull();
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
        long tableNativePtr = table.getNativeTablePointer();
        NullTypesColumnInfo columnInfo = (NullTypesColumnInfo) realm.schema.getColumnInfo(some.test.NullTypes.class);
        some.test.NullTypes object = null;
        while (objects.hasNext()) {
            object = (some.test.NullTypes) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                String realmGet$fieldStringNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldStringNotNull();
                if (realmGet$fieldStringNotNull != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, realmGet$fieldStringNotNull, false);
                }
                String realmGet$fieldStringNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldStringNull();
                if (realmGet$fieldStringNull != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, realmGet$fieldStringNull, false);
                }
                Boolean realmGet$fieldBooleanNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBooleanNotNull();
                if (realmGet$fieldBooleanNotNull != null) {
                    Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, realmGet$fieldBooleanNotNull, false);
                }
                Boolean realmGet$fieldBooleanNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBooleanNull();
                if (realmGet$fieldBooleanNull != null) {
                    Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, realmGet$fieldBooleanNull, false);
                }
                byte[] realmGet$fieldBytesNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBytesNotNull();
                if (realmGet$fieldBytesNotNull != null) {
                    Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, realmGet$fieldBytesNotNull, false);
                }
                byte[] realmGet$fieldBytesNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBytesNull();
                if (realmGet$fieldBytesNull != null) {
                    Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, realmGet$fieldBytesNull, false);
                }
                Number realmGet$fieldByteNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldByteNotNull();
                if (realmGet$fieldByteNotNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, realmGet$fieldByteNotNull.longValue(), false);
                }
                Number realmGet$fieldByteNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldByteNull();
                if (realmGet$fieldByteNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, realmGet$fieldByteNull.longValue(), false);
                }
                Number realmGet$fieldShortNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldShortNotNull();
                if (realmGet$fieldShortNotNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, realmGet$fieldShortNotNull.longValue(), false);
                }
                Number realmGet$fieldShortNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldShortNull();
                if (realmGet$fieldShortNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, realmGet$fieldShortNull.longValue(), false);
                }
                Number realmGet$fieldIntegerNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldIntegerNotNull();
                if (realmGet$fieldIntegerNotNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, realmGet$fieldIntegerNotNull.longValue(), false);
                }
                Number realmGet$fieldIntegerNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldIntegerNull();
                if (realmGet$fieldIntegerNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, realmGet$fieldIntegerNull.longValue(), false);
                }
                Number realmGet$fieldLongNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldLongNotNull();
                if (realmGet$fieldLongNotNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, realmGet$fieldLongNotNull.longValue(), false);
                }
                Number realmGet$fieldLongNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldLongNull();
                if (realmGet$fieldLongNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, realmGet$fieldLongNull.longValue(), false);
                }
                Float realmGet$fieldFloatNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldFloatNotNull();
                if (realmGet$fieldFloatNotNull != null) {
                    Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, realmGet$fieldFloatNotNull, false);
                }
                Float realmGet$fieldFloatNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldFloatNull();
                if (realmGet$fieldFloatNull != null) {
                    Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, realmGet$fieldFloatNull, false);
                }
                Double realmGet$fieldDoubleNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDoubleNotNull();
                if (realmGet$fieldDoubleNotNull != null) {
                    Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, realmGet$fieldDoubleNotNull, false);
                }
                Double realmGet$fieldDoubleNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDoubleNull();
                if (realmGet$fieldDoubleNull != null) {
                    Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, realmGet$fieldDoubleNull, false);
                }
                java.util.Date realmGet$fieldDateNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDateNotNull();
                if (realmGet$fieldDateNotNull != null) {
                    Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, realmGet$fieldDateNotNull.getTime(), false);
                }
                java.util.Date realmGet$fieldDateNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDateNull();
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
    }

    public static long insertOrUpdate(Realm realm, some.test.NullTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.NullTypes.class);
        long tableNativePtr = table.getNativeTablePointer();
        NullTypesColumnInfo columnInfo = (NullTypesColumnInfo) realm.schema.getColumnInfo(some.test.NullTypes.class);
        long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
        cache.put(object, rowIndex);
        String realmGet$fieldStringNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldStringNotNull();
        if (realmGet$fieldStringNotNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, realmGet$fieldStringNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, false);
        }
        String realmGet$fieldStringNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldStringNull();
        if (realmGet$fieldStringNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, realmGet$fieldStringNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, false);
        }
        Boolean realmGet$fieldBooleanNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBooleanNotNull();
        if (realmGet$fieldBooleanNotNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, realmGet$fieldBooleanNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, false);
        }
        Boolean realmGet$fieldBooleanNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBooleanNull();
        if (realmGet$fieldBooleanNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, realmGet$fieldBooleanNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, false);
        }
        byte[] realmGet$fieldBytesNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBytesNotNull();
        if (realmGet$fieldBytesNotNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, realmGet$fieldBytesNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, false);
        }
        byte[] realmGet$fieldBytesNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBytesNull();
        if (realmGet$fieldBytesNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, realmGet$fieldBytesNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, false);
        }
        Number realmGet$fieldByteNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldByteNotNull();
        if (realmGet$fieldByteNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, realmGet$fieldByteNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, false);
        }
        Number realmGet$fieldByteNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldByteNull();
        if (realmGet$fieldByteNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, realmGet$fieldByteNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, false);
        }
        Number realmGet$fieldShortNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldShortNotNull();
        if (realmGet$fieldShortNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, realmGet$fieldShortNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, false);
        }
        Number realmGet$fieldShortNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldShortNull();
        if (realmGet$fieldShortNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, realmGet$fieldShortNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, false);
        }
        Number realmGet$fieldIntegerNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldIntegerNotNull();
        if (realmGet$fieldIntegerNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, realmGet$fieldIntegerNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, false);
        }
        Number realmGet$fieldIntegerNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldIntegerNull();
        if (realmGet$fieldIntegerNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, realmGet$fieldIntegerNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, false);
        }
        Number realmGet$fieldLongNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldLongNotNull();
        if (realmGet$fieldLongNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, realmGet$fieldLongNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, false);
        }
        Number realmGet$fieldLongNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldLongNull();
        if (realmGet$fieldLongNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, realmGet$fieldLongNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, false);
        }
        Float realmGet$fieldFloatNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldFloatNotNull();
        if (realmGet$fieldFloatNotNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, realmGet$fieldFloatNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, false);
        }
        Float realmGet$fieldFloatNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldFloatNull();
        if (realmGet$fieldFloatNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, realmGet$fieldFloatNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, false);
        }
        Double realmGet$fieldDoubleNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDoubleNotNull();
        if (realmGet$fieldDoubleNotNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, realmGet$fieldDoubleNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, false);
        }
        Double realmGet$fieldDoubleNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDoubleNull();
        if (realmGet$fieldDoubleNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, realmGet$fieldDoubleNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, false);
        }
        java.util.Date realmGet$fieldDateNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDateNotNull();
        if (realmGet$fieldDateNotNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, realmGet$fieldDateNotNull.getTime(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, false);
        }
        java.util.Date realmGet$fieldDateNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDateNull();
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
        long tableNativePtr = table.getNativeTablePointer();
        NullTypesColumnInfo columnInfo = (NullTypesColumnInfo) realm.schema.getColumnInfo(some.test.NullTypes.class);
        some.test.NullTypes object = null;
        while (objects.hasNext()) {
            object = (some.test.NullTypes) objects.next();
            if(!cache.containsKey(object)) {
                if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex());
                    continue;
                }
                long rowIndex = Table.nativeAddEmptyRow(tableNativePtr, 1);
                cache.put(object, rowIndex);
                String realmGet$fieldStringNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldStringNotNull();
                if (realmGet$fieldStringNotNull != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, realmGet$fieldStringNotNull, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, false);
                }
                String realmGet$fieldStringNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldStringNull();
                if (realmGet$fieldStringNull != null) {
                    Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, realmGet$fieldStringNull, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, false);
                }
                Boolean realmGet$fieldBooleanNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBooleanNotNull();
                if (realmGet$fieldBooleanNotNull != null) {
                    Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, realmGet$fieldBooleanNotNull, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, false);
                }
                Boolean realmGet$fieldBooleanNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBooleanNull();
                if (realmGet$fieldBooleanNull != null) {
                    Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, realmGet$fieldBooleanNull, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, false);
                }
                byte[] realmGet$fieldBytesNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBytesNotNull();
                if (realmGet$fieldBytesNotNull != null) {
                    Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, realmGet$fieldBytesNotNull, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, false);
                }
                byte[] realmGet$fieldBytesNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldBytesNull();
                if (realmGet$fieldBytesNull != null) {
                    Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, realmGet$fieldBytesNull, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, false);
                }
                Number realmGet$fieldByteNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldByteNotNull();
                if (realmGet$fieldByteNotNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, realmGet$fieldByteNotNull.longValue(), false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, false);
                }
                Number realmGet$fieldByteNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldByteNull();
                if (realmGet$fieldByteNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, realmGet$fieldByteNull.longValue(), false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, false);
                }
                Number realmGet$fieldShortNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldShortNotNull();
                if (realmGet$fieldShortNotNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, realmGet$fieldShortNotNull.longValue(), false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, false);
                }
                Number realmGet$fieldShortNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldShortNull();
                if (realmGet$fieldShortNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, realmGet$fieldShortNull.longValue(), false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, false);
                }
                Number realmGet$fieldIntegerNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldIntegerNotNull();
                if (realmGet$fieldIntegerNotNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, realmGet$fieldIntegerNotNull.longValue(), false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, false);
                }
                Number realmGet$fieldIntegerNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldIntegerNull();
                if (realmGet$fieldIntegerNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, realmGet$fieldIntegerNull.longValue(), false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, false);
                }
                Number realmGet$fieldLongNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldLongNotNull();
                if (realmGet$fieldLongNotNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, realmGet$fieldLongNotNull.longValue(), false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, false);
                }
                Number realmGet$fieldLongNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldLongNull();
                if (realmGet$fieldLongNull != null) {
                    Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, realmGet$fieldLongNull.longValue(), false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, false);
                }
                Float realmGet$fieldFloatNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldFloatNotNull();
                if (realmGet$fieldFloatNotNull != null) {
                    Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, realmGet$fieldFloatNotNull, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, false);
                }
                Float realmGet$fieldFloatNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldFloatNull();
                if (realmGet$fieldFloatNull != null) {
                    Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, realmGet$fieldFloatNull, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, false);
                }
                Double realmGet$fieldDoubleNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDoubleNotNull();
                if (realmGet$fieldDoubleNotNull != null) {
                    Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, realmGet$fieldDoubleNotNull, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, false);
                }
                Double realmGet$fieldDoubleNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDoubleNull();
                if (realmGet$fieldDoubleNull != null) {
                    Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, realmGet$fieldDoubleNull, false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, false);
                }
                java.util.Date realmGet$fieldDateNotNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDateNotNull();
                if (realmGet$fieldDateNotNull != null) {
                    Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, realmGet$fieldDateNotNull.getTime(), false);
                } else {
                    Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, false);
                }
                java.util.Date realmGet$fieldDateNull = ((NullTypesRealmProxyInterface)object).realmGet$fieldDateNull();
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
    }

    public static some.test.NullTypes createDetachedCopy(some.test.NullTypes realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.NullTypes unmanagedObject;
        if (cachedObject != null) {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.NullTypes)cachedObject.object;
            } else {
                unmanagedObject = (some.test.NullTypes)cachedObject.object;
                cachedObject.minDepth = currentDepth;
            }
        } else {
            unmanagedObject = new some.test.NullTypes();
            cache.put(realmObject, new RealmObjectProxy.CacheData(currentDepth, unmanagedObject));
        }
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldStringNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldStringNotNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldStringNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldStringNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldBooleanNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldBooleanNotNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldBooleanNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldBooleanNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldBytesNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldBytesNotNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldBytesNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldBytesNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldByteNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldByteNotNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldByteNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldByteNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldShortNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldShortNotNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldShortNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldShortNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldIntegerNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldIntegerNotNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldIntegerNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldIntegerNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldLongNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldLongNotNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldLongNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldLongNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldFloatNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldFloatNotNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldFloatNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldFloatNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldDoubleNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldDoubleNotNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldDoubleNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldDoubleNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldDateNotNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldDateNotNull());
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldDateNull(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldDateNull());

        // Deep copy of fieldObjectNull
        ((NullTypesRealmProxyInterface) unmanagedObject).realmSet$fieldObjectNull(NullTypesRealmProxy.createDetachedCopy(((NullTypesRealmProxyInterface) realmObject).realmGet$fieldObjectNull(), currentDepth + 1, maxDepth, cache));
        return unmanagedObject;
    }

    @Override
    public String toString() {
        if (!RealmObject.isValid(this)) {
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
