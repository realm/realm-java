/*
 * Copyright 2015 Realm Inc.
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

import java.nio.ByteBuffer;

import io.realm.internal.Keep;
import io.realm.internal.Property;

import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_BINARY;
import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_BOOLEAN;
import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_DATE;
import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_DOUBLE;
import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_FLOAT;
import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_INTEGER;
import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_LINKING_OBJECTS;
import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_LIST;
import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_OBJECT;
import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_STRING;
import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_UNSUPPORTED_DATE;
import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_UNSUPPORTED_MIXED;
import static io.realm.RealmFieldTypeConstants.CORE_TYPE_VALUE_UNSUPPORTED_TABLE;
import static io.realm.RealmFieldTypeConstants.LIST_OFFSET;
import static io.realm.RealmFieldTypeConstants.MAX_CORE_TYPE_VALUE;


interface RealmFieldTypeConstants {
    int LIST_OFFSET = Property.TYPE_ARRAY;

    int CORE_TYPE_VALUE_INTEGER = 0;
    int CORE_TYPE_VALUE_BOOLEAN = 1;
    int CORE_TYPE_VALUE_STRING = 2;
    int CORE_TYPE_VALUE_BINARY = 4;
    int CORE_TYPE_VALUE_UNSUPPORTED_TABLE = 5;
    int CORE_TYPE_VALUE_UNSUPPORTED_MIXED = 6;
    int CORE_TYPE_VALUE_UNSUPPORTED_DATE = 7;
    int CORE_TYPE_VALUE_DATE = 8;
    int CORE_TYPE_VALUE_FLOAT = 9;
    int CORE_TYPE_VALUE_DOUBLE = 10;
    int CORE_TYPE_VALUE_OBJECT = 12;
    int CORE_TYPE_VALUE_LIST = 13;
    int CORE_TYPE_VALUE_LINKING_OBJECTS = 14;

    int MAX_CORE_TYPE_VALUE = CORE_TYPE_VALUE_LINKING_OBJECTS;
}

/**
 * List of the types used by Realm's underlying storage engine.
 * <p>
 * Normally there is no reason to interact with the underlying Realm types as Realm will automatically
 * convert between normal Java types and the Realm types. However it is possible to access these
 * types through a {@link DynamicRealmObject}.
 */
@Keep
public enum RealmFieldType {
    // Makes sure numbers match with <realm/column_type.hpp>.
    INTEGER(CORE_TYPE_VALUE_INTEGER),
    BOOLEAN(CORE_TYPE_VALUE_BOOLEAN),
    STRING(CORE_TYPE_VALUE_STRING),
    BINARY(CORE_TYPE_VALUE_BINARY),
    DATE(CORE_TYPE_VALUE_DATE),
    FLOAT(CORE_TYPE_VALUE_FLOAT),
    DOUBLE(CORE_TYPE_VALUE_DOUBLE),
    OBJECT(CORE_TYPE_VALUE_OBJECT),

    LIST(CORE_TYPE_VALUE_LIST),
    LINKING_OBJECTS(CORE_TYPE_VALUE_LINKING_OBJECTS),

    INTEGER_LIST(CORE_TYPE_VALUE_INTEGER + LIST_OFFSET),
    BOOLEAN_LIST(CORE_TYPE_VALUE_BOOLEAN + LIST_OFFSET),
    STRING_LIST(CORE_TYPE_VALUE_STRING + LIST_OFFSET),
    BINARY_LIST(CORE_TYPE_VALUE_BINARY + LIST_OFFSET),
    DATE_LIST(CORE_TYPE_VALUE_DATE + LIST_OFFSET),
    FLOAT_LIST(CORE_TYPE_VALUE_FLOAT + LIST_OFFSET),
    DOUBLE_LIST(CORE_TYPE_VALUE_DOUBLE + LIST_OFFSET);

    // Primitive array for fast mapping between between native values and their Realm type.
    private static final RealmFieldType[] basicTypes = new RealmFieldType[MAX_CORE_TYPE_VALUE + 1];
    private static final RealmFieldType[] listTypes = new RealmFieldType[MAX_CORE_TYPE_VALUE + 1];

    static {
        for (RealmFieldType columnType : values()) {
            final int nativeValue = columnType.nativeValue;
            if (nativeValue < LIST_OFFSET) {
                basicTypes[nativeValue] = columnType;
            } else {
                listTypes[nativeValue - LIST_OFFSET] = columnType;
            }
        }
    }

    private final int nativeValue;

    RealmFieldType(int nativeValue) {
        this.nativeValue = nativeValue;
    }

    /**
     * Returns the native value representing this type.
     *
     * @return the value used by the underlying storage engine to represent this type.
     */
    public int getNativeValue() {
        return nativeValue;
    }

    /**
     * Checks if the given Java object can be converted to the underlying Realm type.
     *
     * @param obj object to test compatibility on.
     * @return {@code true} if object can be converted to the Realm type, {@code false} otherwise.
     */
    public boolean isValid(Object obj) {
        switch (nativeValue) {
            case CORE_TYPE_VALUE_INTEGER:
                return (obj instanceof Long || obj instanceof Integer || obj instanceof Short || obj instanceof Byte);
            case CORE_TYPE_VALUE_BOOLEAN:
                return (obj instanceof Boolean);
            case CORE_TYPE_VALUE_STRING:
                return (obj instanceof String);
            case CORE_TYPE_VALUE_BINARY:
                return (obj instanceof byte[] || obj instanceof ByteBuffer);
            case CORE_TYPE_VALUE_DATE:
                return (obj instanceof java.util.Date);
            case CORE_TYPE_VALUE_FLOAT:
                return (obj instanceof Float);
            case CORE_TYPE_VALUE_DOUBLE:
                return (obj instanceof Double);
            case CORE_TYPE_VALUE_OBJECT:
                return false;
            case CORE_TYPE_VALUE_LIST:
                return false;
            case CORE_TYPE_VALUE_LINKING_OBJECTS:
                return false;
            case CORE_TYPE_VALUE_INTEGER + LIST_OFFSET:
                return false;
            case CORE_TYPE_VALUE_BOOLEAN + LIST_OFFSET:
                return false;
            case CORE_TYPE_VALUE_STRING + LIST_OFFSET:
                return false;
            case CORE_TYPE_VALUE_BINARY + LIST_OFFSET:
                return false;
            case CORE_TYPE_VALUE_DATE + LIST_OFFSET:
                return false;
            case CORE_TYPE_VALUE_FLOAT + LIST_OFFSET:
                return false;
            case CORE_TYPE_VALUE_DOUBLE + LIST_OFFSET:
                return false;
            default:
                throw new RuntimeException("Unsupported Realm type:  " + this);
        }
    }

    /**
     * Converts the underlying value used by the storage engine to the proper Realm type.
     *
     * @param value the value to convert
     * @return the corresponding Realm type.
     * @throws IllegalArgumentException if value isn't valid.
     */
    public static RealmFieldType fromNativeValue(int value) {
        if (0 <= value && value < basicTypes.length) {
            RealmFieldType e = basicTypes[value];
            if (e != null) {
                return e;
            }
        }
        if (LIST_OFFSET <= value) {
            final int elementValue = value - LIST_OFFSET;
            if (elementValue < listTypes.length) {
                RealmFieldType e = listTypes[elementValue];
                if (e != null) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid native Realm type: " + value);
    }
}

