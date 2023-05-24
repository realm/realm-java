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
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.jar.JarFile
import java.util.regex.Pattern

const val DOT_CLASS = ".class"

/**
 * Abstract class defining the structure of doing different types of builds.
 *
 */
abstract class BuildTemplate(
    private val metadata: ProjectMetaData,
    private val allJars: List<RegularFile>,
    protected val outputProvider: FileSystem,
    val inputs: List<Directory>,
) {
    protected lateinit var classPool: ManagedClassPool
    protected lateinit var outputClassNames: Set<String>
    private lateinit var outputReferencedClassNames: Set<String>
    protected lateinit var outputModelClasses: List<CtClass>
    protected val processedClasses = mutableMapOf<String, CtClass>()

    protected fun File.categorize(dirPath: String): String =
        absolutePath
            .substring(
                startIndex = dirPath.length + 1,
                endIndex = absolutePath.length - DOT_CLASS.length
            )
            .replace(File.separatorChar, '.')

    /**
     * Find all the class names available for transforms as well as all referenced classes.
     */
    abstract fun prepareOutputClasses()

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
     fun categorizeClassNames():Set<String> {
        return inputs.flatMap { directory ->
            val dirPath: String = directory.asFile.absolutePath

            directory.asFile.walk()
                .filter(File::isFile)
                .filter { file -> file.shouldCategorize() }
                .filter { file -> file.absolutePath.endsWith(DOT_CLASS) }
                .map { it.categorize(dirPath) }
        }.toSet()
    }

    private fun categorizeClassNames(referencedInputs: ConfigurableFileCollection): Set<String> =
        referencedInputs.flatMap { file ->
            JarFile(file).use { jarFile ->
                jarFile.entries()
                    .toList()
                    .filter { jarEntry ->
                        !jarEntry.isDirectory && jarEntry.name.endsWith(DOT_CLASS)
                    }
                    .map { jarEntry ->
                        val path: String = jarEntry.name
                        // The jar might not using File.separatorChar as the path separator. So we just replace both `\` and
                        // `/`. It depends on how the jar file was created.
                        // See http://stackoverflow.com/questions/13846000/file-separators-of-path-name-of-zipentry
                        path.substring(startIndex = 0, endIndex = path.length - DOT_CLASS.length)
                            .replace('/', '.')
                            .replace('\\', '.')
                    }
            }// Crash transformer if this fails to close
        }.toSet()

    /**
     * Returns `true` if this build contains no relevant classes to transform.
     */
    fun hasNoOutput(): Boolean {
        return outputClassNames.isEmpty()
    }


    fun prepareReferencedClasses(referencedInputs: ConfigurableFileCollection) {
        outputReferencedClassNames = categorizeClassNames(referencedInputs) // referenced files

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
        processedClasses.forEach { (fqName: String, clazz: CtClass) ->
            outputProvider.addEntry(
                "${fqName.replace('.', '/')}.class",
                clazz.toBytecode()
            )
        }
    }

    fun copyResourceFiles() {
        inputs.forEach { directory: Directory ->
            val dirName = directory.asFile.absolutePath + File.separator
            directory.asFile.walk().filter(File::isFile)
                .filterNot { it.absolutePath.endsWith(DOT_CLASS) }
                .forEach { file ->
                    val pathWithoutPrefix = file.absolutePath.removePrefix(dirName)
                    // We need to transform platform paths into consistent zip entry paths
                    // https://github.com/realm/realm-java/issues/7757
                    val zipEntryPath = pathWithoutPrefix.replace(File.separatorChar, '/')

                    outputProvider.addEntry(zipEntryPath, file.readBytes())
                }
        }
        allJars.forEach { file ->
            JarFile(file.asFile).use { jarFile ->
                jarFile.entries().toList()
                    .forEach { jarEntry ->
                        val jarBytes = jarFile.getInputStream(jarEntry).use {
                            it.readBytes()
                        }

                        outputProvider.addEntry(
                            jarEntry.name,
                            jarBytes
                        )
                    }
            }
        }

        classPool.close()
    }

    private fun FileSystem.addEntry(entryPath: String, input: ByteArray) {
        getPath(entryPath).let { path ->
            path.parent?.let { Files.createDirectories(it) }
            Files.newOutputStream(path, StandardOpenOption.CREATE).use { stream ->
                stream.write(input)
                stream.close()
            }
        }
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

    protected abstract fun findModelClasses(classNames: Set<String>): List<CtClass>

    open fun File.shouldCategorize(): Boolean = true
}
