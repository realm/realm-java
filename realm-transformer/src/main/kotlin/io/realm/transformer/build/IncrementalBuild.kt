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
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import io.realm.annotations.RealmClass
import io.realm.transformer.BytecodeModifier
import io.realm.transformer.RealmTransformer
import io.realm.transformer.ext.safeSubtypeOf
import io.realm.transformer.logger
import javassist.CtClass
import javassist.NotFoundException
import org.gradle.api.Project
import java.io.File
import java.util.jar.JarFile

class IncrementalBuild(project: Project, outputProvider: TransformOutputProvider, transform: RealmTransformer)
    : BuildTemplate(project, outputProvider, transform) {

    override fun prepareOutputClasses(inputs: MutableCollection<TransformInput>) {
        this.inputs = inputs;
        categorizeClassNames(inputs, outputClassNames, outputReferencedClassNames) // Output files
        logger.debug("Incremental build. Files being processed: ${outputClassNames.size}.")
        logger.debug("Incremental files: ${outputClassNames.joinToString(",")}")
    }

    override fun filterForModelClasses(outputClassNames: Set<String>, outputReferencedClassNames: Set<String>) {
        outputModelClasses.addAll(findModelClasses(outputClassNames))
    }

    override fun transformDirectAccessToModelFields() {
        // Use accessors instead of direct field access
        outputClassNames.forEach {
            logger.debug("Modify accessors in class: $it")
            val ctClass: CtClass = classPool.getCtClass(it)
            BytecodeModifier.useRealmAccessors(classPool, ctClass, null)
            ctClass.writeFile(getOutputFile(outputProvider).canonicalPath)
        }

    }


    /**
     * Categorize the transform input into its two main categorizes: `directoryFiles` which are
     * source files in the current project and `jarFiles` which are source files found in jars.
     *
     * @param inputs set of input files
     * @param directoryFiles the set of files in directories getting compiled. These are candidates for the transformer.
     * @param jarFiles the set of files that are possible referenced but never transformed (required by JavaAssist).
     * @param isIncremental `true` if build is incremental.
     */
    override fun categorizeClassNames(inputs: Collection<TransformInput>,
                             directoryFiles: MutableSet<String>,
                             jarFiles: MutableSet<String>) {
        inputs.forEach {
             it.directoryInputs.forEach {
                val dirPath: String = it.file.absolutePath

                 it.changedFiles.entries.forEach {
                    if (it.value == Status.NOTCHANGED || it.value == Status.REMOVED) {
                        return@forEach
                    }
                    val filePath: String = it.key.absolutePath
                    if (filePath.endsWith(SdkConstants.DOT_CLASS)) {
                        val className = filePath
                                .substring(dirPath.length + 1, filePath.length - SdkConstants.DOT_CLASS.length)
                                .replace(File.separatorChar, '.')
                        directoryFiles.add(className)
                    }
                }
            }

            it.jarInputs.forEach {
                if (it.status == Status.REMOVED) {
                    return@forEach
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
                            jarFiles.add(className)
                        }
                jarFile.close() // Crash transformer if this fails
            }
        }
    }

    override fun findModelClasses(classNames: Set<String>): Collection<CtClass> {
        val realmObjectProxyInterface: CtClass = classPool.get("io.realm.internal.RealmObjectProxy")
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
                // pass the above check (e.g. if the model class has the @RealmClass annotation), so
                // ignore them.
                .filter { !it.safeSubtypeOf(realmObjectProxyInterface) }
                // Unfortunately the RealmObject base class passes all above checks, so explicitly
                // ignore it.
                .filter { !it.name.equals("io.realm.RealmObject") }
    }
}