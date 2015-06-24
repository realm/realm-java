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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.EnumSet;

import javax.lang.model.element.Modifier;
import javax.tools.SimpleJavaFileObject;

// Helper class for creating RealmObject java files
public class TestRealmObjectFileObject extends SimpleJavaFileObject {
    private StringWriter stringWriter;

    private TestRealmObjectFileObject(String name, StringWriter stringWriter) {
        super(URI.create(name + ".java"), Kind.SOURCE);
        this.stringWriter = stringWriter;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return stringWriter.getBuffer();
    }

    // Helper function to create a Realm object java file with a single field.
    public static TestRealmObjectFileObject getSingleFieldInstance(String className,
                                                            String annotationToField,
                                                            String fieldType,
                                                            String fieldName) throws IOException {
        String FieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());
        StringWriter stringWriter = new StringWriter();
        JavaWriter writer = new JavaWriter(stringWriter);

        // Package name
        writer.emitPackage("some.test");

        // Import Realm classes
        writer.emitImports("io.realm.*");
        writer.emitImports("io.realm.annotations.*");

        // Begin the class definition
        writer.beginType(
                className, // full qualified name of the item to generate
                "class",                     // the type of the item
                EnumSet.of(Modifier.PUBLIC), // modifiers to apply
                "RealmObject")                   // class to extend
                .emitEmptyLine();

        // Declaration of field
        writer.emitAnnotation(annotationToField);
        writer.emitField(fieldType, fieldName, EnumSet.of(Modifier.PRIVATE));

        // Getter
        writer.beginMethod(
                fieldType, // Return type
                "get" + FieldName, // Method name
                EnumSet.of(Modifier.PUBLIC)); // Modifiers
        writer.emitStatement("return " +  fieldName);
        writer.endMethod();

        // Setter
        writer.beginMethod(
                "void", // Return type
                "set"+ FieldName, // Method name
                EnumSet.of(Modifier.PUBLIC),
                fieldType, fieldName); // Modifiers
        writer.emitStatement("this." + fieldName + "=" + fieldName);
        writer.endMethod();

        writer.endType();

        return new TestRealmObjectFileObject(className, stringWriter);
    }
}
