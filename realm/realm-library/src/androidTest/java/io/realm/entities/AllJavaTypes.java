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

import io.realm.Mixed;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;


public class AllJavaTypes extends RealmObject {

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
    public static final String FIELD_MIXED = "fieldMixed";
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
    private Mixed fieldMixed;
    private AllJavaTypes fieldObject;
    private RealmList<AllJavaTypes> fieldList;

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

    @LinkingObjects(FIELD_OBJECT)
    private final RealmResults<AllJavaTypes> objectParents = null;

    @LinkingObjects(FIELD_LIST)
    private final RealmResults<AllJavaTypes> listParents = null;

    public AllJavaTypes() {
    }

    public AllJavaTypes(long fieldLong) {
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

    public AllJavaTypes getFieldObject() {
        return fieldObject;
    }

    public void setFieldObject(AllJavaTypes columnRealmObject) {
        this.fieldObject = columnRealmObject;
    }

    public RealmList<AllJavaTypes> getFieldList() {
        return fieldList;
    }

    public void setFieldList(RealmList<AllJavaTypes> columnRealmList) {
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

    public Mixed getFieldMixed() {
        return fieldMixed;
    }

    public void setFieldMixed(Mixed fieldMixed) {
        this.fieldMixed = fieldMixed;
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

    public RealmResults<AllJavaTypes> getObjectParents() {
        return objectParents;
    }

    public RealmResults<AllJavaTypes> getListParents() {
        return listParents;
    }
}
