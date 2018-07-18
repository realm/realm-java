package io.realm.buildtransformer.asm

import io.realm.buildtransformer.ByteCodeMethodName
import io.realm.buildtransformer.ByteCodeTypeDescriptor
import io.realm.buildtransformer.QualifiedName
import io.realm.buildtransformer.asm.visitors.AnnotatedCodeStripVisitor
import io.realm.buildtransformer.asm.visitors.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File

/**
 * Transformer that will transform a pool of classes by removing all classes, methods and fields annotated with
 * a given annotation.
 *
 * It does so in 2 parses using the ASM Visitor API. The first parse will gather metadata about the class hierarchy,
 * the 2nd parse will do the actual transform. The ASM Tree API was considered as well, but it does not provide
 * access to referenced classes easily, so two parses would be required here as well and the Visitor API is faster and
 * requires less memory.
 */
class ClassPoolTransformer(annotationQualifiedName: QualifiedName, private val inputClasses: Set<File>) {

    private val annotationDescriptor: String = createDescriptor(annotationQualifiedName)

    /**
     * Transform files
     *
     * @return All input files that where not removed.
     */
    fun transform(): Set<File> {
        val (markedClasses, markedMethods) = parse1()
        return parse2(markedClasses, markedMethods)
    }

    /**
     * Parse 1: Collect all classes, interfaces and enums that contain the given annotation. This include both top-level
     * and inner types.
     */
    private fun parse1(): Pair<Set<String>, Map<ByteCodeTypeDescriptor, Set<ByteCodeMethodName>>> {
        val metadataCollector = AnnotationVisitor(annotationDescriptor)
        inputClasses.forEach {
            it.inputStream().use {
                val classReader = ClassReader(it)
                classReader.accept(metadataCollector, 0)
            }
        }
        return Pair(metadataCollector.annotatedClasses, metadataCollector.annotatedMethods)
    }

    /**
     * Parse 2: Remove all testclasses, methods and fields marked with the annotation
     */
    private fun parse2(markedClasses: Set<String>, markedMethods: Map<ByteCodeTypeDescriptor, Set<ByteCodeMethodName>>): Set<File> {
        val modifiedClasses: MutableSet<File> = mutableSetOf()
        inputClasses.forEach { classFile ->
            var result = ByteArray(0)
            classFile.inputStream().use { inputStream ->
                val writer = ClassWriter(0) // We don't modify methods so no reason to re-calculate method frames
                val classRemover = AnnotatedCodeStripVisitor(annotationDescriptor, markedClasses, markedMethods, writer)
                val reader = ClassReader(inputStream)
                reader.accept(classRemover, 0)
                result = if (classRemover.deleteClass) ByteArray(0) else writer.toByteArray()
            }
            if (result.isNotEmpty()) {
                classFile.outputStream().use { outputStream -> outputStream.write(result) }
                modifiedClasses.add(classFile)
            }
        }
        return modifiedClasses
    }

    /**
     * Creates the descriptor used by ASM to identify types.
     */
    private fun createDescriptor(qualifiedName: String): String {
        return "L${qualifiedName.replace(".", "/")};"
    }
}