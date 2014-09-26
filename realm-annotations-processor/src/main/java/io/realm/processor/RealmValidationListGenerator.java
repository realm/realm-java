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

import java.util.*;
import java.io.BufferedWriter;
import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import com.google.common.base.Joiner;

public class RealmValidationListGenerator {
    private ProcessingEnvironment processingEnvironment;
    private Set<String> classesToValidate = new HashSet<String>();

    private static final String REALM_PACKAGE_NAME = "io.realm";
    private static final String CLASS_NAME = "ValidationList";
    private static final String PROXY_CLASS_SUFFIX = "RealmProxy";


    public RealmValidationListGenerator(ProcessingEnvironment processingEnvironment, Set<String> classesToValidate) {
        this.processingEnvironment = processingEnvironment;
        this.classesToValidate = classesToValidate;
    }

    public void generate() throws IOException {
        String qualifiedGeneratedClassName = String.format("%s.%s", REALM_PACKAGE_NAME, CLASS_NAME);
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));
        writer.setIndent("    ");

        writer.emitPackage(REALM_PACKAGE_NAME);
        writer.emitEmptyLine();

        writer.emitImports("java.util.Arrays", "java.util.List");
        writer.emitEmptyLine();

        // Begin the class definition
        writer.beginType(
                qualifiedGeneratedClassName, // full qualified name of the item to generate
                "class",                     // the type of the item
                EnumSet.of(Modifier.PUBLIC), // modifiers to apply
                null);                       // class to extend
        writer.emitEmptyLine();

        writer.beginMethod("List<String>", "getProxyClasses", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC));
        List<String> entries = new ArrayList<String>();
        for (String classToValidate : classesToValidate) {
            entries.add(String.format("\"%s.%s%s\"", REALM_PACKAGE_NAME, classToValidate, PROXY_CLASS_SUFFIX));
        }
        String statementSection = Joiner.on(", ").join(entries);
        writer.emitStatement("return Arrays.asList(%s)", statementSection);
        writer.endMethod();
        writer.emitEmptyLine();

        writer.endType();
        writer.close();
    }
}