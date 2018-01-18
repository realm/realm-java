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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import io.realm.annotations.RealmModule;
import io.realm.annotations.RealmNamingPolicy;


/**
 * Utility class for holding metadata for the Realm modules.
 * <p>
 * Modules are inherently difficult to process because a model class can be part of multiple modules
 * that contain information required by the model class. At the same time, the module will need the
 * data from processed model classes to fully complete its analysis (FIXME: Why?)
 * <p>
 * For this reason, processing modules are separated into 3 steps:
 * <ol>
 *  <li>
 *      Pre-processing. Done by calling {@link #preProcess(Set)}, which will do an initial parse
 *      of the modules and build up all information it can before before processing any class.
 *      This e.g. includes default naming policies for classes and fields.
 *  </li>
 *  <li>
 *      Process model classes. See {@link ClassMetaData#generate()}.
 *  </li>
 *  <li>
 *      Post-processing. Done by calling {@link #postProcess(ClassCollection)}. All modules can now
 *      be fully verified, and all metadata required to output module files can be generated.
 *  </li>
 * </ol>
 */
public class ModuleMetaData {

    // Pre-processing
    // <FullyQualifiedModuleClassName, X>
    private Map<String, Set<String>> classesInModule = new HashMap<>();
    private Map<String, RealmNamingPolicy> classNamingPolicy = new HashMap<String, RealmNamingPolicy>();
    private Map<String, RealmNamingPolicy> fieldNamingPolicy = new HashMap<String, RealmNamingPolicy>();
    private Map<String, RealmModule> moduleAnnotations = new HashMap<>();

    // Post-processing
    // <FullyQualifiedModuleClassName, X>
    private final ClassCollection availableClasses;
    private Map<String, ClassMetaData> classMetaData = new HashMap<String, ClassMetaData>();
    private Map<String, Set<ClassMetaData>> modules = new HashMap<String, Set<ClassMetaData>>();
    private Map<String, Set<ClassMetaData>> libraryModules = new HashMap<String, Set<ClassMetaData>>();

    private boolean shouldCreateDefaultModule;

    public ModuleMetaData(ClassCollection availableClasses) {
        this.availableClasses = availableClasses;
        for (ClassMetaData classMetaData : availableClasses.getClasses()) {
            this.classMetaData.put(classMetaData.getFullyQualifiedClassName(), classMetaData);
        }
    }

    /**
     * Builds all meta data structures that can be calculated before processing any model classes.
     * Any errors or messages will be posted on the provided Messager.
     *
     * @return True if meta data was correctly created and processing of model classes can continue, false otherwise.
     */
    public boolean preProcess(Set<? extends Element> moduleClasses) {

        // Tracks all module settings with `allClasses` enabled
        Set<ModulePolicyInfo> globalModuleInfo = new HashSet<>();

        // Tracks which module a class last was mentioned in by name using `classes = { ... }`
        // <Qualified
        Map<String, ModulePolicyInfo> classSpecificModuleInfo = new HashMap<String, ModulePolicyInfo>();

        // Check that modules are setup correctly
        for (Element classElement : moduleClasses) {
            String classSimpleName = classElement.getSimpleName().toString();

            // Check that the annotation is only applied to a class
            if (!classElement.getKind().equals(ElementKind.CLASS)) {
                Utils.error("The RealmModule annotation can only be applied to classes", classElement);
                return false;
            }

            // Check that allClasses and classes are not set at the same time
            RealmModule moduleAnnoation = classElement.getAnnotation(RealmModule.class);
            Utils.note("Processing module " + classSimpleName);
            if (moduleAnnoation.allClasses() && hasCustomClassList(classElement)) {
                Utils.error("Setting @RealmModule(allClasses=true) will override @RealmModule(classes={...}) in " + classSimpleName);
                return false;
            }

            // Validate that naming policies are correctly configured.
            if (!validateNamingPolicies(globalModuleInfo, classSpecificModuleInfo, (TypeElement) classElement, moduleAnnoation)) {
                return false;
            }

            moduleAnnotations.put(((TypeElement) classElement).getQualifiedName().toString(), moduleAnnoation);
        }

        return true;
    }

