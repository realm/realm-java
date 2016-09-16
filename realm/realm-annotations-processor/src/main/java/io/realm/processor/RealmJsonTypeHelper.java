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
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for converting between Json types and data types in Java that are supported by Realm.
 */
public class RealmJsonTypeHelper {
    private static final Map<String, JsonToRealmFieldTypeConverter> JAVA_TO_JSON_TYPES;
    static {
        JAVA_TO_JSON_TYPES = new HashMap<String, JsonToRealmFieldTypeConverter>();
        JAVA_TO_JSON_TYPES.put("byte", new SimpleTypeConverter("byte", "Int"));
        JAVA_TO_JSON_TYPES.put("short", new SimpleTypeConverter("short", "Int"));
        JAVA_TO_JSON_TYPES.put("int", new SimpleTypeConverter("int", "Int"));
        JAVA_TO_JSON_TYPES.put("long", new SimpleTypeConverter("long", "Long"));
        JAVA_TO_JSON_TYPES.put("float", new SimpleTypeConverter("float", "Double"));
        JAVA_TO_JSON_TYPES.put("double", new SimpleTypeConverter("double", "Double"));
        JAVA_TO_JSON_TYPES.put("boolean", new SimpleTypeConverter("boolean", "Boolean"));
        JAVA_TO_JSON_TYPES.put("java.lang.Byte", new SimpleTypeConverter("byte", "Int"));
        JAVA_TO_JSON_TYPES.put("java.lang.Short", new SimpleTypeConverter("short", "Int"));
        JAVA_TO_JSON_TYPES.put("java.lang.Integer", new SimpleTypeConverter("int", "Int"));
        JAVA_TO_JSON_TYPES.put("java.lang.Long", new SimpleTypeConverter("long", "Long"));
        JAVA_TO_JSON_TYPES.put("java.lang.Float", new SimpleTypeConverter("float", "Double"));
        JAVA_TO_JSON_TYPES.put("java.lang.Double", new SimpleTypeConverter("double", "Double"));
        JAVA_TO_JSON_TYPES.put("java.lang.Boolean", new SimpleTypeConverter("boolean", "Boolean"));
        JAVA_TO_JSON_TYPES.put("java.lang.String", new SimpleTypeConverter("String", "String"));
        JAVA_TO_JSON_TYPES.put("java.util.Date", new JsonToRealmFieldTypeConverter() {
            @Override
            public void emitTypeConversion(String interfaceName, String setter, String fieldName, String fieldType,
                                           JavaWriter writer)
                    throws IOException {
                writer
                    .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                        .beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                            .emitStatement("((%s) obj).%s(null)", interfaceName, setter)
                        .nextControlFlow("else")
                            .emitStatement("Object timestamp = json.get(\"%s\")", fieldName)
                            .beginControlFlow("if (timestamp instanceof String)")
                               .emitStatement("((%s) obj).%s(JsonUtils.stringToDate((String) timestamp))",
                                       interfaceName, setter)
                            .nextControlFlow("else")
                                .emitStatement("((%s) obj).%s(new Date(json.getLong(\"%s\")))", interfaceName, setter,
                                        fieldName)
                            .endControlFlow()
                        .endControlFlow()
                    .endControlFlow();
            }

            @Override
            public void emitStreamTypeConversion(String interfaceName, String setter, String fieldName, String
                    fieldType, JavaWriter writer, boolean isPrimaryKey)
                    throws IOException {
                writer
                    .beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                        .emitStatement("reader.skipValue()")
                        .emitStatement("((%s) obj).%s(null)", interfaceName, setter)
                    .nextControlFlow("else if (reader.peek() == JsonToken.NUMBER)")
                        .emitStatement("long timestamp = reader.nextLong()", fieldName)
                        .beginControlFlow("if (timestamp > -1)")
                            .emitStatement("((%s) obj).%s(new Date(timestamp))", interfaceName, setter)
                        .endControlFlow()
                    .nextControlFlow("else")
                        .emitStatement("((%s) obj).%s(JsonUtils.stringToDate(reader.nextString()))", interfaceName,
                                setter)
                    .endControlFlow();
            }

            @Override
            public void emitGetObjectWithPrimaryKeyValue(String qualifiedRealmObjectClass,
                                                         String qualifiedRealmObjectProxyClass,
                                                         String fieldName, JavaWriter writer) throws IOException {
                throw new IllegalArgumentException("'Date' is not allowed as a primary key value.");
            }
        });
        JAVA_TO_JSON_TYPES.put("byte[]", new JsonToRealmFieldTypeConverter() {
            @Override
            public void emitTypeConversion(String interfaceName, String setter, String fieldName, String fieldType,
                                           JavaWriter writer)
                    throws IOException {
                writer
                    .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                        .beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                            .emitStatement("((%s) obj).%s(null)", interfaceName, setter)
                        .nextControlFlow("else")
                            .emitStatement("((%s) obj).%s(JsonUtils.stringToBytes(json.getString(\"%s\")))",
                                    interfaceName, setter, fieldName)
                        .endControlFlow()
                    .endControlFlow();
            }

            @Override
            public void emitStreamTypeConversion(String interfaceName, String setter, String fieldName, String
                    fieldType, JavaWriter writer, boolean isPrimaryKey)
                    throws IOException {
                writer
                    .beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                        .emitStatement("reader.skipValue()")
                        .emitStatement("((%s) obj).%s(null)", interfaceName, setter)
                    .nextControlFlow("else")
                        .emitStatement("((%s) obj).%s(JsonUtils.stringToBytes(reader.nextString()))", interfaceName,
                                setter)
                    .endControlFlow();
            }

            @Override
            public void emitGetObjectWithPrimaryKeyValue(String qualifiedRealmObjectClass,
                                                         String qualifiedRealmObjectProxyClass,
                                                         String fieldName, JavaWriter writer) throws IOException {
                throw new IllegalArgumentException("'byte[]' is not allowed as a primary key value.");
            }
        });
    }

