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

import java.util.Collections
import java.util.HashMap

import javax.lang.model.element.VariableElement

/**
 * Helper class for creating the correct method calls to the OsObjectBuilder class.
 */
object OsObjectBuilderTypeHelper {

    private val QUALIFIED_TYPE_TO_BUILDER: Map<QualifiedClassName, String>
    private val QUALIFIED_LIST_TYPE_TO_BUILDER: Map<QualifiedClassName, String>

    init {
        // Map of qualified types to their OsObjectBuilder Type
        val fieldTypes = HashMap<QualifiedClassName, String>()
        fieldTypes[QualifiedClassName("byte")] = "Integer"
        fieldTypes[QualifiedClassName("short")] = "Integer"
        fieldTypes[QualifiedClassName("int")] = "Integer"
        fieldTypes[QualifiedClassName("long")] = "Integer"
        fieldTypes[QualifiedClassName("float")] = "Float"
        fieldTypes[QualifiedClassName("double")] = "Double"
        fieldTypes[QualifiedClassName("boolean")] = "Boolean"
        fieldTypes[QualifiedClassName("byte[]")] = "ByteArray"
        fieldTypes[QualifiedClassName("java.lang.Byte")] = "Integer"
        fieldTypes[QualifiedClassName("java.lang.Short")] = "Integer"
        fieldTypes[QualifiedClassName("java.lang.Integer")] = "Integer"
        fieldTypes[QualifiedClassName("java.lang.Long")] = "Integer"
        fieldTypes[QualifiedClassName("java.lang.Float")] = "Float"
        fieldTypes[QualifiedClassName("java.lang.Double")] = "Double"
        fieldTypes[QualifiedClassName("java.lang.Boolean")] = "Boolean"
        fieldTypes[QualifiedClassName("java.lang.String")] = "String"
        fieldTypes[QualifiedClassName("java.util.Date")] = "Date"
        fieldTypes[QualifiedClassName("io.realm.MutableRealmInteger")] = "MutableRealmInteger"
        QUALIFIED_TYPE_TO_BUILDER = Collections.unmodifiableMap(fieldTypes)

        // Map of qualified types to their OsObjectBuilder Type
        val listTypes = HashMap<QualifiedClassName, String>()
        listTypes[QualifiedClassName("byte[]")] = "ByteArrayList"
        listTypes[QualifiedClassName("java.lang.Byte")] = "ByteList"
        listTypes[QualifiedClassName("java.lang.Short")] = "ShortList"
        listTypes[QualifiedClassName("java.lang.Integer")] = "IntegerList"
        listTypes[QualifiedClassName("java.lang.Long")] = "LongList"
        listTypes[QualifiedClassName("java.lang.Float")] = "FloatList"
        listTypes[QualifiedClassName("java.lang.Double")] = "DoubleList"
        listTypes[QualifiedClassName("java.lang.Boolean")] = "BooleanList"
        listTypes[QualifiedClassName("java.lang.String")] = "StringList"
        listTypes[QualifiedClassName("java.util.Date")] = "DateList"
        listTypes[QualifiedClassName("io.realm.MutableRealmInteger")] = "MutableRealmIntegerList"
        QUALIFIED_LIST_TYPE_TO_BUILDER = Collections.unmodifiableMap(listTypes)
    }

    /**
     * Returns the method name used by the OsObjectBuilder for the given type, e.g. `addInteger`
     * or `addIntegerList`.
     */
    fun getOsObjectBuilderName(field: VariableElement): String {
        return if (Utils.isRealmModel(field)) {
            "addObject"
        } else if (Utils.isRealmModelList(field)) {
            "addObjectList"
        } else if (Utils.isRealmValueList(field)) {
            "add" + getListTypeName(Utils.getRealmListType(field))
        } else if (Utils.isRealmResults(field)) {
            throw IllegalStateException("RealmResults are not supported by OsObjectBuilder: $field")
        } else {
            "add" + getBasicTypeName(Utils.getFieldTypeQualifiedName(field))
        }
    }

    private fun getBasicTypeName(qualifiedType: QualifiedClassName): String {
        val type = QUALIFIED_TYPE_TO_BUILDER[qualifiedType]
        if (type != null) {
            return type
        }
        throw IllegalArgumentException("Unsupported type: $qualifiedType")
    }

    private fun getListTypeName(typeName: QualifiedClassName?): String {
        val type = QUALIFIED_LIST_TYPE_TO_BUILDER[typeName]
        if (type != null) {
            return type
        }
        throw IllegalArgumentException("Unsupported list type: $type")
    }

}
