package io.realm;
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


import io.realm.internal.ColumnInfo;
import io.realm.internal.fields.FieldDescriptor;


/**
 * This is a proxy, whose sole reason for existence, is to make package protected
 * methods on Schema, visible outside the io.realm package.
 *
 * The class is in the package, so it has access to package protected methods.
 * The class is <b>not</b> outside the package.
 * The class implements one or more interfaces visible to package-external clients, that need them.
 *
 * I suggest creating instances of this through a factory method in the service class.
 * That will make it easy to lazily instantiate a singleton should that become advisable.
 */
class SchemaConnector implements FieldDescriptor.SchemaProxy {
    private final RealmSchema schema;

    public SchemaConnector(RealmSchema schema) {
        this.schema = schema;
    }

    @Override
    public boolean hasCache() {
        return schema.haveColumnInfo();
    }

    @Override
    public ColumnInfo getColumnInfo(String tableName) {
        return schema.getColumnInfo(tableName);
    }

    @Override
    public long getNativeTablePtr(String targetTable) {
        return schema.getTable(targetTable).getNativePtr();
    }
}
