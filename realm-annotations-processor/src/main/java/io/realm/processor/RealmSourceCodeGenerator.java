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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;


public class RealmSourceCodeGenerator {
    private ProcessingEnvironment processingEnvironment;
    private String className;
    private String packageName;
    private List<VariableElement> fields;

    public RealmSourceCodeGenerator(ProcessingEnvironment processingEnvironment, String className, String packageName, List<VariableElement> fields) {
        this.processingEnvironment = processingEnvironment;
        this.className = className;
        this.packageName = packageName;
        this.fields = fields;
    }

    private static final Map<String, String> JAVA_TO_REALM_TYPES;

    static {
        JAVA_TO_REALM_TYPES = new HashMap<String, String>();
        JAVA_TO_REALM_TYPES.put("byte", "Long");
        JAVA_TO_REALM_TYPES.put("short", "Long");
        JAVA_TO_REALM_TYPES.put("int", "Long");
        JAVA_TO_REALM_TYPES.put("long", "Long");
        JAVA_TO_REALM_TYPES.put("float", "Float");
        JAVA_TO_REALM_TYPES.put("double", "Double");
        JAVA_TO_REALM_TYPES.put("boolean", "Boolean");
        JAVA_TO_REALM_TYPES.put("Byte", "Long");
        JAVA_TO_REALM_TYPES.put("Short", "Long");
        JAVA_TO_REALM_TYPES.put("Integer", "Long");
        JAVA_TO_REALM_TYPES.put("Long", "Long");
        JAVA_TO_REALM_TYPES.put("Float", "Float");
        JAVA_TO_REALM_TYPES.put("Double", "Double");
        JAVA_TO_REALM_TYPES.put("Boolean", "Boolean");
        JAVA_TO_REALM_TYPES.put("java.lang.String", "String");
        JAVA_TO_REALM_TYPES.put("byte[]", "BinaryByteArray");
        // TODO: add support for char and Char
    }

    private static final Map<String, String> JAVA_TO_COLUMN_TYPES;

