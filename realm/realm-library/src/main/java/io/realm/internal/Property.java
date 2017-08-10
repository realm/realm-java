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
        this.nativePtr = nativeCreateProperty(name, convertFromRealmFieldType(type, isRequired), isPrimary, isIndexed);
        NativeContext.dummyContext.addReference(this);
    }

    Property(String name, RealmFieldType type, String linkedClassName) {
        // Ignore the isRequired when creating the linking property.
        int propertyType = convertFromRealmFieldType(type, false);
        this.nativePtr = nativeCreateProperty(name, propertyType, linkedClassName);
        NativeContext.dummyContext.addReference(this);
    }

    protected Property(long nativePtr) {
        this.nativePtr = nativePtr;
    }

    private int convertFromRealmFieldType(RealmFieldType fieldType, boolean isRequired) {
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
                        String.format("Unsupported filed type: '%s'.", fieldType.name()));

        }
        int requiredFlag = isRequired ? TYPE_REQUIRED : TYPE_NULLABLE;
        return type | requiredFlag;
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    private static native long nativeCreateProperty(String name, int type, boolean isPrimary, boolean isIndexed);

    private static native long nativeCreateProperty(String name, int type, String linkedToName);

    private static native long nativeGetFinalizerPtr();
}
