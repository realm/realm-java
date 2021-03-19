package io.realm;


import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import io.realm.ImportFlag;
import io.realm.ProxyUtils;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnInfo;
import io.realm.internal.NativeContext;
import io.realm.internal.OsList;
import io.realm.internal.OsMap;
import io.realm.internal.OsObject;
import io.realm.internal.OsObjectSchemaInfo;
import io.realm.internal.OsSchemaInfo;
import io.realm.internal.OsSet;
import io.realm.internal.Property;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
import io.realm.internal.android.JsonUtils;
import io.realm.internal.core.NativeMixed;
import io.realm.internal.objectstore.OsObjectBuilder;
import io.realm.log.RealmLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("all")
public class some_test_AllTypesRealmProxy extends some.test.AllTypes
        implements RealmObjectProxy, some_test_AllTypesRealmProxyInterface {

    static final class AllTypesColumnInfo extends ColumnInfo {
        long columnStringColKey;
        long columnLongColKey;
        long columnFloatColKey;
        long columnDoubleColKey;
        long columnBooleanColKey;
        long columnDecimal128ColKey;
        long columnObjectIdColKey;
        long columnUUIDColKey;
        long columnDateColKey;
        long columnMixedColKey;
        long columnBinaryColKey;
        long columnMutableRealmIntegerColKey;
        long columnObjectColKey;
        long columnObjectWithoutPkColKey;
        long columnRealmListColKey;
        long columnRealmListNoPkColKey;
        long columnRealmFinalListColKey;
        long columnRealmFinalListNoPkColKey;
        long columnStringListColKey;
        long columnBinaryListColKey;
        long columnBooleanListColKey;
        long columnLongListColKey;
        long columnIntegerListColKey;
        long columnShortListColKey;
        long columnByteListColKey;
        long columnDoubleListColKey;
        long columnFloatListColKey;
        long columnDateListColKey;
        long columnDecimal128ListColKey;
        long columnObjectIdListColKey;
        long columnUUIDListColKey;
        long columnMixedListColKey;
        long columnRealmDictionaryColKey;
        long columnBooleanDictionaryColKey;
        long columnStringDictionaryColKey;
        long columnIntegerDictionaryColKey;
        long columnFloatDictionaryColKey;
        long columnLongDictionaryColKey;
        long columnShortDictionaryColKey;
        long columnDoubleDictionaryColKey;
        long columnByteDictionaryColKey;
        long columnBinaryDictionaryColKey;
        long columnDateDictionaryColKey;
        long columnObjectIdDictionaryColKey;
        long columnUUIDDictionaryColKey;
        long columnDecimal128DictionaryColKey;
        long columnMixedDictionaryColKey;

        AllTypesColumnInfo(OsSchemaInfo schemaInfo) {
            super(47);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("AllTypes");
            this.columnStringColKey = addColumnDetails("columnString", "columnString", objectSchemaInfo);
            this.columnLongColKey = addColumnDetails("columnLong", "columnLong", objectSchemaInfo);
            this.columnFloatColKey = addColumnDetails("columnFloat", "columnFloat", objectSchemaInfo);
            this.columnDoubleColKey = addColumnDetails("columnDouble", "columnDouble", objectSchemaInfo);
            this.columnBooleanColKey = addColumnDetails("columnBoolean", "columnBoolean", objectSchemaInfo);
            this.columnDecimal128ColKey = addColumnDetails("columnDecimal128", "columnDecimal128", objectSchemaInfo);
            this.columnObjectIdColKey = addColumnDetails("columnObjectId", "columnObjectId", objectSchemaInfo);
            this.columnUUIDColKey = addColumnDetails("columnUUID", "columnUUID", objectSchemaInfo);
            this.columnDateColKey = addColumnDetails("columnDate", "columnDate", objectSchemaInfo);
            this.columnMixedColKey = addColumnDetails("columnMixed", "columnMixed", objectSchemaInfo);
            this.columnBinaryColKey = addColumnDetails("columnBinary", "columnBinary", objectSchemaInfo);
            this.columnMutableRealmIntegerColKey = addColumnDetails("columnMutableRealmInteger", "columnMutableRealmInteger", objectSchemaInfo);
            this.columnObjectColKey = addColumnDetails("columnObject", "columnObject", objectSchemaInfo);
            this.columnObjectWithoutPkColKey = addColumnDetails("columnObjectWithoutPk", "columnObjectWithoutPk", objectSchemaInfo);
            this.columnRealmListColKey = addColumnDetails("columnRealmList", "columnRealmList", objectSchemaInfo);
            this.columnRealmListNoPkColKey = addColumnDetails("columnRealmListNoPk", "columnRealmListNoPk", objectSchemaInfo);
            this.columnRealmFinalListColKey = addColumnDetails("columnRealmFinalList", "columnRealmFinalList", objectSchemaInfo);
            this.columnRealmFinalListNoPkColKey = addColumnDetails("columnRealmFinalListNoPk", "columnRealmFinalListNoPk", objectSchemaInfo);
            this.columnStringListColKey = addColumnDetails("columnStringList", "columnStringList", objectSchemaInfo);
            this.columnBinaryListColKey = addColumnDetails("columnBinaryList", "columnBinaryList", objectSchemaInfo);
            this.columnBooleanListColKey = addColumnDetails("columnBooleanList", "columnBooleanList", objectSchemaInfo);
            this.columnLongListColKey = addColumnDetails("columnLongList", "columnLongList", objectSchemaInfo);
            this.columnIntegerListColKey = addColumnDetails("columnIntegerList", "columnIntegerList", objectSchemaInfo);
            this.columnShortListColKey = addColumnDetails("columnShortList", "columnShortList", objectSchemaInfo);
            this.columnByteListColKey = addColumnDetails("columnByteList", "columnByteList", objectSchemaInfo);
            this.columnDoubleListColKey = addColumnDetails("columnDoubleList", "columnDoubleList", objectSchemaInfo);
            this.columnFloatListColKey = addColumnDetails("columnFloatList", "columnFloatList", objectSchemaInfo);
            this.columnDateListColKey = addColumnDetails("columnDateList", "columnDateList", objectSchemaInfo);
            this.columnDecimal128ListColKey = addColumnDetails("columnDecimal128List", "columnDecimal128List", objectSchemaInfo);
            this.columnObjectIdListColKey = addColumnDetails("columnObjectIdList", "columnObjectIdList", objectSchemaInfo);
            this.columnUUIDListColKey = addColumnDetails("columnUUIDList", "columnUUIDList", objectSchemaInfo);
            this.columnMixedListColKey = addColumnDetails("columnMixedList", "columnMixedList", objectSchemaInfo);
            this.columnRealmDictionaryColKey = addColumnDetails("columnRealmDictionary", "columnRealmDictionary", objectSchemaInfo);
            this.columnBooleanDictionaryColKey = addColumnDetails("columnBooleanDictionary", "columnBooleanDictionary", objectSchemaInfo);
            this.columnStringDictionaryColKey = addColumnDetails("columnStringDictionary", "columnStringDictionary", objectSchemaInfo);
            this.columnIntegerDictionaryColKey = addColumnDetails("columnIntegerDictionary", "columnIntegerDictionary", objectSchemaInfo);
            this.columnFloatDictionaryColKey = addColumnDetails("columnFloatDictionary", "columnFloatDictionary", objectSchemaInfo);
            this.columnLongDictionaryColKey = addColumnDetails("columnLongDictionary", "columnLongDictionary", objectSchemaInfo);
            this.columnShortDictionaryColKey = addColumnDetails("columnShortDictionary", "columnShortDictionary", objectSchemaInfo);
            this.columnDoubleDictionaryColKey = addColumnDetails("columnDoubleDictionary", "columnDoubleDictionary", objectSchemaInfo);
            this.columnByteDictionaryColKey = addColumnDetails("columnByteDictionary", "columnByteDictionary", objectSchemaInfo);
            this.columnBinaryDictionaryColKey = addColumnDetails("columnBinaryDictionary", "columnBinaryDictionary", objectSchemaInfo);
            this.columnDateDictionaryColKey = addColumnDetails("columnDateDictionary", "columnDateDictionary", objectSchemaInfo);
            this.columnObjectIdDictionaryColKey = addColumnDetails("columnObjectIdDictionary", "columnObjectIdDictionary", objectSchemaInfo);
            this.columnUUIDDictionaryColKey = addColumnDetails("columnUUIDDictionary", "columnUUIDDictionary", objectSchemaInfo);
            this.columnDecimal128DictionaryColKey = addColumnDetails("columnDecimal128Dictionary", "columnDecimal128Dictionary", objectSchemaInfo);
            this.columnMixedDictionaryColKey = addColumnDetails("columnMixedDictionary", "columnMixedDictionary", objectSchemaInfo);
            addBacklinkDetails(schemaInfo, "parentObjects", "AllTypes", "columnObject");
        }

        AllTypesColumnInfo(ColumnInfo src, boolean mutable) {
            super(src, mutable);
            copy(src, this);
        }

        @Override
        protected final ColumnInfo copy(boolean mutable) {
            return new AllTypesColumnInfo(this, mutable);
        }

        @Override
        protected final void copy(ColumnInfo rawSrc, ColumnInfo rawDst) {
            final AllTypesColumnInfo src = (AllTypesColumnInfo) rawSrc;
            final AllTypesColumnInfo dst = (AllTypesColumnInfo) rawDst;
            dst.columnStringColKey = src.columnStringColKey;
            dst.columnLongColKey = src.columnLongColKey;
            dst.columnFloatColKey = src.columnFloatColKey;
            dst.columnDoubleColKey = src.columnDoubleColKey;
            dst.columnBooleanColKey = src.columnBooleanColKey;
            dst.columnDecimal128ColKey = src.columnDecimal128ColKey;
            dst.columnObjectIdColKey = src.columnObjectIdColKey;
            dst.columnUUIDColKey = src.columnUUIDColKey;
            dst.columnDateColKey = src.columnDateColKey;
            dst.columnMixedColKey = src.columnMixedColKey;
            dst.columnBinaryColKey = src.columnBinaryColKey;
            dst.columnMutableRealmIntegerColKey = src.columnMutableRealmIntegerColKey;
            dst.columnObjectColKey = src.columnObjectColKey;
            dst.columnObjectWithoutPkColKey = src.columnObjectWithoutPkColKey;
            dst.columnRealmListColKey = src.columnRealmListColKey;
            dst.columnRealmListNoPkColKey = src.columnRealmListNoPkColKey;
            dst.columnRealmFinalListColKey = src.columnRealmFinalListColKey;
            dst.columnRealmFinalListNoPkColKey = src.columnRealmFinalListNoPkColKey;
            dst.columnStringListColKey = src.columnStringListColKey;
            dst.columnBinaryListColKey = src.columnBinaryListColKey;
            dst.columnBooleanListColKey = src.columnBooleanListColKey;
            dst.columnLongListColKey = src.columnLongListColKey;
            dst.columnIntegerListColKey = src.columnIntegerListColKey;
            dst.columnShortListColKey = src.columnShortListColKey;
            dst.columnByteListColKey = src.columnByteListColKey;
            dst.columnDoubleListColKey = src.columnDoubleListColKey;
            dst.columnFloatListColKey = src.columnFloatListColKey;
            dst.columnDateListColKey = src.columnDateListColKey;
            dst.columnDecimal128ListColKey = src.columnDecimal128ListColKey;
            dst.columnObjectIdListColKey = src.columnObjectIdListColKey;
            dst.columnUUIDListColKey = src.columnUUIDListColKey;
            dst.columnMixedListColKey = src.columnMixedListColKey;
            dst.columnRealmDictionaryColKey = src.columnRealmDictionaryColKey;
            dst.columnBooleanDictionaryColKey = src.columnBooleanDictionaryColKey;
            dst.columnStringDictionaryColKey = src.columnStringDictionaryColKey;
            dst.columnIntegerDictionaryColKey = src.columnIntegerDictionaryColKey;
            dst.columnFloatDictionaryColKey = src.columnFloatDictionaryColKey;
            dst.columnLongDictionaryColKey = src.columnLongDictionaryColKey;
            dst.columnShortDictionaryColKey = src.columnShortDictionaryColKey;
            dst.columnDoubleDictionaryColKey = src.columnDoubleDictionaryColKey;
            dst.columnByteDictionaryColKey = src.columnByteDictionaryColKey;
            dst.columnBinaryDictionaryColKey = src.columnBinaryDictionaryColKey;
            dst.columnDateDictionaryColKey = src.columnDateDictionaryColKey;
            dst.columnObjectIdDictionaryColKey = src.columnObjectIdDictionaryColKey;
            dst.columnUUIDDictionaryColKey = src.columnUUIDDictionaryColKey;
            dst.columnDecimal128DictionaryColKey = src.columnDecimal128DictionaryColKey;
            dst.columnMixedDictionaryColKey = src.columnMixedDictionaryColKey;
        }
    }

    private static final String NO_ALIAS = "";
    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private AllTypesColumnInfo columnInfo;
    private ProxyState<some.test.AllTypes> proxyState;
    private final MutableRealmInteger.Managed columnMutableRealmIntegerMutableRealmInteger = new MutableRealmInteger.Managed<some.test.AllTypes>() {
        @Override protected ProxyState<some.test.AllTypes> getProxyState() { return proxyState; }
        @Override protected long getColumnIndex() { return columnInfo.columnMutableRealmIntegerColKey; }
    };
    private RealmList<some.test.AllTypes> columnRealmListRealmList;
    private RealmList<some.test.Simple> columnRealmListNoPkRealmList;
    private RealmList<some.test.AllTypes> columnRealmFinalListRealmList;
    private RealmList<some.test.Simple> columnRealmFinalListNoPkRealmList;
    private RealmList<String> columnStringListRealmList;
    private RealmList<byte[]> columnBinaryListRealmList;
    private RealmList<Boolean> columnBooleanListRealmList;
    private RealmList<Long> columnLongListRealmList;
    private RealmList<Integer> columnIntegerListRealmList;
    private RealmList<Short> columnShortListRealmList;
    private RealmList<Byte> columnByteListRealmList;
    private RealmList<Double> columnDoubleListRealmList;
    private RealmList<Float> columnFloatListRealmList;
    private RealmList<Date> columnDateListRealmList;
    private RealmList<org.bson.types.Decimal128> columnDecimal128ListRealmList;
    private RealmList<org.bson.types.ObjectId> columnObjectIdListRealmList;
    private RealmList<java.util.UUID> columnUUIDListRealmList;
    private RealmList<Mixed> columnMixedListRealmList;
    private RealmDictionary<some.test.AllTypes> columnRealmDictionaryRealmDictionary;
    private RealmDictionary<Boolean> columnBooleanDictionaryRealmDictionary;
    private RealmDictionary<String> columnStringDictionaryRealmDictionary;
    private RealmDictionary<Integer> columnIntegerDictionaryRealmDictionary;
    private RealmDictionary<Float> columnFloatDictionaryRealmDictionary;
    private RealmDictionary<Long> columnLongDictionaryRealmDictionary;
    private RealmDictionary<Short> columnShortDictionaryRealmDictionary;
    private RealmDictionary<Double> columnDoubleDictionaryRealmDictionary;
    private RealmDictionary<Byte> columnByteDictionaryRealmDictionary;
    private RealmDictionary<byte[]> columnBinaryDictionaryRealmDictionary;
    private RealmDictionary<Date> columnDateDictionaryRealmDictionary;
    private RealmDictionary<org.bson.types.ObjectId> columnObjectIdDictionaryRealmDictionary;
    private RealmDictionary<java.util.UUID> columnUUIDDictionaryRealmDictionary;
    private RealmDictionary<org.bson.types.Decimal128> columnDecimal128DictionaryRealmDictionary;
    private RealmDictionary<Mixed> columnMixedDictionaryRealmDictionary;
    private RealmResults<some.test.AllTypes> parentObjectsBacklinks;

    some_test_AllTypesRealmProxy() {
        proxyState.setConstructionFinished();
    }

    @Override
    public void realm$injectObjectContext() {
        if (this.proxyState != null) {
            return;
        }
        final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get();
        this.columnInfo = (AllTypesColumnInfo) context.getColumnInfo();
        this.proxyState = new ProxyState<some.test.AllTypes>(this);
        proxyState.setRealm$realm(context.getRealm());
        proxyState.setRow$realm(context.getRow());
        proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue());
        proxyState.setExcludeFields$realm(context.getExcludeFields());
    }

    @Override
    @SuppressWarnings("cast")
    public String realmGet$columnString() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.columnStringColKey);
    }

    @Override
    public void realmSet$columnString(String value) {
        if (proxyState.isUnderConstruction()) {
            // default value of the primary key is always ignored.
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        throw new io.realm.exceptions.RealmException("Primary key field 'columnString' cannot be changed after object was created.");
    }

    @Override
    @SuppressWarnings("cast")
    public long realmGet$columnLong() {
        proxyState.getRealm$realm().checkIfValid();
        return (long) proxyState.getRow$realm().getLong(columnInfo.columnLongColKey);
    }

    @Override
    public void realmSet$columnLong(long value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setLong(columnInfo.columnLongColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.columnLongColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public float realmGet$columnFloat() {
        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.columnFloatColKey);
    }

    @Override
    public void realmSet$columnFloat(float value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setFloat(columnInfo.columnFloatColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setFloat(columnInfo.columnFloatColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public double realmGet$columnDouble() {
        proxyState.getRealm$realm().checkIfValid();
        return (double) proxyState.getRow$realm().getDouble(columnInfo.columnDoubleColKey);
    }

    @Override
    public void realmSet$columnDouble(double value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setDouble(columnInfo.columnDoubleColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setDouble(columnInfo.columnDoubleColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public boolean realmGet$columnBoolean() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.columnBooleanColKey);
    }

    @Override
    public void realmSet$columnBoolean(boolean value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.columnBooleanColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.columnBooleanColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public org.bson.types.Decimal128 realmGet$columnDecimal128() {
        proxyState.getRealm$realm().checkIfValid();
        return (org.bson.types.Decimal128) proxyState.getRow$realm().getDecimal128(columnInfo.columnDecimal128ColKey);
    }

    @Override
    public void realmSet$columnDecimal128(org.bson.types.Decimal128 value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnDecimal128' to null.");
            }
            row.getTable().setDecimal128(columnInfo.columnDecimal128ColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'columnDecimal128' to null.");
        }
        proxyState.getRow$realm().setDecimal128(columnInfo.columnDecimal128ColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public org.bson.types.ObjectId realmGet$columnObjectId() {
        proxyState.getRealm$realm().checkIfValid();
        return (org.bson.types.ObjectId) proxyState.getRow$realm().getObjectId(columnInfo.columnObjectIdColKey);
    }

    @Override
    public void realmSet$columnObjectId(org.bson.types.ObjectId value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnObjectId' to null.");
            }
            row.getTable().setObjectId(columnInfo.columnObjectIdColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'columnObjectId' to null.");
        }
        proxyState.getRow$realm().setObjectId(columnInfo.columnObjectIdColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public java.util.UUID realmGet$columnUUID() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.util.UUID) proxyState.getRow$realm().getUUID(columnInfo.columnUUIDColKey);
    }

    @Override
    public void realmSet$columnUUID(java.util.UUID value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnUUID' to null.");
            }
            row.getTable().setUUID(columnInfo.columnUUIDColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'columnUUID' to null.");
        }
        proxyState.getRow$realm().setUUID(columnInfo.columnUUIDColKey, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Date realmGet$columnDate() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.util.Date) proxyState.getRow$realm().getDate(columnInfo.columnDateColKey);
    }

    @Override
    public void realmSet$columnDate(Date value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnDate' to null.");
            }
            row.getTable().setDate(columnInfo.columnDateColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'columnDate' to null.");
        }
        proxyState.getRow$realm().setDate(columnInfo.columnDateColKey, value);
    }

    @Override
    public Mixed realmGet$columnMixed() {
        proxyState.getRealm$realm().checkIfValid();
        NativeMixed nativeMixed = proxyState.getRow$realm().getNativeMixed(columnInfo.columnMixedColKey);
        return new Mixed(MixedOperator.fromNativeMixed(proxyState.getRealm$realm(), nativeMixed));
    }

    @Override
    public void realmSet$columnMixed(Mixed value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnMixed")) {
                return;
            }

            value = ProxyUtils.copyToRealmIfNeeded(proxyState, value);

            final Row row = proxyState.getRow$realm();
            if (value == null) {
                row.getTable().setNull(columnInfo.columnMixedColKey, row.getObjectKey(), true);
                return;
            }
            row.getTable().setMixed(columnInfo.columnMixedColKey, row.getObjectKey(), value.getNativePtr(), true);
            return;
        }


        proxyState.getRealm$realm().checkIfValid();

        if (value == null) {
            proxyState.getRow$realm().setNull(columnInfo.columnMixedColKey);
            return;
        }
        value = ProxyUtils.copyToRealmIfNeeded(proxyState, value);
        proxyState.getRow$realm().setMixed(columnInfo.columnMixedColKey, value.getNativePtr());
    }

    @Override
    @SuppressWarnings("cast")
    public byte[] realmGet$columnBinary() {
        proxyState.getRealm$realm().checkIfValid();
        return (byte[]) proxyState.getRow$realm().getBinaryByteArray(columnInfo.columnBinaryColKey);
    }

    @Override
    public void realmSet$columnBinary(byte[] value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnBinary' to null.");
            }
            row.getTable().setBinaryByteArray(columnInfo.columnBinaryColKey, row.getObjectKey(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'columnBinary' to null.");
        }
        proxyState.getRow$realm().setBinaryByteArray(columnInfo.columnBinaryColKey, value);
    }

    @Override
    public MutableRealmInteger realmGet$columnMutableRealmInteger() {
        proxyState.getRealm$realm().checkIfValid();
        return this.columnMutableRealmIntegerMutableRealmInteger;
    }

    @Override
    public some.test.AllTypes realmGet$columnObject() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNullLink(columnInfo.columnObjectColKey)) {
            return null;
        }
        return proxyState.getRealm$realm().get(some.test.AllTypes.class, proxyState.getRow$realm().getLink(columnInfo.columnObjectColKey), false, Collections.<String>emptyList());
    }

    @Override
    public void realmSet$columnObject(some.test.AllTypes value) {
        Realm realm = (Realm) proxyState.getRealm$realm();
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnObject")) {
                return;
            }
            if (value != null && !RealmObject.isManaged(value)) {
                value = realm.copyToRealmOrUpdate(value);
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                // Table#nullifyLink() does not support default value. Just using Row.
                row.nullifyLink(columnInfo.columnObjectColKey);
                return;
            }
            proxyState.checkValidObject(value);
            row.getTable().setLink(columnInfo.columnObjectColKey, row.getObjectKey(), ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getObjectKey(), true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().nullifyLink(columnInfo.columnObjectColKey);
            return;
        }
        proxyState.checkValidObject(value);
        proxyState.getRow$realm().setLink(columnInfo.columnObjectColKey, ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getObjectKey());
    }

    @Override
    public some.test.Simple realmGet$columnObjectWithoutPk() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNullLink(columnInfo.columnObjectWithoutPkColKey)) {
            return null;
        }
        return proxyState.getRealm$realm().get(some.test.Simple.class, proxyState.getRow$realm().getLink(columnInfo.columnObjectWithoutPkColKey), false, Collections.<String>emptyList());
    }

    @Override
    public void realmSet$columnObjectWithoutPk(some.test.Simple value) {
        Realm realm = (Realm) proxyState.getRealm$realm();
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnObjectWithoutPk")) {
                return;
            }
            if (value != null && !RealmObject.isManaged(value)) {
                value = realm.copyToRealm(value);
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                // Table#nullifyLink() does not support default value. Just using Row.
                row.nullifyLink(columnInfo.columnObjectWithoutPkColKey);
                return;
            }
            proxyState.checkValidObject(value);
            row.getTable().setLink(columnInfo.columnObjectWithoutPkColKey, row.getObjectKey(), ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getObjectKey(), true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().nullifyLink(columnInfo.columnObjectWithoutPkColKey);
            return;
        }
        proxyState.checkValidObject(value);
        proxyState.getRow$realm().setLink(columnInfo.columnObjectWithoutPkColKey, ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getObjectKey());
    }

    @Override
    public RealmList<some.test.AllTypes> realmGet$columnRealmList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnRealmListRealmList != null) {
            return columnRealmListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getModelList(columnInfo.columnRealmListColKey);
            columnRealmListRealmList = new RealmList<some.test.AllTypes>(some.test.AllTypes.class, osList, proxyState.getRealm$realm());
            return columnRealmListRealmList;
        }
    }

    @Override
    public void realmSet$columnRealmList(RealmList<some.test.AllTypes> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnRealmList")) {
                return;
            }
            // if the list contains unmanaged RealmObjects, convert them to managed.
            if (value != null && !value.isManaged()) {
                final Realm realm = (Realm) proxyState.getRealm$realm();
                final RealmList<some.test.AllTypes> original = value;
                value = new RealmList<some.test.AllTypes>();
                for (some.test.AllTypes item : original) {
                    if (item == null || RealmObject.isManaged(item)) {
                        value.add(item);
                    } else {
                        value.add(realm.copyToRealmOrUpdate(item));
                    }
                }
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getModelList(columnInfo.columnRealmListColKey);
        // For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.
        if (value != null && value.size() == osList.size()) {
            int objects = value.size();
            for (int i = 0; i < objects; i++) {
                some.test.AllTypes linkedObject = value.get(i);
                proxyState.checkValidObject(linkedObject);
                osList.setRow(i, ((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getObjectKey());
            }
        } else {
            osList.removeAll();
            if (value == null) {
                return;
            }
            int objects = value.size();
            for (int i = 0; i < objects; i++) {
                some.test.AllTypes linkedObject = value.get(i);
                proxyState.checkValidObject(linkedObject);
                osList.addRow(((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getObjectKey());
            }
        }
    }

    @Override
    public RealmList<some.test.Simple> realmGet$columnRealmListNoPk() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnRealmListNoPkRealmList != null) {
            return columnRealmListNoPkRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getModelList(columnInfo.columnRealmListNoPkColKey);
            columnRealmListNoPkRealmList = new RealmList<some.test.Simple>(some.test.Simple.class, osList, proxyState.getRealm$realm());
            return columnRealmListNoPkRealmList;
        }
    }

    @Override
    public void realmSet$columnRealmListNoPk(RealmList<some.test.Simple> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnRealmListNoPk")) {
                return;
            }
            // if the list contains unmanaged RealmObjects, convert them to managed.
            if (value != null && !value.isManaged()) {
                final Realm realm = (Realm) proxyState.getRealm$realm();
                final RealmList<some.test.Simple> original = value;
                value = new RealmList<some.test.Simple>();
                for (some.test.Simple item : original) {
                    if (item == null || RealmObject.isManaged(item)) {
                        value.add(item);
                    } else {
                        value.add(realm.copyToRealm(item));
                    }
                }
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getModelList(columnInfo.columnRealmListNoPkColKey);
        // For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.
        if (value != null && value.size() == osList.size()) {
            int objects = value.size();
            for (int i = 0; i < objects; i++) {
                some.test.Simple linkedObject = value.get(i);
                proxyState.checkValidObject(linkedObject);
                osList.setRow(i, ((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getObjectKey());
            }
        } else {
            osList.removeAll();
            if (value == null) {
                return;
            }
            int objects = value.size();
            for (int i = 0; i < objects; i++) {
                some.test.Simple linkedObject = value.get(i);
                proxyState.checkValidObject(linkedObject);
                osList.addRow(((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getObjectKey());
            }
        }
    }

    @Override
    public RealmList<some.test.AllTypes> realmGet$columnRealmFinalList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnRealmFinalListRealmList != null) {
            return columnRealmFinalListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getModelList(columnInfo.columnRealmFinalListColKey);
            columnRealmFinalListRealmList = new RealmList<some.test.AllTypes>(some.test.AllTypes.class, osList, proxyState.getRealm$realm());
            return columnRealmFinalListRealmList;
        }
    }

    @Override
    public void realmSet$columnRealmFinalList(RealmList<some.test.AllTypes> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnRealmFinalList")) {
                return;
            }
            // if the list contains unmanaged RealmObjects, convert them to managed.
            if (value != null && !value.isManaged()) {
                final Realm realm = (Realm) proxyState.getRealm$realm();
                final RealmList<some.test.AllTypes> original = value;
                value = new RealmList<some.test.AllTypes>();
                for (some.test.AllTypes item : original) {
                    if (item == null || RealmObject.isManaged(item)) {
                        value.add(item);
                    } else {
                        value.add(realm.copyToRealmOrUpdate(item));
                    }
                }
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getModelList(columnInfo.columnRealmFinalListColKey);
        // For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.
        if (value != null && value.size() == osList.size()) {
            int objects = value.size();
            for (int i = 0; i < objects; i++) {
                some.test.AllTypes linkedObject = value.get(i);
                proxyState.checkValidObject(linkedObject);
                osList.setRow(i, ((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getObjectKey());
            }
        } else {
            osList.removeAll();
            if (value == null) {
                return;
            }
            int objects = value.size();
            for (int i = 0; i < objects; i++) {
                some.test.AllTypes linkedObject = value.get(i);
                proxyState.checkValidObject(linkedObject);
                osList.addRow(((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getObjectKey());
            }
        }
    }

    @Override
    public RealmList<some.test.Simple> realmGet$columnRealmFinalListNoPk() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnRealmFinalListNoPkRealmList != null) {
            return columnRealmFinalListNoPkRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getModelList(columnInfo.columnRealmFinalListNoPkColKey);
            columnRealmFinalListNoPkRealmList = new RealmList<some.test.Simple>(some.test.Simple.class, osList, proxyState.getRealm$realm());
            return columnRealmFinalListNoPkRealmList;
        }
    }

    @Override
    public void realmSet$columnRealmFinalListNoPk(RealmList<some.test.Simple> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnRealmFinalListNoPk")) {
                return;
            }
            // if the list contains unmanaged RealmObjects, convert them to managed.
            if (value != null && !value.isManaged()) {
                final Realm realm = (Realm) proxyState.getRealm$realm();
                final RealmList<some.test.Simple> original = value;
                value = new RealmList<some.test.Simple>();
                for (some.test.Simple item : original) {
                    if (item == null || RealmObject.isManaged(item)) {
                        value.add(item);
                    } else {
                        value.add(realm.copyToRealm(item));
                    }
                }
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getModelList(columnInfo.columnRealmFinalListNoPkColKey);
        // For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.
        if (value != null && value.size() == osList.size()) {
            int objects = value.size();
            for (int i = 0; i < objects; i++) {
                some.test.Simple linkedObject = value.get(i);
                proxyState.checkValidObject(linkedObject);
                osList.setRow(i, ((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getObjectKey());
            }
        } else {
            osList.removeAll();
            if (value == null) {
                return;
            }
            int objects = value.size();
            for (int i = 0; i < objects; i++) {
                some.test.Simple linkedObject = value.get(i);
                proxyState.checkValidObject(linkedObject);
                osList.addRow(((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getObjectKey());
            }
        }
    }

    @Override
    public RealmList<String> realmGet$columnStringList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnStringListRealmList != null) {
            return columnStringListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnStringListColKey, RealmFieldType.STRING_LIST);
            columnStringListRealmList = new RealmList<java.lang.String>(java.lang.String.class, osList, proxyState.getRealm$realm());
            return columnStringListRealmList;
        }
    }

    @Override
    public void realmSet$columnStringList(RealmList<String> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnStringList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnStringListColKey, RealmFieldType.STRING_LIST);
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
    public RealmList<byte[]> realmGet$columnBinaryList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnBinaryListRealmList != null) {
            return columnBinaryListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnBinaryListColKey, RealmFieldType.BINARY_LIST);
            columnBinaryListRealmList = new RealmList<byte[]>(byte[].class, osList, proxyState.getRealm$realm());
            return columnBinaryListRealmList;
        }
    }

    @Override
    public void realmSet$columnBinaryList(RealmList<byte[]> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnBinaryList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnBinaryListColKey, RealmFieldType.BINARY_LIST);
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
    public RealmList<Boolean> realmGet$columnBooleanList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnBooleanListRealmList != null) {
            return columnBooleanListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnBooleanListColKey, RealmFieldType.BOOLEAN_LIST);
            columnBooleanListRealmList = new RealmList<java.lang.Boolean>(java.lang.Boolean.class, osList, proxyState.getRealm$realm());
            return columnBooleanListRealmList;
        }
    }

    @Override
    public void realmSet$columnBooleanList(RealmList<Boolean> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnBooleanList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnBooleanListColKey, RealmFieldType.BOOLEAN_LIST);
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
    public RealmList<Long> realmGet$columnLongList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnLongListRealmList != null) {
            return columnLongListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnLongListColKey, RealmFieldType.INTEGER_LIST);
            columnLongListRealmList = new RealmList<java.lang.Long>(java.lang.Long.class, osList, proxyState.getRealm$realm());
            return columnLongListRealmList;
        }
    }

    @Override
    public void realmSet$columnLongList(RealmList<Long> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnLongList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnLongListColKey, RealmFieldType.INTEGER_LIST);
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
    public RealmList<Integer> realmGet$columnIntegerList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnIntegerListRealmList != null) {
            return columnIntegerListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnIntegerListColKey, RealmFieldType.INTEGER_LIST);
            columnIntegerListRealmList = new RealmList<java.lang.Integer>(java.lang.Integer.class, osList, proxyState.getRealm$realm());
            return columnIntegerListRealmList;
        }
    }

    @Override
    public void realmSet$columnIntegerList(RealmList<Integer> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnIntegerList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnIntegerListColKey, RealmFieldType.INTEGER_LIST);
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
    public RealmList<Short> realmGet$columnShortList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnShortListRealmList != null) {
            return columnShortListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnShortListColKey, RealmFieldType.INTEGER_LIST);
            columnShortListRealmList = new RealmList<java.lang.Short>(java.lang.Short.class, osList, proxyState.getRealm$realm());
            return columnShortListRealmList;
        }
    }

    @Override
    public void realmSet$columnShortList(RealmList<Short> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnShortList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnShortListColKey, RealmFieldType.INTEGER_LIST);
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
    public RealmList<Byte> realmGet$columnByteList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnByteListRealmList != null) {
            return columnByteListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnByteListColKey, RealmFieldType.INTEGER_LIST);
            columnByteListRealmList = new RealmList<java.lang.Byte>(java.lang.Byte.class, osList, proxyState.getRealm$realm());
            return columnByteListRealmList;
        }
    }

    @Override
    public void realmSet$columnByteList(RealmList<Byte> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnByteList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnByteListColKey, RealmFieldType.INTEGER_LIST);
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
    public RealmList<Double> realmGet$columnDoubleList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnDoubleListRealmList != null) {
            return columnDoubleListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnDoubleListColKey, RealmFieldType.DOUBLE_LIST);
            columnDoubleListRealmList = new RealmList<java.lang.Double>(java.lang.Double.class, osList, proxyState.getRealm$realm());
            return columnDoubleListRealmList;
        }
    }

    @Override
    public void realmSet$columnDoubleList(RealmList<Double> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnDoubleList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnDoubleListColKey, RealmFieldType.DOUBLE_LIST);
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
    public RealmList<Float> realmGet$columnFloatList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnFloatListRealmList != null) {
            return columnFloatListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnFloatListColKey, RealmFieldType.FLOAT_LIST);
            columnFloatListRealmList = new RealmList<java.lang.Float>(java.lang.Float.class, osList, proxyState.getRealm$realm());
            return columnFloatListRealmList;
        }
    }

    @Override
    public void realmSet$columnFloatList(RealmList<Float> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnFloatList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnFloatListColKey, RealmFieldType.FLOAT_LIST);
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
    public RealmList<Date> realmGet$columnDateList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnDateListRealmList != null) {
            return columnDateListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnDateListColKey, RealmFieldType.DATE_LIST);
            columnDateListRealmList = new RealmList<java.util.Date>(java.util.Date.class, osList, proxyState.getRealm$realm());
            return columnDateListRealmList;
        }
    }

    @Override
    public void realmSet$columnDateList(RealmList<Date> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnDateList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnDateListColKey, RealmFieldType.DATE_LIST);
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
    public RealmList<org.bson.types.Decimal128> realmGet$columnDecimal128List() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnDecimal128ListRealmList != null) {
            return columnDecimal128ListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnDecimal128ListColKey, RealmFieldType.DECIMAL128_LIST);
            columnDecimal128ListRealmList = new RealmList<org.bson.types.Decimal128>(org.bson.types.Decimal128.class, osList, proxyState.getRealm$realm());
            return columnDecimal128ListRealmList;
        }
    }

    @Override
    public void realmSet$columnDecimal128List(RealmList<org.bson.types.Decimal128> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnDecimal128List")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnDecimal128ListColKey, RealmFieldType.DECIMAL128_LIST);
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
    public RealmList<org.bson.types.ObjectId> realmGet$columnObjectIdList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnObjectIdListRealmList != null) {
            return columnObjectIdListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnObjectIdListColKey, RealmFieldType.OBJECT_ID_LIST);
            columnObjectIdListRealmList = new RealmList<org.bson.types.ObjectId>(org.bson.types.ObjectId.class, osList, proxyState.getRealm$realm());
            return columnObjectIdListRealmList;
        }
    }

    @Override
    public void realmSet$columnObjectIdList(RealmList<org.bson.types.ObjectId> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnObjectIdList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnObjectIdListColKey, RealmFieldType.OBJECT_ID_LIST);
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

    @Override
    public RealmList<java.util.UUID> realmGet$columnUUIDList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnUUIDListRealmList != null) {
            return columnUUIDListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnUUIDListColKey, RealmFieldType.UUID_LIST);
            columnUUIDListRealmList = new RealmList<java.util.UUID>(java.util.UUID.class, osList, proxyState.getRealm$realm());
            return columnUUIDListRealmList;
        }
    }

    @Override
    public void realmSet$columnUUIDList(RealmList<java.util.UUID> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnUUIDList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnUUIDListColKey, RealmFieldType.UUID_LIST);
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (java.util.UUID item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addUUID(item);
            }
        }
    }

    @Override
    public RealmList<Mixed> realmGet$columnMixedList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnMixedListRealmList != null) {
            return columnMixedListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnMixedListColKey, RealmFieldType.MIXED_LIST);
            columnMixedListRealmList = new RealmList<io.realm.Mixed>(io.realm.Mixed.class, osList, proxyState.getRealm$realm());
            return columnMixedListRealmList;
        }
    }

    @Override
    public void realmSet$columnMixedList(RealmList<Mixed> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnMixedList")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnMixedListColKey, RealmFieldType.MIXED_LIST);
        if (value != null && !value.isManaged()) {
            final Realm realm = (Realm) proxyState.getRealm$realm();
            final RealmList<Mixed> original = value;
            value = new RealmList<Mixed>();
            for (int i = 0; i < original.size(); i++) {
                value.add(ProxyUtils.copyToRealmIfNeeded(proxyState, original.get(i)));
            }
        }
        osList.removeAll();
        if (value == null) {
            return;
        }
        for (io.realm.Mixed item : value) {
            if (item == null) {
                osList.addNull();
            } else {
                osList.addMixed(item.getNativePtr());
            }
        }
    }

    @Override
    public RealmDictionary<some.test.AllTypes> realmGet$columnRealmDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnRealmDictionaryRealmDictionary != null) {
            return columnRealmDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getModelMap(columnInfo.columnRealmDictionaryColKey);
            columnRealmDictionaryRealmDictionary = new RealmDictionary<some.test.AllTypes>(proxyState.getRealm$realm(), osMap, some.test.AllTypes.class);
            return columnRealmDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnRealmDictionary(RealmDictionary<some.test.AllTypes> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnRealmDictionary")) {
                return;
            }
            // if the dictionary contains unmanaged RealmModel instances, convert them to managed.
            if (value != null && !value.isManaged()) {
                final Realm realm = (Realm) proxyState.getRealm$realm();
                final RealmDictionary<some.test.AllTypes> original = value;
                value = new RealmDictionary<some.test.AllTypes>();
                for (java.util.Map.Entry<String, some.test.AllTypes> entry : original.entrySet()) {
                    String entryKey = entry.getKey();
                    some.test.AllTypes entryValue = entry.getValue();
                    if (entryValue == null || RealmObject.isManaged(entryValue)) {
                        value.put(entryKey, entryValue);
                    } else {
                        value.put(entryKey, realm.copyToRealmOrUpdate(entryValue));
                    }
                }
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getModelMap(columnInfo.columnRealmDictionaryColKey);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, some.test.AllTypes> item : value.entrySet()) {
            String entryKey = item.getKey();
            some.test.AllTypes entryValue = item.getValue();
            if (entryValue == null) {
                osMap.put(entryKey, null);
            } else {
                osMap.putRow(entryKey, ((RealmObjectProxy) entryValue).realmGet$proxyState().getRow$realm().getObjectKey());
            }
        }
    }

    @Override
    public RealmDictionary<Boolean> realmGet$columnBooleanDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnBooleanDictionaryRealmDictionary != null) {
            return columnBooleanDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnBooleanDictionaryColKey, RealmFieldType.STRING_TO_BOOLEAN_MAP);
            columnBooleanDictionaryRealmDictionary = new RealmDictionary<java.lang.Boolean>(proxyState.getRealm$realm(), osMap, java.lang.Boolean.class);
            return columnBooleanDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnBooleanDictionary(RealmDictionary<Boolean> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnBooleanDictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnBooleanDictionaryColKey, RealmFieldType.STRING_TO_BOOLEAN_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, java.lang.Boolean> item : value.entrySet()) {
            String entryKey = item.getKey();
            java.lang.Boolean entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<String> realmGet$columnStringDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnStringDictionaryRealmDictionary != null) {
            return columnStringDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnStringDictionaryColKey, RealmFieldType.STRING_TO_STRING_MAP);
            columnStringDictionaryRealmDictionary = new RealmDictionary<java.lang.String>(proxyState.getRealm$realm(), osMap, java.lang.String.class);
            return columnStringDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnStringDictionary(RealmDictionary<String> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnStringDictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnStringDictionaryColKey, RealmFieldType.STRING_TO_STRING_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, java.lang.String> item : value.entrySet()) {
            String entryKey = item.getKey();
            java.lang.String entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<Integer> realmGet$columnIntegerDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnIntegerDictionaryRealmDictionary != null) {
            return columnIntegerDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnIntegerDictionaryColKey, RealmFieldType.STRING_TO_INTEGER_MAP);
            columnIntegerDictionaryRealmDictionary = new RealmDictionary<java.lang.Integer>(proxyState.getRealm$realm(), osMap, java.lang.Integer.class);
            return columnIntegerDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnIntegerDictionary(RealmDictionary<Integer> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnIntegerDictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnIntegerDictionaryColKey, RealmFieldType.STRING_TO_INTEGER_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, java.lang.Integer> item : value.entrySet()) {
            String entryKey = item.getKey();
            java.lang.Integer entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<Float> realmGet$columnFloatDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnFloatDictionaryRealmDictionary != null) {
            return columnFloatDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnFloatDictionaryColKey, RealmFieldType.STRING_TO_FLOAT_MAP);
            columnFloatDictionaryRealmDictionary = new RealmDictionary<java.lang.Float>(proxyState.getRealm$realm(), osMap, java.lang.Float.class);
            return columnFloatDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnFloatDictionary(RealmDictionary<Float> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnFloatDictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnFloatDictionaryColKey, RealmFieldType.STRING_TO_FLOAT_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, java.lang.Float> item : value.entrySet()) {
            String entryKey = item.getKey();
            java.lang.Float entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<Long> realmGet$columnLongDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnLongDictionaryRealmDictionary != null) {
            return columnLongDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnLongDictionaryColKey, RealmFieldType.STRING_TO_INTEGER_MAP);
            columnLongDictionaryRealmDictionary = new RealmDictionary<java.lang.Long>(proxyState.getRealm$realm(), osMap, java.lang.Long.class);
            return columnLongDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnLongDictionary(RealmDictionary<Long> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnLongDictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnLongDictionaryColKey, RealmFieldType.STRING_TO_INTEGER_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, java.lang.Long> item : value.entrySet()) {
            String entryKey = item.getKey();
            java.lang.Long entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<Short> realmGet$columnShortDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnShortDictionaryRealmDictionary != null) {
            return columnShortDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnShortDictionaryColKey, RealmFieldType.STRING_TO_INTEGER_MAP);
            columnShortDictionaryRealmDictionary = new RealmDictionary<java.lang.Short>(proxyState.getRealm$realm(), osMap, java.lang.Short.class);
            return columnShortDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnShortDictionary(RealmDictionary<Short> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnShortDictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnShortDictionaryColKey, RealmFieldType.STRING_TO_INTEGER_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, java.lang.Short> item : value.entrySet()) {
            String entryKey = item.getKey();
            java.lang.Short entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<Double> realmGet$columnDoubleDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnDoubleDictionaryRealmDictionary != null) {
            return columnDoubleDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnDoubleDictionaryColKey, RealmFieldType.STRING_TO_DOUBLE_MAP);
            columnDoubleDictionaryRealmDictionary = new RealmDictionary<java.lang.Double>(proxyState.getRealm$realm(), osMap, java.lang.Double.class);
            return columnDoubleDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnDoubleDictionary(RealmDictionary<Double> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnDoubleDictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnDoubleDictionaryColKey, RealmFieldType.STRING_TO_DOUBLE_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, java.lang.Double> item : value.entrySet()) {
            String entryKey = item.getKey();
            java.lang.Double entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<Byte> realmGet$columnByteDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnByteDictionaryRealmDictionary != null) {
            return columnByteDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnByteDictionaryColKey, RealmFieldType.STRING_TO_INTEGER_MAP);
            columnByteDictionaryRealmDictionary = new RealmDictionary<java.lang.Byte>(proxyState.getRealm$realm(), osMap, java.lang.Byte.class);
            return columnByteDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnByteDictionary(RealmDictionary<Byte> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnByteDictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnByteDictionaryColKey, RealmFieldType.STRING_TO_INTEGER_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, java.lang.Byte> item : value.entrySet()) {
            String entryKey = item.getKey();
            java.lang.Byte entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<byte[]> realmGet$columnBinaryDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnBinaryDictionaryRealmDictionary != null) {
            return columnBinaryDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnBinaryDictionaryColKey, RealmFieldType.STRING_TO_BINARY_MAP);
            columnBinaryDictionaryRealmDictionary = new RealmDictionary<byte[]>(proxyState.getRealm$realm(), osMap, byte[].class);
            return columnBinaryDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnBinaryDictionary(RealmDictionary<byte[]> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnBinaryDictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnBinaryDictionaryColKey, RealmFieldType.STRING_TO_BINARY_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, byte[]> item : value.entrySet()) {
            String entryKey = item.getKey();
            byte[] entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<Date> realmGet$columnDateDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnDateDictionaryRealmDictionary != null) {
            return columnDateDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnDateDictionaryColKey, RealmFieldType.STRING_TO_DATE_MAP);
            columnDateDictionaryRealmDictionary = new RealmDictionary<java.util.Date>(proxyState.getRealm$realm(), osMap, java.util.Date.class);
            return columnDateDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnDateDictionary(RealmDictionary<Date> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnDateDictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnDateDictionaryColKey, RealmFieldType.STRING_TO_DATE_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, java.util.Date> item : value.entrySet()) {
            String entryKey = item.getKey();
            java.util.Date entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<org.bson.types.ObjectId> realmGet$columnObjectIdDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnObjectIdDictionaryRealmDictionary != null) {
            return columnObjectIdDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnObjectIdDictionaryColKey, RealmFieldType.STRING_TO_OBJECT_ID_MAP);
            columnObjectIdDictionaryRealmDictionary = new RealmDictionary<org.bson.types.ObjectId>(proxyState.getRealm$realm(), osMap, org.bson.types.ObjectId.class);
            return columnObjectIdDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnObjectIdDictionary(RealmDictionary<org.bson.types.ObjectId> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnObjectIdDictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnObjectIdDictionaryColKey, RealmFieldType.STRING_TO_OBJECT_ID_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, org.bson.types.ObjectId> item : value.entrySet()) {
            String entryKey = item.getKey();
            org.bson.types.ObjectId entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<java.util.UUID> realmGet$columnUUIDDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnUUIDDictionaryRealmDictionary != null) {
            return columnUUIDDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnUUIDDictionaryColKey, RealmFieldType.STRING_TO_UUID_MAP);
            columnUUIDDictionaryRealmDictionary = new RealmDictionary<java.util.UUID>(proxyState.getRealm$realm(), osMap, java.util.UUID.class);
            return columnUUIDDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnUUIDDictionary(RealmDictionary<java.util.UUID> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnUUIDDictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnUUIDDictionaryColKey, RealmFieldType.STRING_TO_UUID_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, java.util.UUID> item : value.entrySet()) {
            String entryKey = item.getKey();
            java.util.UUID entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<org.bson.types.Decimal128> realmGet$columnDecimal128Dictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnDecimal128DictionaryRealmDictionary != null) {
            return columnDecimal128DictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnDecimal128DictionaryColKey, RealmFieldType.STRING_TO_DECIMAL128_MAP);
            columnDecimal128DictionaryRealmDictionary = new RealmDictionary<org.bson.types.Decimal128>(proxyState.getRealm$realm(), osMap, org.bson.types.Decimal128.class);
            return columnDecimal128DictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnDecimal128Dictionary(RealmDictionary<org.bson.types.Decimal128> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnDecimal128Dictionary")) {
                return;
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getValueMap(columnInfo.columnDecimal128DictionaryColKey, RealmFieldType.STRING_TO_DECIMAL128_MAP);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, org.bson.types.Decimal128> item : value.entrySet()) {
            String entryKey = item.getKey();
            org.bson.types.Decimal128 entryValue = item.getValue();
            osMap.put(entryKey, entryValue);
        }
    }

    @Override
    public RealmDictionary<Mixed> realmGet$columnMixedDictionary() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnMixedDictionaryRealmDictionary != null) {
            return columnMixedDictionaryRealmDictionary;
        } else {
            OsMap osMap = proxyState.getRow$realm().getMixedMap(columnInfo.columnMixedDictionaryColKey);
            columnMixedDictionaryRealmDictionary = new RealmDictionary<io.realm.Mixed>(proxyState.getRealm$realm(), osMap, io.realm.Mixed.class);
            return columnMixedDictionaryRealmDictionary;
        }
    }

    @Override
    public void realmSet$columnMixedDictionary(RealmDictionary<Mixed> value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnMixedDictionary")) {
                return;
            }
            // if the dictionary contains unmanaged RealmModel instances boxed in Mixed objects, convert them to managed.
            if (value != null && !value.isManaged()) {
                final Realm realm = (Realm) proxyState.getRealm$realm();
                final RealmDictionary<io.realm.Mixed> original = value;
                value = new RealmDictionary<io.realm.Mixed>();
                for (java.util.Map.Entry<String, io.realm.Mixed> item : original.entrySet()) {
                    String entryKey = item.getKey();
                    io.realm.Mixed entryValue = item.getValue();
                    // ensure (potential) RealmModel instances are copied to Realm if generic type is Mixed
                    if (entryValue == null) {
                        value.put(entryKey, null);
                    } else if (entryValue.getType() == MixedType.OBJECT) {
                        RealmModel realmModel = entryValue.asRealmModel(RealmModel.class);
                        RealmModel modelFromRealm = realm.copyToRealmOrUpdate(realmModel);
                        value.put(entryKey, Mixed.valueOf(modelFromRealm));
                    } else {
                        value.put(entryKey, entryValue);
                    }
                }
            }
        }

        proxyState.getRealm$realm().checkIfValid();
        OsMap osMap = proxyState.getRow$realm().getMixedMap(columnInfo.columnMixedDictionaryColKey);
        if (value == null) {
            return;
        }
        osMap.clear();
        for (java.util.Map.Entry<String, io.realm.Mixed> item : value.entrySet()) {
            String entryKey = item.getKey();
            io.realm.Mixed entryValue = item.getValue();
            if (entryValue == null) {
                osMap.put(entryKey, null);
            } else {
                osMap.putMixed(entryKey, entryValue.getNativePtr());
            }
        }
    }

    @Override
    public RealmResults<some.test.AllTypes> realmGet$parentObjects() {
        BaseRealm realm = proxyState.getRealm$realm();
        realm.checkIfValid();
        proxyState.getRow$realm().checkIfAttached();
        if (parentObjectsBacklinks == null) {
            parentObjectsBacklinks = RealmResults.createBacklinkResults(realm, proxyState.getRow$realm(), some.test.AllTypes.class, "columnObject");
        }
        return parentObjectsBacklinks;
    }

    private static OsObjectSchemaInfo createExpectedObjectSchemaInfo() {
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder(NO_ALIAS, "AllTypes", false, 47, 1);
        builder.addPersistedProperty(NO_ALIAS, "columnString", RealmFieldType.STRING, Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "columnLong", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "columnFloat", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "columnDouble", RealmFieldType.DOUBLE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "columnBoolean", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "columnDecimal128", RealmFieldType.DECIMAL128, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "columnObjectId", RealmFieldType.OBJECT_ID, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "columnUUID", RealmFieldType.UUID, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "columnDate", RealmFieldType.DATE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "columnMixed", RealmFieldType.MIXED, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "columnBinary", RealmFieldType.BINARY, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty(NO_ALIAS, "columnMutableRealmInteger", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedLinkProperty(NO_ALIAS, "columnObject", RealmFieldType.OBJECT, "AllTypes");
        builder.addPersistedLinkProperty(NO_ALIAS, "columnObjectWithoutPk", RealmFieldType.OBJECT, "Simple");
        builder.addPersistedLinkProperty(NO_ALIAS, "columnRealmList", RealmFieldType.LIST, "AllTypes");
        builder.addPersistedLinkProperty(NO_ALIAS, "columnRealmListNoPk", RealmFieldType.LIST, "Simple");
        builder.addPersistedLinkProperty(NO_ALIAS, "columnRealmFinalList", RealmFieldType.LIST, "AllTypes");
        builder.addPersistedLinkProperty(NO_ALIAS, "columnRealmFinalListNoPk", RealmFieldType.LIST, "Simple");
        builder.addPersistedValueListProperty(NO_ALIAS, "columnStringList", RealmFieldType.STRING_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnBinaryList", RealmFieldType.BINARY_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnBooleanList", RealmFieldType.BOOLEAN_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnLongList", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnIntegerList", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnShortList", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnByteList", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnDoubleList", RealmFieldType.DOUBLE_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnFloatList", RealmFieldType.FLOAT_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnDateList", RealmFieldType.DATE_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnDecimal128List", RealmFieldType.DECIMAL128_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnObjectIdList", RealmFieldType.OBJECT_ID_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnUUIDList", RealmFieldType.UUID_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty(NO_ALIAS, "columnMixedList", RealmFieldType.MIXED_LIST, !Property.REQUIRED);
        builder.addPersistedLinkProperty(NO_ALIAS, "columnRealmDictionary", RealmFieldType.STRING_TO_LINK_MAP, "AllTypes");
        builder.addPersistedMapProperty(NO_ALIAS, "columnBooleanDictionary", RealmFieldType.STRING_TO_BOOLEAN_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnStringDictionary", RealmFieldType.STRING_TO_STRING_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnIntegerDictionary", RealmFieldType.STRING_TO_INTEGER_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnFloatDictionary", RealmFieldType.STRING_TO_FLOAT_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnLongDictionary", RealmFieldType.STRING_TO_INTEGER_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnShortDictionary", RealmFieldType.STRING_TO_INTEGER_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnDoubleDictionary", RealmFieldType.STRING_TO_DOUBLE_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnByteDictionary", RealmFieldType.STRING_TO_INTEGER_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnBinaryDictionary", RealmFieldType.STRING_TO_BINARY_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnDateDictionary", RealmFieldType.STRING_TO_DATE_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnObjectIdDictionary", RealmFieldType.STRING_TO_OBJECT_ID_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnUUIDDictionary", RealmFieldType.STRING_TO_UUID_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnDecimal128Dictionary", RealmFieldType.STRING_TO_DECIMAL128_MAP, !Property.REQUIRED);
        builder.addPersistedMapProperty(NO_ALIAS, "columnMixedDictionary", RealmFieldType.STRING_TO_MIXED_MAP, !Property.REQUIRED);
        builder.addComputedLinkProperty("parentObjects", "AllTypes", "columnObject");
        return builder.build();
    }

    public static OsObjectSchemaInfo getExpectedObjectSchemaInfo() {
        return expectedObjectSchemaInfo;
    }

    public static AllTypesColumnInfo createColumnInfo(OsSchemaInfo schemaInfo) {
        return new AllTypesColumnInfo(schemaInfo);
    }

    public static String getSimpleClassName() {
        return "AllTypes";
    }

    public static final class ClassNameHelper {
        public static final String INTERNAL_CLASS_NAME = "AllTypes";
    }

    @SuppressWarnings("cast")
    public static some.test.AllTypes createOrUpdateUsingJsonObject(Realm realm, JSONObject json, boolean update)
            throws JSONException {
        throw new UnsupportedOperationException("Creation of RealmModels from JSON containing RealmDictionary properties is not supported yet.");
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.AllTypes createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        throw new UnsupportedOperationException("Creation of RealmModels from JSON containing RealmDictionary properties is not supported yet.");
    }

    static some_test_AllTypesRealmProxy newProxyInstance(BaseRealm realm, Row row) {
        // Ignore default values to avoid creating unexpected objects from RealmModel/RealmList fields
        final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
        objectContext.set(realm, row, realm.getSchema().getColumnInfo(some.test.AllTypes.class), false, Collections.<String>emptyList());
        io.realm.some_test_AllTypesRealmProxy obj = new io.realm.some_test_AllTypesRealmProxy();
        objectContext.clear();
        return obj;
    }

    public static some.test.AllTypes copyOrUpdate(Realm realm, AllTypesColumnInfo columnInfo, some.test.AllTypes object, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
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
            return (some.test.AllTypes) cachedRealmObject;
        }

        some.test.AllTypes realmObject = null;
        boolean canUpdate = update;
        if (canUpdate) {
            Table table = realm.getTable(some.test.AllTypes.class);
            long pkColumnKey = columnInfo.columnStringColKey;
            String value = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnString();
            long objKey = Table.NO_MATCH;
            if (value == null) {
                objKey = table.findFirstNull(pkColumnKey);
            } else {
                objKey = table.findFirstString(pkColumnKey, value);
            }
            if (objKey == Table.NO_MATCH) {
                canUpdate = false;
            } else {
                try {
                    objectContext.set(realm, table.getUncheckedRow(objKey), columnInfo, false, Collections.<String> emptyList());
                    realmObject = new io.realm.some_test_AllTypesRealmProxy();
                    cache.put(object, (RealmObjectProxy) realmObject);
                } finally {
                    objectContext.clear();
                }
            }
        }

        return (canUpdate) ? update(realm, columnInfo, realmObject, object, cache, flags) : copy(realm, columnInfo, object, update, cache, flags);
    }

    public static some.test.AllTypes copy(Realm realm, AllTypesColumnInfo columnInfo, some.test.AllTypes newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache, Set<ImportFlag> flags) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.AllTypes) cachedRealmObject;
        }

        some_test_AllTypesRealmProxyInterface unmanagedSource = (some_test_AllTypesRealmProxyInterface) newObject;

        Table table = realm.getTable(some.test.AllTypes.class);
        OsObjectBuilder builder = new OsObjectBuilder(table, flags);

        // Add all non-"object reference" fields
        builder.addString(columnInfo.columnStringColKey, unmanagedSource.realmGet$columnString());
        builder.addInteger(columnInfo.columnLongColKey, unmanagedSource.realmGet$columnLong());
        builder.addFloat(columnInfo.columnFloatColKey, unmanagedSource.realmGet$columnFloat());
        builder.addDouble(columnInfo.columnDoubleColKey, unmanagedSource.realmGet$columnDouble());
        builder.addBoolean(columnInfo.columnBooleanColKey, unmanagedSource.realmGet$columnBoolean());
        builder.addDecimal128(columnInfo.columnDecimal128ColKey, unmanagedSource.realmGet$columnDecimal128());
        builder.addObjectId(columnInfo.columnObjectIdColKey, unmanagedSource.realmGet$columnObjectId());
        builder.addUUID(columnInfo.columnUUIDColKey, unmanagedSource.realmGet$columnUUID());
        builder.addDate(columnInfo.columnDateColKey, unmanagedSource.realmGet$columnDate());
        builder.addByteArray(columnInfo.columnBinaryColKey, unmanagedSource.realmGet$columnBinary());
        builder.addMutableRealmInteger(columnInfo.columnMutableRealmIntegerColKey, unmanagedSource.realmGet$columnMutableRealmInteger());
        builder.addStringList(columnInfo.columnStringListColKey, unmanagedSource.realmGet$columnStringList());
        builder.addByteArrayList(columnInfo.columnBinaryListColKey, unmanagedSource.realmGet$columnBinaryList());
        builder.addBooleanList(columnInfo.columnBooleanListColKey, unmanagedSource.realmGet$columnBooleanList());
        builder.addLongList(columnInfo.columnLongListColKey, unmanagedSource.realmGet$columnLongList());
        builder.addIntegerList(columnInfo.columnIntegerListColKey, unmanagedSource.realmGet$columnIntegerList());
        builder.addShortList(columnInfo.columnShortListColKey, unmanagedSource.realmGet$columnShortList());
        builder.addByteList(columnInfo.columnByteListColKey, unmanagedSource.realmGet$columnByteList());
        builder.addDoubleList(columnInfo.columnDoubleListColKey, unmanagedSource.realmGet$columnDoubleList());
        builder.addFloatList(columnInfo.columnFloatListColKey, unmanagedSource.realmGet$columnFloatList());
        builder.addDateList(columnInfo.columnDateListColKey, unmanagedSource.realmGet$columnDateList());
        builder.addDecimal128List(columnInfo.columnDecimal128ListColKey, unmanagedSource.realmGet$columnDecimal128List());
        builder.addObjectIdList(columnInfo.columnObjectIdListColKey, unmanagedSource.realmGet$columnObjectIdList());
        builder.addUUIDList(columnInfo.columnUUIDListColKey, unmanagedSource.realmGet$columnUUIDList());
        builder.addBooleanValueDictionary(columnInfo.columnBooleanDictionaryColKey, unmanagedSource.realmGet$columnBooleanDictionary());
        builder.addStringValueDictionary(columnInfo.columnStringDictionaryColKey, unmanagedSource.realmGet$columnStringDictionary());
        builder.addIntegerValueDictionary(columnInfo.columnIntegerDictionaryColKey, unmanagedSource.realmGet$columnIntegerDictionary());
        builder.addFloatValueDictionary(columnInfo.columnFloatDictionaryColKey, unmanagedSource.realmGet$columnFloatDictionary());
        builder.addLongValueDictionary(columnInfo.columnLongDictionaryColKey, unmanagedSource.realmGet$columnLongDictionary());
        builder.addShortValueDictionary(columnInfo.columnShortDictionaryColKey, unmanagedSource.realmGet$columnShortDictionary());
        builder.addDoubleValueDictionary(columnInfo.columnDoubleDictionaryColKey, unmanagedSource.realmGet$columnDoubleDictionary());
        builder.addByteValueDictionary(columnInfo.columnByteDictionaryColKey, unmanagedSource.realmGet$columnByteDictionary());
        builder.addBinaryValueDictionary(columnInfo.columnBinaryDictionaryColKey, unmanagedSource.realmGet$columnBinaryDictionary());
        builder.addDateValueDictionary(columnInfo.columnDateDictionaryColKey, unmanagedSource.realmGet$columnDateDictionary());
        builder.addObjectIdValueDictionary(columnInfo.columnObjectIdDictionaryColKey, unmanagedSource.realmGet$columnObjectIdDictionary());
        builder.addUUIDValueDictionary(columnInfo.columnUUIDDictionaryColKey, unmanagedSource.realmGet$columnUUIDDictionary());
        builder.addDecimal128ValueDictionary(columnInfo.columnDecimal128DictionaryColKey, unmanagedSource.realmGet$columnDecimal128Dictionary());

        // Create the underlying object and cache it before setting any object/objectlist references
        // This will allow us to break any circular dependencies by using the object cache.
        Row row = builder.createNewObject();
        io.realm.some_test_AllTypesRealmProxy managedCopy = newProxyInstance(realm, row);
        cache.put(newObject, managedCopy);

        // Finally add all fields that reference other Realm Objects, either directly or through a list
        Mixed columnMixedMixed = unmanagedSource.realmGet$columnMixed();
        columnMixedMixed = ProxyUtils.copyOrUpdate(columnMixedMixed, realm, update, cache, flags);
        managedCopy.realmSet$columnMixed(columnMixedMixed);

        some.test.AllTypes columnObjectObj = unmanagedSource.realmGet$columnObject();
        if (columnObjectObj == null) {
            managedCopy.realmSet$columnObject(null);
        } else {
            some.test.AllTypes cachecolumnObject = (some.test.AllTypes) cache.get(columnObjectObj);
            if (cachecolumnObject != null) {
                managedCopy.realmSet$columnObject(cachecolumnObject);
            } else {
                managedCopy.realmSet$columnObject(some_test_AllTypesRealmProxy.copyOrUpdate(realm, (some_test_AllTypesRealmProxy.AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class), columnObjectObj, update, cache, flags));
            }
        }

        some.test.Simple columnObjectWithoutPkObj = unmanagedSource.realmGet$columnObjectWithoutPk();
        if (columnObjectWithoutPkObj == null) {
            managedCopy.realmSet$columnObjectWithoutPk(null);
        } else {
            some.test.Simple cachecolumnObjectWithoutPk = (some.test.Simple) cache.get(columnObjectWithoutPkObj);
            if (cachecolumnObjectWithoutPk != null) {
                managedCopy.realmSet$columnObjectWithoutPk(cachecolumnObjectWithoutPk);
            } else {
                managedCopy.realmSet$columnObjectWithoutPk(some_test_SimpleRealmProxy.copyOrUpdate(realm, (some_test_SimpleRealmProxy.SimpleColumnInfo) realm.getSchema().getColumnInfo(some.test.Simple.class), columnObjectWithoutPkObj, update, cache, flags));
            }
        }

        RealmList<some.test.AllTypes> columnRealmListUnmanagedList = unmanagedSource.realmGet$columnRealmList();
        if (columnRealmListUnmanagedList != null) {
            RealmList<some.test.AllTypes> columnRealmListManagedList = managedCopy.realmGet$columnRealmList();
            columnRealmListManagedList.clear();
            for (int i = 0; i < columnRealmListUnmanagedList.size(); i++) {
                some.test.AllTypes columnRealmListUnmanagedItem = columnRealmListUnmanagedList.get(i);
                some.test.AllTypes cachecolumnRealmList = (some.test.AllTypes) cache.get(columnRealmListUnmanagedItem);
                if (cachecolumnRealmList != null) {
                    columnRealmListManagedList.add(cachecolumnRealmList);
                } else {
                    columnRealmListManagedList.add(some_test_AllTypesRealmProxy.copyOrUpdate(realm, (some_test_AllTypesRealmProxy.AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class), columnRealmListUnmanagedItem, update, cache, flags));
                }
            }
        }

        RealmList<some.test.Simple> columnRealmListNoPkUnmanagedList = unmanagedSource.realmGet$columnRealmListNoPk();
        if (columnRealmListNoPkUnmanagedList != null) {
            RealmList<some.test.Simple> columnRealmListNoPkManagedList = managedCopy.realmGet$columnRealmListNoPk();
            columnRealmListNoPkManagedList.clear();
            for (int i = 0; i < columnRealmListNoPkUnmanagedList.size(); i++) {
                some.test.Simple columnRealmListNoPkUnmanagedItem = columnRealmListNoPkUnmanagedList.get(i);
                some.test.Simple cachecolumnRealmListNoPk = (some.test.Simple) cache.get(columnRealmListNoPkUnmanagedItem);
                if (cachecolumnRealmListNoPk != null) {
                    columnRealmListNoPkManagedList.add(cachecolumnRealmListNoPk);
                } else {
                    columnRealmListNoPkManagedList.add(some_test_SimpleRealmProxy.copyOrUpdate(realm, (some_test_SimpleRealmProxy.SimpleColumnInfo) realm.getSchema().getColumnInfo(some.test.Simple.class), columnRealmListNoPkUnmanagedItem, update, cache, flags));
                }
            }
        }

        RealmList<some.test.AllTypes> columnRealmFinalListUnmanagedList = unmanagedSource.realmGet$columnRealmFinalList();
        if (columnRealmFinalListUnmanagedList != null) {
            RealmList<some.test.AllTypes> columnRealmFinalListManagedList = managedCopy.realmGet$columnRealmFinalList();
            columnRealmFinalListManagedList.clear();
            for (int i = 0; i < columnRealmFinalListUnmanagedList.size(); i++) {
                some.test.AllTypes columnRealmFinalListUnmanagedItem = columnRealmFinalListUnmanagedList.get(i);
                some.test.AllTypes cachecolumnRealmFinalList = (some.test.AllTypes) cache.get(columnRealmFinalListUnmanagedItem);
                if (cachecolumnRealmFinalList != null) {
                    columnRealmFinalListManagedList.add(cachecolumnRealmFinalList);
                } else {
                    columnRealmFinalListManagedList.add(some_test_AllTypesRealmProxy.copyOrUpdate(realm, (some_test_AllTypesRealmProxy.AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class), columnRealmFinalListUnmanagedItem, update, cache, flags));
                }
            }
        }

        RealmList<some.test.Simple> columnRealmFinalListNoPkUnmanagedList = unmanagedSource.realmGet$columnRealmFinalListNoPk();
        if (columnRealmFinalListNoPkUnmanagedList != null) {
            RealmList<some.test.Simple> columnRealmFinalListNoPkManagedList = managedCopy.realmGet$columnRealmFinalListNoPk();
            columnRealmFinalListNoPkManagedList.clear();
            for (int i = 0; i < columnRealmFinalListNoPkUnmanagedList.size(); i++) {
                some.test.Simple columnRealmFinalListNoPkUnmanagedItem = columnRealmFinalListNoPkUnmanagedList.get(i);
                some.test.Simple cachecolumnRealmFinalListNoPk = (some.test.Simple) cache.get(columnRealmFinalListNoPkUnmanagedItem);
                if (cachecolumnRealmFinalListNoPk != null) {
                    columnRealmFinalListNoPkManagedList.add(cachecolumnRealmFinalListNoPk);
                } else {
                    columnRealmFinalListNoPkManagedList.add(some_test_SimpleRealmProxy.copyOrUpdate(realm, (some_test_SimpleRealmProxy.SimpleColumnInfo) realm.getSchema().getColumnInfo(some.test.Simple.class), columnRealmFinalListNoPkUnmanagedItem, update, cache, flags));
                }
            }
        }

        RealmList<Mixed> columnMixedListUnmanagedList = unmanagedSource.realmGet$columnMixedList();
        if (columnMixedListUnmanagedList != null) {
            RealmList<Mixed> columnMixedListManagedList = managedCopy.realmGet$columnMixedList();
            columnMixedListManagedList.clear();
            for (int i = 0; i < columnMixedListUnmanagedList.size(); i++) {
                Mixed mixedItem = columnMixedListUnmanagedList.get(i);
                mixedItem = ProxyUtils.copyOrUpdate(mixedItem, realm, update, cache, flags);
                columnMixedListManagedList.add(mixedItem);
            }
        }

        RealmDictionary<some.test.AllTypes> columnRealmDictionaryUnmanagedDictionary = unmanagedSource.realmGet$columnRealmDictionary();
        if (columnRealmDictionaryUnmanagedDictionary != null) {
            RealmDictionary<some.test.AllTypes> columnRealmDictionaryManagedDictionary = managedCopy.realmGet$columnRealmDictionary();
            columnRealmDictionaryManagedDictionary.clear();
            java.util.Set<java.util.Map.Entry<String, some.test.AllTypes>> entries = columnRealmDictionaryUnmanagedDictionary.entrySet();
            for (java.util.Map.Entry<String, some.test.AllTypes> entry : entries) {
                String entryKey = entry.getKey();
                some.test.AllTypes columnRealmDictionaryUnmanagedEntryValue = entry.getValue();
                some.test.AllTypes cachecolumnRealmDictionary = (some.test.AllTypes) cache.get(columnRealmDictionaryUnmanagedEntryValue);
                if (cachecolumnRealmDictionary != null) {
                    columnRealmDictionaryManagedDictionary.put(entryKey, cachecolumnRealmDictionary);
                } else {
                    if (columnRealmDictionaryUnmanagedEntryValue == null) {
                        columnRealmDictionaryManagedDictionary.put(entryKey, null);
                    } else {
                        columnRealmDictionaryManagedDictionary.put(entryKey, some_test_AllTypesRealmProxy.copyOrUpdate(realm, (some_test_AllTypesRealmProxy.AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class), columnRealmDictionaryUnmanagedEntryValue, update, cache, flags));
                    }
                }
            }
        }
        RealmDictionary<Mixed> columnMixedDictionaryUnmanagedDictionary = unmanagedSource.realmGet$columnMixedDictionary();
        if (columnMixedDictionaryUnmanagedDictionary != null) {
            RealmDictionary<Mixed> columnMixedDictionaryManagedDictionary = managedCopy.realmGet$columnMixedDictionary();
            java.util.Set<java.util.Map.Entry<String, io.realm.Mixed>> entries = columnMixedDictionaryUnmanagedDictionary.entrySet();
            java.util.List<String> keys = new java.util.ArrayList<>();
            java.util.List<Long> mixedPointers = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, io.realm.Mixed> entry : entries) {
                Mixed mixedItem = entry.getValue();
                mixedItem = ProxyUtils.copyOrUpdate(mixedItem, realm, update, cache, flags);
                columnMixedDictionaryManagedDictionary.put(entry.getKey(), mixedItem);
            }
        }

        return managedCopy;
    }

    public static long insert(Realm realm, some.test.AllTypes object, Map<RealmModel,Long> cache) {
        throw new UnsupportedOperationException("Calls to 'insert' with RealmModels containing RealmDictionary properties are not supported yet.");
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        throw new UnsupportedOperationException("Calls to 'insert' with RealmModels containing RealmDictionary properties are not supported yet.");
    }

    public static long insertOrUpdate(Realm realm, some.test.AllTypes object, Map<RealmModel,Long> cache) {
        throw new UnsupportedOperationException("Calls to 'insertOrUpdate' with RealmModels containing RealmDictionary properties are not supported yet.");
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        throw new UnsupportedOperationException("Calls to 'insertOrUpdate' with RealmModels containing RealmDictionary properties are not supported yet.");
    }

    public static some.test.AllTypes createDetachedCopy(some.test.AllTypes realmObject, int currentDepth, int maxDepth, Map<RealmModel, CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmObject == null) {
            return null;
        }
        CacheData<RealmModel> cachedObject = cache.get(realmObject);
        some.test.AllTypes unmanagedObject;
        if (cachedObject == null) {
            unmanagedObject = new some.test.AllTypes();
            cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject));
        } else {
            // Reuse cached object or recreate it because it was encountered at a lower depth.
            if (currentDepth >= cachedObject.minDepth) {
                return (some.test.AllTypes) cachedObject.object;
            }
            unmanagedObject = (some.test.AllTypes) cachedObject.object;
            cachedObject.minDepth = currentDepth;
        }
        some_test_AllTypesRealmProxyInterface unmanagedCopy = (some_test_AllTypesRealmProxyInterface) unmanagedObject;
        some_test_AllTypesRealmProxyInterface realmSource = (some_test_AllTypesRealmProxyInterface) realmObject;
        Realm objectRealm = (Realm) ((RealmObjectProxy) realmObject).realmGet$proxyState().getRealm$realm();
        unmanagedCopy.realmSet$columnString(realmSource.realmGet$columnString());
        unmanagedCopy.realmSet$columnLong(realmSource.realmGet$columnLong());
        unmanagedCopy.realmSet$columnFloat(realmSource.realmGet$columnFloat());
        unmanagedCopy.realmSet$columnDouble(realmSource.realmGet$columnDouble());
        unmanagedCopy.realmSet$columnBoolean(realmSource.realmGet$columnBoolean());
        unmanagedCopy.realmSet$columnDecimal128(realmSource.realmGet$columnDecimal128());
        unmanagedCopy.realmSet$columnObjectId(realmSource.realmGet$columnObjectId());
        unmanagedCopy.realmSet$columnUUID(realmSource.realmGet$columnUUID());
        unmanagedCopy.realmSet$columnDate(realmSource.realmGet$columnDate());

        // Deep copy of columnMixed
        unmanagedCopy.realmSet$columnMixed(ProxyUtils.createDetachedCopy(realmSource.realmGet$columnMixed(), objectRealm, currentDepth + 1, maxDepth, cache));
        unmanagedCopy.realmSet$columnBinary(realmSource.realmGet$columnBinary());
        unmanagedCopy.realmGet$columnMutableRealmInteger().set(realmSource.realmGet$columnMutableRealmInteger().get());

        // Deep copy of columnObject
        unmanagedCopy.realmSet$columnObject(some_test_AllTypesRealmProxy.createDetachedCopy(realmSource.realmGet$columnObject(), currentDepth + 1, maxDepth, cache));

        // Deep copy of columnObjectWithoutPk
        unmanagedCopy.realmSet$columnObjectWithoutPk(some_test_SimpleRealmProxy.createDetachedCopy(realmSource.realmGet$columnObjectWithoutPk(), currentDepth + 1, maxDepth, cache));

        // Deep copy of columnRealmList
        if (currentDepth == maxDepth) {
            unmanagedCopy.realmSet$columnRealmList(null);
        } else {
            RealmList<some.test.AllTypes> managedcolumnRealmListList = realmSource.realmGet$columnRealmList();
            RealmList<some.test.AllTypes> unmanagedcolumnRealmListList = new RealmList<some.test.AllTypes>();
            unmanagedCopy.realmSet$columnRealmList(unmanagedcolumnRealmListList);
            int nextDepth = currentDepth + 1;
            int size = managedcolumnRealmListList.size();
            for (int i = 0; i < size; i++) {
                some.test.AllTypes item = some_test_AllTypesRealmProxy.createDetachedCopy(managedcolumnRealmListList.get(i), nextDepth, maxDepth, cache);
                unmanagedcolumnRealmListList.add(item);
            }
        }

        // Deep copy of columnRealmListNoPk
        if (currentDepth == maxDepth) {
            unmanagedCopy.realmSet$columnRealmListNoPk(null);
        } else {
            RealmList<some.test.Simple> managedcolumnRealmListNoPkList = realmSource.realmGet$columnRealmListNoPk();
            RealmList<some.test.Simple> unmanagedcolumnRealmListNoPkList = new RealmList<some.test.Simple>();
            unmanagedCopy.realmSet$columnRealmListNoPk(unmanagedcolumnRealmListNoPkList);
            int nextDepth = currentDepth + 1;
            int size = managedcolumnRealmListNoPkList.size();
            for (int i = 0; i < size; i++) {
                some.test.Simple item = some_test_SimpleRealmProxy.createDetachedCopy(managedcolumnRealmListNoPkList.get(i), nextDepth, maxDepth, cache);
                unmanagedcolumnRealmListNoPkList.add(item);
            }
        }

        // Deep copy of columnRealmFinalList
        if (currentDepth == maxDepth) {
            unmanagedCopy.realmSet$columnRealmFinalList(null);
        } else {
            RealmList<some.test.AllTypes> managedcolumnRealmFinalListList = realmSource.realmGet$columnRealmFinalList();
            RealmList<some.test.AllTypes> unmanagedcolumnRealmFinalListList = new RealmList<some.test.AllTypes>();
            unmanagedCopy.realmSet$columnRealmFinalList(unmanagedcolumnRealmFinalListList);
            int nextDepth = currentDepth + 1;
            int size = managedcolumnRealmFinalListList.size();
            for (int i = 0; i < size; i++) {
                some.test.AllTypes item = some_test_AllTypesRealmProxy.createDetachedCopy(managedcolumnRealmFinalListList.get(i), nextDepth, maxDepth, cache);
                unmanagedcolumnRealmFinalListList.add(item);
            }
        }

        // Deep copy of columnRealmFinalListNoPk
        if (currentDepth == maxDepth) {
            unmanagedCopy.realmSet$columnRealmFinalListNoPk(null);
        } else {
            RealmList<some.test.Simple> managedcolumnRealmFinalListNoPkList = realmSource.realmGet$columnRealmFinalListNoPk();
            RealmList<some.test.Simple> unmanagedcolumnRealmFinalListNoPkList = new RealmList<some.test.Simple>();
            unmanagedCopy.realmSet$columnRealmFinalListNoPk(unmanagedcolumnRealmFinalListNoPkList);
            int nextDepth = currentDepth + 1;
            int size = managedcolumnRealmFinalListNoPkList.size();
            for (int i = 0; i < size; i++) {
                some.test.Simple item = some_test_SimpleRealmProxy.createDetachedCopy(managedcolumnRealmFinalListNoPkList.get(i), nextDepth, maxDepth, cache);
                unmanagedcolumnRealmFinalListNoPkList.add(item);
            }
        }

        unmanagedCopy.realmSet$columnStringList(new RealmList<java.lang.String>());
        unmanagedCopy.realmGet$columnStringList().addAll(realmSource.realmGet$columnStringList());

        unmanagedCopy.realmSet$columnBinaryList(new RealmList<byte[]>());
        unmanagedCopy.realmGet$columnBinaryList().addAll(realmSource.realmGet$columnBinaryList());

        unmanagedCopy.realmSet$columnBooleanList(new RealmList<java.lang.Boolean>());
        unmanagedCopy.realmGet$columnBooleanList().addAll(realmSource.realmGet$columnBooleanList());

        unmanagedCopy.realmSet$columnLongList(new RealmList<java.lang.Long>());
        unmanagedCopy.realmGet$columnLongList().addAll(realmSource.realmGet$columnLongList());

        unmanagedCopy.realmSet$columnIntegerList(new RealmList<java.lang.Integer>());
        unmanagedCopy.realmGet$columnIntegerList().addAll(realmSource.realmGet$columnIntegerList());

        unmanagedCopy.realmSet$columnShortList(new RealmList<java.lang.Short>());
        unmanagedCopy.realmGet$columnShortList().addAll(realmSource.realmGet$columnShortList());

        unmanagedCopy.realmSet$columnByteList(new RealmList<java.lang.Byte>());
        unmanagedCopy.realmGet$columnByteList().addAll(realmSource.realmGet$columnByteList());

        unmanagedCopy.realmSet$columnDoubleList(new RealmList<java.lang.Double>());
        unmanagedCopy.realmGet$columnDoubleList().addAll(realmSource.realmGet$columnDoubleList());

        unmanagedCopy.realmSet$columnFloatList(new RealmList<java.lang.Float>());
        unmanagedCopy.realmGet$columnFloatList().addAll(realmSource.realmGet$columnFloatList());

        unmanagedCopy.realmSet$columnDateList(new RealmList<java.util.Date>());
        unmanagedCopy.realmGet$columnDateList().addAll(realmSource.realmGet$columnDateList());

        unmanagedCopy.realmSet$columnDecimal128List(new RealmList<org.bson.types.Decimal128>());
        unmanagedCopy.realmGet$columnDecimal128List().addAll(realmSource.realmGet$columnDecimal128List());

        unmanagedCopy.realmSet$columnObjectIdList(new RealmList<org.bson.types.ObjectId>());
        unmanagedCopy.realmGet$columnObjectIdList().addAll(realmSource.realmGet$columnObjectIdList());

        unmanagedCopy.realmSet$columnUUIDList(new RealmList<java.util.UUID>());
        unmanagedCopy.realmGet$columnUUIDList().addAll(realmSource.realmGet$columnUUIDList());

        // Deep copy of columnMixedList
        if (currentDepth == maxDepth) {
            unmanagedCopy.realmSet$columnMixedList(null);
        } else {
            RealmList<Mixed> managedcolumnMixedListList = realmSource.realmGet$columnMixedList();
            RealmList<Mixed> unmanagedcolumnMixedListList = new RealmList<Mixed>();
            unmanagedCopy.realmSet$columnMixedList(unmanagedcolumnMixedListList);
            int nextDepth = currentDepth + 1;
            int size = managedcolumnMixedListList.size();
            for (int i = 0; i < size; i++) {
                Mixed item = ProxyUtils.createDetachedCopy(managedcolumnMixedListList.get(i), objectRealm, nextDepth, maxDepth, cache);
                unmanagedcolumnMixedListList.add(item);
            }
        }

        // Deep copy of columnRealmDictionary
        if (currentDepth == maxDepth) {
            unmanagedCopy.realmSet$columnRealmDictionary(null);
        } else {
            RealmDictionary<some.test.AllTypes> managedcolumnRealmDictionaryDictionary = realmSource.realmGet$columnRealmDictionary();
            RealmDictionary<some.test.AllTypes> unmanagedcolumnRealmDictionaryDictionary = new RealmDictionary<some.test.AllTypes>();
            unmanagedCopy.realmSet$columnRealmDictionary(unmanagedcolumnRealmDictionaryDictionary);
            int nextDepth = currentDepth + 1;
            for (Map.Entry<String, some.test.AllTypes> entry : managedcolumnRealmDictionaryDictionary.entrySet()) {
                some.test.AllTypes detachedValue = some_test_AllTypesRealmProxy.createDetachedCopy(entry.getValue(), nextDepth, maxDepth, cache);
                unmanagedcolumnRealmDictionaryDictionary.put(entry.getKey(), detachedValue);
            }
        }

        unmanagedCopy.realmSet$columnBooleanDictionary(new RealmDictionary<java.lang.Boolean>());
        RealmDictionary<java.lang.Boolean> managedcolumnBooleanDictionaryDictionary = realmSource.realmGet$columnBooleanDictionary();
        for (Map.Entry<String, java.lang.Boolean> entry : managedcolumnBooleanDictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnBooleanDictionary().put(entry.getKey(), entry.getValue());
        }

        unmanagedCopy.realmSet$columnStringDictionary(new RealmDictionary<java.lang.String>());
        RealmDictionary<java.lang.String> managedcolumnStringDictionaryDictionary = realmSource.realmGet$columnStringDictionary();
        for (Map.Entry<String, java.lang.String> entry : managedcolumnStringDictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnStringDictionary().put(entry.getKey(), entry.getValue());
        }

        unmanagedCopy.realmSet$columnIntegerDictionary(new RealmDictionary<java.lang.Integer>());
        RealmDictionary<java.lang.Integer> managedcolumnIntegerDictionaryDictionary = realmSource.realmGet$columnIntegerDictionary();
        for (Map.Entry<String, java.lang.Integer> entry : managedcolumnIntegerDictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnIntegerDictionary().put(entry.getKey(), entry.getValue());
        }

        unmanagedCopy.realmSet$columnFloatDictionary(new RealmDictionary<java.lang.Float>());
        RealmDictionary<java.lang.Float> managedcolumnFloatDictionaryDictionary = realmSource.realmGet$columnFloatDictionary();
        for (Map.Entry<String, java.lang.Float> entry : managedcolumnFloatDictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnFloatDictionary().put(entry.getKey(), entry.getValue());
        }

        unmanagedCopy.realmSet$columnLongDictionary(new RealmDictionary<java.lang.Long>());
        RealmDictionary<java.lang.Long> managedcolumnLongDictionaryDictionary = realmSource.realmGet$columnLongDictionary();
        for (Map.Entry<String, java.lang.Long> entry : managedcolumnLongDictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnLongDictionary().put(entry.getKey(), entry.getValue());
        }

        unmanagedCopy.realmSet$columnShortDictionary(new RealmDictionary<java.lang.Short>());
        RealmDictionary<java.lang.Short> managedcolumnShortDictionaryDictionary = realmSource.realmGet$columnShortDictionary();
        for (Map.Entry<String, java.lang.Short> entry : managedcolumnShortDictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnShortDictionary().put(entry.getKey(), entry.getValue());
        }

        unmanagedCopy.realmSet$columnDoubleDictionary(new RealmDictionary<java.lang.Double>());
        RealmDictionary<java.lang.Double> managedcolumnDoubleDictionaryDictionary = realmSource.realmGet$columnDoubleDictionary();
        for (Map.Entry<String, java.lang.Double> entry : managedcolumnDoubleDictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnDoubleDictionary().put(entry.getKey(), entry.getValue());
        }

        unmanagedCopy.realmSet$columnByteDictionary(new RealmDictionary<java.lang.Byte>());
        RealmDictionary<java.lang.Byte> managedcolumnByteDictionaryDictionary = realmSource.realmGet$columnByteDictionary();
        for (Map.Entry<String, java.lang.Byte> entry : managedcolumnByteDictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnByteDictionary().put(entry.getKey(), entry.getValue());
        }

        unmanagedCopy.realmSet$columnBinaryDictionary(new RealmDictionary<byte[]>());
        RealmDictionary<byte[]> managedcolumnBinaryDictionaryDictionary = realmSource.realmGet$columnBinaryDictionary();
        for (Map.Entry<String, byte[]> entry : managedcolumnBinaryDictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnBinaryDictionary().put(entry.getKey(), entry.getValue());
        }

        unmanagedCopy.realmSet$columnDateDictionary(new RealmDictionary<java.util.Date>());
        RealmDictionary<java.util.Date> managedcolumnDateDictionaryDictionary = realmSource.realmGet$columnDateDictionary();
        for (Map.Entry<String, java.util.Date> entry : managedcolumnDateDictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnDateDictionary().put(entry.getKey(), entry.getValue());
        }

        unmanagedCopy.realmSet$columnObjectIdDictionary(new RealmDictionary<org.bson.types.ObjectId>());
        RealmDictionary<org.bson.types.ObjectId> managedcolumnObjectIdDictionaryDictionary = realmSource.realmGet$columnObjectIdDictionary();
        for (Map.Entry<String, org.bson.types.ObjectId> entry : managedcolumnObjectIdDictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnObjectIdDictionary().put(entry.getKey(), entry.getValue());
        }

        unmanagedCopy.realmSet$columnUUIDDictionary(new RealmDictionary<java.util.UUID>());
        RealmDictionary<java.util.UUID> managedcolumnUUIDDictionaryDictionary = realmSource.realmGet$columnUUIDDictionary();
        for (Map.Entry<String, java.util.UUID> entry : managedcolumnUUIDDictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnUUIDDictionary().put(entry.getKey(), entry.getValue());
        }

        unmanagedCopy.realmSet$columnDecimal128Dictionary(new RealmDictionary<org.bson.types.Decimal128>());
        RealmDictionary<org.bson.types.Decimal128> managedcolumnDecimal128DictionaryDictionary = realmSource.realmGet$columnDecimal128Dictionary();
        for (Map.Entry<String, org.bson.types.Decimal128> entry : managedcolumnDecimal128DictionaryDictionary.entrySet()) {
            unmanagedCopy.realmGet$columnDecimal128Dictionary().put(entry.getKey(), entry.getValue());
        }

        // Deep copy of columnMixedDictionary
        if (currentDepth == maxDepth) {
            unmanagedCopy.realmSet$columnMixedDictionary(null);
        } else {
            RealmDictionary<Mixed> managedcolumnMixedDictionaryDictionary = realmSource.realmGet$columnMixedDictionary();
            RealmDictionary<Mixed> unmanagedcolumnMixedDictionaryDictionary = new RealmDictionary<Mixed>();
            unmanagedCopy.realmSet$columnMixedDictionary(unmanagedcolumnMixedDictionaryDictionary);
            int nextDepth = currentDepth + 1;
            for (Map.Entry<String, Mixed> entry : managedcolumnMixedDictionaryDictionary.entrySet()) {
                Mixed detachedValue = ProxyUtils.createDetachedCopy(entry.getValue(), objectRealm, nextDepth, maxDepth, cache);
                unmanagedcolumnMixedDictionaryDictionary.put(entry.getKey(), detachedValue);
            }
        }

        return unmanagedObject;
    }

    static some.test.AllTypes update(Realm realm, AllTypesColumnInfo columnInfo, some.test.AllTypes realmObject, some.test.AllTypes newObject, Map<RealmModel, RealmObjectProxy> cache, Set<ImportFlag> flags) {
        some_test_AllTypesRealmProxyInterface realmObjectTarget = (some_test_AllTypesRealmProxyInterface) realmObject;
        some_test_AllTypesRealmProxyInterface realmObjectSource = (some_test_AllTypesRealmProxyInterface) newObject;
        Table table = realm.getTable(some.test.AllTypes.class);
        OsObjectBuilder builder = new OsObjectBuilder(table, flags);
        builder.addString(columnInfo.columnStringColKey, realmObjectSource.realmGet$columnString());
        builder.addInteger(columnInfo.columnLongColKey, realmObjectSource.realmGet$columnLong());
        builder.addFloat(columnInfo.columnFloatColKey, realmObjectSource.realmGet$columnFloat());
        builder.addDouble(columnInfo.columnDoubleColKey, realmObjectSource.realmGet$columnDouble());
        builder.addBoolean(columnInfo.columnBooleanColKey, realmObjectSource.realmGet$columnBoolean());
        builder.addDecimal128(columnInfo.columnDecimal128ColKey, realmObjectSource.realmGet$columnDecimal128());
        builder.addObjectId(columnInfo.columnObjectIdColKey, realmObjectSource.realmGet$columnObjectId());
        builder.addUUID(columnInfo.columnUUIDColKey, realmObjectSource.realmGet$columnUUID());
        builder.addDate(columnInfo.columnDateColKey, realmObjectSource.realmGet$columnDate());

        Mixed columnMixedMixed = realmObjectSource.realmGet$columnMixed();
        columnMixedMixed = ProxyUtils.copyOrUpdate(columnMixedMixed, realm, true, cache, flags);
        builder.addMixed(columnInfo.columnMixedColKey, columnMixedMixed.getNativePtr());
        builder.addByteArray(columnInfo.columnBinaryColKey, realmObjectSource.realmGet$columnBinary());
        builder.addMutableRealmInteger(columnInfo.columnMutableRealmIntegerColKey, realmObjectSource.realmGet$columnMutableRealmInteger());

        some.test.AllTypes columnObjectObj = realmObjectSource.realmGet$columnObject();
        if (columnObjectObj == null) {
            builder.addNull(columnInfo.columnObjectColKey);
        } else {
            some.test.AllTypes cachecolumnObject = (some.test.AllTypes) cache.get(columnObjectObj);
            if (cachecolumnObject != null) {
                builder.addObject(columnInfo.columnObjectColKey, cachecolumnObject);
            } else {
                builder.addObject(columnInfo.columnObjectColKey, some_test_AllTypesRealmProxy.copyOrUpdate(realm, (some_test_AllTypesRealmProxy.AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class), columnObjectObj, true, cache, flags));
            }
        }

        some.test.Simple columnObjectWithoutPkObj = realmObjectSource.realmGet$columnObjectWithoutPk();
        if (columnObjectWithoutPkObj == null) {
            builder.addNull(columnInfo.columnObjectWithoutPkColKey);
        } else {
            some.test.Simple cachecolumnObjectWithoutPk = (some.test.Simple) cache.get(columnObjectWithoutPkObj);
            if (cachecolumnObjectWithoutPk != null) {
                builder.addObject(columnInfo.columnObjectWithoutPkColKey, cachecolumnObjectWithoutPk);
            } else {
                builder.addObject(columnInfo.columnObjectWithoutPkColKey, some_test_SimpleRealmProxy.copyOrUpdate(realm, (some_test_SimpleRealmProxy.SimpleColumnInfo) realm.getSchema().getColumnInfo(some.test.Simple.class), columnObjectWithoutPkObj, true, cache, flags));
            }
        }

        RealmList<some.test.AllTypes> columnRealmListUnmanagedList = realmObjectSource.realmGet$columnRealmList();
        if (columnRealmListUnmanagedList != null) {
            RealmList<some.test.AllTypes> columnRealmListManagedCopy = new RealmList<some.test.AllTypes>();
            for (int i = 0; i < columnRealmListUnmanagedList.size(); i++) {
                some.test.AllTypes columnRealmListItem = columnRealmListUnmanagedList.get(i);
                some.test.AllTypes cachecolumnRealmList = (some.test.AllTypes) cache.get(columnRealmListItem);
                if (cachecolumnRealmList != null) {
                    columnRealmListManagedCopy.add(cachecolumnRealmList);
                } else {
                    columnRealmListManagedCopy.add(some_test_AllTypesRealmProxy.copyOrUpdate(realm, (some_test_AllTypesRealmProxy.AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class), columnRealmListItem, true, cache, flags));
                }
            }
            builder.addObjectList(columnInfo.columnRealmListColKey, columnRealmListManagedCopy);
        } else {
            builder.addObjectList(columnInfo.columnRealmListColKey, new RealmList<some.test.AllTypes>());
        }

        RealmList<some.test.Simple> columnRealmListNoPkUnmanagedList = realmObjectSource.realmGet$columnRealmListNoPk();
        if (columnRealmListNoPkUnmanagedList != null) {
            RealmList<some.test.Simple> columnRealmListNoPkManagedCopy = new RealmList<some.test.Simple>();
            for (int i = 0; i < columnRealmListNoPkUnmanagedList.size(); i++) {
                some.test.Simple columnRealmListNoPkItem = columnRealmListNoPkUnmanagedList.get(i);
                some.test.Simple cachecolumnRealmListNoPk = (some.test.Simple) cache.get(columnRealmListNoPkItem);
                if (cachecolumnRealmListNoPk != null) {
                    columnRealmListNoPkManagedCopy.add(cachecolumnRealmListNoPk);
                } else {
                    columnRealmListNoPkManagedCopy.add(some_test_SimpleRealmProxy.copyOrUpdate(realm, (some_test_SimpleRealmProxy.SimpleColumnInfo) realm.getSchema().getColumnInfo(some.test.Simple.class), columnRealmListNoPkItem, true, cache, flags));
                }
            }
            builder.addObjectList(columnInfo.columnRealmListNoPkColKey, columnRealmListNoPkManagedCopy);
        } else {
            builder.addObjectList(columnInfo.columnRealmListNoPkColKey, new RealmList<some.test.Simple>());
        }

        RealmList<some.test.AllTypes> columnRealmFinalListUnmanagedList = realmObjectSource.realmGet$columnRealmFinalList();
        if (columnRealmFinalListUnmanagedList != null) {
            RealmList<some.test.AllTypes> columnRealmFinalListManagedCopy = new RealmList<some.test.AllTypes>();
            for (int i = 0; i < columnRealmFinalListUnmanagedList.size(); i++) {
                some.test.AllTypes columnRealmFinalListItem = columnRealmFinalListUnmanagedList.get(i);
                some.test.AllTypes cachecolumnRealmFinalList = (some.test.AllTypes) cache.get(columnRealmFinalListItem);
                if (cachecolumnRealmFinalList != null) {
                    columnRealmFinalListManagedCopy.add(cachecolumnRealmFinalList);
                } else {
                    columnRealmFinalListManagedCopy.add(some_test_AllTypesRealmProxy.copyOrUpdate(realm, (some_test_AllTypesRealmProxy.AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class), columnRealmFinalListItem, true, cache, flags));
                }
            }
            builder.addObjectList(columnInfo.columnRealmFinalListColKey, columnRealmFinalListManagedCopy);
        } else {
            builder.addObjectList(columnInfo.columnRealmFinalListColKey, new RealmList<some.test.AllTypes>());
        }

        RealmList<some.test.Simple> columnRealmFinalListNoPkUnmanagedList = realmObjectSource.realmGet$columnRealmFinalListNoPk();
        if (columnRealmFinalListNoPkUnmanagedList != null) {
            RealmList<some.test.Simple> columnRealmFinalListNoPkManagedCopy = new RealmList<some.test.Simple>();
            for (int i = 0; i < columnRealmFinalListNoPkUnmanagedList.size(); i++) {
                some.test.Simple columnRealmFinalListNoPkItem = columnRealmFinalListNoPkUnmanagedList.get(i);
                some.test.Simple cachecolumnRealmFinalListNoPk = (some.test.Simple) cache.get(columnRealmFinalListNoPkItem);
                if (cachecolumnRealmFinalListNoPk != null) {
                    columnRealmFinalListNoPkManagedCopy.add(cachecolumnRealmFinalListNoPk);
                } else {
                    columnRealmFinalListNoPkManagedCopy.add(some_test_SimpleRealmProxy.copyOrUpdate(realm, (some_test_SimpleRealmProxy.SimpleColumnInfo) realm.getSchema().getColumnInfo(some.test.Simple.class), columnRealmFinalListNoPkItem, true, cache, flags));
                }
            }
            builder.addObjectList(columnInfo.columnRealmFinalListNoPkColKey, columnRealmFinalListNoPkManagedCopy);
        } else {
            builder.addObjectList(columnInfo.columnRealmFinalListNoPkColKey, new RealmList<some.test.Simple>());
        }
        builder.addStringList(columnInfo.columnStringListColKey, realmObjectSource.realmGet$columnStringList());
        builder.addByteArrayList(columnInfo.columnBinaryListColKey, realmObjectSource.realmGet$columnBinaryList());
        builder.addBooleanList(columnInfo.columnBooleanListColKey, realmObjectSource.realmGet$columnBooleanList());
        builder.addLongList(columnInfo.columnLongListColKey, realmObjectSource.realmGet$columnLongList());
        builder.addIntegerList(columnInfo.columnIntegerListColKey, realmObjectSource.realmGet$columnIntegerList());
        builder.addShortList(columnInfo.columnShortListColKey, realmObjectSource.realmGet$columnShortList());
        builder.addByteList(columnInfo.columnByteListColKey, realmObjectSource.realmGet$columnByteList());
        builder.addDoubleList(columnInfo.columnDoubleListColKey, realmObjectSource.realmGet$columnDoubleList());
        builder.addFloatList(columnInfo.columnFloatListColKey, realmObjectSource.realmGet$columnFloatList());
        builder.addDateList(columnInfo.columnDateListColKey, realmObjectSource.realmGet$columnDateList());
        builder.addDecimal128List(columnInfo.columnDecimal128ListColKey, realmObjectSource.realmGet$columnDecimal128List());
        builder.addObjectIdList(columnInfo.columnObjectIdListColKey, realmObjectSource.realmGet$columnObjectIdList());
        builder.addUUIDList(columnInfo.columnUUIDListColKey, realmObjectSource.realmGet$columnUUIDList());

        RealmList<Mixed> columnMixedListUnmanagedList = realmObjectSource.realmGet$columnMixedList();
        if (columnMixedListUnmanagedList != null) {
            RealmList<Mixed> columnMixedListManagedCopy = new RealmList<Mixed>();
            for (int i = 0; i < columnMixedListUnmanagedList.size(); i++) {
                Mixed mixedItem = columnMixedListUnmanagedList.get(i);
                mixedItem = ProxyUtils.copyOrUpdate(mixedItem, realm, true, cache, flags);
                columnMixedListManagedCopy.add(mixedItem);
            }
            builder.addMixedList(columnInfo.columnMixedListColKey, columnMixedListManagedCopy);
        } else {
            builder.addMixedList(columnInfo.columnMixedListColKey, new RealmList<Mixed>());
        }

        RealmDictionary<some.test.AllTypes> columnRealmDictionaryUnmanagedDictionary = realmObjectSource.realmGet$columnRealmDictionary();
        if (columnRealmDictionaryUnmanagedDictionary != null) {
            RealmDictionary<some.test.AllTypes> columnRealmDictionaryManagedDictionary = new RealmDictionary<>();
            java.util.Set<java.util.Map.Entry<String, some.test.AllTypes>> entries = columnRealmDictionaryUnmanagedDictionary.entrySet();
            for (java.util.Map.Entry<String, some.test.AllTypes> entry : entries) {
                String entryKey = entry.getKey();
                some.test.AllTypes columnRealmDictionaryUnmanagedEntryValue = entry.getValue();
                some.test.AllTypes cachecolumnRealmDictionary = (some.test.AllTypes) cache.get(columnRealmDictionaryUnmanagedEntryValue);
                if (cachecolumnRealmDictionary != null) {
                    columnRealmDictionaryManagedDictionary.put(entryKey, cachecolumnRealmDictionary);
                } else {
                    if (columnRealmDictionaryUnmanagedEntryValue == null) {
                        columnRealmDictionaryManagedDictionary.put(entryKey, null);
                    } else {
                        columnRealmDictionaryManagedDictionary.put(entryKey, some_test_AllTypesRealmProxy.copyOrUpdate(realm, (some_test_AllTypesRealmProxy.AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class), columnRealmDictionaryUnmanagedEntryValue, true, cache, flags));
                    }
                }
            }
            builder.addObjectDictionary(columnInfo.columnRealmDictionaryColKey, columnRealmDictionaryManagedDictionary);
        } else {
            builder.addObjectDictionary(columnInfo.columnRealmDictionaryColKey, null);
        }
        builder.addBooleanValueDictionary(columnInfo.columnBooleanDictionaryColKey, realmObjectSource.realmGet$columnBooleanDictionary());
        builder.addStringValueDictionary(columnInfo.columnStringDictionaryColKey, realmObjectSource.realmGet$columnStringDictionary());
        builder.addIntegerValueDictionary(columnInfo.columnIntegerDictionaryColKey, realmObjectSource.realmGet$columnIntegerDictionary());
        builder.addFloatValueDictionary(columnInfo.columnFloatDictionaryColKey, realmObjectSource.realmGet$columnFloatDictionary());
        builder.addLongValueDictionary(columnInfo.columnLongDictionaryColKey, realmObjectSource.realmGet$columnLongDictionary());
        builder.addShortValueDictionary(columnInfo.columnShortDictionaryColKey, realmObjectSource.realmGet$columnShortDictionary());
        builder.addDoubleValueDictionary(columnInfo.columnDoubleDictionaryColKey, realmObjectSource.realmGet$columnDoubleDictionary());
        builder.addByteValueDictionary(columnInfo.columnByteDictionaryColKey, realmObjectSource.realmGet$columnByteDictionary());
        builder.addBinaryValueDictionary(columnInfo.columnBinaryDictionaryColKey, realmObjectSource.realmGet$columnBinaryDictionary());
        builder.addDateValueDictionary(columnInfo.columnDateDictionaryColKey, realmObjectSource.realmGet$columnDateDictionary());
        builder.addObjectIdValueDictionary(columnInfo.columnObjectIdDictionaryColKey, realmObjectSource.realmGet$columnObjectIdDictionary());
        builder.addUUIDValueDictionary(columnInfo.columnUUIDDictionaryColKey, realmObjectSource.realmGet$columnUUIDDictionary());
        builder.addDecimal128ValueDictionary(columnInfo.columnDecimal128DictionaryColKey, realmObjectSource.realmGet$columnDecimal128Dictionary());

        RealmDictionary<Mixed> columnMixedDictionaryUnmanagedDictionary = realmObjectSource.realmGet$columnMixedDictionary();
        if (columnMixedDictionaryUnmanagedDictionary != null) {
            RealmDictionary<Mixed> columnMixedDictionaryManagedDictionary = new RealmDictionary<>();
            java.util.Set<java.util.Map.Entry<String, io.realm.Mixed>> entries = columnMixedDictionaryUnmanagedDictionary.entrySet();
            java.util.List<String> keys = new java.util.ArrayList<>();
            java.util.List<Long> mixedPointers = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, io.realm.Mixed> entry : entries) {
                Mixed mixedItem = entry.getValue();
                mixedItem = ProxyUtils.copyOrUpdate(mixedItem, realm, true, cache, flags);
                columnMixedDictionaryManagedDictionary.put(entry.getKey(), mixedItem);
            }
            builder.addMixedValueDictionary(columnInfo.columnMixedDictionaryColKey, columnMixedDictionaryManagedDictionary);
        } else {
            builder.addMixedValueDictionary(columnInfo.columnMixedDictionaryColKey, null);
        }


        builder.updateExistingTopLevelObject();
        return realmObject;
    }

    @Override
    @SuppressWarnings("ArrayToString")
    public String toString() {
        if (!RealmObject.isValid(this)) {
            return "Invalid object";
        }
        StringBuilder stringBuilder = new StringBuilder("AllTypes = proxy[");
        stringBuilder.append("{columnString:");
        stringBuilder.append(realmGet$columnString() != null ? realmGet$columnString() : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnLong:");
        stringBuilder.append(realmGet$columnLong());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnFloat:");
        stringBuilder.append(realmGet$columnFloat());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDouble:");
        stringBuilder.append(realmGet$columnDouble());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBoolean:");
        stringBuilder.append(realmGet$columnBoolean());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDecimal128:");
        stringBuilder.append(realmGet$columnDecimal128());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnObjectId:");
        stringBuilder.append(realmGet$columnObjectId());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnUUID:");
        stringBuilder.append(realmGet$columnUUID());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDate:");
        stringBuilder.append(realmGet$columnDate());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnMixed:");
        stringBuilder.append((realmGet$columnMixed().isNull()) ? "null" : "realmGet$columnMixed()");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBinary:");
        stringBuilder.append("binary(" + realmGet$columnBinary().length + ")");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnMutableRealmInteger:");
        stringBuilder.append(realmGet$columnMutableRealmInteger().get());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnObject:");
        stringBuilder.append(realmGet$columnObject() != null ? "AllTypes" : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnObjectWithoutPk:");
        stringBuilder.append(realmGet$columnObjectWithoutPk() != null ? "Simple" : "null");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnRealmList:");
        stringBuilder.append("RealmList<AllTypes>[").append(realmGet$columnRealmList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnRealmListNoPk:");
        stringBuilder.append("RealmList<Simple>[").append(realmGet$columnRealmListNoPk().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnRealmFinalList:");
        stringBuilder.append("RealmList<AllTypes>[").append(realmGet$columnRealmFinalList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnRealmFinalListNoPk:");
        stringBuilder.append("RealmList<Simple>[").append(realmGet$columnRealmFinalListNoPk().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnStringList:");
        stringBuilder.append("RealmList<String>[").append(realmGet$columnStringList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBinaryList:");
        stringBuilder.append("RealmList<byte[]>[").append(realmGet$columnBinaryList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBooleanList:");
        stringBuilder.append("RealmList<Boolean>[").append(realmGet$columnBooleanList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnLongList:");
        stringBuilder.append("RealmList<Long>[").append(realmGet$columnLongList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnIntegerList:");
        stringBuilder.append("RealmList<Integer>[").append(realmGet$columnIntegerList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnShortList:");
        stringBuilder.append("RealmList<Short>[").append(realmGet$columnShortList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnByteList:");
        stringBuilder.append("RealmList<Byte>[").append(realmGet$columnByteList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDoubleList:");
        stringBuilder.append("RealmList<Double>[").append(realmGet$columnDoubleList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnFloatList:");
        stringBuilder.append("RealmList<Float>[").append(realmGet$columnFloatList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDateList:");
        stringBuilder.append("RealmList<Date>[").append(realmGet$columnDateList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDecimal128List:");
        stringBuilder.append("RealmList<Decimal128>[").append(realmGet$columnDecimal128List().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnObjectIdList:");
        stringBuilder.append("RealmList<ObjectId>[").append(realmGet$columnObjectIdList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnUUIDList:");
        stringBuilder.append("RealmList<UUID>[").append(realmGet$columnUUIDList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnMixedList:");
        stringBuilder.append("RealmList<Mixed>[").append(realmGet$columnMixedList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnRealmDictionary:");
        stringBuilder.append("RealmDictionary<AllTypes>[").append(realmGet$columnRealmDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBooleanDictionary:");
        stringBuilder.append("RealmDictionary<Boolean>[").append(realmGet$columnBooleanDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnStringDictionary:");
        stringBuilder.append("RealmDictionary<String>[").append(realmGet$columnStringDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnIntegerDictionary:");
        stringBuilder.append("RealmDictionary<Integer>[").append(realmGet$columnIntegerDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnFloatDictionary:");
        stringBuilder.append("RealmDictionary<Float>[").append(realmGet$columnFloatDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnLongDictionary:");
        stringBuilder.append("RealmDictionary<Long>[").append(realmGet$columnLongDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnShortDictionary:");
        stringBuilder.append("RealmDictionary<Short>[").append(realmGet$columnShortDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDoubleDictionary:");
        stringBuilder.append("RealmDictionary<Double>[").append(realmGet$columnDoubleDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnByteDictionary:");
        stringBuilder.append("RealmDictionary<Byte>[").append(realmGet$columnByteDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBinaryDictionary:");
        stringBuilder.append("RealmDictionary<byte[]>[").append(realmGet$columnBinaryDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDateDictionary:");
        stringBuilder.append("RealmDictionary<Date>[").append(realmGet$columnDateDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnObjectIdDictionary:");
        stringBuilder.append("RealmDictionary<ObjectId>[").append(realmGet$columnObjectIdDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnUUIDDictionary:");
        stringBuilder.append("RealmDictionary<UUID>[").append(realmGet$columnUUIDDictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnDecimal128Dictionary:");
        stringBuilder.append("RealmDictionary<Decimal128>[").append(realmGet$columnDecimal128Dictionary().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnMixedDictionary:");
        stringBuilder.append("RealmDictionary<Mixed>[").append(realmGet$columnMixedDictionary().size()).append("]");
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
        some_test_AllTypesRealmProxy aAllTypes = (some_test_AllTypesRealmProxy)o;

        BaseRealm realm = proxyState.getRealm$realm();
        BaseRealm otherRealm = aAllTypes.proxyState.getRealm$realm();
        String path = realm.getPath();
        String otherPath = otherRealm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;
        if (realm.isFrozen() != otherRealm.isFrozen()) return false;
        if (!realm.sharedRealm.getVersionID().equals(otherRealm.sharedRealm.getVersionID())) {
            return false;
        }

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aAllTypes.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getObjectKey() != aAllTypes.proxyState.getRow$realm().getObjectKey()) return false;

        return true;
    }
}