    /**
     * Validates that the class/field naming policy for this module is correct.
     *
     * @param globalModuleInfo list of all modules with `allClasses` set
     * @param classSpecificModuleInfo
     * @param classElement class element currently being validated
     * @param moduleAnnotation annotation on this class.
     * @return {@code true} if everything checks out, {@code false} if an error was found and reported.
     */
    private boolean validateNamingPolicies(Set<ModulePolicyInfo> globalModuleInfo, Map<String, ModulePolicyInfo> classSpecificModuleInfo, TypeElement classElement, RealmModule moduleAnnotation) {
        // Check that module naming policies do not conflict
        RealmNamingPolicy classNamePolicy = moduleAnnotation.classNamingPolicy();
        RealmNamingPolicy fieldNamePolicy = moduleAnnotation.fieldNamingPolicy();
        String qualifiedModuleClassName = classElement.getQualifiedName().toString();
        ModulePolicyInfo moduleInfo = new ModulePolicyInfo(qualifiedModuleClassName, classNamePolicy, fieldNamePolicy);

        // The difference between `allClasses` and a list of classes is a bit tricky at this stage
        // as we haven't processed the full list of classes yet. We therefore need to treat
        // each case specifically :(
        // We do not compare against the default module as it is always configured correctly
        // with NO_POLICY, meaning it will not trigger any errors anyway.
        if (moduleAnnotation.allClasses()) {
            // Check for conflicts with other modules with `allClasses` set. Only need to
            // check the first since other conflicts would have been detected in an earlier
            // iteration.
            if (!globalModuleInfo.isEmpty()) {
                ModulePolicyInfo otherModuleInfo = globalModuleInfo.iterator().next();
                if (checkAndReportPolicyConflict(moduleInfo, otherModuleInfo)) {
                    return false;
                }
            }

            // Check for conflicts with specifically named classes. This can happen if another
            // module are listing specific classes with another policy.
            for (Map.Entry<String, ModulePolicyInfo> classPolicyInfo : classSpecificModuleInfo.entrySet()) {
                if (checkAndReportPolicyConflict(moduleInfo, classPolicyInfo.getValue())) {
                    return false;
                }
            }

            // Everything checks out. Add moduleInfo so we can track it for the next module.
            globalModuleInfo.add(moduleInfo);

        } else {
            // We need to verify each class in the modules class list
            Set<String> classNames = getClassListFromModule(classElement);
            for (String qualifiedClassName : classNames) {

                // Check that no other module with `allClasses` conflict with this specific
                // class configuration
                if (!globalModuleInfo.isEmpty()) {
                    ModulePolicyInfo otherModuleInfo = globalModuleInfo.iterator().next();
                    if (checkAndReportPolicyConflict(moduleInfo, otherModuleInfo)) {
                        return false;
                    }
                }

                // Check that this specific class isn't conflicting with another module
                // specifically mentioning it using `classes = { ... }`
                ModulePolicyInfo otherModuleInfo = classSpecificModuleInfo.get(qualifiedClassName);
                if (otherModuleInfo != null) {
                    if (checkAndReportPolicyConflict(qualifiedClassName, moduleInfo, otherModuleInfo)) {
                        return false;
                    }
                }

                // Keep track of the specific class for other module checks. We only
                // need to track the latest module seen as previous errors would have been
                // caught in a previous iteration of the loop.
                classSpecificModuleInfo.put(qualifiedClassName, moduleInfo);
            }
            classesInModule.put(qualifiedModuleClassName, classNames);
        }

        classNamingPolicy.put(qualifiedModuleClassName, classNamePolicy);
        fieldNamingPolicy.put(qualifiedModuleClassName, fieldNamePolicy);
        return true;
    }

    /**
     * All model classes have now been processed and the final validation of modules can occur.
     * Any errors or messages will be posted on the provided Messager.
     *
     * @param modelClasses all Realm model classes found by the annotation processor.
     * @return {@code true} if the module is valid, {@code false} otherwise.
     */
    public boolean postProcess(ClassCollection modelClasses) {

        for (Map.Entry<String, Set<String>> module : classesInModule.entrySet()) {

            // Find all processed metadata for each class part of this module.
            Set<ClassMetaData> classData = new LinkedHashSet<>();
            for (String qualifiedClassName : module.getValue()) {
                if (!modelClasses.containsQualifiedClass(qualifiedClassName)) {
                    Utils.error(Utils.stripPackage(qualifiedClassName) + " could not be added to the module. " +
                            "Only classes extending RealmObject, which are part of this project, can be added.");
                    return false;

                }
                classData.add(modelClasses.getClassFromQualifiedName(qualifiedClassName));
            }

            // Create either a Library or App module
            String qualifiedModuleClassName = module.getKey();
            if (moduleAnnotations.get(qualifiedModuleClassName).library()) {
                libraryModules.put(qualifiedModuleClassName, classData);
            } else {
                modules.put(qualifiedModuleClassName, classData);
            }
        }

        // Check that app and library modules are not mixed
        if (modules.size() > 0 && libraryModules.size() > 0) {
            Utils.error("Normal modules and library modules cannot be mixed in the same project");
            return false;
        }

        // Create default Realm module if needed.
        // Note: Kotlin will trigger the annotation processor even if no Realm annotations are used.
        // The DefaultRealmModule should not be created in this case either.
        if (libraryModules.size() == 0 && availableClasses.size() > 0) {
            shouldCreateDefaultModule = true;
            String defaultModuleName = Constants.REALM_PACKAGE_NAME + "." + Constants.DEFAULT_MODULE_CLASS_NAME;
            modules.put(defaultModuleName, availableClasses.getClasses());
        }

        return true;
    }

    // Checks if two modules have policy conflicts. Returns true if a conflict was found and reported.
    private boolean checkAndReportPolicyConflict(ModulePolicyInfo moduleInfo, ModulePolicyInfo otherModuleInfo) {
        return checkAndReportPolicyConflict(null, moduleInfo, otherModuleInfo);
    }

