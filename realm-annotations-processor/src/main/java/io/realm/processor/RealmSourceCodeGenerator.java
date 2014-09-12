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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


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
                "io.realm.RealmLinkList",
                "io.realm.RealmObject",
                "io.realm.RealmList")
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
                String castingType = "String".equals(realmType) ? "String" : realmType.toLowerCase(Locale.getDefault());

                // Getter
                writer.emitAnnotation("Override");
                writer.beginMethod(fieldTypeCanonicalName, "get" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.emitStatement(
                        "return (%s) row.get%s(%d)",
                        fieldTypeCanonicalName, realmType, columnNumber);
                writer.endMethod();
                writer.emitEmptyLine();

                // Setter
                writer.emitAnnotation("Override");
                writer.beginMethod("void", "set" + capitaliseFirstChar(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.emitStatement(
                        "row.set%s(%d, (%s) value)",
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
                        "return new RealmLinkList(%s.class, realmGetRow().getLinkList(%d), realm)",
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
                writer.emitStatement("table.addColumnLink(ColumnType.LINK, \"%s\", transaction.getTable(\"%s\"))",
                        fieldName.toLowerCase(Locale.getDefault()), fieldTypeName.toLowerCase(Locale.getDefault()));
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                String genericCanonicalType = ((DeclaredType) field.asType()).getTypeArguments().get(0).toString();
                String genericType;
                if (genericCanonicalType.contains(".")) {
                    genericType = genericCanonicalType.substring(genericCanonicalType.lastIndexOf('.') + 1);
                } else {
                    genericType = genericCanonicalType;
                }

                writer.emitStatement("table.addColumnLink(ColumnType.LINK_LIST, \"%s\", transaction.getTable(\"%s\"))",
                        fieldName.toLowerCase(Locale.getDefault()), genericType);
            }
        }

        writer.emitStatement("return table");
        writer.endControlFlow();
        writer.emitStatement("return transaction.getTable(\"%s\")", this.className);
        writer.endMethod();
        writer.emitEmptyLine();

        // End the class definition
        writer.endType();
        writer.close();
    }

    private static String capitaliseFirstChar(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private void info(String message) {
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }
}
