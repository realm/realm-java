/*
 * Copyright 2015 Realm Inc.
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

import java.util.Date;
import java.util.UUID;

import io.realm.RealmAny;
import io.realm.RealmDictionary;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.RealmSet;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;


public class AllJavaTypesUnsupportedTypes extends RealmObject {

    public static final String CLASS_NAME = "AllJavaTypes";

    public static final String FIELD_IGNORED = "fieldIgnored";
    public static final String FIELD_STRING = "fieldString";
    public static final String FIELD_SHORT = "fieldShort";
    public static final String FIELD_INT = "fieldInt";
    public static final String FIELD_LONG = "fieldLong";
    public static final String FIELD_ID = "fieldId";
    public static final String FIELD_BYTE = "fieldByte";
    public static final String FIELD_FLOAT = "fieldFloat";
    public static final String FIELD_DOUBLE = "fieldDouble";
    public static final String FIELD_BOOLEAN = "fieldBoolean";
    public static final String FIELD_DATE = "fieldDate";
    public static final String FIELD_BINARY = "fieldBinary";
    public static final String FIELD_DECIMAL128 = "fieldDecimal128";
    public static final String FIELD_OBJECT_ID = "fieldObjectId";
    public static final String FIELD_UUID = "fieldUUID";
    public static final String FIELD_REALM_ANY = "fieldRealmAny";
    public static final String FIELD_OBJECT = "fieldObject";
    public static final String FIELD_LIST = "fieldList";

    public static final String FIELD_STRING_LIST = "fieldStringList";
    public static final String FIELD_BINARY_LIST = "fieldBinaryList";
    public static final String FIELD_BOOLEAN_LIST = "fieldBooleanList";
    public static final String FIELD_LONG_LIST = "fieldLongList";
    public static final String FIELD_INTEGER_LIST = "fieldIntegerList";
    public static final String FIELD_SHORT_LIST = "fieldShortList";
    public static final String FIELD_BYTE_LIST = "fieldByteList";
    public static final String FIELD_DOUBLE_LIST = "fieldDoubleList";
    public static final String FIELD_FLOAT_LIST = "fieldFloatList";
    public static final String FIELD_DATE_LIST = "fieldDateList";
    public static final String FIELD_DECIMAL128_LIST = "fieldDecimal128List";
    public static final String FIELD_OBJECT_ID_LIST = "fieldObjectIdList";
    public static final String FIELD_UUID_LIST = "fieldUUIDList";
    public static final String FIELD_REALM_ANY_LIST = "fieldRealmAnyList";

    public static final String FIELD_LINK_DICTIONARY = "columnRealmDictionary";
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
    public static final String FIELD_REALM_ANY_DICTIONARY = "columnRealmAnyDictionary";

    public static final String FIELD_LINK_SET = "columnRealmSet";
    public static final String FIELD_BOOLEAN_SET = "columnBooleanSet";
    public static final String FIELD_STRING_SET = "columnStringSet";
    public static final String FIELD_INTEGER_SET = "columnIntegerSet";
    public static final String FIELD_FLOAT_SET = "columnFloatSet";
    public static final String FIELD_LONG_SET = "columnLongSet";
    public static final String FIELD_SHORT_SET = "columnShortSet";
    public static final String FIELD_DOUBLE_SET = "columnDoubleSet";
    public static final String FIELD_BYTE_SET = "columnByteSet";
    public static final String FIELD_BINARY_SET = "columnBinarySet";
    public static final String FIELD_DATE_SET = "columnDateSet";
    public static final String FIELD_OBJECT_ID_SET = "columnObjectIdSet";
    public static final String FIELD_UUID_SET = "columnUUIDSet";
    public static final String FIELD_DECIMAL128_SET = "columnDecimal128Set";
    public static final String FIELD_REALM_ANY_SET = "columnRealmAnySet";

    public static final String FIELD_LO_OBJECT = "objectParents";
    public static final String FIELD_LO_LIST = "listParents";

    public static final String[] INVALID_FIELD_TYPES_FOR_DISTINCT = new String[] {
            FIELD_OBJECT + "." + FIELD_LIST,
            FIELD_OBJECT + "." + FIELD_STRING_LIST,
            FIELD_OBJECT + "." + FIELD_BINARY_LIST,
            FIELD_OBJECT + "." + FIELD_BOOLEAN_LIST,
            FIELD_OBJECT + "." + FIELD_LONG_LIST,
            FIELD_OBJECT + "." + FIELD_INTEGER_LIST,
            FIELD_OBJECT + "." + FIELD_SHORT_LIST,
            FIELD_OBJECT + "." + FIELD_BYTE_LIST,
            FIELD_OBJECT + "." + FIELD_DOUBLE_LIST,
            FIELD_OBJECT + "." + FIELD_FLOAT_LIST,
            FIELD_OBJECT + "." + FIELD_DATE_LIST,
            FIELD_OBJECT + "." + FIELD_DECIMAL128_LIST,
            FIELD_OBJECT + "." + FIELD_OBJECT_ID_LIST,
            FIELD_OBJECT + "." + FIELD_UUID_LIST,
    };

    @Ignore
    private String fieldIgnored;
    @Index
    private String fieldString;
    @PrimaryKey
    private long fieldId;
    private long fieldLong;
    private short fieldShort;
    private int fieldInt;
    private byte fieldByte;
    private float fieldFloat;
    private double fieldDouble;
    private boolean fieldBoolean;
    private Date fieldDate;
    private byte[] fieldBinary;
    private Decimal128 fieldDecimal128;
    private ObjectId fieldObjectId;
    private UUID fieldUUID;
    private RealmAny fieldRealmAny;
    private AllJavaTypesUnsupportedTypes fieldObject;
    private RealmList<AllJavaTypesUnsupportedTypes> fieldList;

    private RealmList<String> fieldStringList;
    private RealmList<byte[]> fieldBinaryList;
    private RealmList<Boolean> fieldBooleanList;
    private RealmList<Long> fieldLongList;
    private RealmList<Integer> fieldIntegerList;
    private RealmList<Short> fieldShortList;
    private RealmList<Byte> fieldByteList;
    private RealmList<Double> fieldDoubleList;
    private RealmList<Float> fieldFloatList;
    private RealmList<Date> fieldDateList;
    private RealmList<Decimal128> fieldDecimal128List;
    private RealmList<ObjectId> fieldObjectIdList;
    private RealmList<UUID> fieldUUIDList;
    private RealmList<RealmAny> fieldRealmAnyList;

    private RealmDictionary<AllJavaTypesUnsupportedTypes> columnRealmDictionary;
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
    private RealmDictionary<RealmAny> columnRealmAnyDictionary;

    private RealmSet<AllJavaTypesUnsupportedTypes> columnRealmSet;
    private RealmSet<Boolean> columnBooleanSet;
    private RealmSet<String> columnStringSet;
    private RealmSet<Integer> columnIntegerSet;
    private RealmSet<Float> columnFloatSet;
    private RealmSet<Long> columnLongSet;
    private RealmSet<Short> columnShortSet;
    private RealmSet<Double> columnDoubleSet;
    private RealmSet<Byte> columnByteSet;
    private RealmSet<byte[]> columnBinarySet;
    private RealmSet<Date> columnDateSet;
    private RealmSet<ObjectId> columnObjectIdSet;
    private RealmSet<UUID> columnUUIDSet;
    private RealmSet<Decimal128> columnDecimal128Set;
    private RealmSet<RealmAny> columnRealmAnySet;

    @LinkingObjects(FIELD_OBJECT)
    private final RealmResults<AllJavaTypesUnsupportedTypes> objectParents = null;

    @LinkingObjects(FIELD_LIST)
    private final RealmResults<AllJavaTypesUnsupportedTypes> listParents = null;

    public AllJavaTypesUnsupportedTypes() {
    }

    public AllJavaTypesUnsupportedTypes(long fieldLong) {
        this.fieldId = fieldLong;
        this.fieldLong = fieldLong;
    }

    public String getFieldIgnored() {
        return fieldIgnored;
    }

    public void setFieldIgnored(String fieldIgnored) {
        this.fieldIgnored = fieldIgnored;
    }

    public String getFieldString() {
        return fieldString;
    }

    public void setFieldString(String fieldString) {
        this.fieldString = fieldString;
    }

    public short getFieldShort() {
        return fieldShort;
    }

    public void setFieldShort(short fieldShort) {
        this.fieldShort = fieldShort;
    }

    public long getFieldLong() {
        return fieldLong;
    }

    public void setFieldLong(long fieldLong) {
        this.fieldLong = fieldLong;
    }

    public int getFieldInt() {
        return fieldInt;
    }

    public void setFieldInt(int fieldInt) {
        this.fieldInt = fieldInt;
    }

    public long getFieldId() {
        return fieldId;
    }

    public void setFieldId(long fieldId) {
        this.fieldId = fieldId;
    }

    public byte getFieldByte() {
        return fieldByte;
    }

    public void setFieldByte(byte fieldByte) {
        this.fieldByte = fieldByte;
    }

    public float getFieldFloat() {
        return fieldFloat;
    }

    public void setFieldFloat(float fieldFloat) {
        this.fieldFloat = fieldFloat;
    }

    public double getFieldDouble() {
        return fieldDouble;
    }

    public void setFieldDouble(double fieldDouble) {
        this.fieldDouble = fieldDouble;
    }

    public boolean isFieldBoolean() {
        return fieldBoolean;
    }

    public void setFieldBoolean(boolean fieldBoolean) {
        this.fieldBoolean = fieldBoolean;
    }

    public Date getFieldDate() {
        return fieldDate;
    }

    public void setFieldDate(Date fieldDate) {
        this.fieldDate = fieldDate;
    }

    public byte[] getFieldBinary() {
        return fieldBinary;
    }

    public void setFieldBinary(byte[] fieldBinary) {
        this.fieldBinary = fieldBinary;
    }

    public AllJavaTypesUnsupportedTypes getFieldObject() {
        return fieldObject;
    }

    public void setFieldObject(AllJavaTypesUnsupportedTypes columnRealmObject) {
        this.fieldObject = columnRealmObject;
    }

    public RealmList<AllJavaTypesUnsupportedTypes> getFieldList() {
        return fieldList;
    }

    public void setFieldList(RealmList<AllJavaTypesUnsupportedTypes> columnRealmList) {
        this.fieldList = columnRealmList;
    }

    public RealmList<String> getFieldStringList() {
        return fieldStringList;
    }

    public void setFieldStringList(RealmList<String> fieldStringList) {
        this.fieldStringList = fieldStringList;
    }

    public RealmList<byte[]> getFieldBinaryList() {
        return fieldBinaryList;
    }

    public void setFieldBinaryList(RealmList<byte[]> fieldBinaryList) {
        this.fieldBinaryList = fieldBinaryList;
    }

    public RealmList<Boolean> getFieldBooleanList() {
        return fieldBooleanList;
    }

    public void setFieldBooleanList(RealmList<Boolean> fieldBooleanList) {
        this.fieldBooleanList = fieldBooleanList;
    }

    public RealmList<Long> getFieldLongList() {
        return fieldLongList;
    }

    public void setFieldLongList(RealmList<Long> fieldLongList) {
        this.fieldLongList = fieldLongList;
    }

    public RealmList<Integer> getFieldIntegerList() {
        return fieldIntegerList;
    }

    public void setFieldIntegerList(RealmList<Integer> fieldIntegerList) {
        this.fieldIntegerList = fieldIntegerList;
    }

    public RealmList<Short> getFieldShortList() {
        return fieldShortList;
    }

    public void setFieldShortList(RealmList<Short> fieldShortList) {
        this.fieldShortList = fieldShortList;
    }

    public RealmList<Byte> getFieldByteList() {
        return fieldByteList;
    }

    public void setFieldByteList(RealmList<Byte> fieldByteList) {
        this.fieldByteList = fieldByteList;
    }

    public RealmList<Double> getFieldDoubleList() {
        return fieldDoubleList;
    }

    public void setFieldDoubleList(RealmList<Double> fieldDoubleList) {
        this.fieldDoubleList = fieldDoubleList;
    }

    public RealmList<Float> getFieldFloatList() {
        return fieldFloatList;
    }

    public void setFieldFloatList(RealmList<Float> fieldFloatList) {
        this.fieldFloatList = fieldFloatList;
    }

    public RealmList<Date> getFieldDateList() {
        return fieldDateList;
    }

    public void setFieldDateList(RealmList<Date> fieldDateList) {
        this.fieldDateList = fieldDateList;
    }

    public Decimal128 getFieldDecimal128() {
        return fieldDecimal128;
    }

    public void setFieldDecimal128(Decimal128 fieldDecimal128) {
        this.fieldDecimal128 = fieldDecimal128;
    }

    public ObjectId getFieldObjectId() {
        return fieldObjectId;
    }

    public void setFieldObjectId(ObjectId fieldObjectId) {
        this.fieldObjectId = fieldObjectId;
    }

    public UUID getFieldUUID() {
        return fieldUUID;
    }

    public void setFieldUUID(UUID fieldUUID) {
        this.fieldUUID = fieldUUID;
    }

    public RealmAny getFieldRealmAny() {
        return fieldRealmAny;
    }

    public void setFieldRealmAny(RealmAny fieldRealmAny) {
        this.fieldRealmAny = fieldRealmAny;
    }

    public RealmList<Decimal128> getFieldDecimal128List() {
        return fieldDecimal128List;
    }

    public void setFieldDecimal128List(RealmList<Decimal128> fieldDecimal128List) {
        this.fieldDecimal128List = fieldDecimal128List;
    }

    public RealmList<ObjectId> getFieldObjectIdList() {
        return fieldObjectIdList;
    }

    public void setFieldObjectIdList(RealmList<ObjectId> fieldObjectIdList) {
        this.fieldObjectIdList = fieldObjectIdList;
    }

    public RealmList<UUID> getFieldUUIDList() {
        return fieldUUIDList;
    }

    public void setFieldUUIDList(RealmList<UUID> fieldUUIDList) {
        this.fieldUUIDList = fieldUUIDList;
    }

    public RealmList<RealmAny> getFieldRealmAnyList() {
        return fieldRealmAnyList;
    }

    public void setFieldRealmAnyList(RealmList<RealmAny> fieldRealmAnyList) {
        this.fieldRealmAnyList = fieldRealmAnyList;
    }

    public RealmResults<AllJavaTypesUnsupportedTypes> getObjectParents() {
        return objectParents;
    }

    public RealmResults<AllJavaTypesUnsupportedTypes> getListParents() {
        return listParents;
    }

    public RealmDictionary<AllJavaTypesUnsupportedTypes> getColumnRealmDictionary() {
        return columnRealmDictionary;
    }

    public void setColumnRealmDictionary(RealmDictionary<AllJavaTypesUnsupportedTypes> columnRealmDictionary) {
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

    public RealmDictionary<RealmAny> getColumnRealmAnyDictionary() {
        return columnRealmAnyDictionary;
    }

    public void setColumnRealmAnyDictionary(RealmDictionary<RealmAny> columnRealmAnyDictionary) {
        this.columnRealmAnyDictionary = columnRealmAnyDictionary;
    }

    public RealmSet<AllJavaTypesUnsupportedTypes> getColumnRealmSet() {
        return columnRealmSet;
    }

    public void setColumnRealmSet(RealmSet<AllJavaTypesUnsupportedTypes> columnRealmSet) {
        this.columnRealmSet = columnRealmSet;
    }

    public RealmSet<Boolean> getColumnBooleanSet() {
        return columnBooleanSet;
    }

    public void setColumnBooleanSet(RealmSet<Boolean> columnBooleanSet) {
        this.columnBooleanSet = columnBooleanSet;
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

    public RealmSet<Double> getColumnDoubleSet() {
        return columnDoubleSet;
    }

    public void setColumnDoubleSet(RealmSet<Double> columnDoubleSet) {
        this.columnDoubleSet = columnDoubleSet;
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

    public RealmSet<Date> getColumnDateSet() {
        return columnDateSet;
    }

    public void setColumnDateSet(RealmSet<Date> columnDateSet) {
        this.columnDateSet = columnDateSet;
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

    public void setColumnUUIDSet(RealmSet<UUID> columnUUISet) {
        this.columnUUIDSet = columnUUISet;
    }

    public RealmSet<Decimal128> getColumnDecimal128Set() {
        return columnDecimal128Set;
    }

    public void setColumnDecimal128Set(RealmSet<Decimal128> columnDecimal12Set) {
        this.columnDecimal128Set = columnDecimal12Set;
    }

    public RealmSet<RealmAny> getColumnRealmAnySet() {
        return columnRealmAnySet;
    }

    public void setColumnRealmAnySet(RealmSet<RealmAny> columnRealmAnySet) {
        this.columnRealmAnySet = columnRealmAnySet;
    }
}
