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
import io.realm.RealmDictionary;
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

    public static final String FIELD_BOOLEAN_DICTIONARY = "columnBooleanDictionary";
    public static final String FIELD_STRING_DICTIONARY = "columnStringDictionary";
    public static final String FIELD_INTEGER_DICTIONARY = "columnIntegerDictionary";
    public static final String FIELD_FLOAT_DICTIONARY = "columnFloatDictionary";
    public static final String FIELD_LONG_DICTIONARY = "columnLongDictionary";
    public static final String FIELD_SHORT_DICTIONARY = "columnShortDictionary";
    public static final String FIELD_DOUBLE_DICTIONARY = "columnDoubleDictionary";
    public static final String FIELD_BYTE_DICTIONARY = "columnByteDictionary";
    public static final String FIELD_BINARY_DICTIONARY = "columnBinaryDictionary";
    public static final String FIELD_DATE_DICTIONARY = "columnDateDictionary";
    public static final String FIELD_OBJECT_ID_DICTIONARY = "columnObjectIdDictionary";
    public static final String FIELD_UUID_DICTIONARY = "columnUUIDDictionary";
    public static final String FIELD_DECIMAL128_DICTIONARY = "columnDecimal128Dictionary";
    public static final String FIELD_MIXED_DICTIONARY = "columnMixedDictionary";

    public static final String FIELD_REQUIRED_BOOLEAN_DICTIONARY = "columnRequiredBooleanDictionary";
    public static final String FIELD_REQUIRED_STRING_DICTIONARY = "columnRequiredStringDictionary";
    public static final String FIELD_REQUIRED_INTEGER_DICTIONARY = "columnRequiredIntegerDictionary";
    public static final String FIELD_REQUIRED_FLOAT_DICTIONARY = "columnRequiredFloatDictionary";
    public static final String FIELD_REQUIRED_LONG_DICTIONARY = "columnRequiredLongDictionary";
    public static final String FIELD_REQUIRED_SHORT_DICTIONARY = "columnRequiredShortDictionary";
    public static final String FIELD_REQUIRED_DOUBLE_DICTIONARY = "columnRequiredDoubleDictionary";
    public static final String FIELD_REQUIRED_BYTE_DICTIONARY = "columnRequiredByteDictionary";
    public static final String FIELD_REQUIRED_BINARY_DICTIONARY = "columnRequiredBinaryDictionary";
    public static final String FIELD_REQUIRED_BOXED_BINARY_DICTIONARY = "columnRequiredBoxedBinaryDictionary";
    public static final String FIELD_REQUIRED_DATE_DICTIONARY = "columnRequiredDateDictionary";
    public static final String FIELD_REQUIRED_OBJECT_ID_DICTIONARY = "columnRequiredObjectIdDictionary";
    public static final String FIELD_REQUIRED_UUID_DICTIONARY = "columnRequiredUUIDDictionary";
    public static final String FIELD_REQUIRED_DECIMAL128_DICTIONARY = "columnRequiredDecimal128Dictionary";

    public static final String[] INVALID_TYPES_FIELDS_FOR_DISTINCT
            = new String[]{FIELD_REALMOBJECT, FIELD_REALMLIST, FIELD_DOUBLE, FIELD_FLOAT,
            FIELD_STRING_LIST, FIELD_BINARY_LIST, FIELD_BOOLEAN_LIST, FIELD_LONG_LIST,
            FIELD_DOUBLE_LIST, FIELD_FLOAT_LIST, FIELD_DATE_LIST, FIELD_BOOLEAN_DICTIONARY,
            FIELD_STRING_DICTIONARY, FIELD_INTEGER_DICTIONARY, FIELD_FLOAT_DICTIONARY,
            FIELD_LONG_DICTIONARY, FIELD_SHORT_DICTIONARY, FIELD_DOUBLE_DICTIONARY,
            FIELD_BYTE_DICTIONARY, FIELD_BINARY_DICTIONARY,
            FIELD_DATE_DICTIONARY, FIELD_OBJECT_ID_DICTIONARY, FIELD_UUID_DICTIONARY,
            FIELD_DECIMAL128_DICTIONARY, FIELD_MIXED_DICTIONARY, FIELD_REQUIRED_BOOLEAN_DICTIONARY,
            FIELD_REQUIRED_STRING_DICTIONARY, FIELD_REQUIRED_INTEGER_DICTIONARY, FIELD_REQUIRED_FLOAT_DICTIONARY,
            FIELD_REQUIRED_LONG_DICTIONARY, FIELD_REQUIRED_SHORT_DICTIONARY, FIELD_REQUIRED_DOUBLE_DICTIONARY,
            FIELD_REQUIRED_BYTE_DICTIONARY, FIELD_REQUIRED_BINARY_DICTIONARY, FIELD_REQUIRED_BOXED_BINARY_DICTIONARY,
            FIELD_REQUIRED_DATE_DICTIONARY, FIELD_REQUIRED_OBJECT_ID_DICTIONARY, FIELD_REQUIRED_UUID_DICTIONARY};


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

    private RealmDictionary<DogPrimaryKey> columnRealmDictionary;

    private RealmDictionary<Boolean> columnBooleanDictionary;
    private RealmDictionary<String> columnStringDictionary;
    private RealmDictionary<Integer> columnIntegerDictionary;
    private RealmDictionary<Float> columnFloatDictionary;
    private RealmDictionary<Long> columnLongDictionary;
    private RealmDictionary<Short> columnShortDictionary;
    private RealmDictionary<Double> columnDoubleDictionary;
    private RealmDictionary<Byte> columnByteDictionary;
    private RealmDictionary<byte[]> columnBinaryDictionary;
    private RealmDictionary<Date> columnDateDictionary;
    private RealmDictionary<ObjectId> columnObjectIdDictionary;
    private RealmDictionary<UUID> columnUUIDDictionary;
    private RealmDictionary<Decimal128> columnDecimal128Dictionary;
    private RealmDictionary<Mixed> columnMixedDictionary;

    @Required
    private RealmDictionary<Boolean> columnRequiredBooleanDictionary;
    @Required
    private RealmDictionary<String> columnRequiredStringDictionary;
    @Required
    private RealmDictionary<Integer> columnRequiredIntegerDictionary;
    @Required
    private RealmDictionary<Float> columnRequiredFloatDictionary;
    @Required
    private RealmDictionary<Long> columnRequiredLongDictionary;
    @Required
    private RealmDictionary<Short> columnRequiredShortDictionary;
    @Required
    private RealmDictionary<Double> columnRequiredDoubleDictionary;
    @Required
    private RealmDictionary<Byte> columnRequiredByteDictionary;
    @Required
    private RealmDictionary<byte[]> columnRequiredBinaryDictionary;
    @Required
    private RealmDictionary<Date> columnRequiredDateDictionary;
    @Required
    private RealmDictionary<ObjectId> columnRequiredObjectIdDictionary;
    @Required
    private RealmDictionary<UUID> columnRequiredUUIDDictionary;
    @Required
    private RealmDictionary<Decimal128> columnRequiredDecimal128Dictionary;

