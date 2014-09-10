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

package io.realm.tests.api.entities;
import java.util.Date;

@io.realm.base.RealmClass
public class AllColumns extends io.realm.typed.RealmObject {

    private String columnString;
    private long columnLong;
    private Long columnLLong;
    private float columnFloat;
    private double columnDouble;
    private boolean columnBoolean;
    private Date columnDate;
    private Integer integerNumber;
    private int intNumber;

    //private byte columnBinary;
    //private User columnRealmObject;


    public boolean getColumnBoolean() {
        return columnBoolean;
    }

    public void setColumnBoolean(boolean value) {
        columnBoolean = value;
    }

    public java.util.Date getColumnDate() {
        return columnDate;
    }

    public void setColumnDate(java.util.Date value) {
        columnDate = value;
    }

    public double getColumnDouble() {
        return columnDouble;
    }

    public void setColumnDouble(double value) {
        columnDouble = value;
    }

    public float getColumnFloat() {
        return columnFloat;
    }

    public void setColumnFloat(float value) {
        columnFloat = value;
    }

    public Long getColumnLLong() {
        return columnLLong;
    }

    public void setColumnLLong(Long value) {
        columnLLong = value;
    }

    public long getColumnLong() {
        return columnLong;
    }

    public void setColumnLong(long value) {
        columnLong = value;
    }

    public String getColumnString() {
        return columnString;
    }

    public void setColumnString(String value) {
        columnString = value;
    }

    public int getIntNumber() {
        return intNumber;
    }

    public void setIntNumber(int value) {
        intNumber = value;
    }


    public Integer getIntegerNumber() {
        return integerNumber;
    }

    public void setIntegerNumber(Integer value) {
        integerNumber = value;
    }



}
