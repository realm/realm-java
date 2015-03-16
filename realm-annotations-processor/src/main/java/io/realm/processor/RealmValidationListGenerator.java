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
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class RealmValidationListGenerator {
    private ProcessingEnvironment processingEnvironment;
    private Set<ClassMetaData> classesToValidate = new HashSet<ClassMetaData>();

    private static final String CLASS_NAME = "ValidationList";

    public RealmValidationListGenerator(ProcessingEnvironment processingEnvironment, Set<ClassMetaData> classesToValidate) {
        this.processingEnvironment = processingEnvironment;
        this.classesToValidate = classesToValidate;
    }

    public void generate() throws IOException {
        String qualifiedGeneratedClassName = String.format("%s.%s", Constants.REALM_PACKAGE_NAME, CLASS_NAME);
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));
        writer.setIndent("    ");

        writer.emitPackage(Constants.REALM_PACKAGE_NAME);
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
        for (ClassMetaData classToValidate : classesToValidate) {
            entries.add(String.format("\"%s\"", classToValidate.getSimpleClassName()));
        }
        String statementSection = joinStringList(entries, ", ");
        writer.emitStatement("return Arrays.asList(%s)", statementSection);
        writer.endMethod();
        writer.emitEmptyLine();

        writer.endType();
        writer.close();
    }

    public static String joinStringList(List<String> strings, String separator) {
        StringBuilder stringBuilder = new StringBuilder();
        ListIterator<String> iterator = strings.listIterator();
        while (iterator.hasNext()) {
            int index = iterator.nextIndex();
            String item = iterator.next();

            if (index > 0) {
                stringBuilder.append(separator);
            }
            stringBuilder.append(item);
        }
        return stringBuilder.toString();
    }
}