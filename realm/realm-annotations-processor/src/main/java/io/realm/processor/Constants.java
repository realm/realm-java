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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class Constants {
    public static final String REALM_PACKAGE_NAME = "io.realm";
    public static final String PROXY_SUFFIX = "RealmProxy";
    public static final String INTERFACE_SUFFIX = "RealmProxyInterface";
    public static final String INDENT = "    ";
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


    /**
     * Realm types and their corresponding Java types
     */
    public enum RealmFieldType {
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

        BACKLINK("LINKING_OBJECTS", null),

        INTEGER_LIST("INTEGER_LIST", "List"),
        BOOLEAN_LIST("BOOLEAN_LIST", "List"),
        STRING_LIST("STRING_LIST", "List"),
        BINARY_LIST("BINARY_LIST", "List"),
        DATE_LIST("DATE_LIST", "List"),
        FLOAT_LIST("FLOAT_LIST", "List"),
        DOUBLE_LIST("DOUBLE_LIST", "List");

        private final String realmType;
        private final String javaType;

        /**
         * @param realmType The simple name of the Enum type used in the Java bindings, to represent this type.
         * @param javaType The simple name of the Java type needed to store this Realm Type
         */
        RealmFieldType(String realmType, String javaType) {
            this.realmType = "RealmFieldType." + realmType;
            this.javaType = javaType;
        }

        /**
         * Get the name of the enum, used in the Java bindings, used to represent the corresponding type.
         * @return the name of the enum used to represent this Realm Type
         */
        public String getRealmType() {
            return realmType;
        }

        /**
         * Get the name of the Java type needed to store this Realm Type
         * @return the simple name for the corresponding Java type
         */
        public String getJavaType() {
            return javaType;
        }
    }


    static final Map<String, RealmFieldType> JAVA_TO_REALM_TYPES;

    static {
        Map<String, RealmFieldType> m = new HashMap<String, RealmFieldType>();
        m.put("byte", RealmFieldType.INTEGER);
        m.put("short", RealmFieldType.INTEGER);
        m.put("int", RealmFieldType.INTEGER);
        m.put("long", RealmFieldType.INTEGER);
        m.put("float", RealmFieldType.FLOAT);
        m.put("double", RealmFieldType.DOUBLE);
        m.put("boolean", RealmFieldType.BOOLEAN);
        m.put("java.lang.Byte", RealmFieldType.INTEGER);
        m.put("java.lang.Short", RealmFieldType.INTEGER);
        m.put("java.lang.Integer", RealmFieldType.INTEGER);
        m.put("java.lang.Long", RealmFieldType.INTEGER);
        m.put("java.lang.Float", RealmFieldType.FLOAT);
        m.put("java.lang.Double", RealmFieldType.DOUBLE);
        m.put("java.lang.Boolean", RealmFieldType.BOOLEAN);
        m.put("java.lang.String", RealmFieldType.STRING);
        m.put("java.util.Date", RealmFieldType.DATE);
        m.put("byte[]", RealmFieldType.BINARY);
        // TODO: add support for char and Char
        JAVA_TO_REALM_TYPES = Collections.unmodifiableMap(m);
    }


    static final Map<String, RealmFieldType> LIST_ELEMENT_TYPE_TO_REALM_TYPES;

    static {
        Map<String, RealmFieldType> m = new HashMap<String, RealmFieldType>();
        m.put("java.lang.Byte", RealmFieldType.INTEGER_LIST);
        m.put("java.lang.Short", RealmFieldType.INTEGER_LIST);
        m.put("java.lang.Integer", RealmFieldType.INTEGER_LIST);
        m.put("java.lang.Long", RealmFieldType.INTEGER_LIST);
        m.put("java.lang.Float", RealmFieldType.FLOAT_LIST);
        m.put("java.lang.Double", RealmFieldType.DOUBLE_LIST);
        m.put("java.lang.Boolean", RealmFieldType.BOOLEAN_LIST);
        m.put("java.lang.String", RealmFieldType.STRING_LIST);
        m.put("java.util.Date", RealmFieldType.DATE_LIST);
        m.put("byte[]", RealmFieldType.BINARY_LIST);
        LIST_ELEMENT_TYPE_TO_REALM_TYPES = Collections.unmodifiableMap(m);
    }
}
