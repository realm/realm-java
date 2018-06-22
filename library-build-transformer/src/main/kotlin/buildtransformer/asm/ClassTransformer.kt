package buildtransformer.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File

/**
 * Class that reads the byte code of a class and transforms it by removing everything marked with the provided
 * annotation. If the entire class is removed, the file should be deleted on disk. This is not the responsibility of
 * this class
 */
class ClassTransformer(private val classFile: File, qualifiedAnnotationName: String) {

    private var classRemoved: Boolean = false
    private val annotationDescription: String = createDescription(qualifiedAnnotationName)
    private val classLoader: ClassLoader = this::class.java.classLoader

    /**
     * Converts a fully qualified name to the description format used by the Java byte code
     */
    private fun createDescription(qualifiedAnnotationName: String): String {
        return "L${qualifiedAnnotationName.replace(".", "/")};"
    }

    fun transform(): ByteArray {
        classFile.inputStream().use {

            // Read the file into the Tree API from ASM (ClassNode)
            val classNode = ClassNode()
            val classReader = ClassReader(it)
            classReader.accept(classNode, 0)

            // Check if the class itself is marked with an annotation, in that case the entire file should be deleted
            classNode.invisibleAnnotations?.forEach {
                if (it.desc == annotationDescription) {
                    classRemoved = true
                    return@use ByteArray(0)
                }
            }

            // Remove all fields with the annotation
            classNode.fields?.removeAll {
                it.invisibleAnnotations?.forEach {
                    if (it.desc == annotationDescription) {
                        return@removeAll true
                    }
                }
                return@removeAll false
            }

            // Remove all methods with the annotation
            classNode.methods?.removeAll {
                it.invisibleAnnotations?.forEach {
                    if (it.desc == annotationDescription) {
                        return@removeAll true
                    }
                }
                return@removeAll false
            }

            // Remove all inner classes, interfaces and enums with the annotation
            // TODO: Know limitation

            // Write the modified file back again
            val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
            classNode.accept(writer)
            return writer.toByteArray()
        }
        return ByteArray(0)
    }

    fun isClassRemoved(): Boolean {
        return false
    }
}