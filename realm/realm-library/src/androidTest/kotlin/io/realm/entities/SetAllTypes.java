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

import io.realm.RealmAny;
import io.realm.RealmObject;
import io.realm.RealmSet;
import io.realm.annotations.Required;

public class SetAllTypes extends RealmObject {

    private RealmSet<Boolean> columnBooleanSet;
    private RealmSet<String> columnStringSet;
    private RealmSet<Integer> columnIntegerSet;
    private RealmSet<Float> columnFloatSet;
    private RealmSet<Long> columnLongSet;
    private RealmSet<Short> columnShortSet;
    private RealmSet<Double> columnDoubleSet;
    private RealmSet<Byte> columnByteSet;
    private RealmSet<byte[]> columnBinarySet;
    private RealmSet<Date> columnDateSet;
    private RealmSet<ObjectId> columnObjectIdSet;
    private RealmSet<UUID> columnUUIDSet;
    private RealmSet<Decimal128> columnDecimal128Set;
    private RealmSet<DogPrimaryKey> columnRealmModelSet;
    private RealmSet<Owner> columnRealmModelNoPkSet;
    private RealmSet<RealmAny> columnRealmAnySet;


    public String columnString;

    @Required
    private RealmSet<Boolean> columnRequiredBooleanSet;
    @Required
    private RealmSet<String> columnRequiredStringSet;
    @Required
    private RealmSet<Integer> columnRequiredIntegerSet;
    @Required
    private RealmSet<Float> columnRequiredFloatSet;
    @Required
    private RealmSet<Long> columnRequiredLongSet;
    @Required
    private RealmSet<Short> columnRequiredShortSet;
    @Required
    private RealmSet<Double> columnRequiredDoubleSet;
    @Required
    private RealmSet<Byte> columnRequiredByteSet;
    @Required
    private RealmSet<byte[]> columnRequiredBinarySet;
    @Required
    private RealmSet<Date> columnRequiredDateSet;
    @Required
    private RealmSet<ObjectId> columnRequiredObjectIdSet;
    @Required
    private RealmSet<UUID> columnRequiredUUIDSet;
    @Required
    private RealmSet<Decimal128> columnRequiredDecimal128Set;

    public RealmSet<Boolean> getColumnBooleanSet() {
        return columnBooleanSet;
    }

    public void setColumnBooleanSet(RealmSet<Boolean> columnBooleanSet) {
        this.columnBooleanSet = columnBooleanSet;
    }

    public RealmSet<String> getColumnStringSet() {
        return columnStringSet;
    }

    public void setColumnStringSet(RealmSet<String> columnStringSet) {
        this.columnStringSet = columnStringSet;
    }

    public RealmSet<Integer> getColumnIntegerSet() {
        return columnIntegerSet;
    }

    public void setColumnIntegerSet(RealmSet<Integer> columnIntegerSet) {
        this.columnIntegerSet = columnIntegerSet;
    }

    public RealmSet<Float> getColumnFloatSet() {
        return columnFloatSet;
    }

    public void setColumnFloatSet(RealmSet<Float> columnFloatSet) {
        this.columnFloatSet = columnFloatSet;
    }

    public RealmSet<Long> getColumnLongSet() {
        return columnLongSet;
    }

    public void setColumnLongSet(RealmSet<Long> columnLongSet) {
        this.columnLongSet = columnLongSet;
    }

    public RealmSet<Short> getColumnShortSet() {
        return columnShortSet;
    }

    public void setColumnShortSet(RealmSet<Short> columnShortSet) {
        this.columnShortSet = columnShortSet;
    }

    public RealmSet<Double> getColumnDoubleSet() {
        return columnDoubleSet;
    }

    public void setColumnDoubleSet(RealmSet<Double> columnDoubleSet) {
        this.columnDoubleSet = columnDoubleSet;
    }

    public RealmSet<Byte> getColumnByteSet() {
        return columnByteSet;
    }

    public void setColumnByteSet(RealmSet<Byte> columnByteSet) {
        this.columnByteSet = columnByteSet;
    }

    public RealmSet<byte[]> getColumnBinarySet() {
        return columnBinarySet;
    }

    public void setColumnBinarySet(RealmSet<byte[]> columnBinarySet) {
        this.columnBinarySet = columnBinarySet;
    }

    public RealmSet<Date> getColumnDateSet() {
        return columnDateSet;
    }

    public void setColumnDateSet(RealmSet<Date> columnDateSet) {
        this.columnDateSet = columnDateSet;
    }

    public RealmSet<ObjectId> getColumnObjectIdSet() {
        return columnObjectIdSet;
    }

    public void setColumnObjectIdSet(RealmSet<ObjectId> columnObjectIdSet) {
        this.columnObjectIdSet = columnObjectIdSet;
    }

    public RealmSet<UUID> getColumnUUIDSet() {
        return columnUUIDSet;
    }

    public void setColumnUUIDSet(RealmSet<UUID> columnUUIDSet) {
        this.columnUUIDSet = columnUUIDSet;
    }

