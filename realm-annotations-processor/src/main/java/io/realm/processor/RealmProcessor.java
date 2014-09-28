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
import io.realm.annotations.RealmClass;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@SupportedAnnotationTypes({"io.realm.annotations.RealmClass", "io.realm.annotations.Ignore"})
@SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_6)
public class RealmProcessor extends AbstractProcessor {
    Set<String> classesToValidate = new HashSet<String>();
    boolean done = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

	RealmUpdateChecker updateChecker = new RealmUpdateChecker();
	updateChecker.executeRealmVersionUpdate();

        for (Element classElement : roundEnv.getElementsAnnotatedWith(RealmClass.class)) {
            String className;
            String packageName;
            List<VariableElement> fields = new ArrayList<VariableElement>();

            // Check the annotation was applied to a Class
            if (!classElement.getKind().equals(ElementKind.CLASS)) {
                error("The RealmClass annotation can only be applied to classes");
                return false;
            }
            TypeElement typeElement = (TypeElement) classElement;
            className = typeElement.getSimpleName().toString();
            classesToValidate.add(className);

            if (typeElement.toString().endsWith(".RealmObject") || typeElement.toString().endsWith("RealmProxy")) {
                continue;
            }

            // Get the package of the class
            Element enclosingElement = typeElement.getEnclosingElement();
            if (!enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
                error("The RealmClass annotation does not support nested classes");
                return false;
            }

            TypeElement parentElement = (TypeElement) processingEnv.getTypeUtils().asElement(typeElement.getSuperclass());
            if (!parentElement.toString().endsWith(".RealmObject")) {
                error("A RealmClass annotated object must be derived from RealmObject");
                return false;
            }

            PackageElement packageElement = (PackageElement) enclosingElement;
            packageName = packageElement.getQualifiedName().toString();

            for (Element element : typeElement.getEnclosedElements()) {
                if (element.getKind().equals(ElementKind.FIELD)) {
                    VariableElement variableElement = (VariableElement) element;
                    if (variableElement.getAnnotation(Ignore.class) != null) {
                        // The field has the @Ignore annotation. No need to go any further.
                        continue;
                    }

                    if (!variableElement.getModifiers().contains(Modifier.PRIVATE)) {
                        error("The fields of the model must be private");
                        return false;
                    }

                    fields.add(variableElement);
                }
            }

            RealmProxyClassGenerator sourceCodeGenerator =
                    new RealmProxyClassGenerator(processingEnv, className, packageName, fields);
            try {
                sourceCodeGenerator.generate();
            } catch (IOException e) {
                error(e.getMessage());
            } catch (UnsupportedOperationException e) {
                error(e.getMessage());
            }
        }

        if (!done) {
            RealmValidationListGenerator validationGenerator = new RealmValidationListGenerator(processingEnv, classesToValidate);
            done = true;
            try {
                validationGenerator.generate();
            } catch (IOException e) {
                error(e.getMessage());
            }
        }

        return true;
    }

    private void error(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }
}
