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
        long columnRealmListColKey;
        long columnRealmFinalListColKey;
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

        AllTypesColumnInfo(OsSchemaInfo schemaInfo) {
            super(29);
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
            this.columnRealmListColKey = addColumnDetails("columnRealmList", "columnRealmList", objectSchemaInfo);
            this.columnRealmFinalListColKey = addColumnDetails("columnRealmFinalList", "columnRealmFinalList", objectSchemaInfo);
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
            dst.columnRealmListColKey = src.columnRealmListColKey;
            dst.columnRealmFinalListColKey = src.columnRealmFinalListColKey;
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
        }
    }

    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private AllTypesColumnInfo columnInfo;
    private ProxyState<some.test.AllTypes> proxyState;
    private final MutableRealmInteger.Managed columnMutableRealmIntegerMutableRealmInteger = new MutableRealmInteger.Managed<some.test.AllTypes>() {
        @Override protected ProxyState<some.test.AllTypes> getProxyState() { return proxyState; }
        @Override protected long getColumnIndex() { return columnInfo.columnMutableRealmIntegerColKey; }
    };
    private RealmList<some.test.AllTypes> columnRealmListRealmList;
    private RealmList<some.test.AllTypes> columnRealmFinalListRealmList;
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
                value = realm.copyToRealm(value);
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
                        value.add(realm.copyToRealm(item));
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
                        value.add(realm.copyToRealm(item));
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
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("AllTypes", false, 29, 1);
        builder.addPersistedProperty("columnString", RealmFieldType.STRING, Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("columnLong", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnFloat", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnDouble", RealmFieldType.DOUBLE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnBoolean", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnDecimal128", RealmFieldType.DECIMAL128, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnObjectId", RealmFieldType.OBJECT_ID, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnUUID", RealmFieldType.UUID, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnDate", RealmFieldType.DATE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnMixed", RealmFieldType.MIXED, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("columnBinary", RealmFieldType.BINARY, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnMutableRealmInteger", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedLinkProperty("columnObject", RealmFieldType.OBJECT, "AllTypes");
        builder.addPersistedLinkProperty("columnRealmList", RealmFieldType.LIST, "AllTypes");
        builder.addPersistedLinkProperty("columnRealmFinalList", RealmFieldType.LIST, "AllTypes");
        builder.addPersistedValueListProperty("columnStringList", RealmFieldType.STRING_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnBinaryList", RealmFieldType.BINARY_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnBooleanList", RealmFieldType.BOOLEAN_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnLongList", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnIntegerList", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnShortList", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnByteList", RealmFieldType.INTEGER_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnDoubleList", RealmFieldType.DOUBLE_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnFloatList", RealmFieldType.FLOAT_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnDateList", RealmFieldType.DATE_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnDecimal128List", RealmFieldType.DECIMAL128_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnObjectIdList", RealmFieldType.OBJECT_ID_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnUUIDList", RealmFieldType.UUID_LIST, !Property.REQUIRED);
        builder.addPersistedValueListProperty("columnMixedList", RealmFieldType.MIXED_LIST, !Property.REQUIRED);
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
        final List<String> excludeFields = new ArrayList<String>(17);
        some.test.AllTypes obj = null;
        if (update) {
            Table table = realm.getTable(some.test.AllTypes.class);
            AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
            long pkColumnKey = columnInfo.columnStringColKey;
            long objKey = Table.NO_MATCH;
            if (json.isNull("columnString")) {
                objKey = table.findFirstNull(pkColumnKey);
            } else {
                objKey = table.findFirstString(pkColumnKey, json.getString("columnString"));
            }
            if (objKey != Table.NO_MATCH) {
                final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
                try {
                    objectContext.set(realm, table.getUncheckedRow(objKey), realm.getSchema().getColumnInfo(some.test.AllTypes.class), false, Collections.<String> emptyList());
                    obj = new io.realm.some_test_AllTypesRealmProxy();
                } finally {
                    objectContext.clear();
                }
            }
        }
        if (obj == null) {
            if (json.has("columnObject")) {
                excludeFields.add("columnObject");
            }
            if (json.has("columnRealmList")) {
                excludeFields.add("columnRealmList");
            }
            if (json.has("columnRealmFinalList")) {
                excludeFields.add("columnRealmFinalList");
            }
            if (json.has("columnStringList")) {
                excludeFields.add("columnStringList");
            }
            if (json.has("columnBinaryList")) {
                excludeFields.add("columnBinaryList");
            }
            if (json.has("columnBooleanList")) {
                excludeFields.add("columnBooleanList");
            }
            if (json.has("columnLongList")) {
                excludeFields.add("columnLongList");
            }
            if (json.has("columnIntegerList")) {
                excludeFields.add("columnIntegerList");
            }
            if (json.has("columnShortList")) {
                excludeFields.add("columnShortList");
            }
            if (json.has("columnByteList")) {
                excludeFields.add("columnByteList");
            }
            if (json.has("columnDoubleList")) {
                excludeFields.add("columnDoubleList");
            }
            if (json.has("columnFloatList")) {
                excludeFields.add("columnFloatList");
            }
            if (json.has("columnDateList")) {
                excludeFields.add("columnDateList");
            }
            if (json.has("columnDecimal128List")) {
                excludeFields.add("columnDecimal128List");
            }
            if (json.has("columnObjectIdList")) {
                excludeFields.add("columnObjectIdList");
            }
            if (json.has("columnUUIDList")) {
                excludeFields.add("columnUUIDList");
            }
            if (json.has("columnMixedList")) {
                excludeFields.add("columnMixedList");
            }
            if (json.has("columnString")) {
                if (json.isNull("columnString")) {
                    obj = (io.realm.some_test_AllTypesRealmProxy) realm.createObjectInternal(some.test.AllTypes.class, null, true, excludeFields);
                } else {
                    obj = (io.realm.some_test_AllTypesRealmProxy) realm.createObjectInternal(some.test.AllTypes.class, json.getString("columnString"), true, excludeFields);
                }
            } else {
                throw new IllegalArgumentException("JSON object doesn't have the primary key field 'columnString'.");
            }
        }

        final some_test_AllTypesRealmProxyInterface objProxy = (some_test_AllTypesRealmProxyInterface) obj;
        if (json.has("columnLong")) {
            if (json.isNull("columnLong")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnLong' to null.");
            } else {
                objProxy.realmSet$columnLong((long) json.getLong("columnLong"));
            }
        }
        if (json.has("columnFloat")) {
            if (json.isNull("columnFloat")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnFloat' to null.");
            } else {
                objProxy.realmSet$columnFloat((float) json.getDouble("columnFloat"));
            }
        }
        if (json.has("columnDouble")) {
            if (json.isNull("columnDouble")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnDouble' to null.");
            } else {
                objProxy.realmSet$columnDouble((double) json.getDouble("columnDouble"));
            }
        }
        if (json.has("columnBoolean")) {
            if (json.isNull("columnBoolean")) {
                throw new IllegalArgumentException("Trying to set non-nullable field 'columnBoolean' to null.");
            } else {
                objProxy.realmSet$columnBoolean((boolean) json.getBoolean("columnBoolean"));
            }
        }
        if (json.has("columnDecimal128")) {
            if (json.isNull("columnDecimal128")) {
                objProxy.realmSet$columnDecimal128(null);
            } else {
                Object decimal = json.get("columnDecimal128");
                if (decimal instanceof org.bson.types.Decimal128) {
                    objProxy.realmSet$columnDecimal128((org.bson.types.Decimal128) decimal);
                } else if (decimal instanceof String) {
                    objProxy.realmSet$columnDecimal128(org.bson.types.Decimal128.parse((String)decimal));
                } else if (decimal instanceof Integer) {
                    objProxy.realmSet$columnDecimal128(new org.bson.types.Decimal128((Integer)(decimal)));
                } else if (decimal instanceof Long) {
                    objProxy.realmSet$columnDecimal128(new org.bson.types.Decimal128((Long)(decimal)));
                } else if (decimal instanceof Double) {
                    objProxy.realmSet$columnDecimal128(new org.bson.types.Decimal128(new java.math.BigDecimal((Double)(decimal))));
                } else {
                    throw new UnsupportedOperationException(decimal.getClass() + " is not supported as a Decimal128 value");
                }
            }
        }
        if (json.has("columnObjectId")) {
            if (json.isNull("columnObjectId")) {
                objProxy.realmSet$columnObjectId(null);
            } else {
                Object id = json.get("columnObjectId");
                if (id instanceof org.bson.types.ObjectId) {
                    objProxy.realmSet$columnObjectId((org.bson.types.ObjectId) id);
                } else {
                    objProxy.realmSet$columnObjectId(new org.bson.types.ObjectId((String)id));
                }
            }
        }
        if (json.has("columnUUID")) {
            if (json.isNull("columnUUID")) {
                objProxy.realmSet$columnUUID(null);
            } else {
                Object id = json.get("columnUUID");
                if (id instanceof java.util.UUID) {
                    objProxy.realmSet$columnUUID((java.util.UUID) id);
                } else {
                    objProxy.realmSet$columnUUID(java.util.UUID.fromString((String)id));
                }
            }
        }
        if (json.has("columnDate")) {
            if (json.isNull("columnDate")) {
                objProxy.realmSet$columnDate(null);
            } else {
                Object timestamp = json.get("columnDate");
                if (timestamp instanceof String) {
                    objProxy.realmSet$columnDate(JsonUtils.stringToDate((String) timestamp));
                } else {
                    objProxy.realmSet$columnDate(new Date(json.getLong("columnDate")));
                }
            }
        }
        if (json.has("columnBinary")) {
            if (json.isNull("columnBinary")) {
                objProxy.realmSet$columnBinary(null);
            } else {
                objProxy.realmSet$columnBinary(JsonUtils.stringToBytes(json.getString("columnBinary")));
            }
        }
        if (json.has("columnMutableRealmInteger")) {
            objProxy.realmGet$columnMutableRealmInteger().set((json.isNull("columnMutableRealmInteger")) ? null : json.getLong("columnMutableRealmInteger"));
        }
        if (json.has("columnObject")) {
            if (json.isNull("columnObject")) {
                objProxy.realmSet$columnObject(null);
            } else {
                some.test.AllTypes columnObjectObj = some_test_AllTypesRealmProxy.createOrUpdateUsingJsonObject(realm, json.getJSONObject("columnObject"), update);
                objProxy.realmSet$columnObject(columnObjectObj);
            }
        }
        if (json.has("columnRealmList")) {
            if (json.isNull("columnRealmList")) {
                objProxy.realmSet$columnRealmList(null);
            } else {
                objProxy.realmGet$columnRealmList().clear();
                JSONArray array = json.getJSONArray("columnRealmList");
                for (int i = 0; i < array.length(); i++) {
                    some.test.AllTypes item = some_test_AllTypesRealmProxy.createOrUpdateUsingJsonObject(realm, array.getJSONObject(i), update);
                    objProxy.realmGet$columnRealmList().add(item);
                }
            }
        }
        if (json.has("columnRealmFinalList")) {
            if (json.isNull("columnRealmFinalList")) {
                objProxy.realmSet$columnRealmFinalList(null);
            } else {
                objProxy.realmGet$columnRealmFinalList().clear();
                JSONArray array = json.getJSONArray("columnRealmFinalList");
                for (int i = 0; i < array.length(); i++) {
                    some.test.AllTypes item = some_test_AllTypesRealmProxy.createOrUpdateUsingJsonObject(realm, array.getJSONObject(i), update);
                    objProxy.realmGet$columnRealmFinalList().add(item);
                }
            }
        }
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnStringList(), json, "columnStringList");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnBinaryList(), json, "columnBinaryList");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnBooleanList(), json, "columnBooleanList");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnLongList(), json, "columnLongList");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnIntegerList(), json, "columnIntegerList");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnShortList(), json, "columnShortList");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnByteList(), json, "columnByteList");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnDoubleList(), json, "columnDoubleList");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnFloatList(), json, "columnFloatList");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnDateList(), json, "columnDateList");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnDecimal128List(), json, "columnDecimal128List");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnObjectIdList(), json, "columnObjectIdList");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnUUIDList(), json, "columnUUIDList");
        ProxyUtils.setRealmListWithJsonObject(objProxy.realmGet$columnMixedList(), json, "columnMixedList");
        return obj;
    }

    @SuppressWarnings("cast")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static some.test.AllTypes createUsingJsonStream(Realm realm, JsonReader reader)
            throws IOException {
        boolean jsonHasPrimaryKey = false;
        final some.test.AllTypes obj = new some.test.AllTypes();
        final some_test_AllTypesRealmProxyInterface objProxy = (some_test_AllTypesRealmProxyInterface) obj;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (false) {
            } else if (name.equals("columnString")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$columnString((String) reader.nextString());
                } else {
                    reader.skipValue();
                    objProxy.realmSet$columnString(null);
                }
                jsonHasPrimaryKey = true;
            } else if (name.equals("columnLong")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$columnLong((long) reader.nextLong());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'columnLong' to null.");
                }
            } else if (name.equals("columnFloat")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$columnFloat((float) reader.nextDouble());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'columnFloat' to null.");
                }
            } else if (name.equals("columnDouble")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$columnDouble((double) reader.nextDouble());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'columnDouble' to null.");
                }
            } else if (name.equals("columnBoolean")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$columnBoolean((boolean) reader.nextBoolean());
                } else {
                    reader.skipValue();
                    throw new IllegalArgumentException("Trying to set non-nullable field 'columnBoolean' to null.");
                }
            } else if (name.equals("columnDecimal128")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$columnDecimal128(null);
                } else {
                    objProxy.realmSet$columnDecimal128(org.bson.types.Decimal128.parse(reader.nextString()));
                }
            } else if (name.equals("columnObjectId")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$columnObjectId(null);
                } else {
                    objProxy.realmSet$columnObjectId(new org.bson.types.ObjectId(reader.nextString()));
                }
            } else if (name.equals("columnUUID")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$columnUUID(null);
                } else {
                    objProxy.realmSet$columnUUID(java.util.UUID.fromString(reader.nextString()));
                }
            } else if (name.equals("columnDate")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$columnDate(null);
                } else if (reader.peek() == JsonToken.NUMBER) {
                    long timestamp = reader.nextLong();
                    if (timestamp > -1) {
                        objProxy.realmSet$columnDate(new Date(timestamp));
                    }
                } else {
                    objProxy.realmSet$columnDate(JsonUtils.stringToDate(reader.nextString()));
                }
            } else if (name.equals("columnMixed")) {
            } else if (name.equals("columnBinary")) {
                if (reader.peek() != JsonToken.NULL) {
                    objProxy.realmSet$columnBinary(JsonUtils.stringToBytes(reader.nextString()));
                } else {
                    reader.skipValue();
                    objProxy.realmSet$columnBinary(null);
                }
            } else if (name.equals("columnMutableRealmInteger")) {
                Long val = null;
                if (reader.peek() != JsonToken.NULL) {
                    val = reader.nextLong();
                } else {
                    reader.skipValue();
                }
                objProxy.realmGet$columnMutableRealmInteger().set(val);
            } else if (name.equals("columnObject")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$columnObject(null);
                } else {
                    some.test.AllTypes columnObjectObj = some_test_AllTypesRealmProxy.createUsingJsonStream(realm, reader);
                    objProxy.realmSet$columnObject(columnObjectObj);
                }
            } else if (name.equals("columnRealmList")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$columnRealmList(null);
                } else {
                    objProxy.realmSet$columnRealmList(new RealmList<some.test.AllTypes>());
                    reader.beginArray();
                    while (reader.hasNext()) {
                        some.test.AllTypes item = some_test_AllTypesRealmProxy.createUsingJsonStream(realm, reader);
                        objProxy.realmGet$columnRealmList().add(item);
                    }
                    reader.endArray();
                }
            } else if (name.equals("columnRealmFinalList")) {
                if (reader.peek() == JsonToken.NULL) {
                    reader.skipValue();
                    objProxy.realmSet$columnRealmFinalList(null);
                } else {
                    objProxy.realmSet$columnRealmFinalList(new RealmList<some.test.AllTypes>());
                    reader.beginArray();
                    while (reader.hasNext()) {
                        some.test.AllTypes item = some_test_AllTypesRealmProxy.createUsingJsonStream(realm, reader);
                        objProxy.realmGet$columnRealmFinalList().add(item);
                    }
                    reader.endArray();
                }
            } else if (name.equals("columnStringList")) {
                objProxy.realmSet$columnStringList(ProxyUtils.createRealmListWithJsonStream(java.lang.String.class, reader));
            } else if (name.equals("columnBinaryList")) {
                objProxy.realmSet$columnBinaryList(ProxyUtils.createRealmListWithJsonStream(byte[].class, reader));
            } else if (name.equals("columnBooleanList")) {
                objProxy.realmSet$columnBooleanList(ProxyUtils.createRealmListWithJsonStream(java.lang.Boolean.class, reader));
            } else if (name.equals("columnLongList")) {
                objProxy.realmSet$columnLongList(ProxyUtils.createRealmListWithJsonStream(java.lang.Long.class, reader));
            } else if (name.equals("columnIntegerList")) {
                objProxy.realmSet$columnIntegerList(ProxyUtils.createRealmListWithJsonStream(java.lang.Integer.class, reader));
            } else if (name.equals("columnShortList")) {
                objProxy.realmSet$columnShortList(ProxyUtils.createRealmListWithJsonStream(java.lang.Short.class, reader));
            } else if (name.equals("columnByteList")) {
                objProxy.realmSet$columnByteList(ProxyUtils.createRealmListWithJsonStream(java.lang.Byte.class, reader));
            } else if (name.equals("columnDoubleList")) {
                objProxy.realmSet$columnDoubleList(ProxyUtils.createRealmListWithJsonStream(java.lang.Double.class, reader));
            } else if (name.equals("columnFloatList")) {
                objProxy.realmSet$columnFloatList(ProxyUtils.createRealmListWithJsonStream(java.lang.Float.class, reader));
            } else if (name.equals("columnDateList")) {
                objProxy.realmSet$columnDateList(ProxyUtils.createRealmListWithJsonStream(java.util.Date.class, reader));
            } else if (name.equals("columnDecimal128List")) {
                objProxy.realmSet$columnDecimal128List(ProxyUtils.createRealmListWithJsonStream(org.bson.types.Decimal128.class, reader));
            } else if (name.equals("columnObjectIdList")) {
                objProxy.realmSet$columnObjectIdList(ProxyUtils.createRealmListWithJsonStream(org.bson.types.ObjectId.class, reader));
            } else if (name.equals("columnUUIDList")) {
                objProxy.realmSet$columnUUIDList(ProxyUtils.createRealmListWithJsonStream(java.util.UUID.class, reader));
            } else if (name.equals("columnMixedList")) {
                objProxy.realmSet$columnMixedList(ProxyUtils.createRealmListWithJsonStream(io.realm.Mixed.class, reader));
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        if (!jsonHasPrimaryKey) {
            throw new IllegalArgumentException("JSON object doesn't have the primary key field 'columnString'.");
        }
        return realm.copyToRealm(obj);
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
        builder.addMixed(columnInfo.columnMixedColKey, unmanagedSource.realmGet$columnMixed());
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
        builder.addMixedList(columnInfo.columnMixedListColKey, unmanagedSource.realmGet$columnMixedList());

        // Create the underlying object and cache it before setting any object/objectlist references
        // This will allow us to break any circular dependencies by using the object cache.
        Row row = builder.createNewObject();
        io.realm.some_test_AllTypesRealmProxy managedCopy = newProxyInstance(realm, row);
        cache.put(newObject, managedCopy);

        // Finally add all fields that reference other Realm Objects, either directly or through a list
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

        return managedCopy;
    }

    public static long insert(Realm realm, some.test.AllTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativePtr();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
        long pkColumnKey = columnInfo.columnStringColKey;
        String primaryKeyValue = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnString();
        long objKey = Table.NO_MATCH;
        if (primaryKeyValue == null) {
            objKey = Table.nativeFindFirstNull(tableNativePtr, pkColumnKey);
        } else {
            objKey = Table.nativeFindFirstString(tableNativePtr, pkColumnKey, primaryKeyValue);
        }
        if (objKey == Table.NO_MATCH) {
            objKey = OsObject.createRowWithPrimaryKey(table, pkColumnKey, primaryKeyValue);
        } else {
            Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
        }
        cache.put(object, objKey);
        Table.nativeSetLong(tableNativePtr, columnInfo.columnLongColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnLong(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnFloat(), false);
        Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDouble(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBoolean(), false);
        org.bson.types.Decimal128 realmGet$columnDecimal128 = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDecimal128();
        if (realmGet$columnDecimal128 != null) {
            Table.nativeSetDecimal128(tableNativePtr, columnInfo.columnDecimal128ColKey, objKey, realmGet$columnDecimal128.getLow(), realmGet$columnDecimal128.getHigh(), false);
        }
        org.bson.types.ObjectId realmGet$columnObjectId = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObjectId();
        if (realmGet$columnObjectId != null) {
            Table.nativeSetObjectId(tableNativePtr, columnInfo.columnObjectIdColKey, objKey, realmGet$columnObjectId.toString(), false);
        }
        java.util.UUID realmGet$columnUUID = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnUUID();
        if (realmGet$columnUUID != null) {
            Table.nativeSetUUID(tableNativePtr, columnInfo.columnUUIDColKey, objKey, realmGet$columnUUID.toString(), false);
        }
        java.util.Date realmGet$columnDate = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDate();
        if (realmGet$columnDate != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateColKey, objKey, realmGet$columnDate.getTime(), false);
        }
        io.realm.Mixed realmGet$columnMixed = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMixed();
        if (realmGet$columnMixed != null) {
            Table.nativeSetMixed(tableNativePtr, columnInfo.columnMixedColKey, objKey, realmGet$columnMixed.getNativePtr(), false);
        }
        byte[] realmGet$columnBinary = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBinary();
        if (realmGet$columnBinary != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryColKey, objKey, realmGet$columnBinary, false);
        }
        Long realmGet$columnMutableRealmInteger = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMutableRealmInteger().get();
        if (realmGet$columnMutableRealmInteger != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.columnMutableRealmIntegerColKey, objKey, realmGet$columnMutableRealmInteger.longValue(), false);
        }

        some.test.AllTypes columnObjectObj = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObject();
        if (columnObjectObj != null) {
            Long cachecolumnObject = cache.get(columnObjectObj);
            if (cachecolumnObject == null) {
                cachecolumnObject = some_test_AllTypesRealmProxy.insert(realm, columnObjectObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.columnObjectColKey, objKey, cachecolumnObject, false);
        }

        RealmList<some.test.AllTypes> columnRealmListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnRealmList();
        if (columnRealmListList != null) {
            OsList columnRealmListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnRealmListColKey);
            for (some.test.AllTypes columnRealmListItem : columnRealmListList) {
                Long cacheItemIndexcolumnRealmList = cache.get(columnRealmListItem);
                if (cacheItemIndexcolumnRealmList == null) {
                    cacheItemIndexcolumnRealmList = some_test_AllTypesRealmProxy.insert(realm, columnRealmListItem, cache);
                }
                columnRealmListOsList.addRow(cacheItemIndexcolumnRealmList);
            }
        }

        RealmList<some.test.AllTypes> columnRealmFinalListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnRealmFinalList();
        if (columnRealmFinalListList != null) {
            OsList columnRealmFinalListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnRealmFinalListColKey);
            for (some.test.AllTypes columnRealmFinalListItem : columnRealmFinalListList) {
                Long cacheItemIndexcolumnRealmFinalList = cache.get(columnRealmFinalListItem);
                if (cacheItemIndexcolumnRealmFinalList == null) {
                    cacheItemIndexcolumnRealmFinalList = some_test_AllTypesRealmProxy.insert(realm, columnRealmFinalListItem, cache);
                }
                columnRealmFinalListOsList.addRow(cacheItemIndexcolumnRealmFinalList);
            }
        }

        RealmList<java.lang.String> columnStringListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnStringList();
        if (columnStringListList != null) {
            OsList columnStringListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnStringListColKey);
            for (java.lang.String columnStringListItem : columnStringListList) {
                if (columnStringListItem == null) {
                    columnStringListOsList.addNull();
                } else {
                    columnStringListOsList.addString(columnStringListItem);
                }
            }
        }

        RealmList<byte[]> columnBinaryListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBinaryList();
        if (columnBinaryListList != null) {
            OsList columnBinaryListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnBinaryListColKey);
            for (byte[] columnBinaryListItem : columnBinaryListList) {
                if (columnBinaryListItem == null) {
                    columnBinaryListOsList.addNull();
                } else {
                    columnBinaryListOsList.addBinary(columnBinaryListItem);
                }
            }
        }

        RealmList<java.lang.Boolean> columnBooleanListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBooleanList();
        if (columnBooleanListList != null) {
            OsList columnBooleanListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnBooleanListColKey);
            for (java.lang.Boolean columnBooleanListItem : columnBooleanListList) {
                if (columnBooleanListItem == null) {
                    columnBooleanListOsList.addNull();
                } else {
                    columnBooleanListOsList.addBoolean(columnBooleanListItem);
                }
            }
        }

        RealmList<java.lang.Long> columnLongListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnLongList();
        if (columnLongListList != null) {
            OsList columnLongListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnLongListColKey);
            for (java.lang.Long columnLongListItem : columnLongListList) {
                if (columnLongListItem == null) {
                    columnLongListOsList.addNull();
                } else {
                    columnLongListOsList.addLong(columnLongListItem.longValue());
                }
            }
        }

        RealmList<java.lang.Integer> columnIntegerListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnIntegerList();
        if (columnIntegerListList != null) {
            OsList columnIntegerListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnIntegerListColKey);
            for (java.lang.Integer columnIntegerListItem : columnIntegerListList) {
                if (columnIntegerListItem == null) {
                    columnIntegerListOsList.addNull();
                } else {
                    columnIntegerListOsList.addLong(columnIntegerListItem.longValue());
                }
            }
        }

        RealmList<java.lang.Short> columnShortListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnShortList();
        if (columnShortListList != null) {
            OsList columnShortListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnShortListColKey);
            for (java.lang.Short columnShortListItem : columnShortListList) {
                if (columnShortListItem == null) {
                    columnShortListOsList.addNull();
                } else {
                    columnShortListOsList.addLong(columnShortListItem.longValue());
                }
            }
        }

        RealmList<java.lang.Byte> columnByteListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnByteList();
        if (columnByteListList != null) {
            OsList columnByteListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnByteListColKey);
            for (java.lang.Byte columnByteListItem : columnByteListList) {
                if (columnByteListItem == null) {
                    columnByteListOsList.addNull();
                } else {
                    columnByteListOsList.addLong(columnByteListItem.longValue());
                }
            }
        }

        RealmList<java.lang.Double> columnDoubleListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDoubleList();
        if (columnDoubleListList != null) {
            OsList columnDoubleListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnDoubleListColKey);
            for (java.lang.Double columnDoubleListItem : columnDoubleListList) {
                if (columnDoubleListItem == null) {
                    columnDoubleListOsList.addNull();
                } else {
                    columnDoubleListOsList.addDouble(columnDoubleListItem.doubleValue());
                }
            }
        }

        RealmList<java.lang.Float> columnFloatListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnFloatList();
        if (columnFloatListList != null) {
            OsList columnFloatListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnFloatListColKey);
            for (java.lang.Float columnFloatListItem : columnFloatListList) {
                if (columnFloatListItem == null) {
                    columnFloatListOsList.addNull();
                } else {
                    columnFloatListOsList.addFloat(columnFloatListItem.floatValue());
                }
            }
        }

        RealmList<java.util.Date> columnDateListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDateList();
        if (columnDateListList != null) {
            OsList columnDateListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnDateListColKey);
            for (java.util.Date columnDateListItem : columnDateListList) {
                if (columnDateListItem == null) {
                    columnDateListOsList.addNull();
                } else {
                    columnDateListOsList.addDate(columnDateListItem);
                }
            }
        }

        RealmList<org.bson.types.Decimal128> columnDecimal128ListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDecimal128List();
        if (columnDecimal128ListList != null) {
            OsList columnDecimal128ListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnDecimal128ListColKey);
            for (org.bson.types.Decimal128 columnDecimal128ListItem : columnDecimal128ListList) {
                if (columnDecimal128ListItem == null) {
                    columnDecimal128ListOsList.addNull();
                } else {
                    columnDecimal128ListOsList.addDecimal128(columnDecimal128ListItem);
                }
            }
        }

        RealmList<org.bson.types.ObjectId> columnObjectIdListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObjectIdList();
        if (columnObjectIdListList != null) {
            OsList columnObjectIdListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnObjectIdListColKey);
            for (org.bson.types.ObjectId columnObjectIdListItem : columnObjectIdListList) {
                if (columnObjectIdListItem == null) {
                    columnObjectIdListOsList.addNull();
                } else {
                    columnObjectIdListOsList.addObjectId(columnObjectIdListItem);
                }
            }
        }

        RealmList<java.util.UUID> columnUUIDListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnUUIDList();
        if (columnUUIDListList != null) {
            OsList columnUUIDListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnUUIDListColKey);
            for (java.util.UUID columnUUIDListItem : columnUUIDListList) {
                if (columnUUIDListItem == null) {
                    columnUUIDListOsList.addNull();
                } else {
                    columnUUIDListOsList.addUUID(columnUUIDListItem);
                }
            }
        }

        RealmList<io.realm.Mixed> columnMixedListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMixedList();
        if (columnMixedListList != null) {
            OsList columnMixedListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnMixedListColKey);
            for (io.realm.Mixed columnMixedListItem : columnMixedListList) {
                if (columnMixedListItem == null) {
                    columnMixedListOsList.addNull();
                } else {
                    columnMixedListOsList.addMixed(columnMixedListItem.getNativePtr());
                }
            }
        }
        return objKey;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativePtr();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
        long pkColumnKey = columnInfo.columnStringColKey;
        some.test.AllTypes object = null;
        while (objects.hasNext()) {
            object = (some.test.AllTypes) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            String primaryKeyValue = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnString();
            long objKey = Table.NO_MATCH;
            if (primaryKeyValue == null) {
                objKey = Table.nativeFindFirstNull(tableNativePtr, pkColumnKey);
            } else {
                objKey = Table.nativeFindFirstString(tableNativePtr, pkColumnKey, primaryKeyValue);
            }
            if (objKey == Table.NO_MATCH) {
                objKey = OsObject.createRowWithPrimaryKey(table, pkColumnKey, primaryKeyValue);
            } else {
                Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
            }
            cache.put(object, objKey);
            Table.nativeSetLong(tableNativePtr, columnInfo.columnLongColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnLong(), false);
            Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnFloat(), false);
            Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDouble(), false);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBoolean(), false);
            org.bson.types.Decimal128 realmGet$columnDecimal128 = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDecimal128();
            if (realmGet$columnDecimal128 != null) {
                Table.nativeSetDecimal128(tableNativePtr, columnInfo.columnDecimal128ColKey, objKey, realmGet$columnDecimal128.getLow(), realmGet$columnDecimal128.getHigh(), false);
            }
            org.bson.types.ObjectId realmGet$columnObjectId = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObjectId();
            if (realmGet$columnObjectId != null) {
                Table.nativeSetObjectId(tableNativePtr, columnInfo.columnObjectIdColKey, objKey, realmGet$columnObjectId.toString(), false);
            }
            java.util.UUID realmGet$columnUUID = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnUUID();
            if (realmGet$columnUUID != null) {
                Table.nativeSetUUID(tableNativePtr, columnInfo.columnUUIDColKey, objKey, realmGet$columnUUID.toString(), false);
            }
            java.util.Date realmGet$columnDate = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDate();
            if (realmGet$columnDate != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateColKey, objKey, realmGet$columnDate.getTime(), false);
            }
            io.realm.Mixed realmGet$columnMixed = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMixed();
            if (realmGet$columnMixed != null) {
                Table.nativeSetMixed(tableNativePtr, columnInfo.columnMixedColKey, objKey, realmGet$columnMixed.getNativePtr(), false);
            }
            byte[] realmGet$columnBinary = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBinary();
            if (realmGet$columnBinary != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryColKey, objKey, realmGet$columnBinary, false);
            }
            Long realmGet$columnMutableRealmInteger = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMutableRealmInteger().get();
            if (realmGet$columnMutableRealmInteger != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.columnMutableRealmIntegerColKey, objKey, realmGet$columnMutableRealmInteger.longValue(), false);
            }

            some.test.AllTypes columnObjectObj = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObject();
            if (columnObjectObj != null) {
                Long cachecolumnObject = cache.get(columnObjectObj);
                if (cachecolumnObject == null) {
                    cachecolumnObject = some_test_AllTypesRealmProxy.insert(realm, columnObjectObj, cache);
                }
                table.setLink(columnInfo.columnObjectColKey, objKey, cachecolumnObject, false);
            }

            RealmList<some.test.AllTypes> columnRealmListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnRealmList();
            if (columnRealmListList != null) {
                OsList columnRealmListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnRealmListColKey);
                for (some.test.AllTypes columnRealmListItem : columnRealmListList) {
                    Long cacheItemIndexcolumnRealmList = cache.get(columnRealmListItem);
                    if (cacheItemIndexcolumnRealmList == null) {
                        cacheItemIndexcolumnRealmList = some_test_AllTypesRealmProxy.insert(realm, columnRealmListItem, cache);
                    }
                    columnRealmListOsList.addRow(cacheItemIndexcolumnRealmList);
                }
            }

            RealmList<some.test.AllTypes> columnRealmFinalListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnRealmFinalList();
            if (columnRealmFinalListList != null) {
                OsList columnRealmFinalListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnRealmFinalListColKey);
                for (some.test.AllTypes columnRealmFinalListItem : columnRealmFinalListList) {
                    Long cacheItemIndexcolumnRealmFinalList = cache.get(columnRealmFinalListItem);
                    if (cacheItemIndexcolumnRealmFinalList == null) {
                        cacheItemIndexcolumnRealmFinalList = some_test_AllTypesRealmProxy.insert(realm, columnRealmFinalListItem, cache);
                    }
                    columnRealmFinalListOsList.addRow(cacheItemIndexcolumnRealmFinalList);
                }
            }

            RealmList<java.lang.String> columnStringListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnStringList();
            if (columnStringListList != null) {
                OsList columnStringListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnStringListColKey);
                for (java.lang.String columnStringListItem : columnStringListList) {
                    if (columnStringListItem == null) {
                        columnStringListOsList.addNull();
                    } else {
                        columnStringListOsList.addString(columnStringListItem);
                    }
                }
            }

            RealmList<byte[]> columnBinaryListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBinaryList();
            if (columnBinaryListList != null) {
                OsList columnBinaryListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnBinaryListColKey);
                for (byte[] columnBinaryListItem : columnBinaryListList) {
                    if (columnBinaryListItem == null) {
                        columnBinaryListOsList.addNull();
                    } else {
                        columnBinaryListOsList.addBinary(columnBinaryListItem);
                    }
                }
            }

            RealmList<java.lang.Boolean> columnBooleanListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBooleanList();
            if (columnBooleanListList != null) {
                OsList columnBooleanListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnBooleanListColKey);
                for (java.lang.Boolean columnBooleanListItem : columnBooleanListList) {
                    if (columnBooleanListItem == null) {
                        columnBooleanListOsList.addNull();
                    } else {
                        columnBooleanListOsList.addBoolean(columnBooleanListItem);
                    }
                }
            }

            RealmList<java.lang.Long> columnLongListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnLongList();
            if (columnLongListList != null) {
                OsList columnLongListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnLongListColKey);
                for (java.lang.Long columnLongListItem : columnLongListList) {
                    if (columnLongListItem == null) {
                        columnLongListOsList.addNull();
                    } else {
                        columnLongListOsList.addLong(columnLongListItem.longValue());
                    }
                }
            }

            RealmList<java.lang.Integer> columnIntegerListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnIntegerList();
            if (columnIntegerListList != null) {
                OsList columnIntegerListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnIntegerListColKey);
                for (java.lang.Integer columnIntegerListItem : columnIntegerListList) {
                    if (columnIntegerListItem == null) {
                        columnIntegerListOsList.addNull();
                    } else {
                        columnIntegerListOsList.addLong(columnIntegerListItem.longValue());
                    }
                }
            }

            RealmList<java.lang.Short> columnShortListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnShortList();
            if (columnShortListList != null) {
                OsList columnShortListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnShortListColKey);
                for (java.lang.Short columnShortListItem : columnShortListList) {
                    if (columnShortListItem == null) {
                        columnShortListOsList.addNull();
                    } else {
                        columnShortListOsList.addLong(columnShortListItem.longValue());
                    }
                }
            }

            RealmList<java.lang.Byte> columnByteListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnByteList();
            if (columnByteListList != null) {
                OsList columnByteListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnByteListColKey);
                for (java.lang.Byte columnByteListItem : columnByteListList) {
                    if (columnByteListItem == null) {
                        columnByteListOsList.addNull();
                    } else {
                        columnByteListOsList.addLong(columnByteListItem.longValue());
                    }
                }
            }

            RealmList<java.lang.Double> columnDoubleListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDoubleList();
            if (columnDoubleListList != null) {
                OsList columnDoubleListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnDoubleListColKey);
                for (java.lang.Double columnDoubleListItem : columnDoubleListList) {
                    if (columnDoubleListItem == null) {
                        columnDoubleListOsList.addNull();
                    } else {
                        columnDoubleListOsList.addDouble(columnDoubleListItem.doubleValue());
                    }
                }
            }

            RealmList<java.lang.Float> columnFloatListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnFloatList();
            if (columnFloatListList != null) {
                OsList columnFloatListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnFloatListColKey);
                for (java.lang.Float columnFloatListItem : columnFloatListList) {
                    if (columnFloatListItem == null) {
                        columnFloatListOsList.addNull();
                    } else {
                        columnFloatListOsList.addFloat(columnFloatListItem.floatValue());
                    }
                }
            }

            RealmList<java.util.Date> columnDateListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDateList();
            if (columnDateListList != null) {
                OsList columnDateListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnDateListColKey);
                for (java.util.Date columnDateListItem : columnDateListList) {
                    if (columnDateListItem == null) {
                        columnDateListOsList.addNull();
                    } else {
                        columnDateListOsList.addDate(columnDateListItem);
                    }
                }
            }

            RealmList<org.bson.types.Decimal128> columnDecimal128ListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDecimal128List();
            if (columnDecimal128ListList != null) {
                OsList columnDecimal128ListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnDecimal128ListColKey);
                for (org.bson.types.Decimal128 columnDecimal128ListItem : columnDecimal128ListList) {
                    if (columnDecimal128ListItem == null) {
                        columnDecimal128ListOsList.addNull();
                    } else {
                        columnDecimal128ListOsList.addDecimal128(columnDecimal128ListItem);
                    }
                }
            }

            RealmList<org.bson.types.ObjectId> columnObjectIdListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObjectIdList();
            if (columnObjectIdListList != null) {
                OsList columnObjectIdListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnObjectIdListColKey);
                for (org.bson.types.ObjectId columnObjectIdListItem : columnObjectIdListList) {
                    if (columnObjectIdListItem == null) {
                        columnObjectIdListOsList.addNull();
                    } else {
                        columnObjectIdListOsList.addObjectId(columnObjectIdListItem);
                    }
                }
            }

            RealmList<java.util.UUID> columnUUIDListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnUUIDList();
            if (columnUUIDListList != null) {
                OsList columnUUIDListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnUUIDListColKey);
                for (java.util.UUID columnUUIDListItem : columnUUIDListList) {
                    if (columnUUIDListItem == null) {
                        columnUUIDListOsList.addNull();
                    } else {
                        columnUUIDListOsList.addUUID(columnUUIDListItem);
                    }
                }
            }

            RealmList<io.realm.Mixed> columnMixedListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMixedList();
            if (columnMixedListList != null) {
                OsList columnMixedListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnMixedListColKey);
                for (io.realm.Mixed columnMixedListItem : columnMixedListList) {
                    if (columnMixedListItem == null) {
                        columnMixedListOsList.addNull();
                    } else {
                        columnMixedListOsList.addMixed(columnMixedListItem.getNativePtr());
                    }
                }
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.AllTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey();
        }
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativePtr();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
        long pkColumnKey = columnInfo.columnStringColKey;
        String primaryKeyValue = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnString();
        long objKey = Table.NO_MATCH;
        if (primaryKeyValue == null) {
            objKey = Table.nativeFindFirstNull(tableNativePtr, pkColumnKey);
        } else {
            objKey = Table.nativeFindFirstString(tableNativePtr, pkColumnKey, primaryKeyValue);
        }
        if (objKey == Table.NO_MATCH) {
            objKey = OsObject.createRowWithPrimaryKey(table, pkColumnKey, primaryKeyValue);
        }
        cache.put(object, objKey);
        Table.nativeSetLong(tableNativePtr, columnInfo.columnLongColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnLong(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnFloat(), false);
        Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDouble(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBoolean(), false);
        org.bson.types.Decimal128 realmGet$columnDecimal128 = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDecimal128();
        if (realmGet$columnDecimal128 != null) {
            Table.nativeSetDecimal128(tableNativePtr, columnInfo.columnDecimal128ColKey, objKey, realmGet$columnDecimal128.getLow(), realmGet$columnDecimal128.getHigh(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnDecimal128ColKey, objKey, false);
        }
        org.bson.types.ObjectId realmGet$columnObjectId = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObjectId();
        if (realmGet$columnObjectId != null) {
            Table.nativeSetObjectId(tableNativePtr, columnInfo.columnObjectIdColKey, objKey, realmGet$columnObjectId.toString(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnObjectIdColKey, objKey, false);
        }
        java.util.UUID realmGet$columnUUID = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnUUID();
        if (realmGet$columnUUID != null) {
            Table.nativeSetUUID(tableNativePtr, columnInfo.columnUUIDColKey, objKey, realmGet$columnUUID.toString(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnUUIDColKey, objKey, false);
        }
        java.util.Date realmGet$columnDate = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDate();
        if (realmGet$columnDate != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateColKey, objKey, realmGet$columnDate.getTime(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnDateColKey, objKey, false);
        }
        io.realm.Mixed realmGet$columnMixed = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMixed();
        if (realmGet$columnMixed != null) {
            Table.nativeSetMixed(tableNativePtr, columnInfo.columnMixedColKey, objKey, realmGet$columnMixed.getNativePtr(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnMixedColKey, objKey, false);
        }
        byte[] realmGet$columnBinary = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBinary();
        if (realmGet$columnBinary != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryColKey, objKey, realmGet$columnBinary, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnBinaryColKey, objKey, false);
        }
        Long realmGet$columnMutableRealmInteger = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMutableRealmInteger().get();
        if (realmGet$columnMutableRealmInteger != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.columnMutableRealmIntegerColKey, objKey, realmGet$columnMutableRealmInteger.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnMutableRealmIntegerColKey, objKey, false);
        }

        some.test.AllTypes columnObjectObj = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObject();
        if (columnObjectObj != null) {
            Long cachecolumnObject = cache.get(columnObjectObj);
            if (cachecolumnObject == null) {
                cachecolumnObject = some_test_AllTypesRealmProxy.insertOrUpdate(realm, columnObjectObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.columnObjectColKey, objKey, cachecolumnObject, false);
        } else {
            Table.nativeNullifyLink(tableNativePtr, columnInfo.columnObjectColKey, objKey);
        }

        OsList columnRealmListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnRealmListColKey);
        RealmList<some.test.AllTypes> columnRealmListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnRealmList();
        if (columnRealmListList != null && columnRealmListList.size() == columnRealmListOsList.size()) {
            // For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.
            int objects = columnRealmListList.size();
            for (int i = 0; i < objects; i++) {
                some.test.AllTypes columnRealmListItem = columnRealmListList.get(i);
                Long cacheItemIndexcolumnRealmList = cache.get(columnRealmListItem);
                if (cacheItemIndexcolumnRealmList == null) {
                    cacheItemIndexcolumnRealmList = some_test_AllTypesRealmProxy.insertOrUpdate(realm, columnRealmListItem, cache);
                }
                columnRealmListOsList.setRow(i, cacheItemIndexcolumnRealmList);
            }
        } else {
            columnRealmListOsList.removeAll();
            if (columnRealmListList != null) {
                for (some.test.AllTypes columnRealmListItem : columnRealmListList) {
                    Long cacheItemIndexcolumnRealmList = cache.get(columnRealmListItem);
                    if (cacheItemIndexcolumnRealmList == null) {
                        cacheItemIndexcolumnRealmList = some_test_AllTypesRealmProxy.insertOrUpdate(realm, columnRealmListItem, cache);
                    }
                    columnRealmListOsList.addRow(cacheItemIndexcolumnRealmList);
                }
            }
        }


        OsList columnRealmFinalListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnRealmFinalListColKey);
        RealmList<some.test.AllTypes> columnRealmFinalListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnRealmFinalList();
        if (columnRealmFinalListList != null && columnRealmFinalListList.size() == columnRealmFinalListOsList.size()) {
            // For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.
            int objects = columnRealmFinalListList.size();
            for (int i = 0; i < objects; i++) {
                some.test.AllTypes columnRealmFinalListItem = columnRealmFinalListList.get(i);
                Long cacheItemIndexcolumnRealmFinalList = cache.get(columnRealmFinalListItem);
                if (cacheItemIndexcolumnRealmFinalList == null) {
                    cacheItemIndexcolumnRealmFinalList = some_test_AllTypesRealmProxy.insertOrUpdate(realm, columnRealmFinalListItem, cache);
                }
                columnRealmFinalListOsList.setRow(i, cacheItemIndexcolumnRealmFinalList);
            }
        } else {
            columnRealmFinalListOsList.removeAll();
            if (columnRealmFinalListList != null) {
                for (some.test.AllTypes columnRealmFinalListItem : columnRealmFinalListList) {
                    Long cacheItemIndexcolumnRealmFinalList = cache.get(columnRealmFinalListItem);
                    if (cacheItemIndexcolumnRealmFinalList == null) {
                        cacheItemIndexcolumnRealmFinalList = some_test_AllTypesRealmProxy.insertOrUpdate(realm, columnRealmFinalListItem, cache);
                    }
                    columnRealmFinalListOsList.addRow(cacheItemIndexcolumnRealmFinalList);
                }
            }
        }


        OsList columnStringListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnStringListColKey);
        columnStringListOsList.removeAll();
        RealmList<java.lang.String> columnStringListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnStringList();
        if (columnStringListList != null) {
            for (java.lang.String columnStringListItem : columnStringListList) {
                if (columnStringListItem == null) {
                    columnStringListOsList.addNull();
                } else {
                    columnStringListOsList.addString(columnStringListItem);
                }
            }
        }


        OsList columnBinaryListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnBinaryListColKey);
        columnBinaryListOsList.removeAll();
        RealmList<byte[]> columnBinaryListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBinaryList();
        if (columnBinaryListList != null) {
            for (byte[] columnBinaryListItem : columnBinaryListList) {
                if (columnBinaryListItem == null) {
                    columnBinaryListOsList.addNull();
                } else {
                    columnBinaryListOsList.addBinary(columnBinaryListItem);
                }
            }
        }


        OsList columnBooleanListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnBooleanListColKey);
        columnBooleanListOsList.removeAll();
        RealmList<java.lang.Boolean> columnBooleanListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBooleanList();
        if (columnBooleanListList != null) {
            for (java.lang.Boolean columnBooleanListItem : columnBooleanListList) {
                if (columnBooleanListItem == null) {
                    columnBooleanListOsList.addNull();
                } else {
                    columnBooleanListOsList.addBoolean(columnBooleanListItem);
                }
            }
        }


        OsList columnLongListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnLongListColKey);
        columnLongListOsList.removeAll();
        RealmList<java.lang.Long> columnLongListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnLongList();
        if (columnLongListList != null) {
            for (java.lang.Long columnLongListItem : columnLongListList) {
                if (columnLongListItem == null) {
                    columnLongListOsList.addNull();
                } else {
                    columnLongListOsList.addLong(columnLongListItem.longValue());
                }
            }
        }


        OsList columnIntegerListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnIntegerListColKey);
        columnIntegerListOsList.removeAll();
        RealmList<java.lang.Integer> columnIntegerListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnIntegerList();
        if (columnIntegerListList != null) {
            for (java.lang.Integer columnIntegerListItem : columnIntegerListList) {
                if (columnIntegerListItem == null) {
                    columnIntegerListOsList.addNull();
                } else {
                    columnIntegerListOsList.addLong(columnIntegerListItem.longValue());
                }
            }
        }


        OsList columnShortListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnShortListColKey);
        columnShortListOsList.removeAll();
        RealmList<java.lang.Short> columnShortListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnShortList();
        if (columnShortListList != null) {
            for (java.lang.Short columnShortListItem : columnShortListList) {
                if (columnShortListItem == null) {
                    columnShortListOsList.addNull();
                } else {
                    columnShortListOsList.addLong(columnShortListItem.longValue());
                }
            }
        }


        OsList columnByteListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnByteListColKey);
        columnByteListOsList.removeAll();
        RealmList<java.lang.Byte> columnByteListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnByteList();
        if (columnByteListList != null) {
            for (java.lang.Byte columnByteListItem : columnByteListList) {
                if (columnByteListItem == null) {
                    columnByteListOsList.addNull();
                } else {
                    columnByteListOsList.addLong(columnByteListItem.longValue());
                }
            }
        }


        OsList columnDoubleListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnDoubleListColKey);
        columnDoubleListOsList.removeAll();
        RealmList<java.lang.Double> columnDoubleListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDoubleList();
        if (columnDoubleListList != null) {
            for (java.lang.Double columnDoubleListItem : columnDoubleListList) {
                if (columnDoubleListItem == null) {
                    columnDoubleListOsList.addNull();
                } else {
                    columnDoubleListOsList.addDouble(columnDoubleListItem.doubleValue());
                }
            }
        }


        OsList columnFloatListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnFloatListColKey);
        columnFloatListOsList.removeAll();
        RealmList<java.lang.Float> columnFloatListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnFloatList();
        if (columnFloatListList != null) {
            for (java.lang.Float columnFloatListItem : columnFloatListList) {
                if (columnFloatListItem == null) {
                    columnFloatListOsList.addNull();
                } else {
                    columnFloatListOsList.addFloat(columnFloatListItem.floatValue());
                }
            }
        }


        OsList columnDateListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnDateListColKey);
        columnDateListOsList.removeAll();
        RealmList<java.util.Date> columnDateListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDateList();
        if (columnDateListList != null) {
            for (java.util.Date columnDateListItem : columnDateListList) {
                if (columnDateListItem == null) {
                    columnDateListOsList.addNull();
                } else {
                    columnDateListOsList.addDate(columnDateListItem);
                }
            }
        }


        OsList columnDecimal128ListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnDecimal128ListColKey);
        columnDecimal128ListOsList.removeAll();
        RealmList<org.bson.types.Decimal128> columnDecimal128ListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDecimal128List();
        if (columnDecimal128ListList != null) {
            for (org.bson.types.Decimal128 columnDecimal128ListItem : columnDecimal128ListList) {
                if (columnDecimal128ListItem == null) {
                    columnDecimal128ListOsList.addNull();
                } else {
                    columnDecimal128ListOsList.addDecimal128(columnDecimal128ListItem);
                }
            }
        }


        OsList columnObjectIdListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnObjectIdListColKey);
        columnObjectIdListOsList.removeAll();
        RealmList<org.bson.types.ObjectId> columnObjectIdListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObjectIdList();
        if (columnObjectIdListList != null) {
            for (org.bson.types.ObjectId columnObjectIdListItem : columnObjectIdListList) {
                if (columnObjectIdListItem == null) {
                    columnObjectIdListOsList.addNull();
                } else {
                    columnObjectIdListOsList.addObjectId(columnObjectIdListItem);
                }
            }
        }


        OsList columnUUIDListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnUUIDListColKey);
        columnUUIDListOsList.removeAll();
        RealmList<java.util.UUID> columnUUIDListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnUUIDList();
        if (columnUUIDListList != null) {
            for (java.util.UUID columnUUIDListItem : columnUUIDListList) {
                if (columnUUIDListItem == null) {
                    columnUUIDListOsList.addNull();
                } else {
                    columnUUIDListOsList.addUUID(columnUUIDListItem);
                }
            }
        }


        OsList columnMixedListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnMixedListColKey);
        columnMixedListOsList.removeAll();
        RealmList<io.realm.Mixed> columnMixedListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMixedList();
        if (columnMixedListList != null) {
            for (io.realm.Mixed columnMixedListItem : columnMixedListList) {
                if (columnMixedListItem == null) {
                    columnMixedListOsList.addNull();
                } else {
                    columnMixedListOsList.addMixed(columnMixedListItem.getNativePtr());
                }
            }
        }

        return objKey;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativePtr();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
        long pkColumnKey = columnInfo.columnStringColKey;
        some.test.AllTypes object = null;
        while (objects.hasNext()) {
            object = (some.test.AllTypes) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && !RealmObject.isFrozen(object) && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getObjectKey());
                continue;
            }
            String primaryKeyValue = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnString();
            long objKey = Table.NO_MATCH;
            if (primaryKeyValue == null) {
                objKey = Table.nativeFindFirstNull(tableNativePtr, pkColumnKey);
            } else {
                objKey = Table.nativeFindFirstString(tableNativePtr, pkColumnKey, primaryKeyValue);
            }
            if (objKey == Table.NO_MATCH) {
                objKey = OsObject.createRowWithPrimaryKey(table, pkColumnKey, primaryKeyValue);
            }
            cache.put(object, objKey);
            Table.nativeSetLong(tableNativePtr, columnInfo.columnLongColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnLong(), false);
            Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnFloat(), false);
            Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDouble(), false);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanColKey, objKey, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBoolean(), false);
            org.bson.types.Decimal128 realmGet$columnDecimal128 = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDecimal128();
            if (realmGet$columnDecimal128 != null) {
                Table.nativeSetDecimal128(tableNativePtr, columnInfo.columnDecimal128ColKey, objKey, realmGet$columnDecimal128.getLow(), realmGet$columnDecimal128.getHigh(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnDecimal128ColKey, objKey, false);
            }
            org.bson.types.ObjectId realmGet$columnObjectId = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObjectId();
            if (realmGet$columnObjectId != null) {
                Table.nativeSetObjectId(tableNativePtr, columnInfo.columnObjectIdColKey, objKey, realmGet$columnObjectId.toString(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnObjectIdColKey, objKey, false);
            }
            java.util.UUID realmGet$columnUUID = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnUUID();
            if (realmGet$columnUUID != null) {
                Table.nativeSetUUID(tableNativePtr, columnInfo.columnUUIDColKey, objKey, realmGet$columnUUID.toString(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnUUIDColKey, objKey, false);
            }
            java.util.Date realmGet$columnDate = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDate();
            if (realmGet$columnDate != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateColKey, objKey, realmGet$columnDate.getTime(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnDateColKey, objKey, false);
            }
            io.realm.Mixed realmGet$columnMixed = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMixed();
            if (realmGet$columnMixed != null) {
                Table.nativeSetMixed(tableNativePtr, columnInfo.columnMixedColKey, objKey, realmGet$columnMixed.getNativePtr(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnMixedColKey, objKey, false);
            }
            byte[] realmGet$columnBinary = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBinary();
            if (realmGet$columnBinary != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryColKey, objKey, realmGet$columnBinary, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnBinaryColKey, objKey, false);
            }
            Long realmGet$columnMutableRealmInteger = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMutableRealmInteger().get();
            if (realmGet$columnMutableRealmInteger != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.columnMutableRealmIntegerColKey, objKey, realmGet$columnMutableRealmInteger.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnMutableRealmIntegerColKey, objKey, false);
            }

            some.test.AllTypes columnObjectObj = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObject();
            if (columnObjectObj != null) {
                Long cachecolumnObject = cache.get(columnObjectObj);
                if (cachecolumnObject == null) {
                    cachecolumnObject = some_test_AllTypesRealmProxy.insertOrUpdate(realm, columnObjectObj, cache);
                }
                Table.nativeSetLink(tableNativePtr, columnInfo.columnObjectColKey, objKey, cachecolumnObject, false);
            } else {
                Table.nativeNullifyLink(tableNativePtr, columnInfo.columnObjectColKey, objKey);
            }

            OsList columnRealmListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnRealmListColKey);
            RealmList<some.test.AllTypes> columnRealmListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnRealmList();
            if (columnRealmListList != null && columnRealmListList.size() == columnRealmListOsList.size()) {
                // For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.
                int objectCount = columnRealmListList.size();
                for (int i = 0; i < objectCount; i++) {
                    some.test.AllTypes columnRealmListItem = columnRealmListList.get(i);
                    Long cacheItemIndexcolumnRealmList = cache.get(columnRealmListItem);
                    if (cacheItemIndexcolumnRealmList == null) {
                        cacheItemIndexcolumnRealmList = some_test_AllTypesRealmProxy.insertOrUpdate(realm, columnRealmListItem, cache);
                    }
                    columnRealmListOsList.setRow(i, cacheItemIndexcolumnRealmList);
                }
            } else {
                columnRealmListOsList.removeAll();
                if (columnRealmListList != null) {
                    for (some.test.AllTypes columnRealmListItem : columnRealmListList) {
                        Long cacheItemIndexcolumnRealmList = cache.get(columnRealmListItem);
                        if (cacheItemIndexcolumnRealmList == null) {
                            cacheItemIndexcolumnRealmList = some_test_AllTypesRealmProxy.insertOrUpdate(realm, columnRealmListItem, cache);
                        }
                        columnRealmListOsList.addRow(cacheItemIndexcolumnRealmList);
                    }
                }
            }


            OsList columnRealmFinalListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnRealmFinalListColKey);
            RealmList<some.test.AllTypes> columnRealmFinalListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnRealmFinalList();
            if (columnRealmFinalListList != null && columnRealmFinalListList.size() == columnRealmFinalListOsList.size()) {
                // For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.
                int objectCount = columnRealmFinalListList.size();
                for (int i = 0; i < objectCount; i++) {
                    some.test.AllTypes columnRealmFinalListItem = columnRealmFinalListList.get(i);
                    Long cacheItemIndexcolumnRealmFinalList = cache.get(columnRealmFinalListItem);
                    if (cacheItemIndexcolumnRealmFinalList == null) {
                        cacheItemIndexcolumnRealmFinalList = some_test_AllTypesRealmProxy.insertOrUpdate(realm, columnRealmFinalListItem, cache);
                    }
                    columnRealmFinalListOsList.setRow(i, cacheItemIndexcolumnRealmFinalList);
                }
            } else {
                columnRealmFinalListOsList.removeAll();
                if (columnRealmFinalListList != null) {
                    for (some.test.AllTypes columnRealmFinalListItem : columnRealmFinalListList) {
                        Long cacheItemIndexcolumnRealmFinalList = cache.get(columnRealmFinalListItem);
                        if (cacheItemIndexcolumnRealmFinalList == null) {
                            cacheItemIndexcolumnRealmFinalList = some_test_AllTypesRealmProxy.insertOrUpdate(realm, columnRealmFinalListItem, cache);
                        }
                        columnRealmFinalListOsList.addRow(cacheItemIndexcolumnRealmFinalList);
                    }
                }
            }


            OsList columnStringListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnStringListColKey);
            columnStringListOsList.removeAll();
            RealmList<java.lang.String> columnStringListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnStringList();
            if (columnStringListList != null) {
                for (java.lang.String columnStringListItem : columnStringListList) {
                    if (columnStringListItem == null) {
                        columnStringListOsList.addNull();
                    } else {
                        columnStringListOsList.addString(columnStringListItem);
                    }
                }
            }


            OsList columnBinaryListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnBinaryListColKey);
            columnBinaryListOsList.removeAll();
            RealmList<byte[]> columnBinaryListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBinaryList();
            if (columnBinaryListList != null) {
                for (byte[] columnBinaryListItem : columnBinaryListList) {
                    if (columnBinaryListItem == null) {
                        columnBinaryListOsList.addNull();
                    } else {
                        columnBinaryListOsList.addBinary(columnBinaryListItem);
                    }
                }
            }


            OsList columnBooleanListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnBooleanListColKey);
            columnBooleanListOsList.removeAll();
            RealmList<java.lang.Boolean> columnBooleanListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBooleanList();
            if (columnBooleanListList != null) {
                for (java.lang.Boolean columnBooleanListItem : columnBooleanListList) {
                    if (columnBooleanListItem == null) {
                        columnBooleanListOsList.addNull();
                    } else {
                        columnBooleanListOsList.addBoolean(columnBooleanListItem);
                    }
                }
            }


            OsList columnLongListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnLongListColKey);
            columnLongListOsList.removeAll();
            RealmList<java.lang.Long> columnLongListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnLongList();
            if (columnLongListList != null) {
                for (java.lang.Long columnLongListItem : columnLongListList) {
                    if (columnLongListItem == null) {
                        columnLongListOsList.addNull();
                    } else {
                        columnLongListOsList.addLong(columnLongListItem.longValue());
                    }
                }
            }


            OsList columnIntegerListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnIntegerListColKey);
            columnIntegerListOsList.removeAll();
            RealmList<java.lang.Integer> columnIntegerListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnIntegerList();
            if (columnIntegerListList != null) {
                for (java.lang.Integer columnIntegerListItem : columnIntegerListList) {
                    if (columnIntegerListItem == null) {
                        columnIntegerListOsList.addNull();
                    } else {
                        columnIntegerListOsList.addLong(columnIntegerListItem.longValue());
                    }
                }
            }


            OsList columnShortListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnShortListColKey);
            columnShortListOsList.removeAll();
            RealmList<java.lang.Short> columnShortListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnShortList();
            if (columnShortListList != null) {
                for (java.lang.Short columnShortListItem : columnShortListList) {
                    if (columnShortListItem == null) {
                        columnShortListOsList.addNull();
                    } else {
                        columnShortListOsList.addLong(columnShortListItem.longValue());
                    }
                }
            }


            OsList columnByteListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnByteListColKey);
            columnByteListOsList.removeAll();
            RealmList<java.lang.Byte> columnByteListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnByteList();
            if (columnByteListList != null) {
                for (java.lang.Byte columnByteListItem : columnByteListList) {
                    if (columnByteListItem == null) {
                        columnByteListOsList.addNull();
                    } else {
                        columnByteListOsList.addLong(columnByteListItem.longValue());
                    }
                }
            }


            OsList columnDoubleListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnDoubleListColKey);
            columnDoubleListOsList.removeAll();
            RealmList<java.lang.Double> columnDoubleListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDoubleList();
            if (columnDoubleListList != null) {
                for (java.lang.Double columnDoubleListItem : columnDoubleListList) {
                    if (columnDoubleListItem == null) {
                        columnDoubleListOsList.addNull();
                    } else {
                        columnDoubleListOsList.addDouble(columnDoubleListItem.doubleValue());
                    }
                }
            }


            OsList columnFloatListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnFloatListColKey);
            columnFloatListOsList.removeAll();
            RealmList<java.lang.Float> columnFloatListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnFloatList();
            if (columnFloatListList != null) {
                for (java.lang.Float columnFloatListItem : columnFloatListList) {
                    if (columnFloatListItem == null) {
                        columnFloatListOsList.addNull();
                    } else {
                        columnFloatListOsList.addFloat(columnFloatListItem.floatValue());
                    }
                }
            }


            OsList columnDateListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnDateListColKey);
            columnDateListOsList.removeAll();
            RealmList<java.util.Date> columnDateListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDateList();
            if (columnDateListList != null) {
                for (java.util.Date columnDateListItem : columnDateListList) {
                    if (columnDateListItem == null) {
                        columnDateListOsList.addNull();
                    } else {
                        columnDateListOsList.addDate(columnDateListItem);
                    }
                }
            }


            OsList columnDecimal128ListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnDecimal128ListColKey);
            columnDecimal128ListOsList.removeAll();
            RealmList<org.bson.types.Decimal128> columnDecimal128ListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDecimal128List();
            if (columnDecimal128ListList != null) {
                for (org.bson.types.Decimal128 columnDecimal128ListItem : columnDecimal128ListList) {
                    if (columnDecimal128ListItem == null) {
                        columnDecimal128ListOsList.addNull();
                    } else {
                        columnDecimal128ListOsList.addDecimal128(columnDecimal128ListItem);
                    }
                }
            }


            OsList columnObjectIdListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnObjectIdListColKey);
            columnObjectIdListOsList.removeAll();
            RealmList<org.bson.types.ObjectId> columnObjectIdListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObjectIdList();
            if (columnObjectIdListList != null) {
                for (org.bson.types.ObjectId columnObjectIdListItem : columnObjectIdListList) {
                    if (columnObjectIdListItem == null) {
                        columnObjectIdListOsList.addNull();
                    } else {
                        columnObjectIdListOsList.addObjectId(columnObjectIdListItem);
                    }
                }
            }


            OsList columnUUIDListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnUUIDListColKey);
            columnUUIDListOsList.removeAll();
            RealmList<java.util.UUID> columnUUIDListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnUUIDList();
            if (columnUUIDListList != null) {
                for (java.util.UUID columnUUIDListItem : columnUUIDListList) {
                    if (columnUUIDListItem == null) {
                        columnUUIDListOsList.addNull();
                    } else {
                        columnUUIDListOsList.addUUID(columnUUIDListItem);
                    }
                }
            }


            OsList columnMixedListOsList = new OsList(table.getUncheckedRow(objKey), columnInfo.columnMixedListColKey);
            columnMixedListOsList.removeAll();
            RealmList<io.realm.Mixed> columnMixedListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMixedList();
            if (columnMixedListList != null) {
                for (io.realm.Mixed columnMixedListItem : columnMixedListList) {
                    if (columnMixedListItem == null) {
                        columnMixedListOsList.addNull();
                    } else {
                        columnMixedListOsList.addMixed(columnMixedListItem.getNativePtr());
                    }
                }
            }

        }
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
        unmanagedCopy.realmSet$columnString(realmSource.realmGet$columnString());
        unmanagedCopy.realmSet$columnLong(realmSource.realmGet$columnLong());
        unmanagedCopy.realmSet$columnFloat(realmSource.realmGet$columnFloat());
        unmanagedCopy.realmSet$columnDouble(realmSource.realmGet$columnDouble());
        unmanagedCopy.realmSet$columnBoolean(realmSource.realmGet$columnBoolean());
        unmanagedCopy.realmSet$columnDecimal128(realmSource.realmGet$columnDecimal128());
        unmanagedCopy.realmSet$columnObjectId(realmSource.realmGet$columnObjectId());
        unmanagedCopy.realmSet$columnUUID(realmSource.realmGet$columnUUID());
        unmanagedCopy.realmSet$columnDate(realmSource.realmGet$columnDate());
        unmanagedCopy.realmSet$columnMixed(realmSource.realmGet$columnMixed());
        unmanagedCopy.realmSet$columnBinary(realmSource.realmGet$columnBinary());
        unmanagedCopy.realmGet$columnMutableRealmInteger().set(realmSource.realmGet$columnMutableRealmInteger().get());

        // Deep copy of columnObject
        unmanagedCopy.realmSet$columnObject(some_test_AllTypesRealmProxy.createDetachedCopy(realmSource.realmGet$columnObject(), currentDepth + 1, maxDepth, cache));

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

        unmanagedCopy.realmSet$columnMixedList(new RealmList<io.realm.Mixed>());
        unmanagedCopy.realmGet$columnMixedList().addAll(realmSource.realmGet$columnMixedList());

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
        builder.addMixed(columnInfo.columnMixedColKey, realmObjectSource.realmGet$columnMixed());
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
        builder.addMixedList(columnInfo.columnMixedListColKey, realmObjectSource.realmGet$columnMixedList());

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
        stringBuilder.append(realmGet$columnMixed() != null ? realmGet$columnMixed() : "null");
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
        stringBuilder.append("{columnRealmList:");
        stringBuilder.append("RealmList<AllTypes>[").append(realmGet$columnRealmList().size()).append("]");
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnRealmFinalList:");
        stringBuilder.append("RealmList<AllTypes>[").append(realmGet$columnRealmFinalList().size()).append("]");
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
