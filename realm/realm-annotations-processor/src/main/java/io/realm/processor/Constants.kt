/*
 * Copyright 2019 Realm Inc.
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

package io.realm.processor

object Constants {

    const val REALM_PACKAGE_NAME = "io.realm"
    const val PROXY_SUFFIX = "RealmProxy"
    const val INTERFACE_SUFFIX = "RealmProxyInterface"
    const val INDENT = "    "
    const val DEFAULT_MODULE_CLASS_NAME = "DefaultRealmModule"
    const val STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE =
            "throw new IllegalArgumentException(\"Trying to set non-nullable field '%s' to null.\")"
    const val STATEMENT_EXCEPTION_NO_PRIMARY_KEY_IN_JSON =
            "throw new IllegalArgumentException(\"JSON object doesn't have the primary key field '%s'.\")"
    const val STATEMENT_EXCEPTION_PRIMARY_KEY_CANNOT_BE_CHANGED =
            "throw new io.realm.exceptions.RealmException(\"Primary key field '%s' cannot be changed after object was created.\")"
    const val STATEMENT_EXCEPTION_ILLEGAL_JSON_LOAD
            = "throw new io.realm.exceptions.RealmException(\"\\\"%s\\\" field \\\"%s\\\" cannot be loaded from json\")"
    val JAVA_TO_REALM_TYPES = mapOf("byte" to RealmFieldType.INTEGER,
            "short" to RealmFieldType.INTEGER,
            "int" to RealmFieldType.INTEGER,
            "long" to RealmFieldType.INTEGER,
            "float" to RealmFieldType.FLOAT,
            "double" to RealmFieldType.DOUBLE,
            "boolean" to RealmFieldType.BOOLEAN,
            "java.lang.Byte" to RealmFieldType.INTEGER,
            "java.lang.Short" to RealmFieldType.INTEGER,
            "java.lang.Integer" to RealmFieldType.INTEGER,
            "java.lang.Long" to RealmFieldType.INTEGER,
            "java.lang.Float" to RealmFieldType.FLOAT,
            "java.lang.Double" to RealmFieldType.DOUBLE,
            "java.lang.Boolean" to RealmFieldType.BOOLEAN,
            "java.lang.String" to RealmFieldType.STRING,
            "java.util.Date" to RealmFieldType.DATE,
            "byte[]" to RealmFieldType.BINARY,
            "org.bson.types.Decimal128" to RealmFieldType.DECIMAL128,
            "org.bson.types.ObjectId" to RealmFieldType.OBJECT_ID,
            "java.util.UUID" to RealmFieldType.UUID
    )

    val LIST_ELEMENT_TYPE_TO_REALM_TYPES = mapOf(
            "java.lang.Byte" to RealmFieldType.INTEGER_LIST,
            "java.lang.Short" to RealmFieldType.INTEGER_LIST,
            "java.lang.Integer" to RealmFieldType.INTEGER_LIST,
            "java.lang.Long" to RealmFieldType.INTEGER_LIST,
            "java.lang.Float" to RealmFieldType.FLOAT_LIST,
            "java.lang.Double" to RealmFieldType.DOUBLE_LIST,
            "java.lang.Boolean" to RealmFieldType.BOOLEAN_LIST,
            "java.lang.String" to RealmFieldType.STRING_LIST,
            "java.util.Date" to RealmFieldType.DATE_LIST,
            "byte[]" to RealmFieldType.BINARY_LIST,
            "org.bson.types.Decimal128" to RealmFieldType.DECIMAL128_LIST,
            "org.bson.types.ObjectId" to RealmFieldType.OBJECT_ID_LIST,
            "java.util.UUID" to RealmFieldType.UUID_LIST,
            "io.realm.RealmAny" to RealmFieldType.MIXED_LIST
    )

    val DICTIONARY_ELEMENT_TYPE_TO_REALM_TYPES = mapOf(
            "java.lang.Byte" to RealmFieldType.STRING_TO_INTEGER_MAP,
            "java.lang.Short" to RealmFieldType.STRING_TO_INTEGER_MAP,
            "java.lang.Integer" to RealmFieldType.STRING_TO_INTEGER_MAP,
            "java.lang.Long" to RealmFieldType.STRING_TO_INTEGER_MAP,
            "java.lang.Float" to RealmFieldType.STRING_TO_FLOAT_MAP,
            "java.lang.Double" to RealmFieldType.STRING_TO_DOUBLE_MAP,
            "java.lang.Boolean" to RealmFieldType.STRING_TO_BOOLEAN_MAP,
            "java.lang.String" to RealmFieldType.STRING_TO_STRING_MAP,
            "java.util.Date" to RealmFieldType.STRING_TO_DATE_MAP,
            "byte[]" to RealmFieldType.STRING_TO_BINARY_MAP,
            "org.bson.types.Decimal128" to RealmFieldType.STRING_TO_DECIMAL128_MAP,
            "org.bson.types.ObjectId" to RealmFieldType.STRING_TO_OBJECT_ID_MAP,
            "java.util.UUID" to RealmFieldType.STRING_TO_UUID_MAP,
            "io.realm.RealmAny" to RealmFieldType.STRING_TO_MIXED_MAP
    )

    val SET_ELEMENT_TYPE_TO_REALM_TYPES = mapOf(
            "java.lang.Byte" to RealmFieldType.INTEGER_SET,
            "java.lang.Short" to RealmFieldType.INTEGER_SET,
            "java.lang.Integer" to RealmFieldType.INTEGER_SET,
            "java.lang.Long" to RealmFieldType.INTEGER_SET,
            "java.lang.Float" to RealmFieldType.FLOAT_SET,
            "java.lang.Double" to RealmFieldType.DOUBLE_SET,
            "java.lang.Boolean" to RealmFieldType.BOOLEAN_SET,
            "java.lang.String" to RealmFieldType.STRING_SET,
            "java.util.Date" to RealmFieldType.DATE_SET,
            "byte[]" to RealmFieldType.BINARY_SET,
            "org.bson.types.Decimal128" to RealmFieldType.DECIMAL128_SET,
            "org.bson.types.ObjectId" to RealmFieldType.OBJECT_ID_SET,
            "java.util.UUID" to RealmFieldType.UUID_SET,
            "io.realm.RealmAny" to RealmFieldType.MIXED_SET
    )

    /**
     * Realm types and their corresponding Java types.
     *
     * @param realmType The simple name of the Enum type used in the Java bindings, to represent this type.
     * @param javaType The simple name of the Java type needed to store this Realm Type
     */
    enum class RealmFieldType(realmType: String?, val javaType: String?) {
        NOTYPE(null, "Void"),
        INTEGER("INTEGER", "Long"),
        FLOAT("FLOAT", "Float"),
        DOUBLE("DOUBLE", "Double"),
        BOOLEAN("BOOLEAN", "Boolean"),
        STRING("STRING", "String"),
        DATE("DATE", "Date"),
        BINARY("BINARY", "BinaryByteArray"),
        REALM_INTEGER("INTEGER", "Long"),
        MIXED("MIXED", "RealmAny"),
        OBJECT("OBJECT", "Object"),
        LIST("LIST", "List"),
        DECIMAL128("DECIMAL128", "Decimal128"),
        OBJECT_ID("OBJECT_ID", "ObjectId"),
        UUID("UUID", "UUID"),

        BACKLINK("LINKING_OBJECTS", null),

        INTEGER_LIST("INTEGER_LIST", "List"),
        BOOLEAN_LIST("BOOLEAN_LIST", "List"),
        STRING_LIST("STRING_LIST", "List"),
        BINARY_LIST("BINARY_LIST", "List"),
        DATE_LIST("DATE_LIST", "List"),
        FLOAT_LIST("FLOAT_LIST", "List"),
        DOUBLE_LIST("DOUBLE_LIST", "List"),
        DECIMAL128_LIST("DECIMAL128_LIST", "List"),
        OBJECT_ID_LIST("OBJECT_ID_LIST", "List"),
        UUID_LIST("UUID_LIST", "List"),
        MIXED_LIST("MIXED_LIST", "List"),

        STRING_TO_LINK_MAP("STRING_TO_LINK_MAP", "Map"),
        STRING_TO_INTEGER_MAP("STRING_TO_INTEGER_MAP", "Map"),
        STRING_TO_BOOLEAN_MAP("STRING_TO_BOOLEAN_MAP", "Map"),
        STRING_TO_STRING_MAP("STRING_TO_STRING_MAP", "Map"),
        STRING_TO_BINARY_MAP("STRING_TO_BINARY_MAP", "Map"),
        STRING_TO_DATE_MAP("STRING_TO_DATE_MAP", "Map"),
        STRING_TO_FLOAT_MAP("STRING_TO_FLOAT_MAP", "Map"),
        STRING_TO_DOUBLE_MAP("STRING_TO_DOUBLE_MAP", "Map"),
        STRING_TO_DECIMAL128_MAP("STRING_TO_DECIMAL128_MAP", "Map"),
        STRING_TO_OBJECT_ID_MAP("STRING_TO_OBJECT_ID_MAP", "Map"),
        STRING_TO_UUID_MAP("STRING_TO_UUID_MAP", "Map"),
        STRING_TO_MIXED_MAP("STRING_TO_MIXED_MAP", "Map"),

        INTEGER_SET("INTEGER_SET", "Set"),
        BOOLEAN_SET("BOOLEAN_SET", "Set"),
        STRING_SET("STRING_SET", "Set"),
        BINARY_SET("BINARY_SET", "Set"),
        DATE_SET("DATE_SET", "Set"),
        FLOAT_SET("FLOAT_SET", "Set"),
        DOUBLE_SET("DOUBLE_SET", "Set"),
        DECIMAL128_SET("DECIMAL128_SET", "Set"),
        OBJECT_ID_SET("OBJECT_ID_SET", "Set"),
        UUID_SET("UUID_SET", "Set"),
        LINK_SET("LINK_SET", "Set"),
        MIXED_SET("MIXED_SET", "Set");

        /**
         * The name of the enum, used in the Java bindings, used to represent the corresponding type.
         */
        val realmType: String = "RealmFieldType.$realmType"
    }
}
