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
import java.util.UUID;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import io.realm.annotations.RealmClass;
import io.realm.annotations.internal.RealmModule;

/**
 * The RealmProcessor is responsible for creating the plumbing that connects the RealmObjects to a Realm. The process
 * for doing so is summarized below and then described in more detail:
 *
 * SUMMARY:
 * 1 ) Create proxy classes for all classes marked with @RealmClass. They are named <modelClass>RealmProxy.java
 * 2 ) Create a RealmProxyMediator that can map between model classes and static helper methods in the proxy classes.
 *     It is named <UUID>.java
 * 3 ) Create a binder class for all classes marked with @RealmModule. They are named <moduleName>Binder.java
 * 4 ) Create DefaultRealmModule if no custom library module is defined.
 * 5 ) Create DefaultRealmModuleBinder if needed.
 *
 *
 * WHY:
 * *
 * 1) A RealmObjectProxy object is created for each class annotated with {@link io.realm.annotations.RealmClass}. This
 * proxy extends the original model class and rewires all field access to point to the native Realm data instead of Java
 * objects. It also adds a lot of static helper methods to the class.
 *
 * 2) A RealmProxyMediator class is generated. This class has an interface that matches the static helper methods for
 * the proxy classes. All access to to these static helper methods should be done through this RealmProxyMediator.
 *
 * This is done as Realm otherwise would have no other option than using reflection for accessing the static methods
 * as they are not known until after the annotation processor has run. It also allows ProGuard to obfuscate all model
 * classes as all access to the methods happen through the RealmProxyMediator.
 *
 * To allow Realm to be used in both library projects and app code, the RealmProxyMediator will be given a random name,
 * a UUID.
 *
 * 3) For each class annotated with @RealmModule a matching Binder class is created. The purpose of the Binder class
 * is to add a reference to the created RealmProxyMediator. This is done by extending the original module class and
 * overriding a getter method. All modules should extend the class RealmClassCollection.
 *
 * 4) The annotation processor is is either in "library" mode or in "app" mode. This is defined by having a class
 * annotated with @RealmModule(library = true). Modules with library = true and library = false cannot be mixed, and
 * will throw an error. If no library modules are defined, we will create a DefaultRealmModule containing all known
 * RealmObjects. Realm automatically knows about this module.
 *
 * 5) Just like in step 3, a Binder class is created for the Default module if needed.
 *
 *
 * This means the workflow when instantiating a Realm on runtime is the following:
 *
 * 1) Create a Realm.
 *
 * 2) It is assigned one or more modules. If no module is assigned, the default module is used.
 *
 * 3) From each module Realm retrieves the available classes and the RealmProxyMediator associated with these classes.
 *
 * 4) Each time a static helper method is needed, the Realm can now delegate these method calls to the appropriate
 *    RealmProxyMediator which in turn will delegate the method call to the appropriate RealmObjectProxy class.
 *
 *
 * DESIGN GOALS:
 * While the above process might seem overly complicated it is necessary if we want to support the following design
 * goals:
 *
 * - Minimize reflection.
 * - As much as possible can be obfuscated.
 * - Library projects must be able to use Realm without interfering with app code.
 * - App code must be able to use model classes provided by library code.
 * - It should work for app developers out of the box (ie. put the burden on the library developer)
 */
@SupportedAnnotationTypes({
        "io.realm.annotations.RealmClass",
        "io.realm.annotations.Ignore",
        "io.realm.annotations.Index",
        "io.realm.annotations.PrimaryKey"
})
public class RealmProcessor extends AbstractProcessor {

    Set<ClassMetaData> classesToValidate = new HashSet<ClassMetaData>();
    private boolean hasProcessedModules = false;

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        RealmVersionChecker updateChecker = RealmVersionChecker.getInstance(processingEnv);
        updateChecker.executeRealmVersionUpdate();
        Utils.initialize(processingEnv);

        // Create all proxy classes
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

        if (!hasProcessedModules) {
            hasProcessedModules = true;
            return processModules(roundEnv);
        }

        return true;
    }

    private boolean processModules(RoundEnvironment roundEnv) {
        // Check that modules are setup correctly
        Set<String> modules = new HashSet<String>();
        Set<String> libraryModules = new HashSet<String>();
        for (Element classElement : roundEnv.getElementsAnnotatedWith(RealmModule.class)) {
            if (!classElement.getKind().equals(ElementKind.CLASS)) {
                Utils.error("The RealmModule annotation can only be applied to classes", classElement);
            }

            // Check that the class extends RealmClassCollection
            TypeElement typeElement = (TypeElement) classElement;
            TypeElement parentElement = (TypeElement) processingEnv.getTypeUtils().asElement(typeElement.getSuperclass());
            if (!parentElement.toString().endsWith(".RealmCollectionClass")) {
                Utils.error("A RealmModule annotated object must be derived from RealmCollectionClass", classElement);
            }

            RealmModule module = classElement.getAnnotation(RealmModule.class);
            if (module.library()) {
                libraryModules.add(((TypeElement) classElement).getQualifiedName().toString());
            } else {
                modules.add(((TypeElement) classElement).getQualifiedName().toString());
            }
        }

        if (modules.size() > 0 && libraryModules.size() > 0) {
            Utils.error("Normal modules and library modules cannot be mixed in the same project");
            return true;
        }

        // Create DefaultModule if needed
        if (libraryModules.size() == 0 ) {
            Utils.note("Creating DefaultRealmModule");
            DefaultModuleGenerator defaultModuleGenerator = new DefaultModuleGenerator(processingEnv, classesToValidate);
            try {
                defaultModuleGenerator.generate();
                modules.add(DefaultModuleGenerator.CLASS_NAME);
            } catch (IOException e) {
                Utils.error(e.getMessage());
            }
        }

        // Create ProxyMediator for all RealmClasses in this project.
        String proxyMediatorName;
        if (libraryModules.size() > 0) {
            // Autogenerate random name for library module RealmProxyMediator classes
            // This should prevent conflicts with other libraries
            proxyMediatorName = UUID.randomUUID().toString().replace("-", "");
        } else {
            // Default RealmProxyMediator only used by "app" code
            proxyMediatorName = "DefaultRealmProxyMediator";
        }

        RealmProxyMediatorGenerator mediatorImplGenerator = new RealmProxyMediatorGenerator(processingEnv, proxyMediatorName, classesToValidate);
        try {
            Utils.note("Creating RealmProxyMediator");
            mediatorImplGenerator.generate();
        } catch (IOException e) {
            Utils.error(e.getMessage());
            return true;
        }

        // Create Binder classes for all modules
        Set<String> binders = (modules.size() > 0) ? modules : libraryModules;
        for (String binder : binders) {
            ModuleBinderGenerator binderGenerator = new ModuleBinderGenerator(processingEnv, Utils.stripPackage(binder), proxyMediatorName);
            try {
                binderGenerator.generate();
            } catch (IOException e) {
                Utils.error(e.getMessage());
                return true;
            }
        }
        return false;
    }
}
