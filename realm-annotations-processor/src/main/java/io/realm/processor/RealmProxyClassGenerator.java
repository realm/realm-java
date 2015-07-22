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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

public class RealmProxyClassGenerator {
    private ProcessingEnvironment processingEnvironment;
    private ClassMetaData metadata;
    private final String className;

    // Class metadata for generating proxy classes
    private Elements elementUtils;
    private Types typeUtils;
    private TypeMirror realmObject;
    private DeclaredType realmList;

    public RealmProxyClassGenerator(ProcessingEnvironment processingEnvironment, ClassMetaData metadata) {
        this.processingEnvironment = processingEnvironment;
        this.metadata = metadata;
        this.className = metadata.getSimpleClassName();
    }

    public void generate() throws IOException, UnsupportedOperationException {
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        realmObject = elementUtils.getTypeElement("io.realm.RealmObject").asType();
        realmList = typeUtils.getDeclaredType(elementUtils.getTypeElement("io.realm.RealmList"), typeUtils.getWildcardType(null, null));

        String qualifiedGeneratedClassName = String.format("%s.%s", Constants.REALM_PACKAGE_NAME, Utils.getProxyClassName(className));
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));

        // Set source code indent to 4 spaces
        writer.setIndent("    ");

        writer.emitPackage(Constants.REALM_PACKAGE_NAME)
                .emitEmptyLine();

        ArrayList<String> imports = new ArrayList<String>();
        imports.add("android.util.JsonReader");
        imports.add("android.util.JsonToken");
        imports.add("io.realm.RealmObject");
        imports.add("io.realm.exceptions.RealmException");
        imports.add("io.realm.exceptions.RealmMigrationNeededException");
        imports.add("io.realm.internal.ColumnType");
        imports.add("io.realm.internal.RealmObjectProxy");
        imports.add("io.realm.internal.Table");
        imports.add("io.realm.internal.TableOrView");
        imports.add("io.realm.internal.ImplicitTransaction");
        imports.add("io.realm.internal.LinkView");
        imports.add("io.realm.internal.android.JsonUtils");
        imports.add("java.io.IOException");
        imports.add("java.util.ArrayList");
        imports.add("java.util.Collections");
        imports.add("java.util.List");
        imports.add("java.util.Arrays");
        imports.add("java.util.Date");
        imports.add("java.util.Map");
        imports.add("java.util.HashMap");
        imports.add("org.json.JSONObject");
        imports.add("org.json.JSONException");
        imports.add("org.json.JSONArray");
        imports.add(metadata.getFullyQualifiedClassName());

        for (VariableElement field : metadata.getFields()) {
            String fieldTypeName = "";
            if (typeUtils.isAssignable(field.asType(), realmObject)) { // Links
                fieldTypeName = field.asType().toString();
            } else if (typeUtils.isAssignable(field.asType(), realmList)) { // LinkLists
                fieldTypeName = ((DeclaredType) field.asType()).getTypeArguments().get(0).toString();
            }
            if (!fieldTypeName.isEmpty() && !imports.contains(fieldTypeName)) {
                imports.add(fieldTypeName);
            }
        }
        Collections.sort(imports);
        writer.emitImports(imports);
        writer.emitEmptyLine();

        // Begin the class definition
        writer.beginType(
                qualifiedGeneratedClassName, // full qualified name of the item to generate
                "class",                     // the type of the item
                EnumSet.of(Modifier.PUBLIC), // modifiers to apply
                className,                   // class to extend
                "RealmObjectProxy")          // interfaces to implement
                .emitEmptyLine();

        emitClassFields(writer);
        emitAccessors(writer);
        emitInitTableMethod(writer);
        emitValidateTableMethod(writer);
        emitGetTableNameMethod(writer);
        emitGetFieldNamesMethod(writer);
        emitGetColumnIndicesMethod(writer);
        emitCreateOrUpdateUsingJsonObject(writer);
        emitCreateUsingJsonStream(writer);
        emitCopyOrUpdateMethod(writer);
        emitCopyMethod(writer);
        emitUpdateMethod(writer);
        emitToStringMethod(writer);
        emitHashcodeMethod(writer);
        emitEqualsMethod(writer);

        // End the class definition
        writer.endType();
        writer.close();
    }

    private void emitClassFields(JavaWriter writer) throws IOException {
        for (VariableElement variableElement : metadata.getFields()) {
            writer.emitField("long", staticFieldIndexVarName(variableElement), EnumSet.of(Modifier.PRIVATE, Modifier.STATIC));
        }
        writer.emitField("Map<String, Long>", "columnIndices", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC));
        writer.emitField("List<String>", "FIELD_NAMES", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL));
        writer.beginInitializer(true);
        writer.emitStatement("List<String> fieldNames = new ArrayList<String>()");
        for (VariableElement field : metadata.getFields()) {
            writer.emitStatement("fieldNames.add(\"%s\")", field.getSimpleName().toString());
        }
        writer.emitStatement("FIELD_NAMES = Collections.unmodifiableList(fieldNames)");
        writer.endInitializer();
        writer.emitEmptyLine();
    }

    private void emitAccessors(JavaWriter writer) throws IOException {
        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();

            if (Constants.JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                /**
                 * Primitives and boxed types
                 */
                String realmType = Constants.JAVA_TO_REALM_TYPES.get(fieldTypeCanonicalName);
                String castingType = Constants.CASTING_TYPES.get(fieldTypeCanonicalName);

                // Getter
                writer.emitAnnotation("Override");
                writer.beginMethod(fieldTypeCanonicalName, metadata.getGetter(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.emitStatement(
                        "realm.checkIfValid()"
                );
                writer.emitStatement(
                        "return (%s) row.get%s(%s)",
                        fieldTypeCanonicalName, realmType, staticFieldIndexVarName(field));
                writer.endMethod();
                writer.emitEmptyLine();

                // Setter
                writer.emitAnnotation("Override");
                writer.beginMethod("void", metadata.getSetter(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.emitStatement(
                        "realm.checkIfValid()"
                );
                writer.emitStatement(
                        "row.set%s(%s, (%s) value)",
                        realmType, staticFieldIndexVarName(field), castingType);
                writer.endMethod();
            } else if (typeUtils.isAssignable(field.asType(), realmObject)) {
                /**
                 * Links
                 */

                // Getter
                writer.emitAnnotation("Override");
                writer.beginMethod(fieldTypeCanonicalName, metadata.getGetter(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.beginControlFlow("if (row.isNullLink(%s))", staticFieldIndexVarName(field));
                writer.emitStatement("return null");
                writer.endControlFlow();
                writer.emitStatement(
                        "return realm.getByIndex(%s.class, row.getLink(%s))",
                        fieldTypeCanonicalName, staticFieldIndexVarName(field));
                writer.endMethod();
                writer.emitEmptyLine();

                // Setter
                writer.emitAnnotation("Override");
                writer.beginMethod("void", metadata.getSetter(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.beginControlFlow("if (value == null)");
                writer.emitStatement("row.nullifyLink(%s)", staticFieldIndexVarName(field));
                writer.emitStatement("return");
                writer.endControlFlow();
                writer.emitStatement("row.setLink(%s, value.row.getIndex())", staticFieldIndexVarName(field));
                writer.endMethod();
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                /**
                 * LinkLists
                 */
                String genericType = Utils.getGenericType(field);

                // Getter
                writer.emitAnnotation("Override");
                writer.beginMethod(fieldTypeCanonicalName, metadata.getGetter(fieldName), EnumSet.of(Modifier.PUBLIC));
                writer.emitStatement(
                        "return new RealmList<%s>(%s.class, row.getLinkList(%s), realm)",
                        genericType, genericType, staticFieldIndexVarName(field));
                writer.endMethod();
                writer.emitEmptyLine();

                // Setter
                writer.emitAnnotation("Override");
                writer.beginMethod("void", metadata.getSetter(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
                writer.emitStatement("LinkView links = row.getLinkList(%s)", staticFieldIndexVarName(field));
                writer.beginControlFlow("if (value == null)");
                writer.emitStatement("return"); // TODO: delete all the links instead
                writer.endControlFlow();
                writer.emitStatement("links.clear()");
                writer.beginControlFlow("for (RealmObject linkedObject : (RealmList<? extends RealmObject>) value)");
                writer.emitStatement("links.add(linkedObject.row.getIndex())");
                writer.endControlFlow();
                writer.endMethod();
            } else {
                throw new UnsupportedOperationException(
                        String.format("Type %s of field %s is not supported", fieldTypeCanonicalName, fieldName));
            }
            writer.emitEmptyLine();
        }
    }

    private void emitInitTableMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                "Table", // Return type
                "initTable", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "ImplicitTransaction", "transaction"); // Argument type & argument name

        writer.beginControlFlow("if (!transaction.hasTable(\"" + Constants.TABLE_PREFIX + this.className + "\"))");
        writer.emitStatement("Table table = transaction.getTable(\"%s%s\")", Constants.TABLE_PREFIX, this.className);

        // For each field generate corresponding table index constant
        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            String fieldTypeSimpleName = Utils.getFieldTypeSimpleName(field);

            if (Constants.JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                String nullableFlag;
                if (metadata.isNullable(field)) {
                    nullableFlag = "Table.NULLABLE";
                } else {
                    nullableFlag = "Table.NOT_NULLABLE";
                }
                writer.emitStatement("table.addColumn(%s, \"%s\", %s)",
                        Constants.JAVA_TO_COLUMN_TYPES.get(fieldTypeCanonicalName),
                        fieldName, nullableFlag);
            } else if (typeUtils.isAssignable(field.asType(), realmObject)) {
                writer.beginControlFlow("if (!transaction.hasTable(\"%s%s\"))", Constants.TABLE_PREFIX, fieldTypeSimpleName);
                writer.emitStatement("%s%s.initTable(transaction)", fieldTypeSimpleName, Constants.PROXY_SUFFIX);
                writer.endControlFlow();
                writer.emitStatement("table.addColumnLink(ColumnType.LINK, \"%s\", transaction.getTable(\"%s%s\"))",
                        fieldName, Constants.TABLE_PREFIX, fieldTypeSimpleName);
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                String genericType = Utils.getGenericType(field);
                writer.beginControlFlow("if (!transaction.hasTable(\"%s%s\"))", Constants.TABLE_PREFIX, genericType);
                writer.emitStatement("%s%s.initTable(transaction)", genericType, Constants.PROXY_SUFFIX);
                writer.endControlFlow();
                writer.emitStatement("table.addColumnLink(ColumnType.LINK_LIST, \"%s\", transaction.getTable(\"%s%s\"))",
                        fieldName, Constants.TABLE_PREFIX, genericType);
            }
        }

        for (VariableElement field : metadata.getIndexedFields()) {
            String fieldName = field.getSimpleName().toString();
            writer.emitStatement("table.addSearchIndex(table.getColumnIndex(\"%s\"))", fieldName);
        }

        if (metadata.hasPrimaryKey()) {
            String fieldName = metadata.getPrimaryKey().getSimpleName().toString();
            writer.emitStatement("table.setPrimaryKey(\"%s\")", fieldName);
        } else {
            writer.emitStatement("table.setPrimaryKey(\"\")");
        }

        writer.emitStatement("return table");
        writer.endControlFlow();
        writer.emitStatement("return transaction.getTable(\"%s%s\")", Constants.TABLE_PREFIX, this.className);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitValidateTableMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                "void", // Return type
                "validateTable", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "ImplicitTransaction", "transaction"); // Argument type & argument name

        writer.beginControlFlow("if (transaction.hasTable(\"" + Constants.TABLE_PREFIX + this.className + "\"))");
        writer.emitStatement("Table table = transaction.getTable(\"%s%s\")", Constants.TABLE_PREFIX, this.className);

        // verify number of columns
        writer.beginControlFlow("if (table.getColumnCount() != " + metadata.getFields().size() + ")");
        writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Field count does not match - expected %d but was \" + table.getColumnCount())",
                metadata.getFields().size());
        writer.endControlFlow();

        // create type dictionary for lookup
        writer.emitStatement("Map<String, ColumnType> columnTypes = new HashMap<String, ColumnType>()");
        writer.beginControlFlow("for (long i = 0; i < " + metadata.getFields().size() + "; i++)");
        writer.emitStatement("columnTypes.put(table.getColumnName(i), table.getColumnType(i))");
        writer.endControlFlow();

        // Populate column indices
        writer.emitEmptyLine();
        writer.emitStatement("columnIndices = new HashMap<String, Long>()");
        writer
                .beginControlFlow("for (String fieldName : getFieldNames())")
                    .emitStatement("long index = table.getColumnIndex(fieldName)")
                    .beginControlFlow("if (index == -1)")
                        .emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Field '\" + fieldName + \"' not found for type %s\")", metadata.getSimpleClassName())
                    .endControlFlow()
                    .emitStatement("columnIndices.put(fieldName, index)")
                .endControlFlow();
        for (VariableElement field : metadata.getFields()) {
            writer.emitStatement("%s = table.getColumnIndex(\"%s\")", staticFieldIndexVarName(field), field.getSimpleName().toString());
        }
        writer.emitEmptyLine();

        // For each field verify there is a corresponding
        long fieldIndex = 0;
        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            String fieldTypeSimpleName = Utils.getFieldTypeSimpleName(field);

            if (Constants.JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                // make sure types align
                writer.beginControlFlow("if (!columnTypes.containsKey(\"%s\"))", fieldName);
                writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Missing field '%s'\")", fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (columnTypes.get(\"%s\") != %s)",
                        fieldName, Constants.JAVA_TO_COLUMN_TYPES.get(fieldTypeCanonicalName));
                writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Invalid type '%s' for field '%s'\")",
                        fieldTypeSimpleName, fieldName);
                writer.endControlFlow();

                // make sure that nullability matches
                if (metadata.isNullable(field)) {
                    writer.beginControlFlow("if (!table.isColumnNullable(%s))", staticFieldIndexVarName(field));
                    writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Add annotation @Required or @PrimaryKey to field '%s'\")", fieldName);
                    writer.endControlFlow();
                } else {
                    writer.beginControlFlow("if (table.isColumnNullable(%s))", staticFieldIndexVarName(field));
                    writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Remove annotation @Required or @PrimaryKey from field '%s'\")", fieldName);
                    writer.endControlFlow();
                }

                // Validate @PrimaryKey
                if (field.equals(metadata.getPrimaryKey())) {
                    writer.beginControlFlow("if (table.getPrimaryKey() != table.getColumnIndex(\"%s\"))", fieldName);
                    writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Primary key not defined for field '%s'\")", fieldName);
                    writer.endControlFlow();
                }

                // Validate @Index
                if (metadata.getIndexedFields().contains(field)) {
                    writer.beginControlFlow("if (!table.hasSearchIndex(table.getColumnIndex(\"%s\")))", fieldName);
                    writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Index not defined for field '%s'\")", fieldName);
                    writer.endControlFlow();
                }

            } else if (typeUtils.isAssignable(field.asType(), realmObject)) { // Links
                writer.beginControlFlow("if (!columnTypes.containsKey(\"%s\"))", fieldName);
                writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Missing field '%s'\")", fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (columnTypes.get(\"%s\") != ColumnType.LINK)", fieldName);
                writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Invalid type '%s' for field '%s'\")",
                        fieldTypeSimpleName, fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (!transaction.hasTable(\"%s%s\"))", Constants.TABLE_PREFIX, fieldTypeSimpleName);
                writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Missing class '%s%s' for field '%s'\")",
                        Constants.TABLE_PREFIX, fieldTypeSimpleName, fieldName);
                writer.endControlFlow();

                writer.emitStatement("Table table_%d = transaction.getTable(\"%s%s\")", fieldIndex, Constants.TABLE_PREFIX, fieldTypeSimpleName);
                writer.beginControlFlow("if (!table.getLinkTarget(%s).hasSameSchema(table_%d))",
                        staticFieldIndexVarName(field), fieldIndex);
                writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Invalid RealmObject for field '%s': '\" + table.getLinkTarget(%s).getName() + \"' expected - was '\" + table_%d.getName() + \"'\")",
                        fieldName, staticFieldIndexVarName(field), fieldIndex);
                writer.endControlFlow();
            } else if (typeUtils.isAssignable(field.asType(), realmList)) { // Link Lists
                String genericType = Utils.getGenericType(field);
                writer.beginControlFlow("if (!columnTypes.containsKey(\"%s\"))", fieldName);
                writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Missing field '%s'\")", fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (columnTypes.get(\"%s\") != ColumnType.LINK_LIST)", fieldName);
                writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Invalid type '%s' for field '%s'\")",
                        genericType, fieldName);
                writer.endControlFlow();
                writer.beginControlFlow("if (!transaction.hasTable(\"%s%s\"))", Constants.TABLE_PREFIX, genericType);
                writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Missing class '%s%s' for field '%s'\")",
                        Constants.TABLE_PREFIX, genericType, fieldName);
                writer.endControlFlow();

                writer.emitStatement("Table table_%d = transaction.getTable(\"%s%s\")", fieldIndex, Constants.TABLE_PREFIX, genericType);
                writer.beginControlFlow("if (!table.getLinkTarget(%s).hasSameSchema(table_%d))",
                        staticFieldIndexVarName(field), fieldIndex);
                writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"Invalid RealmList type for field '%s': '\" + table.getLinkTarget(%s).getName() + \"' expected - was '\" + table_%d.getName() + \"'\")",
                        fieldName, staticFieldIndexVarName(field), fieldIndex);
                writer.endControlFlow();
            }
            fieldIndex++;
        }

        writer.nextControlFlow("else");
        writer.emitStatement("throw new RealmMigrationNeededException(transaction.getPath(), \"The %s class is missing from the schema for this Realm.\")", metadata.getSimpleClassName());
        writer.endControlFlow();
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitGetTableNameMethod(JavaWriter writer) throws IOException {
        writer.beginMethod("String", "getTableName", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC));
        writer.emitStatement("return \"%s%s\"", Constants.TABLE_PREFIX, className);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitGetFieldNamesMethod(JavaWriter writer) throws IOException {
        writer.beginMethod("List<String>", "getFieldNames", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC));
        writer.emitStatement("return FIELD_NAMES");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitGetColumnIndicesMethod(JavaWriter writer) throws IOException {
        writer.beginMethod("Map<String,Long>", "getColumnIndices", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC));
        writer.emitStatement("return columnIndices");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitCopyOrUpdateMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                className, // Return type
                "copyOrUpdate", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", className, "object", "boolean", "update", "Map<RealmObject,RealmObjectProxy>", "cache" // Argument type & argument name
        );

        // If object is already in the Realm there is nothing to update
        writer
            .beginControlFlow("if (object.realm != null && object.realm.getPath().equals(realm.getPath()))")
                .emitStatement("return object")
            .endControlFlow();

        if (!metadata.hasPrimaryKey()) {
            writer.emitStatement("return copy(realm, object, update, cache)");
        } else {
            writer
                .emitStatement("%s realmObject = null", className)
                .emitStatement("boolean canUpdate = update")
                .beginControlFlow("if (canUpdate)")
                    .emitStatement("Table table = realm.getTable(%s.class)", className)
                    .emitStatement("long pkColumnIndex = table.getPrimaryKey()");

            if (Utils.isString(metadata.getPrimaryKey())) {
                writer
                    .beginControlFlow("if (object.%s() == null)", metadata.getPrimaryKeyGetter())
                        .emitStatement("throw new IllegalArgumentException(\"Primary key value must not be null.\")")
                    .endControlFlow()
                    .emitStatement("long rowIndex = table.findFirstString(pkColumnIndex, object.%s())", metadata.getPrimaryKeyGetter());
            } else {
                writer.emitStatement("long rowIndex = table.findFirstLong(pkColumnIndex, object.%s())", metadata.getPrimaryKeyGetter());
            }

            writer
                .beginControlFlow("if (rowIndex != TableOrView.NO_MATCH)")
                    .emitStatement("realmObject = new %s()", Utils.getProxyClassName(className))
                    .emitStatement("realmObject.realm = realm")
                    .emitStatement("realmObject.row = table.getUncheckedRowByIndex(rowIndex)")
                    .emitStatement("cache.put(object, (RealmObjectProxy) realmObject)")
                .nextControlFlow("else")
                    .emitStatement("canUpdate = false")
                .endControlFlow();

            writer.endControlFlow();

            writer
                .emitEmptyLine()
                .beginControlFlow("if (canUpdate)")
                    .emitStatement("return update(realm, realmObject, object, cache)")
                .nextControlFlow("else")
                    .emitStatement("return copy(realm, object, update, cache)")
                .endControlFlow();
        }

        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitCopyMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                className, // Return type
                "copy", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", className, "newObject", "boolean", "update", "Map<RealmObject,RealmObjectProxy>", "cache"); // Argument type & argument name

        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("%s realmObject = realm.createObject(%s.class, newObject.%s())", className, className, metadata.getPrimaryKeyGetter());
        } else {
            writer.emitStatement("%s realmObject = realm.createObject(%s.class)", className, className);
        }
        writer.emitStatement("cache.put(newObject, (RealmObjectProxy) realmObject)");
        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String fieldType = field.asType().toString();
            if (typeUtils.isAssignable(field.asType(), realmObject)) {
                writer
                    .emitEmptyLine()
                    .emitStatement("%s %sObj = newObject.%s()", fieldType, fieldName, metadata.getGetter(fieldName))
                    .beginControlFlow("if (%sObj != null)", fieldName)
                        .emitStatement("%s cache%s = (%s) cache.get(%sObj)", fieldType, fieldName, fieldType, fieldName)
                        .beginControlFlow("if (cache%s != null)", fieldName)
                            .emitStatement("realmObject.%s(cache%s)", metadata.getSetter(fieldName), fieldName)
                        .nextControlFlow("else")
                            .emitStatement("realmObject.%s(%s.copyOrUpdate(realm, %sObj, update, cache))",
                                    metadata.getSetter(fieldName),
                                    Utils.getProxyClassSimpleName(field),
                                    fieldName)
                        .endControlFlow()
                    .endControlFlow();
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                writer
                    .emitEmptyLine()
                    .emitStatement("RealmList<%s> %sList = newObject.%s()", Utils.getGenericType(field), fieldName, metadata.getGetter(fieldName))
                    .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("RealmList<%s> %sRealmList = realmObject.%s()", Utils.getGenericType(field), fieldName, metadata.getGetter(fieldName))
                        .beginControlFlow("for (int i = 0; i < %sList.size(); i++)", fieldName)
                                .emitStatement("%s %sItem = %sList.get(i)", Utils.getGenericType(field), fieldName, fieldName)
                                .emitStatement("%s cache%s = (%s) cache.get(%sItem)", Utils.getGenericType(field), fieldName, Utils.getGenericType(field), fieldName)
                                .beginControlFlow("if (cache%s != null)", fieldName)
                                        .emitStatement("%sRealmList.add(cache%s)", fieldName, fieldName)
                                .nextControlFlow("else")
                                        .emitStatement("%sRealmList.add(%s.copyOrUpdate(realm, %sList.get(i), update, cache))", fieldName, Utils.getProxyClassSimpleName(field), fieldName)
                                .endControlFlow()
                        .endControlFlow()
                    .endControlFlow()
                    .emitEmptyLine();

            } else {
                if (Constants.NULLABLE_JAVA_TYPES.containsKey(fieldType) && !metadata.isNullable(field)) {
                    writer.emitStatement("realmObject.%s(newObject.%s() != null ? newObject.%s() : %s)",
                            metadata.getSetter(fieldName),
                            metadata.getGetter(fieldName),
                            metadata.getGetter(fieldName),
                            Constants.NULLABLE_JAVA_TYPES.get(fieldType));
                } else {
                    writer.emitStatement("realmObject.%s(newObject.%s())", metadata.getSetter(fieldName), metadata.getGetter(fieldName));
                }
            }
        }

        writer.emitStatement("return realmObject");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitUpdateMethod(JavaWriter writer) throws IOException {

        writer.beginMethod(
                className, // Return type
                "update", // Method name
                EnumSet.of(Modifier.STATIC), // Modifiers
                "Realm", "realm", className, "realmObject", className, "newObject", "Map<RealmObject, RealmObjectProxy>", "cache"); // Argument type & argument name

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            if (typeUtils.isAssignable(field.asType(), realmObject)) {
                writer
                    .emitStatement("%s %sObj = newObject.%s()", Utils.getFieldTypeSimpleName(field), fieldName, metadata.getGetter(fieldName))
                    .beginControlFlow("if (%sObj != null)", fieldName)
                        .emitStatement("%s cache%s = (%s) cache.get(%sObj)", Utils.getFieldTypeSimpleName(field), fieldName, Utils.getFieldTypeSimpleName(field), fieldName)
                        .beginControlFlow("if (cache%s != null)", fieldName)
                            .emitStatement("realmObject.%s(cache%s)", metadata.getSetter(fieldName), fieldName)
                        .nextControlFlow("else")
                            .emitStatement("realmObject.%s(%s.copyOrUpdate(realm, %sObj, true, cache))",
                                    metadata.getSetter(fieldName),
                                    Utils.getProxyClassSimpleName(field),
                                    fieldName,
                                    Utils.getFieldTypeSimpleName(field)
                            )
                        .endControlFlow()
                    .nextControlFlow("else")
                        .emitStatement("realmObject.%s(null)", metadata.getSetter(fieldName))
                    .endControlFlow();
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                writer
                    .emitStatement("RealmList<%s> %sList = newObject.%s()", Utils.getGenericType(field), fieldName, metadata.getGetter(fieldName))
                    .emitStatement("RealmList<%s> %sRealmList = realmObject.%s()", Utils.getGenericType(field), fieldName, metadata.getGetter(fieldName))
                    .emitStatement("%sRealmList.clear()", fieldName)
                    .beginControlFlow("if (%sList != null)", fieldName)
                        .beginControlFlow("for (int i = 0; i < %sList.size(); i++)", fieldName)
                            .emitStatement("%s %sItem = %sList.get(i)", Utils.getGenericType(field), fieldName, fieldName)
                            .emitStatement("%s cache%s = (%s) cache.get(%sItem)", Utils.getGenericType(field), fieldName, Utils.getGenericType(field), fieldName)
                            .beginControlFlow("if (cache%s != null)", fieldName)
                                .emitStatement("%sRealmList.add(cache%s)", fieldName, fieldName)
                            .nextControlFlow("else")
                                .emitStatement("%sRealmList.add(%s.copyOrUpdate(realm, %sList.get(i), true, cache))", fieldName, Utils.getProxyClassSimpleName(field), fieldName)
                            .endControlFlow()
                        .endControlFlow()
                    .endControlFlow();

            } else {
                if (field == metadata.getPrimaryKey()) {
                    continue;
                }

                String fieldType = field.asType().toString();
                if (Constants.NULLABLE_JAVA_TYPES.containsKey(fieldType) && !metadata.isNullable(field)) {
                    writer.emitStatement("realmObject.%s(newObject.%s() != null ? newObject.%s() : %s)",
                            metadata.getSetter(fieldName),
                            metadata.getGetter(fieldName),
                            metadata.getGetter(fieldName),
                            Constants.NULLABLE_JAVA_TYPES.get(fieldType));
                } else {
                    writer.emitStatement("realmObject.%s(newObject.%s())", metadata.getSetter(fieldName), metadata.getGetter(fieldName));
                }
            }
        }

        writer.emitStatement("return realmObject");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitToStringMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod("String", "toString", EnumSet.of(Modifier.PUBLIC));
        writer.beginControlFlow("if (!isValid())");
        writer.emitStatement("return \"Invalid object\"");
        writer.endControlFlow();
        writer.emitStatement("StringBuilder stringBuilder = new StringBuilder(\"%s = [\")", className);
        List<VariableElement> fields = metadata.getFields();
        for (int i = 0; i < fields.size(); i++) {
            VariableElement field = fields.get(i);
            String fieldName = field.getSimpleName().toString();

            writer.emitStatement("stringBuilder.append(\"{%s:\")", fieldName);
            if (typeUtils.isAssignable(field.asType(), realmObject)) {
                String fieldTypeSimpleName = Utils.getFieldTypeSimpleName(field);
                writer.emitStatement(
                        "stringBuilder.append(%s() != null ? \"%s\" : \"null\")",
                        metadata.getGetter(fieldName),
                        fieldTypeSimpleName
                );
            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                String genericType = Utils.getGenericType(field);
                writer.emitStatement("stringBuilder.append(\"RealmList<%s>[\").append(%s().size()).append(\"]\")",
                        genericType,
                        metadata.getGetter(fieldName));
            } else {
                if (metadata.isNullable(field)) {
                    writer.emitStatement("stringBuilder.append(%s() != null ? %s() : \"null\")",
                            metadata.getGetter(fieldName),
                            metadata.getGetter(fieldName)
                    );
                } else {
                    writer.emitStatement("stringBuilder.append(%s())", metadata.getGetter(fieldName));
                }
            }
            writer.emitStatement("stringBuilder.append(\"}\")");

            if (i < fields.size() - 1) {
                writer.emitStatement("stringBuilder.append(\",\")");
            }
        }

        writer.emitStatement("stringBuilder.append(\"]\")");
        writer.emitStatement("return stringBuilder.toString()");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitHashcodeMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod("int", "hashCode", EnumSet.of(Modifier.PUBLIC));
        writer.emitStatement("String realmName = realm.getPath()");
        writer.emitStatement("String tableName = row.getTable().getName()");
        writer.emitStatement("long rowIndex = row.getIndex()");
        writer.emitEmptyLine();
        writer.emitStatement("int result = 17");
        writer.emitStatement("result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0)");
        writer.emitStatement("result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0)");
        writer.emitStatement("result = 31 * result + (int) (rowIndex ^ (rowIndex >>> 32))");
        writer.emitStatement("return result");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitEqualsMethod(JavaWriter writer) throws IOException {
        String proxyClassName = className + Constants.PROXY_SUFFIX;
        writer.emitAnnotation("Override");
        writer.beginMethod("boolean", "equals", EnumSet.of(Modifier.PUBLIC), "Object", "o");
        writer.emitStatement("if (this == o) return true");
        writer.emitStatement("if (o == null || getClass() != o.getClass()) return false");
        writer.emitStatement("%s a%s = (%s)o", proxyClassName, className, proxyClassName);  // FooRealmProxy aFoo = (FooRealmProxy)o
        writer.emitEmptyLine();
        writer.emitStatement("String path = realm.getPath()");
        writer.emitStatement("String otherPath = a%s.realm.getPath()", className);
        writer.emitStatement("if (path != null ? !path.equals(otherPath) : otherPath != null) return false;");
        writer.emitEmptyLine();
        writer.emitStatement("String tableName = row.getTable().getName()");
        writer.emitStatement("String otherTableName = a%s.row.getTable().getName()", className);
        writer.emitStatement("if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false");
        writer.emitEmptyLine();
        writer.emitStatement("if (row.getIndex() != a%s.row.getIndex()) return false", className);
        writer.emitEmptyLine();
        writer.emitStatement("return true");
        writer.endMethod();
        writer.emitEmptyLine();
    }


    private void emitCreateOrUpdateUsingJsonObject(JavaWriter writer) throws IOException {
        writer.beginMethod(
                className,
                "createOrUpdateUsingJsonObject",
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
                Arrays.asList("Realm", "realm", "JSONObject", "json", "boolean", "update"),
                Arrays.asList("JSONException"));

        if (!metadata.hasPrimaryKey()) {
            writer.emitStatement("%s obj = realm.createObject(%s.class)", className, className);
        } else {
            String pkType = Utils.isString(metadata.getPrimaryKey()) ? "String" : "Long";
            writer
                .emitStatement("%s obj = null", className)
                .beginControlFlow("if (update)")
                    .emitStatement("Table table = realm.getTable(%s.class)", className)
                    .emitStatement("long pkColumnIndex = table.getPrimaryKey()")
                    .beginControlFlow("if (!json.isNull(\"%s\"))", metadata.getPrimaryKey().getSimpleName())
                        .emitStatement("long rowIndex = table.findFirst%s(pkColumnIndex, json.get%s(\"%s\"))",
                                pkType, pkType, metadata.getPrimaryKey().getSimpleName())
                        .beginControlFlow("if (rowIndex != TableOrView.NO_MATCH)")
                            .emitStatement("obj = new %s()", Utils.getProxyClassName(className))
                            .emitStatement("obj.realm = realm")
                            .emitStatement("obj.row = table.getUncheckedRowByIndex(rowIndex)")
                        .endControlFlow()
                    .endControlFlow()
                .endControlFlow()
                .beginControlFlow("if (obj == null)")
                    .emitStatement("obj = realm.createObject(%s.class)", className)
                .endControlFlow();
        }

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String qualifiedFieldType = field.asType().toString();
            if (typeUtils.isAssignable(field.asType(), realmObject)) {
                RealmJsonTypeHelper.emitFillRealmObjectWithJsonValue(
                        metadata.getSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        Utils.getProxyClassSimpleName(field),
                        writer);

            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                RealmJsonTypeHelper.emitFillRealmListWithJsonValue(
                        metadata.getGetter(fieldName),
                        metadata.getSetter(fieldName),
                        fieldName,
                        ((DeclaredType) field.asType()).getTypeArguments().get(0).toString(),
                        Utils.getProxyClassSimpleName(field),
                        writer);

            } else {
                RealmJsonTypeHelper.emitFillJavaTypeWithJsonValue(
                        metadata.getSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        writer);
            }
        }

        writer.emitStatement("return obj");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitCreateUsingJsonStream(JavaWriter writer) throws IOException {
        writer.beginMethod(
                className,
                "createUsingJsonStream",
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
                Arrays.asList("Realm", "realm", "JsonReader", "reader"),
                Arrays.asList("IOException"));

        writer.emitStatement("%s obj = realm.createObject(%s.class)",className, className);
        writer.emitStatement("reader.beginObject()");
        writer.beginControlFlow("while (reader.hasNext())");
        writer.emitStatement("String name = reader.nextName()");

        List<VariableElement> fields = metadata.getFields();
        for (int i = 0; i < fields.size(); i++) {
            VariableElement field = fields.get(i);
            String fieldName = field.getSimpleName().toString();
            String qualifiedFieldType = field.asType().toString();

            if (i == 0) {
                writer.beginControlFlow("if (name.equals(\"%s\") && reader.peek() != JsonToken.NULL)", fieldName);
            } else {
                writer.nextControlFlow("else if (name.equals(\"%s\")  && reader.peek() != JsonToken.NULL)", fieldName);
            }
            if (typeUtils.isAssignable(field.asType(), realmObject)) {
                RealmJsonTypeHelper.emitFillRealmObjectFromStream(
                        metadata.getSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        Utils.getProxyClassSimpleName(field),
                        writer);

            } else if (typeUtils.isAssignable(field.asType(), realmList)) {
                RealmJsonTypeHelper.emitFillRealmListFromStream(
                        metadata.getGetter(fieldName),
                        metadata.getSetter(fieldName),
                        ((DeclaredType) field.asType()).getTypeArguments().get(0).toString(),
                        Utils.getProxyClassSimpleName(field),
                        writer);

            } else {
                RealmJsonTypeHelper.emitFillJavaTypeFromStream(
                        metadata.getSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        writer);
            }
        }

        if (fields.size() > 0) {
            writer.nextControlFlow("else");
            writer.emitStatement("reader.skipValue()");
            writer.endControlFlow();
        }
        writer.endControlFlow();
        writer.emitStatement("reader.endObject()");
        writer.emitStatement("return obj");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private String staticFieldIndexVarName(VariableElement variableElement) {
        return "INDEX_" + variableElement.getSimpleName().toString().toUpperCase();
    }
}
