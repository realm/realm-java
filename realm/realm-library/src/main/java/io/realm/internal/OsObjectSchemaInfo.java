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


import java.util.ArrayList;
import java.util.List;

import io.realm.RealmFieldType;

/**
 * Immutable Java wrapper for Object Store ObjectSchema.
 *
 * @see OsSchemaInfo
 */
public class OsObjectSchemaInfo implements NativeObject {

    public static class Builder {
        private String className;
        private List<Property> propertyList = new ArrayList<Property>();

        /**
         * Creates an empty builder for {@code OsObjectSchemaInfo}. This constructor is intended to be used by
         * the validation of schema, object schemas and properties through the object store.
         *
         * @param className name of the class
         */
        public Builder(String className) {
            this.className = className;
        }

        /**
         * Adds a property to this builder.
         *
         * @param name the name of the property.
         * @param type the type of the property.
         * @param isPrimaryKey set to true if this property is the primary key.
         * @param isIndexed set to true if this property needs an index.
         * @param isRequired set to false if this property is not nullable.
         * @return this {@code OsObjectSchemaInfo}.
         */
        public Builder addProperty(String name, RealmFieldType type, boolean isPrimaryKey, boolean isIndexed,
                                   boolean isRequired) {
            final Property property = new Property(name, type, isPrimaryKey, isIndexed, isRequired);
            propertyList.add(property);
            return this;
        }

        /**
         * Adds a linked property to this {@code OsObjectSchema}.
         *
         * @param name the name of the linked property.
         * @param type the type of the linked property.
         * @return this {@code OsObjectSchemaInfo}.
         */
        public Builder addLinkedProperty(String name, RealmFieldType type, String linkedClassName) {
            final Property property = new Property(name, type, linkedClassName);
            propertyList.add(property);
            return this;
        }

        public OsObjectSchemaInfo build() {
            OsObjectSchemaInfo info = new OsObjectSchemaInfo(className);
            for (Property property : propertyList) {
                nativeAddProperty(info.nativePtr, property.getNativePtr());
            }

            return info;
        }
    }

    private long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    /**
     * Creates an empty schema object using object store. This constructor is intended to be used by
     * the validation of schema, object schemas and properties through the object store.
     *
     * @param className name of the class
     */
    private OsObjectSchemaInfo(String className) {
        this(nativeCreateRealmObjectSchema(className));
    }

    /**
     * Create a java wrapper class for given {@code ObjectSchema} pointer. This java wrapper will take the ownership of
     * the object's memory and release it through phantom reference.
     *
     * @param nativePtr pointer to the {@code ObjectSchema} object.
     */
    private OsObjectSchemaInfo(long nativePtr) {
        this.nativePtr = nativePtr;
        NativeContext.dummyContext.addReference(this);
    }

    /**
     * @return the class name of this {@code OsObjectSchema} represents for.
     */
    public String getClassName() {
        return nativeGetClassName(nativePtr);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    private static native long nativeCreateRealmObjectSchema(String className);

    private static native long nativeGetFinalizerPtr();

    private static native void nativeAddProperty(long nativePtr, long nativePropertyPtr);

    private static native String nativeGetClassName(long nativePtr);
}
