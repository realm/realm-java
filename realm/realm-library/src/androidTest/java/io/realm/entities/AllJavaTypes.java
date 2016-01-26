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

public class AllJavaTypes extends RealmObject{

    public static final String CLASS_NAME = "AllJavaTypes";
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
    public static String FIELD_BINARY = "fieldBinary";
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
        return realmGetter$fieldIgnored();
    }

    public void setFieldIgnored(String fieldIgnored) {
        realmSetter$fieldIgnored(fieldIgnored);
    }

    public String realmGetter$fieldIgnored() {
        return fieldIgnored;
    }

    public void realmSetter$fieldIgnored(String fieldIgnored) {
        this.fieldIgnored = fieldIgnored;
    }

    public String getFieldString() {
        return realmGetter$fieldString();
    }

    public void setFieldString(String fieldString) {
        realmSetter$fieldString(fieldString);
    }

    public String realmGetter$fieldString() {
        return fieldString;
    }

    public void realmSetter$fieldString(String fieldString) {
        this.fieldString = fieldString;
    }

    public short getFieldShort() {
        return realmGetter$fieldShort();
    }

    public void setFieldShort(short fieldShort) {
        realmSetter$fieldShort(fieldShort);
    }

    public short realmGetter$fieldShort() {
        return fieldShort;
    }

    public void realmSetter$fieldShort(short fieldShort) {
        this.fieldShort = fieldShort;
    }

    public int getFieldInt() {
        return realmGetter$fieldInt();
    }

    public void setFieldInt(int fieldInt) {
        realmSetter$fieldInt(fieldInt);
    }

    public int realmGetter$fieldInt() {
        return fieldInt;
    }

    public void realmSetter$fieldInt(int fieldInt) {
        this.fieldInt = fieldInt;
    }

    public long getFieldLong() {
        return realmGetter$fieldLong();
    }

    public void setFieldLong(long fieldLong) {
        realmSetter$fieldLong(fieldLong);
    }

    public long realmGetter$fieldLong() {
        return fieldLong;
    }

    public void realmSetter$fieldLong(long fieldLong) {
        this.fieldLong = fieldLong;
    }

    public byte getFieldByte() {
        return realmGetter$fieldByte();
    }

    public void setFieldByte(byte fieldByte) {
        realmSetter$fieldByte(fieldByte);
    }

    public byte realmGetter$fieldByte() {
        return fieldByte;
    }

    public void realmSetter$fieldByte(byte fieldByte) {
        this.fieldByte = fieldByte;
    }

    public float getFieldFloat() {
        return realmGetter$fieldFloat();
    }

    public void setFieldFloat(float fieldFloat) {
        realmSetter$fieldFloat(fieldFloat);
    }

    public float realmGetter$fieldFloat() {
        return fieldFloat;
    }

    public void realmSetter$fieldFloat(float fieldFloat) {
        this.fieldFloat = fieldFloat;
    }

    public double getFieldDouble() {
        return realmGetter$fieldDouble();
    }

    public void setFieldDouble(double fieldDouble) {
        realmSetter$fieldDouble(fieldDouble);
    }

    public double realmGetter$fieldDouble() {
        return fieldDouble;
    }

    public void realmSetter$fieldDouble(double fieldDouble) {
        this.fieldDouble = fieldDouble;
    }

    public boolean getFieldBoolean() {
        return realmGetter$fieldBoolean();
    }

    public void setFieldBoolean(boolean fieldBoolean) {
        realmSetter$fieldBoolean(fieldBoolean);
    }

    public boolean realmGetter$fieldBoolean() {
        return fieldBoolean;
    }

    public void realmSetter$fieldBoolean(boolean fieldBoolean) {
        this.fieldBoolean = fieldBoolean;
    }

    public Date getFieldDate() {
        return realmGetter$fieldDate();
    }

    public void setFieldDate(Date fieldDate) {
        realmSetter$fieldDate(fieldDate);
    }

    public Date realmGetter$fieldDate() {
        return fieldDate;
    }

    public void realmSetter$fieldDate(Date fieldDate) {
        this.fieldDate = fieldDate;
    }

    public byte[] getFieldBinary() {
        return realmGetter$fieldBinary();
    }

    public void setFieldBinary(byte[] fieldBinary) {
        realmSetter$fieldBinary(fieldBinary);
    }

    public byte[] realmGetter$fieldBinary() {
        return fieldBinary;
    }

    public void realmSetter$fieldBinary(byte[] fieldBinary) {
        this.fieldBinary = fieldBinary;
    }

    public AllJavaTypes getFieldObject() {
        return realmGetter$fieldObject();
    }

    public void setFieldObject(AllJavaTypes fieldObject) {
        realmSetter$fieldObject(fieldObject);
    }

    public AllJavaTypes realmGetter$fieldObject() {
        return fieldObject;
    }

    public void realmSetter$fieldObject(AllJavaTypes fieldObject) {
        this.fieldObject = fieldObject;
    }

    public RealmList<AllJavaTypes> getFieldList() {
        return realmGetter$fieldList();
    }

    public void setFieldList(RealmList<AllJavaTypes> fieldList) {
        realmSetter$fieldList(fieldList);
    }

    public RealmList<AllJavaTypes> realmGetter$fieldList() {
        return fieldList;
    }

    public void realmSetter$fieldList(RealmList<AllJavaTypes> fieldList) {
        this.fieldList = fieldList;
    }
}
