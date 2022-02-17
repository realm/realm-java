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

import java.util.*
import javax.lang.model.element.VariableElement

/**
 * Helper class for creating the correct method calls to the OsObjectBuilder class.
 */
object OsObjectBuilderTypeHelper {

    private val QUALIFIED_TYPE_TO_BUILDER: Map<QualifiedClassName, String>
    private val QUALIFIED_LIST_TYPE_TO_BUILDER: Map<QualifiedClassName, String>
    private val QUALIFIED_MAP_VALUES: Map<QualifiedClassName, String> = mapOf(
            QualifiedClassName("io.realm.RealmAny") to "RealmAnyValueDictionary",
            QualifiedClassName("java.lang.Boolean") to "BooleanValueDictionary",
            QualifiedClassName("java.lang.String") to "StringValueDictionary",
            QualifiedClassName("java.lang.Integer") to "IntegerValueDictionary",
            QualifiedClassName("java.lang.Float") to "FloatValueDictionary",
            QualifiedClassName("java.lang.Long") to "LongValueDictionary",
            QualifiedClassName("java.lang.Short") to "ShortValueDictionary",
            QualifiedClassName("java.lang.Byte") to "ByteValueDictionary",
            QualifiedClassName("java.lang.Double") to "DoubleValueDictionary",
            QualifiedClassName("java.util.Date") to "DateValueDictionary",
            QualifiedClassName("byte[]") to "BinaryValueDictionary",
            QualifiedClassName("org.bson.types.ObjectId") to "ObjectIdValueDictionary",
            QualifiedClassName("org.bson.types.Decimal128") to "Decimal128ValueDictionary",
            QualifiedClassName("java.util.UUID") to "UUIDValueDictionary"
    )

    private val QUALIFIED_SET_VALUES: Map<QualifiedClassName, String> = mapOf(
            QualifiedClassName("io.realm.RealmAny") to "RealmAnySet",
            QualifiedClassName("java.lang.Boolean") to "BooleanSet",
            QualifiedClassName("java.lang.String") to "StringSet",
            QualifiedClassName("java.lang.Integer") to "IntegerSet",
            QualifiedClassName("java.lang.Float") to "FloatSet",
            QualifiedClassName("java.lang.Long") to "LongSet",
            QualifiedClassName("java.lang.Short") to "ShortSet",
            QualifiedClassName("java.lang.Byte") to "ByteSet",
            QualifiedClassName("java.lang.Double") to "DoubleSet",
            QualifiedClassName("java.util.Date") to "DateSet",
            QualifiedClassName("byte[]") to "BinarySet",
            QualifiedClassName("org.bson.types.ObjectId") to "ObjectIdSet",
            QualifiedClassName("org.bson.types.Decimal128") to "Decimal128Set",
            QualifiedClassName("java.util.UUID") to "UUIDSet"
    )

    init {
        // Map of qualified types to their OsObjectBuilder Type
        val fieldTypes = HashMap<QualifiedClassName, String>()
        fieldTypes.apply {
            this[QualifiedClassName("byte")] = "Integer"
            this[QualifiedClassName("byte")] = "Integer"
            this[QualifiedClassName("short")] = "Integer"
            this[QualifiedClassName("int")] = "Integer"
            this[QualifiedClassName("long")] = "Integer"
            this[QualifiedClassName("float")] = "Float"
            this[QualifiedClassName("double")] = "Double"
            this[QualifiedClassName("boolean")] = "Boolean"
            this[QualifiedClassName("byte[]")] = "ByteArray"
            this[QualifiedClassName("java.lang.Byte")] = "Integer"
            this[QualifiedClassName("java.lang.Short")] = "Integer"
            this[QualifiedClassName("java.lang.Integer")] = "Integer"
            this[QualifiedClassName("java.lang.Long")] = "Integer"
            this[QualifiedClassName("java.lang.Float")] = "Float"
            this[QualifiedClassName("java.lang.Double")] = "Double"
            this[QualifiedClassName("java.lang.Boolean")] = "Boolean"
            this[QualifiedClassName("java.lang.String")] = "String"
            this[QualifiedClassName("java.util.Date")] = "Date"
            this[QualifiedClassName("org.bson.types.Decimal128")] = "Decimal128"
            this[QualifiedClassName("org.bson.types.ObjectId")] = "ObjectId"
            this[QualifiedClassName("java.util.UUID")] = "UUID"
            this[QualifiedClassName("io.realm.MutableRealmInteger")] = "MutableRealmInteger"
            this[QualifiedClassName("io.realm.RealmAny")] = "RealmAny"
        }
        QUALIFIED_TYPE_TO_BUILDER = Collections.unmodifiableMap(fieldTypes)

        // Map of qualified types to their OsObjectBuilder Type
        val listTypes = HashMap<QualifiedClassName, String>()
        listTypes.apply {
            this[QualifiedClassName("byte[]")] = "ByteArrayList"
            this[QualifiedClassName("java.lang.Byte")] = "ByteList"
            this[QualifiedClassName("java.lang.Short")] = "ShortList"
            this[QualifiedClassName("java.lang.Integer")] = "IntegerList"
            this[QualifiedClassName("java.lang.Long")] = "LongList"
            this[QualifiedClassName("java.lang.Float")] = "FloatList"
            this[QualifiedClassName("java.lang.Double")] = "DoubleList"
            this[QualifiedClassName("java.lang.Boolean")] = "BooleanList"
            this[QualifiedClassName("java.lang.String")] = "StringList"
            this[QualifiedClassName("java.util.Date")] = "DateList"
            this[QualifiedClassName("io.realm.MutableRealmInteger")] = "MutableRealmIntegerList"
            this[QualifiedClassName("org.bson.types.Decimal128")] = "Decimal128List"
            this[QualifiedClassName("org.bson.types.ObjectId")] = "ObjectIdList"
            this[QualifiedClassName("io.realm.RealmAny")] = "RealmAnyList"
            this[QualifiedClassName("java.util.UUID")] = "UUIDList"
        }
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
        } else if (Utils.isRealmDictionary(field)) {
            "add" + getDictionaryValueTypeName(Utils.getDictionaryType(field))
        } else if (Utils.isRealmSet(field)) {
            "add" + getSetValueTypeName(Utils.getSetType(field))
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

    private fun getDictionaryValueTypeName(typeName: QualifiedClassName?): String {
        return requireNotNull(QUALIFIED_MAP_VALUES[typeName]) {
            "Unsupported dictionary value type: '$typeName'"
        }
    }

    private fun getSetValueTypeName(typeName: QualifiedClassName?): String {
        return requireNotNull(QUALIFIED_SET_VALUES[typeName]) {
            "Unsupported set value type: '$typeName'"
        }
    }
}
