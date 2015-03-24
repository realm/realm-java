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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import io.realm.annotations.internal.RealmModule;

/**
 * This class is responsible for creating the DefaultRealmModule that contains all known
 * {@link io.realm.annotations.RealmClass}' known at compile time.
 */
public class DefaultModuleGenerator {

    private final ProcessingEnvironment env;

    public DefaultModuleGenerator(ProcessingEnvironment env) {
        this.env = env;
    }

    public void generate() throws IOException {
        String qualifiedGeneratedClassName = String.format("%s.%s", Constants.REALM_PACKAGE_NAME, Constants.DEFAULT_MODULE_CLASS_NAME);
        JavaFileObject sourceFile = env.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));
        writer.setIndent("    ");

        writer.emitPackage(Constants.REALM_PACKAGE_NAME);
        writer.emitEmptyLine();

        Map<String, Boolean> attributes = new HashMap<String, Boolean>();
        attributes.put("allClasses", Boolean.TRUE);
        writer.emitAnnotation(RealmModule.class, attributes);
        writer.beginType(
                qualifiedGeneratedClassName,        // full qualified name of the item to generate
                "class",                            // the type of the item
                Collections.<Modifier>emptySet(),   // modifiers to apply
                null);                              // class to extend
        writer.emitEmptyLine();

        writer.endType();
        writer.close();
    }
}

