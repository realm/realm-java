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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import io.realm.processor.nameconverter.NameConverter;


/**
 * Utility class for holding metadata for the Realm modules.
 * <p>
 * Modules are inherently difficult to process because a model class can be part of multiple modules
 * that contain information required by the model class (e.g. class/field naming policies). At the
 * same time, the module will need the data from processed model classes to fully complete its
 * analysis (e.g. to ensure that only valid Realm model classes are added to the module).
 * <p>
 * For this reason, processing modules are separated into 3 steps:
 * <ol>
 *  <li>
 *      Pre-processing. Done by calling {@link #preProcess(Set)}, which will do an initial parse
 *      of the modules and build up all information it can before processing any model classes.
 *  </li>
 *  <li>
 *      Process model classes. See {@link ClassMetaData#generate(ModuleMetaData)}.
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
    private Set<String> globalModules = new LinkedHashSet<>(); // All modules with `allClasses = true` set
    private Map<String, Set<String>> specificClassesModules = new LinkedHashMap<>(); // Modules with classes specifically named
    private Map<String, RealmNamingPolicy> classNamingPolicy = new LinkedHashMap<>();
    private Map<String, RealmNamingPolicy> fieldNamingPolicy = new LinkedHashMap<>();
    private Map<String, RealmModule> moduleAnnotations = new HashMap<>();

    // Post-processing
    // <FullyQualifiedModuleClassName, X>
    private Map<String, Set<ClassMetaData>> modules = new LinkedHashMap<>();
    private Map<String, Set<ClassMetaData>> libraryModules = new LinkedHashMap<>();

    private boolean shouldCreateDefaultModule;

    /**
     * Builds all meta data structures that can be calculated before processing any model classes.
     * Any errors or messages will be posted on the provided Messager.
     *
     * @return True if meta data was correctly created and processing of model classes can continue, false otherwise.
     */
    public boolean preProcess(Set<? extends Element> moduleClasses) {

        // Tracks all module settings with `allClasses` enabled
        Set<ModulePolicyInfo> globalModuleInfo = new HashSet<>();

        // Tracks which modules a class was mentioned in by name using `classes = { ... }`
        // <Qualified
        Map<String, List<ModulePolicyInfo>> classSpecificModuleInfo = new HashMap<>();

        // Check that modules are setup correctly
        for (Element classElement : moduleClasses) {
            String classSimpleName = classElement.getSimpleName().toString();

            // Check that the annotation is only applied to a class
            if (!classElement.getKind().equals(ElementKind.CLASS)) {
                Utils.error("The RealmModule annotation can only be applied to classes", classElement);
                return false;
            }

            // Check that allClasses and classes are not set at the same time
            RealmModule moduleAnnotation = classElement.getAnnotation(RealmModule.class);
            Utils.note("Processing module " + classSimpleName);
            if (moduleAnnotation.allClasses() && hasCustomClassList(classElement)) {
                Utils.error("Setting @RealmModule(allClasses=true) will override @RealmModule(classes={...}) in " + classSimpleName);
                return false;
            }

            // Validate that naming policies are correctly configured.
            if (!validateNamingPolicies(globalModuleInfo, classSpecificModuleInfo, (TypeElement) classElement, moduleAnnotation)) {
                return false;
            }

            moduleAnnotations.put(((TypeElement) classElement).getQualifiedName().toString(), moduleAnnotation);
        }

        return true;
    }

    /**
     * Validates that the class/field naming policy for this module is correct.
     *
     * @param globalModuleInfo list of all modules with `allClasses` set
     * @param classSpecificModuleInfo map of explicit classes and which modules they are explicitly mentioned in.
     * @param classElement class element currently being validated
     * @param moduleAnnotation annotation on this class.
     * @return {@code true} if everything checks out, {@code false} if an error was found and reported.
     */
    private boolean validateNamingPolicies(Set<ModulePolicyInfo> globalModuleInfo, Map<String, List<ModulePolicyInfo>> classSpecificModuleInfo, TypeElement classElement, RealmModule moduleAnnotation) {
        RealmNamingPolicy classNamePolicy = moduleAnnotation.classNamingPolicy();
        RealmNamingPolicy fieldNamePolicy = moduleAnnotation.fieldNamingPolicy();
        String qualifiedModuleClassName = classElement.getQualifiedName().toString();
        ModulePolicyInfo moduleInfo = new ModulePolicyInfo(qualifiedModuleClassName, classNamePolicy, fieldNamePolicy);

        // The difference between `allClasses` and a list of classes is a bit tricky at this stage
        // as we haven't processed the full list of classes yet. We therefore need to treat
        // each case specifically :(
        // We do not compare against the default module as it is always configured correctly
        // with NO_POLICY, meaning it will not trigger any errors.
        if (moduleAnnotation.allClasses()) {
            // Check for conflicts with all other modules with `allClasses` set.
            for (ModulePolicyInfo otherModuleInfo : globalModuleInfo) {
                if (checkAndReportPolicyConflict(moduleInfo, otherModuleInfo)) {
                    return false;
                }
            }

            // Check for conflicts with specifically named classes. This can happen if another
            // module is listing specific classes with another policy.
            for (Map.Entry<String, List<ModulePolicyInfo>> classPolicyInfo : classSpecificModuleInfo.entrySet()) {
                for (ModulePolicyInfo otherModuleInfo : classPolicyInfo.getValue()) {
                    if (checkAndReportPolicyConflict(moduleInfo, otherModuleInfo)) {
                        return false;
                    }
                }
            }

            // Everything checks out. Add moduleInfo so we can track it for the next module.
            globalModuleInfo.add(moduleInfo);
            globalModules.add(qualifiedModuleClassName);

        } else {
            // We need to verify each class in the modules class list
            Set<String> classNames = getClassListFromModule(classElement);
            for (String qualifiedClassName : classNames) {

                // Check that no other module with `allClasses` conflict with this specific
                // class configuration
                for (ModulePolicyInfo otherModuleInfo : globalModuleInfo) {
                    if (checkAndReportPolicyConflict(moduleInfo, otherModuleInfo)) {
                        return false;
                    }
                }

                // Check that this specific class isn't conflicting with another module
                // specifically mentioning it using `classes = { ... }`
                List<ModulePolicyInfo> otherModules = classSpecificModuleInfo.get(qualifiedClassName);
                if (otherModules != null) {
                    for (ModulePolicyInfo otherModuleInfo : otherModules) {
                        if (checkAndReportPolicyConflict(qualifiedClassName, moduleInfo, otherModuleInfo)) {
                            return false;
                        }
                    }
                }

                // Keep track of the specific class for other module checks. We only
                // need to track the latest module seen as previous errors would have been
                // caught in a previous iteration of the loop.
                if (!classSpecificModuleInfo.containsKey(qualifiedClassName)) {
                    classSpecificModuleInfo.put(qualifiedClassName, new ArrayList<>());
                }
                classSpecificModuleInfo.get(qualifiedClassName).add(moduleInfo);
            }
            specificClassesModules.put(qualifiedModuleClassName, classNames);
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

        // Process all global modules
        for (String qualifiedModuleClassName : globalModules) {
            Set<ClassMetaData> classData = new LinkedHashSet<>();
            classData.addAll(modelClasses.getClasses());
            defineModule(qualifiedModuleClassName, classData);
        }

        // Process all modules with specific classes
        for (Map.Entry<String, Set<String>> module : specificClassesModules.entrySet()) {
            String qualifiedModuleClassName = module.getKey();
            Set<ClassMetaData> classData = new LinkedHashSet<>();
            for (String qualifiedModelClassName : module.getValue()) {
                if (!modelClasses.containsQualifiedClass(qualifiedModelClassName)) {
                    Utils.error(Utils.stripPackage(qualifiedModelClassName) + " could not be added to the module. " +
                            "Only classes extending RealmObject or implementing RealmModel, which are part of this project, can be added.");
                    return false;

                }
                classData.add(modelClasses.getClassFromQualifiedName(qualifiedModelClassName));
            }
            defineModule(qualifiedModuleClassName, classData);
        }

        // Check that app and library modules are not mixed
        if (modules.size() > 0 && libraryModules.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Normal modules and library modules cannot be mixed in the same project.");
            sb.append('\n');
            sb.append("Normal module(s):\n");
            for (String module : modules.keySet()) {
                sb.append("  ");
                sb.append(module);
                sb.append('\n');
            }
            sb.append("Library module(s):\n");
            for (String module : libraryModules.keySet()) {
                sb.append("  ");
                sb.append(module);
                sb.append('\n');
            }
            Utils.error(sb.toString());
            return false;
        }

        // Create default Realm module if needed.
        // Note: Kotlin will trigger the annotation processor even if no Realm annotations are used.
        // The DefaultRealmModule should not be created in this case either.
        if (libraryModules.size() == 0 && modelClasses.size() > 0) {
            shouldCreateDefaultModule = true;
            String defaultModuleName = Constants.REALM_PACKAGE_NAME + "." + Constants.DEFAULT_MODULE_CLASS_NAME;
            modules.put(defaultModuleName, modelClasses.getClasses());
        }

        return true;
    }

    private void defineModule(String qualifiedModuleClassName, Set<ClassMetaData> classData) {
        if (!classData.isEmpty()) {
            if (moduleAnnotations.get(qualifiedModuleClassName).library()) {
                libraryModules.put(qualifiedModuleClassName, classData);
            } else {
                modules.put(qualifiedModuleClassName, classData);
            }
        }
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
        Map<String, Set<ClassMetaData>> allModules = new LinkedHashMap<>();
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

    /**
     * Only available after {@link #preProcess(Set)} has run.
     * Returns the module name policy the given name.
     */
    public NameConverter getClassNameFormatter(String qualifiedClassName) {
        // We already validated that module definitions all agree on the same name policy
        // so just find first match
        if (!globalModules.isEmpty()) {
            return Utils.getNameFormatter(classNamingPolicy.get(globalModules.iterator().next()));
        }

        // No global modules found, so find match in modules specifically listing the class.
        // We already validated that all modules agree on the converter, so just find first match.
        for (Map.Entry<String, Set<String>> moduleInfo : specificClassesModules.entrySet()) {
            if (moduleInfo.getValue().contains(qualifiedClassName)) {
                return Utils.getNameFormatter(classNamingPolicy.get(moduleInfo.getKey()));
            }
        }

        // No policy was provided anywhere for this class
        return Utils.getNameFormatter(RealmNamingPolicy.NO_POLICY);
    }


    /**
     * Only available after {@link #preProcess(Set)} has run.
     *
     * Returns the module name policy the field names.
     *
     * @param qualifiedClassName
     */
    public NameConverter getFieldNameFormatter(String qualifiedClassName) {
        // We already validated that module definitions all agree on the same name policy
        // so just find first match
        if (!globalModules.isEmpty()) {
            return Utils.getNameFormatter(fieldNamingPolicy.get(globalModules.iterator().next()));
        }

        for (Map.Entry<String, Set<String>> moduleInfo : specificClassesModules.entrySet()) {
            if (moduleInfo.getValue().contains(qualifiedClassName)) {
                return Utils.getNameFormatter(fieldNamingPolicy.get(moduleInfo.getKey()));
            }
        }

        return Utils.getNameFormatter(RealmNamingPolicy.NO_POLICY);
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
