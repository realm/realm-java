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

    public static final String[] INVALID_TYPES_FIELDS_FOR_DISTINCT = new String[]{FIELD_REALMOBJECT, FIELD_REALMLIST, FIELD_DOUBLE, FIELD_FLOAT};

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
    private AllTypes columnRealmObject;
    private RealmList<AllTypes> columnRealmList;

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

    public AllTypes getColumnRealmObject() {
        return columnRealmObject;
    }

    public void setColumnRealmObject(AllTypes columnRealmObject) {
        this.columnRealmObject = columnRealmObject;
    }

    public RealmList<AllTypes> getColumnRealmList() {
        return columnRealmList;
    }

    public void setColumnRealmList(RealmList<AllTypes> columnRealmList) {
        this.columnRealmList = columnRealmList;
    }
}