    public RealmSet<Decimal128> getColumnDecimal128Set() {
        return columnDecimal128Set;
    }

    public void setColumnDecimal128Set(RealmSet<Decimal128> columnDecimal128Set) {
        this.columnDecimal128Set = columnDecimal128Set;
    }

    public RealmSet<DogPrimaryKey> getColumnRealmModelSet() {
        return columnRealmModelSet;
    }

    public void setColumnRealmModelSet(RealmSet<DogPrimaryKey> columnRealmModelSet) {
        this.columnRealmModelSet = columnRealmModelSet;
    }

    public RealmSet<Owner> getColumnRealmModelNoPkSet() {
        return columnRealmModelNoPkSet;
    }

    public void setColumnRealmModelNoPkSet(RealmSet<Owner> columnRealmModelNoPkSet) {
        this.columnRealmModelNoPkSet = columnRealmModelNoPkSet;
    }

    public RealmSet<RealmAny> getColumnRealmAnySet() {
        return columnRealmAnySet;
    }

    public void setColumnRealmAnySet(RealmSet<RealmAny> columnRealmAnySet) {
        this.columnRealmAnySet = columnRealmAnySet;
    }

    public RealmSet<Boolean> getColumnRequiredBooleanSet() {
        return columnRequiredBooleanSet;
    }

    public void setColumnRequiredBooleanSet(RealmSet<Boolean> columnRequiredBooleanSet) {
        this.columnRequiredBooleanSet = columnRequiredBooleanSet;
    }

    public RealmSet<String> getColumnRequiredStringSet() {
        return columnRequiredStringSet;
    }

    public void setColumnRequiredStringSet(RealmSet<String> columnRequiredStringSet) {
        this.columnRequiredStringSet = columnRequiredStringSet;
    }

    public RealmSet<Integer> getColumnRequiredIntegerSet() {
        return columnRequiredIntegerSet;
    }

    public void setColumnRequiredIntegerSet(RealmSet<Integer> columnRequiredIntegerSet) {
        this.columnRequiredIntegerSet = columnRequiredIntegerSet;
    }

    public RealmSet<Float> getColumnRequiredFloatSet() {
        return columnRequiredFloatSet;
    }

    public void setColumnRequiredFloatSet(RealmSet<Float> columnRequiredFloatSet) {
        this.columnRequiredFloatSet = columnRequiredFloatSet;
    }

    public RealmSet<Long> getColumnRequiredLongSet() {
        return columnRequiredLongSet;
    }

    public void setColumnRequiredLongSet(RealmSet<Long> columnRequiredLongSet) {
        this.columnRequiredLongSet = columnRequiredLongSet;
    }

    public RealmSet<Short> getColumnRequiredShortSet() {
        return columnRequiredShortSet;
    }

    public void setColumnRequiredShortSet(RealmSet<Short> columnRequiredShortSet) {
        this.columnRequiredShortSet = columnRequiredShortSet;
    }

    public RealmSet<Double> getColumnRequiredDoubleSet() {
        return columnRequiredDoubleSet;
    }

    public void setColumnRequiredDoubleSet(RealmSet<Double> columnRequiredDoubleSet) {
        this.columnRequiredDoubleSet = columnRequiredDoubleSet;
    }

    public RealmSet<Byte> getColumnRequiredByteSet() {
        return columnRequiredByteSet;
    }

    public void setColumnRequiredByteSet(RealmSet<Byte> columnRequiredByteSet) {
        this.columnRequiredByteSet = columnRequiredByteSet;
    }

    public RealmSet<byte[]> getColumnRequiredBinarySet() {
        return columnRequiredBinarySet;
    }

    public void setColumnRequiredBinarySet(RealmSet<byte[]> columnRequiredBinarySet) {
        this.columnRequiredBinarySet = columnRequiredBinarySet;
    }

    public RealmSet<Date> getColumnRequiredDateSet() {
        return columnRequiredDateSet;
    }

    public void setColumnRequiredDateSet(RealmSet<Date> columnRequiredDateSet) {
        this.columnRequiredDateSet = columnRequiredDateSet;
    }

    public RealmSet<ObjectId> getColumnRequiredObjectIdSet() {
        return columnRequiredObjectIdSet;
    }

    public void setColumnRequiredObjectIdSet(RealmSet<ObjectId> columnRequiredObjectIdSet) {
        this.columnRequiredObjectIdSet = columnRequiredObjectIdSet;
    }

    public RealmSet<UUID> getColumnRequiredUUIDSet() {
        return columnRequiredUUIDSet;
    }

    public void setColumnRequiredUUIDSet(RealmSet<UUID> columnRequiredUUIDSet) {
        this.columnRequiredUUIDSet = columnRequiredUUIDSet;
    }

    public RealmSet<Decimal128> getColumnRequiredDecimal128Set() {
        return columnRequiredDecimal128Set;
    }

    public void setColumnRequiredDecimal128Set(RealmSet<Decimal128> columnRequiredDecimal128Set) {
        this.columnRequiredDecimal128Set = columnRequiredDecimal128Set;
    }
}
