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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import io.realm.annotations.RealmClass;

/**
 * The RealmProcessor is responsible for creating the plumbing that connects the RealmObjects to a Realm. The process
 * for doing so is summarized below and then described in more detail.
 * <p>
 *
 * <h1>DESIGN GOALS</h1>
 *
 * The processor should support the following design goals:
 * <ul>
 *  <li>Minimize reflection.</li>
 *  <li>Realm code can be obfuscated as much as possible.</li>
 *  <li>Library projects must be able to use Realm without interfering with app code.</li>
 *  <li>App code must be able to use RealmObject classes provided by library code.</li>
 *  <li>It should work for app developers out of the box (ie. put the burden on the library developer)</li>
 * </ul>
 *
 * <h1>SUMMARY</h1>
 *
 * <ol>
 *  <li>Create proxy classes for all classes marked with @RealmClass. They are named &lt;className&gt;RealmProxy.java</li>
 *  <li>Create a DefaultRealmModule containing all RealmObject classes (if needed).</li>
 *  <li>Create a RealmProxyMediator class for all classes marked with {@code @RealmModule}. They are named {@code <moduleName>Mediator.java}</li>
 * </ol>
 *
 * <h1>WHY</h1>
 *
 * <ol>
 * <li>A RealmObjectProxy object is created for each class annotated with {@link io.realm.annotations.RealmClass}. This
 * proxy extends the original RealmObject class and rewires all field access to point to the native Realm memory instead of
 * Java memory. It also adds some static helper methods to the class.</li>
 *
 * <li>The annotation processor is either in "library" mode or in "app" mode. This is defined by having a class
 * annotated with @RealmModule(library = true). It is not allowed to have both a class with library = true and
 * library = false in the same IntelliJ module and it will cause the annotation processor to throw an exception. If no
 * library modules are defined, we will create a DefaultRealmModule containing all known RealmObjects and with the
 * {@code @RealmModule} annotation. Realm automatically knows about this module, but it is still possible for users to create
 * their own modules with a subset of model classes.</li>
 *
 * <li>For each class annotated with @RealmModule a matching Mediator class is created (including the default one). This
 * class has an interface that matches the static helper methods for the proxy classes. All access to these static
 * helper methods should be done through this Mediator.</li>
 * </ol>
 *
 * This allows ProGuard to obfuscate all RealmObject and proxy classes as all access to the static methods now happens through
 * the Mediator, and the only requirement is now that only RealmModule and Mediator class names cannot be obfuscated.
 *
 *
 * <h1>CREATING A REALM</h1>
 *
 * This means the workflow when instantiating a Realm on runtime is the following:
 *
 * <ol>
 *  <li>Open a Realm.</li>
 *  <li>Assign one or more modules (that are allowed to overlap). If no module is assigned, the default module is used.</li>
 *  <li>The Realm schema is now defined as all RealmObject classes known by these modules.</li>
 *  <li>Each time a static helper method is needed, Realm can now delegate these method calls to the appropriate
 *    Mediator which in turn will delegate the method call to the appropriate RealmObjectProxy class.</li>
 * </ol>
 *
 * <h1>CREATING A MANAGED RealmObject</h1>
 *
 * To allow to specify default values by model's constructor or direct field assignment,
 * the flow of creating the proxy object is a bit complicated. This section illustrates
 * how proxy object should be created.
 *
 * <ol>
 *  <li>Get the thread local {@code io.realm.BaseRealm.RealmObjectContext} instance by {@code BaseRealm.objectContext.get()} </li>
 *  <li>Set the object context information to the {@code RealmObjectContext} those should be set to the creating proxy object.</li>
 *  <li>Create proxy object ({@code new io.realm.FooRealmProxy()}).</li>
 *  <li>Set the object context information to the created proxy when the first access of its accessors (or in its constructor if accessors are not used in the model's constructor).</li>
 *  <li>Clear the object context information in the thread local {@code io.realm.BaseRealm.RealmObjectContext} instance by calling {@code
 *  #clear()} method.</li>
 * </ol>
 *
 * The reason of this complicated step is that we can't pass these context information
 * via the constructor of the proxy. It's because the constructor of the proxy is executed
 * <b>after</b> the constructor of the model class. The access to the fields in the model's
 * constructor happens before the assignment of the context information to the 'proxyState'.
 * This will cause the {@link NullPointerException} if getters/setter is accessed in the model's
 * constructor (see https://github.com/realm/realm-java/issues/2536 ).
 */
