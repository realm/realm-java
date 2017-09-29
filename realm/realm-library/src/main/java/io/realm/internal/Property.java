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

package io.realm.internal;


import java.util.Locale;

import io.realm.RealmFieldType;


/**
 * Class for handling properties/fields.
 */

public class Property implements NativeObject {
    public static final boolean PRIMARY_KEY = true;
    public static final boolean REQUIRED = true;
    public static final boolean INDEXED = true;

    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_INT = 0;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_BOOL = 1;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_STRING = 2;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_DATA = 3;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_DATE = 4;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_FLOAT = 5;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_DOUBLE = 6;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_OBJECT = 7;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_LINKING_OBJECTS = 8;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_REQUIRED = 0;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_NULLABLE = 64;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_ARRAY = 128;

    private long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    Property(String name, RealmFieldType type, boolean isPrimary, boolean isIndexed, boolean isRequired) {
        this(nativeCreatePersistedProperty(name, convertFromRealmFieldType(type, isRequired), isPrimary, isIndexed));
    }

    Property(String name, RealmFieldType type, String linkedClassName) {
        // Ignore the isRequired when creating the linking property.
        this(nativeCreatePersistedLinkProperty(name, convertFromRealmFieldType(type, false), linkedClassName));
    }

    Property(String name, String sourceClassName, String sourceFieldName) {
        this(nativeCreateComputedLinkProperty(name, sourceClassName, sourceFieldName));
    }

    Property(long nativePtr) {
        this.nativePtr = nativePtr;
        NativeContext.dummyContext.addReference(this);
    }

    private static int convertFromRealmFieldType(RealmFieldType fieldType, boolean isRequired) {
        int type;
        switch (fieldType) {
            case OBJECT:
                type = TYPE_OBJECT | TYPE_NULLABLE;
                return type;
            case LIST:
                type = TYPE_OBJECT | TYPE_ARRAY;
                return type;
            case LINKING_OBJECTS:
                type = TYPE_LINKING_OBJECTS | TYPE_ARRAY;
                return type;
            case INTEGER:
                type = TYPE_INT;
                break;
            case BOOLEAN:
                type = TYPE_BOOL;
                break;
            case STRING:
                type = TYPE_STRING;
                break;
            case BINARY:
                type = TYPE_DATA;
                break;
            case DATE:
                type = TYPE_DATE;
                break;
            case FLOAT:
                type = TYPE_FLOAT;
                break;
            case DOUBLE:
                type = TYPE_DOUBLE;
                break;
            default:
                throw new IllegalArgumentException(
                        String.format(Locale.US, "Unsupported filed type: '%s'.", fieldType.name()));

        }
        int requiredFlag = isRequired ? TYPE_REQUIRED : TYPE_NULLABLE;
        return type | requiredFlag;
    }

    private static RealmFieldType convertToRealmFieldType(int propertyType) {
        // Clear the nullable flag
        switch (propertyType & ~TYPE_NULLABLE) {
            case  TYPE_OBJECT:
                return RealmFieldType.OBJECT;
            case TYPE_OBJECT | TYPE_ARRAY:
                return RealmFieldType.LIST;
            case TYPE_LINKING_OBJECTS | TYPE_ARRAY:
                return RealmFieldType.LINKING_OBJECTS;
            case TYPE_INT:
                return RealmFieldType.INTEGER;
            case TYPE_BOOL:
                return RealmFieldType.BOOLEAN;
            case TYPE_STRING:
                return RealmFieldType.STRING;
            case TYPE_DATA:
                return RealmFieldType.BINARY;
            case TYPE_DATE:
                return RealmFieldType.DATE;
            case TYPE_FLOAT:
                return RealmFieldType.FLOAT;
            case TYPE_DOUBLE:
                return RealmFieldType.DOUBLE;
            default:
                throw new IllegalArgumentException(
                        String.format(Locale.US, "Unsupported property type: '%d'", propertyType));

        }
    }

    public RealmFieldType getType() {
        return convertToRealmFieldType(nativeGetType(nativePtr));
    }

    public String getLinkedObjectName() {
        return nativeGetLinkedObjectName(nativePtr);
    }

    public long getColumnIndex() {
        return nativeGetColumnIndex(nativePtr);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    private static native long nativeGetFinalizerPtr();

    private static native long nativeCreatePersistedProperty(
            String name, int type, boolean isPrimary, boolean isIndexed);

    private static native long nativeCreatePersistedLinkProperty(String name, int type, String linkedToName);

    private static native long nativeCreateComputedLinkProperty(
            String name, String sourceClassName, String sourceFieldName);

    private static native int nativeGetType(long nativePtr);

    private static native long nativeGetColumnIndex(long nativePtr);

    // Return null if the property is not OBJECT, LIST or LINKING_OBJECT type.
    private static native String nativeGetLinkedObjectName(long nativePtr);
}
