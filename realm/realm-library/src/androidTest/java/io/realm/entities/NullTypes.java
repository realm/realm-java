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
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

// Always follow below order and put comments like below to make NullTypes Related cases
// 1 String
// 2 Bytes
// 3 Boolean
// 4 Byte
// 5 Short
// 6 Integer
// 7 Long
// 8 Float
// 9 Double
// 10 Date
// 11 Object
public class NullTypes extends RealmObject {

    public static String CLASS_NAME = "NullTypes";
    public static String FIELD_ID = "id";
    public static String FIELD_STRING_NOT_NULL = "fieldStringNotNull";
    public static String FIELD_STRING_NULL = "fieldStringNull";
    public static String FIELD_BYTES_NOT_NULL = "fieldBytesNotNull";
    public static String FIELD_BYTES_NULL = "fieldBytesNull";
    public static String FIELD_BOOLEAN_NOT_NULL = "fieldBooleanNotNull";
    public static String FIELD_BOOLEAN_NULL = "fieldBooleanNull";
    public static String FIELD_BYTE_NOT_NULL = "fieldByteNotNull";
    public static String FIELD_BYTE_NULL = "fieldByteNull";
    public static String FIELD_SHORT_NOT_NULL = "fieldShortNotNull";
    public static String FIELD_SHORT_NULL = "fieldShortNull";
    public static String FIELD_INTEGER_NOT_NULL = "fieldIntegerNotNull";
    public static String FIELD_INTEGER_NULL = "fieldIntegerNull";
    public static String FIELD_LONG_NOT_NULL = "fieldLongNotNull";
    public static String FIELD_LONG_NULL = "fieldLongNull";
    public static String FIELD_FLOAT_NOT_NULL = "fieldFloatNotNull";
    public static String FIELD_FLOAT_NULL = "fieldFloatNull";
    public static String FIELD_DOUBLE_NOT_NULL = "fieldDoubleNotNull";
    public static String FIELD_DOUBLE_NULL = "fieldDoubleNull";
    public static String FIELD_DATE_NOT_NULL = "fieldDateNotNull";
    public static String FIELD_DATE_NULL = "fieldDateNull";
    public static String FIELD_OBJECT_NULL = "fieldObjectNull";
    public static String FIELD_LIST_NULL = "fieldListNull";

    @PrimaryKey
    private int id;

    @Required
    private String fieldStringNotNull = "";
    private String fieldStringNull;

    @Required
    private byte[] fieldBytesNotNull = new byte[0];
    private byte[] fieldBytesNull;

    @Required
    private Boolean fieldBooleanNotNull = false;
    private Boolean fieldBooleanNull;

    @Required
    private Byte fieldByteNotNull = 0;
    private Byte fieldByteNull;

    @Required
    private Short fieldShortNotNull = 0;
    private Short fieldShortNull;

    @Required
    private Integer fieldIntegerNotNull = 0;
    private Integer fieldIntegerNull;

    @Required
    private Long fieldLongNotNull = 0L;
    private Long fieldLongNull;

    @Required
    private Float fieldFloatNotNull = 0F;
    private Float fieldFloatNull;

    @Required
    private Double fieldDoubleNotNull = 0D;
    private Double fieldDoubleNull;

    @Required
    private Date fieldDateNotNull = new Date(0);
    private Date fieldDateNull;

    private NullTypes fieldObjectNull;

    private RealmList<NullTypes> fieldListNull;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFieldStringNotNull() {
        return fieldStringNotNull;
    }

    public void setFieldStringNotNull(String fieldStringNotNull) {
        this.fieldStringNotNull = fieldStringNotNull;
    }

    public String getFieldStringNull() {
        return fieldStringNull;
    }

    public void setFieldStringNull(String fieldStringNull) {
        this.fieldStringNull = fieldStringNull;
    }

    public byte[] getFieldBytesNull() {
        return fieldBytesNull;
    }

    public void setFieldBytesNull(byte[] fieldBytesNull) {
        this.fieldBytesNull = fieldBytesNull;
    }

    public byte[] getFieldBytesNotNull() {
        return fieldBytesNotNull;
    }

