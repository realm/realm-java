/*
 * Copyright 2017 Realm Inc.
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
package io.realm.entities.realmname;

import io.realm.RealmObject;
import io.realm.annotations.RealmName;
import io.realm.annotations.RealmNamePolicy;

// Class will inherit RealmNamePolicy.IDENTITY
@RealmName(policy = RealmNamePolicy.LOWER_CASE_WITH_DASHES)
public class FieldNameOverrideClassPolicy extends RealmObject {

    public static final String CLASS_NAME = "field-name-override-class-policy";
    public static final String FIELD_CAMEL_CASE = "camel_case";

    @RealmName(name = "camel_case")
    public String camelCase;

}
