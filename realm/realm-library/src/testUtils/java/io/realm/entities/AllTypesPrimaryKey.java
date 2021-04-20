/*
 * Copyright 2014 Realm Inc.
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
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class AllTypesPrimaryKey extends RealmObject {
    private String columnString;
    @PrimaryKey
    private long columnLong;
    private float columnFloat;
    private double columnDouble;
    private boolean columnBoolean;
    private Date columnDate;
    private byte[] columnBinary;
    private DogPrimaryKey columnRealmObject;
    private RealmList<DogPrimaryKey> columnRealmList;
    private Boolean columnBoxedBoolean;
    private ObjectId columnObjectId;
    private Decimal128 columnDecimal128;
    private UUID columnUUID;
    private Mixed columnMixed;

    private RealmList<String> columnStringList;
    private RealmList<byte[]> columnBinaryList;
    private RealmList<Boolean> columnBooleanList;
    private RealmList<Long> columnLongList;
    private RealmList<Double> columnDoubleList;
    private RealmList<Float> columnFloatList;
    private RealmList<Date> columnDateList;
    private RealmList<ObjectId> columnObjectIdList;
    private RealmList<Decimal128> columnDecimal128List;
    private RealmList<UUID> columnUUIDList;
    private RealmList<Mixed> columnMixedList;

    public String getColumnString() {
        return columnString;
    }

    public void setColumnString(String columnString) {
        this.columnString = columnString;
    }

    public long getColumnLong() {
        return columnLong;
    }

    public void setColumnLong(long columnLong) {
        this.columnLong = columnLong;
    }

    public float getColumnFloat() {
        return columnFloat;
    }

    public void setColumnFloat(float columnFloat) {
        this.columnFloat = columnFloat;
    }

    public double getColumnDouble() {
        return columnDouble;
    }

    public void setColumnDouble(double columnDouble) {
        this.columnDouble = columnDouble;
    }

    public boolean isColumnBoolean() {
        return columnBoolean;
    }

    public void setColumnBoolean(boolean columnBoolean) {
        this.columnBoolean = columnBoolean;
    }

    public Date getColumnDate() {
        return columnDate;
    }

    public void setColumnDate(Date columnDate) {
        this.columnDate = columnDate;
    }

    public byte[] getColumnBinary() {
        return columnBinary;
    }

    public void setColumnBinary(byte[] columnBinary) {
        this.columnBinary = columnBinary;
    }

    public DogPrimaryKey getColumnRealmObject() {
        return columnRealmObject;
    }

    public void setColumnRealmObject(DogPrimaryKey columnRealmObject) {
        this.columnRealmObject = columnRealmObject;
    }

    public RealmList<DogPrimaryKey> getColumnRealmList() {
        return columnRealmList;
    }

    public void setColumnRealmList(RealmList<DogPrimaryKey> columnRealmList) {
        this.columnRealmList = columnRealmList;
    }

    public Boolean getColumnBoxedBoolean() {
        return columnBoxedBoolean;
    }

    public void setColumnBoxedBoolean(Boolean columnBoxedBoolean) {
        this.columnBoxedBoolean = columnBoxedBoolean;
    }

    public ObjectId getColumnObjectId() {
        return columnObjectId;
    }

    public void setColumnObjectId(ObjectId columnObjectId) {
        this.columnObjectId = columnObjectId;
    }

    public Decimal128 getColumnDecimal128() {
        return columnDecimal128;
    }

    public void setColumnDecimal128(Decimal128 columnDecimal128) {
        this.columnDecimal128 = columnDecimal128;
    }

    public UUID getColumnUUID() {
        return columnUUID;
    }

    public void setColumnUUID(UUID columnUUID) {
        this.columnUUID = columnUUID;
    }

    public Mixed getColumnMixed() {
        return columnMixed;
    }

    public void setColumnMixed(Mixed columnMixed) {
        this.columnMixed = columnMixed;
    }

    public RealmList<String> getColumnStringList() {
        return columnStringList;
    }

    public void setColumnStringList(RealmList<String> columnStringList) {
        this.columnStringList = columnStringList;
    }

    public RealmList<byte[]> getColumnBinaryList() {
        return columnBinaryList;
    }

    public void setColumnBinaryList(RealmList<byte[]> columnBinaryList) {
        this.columnBinaryList = columnBinaryList;
    }

    public RealmList<Boolean> getColumnBooleanList() {
        return columnBooleanList;
    }

    public void setColumnBooleanList(RealmList<Boolean> columnBooleanList) {
        this.columnBooleanList = columnBooleanList;
    }

    public RealmList<Long> getColumnLongList() {
        return columnLongList;
    }

    public void setColumnLongList(RealmList<Long> columnLongList) {
        this.columnLongList = columnLongList;
    }

    public RealmList<Double> getColumnDoubleList() {
        return columnDoubleList;
    }

    public void setColumnDoubleList(RealmList<Double> columnDoubleList) {
        this.columnDoubleList = columnDoubleList;
    }

    public RealmList<Float> getColumnFloatList() {
        return columnFloatList;
    }

    public void setColumnFloatList(RealmList<Float> columnFloatList) {
        this.columnFloatList = columnFloatList;
    }

    public RealmList<Date> getColumnDateList() {
        return columnDateList;
    }

    public void setColumnDateList(RealmList<Date> columnDateList) {
        this.columnDateList = columnDateList;
    }

    public RealmList<ObjectId> getColumnObjectIdList() {
        return columnObjectIdList;
    }

    public void setColumnObjectIdList(RealmList<ObjectId> columnObjectIdList) {
        this.columnObjectIdList = columnObjectIdList;
    }

    public RealmList<Decimal128> getColumnDecimal128List() {
        return columnDecimal128List;
    }

    public void setColumnDecimal128List(RealmList<Decimal128> columnDecimal128List) {
        this.columnDecimal128List = columnDecimal128List;
    }

    public RealmList<UUID> getColumnUUIDList() {
        return columnUUIDList;
    }

    public void setColumnUUIDList(RealmList<UUID> columnUUIDList) {
        this.columnUUIDList = columnUUIDList;
    }

    public RealmList<Mixed> getColumnMixedList() {
        return columnMixedList;
    }

    public void setColumnMixedList(RealmList<Mixed> columnMixedList) {
        this.columnMixedList = columnMixedList;
    }
}
