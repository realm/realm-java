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
import io.realm.annotations.LinkingObjects;
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

    public static final String CLASS_NAME = "NullTypes";
    public static final String FIELD_ID = "id";
    public static final String FIELD_STRING_NOT_NULL = "fieldStringNotNull";
    public static final String FIELD_STRING_NULL = "fieldStringNull";
    public static final String FIELD_BYTES_NOT_NULL = "fieldBytesNotNull";
    public static final String FIELD_BYTES_NULL = "fieldBytesNull";
    public static final String FIELD_BOOLEAN_NOT_NULL = "fieldBooleanNotNull";
    public static final String FIELD_BOOLEAN_NULL = "fieldBooleanNull";
    public static final String FIELD_BYTE_NOT_NULL = "fieldByteNotNull";
    public static final String FIELD_BYTE_NULL = "fieldByteNull";
    public static final String FIELD_SHORT_NOT_NULL = "fieldShortNotNull";
    public static final String FIELD_SHORT_NULL = "fieldShortNull";
    public static final String FIELD_INTEGER_NOT_NULL = "fieldIntegerNotNull";
    public static final String FIELD_INTEGER_NULL = "fieldIntegerNull";
    public static final String FIELD_LONG_NOT_NULL = "fieldLongNotNull";
    public static final String FIELD_LONG_NULL = "fieldLongNull";
    public static final String FIELD_FLOAT_NOT_NULL = "fieldFloatNotNull";
    public static final String FIELD_FLOAT_NULL = "fieldFloatNull";
    public static final String FIELD_DOUBLE_NOT_NULL = "fieldDoubleNotNull";
    public static final String FIELD_DOUBLE_NULL = "fieldDoubleNull";
    public static final String FIELD_DATE_NOT_NULL = "fieldDateNotNull";
    public static final String FIELD_DATE_NULL = "fieldDateNull";
    public static final String FIELD_OBJECT_NULL = "fieldObjectNull";
    public static final String FIELD_LIST_NULL = "fieldListNull";
    public static final String FIELD_LO_OBJECT = "objectParents";
    public static final String FIELD_LO_LIST = "listParents";

    public static final String FIELD_STRING_LIST_NOT_NULL = "fieldStringListNotNull";
    public static final String FIELD_STRING_LIST_NULL = "fieldStringListNull";
    public static final String FIELD_BINARY_LIST_NOT_NULL = "fieldBinaryListNotNull";
    public static final String FIELD_BINARY_LIST_NULL = "fieldBinaryListNull";
    public static final String FIELD_BOOLEAN_LIST_NOT_NULL = "fieldBooleanListNotNull";
    public static final String FIELD_BOOLEAN_LIST_NULL = "fieldBooleanListNull";
    public static final String FIELD_LONG_LIST_NOT_NULL = "fieldLongListNotNull";
    public static final String FIELD_LONG_LIST_NULL = "fieldLongListNull";
    public static final String FIELD_INTEGER_LIST_NOT_NULL = "fieldIntegerListNotNull";
    public static final String FIELD_INTEGER_LIST_NULL = "fieldIntegerListNull";
    public static final String FIELD_SHORT_LIST_NOT_NULL = "fieldShortListNotNull";
    public static final String FIELD_SHORT_LIST_NULL = "fieldShortListNull";
    public static final String FIELD_BYTE_LIST_NOT_NULL = "fieldByteListNotNull";
    public static final String FIELD_BYTE_LIST_NULL = "fieldByteListNull";
    public static final String FIELD_DOUBLE_LIST_NOT_NULL = "fieldDoubleListNotNull";
    public static final String FIELD_DOUBLE_LIST_NULL = "fieldDoubleListNull";
    public static final String FIELD_FLOAT_LIST_NOT_NULL = "fieldFloatListNotNull";
    public static final String FIELD_FLOAT_LIST_NULL = "fieldFloatListNull";
    public static final String FIELD_DATE_LIST_NOT_NULL = "fieldDateListNotNull";
    public static final String FIELD_DATE_LIST_NULL = "fieldDateListNull";

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

    // never nullable
    private RealmList<NullTypes> fieldListNull;

    @Required
    private RealmList<String> fieldStringListNotNull;
    private RealmList<String> fieldStringListNull;

    @Required
    private RealmList<byte[]> fieldBinaryListNotNull;
    private RealmList<byte[]> fieldBinaryListNull;

    @Required
    private RealmList<Boolean> fieldBooleanListNotNull;
    private RealmList<Boolean> fieldBooleanListNull;

    @Required
    private RealmList<Long> fieldLongListNotNull;
    private RealmList<Long> fieldLongListNull;

    @Required
    private RealmList<Integer> fieldIntegerListNotNull;
    private RealmList<Integer> fieldIntegerListNull;

    @Required
    private RealmList<Short> fieldShortListNotNull;
    private RealmList<Short> fieldShortListNull;

    @Required
    private RealmList<Byte> fieldByteListNotNull;
    private RealmList<Byte> fieldByteListNull;

    @Required
    private RealmList<Double> fieldDoubleListNotNull;
    private RealmList<Double> fieldDoubleListNull;

    @Required
    private RealmList<Float> fieldFloatListNotNull;
    private RealmList<Float> fieldFloatListNull;

    @Required
    private RealmList<Date> fieldDateListNotNull;
    private RealmList<Date> fieldDateListNull;

    // never nullable
    @LinkingObjects(FIELD_OBJECT_NULL)
    private final RealmResults<NullTypes> objectParents = null;

    // never nullable
    @LinkingObjects(FIELD_LIST_NULL)
    private final RealmResults<NullTypes> listParents = null;

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

    public RealmResults<NullTypes> getObjectParents() {
        return objectParents;
    }

    public RealmResults<NullTypes> getListParents() {
        return listParents;
    }

    public RealmList<String> getFieldStringListNotNull() {
        return fieldStringListNotNull;
    }

    public void setFieldStringListNotNull(RealmList<String> fieldStringListNotNull) {
        this.fieldStringListNotNull = fieldStringListNotNull;
    }

    public RealmList<String> getFieldStringListNull() {
        return fieldStringListNull;
    }

    public void setFieldStringListNull(RealmList<String> fieldStringListNull) {
        this.fieldStringListNull = fieldStringListNull;
    }

    public RealmList<byte[]> getFieldBinaryListNotNull() {
        return fieldBinaryListNotNull;
    }

    public void setFieldBinaryListNotNull(RealmList<byte[]> fieldBinaryListNotNull) {
        this.fieldBinaryListNotNull = fieldBinaryListNotNull;
    }

    public RealmList<byte[]> getFieldBinaryListNull() {
        return fieldBinaryListNull;
    }

    public void setFieldBinaryListNull(RealmList<byte[]> fieldBinaryListNull) {
        this.fieldBinaryListNull = fieldBinaryListNull;
    }

    public RealmList<Boolean> getFieldBooleanListNotNull() {
        return fieldBooleanListNotNull;
    }

    public void setFieldBooleanListNotNull(RealmList<Boolean> fieldBooleanListNotNull) {
        this.fieldBooleanListNotNull = fieldBooleanListNotNull;
    }

    public RealmList<Boolean> getFieldBooleanListNull() {
        return fieldBooleanListNull;
    }

    public void setFieldBooleanListNull(RealmList<Boolean> fieldBooleanListNull) {
        this.fieldBooleanListNull = fieldBooleanListNull;
    }

    public RealmList<Long> getFieldLongListNotNull() {
        return fieldLongListNotNull;
    }

    public void setFieldLongListNotNull(RealmList<Long> fieldLongListNotNull) {
        this.fieldLongListNotNull = fieldLongListNotNull;
    }

    public RealmList<Long> getFieldLongListNull() {
        return fieldLongListNull;
    }

    public void setFieldLongListNull(RealmList<Long> fieldLongListNull) {
        this.fieldLongListNull = fieldLongListNull;
    }

    public RealmList<Integer> getFieldIntegerListNotNull() {
        return fieldIntegerListNotNull;
    }

    public void setFieldIntegerListNotNull(RealmList<Integer> fieldIntegerListNotNull) {
        this.fieldIntegerListNotNull = fieldIntegerListNotNull;
    }

    public RealmList<Integer> getFieldIntegerListNull() {
        return fieldIntegerListNull;
    }

    public void setFieldIntegerListNull(RealmList<Integer> fieldIntegerListNull) {
        this.fieldIntegerListNull = fieldIntegerListNull;
    }

    public RealmList<Short> getFieldShortListNotNull() {
        return fieldShortListNotNull;
    }

    public void setFieldShortListNotNull(RealmList<Short> fieldShortListNotNull) {
        this.fieldShortListNotNull = fieldShortListNotNull;
    }

    public RealmList<Short> getFieldShortListNull() {
        return fieldShortListNull;
    }

    public void setFieldShortListNull(RealmList<Short> fieldShortListNull) {
        this.fieldShortListNull = fieldShortListNull;
    }

    public RealmList<Byte> getFieldByteListNotNull() {
        return fieldByteListNotNull;
    }

    public void setFieldByteListNotNull(RealmList<Byte> fieldByteListNotNull) {
        this.fieldByteListNotNull = fieldByteListNotNull;
    }

    public RealmList<Byte> getFieldByteListNull() {
        return fieldByteListNull;
    }

    public void setFieldByteListNull(RealmList<Byte> fieldByteListNull) {
        this.fieldByteListNull = fieldByteListNull;
    }

    public RealmList<Double> getFieldDoubleListNotNull() {
        return fieldDoubleListNotNull;
    }

    public void setFieldDoubleListNotNull(RealmList<Double> fieldDoubleListNotNull) {
        this.fieldDoubleListNotNull = fieldDoubleListNotNull;
    }

    public RealmList<Double> getFieldDoubleListNull() {
        return fieldDoubleListNull;
    }

    public void setFieldDoubleListNull(RealmList<Double> fieldDoubleListNull) {
        this.fieldDoubleListNull = fieldDoubleListNull;
    }

    public RealmList<Float> getFieldFloatListNotNull() {
        return fieldFloatListNotNull;
    }

    public void setFieldFloatListNotNull(RealmList<Float> fieldFloatListNotNull) {
        this.fieldFloatListNotNull = fieldFloatListNotNull;
    }

    public RealmList<Float> getFieldFloatListNull() {
        return fieldFloatListNull;
    }

    public void setFieldFloatListNull(RealmList<Float> fieldFloatListNull) {
        this.fieldFloatListNull = fieldFloatListNull;
    }

    public RealmList<Date> getFieldDateListNotNull() {
        return fieldDateListNotNull;
    }

    public void setFieldDateListNotNull(RealmList<Date> fieldDateListNotNull) {
        this.fieldDateListNotNull = fieldDateListNotNull;
    }

    public RealmList<Date> getFieldDateListNull() {
        return fieldDateListNull;
    }

    public void setFieldDateListNull(RealmList<Date> fieldDateListNull) {
        this.fieldDateListNull = fieldDateListNull;
    }
}
