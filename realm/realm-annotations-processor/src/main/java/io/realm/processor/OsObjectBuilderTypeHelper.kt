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
package io.realm.processor

import java.util.Collections
import java.util.HashMap

import javax.lang.model.element.VariableElement

/**
 * Helper class for creating the correct method calls to the OsObjectBuilder class.
 */
object OsObjectBuilderTypeHelper {

    private val QUALIFIED_TYPE_TO_BUILDER: Map<String, String>
    private val QUALIFIED_LIST_TYPE_TO_BUILDER: Map<String, String>

    init {
        // Map of qualified types to their OsObjectBuilder Type
        val fieldTypes = HashMap<String, String>()
        fieldTypes["byte"] = "Integer"
        fieldTypes["short"] = "Integer"
        fieldTypes["int"] = "Integer"
        fieldTypes["long"] = "Integer"
        fieldTypes["float"] = "Float"
        fieldTypes["double"] = "Double"
        fieldTypes["boolean"] = "Boolean"
        fieldTypes["byte[]"] = "ByteArray"
        fieldTypes["java.lang.Byte"] = "Integer"
        fieldTypes["java.lang.Short"] = "Integer"
        fieldTypes["java.lang.Integer"] = "Integer"
        fieldTypes["java.lang.Long"] = "Integer"
        fieldTypes["java.lang.Float"] = "Float"
        fieldTypes["java.lang.Double"] = "Double"
        fieldTypes["java.lang.Boolean"] = "Boolean"
        fieldTypes["java.lang.String"] = "String"
        fieldTypes["java.util.Date"] = "Date"
        fieldTypes["io.realm.MutableRealmInteger"] = "MutableRealmInteger"
        QUALIFIED_TYPE_TO_BUILDER = Collections.unmodifiableMap(fieldTypes)

        // Map of qualified types to their OsObjectBuilder Type
        val listTypes = HashMap<String, String>()
        listTypes["byte[]"] = "ByteArrayList"
        listTypes["java.lang.Byte"] = "ByteList"
        listTypes["java.lang.Short"] = "ShortList"
        listTypes["java.lang.Integer"] = "IntegerList"
        listTypes["java.lang.Long"] = "LongList"
        listTypes["java.lang.Float"] = "FloatList"
        listTypes["java.lang.Double"] = "DoubleList"
        listTypes["java.lang.Boolean"] = "BooleanList"
        listTypes["java.lang.String"] = "StringList"
        listTypes["java.util.Date"] = "DateList"
        listTypes["io.realm.MutableRealmInteger"] = "MutableRealmIntegerList"
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

    private fun getBasicTypeName(qualifiedType: String): String {
        val type = QUALIFIED_TYPE_TO_BUILDER[qualifiedType]
        if (type != null) {
            return type
        }
        throw IllegalArgumentException("Unsupported type: $qualifiedType")
    }

    private fun getListTypeName(qualifiedType: String?): String {
        val type = QUALIFIED_LIST_TYPE_TO_BUILDER[qualifiedType]
        if (type != null) {
            return type
        }
        throw IllegalArgumentException("Unsupported list type: " + qualifiedType!!)
    }

}
