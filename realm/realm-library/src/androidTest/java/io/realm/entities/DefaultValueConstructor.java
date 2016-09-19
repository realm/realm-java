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

public class DefaultValueConstructor extends RealmObject {

    public static final String CLASS_NAME = "DefaultValueOfField";
    public static String FIELD_IGNORED = "fieldIgnored";
    public static String FIELD_RANDOM_STRING = "fieldRandomString";
    public static String FIELD_STRING = "fieldString";
    public static String FIELD_SHORT = "fieldShort";
    public static String FIELD_INT = "fieldInt";
    public static String FIELD_LONG_PRIMARY_KEY = "fieldLongPrimaryKey";
    public static String FIELD_LONG = "fieldLong";
    public static String FIELD_BYTE = "fieldByte";
    public static String FIELD_FLOAT = "fieldFloat";
    public static String FIELD_DOUBLE = "fieldDouble";
    public static String FIELD_BOOLEAN = "fieldBoolean";
    public static String FIELD_DATE = "fieldDate";
    public static String FIELD_BINARY = "fieldBinary";
    public static String FIELD_OBJECT = "fieldObject";
    public static String FIELD_LIST = "fieldList";


    public static String FIELD_IGNORED_DEFAULT_VALUE = "ignored";
    public static String FIELD_STRING_DEFAULT_VALUE = "defaultString";
    public static short FIELD_SHORT_DEFAULT_VALUE = 1234;
    public static int FIELD_INT_DEFAULT_VALUE = 123456;
    public static long FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE = 2L * Integer.MAX_VALUE;
    public static long FIELD_LONG_DEFAULT_VALUE = 3L * Integer.MAX_VALUE;
    public static byte FIELD_BYTE_DEFAULT_VALUE = 100;
    public static float FIELD_FLOAT_DEFAULT_VALUE = 0.5f;
    public static double FIELD_DOUBLE_DEFAULT_VALUE = 0.25;
    public static boolean FIELD_BOOLEAN_DEFAULT_VALUE = true;
    public static Date FIELD_DATE_DEFAULT_VALUE = new Date(1473691826000L /*2016/9/12 23:56:26 JST*/);
    public static byte[] FIELD_BINARY_DEFAULT_VALUE = new byte[] {123, -100, 0, 2};
    public static RandomPrimaryKey FIELD_OBJECT_DEFAULT_VALUE;
    public static RealmList<RandomPrimaryKey> FIELD_LIST_DEFAULT_VALUE;

    static {
        FIELD_OBJECT_DEFAULT_VALUE = new RandomPrimaryKey();
        FIELD_LIST_DEFAULT_VALUE = new RealmList<RandomPrimaryKey>();
        FIELD_LIST_DEFAULT_VALUE.add(new RandomPrimaryKey());
    }

    public static String lastRandomStringValue;

    @Ignore private String fieldIgnored;
    private String fieldString;
    private String fieldRandomString;
    private short fieldShort;
    private int fieldInt;
    @PrimaryKey private long fieldLongPrimaryKey;
    private long fieldLong;
    private byte fieldByte;
    private float fieldFloat;
    private double fieldDouble;
    private boolean fieldBoolean;
    private Date fieldDate;
    private byte[] fieldBinary;
    private RandomPrimaryKey fieldObject;
    private RealmList<RandomPrimaryKey> fieldList;

    public DefaultValueConstructor() {
        fieldIgnored = FIELD_IGNORED_DEFAULT_VALUE;
        fieldString = FIELD_STRING_DEFAULT_VALUE;
        fieldRandomString = lastRandomStringValue = UUID.randomUUID().toString();
        fieldShort = FIELD_SHORT_DEFAULT_VALUE;
        fieldInt = FIELD_INT_DEFAULT_VALUE;
        fieldLongPrimaryKey = FIELD_LONG_PRIMARY_KEY_DEFAULT_VALUE;
        fieldLong = FIELD_LONG_DEFAULT_VALUE;
        fieldByte = FIELD_BYTE_DEFAULT_VALUE;
        fieldFloat = FIELD_FLOAT_DEFAULT_VALUE;
        fieldDouble = FIELD_DOUBLE_DEFAULT_VALUE;
        fieldBoolean = FIELD_BOOLEAN_DEFAULT_VALUE;
        fieldDate = FIELD_DATE_DEFAULT_VALUE;
        fieldBinary = FIELD_BINARY_DEFAULT_VALUE;
        fieldObject = FIELD_OBJECT_DEFAULT_VALUE;
        fieldList = FIELD_LIST_DEFAULT_VALUE;
    }

    public DefaultValueConstructor(long fieldLong) {
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
}
