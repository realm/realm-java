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
    public static final String INTERFACE_SUFFIX = "RealmProxyInterface";
    public static final String INDENT = "    ";
    public static final String TABLE_PREFIX = "class_";
    public static final String DEFAULT_MODULE_CLASS_NAME = "DefaultRealmModule";
    static final String STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE =
            "throw new IllegalArgumentException(\"Trying to set non-nullable field '%s' to null.\")";
    static final String STATEMENT_EXCEPTION_NO_PRIMARY_KEY_IN_JSON =
            "throw new IllegalArgumentException(\"JSON object doesn't have the primary key field '%s'.\")";
    static final String STATEMENT_EXCEPTION_PRIMARY_KEY_CANNOT_BE_CHANGED =
            "throw new io.realm.exceptions.RealmException(\"Primary key field '%s' cannot be changed after object" +
                    " was created.\")";
    static final String STATEMENT_EXCEPTION_ILLEGAL_JSON_LOAD =
            "throw new io.realm.exceptions.RealmException(\"\\\"%s\\\" field \\\"%s\\\" cannot be loaded from json\")";

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

    static final Map<String, String> JAVA_TO_COLUMN_TYPES;

    static {
        JAVA_TO_COLUMN_TYPES = new HashMap<String, String>();
        JAVA_TO_COLUMN_TYPES.put("byte", "RealmFieldType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("short", "RealmFieldType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("int", "RealmFieldType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("long", "RealmFieldType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("float", "RealmFieldType.FLOAT");
        JAVA_TO_COLUMN_TYPES.put("double", "RealmFieldType.DOUBLE");
        JAVA_TO_COLUMN_TYPES.put("boolean", "RealmFieldType.BOOLEAN");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Byte", "RealmFieldType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Short", "RealmFieldType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Integer", "RealmFieldType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Long", "RealmFieldType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Float", "RealmFieldType.FLOAT");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Double", "RealmFieldType.DOUBLE");
        JAVA_TO_COLUMN_TYPES.put("java.lang.Boolean", "RealmFieldType.BOOLEAN");
        JAVA_TO_COLUMN_TYPES.put("java.lang.String", "RealmFieldType.STRING");
        JAVA_TO_COLUMN_TYPES.put("java.util.Date", "RealmFieldType.DATE");
        JAVA_TO_COLUMN_TYPES.put("byte[]", "RealmFieldType.BINARY");
    }

    static final Map<String, String> JAVA_TO_FIELD_SETTER;

    static {
        JAVA_TO_FIELD_SETTER = new HashMap<String, String>();
        JAVA_TO_FIELD_SETTER.put("byte", "setByte");
        JAVA_TO_FIELD_SETTER.put("short", "setShort");
        JAVA_TO_FIELD_SETTER.put("int", "setInt");
        JAVA_TO_FIELD_SETTER.put("long", "setLong");
        JAVA_TO_FIELD_SETTER.put("float", "setFloat");
        JAVA_TO_FIELD_SETTER.put("double", "setDouble");
        JAVA_TO_FIELD_SETTER.put("boolean", "setBoolean");
        JAVA_TO_FIELD_SETTER.put("java.lang.Byte", "set");
        JAVA_TO_FIELD_SETTER.put("java.lang.Short", "set");
        JAVA_TO_FIELD_SETTER.put("java.lang.Integer", "set");
        JAVA_TO_FIELD_SETTER.put("java.lang.Long", "set");
        JAVA_TO_FIELD_SETTER.put("java.lang.Float", "set");
        JAVA_TO_FIELD_SETTER.put("java.lang.Double", "set");
        JAVA_TO_FIELD_SETTER.put("java.lang.Boolean", "set");
        JAVA_TO_FIELD_SETTER.put("java.lang.String", "set");
        JAVA_TO_FIELD_SETTER.put("java.util.Date", "set");
        JAVA_TO_FIELD_SETTER.put("byte[]", "set");
    }
}
