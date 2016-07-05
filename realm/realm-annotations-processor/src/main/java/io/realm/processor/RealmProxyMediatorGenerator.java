/*
 * Copyright 2015 Realm Inc.
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
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import io.realm.annotations.RealmModule;

import static io.realm.processor.Constants.REALM_PACKAGE_NAME;

public class RealmProxyMediatorGenerator {
    private final String className;
    private ProcessingEnvironment processingEnvironment;
    private List<String> qualifiedModelClasses = new ArrayList<String>();
    private List<String> qualifiedProxyClasses = new ArrayList<String>();

    public RealmProxyMediatorGenerator(ProcessingEnvironment processingEnvironment,
                                       String className, Set<ClassMetaData> classesToValidate) {
        this.processingEnvironment = processingEnvironment;
        this.className = className;

        for (ClassMetaData metadata : classesToValidate) {
            String simpleName = metadata.getSimpleClassName();
            qualifiedModelClasses.add(metadata.getFullyQualifiedClassName());
            qualifiedProxyClasses.add(REALM_PACKAGE_NAME + "." + getProxyClassName(simpleName));
        }
    }

    public void generate() throws IOException {
        String qualifiedGeneratedClassName = String.format("%s.%sMediator", REALM_PACKAGE_NAME, className);
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));
        writer.setIndent("    ");

        writer.emitPackage(REALM_PACKAGE_NAME);
        writer.emitEmptyLine();

        writer.emitImports(
                "android.util.JsonReader",
                "java.io.IOException",
                "java.util.Collections",
                "java.util.HashMap",
                "java.util.HashSet",
                "java.util.List",
                "java.util.Map",
                "java.util.Set",
                "io.realm.internal.ColumnInfo",
                "io.realm.internal.ImplicitTransaction",
                "io.realm.internal.RealmObjectProxy",
                "io.realm.internal.RealmProxyMediator",
                "io.realm.internal.Table",
                "org.json.JSONException",
                "org.json.JSONObject"
        );

        writer.emitEmptyLine();

        writer.emitAnnotation(RealmModule.class);
        writer.beginType(
                qualifiedGeneratedClassName,        // full qualified name of the item to generate
                "class",                            // the type of the item
                Collections.<Modifier>emptySet(),   // modifiers to apply
                "RealmProxyMediator");              // class to extend
        writer.emitEmptyLine();

        emitFields(writer);
        emitCreateTableMethod(writer);
        emitValidateTableMethod(writer);
        emitGetFieldNamesMethod(writer);
        emitGetTableNameMethod(writer);
        emitNewInstanceMethod(writer);
        emitGetClassModelList(writer);
        emitCopyToRealmMethod(writer);
        emitCreteOrUpdateUsingJsonObject(writer);
        emitCreateUsingJsonStream(writer);
        emitCreateDetachedCopyMethod(writer);
        writer.endType();
        writer.close();
    }

    private void emitFields(JavaWriter writer) throws IOException {
        writer.emitField("Set<Class<? extends RealmModel>>", "MODEL_CLASSES", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL));
        writer.beginInitializer(true);
        writer.emitStatement("Set<Class<? extends RealmModel>> modelClasses = new HashSet<Class<? extends RealmModel>>()");
        for (String clazz : qualifiedModelClasses) {
            writer.emitStatement("modelClasses.add(%s.class)", clazz);
        }
        writer.emitStatement("MODEL_CLASSES = Collections.unmodifiableSet(modelClasses)");
        writer.endInitializer();
        writer.emitEmptyLine();
    }

    private void emitCreateTableMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "Table",
                "createTable",
                EnumSet.of(Modifier.PUBLIC),
                "Class<? extends RealmModel>", "clazz", "ImplicitTransaction", "transaction"
        );
        emitMediatorSwitch(new ProxySwitchStatement() {
            @Override
            public void emitStatement(int i, JavaWriter writer) throws IOException {
                writer.emitStatement("return %s.initTable(transaction)", qualifiedProxyClasses.get(i));
            }
        }, writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitValidateTableMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "ColumnInfo",
                "validateTable",
                EnumSet.of(Modifier.PUBLIC),
                "Class<? extends RealmModel>", "clazz", "ImplicitTransaction", "transaction"
        );
        emitMediatorSwitch(new ProxySwitchStatement() {
            @Override
            public void emitStatement(int i, JavaWriter writer) throws IOException {
                writer.emitStatement("return %s.validateTable(transaction)", qualifiedProxyClasses.get(i));
            }
        }, writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitGetFieldNamesMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "List<String>",
                "getFieldNames",
                EnumSet.of(Modifier.PUBLIC),
                "Class<? extends RealmModel>", "clazz"
        );
        emitMediatorSwitch(new ProxySwitchStatement() {
            @Override
            public void emitStatement(int i, JavaWriter writer) throws IOException {
                writer.emitStatement("return %s.getFieldNames()", qualifiedProxyClasses.get(i));
            }
        }, writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitGetTableNameMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "String",
                "getTableName",
                EnumSet.of(Modifier.PUBLIC),
                "Class<? extends RealmModel>", "clazz"
        );
        emitMediatorSwitch(new ProxySwitchStatement() {
            @Override
            public void emitStatement(int i, JavaWriter writer) throws IOException {
                writer.emitStatement("return %s.getTableName()", qualifiedProxyClasses.get(i));
            }
        }, writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitNewInstanceMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "<E extends RealmModel> E",
                "newInstance",
                EnumSet.of(Modifier.PUBLIC),
                "Class<E>", "clazz", "ColumnInfo", "columnInfo"
        );
        emitMediatorSwitch(new ProxySwitchStatement() {
            @Override
            public void emitStatement(int i, JavaWriter writer) throws IOException {
                writer.emitStatement("return clazz.cast(new %s(columnInfo))", qualifiedProxyClasses.get(i));
            }
        }, writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitGetClassModelList(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod("Set<Class<? extends RealmModel>>", "getModelClasses", EnumSet.of(Modifier.PUBLIC));
        writer.emitStatement("return MODEL_CLASSES");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitCopyToRealmMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "<E extends RealmModel> E",
                "copyOrUpdate",
                EnumSet.of(Modifier.PUBLIC),
                "Realm", "realm", "E", "obj", "boolean", "update", "Map<RealmModel, RealmObjectProxy>",  "cache"
        );
        writer.emitSingleLineComment("This cast is correct because obj is either");
        writer.emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject");
        writer.emitStatement("@SuppressWarnings(\"unchecked\") Class<E> clazz = (Class<E>) ((obj instanceof RealmObjectProxy) ? obj.getClass().getSuperclass() : obj.getClass())");
        writer.emitEmptyLine();
        emitMediatorSwitch(new ProxySwitchStatement() {
            @Override
            public void emitStatement(int i, JavaWriter writer) throws IOException {
                writer.emitStatement("return clazz.cast(%s.copyOrUpdate(realm, (%s) obj, update, cache))", qualifiedProxyClasses.get(i), qualifiedModelClasses.get(i));
            }
        }, writer, false);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitCreteOrUpdateUsingJsonObject(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "<E extends RealmModel> E",
                "createOrUpdateUsingJsonObject",
                EnumSet.of(Modifier.PUBLIC),
                Arrays.asList("Class<E>", "clazz", "Realm", "realm", "JSONObject", "json", "boolean", "update"),
                Arrays.asList("JSONException")
        );
        emitMediatorSwitch(new ProxySwitchStatement() {
            @Override
            public void emitStatement(int i, JavaWriter writer) throws IOException {
                writer.emitStatement("return clazz.cast(%s.createOrUpdateUsingJsonObject(realm, json, update))", qualifiedProxyClasses.get(i));
            }
        }, writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitCreateUsingJsonStream(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "<E extends RealmModel> E",
                "createUsingJsonStream",
                EnumSet.of(Modifier.PUBLIC),
                Arrays.asList("Class<E>", "clazz", "Realm", "realm", "JsonReader", "reader"),
                Arrays.asList("java.io.IOException")
        );
        emitMediatorSwitch(new ProxySwitchStatement() {
            @Override
            public void emitStatement(int i, JavaWriter writer) throws IOException {
                writer.emitStatement("return clazz.cast(%s.createUsingJsonStream(realm, reader))", qualifiedProxyClasses.get(i));
            }
        }, writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitCreateDetachedCopyMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "<E extends RealmModel> E",
                "createDetachedCopy",
                EnumSet.of(Modifier.PUBLIC),
                "E", "realmObject", "int", "maxDepth", "Map<RealmModel, RealmObjectProxy.CacheData<RealmModel>>", "cache"
        );
        writer.emitSingleLineComment("This cast is correct because obj is either");
        writer.emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject");
        writer.emitStatement("@SuppressWarnings(\"unchecked\") Class<E> clazz = (Class<E>) realmObject.getClass().getSuperclass()");
        writer.emitEmptyLine();
        emitMediatorSwitch(new ProxySwitchStatement() {
            @Override
            public void emitStatement(int i, JavaWriter writer) throws IOException {
                writer.emitStatement("return clazz.cast(%s.createDetachedCopy((%s) realmObject, 0, maxDepth, cache))",
                        qualifiedProxyClasses.get(i), qualifiedModelClasses.get(i));
            }
        }, writer, false);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    // Emits the control flow for selecting the appropriate proxy class based on the model class
    // Currently it is just if..else, which is inefficient for large amounts amounts of model classes.
    // Consider switching to HashMap or similar.
    private void emitMediatorSwitch(ProxySwitchStatement statement, JavaWriter writer) throws IOException {
        emitMediatorSwitch(statement, writer, true);
    }

    private void emitMediatorSwitch(ProxySwitchStatement statement, JavaWriter writer, boolean nullPointerCheck)
            throws IOException {
        if (nullPointerCheck) {
            writer.emitStatement("checkClass(clazz)");
            writer.emitEmptyLine();
        }
        if (qualifiedModelClasses.size() == 0) {
            writer.emitStatement("throw getMissingProxyClassException(clazz)");
        } else {
            writer.beginControlFlow("if (clazz.equals(%s.class))", qualifiedModelClasses.get(0));
            statement.emitStatement(0, writer);
            for (int i = 1; i < qualifiedModelClasses.size(); i++) {
                writer.nextControlFlow("else if (clazz.equals(%s.class))", qualifiedModelClasses.get(i));
                statement.emitStatement(i, writer);
            }
            writer.nextControlFlow("else");
            writer.emitStatement("throw getMissingProxyClassException(clazz)");
            writer.endControlFlow();
        }
    }

    private String getProxyClassName(String clazz) {
        return clazz + Constants.PROXY_SUFFIX;
    }

    private interface ProxySwitchStatement {
        void emitStatement(int i, JavaWriter writer) throws IOException;
    }
}
