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
    INTEGER(0),
    BOOLEAN(1),
    STRING(2),
    BINARY(4),
    UNSUPPORTED_TABLE(5),
    UNSUPPORTED_MIXED(6),
    UNSUPPORTED_DATE(7),
    DATE(8),
    FLOAT(9),
    DOUBLE(10),
    OBJECT(12),
    LIST(13),
    LINKING_OBJECTS(14);

    // Primitive array for fast mapping between between native values and their Realm type.
    private static final RealmFieldType[] typeList = new RealmFieldType[15];

    static {
        RealmFieldType[] columnTypes = values();
        for (int i = 0; i < columnTypes.length; i++) {
            typeList[columnTypes[i].nativeValue] = columnTypes[i];
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
            case 0:
                return (obj instanceof Long || obj instanceof Integer || obj instanceof Short || obj instanceof Byte);
            case 1:
                return (obj instanceof Boolean);
            case 2:
                return (obj instanceof String);
            case 4:
                return (obj instanceof byte[] || obj instanceof ByteBuffer);
            case 5:
                return (obj == null || obj instanceof Object[][]);
            case 7:
                return (obj instanceof java.util.Date); // The unused DateTime.
            case 8:
                return (obj instanceof java.util.Date);
            case 9:
                return (obj instanceof Float);
            case 10:
                return (obj instanceof Double);
            case 12:
                return false;
            case 13:
                return false;
            case 14:
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
        if (0 <= value && value < typeList.length) {
            RealmFieldType e = typeList[value];
            if (e != null) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid native Realm type: " + value);
    }
}

