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


import javax.annotation.Nullable;

import io.realm.RealmFieldType;

/**
 * Immutable Java wrapper for Object Store ObjectSchema.
 *
 * @see OsSchemaInfo
 */
public class OsObjectSchemaInfo implements NativeObject {

    public static class Builder {
        private final String className;
        private final long[] persistedPropertyPtrArray;
        private int persistedPropertyPtrCurPos = 0;
        private final long[] computedPropertyPtrArray;
        private int computedPropertyPtrCurPos = 0;

        /**
         * Creates an empty builder for {@code OsObjectSchemaInfo}. This constructor is intended to be used by
         * the validation of schema, object schemas and properties through the object store.
         *
         * @param className name of the class
         */
        public Builder(String className, int persistedPropertyCapacity, int computedPropertyCapacity) {
            this.className = className;
            this.persistedPropertyPtrArray = new long[persistedPropertyCapacity];
            this.computedPropertyPtrArray = new long[computedPropertyCapacity];
        }

        /**
         * Adds a persisted non-link, non value list property to this builder.
         *
         * @param name the name of the property.
         * @param type the type of the property.
         * @param isPrimaryKey set to true if this property is the primary key.
         * @param isIndexed set to true if this property needs an index.
         * @param isRequired set to true if this property is not nullable.
         * @return this {@code OsObjectSchemaInfo}.
         */
        public Builder addPersistedProperty(String name, RealmFieldType type, boolean isPrimaryKey, boolean isIndexed,
                boolean isRequired) {
            long propertyPtr = Property.nativeCreatePersistedProperty(name,
                    Property.convertFromRealmFieldType(type, isRequired), isPrimaryKey, isIndexed);
            persistedPropertyPtrArray[persistedPropertyPtrCurPos] = propertyPtr;
            persistedPropertyPtrCurPos++;
            return this;
        }

        /**
         * Adds a persisted value list property to this builder.
         *
         * @param name the name of the property.
         * @param type the type of the property. It must be one of value list type.
         * @param isRequired set to true if this property is not nullable.
         * @return this {@code OsObjectSchemaInfo}.
         */
        public Builder addPersistedValueListProperty(String name, RealmFieldType type, boolean isRequired) {
            long propertyPtr = Property.nativeCreatePersistedProperty(name,
                    Property.convertFromRealmFieldType(type, isRequired), !Property.PRIMARY_KEY, !Property.INDEXED);
            persistedPropertyPtrArray[persistedPropertyPtrCurPos] = propertyPtr;
            persistedPropertyPtrCurPos++;
            return this;
        }

        /**
         * Adds a persisted link property to this {@code OsObjectSchemaInfo}. A persisted link property will be stored
         * in the Realm file's schema.
         *
         * @param name the name of the link property.
         * @param type the type of the link property. Can only be {@link RealmFieldType#OBJECT} or
         * {@link RealmFieldType#LIST}.
         * @return this {@code OsObjectSchemaInfo.Builder}.
         */
        public Builder addPersistedLinkProperty(String name, RealmFieldType type, String linkedClassName) {
            long propertyPtr = Property.nativeCreatePersistedLinkProperty(name,
                    Property.convertFromRealmFieldType(type, false), linkedClassName);
            persistedPropertyPtrArray[persistedPropertyPtrCurPos] = propertyPtr;
            persistedPropertyPtrCurPos++;
            return this;
        }

        /**
         * Adds a computed link property to this {@code OsObjectSchemaInfo}. A computed link property doesn't store
         * information in the Realm file's schema. This property type will always be
         * {@link RealmFieldType#LINKING_OBJECTS}.
         *
         * @param name the name of the property .
         * @param sourceClass The class name of the the class linking to this class, ie. the source class.
         * @param sourceClassName The field name in the source class that links to this class.
         * @return this {@code OsObjectSchemaInfo.Builder}.
         */
        public Builder addComputedLinkProperty(String name, String sourceClass, String sourceClassName) {
            long propertyPtr = Property.nativeCreateComputedLinkProperty(name, sourceClass, sourceClassName);
            computedPropertyPtrArray[computedPropertyPtrCurPos] = propertyPtr;
            computedPropertyPtrCurPos++;
            return this;
        }

        /**
         * Creates {@link OsObjectSchemaInfo} object from this builder. After calling, this {@code Builder} becomes
         * invalid. All the property pointers will be freed.
         *
         * @return a newly created {@link OsObjectSchemaInfo}.
         */
        public OsObjectSchemaInfo build() {
            if (persistedPropertyPtrCurPos == -1 || computedPropertyPtrCurPos == -1) {
                throw new IllegalStateException("'OsObjectSchemaInfo.build()' has been called before on this object.");
            }
            OsObjectSchemaInfo info = new OsObjectSchemaInfo(className);
            nativeAddProperties(info.nativePtr, persistedPropertyPtrArray, computedPropertyPtrArray);
            persistedPropertyPtrCurPos = -1;
            computedPropertyPtrCurPos = -1;
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
     * Creates a java wrapper class for given {@code ObjectSchema} pointer. This java wrapper will take the ownership of
     * the object's memory and release it through phantom reference.
     *
     * @param nativePtr pointer to the {@code ObjectSchema} object.
     */
    OsObjectSchemaInfo(long nativePtr) {
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
     * Returns a property by the given name.
     *
     * @param propertyName the name of the property.
     * @return a {@link Property} object.
     */
    public Property getProperty(String propertyName) {
        return new Property(nativeGetProperty(nativePtr, propertyName));
    }

    /**
     * Returns the primary key property for this {@code ObjectSchema}.
     *
     * @return a {@link Property} object of the primary key property, {@code null} if this {@code ObjectSchema} doesn't
     * contains a primary key.
     */
    public @Nullable Property getPrimaryKeyProperty() {
        long propertyPtr = nativeGetPrimaryKeyProperty(nativePtr);
        return propertyPtr == 0 ? null : new Property(nativeGetPrimaryKeyProperty(nativePtr));
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

    // Add properties to the ObjectSchema and delete property pointers.
    private static native void nativeAddProperties(long nativePtr, long[] persistedPropPtrs, long[] computedPropPtrs);

    private static native String nativeGetClassName(long nativePtr);

    // Throw ISE if the property doesn't exist.
    private static native long nativeGetProperty(long nativePtr, String propertyName);

    // Return nullptr if it doesn't have a primary key.
    private static native long nativeGetPrimaryKeyProperty(long nativePtr);
}
