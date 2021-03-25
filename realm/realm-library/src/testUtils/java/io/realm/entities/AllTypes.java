/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.entities;

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import io.realm.Mixed;
import io.realm.MutableRealmInteger;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.RealmSet;
import io.realm.TestHelper;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.Required;

public class AllTypes extends RealmObject {

    public static final String CLASS_NAME = "AllTypes";
    public static final String FIELD_STRING = "columnString";
    public static final String FIELD_LONG = "columnLong";
    public static final String FIELD_FLOAT = "columnFloat";
    public static final String FIELD_DOUBLE = "columnDouble";
    public static final String FIELD_BOOLEAN = "columnBoolean";
    public static final String FIELD_DATE = "columnDate";
    public static final String FIELD_BINARY = "columnBinary";
    public static final String FIELD_MUTABLEREALMINTEGER = "columnMutableRealmInteger";
    public static final String FIELD_DECIMAL128 = "columnDecimal128";
    public static final String FIELD_OBJECT_ID = "columnObjectId";
    public static final String FIELD_UUID = "columnUUID";
    public static final String FIELD_MIXED = "columnMixed";
    public static final String FIELD_REALMOBJECT = "columnRealmObject";
    public static final String FIELD_REALMLINK = "columnRealmLink";
    public static final String FIELD_REALMBACKLINK = "columnRealmBackLink";

    public static final String FIELD_REALMLIST = "columnRealmList";
    public static final String FIELD_STRING_LIST = "columnStringList";
    public static final String FIELD_BINARY_LIST = "columnBinaryList";
    public static final String FIELD_BOOLEAN_LIST = "columnBooleanList";
    public static final String FIELD_LONG_LIST = "columnLongList";
    public static final String FIELD_DOUBLE_LIST = "columnDoubleList";
    public static final String FIELD_FLOAT_LIST = "columnFloatList";
    public static final String FIELD_DATE_LIST = "columnDateList";

    public static final String[] INVALID_TYPES_FIELDS_FOR_DISTINCT
            = new String[]{FIELD_REALMOBJECT, FIELD_REALMLIST, FIELD_DOUBLE, FIELD_FLOAT,
            FIELD_STRING_LIST, FIELD_BINARY_LIST, FIELD_BOOLEAN_LIST, FIELD_LONG_LIST,
            FIELD_DOUBLE_LIST, FIELD_FLOAT_LIST, FIELD_DATE_LIST};


    @Required
    private String columnString = "";
    private long columnLong;
    private float columnFloat;
    private double columnDouble;
    private boolean columnBoolean;
    @Required
    private Date columnDate = new Date(0);
    @Required
    private byte[] columnBinary = new byte[0];
    @Required
    private Decimal128 columnDecimal128 = new Decimal128(BigDecimal.ZERO);
    @Required
    private ObjectId columnObjectId = new ObjectId(TestHelper.randomObjectIdHexString());
    @Required
    private UUID columnUUID = UUID.randomUUID();

    private Mixed columnMixed = Mixed.nullValue();

    private final MutableRealmInteger columnMutableRealmInteger = MutableRealmInteger.ofNull();

    private Dog columnRealmObject;
    private AllTypes columnRealmLink;

    @LinkingObjects("columnRealmLink")
    final private RealmResults<AllTypes> columnRealmBackLink = null;

    private RealmList<Dog> columnRealmList;

    private RealmList<String> columnStringList;
    private RealmList<byte[]> columnBinaryList;
    private RealmList<Boolean> columnBooleanList;
    private RealmList<Long> columnLongList;
    private RealmList<Double> columnDoubleList;
    private RealmList<Float> columnFloatList;
    private RealmList<Date> columnDateList;
    private RealmList<Decimal128> columnDecimal128List;
    private RealmList<ObjectId> columnObjectIdList;
    private RealmList<UUID> columnUUIDList;
    private RealmList<Mixed> columnMixedList;

//    private RealmSet<Boolean> columnBooleanSet;
    private RealmSet<String> columnStringSet;
    private RealmSet<Integer> columnIntegerSet;
    private RealmSet<Float> columnFloatSet;
    private RealmSet<Long> columnLongSet;
    private RealmSet<Short> columnShortSet;
//    private RealmSet<Double> columnDoubleSet;
    private RealmSet<Byte> columnByteSet;
    private RealmSet<byte[]> columnBinarySet;
//    private RealmSet<Date> columnDateSet;
    private RealmSet<ObjectId> columnObjectIdSet;
    private RealmSet<UUID> columnUUIDSet;
//    private RealmSet<Decimal128> columnDecimal128Set;
//    private RealmSet<Mixed> columnMixedSet;

