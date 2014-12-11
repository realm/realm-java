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
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

public class RealmProxyMediatorImplGenerator {
    private ProcessingEnvironment processingEnvironment;
    private ArrayList<String> qualifiedModelClasses = new ArrayList<String>();
    private ArrayList<String> simpleModelClasses = new ArrayList<String>();
    private ArrayList<String> proxyClasses = new ArrayList<String>();

    private static final String REALM_PACKAGE_NAME = "io.realm";
    private static final String CLASS_NAME = "RealmProxyMediatorImpl";
    private static final String EXCEPTION_MSG = "\"Could not find the generated proxy class for \" + clazz + \". \" + RealmProxyMediator.APT_NOT_EXECUTED_MESSAGE";

    public RealmProxyMediatorImplGenerator(ProcessingEnvironment processingEnvironment, Set<String> classesToValidate) {
        this.processingEnvironment = processingEnvironment;
        for (String clazz : classesToValidate) {
            String simpleName = stripPackage(clazz);
            qualifiedModelClasses.add(clazz);
            simpleModelClasses.add(simpleName);
            proxyClasses.add(getProxyClassName(simpleName));
        }
    }

    public void generate() throws IOException {
        String qualifiedGeneratedClassName = String.format("%s.%s", REALM_PACKAGE_NAME, CLASS_NAME);
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));
        writer.setIndent("    ");

        writer.emitPackage(REALM_PACKAGE_NAME);
        writer.emitEmptyLine();

        writer.emitImports(
                "java.util.ArrayList",
                "java.util.Collections",
                "java.util.List",
                "io.realm.exceptions.RealmException",
                "io.realm.internal.ImplicitTransaction",
                "io.realm.internal.Table"
        );
        writer.emitImports(qualifiedModelClasses);

        writer.emitEmptyLine();

        // Begin the class definition
        writer.beginType(
                qualifiedGeneratedClassName, // full qualified name of the item to generate
                "class",                     // the type of the item
                Collections.EMPTY_SET,       // modifiers to apply
                null,                        // class to extend
                "RealmProxyMediator");       // Interfaces to implement
        writer.emitEmptyLine();

        emitFields(writer);
        emitConstuctor(writer);
        emitCreateTableMethod(writer);
        emitValidateTableMethod(writer);
        emitGetFieldNamesMethod(writer);
        emitGetClassModelName(writer);
        emitNewInstanceMethod(writer);
        emitGetClassModelList(writer);

        writer.endType();
        writer.close();
    }

    private void emitFields(JavaWriter writer) throws IOException {
        writer.emitField("List<Class<? extends RealmObject>>", "MODEL_CLASSES", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL));
        writer.beginInitializer(true);
        writer.emitStatement("ArrayList<Class<? extends RealmObject>> modelClasses = new ArrayList<Class<? extends RealmObject>>()");
        for (String clazz : simpleModelClasses) {
            writer.emitStatement("modelClasses.add(%s.class)", clazz);
        }
        writer.emitStatement("MODEL_CLASSES = Collections.unmodifiableList(modelClasses)");
        writer.endInitializer();
        writer.emitEmptyLine();
    }

    private void emitConstuctor(JavaWriter writer) throws IOException {
        writer.beginConstructor(Collections.EMPTY_SET);
        writer.endConstructor();
        writer.emitEmptyLine();
    }

    private void emitCreateTableMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "Table",
                "createTable",
                EnumSet.of(Modifier.PUBLIC),
                "Class<? extends RealmObject>", "clazz", "ImplicitTransaction", "transaction"
        );
        emitMediatorSwitch("return %s.initTable(transaction)", writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitValidateTableMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "void",
                "validateTable",
                EnumSet.of(Modifier.PUBLIC),
                "Class<? extends RealmObject>", "clazz", "ImplicitTransaction", "transaction"
        );
        emitMediatorSwitch("%s.validateTable(transaction)", writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitGetFieldNamesMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "List<String>",
                "getFieldNames",
                EnumSet.of(Modifier.PUBLIC),
                "Class<? extends RealmObject>", "clazz"
        );
        emitMediatorSwitch("return %s.getFieldNames()", writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitGetClassModelName(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "String",
                "getClassModelName",
                EnumSet.of(Modifier.PUBLIC),
                "Class<? extends RealmObject>", "clazz"
        );
        emitMediatorSwitch("return %s.getClassModelName()", writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitNewInstanceMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "<E extends RealmObject> E",
                "newInstance",
                EnumSet.of(Modifier.PUBLIC),
                "Class<E>", "clazz"
        );
        emitMediatorSwitch("return (E) new %s()", writer);
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitGetClassModelList(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod("List<Class<? extends RealmObject>>", "getModelClasses", EnumSet.of(Modifier.PUBLIC));
        writer.emitStatement("return MODEL_CLASSES");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    // Emits the control flow for selecting the appropriate proxy class based on the model class
    // Currently it is just if..else, which is inefficient for large amounts amounts of model classes.
    // Consider switching to HashMap or similar.
    private void emitMediatorSwitch(String proxyStatement, JavaWriter writer) throws IOException {
        writer.emitStatement("String classQualifiedName = clazz.getName()");
        writer.emitEmptyLine();
        if (simpleModelClasses.size() == 0) {
            writer.emitStatement("throw new RealmException(%s)", EXCEPTION_MSG);
        } else {
            writer.beginControlFlow("if(classQualifiedName.equals(%s.class.getName()))", simpleModelClasses.get(0));
            writer.emitStatement(proxyStatement, proxyClasses.get(0));
            for (int i = 1; i < simpleModelClasses.size(); i++) {
                writer.nextControlFlow("else if (classQualifiedName.equals(%s.class.getName()))", simpleModelClasses.get(i));
                writer.emitStatement(proxyStatement, proxyClasses.get(i));
            }
            writer.nextControlFlow("else");
            writer.emitStatement("throw new RealmException(%s)", EXCEPTION_MSG);
            writer.endControlFlow();
        }
    }

    private String getProxyClassName(String clazz) {
        return clazz + RealmProxyClassGenerator.PROXY_SUFFIX;
    }

    private String stripPackage(String clazz) {
        String[] parts = clazz.split("\\.");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        } else {
            return clazz;
        }
    }
}