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

package io.realm;

import io.realm.internal.ColumnIndices;
import io.realm.internal.Table;

/**
 * Immutable {@link RealmSchema}.
 */
class ImmutableRealmSchema extends RealmSchema {

    private static final String SCHEMA_IMMUTABLE_EXCEPTION_MSG = "This 'RealmSchema' is immutable." +
            " Please use 'DynamicRealm.getSchema() to get a mutable instance.";

    ImmutableRealmSchema(BaseRealm realm, ColumnIndices columnIndices) {
        super(realm, columnIndices);
    }

    @Override
    public RealmObjectSchema get(String className) {
        checkNotEmpty(className, EMPTY_STRING_MSG);

        String internalClassName = Table.getTableNameForClass(className);
        if (!realm.getSharedRealm().hasTable(internalClassName)) { return null; }
        Table table = realm.getSharedRealm().getTable(internalClassName);
        return new ImmutableRealmObjectSchema(realm, this, table, getColumnInfo(className));
    }

    @Override
    public RealmObjectSchema create(String className) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema createWithPrimaryKeyField(String className, String primaryKeyFieldName, Class<?> fieldType, FieldAttribute... attributes) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public void remove(String className) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema rename(String oldClassName, String newClassName) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }
}
