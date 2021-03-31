/*
 * Copyright 2021 Realm Inc.
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
import io.realm.RealmDictionary;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class DictionaryAllTypes extends RealmObject {
    public static final String FIELD_STRING_DICTIONARY = "columnStringDictionary";

    @PrimaryKey
    private String id = "";

    private RealmDictionary<DogPrimaryKey> columnRealmDictionary;

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

    @Required
    private RealmDictionary<Boolean> columnRequiredBooleanDictionary;
    @Required
    private RealmDictionary<String> columnRequiredStringDictionary;
    @Required
    private RealmDictionary<Integer> columnRequiredIntegerDictionary;
    @Required
    private RealmDictionary<Float> columnRequiredFloatDictionary;
    @Required
    private RealmDictionary<Long> columnRequiredLongDictionary;
    @Required
    private RealmDictionary<Short> columnRequiredShortDictionary;
    @Required
    private RealmDictionary<Double> columnRequiredDoubleDictionary;
    @Required
    private RealmDictionary<Byte> columnRequiredByteDictionary;
    @Required
    private RealmDictionary<byte[]> columnRequiredBinaryDictionary;
    @Required
    private RealmDictionary<Date> columnRequiredDateDictionary;
    @Required
    private RealmDictionary<ObjectId> columnRequiredObjectIdDictionary;
    @Required
    private RealmDictionary<UUID> columnRequiredUUIDDictionary;
    @Required
    private RealmDictionary<Decimal128> columnRequiredDecimal128Dictionary;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RealmDictionary<DogPrimaryKey> getColumnRealmDictionary() {
        return columnRealmDictionary;
    }

    public void setColumnRealmDictionary(RealmDictionary<DogPrimaryKey> columnRealmDictionary) {
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

    public RealmDictionary<Boolean> getColumnRequiredBooleanDictionary() {
        return columnRequiredBooleanDictionary;
    }

    public void setColumnRequiredBooleanDictionary(RealmDictionary<Boolean> columnRequiredBooleanDictionary) {
        this.columnRequiredBooleanDictionary = columnRequiredBooleanDictionary;
    }

    public RealmDictionary<String> getColumnRequiredStringDictionary() {
        return columnRequiredStringDictionary;
    }

    public void setColumnRequiredStringDictionary(RealmDictionary<String> columnRequiredStringDictionary) {
        this.columnRequiredStringDictionary = columnRequiredStringDictionary;
    }

    public RealmDictionary<Integer> getColumnRequiredIntegerDictionary() {
        return columnRequiredIntegerDictionary;
    }

    public void setColumnRequiredIntegerDictionary(RealmDictionary<Integer> columnRequiredIntegerDictionary) {
        this.columnRequiredIntegerDictionary = columnRequiredIntegerDictionary;
    }

    public RealmDictionary<Float> getColumnRequiredFloatDictionary() {
        return columnRequiredFloatDictionary;
    }

    public void setColumnRequiredFloatDictionary(RealmDictionary<Float> columnRequiredFloatDictionary) {
        this.columnRequiredFloatDictionary = columnRequiredFloatDictionary;
    }

    public RealmDictionary<Long> getColumnRequiredLongDictionary() {
        return columnRequiredLongDictionary;
    }

    public void setColumnRequiredLongDictionary(RealmDictionary<Long> columnRequiredLongDictionary) {
        this.columnRequiredLongDictionary = columnRequiredLongDictionary;
    }

    public RealmDictionary<Short> getColumnRequiredShortDictionary() {
        return columnRequiredShortDictionary;
    }

    public void setColumnRequiredShortDictionary(RealmDictionary<Short> columnRequiredShortDictionary) {
        this.columnRequiredShortDictionary = columnRequiredShortDictionary;
    }

    public RealmDictionary<Double> getColumnRequiredDoubleDictionary() {
        return columnRequiredDoubleDictionary;
    }

    public void setColumnRequiredDoubleDictionary(RealmDictionary<Double> columnRequiredDoubleDictionary) {
        this.columnRequiredDoubleDictionary = columnRequiredDoubleDictionary;
    }

    public RealmDictionary<Byte> getColumnRequiredByteDictionary() {
        return columnRequiredByteDictionary;
    }

    public void setColumnRequiredByteDictionary(RealmDictionary<Byte> columnRequiredByteDictionary) {
        this.columnRequiredByteDictionary = columnRequiredByteDictionary;
    }

    public RealmDictionary<byte[]> getColumnRequiredBinaryDictionary() {
        return columnRequiredBinaryDictionary;
    }

    public void setColumnRequiredBinaryDictionary(RealmDictionary<byte[]> columnRequiredBinaryDictionary) {
        this.columnRequiredBinaryDictionary = columnRequiredBinaryDictionary;
    }


    public RealmDictionary<Date> getColumnRequiredDateDictionary() {
        return columnRequiredDateDictionary;
    }

    public void setColumnRequiredDateDictionary(RealmDictionary<Date> columnRequiredDateDictionary) {
        this.columnRequiredDateDictionary = columnRequiredDateDictionary;
    }

    public RealmDictionary<ObjectId> getColumnRequiredObjectIdDictionary() {
        return columnRequiredObjectIdDictionary;
    }

    public void setColumnRequiredObjectIdDictionary(RealmDictionary<ObjectId> columnRequiredObjectIdDictionary) {
        this.columnRequiredObjectIdDictionary = columnRequiredObjectIdDictionary;
    }

    public RealmDictionary<UUID> getColumnRequiredUUIDDictionary() {
        return columnRequiredUUIDDictionary;
    }

    public void setColumnRequiredUUIDDictionary(RealmDictionary<UUID> columnRequiredUUIDDictionary) {
        this.columnRequiredUUIDDictionary = columnRequiredUUIDDictionary;
    }

    public RealmDictionary<Decimal128> getColumnRequiredDecimal128Dictionary() {
        return columnRequiredDecimal128Dictionary;
    }

    public void setColumnRequiredDecimal128Dictionary(RealmDictionary<Decimal128> columnRequiredDecimal128Dictionary) {
        this.columnRequiredDecimal128Dictionary = columnRequiredDecimal128Dictionary;
    }
}
