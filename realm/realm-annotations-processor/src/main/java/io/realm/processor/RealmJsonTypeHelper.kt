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

import com.squareup.javawriter.JavaWriter
import java.io.IOException
import java.util.*


/**
 * Helper class for converting between Json types and data types in Java that are supported by Realm.
 */
object RealmJsonTypeHelper {
    private val JAVA_TO_JSON_TYPES: Map<QualifiedClassName, JsonToRealmFieldTypeConverter>

    init {
        val m = HashMap<QualifiedClassName, JsonToRealmFieldTypeConverter>()
        m[QualifiedClassName("byte")] = SimpleTypeConverter("byte", "Int")
        m[QualifiedClassName("short")] = SimpleTypeConverter("short", "Int")
        m[QualifiedClassName("int")] = SimpleTypeConverter("int", "Int")
        m[QualifiedClassName("long")] = SimpleTypeConverter("long", "Long")
        m[QualifiedClassName("float")] = SimpleTypeConverter("float", "Double")
        m[QualifiedClassName("double")] = SimpleTypeConverter("double", "Double")
        m[QualifiedClassName("boolean")] = SimpleTypeConverter("boolean", "Boolean")
        m[QualifiedClassName("byte[]")] = ByteArrayTypeConverter()
        m[QualifiedClassName("java.lang.Byte")] = m[QualifiedClassName("byte")] as JsonToRealmFieldTypeConverter
        m[QualifiedClassName("java.lang.Short")] = m[QualifiedClassName("short")] as JsonToRealmFieldTypeConverter
        m[QualifiedClassName("java.lang.Integer")] = m[QualifiedClassName("int")] as JsonToRealmFieldTypeConverter
        m[QualifiedClassName("java.lang.Long")] = m[QualifiedClassName("long")] as JsonToRealmFieldTypeConverter
        m[QualifiedClassName("java.lang.Float")] = m[QualifiedClassName("float")] as JsonToRealmFieldTypeConverter
        m[QualifiedClassName("java.lang.Double")] = m[QualifiedClassName("double")] as JsonToRealmFieldTypeConverter
        m[QualifiedClassName("java.lang.Boolean")] = m[QualifiedClassName("boolean")] as JsonToRealmFieldTypeConverter
        m[QualifiedClassName("java.lang.String")] = SimpleTypeConverter("String", "String")
        m[QualifiedClassName("java.util.Date")] = DateTypeConverter()
        m[QualifiedClassName("org.bson.types.Decimal128")] = Decimal128TypeConverter()
        m[QualifiedClassName("org.bson.types.ObjectId")] = ObjectIdTypeConverter()
        m[QualifiedClassName("java.util.UUID")] = UUIDTypeConverter()
        m[QualifiedClassName("io.realm.MutableRealmInteger")] = MutableRealmIntegerTypeConverter()
        JAVA_TO_JSON_TYPES = Collections.unmodifiableMap(m)
    }

    @Throws(IOException::class)
    fun emitIllegalJsonValueException(fieldType: String, fieldName: String, writer: JavaWriter) {
        writer.apply {
            beginControlFlow("if (json.has(\"%s\"))", fieldName)
                emitStatement(Constants.STATEMENT_EXCEPTION_ILLEGAL_JSON_LOAD, fieldType, fieldName)
            endControlFlow()
        }
    }

    @Throws(IOException::class)
    fun emitCreateObjectWithPrimaryKeyValue(realmObjectClass: QualifiedClassName,
                                            realmObjectProxyClass: QualifiedClassName,
                                            fieldType: QualifiedClassName,
                                            fieldName: String,
                                            writer: JavaWriter) {
        val typeEmitter = JAVA_TO_JSON_TYPES[fieldType]
        typeEmitter?.emitGetObjectWithPrimaryKeyValue(realmObjectClass, realmObjectProxyClass, fieldName, writer)
    }

    @Throws(IOException::class)
    fun emitFillRealmObjectWithJsonValue(varName: String,
                                         setter: String,
                                         fieldName: String,
                                         qualifiedFieldType: QualifiedClassName,
                                         proxyClass: SimpleClassName,
                                         embedded: Boolean,
                                         writer: JavaWriter) {
        writer.apply {
            beginControlFlow("if (json.has(\"%s\"))", fieldName)
                beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                    emitStatement("%s.%s(null)", varName, setter)
                nextControlFlow("else")
                    if (!embedded) {
                        emitStatement("%s %sObj = %s.createOrUpdateUsingJsonObject(realm, json.getJSONObject(\"%s\"), update)", qualifiedFieldType, fieldName, proxyClass, fieldName)
                        emitStatement("%s.%s(%sObj)", varName, setter, fieldName)
                    } else {
                        emitStatement("%s.createOrUpdateEmbeddedUsingJsonObject(realm, (RealmModel)%s, \"%s\", json.getJSONObject(\"%s\"), update)", proxyClass, varName, fieldName, fieldName)
                    }
                endControlFlow()
            endControlFlow()
        }
    }

