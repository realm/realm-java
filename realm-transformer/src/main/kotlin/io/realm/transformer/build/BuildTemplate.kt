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

import com.android.SdkConstants
import com.android.build.api.transform.Format
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.google.common.io.Files
import io.realm.transformer.BytecodeModifier
import io.realm.transformer.ManagedClassPool
import io.realm.transformer.logger
import io.realm.transformer.Utils
import io.realm.transformer.ext.getBootClasspath
import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Project
import java.io.File
import java.util.regex.Pattern

/**
 * Abstract class defining the structure of doing different types of builds.
 *
 */
abstract class BuildTemplate(val project: Project, val outputProvider: TransformOutputProvider, val transform: Transform) {

    protected lateinit var inputs: MutableCollection<TransformInput>
    protected lateinit var classPool: ManagedClassPool
    protected val outputClassNames: MutableSet<String> = hashSetOf()
    protected val outputReferencedClassNames: MutableSet<String> = hashSetOf()
    protected val outputModelClasses: ArrayList<CtClass> = arrayListOf()

    /**
     * Find all the class names available for transforms as well as all referenced classes.
     */
    abstract fun prepareOutputClasses(inputs: MutableCollection<TransformInput>)

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
    protected abstract fun categorizeClassNames(inputs: Collection<TransformInput>,
                                          directoryFiles: MutableSet<String>,
                                          referencedFiles: MutableSet<String>)

    /**
     * Returns `true` if this build contains no relevant classes to transform.
     */
    fun hasNoOutput(): Boolean {
        return outputClassNames.isEmpty()
    }


    fun prepareReferencedClasses(referencedInputs: Collection<TransformInput>) {
        categorizeClassNames(referencedInputs, outputReferencedClassNames, outputReferencedClassNames) // referenced files

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
        }
    }

    fun transformModelClasses() {
        // Add accessors to the model classes in the target project
        outputModelClasses.forEach {
            logger.debug("Modify model class: ${it.name}")
            BytecodeModifier.addRealmAccessors(it)
            BytecodeModifier.addRealmProxyInterface(it, classPool)
            BytecodeModifier.callInjectObjectContextFromConstructors(it)
        }
    }

    abstract fun transformDirectAccessToModelFields()

    fun copyResourceFiles() {
        copyResourceFiles(inputs)
        classPool.close();
    }

    private fun copyResourceFiles(inputs: MutableCollection<TransformInput>) {
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

    protected fun getOutputFile(outputProvider: TransformOutputProvider): File {
        return outputProvider.getContentLocation(
                "realm", transform.inputTypes, transform.scopes, Format.DIRECTORY)
    }

    /**
     * There is no official way to get the path to android.jar for transform.
     * See https://code.google.com/p/android/issues/detail?id=209426
     */
    private fun addBootClassesToClassPool(classPool: ClassPool) {
        try {
            project.getBootClasspath().forEach {
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
