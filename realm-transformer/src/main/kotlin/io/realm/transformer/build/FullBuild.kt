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

import io.realm.transformer.BytecodeModifier
import io.realm.transformer.ProjectMetaData
import io.realm.transformer.ext.safeSubtypeOf
import io.realm.transformer.logger
import javassist.CtClass
import javassist.CtField
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import java.io.File
import java.nio.file.FileSystem
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class FullBuild(
    metadata: ProjectMetaData,
    allJars: ListProperty<RegularFile>,
    outputProvider: FileSystem
) : BuildTemplate(
    metadata = metadata,
    allJars = allJars,
    outputProvider = outputProvider
) {
    private val allModelClasses: ArrayList<CtClass> = arrayListOf()

    override fun prepareOutputClasses(inputs: ListProperty<Directory>) {
        this.inputs = inputs;
        categorizeClassNames(inputs, outputClassNames)
        logger.debug("Full build. Files being processed: ${outputClassNames.size}.")
    }

    override fun categorizeClassNames(inputs: ListProperty<Directory>,
                                      directoryFiles: MutableSet<String>) {

        inputs.get().forEach { directory ->
            val dirPath: String = directory.asFile.absolutePath
            directory.asFile.walk().filter(File::isFile).forEach { file ->
                if (file.absolutePath.endsWith(DOT_CLASS)) {
                    val className: String = file.absolutePath
                        .substring(dirPath.length + 1, file.absolutePath.length - DOT_CLASS.length)
                        .replace(File.separatorChar, '.')
                    directoryFiles.add(className)
                }
            }
        }
    }

    override fun categorizeClassNames(referencedInputs: ConfigurableFileCollection,
                                      jarFiles: MutableSet<String>) {

        referencedInputs.forEach {
                val jarFile = JarFile(it)
                jarFile.entries()
                        .toList()
                        .filter {
                            !it.isDirectory && it.name.endsWith(DOT_CLASS)
                        }
                        .forEach {
                            val path: String = it.name
                            // The jar might not using File.separatorChar as the path separator. So we just replace both `\` and
                            // `/`. It depends on how the jar file was created.
                            // See http://stackoverflow.com/questions/13846000/file-separators-of-path-name-of-zipentry
                            val className: String = path
                                    .substring(0, path.length - DOT_CLASS.length)
                                    .replace('/', '.')
                                    .replace('\\', '.')
                            jarFiles.add(className)
                        }
                jarFile.close() // Crash transformer if this fails
            }
    }

    override fun findModelClasses(classNames: Set<String>): Collection<CtClass> {
        val realmObjectProxyInterface: CtClass = classPool.get("io.realm.internal.RealmObjectProxy")

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
                if (clazz.safeSubtypeOf(realmObjectProxyInterface)) {
                    return@mapNotNull clazz.superclass;
                } else {
                    return@mapNotNull null
                }
            }
    }

    override fun filterForModelClasses(classNames: Set<String>, extraClassNames: Set<String>) {

        val allClassNames: Set<String> = merge(classNames, extraClassNames)

        allModelClasses.addAll(findModelClasses(allClassNames))

        outputModelClasses.addAll(allModelClasses.filter {
            outputClassNames.contains(it.name)
        })
    }

    override fun transformDirectAccessToModelFields() {
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
            logger.debug("Modifying accessors in class: $it")
            try {
                val ctClass: CtClass = classPool.getCtClass(it)
                if (ctClass.isFrozen) {
                    ctClass.defrost()
                }
                BytecodeModifier.useRealmAccessors(classPool, ctClass, allManagedFields)
                processedClasses[it] = ctClass
            } catch (e: Exception) {
                throw RuntimeException("Failed to transform $it.", e)
            }
        }
    }

    private fun merge(set1: Set<String>, set2: Set<String>): Set<String>  {
        val merged: MutableSet<String> = hashSetOf()
        merged.addAll(set1)
        merged.addAll(set2)
        return merged
    }

}