    public static void emitCreateObjectWithPrimaryKeyValue(String qualifiedRealmObjectClass,
                                                           String qualifiedRealmObjectProxyClass,
                                                           String qualifiedFieldType,
                                                           String fieldName, JavaWriter writer) throws IOException {
        JsonToRealmFieldTypeConverter typeEmitter = JAVA_TO_JSON_TYPES.get(qualifiedFieldType);
        if (typeEmitter != null) {
            typeEmitter.emitGetObjectWithPrimaryKeyValue(qualifiedRealmObjectClass, qualifiedRealmObjectProxyClass, fieldName, writer);
        }
    }

    public static void emitFillJavaTypeWithJsonValue(String interfaceName, String setter, String fieldName, String
            qualifiedFieldType,
                                                     JavaWriter writer) throws IOException {
        JsonToRealmFieldTypeConverter typeEmitter = JAVA_TO_JSON_TYPES.get(qualifiedFieldType);
        if (typeEmitter != null) {
            typeEmitter.emitTypeConversion(interfaceName, setter, fieldName, qualifiedFieldType, writer);
        }
    }

    public static void emitFillRealmObjectWithJsonValue(String interfaceName, String setter, String fieldName, String
            qualifiedFieldType, String proxyClass, JavaWriter writer) throws IOException {
        writer
            .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                .beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                    .emitStatement("((%s) obj).%s(null)", interfaceName, setter)
                .nextControlFlow("else")
                    .emitStatement("%s %sObj = %s.createOrUpdateUsingJsonObject(realm, json.getJSONObject(\"%s\"), update)",
                            qualifiedFieldType, fieldName, proxyClass, fieldName)
                    .emitStatement("((%s) obj).%s(%sObj)", interfaceName, setter, fieldName)
                .endControlFlow()
            .endControlFlow();
    }

    public static void emitFillRealmListWithJsonValue(String interfaceName, String getter, String setter, String
            fieldName, String fieldTypeCanonicalName, String proxyClass, JavaWriter writer) throws IOException {
        writer
            .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                .beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                    .emitStatement("((%s) obj).%s(null)", interfaceName, setter)
                .nextControlFlow("else")
                    .emitStatement("((%s) obj).%s().clear()", interfaceName, getter)
                    .emitStatement("JSONArray array = json.getJSONArray(\"%s\")", fieldName)
                    .beginControlFlow("for (int i = 0; i < array.length(); i++)")
                        .emitStatement("%s item = %s.createOrUpdateUsingJsonObject(realm, array.getJSONObject(i), update)",
                                fieldTypeCanonicalName, proxyClass, fieldTypeCanonicalName)
                        .emitStatement("((%s) obj).%s().add(item)", interfaceName, getter)
                    .endControlFlow()
                .endControlFlow()
            .endControlFlow();
    }


    public static void emitFillJavaTypeFromStream(String interfaceName, ClassMetaData metaData, String fieldName, String
            fieldType, JavaWriter writer) throws IOException {
        String setter = metaData.getSetter(fieldName);
        boolean isPrimaryKey = false;
        if (metaData.hasPrimaryKey() && metaData.getPrimaryKey().getSimpleName().toString().equals(fieldName)) {
            isPrimaryKey = true;
        }
        if (JAVA_TO_JSON_TYPES.containsKey(fieldType)) {
            JAVA_TO_JSON_TYPES.get(fieldType).emitStreamTypeConversion(interfaceName, setter, fieldName, fieldType,
                    writer, isPrimaryKey);
        }
    }

    public static void emitFillRealmObjectFromStream(String interfaceName, String setter, String fieldName, String
            fieldTypeCanonicalName, String proxyClass, JavaWriter writer) throws IOException {
        writer
            .beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                .emitStatement("reader.skipValue()")
                .emitStatement("((%s) obj).%s(null)", interfaceName, setter)
            .nextControlFlow("else")
                .emitStatement("%s %sObj = %s.createUsingJsonStream(realm, reader)", fieldTypeCanonicalName, fieldName,
                        proxyClass)
                .emitStatement("((%s) obj).%s(%sObj)", interfaceName, setter, fieldName)
            .endControlFlow();
    }

