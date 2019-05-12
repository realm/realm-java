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

package io.realm.processor

import com.squareup.javawriter.JavaWriter

import java.io.IOException
import java.util.Collections
import java.util.HashMap
import java.util.Locale


/**
 * Helper class for converting between Json types and data types in Java that are supported by Realm.
 */
object RealmJsonTypeHelper {
    private val JAVA_TO_JSON_TYPES: Map<String, JsonToRealmFieldTypeConverter>

    init {
        val m = HashMap<String, JsonToRealmFieldTypeConverter>()
        m["byte"] = SimpleTypeConverter("byte", "Int")
        m["short"] = SimpleTypeConverter("short", "Int")
        m["int"] = SimpleTypeConverter("int", "Int")
        m["long"] = SimpleTypeConverter("long", "Long")
        m["float"] = SimpleTypeConverter("float", "Double")
        m["double"] = SimpleTypeConverter("double", "Double")
        m["boolean"] = SimpleTypeConverter("boolean", "Boolean")
        m["byte[]"] = ByteArrayTypeConverter()
        m["java.lang.Byte"] = m["byte"] as JsonToRealmFieldTypeConverter
        m["java.lang.Short"] = m["short"] as JsonToRealmFieldTypeConverter
        m["java.lang.Integer"] = m["int"] as JsonToRealmFieldTypeConverter
        m["java.lang.Long"] = m["long"] as JsonToRealmFieldTypeConverter
        m["java.lang.Float"] = m["float"] as JsonToRealmFieldTypeConverter
        m["java.lang.Double"] = m["double"] as JsonToRealmFieldTypeConverter
        m["java.lang.Boolean"] = m["boolean"] as JsonToRealmFieldTypeConverter
        m["java.lang.String"] = SimpleTypeConverter("String", "String")
        m["java.util.Date"] = DateTypeConverter()
        m["io.realm.MutableRealmInteger"] = MutableRealmIntegerTypeConverter()
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
    fun emitCreateObjectWithPrimaryKeyValue(qualifiedRealmObjectClass: String,
                                            qualifiedRealmObjectProxyClass: String,
                                            qualifiedFieldType: String,
                                            fieldName: String,
                                            writer: JavaWriter) {
        val typeEmitter = JAVA_TO_JSON_TYPES[qualifiedFieldType]
        typeEmitter?.emitGetObjectWithPrimaryKeyValue(qualifiedRealmObjectClass, qualifiedRealmObjectProxyClass, fieldName, writer)
    }

    @Throws(IOException::class)
    fun emitFillRealmObjectWithJsonValue(varName: String,
                                         setter: String,
                                         fieldName: String,
                                         qualifiedFieldType: String,
                                         proxyClass: String,
                                         writer: JavaWriter) {
        writer.apply {
            beginControlFlow("if (json.has(\"%s\"))", fieldName)
                beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                    emitStatement("%s.%s(null)", varName, setter)
                nextControlFlow("else")
                    emitStatement("%s %sObj = %s.createOrUpdateUsingJsonObject(realm, json.getJSONObject(\"%s\"), update)", qualifiedFieldType, fieldName, proxyClass, fieldName)
                    emitStatement("%s.%s(%sObj)", varName, setter, fieldName)
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
                                       proxyClass: String,
                                       writer: JavaWriter) {
        writer.apply {
            beginControlFlow("if (json.has(\"%s\"))", fieldName)
                beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                    emitStatement("%s.%s(null)", varName, setter)
                nextControlFlow("else")
                    emitStatement("%s.%s().clear()", varName, getter)
                    emitStatement("JSONArray array = json.getJSONArray(\"%s\")", fieldName)
                    beginControlFlow("for (int i = 0; i < array.length(); i++)")
                        emitStatement("%s item = %s.createOrUpdateUsingJsonObject(realm, array.getJSONObject(i), update)", fieldTypeCanonicalName, proxyClass, fieldTypeCanonicalName)
                        emitStatement("%s.%s().add(item)", varName, getter)
                    endControlFlow()
                endControlFlow()
            endControlFlow()
        }
    }

    @Throws(IOException::class)
    fun emitFillJavaTypeWithJsonValue(varName: String, accessor: String, fieldName: String, qualifiedFieldType: String, writer: JavaWriter) {
        val typeEmitter = JAVA_TO_JSON_TYPES[qualifiedFieldType]
        typeEmitter?.emitTypeConversion(varName, accessor, fieldName, qualifiedFieldType, writer)
    }

    @Throws(IOException::class)
    fun emitFillRealmObjectFromStream(varName: String,
                                      setter: String,
                                      fieldName: String,
                                      fieldTypeCanonicalName:
                                      String, proxyClass:
                                      String, writer: JavaWriter) {
        writer.apply {
            beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                emitStatement("reader.skipValue()")
                emitStatement("%s.%s(null)", varName, setter)
            nextControlFlow("else")
                emitStatement("%s %sObj = %s.createUsingJsonStream(realm, reader)", fieldTypeCanonicalName, fieldName, proxyClass)
                emitStatement("%s.%s(%sObj)", varName, setter, fieldName)
            endControlFlow()
        }
    }

    @Throws(IOException::class)
    fun emitFillRealmListFromStream(varName: String,
                                    getter: String,
                                    setter: String,
                                    fieldTypeCanonicalName: String,
                                    proxyClass: String,
                                    writer: JavaWriter) {
        writer.apply {
            beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                emitStatement("reader.skipValue()")
                emitStatement("%s.%s(null)", varName, setter)
            nextControlFlow("else")
                emitStatement("%s.%s(new RealmList<%s>())", varName, setter, fieldTypeCanonicalName)
                emitStatement("reader.beginArray()")
                beginControlFlow("while (reader.hasNext())")
                    emitStatement("%s item = %s.createUsingJsonStream(realm, reader)", fieldTypeCanonicalName, proxyClass)
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
                                   fieldType: String,
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
        override fun emitTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: String, writer: JavaWriter) {
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
        override fun emitStreamTypeConversion(varName: String, setter: String, fieldName: String, fieldType: String, writer: JavaWriter, isPrimaryKey: Boolean) {
            // Only throw exception for primitive types.
            // For boxed types and String, exception will be thrown in the setter.
            val statementSetNullOrThrow = if (Utils.isPrimitiveType(fieldType))
                String.format(Locale.US, Constants.STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE, fieldName)
            else
                String.format(Locale.US, "%s.%s(null)", varName, setter)

            writer.apply {
                beginControlFlow("if (reader.peek() != JsonToken.NULL)")
                    emitStatement("%s.%s((%s) reader.next%s())", varName, setter, castType, jsonType)
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
        override fun emitGetObjectWithPrimaryKeyValue(qualifiedRealmObjectClass: String,
                                                      qualifiedRealmObjectProxyClass: String, fieldName: String, writer: JavaWriter) {
            // No error checking is done here for valid primary key types.
            // This should be done by the annotation processor.
            writer.apply {
                beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        emitStatement("obj = (%1\$s) realm.createObjectInternal(%2\$s.class, null, true, excludeFields)", qualifiedRealmObjectProxyClass, qualifiedRealmObjectClass)
                    nextControlFlow("else")
                        emitStatement("obj = (%1\$s) realm.createObjectInternal(%2\$s.class, json.get%3\$s(\"%4\$s\"), true, excludeFields)", qualifiedRealmObjectProxyClass, qualifiedRealmObjectClass, jsonType, fieldName)
                    endControlFlow()
                nextControlFlow("else")
                    emitStatement(Constants.STATEMENT_EXCEPTION_NO_PRIMARY_KEY_IN_JSON, fieldName)
                endControlFlow()
            }
        }
    }

    private class ByteArrayTypeConverter : JsonToRealmFieldTypeConverter {
        @Throws(IOException::class)
        override fun emitTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: String, writer: JavaWriter) {
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
        override fun emitStreamTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: String, writer: JavaWriter, isPrimaryKey: Boolean) {
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
        override fun emitGetObjectWithPrimaryKeyValue(qualifiedRealmObjectClass: String, qualifiedRealmObjectProxyClass: String, fieldName: String, writer: JavaWriter) {
            throw IllegalArgumentException("'byte[]' is not allowed as a primary key value.")
        }
    }

    private class DateTypeConverter : JsonToRealmFieldTypeConverter {
        @Throws(IOException::class)
        override fun emitTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: String, writer: JavaWriter) {
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
        override fun emitStreamTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: String, writer: JavaWriter, isPrimaryKey: Boolean) {
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
        override fun emitGetObjectWithPrimaryKeyValue(qualifiedRealmObjectClass: String, qualifiedRealmObjectProxyClass: String, fieldName: String, writer: JavaWriter) {
            throw IllegalArgumentException("'Date' is not allowed as a primary key value.")
        }
    }

    private class MutableRealmIntegerTypeConverter : JsonToRealmFieldTypeConverter {
        @Throws(IOException::class)
        override fun emitTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: String, writer: JavaWriter) {
            writer.apply {
                beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    emitStatement("%1\$s.%2\$s().set((json.isNull(\"%3\$s\")) ? null : json.getLong(\"%3\$s\"))", varName, accessor, fieldName)
                endControlFlow()
            }
        }

        @Throws(IOException::class)
        override fun emitStreamTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: String, writer: JavaWriter, isPrimaryKey: Boolean) {
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
        override fun emitGetObjectWithPrimaryKeyValue(qualifiedRealmObjectClass: String, qualifiedRealmObjectProxyClass: String, fieldName: String, writer: JavaWriter) {
            throw IllegalArgumentException("'MutableRealmInteger' is not allowed as a primary key value.")
        }
    }

    private interface JsonToRealmFieldTypeConverter {
        @Throws(IOException::class)
        fun emitTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: String, writer: JavaWriter)

        @Throws(IOException::class)
        fun emitStreamTypeConversion(varName: String, accessor: String, fieldName: String, fieldType: String, writer: JavaWriter, isPrimaryKey: Boolean)

        @Throws(IOException::class)
        fun emitGetObjectWithPrimaryKeyValue(qualifiedRealmObjectClass: String, qualifiedRealmObjectProxyClass: String, fieldName: String, writer: JavaWriter)
    }
}