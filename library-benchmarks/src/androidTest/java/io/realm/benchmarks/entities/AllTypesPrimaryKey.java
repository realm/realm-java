/*
 * Copyright 2017 Realm Inc.
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

package io.realm.benchmarks.entities;

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
    private AllTypesPrimaryKey columnRealmObject;
    private RealmList<AllTypesPrimaryKey> columnRealmList;
    private Boolean columnBoxedBoolean;

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

    public AllTypesPrimaryKey getColumnRealmObject() {
        return columnRealmObject;
    }

    public void setColumnRealmObject(AllTypesPrimaryKey columnRealmObject) {
        this.columnRealmObject = columnRealmObject;
    }

    public RealmList<AllTypesPrimaryKey> getColumnRealmList() {
        return columnRealmList;
    }

    public void setColumnRealmList(RealmList<AllTypesPrimaryKey> columnRealmList) {
        this.columnRealmList = columnRealmList;
    }

    public Boolean getColumnBoxedBoolean() {
        return columnBoxedBoolean;
    }

    public void setColumnBoxedBoolean(Boolean columnBoxedBoolean) {
        this.columnBoxedBoolean = columnBoxedBoolean;
    }
}
