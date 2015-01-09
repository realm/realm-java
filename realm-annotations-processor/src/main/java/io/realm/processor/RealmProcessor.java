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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.RealmClass;


@SupportedAnnotationTypes({"io.realm.annotations.RealmClass", "io.realm.annotations.Ignore", "io.realm.annotations.Index"})
public class RealmProcessor extends AbstractProcessor {
    Set<String> classesToValidate = new HashSet<String>();
    boolean done = false;

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        RealmVersionChecker updateChecker = new RealmVersionChecker(processingEnv);
        updateChecker.executeRealmVersionUpdate();

        for (Element classElement : roundEnv.getElementsAnnotatedWith(RealmClass.class)) {
            String className;
            String packageName;
            List<VariableElement> fields = new ArrayList<VariableElement>();
            List<VariableElement> indexedFields = new ArrayList<VariableElement>();
            Set<VariableElement> ignoredFields = new HashSet<VariableElement>();
            Set<String> expectedGetters = new HashSet<String>();
            Set<String> expectedSetters = new HashSet<String>();
            Set<ExecutableElement> methods = new HashSet<ExecutableElement>();
            Map<String, String> getters = new HashMap<String, String>();
            Map<String, String> setters = new HashMap<String, String>();

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
                        ignoredFields.add(variableElement);
                        continue;
                    }

                    if (variableElement.getAnnotation(Index.class) != null) {
                        // The field has the @Index annotation. It's only valid for:
                        // * String
                        String elementTypeCanonicalName = variableElement.asType().toString();
                        if (elementTypeCanonicalName.equals("java.lang.String")) {
                            indexedFields.add(variableElement);
                        } else {
                            error("@Index is only applicable to String fields - got " + element);
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
                    methods.add(executableElement);
                }
            }

            List<String> fieldNames = new ArrayList<String>();
            List<String> ignoreFieldNames = new ArrayList<String>();
            for (VariableElement field : fields) {
                fieldNames.add(field.getSimpleName().toString());
            }
            for (VariableElement ignoredField : ignoredFields) {
                fieldNames.add(ignoredField.getSimpleName().toString());
                ignoreFieldNames.add(ignoredField.getSimpleName().toString());
            }

            for (ExecutableElement executableElement : methods) {

                String methodName = executableElement.getSimpleName().toString();

                // Check the modifiers of the method
                Set<Modifier> modifiers = executableElement.getModifiers();
                if (modifiers.contains(Modifier.STATIC)) {
                    continue; // We're cool with static methods. Move along!
                } else if (!modifiers.contains(Modifier.PUBLIC)) {
                    error("The methods of the model must be public", executableElement);
                }

                if (methodName.startsWith("get") || methodName.startsWith("is")) {
                    boolean found = false;

                    if (methodName.startsWith("is")) {
                        String methodMinusIs = methodName.substring(2);
                        String methodMinusIsCapitalised = lowerFirstChar(methodMinusIs);
                        if (fieldNames.contains(methodName)) { // isDone -> isDone
                            expectedGetters.remove(methodName);
                            if (!ignoreFieldNames.contains(methodName)) {
                                getters.put(methodName, methodName);
                            }
                            found = true;
                        } else if (fieldNames.contains(methodMinusIs)) {  // mDone -> ismDone
                            expectedGetters.remove(methodMinusIs);
                            if (!ignoreFieldNames.contains(methodMinusIs)) {
                                getters.put(methodMinusIs, methodName);
                            }
                            found = true;
                        } else if (fieldNames.contains(methodMinusIsCapitalised)) { // done -> isDone
                            expectedGetters.remove(methodMinusIsCapitalised);
                            if (!ignoreFieldNames.contains(methodMinusIsCapitalised)) {
                                getters.put(methodMinusIsCapitalised, methodName);
                            }
                            found = true;
                        }
                    }

                    if (!found && methodName.startsWith("get")) {
                        String methodMinusGet = methodName.substring(3);
                        String methodMinusGetCapitalised = lowerFirstChar(methodMinusGet);
                        if (fieldNames.contains(methodMinusGet)) { // mPerson -> getmPerson
                            expectedGetters.remove(methodMinusGet);
                            if (!ignoreFieldNames.contains(methodMinusGet)) {
                                getters.put(methodMinusGet, methodName);
                            }
                            found = true;
                        } else if (fieldNames.contains(methodMinusGetCapitalised)) { // person -> getPerson
                            expectedGetters.remove(methodMinusGetCapitalised);
                            if (!ignoreFieldNames.contains(methodMinusGetCapitalised)) {
                                getters.put(methodMinusGetCapitalised, methodName);
                            }
                            found = true;
                        }
                    }

                    if (!found) {
                        note(String.format("Getter %s is not associated to any field", methodName));
                    }
                } else if (methodName.startsWith("set")) {
                    boolean found = false;

                    String methodMinusSet = methodName.substring(3);
                    String methodMinusSetCapitalised = lowerFirstChar(methodMinusSet);
                    String methodMenusSetPlusIs = "is" + methodMinusSet;

                    if (fieldNames.contains(methodMinusSet)) { // mPerson -> setmPerson
                        expectedSetters.remove(methodMinusSet);
                        if (!ignoreFieldNames.contains(methodMinusSet)) {
                            setters.put(methodMinusSet, methodName);
                        }
                        found = true;
                    } else if (fieldNames.contains(methodMinusSetCapitalised)) { // person -> setPerson
                        expectedSetters.remove(methodMinusSetCapitalised);
                        if (!ignoreFieldNames.contains(methodMinusSetCapitalised)) {
                            setters.put(methodMinusSetCapitalised, methodName);
                        }
                        found = true;
                    } else if (fieldNames.contains(methodMenusSetPlusIs)) { // isReady -> setReady
                        expectedSetters.remove(methodMenusSetPlusIs);
                        if (!ignoreFieldNames.contains(methodMenusSetPlusIs)) {
                            setters.put(methodMenusSetPlusIs, methodName);
                        }
                        found = true;
                    }

                    if (!found) {
                        note(String.format("Setter %s is not associated to any field", methodName));
                    }
                } else {
                    error("Only getters and setters should be defined in model classes", executableElement);
                }
            }

            for (String expectedGetter : expectedGetters) {
                error("No getter found for field " + expectedGetter);
            }
            for (String expectedSetter : expectedSetters) {
                error("No setter found for field " + expectedSetter);
            }

            RealmProxyClassGenerator sourceCodeGenerator =
                    new RealmProxyClassGenerator(processingEnv, className, packageName, fields, getters, setters, indexedFields);
            try {
                sourceCodeGenerator.generate();
            } catch (IOException e) {
                error(e.getMessage(), classElement);
            } catch (UnsupportedOperationException e) {
                error(e.getMessage(), classElement);
            }
        }

        if (!done) {
            RealmProxyMediatorImplGenerator mediatorImplGenerator = new RealmProxyMediatorImplGenerator(processingEnv, classesToValidate);
            try {
                mediatorImplGenerator.generate();
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
