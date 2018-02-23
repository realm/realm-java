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
import io.realm.internal.UncheckedRow;
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
public class some_test_AllTypesRealmProxy extends some.test.AllTypes
        implements RealmObjectProxy, some_test_AllTypesRealmProxyInterface {

    static final class AllTypesColumnInfo extends ColumnInfo {
        long columnStringIndex;
        long columnLongIndex;
        long columnFloatIndex;
        long columnDoubleIndex;
        long columnBooleanIndex;
        long columnDateIndex;
        long columnBinaryIndex;
        long columnMutableRealmIntegerIndex;
        long columnObjectIndex;
        long columnRealmListIndex;
        long columnStringListIndex;
        long columnBinaryListIndex;
        long columnBooleanListIndex;
        long columnLongListIndex;
        long columnIntegerListIndex;
        long columnShortListIndex;
        long columnByteListIndex;
        long columnDoubleListIndex;
        long columnFloatListIndex;
        long columnDateListIndex;

        AllTypesColumnInfo(OsSchemaInfo schemaInfo) {
            super(20);
            OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo("AllTypes");
            this.columnStringIndex = addColumnDetails("columnString", "columnString", objectSchemaInfo);
            this.columnLongIndex = addColumnDetails("columnLong", "columnLong", objectSchemaInfo);
            this.columnFloatIndex = addColumnDetails("columnFloat", "columnFloat", objectSchemaInfo);
            this.columnDoubleIndex = addColumnDetails("columnDouble", "columnDouble", objectSchemaInfo);
            this.columnBooleanIndex = addColumnDetails("columnBoolean", "columnBoolean", objectSchemaInfo);
            this.columnDateIndex = addColumnDetails("columnDate", "columnDate", objectSchemaInfo);
            this.columnBinaryIndex = addColumnDetails("columnBinary", "columnBinary", objectSchemaInfo);
            this.columnMutableRealmIntegerIndex = addColumnDetails("columnMutableRealmInteger", "columnMutableRealmInteger", objectSchemaInfo);
            this.columnObjectIndex = addColumnDetails("columnObject", "columnObject", objectSchemaInfo);
            this.columnRealmListIndex = addColumnDetails("columnRealmList", "columnRealmList", objectSchemaInfo);
            this.columnStringListIndex = addColumnDetails("columnStringList", "columnStringList", objectSchemaInfo);
            this.columnBinaryListIndex = addColumnDetails("columnBinaryList", "columnBinaryList", objectSchemaInfo);
            this.columnBooleanListIndex = addColumnDetails("columnBooleanList", "columnBooleanList", objectSchemaInfo);
            this.columnLongListIndex = addColumnDetails("columnLongList", "columnLongList", objectSchemaInfo);
            this.columnIntegerListIndex = addColumnDetails("columnIntegerList", "columnIntegerList", objectSchemaInfo);
            this.columnShortListIndex = addColumnDetails("columnShortList", "columnShortList", objectSchemaInfo);
            this.columnByteListIndex = addColumnDetails("columnByteList", "columnByteList", objectSchemaInfo);
            this.columnDoubleListIndex = addColumnDetails("columnDoubleList", "columnDoubleList", objectSchemaInfo);
            this.columnFloatListIndex = addColumnDetails("columnFloatList", "columnFloatList", objectSchemaInfo);
            this.columnDateListIndex = addColumnDetails("columnDateList", "columnDateList", objectSchemaInfo);
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
            dst.columnStringIndex = src.columnStringIndex;
            dst.columnLongIndex = src.columnLongIndex;
            dst.columnFloatIndex = src.columnFloatIndex;
            dst.columnDoubleIndex = src.columnDoubleIndex;
            dst.columnBooleanIndex = src.columnBooleanIndex;
            dst.columnDateIndex = src.columnDateIndex;
            dst.columnBinaryIndex = src.columnBinaryIndex;
            dst.columnMutableRealmIntegerIndex = src.columnMutableRealmIntegerIndex;
            dst.columnObjectIndex = src.columnObjectIndex;
            dst.columnRealmListIndex = src.columnRealmListIndex;
            dst.columnStringListIndex = src.columnStringListIndex;
            dst.columnBinaryListIndex = src.columnBinaryListIndex;
            dst.columnBooleanListIndex = src.columnBooleanListIndex;
            dst.columnLongListIndex = src.columnLongListIndex;
            dst.columnIntegerListIndex = src.columnIntegerListIndex;
            dst.columnShortListIndex = src.columnShortListIndex;
            dst.columnByteListIndex = src.columnByteListIndex;
            dst.columnDoubleListIndex = src.columnDoubleListIndex;
            dst.columnFloatListIndex = src.columnFloatListIndex;
            dst.columnDateListIndex = src.columnDateListIndex;
        }
    }

    private static final OsObjectSchemaInfo expectedObjectSchemaInfo = createExpectedObjectSchemaInfo();

    private AllTypesColumnInfo columnInfo;
    private ProxyState<some.test.AllTypes> proxyState;
    private final MutableRealmInteger.Managed columnMutableRealmIntegerMutableRealmInteger = new MutableRealmInteger.Managed<some.test.AllTypes>() {
        @Override protected ProxyState<some.test.AllTypes> getProxyState() { return proxyState; }
        @Override protected long getColumnIndex() { return columnInfo.columnMutableRealmIntegerIndex; }
    };
    private RealmList<some.test.AllTypes> columnRealmListRealmList;
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
        return (java.lang.String) proxyState.getRow$realm().getString(columnInfo.columnStringIndex);
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
        return (long) proxyState.getRow$realm().getLong(columnInfo.columnLongIndex);
    }

    @Override
    public void realmSet$columnLong(long value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setLong(columnInfo.columnLongIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setLong(columnInfo.columnLongIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public float realmGet$columnFloat() {
        proxyState.getRealm$realm().checkIfValid();
        return (float) proxyState.getRow$realm().getFloat(columnInfo.columnFloatIndex);
    }

    @Override
    public void realmSet$columnFloat(float value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setFloat(columnInfo.columnFloatIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setFloat(columnInfo.columnFloatIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public double realmGet$columnDouble() {
        proxyState.getRealm$realm().checkIfValid();
        return (double) proxyState.getRow$realm().getDouble(columnInfo.columnDoubleIndex);
    }

    @Override
    public void realmSet$columnDouble(double value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setDouble(columnInfo.columnDoubleIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setDouble(columnInfo.columnDoubleIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public boolean realmGet$columnBoolean() {
        proxyState.getRealm$realm().checkIfValid();
        return (boolean) proxyState.getRow$realm().getBoolean(columnInfo.columnBooleanIndex);
    }

    @Override
    public void realmSet$columnBoolean(boolean value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            final Row row = proxyState.getRow$realm();
            row.getTable().setBoolean(columnInfo.columnBooleanIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        proxyState.getRow$realm().setBoolean(columnInfo.columnBooleanIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public Date realmGet$columnDate() {
        proxyState.getRealm$realm().checkIfValid();
        return (java.util.Date) proxyState.getRow$realm().getDate(columnInfo.columnDateIndex);
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
            row.getTable().setDate(columnInfo.columnDateIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'columnDate' to null.");
        }
        proxyState.getRow$realm().setDate(columnInfo.columnDateIndex, value);
    }

    @Override
    @SuppressWarnings("cast")
    public byte[] realmGet$columnBinary() {
        proxyState.getRealm$realm().checkIfValid();
        return (byte[]) proxyState.getRow$realm().getBinaryByteArray(columnInfo.columnBinaryIndex);
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
            row.getTable().setBinaryByteArray(columnInfo.columnBinaryIndex, row.getIndex(), value, true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            throw new IllegalArgumentException("Trying to set non-nullable field 'columnBinary' to null.");
        }
        proxyState.getRow$realm().setBinaryByteArray(columnInfo.columnBinaryIndex, value);
    }

    @Override
    public MutableRealmInteger realmGet$columnMutableRealmInteger() {
        proxyState.getRealm$realm().checkIfValid();
        return this.columnMutableRealmIntegerMutableRealmInteger;
    }

    @Override
    public some.test.AllTypes realmGet$columnObject() {
        proxyState.getRealm$realm().checkIfValid();
        if (proxyState.getRow$realm().isNullLink(columnInfo.columnObjectIndex)) {
            return null;
        }
        return proxyState.getRealm$realm().get(some.test.AllTypes.class, proxyState.getRow$realm().getLink(columnInfo.columnObjectIndex), false, Collections.<String>emptyList());
    }

    @Override
    public void realmSet$columnObject(some.test.AllTypes value) {
        if (proxyState.isUnderConstruction()) {
            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }
            if (proxyState.getExcludeFields$realm().contains("columnObject")) {
                return;
            }
            if (value != null && !RealmObject.isManaged(value)) {
                value = ((Realm) proxyState.getRealm$realm()).copyToRealm(value);
            }
            final Row row = proxyState.getRow$realm();
            if (value == null) {
                // Table#nullifyLink() does not support default value. Just using Row.
                row.nullifyLink(columnInfo.columnObjectIndex);
                return;
            }
            proxyState.checkValidObject(value);
            row.getTable().setLink(columnInfo.columnObjectIndex, row.getIndex(), ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getIndex(), true);
            return;
        }

        proxyState.getRealm$realm().checkIfValid();
        if (value == null) {
            proxyState.getRow$realm().nullifyLink(columnInfo.columnObjectIndex);
            return;
        }
        proxyState.checkValidObject(value);
        proxyState.getRow$realm().setLink(columnInfo.columnObjectIndex, ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getIndex());
    }

    @Override
    public RealmList<some.test.AllTypes> realmGet$columnRealmList() {
        proxyState.getRealm$realm().checkIfValid();
        // use the cached value if available
        if (columnRealmListRealmList != null) {
            return columnRealmListRealmList;
        } else {
            OsList osList = proxyState.getRow$realm().getModelList(columnInfo.columnRealmListIndex);
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
        OsList osList = proxyState.getRow$realm().getModelList(columnInfo.columnRealmListIndex);
        // For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.
        if (value != null && value.size() == osList.size()) {
            int objects = value.size();
            for (int i = 0; i < objects; i++) {
                some.test.AllTypes linkedObject = value.get(i);
                proxyState.checkValidObject(linkedObject);
                osList.setRow(i, ((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getIndex());
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
                osList.addRow(((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getIndex());
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnStringListIndex, RealmFieldType.STRING_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnStringListIndex, RealmFieldType.STRING_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnBinaryListIndex, RealmFieldType.BINARY_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnBinaryListIndex, RealmFieldType.BINARY_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnBooleanListIndex, RealmFieldType.BOOLEAN_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnBooleanListIndex, RealmFieldType.BOOLEAN_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnLongListIndex, RealmFieldType.INTEGER_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnLongListIndex, RealmFieldType.INTEGER_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnIntegerListIndex, RealmFieldType.INTEGER_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnIntegerListIndex, RealmFieldType.INTEGER_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnShortListIndex, RealmFieldType.INTEGER_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnShortListIndex, RealmFieldType.INTEGER_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnByteListIndex, RealmFieldType.INTEGER_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnByteListIndex, RealmFieldType.INTEGER_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnDoubleListIndex, RealmFieldType.DOUBLE_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnDoubleListIndex, RealmFieldType.DOUBLE_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnFloatListIndex, RealmFieldType.FLOAT_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnFloatListIndex, RealmFieldType.FLOAT_LIST);
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
            OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnDateListIndex, RealmFieldType.DATE_LIST);
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
        OsList osList = proxyState.getRow$realm().getValueList(columnInfo.columnDateListIndex, RealmFieldType.DATE_LIST);
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
        OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder("AllTypes", 20, 1);
        builder.addPersistedProperty("columnString", RealmFieldType.STRING, Property.PRIMARY_KEY, Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedProperty("columnLong", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnFloat", RealmFieldType.FLOAT, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnDouble", RealmFieldType.DOUBLE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnBoolean", RealmFieldType.BOOLEAN, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnDate", RealmFieldType.DATE, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnBinary", RealmFieldType.BINARY, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
        builder.addPersistedProperty("columnMutableRealmInteger", RealmFieldType.INTEGER, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
        builder.addPersistedLinkProperty("columnObject", RealmFieldType.OBJECT, "AllTypes");
        builder.addPersistedLinkProperty("columnRealmList", RealmFieldType.LIST, "AllTypes");
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
        final List<String> excludeFields = new ArrayList<String>(12);
        some.test.AllTypes obj = null;
        if (update) {
            Table table = realm.getTable(some.test.AllTypes.class);
            AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
            long pkColumnIndex = columnInfo.columnStringIndex;
            long rowIndex = Table.NO_MATCH;
            if (json.isNull("columnString")) {
                rowIndex = table.findFirstNull(pkColumnIndex);
            } else {
                rowIndex = table.findFirstString(pkColumnIndex, json.getString("columnString"));
            }
            if (rowIndex != Table.NO_MATCH) {
                final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get();
                try {
                    objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.getSchema().getColumnInfo(some.test.AllTypes.class), false, Collections.<String> emptyList());
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

    public static some.test.AllTypes copyOrUpdate(Realm realm, some.test.AllTypes object, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
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
            return (some.test.AllTypes) cachedRealmObject;
        }

        some.test.AllTypes realmObject = null;
        boolean canUpdate = update;
        if (canUpdate) {
            Table table = realm.getTable(some.test.AllTypes.class);
            AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
            long pkColumnIndex = columnInfo.columnStringIndex;
            String value = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnString();
            long rowIndex = Table.NO_MATCH;
            if (value == null) {
                rowIndex = table.findFirstNull(pkColumnIndex);
            } else {
                rowIndex = table.findFirstString(pkColumnIndex, value);
            }
            if (rowIndex == Table.NO_MATCH) {
                canUpdate = false;
            } else {
                try {
                    objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.getSchema().getColumnInfo(some.test.AllTypes.class), false, Collections.<String> emptyList());
                    realmObject = new io.realm.some_test_AllTypesRealmProxy();
                    cache.put(object, (RealmObjectProxy) realmObject);
                } finally {
                    objectContext.clear();
                }
            }
        }

        return (canUpdate) ? update(realm, realmObject, object, cache) : copy(realm, object, update, cache);
    }

    public static some.test.AllTypes copy(Realm realm, some.test.AllTypes newObject, boolean update, Map<RealmModel,RealmObjectProxy> cache) {
        RealmObjectProxy cachedRealmObject = cache.get(newObject);
        if (cachedRealmObject != null) {
            return (some.test.AllTypes) cachedRealmObject;
        }

        // rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.
        some.test.AllTypes realmObject = realm.createObjectInternal(some.test.AllTypes.class, ((some_test_AllTypesRealmProxyInterface) newObject).realmGet$columnString(), false, Collections.<String>emptyList());
        cache.put(newObject, (RealmObjectProxy) realmObject);

        some_test_AllTypesRealmProxyInterface realmObjectSource = (some_test_AllTypesRealmProxyInterface) newObject;
        some_test_AllTypesRealmProxyInterface realmObjectCopy = (some_test_AllTypesRealmProxyInterface) realmObject;

        realmObjectCopy.realmSet$columnLong(realmObjectSource.realmGet$columnLong());
        realmObjectCopy.realmSet$columnFloat(realmObjectSource.realmGet$columnFloat());
        realmObjectCopy.realmSet$columnDouble(realmObjectSource.realmGet$columnDouble());
        realmObjectCopy.realmSet$columnBoolean(realmObjectSource.realmGet$columnBoolean());
        realmObjectCopy.realmSet$columnDate(realmObjectSource.realmGet$columnDate());
        realmObjectCopy.realmSet$columnBinary(realmObjectSource.realmGet$columnBinary());

        realmObjectCopy.realmGet$columnMutableRealmInteger().set(realmObjectSource.realmGet$columnMutableRealmInteger().get());

        some.test.AllTypes columnObjectObj = realmObjectSource.realmGet$columnObject();
        if (columnObjectObj == null) {
            realmObjectCopy.realmSet$columnObject(null);
        } else {
            some.test.AllTypes cachecolumnObject = (some.test.AllTypes) cache.get(columnObjectObj);
            if (cachecolumnObject != null) {
                realmObjectCopy.realmSet$columnObject(cachecolumnObject);
            } else {
                realmObjectCopy.realmSet$columnObject(some_test_AllTypesRealmProxy.copyOrUpdate(realm, columnObjectObj, update, cache));
            }
        }

        RealmList<some.test.AllTypes> columnRealmListList = realmObjectSource.realmGet$columnRealmList();
        if (columnRealmListList != null) {
            RealmList<some.test.AllTypes> columnRealmListRealmList = realmObjectCopy.realmGet$columnRealmList();
            columnRealmListRealmList.clear();
            for (int i = 0; i < columnRealmListList.size(); i++) {
                some.test.AllTypes columnRealmListItem = columnRealmListList.get(i);
                some.test.AllTypes cachecolumnRealmList = (some.test.AllTypes) cache.get(columnRealmListItem);
                if (cachecolumnRealmList != null) {
                    columnRealmListRealmList.add(cachecolumnRealmList);
                } else {
                    columnRealmListRealmList.add(some_test_AllTypesRealmProxy.copyOrUpdate(realm, columnRealmListItem, update, cache));
                }
            }
        }

        realmObjectCopy.realmSet$columnStringList(realmObjectSource.realmGet$columnStringList());
        realmObjectCopy.realmSet$columnBinaryList(realmObjectSource.realmGet$columnBinaryList());
        realmObjectCopy.realmSet$columnBooleanList(realmObjectSource.realmGet$columnBooleanList());
        realmObjectCopy.realmSet$columnLongList(realmObjectSource.realmGet$columnLongList());
        realmObjectCopy.realmSet$columnIntegerList(realmObjectSource.realmGet$columnIntegerList());
        realmObjectCopy.realmSet$columnShortList(realmObjectSource.realmGet$columnShortList());
        realmObjectCopy.realmSet$columnByteList(realmObjectSource.realmGet$columnByteList());
        realmObjectCopy.realmSet$columnDoubleList(realmObjectSource.realmGet$columnDoubleList());
        realmObjectCopy.realmSet$columnFloatList(realmObjectSource.realmGet$columnFloatList());
        realmObjectCopy.realmSet$columnDateList(realmObjectSource.realmGet$columnDateList());
        return realmObject;
    }

    public static long insert(Realm realm, some.test.AllTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativePtr();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
        long pkColumnIndex = columnInfo.columnStringIndex;
        String primaryKeyValue = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnString();
        long rowIndex = Table.NO_MATCH;
        if (primaryKeyValue == null) {
            rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
        } else {
            rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
        }
        if (rowIndex == Table.NO_MATCH) {
            rowIndex = OsObject.createRowWithPrimaryKey(table, pkColumnIndex, primaryKeyValue);
        } else {
            Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
        }
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.columnLongIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnLong(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnFloat(), false);
        Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDouble(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBoolean(), false);
        java.util.Date realmGet$columnDate = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDate();
        if (realmGet$columnDate != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateIndex, rowIndex, realmGet$columnDate.getTime(), false);
        }
        byte[] realmGet$columnBinary = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBinary();
        if (realmGet$columnBinary != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, realmGet$columnBinary, false);
        }
        Long realmGet$columnMutableRealmInteger = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMutableRealmInteger().get();
        if (realmGet$columnMutableRealmInteger != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.columnMutableRealmIntegerIndex, rowIndex, realmGet$columnMutableRealmInteger.longValue(), false);
        }

        some.test.AllTypes columnObjectObj = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObject();
        if (columnObjectObj != null) {
            Long cachecolumnObject = cache.get(columnObjectObj);
            if (cachecolumnObject == null) {
                cachecolumnObject = some_test_AllTypesRealmProxy.insert(realm, columnObjectObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.columnObjectIndex, rowIndex, cachecolumnObject, false);
        }

        RealmList<some.test.AllTypes> columnRealmListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnRealmList();
        if (columnRealmListList != null) {
            OsList columnRealmListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnRealmListIndex);
            for (some.test.AllTypes columnRealmListItem : columnRealmListList) {
                Long cacheItemIndexcolumnRealmList = cache.get(columnRealmListItem);
                if (cacheItemIndexcolumnRealmList == null) {
                    cacheItemIndexcolumnRealmList = some_test_AllTypesRealmProxy.insert(realm, columnRealmListItem, cache);
                }
                columnRealmListOsList.addRow(cacheItemIndexcolumnRealmList);
            }
        }

        RealmList<java.lang.String> columnStringListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnStringList();
        if (columnStringListList != null) {
            OsList columnStringListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnStringListIndex);
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
            OsList columnBinaryListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnBinaryListIndex);
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
            OsList columnBooleanListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnBooleanListIndex);
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
            OsList columnLongListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnLongListIndex);
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
            OsList columnIntegerListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnIntegerListIndex);
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
            OsList columnShortListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnShortListIndex);
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
            OsList columnByteListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnByteListIndex);
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
            OsList columnDoubleListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnDoubleListIndex);
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
            OsList columnFloatListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnFloatListIndex);
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
            OsList columnDateListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnDateListIndex);
            for (java.util.Date columnDateListItem : columnDateListList) {
                if (columnDateListItem == null) {
                    columnDateListOsList.addNull();
                } else {
                    columnDateListOsList.addDate(columnDateListItem);
                }
            }
        }
        return rowIndex;
    }

    public static void insert(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativePtr();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
        long pkColumnIndex = columnInfo.columnStringIndex;
        some.test.AllTypes object = null;
        while (objects.hasNext()) {
            object = (some.test.AllTypes) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            String primaryKeyValue = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnString();
            long rowIndex = Table.NO_MATCH;
            if (primaryKeyValue == null) {
                rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
            } else {
                rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
            }
            if (rowIndex == Table.NO_MATCH) {
                rowIndex = OsObject.createRowWithPrimaryKey(table, pkColumnIndex, primaryKeyValue);
            } else {
                Table.throwDuplicatePrimaryKeyException(primaryKeyValue);
            }
            cache.put(object, rowIndex);
            Table.nativeSetLong(tableNativePtr, columnInfo.columnLongIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnLong(), false);
            Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnFloat(), false);
            Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDouble(), false);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBoolean(), false);
            java.util.Date realmGet$columnDate = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDate();
            if (realmGet$columnDate != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateIndex, rowIndex, realmGet$columnDate.getTime(), false);
            }
            byte[] realmGet$columnBinary = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBinary();
            if (realmGet$columnBinary != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, realmGet$columnBinary, false);
            }
            Long realmGet$columnMutableRealmInteger = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMutableRealmInteger().get();
            if (realmGet$columnMutableRealmInteger != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.columnMutableRealmIntegerIndex, rowIndex, realmGet$columnMutableRealmInteger.longValue(), false);
            }

            some.test.AllTypes columnObjectObj = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObject();
            if (columnObjectObj != null) {
                Long cachecolumnObject = cache.get(columnObjectObj);
                if (cachecolumnObject == null) {
                    cachecolumnObject = some_test_AllTypesRealmProxy.insert(realm, columnObjectObj, cache);
                }
                table.setLink(columnInfo.columnObjectIndex, rowIndex, cachecolumnObject, false);
            }

            RealmList<some.test.AllTypes> columnRealmListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnRealmList();
            if (columnRealmListList != null) {
                OsList columnRealmListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnRealmListIndex);
                for (some.test.AllTypes columnRealmListItem : columnRealmListList) {
                    Long cacheItemIndexcolumnRealmList = cache.get(columnRealmListItem);
                    if (cacheItemIndexcolumnRealmList == null) {
                        cacheItemIndexcolumnRealmList = some_test_AllTypesRealmProxy.insert(realm, columnRealmListItem, cache);
                    }
                    columnRealmListOsList.addRow(cacheItemIndexcolumnRealmList);
                }
            }

            RealmList<java.lang.String> columnStringListList = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnStringList();
            if (columnStringListList != null) {
                OsList columnStringListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnStringListIndex);
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
                OsList columnBinaryListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnBinaryListIndex);
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
                OsList columnBooleanListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnBooleanListIndex);
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
                OsList columnLongListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnLongListIndex);
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
                OsList columnIntegerListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnIntegerListIndex);
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
                OsList columnShortListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnShortListIndex);
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
                OsList columnByteListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnByteListIndex);
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
                OsList columnDoubleListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnDoubleListIndex);
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
                OsList columnFloatListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnFloatListIndex);
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
                OsList columnDateListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnDateListIndex);
                for (java.util.Date columnDateListItem : columnDateListList) {
                    if (columnDateListItem == null) {
                        columnDateListOsList.addNull();
                    } else {
                        columnDateListOsList.addDate(columnDateListItem);
                    }
                }
            }
        }
    }

    public static long insertOrUpdate(Realm realm, some.test.AllTypes object, Map<RealmModel,Long> cache) {
        if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
            return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex();
        }
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativePtr();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
        long pkColumnIndex = columnInfo.columnStringIndex;
        String primaryKeyValue = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnString();
        long rowIndex = Table.NO_MATCH;
        if (primaryKeyValue == null) {
            rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
        } else {
            rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
        }
        if (rowIndex == Table.NO_MATCH) {
            rowIndex = OsObject.createRowWithPrimaryKey(table, pkColumnIndex, primaryKeyValue);
        }
        cache.put(object, rowIndex);
        Table.nativeSetLong(tableNativePtr, columnInfo.columnLongIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnLong(), false);
        Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnFloat(), false);
        Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDouble(), false);
        Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBoolean(), false);
        java.util.Date realmGet$columnDate = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDate();
        if (realmGet$columnDate != null) {
            Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateIndex, rowIndex, realmGet$columnDate.getTime(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnDateIndex, rowIndex, false);
        }
        byte[] realmGet$columnBinary = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBinary();
        if (realmGet$columnBinary != null) {
            Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, realmGet$columnBinary, false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, false);
        }
        Long realmGet$columnMutableRealmInteger = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMutableRealmInteger().get();
        if (realmGet$columnMutableRealmInteger != null) {
            Table.nativeSetLong(tableNativePtr, columnInfo.columnMutableRealmIntegerIndex, rowIndex, realmGet$columnMutableRealmInteger.longValue(), false);
        } else {
            Table.nativeSetNull(tableNativePtr, columnInfo.columnMutableRealmIntegerIndex, rowIndex, false);
        }

        some.test.AllTypes columnObjectObj = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObject();
        if (columnObjectObj != null) {
            Long cachecolumnObject = cache.get(columnObjectObj);
            if (cachecolumnObject == null) {
                cachecolumnObject = some_test_AllTypesRealmProxy.insertOrUpdate(realm, columnObjectObj, cache);
            }
            Table.nativeSetLink(tableNativePtr, columnInfo.columnObjectIndex, rowIndex, cachecolumnObject, false);
        } else {
            Table.nativeNullifyLink(tableNativePtr, columnInfo.columnObjectIndex, rowIndex);
        }

        OsList columnRealmListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnRealmListIndex);
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


        OsList columnStringListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnStringListIndex);
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


        OsList columnBinaryListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnBinaryListIndex);
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


        OsList columnBooleanListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnBooleanListIndex);
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


        OsList columnLongListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnLongListIndex);
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


        OsList columnIntegerListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnIntegerListIndex);
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


        OsList columnShortListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnShortListIndex);
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


        OsList columnByteListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnByteListIndex);
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


        OsList columnDoubleListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnDoubleListIndex);
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


        OsList columnFloatListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnFloatListIndex);
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


        OsList columnDateListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnDateListIndex);
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

        return rowIndex;
    }

    public static void insertOrUpdate(Realm realm, Iterator<? extends RealmModel> objects, Map<RealmModel,Long> cache) {
        Table table = realm.getTable(some.test.AllTypes.class);
        long tableNativePtr = table.getNativePtr();
        AllTypesColumnInfo columnInfo = (AllTypesColumnInfo) realm.getSchema().getColumnInfo(some.test.AllTypes.class);
        long pkColumnIndex = columnInfo.columnStringIndex;
        some.test.AllTypes object = null;
        while (objects.hasNext()) {
            object = (some.test.AllTypes) objects.next();
            if (cache.containsKey(object)) {
                continue;
            }
            if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex());
                continue;
            }
            String primaryKeyValue = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnString();
            long rowIndex = Table.NO_MATCH;
            if (primaryKeyValue == null) {
                rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex);
            } else {
                rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue);
            }
            if (rowIndex == Table.NO_MATCH) {
                rowIndex = OsObject.createRowWithPrimaryKey(table, pkColumnIndex, primaryKeyValue);
            }
            cache.put(object, rowIndex);
            Table.nativeSetLong(tableNativePtr, columnInfo.columnLongIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnLong(), false);
            Table.nativeSetFloat(tableNativePtr, columnInfo.columnFloatIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnFloat(), false);
            Table.nativeSetDouble(tableNativePtr, columnInfo.columnDoubleIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDouble(), false);
            Table.nativeSetBoolean(tableNativePtr, columnInfo.columnBooleanIndex, rowIndex, ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBoolean(), false);
            java.util.Date realmGet$columnDate = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnDate();
            if (realmGet$columnDate != null) {
                Table.nativeSetTimestamp(tableNativePtr, columnInfo.columnDateIndex, rowIndex, realmGet$columnDate.getTime(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnDateIndex, rowIndex, false);
            }
            byte[] realmGet$columnBinary = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnBinary();
            if (realmGet$columnBinary != null) {
                Table.nativeSetByteArray(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, realmGet$columnBinary, false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnBinaryIndex, rowIndex, false);
            }
            Long realmGet$columnMutableRealmInteger = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnMutableRealmInteger().get();
            if (realmGet$columnMutableRealmInteger != null) {
                Table.nativeSetLong(tableNativePtr, columnInfo.columnMutableRealmIntegerIndex, rowIndex, realmGet$columnMutableRealmInteger.longValue(), false);
            } else {
                Table.nativeSetNull(tableNativePtr, columnInfo.columnMutableRealmIntegerIndex, rowIndex, false);
            }

            some.test.AllTypes columnObjectObj = ((some_test_AllTypesRealmProxyInterface) object).realmGet$columnObject();
            if (columnObjectObj != null) {
                Long cachecolumnObject = cache.get(columnObjectObj);
                if (cachecolumnObject == null) {
                    cachecolumnObject = some_test_AllTypesRealmProxy.insertOrUpdate(realm, columnObjectObj, cache);
                }
                Table.nativeSetLink(tableNativePtr, columnInfo.columnObjectIndex, rowIndex, cachecolumnObject, false);
            } else {
                Table.nativeNullifyLink(tableNativePtr, columnInfo.columnObjectIndex, rowIndex);
            }

            OsList columnRealmListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnRealmListIndex);
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


            OsList columnStringListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnStringListIndex);
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


            OsList columnBinaryListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnBinaryListIndex);
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


            OsList columnBooleanListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnBooleanListIndex);
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


            OsList columnLongListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnLongListIndex);
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


            OsList columnIntegerListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnIntegerListIndex);
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


            OsList columnShortListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnShortListIndex);
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


            OsList columnByteListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnByteListIndex);
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


            OsList columnDoubleListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnDoubleListIndex);
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


            OsList columnFloatListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnFloatListIndex);
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


            OsList columnDateListOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.columnDateListIndex);
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
        unmanagedCopy.realmSet$columnDate(realmSource.realmGet$columnDate());
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

        return unmanagedObject;
    }

    static some.test.AllTypes update(Realm realm, some.test.AllTypes realmObject, some.test.AllTypes newObject, Map<RealmModel, RealmObjectProxy> cache) {
        some_test_AllTypesRealmProxyInterface realmObjectTarget = (some_test_AllTypesRealmProxyInterface) realmObject;
        some_test_AllTypesRealmProxyInterface realmObjectSource = (some_test_AllTypesRealmProxyInterface) newObject;
        realmObjectTarget.realmSet$columnLong(realmObjectSource.realmGet$columnLong());
        realmObjectTarget.realmSet$columnFloat(realmObjectSource.realmGet$columnFloat());
        realmObjectTarget.realmSet$columnDouble(realmObjectSource.realmGet$columnDouble());
        realmObjectTarget.realmSet$columnBoolean(realmObjectSource.realmGet$columnBoolean());
        realmObjectTarget.realmSet$columnDate(realmObjectSource.realmGet$columnDate());
        realmObjectTarget.realmSet$columnBinary(realmObjectSource.realmGet$columnBinary());
        realmObjectTarget.realmGet$columnMutableRealmInteger().set(realmObjectSource.realmGet$columnMutableRealmInteger().get());
        some.test.AllTypes columnObjectObj = realmObjectSource.realmGet$columnObject();
        if (columnObjectObj == null) {
            realmObjectTarget.realmSet$columnObject(null);
        } else {
            some.test.AllTypes cachecolumnObject = (some.test.AllTypes) cache.get(columnObjectObj);
            if (cachecolumnObject != null) {
                realmObjectTarget.realmSet$columnObject(cachecolumnObject);
            } else {
                realmObjectTarget.realmSet$columnObject(some_test_AllTypesRealmProxy.copyOrUpdate(realm, columnObjectObj, true, cache));
            }
        }
        RealmList<some.test.AllTypes> columnRealmListList = realmObjectSource.realmGet$columnRealmList();
        RealmList<some.test.AllTypes> columnRealmListRealmList = realmObjectTarget.realmGet$columnRealmList();
        if (columnRealmListList != null && columnRealmListList.size() == columnRealmListRealmList.size()) {
            // For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.
            int objects = columnRealmListList.size();
            for (int i = 0; i < objects; i++) {
                some.test.AllTypes columnRealmListItem = columnRealmListList.get(i);
                some.test.AllTypes cachecolumnRealmList = (some.test.AllTypes) cache.get(columnRealmListItem);
                if (cachecolumnRealmList != null) {
                    columnRealmListRealmList.set(i, cachecolumnRealmList);
                } else {
                    columnRealmListRealmList.set(i, some_test_AllTypesRealmProxy.copyOrUpdate(realm, columnRealmListItem, true, cache));
                }
            }
        } else {
            columnRealmListRealmList.clear();
            if (columnRealmListList != null) {
                for (int i = 0; i < columnRealmListList.size(); i++) {
                    some.test.AllTypes columnRealmListItem = columnRealmListList.get(i);
                    some.test.AllTypes cachecolumnRealmList = (some.test.AllTypes) cache.get(columnRealmListItem);
                    if (cachecolumnRealmList != null) {
                        columnRealmListRealmList.add(cachecolumnRealmList);
                    } else {
                        columnRealmListRealmList.add(some_test_AllTypesRealmProxy.copyOrUpdate(realm, columnRealmListItem, true, cache));
                    }
                }
            }
        }
        realmObjectTarget.realmSet$columnStringList(realmObjectSource.realmGet$columnStringList());
        realmObjectTarget.realmSet$columnBinaryList(realmObjectSource.realmGet$columnBinaryList());
        realmObjectTarget.realmSet$columnBooleanList(realmObjectSource.realmGet$columnBooleanList());
        realmObjectTarget.realmSet$columnLongList(realmObjectSource.realmGet$columnLongList());
        realmObjectTarget.realmSet$columnIntegerList(realmObjectSource.realmGet$columnIntegerList());
        realmObjectTarget.realmSet$columnShortList(realmObjectSource.realmGet$columnShortList());
        realmObjectTarget.realmSet$columnByteList(realmObjectSource.realmGet$columnByteList());
        realmObjectTarget.realmSet$columnDoubleList(realmObjectSource.realmGet$columnDoubleList());
        realmObjectTarget.realmSet$columnFloatList(realmObjectSource.realmGet$columnFloatList());
        realmObjectTarget.realmSet$columnDateList(realmObjectSource.realmGet$columnDateList());
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
        stringBuilder.append("{columnDate:");
        stringBuilder.append(realmGet$columnDate());
        stringBuilder.append("}");
        stringBuilder.append(",");
        stringBuilder.append("{columnBinary:");
        stringBuilder.append(realmGet$columnBinary());
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
        some_test_AllTypesRealmProxy aAllTypes = (some_test_AllTypesRealmProxy)o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = aAllTypes.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = aAllTypes.proxyState.getRow$realm().getTable().getName();
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false;

        if (proxyState.getRow$realm().getIndex() != aAllTypes.proxyState.getRow$realm().getIndex()) return false;

        return true;
    }
}
