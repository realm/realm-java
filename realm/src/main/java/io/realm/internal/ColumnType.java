/*
 * Copyright 2014 Realm Inc.
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

import java.nio.ByteBuffer;

// Make sure numbers match with <realm/column_type.hpp>
// FIXME: Add a unit test that verifies the correct correspondence.

public enum ColumnType {
    BOOLEAN(1),
    INTEGER(0),
    FLOAT(9),
    DOUBLE(10),
    STRING(2),
    BINARY(4),
    DATE(7),
    TABLE(5),
    MIXED(6),
    LINK(12),
    LINK_LIST(13);
    // When adding above, remember to update size of largest number below

    private final int nativeValue;

    // Note that if this array is too small, an
    // IndexOutOfBoundsException will be thrown during class loading.
    private static ColumnType[] byNativeValue = new ColumnType[14];

    static {
        ColumnType[] columnTypes = values();
        for (int i=0; i<columnTypes.length; ++i) {
            int v = columnTypes[i].nativeValue;
            byNativeValue[v] = columnTypes[i];
        }
    }

    private ColumnType(int nativeValue)
    {
        this.nativeValue = nativeValue;
    }

    public int getValue() {
        return nativeValue;
    }

    public boolean matchObject(Object obj) {
        switch (this.nativeValue) {
        case 0: return (obj instanceof Long || obj instanceof Integer || obj instanceof Short ||
                obj instanceof Byte);
        case 1: return (obj instanceof Boolean);
        case 2: return (obj instanceof String);
        case 4: return (obj instanceof byte[] || obj instanceof ByteBuffer);
        case 5: return (obj == null || obj instanceof Object[][]);
        case 6: return (obj instanceof Mixed ||
                obj instanceof Long || obj instanceof Integer ||
                obj instanceof Short || obj instanceof Byte || obj instanceof Boolean ||
                obj instanceof Float || obj instanceof Double ||
                obj instanceof String ||
                obj instanceof byte[] || obj instanceof ByteBuffer ||
                obj == null || obj instanceof Object[][] ||
                obj instanceof java.util.Date);
        case 7: return (obj instanceof java.util.Date);
        case 9: return (obj instanceof Float);
        case 10: return (obj instanceof Double);
        default: throw new RuntimeException("Invalid index in ColumnType.");
        }
    }

    static ColumnType fromNativeValue(int value)
    {
        if (0 <= value && value < byNativeValue.length) {
            ColumnType e = byNativeValue[value];
            if (e != null)
                return e;
        }
        throw new IllegalArgumentException("Bad native column type");
    }
}

