/*
 * Copyright 2016 Realm Inc.
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

package io.realm.entities.pojo;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.entities.Dog;

@RealmClass
public class AllTypesRealmModel implements RealmModel {
    public static final String CLASS_NAME = "AllTypesRealmModel";
    public static final String FIELD_LONG = "columnLong";
    public static final String FIELD_BYTE = "columnByte";
    public static final String FIELD_DOUBLE = "columnDouble";
    public static final String FIELD_STRING = "columnString";
    public static final String FIELD_BINARY = "columnBinary";
    public static final String FIELD_BOOLEAN = "columnBoolean";

    @Index
    public String columnString;
    @PrimaryKey
    public long columnLong;
    public byte columnByte;
    public float columnFloat;
    public double columnDouble;
    public boolean columnBoolean;
    public Date columnDate;
    public byte[] columnBinary;
    public Dog columnRealmObject;
    public RealmList<Dog> columnRealmList;

    @Override
    public int hashCode() {
        return 42;
    }
}
