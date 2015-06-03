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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import io.realm.annotations.internal.RealmModule;

/**
 * Utility class for holding metadata for the Realm modules.
 */
public class ModuleMetaData {

    private final Set<ClassMetaData> availableClasses;
    private final RoundEnvironment env;
    private Map<String, Set<ClassMetaData>> modules = new HashMap<String, Set<ClassMetaData>>();
    private Map<String, Set<ClassMetaData>> libraryModules = new HashMap<String, Set<ClassMetaData>>();
    private Map<String, ClassMetaData> classMetaData = new HashMap<String, ClassMetaData>();
    private boolean shouldCreateDefaultModule;

    public ModuleMetaData(RoundEnvironment env, Set<ClassMetaData> availableClasses) {
        this.env = env;
        this.availableClasses = availableClasses;
        for (ClassMetaData classMetaData : availableClasses) {
            this.classMetaData.put(classMetaData.getFullyQualifiedClassName(), classMetaData);
        }
    }

    /**
     * Build the meta data structures for this class. Any errors or messages will be posted on the provided Messager.
     *
     * @return True if meta data was correctly created and processing can continue, false otherwise.
     */
    public boolean generate(ProcessingEnvironment processingEnv) {

        // Check that modules are setup correctly
        for (Element classElement : env.getElementsAnnotatedWith(RealmModule.class)) {
            String classSimpleName = classElement.getSimpleName().toString();

            // Check that the annotation is only applied to a class
            if (!classElement.getKind().equals(ElementKind.CLASS)) {
                Utils.error("The RealmModule annotation can only be applied to classes", classElement);
                return false;
            }

            // Check that allClasses and classes are not set at the same time
            RealmModule module = classElement.getAnnotation(RealmModule.class);
            Utils.note("Processing " + classSimpleName);
            if (module.allClasses() && module.classes().length > 0) {
                Utils.error("Setting @RealmModule(allClasses=true) will override @RealmModule(classes={...}) in " + classSimpleName);
                return false;
            }

            // Check that classes added are proper Realm model classes
            String qualifiedName = ((TypeElement) classElement).getQualifiedName().toString();
            Set<ClassMetaData> classes;
            if (module.allClasses()) {
                classes = availableClasses;
            } else {
                classes = new HashSet<ClassMetaData>();
                for (Class<?> clazz : module.classes()) {
                    if (!clazz.getSuperclass().toString().endsWith("RealmObject")) {
                        Utils.error(clazz.getSimpleName() + " is not extending RealmObject. Only RealmObjects can be " +
                                "part of a module.");
                        return false;
                    }
                    ClassMetaData metadata = classMetaData.get(clazz.getName());
                    if (metadata == null) {
                        Utils.error(Utils.stripPackage(qualifiedName) + " could not be added to the module. It is not " +
                                "possible to add classes which are part of another library.");
                        return false;
                    }
                    classes.add(metadata);
                }
            }

            // Create either a Library or App module
            if (module.library()) {
                libraryModules.put(qualifiedName, classes);
            } else {
                modules.put(qualifiedName, classes);
            }
        }

        // Check that app and library modules are not mixed
        if (modules.size() > 0 && libraryModules.size() > 0) {
            Utils.error("Normal modules and library modules cannot be mixed in the same project");
            return false;
        }

        // Add default realm module if needed.
        if (libraryModules.size() == 0) {
            shouldCreateDefaultModule = true;
            String defautModuleName = Constants.REALM_PACKAGE_NAME + "." + Constants.DEFAULT_MODULE_CLASS_NAME;
            modules.put(defautModuleName, availableClasses);
        }

        return true;
    }

    /**
     * Returns all module classes and the RealmObjects they know of.
     */
    public Map<String, Set<ClassMetaData>> getAllModules() {
        Map<String, Set<ClassMetaData>> allModules = new HashMap<String, Set<ClassMetaData>>();
        allModules.putAll(modules);
        allModules.putAll(libraryModules);
        return allModules;
    }

    /**
     * Returns {@code true} if the DefaultRealmModule.java file should be created.
     */
    public boolean shouldCreateDefaultModule() {
        return shouldCreateDefaultModule;
    }
}
