/*
 * Copyright 2019 Realm Inc.
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

package io.realm.processor

import java.io.IOException
import java.util.HashSet

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

import io.realm.annotations.RealmClass
import io.realm.annotations.RealmModule
import javax.lang.model.element.Name
import javax.lang.model.type.TypeMirror


/**
 * The RealmProcessor is responsible for creating the plumbing that connects the RealmObjects to a
 * Realm. The process for doing so is summarized below and then described in more detail.
 *
 * <h1>DESIGN GOALS</h1>
 *
 * The processor should support the following design goals:
 *
 *  * Minimize reflection.
 *  * Realm code can be obfuscated as much as possible.
 *  * Library projects must be able to use Realm without interfering with app code.
 *  * App code must be able to use RealmObject classes provided by library code.
 *  * It should work for app developers out of the box (ie. put the burden on the library developer)
 *
 * <h1>SUMMARY</h1>
 *
 *  1. Create proxy classes for all classes marked with @RealmClass. They are named
 *     `<className>RealmProxy.java`.
 *  2. Create a DefaultRealmModule containing all RealmObject classes (if needed).
 *  3. Create a RealmProxyMediator class for all classes marked with `@RealmModule`. They are named
 *     `<moduleName>Mediator.java`
 *
 * <h1>WHY</h1>
 *
 *  1. A RealmObjectProxy object is created for each class annotated with
 *     [io.realm.annotations.RealmClass]. This proxy extends the original RealmObject class and
 *     rewires all field access to point to the native Realm memory instead of Java memory. It also
 *     adds some static helper methods to the class.
 *
 *  2. The annotation processor is either in "library" mode or in "app" mode. This is defined by
 *     having a class annotated with @RealmModule(library = true). It is not allowed to have both a
 *     class with `library = true` and `library = false` in the same IntelliJ module and it will
 *     cause the annotation processor to throw an exception. If no library modules are defined, we
 *     will create a DefaultRealmModule containing all known RealmObjects and with the
 *     `@RealmModule` annotation. Realm automatically knows about this module, but it is still
 *     possible for users to create their own modules with a subset of model classes.
 *
 *  3. For each class annotated with @RealmModule a matching Mediator class is created (including
 *     the default one). This class has an interface that matches the static helper methods for the
 *     proxy classes. All access to these static helper methods should be done through this Mediator.
 *
 * This allows ProGuard to obfuscate all RealmObject and proxy classes as all access to the static
 * methods now happens through the Mediator, and the only requirement is now that only RealmModule
 * and Mediator class names cannot be obfuscated.
 *
 * <h1>CREATING A REALM</h1>
 *
 * This means the workflow when instantiating a Realm on runtime is the following:
 *
 *  1. Open a Realm.
 *  2. Assign one or more modules (that are allowed to overlap). If no module is assigned, the
 *     default module is used.
 *  3. The Realm schema is now defined as all RealmObject classes known by these modules.
 *  4. Each time a static helper method is needed, Realm can now delegate these method calls to the
 *     appropriate Mediator which in turn will delegate the method call to the appropriate
 *     RealmObjectProxy class.
 *
 * <h1>CREATING A MANAGED RealmObject</h1>
 *
 * To allow to specify default values by model's constructor or direct field assignment, the flow of
 * creating the proxy object is a bit complicated. This section illustrates how proxy object should
 * be created.
 *
 *  1. Get the thread local `io.realm.BaseRealm.RealmObjectContext` instance by
 *     `BaseRealm.objectContext.get()`
 *  2. Set the object context information to the `RealmObjectContext` those should be set to the
 *     creating proxy object.
 *  3. Create proxy object (`new io.realm.FooRealmProxy()`).
 *  4. Set the object context information to the created proxy when the first access of its
 *     accessors (or in its constructor if accessors are not used in the model's constructor).
 *  5. Clear the object context information in the thread local
 *     `io.realm.BaseRealm.RealmObjectContext` instance by calling `#clear()` method.
 *
 * The reason of this complicated step is that we can't pass these context information
 * via the constructor of the proxy. It's because the constructor of the proxy is executed
 * **after** the constructor of the model class. The access to the fields in the model's
 * constructor happens before the assignment of the context information to the 'proxyState'.
 * This will cause the [NullPointerException] if getters/setter is accessed in the model's
 * constructor (see [Issue #2536](https://github.com/realm/realm-java/issues/2536)).
 */

inline class QualifiedClassName(val name: String) {
    constructor(name: Name): this(name.toString())
    constructor(name: TypeMirror) : this(name.toString())
    fun getSimpleName(): SimpleClassName {
        return SimpleClassName(Utils.stripPackage(name))
    }
    override fun toString(): String {
        return name
    }
}
inline class SimpleClassName(val name: String) {
    constructor(name: Name): this(name.toString())
    override fun toString(): String {
        return name
    }
}

@SupportedAnnotationTypes(
        "io.realm.annotations.RealmClass",
        "io.realm.annotations.RealmField",
        "io.realm.annotations.Ignore",
        "io.realm.annotations.Index",
        "io.realm.annotations.PrimaryKey",
        "io.realm.annotations.RealmModule",
        "io.realm.annotations.Required")
@SupportedOptions(value = ["realm.suppressWarnings", "realm.ignoreKotlinNullability"])
class RealmProcessor : AbstractProcessor() {

    // Don't consume annotations. This allows 3rd party annotation processors to run.
    private val CONSUME_ANNOTATIONS = false
    private val ABORT = true // Abort the annotation processor by consuming all annotations

