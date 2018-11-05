/*
 * Copyright 2018 Realm Inc.
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
import io.realm.RealmResults;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.RealmNamingPolicy;


@RealmClass(fieldNamingPolicy = RealmNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
public class MappedAllJavaTypes extends RealmObject {

    public static final String CLASS_NAME = "MappedAllJavaTypes";

    @Ignore
    public String fieldIgnored;
    @Index
    public String fieldString;
    @PrimaryKey
    public long fieldId;
    public long fieldLong;
    public short fieldShort;
    public int fieldInt;
    public byte fieldByte;
    public float fieldFloat;
    public double fieldDouble;
    public boolean fieldBoolean;
    public Date fieldDate;
    public byte[] fieldBinary;
    public MappedAllJavaTypes fieldObject;
    public RealmList<MappedAllJavaTypes> fieldList;

    public RealmList<String> fieldStringList;
    public RealmList<byte[]> fieldBinaryList;
    public RealmList<Boolean> fieldBooleanList;
    public RealmList<Long> fieldLongList;
    public RealmList<Integer> fieldIntegerList;
    public RealmList<Short> fieldShortList;
    public RealmList<Byte> fieldByteList;
    public RealmList<Double> fieldDoubleList;
    public RealmList<Float> fieldFloatList;
    public RealmList<Date> fieldDateList;

    public MappedAllJavaTypes() {
    }

    public MappedAllJavaTypes(long fieldLong) {
        this.fieldId = fieldLong;
        this.fieldLong = fieldLong;
    }
}
