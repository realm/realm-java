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

import static io.realm.RealmFieldType.BINARY_LIST;
import static io.realm.RealmFieldType.BINARY_SET;
import static io.realm.RealmFieldType.BOOLEAN_LIST;
import static io.realm.RealmFieldType.BOOLEAN_SET;
import static io.realm.RealmFieldType.DATE_LIST;
import static io.realm.RealmFieldType.DATE_SET;
import static io.realm.RealmFieldType.DECIMAL128_LIST;
import static io.realm.RealmFieldType.DECIMAL128_SET;
import static io.realm.RealmFieldType.DOUBLE_LIST;
import static io.realm.RealmFieldType.DOUBLE_SET;
import static io.realm.RealmFieldType.FLOAT_LIST;
import static io.realm.RealmFieldType.FLOAT_SET;
import static io.realm.RealmFieldType.INTEGER_LIST;
import static io.realm.RealmFieldType.INTEGER_SET;
import static io.realm.RealmFieldType.LINK_SET;
import static io.realm.RealmFieldType.MIXED_LIST;
import static io.realm.RealmFieldType.MIXED_SET;
import static io.realm.RealmFieldType.OBJECT_ID_LIST;
import static io.realm.RealmFieldType.OBJECT_ID_SET;
import static io.realm.RealmFieldType.STRING_LIST;
import static io.realm.RealmFieldType.STRING_SET;
import static io.realm.RealmFieldType.STRING_TO_BINARY_MAP;
import static io.realm.RealmFieldType.STRING_TO_BOOLEAN_MAP;
import static io.realm.RealmFieldType.STRING_TO_DATE_MAP;
import static io.realm.RealmFieldType.STRING_TO_DECIMAL128_MAP;
import static io.realm.RealmFieldType.STRING_TO_DOUBLE_MAP;
import static io.realm.RealmFieldType.STRING_TO_FLOAT_MAP;
import static io.realm.RealmFieldType.STRING_TO_INTEGER_MAP;
import static io.realm.RealmFieldType.STRING_TO_LINK_MAP;
import static io.realm.RealmFieldType.STRING_TO_MIXED_MAP;
import static io.realm.RealmFieldType.STRING_TO_OBJECT_ID_MAP;
import static io.realm.RealmFieldType.STRING_TO_STRING_MAP;
import static io.realm.RealmFieldType.STRING_TO_UUID_MAP;
import static io.realm.RealmFieldType.UUID_LIST;
import static io.realm.RealmFieldType.UUID_SET;


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
    public static final int TYPE_DECIMAL128 = 11;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_OBJECT_ID = 10;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_UUID = 12;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_MIXED = 9;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_REQUIRED = 0;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_NULLABLE = 64;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_ARRAY = 128;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_SET = 256;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_DICTIONARY = 512;

    private long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    Property(long nativePtr) {
        this.nativePtr = nativePtr;
        NativeContext.dummyContext.addReference(this);
    }

    static int convertFromRealmFieldType(RealmFieldType fieldType, boolean isRequired) {
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
            case DECIMAL128:
                type = TYPE_DECIMAL128;
                break;
            case OBJECT_ID:
                type = TYPE_OBJECT_ID;
                break;
            case UUID:
                type = TYPE_UUID;
                break;
            case MIXED:
                type = TYPE_MIXED;
                break;
            case DOUBLE:
                type = TYPE_DOUBLE;
                break;
            case INTEGER_LIST:
                //noinspection PointlessBitwiseExpression
                type = TYPE_INT | TYPE_ARRAY;
                break;
            case BOOLEAN_LIST:
                type = TYPE_BOOL | TYPE_ARRAY;
                break;
            case STRING_LIST:
                type = TYPE_STRING | TYPE_ARRAY;
                break;
            case BINARY_LIST:
                type = TYPE_DATA | TYPE_ARRAY;
                break;
            case DATE_LIST:
                type = TYPE_DATE | TYPE_ARRAY;
                break;
            case FLOAT_LIST:
                type = TYPE_FLOAT | TYPE_ARRAY;
                break;
            case DECIMAL128_LIST:
                type = TYPE_DECIMAL128 | TYPE_ARRAY;
                break;
            case OBJECT_ID_LIST:
                type = TYPE_OBJECT_ID | TYPE_ARRAY;
                break;
            case UUID_LIST:
                type = TYPE_UUID | TYPE_ARRAY;
                break;
            case DOUBLE_LIST:
                type = TYPE_DOUBLE | TYPE_ARRAY;
                break;
            case MIXED_LIST:
                type = TYPE_MIXED | TYPE_ARRAY;
                break;
            case STRING_TO_MIXED_MAP:
                type = TYPE_MIXED | TYPE_DICTIONARY;
                break;
            case STRING_TO_BOOLEAN_MAP:
                type = TYPE_BOOL | TYPE_DICTIONARY;
                break;
            case STRING_TO_STRING_MAP:
                type = TYPE_STRING | TYPE_DICTIONARY;
                break;
            case STRING_TO_INTEGER_MAP:
                type = TYPE_INT | TYPE_DICTIONARY;
                break;
            case STRING_TO_FLOAT_MAP:
                type = TYPE_FLOAT | TYPE_DICTIONARY;
                break;
            case STRING_TO_DOUBLE_MAP:
                type = TYPE_DOUBLE | TYPE_DICTIONARY;
                break;
            case STRING_TO_BINARY_MAP:
                type = TYPE_DATA | TYPE_DICTIONARY;
                break;
            case STRING_TO_DATE_MAP:
                type = TYPE_DATE | TYPE_DICTIONARY;
                break;
            case STRING_TO_DECIMAL128_MAP:
                type = TYPE_DECIMAL128 | TYPE_DICTIONARY;
                break;
            case STRING_TO_OBJECT_ID_MAP:
                type = TYPE_OBJECT_ID | TYPE_DICTIONARY;
                break;
            case STRING_TO_UUID_MAP:
                type = TYPE_UUID | TYPE_DICTIONARY;
                break;
            case STRING_TO_LINK_MAP:
                type = TYPE_OBJECT | TYPE_DICTIONARY;
                break;
            case BOOLEAN_SET:
                type = TYPE_BOOL | TYPE_SET;
                break;
            case STRING_SET:
                type = TYPE_STRING | TYPE_SET;
                break;
            case INTEGER_SET:
                type = TYPE_INT | TYPE_SET;
                break;
            case FLOAT_SET:
                type = TYPE_FLOAT | TYPE_SET;
                break;
            case DOUBLE_SET:
                type = TYPE_DOUBLE | TYPE_SET;
                break;
            case BINARY_SET:
                type = TYPE_DATA | TYPE_SET;
                break;
            case DATE_SET:
                type = TYPE_DATE | TYPE_SET;
                break;
            case DECIMAL128_SET:
                type = TYPE_DECIMAL128 | TYPE_SET;
                break;
            case OBJECT_ID_SET:
                type = TYPE_OBJECT_ID | TYPE_SET;
                break;
            case UUID_SET:
                type = TYPE_UUID | TYPE_SET;
                break;
            case LINK_SET:
                type = TYPE_OBJECT | TYPE_SET;
                return type;
            case MIXED_SET:
                type = TYPE_MIXED | TYPE_SET;
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
            case TYPE_DECIMAL128:
                return RealmFieldType.DECIMAL128;
            case TYPE_OBJECT_ID:
                return RealmFieldType.OBJECT_ID;
            case TYPE_UUID:
                return RealmFieldType.UUID;
            case TYPE_MIXED:
                return RealmFieldType.MIXED;
            //noinspection PointlessBitwiseExpression
            case TYPE_INT | TYPE_ARRAY:
                return INTEGER_LIST;
            case TYPE_BOOL | TYPE_ARRAY:
                return BOOLEAN_LIST;
            case TYPE_STRING | TYPE_ARRAY:
                return STRING_LIST;
            case TYPE_DATA | TYPE_ARRAY:
                return BINARY_LIST;
            case TYPE_DATE | TYPE_ARRAY:
                return DATE_LIST;
            case TYPE_FLOAT | TYPE_ARRAY:
                return FLOAT_LIST;
            case TYPE_DOUBLE | TYPE_ARRAY:
                return DOUBLE_LIST;
            case TYPE_DECIMAL128 | TYPE_ARRAY:
                return DECIMAL128_LIST;
            case TYPE_OBJECT_ID | TYPE_ARRAY:
                return OBJECT_ID_LIST;
            case TYPE_UUID | TYPE_ARRAY:
                return UUID_LIST;
            case TYPE_MIXED | TYPE_ARRAY:
                return MIXED_LIST;
            case TYPE_MIXED | TYPE_DICTIONARY:
                return STRING_TO_MIXED_MAP;
            case TYPE_BOOL | TYPE_DICTIONARY:
                return STRING_TO_BOOLEAN_MAP;
            case TYPE_STRING | TYPE_DICTIONARY:
                return STRING_TO_STRING_MAP;
            case TYPE_INT | TYPE_DICTIONARY:
                return STRING_TO_INTEGER_MAP;
            case TYPE_FLOAT | TYPE_DICTIONARY:
                return STRING_TO_FLOAT_MAP;
            case TYPE_DOUBLE | TYPE_DICTIONARY:
                return STRING_TO_DOUBLE_MAP;
            case TYPE_DATA | TYPE_DICTIONARY:
                return STRING_TO_BINARY_MAP;
            case TYPE_DATE | TYPE_DICTIONARY:
                return STRING_TO_DATE_MAP;
            case TYPE_DECIMAL128 | TYPE_DICTIONARY:
                return STRING_TO_DECIMAL128_MAP;
            case TYPE_OBJECT_ID | TYPE_DICTIONARY:
                return STRING_TO_OBJECT_ID_MAP;
            case TYPE_UUID | TYPE_DICTIONARY:
                return STRING_TO_UUID_MAP;
            case TYPE_OBJECT | TYPE_DICTIONARY:
                return STRING_TO_LINK_MAP;
            case TYPE_BOOL | TYPE_SET:
                return BOOLEAN_SET;
            case TYPE_STRING | TYPE_SET:
                return STRING_SET;
            case TYPE_INT | TYPE_SET:
                return INTEGER_SET;
            case TYPE_FLOAT | TYPE_SET:
                return FLOAT_SET;
            case TYPE_DOUBLE | TYPE_SET:
                return DOUBLE_SET;
            case TYPE_DATA | TYPE_SET:
                return BINARY_SET;
            case TYPE_DATE | TYPE_SET:
                return DATE_SET;
            case TYPE_DECIMAL128 | TYPE_SET:
                return DECIMAL128_SET;
            case TYPE_OBJECT_ID | TYPE_SET:
                return OBJECT_ID_SET;
            case TYPE_UUID | TYPE_SET:
                return UUID_SET;
            case TYPE_OBJECT | TYPE_SET:
                return LINK_SET;
            case TYPE_MIXED | TYPE_SET:
                return MIXED_SET;
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

    public long getColumnKey() {
        return nativeGetColumnKey(nativePtr);
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

    // nativeCreateXxxProperty will be called by OsObjectSchemaInfo directly to avoid creating temporary Property
    // objects.
    static native long nativeCreatePersistedProperty(String internalName,
                                                     String publicName,
                                                     int type,
                                                     boolean isPrimary,
                                                     boolean isIndexed);

    static native long nativeCreatePersistedLinkProperty(String internalName, String publicName, int type, String linkedToName);

    static native long nativeCreateComputedLinkProperty(String name, String sourceClassName, String sourceFieldName);

    private static native int nativeGetType(long nativePtr);

    private static native long nativeGetColumnKey(long nativePtr);

    // Return null if the property is not OBJECT, LIST or LINKING_OBJECT type.
    private static native String nativeGetLinkedObjectName(long nativePtr);
}
