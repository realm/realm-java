package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.ImportFlag;
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
import io.realm.internal.objectstore.OsObjectBuilder;
import io.realm.log.RealmLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("all")
public class some_test_NullTypesRealmProxy extends some.test.NullTypes
        implements RealmObjectProxy, some_test_NullTypesRealmProxyInterface {

    static final class NullTypesColumnInfo extends ColumnInfo {
        long fieldStringNotNullColKey;
        long fieldStringNullColKey;
        long fieldBooleanNotNullColKey;
        long fieldBooleanNullColKey;
        long fieldBytesNotNullColKey;
        long fieldBytesNullColKey;
        long fieldByteNotNullColKey;
        long fieldByteNullColKey;
        long fieldShortNotNullColKey;
        long fieldShortNullColKey;
        long fieldIntegerNotNullColKey;
        long fieldIntegerNullColKey;
        long fieldLongNotNullColKey;
        long fieldLongNullColKey;
        long fieldFloatNotNullColKey;
        long fieldFloatNullColKey;
        long fieldDoubleNotNullColKey;
        long fieldDoubleNullColKey;
        long fieldDateNotNullColKey;
        long fieldDateNullColKey;
        long fieldDecimal128NotNullColKey;
        long fieldDecimal128NullColKey;
        long fieldObjectIdNotNullColKey;
        long fieldObjectIdNullColKey;
        long fieldObjectNullColKey;
        long fieldStringListNotNullColKey;
        long fieldStringListNullColKey;
        long fieldBinaryListNotNullColKey;
        long fieldBinaryListNullColKey;
        long fieldBooleanListNotNullColKey;
        long fieldBooleanListNullColKey;
        long fieldLongListNotNullColKey;
        long fieldLongListNullColKey;
        long fieldIntegerListNotNullColKey;
        long fieldIntegerListNullColKey;
        long fieldShortListNotNullColKey;
        long fieldShortListNullColKey;
        long fieldByteListNotNullColKey;
        long fieldByteListNullColKey;
        long fieldDoubleListNotNullColKey;
        long fieldDoubleListNullColKey;
        long fieldFloatListNotNullColKey;
        long fieldFloatListNullColKey;
        long fieldDateListNotNullColKey;
        long fieldDateListNullColKey;
        long fieldDecimal128ListNotNullColKey;
        long fieldDecimal128ListNullColKey;
        long fieldObjectIdListNotNullColKey;
        long fieldObjectIdListNullColKey;

        NullTypesColumnInfo(OsSchemaInfo schemaInfo) {
            super(49);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("NullTypes");
            this.fieldStringNotNullColKey = addColumnDetails("fieldStringNotNull", "fieldStringNotNull", objectSchemaInfo);
            this.fieldStringNullColKey = addColumnDetails("fieldStringNull", "fieldStringNull", objectSchemaInfo);
            this.fieldBooleanNotNullColKey = addColumnDetails("fieldBooleanNotNull", "fieldBooleanNotNull", objectSchemaInfo);
            this.fieldBooleanNullColKey = addColumnDetails("fieldBooleanNull", "fieldBooleanNull", objectSchemaInfo);
            this.fieldBytesNotNullColKey = addColumnDetails("fieldBytesNotNull", "fieldBytesNotNull", objectSchemaInfo);
            this.fieldBytesNullColKey = addColumnDetails("fieldBytesNull", "fieldBytesNull", objectSchemaInfo);
            this.fieldByteNotNullColKey = addColumnDetails("fieldByteNotNull", "fieldByteNotNull", objectSchemaInfo);
            this.fieldByteNullColKey = addColumnDetails("fieldByteNull", "fieldByteNull", objectSchemaInfo);
            this.fieldShortNotNullColKey = addColumnDetails("fieldShortNotNull", "fieldShortNotNull", objectSchemaInfo);
            this.fieldShortNullColKey = addColumnDetails("fieldShortNull", "fieldShortNull", objectSchemaInfo);
            this.fieldIntegerNotNullColKey = addColumnDetails("fieldIntegerNotNull", "fieldIntegerNotNull", objectSchemaInfo);
            this.fieldIntegerNullColKey = addColumnDetails("fieldIntegerNull", "fieldIntegerNull", objectSchemaInfo);
            this.fieldLongNotNullColKey = addColumnDetails("fieldLongNotNull", "fieldLongNotNull", objectSchemaInfo);
            this.fieldLongNullColKey = addColumnDetails("fieldLongNull", "fieldLongNull", objectSchemaInfo);
            this.fieldFloatNotNullColKey = addColumnDetails("fieldFloatNotNull", "fieldFloatNotNull", objectSchemaInfo);
            this.fieldFloatNullColKey = addColumnDetails("fieldFloatNull", "fieldFloatNull", objectSchemaInfo);
            this.fieldDoubleNotNullColKey = addColumnDetails("fieldDoubleNotNull", "fieldDoubleNotNull", objectSchemaInfo);
            this.fieldDoubleNullColKey = addColumnDetails("fieldDoubleNull", "fieldDoubleNull", objectSchemaInfo);
            this.fieldDateNotNullColKey = addColumnDetails("fieldDateNotNull", "fieldDateNotNull", objectSchemaInfo);
            this.fieldDateNullColKey = addColumnDetails("fieldDateNull", "fieldDateNull", objectSchemaInfo);
            this.fieldDecimal128NotNullColKey = addColumnDetails("fieldDecimal128NotNull", "fieldDecimal128NotNull", objectSchemaInfo);
            this.fieldDecimal128NullColKey = addColumnDetails("fieldDecimal128Null", "fieldDecimal128Null", objectSchemaInfo);
            this.fieldObjectIdNotNullColKey = addColumnDetails("fieldObjectIdNotNull", "fieldObjectIdNotNull", objectSchemaInfo);
            this.fieldObjectIdNullColKey = addColumnDetails("fieldObjectIdNull", "fieldObjectIdNull", objectSchemaInfo);
            this.fieldObjectNullColKey = addColumnDetails("fieldObjectNull", "fieldObjectNull", objectSchemaInfo);
            this.fieldStringListNotNullColKey = addColumnDetails("fieldStringListNotNull", "fieldStringListNotNull", objectSchemaInfo);
            this.fieldStringListNullColKey = addColumnDetails("fieldStringListNull", "fieldStringListNull", objectSchemaInfo);
            this.fieldBinaryListNotNullColKey = addColumnDetails("fieldBinaryListNotNull", "fieldBinaryListNotNull", objectSchemaInfo);
            this.fieldBinaryListNullColKey = addColumnDetails("fieldBinaryListNull", "fieldBinaryListNull", objectSchemaInfo);
            this.fieldBooleanListNotNullColKey = addColumnDetails("fieldBooleanListNotNull", "fieldBooleanListNotNull", objectSchemaInfo);
            this.fieldBooleanListNullColKey = addColumnDetails("fieldBooleanListNull", "fieldBooleanListNull", objectSchemaInfo);
            this.fieldLongListNotNullColKey = addColumnDetails("fieldLongListNotNull", "fieldLongListNotNull", objectSchemaInfo);
            this.fieldLongListNullColKey = addColumnDetails("fieldLongListNull", "fieldLongListNull", objectSchemaInfo);
            this.fieldIntegerListNotNullColKey = addColumnDetails("fieldIntegerListNotNull", "fieldIntegerListNotNull", objectSchemaInfo);
            this.fieldIntegerListNullColKey = addColumnDetails("fieldIntegerListNull", "fieldIntegerListNull", objectSchemaInfo);
            this.fieldShortListNotNullColKey = addColumnDetails("fieldShortListNotNull", "fieldShortListNotNull", objectSchemaInfo);
            this.fieldShortListNullColKey = addColumnDetails("fieldShortListNull", "fieldShortListNull", objectSchemaInfo);
            this.fieldByteListNotNullColKey = addColumnDetails("fieldByteListNotNull", "fieldByteListNotNull", objectSchemaInfo);
            this.fieldByteListNullColKey = addColumnDetails("fieldByteListNull", "fieldByteListNull", objectSchemaInfo);
            this.fieldDoubleListNotNullColKey = addColumnDetails("fieldDoubleListNotNull", "fieldDoubleListNotNull", objectSchemaInfo);
            this.fieldDoubleListNullColKey = addColumnDetails("fieldDoubleListNull", "fieldDoubleListNull", objectSchemaInfo);
            this.fieldFloatListNotNullColKey = addColumnDetails("fieldFloatListNotNull", "fieldFloatListNotNull", objectSchemaInfo);
            this.fieldFloatListNullColKey = addColumnDetails("fieldFloatListNull", "fieldFloatListNull", objectSchemaInfo);
            this.fieldDateListNotNullColKey = addColumnDetails("fieldDateListNotNull", "fieldDateListNotNull", objectSchemaInfo);
            this.fieldDateListNullColKey = addColumnDetails("fieldDateListNull", "fieldDateListNull", objectSchemaInfo);
            this.fieldDecimal128ListNotNullColKey = addColumnDetails("fieldDecimal128ListNotNull", "fieldDecimal128ListNotNull", objectSchemaInfo);
            this.fieldDecimal128ListNullColKey = addColumnDetails("fieldDecimal128ListNull", "fieldDecimal128ListNull", objectSchemaInfo);
            this.fieldObjectIdListNotNullColKey = addColumnDetails("fieldObjectIdListNotNull", "fieldObjectIdListNotNull", objectSchemaInfo);
            this.fieldObjectIdListNullColKey = addColumnDetails("fieldObjectIdListNull", "fieldObjectIdListNull", objectSchemaInfo);
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
            dst.fieldStringNotNullColKey = src.fieldStringNotNullColKey;
            dst.fieldStringNullColKey = src.fieldStringNullColKey;
            dst.fieldBooleanNotNullColKey = src.fieldBooleanNotNullColKey;
            dst.fieldBooleanNullColKey = src.fieldBooleanNullColKey;
            dst.fieldBytesNotNullColKey = src.fieldBytesNotNullColKey;
            dst.fieldBytesNullColKey = src.fieldBytesNullColKey;
            dst.fieldByteNotNullColKey = src.fieldByteNotNullColKey;
            dst.fieldByteNullColKey = src.fieldByteNullColKey;
            dst.fieldShortNotNullColKey = src.fieldShortNotNullColKey;
            dst.fieldShortNullColKey = src.fieldShortNullColKey;
            dst.fieldIntegerNotNullColKey = src.fieldIntegerNotNullColKey;
            dst.fieldIntegerNullColKey = src.fieldIntegerNullColKey;
            dst.fieldLongNotNullColKey = src.fieldLongNotNullColKey;
            dst.fieldLongNullColKey = src.fieldLongNullColKey;
            dst.fieldFloatNotNullColKey = src.fieldFloatNotNullColKey;
            dst.fieldFloatNullColKey = src.fieldFloatNullColKey;
            dst.fieldDoubleNotNullColKey = src.fieldDoubleNotNullColKey;
            dst.fieldDoubleNullColKey = src.fieldDoubleNullColKey;
            dst.fieldDateNotNullColKey = src.fieldDateNotNullColKey;
            dst.fieldDateNullColKey = src.fieldDateNullColKey;
            dst.fieldDecimal128NotNullColKey = src.fieldDecimal128NotNullColKey;
            dst.fieldDecimal128NullColKey = src.fieldDecimal128NullColKey;
            dst.fieldObjectIdNotNullColKey = src.fieldObjectIdNotNullColKey;
            dst.fieldObjectIdNullColKey = src.fieldObjectIdNullColKey;
            dst.fieldObjectNullColKey = src.fieldObjectNullColKey;
            dst.fieldStringListNotNullColKey = src.fieldStringListNotNullColKey;
            dst.fieldStringListNullColKey = src.fieldStringListNullColKey;
            dst.fieldBinaryListNotNullColKey = src.fieldBinaryListNotNullColKey;
            dst.fieldBinaryListNullColKey = src.fieldBinaryListNullColKey;
            dst.fieldBooleanListNotNullColKey = src.fieldBooleanListNotNullColKey;
            dst.fieldBooleanListNullColKey = src.fieldBooleanListNullColKey;
            dst.fieldLongListNotNullColKey = src.fieldLongListNotNullColKey;
            dst.fieldLongListNullColKey = src.fieldLongListNullColKey;
            dst.fieldIntegerListNotNullColKey = src.fieldIntegerListNotNullColKey;
            dst.fieldIntegerListNullColKey = src.fieldIntegerListNullColKey;
            dst.fieldShortListNotNullColKey = src.fieldShortListNotNullColKey;
            dst.fieldShortListNullColKey = src.fieldShortListNullColKey;
            dst.fieldByteListNotNullColKey = src.fieldByteListNotNullColKey;
            dst.fieldByteListNullColKey = src.fieldByteListNullColKey;
            dst.fieldDoubleListNotNullColKey = src.fieldDoubleListNotNullColKey;
            dst.fieldDoubleListNullColKey = src.fieldDoubleListNullColKey;
            dst.fieldFloatListNotNullColKey = src.fieldFloatListNotNullColKey;
            dst.fieldFloatListNullColKey = src.fieldFloatListNullColKey;
            dst.fieldDateListNotNullColKey = src.fieldDateListNotNullColKey;
            dst.fieldDateListNullColKey = src.fieldDateListNullColKey;
            dst.fieldDecimal128ListNotNullColKey = src.fieldDecimal128ListNotNullColKey;
            dst.fieldDecimal128ListNullColKey = src.fieldDecimal128ListNullColKey;
            dst.fieldObjectIdListNotNullColKey = src.fieldObjectIdListNotNullColKey;
            dst.fieldObjectIdListNullColKey = src.fieldObjectIdListNullColKey;
        }
    }

    private static final String NO_ALIAS = "";
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
    private RealmList<org.bson.types.Decimal128> fieldDecimal128ListNotNullRealmList;
    private RealmList<org.bson.types.Decimal128> fieldDecimal128ListNullRealmList;
    private RealmList<org.bson.types.ObjectId> fieldObjectIdListNotNullRealmList;
    private RealmList<org.bson.types.ObjectId> fieldObjectIdListNullRealmList;

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
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.fieldStringNotNullColKey);
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
            row.getTable().setString(columnInfo.fieldStringNotNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldStringNotNull' to null.");
        }
        proxyState.getRow$realm().setString(columnInfo.fieldStringNotNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$fieldStringNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.fieldStringNullColKey);
    }

    @Override
    public void realmSet$fieldStringNull(String value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldStringNullColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setString(columnInfo.fieldStringNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldStringNullColKey);
            return;
        }
        proxyState.getRow$realm().setString(columnInfo.fieldStringNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Boolean realmGet$fieldBooleanNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.fieldBooleanNotNullColKey);
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
            row.getTable().setBoolean(columnInfo.fieldBooleanNotNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldBooleanNotNull' to null.");
        }
        proxyState.getRow$realm().setBoolean(columnInfo.fieldBooleanNotNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Boolean realmGet$fieldBooleanNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldBooleanNullColKey)) {
            return null;
        }
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.fieldBooleanNullColKey);
    }

    @Override
    public void realmSet$fieldBooleanNull(Boolean value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldBooleanNullColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setBoolean(columnInfo.fieldBooleanNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldBooleanNullColKey);
            return;
        }
        proxyState.getRow$realm().setBoolean(columnInfo.fieldBooleanNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public byte[] realmGet$fieldBytesNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (byte[]) proxyState.getRow$realm().getBinaryByteArray(columnInfo.fieldBytesNotNullColKey);
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
            row.getTable().setBinaryByteArray(columnInfo.fieldBytesNotNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldBytesNotNull' to null.");
        }
        proxyState.getRow$realm().setBinaryByteArray(columnInfo.fieldBytesNotNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public byte[] realmGet$fieldBytesNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (byte[]) proxyState.getRow$realm().getBinaryByteArray(columnInfo.fieldBytesNullColKey);
    }

    @Override
    public void realmSet$fieldBytesNull(byte[] value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldBytesNullColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setBinaryByteArray(columnInfo.fieldBytesNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldBytesNullColKey);
            return;
        }
        proxyState.getRow$realm().setBinaryByteArray(columnInfo.fieldBytesNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Byte realmGet$fieldByteNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (byte) proxyState.getRow$realm().getLong(columnInfo.fieldByteNotNullColKey);
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
            row.getTable().setLong(columnInfo.fieldByteNotNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldByteNotNull' to null.");
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldByteNotNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Byte realmGet$fieldByteNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldByteNullColKey)) {
            return null;
        }
        return (byte) proxyState.getRow$realm().getLong(columnInfo.fieldByteNullColKey);
    }

    @Override
    public void realmSet$fieldByteNull(Byte value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldByteNullColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setLong(columnInfo.fieldByteNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldByteNullColKey);
            return;
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldByteNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Short realmGet$fieldShortNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (short) proxyState.getRow$realm().getLong(columnInfo.fieldShortNotNullColKey);
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
            row.getTable().setLong(columnInfo.fieldShortNotNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldShortNotNull' to null.");
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldShortNotNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Short realmGet$fieldShortNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldShortNullColKey)) {
            return null;
        }
        return (short) proxyState.getRow$realm().getLong(columnInfo.fieldShortNullColKey);
    }

    @Override
    public void realmSet$fieldShortNull(Short value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldShortNullColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setLong(columnInfo.fieldShortNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldShortNullColKey);
            return;
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldShortNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Integer realmGet$fieldIntegerNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (int) proxyState.getRow$realm().getLong(columnInfo.fieldIntegerNotNullColKey);
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
            row.getTable().setLong(columnInfo.fieldIntegerNotNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldIntegerNotNull' to null.");
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldIntegerNotNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Integer realmGet$fieldIntegerNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldIntegerNullColKey)) {
            return null;
        }
        return (int) proxyState.getRow$realm().getLong(columnInfo.fieldIntegerNullColKey);
    }

    @Override
    public void realmSet$fieldIntegerNull(Integer value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldIntegerNullColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setLong(columnInfo.fieldIntegerNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldIntegerNullColKey);
            return;
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldIntegerNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Long realmGet$fieldLongNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (long) proxyState.getRow$realm().getLong(columnInfo.fieldLongNotNullColKey);
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
            row.getTable().setLong(columnInfo.fieldLongNotNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldLongNotNull' to null.");
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldLongNotNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Long realmGet$fieldLongNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldLongNullColKey)) {
            return null;
        }
        return (long) proxyState.getRow$realm().getLong(columnInfo.fieldLongNullColKey);
    }

    @Override
    public void realmSet$fieldLongNull(Long value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldLongNullColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setLong(columnInfo.fieldLongNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldLongNullColKey);
            return;
        }
        proxyState.getRow$realm().setLong(columnInfo.fieldLongNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Float realmGet$fieldFloatNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.fieldFloatNotNullColKey);
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
            row.getTable().setFloat(columnInfo.fieldFloatNotNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldFloatNotNull' to null.");
        }
        proxyState.getRow$realm().setFloat(columnInfo.fieldFloatNotNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Float realmGet$fieldFloatNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldFloatNullColKey)) {
            return null;
        }
        return (float) proxyState.getRow$realm().getFloat(columnInfo.fieldFloatNullColKey);
    }

    @Override
    public void realmSet$fieldFloatNull(Float value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldFloatNullColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setFloat(columnInfo.fieldFloatNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldFloatNullColKey);
            return;
        }
        proxyState.getRow$realm().setFloat(columnInfo.fieldFloatNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Double realmGet$fieldDoubleNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (double) proxyState.getRow$realm().getDouble(columnInfo.fieldDoubleNotNullColKey);
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
            row.getTable().setDouble(columnInfo.fieldDoubleNotNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldDoubleNotNull' to null.");
        }
        proxyState.getRow$realm().setDouble(columnInfo.fieldDoubleNotNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Double realmGet$fieldDoubleNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldDoubleNullColKey)) {
            return null;
        }
        return (double) proxyState.getRow$realm().getDouble(columnInfo.fieldDoubleNullColKey);
    }

    @Override
    public void realmSet$fieldDoubleNull(Double value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldDoubleNullColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setDouble(columnInfo.fieldDoubleNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldDoubleNullColKey);
            return;
        }
        proxyState.getRow$realm().setDouble(columnInfo.fieldDoubleNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Date realmGet$fieldDateNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.util.Date) proxyState.getRow$realm().getDate(columnInfo.fieldDateNotNullColKey);
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
            row.getTable().setDate(columnInfo.fieldDateNotNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldDateNotNull' to null.");
        }
        proxyState.getRow$realm().setDate(columnInfo.fieldDateNotNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Date realmGet$fieldDateNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldDateNullColKey)) {
            return null;
        }
        return (java.util.Date) proxyState.getRow$realm().getDate(columnInfo.fieldDateNullColKey);
    }

    @Override
    public void realmSet$fieldDateNull(Date value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldDateNullColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setDate(columnInfo.fieldDateNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldDateNullColKey);
            return;
        }
        proxyState.getRow$realm().setDate(columnInfo.fieldDateNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public org.bson.types.Decimal128 realmGet$fieldDecimal128NotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (org.bson.types.Decimal128) proxyState.getRow$realm().getDecimal128(columnInfo.fieldDecimal128NotNullColKey);
    }

    @Override
    public void realmSet$fieldDecimal128NotNull(org.bson.types.Decimal128 value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'fieldDecimal128NotNull' to null.");
            }
            row.getTable().setDecimal128(columnInfo.fieldDecimal128NotNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldDecimal128NotNull' to null.");
        }
        proxyState.getRow$realm().setDecimal128(columnInfo.fieldDecimal128NotNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public org.bson.types.Decimal128 realmGet$fieldDecimal128Null() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldDecimal128NullColKey)) {
            return null;
        }
        return (org.bson.types.Decimal128) proxyState.getRow$realm().getDecimal128(columnInfo.fieldDecimal128NullColKey);
    }

    @Override
    public void realmSet$fieldDecimal128Null(org.bson.types.Decimal128 value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldDecimal128NullColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setDecimal128(columnInfo.fieldDecimal128NullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldDecimal128NullColKey);
            return;
        }
        proxyState.getRow$realm().setDecimal128(columnInfo.fieldDecimal128NullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public org.bson.types.ObjectId realmGet$fieldObjectIdNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        return (org.bson.types.ObjectId) proxyState.getRow$realm().getObjectId(columnInfo.fieldObjectIdNotNullColKey);
    }

    @Override
    public void realmSet$fieldObjectIdNotNull(org.bson.types.ObjectId value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'fieldObjectIdNotNull' to null.");
            }
            row.getTable().setObjectId(columnInfo.fieldObjectIdNotNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'fieldObjectIdNotNull' to null.");
        }
        proxyState.getRow$realm().setObjectId(columnInfo.fieldObjectIdNotNullColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public org.bson.types.ObjectId realmGet$fieldObjectIdNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNull(columnInfo.fieldObjectIdNullColKey)) {
            return null;
        }
        return (org.bson.types.ObjectId) proxyState.getRow$realm().getObjectId(columnInfo.fieldObjectIdNullColKey);
    }

    @Override
    public void realmSet$fieldObjectIdNull(org.bson.types.ObjectId value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.fieldObjectIdNullColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setObjectId(columnInfo.fieldObjectIdNullColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.fieldObjectIdNullColKey);
            return;
        }
        proxyState.getRow$realm().setObjectId(columnInfo.fieldObjectIdNullColKey, value);
    }

    @Override
    public some.test.NullTypes realmGet$fieldObjectNull() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNullLink(columnInfo.fieldObjectNullColKey)) {
            return null;
        }
        return proxyState.getRealm$realm().get(some.test.NullTypes.class, proxyState.getRow$realm().getLink(columnInfo.fieldObjectNullColKey), false, Collections.<String>emptyList());
    }

    @Override
    public void realmSet$fieldObjectNull(some.test.NullTypes value) {
        Realm realm = (Realm) proxyState.getRealm$realm();
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldObjectNull")) {
                return;
            }
            if (value != null && !RealmObject.isManaged(value)) {
                value = realm.copyToRealm(value);
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                // Table#nullifyLink() does not support default value. Just using Row.
                row.nullifyLink(columnInfo.fieldObjectNullColKey);
                return;
            }
            proxyState.checkValidObject(value);
            row.getTable().setLink(columnInfo.fieldObjectNullColKey, row.getObjectKey(), ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getObjectKey(), true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().nullifyLink(columnInfo.fieldObjectNullColKey);
            return;
        }
        proxyState.checkValidObject(value);
        proxyState.getRow$realm().setLink(columnInfo.fieldObjectNullColKey, ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getObjectKey());
    }

    @Override
    public RealmList<String> realmGet$fieldStringListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldStringListNotNullRealmList != null) {
            return fieldStringListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldStringListNotNullColKey, RealmFieldType.STRING_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldStringListNotNullColKey, RealmFieldType.STRING_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldStringListNullColKey, RealmFieldType.STRING_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldStringListNullColKey, RealmFieldType.STRING_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBinaryListNotNullColKey, RealmFieldType.BINARY_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBinaryListNotNullColKey, RealmFieldType.BINARY_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBinaryListNullColKey, RealmFieldType.BINARY_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBinaryListNullColKey, RealmFieldType.BINARY_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBooleanListNotNullColKey, RealmFieldType.BOOLEAN_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBooleanListNotNullColKey, RealmFieldType.BOOLEAN_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBooleanListNullColKey, RealmFieldType.BOOLEAN_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldBooleanListNullColKey, RealmFieldType.BOOLEAN_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldLongListNotNullColKey, RealmFieldType.INTEGER_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldLongListNotNullColKey, RealmFieldType.INTEGER_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldLongListNullColKey, RealmFieldType.INTEGER_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldLongListNullColKey, RealmFieldType.INTEGER_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldIntegerListNotNullColKey, RealmFieldType.INTEGER_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldIntegerListNotNullColKey, RealmFieldType.INTEGER_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldIntegerListNullColKey, RealmFieldType.INTEGER_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldIntegerListNullColKey, RealmFieldType.INTEGER_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldShortListNotNullColKey, RealmFieldType.INTEGER_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldShortListNotNullColKey, RealmFieldType.INTEGER_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldShortListNullColKey, RealmFieldType.INTEGER_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldShortListNullColKey, RealmFieldType.INTEGER_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldByteListNotNullColKey, RealmFieldType.INTEGER_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldByteListNotNullColKey, RealmFieldType.INTEGER_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldByteListNullColKey, RealmFieldType.INTEGER_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldByteListNullColKey, RealmFieldType.INTEGER_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDoubleListNotNullColKey, RealmFieldType.DOUBLE_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDoubleListNotNullColKey, RealmFieldType.DOUBLE_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDoubleListNullColKey, RealmFieldType.DOUBLE_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDoubleListNullColKey, RealmFieldType.DOUBLE_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldFloatListNotNullColKey, RealmFieldType.FLOAT_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldFloatListNotNullColKey, RealmFieldType.FLOAT_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldFloatListNullColKey, RealmFieldType.FLOAT_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldFloatListNullColKey, RealmFieldType.FLOAT_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDateListNotNullColKey, RealmFieldType.DATE_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDateListNotNullColKey, RealmFieldType.DATE_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDateListNullColKey, RealmFieldType.DATE_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDateListNullColKey, RealmFieldType.DATE_LIST);
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

    @Override
    public RealmList<org.bson.types.Decimal128> realmGet$fieldDecimal128ListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldDecimal128ListNotNullRealmList != null) {
            return fieldDecimal128ListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDecimal128ListNotNullColKey, RealmFieldType.DECIMAL128_LIST);
            fieldDecimal128ListNotNullRealmList = new RealmList<org.bson.types.Decimal128>(org.bson.types.Decimal128.class, osList, proxyState.getRealm$realm());
            return fieldDecimal128ListNotNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldDecimal128ListNotNull(RealmList<org.bson.types.Decimal128> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldDecimal128ListNotNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDecimal128ListNotNullColKey, RealmFieldType.DECIMAL128_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (org.bson.types.Decimal128 item : value) {
            if (item == null) {
                throw new IllegalArgumentException("Storing 'null' into fieldDecimal128ListNotNull' is not allowed by the schema.");
            } else {
                osList.addDecimal128(item);
            }
        }
    }

    @Override
    public RealmList<org.bson.types.Decimal128> realmGet$fieldDecimal128ListNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldDecimal128ListNullRealmList != null) {
            return fieldDecimal128ListNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDecimal128ListNullColKey, RealmFieldType.DECIMAL128_LIST);
            fieldDecimal128ListNullRealmList = new RealmList<org.bson.types.Decimal128>(org.bson.types.Decimal128.class, osList, proxyState.getRealm$realm());
            return fieldDecimal128ListNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldDecimal128ListNull(RealmList<org.bson.types.Decimal128> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldDecimal128ListNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldDecimal128ListNullColKey, RealmFieldType.DECIMAL128_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (org.bson.types.Decimal128 item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addDecimal128(item);
            }
        }
    }

    @Override
    public RealmList<org.bson.types.ObjectId> realmGet$fieldObjectIdListNotNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldObjectIdListNotNullRealmList != null) {
            return fieldObjectIdListNotNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldObjectIdListNotNullColKey, RealmFieldType.OBJECT_ID_LIST);
            fieldObjectIdListNotNullRealmList = new RealmList<org.bson.types.ObjectId>(org.bson.types.ObjectId.class, osList, proxyState.getRealm$realm());
            return fieldObjectIdListNotNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldObjectIdListNotNull(RealmList<org.bson.types.ObjectId> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldObjectIdListNotNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldObjectIdListNotNullColKey, RealmFieldType.OBJECT_ID_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (org.bson.types.ObjectId item : value) {
            if (item == null) {
                throw new IllegalArgumentException("Storing 'null' into fieldObjectIdListNotNull' is not allowed by the schema.");
            } else {
                osList.addObjectId(item);
            }
        }
    }

    @Override
    public RealmList<org.bson.types.ObjectId> realmGet$fieldObjectIdListNull() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (fieldObjectIdListNullRealmList != null) {
            return fieldObjectIdListNullRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldObjectIdListNullColKey, RealmFieldType.OBJECT_ID_LIST);
            fieldObjectIdListNullRealmList = new RealmList<org.bson.types.ObjectId>(org.bson.types.ObjectId.class, osList, proxyState.getRealm$realm());
            return fieldObjectIdListNullRealmList;
        }
    }

    @Override
    public void realmSet$fieldObjectIdListNull(RealmList<org.bson.types.ObjectId> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("fieldObjectIdListNull")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.fieldObjectIdListNullColKey, RealmFieldType.OBJECT_ID_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (org.bson.types.ObjectId item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addObjectId(item);
            }
        }
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder(NO_ALIAS, "NullTypes", false, 49, 0);
        builder.addPersistedProperty(NO_ALIAS, "fieldStringNotNull", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldStringNull", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldBooleanNotNull", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldBooleanNull", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldBytesNotNull", RealmFieldType.BINARY, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldBytesNull", RealmFieldType.BINARY, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldByteNotNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldByteNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldShortNotNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldShortNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldIntegerNotNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldIntegerNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldLongNotNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldLongNull", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldFloatNotNull", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldFloatNull", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldDoubleNotNull", RealmFieldType.DOUBLE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldDoubleNull", RealmFieldType.DOUBLE, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldDateNotNull", RealmFieldType.DATE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldDateNull", RealmFieldType.DATE, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldDecimal128NotNull", RealmFieldType.DECIMAL128, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldDecimal128Null", RealmFieldType.DECIMAL128, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldObjectIdNotNull", RealmFieldType.OBJECT_ID, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "fieldObjectIdNull", RealmFieldType.OBJECT_ID, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedLinkProperty(NO_ALIAS, "fieldObjectNull", RealmFieldType.OBJECT, "NullTypes");
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldStringListNotNull", RealmFieldType.STRING_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldStringListNull", RealmFieldType.STRING_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldBinaryListNotNull", RealmFieldType.BINARY_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldBinaryListNull", RealmFieldType.BINARY_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldBooleanListNotNull", RealmFieldType.BOOLEAN_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldBooleanListNull", RealmFieldType.BOOLEAN_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldLongListNotNull", RealmFieldType.INTEGER_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldLongListNull", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldIntegerListNotNull", RealmFieldType.INTEGER_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldIntegerListNull", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldShortListNotNull", RealmFieldType.INTEGER_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldShortListNull", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldByteListNotNull", RealmFieldType.INTEGER_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldByteListNull", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldDoubleListNotNull", RealmFieldType.DOUBLE_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldDoubleListNull", RealmFieldType.DOUBLE_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldFloatListNotNull", RealmFieldType.FLOAT_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldFloatListNull", RealmFieldType.FLOAT_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldDateListNotNull", RealmFieldType.DATE_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldDateListNull", RealmFieldType.DATE_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldDecimal128ListNotNull", RealmFieldType.DECIMAL128_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldDecimal128ListNull", RealmFieldType.DECIMAL128_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldObjectIdListNotNull", RealmFieldType.OBJECT_ID_LIST, Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "fieldObjectIdListNull", RealmFieldType.OBJECT_ID_LIST, !Property.REQUIRED);
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
        final List<String> excludeFields = new ArrayList<String>(25);
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
        if (json.has("fieldDecimal128ListNotNull")) {
            excludeFields.add("fieldDecimal128ListNotNull");
        }
        if (json.has("fieldDecimal128ListNull")) {
            excludeFields.add("fieldDecimal128ListNull");
        }
        if (json.has("fieldObjectIdListNotNull")) {
            excludeFields.add("fieldObjectIdListNotNull");
        }
        if (json.has("fieldObjectIdListNull")) {
            excludeFields.add("fieldObjectIdListNull");
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
        if (json.has("fieldDecimal128NotNull")) {
            if (json.isNull("fieldDecimal128NotNull")) {
                objProxy.realmSet$fieldDecimal128NotNull(null);
            } else {
                Object decimal = json.get("fieldDecimal128NotNull");
                if (decimal instanceof org.bson.types.Decimal128) {
                    objProxy.realmSet$fieldDecimal128NotNull((org.bson.types.Decimal128) decimal);
                } else if (decimal instanceof String) {
                    objProxy.realmSet$fieldDecimal128NotNull(org.bson.types.Decimal128.parse((String)decimal));
                } else if (decimal instanceof Integer) {
                    objProxy.realmSet$fieldDecimal128NotNull(new org.bson.types.Decimal128((Integer)(decimal)));
                } else if (decimal instanceof Long) {
                    objProxy.realmSet$fieldDecimal128NotNull(new org.bson.types.Decimal128((Long)(decimal)));
                } else if (decimal instanceof Double) {
                    objProxy.realmSet$fieldDecimal128NotNull(new org.bson.types.Decimal128(new java.math.BigDecimal((Double)(decimal))));
                } else {
                    throw new UnsupportedOperationException(decimal.getClass() + " is not supported as a Decimal128 value");
                }
            }
        }
        if (json.has("fieldDecimal128Null")) {
            if (json.isNull("fieldDecimal128Null")) {
                objProxy.realmSet$fieldDecimal128Null(null);
            } else {
                Object decimal = json.get("fieldDecimal128Null");
                if (decimal instanceof org.bson.types.Decimal128) {
                    objProxy.realmSet$fieldDecimal128Null((org.bson.types.Decimal128) decimal);
                } else if (decimal instanceof String) {
                    objProxy.realmSet$fieldDecimal128Null(org.bson.types.Decimal128.parse((String)decimal));
                } else if (decimal instanceof Integer) {
                    objProxy.realmSet$fieldDecimal128Null(new org.bson.types.Decimal128((Integer)(decimal)));
                } else if (decimal instanceof Long) {
                    objProxy.realmSet$fieldDecimal128Null(new org.bson.types.Decimal128((Long)(decimal)));
                } else if (decimal instanceof Double) {
                    objProxy.realmSet$fieldDecimal128Null(new org.bson.types.Decimal128(new java.math.BigDecimal((Double)(decimal))));
                } else {
                    throw new UnsupportedOperationException(decimal.getClass() + " is not supported as a Decimal128 value");
                }
            }
        }
        if (json.has("fieldObjectIdNotNull")) {
            if (json.isNull("fieldObjectIdNotNull")) {
                objProxy.realmSet$fieldObjectIdNotNull(null);
            } else {
                Object id = json.get("fieldObjectIdNotNull");
                if (id instanceof org.bson.types.ObjectId) {
                    objProxy.realmSet$fieldObjectIdNotNull((org.bson.types.ObjectId) id);
                } else {
                    objProxy.realmSet$fieldObjectIdNotNull(new org.bson.types.ObjectId((String)id));
                }
            }
        }
        if (json.has("fieldObjectIdNull")) {
            if (json.isNull("fieldObjectIdNull")) {
                objProxy.realmSet$fieldObjectIdNull(null);
            } else {
                Object id = json.get("fieldObjectIdNull");
                if (id instanceof org.bson.types.ObjectId) {
                    objProxy.realmSet$fieldObjectIdNull((org.bson.types.ObjectId) id);
                } else {
                    objProxy.realmSet$fieldObjectIdNull(new org.bson.types.ObjectId((String)id));
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
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldDecimal128ListNotNull(), json, "fieldDecimal128ListNotNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldDecimal128ListNull(), json, "fieldDecimal128ListNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldObjectIdListNotNull(), json, "fieldObjectIdListNotNull");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$fieldObjectIdListNull(), json, "fieldObjectIdListNull");
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
            } else if (name.equals("fieldDecimal128NotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$fieldDecimal128NotNull(null);
                } else {
                    objProxy.realmSet$fieldDecimal128NotNull(org.bson.types.Decimal128.parse(reader.nextString()));
                }
            } else if (name.equals("fieldDecimal128Null")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$fieldDecimal128Null(null);
                } else {
                    objProxy.realmSet$fieldDecimal128Null(org.bson.types.Decimal128.parse(reader.nextString()));
                }
            } else if (name.equals("fieldObjectIdNotNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$fieldObjectIdNotNull(null);
                } else {
                    objProxy.realmSet$fieldObjectIdNotNull(new org.bson.types.ObjectId(reader.nextString()));
                }
            } else if (name.equals("fieldObjectIdNull")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$fieldObjectIdNull(null);
                } else {
                    objProxy.realmSet$fieldObjectIdNull(new org.bson.types.ObjectId(reader.nextString()));
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
            } else if (name.equals("fieldDecimal128ListNotNull")) {
                objProxy.realmSet$fieldDecimal128ListNotNull(ProxyUtils.createRealmListWithJsonStream(org.bson.types.Decimal128.class, reader));
            } else if (name.equals("fieldDecimal128ListNull")) {
                objProxy.realmSet$fieldDecimal128ListNull(ProxyUtils.createRealmListWithJsonStream(org.bson.types.Decimal128.class, reader));
            } else if (name.equals("fieldObjectIdListNotNull")) {
                objProxy.realmSet$fieldObjectIdListNotNull(ProxyUtils.createRealmListWithJsonStream(org.bson.types.ObjectId.class, reader));
            } else if (name.equals("fieldObjectIdListNull")) {
                objProxy.realmSet$fieldObjectIdListNull(ProxyUtils.createRealmListWithJsonStream(org.bson.types.ObjectId.class, reader));
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return realm.copyToRealm(obj);
    }

    static some_test_NullTypesRealmProxy newProxyInstance(BaseRealm realm, Row row) {
        // Ignore default values to avoid creating unexpected objects from RealmModel/RealmList fields
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        objectContext.set(realm, row, realm.getSchema().getColumnInfo(some.test.NullTypes.class), false, Collections.<String>emptyList());
        io.realm.some_test_NullTypesRealmProxy obj = new io.realm.some_test_NullTypesRealmProxy();
        objectContext.clear();
        return obj;
    }

    public static some.test.NullTypes copyOrUpdate(Realm realm, NullTypesColumnInfo columnInfo, some.test.NullTypes object, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null) {
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

        return copy(realm, columnInfo, object, update, cache, flags);
    }

    public static some.test.NullTypes copy(Realm realm, NullTypesColumnInfo columnInfo, some.test.NullTypes newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.NullTypes) cachedRealmObject;
        }

        some_test_NullTypesRealmProxyInterface unmanagedSource = (some_test_NullTypesRealmProxyInterface) newObject;

        Table table = realm.getTable(some.test.NullTypes.class);
        OsObjectBuilder builder = new OsObjectBuilder(table, flags);

        // Add all non-"object reference" fields
        builder.addString(columnInfo.fieldStringNotNullColKey, unmanagedSource.realmGet$fieldStringNotNull());
        builder.addString(columnInfo.fieldStringNullColKey, unmanagedSource.realmGet$fieldStringNull());
        builder.addBoolean(columnInfo.fieldBooleanNotNullColKey, unmanagedSource.realmGet$fieldBooleanNotNull());
        builder.addBoolean(columnInfo.fieldBooleanNullColKey, unmanagedSource.realmGet$fieldBooleanNull());
        builder.addByteArray(columnInfo.fieldBytesNotNullColKey, unmanagedSource.realmGet$fieldBytesNotNull());
        builder.addByteArray(columnInfo.fieldBytesNullColKey, unmanagedSource.realmGet$fieldBytesNull());
        builder.addInteger(columnInfo.fieldByteNotNullColKey, unmanagedSource.realmGet$fieldByteNotNull());
        builder.addInteger(columnInfo.fieldByteNullColKey, unmanagedSource.realmGet$fieldByteNull());
        builder.addInteger(columnInfo.fieldShortNotNullColKey, unmanagedSource.realmGet$fieldShortNotNull());
        builder.addInteger(columnInfo.fieldShortNullColKey, unmanagedSource.realmGet$fieldShortNull());
        builder.addInteger(columnInfo.fieldIntegerNotNullColKey, unmanagedSource.realmGet$fieldIntegerNotNull());
        builder.addInteger(columnInfo.fieldIntegerNullColKey, unmanagedSource.realmGet$fieldIntegerNull());
        builder.addInteger(columnInfo.fieldLongNotNullColKey, unmanagedSource.realmGet$fieldLongNotNull());
        builder.addInteger(columnInfo.fieldLongNullColKey, unmanagedSource.realmGet$fieldLongNull());
        builder.addFloat(columnInfo.fieldFloatNotNullColKey, unmanagedSource.realmGet$fieldFloatNotNull());
        builder.addFloat(columnInfo.fieldFloatNullColKey, unmanagedSource.realmGet$fieldFloatNull());
        builder.addDouble(columnInfo.fieldDoubleNotNullColKey, unmanagedSource.realmGet$fieldDoubleNotNull());
        builder.addDouble(columnInfo.fieldDoubleNullColKey, unmanagedSource.realmGet$fieldDoubleNull());
        builder.addDate(columnInfo.fieldDateNotNullColKey, unmanagedSource.realmGet$fieldDateNotNull());
        builder.addDate(columnInfo.fieldDateNullColKey, unmanagedSource.realmGet$fieldDateNull());
        builder.addDecimal128(columnInfo.fieldDecimal128NotNullColKey, unmanagedSource.realmGet$fieldDecimal128NotNull());
        builder.addDecimal128(columnInfo.fieldDecimal128NullColKey, unmanagedSource.realmGet$fieldDecimal128Null());
        builder.addObjectId(columnInfo.fieldObjectIdNotNullColKey, unmanagedSource.realmGet$fieldObjectIdNotNull());
        builder.addObjectId(columnInfo.fieldObjectIdNullColKey, unmanagedSource.realmGet$fieldObjectIdNull());
        builder.addStringList(columnInfo.fieldStringListNotNullColKey, unmanagedSource.realmGet$fieldStringListNotNull());
        builder.addStringList(columnInfo.fieldStringListNullColKey, unmanagedSource.realmGet$fieldStringListNull());
        builder.addByteArrayList(columnInfo.fieldBinaryListNotNullColKey, unmanagedSource.realmGet$fieldBinaryListNotNull());
        builder.addByteArrayList(columnInfo.fieldBinaryListNullColKey, unmanagedSource.realmGet$fieldBinaryListNull());
        builder.addBooleanList(columnInfo.fieldBooleanListNotNullColKey, unmanagedSource.realmGet$fieldBooleanListNotNull());
        builder.addBooleanList(columnInfo.fieldBooleanListNullColKey, unmanagedSource.realmGet$fieldBooleanListNull());
        builder.addLongList(columnInfo.fieldLongListNotNullColKey, unmanagedSource.realmGet$fieldLongListNotNull());
        builder.addLongList(columnInfo.fieldLongListNullColKey, unmanagedSource.realmGet$fieldLongListNull());
        builder.addIntegerList(columnInfo.fieldIntegerListNotNullColKey, unmanagedSource.realmGet$fieldIntegerListNotNull());
        builder.addIntegerList(columnInfo.fieldIntegerListNullColKey, unmanagedSource.realmGet$fieldIntegerListNull());
        builder.addShortList(columnInfo.fieldShortListNotNullColKey, unmanagedSource.realmGet$fieldShortListNotNull());
        builder.addShortList(columnInfo.fieldShortListNullColKey, unmanagedSource.realmGet$fieldShortListNull());
        builder.addByteList(columnInfo.fieldByteListNotNullColKey, unmanagedSource.realmGet$fieldByteListNotNull());
        builder.addByteList(columnInfo.fieldByteListNullColKey, unmanagedSource.realmGet$fieldByteListNull());
        builder.addDoubleList(columnInfo.fieldDoubleListNotNullColKey, unmanagedSource.realmGet$fieldDoubleListNotNull());
        builder.addDoubleList(columnInfo.fieldDoubleListNullColKey, unmanagedSource.realmGet$fieldDoubleListNull());
        builder.addFloatList(columnInfo.fieldFloatListNotNullColKey, unmanagedSource.realmGet$fieldFloatListNotNull());
        builder.addFloatList(columnInfo.fieldFloatListNullColKey, unmanagedSource.realmGet$fieldFloatListNull());
        builder.addDateList(columnInfo.fieldDateListNotNullColKey, unmanagedSource.realmGet$fieldDateListNotNull());
        builder.addDateList(columnInfo.fieldDateListNullColKey, unmanagedSource.realmGet$fieldDateListNull());
        builder.addDecimal128List(columnInfo.fieldDecimal128ListNotNullColKey, unmanagedSource.realmGet$fieldDecimal128ListNotNull());
        builder.addDecimal128List(columnInfo.fieldDecimal128ListNullColKey, unmanagedSource.realmGet$fieldDecimal128ListNull());
        builder.addObjectIdList(columnInfo.fieldObjectIdListNotNullColKey, unmanagedSource.realmGet$fieldObjectIdListNotNull());
        builder.addObjectIdList(columnInfo.fieldObjectIdListNullColKey, unmanagedSource.realmGet$fieldObjectIdListNull());

        // Create the underlying object and cache it before setting any object/objectlist references
        // This will allow us to break any circular dependencies by using the object cache.
        Row row = builder.createNewObject();
        io.realm.some_test_NullTypesRealmProxy managedCopy = newProxyInstance(realm, row);
        cache.put(newObject, managedCopy);

        // Finally add all fields that reference other Realm Objects, either directly or through a list
        some.test.NullTypes fieldObjectNullObj = unmanagedSource.realmGet$fieldObjectNull();
        if (fieldObjectNullObj == null) {
            managedCopy.realmSet$fieldObjectNull(null);
        } else {
            some.test.NullTypes cachefieldObjectNull = (some.test.NullTypes) cache.get(fieldObjectNullObj);
            if (cachefieldObjectNull != null) {
                managedCopy.realmSet$fieldObjectNull(cachefieldObjectNull);
            } else {
                managedCopy.realmSet$fieldObjectNull(some_test_NullTypesRealmProxy.copyOrUpdate(realm, (some_test_NullTypesRealmProxy.NullTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.NullTypes.class), fieldObjectNullObj, update, cache, flags));
            }
        }

        return managedCopy;
    }

    public static long insert(Realm realm, some.test.NullTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(some.test.NullTypes.class);
        long tableNativePtr = table.getNativePtr();
        NullTypesColumnInfo columnInfo = (NullTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.NullTypes.class);
        long objKey = OsObject.createRow(table);
        cache.put(object, objKey);
        String realmGet$fieldStringNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNotNull();
        if (realmGet$fieldStringNotNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullColKey, objKey, realmGet$fieldStringNotNull, false);
        }
        String realmGet$fieldStringNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNull();
        if (realmGet$fieldStringNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullColKey, objKey, realmGet$fieldStringNull, false);
        }
        Boolean realmGet$fieldBooleanNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNotNull();
        if (realmGet$fieldBooleanNotNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullColKey, objKey, realmGet$fieldBooleanNotNull, false);
        }
        Boolean realmGet$fieldBooleanNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNull();
        if (realmGet$fieldBooleanNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullColKey, objKey, realmGet$fieldBooleanNull, false);
        }
        byte[] realmGet$fieldBytesNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNotNull();
        if (realmGet$fieldBytesNotNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullColKey, objKey, realmGet$fieldBytesNotNull, false);
        }
        byte[] realmGet$fieldBytesNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNull();
        if (realmGet$fieldBytesNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullColKey, objKey, realmGet$fieldBytesNull, false);
        }
        Number realmGet$fieldByteNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNotNull();
        if (realmGet$fieldByteNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullColKey, objKey, realmGet$fieldByteNotNull.longValue(), false);
        }
        Number realmGet$fieldByteNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNull();
        if (realmGet$fieldByteNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullColKey, objKey, realmGet$fieldByteNull.longValue(), false);
        }
        Number realmGet$fieldShortNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNotNull();
        if (realmGet$fieldShortNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullColKey, objKey, realmGet$fieldShortNotNull.longValue(), false);
        }
        Number realmGet$fieldShortNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNull();
        if (realmGet$fieldShortNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullColKey, objKey, realmGet$fieldShortNull.longValue(), false);
        }
        Number realmGet$fieldIntegerNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNotNull();
        if (realmGet$fieldIntegerNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullColKey, objKey, realmGet$fieldIntegerNotNull.longValue(), false);
        }
        Number realmGet$fieldIntegerNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNull();
        if (realmGet$fieldIntegerNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullColKey, objKey, realmGet$fieldIntegerNull.longValue(), false);
        }
        Number realmGet$fieldLongNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNotNull();
        if (realmGet$fieldLongNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullColKey, objKey, realmGet$fieldLongNotNull.longValue(), false);
        }
        Number realmGet$fieldLongNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNull();
        if (realmGet$fieldLongNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullColKey, objKey, realmGet$fieldLongNull.longValue(), false);
        }
        Float realmGet$fieldFloatNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNotNull();
        if (realmGet$fieldFloatNotNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullColKey, objKey, realmGet$fieldFloatNotNull, false);
        }
        Float realmGet$fieldFloatNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNull();
        if (realmGet$fieldFloatNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullColKey, objKey, realmGet$fieldFloatNull, false);
        }
        Double realmGet$fieldDoubleNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNotNull();
        if (realmGet$fieldDoubleNotNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullColKey, objKey, realmGet$fieldDoubleNotNull, false);
        }
        Double realmGet$fieldDoubleNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNull();
        if (realmGet$fieldDoubleNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullColKey, objKey, realmGet$fieldDoubleNull, false);
        }
        java.util.Date realmGet$fieldDateNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNotNull();
        if (realmGet$fieldDateNotNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullColKey, objKey, realmGet$fieldDateNotNull.getTime(), false);
        }
        java.util.Date realmGet$fieldDateNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNull();
        if (realmGet$fieldDateNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNullColKey, objKey, realmGet$fieldDateNull.getTime(), false);
        }
        org.bson.types.Decimal128 realmGet$fieldDecimal128NotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128NotNull();
        if (realmGet$fieldDecimal128NotNull != null) {
            Table.nativeSetDecimal128(tableNativePtr, columnInfo.fieldDecimal128NotNullColKey, objKey, realmGet$fieldDecimal128NotNull.getLow(), realmGet$fieldDecimal128NotNull.getHigh(), false);
        }
        org.bson.types.Decimal128 realmGet$fieldDecimal128Null = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128Null();
        if (realmGet$fieldDecimal128Null != null) {
            Table.nativeSetDecimal128(tableNativePtr, columnInfo.fieldDecimal128NullColKey, objKey, realmGet$fieldDecimal128Null.getLow(), realmGet$fieldDecimal128Null.getHigh(), false);
        }
        org.bson.types.ObjectId realmGet$fieldObjectIdNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdNotNull();
        if (realmGet$fieldObjectIdNotNull != null) {
            Table.nativeSetObjectId(tableNativePtr, columnInfo.fieldObjectIdNotNullColKey, objKey, realmGet$fieldObjectIdNotNull.toString(), false);
        }
        org.bson.types.ObjectId realmGet$fieldObjectIdNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdNull();
        if (realmGet$fieldObjectIdNull != null) {
            Table.nativeSetObjectId(tableNativePtr, columnInfo.fieldObjectIdNullColKey, objKey, realmGet$fieldObjectIdNull.toString(), false);
        }

        some.test.NullTypes fieldObjectNullObj = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectNull();
        if (fieldObjectNullObj != null) {
            Long cachefieldObjectNull = cache.get(fieldObjectNullObj);
            if (cachefieldObjectNull == null) {
                cachefieldObjectNull = some_test_NullTypesRealmProxy.insert(realm, fieldObjectNullObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.fieldObjectNullColKey, objKey, cachefieldObjectNull, false);
        }

        RealmList<java.lang.String> fieldStringListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringListNotNull();
        if (fieldStringListNotNullList != null) {
            OsList fieldStringListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldStringListNotNullColKey);
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
            OsList fieldStringListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldStringListNullColKey);
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
            OsList fieldBinaryListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBinaryListNotNullColKey);
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
            OsList fieldBinaryListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBinaryListNullColKey);
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
            OsList fieldBooleanListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBooleanListNotNullColKey);
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
            OsList fieldBooleanListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBooleanListNullColKey);
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
            OsList fieldLongListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldLongListNotNullColKey);
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
            OsList fieldLongListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldLongListNullColKey);
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
            OsList fieldIntegerListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldIntegerListNotNullColKey);
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
            OsList fieldIntegerListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldIntegerListNullColKey);
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
            OsList fieldShortListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldShortListNotNullColKey);
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
            OsList fieldShortListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldShortListNullColKey);
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
            OsList fieldByteListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldByteListNotNullColKey);
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
            OsList fieldByteListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldByteListNullColKey);
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
            OsList fieldDoubleListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDoubleListNotNullColKey);
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
            OsList fieldDoubleListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDoubleListNullColKey);
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
            OsList fieldFloatListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldFloatListNotNullColKey);
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
            OsList fieldFloatListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldFloatListNullColKey);
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
            OsList fieldDateListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDateListNotNullColKey);
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
            OsList fieldDateListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDateListNullColKey);
            for (java.util.Date fieldDateListNullItem : fieldDateListNullList) {
                if (fieldDateListNullItem == null) {
                    fieldDateListNullOsList.addNull();
                } else {
                    fieldDateListNullOsList.addDate(fieldDateListNullItem);
                }
            }
        }

        RealmList<org.bson.types.Decimal128> fieldDecimal128ListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128ListNotNull();
        if (fieldDecimal128ListNotNullList != null) {
            OsList fieldDecimal128ListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDecimal128ListNotNullColKey);
            for (org.bson.types.Decimal128 fieldDecimal128ListNotNullItem : fieldDecimal128ListNotNullList) {
                if (fieldDecimal128ListNotNullItem == null) {
                    fieldDecimal128ListNotNullOsList.addNull();
                } else {
                    fieldDecimal128ListNotNullOsList.addDecimal128(fieldDecimal128ListNotNullItem);
                }
            }
        }

        RealmList<org.bson.types.Decimal128> fieldDecimal128ListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128ListNull();
        if (fieldDecimal128ListNullList != null) {
            OsList fieldDecimal128ListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDecimal128ListNullColKey);
            for (org.bson.types.Decimal128 fieldDecimal128ListNullItem : fieldDecimal128ListNullList) {
                if (fieldDecimal128ListNullItem == null) {
                    fieldDecimal128ListNullOsList.addNull();
                } else {
                    fieldDecimal128ListNullOsList.addDecimal128(fieldDecimal128ListNullItem);
                }
            }
        }

        RealmList<org.bson.types.ObjectId> fieldObjectIdListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdListNotNull();
        if (fieldObjectIdListNotNullList != null) {
            OsList fieldObjectIdListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldObjectIdListNotNullColKey);
            for (org.bson.types.ObjectId fieldObjectIdListNotNullItem : fieldObjectIdListNotNullList) {
                if (fieldObjectIdListNotNullItem == null) {
                    fieldObjectIdListNotNullOsList.addNull();
                } else {
                    fieldObjectIdListNotNullOsList.addObjectId(fieldObjectIdListNotNullItem);
                }
            }
        }

        RealmList<org.bson.types.ObjectId> fieldObjectIdListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdListNull();
        if (fieldObjectIdListNullList != null) {
            OsList fieldObjectIdListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldObjectIdListNullColKey);
            for (org.bson.types.ObjectId fieldObjectIdListNullItem : fieldObjectIdListNullList) {
                if (fieldObjectIdListNullItem == null) {
                    fieldObjectIdListNullOsList.addNull();
                } else {
                    fieldObjectIdListNullOsList.addObjectId(fieldObjectIdListNullItem);
                }
            }
        }
        return objKey;
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
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            long objKey = OsObject.createRow(table);
            cache.put(object, objKey);
            String realmGet$fieldStringNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNotNull();
            if (realmGet$fieldStringNotNull != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullColKey, objKey, realmGet$fieldStringNotNull, false);
            }
            String realmGet$fieldStringNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNull();
            if (realmGet$fieldStringNull != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullColKey, objKey, realmGet$fieldStringNull, false);
            }
            Boolean realmGet$fieldBooleanNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNotNull();
            if (realmGet$fieldBooleanNotNull != null) {
                Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullColKey, objKey, realmGet$fieldBooleanNotNull, false);
            }
            Boolean realmGet$fieldBooleanNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNull();
            if (realmGet$fieldBooleanNull != null) {
                Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullColKey, objKey, realmGet$fieldBooleanNull, false);
            }
            byte[] realmGet$fieldBytesNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNotNull();
            if (realmGet$fieldBytesNotNull != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullColKey, objKey, realmGet$fieldBytesNotNull, false);
            }
            byte[] realmGet$fieldBytesNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNull();
            if (realmGet$fieldBytesNull != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullColKey, objKey, realmGet$fieldBytesNull, false);
            }
            Number realmGet$fieldByteNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNotNull();
            if (realmGet$fieldByteNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullColKey, objKey, realmGet$fieldByteNotNull.longValue(), false);
            }
            Number realmGet$fieldByteNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNull();
            if (realmGet$fieldByteNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullColKey, objKey, realmGet$fieldByteNull.longValue(), false);
            }
            Number realmGet$fieldShortNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNotNull();
            if (realmGet$fieldShortNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullColKey, objKey, realmGet$fieldShortNotNull.longValue(), false);
            }
            Number realmGet$fieldShortNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNull();
            if (realmGet$fieldShortNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullColKey, objKey, realmGet$fieldShortNull.longValue(), false);
            }
            Number realmGet$fieldIntegerNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNotNull();
            if (realmGet$fieldIntegerNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullColKey, objKey, realmGet$fieldIntegerNotNull.longValue(), false);
            }
            Number realmGet$fieldIntegerNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNull();
            if (realmGet$fieldIntegerNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullColKey, objKey, realmGet$fieldIntegerNull.longValue(), false);
            }
            Number realmGet$fieldLongNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNotNull();
            if (realmGet$fieldLongNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullColKey, objKey, realmGet$fieldLongNotNull.longValue(), false);
            }
            Number realmGet$fieldLongNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNull();
            if (realmGet$fieldLongNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullColKey, objKey, realmGet$fieldLongNull.longValue(), false);
            }
            Float realmGet$fieldFloatNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNotNull();
            if (realmGet$fieldFloatNotNull != null) {
                Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullColKey, objKey, realmGet$fieldFloatNotNull, false);
            }
            Float realmGet$fieldFloatNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNull();
            if (realmGet$fieldFloatNull != null) {
                Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullColKey, objKey, realmGet$fieldFloatNull, false);
            }
            Double realmGet$fieldDoubleNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNotNull();
            if (realmGet$fieldDoubleNotNull != null) {
                Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullColKey, objKey, realmGet$fieldDoubleNotNull, false);
            }
            Double realmGet$fieldDoubleNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNull();
            if (realmGet$fieldDoubleNull != null) {
                Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullColKey, objKey, realmGet$fieldDoubleNull, false);
            }
            java.util.Date realmGet$fieldDateNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNotNull();
            if (realmGet$fieldDateNotNull != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullColKey, objKey, realmGet$fieldDateNotNull.getTime(), false);
            }
            java.util.Date realmGet$fieldDateNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNull();
            if (realmGet$fieldDateNull != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNullColKey, objKey, realmGet$fieldDateNull.getTime(), false);
            }
            org.bson.types.Decimal128 realmGet$fieldDecimal128NotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128NotNull();
            if (realmGet$fieldDecimal128NotNull != null) {
                Table.nativeSetDecimal128(tableNativePtr, columnInfo.fieldDecimal128NotNullColKey, objKey, realmGet$fieldDecimal128NotNull.getLow(), realmGet$fieldDecimal128NotNull.getHigh(), false);
            }
            org.bson.types.Decimal128 realmGet$fieldDecimal128Null = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128Null();
            if (realmGet$fieldDecimal128Null != null) {
                Table.nativeSetDecimal128(tableNativePtr, columnInfo.fieldDecimal128NullColKey, objKey, realmGet$fieldDecimal128Null.getLow(), realmGet$fieldDecimal128Null.getHigh(), false);
            }
            org.bson.types.ObjectId realmGet$fieldObjectIdNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdNotNull();
            if (realmGet$fieldObjectIdNotNull != null) {
                Table.nativeSetObjectId(tableNativePtr, columnInfo.fieldObjectIdNotNullColKey, objKey, realmGet$fieldObjectIdNotNull.toString(), false);
            }
            org.bson.types.ObjectId realmGet$fieldObjectIdNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdNull();
            if (realmGet$fieldObjectIdNull != null) {
                Table.nativeSetObjectId(tableNativePtr, columnInfo.fieldObjectIdNullColKey, objKey, realmGet$fieldObjectIdNull.toString(), false);
            }

            some.test.NullTypes fieldObjectNullObj = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectNull();
            if (fieldObjectNullObj != null) {
                Long cachefieldObjectNull = cache.get(fieldObjectNullObj);
                if (cachefieldObjectNull == null) {
                    cachefieldObjectNull = some_test_NullTypesRealmProxy.insert(realm, fieldObjectNullObj, cache);
                }
                table.setLink(columnInfo.fieldObjectNullColKey, objKey, cachefieldObjectNull, false);
            }

            RealmList<java.lang.String> fieldStringListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringListNotNull();
            if (fieldStringListNotNullList != null) {
                OsList fieldStringListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldStringListNotNullColKey);
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
                OsList fieldStringListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldStringListNullColKey);
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
                OsList fieldBinaryListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBinaryListNotNullColKey);
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
                OsList fieldBinaryListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBinaryListNullColKey);
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
                OsList fieldBooleanListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBooleanListNotNullColKey);
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
                OsList fieldBooleanListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBooleanListNullColKey);
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
                OsList fieldLongListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldLongListNotNullColKey);
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
                OsList fieldLongListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldLongListNullColKey);
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
                OsList fieldIntegerListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldIntegerListNotNullColKey);
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
                OsList fieldIntegerListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldIntegerListNullColKey);
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
                OsList fieldShortListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldShortListNotNullColKey);
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
                OsList fieldShortListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldShortListNullColKey);
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
                OsList fieldByteListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldByteListNotNullColKey);
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
                OsList fieldByteListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldByteListNullColKey);
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
                OsList fieldDoubleListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDoubleListNotNullColKey);
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
                OsList fieldDoubleListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDoubleListNullColKey);
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
                OsList fieldFloatListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldFloatListNotNullColKey);
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
                OsList fieldFloatListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldFloatListNullColKey);
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
                OsList fieldDateListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDateListNotNullColKey);
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
                OsList fieldDateListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDateListNullColKey);
                for (java.util.Date fieldDateListNullItem : fieldDateListNullList) {
                    if (fieldDateListNullItem == null) {
                        fieldDateListNullOsList.addNull();
                    } else {
                        fieldDateListNullOsList.addDate(fieldDateListNullItem);
                    }
                }
            }

            RealmList<org.bson.types.Decimal128> fieldDecimal128ListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128ListNotNull();
            if (fieldDecimal128ListNotNullList != null) {
                OsList fieldDecimal128ListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDecimal128ListNotNullColKey);
                for (org.bson.types.Decimal128 fieldDecimal128ListNotNullItem : fieldDecimal128ListNotNullList) {
                    if (fieldDecimal128ListNotNullItem == null) {
                        fieldDecimal128ListNotNullOsList.addNull();
                    } else {
                        fieldDecimal128ListNotNullOsList.addDecimal128(fieldDecimal128ListNotNullItem);
                    }
                }
            }

            RealmList<org.bson.types.Decimal128> fieldDecimal128ListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128ListNull();
            if (fieldDecimal128ListNullList != null) {
                OsList fieldDecimal128ListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDecimal128ListNullColKey);
                for (org.bson.types.Decimal128 fieldDecimal128ListNullItem : fieldDecimal128ListNullList) {
                    if (fieldDecimal128ListNullItem == null) {
                        fieldDecimal128ListNullOsList.addNull();
                    } else {
                        fieldDecimal128ListNullOsList.addDecimal128(fieldDecimal128ListNullItem);
                    }
                }
            }

            RealmList<org.bson.types.ObjectId> fieldObjectIdListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdListNotNull();
            if (fieldObjectIdListNotNullList != null) {
                OsList fieldObjectIdListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldObjectIdListNotNullColKey);
                for (org.bson.types.ObjectId fieldObjectIdListNotNullItem : fieldObjectIdListNotNullList) {
                    if (fieldObjectIdListNotNullItem == null) {
                        fieldObjectIdListNotNullOsList.addNull();
                    } else {
                        fieldObjectIdListNotNullOsList.addObjectId(fieldObjectIdListNotNullItem);
                    }
                }
            }

            RealmList<org.bson.types.ObjectId> fieldObjectIdListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdListNull();
            if (fieldObjectIdListNullList != null) {
                OsList fieldObjectIdListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldObjectIdListNullColKey);
                for (org.bson.types.ObjectId fieldObjectIdListNullItem : fieldObjectIdListNullList) {
                    if (fieldObjectIdListNullItem == null) {
                        fieldObjectIdListNullOsList.addNull();
                    } else {
                        fieldObjectIdListNullOsList.addObjectId(fieldObjectIdListNullItem);
                    }
                }
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.NullTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(some.test.NullTypes.class);
        long tableNativePtr = table.getNativePtr();
        NullTypesColumnInfo columnInfo = (NullTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.NullTypes.class);
        long objKey = OsObject.createRow(table);
        cache.put(object, objKey);
        String realmGet$fieldStringNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNotNull();
        if (realmGet$fieldStringNotNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullColKey, objKey, realmGet$fieldStringNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNotNullColKey, objKey, false);
        }
        String realmGet$fieldStringNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNull();
        if (realmGet$fieldStringNull != null) {
            Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullColKey, objKey, realmGet$fieldStringNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNullColKey, objKey, false);
        }
        Boolean realmGet$fieldBooleanNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNotNull();
        if (realmGet$fieldBooleanNotNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullColKey, objKey, realmGet$fieldBooleanNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNotNullColKey, objKey, false);
        }
        Boolean realmGet$fieldBooleanNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNull();
        if (realmGet$fieldBooleanNull != null) {
            Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullColKey, objKey, realmGet$fieldBooleanNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNullColKey, objKey, false);
        }
        byte[] realmGet$fieldBytesNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNotNull();
        if (realmGet$fieldBytesNotNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullColKey, objKey, realmGet$fieldBytesNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNotNullColKey, objKey, false);
        }
        byte[] realmGet$fieldBytesNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNull();
        if (realmGet$fieldBytesNull != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullColKey, objKey, realmGet$fieldBytesNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNullColKey, objKey, false);
        }
        Number realmGet$fieldByteNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNotNull();
        if (realmGet$fieldByteNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullColKey, objKey, realmGet$fieldByteNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNotNullColKey, objKey, false);
        }
        Number realmGet$fieldByteNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNull();
        if (realmGet$fieldByteNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullColKey, objKey, realmGet$fieldByteNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNullColKey, objKey, false);
        }
        Number realmGet$fieldShortNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNotNull();
        if (realmGet$fieldShortNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullColKey, objKey, realmGet$fieldShortNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNotNullColKey, objKey, false);
        }
        Number realmGet$fieldShortNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNull();
        if (realmGet$fieldShortNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullColKey, objKey, realmGet$fieldShortNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNullColKey, objKey, false);
        }
        Number realmGet$fieldIntegerNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNotNull();
        if (realmGet$fieldIntegerNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullColKey, objKey, realmGet$fieldIntegerNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNotNullColKey, objKey, false);
        }
        Number realmGet$fieldIntegerNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNull();
        if (realmGet$fieldIntegerNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullColKey, objKey, realmGet$fieldIntegerNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNullColKey, objKey, false);
        }
        Number realmGet$fieldLongNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNotNull();
        if (realmGet$fieldLongNotNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullColKey, objKey, realmGet$fieldLongNotNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNotNullColKey, objKey, false);
        }
        Number realmGet$fieldLongNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNull();
        if (realmGet$fieldLongNull != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullColKey, objKey, realmGet$fieldLongNull.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNullColKey, objKey, false);
        }
        Float realmGet$fieldFloatNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNotNull();
        if (realmGet$fieldFloatNotNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullColKey, objKey, realmGet$fieldFloatNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNotNullColKey, objKey, false);
        }
        Float realmGet$fieldFloatNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNull();
        if (realmGet$fieldFloatNull != null) {
            Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullColKey, objKey, realmGet$fieldFloatNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNullColKey, objKey, false);
        }
        Double realmGet$fieldDoubleNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNotNull();
        if (realmGet$fieldDoubleNotNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullColKey, objKey, realmGet$fieldDoubleNotNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNotNullColKey, objKey, false);
        }
        Double realmGet$fieldDoubleNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNull();
        if (realmGet$fieldDoubleNull != null) {
            Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullColKey, objKey, realmGet$fieldDoubleNull, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNullColKey, objKey, false);
        }
        java.util.Date realmGet$fieldDateNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNotNull();
        if (realmGet$fieldDateNotNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullColKey, objKey, realmGet$fieldDateNotNull.getTime(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNotNullColKey, objKey, false);
        }
        java.util.Date realmGet$fieldDateNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNull();
        if (realmGet$fieldDateNull != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNullColKey, objKey, realmGet$fieldDateNull.getTime(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNullColKey, objKey, false);
        }
        org.bson.types.Decimal128 realmGet$fieldDecimal128NotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128NotNull();
        if (realmGet$fieldDecimal128NotNull != null) {
            Table.nativeSetDecimal128(tableNativePtr, columnInfo.fieldDecimal128NotNullColKey, objKey, realmGet$fieldDecimal128NotNull.getLow(), realmGet$fieldDecimal128NotNull.getHigh(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDecimal128NotNullColKey, objKey, false);
        }
        org.bson.types.Decimal128 realmGet$fieldDecimal128Null = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128Null();
        if (realmGet$fieldDecimal128Null != null) {
            Table.nativeSetDecimal128(tableNativePtr, columnInfo.fieldDecimal128NullColKey, objKey, realmGet$fieldDecimal128Null.getLow(), realmGet$fieldDecimal128Null.getHigh(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldDecimal128NullColKey, objKey, false);
        }
        org.bson.types.ObjectId realmGet$fieldObjectIdNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdNotNull();
        if (realmGet$fieldObjectIdNotNull != null) {
            Table.nativeSetObjectId(tableNativePtr, columnInfo.fieldObjectIdNotNullColKey, objKey, realmGet$fieldObjectIdNotNull.toString(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldObjectIdNotNullColKey, objKey, false);
        }
        org.bson.types.ObjectId realmGet$fieldObjectIdNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdNull();
        if (realmGet$fieldObjectIdNull != null) {
            Table.nativeSetObjectId(tableNativePtr, columnInfo.fieldObjectIdNullColKey, objKey, realmGet$fieldObjectIdNull.toString(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.fieldObjectIdNullColKey, objKey, false);
        }

        some.test.NullTypes fieldObjectNullObj = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectNull();
        if (fieldObjectNullObj != null) {
            Long cachefieldObjectNull = cache.get(fieldObjectNullObj);
            if (cachefieldObjectNull == null) {
                cachefieldObjectNull = some_test_NullTypesRealmProxy.insertOrUpdate(realm, fieldObjectNullObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.fieldObjectNullColKey, objKey, cachefieldObjectNull, false);
        } else {
            Table.nativeNullifyLink(tableNativePtr, columnInfo.fieldObjectNullColKey, objKey);
        }

        OsList fieldStringListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldStringListNotNullColKey);
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


        OsList fieldStringListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldStringListNullColKey);
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


        OsList fieldBinaryListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBinaryListNotNullColKey);
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


        OsList fieldBinaryListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBinaryListNullColKey);
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


        OsList fieldBooleanListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBooleanListNotNullColKey);
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


        OsList fieldBooleanListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBooleanListNullColKey);
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


        OsList fieldLongListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldLongListNotNullColKey);
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


        OsList fieldLongListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldLongListNullColKey);
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


        OsList fieldIntegerListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldIntegerListNotNullColKey);
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


        OsList fieldIntegerListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldIntegerListNullColKey);
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


        OsList fieldShortListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldShortListNotNullColKey);
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


        OsList fieldShortListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldShortListNullColKey);
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


        OsList fieldByteListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldByteListNotNullColKey);
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


        OsList fieldByteListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldByteListNullColKey);
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


        OsList fieldDoubleListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDoubleListNotNullColKey);
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


        OsList fieldDoubleListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDoubleListNullColKey);
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


        OsList fieldFloatListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldFloatListNotNullColKey);
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


        OsList fieldFloatListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldFloatListNullColKey);
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


        OsList fieldDateListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDateListNotNullColKey);
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


        OsList fieldDateListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDateListNullColKey);
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


        OsList fieldDecimal128ListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDecimal128ListNotNullColKey);
        fieldDecimal128ListNotNullOsList.removeAll();
        RealmList<org.bson.types.Decimal128> fieldDecimal128ListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128ListNotNull();
        if (fieldDecimal128ListNotNullList != null) {
            for (org.bson.types.Decimal128 fieldDecimal128ListNotNullItem : fieldDecimal128ListNotNullList) {
                if (fieldDecimal128ListNotNullItem == null) {
                    fieldDecimal128ListNotNullOsList.addNull();
                } else {
                    fieldDecimal128ListNotNullOsList.addDecimal128(fieldDecimal128ListNotNullItem);
                }
            }
        }


        OsList fieldDecimal128ListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDecimal128ListNullColKey);
        fieldDecimal128ListNullOsList.removeAll();
        RealmList<org.bson.types.Decimal128> fieldDecimal128ListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128ListNull();
        if (fieldDecimal128ListNullList != null) {
            for (org.bson.types.Decimal128 fieldDecimal128ListNullItem : fieldDecimal128ListNullList) {
                if (fieldDecimal128ListNullItem == null) {
                    fieldDecimal128ListNullOsList.addNull();
                } else {
                    fieldDecimal128ListNullOsList.addDecimal128(fieldDecimal128ListNullItem);
                }
            }
        }


        OsList fieldObjectIdListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldObjectIdListNotNullColKey);
        fieldObjectIdListNotNullOsList.removeAll();
        RealmList<org.bson.types.ObjectId> fieldObjectIdListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdListNotNull();
        if (fieldObjectIdListNotNullList != null) {
            for (org.bson.types.ObjectId fieldObjectIdListNotNullItem : fieldObjectIdListNotNullList) {
                if (fieldObjectIdListNotNullItem == null) {
                    fieldObjectIdListNotNullOsList.addNull();
                } else {
                    fieldObjectIdListNotNullOsList.addObjectId(fieldObjectIdListNotNullItem);
                }
            }
        }


        OsList fieldObjectIdListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldObjectIdListNullColKey);
        fieldObjectIdListNullOsList.removeAll();
        RealmList<org.bson.types.ObjectId> fieldObjectIdListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdListNull();
        if (fieldObjectIdListNullList != null) {
            for (org.bson.types.ObjectId fieldObjectIdListNullItem : fieldObjectIdListNullList) {
                if (fieldObjectIdListNullItem == null) {
                    fieldObjectIdListNullOsList.addNull();
                } else {
                    fieldObjectIdListNullOsList.addObjectId(fieldObjectIdListNullItem);
                }
            }
        }

        return objKey;
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
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            long objKey = OsObject.createRow(table);
            cache.put(object, objKey);
            String realmGet$fieldStringNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNotNull();
            if (realmGet$fieldStringNotNull != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNotNullColKey, objKey, realmGet$fieldStringNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNotNullColKey, objKey, false);
            }
            String realmGet$fieldStringNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldStringNull();
            if (realmGet$fieldStringNull != null) {
                Table.nativeSetString(tableNativePtr, columnInfo.fieldStringNullColKey, objKey, realmGet$fieldStringNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldStringNullColKey, objKey, false);
            }
            Boolean realmGet$fieldBooleanNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNotNull();
            if (realmGet$fieldBooleanNotNull != null) {
                Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNotNullColKey, objKey, realmGet$fieldBooleanNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNotNullColKey, objKey, false);
            }
            Boolean realmGet$fieldBooleanNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBooleanNull();
            if (realmGet$fieldBooleanNull != null) {
                Table.nativeSetBoolean(tableNativePtr, columnInfo.fieldBooleanNullColKey, objKey, realmGet$fieldBooleanNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldBooleanNullColKey, objKey, false);
            }
            byte[] realmGet$fieldBytesNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNotNull();
            if (realmGet$fieldBytesNotNull != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNotNullColKey, objKey, realmGet$fieldBytesNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNotNullColKey, objKey, false);
            }
            byte[] realmGet$fieldBytesNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldBytesNull();
            if (realmGet$fieldBytesNull != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.fieldBytesNullColKey, objKey, realmGet$fieldBytesNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldBytesNullColKey, objKey, false);
            }
            Number realmGet$fieldByteNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNotNull();
            if (realmGet$fieldByteNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNotNullColKey, objKey, realmGet$fieldByteNotNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNotNullColKey, objKey, false);
            }
            Number realmGet$fieldByteNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldByteNull();
            if (realmGet$fieldByteNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldByteNullColKey, objKey, realmGet$fieldByteNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldByteNullColKey, objKey, false);
            }
            Number realmGet$fieldShortNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNotNull();
            if (realmGet$fieldShortNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNotNullColKey, objKey, realmGet$fieldShortNotNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNotNullColKey, objKey, false);
            }
            Number realmGet$fieldShortNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldShortNull();
            if (realmGet$fieldShortNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldShortNullColKey, objKey, realmGet$fieldShortNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldShortNullColKey, objKey, false);
            }
            Number realmGet$fieldIntegerNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNotNull();
            if (realmGet$fieldIntegerNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNotNullColKey, objKey, realmGet$fieldIntegerNotNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNotNullColKey, objKey, false);
            }
            Number realmGet$fieldIntegerNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldIntegerNull();
            if (realmGet$fieldIntegerNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldIntegerNullColKey, objKey, realmGet$fieldIntegerNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldIntegerNullColKey, objKey, false);
            }
            Number realmGet$fieldLongNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNotNull();
            if (realmGet$fieldLongNotNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNotNullColKey, objKey, realmGet$fieldLongNotNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNotNullColKey, objKey, false);
            }
            Number realmGet$fieldLongNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldLongNull();
            if (realmGet$fieldLongNull != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.fieldLongNullColKey, objKey, realmGet$fieldLongNull.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldLongNullColKey, objKey, false);
            }
            Float realmGet$fieldFloatNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNotNull();
            if (realmGet$fieldFloatNotNull != null) {
                Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNotNullColKey, objKey, realmGet$fieldFloatNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNotNullColKey, objKey, false);
            }
            Float realmGet$fieldFloatNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldFloatNull();
            if (realmGet$fieldFloatNull != null) {
                Table.nativeSetFloat(tableNativePtr, columnInfo.fieldFloatNullColKey, objKey, realmGet$fieldFloatNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldFloatNullColKey, objKey, false);
            }
            Double realmGet$fieldDoubleNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNotNull();
            if (realmGet$fieldDoubleNotNull != null) {
                Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNotNullColKey, objKey, realmGet$fieldDoubleNotNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNotNullColKey, objKey, false);
            }
            Double realmGet$fieldDoubleNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDoubleNull();
            if (realmGet$fieldDoubleNull != null) {
                Table.nativeSetDouble(tableNativePtr, columnInfo.fieldDoubleNullColKey, objKey, realmGet$fieldDoubleNull, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDoubleNullColKey, objKey, false);
            }
            java.util.Date realmGet$fieldDateNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNotNull();
            if (realmGet$fieldDateNotNull != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNotNullColKey, objKey, realmGet$fieldDateNotNull.getTime(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNotNullColKey, objKey, false);
            }
            java.util.Date realmGet$fieldDateNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDateNull();
            if (realmGet$fieldDateNull != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.fieldDateNullColKey, objKey, realmGet$fieldDateNull.getTime(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDateNullColKey, objKey, false);
            }
            org.bson.types.Decimal128 realmGet$fieldDecimal128NotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128NotNull();
            if (realmGet$fieldDecimal128NotNull != null) {
                Table.nativeSetDecimal128(tableNativePtr, columnInfo.fieldDecimal128NotNullColKey, objKey, realmGet$fieldDecimal128NotNull.getLow(), realmGet$fieldDecimal128NotNull.getHigh(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDecimal128NotNullColKey, objKey, false);
            }
            org.bson.types.Decimal128 realmGet$fieldDecimal128Null = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128Null();
            if (realmGet$fieldDecimal128Null != null) {
                Table.nativeSetDecimal128(tableNativePtr, columnInfo.fieldDecimal128NullColKey, objKey, realmGet$fieldDecimal128Null.getLow(), realmGet$fieldDecimal128Null.getHigh(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldDecimal128NullColKey, objKey, false);
            }
            org.bson.types.ObjectId realmGet$fieldObjectIdNotNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdNotNull();
            if (realmGet$fieldObjectIdNotNull != null) {
                Table.nativeSetObjectId(tableNativePtr, columnInfo.fieldObjectIdNotNullColKey, objKey, realmGet$fieldObjectIdNotNull.toString(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldObjectIdNotNullColKey, objKey, false);
            }
            org.bson.types.ObjectId realmGet$fieldObjectIdNull = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdNull();
            if (realmGet$fieldObjectIdNull != null) {
                Table.nativeSetObjectId(tableNativePtr, columnInfo.fieldObjectIdNullColKey, objKey, realmGet$fieldObjectIdNull.toString(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.fieldObjectIdNullColKey, objKey, false);
            }

            some.test.NullTypes fieldObjectNullObj = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectNull();
            if (fieldObjectNullObj != null) {
                Long cachefieldObjectNull = cache.get(fieldObjectNullObj);
                if (cachefieldObjectNull == null) {
                    cachefieldObjectNull = some_test_NullTypesRealmProxy.insertOrUpdate(realm, fieldObjectNullObj, cache);
                }
                Table.nativeSetLink(tableNativePtr, columnInfo.fieldObjectNullColKey, objKey, cachefieldObjectNull, false);
            } else {
                Table.nativeNullifyLink(tableNativePtr, columnInfo.fieldObjectNullColKey, objKey);
            }

            OsList fieldStringListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldStringListNotNullColKey);
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


            OsList fieldStringListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldStringListNullColKey);
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


            OsList fieldBinaryListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBinaryListNotNullColKey);
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


            OsList fieldBinaryListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBinaryListNullColKey);
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


            OsList fieldBooleanListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBooleanListNotNullColKey);
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


            OsList fieldBooleanListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldBooleanListNullColKey);
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


            OsList fieldLongListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldLongListNotNullColKey);
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


            OsList fieldLongListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldLongListNullColKey);
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


            OsList fieldIntegerListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldIntegerListNotNullColKey);
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


            OsList fieldIntegerListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldIntegerListNullColKey);
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


            OsList fieldShortListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldShortListNotNullColKey);
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


            OsList fieldShortListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldShortListNullColKey);
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


            OsList fieldByteListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldByteListNotNullColKey);
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


            OsList fieldByteListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldByteListNullColKey);
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


            OsList fieldDoubleListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDoubleListNotNullColKey);
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


            OsList fieldDoubleListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDoubleListNullColKey);
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


            OsList fieldFloatListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldFloatListNotNullColKey);
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


            OsList fieldFloatListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldFloatListNullColKey);
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


            OsList fieldDateListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDateListNotNullColKey);
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


            OsList fieldDateListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDateListNullColKey);
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


            OsList fieldDecimal128ListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDecimal128ListNotNullColKey);
            fieldDecimal128ListNotNullOsList.removeAll();
            RealmList<org.bson.types.Decimal128> fieldDecimal128ListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128ListNotNull();
            if (fieldDecimal128ListNotNullList != null) {
                for (org.bson.types.Decimal128 fieldDecimal128ListNotNullItem : fieldDecimal128ListNotNullList) {
                    if (fieldDecimal128ListNotNullItem == null) {
                        fieldDecimal128ListNotNullOsList.addNull();
                    } else {
                        fieldDecimal128ListNotNullOsList.addDecimal128(fieldDecimal128ListNotNullItem);
                    }
                }
            }


            OsList fieldDecimal128ListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldDecimal128ListNullColKey);
            fieldDecimal128ListNullOsList.removeAll();
            RealmList<org.bson.types.Decimal128> fieldDecimal128ListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldDecimal128ListNull();
            if (fieldDecimal128ListNullList != null) {
                for (org.bson.types.Decimal128 fieldDecimal128ListNullItem : fieldDecimal128ListNullList) {
                    if (fieldDecimal128ListNullItem == null) {
                        fieldDecimal128ListNullOsList.addNull();
                    } else {
                        fieldDecimal128ListNullOsList.addDecimal128(fieldDecimal128ListNullItem);
                    }
                }
            }


            OsList fieldObjectIdListNotNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldObjectIdListNotNullColKey);
            fieldObjectIdListNotNullOsList.removeAll();
            RealmList<org.bson.types.ObjectId> fieldObjectIdListNotNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdListNotNull();
            if (fieldObjectIdListNotNullList != null) {
                for (org.bson.types.ObjectId fieldObjectIdListNotNullItem : fieldObjectIdListNotNullList) {
                    if (fieldObjectIdListNotNullItem == null) {
                        fieldObjectIdListNotNullOsList.addNull();
                    } else {
                        fieldObjectIdListNotNullOsList.addObjectId(fieldObjectIdListNotNullItem);
                    }
                }
            }


            OsList fieldObjectIdListNullOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.fieldObjectIdListNullColKey);
            fieldObjectIdListNullOsList.removeAll();
            RealmList<org.bson.types.ObjectId> fieldObjectIdListNullList = ((some_test_NullTypesRealmProxyInterface) object).realmGet$fieldObjectIdListNull();
            if (fieldObjectIdListNullList != null) {
                for (org.bson.types.ObjectId fieldObjectIdListNullItem : fieldObjectIdListNullList) {
                    if (fieldObjectIdListNullItem == null) {
                        fieldObjectIdListNullOsList.addNull();
                    } else {
                        fieldObjectIdListNullOsList.addObjectId(fieldObjectIdListNullItem);
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
        unmanagedCopy.realmSet$fieldDecimal128NotNull(realmSource.realmGet$fieldDecimal128NotNull());
        unmanagedCopy.realmSet$fieldDecimal128Null(realmSource.realmGet$fieldDecimal128Null());
        unmanagedCopy.realmSet$fieldObjectIdNotNull(realmSource.realmGet$fieldObjectIdNotNull());
        unmanagedCopy.realmSet$fieldObjectIdNull(realmSource.realmGet$fieldObjectIdNull());

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

        unmanagedCopy.realmSet$fieldDecimal128ListNotNull(new RealmList<org.bson.types.Decimal128>());
        unmanagedCopy.realmGet$fieldDecimal128ListNotNull().addAll(realmSource.realmGet$fieldDecimal128ListNotNull());

        unmanagedCopy.realmSet$fieldDecimal128ListNull(new RealmList<org.bson.types.Decimal128>());
        unmanagedCopy.realmGet$fieldDecimal128ListNull().addAll(realmSource.realmGet$fieldDecimal128ListNull());

        unmanagedCopy.realmSet$fieldObjectIdListNotNull(new RealmList<org.bson.types.ObjectId>());
        unmanagedCopy.realmGet$fieldObjectIdListNotNull().addAll(realmSource.realmGet$fieldObjectIdListNotNull());

        unmanagedCopy.realmSet$fieldObjectIdListNull(new RealmList<org.bson.types.ObjectId>());
        unmanagedCopy.realmGet$fieldObjectIdListNull().addAll(realmSource.realmGet$fieldObjectIdListNull());

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
        stringBuilder.append("binary(" + realmGet$fieldBytesNotNull().length + ")");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldBytesNull:");
        stringBuilder.append((realmGet$fieldBytesNull() == null) ? "null" : "binary(" + realmGet$fieldBytesNull().length + ")");
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
        stringBuilder.append("{fieldDecimal128NotNull:");
        stringBuilder.append(realmGet$fieldDecimal128NotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDecimal128Null:");
        stringBuilder.append(realmGet$fieldDecimal128Null() != null ? realmGet$fieldDecimal128Null() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldObjectIdNotNull:");
        stringBuilder.append(realmGet$fieldObjectIdNotNull());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldObjectIdNull:");
        stringBuilder.append(realmGet$fieldObjectIdNull() != null ? realmGet$fieldObjectIdNull() : "null");
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
        stringBuilder.append(",");
        stringBuilder.append("{fieldDecimal128ListNotNull:");
        stringBuilder.append("RealmList<Decimal128>[").append(realmGet$fieldDecimal128ListNotNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldDecimal128ListNull:");
        stringBuilder.append("RealmList<Decimal128>[").append(realmGet$fieldDecimal128ListNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldObjectIdListNotNull:");
        stringBuilder.append("RealmList<ObjectId>[").append(realmGet$fieldObjectIdListNotNull().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{fieldObjectIdListNull:");
        stringBuilder.append("RealmList<ObjectId>[").append(realmGet$fieldObjectIdListNull().size()).append("]");
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
        long objKey = proxyState.getRow$realm().getObjectKey();

        int result = 17;
        result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0);
        result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0);
        result = 31 * result + (int) (objKey ^ (objKey >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        some_test_NullTypesRealmProxy aNullTypes = (some_test_NullTypesRealmProxy)o;

        BaseRealm realm = proxyState.getRealm$realm();
        BaseRealm otherRealm = aNullTypes.proxyState.getRealm$realm();
        String path = realm.getPath();
        String otherPath = otherRealm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;
        if (realm.isFrozen() != otherRealm.isFrozen()) return false;
        if (!realm.sharedRealm.getVersionID().equals(otherRealm.sharedRealm.getVersionID())) {
            return false;
        }

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aNullTypes.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getObjectKey() != aNullTypes.proxyState.getRow$realm().getObjectKey()) return false;

        return true;
    }
}