//    private RealmSet<Boolean> columnBooleanSet;
    private RealmSet<String> columnStringSet;
//    private RealmSet<Integer> columnIntegerSet;
//    private RealmSet<Float> columnFloatSet;
//    private RealmSet<Long> columnLongSet;
//    private RealmSet<Short> columnShortSet;
//    private RealmSet<Double> columnDoubleSet;
//    private RealmSet<Byte> columnByteSet;
//    private RealmSet<byte[]> columnBinarySet;
//    private RealmSet<Date> columnDateSet;
//    private RealmSet<ObjectId> columnObjectIdSet;
//    private RealmSet<UUID> columnUUIDSet;
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


    public RealmDictionary<DogPrimaryKey> getColumnRealmDictionary() {
        return columnRealmDictionary;
    }

    public void setColumnRealmDictionary(RealmDictionary<DogPrimaryKey> columnRealmDictionary) {
        this.columnRealmDictionary = columnRealmDictionary;
    }

    public RealmDictionary<Boolean> getColumnBooleanDictionary() {
        return columnBooleanDictionary;
    }

    public void setColumnBooleanDictionary(RealmDictionary<Boolean> columnBooleanDictionary) {
        this.columnBooleanDictionary = columnBooleanDictionary;
    }

    public RealmDictionary<String> getColumnStringDictionary() {
        return columnStringDictionary;
    }

    public void setColumnStringDictionary(RealmDictionary<String> columnStringDictionary) {
        this.columnStringDictionary = columnStringDictionary;
    }

    public RealmDictionary<Integer> getColumnIntegerDictionary() {
        return columnIntegerDictionary;
    }

    public void setColumnIntegerDictionary(RealmDictionary<Integer> columnIntegerDictionary) {
        this.columnIntegerDictionary = columnIntegerDictionary;
    }

    public RealmDictionary<Float> getColumnFloatDictionary() {
        return columnFloatDictionary;
    }

    public void setColumnFloatDictionary(RealmDictionary<Float> columnFloatDictionary) {
        this.columnFloatDictionary = columnFloatDictionary;
    }

    public RealmDictionary<Long> getColumnLongDictionary() {
        return columnLongDictionary;
    }

    public void setColumnLongDictionary(RealmDictionary<Long> columnLongDictionary) {
        this.columnLongDictionary = columnLongDictionary;
    }

    public RealmDictionary<Short> getColumnShortDictionary() {
        return columnShortDictionary;
    }

    public void setColumnShortDictionary(RealmDictionary<Short> columnShortDictionary) {
        this.columnShortDictionary = columnShortDictionary;
    }

    public RealmDictionary<Double> getColumnDoubleDictionary() {
        return columnDoubleDictionary;
    }

    public void setColumnDoubleDictionary(RealmDictionary<Double> columnDoubleDictionary) {
        this.columnDoubleDictionary = columnDoubleDictionary;
    }

    public RealmDictionary<Byte> getColumnByteDictionary() {
        return columnByteDictionary;
    }

    public void setColumnByteDictionary(RealmDictionary<Byte> columnByteDictionary) {
        this.columnByteDictionary = columnByteDictionary;
    }

    public RealmDictionary<byte[]> getColumnBinaryDictionary() {
        return columnBinaryDictionary;
    }

    public void setColumnBinaryDictionary(RealmDictionary<byte[]> columnBinaryDictionary) {
        this.columnBinaryDictionary = columnBinaryDictionary;
    }

    public RealmDictionary<Date> getColumnDateDictionary() {
        return columnDateDictionary;
    }

    public void setColumnDateDictionary(RealmDictionary<Date> columnDateDictionary) {
        this.columnDateDictionary = columnDateDictionary;
    }

    public RealmDictionary<ObjectId> getColumnObjectIdDictionary() {
        return columnObjectIdDictionary;
    }

    public void setColumnObjectIdDictionary(RealmDictionary<ObjectId> columnObjectIdDictionary) {
        this.columnObjectIdDictionary = columnObjectIdDictionary;
    }

    public RealmDictionary<UUID> getColumnUUIDDictionary() {
        return columnUUIDDictionary;
    }

    public void setColumnUUIDDictionary(RealmDictionary<UUID> columnUUIDDictionary) {
        this.columnUUIDDictionary = columnUUIDDictionary;
    }

    public RealmDictionary<Decimal128> getColumnDecimal128Dictionary() {
        return columnDecimal128Dictionary;
    }

    public void setColumnDecimal128Dictionary(RealmDictionary<Decimal128> columnDecimal128Dictionary) {
        this.columnDecimal128Dictionary = columnDecimal128Dictionary;
    }

    public RealmDictionary<Mixed> getColumnMixedDictionary() {
        return columnMixedDictionary;
    }

    public void setColumnMixedDictionary(RealmDictionary<Mixed> columnMixedDictionary) {
        this.columnMixedDictionary = columnMixedDictionary;
    }

    public RealmDictionary<Boolean> getColumnRequiredBooleanDictionary() {
        return columnRequiredBooleanDictionary;
    }

    public void setColumnRequiredBooleanDictionary(RealmDictionary<Boolean> columnRequiredBooleanDictionary) {
        this.columnRequiredBooleanDictionary = columnRequiredBooleanDictionary;
    }

    public RealmDictionary<String> getColumnRequiredStringDictionary() {
        return columnRequiredStringDictionary;
    }

    public void setColumnRequiredStringDictionary(RealmDictionary<String> columnRequiredStringDictionary) {
        this.columnRequiredStringDictionary = columnRequiredStringDictionary;
    }

    public RealmDictionary<Integer> getColumnRequiredIntegerDictionary() {
        return columnRequiredIntegerDictionary;
    }

    public void setColumnRequiredIntegerDictionary(RealmDictionary<Integer> columnRequiredIntegerDictionary) {
        this.columnRequiredIntegerDictionary = columnRequiredIntegerDictionary;
    }

    public RealmDictionary<Float> getColumnRequiredFloatDictionary() {
        return columnRequiredFloatDictionary;
    }

    public void setColumnRequiredFloatDictionary(RealmDictionary<Float> columnRequiredFloatDictionary) {
        this.columnRequiredFloatDictionary = columnRequiredFloatDictionary;
    }

    public RealmDictionary<Long> getColumnRequiredLongDictionary() {
        return columnRequiredLongDictionary;
    }

    public void setColumnRequiredLongDictionary(RealmDictionary<Long> columnRequiredLongDictionary) {
        this.columnRequiredLongDictionary = columnRequiredLongDictionary;
    }

    public RealmDictionary<Short> getColumnRequiredShortDictionary() {
        return columnRequiredShortDictionary;
    }

    public void setColumnRequiredShortDictionary(RealmDictionary<Short> columnRequiredShortDictionary) {
        this.columnRequiredShortDictionary = columnRequiredShortDictionary;
    }

    public RealmDictionary<Double> getColumnRequiredDoubleDictionary() {
        return columnRequiredDoubleDictionary;
    }

    public void setColumnRequiredDoubleDictionary(RealmDictionary<Double> columnRequiredDoubleDictionary) {
        this.columnRequiredDoubleDictionary = columnRequiredDoubleDictionary;
    }

    public RealmDictionary<Byte> getColumnRequiredByteDictionary() {
        return columnRequiredByteDictionary;
    }

    public void setColumnRequiredByteDictionary(RealmDictionary<Byte> columnRequiredByteDictionary) {
        this.columnRequiredByteDictionary = columnRequiredByteDictionary;
    }

    public RealmDictionary<byte[]> getColumnRequiredBinaryDictionary() {
        return columnRequiredBinaryDictionary;
    }

    public void setColumnRequiredBinaryDictionary(RealmDictionary<byte[]> columnRequiredBinaryDictionary) {
        this.columnRequiredBinaryDictionary = columnRequiredBinaryDictionary;
    }


    public RealmDictionary<Date> getColumnRequiredDateDictionary() {
        return columnRequiredDateDictionary;
    }

    public void setColumnRequiredDateDictionary(RealmDictionary<Date> columnRequiredDateDictionary) {
        this.columnRequiredDateDictionary = columnRequiredDateDictionary;
    }

    public RealmDictionary<ObjectId> getColumnRequiredObjectIdDictionary() {
        return columnRequiredObjectIdDictionary;
    }

    public void setColumnRequiredObjectIdDictionary(RealmDictionary<ObjectId> columnRequiredObjectIdDictionary) {
        this.columnRequiredObjectIdDictionary = columnRequiredObjectIdDictionary;
    }

    public RealmDictionary<UUID> getColumnRequiredUUIDDictionary() {
        return columnRequiredUUIDDictionary;
    }

    public void setColumnRequiredUUIDDictionary(RealmDictionary<UUID> columnRequiredUUIDDictionary) {
        this.columnRequiredUUIDDictionary = columnRequiredUUIDDictionary;
    }

    public RealmDictionary<Decimal128> getColumnRequiredDecimal128Dictionary() {
        return columnRequiredDecimal128Dictionary;
    }

    public void setColumnRequiredDecimal128Dictionary(RealmDictionary<Decimal128> columnRequiredDecimal128Dictionary) {
        this.columnRequiredDecimal128Dictionary = columnRequiredDecimal128Dictionary;
    }

    public RealmSet<String> getColumnStringSet() {
        return columnStringSet;
    }

    public void setColumnStringSet(RealmSet<String> columnStringSet) {
        this.columnStringSet = columnStringSet;
    }
}
