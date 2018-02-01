/*
 * Copyright 2016 Realm Inc.
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
import java.util.Locale;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

import io.realm.annotations.Ignore;


public class RealmProxyInterfaceGenerator {
    private ProcessingEnvironment processingEnvironment;
    private ClassMetaData metaData;
    private final String className;

    public RealmProxyInterfaceGenerator(ProcessingEnvironment processingEnvironment, ClassMetaData metaData) {
        this.processingEnvironment = processingEnvironment;
        this.metaData = metaData;
        this.className = metaData.getFullyQualifiedClassName();
    }

    public void generate() throws IOException {
        String qualifiedGeneratedInterfaceName =
                String.format(Locale.US, "%s.%s", Constants.REALM_PACKAGE_NAME, Utils.getProxyInterfaceName(className));
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedInterfaceName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));

        writer.setIndent(Constants.INDENT);

        writer
                .emitPackage(Constants.REALM_PACKAGE_NAME)
                .emitEmptyLine()
                .beginType(qualifiedGeneratedInterfaceName, "interface", EnumSet.of(Modifier.PUBLIC));
        for (VariableElement field : metaData.getFields()) {
            if (field.getModifiers().contains(Modifier.STATIC) || (field.getAnnotation(Ignore.class) != null)) {
                continue;
            }
            // The field is neither static nor ignored
            String fieldName = field.getSimpleName().toString();
            String fieldTypeCanonicalName = field.asType().toString();
            writer
                    .beginMethod(
                            fieldTypeCanonicalName,
                            metaData.getInternalGetter(fieldName),
                            EnumSet.of(Modifier.PUBLIC))
                    .endMethod();

            // MutableRealmIntegers do not have setters.
            if (Utils.isMutableRealmInteger(field)) { continue; }
            writer
                    .beginMethod(
                            "void",
                            metaData.getInternalSetter(fieldName),
                            EnumSet.of(Modifier.PUBLIC),
                            fieldTypeCanonicalName,
                            "value")
                    .endMethod();
        }

        // backlinks are final and have only a getter.
        for (Backlink backlink : metaData.getBacklinkFields()) {
            writer
                    .beginMethod(
                            backlink.getTargetFieldType(),
                            metaData.getInternalGetter(backlink.getTargetField()),
                            EnumSet.of(Modifier.PUBLIC))
                    .endMethod();
        }

        writer.endType();
        writer.close();
    }
}
