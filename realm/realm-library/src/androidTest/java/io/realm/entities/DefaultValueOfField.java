/*
 * Copyright 2016 Realm Inc.
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

import java.util.Date;
import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class DefaultValueOfField extends RealmObject {

    public static final String CLASS_NAME = "DefaultValueOfField";
    public static final String FIELD_IGNORED = "fieldIgnored";
    public static final String FIELD_RANDOM_STRING = "fieldRandomString";
    public static final String FIELD_STRING = "fieldString";
    public static final String FIELD_SHORT = "fieldShort";
    public static final String FIELD_INT = "fieldInt";
    public static final String FIELD_LONG_PRIMARY_KEY = "fieldLongPrimaryKey";
    public static final String FIELD_LONG = "fieldLong";
    public static final String FIELD_BYTE = "fieldByte";
    public static final String FIELD_FLOAT = "fieldFloat";
    public static final String FIELD_DOUBLE = "fieldDouble";
    public static final String FIELD_BOOLEAN = "fieldBoolean";
    public static final String FIELD_DATE = "fieldDate";
    public static final String FIELD_BINARY = "fieldBinary";
    public static final String FIELD_OBJECT = "fieldObject";
    public static final String FIELD_LIST = "fieldList";


    public static final String FIELD_IGNORED_DEFAULT_VALUE = "ignored";
    public static final String FIELD_STRING_DEFAULT_VALUE = "defaultString";
    public static final short FIELD_SHORT_DEFAULT_VALUE = 1234;
    public static final int FIELD_INT_DEFAULT_VALUE = 123456;
    public static final long FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE = 2L * Integer.MAX_VALUE;
    public static final long FIELD_LONG_DEFAULT_VALUE = 3L * Integer.MAX_VALUE;
    public static final byte FIELD_BYTE_DEFAULT_VALUE = 100;
    public static final float FIELD_FLOAT_DEFAULT_VALUE = 0.5f;
    public static final double FIELD_DOUBLE_DEFAULT_VALUE = 0.25;
    public static final boolean FIELD_BOOLEAN_DEFAULT_VALUE = true;
    public static final Date FIELD_DATE_DEFAULT_VALUE = new Date(1473691826000L /*2016/9/12 23:56:26 JST*/);
    public static final byte[] FIELD_BINARY_DEFAULT_VALUE = new byte[] {123, -100, 0, 2};
    public static final RandomPrimaryKey FIELD_OBJECT_DEFAULT_VALUE;
    public static final RealmList<RandomPrimaryKey> FIELD_LIST_DEFAULT_VALUE;
    public static final RealmList<String> FIELD_STRING_LIST_DEFAULT_VALUE;
    public static final RealmList<Boolean> FIELD_BOOLEAN_LIST_DEFAULT_VALUE;
    public static final RealmList<byte[]> FIELD_BINARY_LIST_DEFAULT_VALUE;
    public static final RealmList<Long> FIELD_LONG_LIST_DEFAULT_VALUE;
    public static final RealmList<Integer> FIELD_INTEGER_LIST_DEFAULT_VALUE;
    public static final RealmList<Short> FIELD_SHORT_LIST_DEFAULT_VALUE;
    public static final RealmList<Byte> FIELD_BYTE_LIST_DEFAULT_VALUE;
    public static final RealmList<Double> FIELD_DOUBLE_LIST_DEFAULT_VALUE;
    public static final RealmList<Float> FIELD_FLOAT_LIST_DEFAULT_VALUE;
    public static final RealmList<Date> FIELD_DATE_LIST_DEFAULT_VALUE;

    static {
        FIELD_OBJECT_DEFAULT_VALUE = new RandomPrimaryKey();
        FIELD_LIST_DEFAULT_VALUE = new RealmList<RandomPrimaryKey>();
        FIELD_LIST_DEFAULT_VALUE.add(new RandomPrimaryKey());

        FIELD_STRING_LIST_DEFAULT_VALUE = new RealmList<>("1");
        FIELD_BOOLEAN_LIST_DEFAULT_VALUE = new RealmList<>(true);
        FIELD_BINARY_LIST_DEFAULT_VALUE = new RealmList<>(new byte[] {1});
        FIELD_LONG_LIST_DEFAULT_VALUE = new RealmList<>(1L);
        FIELD_INTEGER_LIST_DEFAULT_VALUE = new RealmList<>(1);
        FIELD_SHORT_LIST_DEFAULT_VALUE = new RealmList<>((short) 1);
        FIELD_BYTE_LIST_DEFAULT_VALUE = new RealmList<>((byte) 1);
        FIELD_DOUBLE_LIST_DEFAULT_VALUE = new RealmList<>(1D);
        FIELD_FLOAT_LIST_DEFAULT_VALUE = new RealmList<>(1F);
        FIELD_DATE_LIST_DEFAULT_VALUE = new RealmList<>(new Date(1));
    }

    public static String lastRandomStringValue;

    @Ignore private String fieldIgnored = FIELD_IGNORED_DEFAULT_VALUE;
    private String fieldString = FIELD_STRING_DEFAULT_VALUE;
    private String fieldRandomString = lastRandomStringValue = UUID.randomUUID().toString();
    private short fieldShort = FIELD_SHORT_DEFAULT_VALUE;
    private int fieldInt = FIELD_INT_DEFAULT_VALUE;
    @PrimaryKey private long fieldLongPrimaryKey = FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE;
    private long fieldLong = FIELD_LONG_DEFAULT_VALUE;
    private byte fieldByte = FIELD_BYTE_DEFAULT_VALUE;
    private float fieldFloat = FIELD_FLOAT_DEFAULT_VALUE;
    private double fieldDouble = FIELD_DOUBLE_DEFAULT_VALUE;
    private boolean fieldBoolean = FIELD_BOOLEAN_DEFAULT_VALUE;
    private Date fieldDate = FIELD_DATE_DEFAULT_VALUE;
    private byte[] fieldBinary = FIELD_BINARY_DEFAULT_VALUE;
    private RandomPrimaryKey fieldObject = FIELD_OBJECT_DEFAULT_VALUE;
    private RealmList<RandomPrimaryKey> fieldList = FIELD_LIST_DEFAULT_VALUE;

    private RealmList<String> fieldStringList = FIELD_STRING_LIST_DEFAULT_VALUE;
    private RealmList<byte[]> fieldBinaryList = FIELD_BINARY_LIST_DEFAULT_VALUE;
    private RealmList<Boolean> fieldBooleanList = FIELD_BOOLEAN_LIST_DEFAULT_VALUE;
    private RealmList<Long> fieldLongList = FIELD_LONG_LIST_DEFAULT_VALUE;
    private RealmList<Integer> fieldIntegerList = FIELD_INTEGER_LIST_DEFAULT_VALUE;
    private RealmList<Short> fieldShortList = FIELD_SHORT_LIST_DEFAULT_VALUE;
    private RealmList<Byte> fieldByteList = FIELD_BYTE_LIST_DEFAULT_VALUE;
    private RealmList<Double> fieldDoubleList = FIELD_DOUBLE_LIST_DEFAULT_VALUE;
    private RealmList<Float> fieldFloatList = FIELD_FLOAT_LIST_DEFAULT_VALUE;
    private RealmList<Date> fieldDateList = FIELD_DATE_LIST_DEFAULT_VALUE;

    public DefaultValueOfField() {
    }

    public DefaultValueOfField(long fieldLong) {
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

    public String getFieldRandomString() {
        return fieldRandomString;
    }

    public void setFieldRandomString(String fieldRandomString) {
        this.fieldRandomString = fieldRandomString;
    }

    public short getFieldShort() {
        return fieldShort;
    }

    public void setFieldShort(short fieldShort) {
        this.fieldShort = fieldShort;
    }

    public int getFieldInt() {
        return fieldInt;
    }

    public void setFieldInt(int fieldInt) {
        this.fieldInt = fieldInt;
    }

    public long getFieldLongPrimaryKey() {
        return fieldLongPrimaryKey;
    }

    public void setFieldLongPrimaryKey(long fieldLongPrimaryKey) {
        this.fieldLongPrimaryKey = fieldLongPrimaryKey;
    }

    public long getFieldLong() {
        return fieldLong;
    }

    public void setFieldLong(long fieldLong) {
        this.fieldLong = fieldLong;
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

    public RandomPrimaryKey getFieldObject() {
        return fieldObject;
    }

    public void setFieldObject(RandomPrimaryKey fieldObject) {
        this.fieldObject = fieldObject;
    }

    public RealmList<RandomPrimaryKey> getFieldList() {
        return fieldList;
    }

    public void setFieldList(RealmList<RandomPrimaryKey> fieldList) {
        this.fieldList = fieldList;
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
}
