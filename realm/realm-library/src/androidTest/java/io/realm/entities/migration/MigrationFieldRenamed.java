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

// Class used to test what happens if you rename a primary field in a migration.
public class MigrationFieldRenamed extends RealmObject implements MigrationPrimaryKey {
    public static String CLASS_NAME = "MigrationFieldRenamed";

    public static long DEFAULT_FIELDS_COUNT = 5;
    public static long DEFAULT_PRIMARY_INDEX = 2;

    public static String FIELD_PRIMARY = "fieldRenamedPrimary";

    private Byte fieldFirst;
    private Short fieldSecond;

    // PK is placed in the middle to check if prior/posterior fields' removal is properly reflected
    // during migration step. The MigrationPrimaryKey interface' PK field name is `fieldPrimary`.
    @PrimaryKey
    private String fieldRenamedPrimary;
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

    public void setFieldRenamedPrimary(String fieldRenamedPrimary) {
        this.fieldRenamedPrimary = fieldRenamedPrimary;
    }

    public String getFieldRenamedPrimary() {
        return this.fieldRenamedPrimary;
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
