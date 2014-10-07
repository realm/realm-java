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

import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.RealmClass;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@SupportedAnnotationTypes({"io.realm.annotations.RealmClass", "io.realm.annotations.Ignore", "io.realm.annotations.Index"})
@SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
public class RealmProcessor extends AbstractProcessor {
    Set<String> classesToValidate = new HashSet<String>();
    boolean done = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        RealmVersionChecker updateChecker = new RealmVersionChecker(processingEnv);
        updateChecker.executeRealmVersionUpdate();

        for (Element classElement : roundEnv.getElementsAnnotatedWith(RealmClass.class)) {
            String className;
            String packageName;
            List<VariableElement> fields = new ArrayList<VariableElement>();
            List<VariableElement> indexedFields = new ArrayList<VariableElement>();
            List<String> ignoredFields = new ArrayList<String>();
            List<String> expectedGetters = new ArrayList<String>();
            List<String> expectedSetters = new ArrayList<String>();

            // Check the annotation was applied to a Class
            if (!classElement.getKind().equals(ElementKind.CLASS)) {
                error("The RealmClass annotation can only be applied to classes", classElement);
            }
            TypeElement typeElement = (TypeElement) classElement;
            className = typeElement.getSimpleName().toString();

            if (typeElement.toString().endsWith(".RealmObject") || typeElement.toString().endsWith("RealmProxy")) {
                continue;
            }

            note("Processing class " + className);

            classesToValidate.add(typeElement.toString());

            // Get the package of the class
            Element enclosingElement = typeElement.getEnclosingElement();
            if (!enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
                error("The RealmClass annotation does not support nested classes", classElement);
            }

            TypeElement parentElement = (TypeElement) processingEnv.getTypeUtils().asElement(typeElement.getSuperclass());
            if (!parentElement.toString().endsWith(".RealmObject")) {
                error("A RealmClass annotated object must be derived from RealmObject", classElement);
            }

            PackageElement packageElement = (PackageElement) enclosingElement;
            packageName = packageElement.getQualifiedName().toString();

            for (Element element : typeElement.getEnclosedElements()) {
                ElementKind elementKind = element.getKind();
                if (elementKind.equals(ElementKind.FIELD)) {
                    VariableElement variableElement = (VariableElement) element;
                    String fieldName = variableElement.getSimpleName().toString();
                    if (variableElement.getAnnotation(Ignore.class) != null) {
                        // The field has the @Ignore annotation. No need to go any further.
                        ignoredFields.add(fieldName);
                        continue;
                    }

                    if (variableElement.getAnnotation(Index.class) != null) {
                        // The field has the @Index annotation. It's only valid for:
                        // * String
                        String elementTypeCanonicalName = variableElement.asType().toString();
                        if (elementTypeCanonicalName.equals("java.lang.String")) {
                            indexedFields.add(variableElement);
                        } else {
                            error("@Index is only possible for String fields - got " + elementTypeCanonicalName);
                            return true;
                        }
                    }

                    if (!variableElement.getModifiers().contains(Modifier.PRIVATE)) {
                        error("The fields of the model must be private", variableElement);
                    }

                    fields.add(variableElement);
                    expectedGetters.add(fieldName);
                    expectedSetters.add(fieldName);
                } else if (elementKind.equals(ElementKind.METHOD)) {
                    ExecutableElement executableElement = (ExecutableElement) element;

                    if (!executableElement.getModifiers().contains(Modifier.PUBLIC)) {
                        error("The methods of the model must be public", executableElement);
                    }

                    String methodName = executableElement.getSimpleName().toString();
                    String computedFieldName = methodName.startsWith("is")?lowerFirstChar(methodName.substring(2)):lowerFirstChar(methodName.substring(3));
                    if (methodName.startsWith("get") || methodName.startsWith("is")) {
                        boolean found = false;
                        for (VariableElement field : fields) {
                            if (field.getSimpleName().toString().equals(computedFieldName)) {
                                found = true;
                            }
                        }
                        if (ignoredFields.contains(computedFieldName)) {
                            found = true;
                        }
                        if (!found) {
                            error(String.format("No field named %s for the getter %s", computedFieldName, methodName), executableElement);
                        }
                        expectedGetters.remove(computedFieldName);
                    } else if (methodName.startsWith("set")) {
                        boolean found = false;
                        for (VariableElement field : fields) {
                            if (field.getSimpleName().toString().equals(computedFieldName)) {
                                found = true;
                            }
                        }
                        if (ignoredFields.contains(computedFieldName)) {
                            found = true;
                        }
                        if (!found) {
                            error(String.format("No field named %s for the setter %s", computedFieldName, methodName), executableElement);
                        }
                        expectedSetters.remove(computedFieldName);
                    } else {
                        error("Only getters and setters should be defined in model classes", executableElement);
                    }
                }
            }

            for (String expectedGetter : expectedGetters) {
                error("No getter found for field " + expectedGetter);
            }
            for (String expectedSetter : expectedSetters) {
                error("No getter found for field " + expectedSetter);
            }

            RealmProxyClassGenerator sourceCodeGenerator =
                    new RealmProxyClassGenerator(processingEnv, className, packageName, fields, indexedFields);
            try {
                sourceCodeGenerator.generate();
            } catch (IOException e) {
                error(e.getMessage(), classElement);
            } catch (UnsupportedOperationException e) {
                error(e.getMessage(), classElement);
            }
        }

        if (!done) {
            RealmValidationListGenerator validationGenerator = new RealmValidationListGenerator(processingEnv, classesToValidate);
            try {
                validationGenerator.generate();
                done = true;
            } catch (IOException e) {
                error(e.getMessage());
            }
        }

        return true;
    }

    private static String lowerFirstChar(String input) {
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    private void error(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void error(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

    private void note(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }
}
