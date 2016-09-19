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

package io.realm.entities;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RandomPrimaryKey extends RealmObject {

    public static final String CLASS_NAME = "RandomPrimaryKey";
    public static String FIELD_RANDOM_PRIMARY_KEY = "fieldRandomPrimaryKey";
    public static String FIELD_INT = "fieldInt";


    public static int FIELD_INT_DEFAULT_VALUE = 1357924;

    @PrimaryKey private String fieldRandomPrimaryKey = UUID.randomUUID().toString();
    private int fieldInt = FIELD_INT_DEFAULT_VALUE;

    public RandomPrimaryKey() {
    }

    public String getFieldRandomPrimaryKey() {
        return fieldRandomPrimaryKey;
    }

    public void setFieldRandomPrimaryKey(String fieldRandomPrimaryKey) {
        this.fieldRandomPrimaryKey = fieldRandomPrimaryKey;
    }

    public int getFieldInt() {
        return fieldInt;
    }

    public void setFieldInt(int fieldInt) {
        this.fieldInt = fieldInt;
    }
}
