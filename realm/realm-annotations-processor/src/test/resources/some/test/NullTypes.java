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

package some.test;

import java.lang.String;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class NullTypes extends RealmObject {
    @Required
    private String fieldStringNotNull;
    private String fieldStringNull;

    @Required
    private Boolean fieldBooleanNotNull;
    private Boolean fieldBooleanNull;

    @Required
    private byte[] fieldBytesNotNull;
    private byte[] fieldBytesNull;

    @Required
    private Byte fieldByteNotNull;
    private Byte fieldByteNull;

    @Required
    private Short fieldShortNotNull;
    private Short fieldShortNull;

    @Required
    private Integer fieldIntegerNotNull;
    private Integer fieldIntegerNull;

    @Required
    private Long fieldLongNotNull;
    private Long fieldLongNull;

    @Required
    private Float fieldFloatNotNull;
    private Float fieldFloatNull;

    @Required
    private Double fieldDoubleNotNull;
    private Double fieldDoubleNull;

    @Required
    private Date fieldDateNotNull;
    private Date fieldDateNull;

    private NullTypes fieldObjectNull;

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

    public String getFieldStringNotNull() {
        return realmGet$fieldStringNotNull();
    }

    public void setFieldStringNotNull(String fieldStringNotNull) {
        realmSet$fieldStringNotNull(fieldStringNotNull);
    }

    public String realmGet$fieldStringNotNull() {
        return fieldStringNotNull;
    }

    public void realmSet$fieldStringNotNull(String fieldStringNotNull) {
        this.fieldStringNotNull = fieldStringNotNull;
    }

    public String getFieldStringNull() {
        return realmGet$fieldStringNull();
    }

    public void setFieldStringNull(String fieldStringNull) {
        realmSet$fieldStringNull(fieldStringNull);
    }

    public String realmGet$fieldStringNull() {
        return fieldStringNull;
    }

    public void realmSet$fieldStringNull(String fieldStringNull) {
        this.fieldStringNull = fieldStringNull;
    }

    public Boolean getFieldBooleanNotNull() {
        return realmGet$fieldBooleanNotNull();
    }

    public void setFieldBooleanNotNull(Boolean fieldBooleanNotNull) {
        realmSet$fieldBooleanNotNull(fieldBooleanNotNull);
    }

    public Boolean realmGet$fieldBooleanNotNull() {
        return fieldBooleanNotNull;
    }

    public void realmSet$fieldBooleanNotNull(Boolean fieldBooleanNotNull) {
        this.fieldBooleanNotNull = fieldBooleanNotNull;
    }

    public Boolean getFieldBooleanNull() {
        return realmGet$fieldBooleanNull();
    }

    public void setFieldBooleanNull(Boolean fieldBooleanNull) {
        realmSet$fieldBooleanNull(fieldBooleanNull);
    }

    public Boolean realmGet$fieldBooleanNull() {
        return fieldBooleanNull;
    }

    public void realmSet$fieldBooleanNull(Boolean fieldBooleanNull) {
        this.fieldBooleanNull = fieldBooleanNull;
    }

    public byte[] getFieldBytesNotNull() {
        return realmGet$fieldBytesNotNull();
    }

    public void setFieldBytesNotNull(byte[] fieldBytesNotNull) {
        realmSet$fieldBytesNotNull(fieldBytesNotNull);
    }

    public byte[] realmGet$fieldBytesNotNull() {
        return fieldBytesNotNull;
    }

    public void realmSet$fieldBytesNotNull(byte[] fieldBytesNotNull) {
        this.fieldBytesNotNull = fieldBytesNotNull;
    }

    public byte[] getFieldBytesNull() {
        return realmGet$fieldBytesNull();
    }

    public void setFieldBytesNull(byte[] fieldBytesNull) {
        realmSet$fieldBytesNull(fieldBytesNull);
    }

    public byte[] realmGet$fieldBytesNull() {
        return fieldBytesNull;
    }

    public void realmSet$fieldBytesNull(byte[] fieldBytesNull) {
        this.fieldBytesNull = fieldBytesNull;
    }

    public Byte getFieldByteNotNull() {
        return realmGet$fieldByteNotNull();
    }

    public void setFieldByteNotNull(Byte fieldByteNotNull) {
        realmSet$fieldByteNotNull(fieldByteNotNull);
    }

    public Byte realmGet$fieldByteNotNull() {
        return fieldByteNotNull;
    }

    public void realmSet$fieldByteNotNull(Byte fieldByteNotNull) {
        this.fieldByteNotNull = fieldByteNotNull;
    }

    public Byte getFieldByteNull() {
        return realmGet$fieldByteNull();
    }

    public void setFieldByteNull(Byte fieldByteNull) {
        realmSet$fieldByteNull(fieldByteNull);
    }

    public Byte realmGet$fieldByteNull() {
        return fieldByteNull;
    }

    public void realmSet$fieldByteNull(Byte fieldByteNull) {
        this.fieldByteNull = fieldByteNull;
    }

    public Short getFieldShortNotNull() {
        return realmGet$fieldShortNotNull();
    }

    public void setFieldShortNotNull(Short fieldShortNotNull) {
        realmSet$fieldShortNotNull(fieldShortNotNull);
    }

    public Short realmGet$fieldShortNotNull() {
        return fieldShortNotNull;
    }

    public void realmSet$fieldShortNotNull(Short fieldShortNotNull) {
        this.fieldShortNotNull = fieldShortNotNull;
    }

    public Short getFieldShortNull() {
        return realmGet$fieldShortNull();
    }

    public void setFieldShortNull(Short fieldShortNull) {
        realmSet$fieldShortNull(fieldShortNull);
    }

    public Short realmGet$fieldShortNull() {
        return fieldShortNull;
    }

    public void realmSet$fieldShortNull(Short fieldShortNull) {
        this.fieldShortNull = fieldShortNull;
    }

    public Integer getFieldIntegerNotNull() {
        return realmGet$fieldIntegerNotNull();
    }

    public void setFieldIntegerNotNull(Integer fieldIntegerNotNull) {
        realmSet$fieldIntegerNotNull(fieldIntegerNotNull);
    }

    public Integer realmGet$fieldIntegerNotNull() {
        return fieldIntegerNotNull;
    }

    public void realmSet$fieldIntegerNotNull(Integer fieldIntegerNotNull) {
        this.fieldIntegerNotNull = fieldIntegerNotNull;
    }

    public Integer getFieldIntegerNull() {
        return realmGet$fieldIntegerNull();
    }

    public void setFieldIntegerNull(Integer fieldIntegerNull) {
        realmSet$fieldIntegerNull(fieldIntegerNull);
    }

    public Integer realmGet$fieldIntegerNull() {
        return fieldIntegerNull;
    }

    public void realmSet$fieldIntegerNull(Integer fieldIntegerNull) {
        this.fieldIntegerNull = fieldIntegerNull;
    }

    public Long getFieldLongNotNull() {
        return realmGet$fieldLongNotNull();
    }

    public void setFieldLongNotNull(Long fieldLongNotNull) {
        realmSet$fieldLongNotNull(fieldLongNotNull);
    }

    public Long realmGet$fieldLongNotNull() {
        return fieldLongNotNull;
    }

    public void realmSet$fieldLongNotNull(Long fieldLongNotNull) {
        this.fieldLongNotNull = fieldLongNotNull;
    }

    public Long getFieldLongNull() {
        return realmGet$fieldLongNull();
    }

    public void setFieldLongNull(Long fieldLongNull) {
        realmSet$fieldLongNull(fieldLongNull);
    }

    public Long realmGet$fieldLongNull() {
        return fieldLongNull;
    }

    public void realmSet$fieldLongNull(Long fieldLongNull) {
        this.fieldLongNull = fieldLongNull;
    }

    public Float getFieldFloatNotNull() {
        return realmGet$fieldFloatNotNull();
    }

    public void setFieldFloatNotNull(Float fieldFloatNotNull) {
        realmSet$fieldFloatNotNull(fieldFloatNotNull);
    }

    public Float realmGet$fieldFloatNotNull() {
        return fieldFloatNotNull;
    }

    public void realmSet$fieldFloatNotNull(Float fieldFloatNotNull) {
        this.fieldFloatNotNull = fieldFloatNotNull;
    }

    public Float getFieldFloatNull() {
        return realmGet$fieldFloatNull();
    }

    public void setFieldFloatNull(Float fieldFloatNull) {
        realmSet$fieldFloatNull(fieldFloatNull);
    }

    public Float realmGet$fieldFloatNull() {
        return fieldFloatNull;
    }

    public void realmSet$fieldFloatNull(Float fieldFloatNull) {
        this.fieldFloatNull = fieldFloatNull;
    }

    public Double getFieldDoubleNotNull() {
        return realmGet$fieldDoubleNotNull();
    }

    public void setFieldDoubleNotNull(Double fieldDoubleNotNull) {
        realmSet$fieldDoubleNotNull(fieldDoubleNotNull);
    }

    public Double realmGet$fieldDoubleNotNull() {
        return fieldDoubleNotNull;
    }

    public void realmSet$fieldDoubleNotNull(Double fieldDoubleNotNull) {
        this.fieldDoubleNotNull = fieldDoubleNotNull;
    }

    public Double getFieldDoubleNull() {
        return realmGet$fieldDoubleNull();
    }

    public void setFieldDoubleNull(Double fieldDoubleNull) {
        realmSet$fieldDoubleNull(fieldDoubleNull);
    }

    public Double realmGet$fieldDoubleNull() {
        return fieldDoubleNull;
    }

    public void realmSet$fieldDoubleNull(Double fieldDoubleNull) {
        this.fieldDoubleNull = fieldDoubleNull;
    }

    public Date getFieldDateNotNull() {
        return realmGet$fieldDateNotNull();
    }

    public void setFieldDateNotNull(Date fieldDateNotNull) {
        realmSet$fieldDateNotNull(fieldDateNotNull);
    }

    public Date realmGet$fieldDateNotNull() {
        return fieldDateNotNull;
    }

    public void realmSet$fieldDateNotNull(Date fieldDateNotNull) {
        this.fieldDateNotNull = fieldDateNotNull;
    }

    public Date getFieldDateNull() {
        return realmGet$fieldDateNull();
    }

    public void setFieldDateNull(Date fieldDateNull) {
        realmSet$fieldDateNull(fieldDateNull);
    }

    public Date realmGet$fieldDateNull() {
        return fieldDateNull;
    }

    public void realmSet$fieldDateNull(Date fieldDateNull) {
        this.fieldDateNull = fieldDateNull;
    }

    public NullTypes getFieldObjectNull() {
        return realmGet$fieldObjectNull();
    }

    public void setFieldObjectNull(NullTypes fieldObjectNull) {
        realmSet$fieldObjectNull(fieldObjectNull);
    }

    public NullTypes realmGet$fieldObjectNull() {
        return fieldObjectNull;
    }

    public void realmSet$fieldObjectNull(NullTypes fieldObjectNull) {
        this.fieldObjectNull = fieldObjectNull;
    }
}
