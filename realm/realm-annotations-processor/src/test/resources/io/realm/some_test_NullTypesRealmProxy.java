package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.ProxyUtils;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.OsList;
import io.realm.internal.OsObject;
import io.realm.internal.OsObjectSchemaInfo;
import io.realm.internal.OsSchemaInfo;
import io.realm.internal.Property;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
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
public class some_test_NullTypesRealmProxy extends some.test.NullTypes
        implements RealmObjectProxy, some_test_NullTypesRealmProxyInterface {

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
        long fieldStringListNotNullIndex;
        long fieldStringListNullIndex;
        long fieldBinaryListNotNullIndex;
        long fieldBinaryListNullIndex;
        long fieldBooleanListNotNullIndex;
        long fieldBooleanListNullIndex;
        long fieldLongListNotNullIndex;
        long fieldLongListNullIndex;
        long fieldIntegerListNotNullIndex;
        long fieldIntegerListNullIndex;
        long fieldShortListNotNullIndex;
        long fieldShortListNullIndex;
        long fieldByteListNotNullIndex;
        long fieldByteListNullIndex;
        long fieldDoubleListNotNullIndex;
        long fieldDoubleListNullIndex;
        long fieldFloatListNotNullIndex;
        long fieldFloatListNullIndex;
        long fieldDateListNotNullIndex;
        long fieldDateListNullIndex;

        NullTypesColumnInfo(OsSchemaInfo schemaInfo) {
            super(41);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("NullTypes");
            this.fieldStringNotNullIndex = addColumnDetails("fieldStringNotNull", "fieldStringNotNull", objectSchemaInfo);
            this.fieldStringNullIndex = addColumnDetails("fieldStringNull", "fieldStringNull", objectSchemaInfo);
            this.fieldBooleanNotNullIndex = addColumnDetails("fieldBooleanNotNull", "fieldBooleanNotNull", objectSchemaInfo);
            this.fieldBooleanNullIndex = addColumnDetails("fieldBooleanNull", "fieldBooleanNull", objectSchemaInfo);
            this.fieldBytesNotNullIndex = addColumnDetails("fieldBytesNotNull", "fieldBytesNotNull", objectSchemaInfo);
            this.fieldBytesNullIndex = addColumnDetails("fieldBytesNull", "fieldBytesNull", objectSchemaInfo);
            this.fieldByteNotNullIndex = addColumnDetails("fieldByteNotNull", "fieldByteNotNull", objectSchemaInfo);
            this.fieldByteNullIndex = addColumnDetails("fieldByteNull", "fieldByteNull", objectSchemaInfo);
            this.fieldShortNotNullIndex = addColumnDetails("fieldShortNotNull", "fieldShortNotNull", objectSchemaInfo);
            this.fieldShortNullIndex = addColumnDetails("fieldShortNull", "fieldShortNull", objectSchemaInfo);
            this.fieldIntegerNotNullIndex = addColumnDetails("fieldIntegerNotNull", "fieldIntegerNotNull", objectSchemaInfo);
            this.fieldIntegerNullIndex = addColumnDetails("fieldIntegerNull", "fieldIntegerNull", objectSchemaInfo);
            this.fieldLongNotNullIndex = addColumnDetails("fieldLongNotNull", "fieldLongNotNull", objectSchemaInfo);
            this.fieldLongNullIndex = addColumnDetails("fieldLongNull", "fieldLongNull", objectSchemaInfo);
            this.fieldFloatNotNullIndex = addColumnDetails("fieldFloatNotNull", "fieldFloatNotNull", objectSchemaInfo);
            this.fieldFloatNullIndex = addColumnDetails("fieldFloatNull", "fieldFloatNull", objectSchemaInfo);
            this.fieldDoubleNotNullIndex = addColumnDetails("fieldDoubleNotNull", "fieldDoubleNotNull", objectSchemaInfo);
            this.fieldDoubleNullIndex = addColumnDetails("fieldDoubleNull", "fieldDoubleNull", objectSchemaInfo);
            this.fieldDateNotNullIndex = addColumnDetails("fieldDateNotNull", "fieldDateNotNull", objectSchemaInfo);
            this.fieldDateNullIndex = addColumnDetails("fieldDateNull", "fieldDateNull", objectSchemaInfo);
            this.fieldObjectNullIndex = addColumnDetails("fieldObjectNull", "fieldObjectNull", objectSchemaInfo);
            this.fieldStringListNotNullIndex = addColumnDetails("fieldStringListNotNull", "fieldStringListNotNull", objectSchemaInfo);
            this.fieldStringListNullIndex = addColumnDetails("fieldStringListNull", "fieldStringListNull", objectSchemaInfo);
            this.fieldBinaryListNotNullIndex = addColumnDetails("fieldBinaryListNotNull", "fieldBinaryListNotNull", objectSchemaInfo);
            this.fieldBinaryListNullIndex = addColumnDetails("fieldBinaryListNull", "fieldBinaryListNull", objectSchemaInfo);
            this.fieldBooleanListNotNullIndex = addColumnDetails("fieldBooleanListNotNull", "fieldBooleanListNotNull", objectSchemaInfo);
            this.fieldBooleanListNullIndex = addColumnDetails("fieldBooleanListNull", "fieldBooleanListNull", objectSchemaInfo);
            this.fieldLongListNotNullIndex = addColumnDetails("fieldLongListNotNull", "fieldLongListNotNull", objectSchemaInfo);
            this.fieldLongListNullIndex = addColumnDetails("fieldLongListNull", "fieldLongListNull", objectSchemaInfo);
            this.fieldIntegerListNotNullIndex = addColumnDetails("fieldIntegerListNotNull", "fieldIntegerListNotNull", objectSchemaInfo);
            this.fieldIntegerListNullIndex = addColumnDetails("fieldIntegerListNull", "fieldIntegerListNull", objectSchemaInfo);
            this.fieldShortListNotNullIndex = addColumnDetails("fieldShortListNotNull", "fieldShortListNotNull", objectSchemaInfo);
            this.fieldShortListNullIndex = addColumnDetails("fieldShortListNull", "fieldShortListNull", objectSchemaInfo);
            this.fieldByteListNotNullIndex = addColumnDetails("fieldByteListNotNull", "fieldByteListNotNull", objectSchemaInfo);
            this.fieldByteListNullIndex = addColumnDetails("fieldByteListNull", "fieldByteListNull", objectSchemaInfo);
            this.fieldDoubleListNotNullIndex = addColumnDetails("fieldDoubleListNotNull", "fieldDoubleListNotNull", objectSchemaInfo);
            this.fieldDoubleListNullIndex = addColumnDetails("fieldDoubleListNull", "fieldDoubleListNull", objectSchemaInfo);
            this.fieldFloatListNotNullIndex = addColumnDetails("fieldFloatListNotNull", "fieldFloatListNotNull", objectSchemaInfo);
            this.fieldFloatListNullIndex = addColumnDetails("fieldFloatListNull", "fieldFloatListNull", objectSchemaInfo);
            this.fieldDateListNotNullIndex = addColumnDetails("fieldDateListNotNull", "fieldDateListNotNull", objectSchemaInfo);
            this.fieldDateListNullIndex = addColumnDetails("fieldDateListNull", "fieldDateListNull", objectSchemaInfo);
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
            dst.fieldStringListNotNullIndex = src.fieldStringListNotNullIndex;
            dst.fieldStringListNullIndex = src.fieldStringListNullIndex;
            dst.fieldBinaryListNotNullIndex = src.fieldBinaryListNotNullIndex;
            dst.fieldBinaryListNullIndex = src.fieldBinaryListNullIndex;
            dst.fieldBooleanListNotNullIndex = src.fieldBooleanListNotNullIndex;
            dst.fieldBooleanListNullIndex = src.fieldBooleanListNullIndex;
            dst.fieldLongListNotNullIndex = src.fieldLongListNotNullIndex;
            dst.fieldLongListNullIndex = src.fieldLongListNullIndex;
            dst.fieldIntegerListNotNullIndex = src.fieldIntegerListNotNullIndex;
            dst.fieldIntegerListNullIndex = src.fieldIntegerListNullIndex;
            dst.fieldShortListNotNullIndex = src.fieldShortListNotNullIndex;
            dst.fieldShortListNullIndex = src.fieldShortListNullIndex;
            dst.fieldByteListNotNullIndex = src.fieldByteListNotNullIndex;
            dst.fieldByteListNullIndex = src.fieldByteListNullIndex;
            dst.fieldDoubleListNotNullIndex = src.fieldDoubleListNotNullIndex;
            dst.fieldDoubleListNullIndex = src.fieldDoubleListNullIndex;
            dst.fieldFloatListNotNullIndex = src.fieldFloatListNotNullIndex;
            dst.fieldFloatListNullIndex = src.fieldFloatListNullIndex;
            dst.fieldDateListNotNullIndex = src.fieldDateListNotNullIndex;
            dst.fieldDateListNullIndex = src.fieldDateListNullIndex;
        }
    }

    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private NullTypesColumnInfo columnInfo;
    private ProxyState<some.test.NullTypes> proxyState;
    private RealmList<String> fieldStringListNotNullRealmList;
    private RealmList<String> fieldStringListNullRealmList;
    private RealmList<byte[]> fieldBinaryListNotNullRealmList;
    private RealmList<byte[]> fieldBinaryListNullRealmList;
    private RealmList<Boolean> fieldBooleanListNotNullRealmList;
    private RealmList<Boolean> fieldBooleanListNullRealmList;
    private RealmList<Long> fieldLongListNotNullRealmList;
    private RealmList<Long> fieldLongListNullRealmList;
    private RealmList<Integer> fieldIntegerListNotNullRealmList;
    private RealmList<Integer> fieldIntegerListNullRealmList;
    private RealmList<Short> fieldShortListNotNullRealmList;
    private RealmList<Short> fieldShortListNullRealmList;
    private RealmList<Byte> fieldByteListNotNullRealmList;
    private RealmList<Byte> fieldByteListNullRealmList;
    private RealmList<Double> fieldDoubleListNotNullRealmList;
    private RealmList<Double> fieldDoubleListNullRealmList;
    private RealmList<Float> fieldFloatListNotNullRealmList;
    private RealmList<Float> fieldFloatListNullRealmList;
    private RealmList<Date> fieldDateListNotNullRealmList;
    private RealmList<Date> fieldDateListNullRealmList;

    some_test_NullTypesRealmProxy() {
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
            proxyState.checkValidObject(value);
            row.getTable().setLink(columnInfo.fieldObjectNullIndex, row.getIndex(), ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getIndex(), true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().nullifyLink(columnInfo.fieldObjectNullIndex);
            return;
        }
        proxyState.checkValidObject(value);
        proxyState.getRow$realm().setLink(columnInfo.fieldObjectNullIndex, ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getIndex());
    }

    @Override
    public RealmList<String> realmGet$fieldStringListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldStringListNotNullRealmList != null) {
            return fieldStringListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldStringListNotNullIndex, RealmFieldType.STRING_LIST);
            fieldStringListNotNullRealmList = new RealmList<java.lang.String>(java.lang.String.class, osList, proxyState.getRealm$realm());
            return fieldStringListNotNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldStringListNotNull(RealmList<String> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldStringListNotNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldStringListNotNullIndex, RealmFieldType.STRING_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.String item : value) {
            if (item == null) {
                throw new IllegalArgumentException("Storing 'null' into fieldStringListNotNull' is not allowed by the schema.");
            } else {
                osList.addString(item);
            }
        }
    }

    @Override
    public RealmList<String> realmGet$fieldStringListNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldStringListNullRealmList != null) {
            return fieldStringListNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldStringListNullIndex, RealmFieldType.STRING_LIST);
            fieldStringListNullRealmList = new RealmList<java.lang.String>(java.lang.String.class, osList, proxyState.getRealm$realm());
            return fieldStringListNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldStringListNull(RealmList<String> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldStringListNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldStringListNullIndex, RealmFieldType.STRING_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.String item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addString(item);
            }
        }
    }

    @Override
    public RealmList<byte[]> realmGet$fieldBinaryListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldBinaryListNotNullRealmList != null) {
            return fieldBinaryListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBinaryListNotNullIndex, RealmFieldType.BINARY_LIST);
            fieldBinaryListNotNullRealmList = new RealmList<byte[]>(byte[].class, osList, proxyState.getRealm$realm());
            return fieldBinaryListNotNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldBinaryListNotNull(RealmList<byte[]> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldBinaryListNotNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBinaryListNotNullIndex, RealmFieldType.BINARY_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (byte[] item : value) {
            if (item == null) {
                throw new IllegalArgumentException("Storing 'null' into fieldBinaryListNotNull' is not allowed by the schema.");
            } else {
                osList.addBinary(item);
            }
        }
    }

    @Override
    public RealmList<byte[]> realmGet$fieldBinaryListNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldBinaryListNullRealmList != null) {
            return fieldBinaryListNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBinaryListNullIndex, RealmFieldType.BINARY_LIST);
            fieldBinaryListNullRealmList = new RealmList<byte[]>(byte[].class, osList, proxyState.getRealm$realm());
            return fieldBinaryListNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldBinaryListNull(RealmList<byte[]> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldBinaryListNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBinaryListNullIndex, RealmFieldType.BINARY_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (byte[] item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addBinary(item);
            }
        }
    }

    @Override
    public RealmList<Boolean> realmGet$fieldBooleanListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldBooleanListNotNullRealmList != null) {
            return fieldBooleanListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBooleanListNotNullIndex, RealmFieldType.BOOLEAN_LIST);
            fieldBooleanListNotNullRealmList = new RealmList<java.lang.Boolean>(java.lang.Boolean.class, osList, proxyState.getRealm$realm());
            return fieldBooleanListNotNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldBooleanListNotNull(RealmList<Boolean> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldBooleanListNotNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBooleanListNotNullIndex, RealmFieldType.BOOLEAN_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Boolean item : value) {
            if (item == null) {
                throw new IllegalArgumentException("Storing 'null' into fieldBooleanListNotNull' is not allowed by the schema.");
            } else {
                osList.addBoolean(item);
            }
        }
    }

    @Override
    public RealmList<Boolean> realmGet$fieldBooleanListNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldBooleanListNullRealmList != null) {
            return fieldBooleanListNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBooleanListNullIndex, RealmFieldType.BOOLEAN_LIST);
            fieldBooleanListNullRealmList = new RealmList<java.lang.Boolean>(java.lang.Boolean.class, osList, proxyState.getRealm$realm());
            return fieldBooleanListNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldBooleanListNull(RealmList<Boolean> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldBooleanListNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBooleanListNullIndex, RealmFieldType.BOOLEAN_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Boolean item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addBoolean(item);
            }
        }
    }

    @Override
    public RealmList<Long> realmGet$fieldLongListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldLongListNotNullRealmList != null) {
            return fieldLongListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldLongListNotNullIndex, RealmFieldType.INTEGER_LIST);
            fieldLongListNotNullRealmList = new RealmList<java.lang.Long>(java.lang.Long.class, osList, proxyState.getRealm$realm());
            return fieldLongListNotNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldLongListNotNull(RealmList<Long> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldLongListNotNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldLongListNotNullIndex, RealmFieldType.INTEGER_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Long item : value) {
            if (item == null) {
                throw new IllegalArgumentException("Storing 'null' into fieldLongListNotNull' is not allowed by the schema.");
            } else {
                osList.addLong(item.longValue());
            }
        }
    }

    @Override
    public RealmList<Long> realmGet$fieldLongListNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldLongListNullRealmList != null) {
            return fieldLongListNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldLongListNullIndex, RealmFieldType.INTEGER_LIST);
            fieldLongListNullRealmList = new RealmList<java.lang.Long>(java.lang.Long.class, osList, proxyState.getRealm$realm());
            return fieldLongListNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldLongListNull(RealmList<Long> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldLongListNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldLongListNullIndex, RealmFieldType.INTEGER_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Long item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addLong(item.longValue());
            }
        }
    }

    @Override
    public RealmList<Integer> realmGet$fieldIntegerListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldIntegerListNotNullRealmList != null) {
            return fieldIntegerListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldIntegerListNotNullIndex, RealmFieldType.INTEGER_LIST);
            fieldIntegerListNotNullRealmList = new RealmList<java.lang.Integer>(java.lang.Integer.class, osList, proxyState.getRealm$realm());
            return fieldIntegerListNotNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldIntegerListNotNull(RealmList<Integer> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldIntegerListNotNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldIntegerListNotNullIndex, RealmFieldType.INTEGER_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Integer item : value) {
            if (item == null) {
                throw new IllegalArgumentException("Storing 'null' into fieldIntegerListNotNull' is not allowed by the schema.");
            } else {
                osList.addLong(item.longValue());
            }
        }
    }

    @Override
    public RealmList<Integer> realmGet$fieldIntegerListNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldIntegerListNullRealmList != null) {
            return fieldIntegerListNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldIntegerListNullIndex, RealmFieldType.INTEGER_LIST);
            fieldIntegerListNullRealmList = new RealmList<java.lang.Integer>(java.lang.Integer.class, osList, proxyState.getRealm$realm());
            return fieldIntegerListNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldIntegerListNull(RealmList<Integer> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldIntegerListNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldIntegerListNullIndex, RealmFieldType.INTEGER_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Integer item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addLong(item.longValue());
            }
        }
    }

    @Override
    public RealmList<Short> realmGet$fieldShortListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldShortListNotNullRealmList != null) {
            return fieldShortListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldShortListNotNullIndex, RealmFieldType.INTEGER_LIST);
            fieldShortListNotNullRealmList = new RealmList<java.lang.Short>(java.lang.Short.class, osList, proxyState.getRealm$realm());
            return fieldShortListNotNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldShortListNotNull(RealmList<Short> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldShortListNotNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldShortListNotNullIndex, RealmFieldType.INTEGER_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Short item : value) {
            if (item == null) {
                throw new IllegalArgumentException("Storing 'null' into fieldShortListNotNull' is not allowed by the schema.");
            } else {
                osList.addLong(item.longValue());
            }
        }
    }

    @Override
    public RealmList<Short> realmGet$fieldShortListNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldShortListNullRealmList != null) {
            return fieldShortListNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldShortListNullIndex, RealmFieldType.INTEGER_LIST);
            fieldShortListNullRealmList = new RealmList<java.lang.Short>(java.lang.Short.class, osList, proxyState.getRealm$realm());
            return fieldShortListNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldShortListNull(RealmList<Short> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldShortListNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldShortListNullIndex, RealmFieldType.INTEGER_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Short item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addLong(item.longValue());
            }
        }
    }

    @Override
    public RealmList<Byte> realmGet$fieldByteListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldByteListNotNullRealmList != null) {
            return fieldByteListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldByteListNotNullIndex, RealmFieldType.INTEGER_LIST);
            fieldByteListNotNullRealmList = new RealmList<java.lang.Byte>(java.lang.Byte.class, osList, proxyState.getRealm$realm());
            return fieldByteListNotNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldByteListNotNull(RealmList<Byte> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldByteListNotNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldByteListNotNullIndex, RealmFieldType.INTEGER_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Byte item : value) {
            if (item == null) {
                throw new IllegalArgumentException("Storing 'null' into fieldByteListNotNull' is not allowed by the schema.");
            } else {
                osList.addLong(item.longValue());
            }
        }
    }

    @Override
    public RealmList<Byte> realmGet$fieldByteListNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldByteListNullRealmList != null) {
            return fieldByteListNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldByteListNullIndex, RealmFieldType.INTEGER_LIST);
            fieldByteListNullRealmList = new RealmList<java.lang.Byte>(java.lang.Byte.class, osList, proxyState.getRealm$realm());
            return fieldByteListNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldByteListNull(RealmList<Byte> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldByteListNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldByteListNullIndex, RealmFieldType.INTEGER_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Byte item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addLong(item.longValue());
            }
        }
    }

    @Override
    public RealmList<Double> realmGet$fieldDoubleListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldDoubleListNotNullRealmList != null) {
            return fieldDoubleListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDoubleListNotNullIndex, RealmFieldType.DOUBLE_LIST);
            fieldDoubleListNotNullRealmList = new RealmList<java.lang.Double>(java.lang.Double.class, osList, proxyState.getRealm$realm());
            return fieldDoubleListNotNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldDoubleListNotNull(RealmList<Double> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldDoubleListNotNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDoubleListNotNullIndex, RealmFieldType.DOUBLE_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Double item : value) {
            if (item == null) {
                throw new IllegalArgumentException("Storing 'null' into fieldDoubleListNotNull' is not allowed by the schema.");
            } else {
                osList.addDouble(item.doubleValue());
            }
        }
    }

    @Override
    public RealmList<Double> realmGet$fieldDoubleListNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldDoubleListNullRealmList != null) {
            return fieldDoubleListNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDoubleListNullIndex, RealmFieldType.DOUBLE_LIST);
            fieldDoubleListNullRealmList = new RealmList<java.lang.Double>(java.lang.Double.class, osList, proxyState.getRealm$realm());
            return fieldDoubleListNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldDoubleListNull(RealmList<Double> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldDoubleListNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDoubleListNullIndex, RealmFieldType.DOUBLE_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Double item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addDouble(item.doubleValue());
            }
        }
    }

    @Override
    public RealmList<Float> realmGet$fieldFloatListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldFloatListNotNullRealmList != null) {
            return fieldFloatListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldFloatListNotNullIndex, RealmFieldType.FLOAT_LIST);
            fieldFloatListNotNullRealmList = new RealmList<java.lang.Float>(java.lang.Float.class, osList, proxyState.getRealm$realm());
            return fieldFloatListNotNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldFloatListNotNull(RealmList<Float> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldFloatListNotNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldFloatListNotNullIndex, RealmFieldType.FLOAT_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Float item : value) {
            if (item == null) {
                throw new IllegalArgumentException("Storing 'null' into fieldFloatListNotNull' is not allowed by the schema.");
            } else {
                osList.addFloat(item.floatValue());
            }
        }
    }

    @Override
    public RealmList<Float> realmGet$fieldFloatListNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldFloatListNullRealmList != null) {
            return fieldFloatListNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldFloatListNullIndex, RealmFieldType.FLOAT_LIST);
            fieldFloatListNullRealmList = new RealmList<java.lang.Float>(java.lang.Float.class, osList, proxyState.getRealm$realm());
            return fieldFloatListNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldFloatListNull(RealmList<Float> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldFloatListNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldFloatListNullIndex, RealmFieldType.FLOAT_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.lang.Float item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addFloat(item.floatValue());
            }
        }
    }

    @Override
    public RealmList<Date> realmGet$fieldDateListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldDateListNotNullRealmList != null) {
            return fieldDateListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDateListNotNullIndex, RealmFieldType.DATE_LIST);
            fieldDateListNotNullRealmList = new RealmList<java.util.Date>(java.util.Date.class, osList, proxyState.getRealm$realm());
            return fieldDateListNotNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldDateListNotNull(RealmList<Date> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldDateListNotNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDateListNotNullIndex, RealmFieldType.DATE_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.util.Date item : value) {
            if (item == null) {
                throw new IllegalArgumentException("Storing 'null' into fieldDateListNotNull' is not allowed by the schema.");
            } else {
                osList.addDate(item);
            }
        }
    }

    @Override
    public RealmList<Date> realmGet$fieldDateListNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldDateListNullRealmList != null) {
            return fieldDateListNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDateListNullIndex, RealmFieldType.DATE_LIST);
            fieldDateListNullRealmList = new RealmList<java.util.Date>(java.util.Date.class, osList, proxyState.getRealm$realm());
            return fieldDateListNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldDateListNull(RealmList<Date> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldDateListNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDateListNullIndex, RealmFieldType.DATE_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.util.Date item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addDate(item);
            }
        }
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("NullTypes", 41, 0);
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
        builder.addPersistedValueListProperty("fieldStringListNotNull", RealmFieldType.STRING_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldStringListNull", RealmFieldType.STRING_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldBinaryListNotNull", RealmFieldType.BINARY_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldBinaryListNull", RealmFieldType.BINARY_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldBooleanListNotNull", RealmFieldType.BOOLEAN_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldBooleanListNull", RealmFieldType.BOOLEAN_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldLongListNotNull", RealmFieldType.INTEGER_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldLongListNull", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldIntegerListNotNull", RealmFieldType.INTEGER_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldIntegerListNull", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldShortListNotNull", RealmFieldType.INTEGER_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldShortListNull", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldByteListNotNull", RealmFieldType.INTEGER_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldByteListNull", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldDoubleListNotNull", RealmFieldType.DOUBLE_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldDoubleListNull", RealmFieldType.DOUBLE_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldFloatListNotNull", RealmFieldType.FLOAT_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldFloatListNull", RealmFieldType.FLOAT_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldDateListNotNull", RealmFieldType.DATE_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty("fieldDateListNull", RealmFieldType.DATE_LIST, !Property.REQUIRED);
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static NullTypesColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new NullTypesColumnInfo(schemaInfo);
    }

    public static String getSimpleClassName() {
        return "NullTypes";
    }

    public static final class ClassNameHelper {
        public static final String INTERNAL_CLASS_NAME = "NullTypes";
    }

    @SuppressWarnings("cast")
    public static some.test.NullTypes createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        final List<String> excludeFields = new ArrayList<String>(21);
        if (json.has("fieldObjectNull")) {
            excludeFields.add("fieldObjectNull");
        }
        if (json.has("fieldStringListNotNull")) {
            excludeFields.add("fieldStringListNotNull");
        }
        if (json.has("fieldStringListNull")) {
            excludeFields.add("fieldStringListNull");
        }
        if (json.has("fieldBinaryListNotNull")) {
            excludeFields.add("fieldBinaryListNotNull");
        }
        if (json.has("fieldBinaryListNull")) {
            excludeFields.add("fieldBinaryListNull");
        }
        if (json.has("fieldBooleanListNotNull")) {
            excludeFields.add("fieldBooleanListNotNull");
        }
        if (json.has("fieldBooleanListNull")) {
            excludeFields.add("fieldBooleanListNull");
        }
        if (json.has("fieldLongListNotNull")) {
            excludeFields.add("fieldLongListNotNull");
        }
        if (json.has("fieldLongListNull")) {
            excludeFields.add("fieldLongListNull");
        }
        if (json.has("fieldIntegerListNotNull")) {
            excludeFields.add("fieldIntegerListNotNull");
        }
        if (json.has("fieldIntegerListNull")) {
            excludeFields.add("fieldIntegerListNull");
        }
        if (json.has("fieldShortListNotNull")) {
            excludeFields.add("fieldShortListNotNull");
        }
        if (json.has("fieldShortListNull")) {
            excludeFields.add("fieldShortListNull");
        }
        if (json.has("fieldByteListNotNull")) {
            excludeFields.add("fieldByteListNotNull");
        }
        if (json.has("fieldByteListNull")) {
            excludeFields.add("fieldByteListNull");
        }
        if (json.has("fieldDoubleListNotNull")) {
            excludeFields.add("fieldDoubleListNotNull");
        }
        if (json.has("fieldDoubleListNull")) {
            excludeFields.add("fieldDoubleListNull");
        }
        if (json.has("fieldFloatListNotNull")) {
            excludeFields.add("fieldFloatListNotNull");
        }
        if (json.has("fieldFloatListNull")) {
            excludeFields.add("fieldFloatListNull");
        }
        if (json.has("fieldDateListNotNull")) {
            excludeFields.add("fieldDateListNotNull");
        }
        if (json.has("fieldDateListNull")) {
            excludeFields.add("fieldDateListNull");
        }
        some.test.NullTypes obj = realm.createObjectInternal(some.test.NullTypes.class, true, excludeFields);

        final some_test_NullTypesRealmProxyInterface objProxy = (some_test_NullTypesRealmProxyInterface) obj;
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
                some.test.NullTypes fieldObjectNullObj = some_test_NullTypesRealmProxy.createOrUpdateUsingJsonObject(realm, json.getJSONObject("fieldObjectNull"), update);
                objProxy.realmSet$fieldObjectNull(fieldObjectNullObj);
            }
        }
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldStringListNotNull(), json, "fieldStringListNotNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldStringListNull(), json, "fieldStringListNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldBinaryListNotNull(), json, "fieldBinaryListNotNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldBinaryListNull(), json, "fieldBinaryListNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldBooleanListNotNull(), json, "fieldBooleanListNotNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldBooleanListNull(), json, "fieldBooleanListNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldLongListNotNull(), json, "fieldLongListNotNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldLongListNull(), json, "fieldLongListNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldIntegerListNotNull(), json, "fieldIntegerListNotNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldIntegerListNull(), json, "fieldIntegerListNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldShortListNotNull(), json, "fieldShortListNotNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldShortListNull(), json, "fieldShortListNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldByteListNotNull(), json, "fieldByteListNotNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldByteListNull(), json, "fieldByteListNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldDoubleListNotNull(), json, "fieldDoubleListNotNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldDoubleListNull(), json, "fieldDoubleListNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldFloatListNotNull(), json, "fieldFloatListNotNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldFloatListNull(), json, "fieldFloatListNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldDateListNotNull(), json, "fieldDateListNotNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldDateListNull(), json, "fieldDateListNull");
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.NullTypes createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        final some.test.NullTypes obj = new some.test.NullTypes();
        final some_test_NullTypesRealmProxyInterface objProxy = (some_test_NullTypesRealmProxyInterface) obj;
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
                    some.test.NullTypes fieldObjectNullObj = some_test_NullTypesRealmProxy.createUsingJsonStream(realm, reader);
                    objProxy.realmSet$fieldObjectNull(fieldObjectNullObj);
                }
            } else if (name.equals("fieldStringListNotNull")) {
                objProxy.realmSet$fieldStringListNotNull(ProxyUtils.createRealmListWithJsonStream(java.lang.String.class, reader));
            } else if (name.equals("fieldStringListNull")) {
                objProxy.realmSet$fieldStringListNull(ProxyUtils.createRealmListWithJsonStream(java.lang.String.class, reader));
            } else if (name.equals("fieldBinaryListNotNull")) {
                objProxy.realmSet$fieldBinaryListNotNull(ProxyUtils.createRealmListWithJsonStream(byte[].class, reader));
            } else if (name.equals("fieldBinaryListNull")) {
                objProxy.realmSet$fieldBinaryListNull(ProxyUtils.createRealmListWithJsonStream(byte[].class, reader));
            } else if (name.equals("fieldBooleanListNotNull")) {
                objProxy.realmSet$fieldBooleanListNotNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Boolean.class, reader));
            } else if (name.equals("fieldBooleanListNull")) {
                objProxy.realmSet$fieldBooleanListNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Boolean.class, reader));
            } else if (name.equals("fieldLongListNotNull")) {
                objProxy.realmSet$fieldLongListNotNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Long.class, reader));
            } else if (name.equals("fieldLongListNull")) {
                objProxy.realmSet$fieldLongListNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Long.class, reader));
            } else if (name.equals("fieldIntegerListNotNull")) {
                objProxy.realmSet$fieldIntegerListNotNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Integer.class, reader));
            } else if (name.equals("fieldIntegerListNull")) {
                objProxy.realmSet$fieldIntegerListNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Integer.class, reader));
            } else if (name.equals("fieldShortListNotNull")) {
                objProxy.realmSet$fieldShortListNotNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Short.class, reader));
            } else if (name.equals("fieldShortListNull")) {
                objProxy.realmSet$fieldShortListNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Short.class, reader));
            } else if (name.equals("fieldByteListNotNull")) {
                objProxy.realmSet$fieldByteListNotNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Byte.class, reader));
            } else if (name.equals("fieldByteListNull")) {
                objProxy.realmSet$fieldByteListNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Byte.class, reader));
            } else if (name.equals("fieldDoubleListNotNull")) {
                objProxy.realmSet$fieldDoubleListNotNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Double.class, reader));
            } else if (name.equals("fieldDoubleListNull")) {
                objProxy.realmSet$fieldDoubleListNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Double.class, reader));
            } else if (name.equals("fieldFloatListNotNull")) {
                objProxy.realmSet$fieldFloatListNotNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Float.class, reader));
            } else if (name.equals("fieldFloatListNull")) {
                objProxy.realmSet$fieldFloatListNull(ProxyUtils.createRealmListWithJsonStream(java.lang.Float.class, reader));
            } else if (name.equals("fieldDateListNotNull")) {
                objProxy.realmSet$fieldDateListNotNull(ProxyUtils.createRealmListWithJsonStream(java.util.Date.class, reader));
            } else if (name.equals("fieldDateListNull")) {
                objProxy.realmSet$fieldDateListNull(ProxyUtils.createRealmListWithJsonStream(java.util.Date.class, reader));
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

        some_test_NullTypesRealmProxyInterface realmObjectSource = (some_test_NullTypesRealmProxyInterface) newObject;
        some_test_NullTypesRealmProxyInterface realmObjectCopy = (some_test_NullTypesRealmProxyInterface) realmObject;

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
                realmObjectCopy.realmSet$fieldObjectNull(some_test_NullTypesRealmProxy.copyOrUpdate(realm, fieldObjectNullObj, update, cache));
            }
        }
        realmObjectCopy.realmSet$fieldStringListNotNull(realmObjectSource.realmGet$fieldStringListNotNull());
        realmObjectCopy.realmSet$fieldStringListNull(realmObjectSource.realmGet$fieldStringListNull());
        realmObjectCopy.realmSet$fieldBinaryListNotNull(realmObjectSource.realmGet$fieldBinaryListNotNull());
        realmObjectCopy.realmSet$fieldBinaryListNull(realmObjectSource.realmGet$fieldBinaryListNull());
        realmObjectCopy.realmSet$fieldBooleanListNotNull(realmObjectSource.realmGet$fieldBooleanListNotNull());
        realmObjectCopy.realmSet$fieldBooleanListNull(realmObjectSource.realmGet$fieldBooleanListNull());
        realmObjectCopy.realmSet$fieldLongListNotNull(realmObjectSource.realmGet$fieldLongListNotNull());
        realmObjectCopy.realmSet$fieldLongListNull(realmObjectSource.realmGet$fieldLongListNull());
        realmObjectCopy.realmSet$fieldIntegerListNotNull(realmObjectSource.realmGet$fieldIntegerListNotNull());
        realmObjectCopy.realmSet$fieldIntegerListNull(realmObjectSource.realmGet$fieldIntegerListNull());
        realmObjectCopy.realmSet$fieldShortListNotNull(realmObjectSource.realmGet$fieldShortListNotNull());
        realmObjectCopy.realmSet$fieldShortListNull(realmObjectSource.realmGet$fieldShortListNull());
        realmObjectCopy.realmSet$fieldByteListNotNull(realmObjectSource.realmGet$fieldByteListNotNull());
        realmObjectCopy.realmSet$fieldByteListNull(realmObjectSource.realmGet$fieldByteListNull());
        realmObjectCopy.realmSet$fieldDoubleListNotNull(realmObjectSource.realmGet$fieldDoubleListNotNull());
        realmObjectCopy.realmSet$fieldDoubleListNull(realmObjectSource.realmGet$fieldDoubleListNull());
        realmObjectCopy.realmSet$fieldFloatListNotNull(realmObjectSource.realmGet$fieldFloatListNotNull());
        realmObjectCopy.realmSet$fieldFloatListNull(realmObjectSource.realmGet$fieldFloatListNull());
        realmObjectCopy.realmSet$fieldDateListNotNull(realmObjectSource.realmGet$fieldDateListNotNull());
        realmObjectCopy.realmSet$fieldDateListNull(realmObjectSource.realmGet$fieldDateListNull());
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
        String realmGet$fieldStringNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNotNull();
        if (realmGet$fieldStringNotNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, realmGet$fieldStringNotNull, false);
        }
        String realmGet$fieldStringNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNull();
        if (realmGet$fieldStringNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, realmGet$fieldStringNull, false);
        }
        Boolean realmGet$fieldBooleanNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNotNull();
        if (realmGet$fieldBooleanNotNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, realmGet$fieldBooleanNotNull, false);
        }
        Boolean realmGet$fieldBooleanNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNull();
        if (realmGet$fieldBooleanNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, realmGet$fieldBooleanNull, false);
        }
        byte[] realmGet$fieldBytesNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNotNull();
        if (realmGet$fieldBytesNotNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, realmGet$fieldBytesNotNull, false);
        }
        byte[] realmGet$fieldBytesNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNull();
        if (realmGet$fieldBytesNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, realmGet$fieldBytesNull, false);
        }
        Number realmGet$fieldByteNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNotNull();
        if (realmGet$fieldByteNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, realmGet$fieldByteNotNull.longValue(), false);
        }
        Number realmGet$fieldByteNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNull();
        if (realmGet$fieldByteNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, realmGet$fieldByteNull.longValue(), false);
        }
        Number realmGet$fieldShortNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNotNull();
        if (realmGet$fieldShortNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, realmGet$fieldShortNotNull.longValue(), false);
        }
        Number realmGet$fieldShortNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNull();
        if (realmGet$fieldShortNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, realmGet$fieldShortNull.longValue(), false);
        }
        Number realmGet$fieldIntegerNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNotNull();
        if (realmGet$fieldIntegerNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, realmGet$fieldIntegerNotNull.longValue(), false);
        }
        Number realmGet$fieldIntegerNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNull();
        if (realmGet$fieldIntegerNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, realmGet$fieldIntegerNull.longValue(), false);
        }
        Number realmGet$fieldLongNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNotNull();
        if (realmGet$fieldLongNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, realmGet$fieldLongNotNull.longValue(), false);
        }
        Number realmGet$fieldLongNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNull();
        if (realmGet$fieldLongNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, realmGet$fieldLongNull.longValue(), false);
        }
        Float realmGet$fieldFloatNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNotNull();
        if (realmGet$fieldFloatNotNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, realmGet$fieldFloatNotNull, false);
        }
        Float realmGet$fieldFloatNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNull();
        if (realmGet$fieldFloatNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, realmGet$fieldFloatNull, false);
        }
        Double realmGet$fieldDoubleNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNotNull();
        if (realmGet$fieldDoubleNotNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, realmGet$fieldDoubleNotNull, false);
        }
        Double realmGet$fieldDoubleNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNull();
        if (realmGet$fieldDoubleNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, realmGet$fieldDoubleNull, false);
        }
        java.util.Date realmGet$fieldDateNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNotNull();
        if (realmGet$fieldDateNotNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, realmGet$fieldDateNotNull.getTime(), false);
        }
        java.util.Date realmGet$fieldDateNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNull();
        if (realmGet$fieldDateNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNullIndex, rowIndex, realmGet$fieldDateNull.getTime(), false);
        }

        some.test.NullTypes fieldObjectNullObj = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectNull();
        if (fieldObjectNullObj != null) {
            Long cachefieldObjectNull = cache.get(fieldObjectNullObj);
            if (cachefieldObjectNull == null) {
                cachefieldObjectNull = some_test_NullTypesRealmProxy.insert(realm, fieldObjectNullObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.fieldObjectNullIndex, rowIndex, cachefieldObjectNull, false);
        }

        RealmList<java.lang.String> fieldStringListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringListNotNull();
        if (fieldStringListNotNullList != null) {
            OsList fieldStringListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldStringListNotNullIndex);
            for (java.lang.String fieldStringListNotNullItem : fieldStringListNotNullList) {
                if (fieldStringListNotNullItem == null) {
                    fieldStringListNotNullOsList.addNull();
                } else {
                    fieldStringListNotNullOsList.addString(fieldStringListNotNullItem);
                }
            }
        }

        RealmList<java.lang.String> fieldStringListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringListNull();
        if (fieldStringListNullList != null) {
            OsList fieldStringListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldStringListNullIndex);
            for (java.lang.String fieldStringListNullItem : fieldStringListNullList) {
                if (fieldStringListNullItem == null) {
                    fieldStringListNullOsList.addNull();
                } else {
                    fieldStringListNullOsList.addString(fieldStringListNullItem);
                }
            }
        }

        RealmList<byte[]> fieldBinaryListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBinaryListNotNull();
        if (fieldBinaryListNotNullList != null) {
            OsList fieldBinaryListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBinaryListNotNullIndex);
            for (byte[] fieldBinaryListNotNullItem : fieldBinaryListNotNullList) {
                if (fieldBinaryListNotNullItem == null) {
                    fieldBinaryListNotNullOsList.addNull();
                } else {
                    fieldBinaryListNotNullOsList.addBinary(fieldBinaryListNotNullItem);
                }
            }
        }

        RealmList<byte[]> fieldBinaryListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBinaryListNull();
        if (fieldBinaryListNullList != null) {
            OsList fieldBinaryListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBinaryListNullIndex);
            for (byte[] fieldBinaryListNullItem : fieldBinaryListNullList) {
                if (fieldBinaryListNullItem == null) {
                    fieldBinaryListNullOsList.addNull();
                } else {
                    fieldBinaryListNullOsList.addBinary(fieldBinaryListNullItem);
                }
            }
        }

        RealmList<java.lang.Boolean> fieldBooleanListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanListNotNull();
        if (fieldBooleanListNotNullList != null) {
            OsList fieldBooleanListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBooleanListNotNullIndex);
            for (java.lang.Boolean fieldBooleanListNotNullItem : fieldBooleanListNotNullList) {
                if (fieldBooleanListNotNullItem == null) {
                    fieldBooleanListNotNullOsList.addNull();
                } else {
                    fieldBooleanListNotNullOsList.addBoolean(fieldBooleanListNotNullItem);
                }
            }
        }

        RealmList<java.lang.Boolean> fieldBooleanListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanListNull();
        if (fieldBooleanListNullList != null) {
            OsList fieldBooleanListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBooleanListNullIndex);
            for (java.lang.Boolean fieldBooleanListNullItem : fieldBooleanListNullList) {
                if (fieldBooleanListNullItem == null) {
                    fieldBooleanListNullOsList.addNull();
                } else {
                    fieldBooleanListNullOsList.addBoolean(fieldBooleanListNullItem);
                }
            }
        }

        RealmList<java.lang.Long> fieldLongListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongListNotNull();
        if (fieldLongListNotNullList != null) {
            OsList fieldLongListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldLongListNotNullIndex);
            for (java.lang.Long fieldLongListNotNullItem : fieldLongListNotNullList) {
                if (fieldLongListNotNullItem == null) {
                    fieldLongListNotNullOsList.addNull();
                } else {
                    fieldLongListNotNullOsList.addLong(fieldLongListNotNullItem.longValue());
                }
            }
        }

        RealmList<java.lang.Long> fieldLongListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongListNull();
        if (fieldLongListNullList != null) {
            OsList fieldLongListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldLongListNullIndex);
            for (java.lang.Long fieldLongListNullItem : fieldLongListNullList) {
                if (fieldLongListNullItem == null) {
                    fieldLongListNullOsList.addNull();
                } else {
                    fieldLongListNullOsList.addLong(fieldLongListNullItem.longValue());
                }
            }
        }

        RealmList<java.lang.Integer> fieldIntegerListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerListNotNull();
        if (fieldIntegerListNotNullList != null) {
            OsList fieldIntegerListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldIntegerListNotNullIndex);
            for (java.lang.Integer fieldIntegerListNotNullItem : fieldIntegerListNotNullList) {
                if (fieldIntegerListNotNullItem == null) {
                    fieldIntegerListNotNullOsList.addNull();
                } else {
                    fieldIntegerListNotNullOsList.addLong(fieldIntegerListNotNullItem.longValue());
                }
            }
        }

        RealmList<java.lang.Integer> fieldIntegerListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerListNull();
        if (fieldIntegerListNullList != null) {
            OsList fieldIntegerListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldIntegerListNullIndex);
            for (java.lang.Integer fieldIntegerListNullItem : fieldIntegerListNullList) {
                if (fieldIntegerListNullItem == null) {
                    fieldIntegerListNullOsList.addNull();
                } else {
                    fieldIntegerListNullOsList.addLong(fieldIntegerListNullItem.longValue());
                }
            }
        }

        RealmList<java.lang.Short> fieldShortListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortListNotNull();
        if (fieldShortListNotNullList != null) {
            OsList fieldShortListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldShortListNotNullIndex);
            for (java.lang.Short fieldShortListNotNullItem : fieldShortListNotNullList) {
                if (fieldShortListNotNullItem == null) {
                    fieldShortListNotNullOsList.addNull();
                } else {
                    fieldShortListNotNullOsList.addLong(fieldShortListNotNullItem.longValue());
                }
            }
        }

        RealmList<java.lang.Short> fieldShortListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortListNull();
        if (fieldShortListNullList != null) {
            OsList fieldShortListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldShortListNullIndex);
            for (java.lang.Short fieldShortListNullItem : fieldShortListNullList) {
                if (fieldShortListNullItem == null) {
                    fieldShortListNullOsList.addNull();
                } else {
                    fieldShortListNullOsList.addLong(fieldShortListNullItem.longValue());
                }
            }
        }

        RealmList<java.lang.Byte> fieldByteListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteListNotNull();
        if (fieldByteListNotNullList != null) {
            OsList fieldByteListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldByteListNotNullIndex);
            for (java.lang.Byte fieldByteListNotNullItem : fieldByteListNotNullList) {
                if (fieldByteListNotNullItem == null) {
                    fieldByteListNotNullOsList.addNull();
                } else {
                    fieldByteListNotNullOsList.addLong(fieldByteListNotNullItem.longValue());
                }
            }
        }

        RealmList<java.lang.Byte> fieldByteListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteListNull();
        if (fieldByteListNullList != null) {
            OsList fieldByteListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldByteListNullIndex);
            for (java.lang.Byte fieldByteListNullItem : fieldByteListNullList) {
                if (fieldByteListNullItem == null) {
                    fieldByteListNullOsList.addNull();
                } else {
                    fieldByteListNullOsList.addLong(fieldByteListNullItem.longValue());
                }
            }
        }

        RealmList<java.lang.Double> fieldDoubleListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleListNotNull();
        if (fieldDoubleListNotNullList != null) {
            OsList fieldDoubleListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDoubleListNotNullIndex);
            for (java.lang.Double fieldDoubleListNotNullItem : fieldDoubleListNotNullList) {
                if (fieldDoubleListNotNullItem == null) {
                    fieldDoubleListNotNullOsList.addNull();
                } else {
                    fieldDoubleListNotNullOsList.addDouble(fieldDoubleListNotNullItem.doubleValue());
                }
            }
        }

        RealmList<java.lang.Double> fieldDoubleListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleListNull();
        if (fieldDoubleListNullList != null) {
            OsList fieldDoubleListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDoubleListNullIndex);
            for (java.lang.Double fieldDoubleListNullItem : fieldDoubleListNullList) {
                if (fieldDoubleListNullItem == null) {
                    fieldDoubleListNullOsList.addNull();
                } else {
                    fieldDoubleListNullOsList.addDouble(fieldDoubleListNullItem.doubleValue());
                }
            }
        }

        RealmList<java.lang.Float> fieldFloatListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatListNotNull();
        if (fieldFloatListNotNullList != null) {
            OsList fieldFloatListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldFloatListNotNullIndex);
            for (java.lang.Float fieldFloatListNotNullItem : fieldFloatListNotNullList) {
                if (fieldFloatListNotNullItem == null) {
                    fieldFloatListNotNullOsList.addNull();
                } else {
                    fieldFloatListNotNullOsList.addFloat(fieldFloatListNotNullItem.floatValue());
                }
            }
        }

        RealmList<java.lang.Float> fieldFloatListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatListNull();
        if (fieldFloatListNullList != null) {
            OsList fieldFloatListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldFloatListNullIndex);
            for (java.lang.Float fieldFloatListNullItem : fieldFloatListNullList) {
                if (fieldFloatListNullItem == null) {
                    fieldFloatListNullOsList.addNull();
                } else {
                    fieldFloatListNullOsList.addFloat(fieldFloatListNullItem.floatValue());
                }
            }
        }

        RealmList<java.util.Date> fieldDateListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateListNotNull();
        if (fieldDateListNotNullList != null) {
            OsList fieldDateListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDateListNotNullIndex);
            for (java.util.Date fieldDateListNotNullItem : fieldDateListNotNullList) {
                if (fieldDateListNotNullItem == null) {
                    fieldDateListNotNullOsList.addNull();
                } else {
                    fieldDateListNotNullOsList.addDate(fieldDateListNotNullItem);
                }
            }
        }

        RealmList<java.util.Date> fieldDateListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateListNull();
        if (fieldDateListNullList != null) {
            OsList fieldDateListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDateListNullIndex);
            for (java.util.Date fieldDateListNullItem : fieldDateListNullList) {
                if (fieldDateListNullItem == null) {
                    fieldDateListNullOsList.addNull();
                } else {
                    fieldDateListNullOsList.addDate(fieldDateListNullItem);
                }
            }
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
            String realmGet$fieldStringNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNotNull();
            if (realmGet$fieldStringNotNull != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, realmGet$fieldStringNotNull, false);
            }
            String realmGet$fieldStringNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNull();
            if (realmGet$fieldStringNull != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, realmGet$fieldStringNull, false);
            }
            Boolean realmGet$fieldBooleanNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNotNull();
            if (realmGet$fieldBooleanNotNull != null) {
                Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, realmGet$fieldBooleanNotNull, false);
            }
            Boolean realmGet$fieldBooleanNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNull();
            if (realmGet$fieldBooleanNull != null) {
                Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, realmGet$fieldBooleanNull, false);
            }
            byte[] realmGet$fieldBytesNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNotNull();
            if (realmGet$fieldBytesNotNull != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, realmGet$fieldBytesNotNull, false);
            }
            byte[] realmGet$fieldBytesNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNull();
            if (realmGet$fieldBytesNull != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, realmGet$fieldBytesNull, false);
            }
            Number realmGet$fieldByteNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNotNull();
            if (realmGet$fieldByteNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, realmGet$fieldByteNotNull.longValue(), false);
            }
            Number realmGet$fieldByteNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNull();
            if (realmGet$fieldByteNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, realmGet$fieldByteNull.longValue(), false);
            }
            Number realmGet$fieldShortNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNotNull();
            if (realmGet$fieldShortNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, realmGet$fieldShortNotNull.longValue(), false);
            }
            Number realmGet$fieldShortNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNull();
            if (realmGet$fieldShortNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, realmGet$fieldShortNull.longValue(), false);
            }
            Number realmGet$fieldIntegerNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNotNull();
            if (realmGet$fieldIntegerNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, realmGet$fieldIntegerNotNull.longValue(), false);
            }
            Number realmGet$fieldIntegerNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNull();
            if (realmGet$fieldIntegerNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, realmGet$fieldIntegerNull.longValue(), false);
            }
            Number realmGet$fieldLongNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNotNull();
            if (realmGet$fieldLongNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, realmGet$fieldLongNotNull.longValue(), false);
            }
            Number realmGet$fieldLongNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNull();
            if (realmGet$fieldLongNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, realmGet$fieldLongNull.longValue(), false);
            }
            Float realmGet$fieldFloatNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNotNull();
            if (realmGet$fieldFloatNotNull != null) {
                Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, realmGet$fieldFloatNotNull, false);
            }
            Float realmGet$fieldFloatNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNull();
            if (realmGet$fieldFloatNull != null) {
                Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, realmGet$fieldFloatNull, false);
            }
            Double realmGet$fieldDoubleNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNotNull();
            if (realmGet$fieldDoubleNotNull != null) {
                Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, realmGet$fieldDoubleNotNull, false);
            }
            Double realmGet$fieldDoubleNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNull();
            if (realmGet$fieldDoubleNull != null) {
                Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, realmGet$fieldDoubleNull, false);
            }
            java.util.Date realmGet$fieldDateNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNotNull();
            if (realmGet$fieldDateNotNull != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, realmGet$fieldDateNotNull.getTime(), false);
            }
            java.util.Date realmGet$fieldDateNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNull();
            if (realmGet$fieldDateNull != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNullIndex, rowIndex, realmGet$fieldDateNull.getTime(), false);
            }

            some.test.NullTypes fieldObjectNullObj = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectNull();
            if (fieldObjectNullObj != null) {
                Long cachefieldObjectNull = cache.get(fieldObjectNullObj);
                if (cachefieldObjectNull == null) {
                    cachefieldObjectNull = some_test_NullTypesRealmProxy.insert(realm, fieldObjectNullObj, cache);
                }
                table.setLink(columnInfo.fieldObjectNullIndex, rowIndex, cachefieldObjectNull, false);
            }

            RealmList<java.lang.String> fieldStringListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringListNotNull();
            if (fieldStringListNotNullList != null) {
                OsList fieldStringListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldStringListNotNullIndex);
                for (java.lang.String fieldStringListNotNullItem : fieldStringListNotNullList) {
                    if (fieldStringListNotNullItem == null) {
                        fieldStringListNotNullOsList.addNull();
                    } else {
                        fieldStringListNotNullOsList.addString(fieldStringListNotNullItem);
                    }
                }
            }

            RealmList<java.lang.String> fieldStringListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringListNull();
            if (fieldStringListNullList != null) {
                OsList fieldStringListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldStringListNullIndex);
                for (java.lang.String fieldStringListNullItem : fieldStringListNullList) {
                    if (fieldStringListNullItem == null) {
                        fieldStringListNullOsList.addNull();
                    } else {
                        fieldStringListNullOsList.addString(fieldStringListNullItem);
                    }
                }
            }

            RealmList<byte[]> fieldBinaryListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBinaryListNotNull();
            if (fieldBinaryListNotNullList != null) {
                OsList fieldBinaryListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBinaryListNotNullIndex);
                for (byte[] fieldBinaryListNotNullItem : fieldBinaryListNotNullList) {
                    if (fieldBinaryListNotNullItem == null) {
                        fieldBinaryListNotNullOsList.addNull();
                    } else {
                        fieldBinaryListNotNullOsList.addBinary(fieldBinaryListNotNullItem);
                    }
                }
            }

            RealmList<byte[]> fieldBinaryListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBinaryListNull();
            if (fieldBinaryListNullList != null) {
                OsList fieldBinaryListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBinaryListNullIndex);
                for (byte[] fieldBinaryListNullItem : fieldBinaryListNullList) {
                    if (fieldBinaryListNullItem == null) {
                        fieldBinaryListNullOsList.addNull();
                    } else {
                        fieldBinaryListNullOsList.addBinary(fieldBinaryListNullItem);
                    }
                }
            }

            RealmList<java.lang.Boolean> fieldBooleanListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanListNotNull();
            if (fieldBooleanListNotNullList != null) {
                OsList fieldBooleanListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBooleanListNotNullIndex);
                for (java.lang.Boolean fieldBooleanListNotNullItem : fieldBooleanListNotNullList) {
                    if (fieldBooleanListNotNullItem == null) {
                        fieldBooleanListNotNullOsList.addNull();
                    } else {
                        fieldBooleanListNotNullOsList.addBoolean(fieldBooleanListNotNullItem);
                    }
                }
            }

            RealmList<java.lang.Boolean> fieldBooleanListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanListNull();
            if (fieldBooleanListNullList != null) {
                OsList fieldBooleanListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBooleanListNullIndex);
                for (java.lang.Boolean fieldBooleanListNullItem : fieldBooleanListNullList) {
                    if (fieldBooleanListNullItem == null) {
                        fieldBooleanListNullOsList.addNull();
                    } else {
                        fieldBooleanListNullOsList.addBoolean(fieldBooleanListNullItem);
                    }
                }
            }

            RealmList<java.lang.Long> fieldLongListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongListNotNull();
            if (fieldLongListNotNullList != null) {
                OsList fieldLongListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldLongListNotNullIndex);
                for (java.lang.Long fieldLongListNotNullItem : fieldLongListNotNullList) {
                    if (fieldLongListNotNullItem == null) {
                        fieldLongListNotNullOsList.addNull();
                    } else {
                        fieldLongListNotNullOsList.addLong(fieldLongListNotNullItem.longValue());
                    }
                }
            }

            RealmList<java.lang.Long> fieldLongListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongListNull();
            if (fieldLongListNullList != null) {
                OsList fieldLongListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldLongListNullIndex);
                for (java.lang.Long fieldLongListNullItem : fieldLongListNullList) {
                    if (fieldLongListNullItem == null) {
                        fieldLongListNullOsList.addNull();
                    } else {
                        fieldLongListNullOsList.addLong(fieldLongListNullItem.longValue());
                    }
                }
            }

            RealmList<java.lang.Integer> fieldIntegerListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerListNotNull();
            if (fieldIntegerListNotNullList != null) {
                OsList fieldIntegerListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldIntegerListNotNullIndex);
                for (java.lang.Integer fieldIntegerListNotNullItem : fieldIntegerListNotNullList) {
                    if (fieldIntegerListNotNullItem == null) {
                        fieldIntegerListNotNullOsList.addNull();
                    } else {
                        fieldIntegerListNotNullOsList.addLong(fieldIntegerListNotNullItem.longValue());
                    }
                }
            }

            RealmList<java.lang.Integer> fieldIntegerListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerListNull();
            if (fieldIntegerListNullList != null) {
                OsList fieldIntegerListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldIntegerListNullIndex);
                for (java.lang.Integer fieldIntegerListNullItem : fieldIntegerListNullList) {
                    if (fieldIntegerListNullItem == null) {
                        fieldIntegerListNullOsList.addNull();
                    } else {
                        fieldIntegerListNullOsList.addLong(fieldIntegerListNullItem.longValue());
                    }
                }
            }

            RealmList<java.lang.Short> fieldShortListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortListNotNull();
            if (fieldShortListNotNullList != null) {
                OsList fieldShortListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldShortListNotNullIndex);
                for (java.lang.Short fieldShortListNotNullItem : fieldShortListNotNullList) {
                    if (fieldShortListNotNullItem == null) {
                        fieldShortListNotNullOsList.addNull();
                    } else {
                        fieldShortListNotNullOsList.addLong(fieldShortListNotNullItem.longValue());
                    }
                }
            }

            RealmList<java.lang.Short> fieldShortListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortListNull();
            if (fieldShortListNullList != null) {
                OsList fieldShortListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldShortListNullIndex);
                for (java.lang.Short fieldShortListNullItem : fieldShortListNullList) {
                    if (fieldShortListNullItem == null) {
                        fieldShortListNullOsList.addNull();
                    } else {
                        fieldShortListNullOsList.addLong(fieldShortListNullItem.longValue());
                    }
                }
            }

            RealmList<java.lang.Byte> fieldByteListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteListNotNull();
            if (fieldByteListNotNullList != null) {
                OsList fieldByteListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldByteListNotNullIndex);
                for (java.lang.Byte fieldByteListNotNullItem : fieldByteListNotNullList) {
                    if (fieldByteListNotNullItem == null) {
                        fieldByteListNotNullOsList.addNull();
                    } else {
                        fieldByteListNotNullOsList.addLong(fieldByteListNotNullItem.longValue());
                    }
                }
            }

            RealmList<java.lang.Byte> fieldByteListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteListNull();
            if (fieldByteListNullList != null) {
                OsList fieldByteListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldByteListNullIndex);
                for (java.lang.Byte fieldByteListNullItem : fieldByteListNullList) {
                    if (fieldByteListNullItem == null) {
                        fieldByteListNullOsList.addNull();
                    } else {
                        fieldByteListNullOsList.addLong(fieldByteListNullItem.longValue());
                    }
                }
            }

            RealmList<java.lang.Double> fieldDoubleListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleListNotNull();
            if (fieldDoubleListNotNullList != null) {
                OsList fieldDoubleListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDoubleListNotNullIndex);
                for (java.lang.Double fieldDoubleListNotNullItem : fieldDoubleListNotNullList) {
                    if (fieldDoubleListNotNullItem == null) {
                        fieldDoubleListNotNullOsList.addNull();
                    } else {
                        fieldDoubleListNotNullOsList.addDouble(fieldDoubleListNotNullItem.doubleValue());
                    }
                }
            }

            RealmList<java.lang.Double> fieldDoubleListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleListNull();
            if (fieldDoubleListNullList != null) {
                OsList fieldDoubleListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDoubleListNullIndex);
                for (java.lang.Double fieldDoubleListNullItem : fieldDoubleListNullList) {
                    if (fieldDoubleListNullItem == null) {
                        fieldDoubleListNullOsList.addNull();
                    } else {
                        fieldDoubleListNullOsList.addDouble(fieldDoubleListNullItem.doubleValue());
                    }
                }
            }

            RealmList<java.lang.Float> fieldFloatListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatListNotNull();
            if (fieldFloatListNotNullList != null) {
                OsList fieldFloatListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldFloatListNotNullIndex);
                for (java.lang.Float fieldFloatListNotNullItem : fieldFloatListNotNullList) {
                    if (fieldFloatListNotNullItem == null) {
                        fieldFloatListNotNullOsList.addNull();
                    } else {
                        fieldFloatListNotNullOsList.addFloat(fieldFloatListNotNullItem.floatValue());
                    }
                }
            }

            RealmList<java.lang.Float> fieldFloatListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatListNull();
            if (fieldFloatListNullList != null) {
                OsList fieldFloatListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldFloatListNullIndex);
                for (java.lang.Float fieldFloatListNullItem : fieldFloatListNullList) {
                    if (fieldFloatListNullItem == null) {
                        fieldFloatListNullOsList.addNull();
                    } else {
                        fieldFloatListNullOsList.addFloat(fieldFloatListNullItem.floatValue());
                    }
                }
            }

            RealmList<java.util.Date> fieldDateListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateListNotNull();
            if (fieldDateListNotNullList != null) {
                OsList fieldDateListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDateListNotNullIndex);
                for (java.util.Date fieldDateListNotNullItem : fieldDateListNotNullList) {
                    if (fieldDateListNotNullItem == null) {
                        fieldDateListNotNullOsList.addNull();
                    } else {
                        fieldDateListNotNullOsList.addDate(fieldDateListNotNullItem);
                    }
                }
            }

            RealmList<java.util.Date> fieldDateListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateListNull();
            if (fieldDateListNullList != null) {
                OsList fieldDateListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDateListNullIndex);
                for (java.util.Date fieldDateListNullItem : fieldDateListNullList) {
                    if (fieldDateListNullItem == null) {
                        fieldDateListNullOsList.addNull();
                    } else {
                        fieldDateListNullOsList.addDate(fieldDateListNullItem);
                    }
                }
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
        String realmGet$fieldStringNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNotNull();
        if (realmGet$fieldStringNotNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, realmGet$fieldStringNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, false);
        }
        String realmGet$fieldStringNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNull();
        if (realmGet$fieldStringNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, realmGet$fieldStringNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, false);
        }
        Boolean realmGet$fieldBooleanNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNotNull();
        if (realmGet$fieldBooleanNotNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, realmGet$fieldBooleanNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, false);
        }
        Boolean realmGet$fieldBooleanNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNull();
        if (realmGet$fieldBooleanNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, realmGet$fieldBooleanNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, false);
        }
        byte[] realmGet$fieldBytesNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNotNull();
        if (realmGet$fieldBytesNotNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, realmGet$fieldBytesNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, false);
        }
        byte[] realmGet$fieldBytesNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNull();
        if (realmGet$fieldBytesNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, realmGet$fieldBytesNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, false);
        }
        Number realmGet$fieldByteNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNotNull();
        if (realmGet$fieldByteNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, realmGet$fieldByteNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, false);
        }
        Number realmGet$fieldByteNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNull();
        if (realmGet$fieldByteNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, realmGet$fieldByteNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, false);
        }
        Number realmGet$fieldShortNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNotNull();
        if (realmGet$fieldShortNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, realmGet$fieldShortNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, false);
        }
        Number realmGet$fieldShortNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNull();
        if (realmGet$fieldShortNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, realmGet$fieldShortNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, false);
        }
        Number realmGet$fieldIntegerNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNotNull();
        if (realmGet$fieldIntegerNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, realmGet$fieldIntegerNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, false);
        }
        Number realmGet$fieldIntegerNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNull();
        if (realmGet$fieldIntegerNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, realmGet$fieldIntegerNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, false);
        }
        Number realmGet$fieldLongNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNotNull();
        if (realmGet$fieldLongNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, realmGet$fieldLongNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, false);
        }
        Number realmGet$fieldLongNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNull();
        if (realmGet$fieldLongNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, realmGet$fieldLongNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, false);
        }
        Float realmGet$fieldFloatNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNotNull();
        if (realmGet$fieldFloatNotNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, realmGet$fieldFloatNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, false);
        }
        Float realmGet$fieldFloatNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNull();
        if (realmGet$fieldFloatNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, realmGet$fieldFloatNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, false);
        }
        Double realmGet$fieldDoubleNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNotNull();
        if (realmGet$fieldDoubleNotNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, realmGet$fieldDoubleNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, false);
        }
        Double realmGet$fieldDoubleNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNull();
        if (realmGet$fieldDoubleNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, realmGet$fieldDoubleNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, false);
        }
        java.util.Date realmGet$fieldDateNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNotNull();
        if (realmGet$fieldDateNotNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, realmGet$fieldDateNotNull.getTime(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, false);
        }
        java.util.Date realmGet$fieldDateNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNull();
        if (realmGet$fieldDateNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNullIndex, rowIndex, realmGet$fieldDateNull.getTime(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNullIndex, rowIndex, false);
        }

        some.test.NullTypes fieldObjectNullObj = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectNull();
        if (fieldObjectNullObj != null) {
            Long cachefieldObjectNull = cache.get(fieldObjectNullObj);
            if (cachefieldObjectNull == null) {
                cachefieldObjectNull = some_test_NullTypesRealmProxy.insertOrUpdate(realm, fieldObjectNullObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.fieldObjectNullIndex, rowIndex, cachefieldObjectNull, false);
        } else {
            Table.nativeNullifyLink(tableNativePtr, columnInfo.fieldObjectNullIndex, rowIndex);
        }

        OsList fieldStringListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldStringListNotNullIndex);
        fieldStringListNotNullOsList.removeAll();
        RealmList<java.lang.String> fieldStringListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringListNotNull();
        if (fieldStringListNotNullList != null) {
            for (java.lang.String fieldStringListNotNullItem : fieldStringListNotNullList) {
                if (fieldStringListNotNullItem == null) {
                    fieldStringListNotNullOsList.addNull();
                } else {
                    fieldStringListNotNullOsList.addString(fieldStringListNotNullItem);
                }
            }
        }


        OsList fieldStringListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldStringListNullIndex);
        fieldStringListNullOsList.removeAll();
        RealmList<java.lang.String> fieldStringListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringListNull();
        if (fieldStringListNullList != null) {
            for (java.lang.String fieldStringListNullItem : fieldStringListNullList) {
                if (fieldStringListNullItem == null) {
                    fieldStringListNullOsList.addNull();
                } else {
                    fieldStringListNullOsList.addString(fieldStringListNullItem);
                }
            }
        }


        OsList fieldBinaryListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBinaryListNotNullIndex);
        fieldBinaryListNotNullOsList.removeAll();
        RealmList<byte[]> fieldBinaryListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBinaryListNotNull();
        if (fieldBinaryListNotNullList != null) {
            for (byte[] fieldBinaryListNotNullItem : fieldBinaryListNotNullList) {
                if (fieldBinaryListNotNullItem == null) {
                    fieldBinaryListNotNullOsList.addNull();
                } else {
                    fieldBinaryListNotNullOsList.addBinary(fieldBinaryListNotNullItem);
                }
            }
        }


        OsList fieldBinaryListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBinaryListNullIndex);
        fieldBinaryListNullOsList.removeAll();
        RealmList<byte[]> fieldBinaryListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBinaryListNull();
        if (fieldBinaryListNullList != null) {
            for (byte[] fieldBinaryListNullItem : fieldBinaryListNullList) {
                if (fieldBinaryListNullItem == null) {
                    fieldBinaryListNullOsList.addNull();
                } else {
                    fieldBinaryListNullOsList.addBinary(fieldBinaryListNullItem);
                }
            }
        }


        OsList fieldBooleanListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBooleanListNotNullIndex);
        fieldBooleanListNotNullOsList.removeAll();
        RealmList<java.lang.Boolean> fieldBooleanListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanListNotNull();
        if (fieldBooleanListNotNullList != null) {
            for (java.lang.Boolean fieldBooleanListNotNullItem : fieldBooleanListNotNullList) {
                if (fieldBooleanListNotNullItem == null) {
                    fieldBooleanListNotNullOsList.addNull();
                } else {
                    fieldBooleanListNotNullOsList.addBoolean(fieldBooleanListNotNullItem);
                }
            }
        }


        OsList fieldBooleanListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBooleanListNullIndex);
        fieldBooleanListNullOsList.removeAll();
        RealmList<java.lang.Boolean> fieldBooleanListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanListNull();
        if (fieldBooleanListNullList != null) {
            for (java.lang.Boolean fieldBooleanListNullItem : fieldBooleanListNullList) {
                if (fieldBooleanListNullItem == null) {
                    fieldBooleanListNullOsList.addNull();
                } else {
                    fieldBooleanListNullOsList.addBoolean(fieldBooleanListNullItem);
                }
            }
        }


        OsList fieldLongListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldLongListNotNullIndex);
        fieldLongListNotNullOsList.removeAll();
        RealmList<java.lang.Long> fieldLongListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongListNotNull();
        if (fieldLongListNotNullList != null) {
            for (java.lang.Long fieldLongListNotNullItem : fieldLongListNotNullList) {
                if (fieldLongListNotNullItem == null) {
                    fieldLongListNotNullOsList.addNull();
                } else {
                    fieldLongListNotNullOsList.addLong(fieldLongListNotNullItem.longValue());
                }
            }
        }


        OsList fieldLongListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldLongListNullIndex);
        fieldLongListNullOsList.removeAll();
        RealmList<java.lang.Long> fieldLongListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongListNull();
        if (fieldLongListNullList != null) {
            for (java.lang.Long fieldLongListNullItem : fieldLongListNullList) {
                if (fieldLongListNullItem == null) {
                    fieldLongListNullOsList.addNull();
                } else {
                    fieldLongListNullOsList.addLong(fieldLongListNullItem.longValue());
                }
            }
        }


        OsList fieldIntegerListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldIntegerListNotNullIndex);
        fieldIntegerListNotNullOsList.removeAll();
        RealmList<java.lang.Integer> fieldIntegerListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerListNotNull();
        if (fieldIntegerListNotNullList != null) {
            for (java.lang.Integer fieldIntegerListNotNullItem : fieldIntegerListNotNullList) {
                if (fieldIntegerListNotNullItem == null) {
                    fieldIntegerListNotNullOsList.addNull();
                } else {
                    fieldIntegerListNotNullOsList.addLong(fieldIntegerListNotNullItem.longValue());
                }
            }
        }


        OsList fieldIntegerListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldIntegerListNullIndex);
        fieldIntegerListNullOsList.removeAll();
        RealmList<java.lang.Integer> fieldIntegerListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerListNull();
        if (fieldIntegerListNullList != null) {
            for (java.lang.Integer fieldIntegerListNullItem : fieldIntegerListNullList) {
                if (fieldIntegerListNullItem == null) {
                    fieldIntegerListNullOsList.addNull();
                } else {
                    fieldIntegerListNullOsList.addLong(fieldIntegerListNullItem.longValue());
                }
            }
        }


        OsList fieldShortListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldShortListNotNullIndex);
        fieldShortListNotNullOsList.removeAll();
        RealmList<java.lang.Short> fieldShortListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortListNotNull();
        if (fieldShortListNotNullList != null) {
            for (java.lang.Short fieldShortListNotNullItem : fieldShortListNotNullList) {
                if (fieldShortListNotNullItem == null) {
                    fieldShortListNotNullOsList.addNull();
                } else {
                    fieldShortListNotNullOsList.addLong(fieldShortListNotNullItem.longValue());
                }
            }
        }


        OsList fieldShortListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldShortListNullIndex);
        fieldShortListNullOsList.removeAll();
        RealmList<java.lang.Short> fieldShortListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortListNull();
        if (fieldShortListNullList != null) {
            for (java.lang.Short fieldShortListNullItem : fieldShortListNullList) {
                if (fieldShortListNullItem == null) {
                    fieldShortListNullOsList.addNull();
                } else {
                    fieldShortListNullOsList.addLong(fieldShortListNullItem.longValue());
                }
            }
        }


        OsList fieldByteListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldByteListNotNullIndex);
        fieldByteListNotNullOsList.removeAll();
        RealmList<java.lang.Byte> fieldByteListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteListNotNull();
        if (fieldByteListNotNullList != null) {
            for (java.lang.Byte fieldByteListNotNullItem : fieldByteListNotNullList) {
                if (fieldByteListNotNullItem == null) {
                    fieldByteListNotNullOsList.addNull();
                } else {
                    fieldByteListNotNullOsList.addLong(fieldByteListNotNullItem.longValue());
                }
            }
        }


        OsList fieldByteListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldByteListNullIndex);
        fieldByteListNullOsList.removeAll();
        RealmList<java.lang.Byte> fieldByteListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteListNull();
        if (fieldByteListNullList != null) {
            for (java.lang.Byte fieldByteListNullItem : fieldByteListNullList) {
                if (fieldByteListNullItem == null) {
                    fieldByteListNullOsList.addNull();
                } else {
                    fieldByteListNullOsList.addLong(fieldByteListNullItem.longValue());
                }
            }
        }


        OsList fieldDoubleListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDoubleListNotNullIndex);
        fieldDoubleListNotNullOsList.removeAll();
        RealmList<java.lang.Double> fieldDoubleListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleListNotNull();
        if (fieldDoubleListNotNullList != null) {
            for (java.lang.Double fieldDoubleListNotNullItem : fieldDoubleListNotNullList) {
                if (fieldDoubleListNotNullItem == null) {
                    fieldDoubleListNotNullOsList.addNull();
                } else {
                    fieldDoubleListNotNullOsList.addDouble(fieldDoubleListNotNullItem.doubleValue());
                }
            }
        }


        OsList fieldDoubleListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDoubleListNullIndex);
        fieldDoubleListNullOsList.removeAll();
        RealmList<java.lang.Double> fieldDoubleListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleListNull();
        if (fieldDoubleListNullList != null) {
            for (java.lang.Double fieldDoubleListNullItem : fieldDoubleListNullList) {
                if (fieldDoubleListNullItem == null) {
                    fieldDoubleListNullOsList.addNull();
                } else {
                    fieldDoubleListNullOsList.addDouble(fieldDoubleListNullItem.doubleValue());
                }
            }
        }


        OsList fieldFloatListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldFloatListNotNullIndex);
        fieldFloatListNotNullOsList.removeAll();
        RealmList<java.lang.Float> fieldFloatListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatListNotNull();
        if (fieldFloatListNotNullList != null) {
            for (java.lang.Float fieldFloatListNotNullItem : fieldFloatListNotNullList) {
                if (fieldFloatListNotNullItem == null) {
                    fieldFloatListNotNullOsList.addNull();
                } else {
                    fieldFloatListNotNullOsList.addFloat(fieldFloatListNotNullItem.floatValue());
                }
            }
        }


        OsList fieldFloatListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldFloatListNullIndex);
        fieldFloatListNullOsList.removeAll();
        RealmList<java.lang.Float> fieldFloatListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatListNull();
        if (fieldFloatListNullList != null) {
            for (java.lang.Float fieldFloatListNullItem : fieldFloatListNullList) {
                if (fieldFloatListNullItem == null) {
                    fieldFloatListNullOsList.addNull();
                } else {
                    fieldFloatListNullOsList.addFloat(fieldFloatListNullItem.floatValue());
                }
            }
        }


        OsList fieldDateListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDateListNotNullIndex);
        fieldDateListNotNullOsList.removeAll();
        RealmList<java.util.Date> fieldDateListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateListNotNull();
        if (fieldDateListNotNullList != null) {
            for (java.util.Date fieldDateListNotNullItem : fieldDateListNotNullList) {
                if (fieldDateListNotNullItem == null) {
                    fieldDateListNotNullOsList.addNull();
                } else {
                    fieldDateListNotNullOsList.addDate(fieldDateListNotNullItem);
                }
            }
        }


        OsList fieldDateListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDateListNullIndex);
        fieldDateListNullOsList.removeAll();
        RealmList<java.util.Date> fieldDateListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateListNull();
        if (fieldDateListNullList != null) {
            for (java.util.Date fieldDateListNullItem : fieldDateListNullList) {
                if (fieldDateListNullItem == null) {
                    fieldDateListNullOsList.addNull();
                } else {
                    fieldDateListNullOsList.addDate(fieldDateListNullItem);
                }
            }
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
            String realmGet$fieldStringNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNotNull();
            if (realmGet$fieldStringNotNull != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, realmGet$fieldStringNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNotNullIndex, rowIndex, false);
            }
            String realmGet$fieldStringNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNull();
            if (realmGet$fieldStringNull != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, realmGet$fieldStringNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNullIndex, rowIndex, false);
            }
            Boolean realmGet$fieldBooleanNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNotNull();
            if (realmGet$fieldBooleanNotNull != null) {
                Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, realmGet$fieldBooleanNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNotNullIndex, rowIndex, false);
            }
            Boolean realmGet$fieldBooleanNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNull();
            if (realmGet$fieldBooleanNull != null) {
                Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, realmGet$fieldBooleanNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNullIndex, rowIndex, false);
            }
            byte[] realmGet$fieldBytesNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNotNull();
            if (realmGet$fieldBytesNotNull != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, realmGet$fieldBytesNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNotNullIndex, rowIndex, false);
            }
            byte[] realmGet$fieldBytesNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNull();
            if (realmGet$fieldBytesNull != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, realmGet$fieldBytesNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNullIndex, rowIndex, false);
            }
            Number realmGet$fieldByteNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNotNull();
            if (realmGet$fieldByteNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, realmGet$fieldByteNotNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNotNullIndex, rowIndex, false);
            }
            Number realmGet$fieldByteNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNull();
            if (realmGet$fieldByteNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, realmGet$fieldByteNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNullIndex, rowIndex, false);
            }
            Number realmGet$fieldShortNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNotNull();
            if (realmGet$fieldShortNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, realmGet$fieldShortNotNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNotNullIndex, rowIndex, false);
            }
            Number realmGet$fieldShortNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNull();
            if (realmGet$fieldShortNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, realmGet$fieldShortNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNullIndex, rowIndex, false);
            }
            Number realmGet$fieldIntegerNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNotNull();
            if (realmGet$fieldIntegerNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, realmGet$fieldIntegerNotNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNotNullIndex, rowIndex, false);
            }
            Number realmGet$fieldIntegerNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNull();
            if (realmGet$fieldIntegerNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, realmGet$fieldIntegerNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNullIndex, rowIndex, false);
            }
            Number realmGet$fieldLongNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNotNull();
            if (realmGet$fieldLongNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, realmGet$fieldLongNotNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNotNullIndex, rowIndex, false);
            }
            Number realmGet$fieldLongNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNull();
            if (realmGet$fieldLongNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, realmGet$fieldLongNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNullIndex, rowIndex, false);
            }
            Float realmGet$fieldFloatNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNotNull();
            if (realmGet$fieldFloatNotNull != null) {
                Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, realmGet$fieldFloatNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNotNullIndex, rowIndex, false);
            }
            Float realmGet$fieldFloatNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNull();
            if (realmGet$fieldFloatNull != null) {
                Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, realmGet$fieldFloatNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNullIndex, rowIndex, false);
            }
            Double realmGet$fieldDoubleNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNotNull();
            if (realmGet$fieldDoubleNotNull != null) {
                Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, realmGet$fieldDoubleNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNotNullIndex, rowIndex, false);
            }
            Double realmGet$fieldDoubleNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNull();
            if (realmGet$fieldDoubleNull != null) {
                Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, realmGet$fieldDoubleNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNullIndex, rowIndex, false);
            }
            java.util.Date realmGet$fieldDateNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNotNull();
            if (realmGet$fieldDateNotNull != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, realmGet$fieldDateNotNull.getTime(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNotNullIndex, rowIndex, false);
            }
            java.util.Date realmGet$fieldDateNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNull();
            if (realmGet$fieldDateNull != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNullIndex, rowIndex, realmGet$fieldDateNull.getTime(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNullIndex, rowIndex, false);
            }

            some.test.NullTypes fieldObjectNullObj = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectNull();
            if (fieldObjectNullObj != null) {
                Long cachefieldObjectNull = cache.get(fieldObjectNullObj);
                if (cachefieldObjectNull == null) {
                    cachefieldObjectNull = some_test_NullTypesRealmProxy.insertOrUpdate(realm, fieldObjectNullObj, cache);
                }
                Table.nativeSetLink(tableNativePtr, columnInfo.fieldObjectNullIndex, rowIndex, cachefieldObjectNull, false);
            } else {
                Table.nativeNullifyLink(tableNativePtr, columnInfo.fieldObjectNullIndex, rowIndex);
            }

            OsList fieldStringListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldStringListNotNullIndex);
            fieldStringListNotNullOsList.removeAll();
            RealmList<java.lang.String> fieldStringListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringListNotNull();
            if (fieldStringListNotNullList != null) {
                for (java.lang.String fieldStringListNotNullItem : fieldStringListNotNullList) {
                    if (fieldStringListNotNullItem == null) {
                        fieldStringListNotNullOsList.addNull();
                    } else {
                        fieldStringListNotNullOsList.addString(fieldStringListNotNullItem);
                    }
                }
            }


            OsList fieldStringListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldStringListNullIndex);
            fieldStringListNullOsList.removeAll();
            RealmList<java.lang.String> fieldStringListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringListNull();
            if (fieldStringListNullList != null) {
                for (java.lang.String fieldStringListNullItem : fieldStringListNullList) {
                    if (fieldStringListNullItem == null) {
                        fieldStringListNullOsList.addNull();
                    } else {
                        fieldStringListNullOsList.addString(fieldStringListNullItem);
                    }
                }
            }


            OsList fieldBinaryListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBinaryListNotNullIndex);
            fieldBinaryListNotNullOsList.removeAll();
            RealmList<byte[]> fieldBinaryListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBinaryListNotNull();
            if (fieldBinaryListNotNullList != null) {
                for (byte[] fieldBinaryListNotNullItem : fieldBinaryListNotNullList) {
                    if (fieldBinaryListNotNullItem == null) {
                        fieldBinaryListNotNullOsList.addNull();
                    } else {
                        fieldBinaryListNotNullOsList.addBinary(fieldBinaryListNotNullItem);
                    }
                }
            }


            OsList fieldBinaryListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBinaryListNullIndex);
            fieldBinaryListNullOsList.removeAll();
            RealmList<byte[]> fieldBinaryListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBinaryListNull();
            if (fieldBinaryListNullList != null) {
                for (byte[] fieldBinaryListNullItem : fieldBinaryListNullList) {
                    if (fieldBinaryListNullItem == null) {
                        fieldBinaryListNullOsList.addNull();
                    } else {
                        fieldBinaryListNullOsList.addBinary(fieldBinaryListNullItem);
                    }
                }
            }


            OsList fieldBooleanListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBooleanListNotNullIndex);
            fieldBooleanListNotNullOsList.removeAll();
            RealmList<java.lang.Boolean> fieldBooleanListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanListNotNull();
            if (fieldBooleanListNotNullList != null) {
                for (java.lang.Boolean fieldBooleanListNotNullItem : fieldBooleanListNotNullList) {
                    if (fieldBooleanListNotNullItem == null) {
                        fieldBooleanListNotNullOsList.addNull();
                    } else {
                        fieldBooleanListNotNullOsList.addBoolean(fieldBooleanListNotNullItem);
                    }
                }
            }


            OsList fieldBooleanListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldBooleanListNullIndex);
            fieldBooleanListNullOsList.removeAll();
            RealmList<java.lang.Boolean> fieldBooleanListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanListNull();
            if (fieldBooleanListNullList != null) {
                for (java.lang.Boolean fieldBooleanListNullItem : fieldBooleanListNullList) {
                    if (fieldBooleanListNullItem == null) {
                        fieldBooleanListNullOsList.addNull();
                    } else {
                        fieldBooleanListNullOsList.addBoolean(fieldBooleanListNullItem);
                    }
                }
            }


            OsList fieldLongListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldLongListNotNullIndex);
            fieldLongListNotNullOsList.removeAll();
            RealmList<java.lang.Long> fieldLongListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongListNotNull();
            if (fieldLongListNotNullList != null) {
                for (java.lang.Long fieldLongListNotNullItem : fieldLongListNotNullList) {
                    if (fieldLongListNotNullItem == null) {
                        fieldLongListNotNullOsList.addNull();
                    } else {
                        fieldLongListNotNullOsList.addLong(fieldLongListNotNullItem.longValue());
                    }
                }
            }


            OsList fieldLongListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldLongListNullIndex);
            fieldLongListNullOsList.removeAll();
            RealmList<java.lang.Long> fieldLongListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongListNull();
            if (fieldLongListNullList != null) {
                for (java.lang.Long fieldLongListNullItem : fieldLongListNullList) {
                    if (fieldLongListNullItem == null) {
                        fieldLongListNullOsList.addNull();
                    } else {
                        fieldLongListNullOsList.addLong(fieldLongListNullItem.longValue());
                    }
                }
            }


            OsList fieldIntegerListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldIntegerListNotNullIndex);
            fieldIntegerListNotNullOsList.removeAll();
            RealmList<java.lang.Integer> fieldIntegerListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerListNotNull();
            if (fieldIntegerListNotNullList != null) {
                for (java.lang.Integer fieldIntegerListNotNullItem : fieldIntegerListNotNullList) {
                    if (fieldIntegerListNotNullItem == null) {
                        fieldIntegerListNotNullOsList.addNull();
                    } else {
                        fieldIntegerListNotNullOsList.addLong(fieldIntegerListNotNullItem.longValue());
                    }
                }
            }


            OsList fieldIntegerListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldIntegerListNullIndex);
            fieldIntegerListNullOsList.removeAll();
            RealmList<java.lang.Integer> fieldIntegerListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerListNull();
            if (fieldIntegerListNullList != null) {
                for (java.lang.Integer fieldIntegerListNullItem : fieldIntegerListNullList) {
                    if (fieldIntegerListNullItem == null) {
                        fieldIntegerListNullOsList.addNull();
                    } else {
                        fieldIntegerListNullOsList.addLong(fieldIntegerListNullItem.longValue());
                    }
                }
            }


            OsList fieldShortListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldShortListNotNullIndex);
            fieldShortListNotNullOsList.removeAll();
            RealmList<java.lang.Short> fieldShortListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortListNotNull();
            if (fieldShortListNotNullList != null) {
                for (java.lang.Short fieldShortListNotNullItem : fieldShortListNotNullList) {
                    if (fieldShortListNotNullItem == null) {
                        fieldShortListNotNullOsList.addNull();
                    } else {
                        fieldShortListNotNullOsList.addLong(fieldShortListNotNullItem.longValue());
                    }
                }
            }


            OsList fieldShortListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldShortListNullIndex);
            fieldShortListNullOsList.removeAll();
            RealmList<java.lang.Short> fieldShortListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortListNull();
            if (fieldShortListNullList != null) {
                for (java.lang.Short fieldShortListNullItem : fieldShortListNullList) {
                    if (fieldShortListNullItem == null) {
                        fieldShortListNullOsList.addNull();
                    } else {
                        fieldShortListNullOsList.addLong(fieldShortListNullItem.longValue());
                    }
                }
            }


            OsList fieldByteListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldByteListNotNullIndex);
            fieldByteListNotNullOsList.removeAll();
            RealmList<java.lang.Byte> fieldByteListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteListNotNull();
            if (fieldByteListNotNullList != null) {
                for (java.lang.Byte fieldByteListNotNullItem : fieldByteListNotNullList) {
                    if (fieldByteListNotNullItem == null) {
                        fieldByteListNotNullOsList.addNull();
                    } else {
                        fieldByteListNotNullOsList.addLong(fieldByteListNotNullItem.longValue());
                    }
                }
            }


            OsList fieldByteListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldByteListNullIndex);
            fieldByteListNullOsList.removeAll();
            RealmList<java.lang.Byte> fieldByteListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteListNull();
            if (fieldByteListNullList != null) {
                for (java.lang.Byte fieldByteListNullItem : fieldByteListNullList) {
                    if (fieldByteListNullItem == null) {
                        fieldByteListNullOsList.addNull();
                    } else {
                        fieldByteListNullOsList.addLong(fieldByteListNullItem.longValue());
                    }
                }
            }


            OsList fieldDoubleListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDoubleListNotNullIndex);
            fieldDoubleListNotNullOsList.removeAll();
            RealmList<java.lang.Double> fieldDoubleListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleListNotNull();
            if (fieldDoubleListNotNullList != null) {
                for (java.lang.Double fieldDoubleListNotNullItem : fieldDoubleListNotNullList) {
                    if (fieldDoubleListNotNullItem == null) {
                        fieldDoubleListNotNullOsList.addNull();
                    } else {
                        fieldDoubleListNotNullOsList.addDouble(fieldDoubleListNotNullItem.doubleValue());
                    }
                }
            }


            OsList fieldDoubleListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDoubleListNullIndex);
            fieldDoubleListNullOsList.removeAll();
            RealmList<java.lang.Double> fieldDoubleListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleListNull();
            if (fieldDoubleListNullList != null) {
                for (java.lang.Double fieldDoubleListNullItem : fieldDoubleListNullList) {
                    if (fieldDoubleListNullItem == null) {
                        fieldDoubleListNullOsList.addNull();
                    } else {
                        fieldDoubleListNullOsList.addDouble(fieldDoubleListNullItem.doubleValue());
                    }
                }
            }


            OsList fieldFloatListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldFloatListNotNullIndex);
            fieldFloatListNotNullOsList.removeAll();
            RealmList<java.lang.Float> fieldFloatListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatListNotNull();
            if (fieldFloatListNotNullList != null) {
                for (java.lang.Float fieldFloatListNotNullItem : fieldFloatListNotNullList) {
                    if (fieldFloatListNotNullItem == null) {
                        fieldFloatListNotNullOsList.addNull();
                    } else {
                        fieldFloatListNotNullOsList.addFloat(fieldFloatListNotNullItem.floatValue());
                    }
                }
            }


            OsList fieldFloatListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldFloatListNullIndex);
            fieldFloatListNullOsList.removeAll();
            RealmList<java.lang.Float> fieldFloatListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatListNull();
            if (fieldFloatListNullList != null) {
                for (java.lang.Float fieldFloatListNullItem : fieldFloatListNullList) {
                    if (fieldFloatListNullItem == null) {
                        fieldFloatListNullOsList.addNull();
                    } else {
                        fieldFloatListNullOsList.addFloat(fieldFloatListNullItem.floatValue());
                    }
                }
            }


            OsList fieldDateListNotNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDateListNotNullIndex);
            fieldDateListNotNullOsList.removeAll();
            RealmList<java.util.Date> fieldDateListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateListNotNull();
            if (fieldDateListNotNullList != null) {
                for (java.util.Date fieldDateListNotNullItem : fieldDateListNotNullList) {
                    if (fieldDateListNotNullItem == null) {
                        fieldDateListNotNullOsList.addNull();
                    } else {
                        fieldDateListNotNullOsList.addDate(fieldDateListNotNullItem);
                    }
                }
            }


            OsList fieldDateListNullOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.fieldDateListNullIndex);
            fieldDateListNullOsList.removeAll();
            RealmList<java.util.Date> fieldDateListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateListNull();
            if (fieldDateListNullList != null) {
                for (java.util.Date fieldDateListNullItem : fieldDateListNullList) {
                    if (fieldDateListNullItem == null) {
                        fieldDateListNullOsList.addNull();
                    } else {
                        fieldDateListNullOsList.addDate(fieldDateListNullItem);
                    }
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
        some_test_NullTypesRealmProxyInterface unmanagedCopy = (some_test_NullTypesRealmProxyInterface) unmanagedObject;
        some_test_NullTypesRealmProxyInterface realmSource = (some_test_NullTypesRealmProxyInterface) realmObject;
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
        unmanagedCopy.realmSet$fieldObjectNull(some_test_NullTypesRealmProxy.createDetachedCopy(realmSource.realmGet$fieldObjectNull(), currentDepth + 1, maxDepth, cache));

        unmanagedCopy.realmSet$fieldStringListNotNull(new RealmList<java.lang.String>());
        unmanagedCopy.realmGet$fieldStringListNotNull().addAll(realmSource.realmGet$fieldStringListNotNull());

        unmanagedCopy.realmSet$fieldStringListNull(new RealmList<java.lang.String>());
        unmanagedCopy.realmGet$fieldStringListNull().addAll(realmSource.realmGet$fieldStringListNull());

        unmanagedCopy.realmSet$fieldBinaryListNotNull(new RealmList<byte[]>());
        unmanagedCopy.realmGet$fieldBinaryListNotNull().addAll(realmSource.realmGet$fieldBinaryListNotNull());

        unmanagedCopy.realmSet$fieldBinaryListNull(new RealmList<byte[]>());
        unmanagedCopy.realmGet$fieldBinaryListNull().addAll(realmSource.realmGet$fieldBinaryListNull());

        unmanagedCopy.realmSet$fieldBooleanListNotNull(new RealmList<java.lang.Boolean>());
        unmanagedCopy.realmGet$fieldBooleanListNotNull().addAll(realmSource.realmGet$fieldBooleanListNotNull());

        unmanagedCopy.realmSet$fieldBooleanListNull(new RealmList<java.lang.Boolean>());
        unmanagedCopy.realmGet$fieldBooleanListNull().addAll(realmSource.realmGet$fieldBooleanListNull());

        unmanagedCopy.realmSet$fieldLongListNotNull(new RealmList<java.lang.Long>());
        unmanagedCopy.realmGet$fieldLongListNotNull().addAll(realmSource.realmGet$fieldLongListNotNull());

        unmanagedCopy.realmSet$fieldLongListNull(new RealmList<java.lang.Long>());
        unmanagedCopy.realmGet$fieldLongListNull().addAll(realmSource.realmGet$fieldLongListNull());

        unmanagedCopy.realmSet$fieldIntegerListNotNull(new RealmList<java.lang.Integer>());
        unmanagedCopy.realmGet$fieldIntegerListNotNull().addAll(realmSource.realmGet$fieldIntegerListNotNull());

        unmanagedCopy.realmSet$fieldIntegerListNull(new RealmList<java.lang.Integer>());
        unmanagedCopy.realmGet$fieldIntegerListNull().addAll(realmSource.realmGet$fieldIntegerListNull());

        unmanagedCopy.realmSet$fieldShortListNotNull(new RealmList<java.lang.Short>());
        unmanagedCopy.realmGet$fieldShortListNotNull().addAll(realmSource.realmGet$fieldShortListNotNull());

        unmanagedCopy.realmSet$fieldShortListNull(new RealmList<java.lang.Short>());
        unmanagedCopy.realmGet$fieldShortListNull().addAll(realmSource.realmGet$fieldShortListNull());

        unmanagedCopy.realmSet$fieldByteListNotNull(new RealmList<java.lang.Byte>());
        unmanagedCopy.realmGet$fieldByteListNotNull().addAll(realmSource.realmGet$fieldByteListNotNull());

        unmanagedCopy.realmSet$fieldByteListNull(new RealmList<java.lang.Byte>());
        unmanagedCopy.realmGet$fieldByteListNull().addAll(realmSource.realmGet$fieldByteListNull());

        unmanagedCopy.realmSet$fieldDoubleListNotNull(new RealmList<java.lang.Double>());
        unmanagedCopy.realmGet$fieldDoubleListNotNull().addAll(realmSource.realmGet$fieldDoubleListNotNull());

        unmanagedCopy.realmSet$fieldDoubleListNull(new RealmList<java.lang.Double>());
        unmanagedCopy.realmGet$fieldDoubleListNull().addAll(realmSource.realmGet$fieldDoubleListNull());

        unmanagedCopy.realmSet$fieldFloatListNotNull(new RealmList<java.lang.Float>());
        unmanagedCopy.realmGet$fieldFloatListNotNull().addAll(realmSource.realmGet$fieldFloatListNotNull());

        unmanagedCopy.realmSet$fieldFloatListNull(new RealmList<java.lang.Float>());
        unmanagedCopy.realmGet$fieldFloatListNull().addAll(realmSource.realmGet$fieldFloatListNull());

        unmanagedCopy.realmSet$fieldDateListNotNull(new RealmList<java.util.Date>());
        unmanagedCopy.realmGet$fieldDateListNotNull().addAll(realmSource.realmGet$fieldDateListNotNull());

        unmanagedCopy.realmSet$fieldDateListNull(new RealmList<java.util.Date>());
        unmanagedCopy.realmGet$fieldDateListNull().addAll(realmSource.realmGet$fieldDateListNull());

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
        stringBuilder.append(",");
        stringBuilder.append("{fieldStringListNotNull:");
        stringBuilder.append("RealmList<String>[").append(realmGet$fieldStringListNotNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldStringListNull:");
        stringBuilder.append("RealmList<String>[").append(realmGet$fieldStringListNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBinaryListNotNull:");
        stringBuilder.append("RealmList<byte[]>[").append(realmGet$fieldBinaryListNotNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBinaryListNull:");
        stringBuilder.append("RealmList<byte[]>[").append(realmGet$fieldBinaryListNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBooleanListNotNull:");
        stringBuilder.append("RealmList<Boolean>[").append(realmGet$fieldBooleanListNotNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBooleanListNull:");
        stringBuilder.append("RealmList<Boolean>[").append(realmGet$fieldBooleanListNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldLongListNotNull:");
        stringBuilder.append("RealmList<Long>[").append(realmGet$fieldLongListNotNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldLongListNull:");
        stringBuilder.append("RealmList<Long>[").append(realmGet$fieldLongListNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldIntegerListNotNull:");
        stringBuilder.append("RealmList<Integer>[").append(realmGet$fieldIntegerListNotNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldIntegerListNull:");
        stringBuilder.append("RealmList<Integer>[").append(realmGet$fieldIntegerListNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldShortListNotNull:");
        stringBuilder.append("RealmList<Short>[").append(realmGet$fieldShortListNotNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldShortListNull:");
        stringBuilder.append("RealmList<Short>[").append(realmGet$fieldShortListNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldByteListNotNull:");
        stringBuilder.append("RealmList<Byte>[").append(realmGet$fieldByteListNotNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldByteListNull:");
        stringBuilder.append("RealmList<Byte>[").append(realmGet$fieldByteListNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDoubleListNotNull:");
        stringBuilder.append("RealmList<Double>[").append(realmGet$fieldDoubleListNotNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDoubleListNull:");
        stringBuilder.append("RealmList<Double>[").append(realmGet$fieldDoubleListNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldFloatListNotNull:");
        stringBuilder.append("RealmList<Float>[").append(realmGet$fieldFloatListNotNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldFloatListNull:");
        stringBuilder.append("RealmList<Float>[").append(realmGet$fieldFloatListNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDateListNotNull:");
        stringBuilder.append("RealmList<Date>[").append(realmGet$fieldDateListNotNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDateListNull:");
        stringBuilder.append("RealmList<Date>[").append(realmGet$fieldDateListNull().size()).append("]");
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
        some_test_NullTypesRealmProxy aNullTypes = (some_test_NullTypesRealmProxy)o;

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