    @Throws(IOException::class)
    fun emitFillRealmListWithJsonValue(varName: String,
                                       getter: String,
                                       setter: String,
                                       fieldName: String,
                                       fieldTypeCanonicalName: String,
                                       proxyClass: SimpleClassName,
                                       embedded: Boolean,
                                       writer: JavaWriter) {
        writer.apply {
            beginControlFlow("if (json.has(\"%s\"))", fieldName)
                beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                    emitStatement("%s.%s(null)", varName, setter)
                nextControlFlow("else")
                    emitStatement("%s.%s().clear()", varName, getter)
                    emitStatement("JSONArray array = json.getJSONArray(\"%s\")", fieldName)
                    beginControlFlow("for (int i = 0; i < array.length(); i++)")
                        if (!embedded) {
                            emitStatement("%s item = %s.createOrUpdateUsingJsonObject(realm, array.getJSONObject(i), update)", fieldTypeCanonicalName, proxyClass, fieldTypeCanonicalName)
                            emitStatement("%s.%s().add(item)", varName, getter)
                       } else {
                            emitStatement("%s.createOrUpdateEmbeddedUsingJsonObject(realm, (RealmModel)%s, \"%s\", array.getJSONObject(i), update)", proxyClass, varName, fieldName)
                        }
                    endControlFlow()
                endControlFlow()
            endControlFlow()
        }
    }

    @Throws(IOException::class)
    fun emitFillJavaTypeWithJsonValue(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter) {
        val typeEmitter = JAVA_TO_JSON_TYPES[fieldType]
        typeEmitter?.emitTypeConversion(varName, accessor, fieldName, fieldType, writer)
    }

    @Throws(IOException::class)
    fun emitFillRealmObjectFromStream(varName: String,
                                      setter: String,
                                      fieldName: String,
                                      fieldType: QualifiedClassName,
                                      proxyClass: SimpleClassName,
                                      writer: JavaWriter) {
        writer.apply {
            beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                emitStatement("reader.skipValue()")
                emitStatement("%s.%s(null)", varName, setter)
            nextControlFlow("else")
                emitStatement("%s %sObj = %s.createUsingJsonStream(realm, reader)", fieldType, fieldName, proxyClass)
                emitStatement("%s.%s(%sObj)", varName, setter, fieldName)
            endControlFlow()
        }
    }

    @Throws(IOException::class)
    fun emitFillRealmListFromStream(varName: String,
                                    getter: String,
                                    setter: String,
                                    fieldType: QualifiedClassName,
                                    proxyClass: SimpleClassName,
                                    writer: JavaWriter) {
        writer.apply {
            beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                emitStatement("reader.skipValue()")
                emitStatement("%s.%s(null)", varName, setter)
            nextControlFlow("else")
                emitStatement("%s.%s(new RealmList<%s>())", varName, setter, fieldType)
                emitStatement("reader.beginArray()")
                beginControlFlow("while (reader.hasNext())")
                    emitStatement("%s item = %s.createUsingJsonStream(realm, reader)", fieldType, proxyClass)
                    emitStatement("%s.%s().add(item)", varName, getter)
                endControlFlow()
                emitStatement("reader.endArray()")
            endControlFlow()
        }
    }

    @Throws(IOException::class)
    fun emitFillJavaTypeFromStream(varName: String,
                                   metaData: ClassMetaData,
                                   accessor: String,
                                   fieldName: String,
                                   fieldType: QualifiedClassName,
                                   writer: JavaWriter) {
        val isPrimaryKey = metaData.hasPrimaryKey() && metaData.primaryKey!!.simpleName.toString() == fieldName
        val typeEmitter = JAVA_TO_JSON_TYPES[fieldType]
        typeEmitter?.emitStreamTypeConversion(varName, accessor, fieldName, fieldType, writer, isPrimaryKey)
    }

    /**
     * Creates a conversion between simple types which can be expressed as RealmObject.setFieldName((<castType>)
     * json.get<jsonType>) or RealmObject.setFieldName((<castType>) reader.next<jsonType>
     *
     * @param castType Java type to cast to.
     * @param jsonType JsonType to get data from.
     */
    private class SimpleTypeConverter(private val castType: String, private val jsonType: String) : JsonToRealmFieldTypeConverter {

