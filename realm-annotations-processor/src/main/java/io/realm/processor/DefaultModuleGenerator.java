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
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

/**
 * This class is responsible for creating the DefaultRealmModule that contains all known
 * {@link io.realm.annotations.RealmClass}' known at compile time.
 */
public class DefaultModuleGenerator {

    public static final String REALM_PACKAGE_NAME = "io.realm";
    public static final String CLASS_NAME = "DefaultRealmModule";

    private final ProcessingEnvironment env;
    private final Set<String> qualifiedRealmClasses;

    public DefaultModuleGenerator(ProcessingEnvironment env, Set<String> qualifiedRealmClasses) {
        this.env = env;
        this.qualifiedRealmClasses = qualifiedRealmClasses;
    }

    public void generate() throws IOException {
        String qualifiedGeneratedClassName = String.format("%s.%s", REALM_PACKAGE_NAME, CLASS_NAME);
        JavaFileObject sourceFile = env.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));
        writer.setIndent("    ");

        writer.emitPackage(REALM_PACKAGE_NAME);
        writer.emitEmptyLine();
        writer.emitImports(
            "java.util.Collections",
            "java.util.HashSet",
            "java.util.Set",
            "io.realm.internal.modules.RealmClassCollection"
        );
        writer.emitEmptyLine();

        writer.beginType(
                qualifiedGeneratedClassName,        // full qualified name of the item to generate
                "class",                            // the type of the item
                Collections.<Modifier>emptySet(),   // modifiers to apply
                "RealmClassCollection");            // class to extend
        writer.emitEmptyLine();

        emitFields(writer);
        emitGetModuleClasses(writer);

        writer.endType();
        writer.close();
    }

    private void emitFields(JavaWriter writer) throws IOException {
        writer.emitField("Set<Class<? extends RealmObject>>", "MODEL_CLASSES", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL));
        writer.beginInitializer(true);
        writer.emitStatement("Set<Class<? extends RealmObject>> modelClasses = new HashSet<Class<? extends RealmObject>>()");
        for (String clazz : qualifiedRealmClasses) {
            writer.emitStatement("modelClasses.add(%s.class)", clazz);
        }
        writer.emitStatement("MODEL_CLASSES = Collections.unmodifiableSet(modelClasses)");
        writer.endInitializer();
        writer.emitEmptyLine();
    }

    private void emitGetModuleClasses(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "Set<Class<? extends RealmObject>>",
                "getModuleClasses",
                EnumSet.of(Modifier.PUBLIC)
        );
        writer.emitStatement("return MODEL_CLASSES");
        writer.endMethod();
        writer.emitEmptyLine();
    }
}

