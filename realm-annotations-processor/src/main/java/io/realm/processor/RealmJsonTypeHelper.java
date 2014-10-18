package io.realm.processor;

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.HashMap;

/**
 * Helper class for converting between Json types and data types in Java that are supported by Realm.
 */
public class RealmJsonTypeHelper {

    private static final HashMap<String, JsonToRealmTypeConverter> JAVA_TO_JSON_TYPES;

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
            public void emitTypeConversion(String fieldName, String fieldType, JavaWriter writer) throws IOException {
                writer
                    .emitStatement("long timestamp = json.optLong(\"%s\", -1)", fieldName)
                    .beginControlFlow("if (timestamp > -1)")
                        .emitStatement("set%s(new Date(timestamp))", capitaliseFirstChar(fieldName))
                    .nextControlFlow("else")
                        .emitStatement("String jsonDate = json.getString(\"%s\")", fieldName)
                        .emitStatement("set%s(JsonUtils.stringToDate(jsonDate))", capitaliseFirstChar(fieldName))
                    .endControlFlow();
            }

            @Override
            public void emitStreamTypeConversion(String fieldName, String fieldType, JavaWriter writer) throws IOException {
                writer
                    .beginControlFlow("if (reader.peek() == JsonToken.NUMBER)")
                        .emitStatement("long timestamp = reader.nextLong()", fieldName)
                        .beginControlFlow("if (timestamp > -1)")
                            .emitStatement("set%s(new Date(timestamp))", capitaliseFirstChar(fieldName))
                        .endControlFlow()
                    .nextControlFlow("else")
                        .emitStatement("set%s(JsonUtils.stringToDate(reader.nextString()))", capitaliseFirstChar(fieldName))
                    .endControlFlow();
            }
        });

        JAVA_TO_JSON_TYPES.put("byte[]", new JsonToRealmTypeConverter() {
            @Override
            public void emitTypeConversion(String fieldName, String fieldType, JavaWriter writer) throws IOException {
                writer.emitStatement("set%s(JsonUtils.stringToBytes(json.getString(\"%s\")))", capitaliseFirstChar(fieldName), fieldName);
            }

            @Override
            public void emitStreamTypeConversion(String fieldName, String fieldType, JavaWriter writer) throws IOException {
                writer.emitStatement("set%s(JsonUtils.stringToBytes(reader.nextString()))", capitaliseFirstChar(fieldName));
            }
        });
    }

    public static void emitFillJavaTypeWithJsonValue(String fieldName, String fieldType, JavaWriter writer) throws IOException {
        if (JAVA_TO_JSON_TYPES.containsKey(fieldType)) {
            writer.beginControlFlow("if (json.has(\"%s\"))", fieldName);
            JAVA_TO_JSON_TYPES.get(fieldType).emitTypeConversion(fieldName, fieldType, writer);
            writer.endControlFlow();
        }
    }

    public static void emitFillRealmObjectWithJsonValue(String fieldName, String fieldTypeCanonicalName, JavaWriter writer) throws IOException {
        writer
            .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                .emitStatement("%s obj = getRealm().createObject(%s.class)", fieldTypeCanonicalName, fieldTypeCanonicalName)
                .emitStatement("obj.populateUsingJsonObject(json.getJSONObject(\"%s\"))", fieldName)
                .emitStatement("set%s(obj)", capitaliseFirstChar(fieldName))
            .endControlFlow();
    }

    public static void emitFillRealmListWithJsonValue(String fieldName, String fieldTypeCanonicalName, JavaWriter writer) throws IOException {
        writer
            .beginControlFlow("if (json.has(\"%s\"))", fieldName)
                .emitStatement("JSONArray array = json.getJSONArray(\"%s\")", fieldName)
                .beginControlFlow("for (int i = 0; i < array.length(); i++)")
                    .emitStatement("%s obj = getRealm().createObject(%s.class)", fieldTypeCanonicalName, fieldTypeCanonicalName)
                    .emitStatement("obj.populateUsingJsonObject(array.getJSONObject(i))")
                    .emitStatement("get%s().add(obj)", capitaliseFirstChar(fieldName))
                .endControlFlow()
            .endControlFlow();
    }


    public static void emitFillJavaTypeFromStream(String fieldName, String fieldType, JavaWriter writer) throws IOException {
        if (JAVA_TO_JSON_TYPES.containsKey(fieldType)) {
            JAVA_TO_JSON_TYPES.get(fieldType).emitStreamTypeConversion(fieldName, fieldType, writer);
        }
    }

    public static void emitFillRealmObjectFromStream(String fieldName, String fieldTypeCanonicalName, JavaWriter writer) throws IOException {
//        writer
//                .beginControlFlow("if (json.has(\"%s\"))", fieldName)
//                .emitStatement("%s obj = getRealm().createObject(%s.class)", fieldTypeCanonicalName, fieldTypeCanonicalName)
//                .emitStatement("obj.populateUsingJsonObject(json.getJSONObject(\"%s\"))", fieldName)
//                .emitStatement("set%s(obj)", capitaliseFirstChar(fieldName))
//                .endControlFlow();
    }

    public static void emitFillRealmListFromStream(String fieldName, String fieldTypeCanonicalName, JavaWriter writer) throws IOException {
//        writer
//                .beginControlFlow("if (json.has(\"%s\"))", fieldName)
//                .emitStatement("JSONArray array = json.getJSONArray(\"%s\")", fieldName)
//                .beginControlFlow("for (int i = 0; i < array.length(); i++)")
//                .emitStatement("%s obj = getRealm().createObject(%s.class)", fieldTypeCanonicalName, fieldTypeCanonicalName)
//                .emitStatement("obj.populateUsingJsonObject(array.getJSONObject(i))")
//                .emitStatement("get%s().add(obj)", capitaliseFirstChar(fieldName))
//                .endControlFlow()
//                .endControlFlow();
    }





    private static String capitaliseFirstChar(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    static class SimpleTypeConverter implements JsonToRealmTypeConverter {

        private final String castType;
        private final String jsonType;

        /**
         * Create a conversion between simple types that can be expressed of the form
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
        public void emitTypeConversion(String fieldName, String fieldType, JavaWriter writer) throws IOException {
            writer.emitStatement("set%s((%s) json.get%s(\"%s\"))",
                    capitaliseFirstChar(fieldName),
                    castType,
                    jsonType,
                    fieldName);
        }

        @Override
        public void emitStreamTypeConversion(String fieldName, String fieldType, JavaWriter writer) throws IOException {
            writer.emitStatement("set%s((%s) reader.next%s())",
                    capitaliseFirstChar(fieldName),
                    castType,
                    jsonType);
        }
    }

    private interface JsonToRealmTypeConverter {
        public void emitTypeConversion(String fieldName, String fieldType, JavaWriter writer) throws IOException;
        public void emitStreamTypeConversion(String fieldName, String fieldType, JavaWriter writer) throws IOException;
    }
}