    /**
     * Check for name policy conflicts and report the error if found.
     *
     * @param className optional class name if a specific class is being checked.
     * @param moduleInfo current module.
     * @param otherModuleInfo already processed module.
     * @return {@code true} if any errors was reported, {@code false} otherwise.
     */
    private boolean checkAndReportPolicyConflict(String className, ModulePolicyInfo moduleInfo, ModulePolicyInfo otherModuleInfo) {
        boolean foundErrors = false;

        // Check class naming policy
        RealmNamingPolicy classPolicy = moduleInfo.classNamePolicy;
        RealmNamingPolicy otherClassPolicy = otherModuleInfo.classNamePolicy;
        if (classPolicy != RealmNamingPolicy.NO_POLICY
                && otherClassPolicy != RealmNamingPolicy.NO_POLICY
                && classPolicy != otherClassPolicy) {
            Utils.error(String.format("The modules %s and %s disagree on the class naming policy%s: %s vs. %s. " +
                            "They same policy must be used.",
                    moduleInfo.qualifiedModuleClassName,
                    otherModuleInfo.qualifiedModuleClassName,
                    (className != null) ? " for " + className : "",
                    classPolicy,
                    otherClassPolicy));
            foundErrors = true;
        }

        // Check field naming policy
        RealmNamingPolicy fieldPolicy = moduleInfo.fieldNamePolicy;
        RealmNamingPolicy otherFieldPolicy = otherModuleInfo.fieldNamePolicy;
        if (fieldPolicy != RealmNamingPolicy.NO_POLICY
                && otherFieldPolicy != RealmNamingPolicy.NO_POLICY
                && fieldPolicy != otherFieldPolicy) {
            Utils.error(String.format("The modules %s and %s disagree on the field naming policy%s: %s vs. %s. " +
                            "They same policy should be used.",
                    moduleInfo.qualifiedModuleClassName,
                    otherModuleInfo.qualifiedModuleClassName,
                    (className != null) ? " for " + className : "",
                    fieldPolicy,
                    otherFieldPolicy));
            foundErrors = true;
        }

        return foundErrors;
    }

//    public boolean generate2() {
//
//        // Check that classes added are proper Realm model classes
//        for (Map.Entry<String, Set<String>> module : classesInModule.entrySet()) {
//            for (String fullyQualifiedClassName : module.getValue()) {
//                ClassMetaData metadata = classMetaData.get(fullyQualifiedClassName);
//                if (metadata == null) {
//                    Utils.error(Utils.stripPackage(fullyQualifiedClassName) + " could not be added to the module. " +
//                            "Only classes extending RealmObject, which are part of this project, can be added.");
//                    return false;
//                }
//                classMetaData.put(module.getKey(), metadata);
//            }
//        }
//
//
//        // Create either a Library or App module
//        if (module.library()) {
//            libraryModules.put(qualifiedModuleClassName, classes);
//        } else {
//            modules.put(qualifiedModuleClassName, classes);
//        }
//
//
//        return true;
//    }

    // Detour needed to access the class elements in the array
    // See http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
    @SuppressWarnings("unchecked")
    private Set<String> getClassListFromModule(Element classElement) {
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

    // Work-around for asking for a Class primitive array which would otherwise throw a TypeMirrorException
    // https://community.oracle.com/thread/1184190
    @SuppressWarnings("unchecked")
    private boolean hasCustomClassList(Element classElement) {
        AnnotationMirror annotationMirror = getAnnotationMirror(classElement);
        AnnotationValue annotationValue = getAnnotationValue(annotationMirror);
        if (annotationValue == null) {
            return false;
        } else {
            List<? extends AnnotationValue> moduleClasses = (List<? extends AnnotationValue>) annotationValue.getValue();
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
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
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

    // Tuple helper class
    private class ModulePolicyInfo {
        public final String qualifiedModuleClassName;
        public final RealmNamingPolicy classNamePolicy;
        public final RealmNamingPolicy fieldNamePolicy;

        public ModulePolicyInfo(String qualifiedModuleClassName, RealmNamingPolicy classNamePolicy, RealmNamingPolicy fieldNamePolicy) {
            this.qualifiedModuleClassName = qualifiedModuleClassName;
            this.classNamePolicy = classNamePolicy;
            this.fieldNamePolicy = fieldNamePolicy;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ModulePolicyInfo that = (ModulePolicyInfo) o;

            if (!qualifiedModuleClassName.equals(that.qualifiedModuleClassName)) return false;
            if (classNamePolicy != that.classNamePolicy) return false;
            return fieldNamePolicy == that.fieldNamePolicy;
        }

        @Override
        public int hashCode() {
            int result = qualifiedModuleClassName.hashCode();
            result = 31 * result + classNamePolicy.hashCode();
            result = 31 * result + fieldNamePolicy.hashCode();
            return result;
        }
    }
}
