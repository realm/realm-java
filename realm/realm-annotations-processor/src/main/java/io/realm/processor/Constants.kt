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
    val JAVA_TO_REALM_TYPES = hashMapOf<String, RealmFieldType>()
    val LIST_ELEMENT_TYPE_TO_REALM_TYPES = hashMapOf<String, RealmFieldType>()

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
        OBJECT("OBJECT", "Object"),
        LIST("LIST", "List"),
        DECIMAL128("DECIMAL128", "Decimal128"),
        OBJECT_ID("OBJECT_ID", "ObjectId"),

        BACKLINK("LINKING_OBJECTS", null),

        INTEGER_LIST("INTEGER_LIST", "List"),
        BOOLEAN_LIST("BOOLEAN_LIST", "List"),
        STRING_LIST("STRING_LIST", "List"),
        BINARY_LIST("BINARY_LIST", "List"),
        DATE_LIST("DATE_LIST", "List"),
        FLOAT_LIST("FLOAT_LIST", "List"),
        DOUBLE_LIST("DOUBLE_LIST", "List"),
        DECIMAL128_LIST("DECIMAL128_LIST", "List"),
        OBJECT_ID_LIST("OBJECT_ID_LIST", "List");

        /**
         * The name of the enum, used in the Java bindings, used to represent the corresponding type.
         */
        val realmType: String = "RealmFieldType.$realmType"
    }

    init {
        JAVA_TO_REALM_TYPES.apply {
            this["byte"] = RealmFieldType.INTEGER
            this["short"] = RealmFieldType.INTEGER
            this["int"] = RealmFieldType.INTEGER
            this["long"] = RealmFieldType.INTEGER
            this["float"] = RealmFieldType.FLOAT
            this["double"] = RealmFieldType.DOUBLE
            this["boolean"] = RealmFieldType.BOOLEAN
            this["java.lang.Byte"] = RealmFieldType.INTEGER
            this["java.lang.Short"] = RealmFieldType.INTEGER
            this["java.lang.Integer"] = RealmFieldType.INTEGER
            this["java.lang.Long"] = RealmFieldType.INTEGER
            this["java.lang.Float"] = RealmFieldType.FLOAT
            this["java.lang.Double"] = RealmFieldType.DOUBLE
            this["java.lang.Boolean"] = RealmFieldType.BOOLEAN
            this["java.lang.String"] = RealmFieldType.STRING
            this["java.util.Date"] = RealmFieldType.DATE
            this["byte[]"] = RealmFieldType.BINARY
            this["org.bson.types.Decimal128"] = RealmFieldType.DECIMAL128
            this["org.bson.types.ObjectId"] = RealmFieldType.OBJECT_ID
            // TODO: add support for char and Char
        }

        LIST_ELEMENT_TYPE_TO_REALM_TYPES.apply {
            this["java.lang.Byte"] = RealmFieldType.INTEGER_LIST
            this["java.lang.Short"] = RealmFieldType.INTEGER_LIST
            this["java.lang.Integer"] = RealmFieldType.INTEGER_LIST
            this["java.lang.Long"] = RealmFieldType.INTEGER_LIST
            this["java.lang.Float"] = RealmFieldType.FLOAT_LIST
            this["java.lang.Double"] = RealmFieldType.DOUBLE_LIST
            this["java.lang.Boolean"] = RealmFieldType.BOOLEAN_LIST
            this["java.lang.String"] = RealmFieldType.STRING_LIST
            this["java.util.Date"] = RealmFieldType.DATE_LIST
            this["byte[]"] = RealmFieldType.BINARY_LIST
            this["org.bson.types.Decimal128"] = RealmFieldType.DECIMAL128_LIST
            this["org.bson.types.ObjectId"] = RealmFieldType.OBJECT_ID_LIST
        }
    }
}
