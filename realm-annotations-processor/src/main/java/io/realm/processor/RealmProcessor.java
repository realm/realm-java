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
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import io.realm.annotations.RealmClass;

@SupportedAnnotationTypes({
        "io.realm.annotations.RealmClass",
        "io.realm.annotations.Ignore",
        "io.realm.annotations.Index",
        "io.realm.annotations.PrimaryKey",
        "io.realm.annotations.Nullable"
})
public class RealmProcessor extends AbstractProcessor {
    Set<ClassMetaData> classesToValidate = new HashSet<ClassMetaData>();
    boolean done = false;

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        RealmVersionChecker updateChecker = RealmVersionChecker.getInstance(processingEnv);
        updateChecker.executeRealmVersionUpdate();
        Utils.initialize(processingEnv);

        for (Element classElement : roundEnv.getElementsAnnotatedWith(RealmClass.class)) {

            // Check the annotation was applied to a Class
            if (!classElement.getKind().equals(ElementKind.CLASS)) {
                Utils.error("The RealmClass annotation can only be applied to classes", classElement);
            }
            ClassMetaData metadata = new ClassMetaData(processingEnv, (TypeElement) classElement);
            if (!metadata.isModelClass()) {
                continue;
            }
            Utils.note("Processing class " + metadata.getSimpleClassName());
            boolean success = metadata.generateMetaData(processingEnv.getMessager());
            if (!success) {
                done = true;
                return true; // Abort processing by claiming all annotations
            }
            classesToValidate.add(metadata);

            RealmProxyClassGenerator sourceCodeGenerator = new RealmProxyClassGenerator(processingEnv, metadata);
            try {
                sourceCodeGenerator.generate();
            } catch (IOException e) {
                Utils.error(e.getMessage(), classElement);
            } catch (UnsupportedOperationException e) {
                Utils.error(e.getMessage(), classElement);
            }
        }

        if (!done) {
            RealmValidationListGenerator validationGenerator = new RealmValidationListGenerator(processingEnv, classesToValidate);
            RealmJSonImplGenerator jsonGenerator = new RealmJSonImplGenerator(processingEnv, classesToValidate);
            try {
                validationGenerator.generate();
                jsonGenerator.generate();
                done = true;
            } catch (IOException e) {
                Utils.error(e.getMessage());
            }
        }

        return true;
    }
}
