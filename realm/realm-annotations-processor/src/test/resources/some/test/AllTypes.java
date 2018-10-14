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

package some.test;

import java.util.Date;

import io.realm.MutableRealmInteger;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class AllTypes extends RealmObject {

    public static final String TAG = "AllTypes";
    public static final String FIELD_PARENTS = "columnObject";

    @PrimaryKey
    private String columnString;

    private long columnLong;
    private float columnFloat;
    private double columnDouble;
    private boolean columnBoolean;

    @Required
    private Date columnDate;

    @Required
    private byte[] columnBinary;

    private final MutableRealmInteger columnMutableRealmInteger = MutableRealmInteger.valueOf(0);

    private AllTypes columnObject;

    private RealmList<AllTypes> columnRealmList;

    private RealmList<String> columnStringList;
    private RealmList<byte[]> columnBinaryList;
    private RealmList<Boolean> columnBooleanList;
    private RealmList<Long> columnLongList;
    private RealmList<Integer> columnIntegerList;
    private RealmList<Short> columnShortList;
    private RealmList<Byte> columnByteList;
    private RealmList<Double> columnDoubleList;
    private RealmList<Float> columnFloatList;
    private RealmList<Date> columnDateList;


    @LinkingObjects(FIELD_PARENTS)
    private final RealmResults<AllTypes> parentObjects = null;

    public String getColumnString() {
        return realmGet$columnString();
    }

    public void setColumnString(String columnString) {
        realmSet$columnString(columnString);
    }

    public String realmGet$columnString() {
        return columnString;
    }

    public void realmSet$columnString(String columnString) {
        this.columnString = columnString;
    }

    public long getColumnLong() {
        return realmGet$columnLong();
    }

    public void setColumnLong(long columnLong) {
        realmSet$columnLong(columnLong);
    }

    public long realmGet$columnLong() {
        return columnLong;
    }

    public void realmSet$columnLong(long columnLong) {
        this.columnLong = columnLong;
    }

    public float getColumnFloat() {
        return realmGet$columnFloat();
    }

    public void setColumnFloat(float columnFloat) {
        realmSet$columnFloat(columnFloat);
    }

    public float realmGet$columnFloat() {
        return columnFloat;
    }

    public void realmSet$columnFloat(float columnFloat) {
        this.columnFloat = columnFloat;
    }

    public double getColumnDouble() {
        return realmGet$columnDouble();
    }

    public void setColumnDouble(double columnDouble) {
        realmSet$columnDouble(columnDouble);
    }

    public double realmGet$columnDouble() {
        return columnDouble;
    }

    public void realmSet$columnDouble(double columnDouble) {
        this.columnDouble = columnDouble;
    }

    public boolean isColumnBoolean() {
        return realmGet$columnBoolean();
    }

    public void setColumnBoolean(boolean columnBoolean) {
        realmSet$columnBoolean(columnBoolean);
    }

    public boolean realmGet$columnBoolean() {
        return columnBoolean;
    }

    public void realmSet$columnBoolean(boolean columnBoolean) {
        this.columnBoolean = columnBoolean;
    }

    public Date getColumnDate() {
        return realmGet$columnDate();
    }

    public void setColumnDate(Date columnDate) {
        realmSet$columnDate(columnDate);
    }

    public Date realmGet$columnDate() {
        return columnDate;
    }

    public void realmSet$columnDate(Date columnDate) {
        this.columnDate = columnDate;
    }

    public byte[] getColumnBinary() {
        return realmGet$columnBinary();
    }

    public void setColumnBinary(byte[] columnBinary) {
        realmSet$columnBinary(columnBinary);
    }

    public byte[] realmGet$columnBinary() {
        return columnBinary;
    }

    public void realmSet$columnBinary(byte[] columnBinary) {
        this.columnBinary = columnBinary;
    }

    public AllTypes getColumnObject() {
        return realmGet$columnObject();
    }

    public void setColumnObject(AllTypes columnObject) {
        realmSet$columnObject(columnObject);
    }

    public AllTypes realmGet$columnObject() {
        return columnObject;
    }

    public void realmSet$columnObject(AllTypes columnObject) {
        this.columnObject = columnObject;
    }

    public RealmList<AllTypes> getColumnRealmList() {
        return realmGet$columnRealmList();
    }

    public void setColumnRealmList(RealmList<AllTypes> columnRealmList) {
        realmSet$columnRealmList(columnRealmList);
    }

    public RealmList<AllTypes> realmGet$columnRealmList() {
        return columnRealmList;
    }

    public void realmSet$columnRealmList(RealmList<AllTypes> columnRealmList) {
        this.columnRealmList = columnRealmList;
    }

    public MutableRealmInteger getColumnMutableRealmInteger() {
        return realmGet$columnMutableRealmInteger();
    }

    public MutableRealmInteger realmGet$columnMutableRealmInteger() {
        return columnMutableRealmInteger;
    }
}
