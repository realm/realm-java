/*
 * Copyright 2018 Realm Inc.
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

import javax.lang.model.element.VariableElement;

/**
 * Helper class for creating the correct method calls to the OsObjectBuilder class.
 */
public class OsObjectBuilderTypeHelper {

    private static final Map<String, String> QUALIFIED_TYPE_TO_BUILDER;
    private static final Map<String, String> QUALIFIED_LIST_TYPE_TO_BUILDER;

    static {
        // Map of qualified types to their OsObjectBuilder Type
        Map<String, String> fieldTypes = new HashMap<>();
        fieldTypes.put("byte", "Integer");
        fieldTypes.put("short", "Integer");
        fieldTypes.put("int", "Integer");
        fieldTypes.put("long", "Integer");
        fieldTypes.put("float", "Float");
        fieldTypes.put("double", "Double");
        fieldTypes.put("boolean", "Boolean");
        fieldTypes.put("byte[]", "ByteArray");
        fieldTypes.put("java.lang.Byte", "Integer");
        fieldTypes.put("java.lang.Short", "Integer");
        fieldTypes.put("java.lang.Integer", "Integer");
        fieldTypes.put("java.lang.Long", "Integer");
        fieldTypes.put("java.lang.Float", "Float");
        fieldTypes.put("java.lang.Double", "Double");
        fieldTypes.put("java.lang.Boolean", "Boolean");
        fieldTypes.put("java.lang.String", "String");
        fieldTypes.put("java.util.Date", "Date");
        fieldTypes.put("io.realm.MutableRealmInteger", "MutableRealmInteger");
        QUALIFIED_TYPE_TO_BUILDER = Collections.unmodifiableMap(fieldTypes);

        // Map of qualified types to their OsObjectBuilder Type
        Map<String, String> listTypes = new HashMap<>();
        listTypes.put("byte[]", "ByteArrayList");
        listTypes.put("java.lang.Byte", "ByteList");
        listTypes.put("java.lang.Short", "ShortList");
        listTypes.put("java.lang.Integer", "IntegerList");
        listTypes.put("java.lang.Long", "LongList");
        listTypes.put("java.lang.Float", "FloatList");
        listTypes.put("java.lang.Double", "DoubleList");
        listTypes.put("java.lang.Boolean", "BooleanList");
        listTypes.put("java.lang.String", "StringList");
        listTypes.put("java.util.Date", "DateList");
        listTypes.put("io.realm.MutableRealmInteger", "MutableRealmIntegerList");
        QUALIFIED_LIST_TYPE_TO_BUILDER = Collections.unmodifiableMap(listTypes);
    }

    /**
     * Returns the method name used by the OsObjectBuilder for the given type, e.g. `addInteger`
     * or `addIntegerList`.
     */
    public static String getOsObjectBuilderName(VariableElement field) {
        if (Utils.isRealmModel(field)) {
            return "addObject";
        } else if (Utils.isRealmModelList(field)) {
            return "addObjectList";
        } else if (Utils.isRealmValueList(field)) {
            return "add" + getListTypeName(Utils.getRealmListType(field));
        } else if (Utils.isRealmResults(field)) {
            throw new IllegalStateException("RealmResults are not supported by OsObjectBuilder: " + field);
        } else {
            return "add" + getBasicTypeName(Utils.getFieldTypeQualifiedName(field));
        }
    }

    private static String getBasicTypeName(String qualifiedType) {
        String type = QUALIFIED_TYPE_TO_BUILDER.get(qualifiedType);
        if (type != null) {
            return type;
        }
        throw new IllegalArgumentException("Unsupported type: " + qualifiedType);
    }

    private static String getListTypeName(String qualifiedType) {
        String type = QUALIFIED_LIST_TYPE_TO_BUILDER.get(qualifiedType);
        if (type != null) {
            return type;
        }
        throw new IllegalArgumentException("Unsupported list type: " + qualifiedType);
    }

}
