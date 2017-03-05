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

package io.realm.migration;

/**
 * This interface should be used as a stub for dynamically creating the first version of schema.
 * As this is an interface and does not inherit RealmObject, a schema for {@link MigrationPrimaryKey}
 * there does not exist.
 */
public interface MigrationPrimaryKey {
    String CLASS_NAME    = "MigrationPrimaryKey";

    String FIELD_FIRST   = "fieldFirst";
    String FIELD_SECOND  = "fieldSecond";

    // this is original primary key field name.
    String FIELD_PRIMARY = "fieldPrimary";
    String FIELD_FOURTH  = "fieldFourth";
    String FIELD_FIFTH   = "fieldFifth";
}
