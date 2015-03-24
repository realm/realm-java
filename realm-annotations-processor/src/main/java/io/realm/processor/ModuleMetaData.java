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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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
    private boolean shouldCreateDefaultModule = false;

    public ModuleMetaData(RoundEnvironment env, Set<ClassMetaData> availableClasses) {
        this.env = env;
        this.availableClasses = availableClasses;
        for (ClassMetaData metadata : availableClasses) {
            classMetaData.put(metadata.getFullyQualifiedClassName(), metadata);
        }
    }

    public boolean generate(ProcessingEnvironment processingEnv) {

        // Check that modules are setup correctly
        for (Element classElement : env.getElementsAnnotatedWith(RealmModule.class)) {
            if (!classElement.getKind().equals(ElementKind.CLASS)) {
                Utils.error("The RealmModule annotation can only be applied to classes", classElement);
                return false;
            }

            RealmModule module = classElement.getAnnotation(RealmModule.class);
            Utils.note("Processing " + classElement.getSimpleName());
            if (module.allClasses() && module.classes().length > 0) {
                Utils.error("Setting allClasses to true will override specific classes set in " + classElement.getSimpleName().toString());
                return false;
            }

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
                                "possible to add classes are part of another library.");
                        return false;
                    }
                    classes.add(metadata);
                }
            }

            if (module.library()) {
                libraryModules.put(qualifiedName, classes);
            } else {
                modules.put(qualifiedName, classes);
            }
        }

        if (modules.size() > 0 && libraryModules.size() > 0) {
            Utils.error("Normal modules and library modules cannot be mixed in the same project");
            return false;
        }

        // Add default realm module if needed
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
     * Returns true if the DefaultRealmModule.java file should be created.
     */
    public boolean shouldCreateDefaultModule() {
        return shouldCreateDefaultModule;
    }
}
