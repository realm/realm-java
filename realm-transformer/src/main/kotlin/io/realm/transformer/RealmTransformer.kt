/*
 * Copyright 2018 Realm Inc.
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

package io.realm.transformer

import com.android.SdkConstants
import com.android.build.api.transform.*
import com.google.common.io.Files
import io.realm.annotations.RealmClass
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.NotFoundException
import javassist.bytecode.ClassFile
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.jar.JarFile
import java.util.regex.Pattern

/**
 * This class implements the Transform API provided by the Android Gradle plugin.
 */
class RealmTransformer(val project: Project) : Transform() {

    val logger: Logger = LoggerFactory.getLogger("realm-logger")

    override fun getName(): String {
        return "RealmTransformer"
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return setOf<QualifiedContent.ContentType>(QualifiedContent.DefaultContentType.CLASSES)
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return mutableSetOf(QualifiedContent.Scope.PROJECT)
    }

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> {
        // Scope.PROJECT_LOCAL_DEPS and Scope.SUB_PROJECTS_LOCAL_DEPS is only for compatibility with AGP 1.x, 2.x
        return mutableSetOf(
                QualifiedContent.Scope.EXTERNAL_LIBRARIES,
                QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
                QualifiedContent.Scope.TESTED_CODE
        )
    }

    /**
     * Implements the transform algorithm. The heaviest part of the transform is loading the
     * {@code CtClass} from JavaAssist, so this should be avoided as much as possible.
     *
     * This is also the reason that there are significant changes between a full build and a
     * incremental build. In a full build, we can use text matching to go from a proxy class
     * to the model class. Something we cannot do when building incrementally. In that case
     * we have to deduce all information from the class at hand.
     *
     * @param context
     * @param inputs
     * @param referencedInputs
     * @param outputProvider
     * @param isIncremental
     * @throws IOException
     * @throws TransformException
     * @throws InterruptedException
     */
    override fun transform(context: Context?, inputs: MutableCollection<TransformInput>?,
                           referencedInputs: Collection<TransformInput>?,
                           outputProvider: TransformOutputProvider?, isIncremental: Boolean) {

        val tic = System.currentTimeMillis()

        // Find all the class names available for transforms as well as all referenced classes.
        val outputClassNames: MutableSet<String> = hashSetOf()
        val outputReferencedClassNames: MutableSet<String> = hashSetOf()
        findClassNames(inputs!!, outputClassNames, outputReferencedClassNames, isIncremental) // Output files

        logger.debug("Incremental build: $isIncremental, files being processed: ${outputClassNames.size}.")
        if (isIncremental) {
            logger.debug("Incremental files: ${outputClassNames.joinToString(",")}")
        }

        // Abort transform as quickly as possible if no files where found for processing.
        if (outputClassNames.isEmpty()) {
            exitTransform(emptySet(), emptyList(), tic)
            return
        }

        findClassNames(referencedInputs!!, outputReferencedClassNames, outputReferencedClassNames, isIncremental) // referenced files
        val allClassNames: Set<String> = merge(outputClassNames, outputReferencedClassNames)

        // Create and populate the Javassist class pool
        val classPool = ManagedClassPool(inputs, referencedInputs)
        // Append android.jar to class pool. We don't need the class names of them but only the class in the pool for
        // javassist. See https://github.com/realm/realm-java/issues/2703.
        addBootClassesToClassPool(classPool)
        logger.debug("ClassPool contains Realm classes: ${classPool.getOrNull("io.realm.RealmList") != null}")

        // mark as transformed
        val baseProxyMediator: CtClass = classPool.get("io.realm.internal.RealmProxyMediator")
        val mediatorPattern: Pattern = Pattern.compile("^io\\.realm\\.[^.]+Mediator$")
        val proxyMediatorClasses: Collection<CtClass> = outputClassNames
                .filter { mediatorPattern.matcher(it).find() }
                .map { classPool.getCtClass(it) }
                .filter { it.superclass.equals(baseProxyMediator) }

        logger.debug("Proxy Mediator Classes: ${proxyMediatorClasses.joinToString(",") { it.name }}")
        proxyMediatorClasses.forEach {
            BytecodeModifier.overrideTransformedMarker(it)
        }

        val allModelClasses = findModelClasses(classPool, isIncremental, allClassNames)
        logger.debug("All Model Classes: ${allModelClasses.joinToString(",") { it.name }}")

        val outputModelClasses: Collection<CtClass> = allModelClasses.filter {
            outputClassNames.contains(it.name)
        }

        // Add accessors to the model classes in the target project
        outputModelClasses.forEach {
            logger.debug("Modify model class: ${it.name}")
            BytecodeModifier.addRealmAccessors(it)
            BytecodeModifier.addRealmProxyInterface(it, classPool)
            BytecodeModifier.callInjectObjectContextFromConstructors(it)
        }

        // Populate a list of the fields that need to be managed with bytecode manipulation
        val allManagedFields: ArrayList<CtField> = arrayListOf()
        allModelClasses.forEach {
            allManagedFields.addAll(it.declaredFields.filter {
                BytecodeModifier.isModelField(it)
            })
        }
        logger.debug("Managed Fields: ${allManagedFields.joinToString(",") { it.name }}")

        // Use accessors instead of direct field access
        outputClassNames.forEach {
            logger.debug("Modify accessors in class: $it")
            val ctClass: CtClass = classPool.getCtClass(it)
            BytecodeModifier.useRealmAccessors(ctClass, allManagedFields)
            ctClass.writeFile(getOutputFile(outputProvider!!).canonicalPath)
        }

        copyResourceFiles(inputs, outputProvider!!)
        classPool.close()
        exitTransform(inputs, outputModelClasses, tic)

    }

