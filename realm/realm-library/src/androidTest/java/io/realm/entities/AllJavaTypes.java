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
    public static final String FIELD_OBJECT = "fieldObject";
    public static final String FIELD_LIST = "fieldList";
    public static final String FIELD_LO_OBJECT = "objectParents";
    public static final String FIELD_LO_LIST = "listParents";

    public static final String[] INVALID_FIELDS_FOR_DISTINCT
            = new String[] {FIELD_OBJECT, FIELD_LIST, FIELD_DOUBLE, FIELD_FLOAT, FIELD_LO_OBJECT, FIELD_LO_LIST};

    public static final String INVALID_LINKED_BINARY_FIELD_FOR_DISTINCT
            = AllJavaTypes.FIELD_OBJECT + "." + AllJavaTypes.FIELD_BINARY;

    public static final String[] INVALID_LINKED_TYPES_FIELDS_FOR_DISTINCT = new String[] {
            FIELD_OBJECT + "." + FIELD_BINARY,
            FIELD_OBJECT + "." + FIELD_OBJECT,
            FIELD_OBJECT + "." + FIELD_LIST};

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
    private AllJavaTypes fieldObject;
    private RealmList<AllJavaTypes> fieldList;

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

    public RealmResults<AllJavaTypes> getObjectParents() {
        return objectParents;
    }

    public RealmResults<AllJavaTypes> getListParents() {
        return listParents;
    }
}