        @Throws(IOException::class)
        override fun emitTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter) {
            // Only throw exception for primitive types.
            // For boxed types and String, exception will be thrown in the setter.
            val statementSetNullOrThrow = if (Utils.isPrimitiveType(fieldType))
                String.format(Locale.US, Constants.STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE, fieldName)
            else
                String.format(Locale.US, "%s.%s(null)", varName, accessor)

            writer.apply {
                beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        emitStatement(statementSetNullOrThrow)
                    nextControlFlow("else")
                        emitStatement("%s.%s((%s) json.get%s(\"%s\"))", varName, accessor, castType, jsonType, fieldName)
                    endControlFlow()
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitStreamTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter, isPrimaryKey: Boolean) {
            // Only throw exception for primitive types.
            // For boxed types and String, exception will be thrown in the setter.
            val statementSetNullOrThrow = if (Utils.isPrimitiveType(fieldType))
                String.format(Locale.US, Constants.STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE, fieldName)
            else
                String.format(Locale.US, "%s.%s(null)", varName, accessor)

            writer.apply {
                beginControlFlow("if (reader.peek() != JsonToken.NULL)")
                    emitStatement("%s.%s((%s) reader.next%s())", varName, accessor, castType, jsonType)
                nextControlFlow("else")
                    emitStatement("reader.skipValue()")
                    emitStatement(statementSetNullOrThrow)
                endControlFlow()

                if (isPrimaryKey) {
                    emitStatement("jsonHasPrimaryKey = true")
                }
            }
        }

        @Throws(IOException::class)
        override fun emitGetObjectWithPrimaryKeyValue(realmObjectClass: QualifiedClassName, realmObjectProxyClass: QualifiedClassName, fieldName: String, writer: JavaWriter) {
            // No error checking is done here for valid primary key types.
            // This should be done by the annotation processor.
            writer.apply {
                beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        emitStatement("obj = (%1\$s) realm.createObjectInternal(%2\$s.class, null, true, excludeFields)", realmObjectProxyClass, realmObjectClass)
                    nextControlFlow("else")
                        emitStatement("obj = (%1\$s) realm.createObjectInternal(%2\$s.class, json.get%3\$s(\"%4\$s\"), true, excludeFields)", realmObjectProxyClass, realmObjectClass, jsonType, fieldName)
                    endControlFlow()
                nextControlFlow("else")
                    emitStatement(Constants.STATEMENT_EXCEPTION_NO_PRIMARY_KEY_IN_JSON, fieldName)
                endControlFlow()
            }
        }
    }

