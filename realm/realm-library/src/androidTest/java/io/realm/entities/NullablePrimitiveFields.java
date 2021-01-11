/*
 * Copyright 2020 Realm Inc.
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
import io.realm.RealmObject;

public class NullablePrimitiveFields extends RealmObject {

    public static final String CLASS_NAME = "NullablePrimitiveFields";

    public static final String FIELD_STRING = "fieldString";
    public static final String FIELD_INT = "fieldInt";
    public static final String FIELD_FLOAT = "fieldFloat";
    public static final String FIELD_DOUBLE = "fieldDouble";
    public static final String FIELD_BOOLEAN = "fieldBoolean";
    public static final String FIELD_DATE = "fieldDate";
    public static final String FIELD_BINARY = "fieldBinary";
    public static final String FIELD_OBJECT_ID = "fieldObjectId";
    public static final String FIELD_DECIMAL128 = "fieldDecimal128";
    public static final String FIELD_UUID = "fieldUUID";
    public static final String FIELD_MIXED = "fieldMixed";

    private Boolean fieldBoolean;
    private Integer fieldInt;
    private Float fieldFloat;
    private Double fieldDouble;
    private String fieldString;
    private Byte fieldBinary;
    private Date fieldDate;
    private ObjectId fieldObjectId;
    private Decimal128 fieldDecimal128;
    private UUID fieldUUID;
    private Mixed fieldMixed;

    public Integer getFieldInt() {
        return fieldInt;
    }

    public void setFieldInt(Integer fieldInt) {
        this.fieldInt = fieldInt;
    }

    public Float getFieldFloat() {
        return fieldFloat;
    }

    public void setFieldFloat(Float fieldFloat) {
        this.fieldFloat = fieldFloat;
    }

    public Double getFieldDouble() {
        return fieldDouble;
    }

    public void setFieldDouble(Double fieldDouble) {
        this.fieldDouble = fieldDouble;
    }

    public Boolean getFieldBoolean() {
        return fieldBoolean;
    }

    public void setFieldBoolean(Boolean fieldBoolean) {
        this.fieldBoolean = fieldBoolean;
    }

    public String getFieldString() {
        return fieldString;
    }

    public void setFieldString(String fieldString) {
        this.fieldString = fieldString;
    }

    public Date getFieldDate() {
        return fieldDate;
    }

    public void setFieldDate(Date fieldDate) {
        this.fieldDate = fieldDate;
    }

    public Byte getFieldBinary() {
        return fieldBinary;
    }

    public void setFieldBinary(Byte fieldBinary) {
        this.fieldBinary = fieldBinary;
    }

    public ObjectId getFieldObjectId() {
        return fieldObjectId;
    }

    public void setFieldObjectId(ObjectId fieldObjectId) {
        this.fieldObjectId = fieldObjectId;
    }

    public Decimal128 getFieldDecimal128() {
        return fieldDecimal128;
    }

    public void setFieldDecimal128(Decimal128 fieldDecimal128) {
        this.fieldDecimal128 = fieldDecimal128;
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
}