    /**
     * Go through the transform input in order to find all files we need to transform.
     *
     * @param inputs set of input files
     * @param directoryFiles the set of files in directories getting compiled. These are candidates for the transformer.
     * @param referencedFiles the set of files that are possible referenced but never transformed (required by JavaAssist).
     * @param isIncremental `true` if build is incremental.
     */
    private fun findClassNames(inputs: Collection<TransformInput>, directoryFiles: MutableSet<String>,
                               referencedFiles: MutableSet<String>, isIncremental: Boolean) {
        inputs.forEach {

            // Files in directories are files we most likely want to transform, unless they are
            // marked as referenced scope. See {@link Transform#getReferencedScopes()}.
            it.directoryInputs.forEach {
                val dirPath: String = it.file.absolutePath
                if (isIncremental) {
                    // Incremental builds: Include all changed or added files, i.e. ignore deleted files
                    it.changedFiles.entries.forEach {
                        if (it.value == Status.NOTCHANGED || it.value == Status.REMOVED) {
                            return
                        }
                        val filePath: String = it.key.absolutePath
                        if (filePath.endsWith(SdkConstants.DOT_CLASS)) {
                            val className = filePath
                                    .substring( dirPath.length + 1, filePath.length - SdkConstants.DOT_CLASS.length)
                                    .replace(File.separatorChar, '.')
                            directoryFiles.add(className)
                        }
                    }

                } else {
                    // Non-incremental build: Include all files
                    it.file.walkTopDown().forEach {
                        if (it.isFile) {
                            if (it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                                val className: String = it.absolutePath
                                        .substring(dirPath.length + 1, it.absolutePath.length - SdkConstants.DOT_CLASS.length)
                                        .replace(File.separatorChar, '.')
                                directoryFiles.add(className)
                            }
                        }
                    }
                }
            }

            // Files in Jars are always treated as referenced input. They should already have been
            // modified by the transformer in the project that built the jar.
            it.jarInputs.forEach {
                if (isIncremental && (it.status == Status.REMOVED)) {
                    return;
                }

                val jarFile = JarFile(it.file)
                jarFile.entries()
                    .toList()
                    .filter {
                        !it.isDirectory && it.name.endsWith(SdkConstants.DOT_CLASS)
                    }
                    .forEach {
                        val path: String = it.name
                        // The jar might not using File.separatorChar as the path separator. So we just replace both `\` and
                        // `/`. It depends on how the jar file was created.
                        // See http://stackoverflow.com/questions/13846000/file-separators-of-path-name-of-zipentry
                        val className: String = path
                            .substring(0, path.length - SdkConstants.DOT_CLASS.length)
                            .replace('/', '.')
                            .replace('\\', '.')
                       referencedFiles.add(className)
                    }
                jarFile.close() // Crash transformer if this fails
            }
        }
    }