    public String getColumnString() {
        return columnString;
    }

    public void setColumnString(String columnString) {
        this.columnString = columnString;
    }

    public long getColumnLong() {
        return columnLong;
    }

    public void setColumnLong(long columnLong) {
        this.columnLong = columnLong;
    }

    public float getColumnFloat() {
        return columnFloat;
    }

    public void setColumnFloat(float columnFloat) {
        this.columnFloat = columnFloat;
    }

    public double getColumnDouble() {
        return columnDouble;
    }

    public void setColumnDouble(double columnDouble) {
        this.columnDouble = columnDouble;
    }

    public boolean isColumnBoolean() {
        return columnBoolean;
    }

    public void setColumnBoolean(boolean columnBoolean) {
        this.columnBoolean = columnBoolean;
    }

    public Date getColumnDate() {
        return columnDate;
    }

    public void setColumnDate(Date columnDate) {
        this.columnDate = columnDate;
    }

    public byte[] getColumnBinary() {
        return columnBinary;
    }

    public MutableRealmInteger getColumnRealmInteger() {
        return columnMutableRealmInteger;
    }

    public void setColumnMutableRealmInteger(int value) {
        columnMutableRealmInteger.set(value);
    }


    public void setColumnBinary(byte[] columnBinary) {
        this.columnBinary = columnBinary;
    }

    public Dog getColumnRealmObject() {
        return columnRealmObject;
    }

    public void setColumnRealmObject(Dog columnRealmObject) {
        this.columnRealmObject = columnRealmObject;
    }

    public AllTypes getColumnRealmLink() {
        return columnRealmLink;
    }

    public void setColumnRealmLink(AllTypes columnRealmLink) {
        this.columnRealmLink = columnRealmLink;
    }

    public RealmResults<AllTypes> getColumnRealmBackLink() {
        return columnRealmBackLink;
    }

    public RealmList<Dog> getColumnRealmList() {
        return columnRealmList;
    }

    public void setColumnRealmList(RealmList<Dog> columnRealmList) {
        this.columnRealmList = columnRealmList;
    }

    public RealmList<String> getColumnStringList() {
        return columnStringList;
    }

    public void setColumnStringList(RealmList<String> columnStringList) {
        this.columnStringList = columnStringList;
    }

    public RealmList<byte[]> getColumnBinaryList() {
        return columnBinaryList;
    }

    public void setColumnBinaryList(RealmList<byte[]> columnBinaryList) {
        this.columnBinaryList = columnBinaryList;
    }

    public RealmList<Boolean> getColumnBooleanList() {
        return columnBooleanList;
    }

    public void setColumnBooleanList(RealmList<Boolean> columnBooleanList) {
        this.columnBooleanList = columnBooleanList;
    }

    public RealmList<Long> getColumnLongList() {
        return columnLongList;
    }

    public void setColumnLongList(RealmList<Long> columnLongList) {
        this.columnLongList = columnLongList;
    }

    public RealmList<Double> getColumnDoubleList() {
        return columnDoubleList;
    }

    public void setColumnDoubleList(RealmList<Double> columnDoubleList) {
        this.columnDoubleList = columnDoubleList;
    }

    public RealmList<Float> getColumnFloatList() {
        return columnFloatList;
    }

    public void setColumnFloatList(RealmList<Float> columnFloatList) {
        this.columnFloatList = columnFloatList;
    }

