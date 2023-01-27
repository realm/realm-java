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
package io.realm.transformer.build

import io.realm.transformer.*
import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Pattern

public const val DOT_CLASS = ".class"
public const val DOT_JAR = ".jar"

/**
 * Abstract class defining the structure of doing different types of builds.
 *
 */
abstract class BuildTemplate(private val metadata: ProjectMetaData, private val allJars: ListProperty<RegularFile>, protected val outputProvider: JarOutputStream, val transform: RealmTransformer) {

    protected lateinit var inputs: ListProperty<Directory>
    protected lateinit var classPool: ManagedClassPool
    protected val outputClassNames: MutableSet<String> = hashSetOf()
    protected val outputReferencedClassNames: MutableSet<String> = hashSetOf()
    protected val outputModelClasses: ArrayList<CtClass> = arrayListOf()
    protected val processedClasses = mutableMapOf<String, CtClass>()

    /**
     * Find all the class names available for transforms as well as all referenced classes.
     */
    abstract fun prepareOutputClasses(inputs: ListProperty<Directory>)

    /**
     * Helper method for going through all `TransformInput` and sort classes into buckets of
     * source files in the current project or source files found in jar files.
     *
     * @param inputs set of input files
     * @param directoryFiles the set of files in directories getting compiled. These are potential
     * candidates for the transformer.
     * @param jaFiles the set of files found in jar files. These will never be transformed. This should
     * already be done when creating the jar file.
     */
    protected abstract fun categorizeClassNames(inputs: ListProperty<Directory>,
                                          directoryFiles: MutableSet<String>)

    protected abstract fun categorizeClassNames(referencedInputs: ConfigurableFileCollection,
                                                jarFiles: MutableSet<String>)

    /**
     * Returns `true` if this build contains no relevant classes to transform.
     */
    fun hasNoOutput(): Boolean {
        return outputClassNames.isEmpty()
    }


    fun prepareReferencedClasses(referencedInputs: ConfigurableFileCollection) {
        categorizeClassNames(referencedInputs, outputReferencedClassNames) // referenced files

        // Create and populate the Javassist class pool
        this.classPool = ManagedClassPool(inputs, referencedInputs)
        // Append android.jar to class pool. We don't need the class names of them but only the class in the pool for
        // javassist. See https://github.com/realm/realm-java/issues/2703.
        addBootClassesToClassPool(classPool)
        logger.debug("ClassPool contains Realm classes: ${classPool.getOrNull("io.realm.RealmList") != null}")

        filterForModelClasses(outputClassNames, outputReferencedClassNames)
    }

    protected abstract fun filterForModelClasses(outputClassNames: Set<String>, outputReferencedClassNames: Set<String>)


    fun markMediatorsAsTransformed() {
        val baseProxyMediator: CtClass = classPool.get("io.realm.internal.RealmProxyMediator")
        val mediatorPattern: Pattern = Pattern.compile("^io\\.realm\\.[^.]+Mediator$")
        val proxyMediatorClasses: Collection<CtClass> = outputClassNames
                .filter { mediatorPattern.matcher(it).find() }
                .map { classPool.getCtClass(it) }
                .filter { it.superclass.equals(baseProxyMediator) }

        logger.debug("Proxy Mediator Classes: ${proxyMediatorClasses.joinToString(",") { it.name }}")
        proxyMediatorClasses.forEach {
            BytecodeModifier.overrideTransformedMarker(it)
            processedClasses[it.name] = it
        }
    }

    fun transformModelClasses() {
        // Add accessors to the model classes in the target project
        outputModelClasses.forEach {
            logger.debug("Modify model class: ${it.name}")
            BytecodeModifier.addRealmAccessors(it)
            BytecodeModifier.addRealmProxyInterface(it, classPool)
            BytecodeModifier.callInjectObjectContextFromConstructors(it)

            processedClasses[it.name] = it
        }
    }

    abstract fun transformDirectAccessToModelFields()

    fun copyProcessedClasses() {
        for ((fqname: String, clazz: CtClass) in processedClasses) {
            outputProvider.putNextEntry(JarEntry("${fqname.replace('.', '/')}.class"))
            outputProvider.write(clazz.toBytecode())
            outputProvider.closeEntry()
        }
    }

    fun copyResourceFiles() {
        inputs.get().forEach { directory: Directory ->
            val dirName = directory.asFile.absolutePath + File.separator
            directory.asFile.walk().filter(File::isFile).forEach { file ->
                if (!file.absolutePath.endsWith(DOT_CLASS)) {
                    val pathWithoutPrefix = file.absolutePath.removePrefix(dirName)
                    // We need to transform platform paths into consistent zip entry paths
                    // https://github.com/realm/realm-java/issues/7757
                    val zipEntryPath = pathWithoutPrefix.replace(File.separatorChar, '/')
                    outputProvider.putNextEntry(JarEntry(zipEntryPath))
                    outputProvider.write(file.readBytes())
                    outputProvider.closeEntry()
                }
            }
        }
        allJars.get().forEach { file ->
            val jarFile = JarFile(file.asFile)
            for (jarEntry: JarEntry in jarFile.entries()) {
                outputProvider.putNextEntry(JarEntry(jarEntry.name))
                jarFile.getInputStream(jarEntry).use {
                    outputProvider.write(it.readBytes())
                }
                outputProvider.closeEntry()
            }
            jarFile.close()
        }

        classPool.close()
    }

    /**
     * There is no official way to get the path to android.jar for transform.
     * See https://code.google.com/p/android/issues/detail?id=209426
     */
    private fun addBootClassesToClassPool(classPool: ClassPool) {
        try {
            metadata.bootClassPath.forEach {
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

    fun getOutputModelClasses(): Set<CtClass> {
        return outputModelClasses.toSet()
    }

    protected abstract fun findModelClasses(classNames: Set<String>): Collection<CtClass>

}
