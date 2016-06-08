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

// Class used for testing what happens if you modify fields defined after the primary key field
public class MigrationPosteriorIndexOnly extends RealmObject implements MigrationPrimaryKey {
    public static String CLASS_NAME = "MigrationPosteriorIndexOnly";

    public static long DEFAULT_FIELDS_COUNT = 3;
    public static long DEFAULT_PRIMARY_INDEX = 0;

    @PrimaryKey
    private String fieldPrimary;
    private Integer fieldFourth;
    private Long fieldFifth;

    public void setFieldPrimary(String fieldPrimary) {
        this.fieldPrimary = fieldPrimary;
    }

    public String getFieldPrimary() {
        return this.fieldPrimary;
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
