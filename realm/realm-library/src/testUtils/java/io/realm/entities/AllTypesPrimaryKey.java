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

import java.util.Date;

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

    private RealmList<String> columnStringList;
    private RealmList<byte[]> columnBinaryList;
    private RealmList<Boolean> columnBooleanList;
    private RealmList<Long> columnLongList;
    private RealmList<Double> columnDoubleList;
    private RealmList<Float> columnFloatList;
    private RealmList<Date> columnDateList;

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
}
