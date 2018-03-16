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

import io.realm.internal.ColumnInfo;
import io.realm.internal.Table;
import io.realm.internal.fields.FieldDescriptor;

/**
 * Immutable {@link RealmObjectSchema}.
 */
class ImmutableRealmObjectSchema extends RealmObjectSchema {

    private static final String SCHEMA_IMMUTABLE_EXCEPTION_MSG = "This 'RealmObjectSchema' is immutable." +
            " Please use 'DynamicRealm.getSchema() to get a mutable instance.";

    ImmutableRealmObjectSchema(BaseRealm realm, RealmSchema schema, Table table, ColumnInfo columnInfo) {
        super(realm, schema, table, columnInfo);
    }

    ImmutableRealmObjectSchema(BaseRealm realm, RealmSchema schema, Table table) {
        super(realm, schema, table, new DynamicColumnIndices(table));
    }

    @Override
    public RealmObjectSchema setClassName(String className) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema addField(String fieldName, Class<?> fieldType, FieldAttribute... attributes) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema addRealmObjectField(String fieldName, RealmObjectSchema objectSchema) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema addRealmListField(String fieldName, RealmObjectSchema objectSchema) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema addRealmListField(String fieldName, Class<?> primitiveType) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema removeField(String fieldName) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema renameField(String currentFieldName, String newFieldName) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema addIndex(String fieldName) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema removeIndex(String fieldName) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema addPrimaryKey(String fieldName) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema removePrimaryKey() {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema setRequired(String fieldName, boolean required) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema setNullable(String fieldName, boolean nullable) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    @Override
    public RealmObjectSchema transform(Function function) {
        throw new UnsupportedOperationException(SCHEMA_IMMUTABLE_EXCEPTION_MSG);
    }

    /**
     * Returns a field descriptor based on Java field names found in model classes.
     *
     * @param publicJavaNameDescription field name or linked field description
     * @param validColumnTypes valid field type for the last field in a linked field
     * @return the corresponding FieldDescriptor.
     * @throws IllegalArgumentException if a proper FieldDescriptor could not be created.
     */
    @Override
    FieldDescriptor getColumnIndices(String publicJavaNameDescription, RealmFieldType... validColumnTypes) {
        return FieldDescriptor.createStandardFieldDescriptor(getSchemaConnector(), getTable(), publicJavaNameDescription, validColumnTypes);
    }
}