@SupportedAnnotationTypes({
        "io.realm.annotations.RealmClass",
        "io.realm.annotations.Ignore",
        "io.realm.annotations.Index",
        "io.realm.annotations.PrimaryKey",
        "io.realm.annotations.RealmModule",
        "io.realm.annotations.Required"
})
public class RealmProcessor extends AbstractProcessor {

    // Don't consume annotations. This allows 3rd party annotation processors to run.
    private static final boolean CONSUME_ANNOTATIONS = false;

    Set<ClassMetaData> classesToValidate = new HashSet<ClassMetaData>();
    private boolean hasProcessedModules = false;

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Don't run this processor in subsequent runs. We created everything in the first one.
        if (hasProcessedModules) {
            return CONSUME_ANNOTATIONS;
        }
        RealmVersionChecker updateChecker = RealmVersionChecker.getInstance(processingEnv);
        updateChecker.executeRealmVersionUpdate();

        Utils.initialize(processingEnv);

        Set<String> packages = new TreeSet<String>();

        // Create all proxy classes
        for (Element classElement : roundEnv.getElementsAnnotatedWith(RealmClass.class)) {

            // The class must either extend RealmObject or implement RealmModel
            if (!Utils.isImplementingMarkerInterface(classElement)) {
                Utils.error("A RealmClass annotated object must implement RealmModel or derive from RealmObject", classElement);
            }

            // Check the annotation was applied to a Class
            if (!classElement.getKind().equals(ElementKind.CLASS)) {
                Utils.error("The RealmClass annotation can only be applied to classes", classElement);
                return true; // Abort processing by claiming all annotations
            }

            ClassMetaData metadata = new ClassMetaData(processingEnv, (TypeElement) classElement);
            if (!metadata.isModelClass()) {
                continue;
            }
            Utils.note("Processing class " + metadata.getSimpleClassName());
            boolean success = metadata.generate();
            if (!success) {
                return true;
            }
            classesToValidate.add(metadata);
            packages.add(metadata.getPackageName());

            RealmProxyInterfaceGenerator interfaceGenerator = new RealmProxyInterfaceGenerator(processingEnv, metadata);
            try {
                interfaceGenerator.generate();
            } catch (IOException e) {
                Utils.error(e.getMessage(), classElement);
            }

            RealmProxyClassGenerator sourceCodeGenerator = new RealmProxyClassGenerator(processingEnv, metadata);
            try {
                sourceCodeGenerator.generate();
            } catch (IOException e) {
                Utils.error(e.getMessage(), classElement);
            } catch (UnsupportedOperationException e) {
                Utils.error(e.getMessage(), classElement);
            }
        }

        hasProcessedModules = true;
        processModules(roundEnv);

        return CONSUME_ANNOTATIONS;
    }

    // Returns true if modules was processed successfully, false otherwise
    private boolean processModules(RoundEnvironment roundEnv) {

        ModuleMetaData moduleMetaData = new ModuleMetaData(roundEnv, classesToValidate);
        if (!moduleMetaData.generate(processingEnv)) {
            return false;
        }

        // Create default module if needed
        if (moduleMetaData.shouldCreateDefaultModule()) {
            if (!createDefaultModule()) {
                return false;
            }
        }

        // Create RealmProxyMediators for all Realm modules
        for (Map.Entry<String, Set<ClassMetaData>> module : moduleMetaData.getAllModules().entrySet()) {
            if (!createMediator(Utils.stripPackage(module.getKey()), module.getValue())) {
                return false;
            }
        }

        return true;
    }

    private boolean createDefaultModule() {
        Utils.note("Creating DefaultRealmModule");
        DefaultModuleGenerator defaultModuleGenerator = new DefaultModuleGenerator(processingEnv);
        try {
            defaultModuleGenerator.generate();
        } catch (IOException e) {
            Utils.error(e.getMessage());
            return false;
        }

        return true;
    }

    private boolean createMediator(String simpleModuleName, Set<ClassMetaData> moduleClasses) {
        RealmProxyMediatorGenerator mediatorImplGenerator = new RealmProxyMediatorGenerator(processingEnv,
                simpleModuleName, moduleClasses);
        try {
            mediatorImplGenerator.generate();
        } catch (IOException e) {
            Utils.error(e.getMessage());
            return false;
        }

        return true;
    }
}
