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
public class NoPrimaryKeyNullTypes extends RealmObject {

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

    private NoPrimaryKeyNullTypes fieldObjectNull;

    public String getFieldStringNotNull() {
        return realmGetter$fieldStringNotNull();
    }

    public void setFieldStringNotNull(String fieldStringNotNull) {
        realmSetter$fieldStringNotNull(fieldStringNotNull);
    }

    public String realmGetter$fieldStringNotNull() {
        return fieldStringNotNull;
    }

    public void realmSetter$fieldStringNotNull(String fieldStringNotNull) {
        this.fieldStringNotNull = fieldStringNotNull;
    }

    public String getFieldStringNull() {
        return realmGetter$fieldStringNull();
    }

    public void setFieldStringNull(String fieldStringNull) {
        realmSetter$fieldStringNull(fieldStringNull);
    }

    public String realmGetter$fieldStringNull() {
        return fieldStringNull;
    }

    public void realmSetter$fieldStringNull(String fieldStringNull) {
        this.fieldStringNull = fieldStringNull;
    }

    public byte[] getFieldBytesNotNull() {
        return realmGetter$fieldBytesNotNull();
    }

    public void setFieldBytesNotNull(byte[] fieldBytesNotNull) {
        realmSetter$fieldBytesNotNull(fieldBytesNotNull);
    }

    public byte[] realmGetter$fieldBytesNotNull() {
        return fieldBytesNotNull;
    }

    public void realmSetter$fieldBytesNotNull(byte[] fieldBytesNotNull) {
        this.fieldBytesNotNull = fieldBytesNotNull;
    }

    public byte[] getFieldBytesNull() {
        return realmGetter$fieldBytesNull();
    }

    public void setFieldBytesNull(byte[] fieldBytesNull) {
        realmSetter$fieldBytesNull(fieldBytesNull);
    }

    public byte[] realmGetter$fieldBytesNull() {
        return fieldBytesNull;
    }

    public void realmSetter$fieldBytesNull(byte[] fieldBytesNull) {
        this.fieldBytesNull = fieldBytesNull;
    }

    public Boolean getFieldBooleanNotNull() {
        return realmGetter$fieldBooleanNotNull();
    }

    public void setFieldBooleanNotNull(Boolean fieldBooleanNotNull) {
        realmSetter$fieldBooleanNotNull(fieldBooleanNotNull);
    }

    public Boolean realmGetter$fieldBooleanNotNull() {
        return fieldBooleanNotNull;
    }

    public void realmSetter$fieldBooleanNotNull(Boolean fieldBooleanNotNull) {
        this.fieldBooleanNotNull = fieldBooleanNotNull;
    }

    public Boolean getFieldBooleanNull() {
        return realmGetter$fieldBooleanNull();
    }

    public void setFieldBooleanNull(Boolean fieldBooleanNull) {
        realmSetter$fieldBooleanNull(fieldBooleanNull);
    }

    public Boolean realmGetter$fieldBooleanNull() {
        return fieldBooleanNull;
    }

    public void realmSetter$fieldBooleanNull(Boolean fieldBooleanNull) {
        this.fieldBooleanNull = fieldBooleanNull;
    }

    public Byte getFieldByteNotNull() {
        return realmGetter$fieldByteNotNull();
    }

    public void setFieldByteNotNull(Byte fieldByteNotNull) {
        realmSetter$fieldByteNotNull(fieldByteNotNull);
    }

    public Byte realmGetter$fieldByteNotNull() {
        return fieldByteNotNull;
    }

    public void realmSetter$fieldByteNotNull(Byte fieldByteNotNull) {
        this.fieldByteNotNull = fieldByteNotNull;
    }

    public Byte getFieldByteNull() {
        return realmGetter$fieldByteNull();
    }

    public void setFieldByteNull(Byte fieldByteNull) {
        realmSetter$fieldByteNull(fieldByteNull);
    }

    public Byte realmGetter$fieldByteNull() {
        return fieldByteNull;
    }

    public void realmSetter$fieldByteNull(Byte fieldByteNull) {
        this.fieldByteNull = fieldByteNull;
    }

    public Short getFieldShortNotNull() {
        return realmGetter$fieldShortNotNull();
    }

    public void setFieldShortNotNull(Short fieldShortNotNull) {
        realmSetter$fieldShortNotNull(fieldShortNotNull);
    }

    public Short realmGetter$fieldShortNotNull() {
        return fieldShortNotNull;
    }

    public void realmSetter$fieldShortNotNull(Short fieldShortNotNull) {
        this.fieldShortNotNull = fieldShortNotNull;
    }

    public Short getFieldShortNull() {
        return realmGetter$fieldShortNull();
    }

    public void setFieldShortNull(Short fieldShortNull) {
        realmSetter$fieldShortNull(fieldShortNull);
    }

    public Short realmGetter$fieldShortNull() {
        return fieldShortNull;
    }

    public void realmSetter$fieldShortNull(Short fieldShortNull) {
        this.fieldShortNull = fieldShortNull;
    }

    public Integer getFieldIntegerNotNull() {
        return realmGetter$fieldIntegerNotNull();
    }

    public void setFieldIntegerNotNull(Integer fieldIntegerNotNull) {
        realmSetter$fieldIntegerNotNull(fieldIntegerNotNull);
    }

