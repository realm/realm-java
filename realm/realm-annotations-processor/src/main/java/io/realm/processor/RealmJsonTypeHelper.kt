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

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Helper class for converting between Json types and data types in Java that are supported by Realm.
 */
public class RealmJsonTypeHelper {
    private static final Map<String, JsonToRealmFieldTypeConverter> JAVA_TO_JSON_TYPES;

    static {
        Map<String, JsonToRealmFieldTypeConverter> m = new HashMap<String, JsonToRealmFieldTypeConverter>();
        m.put("byte", new SimpleTypeConverter("byte", "Int"));
        m.put("short", new SimpleTypeConverter("short", "Int"));
        m.put("int", new SimpleTypeConverter("int", "Int"));
        m.put("long", new SimpleTypeConverter("long", "Long"));
        m.put("float", new SimpleTypeConverter("float", "Double"));
        m.put("double", new SimpleTypeConverter("double", "Double"));
        m.put("boolean", new SimpleTypeConverter("boolean", "Boolean"));
        m.put("byte[]", new ByteArrayTypeConverter());
        m.put("java.lang.Byte", m.get("byte"));
        m.put("java.lang.Short", m.get("short"));
        m.put("java.lang.Integer", m.get("int"));
        m.put("java.lang.Long", m.get("long"));
        m.put("java.lang.Float", m.get("float"));
        m.put("java.lang.Double", m.get("double"));
        m.put("java.lang.Boolean", m.get("boolean"));
        m.put("java.lang.String", new SimpleTypeConverter("String", "String"));
        m.put("java.util.Date", new DateTypeConverter());
        m.put("io.realm.MutableRealmInteger", new MutableRealmIntegerTypeConverter());
        JAVA_TO_JSON_TYPES = Collections.unmodifiableMap(m);
    }

    // Static helper class
    private RealmJsonTypeHelper() { }

    // @formatter:off
    public static void emitIllegalJsonValueException(String fieldType, String fieldName, JavaWriter writer)
            throws IOException {
        writer
            .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                .emitStatement(Constants.STATEMENT_EXCEPTION_ILLEGAL_JSON_LOAD, fieldType, fieldName)
            .endControlFlow();
    }
    // @formatter:on

    public static void emitCreateObjectWithPrimaryKeyValue(
            String qualifiedRealmObjectClass, String qualifiedRealmObjectProxyClass, String qualifiedFieldType, String fieldName, JavaWriter writer)
            throws IOException {
        JsonToRealmFieldTypeConverter typeEmitter = JAVA_TO_JSON_TYPES.get(qualifiedFieldType);
        if (typeEmitter != null) {
            typeEmitter.emitGetObjectWithPrimaryKeyValue(
                    qualifiedRealmObjectClass, qualifiedRealmObjectProxyClass, fieldName, writer);
        }
    }

    // @formatter:off
    public static void emitFillRealmObjectWithJsonValue(
            String varName, String setter, String fieldName, String qualifiedFieldType, String proxyClass, JavaWriter writer)
            throws IOException {
        writer
            .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                .beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                    .emitStatement("%s.%s(null)", varName, setter)
                .nextControlFlow("else")
                    .emitStatement(
                        "%s %sObj = %s.createOrUpdateUsingJsonObject(realm, json.getJSONObject(\"%s\"), update)",
                        qualifiedFieldType, fieldName, proxyClass, fieldName)
                    .emitStatement("%s.%s(%sObj)", varName, setter, fieldName)
                .endControlFlow()
            .endControlFlow();
    }
    // @formatter:on

    // @formatter:off
    public static void emitFillRealmListWithJsonValue(
            String varName, String getter, String setter, String fieldName, String fieldTypeCanonicalName, String proxyClass, JavaWriter writer)
            throws IOException {
        writer
            .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                .beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                    .emitStatement("%s.%s(null)", varName, setter)
                .nextControlFlow("else")
                    .emitStatement("%s.%s().clear()", varName, getter)
                    .emitStatement("JSONArray array = json.getJSONArray(\"%s\")", fieldName)
                    .beginControlFlow("for (int i = 0; i < array.length(); i++)")
                        .emitStatement(
                                "%s item = %s.createOrUpdateUsingJsonObject(realm, array.getJSONObject(i), update)",
                                fieldTypeCanonicalName, proxyClass, fieldTypeCanonicalName)
                        .emitStatement("%s.%s().add(item)", varName, getter)
                    .endControlFlow()
                .endControlFlow()
            .endControlFlow();
    }
    // @formatter:on

