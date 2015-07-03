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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import io.realm.annotations.RealmModule;

/**
 * Utility class for holding metadata for the Realm modules.
 */
public class ModuleMetaData {

    private final Set<ClassMetaData> availableClasses;
    private final RoundEnvironment env;
    private Map<String, Set<ClassMetaData>> modules = new HashMap<String, Set<ClassMetaData>>();
    private Map<String, Set<ClassMetaData>> libraryModules = new HashMap<String, Set<ClassMetaData>>();
    private Map<String, ClassMetaData> classMetaData = new HashMap<String, ClassMetaData>(); //
    // <FullyQualifiedClassName, ClassMetaData>
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
            Utils.note("Processing module " + classSimpleName);
            if (module.allClasses() && hasCustomClassList(classElement)) {
                Utils.error("Setting @RealmModule(allClasses=true) will override @RealmModule(classes={...}) in " +
                        classSimpleName);
                return false;
            }

            // Check that classes added are proper Realm model classes
            String qualifiedName = ((TypeElement) classElement).getQualifiedName().toString();
            Set<ClassMetaData> classes;
            if (module.allClasses()) {
                classes = availableClasses;
            } else {
                classes = new HashSet<ClassMetaData>();
                Set<String> classNames = getClassMetaDataFromModule(classElement);
                for (String fullyQualifiedClassName : classNames) {
                    ClassMetaData metadata = classMetaData.get(fullyQualifiedClassName);
                    if (metadata == null) {
                        Utils.error(Utils.stripPackage(fullyQualifiedClassName) + " could not be added to the module." +
                                " " +
                                "Only classes extending RealmObject, which are part of this project, can be added.");
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

    // Detour needed to access the class elements in the array
    // See http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
    private Set<String> getClassMetaDataFromModule(Element classElement) {
        AnnotationMirror annotationMirror = getAnnotationMirror(classElement);
        AnnotationValue annotationValue = getAnnotationValue(annotationMirror);
        Set<String> classes = new HashSet<String>();
        List<? extends AnnotationValue> moduleClasses = (List<? extends AnnotationValue>) annotationValue.getValue();
        for (AnnotationValue classMirror : moduleClasses) {
            String fullyQualifiedClassName = classMirror.getValue().toString();
            classes.add(fullyQualifiedClassName);
        }
        return classes;
    }

    // Work around for asking for a Class primitive array which would otherwise throw a TypeMirrorException
    // https://community.oracle.com/thread/1184190
    private boolean hasCustomClassList(Element classElement) {
        AnnotationMirror annotationMirror = getAnnotationMirror(classElement);
        AnnotationValue annotationValue = getAnnotationValue(annotationMirror);
        if (annotationValue == null) {
            return false;
        } else {
            List<? extends AnnotationValue> moduleClasses = (List<? extends AnnotationValue>) annotationValue
                    .getValue();
            return moduleClasses.size() > 0;
        }
    }

    private AnnotationMirror getAnnotationMirror(Element classElement) {
        AnnotationMirror annotationMirror = null;
        for (AnnotationMirror am : classElement.getAnnotationMirrors()) {
            if (am.getAnnotationType().toString().equals(RealmModule.class.getCanonicalName())) {
                annotationMirror = am;
                break;
            }
        }
        return annotationMirror;
    }

    private AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror) {
        if (annotationMirror == null) {
            return null;
        }
        AnnotationValue annotationValue = null;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror
                .getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals("classes")) {
                annotationValue = entry.getValue();
                break;
            }
        }
        return annotationValue;
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
