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

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class AllJavaTypes extends RealmObject {



    public static String FIELD_IGNORED = "fieldIgnored";
    public static String FIELD_STRING = "fieldString";
    public static String FIELD_SHORT = "fieldShort";
    public static String FIELD_INT = "fieldInt";
    public static String FIELD_LONG = "fieldLong";
    public static String FIELD_BYTE = "fieldByte";
    public static String FIELD_FLOAT = "fieldFloat";
    public static String FIELD_DOUBLE = "fieldDouble";
    public static String FIELD_BOOLEAN = "fieldBoolean";
    public static String FIELD_DATE = "fieldDate";
    public static String FIELD_BLOB = "fieldBinary";
    public static String FIELD_OBJECT = "fieldObject";
    public static String FIELD_LIST = "fieldList";

    @Ignore private String fieldIgnored;
    @Index private String fieldString;
    private short fieldShort;
    private int fieldInt;
    @PrimaryKey private long fieldLong;
    private byte fieldByte;
    private float fieldFloat;
    private double fieldDouble;
    private boolean fieldBoolean;
    private Date fieldDate;
    private byte[] fieldBinary;
    private AllJavaTypes fieldObject;
    private RealmList<AllJavaTypes> fieldList;

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

    public int getFieldInt() {
        return fieldInt;
    }

    public void setFieldInt(int fieldInt) {
        this.fieldInt = fieldInt;
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
}