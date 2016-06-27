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

package io.realm.entities.migration;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.migration.MigrationPrimaryKey;

// Class used to test what happens if you modify a String type primary key field to nullable Integer type in a migration.
public class MigrationFieldTypeToInteger extends RealmObject implements MigrationPrimaryKey {
    public static String CLASS_NAME = "MigrationFieldTypeToInteger";

    public static long DEFAULT_FIELDS_COUNT = 5;
    public static long DEFAULT_PRIMARY_INDEX = 4;

    public static String FIELD_PRIMARY = "fieldIntegerPrimary";

    private Byte fieldFirst;
    private Short fieldSecond;
    @PrimaryKey
    private Integer fieldIntegerPrimary;
    private Integer fieldFourth;
    private Long fieldFifth;

    public void setFieldFirst(Byte fieldFirst) {
        this.fieldFirst = fieldFirst;
    }

    public Byte getFieldFirst() {
        return this.fieldFirst;
    }

    public void setFieldSecond(Short fieldSecond) {
        this.fieldSecond = fieldSecond;
    }

    public Short getFieldSecond() {
        return this.fieldSecond;
    }

    public void setFieldIntegerPrimary(Integer fieldIntegerPrimary) {
        this.fieldIntegerPrimary = fieldIntegerPrimary;
    }

    public Integer getFieldIntegerPrimary() {
        return this.fieldIntegerPrimary;
    }

    public void setFieldFourth(Integer fieldFourth) {
        this.fieldFourth = fieldFourth;
    }

    public Integer getFieldFourth() {
        return this.fieldFourth;
    }

    public void setFieldFifth(Long fieldFifth) {
        this.fieldFifth = fieldFifth;
    }

    public Long getFieldFifth() {
        return this.fieldFifth;
    }
}