    public Integer realmGetter$fieldIntegerNotNull() {
        return fieldIntegerNotNull;
    }

    public void realmSetter$fieldIntegerNotNull(Integer fieldIntegerNotNull) {
        this.fieldIntegerNotNull = fieldIntegerNotNull;
    }

    public Integer getFieldIntegerNull() {
        return realmGetter$fieldIntegerNull();
    }

    public void setFieldIntegerNull(Integer fieldIntegerNull) {
        realmSetter$fieldIntegerNull(fieldIntegerNull);
    }

    public Integer realmGetter$fieldIntegerNull() {
        return fieldIntegerNull;
    }

    public void realmSetter$fieldIntegerNull(Integer fieldIntegerNull) {
        this.fieldIntegerNull = fieldIntegerNull;
    }

    public Long getFieldLongNotNull() {
        return realmGetter$fieldLongNotNull();
    }

    public void setFieldLongNotNull(Long fieldLongNotNull) {
        realmSetter$fieldLongNotNull(fieldLongNotNull);
    }

    public Long realmGetter$fieldLongNotNull() {
        return fieldLongNotNull;
    }

    public void realmSetter$fieldLongNotNull(Long fieldLongNotNull) {
        this.fieldLongNotNull = fieldLongNotNull;
    }

    public Long getFieldLongNull() {
        return realmGetter$fieldLongNull();
    }

    public void setFieldLongNull(Long fieldLongNull) {
        realmSetter$fieldLongNull(fieldLongNull);
    }

    public Long realmGetter$fieldLongNull() {
        return fieldLongNull;
    }

    public void realmSetter$fieldLongNull(Long fieldLongNull) {
        this.fieldLongNull = fieldLongNull;
    }

    public Float getFieldFloatNotNull() {
        return realmGetter$fieldFloatNotNull();
    }

    public void setFieldFloatNotNull(Float fieldFloatNotNull) {
        realmSetter$fieldFloatNotNull(fieldFloatNotNull);
    }

    public Float realmGetter$fieldFloatNotNull() {
        return fieldFloatNotNull;
    }

    public void realmSetter$fieldFloatNotNull(Float fieldFloatNotNull) {
        this.fieldFloatNotNull = fieldFloatNotNull;
    }

    public Float getFieldFloatNull() {
        return realmGetter$fieldFloatNull();
    }

    public void setFieldFloatNull(Float fieldFloatNull) {
        realmSetter$fieldFloatNull(fieldFloatNull);
    }

    public Float realmGetter$fieldFloatNull() {
        return fieldFloatNull;
    }

    public void realmSetter$fieldFloatNull(Float fieldFloatNull) {
        this.fieldFloatNull = fieldFloatNull;
    }

    public Double getFieldDoubleNotNull() {
        return realmGetter$fieldDoubleNotNull();
    }

    public void setFieldDoubleNotNull(Double fieldDoubleNotNull) {
        realmSetter$fieldDoubleNotNull(fieldDoubleNotNull);
    }

    public Double realmGetter$fieldDoubleNotNull() {
        return fieldDoubleNotNull;
    }

    public void realmSetter$fieldDoubleNotNull(Double fieldDoubleNotNull) {
        this.fieldDoubleNotNull = fieldDoubleNotNull;
    }

    public Double getFieldDoubleNull() {
        return realmGetter$fieldDoubleNull();
    }

    public void setFieldDoubleNull(Double fieldDoubleNull) {
        realmSetter$fieldDoubleNull(fieldDoubleNull);
    }

    public Double realmGetter$fieldDoubleNull() {
        return fieldDoubleNull;
    }

    public void realmSetter$fieldDoubleNull(Double fieldDoubleNull) {
        this.fieldDoubleNull = fieldDoubleNull;
    }

    public Date getFieldDateNotNull() {
        return realmGetter$fieldDateNotNull();
    }

    public void setFieldDateNotNull(Date fieldDateNotNull) {
        realmSetter$fieldDateNotNull(fieldDateNotNull);
    }

    public Date realmGetter$fieldDateNotNull() {
        return fieldDateNotNull;
    }

    public void realmSetter$fieldDateNotNull(Date fieldDateNotNull) {
        this.fieldDateNotNull = fieldDateNotNull;
    }

    public Date getFieldDateNull() {
        return realmGetter$fieldDateNull();
    }

    public void setFieldDateNull(Date fieldDateNull) {
        realmSetter$fieldDateNull(fieldDateNull);
    }

    public Date realmGetter$fieldDateNull() {
        return fieldDateNull;
    }

    public void realmSetter$fieldDateNull(Date fieldDateNull) {
        this.fieldDateNull = fieldDateNull;
    }

    public NoPrimaryKeyNullTypes getFieldObjectNull() {
        return realmGetter$fieldObjectNull();
    }

    public void setFieldObjectNull(NoPrimaryKeyNullTypes fieldObjectNull) {
        realmSetter$fieldObjectNull(fieldObjectNull);
    }

    public NoPrimaryKeyNullTypes realmGetter$fieldObjectNull() {
        return fieldObjectNull;
    }

    public void realmSetter$fieldObjectNull(NoPrimaryKeyNullTypes fieldObjectNull) {
        this.fieldObjectNull = fieldObjectNull;
    }
}
