package io.realm.buildtransformer

import com.android.build.api.transform.*
import com.google.common.collect.ImmutableSet
import io.realm.buildtransformer.asm.ClassPoolTransformer
import io.realm.buildtransformer.ext.packageHierarchyRootDir
import io.realm.buildtransformer.util.Stopwatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

// Type alias' for improving readability
typealias ByteCodeTypeDescriptor = String
typealias QualifiedName = String
typealias ByteCodeMethodName = String

// Package level logger
val logger: Logger = LoggerFactory.getLogger("realm-logger")

/**
 * Transformer that strips all testclasses, methods and fields annotated with a given annotation.
 */
class RealmBuildTransformer(private val flavorToStrip: String, private val annotationQualifiedName: QualifiedName) : Transform() {

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
                        .forEach {
                            it.packageHierarchyRootDir = dirPath
                            inputFiles.add(it)
                        }
            }
        }

        transformClassFiles(inputFiles, outputDir)
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
                        .filterNot { it.value == Status.REMOVED }
                        .forEach {
                            val file: File = it.key
                            file.packageHierarchyRootDir = dirPath
                            inputFiles.add(file)
                        }
            }
        }

        transformClassFiles(inputFiles, outputDir)
    }

    private fun transformClassFiles(inputFiles: MutableSet<File>, outputDir: File?) {
        val transformer = ClassPoolTransformer("io.realm.internal.ObjectServer", inputFiles)
        val modifiedFiles = transformer.transform()
        modifiedFiles.forEach {
            // Deleted files will not be part of `modifiedFiles`, so by not copying them to the output dir they
            // are effectively deleted
            val outputFile = File(outputDir, it.absolutePath.substring(it.packageHierarchyRootDir.length))
            it.copyTo(outputFile, overwrite = true)
            logger.debug("Write file to output: ${outputFile.name}")
        }
    }

}
