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

    private static final Map<String, JsonToRealmTypeConverter> JAVA_TO_JSON_TYPES;
    static {
        JAVA_TO_JSON_TYPES = new HashMap<String, JsonToRealmTypeConverter>();
        JAVA_TO_JSON_TYPES.put("byte", new SimpleTypeConverter("byte", "Int"));
        JAVA_TO_JSON_TYPES.put("short", new SimpleTypeConverter("short", "Int"));
        JAVA_TO_JSON_TYPES.put("int", new SimpleTypeConverter("int", "Int"));
        JAVA_TO_JSON_TYPES.put("long", new SimpleTypeConverter("long", "Long"));
        JAVA_TO_JSON_TYPES.put("float", new SimpleTypeConverter("float", "Double"));
        JAVA_TO_JSON_TYPES.put("double", new SimpleTypeConverter("double", "Double"));
        JAVA_TO_JSON_TYPES.put("boolean", new SimpleTypeConverter("boolean", "Boolean"));
        JAVA_TO_JSON_TYPES.put("Byte", new SimpleTypeConverter("Byte", "Int"));
        JAVA_TO_JSON_TYPES.put("Short", new SimpleTypeConverter("Short", "Int"));
        JAVA_TO_JSON_TYPES.put("Integer", new SimpleTypeConverter("Integer", "Int"));
        JAVA_TO_JSON_TYPES.put("Long", new SimpleTypeConverter("Long", "Long"));
        JAVA_TO_JSON_TYPES.put("Float", new SimpleTypeConverter("Float", "Double"));
        JAVA_TO_JSON_TYPES.put("Double", new SimpleTypeConverter("Double", "Double"));
        JAVA_TO_JSON_TYPES.put("Boolean", new SimpleTypeConverter("Boolean", "Boolean"));
        JAVA_TO_JSON_TYPES.put("java.lang.String", new SimpleTypeConverter("String", "String"));
        JAVA_TO_JSON_TYPES.put("java.util.Date", new JsonToRealmTypeConverter() {
            @Override
            public void emitTypeConversion(String setter, String fieldName, String fieldType, JavaWriter writer) throws IOException {
                writer
                    .beginControlFlow("if (!json.isNull(\"%s\"))", fieldName)
                        .emitStatement("Object timestamp = json.get(\"%s\")", fieldName)
                        .beginControlFlow("if (timestamp instanceof String)")
                           .emitStatement("obj.%s(JsonUtils.stringToDate((String) timestamp))", setter)
                        .nextControlFlow("else")
                            .emitStatement("obj.%s(new Date(json.getLong(\"%s\")))", setter, fieldName)
                        .endControlFlow()
                    .nextControlFlow("else")
                        .emitStatement("obj.%s(new Date(0))", setter)
                    .endControlFlow();
            }

            @Override
            public void emitStreamTypeConversion(String setter, String fieldName, String fieldType, JavaWriter writer) throws IOException {
                writer
                    .beginControlFlow("if (reader.peek() == JsonToken.NUMBER)")
                        .emitStatement("long timestamp = reader.nextLong()", fieldName)
                        .beginControlFlow("if (timestamp > -1)")
                            .emitStatement("obj.%s(new Date(timestamp))", setter)
                        .endControlFlow()
                    .nextControlFlow("else")
                        .emitStatement("obj.%s(JsonUtils.stringToDate(reader.nextString()))", setter)
                    .endControlFlow();
            }
        });
        JAVA_TO_JSON_TYPES.put("byte[]", new JsonToRealmTypeConverter() {
            @Override
            public void emitTypeConversion(String setter, String fieldName, String fieldType, JavaWriter writer) throws IOException {
                writer.emitStatement("obj.%s(JsonUtils.stringToBytes(json.isNull(\"%s\") ? null : json.getString(\"%s\")))", setter, fieldName, fieldName);
            }

            @Override
            public void emitStreamTypeConversion(String setter, String fieldName, String fieldType, JavaWriter writer) throws IOException {
                writer.emitStatement("obj.%s(JsonUtils.stringToBytes(reader.nextString()))", setter);
            }
        });
    }

    public static void emitFillJavaTypeWithJsonValue(String setter, String fieldName, String qualifiedFieldType, JavaWriter writer) throws IOException {
        JsonToRealmTypeConverter typeEmitter = JAVA_TO_JSON_TYPES.get(qualifiedFieldType);
        if (typeEmitter != null) {
            typeEmitter.emitTypeConversion(setter, fieldName, qualifiedFieldType, writer);
        }
    }

    public static void emitFillRealmObjectWithJsonValue(String setter, String fieldName, String qualifiedFieldType,
                                                        String proxyClass, JavaWriter writer) throws IOException {
        writer
            .beginControlFlow("if (!json.isNull(\"%s\"))", fieldName)
                .emitStatement("%s %s = standalone ? new %s() : obj.realm.createObject(%s.class)",
                        qualifiedFieldType, fieldName, qualifiedFieldType, qualifiedFieldType)
                .emitStatement("%s.populateUsingJsonObject(%s, json.getJSONObject(\"%s\"))",
                        proxyClass, fieldName, fieldName)
                .emitStatement("obj.%s(%s)", setter, fieldName)
            .endControlFlow();
    }

    public static void emitFillRealmListWithJsonValue(String getter, String setter, String fieldName,
                                                      String fieldTypeCanonicalName, String proxyClass,
                                                      JavaWriter writer) throws IOException {
        writer
            .beginControlFlow("if (!json.isNull(\"%s\"))", fieldName)
                .beginControlFlow("if (standalone)")
                    .emitStatement("obj.%s(new RealmList<%s>())", setter, fieldTypeCanonicalName)
                .endControlFlow()
                .emitStatement("JSONArray array = json.getJSONArray(\"%s\")", fieldName)
                .beginControlFlow("for (int i = 0; i < array.length(); i++)")
                    .emitStatement("%s item = standalone ? new %s() : obj.realm.createObject(%s.class)", fieldTypeCanonicalName, fieldTypeCanonicalName, fieldTypeCanonicalName)
                    .emitStatement("%s.populateUsingJsonObject(item, array.getJSONObject(i))", proxyClass)
                    .emitStatement("obj.%s().add(item)", getter)
                .endControlFlow()
            .endControlFlow();
    }


    public static void emitFillJavaTypeFromStream(String setter, String fieldName, String fieldType, JavaWriter writer) throws IOException {
        if (JAVA_TO_JSON_TYPES.containsKey(fieldType)) {
            JAVA_TO_JSON_TYPES.get(fieldType).emitStreamTypeConversion(setter, fieldName, fieldType, writer);
        }
    }

    public static void emitFillRealmObjectFromStream(String setter, String fieldName, String fieldTypeCanonicalName, String proxyClass, JavaWriter writer) throws IOException {
        writer
            .emitStatement("%s %s = standalone ? new %s() : obj.realm.createObject(%s.class)", fieldTypeCanonicalName, fieldName, fieldTypeCanonicalName, fieldTypeCanonicalName)
            .emitStatement("%s.populateUsingJsonStream(%s, reader)", proxyClass, fieldName)
            .emitStatement("obj.%s(%s)", setter, fieldName);
    }

    public static void emitFillRealmListFromStream(String getter, String setter, String fieldTypeCanonicalName, String proxyClass, JavaWriter writer) throws IOException {
        writer
            .emitStatement("reader.beginArray()")
            .beginControlFlow("if (standalone)")
                .emitStatement("obj.%s(new RealmList<%s>())", setter, fieldTypeCanonicalName)
            .endControlFlow()
            .beginControlFlow("while (reader.hasNext())")
                .emitStatement("%s item = standalone ? new %s() : obj.realm.createObject(%s.class)", fieldTypeCanonicalName, fieldTypeCanonicalName, fieldTypeCanonicalName)
                .emitStatement("%s.populateUsingJsonStream(item, reader)", proxyClass)
                .emitStatement("obj.%s().add(item)", getter)
            .endControlFlow()
            .emitStatement("reader.endArray()");
    }

    private static class SimpleTypeConverter implements JsonToRealmTypeConverter {

        private final String castType;
        private final String jsonType;

        /**
         * Create a conversion between simple types which can be expressed as
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
        public void emitTypeConversion(String setter, String fieldName, String fieldType, JavaWriter writer) throws IOException {
            writer
                .beginControlFlow("if (!json.isNull(\"%s\"))", fieldName)
                    .emitStatement("obj.%s((%s) json.get%s(\"%s\"))",
                        setter,
                        castType,
                        jsonType,
                        fieldName)
                .endControlFlow();
        }

        @Override
        public void emitStreamTypeConversion(String setter, String fieldName, String fieldType, JavaWriter writer) throws IOException {
            writer.emitStatement("obj.%s((%s) reader.next%s())",
                    setter,
                    castType,
                    jsonType);
        }
    }

    private interface JsonToRealmTypeConverter {
        public void emitTypeConversion(String setter, String fieldName, String fieldType, JavaWriter writer) throws IOException;
        public void emitStreamTypeConversion(String setter, String fieldName, String fieldType, JavaWriter writer) throws IOException;
    }
}
