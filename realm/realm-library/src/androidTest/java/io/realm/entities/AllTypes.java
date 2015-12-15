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
import io.realm.annotations.Required;

public class AllTypes extends RealmObject {

    public static final String CLASS_NAME = "AllTypes";
    public static final String FIELD_STRING = "columnString";
    public static final String FIELD_LONG = "columnLong";
    public static final String FIELD_FLOAT = "columnFloat";
    public static final String FIELD_DOUBLE = "columnDouble";
    public static final String FIELD_BOOLEAN = "columnBoolean";
    public static final String FIELD_DATE = "columnDate";
    public static final String FIELD_BINARY = "columnBinary";
    public static final String FIELD_REALMOBJECT = "columnRealmObject";
    public static final String FIELD_REALMLIST = "columnRealmList";

    @Required
    private String columnString = "";
    private long columnLong;
    private float columnFloat;
    private double columnDouble;
    private boolean columnBoolean;
    @Required
    private Date columnDate = new Date(0);
    @Required
    private byte[] columnBinary = new byte[0];
    private Dog columnRealmObject;
    private RealmList<Dog> columnRealmList;

    public String getColumnString() {
        return realmGetter$columnString();
    }

    public void setColumnString(String columnString) {
        realmSetter$columnString(columnString);
    }

    public String realmGetter$columnString() {
        return columnString;
    }

    public void realmSetter$columnString(String columnString) {
        this.columnString = columnString;
    }

    public long getColumnLong() {
        return realmGetter$columnLong();
    }

    public void setColumnLong(long columnLong) {
        realmSetter$columnLong(columnLong);
    }

    public long realmGetter$columnLong() {
        return columnLong;
    }

    public void realmSetter$columnLong(long columnLong) {
        this.columnLong = columnLong;
    }

    public float getColumnFloat() {
        return realmGetter$columnFloat();
    }

    public void setColumnFloat(float columnFloat) {
        realmSetter$columnFloat(columnFloat);
    }

    public float realmGetter$columnFloat() {
        return columnFloat;
    }

    public void realmSetter$columnFloat(float columnFloat) {
        this.columnFloat = columnFloat;
    }

    public double getColumnDouble() {
        return realmGetter$columnDouble();
    }

    public void setColumnDouble(double columnDouble) {
        realmSetter$columnDouble(columnDouble);
    }

    public double realmGetter$columnDouble() {
        return columnDouble;
    }

    public void realmSetter$columnDouble(double columnDouble) {
        this.columnDouble = columnDouble;
    }

    public boolean isColumnBoolean() {
        return realmGetter$columnBoolean();
    }

    public void setColumnBoolean(boolean columnBoolean) {
        realmSetter$columnBoolean(columnBoolean);
    }

    public boolean realmGetter$columnBoolean() {
        return columnBoolean;
    }

    public void realmSetter$columnBoolean(boolean columnBoolean) {
        this.columnBoolean = columnBoolean;
    }

    public Date getColumnDate() {
        return realmGetter$columnDate();
    }

    public void setColumnDate(Date columnDate) {
        realmSetter$columnDate(columnDate);
    }

    public Date realmGetter$columnDate() {
        return columnDate;
    }

    public void realmSetter$columnDate(Date columnDate) {
        this.columnDate = columnDate;
    }

    public byte[] getColumnBinary() {
        return realmGetter$columnBinary();
    }

    public void setColumnBinary(byte[] columnBinary) {
        realmSetter$columnBinary(columnBinary);
    }

    public byte[] realmGetter$columnBinary() {
        return columnBinary;
    }

    public void realmSetter$columnBinary(byte[] columnBinary) {
        this.columnBinary = columnBinary;
    }

    public Dog getColumnRealmObject() {
        return realmGetter$columnRealmObject();
    }

    public void setColumnRealmObject(Dog columnRealmObject) {
        realmSetter$columnRealmObject(columnRealmObject);
    }

    public Dog realmGetter$columnRealmObject() {
        return columnRealmObject;
    }

    public void realmSetter$columnRealmObject(Dog columnRealmObject) {
        this.columnRealmObject = columnRealmObject;
    }

    public RealmList<Dog> getColumnRealmList() {
        return realmGetter$columnRealmList();
    }

    public void setColumnRealmList(RealmList<Dog> columnRealmList) {
        realmSetter$columnRealmList(columnRealmList);
    }

    public RealmList<Dog> realmGetter$columnRealmList() {
        return columnRealmList;
    }

    public void realmSetter$columnRealmList(RealmList<Dog> columnRealmList) {
        this.columnRealmList = columnRealmList;
    }
}
