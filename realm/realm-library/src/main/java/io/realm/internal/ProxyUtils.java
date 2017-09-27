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
package io.realm.internal;

import java.util.Map;

import io.realm.RealmFieldType;
import io.realm.exceptions.RealmMigrationNeededException;

public class ProxyUtils {

    public static void verifyField(SharedRealm sharedRealm, Map<String, RealmFieldType> columnTypes, String fieldName, RealmFieldType fieldType, String fieldSimpleType) {
        if (!columnTypes.containsKey(fieldName)) {
            throw new RealmMigrationNeededException(
                    sharedRealm.getPath(),
                    String.format("Missing field '%s' in existing Realm file. Either remove field or migrate using io.realm.internal.Table.addColumn().", fieldName));
        }
        if (columnTypes.get(fieldName) != fieldType) {
            throw new RealmMigrationNeededException(
                    sharedRealm.getPath(),
                    String.format("Invalid type '%s' for field '%s' in existing Realm file.", fieldSimpleType, fieldName));
        }
    }
}
