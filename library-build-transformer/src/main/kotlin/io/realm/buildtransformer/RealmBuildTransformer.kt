package io.realm.buildtransformer

import com.android.SdkConstants
import com.android.build.api.transform.*
import com.google.common.collect.ImmutableSet
import io.realm.buildtransformer.asm.ClassTransformer
import io.realm.buildtransformer.util.Stopwatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File


// Package level logger
val logger: Logger = LoggerFactory.getLogger("realm-logger")


/**
 * Transformer that strips all classes, methods and fields annotated with a given annotation.
 */
class RealmBuildTransformer : Transform() {

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
        val transformClasses: Boolean = context?.variantName?.startsWith("base") == true

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
        inputs.forEach {
            it.directoryInputs.forEach {
                // Non-incremental build: Include all files
                val dirPath: String = it.file.absolutePath
                it.file.walkTopDown().forEach {
                    if (it.isFile) {
                        val outputFile = File(outputDir, it.absolutePath.substring(dirPath.length))
                        var copyFile = true
                        if (it.absolutePath.endsWith(SdkConstants.DOT_CLASS) && transformClasses) {
                            logger.debug("Transform: ${it.name}")
                            val transformer = ClassTransformer(it, "io.realm.internal.ObjectServer")
                            transformer.transform()
                            copyFile = !transformer.isClassRemoved()
                        }
                        if (copyFile) {
                            it.copyTo(outputFile, overwrite = true)
                        }
                    }
                }
            }
        }
    }

    private fun runIncrementalTransform(inputs: MutableCollection<TransformInput>, outputProvider: TransformOutputProvider, transformClasses: Boolean) {
        logger.debug("Run incremental transform")
        val outputDir = outputProvider.getContentLocation("realmlibrarytransformer", outputTypes, scopes, Format.DIRECTORY)
        inputs.forEach {
            it.directoryInputs.forEach {
                val dirPath: String = it.file.absolutePath
                it.changedFiles.entries.forEach {
                    if (it.value == Status.REMOVED) {
                        return@forEach
                    }
                    val inputFile = it.key
                    val outputFile = File(outputDir, inputFile.absolutePath.substring(dirPath.length))
                    var copyFile = true
                    if (it.value != Status.NOTCHANGED && inputFile.absolutePath.endsWith(SdkConstants.DOT_CLASS) && transformClasses) {
                        logger.info("Transform: ${inputFile.name}")
                        val transformer = ClassTransformer(inputFile, "io.realm.internal.ObjectServer")
                        transformer.transform()
                        copyFile = !transformer.isClassRemoved()
                    }
                    if (copyFile) {
                        inputFile.copyTo(outputFile, overwrite = true)
                    }
                }
            }
        }
    }
}
