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

import java.util.Date;
import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class DefaultValueOverwriteNullLink extends RealmObject {

    public static final String CLASS_NAME = "DefaultValueOverwriteNullLink";
    public static String FIELD_OBJECT = "fieldObject";
    public static String EXPECTED_KEY_OF_FIELD_OBJECT = "expectedKeyOfFieldObject";

    private RandomPrimaryKey fieldObject;
    private String expectedKeyOfFieldObject;

    public DefaultValueOverwriteNullLink() {
        final RandomPrimaryKey firstDefaultValue = new RandomPrimaryKey();
        final RandomPrimaryKey secondDefaultValue = new RandomPrimaryKey();

        expectedKeyOfFieldObject = secondDefaultValue.getFieldRandomPrimaryKey();

        fieldObject = firstDefaultValue;
        fieldObject = null;
        fieldObject = secondDefaultValue;
    }

    public RandomPrimaryKey getFieldObject() {
        return fieldObject;
    }

    public void setFieldObject(RandomPrimaryKey fieldObject) {
        this.fieldObject = fieldObject;
    }

    public String getExpectedKeyOfFieldObject() {
        return expectedKeyOfFieldObject;
    }

    public void setExpectedKeyOfFieldObject(String expectedKeyOfFieldObject) {
        this.expectedKeyOfFieldObject = expectedKeyOfFieldObject;
    }
}