    private class ByteArrayTypeConverter : JsonToRealmFieldTypeConverter {
        @Throws(IOException::class)
        override fun emitTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter) {
            writer.apply {
                beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        emitStatement("%s.%s(null)", varName, accessor)
                    nextControlFlow("else")
                        emitStatement("%s.%s(JsonUtils.stringToBytes(json.getString(\"%s\")))", varName, accessor, fieldName)
                    endControlFlow()
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitStreamTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter, isPrimaryKey: Boolean) {
            writer.apply {
                beginControlFlow("if (reader.peek() != JsonToken.NULL)")
                    emitStatement("%s.%s(JsonUtils.stringToBytes(reader.nextString()))", varName, accessor)
                nextControlFlow("else")
                    emitStatement("reader.skipValue()")
                    emitStatement("%s.%s(null)", varName, accessor)
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitGetObjectWithPrimaryKeyValue(realmObjectClass: QualifiedClassName, realmObjectProxyClass: QualifiedClassName, fieldName: String, writer: JavaWriter) {
            throw IllegalArgumentException("'byte[]' is not allowed as a primary key value.")
        }
    }

    private class DateTypeConverter : JsonToRealmFieldTypeConverter {
        @Throws(IOException::class)
        override fun emitTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter) {
            writer.apply {
                beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        emitStatement("%s.%s(null)", varName, accessor)
                    nextControlFlow("else")
                        emitStatement("Object timestamp = json.get(\"%s\")", fieldName)
                        beginControlFlow("if (timestamp instanceof String)")
                            emitStatement("%s.%s(JsonUtils.stringToDate((String) timestamp))", varName, accessor)
                        nextControlFlow("else")
                            emitStatement("%s.%s(new Date(json.getLong(\"%s\")))", varName, accessor, fieldName)
                        endControlFlow()
                    endControlFlow()
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitStreamTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter, isPrimaryKey: Boolean) {
            writer.apply {
                beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                    emitStatement("reader.skipValue()")
                    emitStatement("%s.%s(null)", varName, accessor)
                nextControlFlow("else if (reader.peek() == JsonToken.NUMBER)")
                    emitStatement("long timestamp = reader.nextLong()", fieldName)
                    beginControlFlow("if (timestamp > -1)")
                        emitStatement("%s.%s(new Date(timestamp))", varName, accessor)
                    endControlFlow()
                nextControlFlow("else")
                    emitStatement("%s.%s(JsonUtils.stringToDate(reader.nextString()))", varName, accessor)
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitGetObjectWithPrimaryKeyValue(realmObjectClass: QualifiedClassName, realmObjectProxyClass: QualifiedClassName, fieldName: String, writer: JavaWriter) {
            throw IllegalArgumentException("'Date' is not allowed as a primary key value.")
        }
    }

    private class Decimal128TypeConverter : JsonToRealmFieldTypeConverter {
        @Throws(IOException::class)
        override fun emitTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter) {
            writer.apply {
                beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        emitStatement("%s.%s(null)", varName, accessor)
                    nextControlFlow("else")
                            emitStatement("Object decimal = json.get(\"%s\")", fieldName)
                            beginControlFlow("if (decimal instanceof org.bson.types.Decimal128)")
                                emitStatement("%s.%s((org.bson.types.Decimal128) decimal)", varName, accessor)
                            nextControlFlow("else if (decimal instanceof String)")
                                emitStatement("%s.%s(org.bson.types.Decimal128.parse((String)decimal))", varName, accessor)
                            nextControlFlow("else if (decimal instanceof Integer)")
                            emitStatement("%s.%s(new org.bson.types.Decimal128((Integer)(decimal)))", varName, accessor, fieldName)
                            nextControlFlow("else if (decimal instanceof Long)")
                                emitStatement("%s.%s(new org.bson.types.Decimal128((Long)(decimal)))", varName, accessor, fieldName)
                            nextControlFlow("else if (decimal instanceof Double)")
                                emitStatement("%s.%s(new org.bson.types.Decimal128(new java.math.BigDecimal((Double)(decimal))))", varName, accessor, fieldName)
                            nextControlFlow("else")
                                emitStatement("throw new UnsupportedOperationException(decimal.getClass() + \" is not supported as a Decimal128 value\")")
                            endControlFlow()
                    endControlFlow()
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitStreamTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter, isPrimaryKey: Boolean) {
            writer.apply {
                beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                    emitStatement("reader.skipValue()")
                    emitStatement("%s.%s(null)", varName, accessor)
                nextControlFlow("else")
                    emitStatement("%s.%s(org.bson.types.Decimal128.parse(reader.nextString()))", varName, accessor)
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitGetObjectWithPrimaryKeyValue(realmObjectClass: QualifiedClassName, realmObjectProxyClass: QualifiedClassName, fieldName: String, writer: JavaWriter) {
            throw IllegalArgumentException("'Decimal128' is not allowed as a primary key value.")
        }
    }

    private class ObjectIdTypeConverter() : JsonToRealmFieldTypeConverter {
        @Throws(IOException::class)
        override fun emitTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter) {
            writer.apply {
                beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        emitStatement("%s.%s(null)", varName, accessor)
                    nextControlFlow("else")
                        emitStatement("Object id = json.get(\"%s\")", fieldName)
                        beginControlFlow("if (id instanceof org.bson.types.ObjectId)")
                            emitStatement("%s.%s((org.bson.types.ObjectId) id)", varName, accessor)
                        nextControlFlow("else")
                            emitStatement("%s.%s(new org.bson.types.ObjectId((String)id))", varName, accessor)
                        endControlFlow()
                    endControlFlow()
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitStreamTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter, isPrimaryKey: Boolean) {
            writer.apply {
                beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                    emitStatement("reader.skipValue()")
                    emitStatement("%s.%s(null)", varName, accessor)
                nextControlFlow("else")
                    emitStatement("%s.%s(new org.bson.types.ObjectId(reader.nextString()))", varName, accessor)
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitGetObjectWithPrimaryKeyValue(realmObjectClass: QualifiedClassName, realmObjectProxyClass: QualifiedClassName, fieldName: String, writer: JavaWriter) {
            // No error checking is done here for valid primary key types.
            // This should be done by the annotation processor.
            writer.apply {
                beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        emitStatement("obj = (%1\$s) realm.createObjectInternal(%2\$s.class, null, true, excludeFields)", realmObjectProxyClass, realmObjectClass)
                    nextControlFlow("else")
                        emitStatement("obj = (%1\$s) realm.createObjectInternal(%2\$s.class, json.get(\"%3\$s\"), true, excludeFields)", realmObjectProxyClass, realmObjectClass, fieldName)
                    endControlFlow()
                nextControlFlow("else")
                    emitStatement(Constants.STATEMENT_EXCEPTION_NO_PRIMARY_KEY_IN_JSON, fieldName)
                endControlFlow()
            }
        }
    }

    private class UUIDTypeConverter() : JsonToRealmFieldTypeConverter {
        @Throws(IOException::class)
        override fun emitTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter) {
            writer.apply {
                beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        emitStatement("%s.%s(null)", varName, accessor)
                    nextControlFlow("else")
                        emitStatement("Object id = json.get(\"%s\")", fieldName)
                        beginControlFlow("if (id instanceof java.util.UUID)")
                            emitStatement("%s.%s((java.util.UUID) id)", varName, accessor)
                        nextControlFlow("else")
                            emitStatement("%s.%s(java.util.UUID.fromString((String)id))", varName, accessor)
                        endControlFlow()
                    endControlFlow()
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitStreamTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter, isPrimaryKey: Boolean) {
            writer.apply {
                beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                    emitStatement("reader.skipValue()")
                    emitStatement("%s.%s(null)", varName, accessor)
                nextControlFlow("else")
                    emitStatement("%s.%s(java.util.UUID.fromString(reader.nextString()))", varName, accessor)
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitGetObjectWithPrimaryKeyValue(realmObjectClass: QualifiedClassName, realmObjectProxyClass: QualifiedClassName, fieldName: String, writer: JavaWriter) {
            // No error checking is done here for valid primary key types.
            // This should be done by the annotation processor.
            writer.apply {
                beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        emitStatement("obj = (%1\$s) realm.createObjectInternal(%2\$s.class, null, true, excludeFields)", realmObjectProxyClass, realmObjectClass)
                    nextControlFlow("else")
                        emitStatement("obj = (%1\$s) realm.createObjectInternal(%2\$s.class, json.get(\"%3\$s\"), true, excludeFields)", realmObjectProxyClass, realmObjectClass, fieldName)
                    endControlFlow()
                nextControlFlow("else")
                    emitStatement(Constants.STATEMENT_EXCEPTION_NO_PRIMARY_KEY_IN_JSON, fieldName)
                endControlFlow()
            }
        }
    }


    private class MutableRealmIntegerTypeConverter : JsonToRealmFieldTypeConverter {
        @Throws(IOException::class)
        override fun emitTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter) {
            writer.apply {
                beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    emitStatement("%1\$s.%2\$s().set((json.isNull(\"%3\$s\")) ? null : json.getLong(\"%3\$s\"))", varName, accessor, fieldName)
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitStreamTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: QualifiedClassName, writer: JavaWriter, isPrimaryKey: Boolean) {
            writer.apply {
                emitStatement("Long val = null")
                beginControlFlow("if (reader.peek() != JsonToken.NULL)")
                    emitStatement("val = reader.nextLong()")
                nextControlFlow("else")
                    emitStatement("reader.skipValue()")
                endControlFlow()
                emitStatement("%1\$s.%2\$s().set(val)", varName, accessor)
            }
        }

        @Throws(IOException::class)
        override fun emitGetObjectWithPrimaryKeyValue(realmObjectClass: QualifiedClassName, realmObjectProxyClass: QualifiedClassName, fieldName: String, writer: JavaWriter) {
            throw IllegalArgumentException("'MutableRealmInteger' is not allowed as a primary key value.")
        }
    }

    private interface JsonToRealmFieldTypeConverter {
        @Throws(IOException::class)
        fun emitTypeConversion(varName: String,
                               accessor: String,
                               fieldName: String,
                               fieldType: QualifiedClassName,
                               writer: JavaWriter)

        @Throws(IOException::class)
        fun emitStreamTypeConversion(varName: String,
                                     accessor: String,
                                     fieldName: String,
                                     fieldType: QualifiedClassName,
                                     writer: JavaWriter,
                                     isPrimaryKey: Boolean)

        @Throws(IOException::class)
        fun emitGetObjectWithPrimaryKeyValue(realmObjectClass: QualifiedClassName,
                                             realmObjectProxyClass: QualifiedClassName,
                                             fieldName: String,
                                             writer: JavaWriter)
    }
}
