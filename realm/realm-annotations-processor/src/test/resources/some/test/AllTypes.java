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

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.UUID;

import io.realm.Mixed;
import io.realm.MutableRealmInteger;
import io.realm.RealmDictionary;
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
    private Decimal128 columnDecimal128;
    @Required
    private ObjectId columnObjectId;
    @Required
    private UUID columnUUID;

    @Required
    private Date columnDate;

    private Mixed columnMixed;

    @Required
    private byte[] columnBinary;

    private final MutableRealmInteger columnMutableRealmInteger = MutableRealmInteger.valueOf(0);

    private AllTypes columnObject;
    private Simple columnObjectWithoutPk;

    private RealmList<AllTypes> columnRealmList;
    private RealmList<Simple> columnRealmListNoPk;

    private final RealmList<AllTypes> columnRealmFinalList = new RealmList<>();
    private final RealmList<Simple> columnRealmFinalListNoPk = new RealmList<>();

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
    private RealmList<Decimal128> columnDecimal128List;
    private RealmList<ObjectId> columnObjectIdList;
    private RealmList<UUID> columnUUIDList;
    private RealmList<Mixed> columnMixedList;

    private RealmDictionary<AllTypes> columnRealmDictionary;

    private RealmDictionary<Boolean> columnBooleanDictionary;
    private RealmDictionary<String> columnStringDictionary;
    private RealmDictionary<Integer> columnIntegerDictionary;
    private RealmDictionary<Float> columnFloatDictionary;
    private RealmDictionary<Long> columnLongDictionary;
    private RealmDictionary<Short> columnShortDictionary;
    private RealmDictionary<Double> columnDoubleDictionary;
    private RealmDictionary<Byte> columnByteDictionary;
    private RealmDictionary<byte[]> columnBinaryDictionary;
    private RealmDictionary<Date> columnDateDictionary;
    private RealmDictionary<ObjectId> columnObjectIdDictionary;
    private RealmDictionary<UUID> columnUUIDDictionary;
    private RealmDictionary<Decimal128> columnDecimal128Dictionary;
    private RealmDictionary<Mixed> columnMixedDictionary;

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

    public Mixed getColumnMixed() {
        return realmGet$columnMixed();
    }

    public Mixed realmGet$columnMixed() {
        return columnMixed;
    }

    public RealmDictionary<AllTypes> getColumnRealmDictionary() {
        return columnRealmDictionary;
    }

    public void setColumnRealmDictionary(RealmDictionary<AllTypes> columnRealmDictionary) {
        this.columnRealmDictionary = columnRealmDictionary;
    }

    public RealmDictionary<Boolean> getColumnBooleanDictionary() {
        return columnBooleanDictionary;
    }

    public void setColumnBooleanDictionary(RealmDictionary<Boolean> columnBooleanDictionary) {
        this.columnBooleanDictionary = columnBooleanDictionary;
    }

    public RealmDictionary<String> getColumnStringDictionary() {
        return columnStringDictionary;
    }

    public void setColumnStringDictionary(RealmDictionary<String> columnStringDictionary) {
        this.columnStringDictionary = columnStringDictionary;
    }

    public RealmDictionary<Integer> getColumnIntegerDictionary() {
        return columnIntegerDictionary;
    }

    public void setColumnIntegerDictionary(RealmDictionary<Integer> columnIntegerDictionary) {
        this.columnIntegerDictionary = columnIntegerDictionary;
    }

    public RealmDictionary<Float> getColumnFloatDictionary() {
        return columnFloatDictionary;
    }

    public void setColumnFloatDictionary(RealmDictionary<Float> columnFloatDictionary) {
        this.columnFloatDictionary = columnFloatDictionary;
    }

    public RealmDictionary<Long> getColumnLongDictionary() {
        return columnLongDictionary;
    }

    public void setColumnLongDictionary(RealmDictionary<Long> columnLongDictionary) {
        this.columnLongDictionary = columnLongDictionary;
    }

    public RealmDictionary<Short> getColumnShortDictionary() {
        return columnShortDictionary;
    }

    public void setColumnShortDictionary(RealmDictionary<Short> columnShortDictionary) {
        this.columnShortDictionary = columnShortDictionary;
    }

    public RealmDictionary<Double> getColumnDoubleDictionary() {
        return columnDoubleDictionary;
    }

    public void setColumnDoubleDictionary(RealmDictionary<Double> columnDoubleDictionary) {
        this.columnDoubleDictionary = columnDoubleDictionary;
    }

    public RealmDictionary<Byte> getColumnByteDictionary() {
        return columnByteDictionary;
    }

    public void setColumnByteDictionary(RealmDictionary<Byte> columnByteDictionary) {
        this.columnByteDictionary = columnByteDictionary;
    }

    public RealmDictionary<byte[]> getColumnBinaryDictionary() {
        return columnBinaryDictionary;
    }

    public void setColumnBinaryDictionary(RealmDictionary<byte[]> columnBinaryDictionary) {
        this.columnBinaryDictionary = columnBinaryDictionary;
    }

    public RealmDictionary<Date> getColumnDateDictionary() {
        return columnDateDictionary;
    }

    public void setColumnDateDictionary(RealmDictionary<Date> columnDateDictionary) {
        this.columnDateDictionary = columnDateDictionary;
    }

    public RealmDictionary<ObjectId> getColumnObjectIdDictionary() {
        return columnObjectIdDictionary;
    }

    public void setColumnObjectIdDictionary(RealmDictionary<ObjectId> columnObjectIdDictionary) {
        this.columnObjectIdDictionary = columnObjectIdDictionary;
    }

    public RealmDictionary<UUID> getColumnUUIDDictionary() {
        return columnUUIDDictionary;
    }

    public void setColumnUUIDDictionary(RealmDictionary<UUID> columnUUIDDictionary) {
        this.columnUUIDDictionary = columnUUIDDictionary;
    }

    public RealmDictionary<Decimal128> getColumnDecimal128Dictionary() {
        return columnDecimal128Dictionary;
    }

    public void setColumnDecimal128Dictionary(RealmDictionary<Decimal128> columnDecimal128Dictionary) {
        this.columnDecimal128Dictionary = columnDecimal128Dictionary;
    }

    public RealmDictionary<Mixed> getColumnMixedDictionary() {
        return columnMixedDictionary;
    }

    public void setColumnMixedDictionary(RealmDictionary<Mixed> columnMixedDictionary) {
        this.columnMixedDictionary = columnMixedDictionary;
    }
}