    public static void emitFillJavaTypeWithJsonValue(
            String varName, String accessor, String fieldName, String qualifiedFieldType, JavaWriter writer)
            throws IOException {
        JsonToRealmFieldTypeConverter typeEmitter = JAVA_TO_JSON_TYPES.get(qualifiedFieldType);
        if (typeEmitter != null) {
            typeEmitter.emitTypeConversion(varName, accessor, fieldName, qualifiedFieldType, writer);
        }
    }

    // @formatter:off
    public static void emitFillRealmObjectFromStream(
            String varName, String setter, String fieldName, String fieldTypeCanonicalName, String proxyClass, JavaWriter writer)
            throws IOException {
        writer
            .beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                .emitStatement("reader.skipValue()")
                .emitStatement("%s.%s(null)", varName, setter)
            .nextControlFlow("else")
                .emitStatement(
                        "%s %sObj = %s.createUsingJsonStream(realm, reader)",
                        fieldTypeCanonicalName, fieldName, proxyClass)
                .emitStatement("%s.%s(%sObj)", varName, setter, fieldName)
            .endControlFlow();
    }
    // @formatter:on

    // @formatter:off
    public static void emitFillRealmListFromStream(
            String varName, String getter, String setter, String fieldTypeCanonicalName, String proxyClass, JavaWriter writer)
            throws IOException {
        writer
            .beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                .emitStatement("reader.skipValue()")
                .emitStatement("%s.%s(null)", varName, setter)
            .nextControlFlow("else")
                .emitStatement("%s.%s(new RealmList<%s>())", varName, setter, fieldTypeCanonicalName)
                .emitStatement("reader.beginArray()")
                .beginControlFlow("while (reader.hasNext())")
                    .emitStatement("%s item = %s.createUsingJsonStream(realm, reader)", fieldTypeCanonicalName, proxyClass)
                    .emitStatement("%s.%s().add(item)", varName, getter)
                .endControlFlow()
                .emitStatement("reader.endArray()")
            .endControlFlow();
    }
    // @formatter:on

    public static void emitFillJavaTypeFromStream(
            String varName, ClassMetaData metaData, String accessor, String fieldName, String fieldType, JavaWriter writer)
            throws IOException {
        boolean isPrimaryKey = metaData.hasPrimaryKey() && metaData.getPrimaryKey().getSimpleName().toString().equals(fieldName);
        JsonToRealmFieldTypeConverter typeEmitter = JAVA_TO_JSON_TYPES.get(fieldType);
        if (typeEmitter != null) {
            typeEmitter.emitStreamTypeConversion(varName, accessor, fieldName, fieldType, writer, isPrimaryKey);
        }
    }

    private static class SimpleTypeConverter implements JsonToRealmFieldTypeConverter {
        private final String castType;
        private final String jsonType;

        /**
         * Creates a conversion between simple types which can be expressed as RealmObject.setFieldName((<castType>)
         * json.get<jsonType>) or RealmObject.setFieldName((<castType>) reader.next<jsonType>
         *
         * @param castType Java type to cast to.
         * @param jsonType JsonType to get data from.
         */
        private SimpleTypeConverter(String castType, String jsonType) {
            this.castType = castType;
            this.jsonType = jsonType;
        }

