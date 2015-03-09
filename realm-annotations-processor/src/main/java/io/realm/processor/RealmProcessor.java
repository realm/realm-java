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
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.internal.RealmModule;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;


/**
 * The RealmProcessor is responsible for creating the plumbing that connects the RealmObjects to a Realm. The process
 * for doing so summarized below and then described in more detail:
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
    Set<String> classesToValidate = new HashSet<String>();
    private boolean hasProcessedModules = false;

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        RealmVersionChecker updateChecker = RealmVersionChecker.getInstance(processingEnv);
        updateChecker.executeRealmVersionUpdate();
        Utils.initialize(processingEnv);

        Types typeUtils = processingEnv.getTypeUtils();
        TypeMirror stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
        List<TypeMirror> validPrimaryKeyTypes = Arrays.asList(
                stringType,
                typeUtils.getPrimitiveType(TypeKind.SHORT),
                typeUtils.getPrimitiveType(TypeKind.INT),
                typeUtils.getPrimitiveType(TypeKind.LONG)
        );

        // Create all proxy classes
        for (Element classElement : roundEnv.getElementsAnnotatedWith(RealmClass.class)) {
            String className;
            String packageName;
            boolean hasDefaultConstructor = false;
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
            VariableElement primaryKey = null;

            for (Element element : typeElement.getEnclosedElements()) {
                ElementKind elementKind = element.getKind();

                if (elementKind.equals(ElementKind.FIELD)) {
                    VariableElement variableElement = (VariableElement) element;
                    String fieldName = variableElement.getSimpleName().toString();

                    Set<Modifier> modifiers = variableElement.getModifiers();
                    if (modifiers.contains(Modifier.STATIC)) {
                        continue; // completely ignore any static fields
                    }

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

                    if (variableElement.getAnnotation(PrimaryKey.class) != null) {
                        // The field has the @PrimaryKey annotation. It is only valid for
                        // String, short, int, long and must only be present one time
                        if (primaryKey != null) {
                            error(String.format("@PrimaryKey cannot be defined more than once. It was found here \"%s\" and here \"%s\"",
                                    primaryKey.getSimpleName().toString(),
                                    variableElement.getSimpleName().toString()));
                            return true;
                        }

                        TypeMirror fieldType = variableElement.asType();
                        if (!Utils.isValidType(typeUtils, fieldType, validPrimaryKeyTypes)) {
                            error("\"" + variableElement.getSimpleName().toString() + "\" is not allowed as primary key. See @PrimaryKey for allowed types.");
                            return true;
                        }

                        primaryKey = variableElement;

                        // Also add as index if the primary key is a string
                        if (Utils.isString(variableElement) && !indexedFields.contains(variableElement)) {
                            indexedFields.add(variableElement);
                        }
                    }

                    if (!variableElement.getModifiers().contains(Modifier.PRIVATE)) {
                        error("The fields of the model must be private", variableElement);
                    }

                    fields.add(variableElement);
                    expectedGetters.add(fieldName);
                    expectedSetters.add(fieldName);
                } else if (elementKind.equals(ElementKind.CONSTRUCTOR)) {
                    hasDefaultConstructor = hasDefaultConstructor || Utils.isDefaultConstructor(element);

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
                        String methodMinusIsCapitalised = Utils.lowerFirstChar(methodMinusIs);
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
                        String methodMinusGetCapitalised = Utils.lowerFirstChar(methodMinusGet);
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
                    String methodMinusSetCapitalised = Utils.lowerFirstChar(methodMinusSet);
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

            if (!hasDefaultConstructor) {
                error("A default public constructor with no argument must be declared if a custom constructor is declared.");
            }

            for (String expectedGetter : expectedGetters) {
                error("No getter found for field " + expectedGetter);
                getters.put(expectedGetter, "");
            }

            for (String expectedSetter : expectedSetters) {
                error("No setter found for field " + expectedSetter);
                setters.put(expectedSetter, "");
            }

            RealmProxyClassGenerator sourceCodeGenerator =
                    new RealmProxyClassGenerator(processingEnv, className, packageName, fields, getters, setters, indexedFields, primaryKey);
            try {
                sourceCodeGenerator.generate();
            } catch (IOException e) {
                error(e.getMessage(), classElement);
                return true;
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
                error("The RealmModule annotation can only be applied to classes", classElement);
            }

            // Check that the class extends RealmClassCollection
            TypeElement typeElement = (TypeElement) classElement;
            TypeElement parentElement = (TypeElement) processingEnv.getTypeUtils().asElement(typeElement.getSuperclass());
            if (!parentElement.toString().endsWith(".RealmCollectionClass")) {
                error("A RealmModule annotated object must be derived from RealmCollectionClass", classElement);
            }

            RealmModule module = classElement.getAnnotation(RealmModule.class);
            if (module.library()) {
                libraryModules.add(((TypeElement) classElement).getQualifiedName().toString());
            } else {
                modules.add(((TypeElement) classElement).getQualifiedName().toString());
            }
        }

        if (modules.size() > 0 && libraryModules.size() > 0) {
            error("Normal modules and library modules cannot be mixed in the same project");
            return true;
        }

        // Create DefaultModule if needed
        if (libraryModules.size() == 0 ) {
            note("Creating DefaultRealmModule");
            DefaultModuleGenerator defaultModuleGenerator = new DefaultModuleGenerator(processingEnv, classesToValidate);
            try {
                defaultModuleGenerator.generate();
                modules.add(DefaultModuleGenerator.CLASS_NAME);
            } catch (IOException e) {
                error(e.getMessage());
                return true;
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
            note("Creating RealmProxyMediator");
            mediatorImplGenerator.generate();
        } catch (IOException e) {
            error(e.getMessage());
            return true;
        }

        // Create Binder classes for all modules
        Set<String> binders = (modules.size() > 0) ? modules : libraryModules;
        for (String binder : binders) {
            ModuleBinderGenerator binderGenerator = new ModuleBinderGenerator(processingEnv, Utils.stripPackage(binder), proxyMediatorName);
            try {
                binderGenerator.generate();
            } catch (IOException e) {
                error(e.getMessage());
                return true;
            }
        }
        return false;
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
