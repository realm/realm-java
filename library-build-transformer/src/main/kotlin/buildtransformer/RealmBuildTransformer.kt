package buildtransformer

import buildtransformer.asm.ClassTransformer
import com.android.SdkConstants
import com.android.build.api.transform.*
import com.google.common.collect.ImmutableSet
import buildtransformer.util.Stopwatch
import java.io.File

/**
 * Transformer that strips all classes, methods and fields annotated with a given annotation.
 */
class RealmBuildTransformer : Transform() {

    override fun getName() : String {
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

        val timer = Stopwatch()
        timer.start("Build Transform time")

        inputs?.forEach {
            it.directoryInputs.forEach {
                val dirPath: String = it.file.absolutePath
                // Non-incremental build: Include all files
                val filesToDelete: MutableList<File> = mutableListOf()
                it.file.walkTopDown().forEach {
                    if (it.isFile) {
                        if (it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                            val transformer = ClassTransformer(it, "io.realm.internal.ObjectServer")
                            transformer.transform();
                            if (transformer.isClassRemoved()) {
                                filesToDelete.add(it)
                            }
                        }
                    }
                }
                filesToDelete.forEach { it.delete() }
            }
        }
        timer.stop()
    }

}