    private val classCollection = ClassCollection() // Metadata for all classes found
    private lateinit var moduleMetaData: ModuleMetaData // Metadata for all modules found

    // List of backlinks
    private val backlinksToValidate = HashSet<Backlink>()

    private var hasProcessedModules = false
    private var round = -1

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        round++

        if (round == 0) {
            RealmVersionChecker.getInstance(processingEnv).executeRealmVersionUpdate()
        }

        if (roundEnv.errorRaised()) {
            return ABORT
        }

        if (!hasProcessedModules) {
            Utils.initialize(processingEnv)
            val typeMirrors = TypeMirrors(processingEnv)

            // Build up internal metadata while validating as much as possible
            if (!preProcessModules(roundEnv)) {
                return ABORT
            }
            if (!processClassAnnotations(roundEnv, typeMirrors)) {
                return ABORT
            }
            if (!postProcessModules()) {
                return ABORT
            }
            if (!validateBacklinks()) {
                return ABORT
            }
            hasProcessedModules = true

            // Create all files
            if (!createProxyClassFiles(typeMirrors)) {
                return ABORT
            }
            if (!createModuleFiles()) {
                return ABORT
            }
        }

        return CONSUME_ANNOTATIONS
    }

    // Create all proxy classes
    private fun processClassAnnotations(roundEnv: RoundEnvironment, typeMirrors: TypeMirrors): Boolean {

        for (classElement in roundEnv.getElementsAnnotatedWith(RealmClass::class.java)) {

            // The class must either extend RealmObject or implement RealmModel
            if (!Utils.isImplementingMarkerInterface(classElement)) {
                Utils.error("A RealmClass annotated object must implement RealmModel or derive from RealmObject.", classElement)
                return false
            }

            // Check the annotation was applied to a Class
            if (classElement.kind != ElementKind.CLASS) {
                Utils.error("The RealmClass annotation can only be applied to classes.", classElement)
                return false
            }

            val metadata = ClassMetaData(processingEnv, typeMirrors, classElement as TypeElement)
            if (!metadata.isModelClass) {
                continue
            }

            Utils.note("Processing class " + metadata.simpleJavaClassName)
            if (!metadata.generate(moduleMetaData)) {
                return false
            }

            classCollection.addClass(metadata)
            backlinksToValidate.addAll(metadata.backlinkFields)
        }

        return true
    }

    // Returns true if modules were processed successfully, false otherwise
    private fun preProcessModules(roundEnv: RoundEnvironment): Boolean {
        moduleMetaData = ModuleMetaData()
        return moduleMetaData.preProcess(roundEnv.getElementsAnnotatedWith(RealmModule::class.java))
    }

    // Returns true of modules where successfully validated, false otherwise
    private fun postProcessModules(): Boolean {
        return moduleMetaData.postProcess(classCollection)
    }

    private fun createModuleFiles(): Boolean {
        // Create default module if needed
        if (moduleMetaData.shouldCreateDefaultModule()) {
            if (!createDefaultModule()) {
                return false
            }
        }

        // Create RealmProxyMediators for all Realm modules
        for ((key, value) in moduleMetaData.allModules) {
            if (!createMediator(key.getSimpleName(), value)) {
                return false
            }
        }

        return true
    }

    private fun createProxyClassFiles(typeMirrors: TypeMirrors): Boolean {
        for (metadata in classCollection.classes) {
            val interfaceGenerator = RealmProxyInterfaceGenerator(processingEnv, metadata)
            try {
                interfaceGenerator.generate()
            } catch (e: IOException) {
                Utils.error(e.message, metadata.classElement)
                return false
            }

            val sourceCodeGenerator = RealmProxyClassGenerator(processingEnv, typeMirrors, metadata, classCollection)
            try {
                sourceCodeGenerator.generate()
            } catch (e: IOException) {
                Utils.error(e.message, metadata.classElement)
                return false
            } catch (e: UnsupportedOperationException) {
                Utils.error(e.message, metadata.classElement)
                return false
            }

        }
        return true
    }

    private fun createDefaultModule(): Boolean {
        Utils.note("Creating DefaultRealmModule")
        val defaultModuleGenerator = DefaultModuleGenerator(processingEnv)
        try {
            defaultModuleGenerator.generate()
        } catch (e: IOException) {
            Utils.error(e.message)
            return false
        }

        return true
    }

    private fun createMediator(moduleName: SimpleClassName, moduleClasses: Set<ClassMetaData>): Boolean {
        val mediatorImplGenerator = RealmProxyMediatorGenerator(processingEnv, moduleName, moduleClasses)
        try {
            mediatorImplGenerator.generate()
        } catch (e: IOException) {
            Utils.error(e.message)
            return false
        }

        return true
    }

    // Because library classes are processed separately, there is no guarantee that this method can
    // see all of the classes necessary to completely validate all of the backlinks.  If it can find
    // the fully-qualified class, though, and prove that the class either does not contain the
    // necessary field, or that it does contain the field, but the field is of the wrong type, it
    // can catch the error at compile time. Otherwise it is caught at runtime, when validating the
    // schema.
    private fun validateBacklinks(): Boolean {
        var allValid = true

        for (backlink in backlinksToValidate) {
            // If the class is not here it might be part of some other compilation unit.
            if (!classCollection.containsQualifiedClass(backlink.sourceClass)) {
                continue
            }
            val clazz = classCollection.getClassFromQualifiedName(backlink.sourceClass!!)

            // If the class is here, we can validate it.
            if (!backlink.validateTarget(clazz) && allValid) {
                allValid = false
            }
        }

        return allValid
    }
}
