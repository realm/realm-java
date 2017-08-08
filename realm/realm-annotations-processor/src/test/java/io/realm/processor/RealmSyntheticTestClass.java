/*
 * Copyright 2015-2017 Realm Inc.
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.tools.SimpleJavaFileObject;

// Helper class for creating RealmObject java files
public class RealmSyntheticTestClass extends SimpleJavaFileObject {
    public static class Field {
        private final Builder builder;
        private String name;
        private String type;
        private String initializer;
        private boolean hasGetter = true;
        private boolean hasSetter = true;
        private EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PRIVATE);
        private final List<String> annotations = new ArrayList<String>();

        Field(Builder builder) {
            this.builder = builder;
        }

        public Field name(String name) {
            this.name = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
            return this;
        }

        public Field type(String type) {
            this.type = type;
            return this;
        }

        public Field modifiers(Modifier... modifiers) {
            this.modifiers = EnumSet.of(modifiers[0], modifiers); // yuk
            return this;
        }

        public Field clearAnnotations() {
            this.annotations.clear();
            return this;
        }

        public Field annotation(String annotation) {
            this.annotations.add(annotation);
            return this;
        }

        public Field initializer(String initializer) {
            this.initializer = initializer;
            return this;
        }

        public Field hasGetter(boolean hasGetter) {
            this.hasGetter = hasGetter;
            return this;
        }

        public Field hasSetter(boolean hasSetter) {
            this.hasSetter = hasSetter;
            return this;
        }

        public Builder builder() {
            return builder;
        }
    }

    public static class Builder {
        private final List<Field> fields = new ArrayList<Field>();
        private String name;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        // Note: this returns the new field, not the builder.
        // To get the builder back, use Field.builder()
        public Field field() {
            Field f = new Field(this);
            fields.add(f);
            return f;
        }

        // Convenience method to support legacy usage
        public Builder field(String name, String type, String annotation) {
            field().name(name).type(type).annotation(annotation);
            return this;
        }

        public RealmSyntheticTestClass build() throws IOException {
            StringWriter stringWriter = new StringWriter();
            JavaWriter writer = new JavaWriter(stringWriter);

            // Package name
            writer.emitPackage("some.test");

            // Import Realm classes
            writer.emitImports("io.realm.*");
            writer.emitImports("io.realm.annotations.*");

            // Begin the class definition
            writer.beginType(
                    name,                        // full qualified name of the item to generate
                    "class",                     // the type of the item
                    EnumSet.of(Modifier.PUBLIC), // modifiers to apply
                    "RealmObject")               // class to extend
                    .emitEmptyLine();

            for (Field field : fields) { generateField(writer, field); }

            writer.endType();

            return new RealmSyntheticTestClass(stringWriter, name);
        }

        private void generateField(JavaWriter writer, Field field) throws IOException {
            if (field.name == null) { throw new IllegalArgumentException("A field must have a name"); }
            if (field.type == null) { throw new IllegalArgumentException("A field must have a type"); }

            // Declaration of field
            for (String annotation : field.annotations) { writer.emitAnnotation(annotation); }
            writer.emitField(field.type, field.name, field.modifiers, field.initializer);

            if (field.hasSetter) { emitSetter(writer, field); }
            if (field.hasGetter) { emitGetter(writer, field); }
       }

        private void emitSetter(JavaWriter writer, Field field) throws IOException {
            // Setter
            writer.beginMethod(
                    "void", // Return type
                    "set" + field.name, // Method name
                    EnumSet.of(Modifier.PUBLIC), field.type, field.name); // Modifiers
            writer.emitStatement("realmSet$" + field.name + "(" + field.name + ")");
            writer.endMethod();

            // Realm Setter
            writer.beginMethod(
                    "void", // Return type
                    "realmSet$" + field.name, // Method name
                    EnumSet.of(Modifier.PUBLIC), field.type, field.name); // Modifiers
            writer.emitStatement("this." + field.name + "=" + field.name);
            writer.endMethod();
        }

        private void emitGetter(JavaWriter writer, Field field) throws IOException {
            // Getter
            writer.beginMethod(
                    field.type, // Return type
                    "get" + field.name, // Method name
                    EnumSet.of(Modifier.PUBLIC)); // Modifiers
            writer.emitStatement("return realmGet$" + field.name + "()");
            writer.endMethod();

            // Realm Getter
            writer.beginMethod(
                    field.type, // Return type
                    "realmGet$" + field.name, // Method name
                    EnumSet.of(Modifier.PUBLIC)); // Modifiers
            writer.emitStatement("return " + field.name);
            writer.endMethod();
        }
    }

    private final StringWriter stringWriter;

    private RealmSyntheticTestClass(StringWriter stringWriter, String name) {
        super(URI.create(name + ".java"), Kind.SOURCE);
        this.stringWriter = stringWriter;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return stringWriter.getBuffer();
    }
}