    public static void emitFillRealmListFromStream(String interfaceName, String getter, String setter, String
            fieldTypeCanonicalName, String proxyClass, JavaWriter writer) throws IOException {
        writer
            .beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                .emitStatement("reader.skipValue()")
                .emitStatement("((%s) obj).%s(null)", interfaceName, setter)
            .nextControlFlow("else")
                .emitStatement("((%s) obj).%s(new RealmList<%s>())", interfaceName, setter, fieldTypeCanonicalName)
                .emitStatement("reader.beginArray()")
                .beginControlFlow("while (reader.hasNext())")
                    .emitStatement("%s item = %s.createUsingJsonStream(realm, reader)", fieldTypeCanonicalName, proxyClass)
                    .emitStatement("((%s) obj).%s().add(item)", interfaceName, getter)
                .endControlFlow()
                .emitStatement("reader.endArray()")
            .endControlFlow();
    }

    private static class SimpleTypeConverter implements JsonToRealmFieldTypeConverter {

        private final String castType;
        private final String jsonType;

        /**
         * Creates a conversion between simple types which can be expressed as
         * RealmObject.setFieldName((<castType>) json.get<jsonType>) or
         * RealmObject.setFieldName((<castType>) reader.next<jsonType>
         *
         * @param castType  Java type to cast to.
         * @param jsonType  JsonType to get data from.
         */
        private SimpleTypeConverter(String castType, String jsonType) {
            this.castType = castType;
            this.jsonType = jsonType;
        }

        @Override
        public void emitTypeConversion(String interfaceName, String setter, String fieldName, String fieldType,
                                       JavaWriter
                writer)
                throws IOException {
            String statementSetNullOrThrow;
            if (Utils.isPrimitiveType(fieldType)) {
                // Only throw exception for primitive types. For boxed types and String, exception will be thrown in
                // the setter.
                statementSetNullOrThrow = String.format(Constants.STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE, fieldName);
            } else {
                statementSetNullOrThrow = String.format("((%s) obj).%s(null)", interfaceName, setter);
            }
            writer
                .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    .beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        .emitStatement(statementSetNullOrThrow)
                    .nextControlFlow("else")
                        .emitStatement("((%s) obj).%s((%s) json.get%s(\"%s\"))", interfaceName, setter, castType,
                                jsonType, fieldName)
                    .endControlFlow()
                .endControlFlow();
        }

        @Override
        public void emitStreamTypeConversion(String interfaceName, String setter, String fieldName, String fieldType,
                                             JavaWriter writer, boolean isPrimaryKey)
                throws IOException {
            String statementSetNullOrThrow;
            if (Utils.isPrimitiveType(fieldType)) {
                // Only throw exception for primitive types. For boxed types and String, exception will be thrown in
                // the setter.
                statementSetNullOrThrow = String.format(Constants.STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE, fieldName);
            } else {
                statementSetNullOrThrow = String.format("((%s) obj).%s(null)", interfaceName, setter);
            }
            writer
                .beginControlFlow("if (reader.peek() == JsonToken.NULL)")
                    .emitStatement("reader.skipValue()")
                    .emitStatement(statementSetNullOrThrow)
                .nextControlFlow("else")
                    .emitStatement("((%s) obj).%s((%s) reader.next%s())", interfaceName, setter, castType, jsonType)
                .endControlFlow();
            if (isPrimaryKey) {
                writer.emitStatement("jsonHasPrimaryKey = true");
            }
        }

        @Override
        public void emitGetObjectWithPrimaryKeyValue(String qualifiedRealmObjectClass,
                                                     String qualifiedRealmObjectProxyClass,
                                                     String fieldName, JavaWriter writer) throws IOException {
            // No error checking is done here for valid primary key types. This should be done by the annotation
            // processor
            writer
                .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                    .beginControlFlow("if (json.isNull(\"%s\"))", fieldName)
                        .emitStatement("obj = (%1$s) realm.createObjectInternal(%2$s.class, null, true, excludeFields)",
                                qualifiedRealmObjectProxyClass, qualifiedRealmObjectClass)
                    .nextControlFlow("else")
                        .emitStatement("obj = (%1$s) realm.createObjectInternal(%2$s.class, json.get%3$s(\"%4$s\"), true, excludeFields)",
                                qualifiedRealmObjectProxyClass, qualifiedRealmObjectClass, jsonType, fieldName)
                    .endControlFlow()
                .nextControlFlow("else")
                    .emitStatement(Constants.STATEMENT_EXCEPTION_NO_PRIMARY_KEY_IN_JSON, fieldName)
                    .endControlFlow();
        }
    }

    private interface JsonToRealmFieldTypeConverter {
        void emitTypeConversion(String interfaceName, String setter, String fieldName, String fieldType, JavaWriter
                writer) throws IOException;
        void emitStreamTypeConversion(String interfaceName, String setter, String fieldName, String fieldType,
                                      JavaWriter writer, boolean isPrimaryKey) throws IOException;
        void emitGetObjectWithPrimaryKeyValue(String qualifiedRealmObjectClass,
                                              String qualifiedRealmObjectProxyClass,
                                              String fieldName, JavaWriter writer) throws IOException;
    }
}