    public RealmList<Date> getColumnDateList() {
        return columnDateList;
    }

    public void setColumnDateList(RealmList<Date> columnDateList) {
        this.columnDateList = columnDateList;
    }

    public Decimal128 getColumnDecimal128() {
        return columnDecimal128;
    }

    public void setColumnDecimal128(Decimal128 columnDecimal128) {
        this.columnDecimal128 = columnDecimal128;
    }

    public ObjectId getColumnObjectId() {
        return columnObjectId;
    }

    public void setColumnObjectId(ObjectId columnObjectId) {
        this.columnObjectId = columnObjectId;
    }

    public UUID getColumnUUID() {
        return columnUUID;
    }

    public void setColumnUUID(UUID columnUUID) {
        this.columnUUID = columnUUID;
    }

    public Mixed getColumnMixed() {
        return columnMixed;
    }

    public void setColumnMixed(Mixed columnMixed) {
        this.columnMixed = columnMixed;
    }

    public RealmList<Decimal128> getColumnDecimal128List() {
        return columnDecimal128List;
    }

    public void setColumnDecimal128List(RealmList<Decimal128> columnDecimal128List) {
        this.columnDecimal128List = columnDecimal128List;
    }

    public RealmList<ObjectId> getColumnObjectIdList() {
        return columnObjectIdList;
    }

    public void setColumnObjectIdList(RealmList<ObjectId> columnObjectIdList) {
        this.columnObjectIdList = columnObjectIdList;
    }

    public RealmList<UUID> getColumnUUIDList() {
        return columnUUIDList;
    }

    public void setColumnUUIDList(RealmList<UUID> columnUUIDList) {
        this.columnUUIDList = columnUUIDList;
    }

    public RealmList<Mixed> getColumnMixedList() {
        return columnMixedList;
    }

    public void setColumnMixedList(RealmList<Mixed> columnMixedList) {
        this.columnMixedList = columnMixedList;
    }

    public RealmSet<String> getColumnStringSet() {
        return columnStringSet;
    }

    public void setColumnStringSet(RealmSet<String> columnStringSet) {
        this.columnStringSet = columnStringSet;
    }

    public RealmSet<Integer> getColumnIntegerSet() {
        return columnIntegerSet;
    }

    public void setColumnIntegerSet(RealmSet<Integer> columnIntegerSet) {
        this.columnIntegerSet = columnIntegerSet;
    }

    public RealmSet<Float> getColumnFloatSet() {
        return columnFloatSet;
    }

    public void setColumnFloatSet(RealmSet<Float> columnFloatSet) {
        this.columnFloatSet = columnFloatSet;
    }

    public RealmSet<Long> getColumnLongSet() {
        return columnLongSet;
    }

    public void setColumnLongSet(RealmSet<Long> columnLongSet) {
        this.columnLongSet = columnLongSet;
    }

    public RealmSet<Short> getColumnShortSet() {
        return columnShortSet;
    }

    public void setColumnShortSet(RealmSet<Short> columnShortSet) {
        this.columnShortSet = columnShortSet;
    }

    public RealmSet<Byte> getColumnByteSet() {
        return columnByteSet;
    }

    public void setColumnByteSet(RealmSet<Byte> columnByteSet) {
        this.columnByteSet = columnByteSet;
    }

    public RealmSet<byte[]> getColumnBinarySet() {
        return columnBinarySet;
    }

    public void setColumnBinarySet(RealmSet<byte[]> columnBinarySet) {
        this.columnBinarySet = columnBinarySet;
    }

    public RealmSet<ObjectId> getColumnObjectIdSet() {
        return columnObjectIdSet;
    }

    public void setColumnObjectIdSet(RealmSet<ObjectId> columnObjectIdSet) {
        this.columnObjectIdSet = columnObjectIdSet;
    }

    public RealmSet<UUID> getColumnUUIDSet() {
        return columnUUIDSet;
    }

    public void setColumnUUIDSet(RealmSet<UUID> columnUUIDSet) {
        this.columnUUIDSet = columnUUIDSet;
    }
}
