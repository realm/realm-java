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
import io.realm.annotations.PrimaryKey;

public class SetAllTypesPrimaryKey extends RealmObject {

    @PrimaryKey
    private long columnLong;

    public RealmSet<Boolean> columnBooleanSet;
    public RealmSet<String> columnStringSet;
    public RealmSet<Integer> columnIntegerSet;
    public RealmSet<Float> columnFloatSet;
    public RealmSet<Long> columnLongSet;
    public RealmSet<Short> columnShortSet;
    public RealmSet<Double> columnDoubleSet;
    public RealmSet<Byte> columnByteSet;
    public RealmSet<byte[]> columnBinarySet;
    public RealmSet<Date> columnDateSet;
    public RealmSet<ObjectId> columnObjectIdSet;
    public RealmSet<UUID> columnUUIDSet;
    public RealmSet<Decimal128> columnDecimal128Set;
    public RealmSet<DogPrimaryKey> columnRealmModelSet;
    public RealmSet<Owner> columnRealmModelNoPkSet;
    public RealmSet<RealmAny> columnRealmAnySet;

    public long getColumnLong() {
        return columnLong;
    }

    public void setColumnLong(long columnLong) {
        this.columnLong = columnLong;
    }
}