    private fun findModelClasses(classPool: ClassPool, isIncrementalBuild: Boolean, classNames: Set<String>): Collection<CtClass>  {
        val realmObjectProxyInterface: CtClass = classPool.get("io.realm.internal.RealmObjectProxy")
        if (isIncrementalBuild) {
            // For incremental builds we need to determine if a class is a model class file
            // based on information in the file itself. This require checks that are only
            // possible once we loaded the CtClass from the ClassPool and is slower
            // than the approach used when doing full builds.
            return classNames
                    // Map strings to CtClass'es.
                    .map { classPool.getCtClass(it) }
                    // Model classes either have the @RealmClass annotation directly (if implementing RealmModel)
                    // or their superclass has it (if extends RealmObject). The annotation processor
                    // will have ensured the annotation is only present in these cases.
                    .filter {
                        var result: Boolean
                        if (it.hasAnnotation(RealmClass::class.java)) {
                            result = true
                        } else {
                            try {
                                result = it.superclass?.hasAnnotation(RealmClass::class.java) == true
                            } catch (e: NotFoundException) {
                                // Can happen if the super class is part of the `android.jar` which might
                                // not have been loaded. In any case, any base class part of Android cannot
                                // be a Realm model class.
                                result = false
                            }
                        }
                        return@filter result
                    }
                    // Proxy classes are generated by the Realm Annotation Processor and might accidentally
                    // parse the above check (e.g. if the model class has the @RealmClass annotation), so
                    // ignore them.
                    .filter { !isSubtypeOf(classPool, it, realmObjectProxyInterface) }
                    // Unfortunately the RealmObject base class parses all above checks, so explicitly
                    // ignore it.
                    .filter { !it.name.equals("io.realm.RealmObject") }

        } else {
            // For full builds, we are currently finding model classes by assuming that only
            // the annotation processor is generating files ending with `RealmProxy`. This is
            // a lot faster as we only need to compare the name of the type before we load
            // the CtClass.
            // Find the model classes
            return classNames
                    // Quick and loose filter where we assume that classes ending with RealmProxy are
                    // a Realm model proxy class generated by the annotation processor. This can
                    // produce false positives: https://github.com/realm/realm-java/issues/3709
                    .filter { it.endsWith("RealmProxy") }
                    .mapNotNull {
                        // Verify the file is in fact a proxy class, in which case the super
                        // class is always present and is the real model class.
                        val clazz: CtClass = classPool.getCtClass(it)
                        if (isSubtypeOf(classPool, clazz, realmObjectProxyInterface)) {
                            return@mapNotNull clazz.superclass;
                        } else {
                            return@mapNotNull null;
                        }
                    }
        }
    }

    /**
     * Returns {@code true} if 'clazz' is considered a subtype of 'superType'.
     *
     * This function is different than {@link CtClass#subtypeOf(CtClass)} in the sense
     * that it will never crash even if classes are missing from the class pool, instead
     * it will just return {@code false}.
     *
     * This e.g. happens with RxJava classes which are optional, but JavaAssist will try
     * to load them and then crash.
     *
     * @param classPool pool of all known classes
     * @param clazz class to check
     * @param typeToCheckAgainst the type we want to check against
     * @return `true` if `clazz` is a subtype of `typeToCheckAgainst`, `false` otherwise.
     */
    private fun isSubtypeOf(classPool: ClassPool, clazz: CtClass, typeToCheckAgainst: CtClass): Boolean {
        val typeToCheckAgainstQualifiedName: String = typeToCheckAgainst.name
        if (clazz == typeToCheckAgainst || clazz.name.equals(typeToCheckAgainstQualifiedName)) {
            return true
        }

        val file: ClassFile = clazz.getClassFile2();

        // Check direct super class
        val superName: String? = file.getSuperclass();
        if (superName.equals(typeToCheckAgainstQualifiedName)) {
            return true;
        }

        // Check direct interfaces
        val ifs: Array<String> = file.interfaces;
        val num: Int = ifs.size
        for (i in 0..num) {
            if (ifs[i] == typeToCheckAgainstQualifiedName) {
                return true;
            }
        }

        // Check other inherited super classes
        if (superName != null) {
            var nextSuper: CtClass
            try {
                nextSuper = classPool.get(superName)
                if (isSubtypeOf(classPool, nextSuper, typeToCheckAgainst)) {
                    return true
                }
            } catch (ignored: NotFoundException) {
            }
        }

        // Check other inherited interfaces
        for (i in 0..num) {
            try {
                val interfaceClass: CtClass = classPool.get(ifs[i])
                if (isSubtypeOf(classPool, interfaceClass, typeToCheckAgainst)) {
                    return true;
                }
            } catch (ignored: NotFoundException) {
            }
        }

        return false;
    }