    static {
        JAVA_TO_COLUMN_TYPES = new HashMap<String, String>();
        JAVA_TO_COLUMN_TYPES.put("byte", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("short", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("int", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("long", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("float", "ColumnType.FLOAT");
        JAVA_TO_COLUMN_TYPES.put("double", "ColumnType.DOUBLE");
        JAVA_TO_COLUMN_TYPES.put("boolean", "ColumnType.BOOLEAN");
        JAVA_TO_COLUMN_TYPES.put("Byte", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("Short", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("Integer", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("Long", "ColumnType.INTEGER");
        JAVA_TO_COLUMN_TYPES.put("Float", "ColumnType.FLOAT");
        JAVA_TO_COLUMN_TYPES.put("Double", "ColumnType.DOUBLE");
        JAVA_TO_COLUMN_TYPES.put("Boolean", "ColumnType.BOOLEAN");
        JAVA_TO_COLUMN_TYPES.put("java.lang.String", "ColumnType.STRING");
        JAVA_TO_COLUMN_TYPES.put("byte[]", "ColumnType.BINARY");
    }

    private static final Map<String, String> CASTING_TYPES;

    static {
        CASTING_TYPES = new HashMap<String, String>();
        CASTING_TYPES.put("byte", "long");
        CASTING_TYPES.put("short", "long");
        CASTING_TYPES.put("int", "long");
        CASTING_TYPES.put("long", "long");
        CASTING_TYPES.put("float", "float");
        CASTING_TYPES.put("double", "double");
        CASTING_TYPES.put("boolean", "boolean");
        CASTING_TYPES.put("Byte", "long");
        CASTING_TYPES.put("Short", "long");
        CASTING_TYPES.put("Integer", "long");
        CASTING_TYPES.put("Long", "long");
        CASTING_TYPES.put("Float", "float");
        CASTING_TYPES.put("Double", "double");
        CASTING_TYPES.put("Boolean", "boolean");
        CASTING_TYPES.put("java.lang.String", "String");
        CASTING_TYPES.put("byte[]", "byte[]");
    }

    private static final Map<String, String[]> HASHCODE;

    static {
        HASHCODE = new HashMap<String, String[]>();
        HASHCODE.put("byte", new String[] {
                "result = 31 * result + (int) get%s()" });
        HASHCODE.put("short", new String[] {
                "result = 31 * result + (int) get%s()" });
        HASHCODE.put("int", new String[] {
                "result = 31 * result + get%s()" });
        HASHCODE.put("long", new String[] {
                "long aLong_%d = get%s()",
                "result = 31 * result + (int) (aLong_%d ^ (aLong_%d >>> 32))" });
        HASHCODE.put("float", new String[] {
                "float aFloat_%d = get%s()",
                "result = 31 * result + (aFloat_%d != +0.0f ? Float.floatToIntBits(aFloat_%d) : 0)" });
        HASHCODE.put("double", new String[] {
                "long temp_%d = Double.doubleToLongBits(get%s())",
                "result = 31 * result + (int) (temp_%d ^ (temp_%d >>> 32))" });
        HASHCODE.put("boolean", new String[] {
                "result = 31 * result + (get%s() ? 1 : 0)" });
        HASHCODE.put("Byte", new String[] {
                "result = 31 * result + (int) get%s()" });
        HASHCODE.put("Short", new String[] {
                "result = 31 * result + (int) get%s()" });
        HASHCODE.put("Integer", new String[] {
                "result = 31 * result + get%s()" });
        HASHCODE.put("Long", new String[] {
                "long aLong_%d = get%s()",
                "result = 31 * result + (int) (aLong_%d ^ (aLong_%d >>> 32))" });
        HASHCODE.put("Float", new String[] {
                "float aFloat_%d = get%s()",
                "result = 31 * result + (aFloat_%d != +0.0f ? Float.floatToIntBits(aFloat_%d) : 0)" });
        HASHCODE.put("Double", new String[] {
                "long temp_%d = Double.doubleToLongBits(get%s())",
                "result = 31 * result + (int) (temp_%d ^ (temp_%d >>> 32))" });
        HASHCODE.put("Boolean", new String[] {
                "result = 31 * result + (get%s() ? 1 : 0)" });
        HASHCODE.put("java.lang.String", new String[] {
                "String aString_%d = get%s()",
                "result = 31 * result + (aString_%d != null ? aString_%d.hashCode() : 0)" });
        HASHCODE.put("byte[]", new String[] {
                "byte[] aByteArray_%d = get%s()",
                "result = 31 * result + (aByteArray_%d != null ? Arrays.hashCode(aByteArray_%d) : 0)" });
    }

    public void generate() throws IOException, UnsupportedOperationException {
        String qualifiedGeneratedClassName = String.format("%s.%sRealmProxy", packageName, className);
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));

        Elements elementUtils = processingEnvironment.getElementUtils();
        Types typeUtils = processingEnvironment.getTypeUtils();

        TypeMirror realmObject = elementUtils.getTypeElement("io.realm.RealmObject").asType();
        DeclaredType realmList = typeUtils.getDeclaredType(elementUtils.getTypeElement("io.realm.RealmList"), typeUtils.getWildcardType(null, null));

        // Set source code indent to 4 spaces
        writer.setIndent("    ");

        writer.emitPackage(packageName)
                .emitEmptyLine();

        writer.emitImports(
                "io.realm.internal.ColumnType",
                "io.realm.internal.Table",
                "io.realm.internal.ImplicitTransaction",
                "io.realm.internal.Row",
                "io.realm.internal.LinkView",
                "io.realm.RealmList",
                "io.realm.RealmObject")
                .emitEmptyLine();

        // Begin the class definition
        writer.beginType(
                qualifiedGeneratedClassName, // full qualified name of the item to generate
                "class",                     // the type of the item
                EnumSet.of(Modifier.PUBLIC), // modifiers to apply
                className)                   // class to extend
                .emitEmptyLine();

        // Accessors
        ListIterator<VariableElement> iterator = fields.listIterator();
        while (iterator.hasNext()) {
            int columnNumber = iterator.nextIndex();
            VariableElement field = iterator.next();

            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();

            if (JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                /**
                 * Primitives and boxed types
                 */
                String realmType = JAVA_TO_REALM_TYPES.get(fieldTypeCanonicalName);
                String castingType = CASTING_TYPES.get(fieldTypeCanonicalName);

                // Getter
                writer.emitAnnotation("Override");
                String getterPrefix = fieldTypeCanonicalName.equals("boolean")?"is":"get";
                writer.beginMethod(fieldTypeCanonicalName, getterPrefix + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.emitStatement(
                        "return (%s) realmGetRow().get%s(%d)",
                        fieldTypeCanonicalName, realmType, columnNumber);
                writer.endMethod();
                writer.emitEmptyLine();

                // Setter
                writer.emitAnnotation("Override");
                writer.beginMethod("void", "set" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.emitStatement(
                        "realmGetRow().set%s(%d, (%s) value)",
                        realmType, columnNumber, castingType);
                writer.endMethod();
            } else if (typeUtils.isAssignable(field.asType(), realmObject)) {
                /**
                 * Links
                 */

                // Getter
                writer.emitAnnotation("Override");
                writer.beginMethod(fieldTypeCanonicalName, "get" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.beginControlFlow("if (realmGetRow().isNullLink(%d))", columnNumber);
                writer.emitStatement("return null");
                writer.endControlFlow();
                writer.emitStatement(
                        "return realm.get(%s.class, realmGetRow().getLink(%d))",
                        fieldTypeCanonicalName, columnNumber);
                writer.endMethod();
                writer.emitEmptyLine();

                // Setter
                writer.emitAnnotation("Override");
                writer.beginMethod("void", "set" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.beginControlFlow("if (value == null)");
                writer.emitStatement("realmGetRow().nullifyLink(%d)", columnNumber);
                writer.endControlFlow();
                writer.emitStatement("realmGetRow().setLink(%d, value.realmGetRow().getIndex())", columnNumber);
                writer.endMethod();
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                /**
                 * LinkLists
                 */
                String genericCanonicalType = ((DeclaredType) field.asType()).getTypeArguments().get(0).toString();
                String genericType;
                if (genericCanonicalType.contains(".")) {
                    genericType = genericCanonicalType.substring(genericCanonicalType.lastIndexOf('.') + 1);
                } else {
                    genericType = genericCanonicalType;
                }

                // Getter
                writer.emitAnnotation("Override");
                writer.beginMethod(fieldTypeCanonicalName, "get" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.emitStatement(
                        "return new RealmList(%s.class, realmGetRow().getLinkList(%d), realm)",
                        genericType, columnNumber);
                writer.endMethod();
                writer.emitEmptyLine();

                // Setter
                writer.emitAnnotation("Override");
                writer.beginMethod("void", "set" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.emitStatement("LinkView links = realmGetRow().getLinkList(%d)", columnNumber);
                writer.beginControlFlow("if (value == null)");
                writer.emitStatement("return"); // TODO: delete all the links instead
                writer.endControlFlow();
                writer.beginControlFlow("for (RealmObject linkedObject : (RealmList<? extends RealmObject>) value)");
                writer.emitStatement("links.add(linkedObject.realmGetRow().getIndex())");
                writer.endControlFlow();
                writer.endMethod();
            } else {
                throw new UnsupportedOperationException(
                        String.format("Type %s of field %s is not supported", fieldTypeCanonicalName, fieldName));
            }
            writer.emitEmptyLine();
        }

        /**
         * initTable method
         */
        writer.beginMethod(
                "Table", // Return type
                "initTable", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "ImplicitTransaction", "transaction"); // Argument type & argument name

        writer.beginControlFlow("if(!transaction.hasTable(\"" + this.className + "\"))");
        writer.emitStatement("Table table = transaction.getTable(\"%s\")", this.className);

        // For each field generate corresponding table index constant
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            String fieldTypeName;
            if (fieldTypeCanonicalName.contains(".")) {
                fieldTypeName = fieldTypeCanonicalName.substring(fieldTypeCanonicalName.lastIndexOf('.') + 1);
            } else {
                fieldTypeName = fieldTypeCanonicalName;
            }

            if (JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                writer.emitStatement("table.addColumn(%s, \"%s\")",
                        JAVA_TO_COLUMN_TYPES.get(fieldTypeCanonicalName),
                        fieldName.toLowerCase(Locale.getDefault()));
            } else if (typeUtils.isAssignable(field.asType(), realmObject)) {
                writer.beginControlFlow("if (!transaction.hasTable(\"%s\"))", fieldTypeName);
                writer.emitStatement("%sRealmProxy.initTable(transaction)", fieldTypeCanonicalName);
                writer.endControlFlow();
                writer.emitStatement("table.addColumnLink(ColumnType.LINK, \"%s\", transaction.getTable(\"%s\"))",
                        fieldName.toLowerCase(Locale.getDefault()), fieldTypeName);
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                String genericCanonicalType = ((DeclaredType) field.asType()).getTypeArguments().get(0).toString();
                String genericType;
                if (genericCanonicalType.contains(".")) {
                    genericType = genericCanonicalType.substring(genericCanonicalType.lastIndexOf('.') + 1);
                } else {
                    genericType = genericCanonicalType;
                }
                writer.beginControlFlow("if (!transaction.hasTable(\"%s\"))", genericType);
                writer.emitStatement("%sRealmProxy.initTable(transaction)", genericCanonicalType);
                writer.endControlFlow();
                writer.emitStatement("table.addColumnLink(ColumnType.LINK_LIST, \"%s\", transaction.getTable(\"%s\"))",
                        fieldName.toLowerCase(Locale.getDefault()), genericType);
            }
        }
        writer.emitStatement("return table");
        writer.endControlFlow();
        writer.emitStatement("return transaction.getTable(\"%s\")", this.className);
        writer.endMethod();
        writer.emitEmptyLine();

        /**
         * toString method
         */
        writer.emitAnnotation("Override");
        writer.beginMethod("String", "toString", EnumSet.of(Modifier.PUBLIC));
        writer.emitStatement("StringBuilder stringBuilder = new StringBuilder(\"%s = [\")", className);
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            String getterPrefix = fieldTypeCanonicalName.equals("boolean")?"is":"get";
            writer.emitStatement("stringBuilder.append(\"{%s:\")", fieldName);
            writer.emitStatement("stringBuilder.append(%s%s())", getterPrefix, capitaliseFirstChar(fieldName));
            writer.emitStatement("stringBuilder.append(\"} \")", fieldName);
        }
        writer.emitStatement("stringBuilder.append(\"]\")");
        writer.emitStatement("return stringBuilder.toString()");
        writer.endMethod();
        writer.emitEmptyLine();

        /**
         * hashCode method
         */
        writer.emitAnnotation("Override");
        writer.beginMethod("int", "hashCode", EnumSet.of(Modifier.PUBLIC));
        writer.emitStatement("int result = 17");
        int counter = 0;
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            if (HASHCODE.containsKey(fieldTypeCanonicalName)) {
                for (String statement : HASHCODE.get(fieldTypeCanonicalName)) {
                    if (statement.contains("%d") && statement.contains("%s")) {
                        // This statement introduces a temporary variable
                        writer.emitStatement(statement, counter, capitaliseFirstChar(fieldName));
                    } else if(statement.contains("%d")) {
                        // This statement uses the temporary variable
                        writer.emitStatement(statement, counter, counter);
                    } else if (statement.contains("%s")) {
                        // This is a normal statement with only one assignment
                        writer.emitStatement(statement, capitaliseFirstChar(fieldName));
                    } else {
                        // This should never happen
                        throw new AssertionError();
                    }
                }
            } else {
                // Links and Link lists
                writer.emitStatement("%s temp_%d = get%s()", fieldTypeCanonicalName, counter, fieldName);
                writer.emitStatement("result = 31 * result + (temp_%d != null ? temp_%d.hashCode() : 0)", counter, counter);
            }
            counter++;
        }
        writer.emitStatement("return result");
        writer.endMethod();
        writer.emitEmptyLine();

        // End the class definition
        writer.endType();
        writer.close();
    }

    private static String capitaliseFirstChar(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}
