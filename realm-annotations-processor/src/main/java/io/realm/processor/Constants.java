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

package io.realm.processor;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String REALM_PACKAGE_NAME = "io.realm";
    public static final String PROXY_SUFFIX = "RealmProxy";
    public static final String TABLE_PREFIX = "class_";
    public static final String DEFAULT_MODULE_CLASS_NAME = "DefaultRealmModule";
    static final String STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE =
            "throw new IllegalArgumentException(\"Trying to set a non-nullable field %s to null.\")";

    static final Map<String, String> JAVA_TO_REALM_TYPES;
    static {
        JAVA_TO_REALM_TYPES = new HashMap<String, String>();
        JAVA_TO_REALM_TYPES.put("byte", "Long");
        JAVA_TO_REALM_TYPES.put("short", "Long");
        JAVA_TO_REALM_TYPES.put("int", "Long");
        JAVA_TO_REALM_TYPES.put("long", "Long");
        JAVA_TO_REALM_TYPES.put("float", "Float");
        JAVA_TO_REALM_TYPES.put("double", "Double");
        JAVA_TO_REALM_TYPES.put("boolean", "Boolean");
        JAVA_TO_REALM_TYPES.put("java.lang.Byte", "Long");
        JAVA_TO_REALM_TYPES.put("java.lang.Short", "Long");
        JAVA_TO_REALM_TYPES.put("java.lang.Integer", "Long");
        JAVA_TO_REALM_TYPES.put("java.lang.Long", "Long");
        JAVA_TO_REALM_TYPES.put("java.lang.Float", "Float");
        JAVA_TO_REALM_TYPES.put("java.lang.Double", "Double");
        JAVA_TO_REALM_TYPES.put("java.lang.Boolean", "Boolean");
        JAVA_TO_REALM_TYPES.put("java.lang.String", "String");
        JAVA_TO_REALM_TYPES.put("java.util.Date", "Date");
        JAVA_TO_REALM_TYPES.put("byte[]", "BinaryByteArray");
        // TODO: add support for char and Char
    }

    // Types in this array are guarded by if != null and use default value if trying to insert null
    static final Map<String, String> NULLABLE_JAVA_TYPES;
    static {
        NULLABLE_JAVA_TYPES = new HashMap<String, String>();

        NULLABLE_JAVA_TYPES.put("java.lang.Byte", "0");
        NULLABLE_JAVA_TYPES.put("java.lang.Short", "0");
        NULLABLE_JAVA_TYPES.put("java.lang.Integer", "0");
        NULLABLE_JAVA_TYPES.put("java.lang.Long", "0");
        NULLABLE_JAVA_TYPES.put("java.lang.Float", "0");
        NULLABLE_JAVA_TYPES.put("java.lang.Double", "0");
        NULLABLE_JAVA_TYPES.put("java.lang.Boolean", "false");
        NULLABLE_JAVA_TYPES.put("java.lang.String", "\"\"");
        NULLABLE_JAVA_TYPES.put("java.util.Date", "new Date(0)");
        NULLABLE_JAVA_TYPES.put("byte[]", "new byte[0]");
    }

    static final Map<String, String> JAVA_TO_COLUMN_TYPES;
    static {
        JAVA_TO_COLUMN_TYPES = new HashMap<String, String>();
        JAVA_TO_COLUMN_TYPES.put("byte", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("short", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("int", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("long", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("float", "ColumnType.FLOAT");
        JAVA_TO_COLUMN_TYPES.put("double", "ColumnType.DOUBLE");
        JAVA_TO_COLUMN_TYPES.put("boolean", "ColumnType.BOOLEAN");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Byte", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Short", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Integer", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Long", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Float", "ColumnType.FLOAT");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Double", "ColumnType.DOUBLE");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Boolean", "ColumnType.BOOLEAN");
        JAVA_TO_COLUMN_TYPES.put("java.lang.String", "ColumnType.STRING");
        JAVA_TO_COLUMN_TYPES.put("java.util.Date", "ColumnType.DATE");
        JAVA_TO_COLUMN_TYPES.put("byte[]", "ColumnType.BINARY");
    }

    static final Map<String, String> CASTING_TYPES;
    static {
        CASTING_TYPES = new HashMap<String, String>();
        CASTING_TYPES.put("byte", "long");
        CASTING_TYPES.put("short", "long");
        CASTING_TYPES.put("int", "long");
        CASTING_TYPES.put("long", "long");
        CASTING_TYPES.put("float", "float");
        CASTING_TYPES.put("double", "double");
        CASTING_TYPES.put("boolean", "boolean");
        CASTING_TYPES.put("java.lang.Byte", "long");
        CASTING_TYPES.put("java.lang.Short", "long");
        CASTING_TYPES.put("java.lang.Integer", "long");
        CASTING_TYPES.put("java.lang.Long", "long");
        CASTING_TYPES.put("java.lang.Float", "float");
        CASTING_TYPES.put("java.lang.Double", "double");
        CASTING_TYPES.put("java.lang.Boolean", "Boolean");
        CASTING_TYPES.put("java.lang.String", "String");
        CASTING_TYPES.put("java.util.Date", "Date");
        CASTING_TYPES.put("byte[]", "byte[]");
    }
}