    public void setFieldBytesNotNull(byte[] fieldBytesNotNull) {
        this.fieldBytesNotNull = fieldBytesNotNull;
    }

    public Boolean getFieldBooleanNotNull() {
        return fieldBooleanNotNull;
    }

    public void setFieldBooleanNotNull(Boolean fieldBooleanNotNull) {
        this.fieldBooleanNotNull = fieldBooleanNotNull;
    }

    public Boolean getFieldBooleanNull() {
        return fieldBooleanNull;
    }

    public void setFieldBooleanNull(Boolean fieldBooleanNull) {
        this.fieldBooleanNull = fieldBooleanNull;
    }

    public Byte getFieldByteNotNull() {
        return fieldByteNotNull;
    }

    public void setFieldByteNotNull(Byte fieldByteNotNull) {
        this.fieldByteNotNull = fieldByteNotNull;
    }

    public Byte getFieldByteNull() {
        return fieldByteNull;
    }

    public void setFieldByteNull(Byte fieldByteNull) {
        this.fieldByteNull = fieldByteNull;
    }

    public Short getFieldShortNotNull() {
        return fieldShortNotNull;
    }

    public void setFieldShortNotNull(Short fieldShortNotNull) {
        this.fieldShortNotNull = fieldShortNotNull;
    }

    public Short getFieldShortNull() {
        return fieldShortNull;
    }

    public void setFieldShortNull(Short fieldShortNull) {
        this.fieldShortNull = fieldShortNull;
    }

    public Integer getFieldIntegerNotNull() {
        return fieldIntegerNotNull;
    }

    public void setFieldIntegerNotNull(Integer fieldIntegerNotNull) {
        this.fieldIntegerNotNull = fieldIntegerNotNull;
    }

    public Integer getFieldIntegerNull() {
        return fieldIntegerNull;
    }

    public void setFieldIntegerNull(Integer fieldIntegerNull) {
        this.fieldIntegerNull = fieldIntegerNull;
    }

    public Long getFieldLongNotNull() {
        return fieldLongNotNull;
    }

    public void setFieldLongNotNull(Long fieldLongNotNull) {
        this.fieldLongNotNull = fieldLongNotNull;
    }

    public Long getFieldLongNull() {
        return fieldLongNull;
    }

    public void setFieldLongNull(Long fieldLongNull) {
        this.fieldLongNull = fieldLongNull;
    }

    public Float getFieldFloatNotNull() {
        return fieldFloatNotNull;
    }

    public void setFieldFloatNotNull(Float fieldFloatNotNull) {
        this.fieldFloatNotNull = fieldFloatNotNull;
    }

    public Float getFieldFloatNull() {
        return fieldFloatNull;
    }

    public void setFieldFloatNull(Float fieldFloatNull) {
        this.fieldFloatNull = fieldFloatNull;
    }

    public Double getFieldDoubleNotNull() {
        return fieldDoubleNotNull;
    }

    public void setFieldDoubleNotNull(Double fieldDoubleNotNull) {
        this.fieldDoubleNotNull = fieldDoubleNotNull;
    }

    public Double getFieldDoubleNull() {
        return fieldDoubleNull;
    }

    public void setFieldDoubleNull(Double fieldDoubleNull) {
        this.fieldDoubleNull = fieldDoubleNull;
    }

    public Date getFieldDateNotNull() {
        return fieldDateNotNull;
    }

    public void setFieldDateNotNull(Date fieldDateNotNull) {
        this.fieldDateNotNull = fieldDateNotNull;
    }

    public Date getFieldDateNull() {
        return fieldDateNull;
    }

    public void setFieldDateNull(Date fieldDateNull) {
        this.fieldDateNull = fieldDateNull;
    }

    public NullTypes getFieldObjectNull() {
        return fieldObjectNull;
    }

    public void setFieldObjectNull(NullTypes fieldObjectNull) {
        this.fieldObjectNull = fieldObjectNull;
    }

    public RealmList<NullTypes> getFieldListNull() {
        return fieldListNull;
    }

    public void setFieldListNull(RealmList<NullTypes> fieldListNull) {
        this.fieldListNull = fieldListNull;
    }
}
