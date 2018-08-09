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
package io.realm.buildtransformer

import com.android.build.api.transform.*
import com.google.common.collect.ImmutableSet
import io.realm.buildtransformer.asm.ClassPoolTransformer
import io.realm.buildtransformer.ext.packageHierarchyRootDir
import io.realm.buildtransformer.ext.shouldBeDeleted
import io.realm.buildtransformer.util.Stopwatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

// Type aliases for improving readability
typealias ByteCodeTypeDescriptor = String
typealias QualifiedName = String
typealias ByteCodeMethodName = String

// Package level logger
val logger: Logger = LoggerFactory.getLogger("realm-build-logger")

/**
 * Transformer that will strip all classes, methods and fields annotated with a given annotation from
 * a specific Android flavour. It is also possible to provide a list of files to delete whether or
 * not they have the annotation. These files will only be deleted from the defined flavour.
 */
class RealmBuildTransformer(private val flavorToStrip: String,
                            private val annotationQualifiedName: QualifiedName,
                            private val specificFilesToStrip: Set<QualifiedName> = setOf()) : Transform() {

    override fun getName(): String {
        return "RealmBuildTransformer"
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return mutableSetOf(QualifiedContent.Scope.PROJECT)
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return setOf<QualifiedContent.ContentType>(QualifiedContent.DefaultContentType.CLASSES)
    }

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> {
        return ImmutableSet.of()
    }

    override fun transform(context: Context?,
                           inputs: MutableCollection<TransformInput>?,
                           referencedInputs: MutableCollection<TransformInput>?,
                           outputProvider: TransformOutputProvider?,
                           isIncremental: Boolean) {
        @Suppress("DEPRECATION")
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)

        // Poor mans version of detecting variants, since the Gradle API does not allow us to
        // register a transformer for only a single build variant
        // https://issuetracker.google.com/issues/37072849
        val transformClasses: Boolean = context?.variantName?.startsWith(flavorToStrip.toLowerCase()) == true

        val timer = Stopwatch()
        timer.start("Build Transform time")
        if (isIncremental) {
            runIncrementalTransform(inputs!!, outputProvider!!, transformClasses)
        } else {
            runFullTransform(inputs!!, outputProvider!!, transformClasses)
        }
        timer.stop()
    }

    private fun runFullTransform(inputs: MutableCollection<TransformInput>, outputProvider: TransformOutputProvider, transformClasses: Boolean) {
        logger.debug("Run full transform")
        val outputDir = outputProvider.getContentLocation("realmlibrarytransformer", outputTypes, scopes, Format.DIRECTORY)
        val inputFiles: MutableSet<File> = mutableSetOf()
        inputs.forEach {
            it.directoryInputs.forEach {
                // Non-incremental build: Include all files
                val dirPath: String = it.file.absolutePath
                it.file.walkTopDown()
                        .filter { it.isFile }
                        .filter { it.name.endsWith(".class") }
                        .forEach { file ->
                            file.packageHierarchyRootDir = dirPath
                            file.shouldBeDeleted = transformClasses && specificFilesToStrip.find { file.absolutePath.endsWith(it) } != null
                            inputFiles.add(file)
                        }
            }
        }

        transformClassFiles(outputDir, inputFiles, transformClasses)
    }

    private fun runIncrementalTransform(inputs: MutableCollection<TransformInput>, outputProvider: TransformOutputProvider, transformClasses: Boolean) {
        logger.debug("Run incremental transform")
        val outputDir = outputProvider.getContentLocation("realmlibrarytransformer", outputTypes, scopes, Format.DIRECTORY)
        val inputFiles: MutableSet<File> = mutableSetOf()
        inputs.forEach {
            it.directoryInputs.forEach iterateDirs@{
                if (!it.file.exists()) {
                    return@iterateDirs // Directory was deleted
                }
                val dirPath: String = it.file.absolutePath
                it.changedFiles.entries
                        .filter { it.key.isFile }
                        .filter { it.key.name.endsWith(".class") }
                        .filterNot { it.value == Status.REMOVED }
                        .forEach {
                            val file: File = it.key
                            file.packageHierarchyRootDir = dirPath
                            file.shouldBeDeleted = transformClasses && specificFilesToStrip.find { file.absolutePath.endsWith(it) } != null
                            inputFiles.add(file)
                        }
            }
        }

        transformClassFiles(outputDir, inputFiles, transformClasses)
    }

    private fun transformClassFiles(outputDir: File, inputFiles: MutableSet<File>, transformClasses: Boolean) {
        val files: Set<File> = if (transformClasses) {
            val transformer = ClassPoolTransformer(annotationQualifiedName, inputFiles)
            transformer.transform()
        } else {
            inputFiles
        }

        copyToOutput(outputDir, files)
    }

    private fun copyToOutput(outputDir: File, files: Set<File>) {
        files.forEach {
            val outputFile = File(outputDir, it.absolutePath.substring(it.packageHierarchyRootDir.length))
            if (it.shouldBeDeleted) {
                if (outputFile.exists()) {
                    outputFile.delete()
                }
            } else {
                it.copyTo(outputFile, overwrite = true)
            }
        }
    }

}