    private fun exitTransform(inputs: Collection<TransformInput>, outputModelClasses: Collection<CtClass>, startTime: Long) {
        val endTime = System.currentTimeMillis()
        logger.debug("Realm Transform time: ${endTime-startTime} milliseconds")
        this.sendAnalytics(inputs, outputModelClasses)
    }

    /**
     * Sends the analytics
     *
     * @param inputs the inputs provided by the Transform API
     * @param inputModelClasses a list of ctClasses describing the Realm models
     */
    private fun sendAnalytics(inputs: Collection<TransformInput>, outputModelClasses: Collection<CtClass>) {
        val disableAnalytics: Boolean = "true".equals(System.getenv()["REALM_DISABLE_ANALYTICS"])
        if (inputs.isEmpty() || disableAnalytics) {
            // Don't send analytics for incremental builds or if they have ben explicitly disabled.
            return
        }

        var containsKotlin = false

        outer@
        for(input: TransformInput in inputs) {
            for (di: DirectoryInput in input.directoryInputs) {
                val path: String = di.file.absolutePath
                val index: Int = path.indexOf("build${File.separator}intermediates${File.separator}classes")
                if (index != -1) {
                    val projectPath: String = path.substring(0, index)
                    val buildFile = File(projectPath + "build.gradle")
                    if (buildFile.exists() && buildFile.readText().contains("kotlin")) {
                        containsKotlin = true
                        break@outer
                    }
                }
            }
        }

        val packages: Collection<String> = outputModelClasses.map {
            it.packageName
        }

        val targetSdk: String? = GroovyUtil.getTargetSdk(project)
        val minSdk: String?  = GroovyUtil.getMinSdk(project)

        if (disableAnalytics) {
            val sync: Boolean = GroovyUtil.isSyncEnabled(project)
            val analytics = RealmAnalytics(packages as Set, containsKotlin, sync, targetSdk, minSdk)
            analytics.execute()
        }
    }

    private fun merge(set1: Set<String>, set2: Set<String>): Set<String>  {
        val merged: MutableSet<String> = hashSetOf()
        merged.addAll(set1)
        merged.addAll(set2)
        return merged
    }


    /**
     * There is no official way to get the path to android.jar for transform.
     * See https://code.google.com/p/android/issues/detail?id=209426
     */
    private fun addBootClassesToClassPool(classPool: ClassPool) {
        try {
            GroovyUtil.getBootClasspath(project).forEach {
                val path: String = it.absolutePath
                logger.debug("Add boot class $path to class pool.")
                classPool.appendClassPath(path)
            }
        } catch (e: Exception) {
            // Just log it. It might not impact the transforming if the method which needs to be transformer doesn't
            // contain classes from android.jar.
            logger.debug("Cannot get bootClasspath caused by: ", e)
        }
    }

    private fun getOutputFile(outputProvider: TransformOutputProvider): File {
        return outputProvider.getContentLocation(
                "realm", inputTypes, scopes, Format.DIRECTORY)
    }

    private fun copyResourceFiles(inputs: MutableCollection<TransformInput> , outputProvider: TransformOutputProvider) {
        inputs.forEach {
            it.directoryInputs.forEach {
                val dirPath: String = it.file.absolutePath
                it.file.walkTopDown().forEach {
                    if (it.isFile) {
                        if (!it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                            logger.debug("  Copying resource $it")
                            val dest = File(getOutputFile(outputProvider), it.absolutePath.substring(dirPath.length))
                            dest.parentFile.mkdirs()
                            Files.copy(it, dest)
                        }
                    }
                }
            }
            // no need to implement the code for `it.jarInputs.each` since PROJECT SCOPE does not use jar input.
        }
    }
}
