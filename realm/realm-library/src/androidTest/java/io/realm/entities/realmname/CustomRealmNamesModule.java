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
package io.realm.entities.realmname;

import io.realm.annotations.RealmModule;
import io.realm.annotations.RealmNamingPolicy;

@RealmModule(classes =
        {
            ClassNameOverrideModulePolicy.class,
            ClassWithPolicy.class,
            ClassWithValueDefinedNames.class,
            DefaultPolicyFromModule.class,
            FieldNameOverrideClassPolicy.class
        },
        classNamingPolicy = RealmNamingPolicy.LOWER_CASE_WITH_UNDERSCORES,
        fieldNamingPolicy = RealmNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
)
public class CustomRealmNamesModule {

}
