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

import java.util.LinkedHashSet;
import java.util.Set;

class OsRealmObjectSchema extends RealmObjectSchema {
    private final long nativePtr;

    /**
     * Creates a schema object using object store. This constructor is intended to be used by
     * the validation of schema, object schemas and properties through the object store. Even though the constructor
     * is public, there is never a purpose which justifies calling it!
     *
     * @param className name of the class
     */
    OsRealmObjectSchema(String className) {
        this.nativePtr = nativeCreateRealmObjectSchema(className);
    }

    OsRealmObjectSchema(long nativePtr) {
        this.nativePtr = nativePtr;
    }

    @Override
    public void close() {
        Set<Property> properties = getProperties();
        for (Property property : properties) {
            property.close();
        }
        nativeClose(nativePtr);
    }

    @Override
    public String getClassName() {
        return nativeGetClassName(nativePtr);
    }

    @Override
    public OsRealmObjectSchema setClassName(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OsRealmObjectSchema addField(String fieldName, Class<?> fieldType, FieldAttribute... attributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OsRealmObjectSchema addRealmObjectField(String fieldName, RealmObjectSchema objectSchema) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OsRealmObjectSchema addRealmListField(String fieldName, RealmObjectSchema objectSchema) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OsRealmObjectSchema removeField(String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OsRealmObjectSchema renameField(String currentFieldName, String newFieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasField(String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OsRealmObjectSchema addIndex(String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasIndex(String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OsRealmObjectSchema removeIndex(String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OsRealmObjectSchema addPrimaryKey(String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OsRealmObjectSchema removePrimaryKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OsRealmObjectSchema setRequired(String fieldName, boolean required) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OsRealmObjectSchema setNullable(String fieldName, boolean nullable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequired(String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNullable(String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPrimaryKey(String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPrimaryKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPrimaryKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getFieldNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OsRealmObjectSchema transform(Function function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RealmFieldType getFieldType(String fieldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    long[] getColumnIndices(String fieldDescription, RealmFieldType... validColumnTypes) {
        throw new UnsupportedOperationException();
    }

    @Override
    OsRealmObjectSchema add(String name, RealmFieldType type, boolean primary, boolean indexed, boolean required) {
        nativeAddProperty(nativePtr, new Property(name, type, primary, indexed, required).getNativePtr());
        return this;
    }

    @Override
    OsRealmObjectSchema add(String name, RealmFieldType type, RealmObjectSchema linkedTo) {
        nativeAddProperty(nativePtr, new Property(name, type, linkedTo).getNativePtr());
        return this;
    }

    long getNativePtr() {
        return nativePtr;
    }

    private Set<Property> getProperties() {
        long[] ptrs = nativeGetProperties(nativePtr);
        Set<Property> properties = new LinkedHashSet<>(ptrs.length);
        for (int i = 0; i < ptrs.length; i++) {
            properties.add(new Property(ptrs[i]));
        }
        return properties;
    }

    static native long nativeCreateRealmObjectSchema(String className);

    static native void nativeAddProperty(long nativePtr, long nativePropertyPtr);

    static native long[] nativeGetProperties(long nativePtr);

    static native void nativeClose(long nativePtr);

    static native String nativeGetClassName(long nativePtr);
}