        @Override
        public void emitTypeConversion(
                String varName, String accessor, String fieldName, String fieldType, JavaWriter writer)
                throws IOException {
            // Only throw exception for primitive types.
            // For boxed types and String, exception will be thrown in the setter.
            String statementSetNullOrThrow = Utils.isPrimitiveType(fieldType) ?
                    String.format(Locale.US, Constants.STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE, fieldName) :
                    String.format(Locale.US, "%s.%s(null)", varName, accessor);

            // @formatter:off
            writer
                .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    .beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        .emitStatement(statementSetNullOrThrow)
                    .nextControlFlow("else")
                        .emitStatement("%s.%s((%s) json.get%s(\"%s\"))", varName, accessor, castType, jsonType, fieldName)
                    .endControlFlow()
                .endControlFlow();
            // @formatter:on
        }

        @Override
        public void emitStreamTypeConversion(
                String varName, String setter, String fieldName, String fieldType, JavaWriter writer, boolean isPrimaryKey)
                throws IOException {
            // Only throw exception for primitive types.
            // For boxed types and String, exception will be thrown in the setter.
            String statementSetNullOrThrow = (Utils.isPrimitiveType(fieldType)) ?
                    String.format(Locale.US, Constants.STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE, fieldName) :
                    String.format(Locale.US, "%s.%s(null)", varName, setter);

            // @formatter:off
            writer
                .beginControlFlow("if (reader.peek() != JsonToken.NULL)")
                    .emitStatement("%s.%s((%s) reader.next%s())", varName, setter, castType, jsonType)
                .nextControlFlow("else")
                    .emitStatement("reader.skipValue()")
                    .emitStatement(statementSetNullOrThrow)
                .endControlFlow();
            // @formatter:on

            if (isPrimaryKey) {
                writer.emitStatement("jsonHasPrimaryKey = true");
            }
        }

        // @formatter:off
        @Override
        public void emitGetObjectWithPrimaryKeyValue(String qualifiedRealmObjectClass,
            String qualifiedRealmObjectProxyClass, String fieldName, JavaWriter writer) throws IOException {
            // No error checking is done here for valid primary key types.
            // This should be done by the annotation processor.
            writer
                .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    .beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        .emitStatement("obj = (%1$s) realm.createObjectInternal(%2$s.class, null, true, excludeFields)",
                                qualifiedRealmObjectProxyClass, qualifiedRealmObjectClass)
                    .nextControlFlow("else")
                        .emitStatement(
                                "obj = (%1$s) realm.createObjectInternal(%2$s.class, json.get%3$s(\"%4$s\"), true, excludeFields)",
                                qualifiedRealmObjectProxyClass, qualifiedRealmObjectClass, jsonType, fieldName)
                    .endControlFlow()
                .nextControlFlow("else")
                    .emitStatement(Constants.STATEMENT_EXCEPTION_NO_PRIMARY_KEY_IN_JSON, fieldName)
                .endControlFlow();
        }
        // @formatter:on
    }

    private static class ByteArrayTypeConverter implements JsonToRealmFieldTypeConverter {
        // @formatter:off
        @Override
        public void emitTypeConversion(String varName, String accessor, String fieldName, String fieldType, JavaWriter writer)
                throws IOException {
            writer
                .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    .beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        .emitStatement("%s.%s(null)", varName, accessor)
                    .nextControlFlow("else")
                        .emitStatement("%s.%s(JsonUtils.stringToBytes(json.getString(\"%s\")))", varName, accessor, fieldName)
                    .endControlFlow()
                .endControlFlow();
        }
        // @formatter:on

        // @formatter:off
        @Override
        public void emitStreamTypeConversion(String varName, String accessor, String fieldName, String fieldType, JavaWriter writer, boolean isPrimaryKey)
                throws IOException {
            writer
                .beginControlFlow("if (reader.peek() != JsonToken.NULL)")
                    .emitStatement("%s.%s(JsonUtils.stringToBytes(reader.nextString()))", varName, accessor)
                .nextControlFlow("else")
                    .emitStatement("reader.skipValue()")
                    .emitStatement("%s.%s(null)", varName, accessor)
                .endControlFlow();
        }
        // @formatter:on

        @Override
        public void emitGetObjectWithPrimaryKeyValue(
                String qualifiedRealmObjectClass, String qualifiedRealmObjectProxyClass, String fieldName, JavaWriter writer)
                throws IOException {
            throw new IllegalArgumentException("'byte[]' is not allowed as a primary key value.");
        }
    }

