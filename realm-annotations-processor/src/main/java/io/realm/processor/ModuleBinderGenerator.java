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
import java.util.Collections;
import java.util.EnumSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

/**
 * This class is responsible for creating the the RealmModuleBinder class that connects the RealmObject classes of the
 * RealmModule with their appropriate RealmProxyMediator.
 */
public class ModuleBinderGenerator {

    public static final String REALM_PACKAGE_NAME = "io.realm";

    private final ProcessingEnvironment env;
    private final String realmProxyMediatorClassName;
    private final String qualifiedModuleClassName;

    public ModuleBinderGenerator(ProcessingEnvironment env, String moduleClassName, String realmProxyMediatorClassName) {
        this.env = env;
        this.qualifiedModuleClassName = moduleClassName;
        this.realmProxyMediatorClassName = realmProxyMediatorClassName;
    }

    public void generate() throws IOException {
        String qualifiedGeneratedClassName = String.format("%s.%sBinder", REALM_PACKAGE_NAME, qualifiedModuleClassName);
        JavaFileObject sourceFile = env.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));
        writer.setIndent("    ");

        writer.emitPackage(REALM_PACKAGE_NAME);
        writer.emitEmptyLine();
        writer.emitImports(
                "io.realm.internal.RealmProxyMediator"

        );
        writer.emitEmptyLine();

        writer.beginType(
                qualifiedGeneratedClassName,        // full qualified name of the item to generate
                "class",                            // the type of the item
                Collections.<Modifier>emptySet(),   // modifiers to apply
                qualifiedModuleClassName);          // class to extend
        writer.emitEmptyLine();

        emitGetMediatorMethod(writer);

        writer.endType();
        writer.close();
    }

    private void emitGetMediatorMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "RealmProxyMediator",
                "getProxyMediator",
                EnumSet.of(Modifier.PUBLIC)
        );
        writer.emitStatement("return %s.getInstance()", realmProxyMediatorClassName);
        writer.endMethod();
        writer.emitEmptyLine();
    }
}

