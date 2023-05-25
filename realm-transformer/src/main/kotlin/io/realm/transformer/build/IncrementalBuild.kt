package io.realm.transformer.build

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

import io.realm.annotations.RealmClass
import io.realm.transformer.BytecodeModifier
import io.realm.transformer.ProjectMetaData
import io.realm.transformer.ext.hasRealmClassAnnotation
import io.realm.transformer.ext.safeSubtypeOf
import io.realm.transformer.logger
import javassist.CtClass
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.incremental.InputFileDetails
import org.gradle.internal.execution.history.changes.DefaultFileChange
import org.gradle.internal.execution.history.changes.IncrementalInputChanges
import java.io.File
import java.nio.file.FileSystem
import kotlin.io.path.deleteIfExists

class IncrementalBuild(
    metadata: ProjectMetaData,
    private val incrementalInputChanges: IncrementalInputChanges,
    inputJars: List<RegularFile>,
    inputDirectories: List<Directory>,
    output: FileSystem,
) : BuildTemplate(
    metadata = metadata,
    allJars = inputJars,
    inputs = inputDirectories,
    output = output,
) {
    // Map containing all file changes
    private lateinit var fileChangeMap: Map<String, InputFileDetails>

    private fun removeDeletedEntries() {
        inputs.forEach { directory ->
            val dirPath: String = directory.asFile.absolutePath

            incrementalInputChanges.allFileChanges
                .filter { details ->
                    details as DefaultFileChange
                    details.isRemoved
                }
                .map { it.file }
                .filter { file -> file.absolutePath.endsWith(DOT_CLASS) }
                .filter { file -> file.startsWith(dirPath) }
                .map { it.categorize(dirPath) }
                .forEach { path ->
                    // We need to transform platform paths into consistent zip entry paths
                    // https://github.com/realm/realm-java/issues/7757
                    val zipEntryPath = "${path.replace('.', '/')}.class"

                    logger.debug("Deleting output entry: $zipEntryPath")
                    output.getPath(zipEntryPath).deleteIfExists()
                }
        }
    }

    private fun processDeltas() {
        fileChangeMap = incrementalInputChanges
            .allFileChanges.associateBy { details ->
                details as DefaultFileChange
                details.path
            }

        // we require to delete any removed entry from the final JAR
        removeDeletedEntries()
    }

    override fun prepareOutputClasses() {
        processDeltas()

        outputClassNames = categorizeClassNames()

        logger.debug("Incremental build. Files being processed: ${outputClassNames.size}.")
        logger.debug("Incremental files: ${outputClassNames.joinToString(",")}")
    }

    override fun File.shouldCategorize(): Boolean = absolutePath in fileChangeMap

    override fun filterForModelClasses(
        outputClassNames: Set<String>,
        outputReferencedClassNames: Set<String>
    ) {
        outputModelClasses = findModelClasses(outputClassNames)
    }

    override fun transformDirectAccessToModelFields() {
        // Use accessors instead of direct field access
        outputClassNames.forEach { className ->
            logger.debug("Modify accessors in class: $className")
            val ctClass: CtClass = classPool.getCtClass(className)
            BytecodeModifier.useRealmAccessors(classPool, ctClass)
            processedClasses[className] = ctClass
        }
    }

    override fun findModelClasses(classNames: Set<String>): List<CtClass> {
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
                it.hasAnnotation(RealmClass::class.java) || hasRealmClassAnnotation(it.superclass)
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