    private static class DateTypeConverter implements JsonToRealmFieldTypeConverter {
        // @formatter:off
        @Override
        public void emitTypeConversion(
                String varName, String accessor, String fieldName, String fieldType, JavaWriter writer)
                throws IOException {
            writer
                .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    .beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        .emitStatement("%s.%s(null)", varName, accessor)
                    .nextControlFlow("else")
                        .emitStatement("Object timestamp = json.get(\"%s\")", fieldName)
                        .beginControlFlow("if (timestamp instanceof String)")
                            .emitStatement("%s.%s(JsonUtils.stringToDate((String) timestamp))", varName, accessor)
                        .nextControlFlow("else")
                            .emitStatement("%s.%s(new Date(json.getLong(\"%s\")))", varName, accessor, fieldName)
                        .endControlFlow()
                    .endControlFlow()
                .endControlFlow();
        }
        // @formatter:on

        // @formatter:off
        @Override
        public void emitStreamTypeConversion(
                String varName, String accessor, String fieldName, String fieldType, JavaWriter writer, boolean isPrimaryKey)
                throws IOException {
            writer
                .beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                    .emitStatement("reader.skipValue()")
                    .emitStatement("%s.%s(null)", varName, accessor)
                .nextControlFlow("else if (reader.peek() == JsonToken.NUMBER)")
                    .emitStatement("long timestamp = reader.nextLong()", fieldName)
                    .beginControlFlow("if (timestamp > -1)")
                        .emitStatement("%s.%s(new Date(timestamp))", varName, accessor)
                    .endControlFlow()
                .nextControlFlow("else")
                    .emitStatement("%s.%s(JsonUtils.stringToDate(reader.nextString()))", varName, accessor)
                .endControlFlow();
        }
        // @formatter:on

        @Override
        public void emitGetObjectWithPrimaryKeyValue(
                String qualifiedRealmObjectClass, String qualifiedRealmObjectProxyClass, String fieldName, JavaWriter writer)
                throws IOException {
            throw new IllegalArgumentException("'Date' is not allowed as a primary key value.");
        }
    }

    private static class MutableRealmIntegerTypeConverter implements JsonToRealmFieldTypeConverter {
        // @formatter:off
        @Override
        public void emitTypeConversion(String varName, String accessor, String fieldName, String fieldType, JavaWriter writer)
                throws IOException {
            writer
                .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    .emitStatement("%1$s.%2$s().set((json.isNull(\"%3$s\")) ? null : json.getLong(\"%3$s\"))", varName, accessor, fieldName)
                .endControlFlow();
        }
        // @formatter:on

        // @formatter:off
        @Override
        public void emitStreamTypeConversion(String varName, String accessor, String fieldName, String fieldType, JavaWriter writer, boolean isPrimaryKey)
                throws IOException {
            writer
                .emitStatement("Long val = null")
                .beginControlFlow("if (reader.peek() != JsonToken.NULL)")
                    .emitStatement("val = reader.nextLong()")
                .nextControlFlow("else")
                    .emitStatement("reader.skipValue()")
                .endControlFlow()
                .emitStatement("%1$s.%2$s().set(val)", varName, accessor);
        }
        // @formatter:on

        @Override
        public void emitGetObjectWithPrimaryKeyValue(String qualifiedRealmObjectClass, String qualifiedRealmObjectProxyClass, String fieldName, JavaWriter writer)
                throws IOException {
            throw new IllegalArgumentException("'MutableRealmInteger' is not allowed as a primary key value.");
        }
    }

    private interface JsonToRealmFieldTypeConverter {
        void emitTypeConversion(String varName, String accessor, String fieldName, String fieldType, JavaWriter writer)
                throws IOException;

        void emitStreamTypeConversion(String varName, String accessor, String fieldName, String fieldType, JavaWriter writer, boolean isPrimaryKey)
                throws IOException;

        void emitGetObjectWithPrimaryKeyValue(String qualifiedRealmObjectClass, String qualifiedRealmObjectProxyClass, String fieldName, JavaWriter writer)
                throws IOException;
    }
}
