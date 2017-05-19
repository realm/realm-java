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


import io.realm.RealmFieldType;

/**
 * Java wrapper for Object Store ObjectSchema.
 *
 * @see OsSchemaInfo
 */
public class OsObjectSchemaInfo implements NativeObject {
    private long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    /**
     * Creates an empty schema object using object store. This constructor is intended to be used by
     * the validation of schema, object schemas and properties through the object store.
     *
     * @param className name of the class
     */
    public OsObjectSchemaInfo(String className) {
        this(nativeCreateRealmObjectSchema(className));
    }

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


    /**
     * Adds a property to this {@code OsObjectSchema}.
     *
     * @param name the name of the property.
     * @param type the type of the property.
     * @param primary set to true if this property is the primary key.
     * @param indexed set to true if this property needs an index.
     * @param required set to false if this property is not nullable.
     * @return this {@code OsObjectSchemaInfo}.
     */
    public OsObjectSchemaInfo add(String name, RealmFieldType type, boolean primary, boolean indexed, boolean required) {
        final Property property = new Property(name, type, primary, indexed, required);
        try {
            nativeAddProperty(nativePtr, property.getNativePtr());
        } finally {
            property.close();
        }
        return this;
    }

    /**
     * Adds a linked property to this {@code OsObjectSchema}.
     *
     * @param name the name of the linked property.
     * @param type the type of the linked property.
     * @return this {@code OsObjectSchemaInfo}.
     */
    public OsObjectSchemaInfo add(String name, RealmFieldType type, String linkedClassName) {
        final Property property = new Property(name, type, linkedClassName);
        try {
            nativeAddProperty(nativePtr, property.getNativePtr());
        } finally {
            property.close();
        }
        return this;
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
